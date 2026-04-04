// === Data Store ===
let data = {
  checklists: [],
  bags: [
    { id: 'b1', name: 'キャリーケース' },
    { id: 'b2', name: 'リュック' },
    { id: 'b3', name: 'ハンドバッグ' }
  ],
  defaultTemplate: ['パスポート','充電器','着替え','歯ブラシ','常備薬','財布','スマホ','イヤホン'],
  customTemplates: [
    { id: 'ct1', label: 'USJ', items: ['レインコート','日焼け止め','モバイルバッテリー','カメラ'] },
    { id: 'ct2', label: 'スキー旅行', items: ['ゴーグル','手袋','ネックウォーマー','リフト券'] }
  ],
  familyGroup: null,
  nextId: 100,
  lastBagId: null
};

// Create sample data
createSampleData();

function createSampleData() {
  const items1 = [
    ...data.defaultTemplate.map((name, i) => ({
      id: 'i' + (i+1), name, isChecked: i < 3, bagId: i < 2 ? 'b1' : (i === 2 ? 'b2' : null)
    })),
    { id: 'i20', name: 'レインコート', isChecked: true, bagId: 'b3' },
    { id: 'i21', name: '日焼け止め', isChecked: false, bagId: null }
  ];
  data.checklists.push({
    id: 'c1', title: '沖縄旅行 2026', createdAt: new Date('2026-04-01'), items: items1, shared: true
  });
  const items2 = data.defaultTemplate.map((name, i) => ({
    id: 'i' + (30+i), name, isChecked: false, bagId: null
  }));
  data.checklists.push({
    id: 'c2', title: '出張 東京', createdAt: new Date('2026-03-28'), items: items2, shared: false
  });
}

function genId() { return 'id' + (data.nextId++); }

// === Screen Navigation ===
function showScreen(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById(id).classList.add('active');
  closeAllMenus();
  if (id === 'screen-list') renderChecklistList();
  if (id === 'screen-bag-manage') renderBagManage();
  if (id === 'screen-template-manage') renderTemplateManage();
  if (id === 'screen-template-select') renderTemplateSelect();
  if (id === 'screen-family') renderFamily();
}

function closeAllMenus() {
  document.querySelectorAll('.dropdown-menu').forEach(m => m.classList.remove('show'));
  document.querySelectorAll('.dialog-overlay').forEach(d => d.classList.remove('show'));
}

// === Checklist List ===
function renderChecklistList() {
  const list = document.getElementById('checklist-list');
  const empty = document.getElementById('empty-state');
  if (data.checklists.length === 0) { list.innerHTML = ''; empty.style.display = 'block'; return; }
  empty.style.display = 'none';
  const sorted = [...data.checklists].sort((a,b) => b.createdAt - a.createdAt);
  list.innerHTML = sorted.map(cl => {
    const checked = cl.items.filter(i => i.isChecked).length;
    const total = cl.items.length;
    const dateStr = cl.createdAt.toLocaleDateString('ja-JP');
    return `<div class="checklist-card" onclick="openChecklist('${cl.id}')">
      <div class="info">
        <div class="title">${cl.shared ? '🔗 ' : ''}${cl.title}</div>
        <div class="meta">${dateStr}</div>
      </div>
      <div class="progress">${checked}/${total}</div>
      <button class="item-delete" onclick="event.stopPropagation();confirmDeleteChecklist('${cl.id}')" title="削除">✕</button>
    </div>`;
  }).join('');
}

// === Create Checklist ===
function createChecklist() {
  const input = document.getElementById('new-title');
  const err = document.getElementById('title-error');
  if (!input.value.trim()) { err.classList.add('show'); return; }
  err.classList.remove('show');
  const items = data.defaultTemplate.map(name => ({ id: genId(), name, isChecked: false, bagId: null }));
  data.checklists.push({ id: genId(), title: input.value.trim(), createdAt: new Date(), items, shared: false });
  input.value = '';
  showScreen('screen-list');
}

// === Checklist Detail ===
let currentChecklistId = null;
let currentView = 'flat';

function openChecklist(id) {
  currentChecklistId = id;
  currentView = 'flat';
  renderDetail();
  showScreen('screen-detail');
}

function renderDetail() {
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  if (!cl) return;
  document.getElementById('detail-title').textContent = cl.title;
  const checked = cl.items.filter(i => i.isChecked).length;
  document.getElementById('detail-progress').textContent = `${checked}/${cl.items.length}`;
  document.querySelectorAll('.view-toggle .toggle-btn').forEach(b => b.classList.remove('active'));
  document.querySelector(`.toggle-btn[onclick="setView('${currentView}')"]`).classList.add('active');

  const container = document.getElementById('detail-items');
  if (currentView === 'flat') {
    container.innerHTML = cl.items.map(item => renderCheckItem(item)).join('');
  } else {
    const groups = {};
    cl.items.forEach(item => {
      const key = item.bagId || '__none__';
      if (!groups[key]) groups[key] = [];
      groups[key].push(item);
    });
    let html = '';
    // Bags with items first
    data.bags.forEach(bag => {
      if (groups[bag.id]) {
        html += `<div class="bag-group-header">👜 ${bag.name}（${groups[bag.id].length}）</div>`;
        html += groups[bag.id].map(item => renderCheckItem(item)).join('');
      }
    });
    // Unassigned
    if (groups['__none__']) {
      html += `<div class="bag-group-header unassigned">📦 未分類（${groups['__none__'].length}）</div>`;
      html += groups['__none__'].map(item => renderCheckItem(item)).join('');
    }
    container.innerHTML = html;
  }
}

function renderCheckItem(item) {
  const bag = item.bagId ? data.bags.find(b => b.id === item.bagId) : null;
  const bagTag = bag ? `<span class="item-bag">👜 ${bag.name}</span>` : '';
  return `<div class="check-item ${item.isChecked ? 'checked' : ''}">
    <input type="checkbox" ${item.isChecked ? 'checked' : ''} onchange="toggleItem('${item.id}')">
    <span class="item-name">${item.name}</span>
    ${bagTag}
    <button class="item-delete" onclick="deleteItem('${item.id}')" title="削除">✕</button>
  </div>`;
}

function setView(v) { currentView = v; renderDetail(); }

// === Check / Uncheck with Bag Selection ===
let pendingCheckItemId = null;

function toggleItem(itemId) {
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  const item = cl.items.find(i => i.id === itemId);
  if (item.isChecked) {
    // Uncheck → remove bag
    item.isChecked = false;
    item.bagId = null;
    renderDetail();
  } else {
    // Check → show bag selection if bags exist
    if (data.bags.length > 0) {
      pendingCheckItemId = itemId;
      showBagSelectDialog();
    } else {
      item.isChecked = true;
      renderDetail();
    }
  }
}

function showBagSelectDialog() {
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  const bagCounts = {};
  cl.items.forEach(i => { if (i.bagId && i.isChecked) bagCounts[i.bagId] = (bagCounts[i.bagId]||0)+1; });

  // Sort: last used → count desc → rest
  const sorted = [...data.bags].sort((a,b) => {
    if (a.id === data.lastBagId) return -1;
    if (b.id === data.lastBagId) return 1;
    const ca = bagCounts[a.id]||0, cb = bagCounts[b.id]||0;
    if (ca !== cb) return cb - ca;
    return 0;
  });

  const list = document.getElementById('bag-select-list');
  list.innerHTML = sorted.map(bag => {
    const count = bagCounts[bag.id] || 0;
    const isLast = bag.id === data.lastBagId;
    return `<div class="bag-option" onclick="selectBag('${bag.id}')">
      <span class="bag-emoji">👜</span>
      <span class="bag-name">${bag.name}${isLast ? ' ⭐' : ''}</span>
      <span class="bag-count">${count > 0 ? count + '個入り' : ''}</span>
    </div>`;
  }).join('');
  document.getElementById('bag-select-dialog').classList.add('show');
}

function selectBag(bagId) {
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  const item = cl.items.find(i => i.id === pendingCheckItemId);
  if (item) {
    item.isChecked = true;
    item.bagId = bagId;
    if (bagId) data.lastBagId = bagId;
  }
  document.getElementById('bag-select-dialog').classList.remove('show');
  pendingCheckItemId = null;
  renderDetail();
}

// === Add / Delete Items ===
function addItem() {
  const input = document.getElementById('new-item-name');
  if (!input.value.trim()) { input.value = ''; return; }
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  cl.items.push({ id: genId(), name: input.value.trim(), isChecked: false, bagId: null });
  input.value = '';
  renderDetail();
}

function deleteItem(itemId) {
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  cl.items = cl.items.filter(i => i.id !== itemId);
  renderDetail();
}

// === Delete Checklist ===
function confirmDeleteChecklist(id) {
  const cl = data.checklists.find(c => c.id === id);
  document.getElementById('delete-msg').textContent = `「${cl.title}」を削除しますか？`;
  document.getElementById('delete-confirm-btn').onclick = () => {
    data.checklists = data.checklists.filter(c => c.id !== id);
    closeDeleteDialog();
    renderChecklistList();
  };
  document.getElementById('delete-dialog').classList.add('show');
}
function closeDeleteDialog() { document.getElementById('delete-dialog').classList.remove('show'); }

// === Detail Menu ===
function toggleDetailMenu() {
  document.getElementById('detail-menu').classList.toggle('show');
}
function toggleSharing() {
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  cl.shared = !cl.shared;
  closeAllMenus();
  renderDetail();
  alert(cl.shared ? '共有を有効にしました 🔗' : '共有を無効にしました 🔒');
}

// === Save as Template Dialog ===
function showSaveTemplateDialog() {
  closeAllMenus();
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  const list = document.getElementById('template-item-select');
  list.innerHTML = cl.items.map(item =>
    `<label class="template-option">
      <input type="checkbox" value="${item.id}" checked>
      <span>${item.name}</span>
    </label>`
  ).join('');
  document.getElementById('template-label').value = '';
  document.getElementById('save-template-dialog').classList.add('show');
}
function closeSaveTemplateDialog() { document.getElementById('save-template-dialog').classList.remove('show'); }

function saveAsTemplate() {
  const label = document.getElementById('template-label').value.trim();
  if (!label) { alert('ラベルを入力してください'); return; }
  const cl = data.checklists.find(c => c.id === currentChecklistId);
  const selected = [...document.querySelectorAll('#template-item-select input:checked')].map(cb => {
    const item = cl.items.find(i => i.id === cb.value);
    return item ? item.name : null;
  }).filter(Boolean);
  if (selected.length === 0) { alert('項目を1つ以上選択してください'); return; }
  data.customTemplates.push({ id: genId(), label, items: selected });
  closeSaveTemplateDialog();
  alert(`「${label}」テンプレートを保存しました ✅`);
}

// === Template Select (Create from Template) ===
function renderTemplateSelect() {
  const list = document.getElementById('template-select-list');
  if (data.customTemplates.length === 0) {
    list.innerHTML = '<p style="color:#999;text-align:center;padding:20px">カスタムテンプレートがありません</p>';
    return;
  }
  list.innerHTML = data.customTemplates.map(t =>
    `<label class="template-option" onclick="this.querySelector('input').click()">
      <input type="checkbox" value="${t.id}" onclick="event.stopPropagation()">
      <span><strong>${t.label}</strong><br><small style="color:#999">${t.items.join(', ')}</small></span>
    </label>`
  ).join('');
}

function createFromTemplate() {
  const titleInput = document.getElementById('template-create-title');
  if (!titleInput.value.trim()) { alert('タイトルを入力してください'); return; }
  const selectedIds = [...document.querySelectorAll('#template-select-list input:checked')].map(cb => cb.value);
  if (selectedIds.length === 0) { alert('テンプレートを1つ以上選択してください'); return; }

  // Merge: default + selected custom templates, deduplicate
  const allNames = new Set(data.defaultTemplate);
  selectedIds.forEach(id => {
    const t = data.customTemplates.find(ct => ct.id === id);
    if (t) t.items.forEach(name => allNames.add(name));
  });

  const items = [...allNames].map(name => ({ id: genId(), name, isChecked: false, bagId: null }));
  data.checklists.push({ id: genId(), title: titleInput.value.trim(), createdAt: new Date(), items, shared: false });
  titleInput.value = '';
  showScreen('screen-list');
}

// === Template Management ===
function renderTemplateManage() {
  // Default template
  const defList = document.getElementById('default-template-items');
  defList.innerHTML = data.defaultTemplate.map((name, i) =>
    `<div class="template-item">
      <span class="item-name">${name}</span>
      <button class="item-delete" onclick="removeDefaultItem(${i})">✕</button>
    </div>`
  ).join('');

  // Custom templates
  const custList = document.getElementById('custom-template-list');
  if (data.customTemplates.length === 0) {
    custList.innerHTML = '<p style="color:#999;text-align:center;padding:20px">カスタムテンプレートがありません</p>';
    return;
  }
  custList.innerHTML = data.customTemplates.map(t =>
    `<div class="custom-template-card">
      <div class="label">📋 ${t.label}</div>
      <div class="items-preview">${t.items.join(', ')}</div>
      <div class="actions">
        <button class="btn text" onclick="renameCustomTemplate('${t.id}')">名前変更</button>
        <button class="btn text" onclick="editCustomTemplateItems('${t.id}')">項目編集</button>
        <button class="btn text" style="color:#EF4444" onclick="deleteCustomTemplate('${t.id}')">削除</button>
      </div>
    </div>`
  ).join('');
}

function addDefaultItem() {
  const input = document.getElementById('new-default-item');
  if (!input.value.trim()) return;
  data.defaultTemplate.push(input.value.trim());
  input.value = '';
  renderTemplateManage();
}

function removeDefaultItem(index) {
  data.defaultTemplate.splice(index, 1);
  renderTemplateManage();
}

function deleteCustomTemplate(id) {
  data.customTemplates = data.customTemplates.filter(t => t.id !== id);
  renderTemplateManage();
}

function renameCustomTemplate(id) {
  const t = data.customTemplates.find(ct => ct.id === id);
  const newLabel = prompt('テンプレート名を変更', t.label);
  if (newLabel && newLabel.trim()) {
    t.label = newLabel.trim();
    renderTemplateManage();
  }
}

function editCustomTemplateItems(id) {
  editingCustomTemplateId = id;
  const t = data.customTemplates.find(ct => ct.id === id);
  document.getElementById('custom-template-edit-title').textContent = '📋 ' + t.label;
  renderCustomTemplateEdit();
  showScreen('screen-custom-template-edit');
}

let editingCustomTemplateId = null;

function renderCustomTemplateEdit() {
  const t = data.customTemplates.find(ct => ct.id === editingCustomTemplateId);
  if (!t) return;
  const list = document.getElementById('custom-template-edit-items');
  list.innerHTML = t.items.map((name, i) =>
    `<div class="template-item">
      <span class="item-name">${name}</span>
      <button class="item-delete" onclick="removeCustomTemplateItem(${i})">✕</button>
    </div>`
  ).join('');
}

function addCustomTemplateItem() {
  const input = document.getElementById('new-custom-template-item');
  if (!input.value.trim()) return;
  const t = data.customTemplates.find(ct => ct.id === editingCustomTemplateId);
  t.items.push(input.value.trim());
  input.value = '';
  renderCustomTemplateEdit();
}

function removeCustomTemplateItem(index) {
  const t = data.customTemplates.find(ct => ct.id === editingCustomTemplateId);
  t.items.splice(index, 1);
  renderCustomTemplateEdit();
}

// === Bag Management ===
function renderBagManage() {
  const list = document.getElementById('bag-list');
  if (data.bags.length === 0) {
    list.innerHTML = '<p style="color:#999;text-align:center;padding:20px">鞄が登録されていません</p>';
    return;
  }
  list.innerHTML = data.bags.map(bag =>
    `<div class="bag-manage-item">
      <span class="bag-emoji">👜</span>
      <span class="bag-name" onclick="renameBag('${bag.id}')" style="cursor:pointer" title="タップで名前変更">${bag.name}</span>
      <button class="item-delete" onclick="deleteBag('${bag.id}')">✕</button>
    </div>`
  ).join('');
}

function renameBag(id) {
  const bag = data.bags.find(b => b.id === id);
  const newName = prompt('鞄の名前を変更', bag.name);
  if (newName && newName.trim()) {
    bag.name = newName.trim();
    renderBagManage();
  }
}

function addBag() {
  const input = document.getElementById('new-bag-name');
  const err = document.getElementById('bag-error');
  if (!input.value.trim()) { err.style.display = 'block'; return; }
  err.style.display = 'none';
  data.bags.push({ id: genId(), name: input.value.trim() });
  input.value = '';
  renderBagManage();
}

function deleteBag(id) {
  data.bags = data.bags.filter(b => b.id !== id);
  // Clear bag assignments
  data.checklists.forEach(cl => {
    cl.items.forEach(item => { if (item.bagId === id) item.bagId = null; });
  });
  renderBagManage();
}

// === Family Group ===
function renderFamily() {
  if (data.familyGroup) {
    document.getElementById('family-no-group').style.display = 'none';
    document.getElementById('family-has-group').style.display = 'block';
    document.getElementById('invite-code-display').textContent = data.familyGroup.inviteCode;
  } else {
    document.getElementById('family-no-group').style.display = 'block';
    document.getElementById('family-has-group').style.display = 'none';
  }
}

function createFamilyGroup() {
  const code = crypto.randomUUID().split('-').slice(0,2).join('-').toUpperCase();
  data.familyGroup = { id: genId(), inviteCode: code };
  renderFamily();
}

function copyInviteCode() {
  const code = data.familyGroup.inviteCode;
  navigator.clipboard.writeText(code).then(() => {
    const hint = document.getElementById('invite-code-hint');
    hint.textContent = 'コピーしました ✅';
    setTimeout(() => { hint.textContent = 'タップでコピー'; }, 2000);
  });
}

function joinFamilyGroup() {
  const input = document.getElementById('invite-code-input');
  if (!input.value.trim()) { alert('招待コードを入力してください'); return; }
  data.familyGroup = { id: genId(), inviteCode: input.value.trim().toUpperCase() };
  input.value = '';
  renderFamily();
  alert('グループに参加しました ✅');
}

function leaveFamilyGroup() {
  if (confirm('グループから退出しますか？')) {
    data.familyGroup = null;
    renderFamily();
  }
}

// === Init ===
renderChecklistList();
