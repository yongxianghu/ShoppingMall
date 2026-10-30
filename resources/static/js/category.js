/* 分类页：左侧分类，右侧商品 */

const params = new URLSearchParams(location.search);
let categoryId = params.get('categoryId') ? Number(params.get('categoryId')) : null;
let sort = '';
let page = 1;
const PAGE_SIZE = 8;
let categories = [];

initHeader('category');
initFooter();

async function loadCategories() {
  categories = await api.get('/api/categories');
  const side = document.getElementById('side');
  side.innerHTML = '<h4>商品分类</h4>' + categories.map((c) => `
      <a href="javascript:void(0)" data-id="${c.id}" class="${categoryId === c.id ? 'on' : ''}">
        ${esc(c.name)}
      </a>`).join('');

  side.querySelectorAll('a[data-id]').forEach((a) => {
    a.addEventListener('click', () => {
      categoryId = Number(a.dataset.id);
      page = 1;
      side.querySelectorAll('a').forEach((x) => x.classList.toggle('on', x === a));
      updateTitle();
      loadProducts();
    });
  });

  updateTitle();
}

function updateTitle() {
  const current = categories.find((c) => c.id === categoryId);
  document.getElementById('catTitle').textContent = current ? current.name : '全部商品';
}

async function loadProducts() {
  const grid = document.getElementById('productGrid');
  grid.innerHTML = '<div class="empty">加载中…</div>';
  document.getElementById('pager').innerHTML = '';

  try {
    const q = new URLSearchParams({ page: page, size: PAGE_SIZE });
    if (categoryId) q.set('categoryId', categoryId);
    if (sort) q.set('sort', sort);

    const data = await api.get('/api/products?' + q.toString());
    const list = data.records || [];
    document.getElementById('catCount').textContent = '共 ' + (data.total || 0) + ' 件';

    grid.innerHTML = list.length
      ? list.map(productCard).join('')
      : '<div class="empty">这个分类下还没有商品</div>';

    renderPager(data);
  } catch (e) {
    grid.innerHTML = '<div class="empty">' + esc(e.message) + '</div>';
  }
}

function renderPager(data) {
  const total = data.pages || 0;
  if (total <= 1) return;
  const current = data.current || 1;
  const pager = document.getElementById('pager');
  let html = `<button ${current === 1 ? 'disabled' : ''} data-page="${current - 1}">上一页</button>`;
  for (let i = Math.max(1, current - 2); i <= Math.min(total, current + 2); i++) {
    html += `<button data-page="${i}" class="${i === current ? 'on' : ''}">${i}</button>`;
  }
  html += `<button ${current === total ? 'disabled' : ''} data-page="${current + 1}">下一页</button>`;
  pager.innerHTML = html;

  pager.querySelectorAll('button[data-page]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const target = Number(btn.dataset.page);
      if (!target || target === current) return;
      page = target;
      loadProducts();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });
}

document.getElementById('sortBar').addEventListener('click', (e) => {
  const btn = e.target.closest('button');
  if (!btn) return;
  sort = btn.dataset.sort;
  page = 1;
  document.querySelectorAll('#sortBar button').forEach((b) => b.classList.toggle('on', b === btn));
  loadProducts();
});

(async function start() {
  try {
    await loadCategories();
    await loadProducts();
  } catch (e) {
    toast(e.message);
  }
})();
