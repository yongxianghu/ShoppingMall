/* 首页：轮播 + 分类入口 + 商品列表（分页/排序/搜索） */

const query = new URLSearchParams(location.search);
const keyword = query.get('keyword') || '';
let sort = query.get('sort') || '';
let page = Number(query.get('page') || 1);
const PAGE_SIZE = 8;

initHeader('index');
initFooter();

/* ---------- 轮播 ---------- */
let bannerTimer = null;
async function loadBanner() {
  try {
    const data = await api.get('/api/home');
    renderBanner(data.banners || []);
    renderQuick(data.categories || []);
  } catch (e) {
    toast(e.message);
  }
}

function renderBanner(list) {
  const host = document.getElementById('banner');
  if (!list.length) return;

  host.innerHTML = `
    <img id="bannerImg" alt="活动">
    <button class="arrow left" id="bannerPrev" aria-label="上一张">‹</button>
    <button class="arrow right" id="bannerNext" aria-label="下一张">›</button>
    <div class="dots" id="dots"></div>`;

  const dots = document.getElementById('dots');
  dots.innerHTML = list.map((b, i) => `<i data-i="${i}" class="${i === 0 ? 'on' : ''}"></i>`).join('');

  let index = 0;
  const img = document.getElementById('bannerImg');
  const show = (i) => {
    index = (i + list.length) % list.length;
    img.src = imgUrl(list[index].image);
    dots.querySelectorAll('i').forEach((d, k) => d.classList.toggle('on', k === index));
  };
  show(0);

  // 每次手动操作后重新计时，避免刚点完就自动翻页
  const restart = () => {
    clearInterval(bannerTimer);
    bannerTimer = setInterval(() => show(index + 1), 4000);
  };

  document.getElementById('bannerPrev').addEventListener('click', () => { show(index - 1); restart(); });
  document.getElementById('bannerNext').addEventListener('click', () => { show(index + 1); restart(); });

  dots.addEventListener('click', (e) => {
    const i = e.target.dataset.i;
    if (i !== undefined) { show(Number(i)); restart(); }
  });

  restart();
}

function renderQuick(categories) {
  document.getElementById('quick').innerHTML = categories.slice(0, 8).map((c) => `
    <a href="category.html?categoryId=${c.id}">
      <img src="${imgUrl(c.icon)}" alt="${esc(c.name)}">
      <span>${esc(c.name)}</span>
    </a>`).join('');
}

/* ---------- 商品列表 ---------- */
async function loadProducts() {
  const grid = document.getElementById('productGrid');
  const pager = document.getElementById('pager');
  grid.innerHTML = '<div class="empty">加载中…</div>';
  pager.innerHTML = '';

  if (keyword) {
    document.getElementById('sectionTitle').textContent = '搜索：' + keyword;
    document.getElementById('sectionSub').textContent = '按关键词匹配商品名与副标题';
  }

  try {
    const params = new URLSearchParams({ page: page, size: PAGE_SIZE });
    if (keyword) params.set('keyword', keyword);
    if (sort) params.set('sort', sort);

    const data = await api.get('/api/products?' + params.toString());
    const list = data.records || [];

    grid.innerHTML = list.length
      ? list.map(productCard).join('')
      : '<div class="empty">没有找到相关商品，换个关键词试试？<br><a href="index.html">回到首页</a></div>';

    renderPager(data);
  } catch (e) {
    grid.innerHTML = '<div class="empty">' + esc(e.message) + '</div>';
  }
}

function renderPager(data) {
  const total = data.pages || 0;
  if (total <= 1) return;

  const pager = document.getElementById('pager');
  const current = data.current || 1;
  let html = `<button ${current === 1 ? 'disabled' : ''} data-page="${current - 1}">上一页</button>`;

  const start = Math.max(1, current - 2);
  const end = Math.min(total, start + 4);
  for (let i = start; i <= end; i++) {
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

/* ---------- 排序 ---------- */
document.getElementById('sortBar').addEventListener('click', (e) => {
  const btn = e.target.closest('button');
  if (!btn) return;
  sort = btn.dataset.sort;
  page = 1;
  document.querySelectorAll('#sortBar button').forEach((b) => b.classList.toggle('on', b === btn));
  loadProducts();
});

loadBanner();
loadProducts();
