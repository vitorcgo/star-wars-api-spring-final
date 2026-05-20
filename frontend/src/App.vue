<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { apiRequest, getDefaultBaseUrl, unwrapCollection } from './api';
import { relationSources, resourceConfigs } from './resourceConfig';

const baseUrl = ref(localStorage.getItem('sw-base-url') || getDefaultBaseUrl());
const apiKey = ref(localStorage.getItem('sw-api-key') || '');
const activeResource = ref('films');
const searchMode = ref(resourceConfigs.films.defaultSearchMode);
const searchValue = ref('');
const page = ref(0);
const size = ref(8);
const items = ref([]);
const pageInfo = ref({ number: 0, totalPages: 0, totalElements: 0 });
const loading = ref(false);
const errorMessage = ref('');
const selected = ref(null);
const latestCreatedKey = ref(null);
const keyOwner = ref('Professor');
const relationOptions = reactive({
  planets: [],
  people: [],
  species: [],
  starships: []
});
const relationLoading = reactive({
  planets: false,
  people: false,
  species: false,
  starships: false
});
const filmVersion = ref('1');

const form = reactive({});

function cloneDefaults(resource) {
  return JSON.parse(JSON.stringify(resourceConfigs[resource].defaults));
}

function syncForm(resource, source) {
  const defaults = cloneDefaults(resource);
  Object.keys(form).forEach((key) => delete form[key]);
  Object.assign(form, defaults);
  if (source) {
    const config = resourceConfigs[resource];
    config.fields.forEach((field) => {
      const value = source[field.name];
      if (field.type === 'relation') {
        form[field.name] = value?.id ?? '';
      } else if (field.type === 'multiRelation') {
        form[field.name] = Array.isArray(value) ? value.map((entry) => entry.id).filter(Boolean) : [];
      } else if (field.type === 'enum') {
        form[field.name] = value || defaults[field.name];
      } else {
        form[field.name] = value ?? defaults[field.name];
      }
    });
  }
}

function persistSettings() {
  localStorage.setItem('sw-base-url', baseUrl.value);
  localStorage.setItem('sw-api-key', apiKey.value);
}

const currentConfig = computed(() => resourceConfigs[activeResource.value]);
const searchModes = computed(() => currentConfig.value.searchModes || []);
const versionable = computed(() => Boolean(currentConfig.value.versionable));
const summaryFields = computed(() => currentConfig.value.summaryFields || []);
const formFields = computed(() => currentConfig.value.fields || []);
const title = computed(() => currentConfig.value.label);
const canWrite = computed(() => Boolean(apiKey.value.trim()));

function makeEmptyForm() {
  selected.value = null;
  syncForm(activeResource.value, null);
}

function resourceLabelForKey(key) {
  return relationSources[key]?.label || key;
}

async function loadRelationOptions(resourceKey) {
  relationLoading[resourceKey] = true;
  try {
    const response = await apiRequest(baseUrl.value, resourceConfigs[relationSources[resourceKey].resource].path, {
      params: { page: 0, size: 200 },
      version: relationSources[resourceKey].resource === 'films' && filmVersion.value === '2' ? '2' : ''
    });
    relationOptions[resourceKey] = unwrapCollection(response).items;
  } catch {
    relationOptions[resourceKey] = [];
  } finally {
    relationLoading[resourceKey] = false;
  }
}

async function loadAllRelationOptions() {
  await Promise.all([
    loadRelationOptions('planets'),
    loadRelationOptions('people'),
    loadRelationOptions('species'),
    loadRelationOptions('starships')
  ]);
}

function getSelectedVersion() {
  return activeResource.value === 'films' && filmVersion.value === '2' ? '2' : '';
}

function buildSearchParams() {
  const params = { page: page.value, size: size.value };
  if (activeResource.value === 'films' && filmVersion.value === '2') {
    return { params, mode: null };
  }
  const mode = searchModes.value.find((item) => item.key === searchMode.value) || searchModes.value[0];
  if (mode && searchValue.value.trim()) {
    params[mode.key] = searchValue.value.trim();
  }
  return { params, mode };
}

async function loadList() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const config = currentConfig.value;
    const { params, mode } = buildSearchParams();
    let path = config.path;

    if (searchValue.value.trim() && mode) {
      path = `${config.path}${mode.endpoint}`;
    }

    const response = await apiRequest(baseUrl.value, path, {
      params,
      version: getSelectedVersion()
    });

    const collection = unwrapCollection(response);
    items.value = collection.items;
    pageInfo.value = collection.page || {
      number: response.currentPage ?? 0,
      totalPages: response.totalPages ?? 1,
      totalElements: response.totalElements ?? collection.items.length
    };
    if (items.value.length > 0 && !selected.value) {
      selected.value = items.value[0];
      syncForm(activeResource.value, items.value[0]);
    }
  } catch (error) {
    errorMessage.value = error.message;
    items.value = [];
    pageInfo.value = { number: 0, totalPages: 0, totalElements: 0 };
  } finally {
    loading.value = false;
  }
}

async function loadDetail(item) {
  if (!item?.id) return;
  selected.value = null;
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await apiRequest(baseUrl.value, `${currentConfig.value.path}/${item.id}`, {
      version: getSelectedVersion()
    });
    selected.value = response;
    syncForm(activeResource.value, response);
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

function serializeField(field) {
  const value = form[field.name];
  if (field.type === 'relation') {
    return value ? { id: Number(value) } : null;
  }
  if (field.type === 'multiRelation') {
    return Array.isArray(value)
      ? value.filter(Boolean).map((id) => ({ id: Number(id) }))
      : [];
  }
  if (field.type === 'number') {
    return value === '' || value === null ? null : Number(value);
  }
  return value;
}

function buildPayload() {
  const payload = {};
  currentConfig.value.fields.forEach((field) => {
    payload[field.name] = serializeField(field);
  });
  return payload;
}

async function submitForm(mode) {
  loading.value = true;
  errorMessage.value = '';
  try {
    const payload = buildPayload();
    const idempotencyKey = mode === 'create' ? crypto.randomUUID() : '';
    const path = mode === 'create'
      ? currentConfig.value.path
      : `${currentConfig.value.path}/${selected.value?.id}`;
    const method = mode === 'create' ? 'POST' : 'PUT';
    const response = await apiRequest(baseUrl.value, path, {
      method,
      body: payload,
      apiKey: apiKey.value.trim(),
      idempotencyKey,
      version: getSelectedVersion()
    });
    const entity = response?.id ? response : response?._embedded ? response : response;
    selected.value = entity;
    await loadList();
    if (entity?.id) {
      await loadDetail(entity);
    }
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function removeItem() {
  if (!selected.value?.id) return;
  if (!window.confirm(`Remover ${title.value.toLowerCase()} #${selected.value.id}?`)) return;
  loading.value = true;
  errorMessage.value = '';
  try {
    await apiRequest(baseUrl.value, `${currentConfig.value.path}/${selected.value.id}`, {
      method: 'DELETE',
      apiKey: apiKey.value.trim(),
      version: getSelectedVersion()
    });
    selected.value = null;
    makeEmptyForm();
    await loadList();
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function createApiKey() {
  loading.value = true;
  errorMessage.value = '';
  try {
    latestCreatedKey.value = await apiRequest(baseUrl.value, '/api/auth/keys', {
      method: 'POST',
      body: { owner: keyOwner.value || 'Professor' }
    });
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function revokeLatestKey() {
  if (!latestCreatedKey.value?.id) return;
  loading.value = true;
  errorMessage.value = '';
  try {
    await apiRequest(baseUrl.value, `/api/auth/keys/${latestCreatedKey.value.id}`, {
      method: 'DELETE',
      apiKey: apiKey.value.trim()
    });
    latestCreatedKey.value.active = false;
  } catch (error) {
    errorMessage.value = error.message;
  } finally {
    loading.value = false;
  }
}

function switchResource(resourceKey) {
  activeResource.value = resourceKey;
  searchMode.value = resourceConfigs[resourceKey].defaultSearchMode || resourceConfigs[resourceKey].searchModes?.[0]?.key || '';
  searchValue.value = '';
  page.value = 0;
  selected.value = null;
  makeEmptyForm();
  loadList();
}

function previousPage() {
  if (page.value > 0) {
    page.value -= 1;
    loadList();
  }
}

function nextPage() {
  if (pageInfo.value && page.value + 1 < pageInfo.value.totalPages) {
    page.value += 1;
    loadList();
  }
}

function badgeText(item) {
  if (!item) return 'None';
  return item.name || item.title || `#${item.id}`;
}

function prettyValue(value) {
  if (Array.isArray(value)) {
    return value.map(badgeText).join(', ') || '—';
  }
  if (value && typeof value === 'object') {
    return value.name || value.title || value.key || `#${value.id || 'object'}`;
  }
  if (value === undefined || value === null || value === '') return '—';
  return String(value);
}

function formatSummary(item, fieldName) {
  return prettyValue(item?.[fieldName]);
}

watch(baseUrl, persistSettings);
watch(apiKey, persistSettings);
watch(filmVersion, () => {
  if (activeResource.value === 'films') {
    loadList();
    loadAllRelationOptions();
  }
});
watch(activeResource, () => {
  loadAllRelationOptions();
});

onMounted(async () => {
  syncForm(activeResource.value, null);
  await loadAllRelationOptions();
  await loadList();
});
</script>

<template>
  <div class="shell">
    <header class="hero">
      <div class="hero__glow"></div>
      <div class="hero__topline">
        <span class="eyebrow">Imperial command deck</span>
        <span class="status-pill" :class="{ ok: !loading, busy: loading }">
          {{ loading ? 'Processing' : 'Ready' }}
        </span>
      </div>
      <div class="hero__grid">
        <div>
          <h1>Star Wars API Control Center</h1>
          <p>
            Manage films, people, planets, species and starships through the live Spring Boot API.
          </p>
        </div>
        <div class="hero__stats">
          <div>
            <strong>{{ title }}</strong>
            <span>Active sector</span>
          </div>
          <div>
            <strong>{{ pageInfo.totalElements || items.length }}</strong>
            <span>Records visible</span>
          </div>
          <div>
            <strong>{{ latestCreatedKey?.key ? 'Issued' : 'Idle' }}</strong>
            <span>API key vault</span>
          </div>
        </div>
      </div>
    </header>

    <section class="toolbar">
      <label>
        <span>API base URL</span>
        <input v-model="baseUrl" type="text" placeholder="https://your-backend.vercel.app" />
      </label>
      <label>
        <span>X-API-Key</span>
        <input v-model="apiKey" type="password" placeholder="Paste your API key" />
      </label>
      <label>
        <span>Page size</span>
        <input v-model.number="size" type="number" min="1" max="100" @change="loadList" />
      </label>
      <div v-if="versionable" class="version-switch">
        <span>Film version</span>
        <div class="segmented">
          <button :class="{ active: filmVersion === '1' }" @click="filmVersion = '1'">v1</button>
          <button :class="{ active: filmVersion === '2' }" @click="filmVersion = '2'">v2</button>
        </div>
      </div>
    </section>

    <section class="resources">
      <button
        v-for="resource in Object.keys(resourceConfigs)"
        :key="resource"
        class="resource-tab"
        :class="{ active: activeResource === resource }"
        @click="switchResource(resource)"
      >
        {{ resourceConfigs[resource].label }}
      </button>
    </section>

    <section v-if="activeResource !== 'films' || filmVersion === '1'" class="workspace">
      <div class="panel list-panel">
        <div class="panel__head">
          <div>
            <h2>{{ title }} registry</h2>
            <p>{{ summaryFields.join(' · ') }}</p>
          </div>
          <div class="panel__actions">
            <button @click="loadList">Refresh</button>
          </div>
        </div>

        <div class="search-row" v-if="searchModes.length">
          <label>
            <span>Search mode</span>
            <select v-model="searchMode">
              <option v-for="mode in searchModes" :key="mode.key" :value="mode.key">
                {{ mode.label }}
              </option>
            </select>
          </label>
          <label class="search-input">
            <span>Search value</span>
            <input
              v-model="searchValue"
              type="text"
              :placeholder="`Search ${title.toLowerCase()}`"
              @keyup.enter="loadList"
            />
          </label>
          <button class="primary" @click="loadList">Search</button>
        </div>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th v-for="field in summaryFields" :key="field">{{ field }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in items"
                :key="item.id"
                :class="{ selected: selected?.id === item.id }"
                @click="loadDetail(item)"
              >
                <td>{{ item.id }}</td>
                <td v-for="field in summaryFields" :key="field">
                  {{ formatSummary(item, field) }}
                </td>
              </tr>
              <tr v-if="!items.length && !loading">
                <td :colspan="summaryFields.length + 1" class="empty-state">No records loaded.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pager">
          <span>Page {{ (pageInfo.number ?? 0) + 1 }} / {{ pageInfo.totalPages || 1 }}</span>
          <div>
            <button :disabled="page <= 0" @click="previousPage">Prev</button>
            <button :disabled="pageInfo.totalPages ? page + 1 >= pageInfo.totalPages : true" @click="nextPage">
              Next
            </button>
          </div>
        </div>
      </div>

      <div class="panel editor-panel">
        <div class="panel__head">
          <div>
            <h2>Record editor</h2>
            <p>Selected item, form and JSON payload.</p>
          </div>
          <div class="panel__actions">
            <button @click="makeEmptyForm">New</button>
            <button class="danger" :disabled="!selected?.id" @click="removeItem">Delete</button>
          </div>
        </div>

        <pre class="json-box">{{ selected ? JSON.stringify(selected, null, 2) : 'Select an item to inspect.' }}</pre>

        <form class="form-grid" @submit.prevent="submitForm(selected?.id ? 'update' : 'create')">
          <div v-for="field in formFields" :key="field.name" class="field">
            <label :for="field.name">{{ field.label }}</label>
            <input
              v-if="field.type === 'text' || field.type === 'number'"
              :id="field.name"
              v-model="form[field.name]"
              :type="field.type === 'number' ? 'number' : 'text'"
              :required="field.required"
              :placeholder="field.label"
            />
            <textarea
              v-else-if="field.type === 'textarea'"
              :id="field.name"
              v-model="form[field.name]"
              rows="4"
            ></textarea>
            <select v-else-if="field.type === 'enum'" :id="field.name" v-model="form[field.name]" :required="field.required">
              <option v-for="option in field.options" :key="option" :value="option">{{ option }}</option>
            </select>
            <select v-else-if="field.type === 'relation'" :id="field.name" v-model="form[field.name]">
              <option value="">None</option>
              <option v-for="option in relationOptions[field.source]" :key="option.id" :value="option.id">
                #{{ option.id }} - {{ badgeText(option) }}
              </option>
            </select>
            <select
              v-else-if="field.type === 'multiRelation'"
              :id="field.name"
              v-model="form[field.name]"
              multiple
              size="5"
            >
              <option v-for="option in relationOptions[field.source]" :key="option.id" :value="option.id">
                #{{ option.id }} - {{ badgeText(option) }}
              </option>
            </select>
          </div>

          <div class="submit-row">
            <button class="primary" type="submit">
              {{ selected?.id ? 'Update record' : 'Create record' }}
            </button>
          </div>
        </form>
      </div>
    </section>

    <section v-else class="workspace film-v2">
      <div class="panel list-panel">
        <div class="panel__head">
          <div>
            <h2>Films v2 feed</h2>
            <p>Compact payload by header X-API-Version: 2.</p>
          </div>
          <div class="panel__actions">
            <button @click="loadList">Refresh</button>
          </div>
        </div>

        <div class="search-row">
          <label>
            <span>Search mode</span>
            <select v-model="searchMode">
              <option value="title">Title</option>
              <option value="director">Director</option>
            </select>
          </label>
          <label class="search-input">
            <span>Search value</span>
            <input
              v-model="searchValue"
              type="text"
              placeholder="Search films"
              @keyup.enter="loadList"
            />
          </label>
          <button class="primary" @click="loadList">Search</button>
        </div>

        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Episode</th>
                <th>Director</th>
                <th>Release</th>
                <th>Characters</th>
                <th>Starships</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in items" :key="item.id" :class="{ selected: selected?.id === item.id }" @click="loadDetail(item)">
                <td>{{ item.id }}</td>
                <td>{{ item.title }}</td>
                <td>{{ item.episodeId }}</td>
                <td>{{ item.director }}</td>
                <td>{{ item.releaseDate }}</td>
                <td>{{ item.characterCount }}</td>
                <td>{{ item.starshipCount }}</td>
              </tr>
              <tr v-if="!items.length && !loading">
                <td colspan="7" class="empty-state">No film summaries loaded.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pager">
          <span>Page {{ (pageInfo.number ?? 0) + 1 }} / {{ pageInfo.totalPages || 1 }}</span>
          <div>
            <button :disabled="page <= 0" @click="previousPage">Prev</button>
            <button :disabled="pageInfo.totalPages ? page + 1 >= pageInfo.totalPages : true" @click="nextPage">
              Next
            </button>
          </div>
        </div>
      </div>

      <div class="panel editor-panel">
        <div class="panel__head">
          <div>
            <h2>Film editor</h2>
            <p>Use v1 endpoints for create/update/delete. v2 is read-only.</p>
          </div>
        </div>

        <pre class="json-box">{{ selected ? JSON.stringify(selected, null, 2) : 'Select a film to inspect.' }}</pre>
        <div class="empty-state">
          Film v2 is read-only. Switch back to v1 to create, update or delete films.
        </div>
      </div>
    </section>

    <section class="panel auth-panel">
      <div class="panel__head">
        <div>
          <h2>API key vault</h2>
          <p>Generate a key, copy it once and paste it in X-API-Key above.</p>
        </div>
      </div>

      <div class="auth-grid">
        <label>
          <span>Owner</span>
          <input v-model="keyOwner" type="text" placeholder="Professor" />
        </label>
        <div class="auth-actions">
          <button class="primary" @click.prevent="createApiKey">Generate key</button>
          <button :disabled="!latestCreatedKey?.id" @click.prevent="revokeLatestKey">Revoke latest</button>
        </div>
      </div>

      <pre class="json-box">
{{ latestCreatedKey ? JSON.stringify(latestCreatedKey, null, 2) : 'No key generated in this browser yet.' }}
      </pre>
    </section>

    <section v-if="errorMessage" class="error-strip">
      {{ errorMessage }}
    </section>
  </div>
</template>
