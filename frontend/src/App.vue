<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import {
  Activity,
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
  CircleAlert,
  Clock3,
  Database,
  FileKey2,
  Info,
  KeyRound,
  Layers3,
  ListFilter,
  Network,
  Plus,
  RefreshCw,
  Save,
  Search,
  ShieldCheck,
  Trash2,
  Unplug,
  X
} from 'lucide-vue-next';
import { apiRequest, getDefaultBaseUrl, unwrapCollection } from './api';
import { resourceConfigs } from './resourceConfig';

const resourceKeys = Object.keys(resourceConfigs);
const apiBaseUrl = ref(localStorage.getItem('api-base-url') || getDefaultBaseUrl());
const apiKey = ref(localStorage.getItem('api-key') || '');
const keyOwner = ref('Professor');
const activePage = ref('overview');
const activeResource = ref('films');
const resourceMode = ref('list');
const filmVersion = ref('1');
const searchMode = ref(resourceConfigs.films.defaultSearchMode);
const searchValue = ref('');
const page = ref(0);
const size = ref(10);
const rows = ref([]);
const selected = ref(null);
const pageInfo = ref({ number: 0, totalPages: 1, totalElements: 0 });
const loading = ref(false);
const errorMessage = ref('');
const latestCreatedKey = ref(null);
const activityLog = ref([]);
const relationOptions = reactive({ planets: [], people: [], species: [], starships: [] });
const form = reactive({});

const navigation = [
  { id: 'overview', label: 'Visão geral', icon: Activity },
  { id: 'films', label: 'Filmes', icon: Layers3, resource: 'films' },
  { id: 'people', label: 'Personagens', icon: Database, resource: 'people' },
  { id: 'planets', label: 'Planetas', icon: Network, resource: 'planets' },
  { id: 'species', label: 'Espécies', icon: ListFilter, resource: 'species' },
  { id: 'starships', label: 'Naves', icon: ShieldCheck, resource: 'starships' },
  { id: 'keys', label: 'Chaves', icon: KeyRound }
];

const filmV2Columns = [
  { key: 'title', label: 'Título' },
  { key: 'episodeId', label: 'Episódio' },
  { key: 'director', label: 'Diretor' },
  { key: 'releaseDate', label: 'Lançamento' },
  { key: 'characterCount', label: 'Personagens' },
  { key: 'starshipCount', label: 'Naves' }
];

const currentConfig = computed(() => resourceConfigs[activeResource.value]);
const isResourcePage = computed(() => Boolean(navigation.find((item) => item.id === activePage.value)?.resource));
const isFilmV2 = computed(() => activeResource.value === 'films' && filmVersion.value === '2');
const searchModes = computed(() => currentConfig.value.searchModes || []);
const summaryFields = computed(() => currentConfig.value.summaryFields || []);
const formFields = computed(() => currentConfig.value.fields || []);
const hiddenFormFields = new Set(['swapiUrl', 'openingCrawl', 'characters', 'starships']);
const visibleFormFields = computed(() =>
  formFields.value.filter((field) => !hiddenFormFields.has(field.name))
);
const fieldLabels = computed(() =>
  Object.fromEntries(formFields.value.map((field) => [field.name, field.label]))
);
const visibleColumns = computed(() => (isFilmV2.value ? filmV2Columns : summaryFields.value));
const apiOnline = computed(() => Boolean(apiBaseUrl.value.trim()));
const writeReady = computed(() => Boolean(apiKey.value.trim()));
const pageTitle = computed(() => {
  if (activePage.value === 'overview') return 'Visão geral';
  if (activePage.value === 'keys') return 'Chaves de acesso';
  if (resourceMode.value === 'form') return selected.value?.id ? `Editar ${currentConfig.value.label}` : `Novo ${currentConfig.value.label}`;
  return `${currentConfig.value.label}${isFilmV2.value ? ' V2' : ''}`;
});

const metrics = computed(() => [
  { label: 'API', value: apiOnline.value ? 'Configurada' : 'Sem URL', tone: apiOnline.value ? 'good' : 'warn' },
  { label: 'Autorização', value: writeReady.value ? 'Pronta' : 'Pendente', tone: writeReady.value ? 'good' : 'warn' },
  { label: 'Registros', value: pageInfo.value.totalElements || rows.value.length, tone: 'neutral' },
  { label: 'Versão', value: activeResource.value === 'films' ? `V${filmVersion.value}` : 'V1', tone: 'neutral' }
]);

const latestActivity = computed(() => activityLog.value[0] || null);
const formHint = computed(() => {
  if (isFilmV2.value) return 'A V2 é somente leitura. Volte para V1 para criar, editar ou excluir.';
  if (!writeReady.value) return 'Informe uma chave de API para salvar, editar ou excluir registros.';
  if (selected.value?.id) return 'Edite os campos necessários e salve. Campos técnicos e vínculos grandes ficam preservados.';
  return 'Preencha os campos obrigatórios e crie um novo registro.';
});

function cloneDefaults(resource) {
  return JSON.parse(JSON.stringify(resourceConfigs[resource].defaults));
}

function syncForm(resource, source) {
  const defaults = cloneDefaults(resource);
  Object.keys(form).forEach((key) => delete form[key]);
  Object.assign(form, defaults);
  if (!source) return;

  resourceConfigs[resource].fields.forEach((field) => {
    const value = source[field.name];
    if (field.type === 'relation') {
      form[field.name] = value?.id ?? '';
    } else if (field.type === 'multiRelation') {
      form[field.name] = Array.isArray(value) ? value.map((entry) => entry.id).filter(Boolean) : [];
    } else {
      form[field.name] = value ?? defaults[field.name];
    }
  });
}

function persistConnection() {
  localStorage.setItem('api-base-url', apiBaseUrl.value);
  localStorage.setItem('api-key', apiKey.value);
}

function pushActivity(type, title, detail = '') {
  activityLog.value.unshift({
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    type,
    title,
    detail,
    time: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  });
  activityLog.value = activityLog.value.slice(0, 6);
}

function showError(error, fallback = 'Erro inesperado.') {
  const message = friendlyError(error, fallback);
  errorMessage.value = message;
  pushActivity('error', 'Ação não concluída', message);
}

function selectedVersion() {
  return isFilmV2.value ? '2' : '';
}

function relationPath(key) {
  const map = { planets: 'planets', people: 'people', species: 'species', starships: 'starships' };
  return resourceConfigs[map[key]].path;
}

async function loadRelationOptions() {
  await Promise.all(
    Object.keys(relationOptions).map(async (key) => {
      try {
        const response = await apiRequest(apiBaseUrl.value, relationPath(key), {
          params: { page: 0, size: 200 }
        });
        relationOptions[key] = unwrapCollection(response).items;
      } catch {
        relationOptions[key] = [];
      }
    })
  );
}

function buildParams() {
  const params = { page: page.value, size: size.value };
  if (isFilmV2.value) return { params, mode: null };
  const mode = searchModes.value.find((item) => item.key === searchMode.value) || searchModes.value[0];
  if (mode && searchValue.value.trim()) params[mode.key] = searchValue.value.trim();
  return { params, mode };
}

function friendlyError(error, fallback = 'Erro inesperado.') {
  if (error?.message === 'Failed to fetch') {
    return 'Não foi possível conectar com a API. Verifique a URL, CORS e se o Render está ativo.';
  }
  if (error?.status === 401) {
    return 'Chave de API ausente. Gere ou cole uma chave antes de salvar alterações.';
  }
  if (error?.status === 403) {
    return 'Chave de API inválida ou revogada. Gere uma nova chave e tente novamente.';
  }
  if (error?.status === 404) {
    return 'Registro não encontrado. Atualize a listagem e tente novamente.';
  }
  if (error?.status === 409 || /integridade|constraint|dependente|viol/i.test(error?.message || '')) {
    return 'Conflito no banco. Verifique vínculos dependentes ou campos únicos já usados, como nave principal de personagem.';
  }
  if (error?.status === 400) {
    return 'Dados inválidos. Confira campos obrigatórios e valores de enum.';
  }
  return error?.message || fallback;
}

async function loadList() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const { params, mode } = buildParams();
    const path = searchValue.value.trim() && mode
      ? `${currentConfig.value.path}${mode.endpoint}`
      : currentConfig.value.path;
    const response = await apiRequest(apiBaseUrl.value, path, {
      params,
      version: selectedVersion()
    });
    const collection = unwrapCollection(response);
    rows.value = collection.items;
    pageInfo.value = collection.page || {
      number: response.currentPage ?? 0,
      totalPages: response.totalPages ?? 1,
      totalElements: response.totalElements ?? collection.items.length
    };
    if (!selected.value && rows.value.length) {
      selected.value = rows.value[0];
      syncForm(activeResource.value, rows.value[0]);
    }
    pushActivity('success', 'Listagem atualizada', `${collection.items.length} registro(s) carregado(s).`);
  } catch (error) {
    showError(error, 'Falha ao carregar listagem.');
    rows.value = [];
    pageInfo.value = { number: 0, totalPages: 1, totalElements: 0 };
  } finally {
    loading.value = false;
  }
}

async function loadDetail(row) {
  if (!row?.id) return;
  loading.value = true;
  errorMessage.value = '';
  try {
    selected.value = await apiRequest(apiBaseUrl.value, `${currentConfig.value.path}/${row.id}`, {
      version: selectedVersion()
    });
    syncForm(activeResource.value, selected.value);
    pushActivity('success', 'Registro carregado', `${currentConfig.value.label} #${row.id} pronto para edição.`);
  } catch (error) {
    showError(error, 'Falha ao carregar registro.');
  } finally {
    loading.value = false;
  }
}

function changePage(item) {
  activePage.value = item.id;
  if (item.resource) {
    activeResource.value = item.resource;
    resourceMode.value = 'list';
    searchMode.value = resourceConfigs[item.resource].defaultSearchMode;
    searchValue.value = '';
    page.value = 0;
    selected.value = null;
    syncForm(item.resource, null);
    loadList();
  }
}

function resetForm() {
  selected.value = null;
  syncForm(activeResource.value, null);
  pushActivity('info', 'Formulário limpo', 'Pronto para criar um novo registro.');
}

function openCreateForm() {
  selected.value = null;
  syncForm(activeResource.value, null);
  resourceMode.value = 'form';
  pushActivity('info', 'Tela de criação aberta', `Novo cadastro em ${currentConfig.value.label}.`);
}

async function openEditForm(row) {
  await loadDetail(row);
  resourceMode.value = 'form';
}

function closeForm() {
  resourceMode.value = 'list';
  pushActivity('info', 'Retorno para listagem', currentConfig.value.label);
}

function displayValue(value) {
  if (Array.isArray(value)) return value.map(displayValue).join(', ') || '-';
  if (value && typeof value === 'object') return value.name || value.title || value.key || `#${value.id || 'registro'}`;
  return value === undefined || value === null || value === '' ? '-' : String(value);
}

function displayField(row, field) {
  return displayValue(row?.[field]);
}

function fieldLabel(field) {
  return fieldLabels.value[field] || field;
}

function serializeField(field) {
  const value = form[field.name];
  if (field.type === 'relation') return value ? { id: Number(value) } : null;
  if (field.type === 'multiRelation') return Array.isArray(value) ? value.filter(Boolean).map((id) => ({ id: Number(id) })) : [];
  if (field.type === 'number') return value === '' || value === null ? null : Number(value);
  return value;
}

function buildPayload() {
  const payload = {};
  formFields.value.forEach((field) => {
    if (hiddenFormFields.has(field.name) && selected.value?.id) {
      payload[field.name] = selected.value[field.name];
      return;
    }
    payload[field.name] = serializeField(field);
  });
  return payload;
}

async function saveRecord() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const updating = Boolean(selected.value?.id);
    const response = await apiRequest(
      apiBaseUrl.value,
      updating ? `${currentConfig.value.path}/${selected.value.id}` : currentConfig.value.path,
      {
        method: updating ? 'PUT' : 'POST',
        body: buildPayload(),
        apiKey: apiKey.value.trim(),
        idempotencyKey: updating ? '' : crypto.randomUUID()
      }
    );
    selected.value = response;
    await loadList();
    resourceMode.value = 'list';
    pushActivity('success', updating ? 'Registro atualizado' : 'Registro criado', `${currentConfig.value.label} salvo com sucesso.`);
  } catch (error) {
    showError(error, 'Falha ao salvar registro.');
  } finally {
    loading.value = false;
  }
}

async function deleteRecord() {
  if (!selected.value?.id) return;
  if (!window.confirm(`Remover ${currentConfig.value.label.toLowerCase()} #${selected.value.id}?`)) return;
  loading.value = true;
  errorMessage.value = '';
  try {
    await apiRequest(apiBaseUrl.value, `${currentConfig.value.path}/${selected.value.id}`, {
      method: 'DELETE',
      apiKey: apiKey.value.trim()
    });
    resetForm();
    await loadList();
    resourceMode.value = 'list';
    pushActivity('success', 'Registro removido', `${currentConfig.value.label} excluído com sucesso.`);
  } catch (error) {
    showError(error, 'Falha ao remover registro.');
  } finally {
    loading.value = false;
  }
}

async function generateKey() {
  loading.value = true;
  errorMessage.value = '';
  try {
    latestCreatedKey.value = await apiRequest(apiBaseUrl.value, '/api/auth/keys', {
      method: 'POST',
      body: { owner: keyOwner.value || 'Professor' }
    });
    pushActivity('success', 'Chave gerada', 'Copie a chave e cole no campo Chave de API.');
  } catch (error) {
    showError(error, 'Falha ao gerar chave.');
  } finally {
    loading.value = false;
  }
}

async function revokeKey() {
  if (!latestCreatedKey.value?.id) return;
  loading.value = true;
  errorMessage.value = '';
  try {
    await apiRequest(apiBaseUrl.value, `/api/auth/keys/${latestCreatedKey.value.id}`, {
      method: 'DELETE',
      apiKey: apiKey.value.trim()
    });
    latestCreatedKey.value.active = false;
    pushActivity('success', 'Chave revogada', 'A última chave gerada foi desativada.');
  } catch (error) {
    showError(error, 'Falha ao revogar chave.');
  } finally {
    loading.value = false;
  }
}

function previousPage() {
  if (page.value <= 0) return;
  page.value -= 1;
  loadList();
}

function nextPage() {
  if (page.value + 1 >= pageInfo.value.totalPages) return;
  page.value += 1;
  loadList();
}

watch(apiBaseUrl, persistConnection);
watch(apiKey, persistConnection);
watch(filmVersion, () => {
  if (activeResource.value === 'films') {
    selected.value = null;
    resourceMode.value = 'list';
    loadList();
  }
});

onMounted(async () => {
  syncForm(activeResource.value, null);
  await loadRelationOptions();
  await loadList();
});
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">
          <Database :size="22" />
        </div>
        <div>
          <strong>Console Administrativo</strong>
          <span>Gestão da API</span>
        </div>
      </div>

      <nav class="nav-list" aria-label="Navegação principal">
        <button
          v-for="item in navigation"
          :key="item.id"
          class="nav-item"
          :class="{ active: activePage === item.id }"
          @click="changePage(item)"
        >
          <component :is="item.icon" :size="18" />
          <span>{{ item.label }}</span>
        </button>
      </nav>

      <div class="connection-panel">
        <label>
          <span>URL da API</span>
          <input v-model="apiBaseUrl" type="text" placeholder="https://api.exemplo.com" />
        </label>
        <label>
          <span>Chave de API</span>
          <input v-model="apiKey" type="password" placeholder="X-API-Key" />
        </label>
      </div>
    </aside>

    <main class="content">
      <header class="topbar">
        <div>
          <span class="section-kicker">Painel administrativo</span>
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="top-actions">
          <button class="ghost" @click="loadList">
            <RefreshCw :size="16" />
            Atualizar
          </button>
        </div>
      </header>

      <section class="metric-grid">
        <article v-for="metric in metrics" :key="metric.label" class="metric-card" :class="metric.tone">
          <span>{{ metric.label }}</span>
          <strong>{{ metric.value }}</strong>
        </article>
      </section>

      <section v-if="errorMessage" class="error-banner">
        <CircleAlert :size="18" />
        <span>{{ errorMessage }}</span>
        <button @click="errorMessage = ''" aria-label="Fechar aviso">
          <X :size="16" />
        </button>
      </section>

      <section v-else-if="latestActivity" class="status-banner" :class="latestActivity.type">
        <CheckCircle2 v-if="latestActivity.type === 'success'" :size="18" />
        <Info v-else-if="latestActivity.type === 'info'" :size="18" />
        <CircleAlert v-else :size="18" />
        <div>
          <strong>{{ latestActivity.title }}</strong>
          <span>{{ latestActivity.detail }}</span>
        </div>
      </section>

      <section v-if="activePage === 'overview'" class="overview-grid">
        <article class="panel overview-main">
          <div class="panel-head">
            <div>
              <span class="section-kicker">Operação</span>
              <h2>Resumo do ambiente</h2>
            </div>
          </div>
          <div class="health-list">
            <div>
              <Unplug :size="18" />
              <span>Endpoint configurado</span>
              <strong>{{ apiBaseUrl || 'Não informado' }}</strong>
            </div>
            <div>
              <FileKey2 :size="18" />
              <span>Escrita protegida</span>
              <strong>{{ writeReady ? 'Chave preenchida' : 'Aguardando chave' }}</strong>
            </div>
            <div>
              <Database :size="18" />
              <span>Recurso ativo</span>
              <strong>{{ currentConfig.label }}</strong>
            </div>
          </div>
        </article>

        <article class="panel">
          <div class="panel-head">
            <div>
              <span class="section-kicker">Atalhos</span>
              <h2>Páginas</h2>
            </div>
          </div>
          <div class="shortcut-list">
            <button v-for="key in resourceKeys" :key="key" @click="changePage(navigation.find((item) => item.resource === key))">
              <span>{{ resourceConfigs[key].label }}</span>
              <ChevronRight :size="16" />
            </button>
          </div>
        </article>

        <article class="panel activity-panel">
          <div class="panel-head">
            <div>
              <span class="section-kicker">Histórico</span>
              <h2>Atividade recente</h2>
            </div>
          </div>
          <div class="activity-list">
            <div v-for="entry in activityLog" :key="entry.id" class="activity-item" :class="entry.type">
              <CheckCircle2 v-if="entry.type === 'success'" :size="16" />
              <Info v-else-if="entry.type === 'info'" :size="16" />
              <CircleAlert v-else :size="16" />
              <div>
                <strong>{{ entry.title }}</strong>
                <span>{{ entry.detail }}</span>
              </div>
              <time>{{ entry.time }}</time>
            </div>
            <div v-if="!activityLog.length" class="activity-empty">
              <Clock3 :size="16" />
              Nenhuma ação registrada nesta sessão.
            </div>
          </div>
        </article>
      </section>

      <section v-else-if="isResourcePage" class="resource-page">
        <section v-if="resourceMode === 'list'" class="panel table-panel">
          <div class="panel-head compact">
            <div>
              <span class="section-kicker">{{ currentConfig.subtitle }}</span>
              <h2>{{ currentConfig.label }}</h2>
            </div>
            <div class="table-actions">
              <div v-if="activeResource === 'films'" class="segmented">
                <button :class="{ active: filmVersion === '1' }" @click="filmVersion = '1'">V1</button>
                <button :class="{ active: filmVersion === '2' }" @click="filmVersion = '2'">V2</button>
              </div>
              <button class="primary" :disabled="isFilmV2" @click="openCreateForm">
                <Plus :size="16" />
                Novo
              </button>
            </div>
          </div>

          <div v-if="!isFilmV2" class="filters">
            <label>
              <span>Campo</span>
              <select v-model="searchMode">
                <option v-for="mode in searchModes" :key="mode.key" :value="mode.key">{{ mode.label }}</option>
              </select>
            </label>
            <label>
              <span>Busca</span>
              <input v-model="searchValue" type="text" placeholder="Digite para filtrar" @keyup.enter="loadList" />
            </label>
            <label class="size-field">
              <span>Itens</span>
              <input v-model.number="size" type="number" min="1" max="100" @change="loadList" />
            </label>
            <button class="primary" @click="loadList">
              <Search :size="16" />
              Buscar
            </button>
          </div>

          <div v-else class="notice">
            Versão V2 em modo de consulta. Operações de escrita ficam na V1.
          </div>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th v-for="column in visibleColumns" :key="isFilmV2 ? column.key : column">
                    {{ isFilmV2 ? column.label : fieldLabel(column) }}
                  </th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="row in rows"
                  :key="row.id"
                  :class="{ selected: selected?.id === row.id }"
                  @click="loadDetail(row)"
                >
                  <td>{{ row.id }}</td>
                  <template v-if="isFilmV2">
                    <td v-for="column in visibleColumns" :key="column.key">{{ displayField(row, column.key) }}</td>
                  </template>
                  <template v-else>
                    <td v-for="column in visibleColumns" :key="column">{{ displayField(row, column) }}</td>
                  </template>
                  <td>
                    <div class="row-actions">
                      <button :disabled="isFilmV2" @click.stop="openEditForm(row)">
                        Editar
                      </button>
                      <button class="danger" :disabled="isFilmV2 || !writeReady" @click.stop="selected = row; deleteRecord()">
                        Excluir
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="!rows.length && !loading">
                  <td :colspan="visibleColumns.length + 2" class="empty-cell">Nenhum registro encontrado.</td>
                </tr>
              </tbody>
            </table>
          </div>

          <footer class="pager">
            <span>Página {{ (pageInfo.number ?? 0) + 1 }} de {{ pageInfo.totalPages || 1 }}</span>
            <div>
              <button :disabled="page <= 0" @click="previousPage">
                <ChevronLeft :size="16" />
              </button>
              <button :disabled="page + 1 >= pageInfo.totalPages" @click="nextPage">
                <ChevronRight :size="16" />
              </button>
            </div>
          </footer>
        </section>

        <section v-else class="panel form-panel">
          <div class="panel-head compact">
            <div>
              <span class="section-kicker">{{ currentConfig.label }}</span>
              <h2>{{ selected?.id ? 'Editar registro' : 'Criar registro' }}</h2>
            </div>
            <div class="editor-actions">
              <button @click="closeForm">
                <ChevronLeft :size="16" />
                Voltar
              </button>
              <button class="danger" :disabled="!selected?.id || isFilmV2 || !writeReady" @click="deleteRecord">
                <Trash2 :size="16" />
                Excluir
              </button>
            </div>
          </div>

          <div class="form-hint" :class="{ warn: !writeReady || isFilmV2 }">
            <Info :size="16" />
            <span>{{ formHint }}</span>
          </div>

          <form class="edit-form" @submit.prevent="saveRecord">
            <label v-for="field in visibleFormFields" :key="field.name">
              <span>{{ field.label }}</span>
              <textarea v-if="field.type === 'textarea'" v-model="form[field.name]" rows="4"></textarea>
              <select v-else-if="field.type === 'enum'" v-model="form[field.name]">
                <option v-for="option in field.options" :key="option" :value="option">{{ option }}</option>
              </select>
              <select v-else-if="field.type === 'relation'" v-model="form[field.name]">
                <option value="">Sem vínculo</option>
                <option v-for="option in relationOptions[field.source]" :key="option.id" :value="option.id">
                  #{{ option.id }} - {{ displayValue(option) }}
                </option>
              </select>
              <select v-else-if="field.type === 'multiRelation'" v-model="form[field.name]" multiple size="5">
                <option v-for="option in relationOptions[field.source]" :key="option.id" :value="option.id">
                  #{{ option.id }} - {{ displayValue(option) }}
                </option>
              </select>
              <input
                v-else
                v-model="form[field.name]"
                :type="field.type === 'number' ? 'number' : 'text'"
                :required="field.required"
              />
            </label>

            <button class="primary wide" type="submit" :disabled="isFilmV2 || !writeReady">
              <Save :size="16" />
              {{ selected?.id ? 'Salvar alterações' : 'Criar registro' }}
            </button>
          </form>
        </section>
      </section>

      <section v-else-if="activePage === 'keys'" class="keys-layout">
        <section class="panel">
          <div class="panel-head compact">
            <div>
              <span class="section-kicker">Autenticação</span>
              <h2>Gerar chave</h2>
            </div>
          </div>
          <div class="key-form">
            <label>
              <span>Responsável</span>
              <input v-model="keyOwner" type="text" placeholder="Professor" />
            </label>
            <button class="primary" @click="generateKey">
              <KeyRound :size="16" />
              Gerar chave
            </button>
            <button :disabled="!latestCreatedKey?.id" @click="revokeKey">
              <Trash2 :size="16" />
              Revogar última
            </button>
          </div>
        </section>

        <section class="panel">
          <div class="panel-head compact">
            <div>
              <span class="section-kicker">Resposta da API</span>
              <h2>Resultado</h2>
            </div>
          </div>
          <pre class="key-output">{{ latestCreatedKey ? JSON.stringify(latestCreatedKey, null, 2) : 'Nenhuma chave gerada nesta sessão.' }}</pre>
        </section>
      </section>
    </main>
  </div>
</template>
