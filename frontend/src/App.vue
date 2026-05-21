<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { apiRequest, getDefaultBaseUrl, unwrapCollection } from './api';
import { relationSources, resourceConfigs } from './resourceConfig';

const baseUrl = ref(localStorage.getItem('sw-base-url') || getDefaultBaseUrl());
const apiKey = ref(localStorage.getItem('sw-api-key') || '');
const keyOwner = ref('Professor');
const activeResource = ref('films');
const filmVersion = ref('1');
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

const relationOptions = reactive({
  planets: [],
  people: [],
  species: [],
  starships: []
});

const form = reactive({});

function cloneDefaults(resource) {
  return JSON.parse(JSON.stringify(resourceConfigs[resource].defaults));
}

function syncForm(resource, source) {
  const defaults = cloneDefaults(resource);
  Object.keys(form).forEach((key) => delete form[key]);
  Object.assign(form, defaults);

  if (!source) return;

  const config = resourceConfigs[resource];
  config.fields.forEach((field) => {
    const value = source[field.name];
    if (field.type === 'relation') {
      form[field.name] = value?.id ?? '';
      return;
    }
    if (field.type === 'multiRelation') {
      form[field.name] = Array.isArray(value) ? value.map((entry) => entry.id).filter(Boolean) : [];
      return;
    }
    form[field.name] = value ?? defaults[field.name];
  });
}

function persistSettings() {
  localStorage.setItem('sw-base-url', baseUrl.value);
  localStorage.setItem('sw-api-key', apiKey.value);
}

const currentConfig = computed(() => resourceConfigs[activeResource.value]);
const searchModes = computed(() => currentConfig.value.searchModes || []);
const summaryFields = computed(() => currentConfig.value.summaryFields || []);
const formFields = computed(() => currentConfig.value.fields || []);
const fieldLabels = computed(() =>
  Object.fromEntries((currentConfig.value.fields || []).map((field) => [field.name, field.label]))
);
const filmV2Columns = [
  { key: 'title', label: 'Título' },
  { key: 'episodeId', label: 'Episódio' },
  { key: 'director', label: 'Diretor' },
  { key: 'releaseDate', label: 'Lançamento' },
  { key: 'characterCount', label: 'Personagens' },
  { key: 'starshipCount', label: 'Naves' }
];
const title = computed(() => currentConfig.value.label);
const subtitle = computed(() => currentConfig.value.subtitle || '');
const versionLabel = computed(() => (activeResource.value === 'films' ? `Filmes v${filmVersion.value}` : title.value));
const hasApiKey = computed(() => Boolean(apiKey.value.trim()));
const connected = computed(() => Boolean(baseUrl.value.trim()));
const headerMode = computed(() => activeResource.value === 'films' && filmVersion.value === '2' ? 'v2' : 'v1');

function getSelectedVersion() {
  return activeResource.value === 'films' && filmVersion.value === '2' ? '2' : '';
}

function isFilmV2() {
  return activeResource.value === 'films' && filmVersion.value === '2';
}

function makeEmptyForm() {
  selected.value = null;
  syncForm(activeResource.value, null);
}

function badgeText(item) {
  if (!item) return 'Sem vínculo';
  return item.name || item.title || `#${item.id}`;
}

function prettyValue(value) {
  if (Array.isArray(value)) {
    return value.map(badgeText).join(', ') || '—';
  }
  if (value && typeof value === 'object') {
    return value.name || value.title || value.key || `#${value.id || 'objeto'}`;
  }
  if (value === undefined || value === null || value === '') return '—';
  return String(value);
}

function formatSummary(item, fieldName) {
  return prettyValue(item?.[fieldName]);
}

function labelForField(fieldName) {
  return fieldLabels.value[fieldName] || fieldName;
}

async function loadRelationOptions(resourceKey) {
  try {
    const response = await apiRequest(baseUrl.value, resourceConfigs[relationSources[resourceKey].resource].path, {
      params: { page: 0, size: 200 }
    });
    relationOptions[resourceKey] = unwrapCollection(response).items;
  } catch {
    relationOptions[resourceKey] = [];
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
    const path = searchValue.value.trim() && mode ? `${config.path}${mode.endpoint}` : config.path;
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
    if (!selected.value && items.value.length) {
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
    return Array.isArray(value) ? value.filter(Boolean).map((id) => ({ id: Number(id) })) : [];
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
    const path = mode === 'create'
      ? currentConfig.value.path
      : `${currentConfig.value.path}/${selected.value?.id}`;
    const response = await apiRequest(baseUrl.value, path, {
      method: mode === 'create' ? 'POST' : 'PUT',
      body: buildPayload(),
      apiKey: apiKey.value.trim(),
      idempotencyKey: mode === 'create' ? crypto.randomUUID() : '',
      version: getSelectedVersion()
    });
    selected.value = response;
    await loadList();
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
  syncForm(activeResource.value, null);
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

watch(baseUrl, persistSettings);
watch(apiKey, persistSettings);
watch(filmVersion, () => {
  if (activeResource.value === 'films') {
    selected.value = null;
    loadList();
  }
});

onMounted(async () => {
  syncForm(activeResource.value, null);
  await loadAllRelationOptions();
  await loadList();
});
</script>

<template>
  <div class="dashboard">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand__mark">SW</span>
        <div>
          <strong>Painel Administrativo</strong>
          <span>API Star Wars</span>
        </div>
      </div>

      <div class="status-block">
        <div>
          <span>Conexão</span>
          <strong>{{ connected ? 'Ativa' : 'Sem URL' }}</strong>
        </div>
        <div>
          <span>Autorização</span>
          <strong>{{ hasApiKey ? 'Configurada' : 'Ausente' }}</strong>
        </div>
      </div>

      <div class="stacked-form">
        <label>
          <span>URL da API</span>
          <input v-model="baseUrl" type="text" placeholder="https://..." />
        </label>
        <label>
          <span>Chave de API</span>
          <input v-model="apiKey" type="password" placeholder="X-API-Key" />
        </label>
        <label>
          <span>Responsável da chave</span>
          <input v-model="keyOwner" type="text" placeholder="Professor" />
        </label>
      </div>

      <div class="nav-group">
        <button
          v-for="resource in Object.keys(resourceConfigs)"
          :key="resource"
          class="nav-button"
          :class="{ active: activeResource === resource }"
          @click="switchResource(resource)"
        >
          {{ resourceConfigs[resource].label }}
        </button>
      </div>

      <div class="sidebar-foot">
        <div v-if="activeResource === 'films'" class="segmented-block">
          <span>Versão dos filmes</span>
          <div class="segmented">
            <button :class="{ active: filmVersion === '1' }" @click="filmVersion = '1'">V1</button>
            <button :class="{ active: filmVersion === '2' }" @click="filmVersion = '2'">V2</button>
          </div>
        </div>
      </div>
    </aside>

    <main class="main">
      <header class="topbar">
        <div>
          <p class="eyebrow">Sistema de administração</p>
          <h1>{{ versionLabel }}</h1>
          <p class="subtitle">{{ subtitle || 'Controle de registros, vínculos e chaves de acesso.' }}</p>
        </div>
        <div class="topbar__meta">
          <div class="meta-card">
            <span>Registros</span>
            <strong>{{ pageInfo.totalElements || items.length }}</strong>
          </div>
          <div class="meta-card">
            <span>Página</span>
            <strong>{{ (pageInfo.number ?? 0) + 1 }}</strong>
          </div>
          <div class="meta-card">
            <span>Modo</span>
            <strong>{{ headerMode }}</strong>
          </div>
        </div>
      </header>

      <section class="workspace">
        <section class="panel list-panel">
          <div class="panel__head">
            <div>
              <h2>{{ title }}</h2>
              <p>{{ currentConfig.description || 'Listagem paginada com busca e seleção.' }}</p>
            </div>
            <div class="panel__actions">
              <button @click="loadList">Atualizar</button>
            </div>
          </div>

          <div v-if="searchModes.length && !isFilmV2()" class="searchbar">
            <label>
              <span>Filtro</span>
              <select v-model="searchMode">
                <option v-for="mode in searchModes" :key="mode.key" :value="mode.key">
                  {{ mode.label }}
                </option>
              </select>
            </label>
            <label class="searchbar__value">
              <span>Texto</span>
              <input
                v-model="searchValue"
                type="text"
                :placeholder="`Buscar ${title.toLowerCase()}`"
                @keyup.enter="loadList"
              />
            </label>
            <button class="primary" @click="loadList">Filtrar</button>
          </div>

          <div v-if="isFilmV2()" class="notice">
            V2 somente leitura. Use a V1 para criar, editar e excluir.
          </div>

          <div class="table-wrap">
            <table class="data-table">
              <thead>
              <tr>
                  <th>ID</th>
                  <th
                    v-for="field in isFilmV2() ? filmV2Columns : summaryFields"
                    :key="isFilmV2() ? field.key : field"
                  >
                    {{ isFilmV2() ? field.label : labelForField(field) }}
                  </th>
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
                  <template v-if="isFilmV2()">
                    <td>{{ item.title }}</td>
                    <td>{{ item.episodeId }}</td>
                    <td>{{ item.director }}</td>
                    <td>{{ item.releaseDate }}</td>
                    <td>{{ item.characterCount }}</td>
                    <td>{{ item.starshipCount }}</td>
                  </template>
                  <template v-else>
                    <td v-for="field in summaryFields" :key="field">{{ formatSummary(item, field) }}</td>
                  </template>
                </tr>
                <tr v-if="!items.length && !loading">
                  <td :colspan="isFilmV2() ? 7 : summaryFields.length + 1" class="empty-state">
                    Nenhum registro carregado.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="pager">
            <span>Página {{ (pageInfo.number ?? 0) + 1 }} de {{ pageInfo.totalPages || 1 }}</span>
            <div>
              <button :disabled="page <= 0" @click="previousPage">Anterior</button>
              <button :disabled="pageInfo.totalPages ? page + 1 >= pageInfo.totalPages : true" @click="nextPage">Próxima</button>
            </div>
          </div>
        </section>

        <section class="panel editor-panel">
          <div class="panel__head">
            <div>
              <h2>Detalhe e edição</h2>
              <p>JSON bruto acima, formulário abaixo.</p>
            </div>
            <div class="panel__actions">
              <button @click="makeEmptyForm">Novo</button>
              <button class="danger" :disabled="!selected?.id || isFilmV2()" @click="removeItem">Excluir</button>
            </div>
          </div>

          <pre class="json-box">{{ selected ? JSON.stringify(selected, null, 2) : 'Selecione um registro.' }}</pre>

          <form class="form-grid" @submit.prevent="submitForm(selected?.id ? 'update' : 'create')">
            <div v-for="field in formFields" :key="field.name" class="field">
              <label :for="field.name">{{ field.label }}</label>
              <input
                v-if="field.type === 'text' || field.type === 'number'"
                :id="field.name"
                v-model="form[field.name]"
                :type="field.type === 'number' ? 'number' : 'text'"
                :required="field.required"
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
                <option value="">Sem vínculo</option>
                <option v-for="option in relationOptions[field.source]" :key="option.id" :value="option.id">
                  #{{ option.id }} - {{ badgeText(option) }}
                </option>
              </select>
              <select v-else-if="field.type === 'multiRelation'" :id="field.name" v-model="form[field.name]" multiple size="5">
                <option v-for="option in relationOptions[field.source]" :key="option.id" :value="option.id">
                  #{{ option.id }} - {{ badgeText(option) }}
                </option>
              </select>
            </div>

            <div class="form-actions">
              <button class="primary" type="submit" :disabled="isFilmV2()">
                {{ selected?.id ? 'Salvar alteração' : 'Criar registro' }}
              </button>
            </div>
          </form>
        </section>
      </section>

      <section class="panel key-panel">
        <div class="panel__head">
          <div>
            <h2>Chave de acesso</h2>
            <p>Gerar e revogar chaves da API.</p>
          </div>
        </div>

        <div class="key-grid">
          <div class="key-card">
            <span>Responsável</span>
            <input v-model="keyOwner" type="text" placeholder="Professor" />
          </div>
          <div class="key-actions">
            <button class="primary" @click.prevent="createApiKey">Gerar chave</button>
            <button :disabled="!latestCreatedKey?.id" @click.prevent="revokeLatestKey">Revogar última</button>
          </div>
        </div>

        <pre class="json-box">
{{ latestCreatedKey ? JSON.stringify(latestCreatedKey, null, 2) : 'Nenhuma chave gerada nesta sessão.' }}
        </pre>
      </section>

      <section v-if="errorMessage" class="error-strip">
        {{ errorMessage }}
      </section>
    </main>
  </div>
</template>
