/* 公共逻辑：登录态、头部导航、提示、加购物车 */

const USER_KEY = 'shop_user';

function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null');
  } catch (e) {
    return null;
  }
}

function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function logout() {
  localStorage.removeItem(USER_KEY);
  location.href = 'index.html';
}

/** 没登录就跳登录页，并记下回来的地址 */
function goLogin() {
  const back = encodeURIComponent(location.pathname + location.search);
  location.href = 'login.html?redirect=' + back;
}

function requireLogin() {
  if (!getUser()) {
    goLogin();
    return false;
  }
  return true;
}

let toastTimer = null;
function toast(message) {
  let el = document.querySelector('.toast');
  if (!el) {
    el = document.createElement('div');
    el.className = 'toast';
    document.body.appendChild(el);
  }
  el.textContent = message;
  el.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), 1800);
}

function money(value) {
  return Number(value || 0).toFixed(2);
}

function esc(text) {
  return String(text == null ? '' : text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/** 加入购物车：未登录先跳登录 */
async function addToCart(productId, quantity) {
  const user = getUser();
  if (!user) {
    toast('请先登录');
    setTimeout(goLogin, 500);
    return;
  }
  try {
    await api.post('/api/cart?userId=' + user.id, { productId: productId, quantity: quantity || 1 });
    toast('已加入购物车');
    refreshCartCount();
  } catch (e) {
    toast(e.message);
  }
}

async function refreshCartCount() {
  const el = document.getElementById('cartCount');
  if (!el) return;
  const user = getUser();
  if (!user) {
    el.textContent = '0';
    return;
  }
  try {
    const rows = await api.get('/api/cart?userId=' + user.id);
    el.textContent = rows.length;
  } catch (e) {
    el.textContent = '0';
  }
}

/** 渲染公共头部，active 用来高亮当前页 */
async function initHeader(active) {
  const user = getUser();
  const host = document.getElementById('app-header');
  if (!host) return;

  host.innerHTML = `
    <div class="topbar">
      <div class="wrap">
        <div>📍 配送至：江西 南昌 · 蓝汐商城（课程实训项目）</div>
        <div class="links">
          ${user
      ? `<span>你好，${esc(user.nickname || user.username)}</span><a href="orders.html">我的订单</a><a href="javascript:logout()">退出</a>`
      : `<a href="login.html">登录</a><a href="login.html?tab=register">注册</a>`}
          <a href="mine.html">个人中心</a>
        </div>
      </div>
    </div>
    <header class="header">
      <div class="wrap">
        <a class="logo" href="index.html">
          <div class="mark">汐</div>
          <div>蓝汐商城<small>LANXI MALL</small></div>
        </a>
        <div class="search">
          <input id="searchInput" placeholder="搜索商品，例如：耳机 / 咖啡 / 键盘">
          <button class="btn" id="searchBtn">搜索</button>
          <div class="hot"><span>热门：</span>
            <a href="index.html?keyword=耳机">蓝牙耳机</a>
            <a href="index.html?keyword=咖啡">手冲咖啡</a>
            <a href="index.html?keyword=键盘">机械键盘</a>
          </div>
        </div>
        <div class="actions">
          <a class="ghost-btn" href="cart.html">购物车 <span class="num" id="cartCount">0</span></a>
          ${user
      ? `<a class="ghost-btn" href="mine.html">${esc(user.nickname || user.username)}</a>`
      : `<a class="ghost-btn" href="login.html">登录 / 注册</a>`}
        </div>
      </div>
    </header>
    <nav class="catnav">
      <div class="wrap" id="navLinks">
        <a href="index.html" class="${active === 'index' ? 'on' : ''}">首页</a>
        <a href="category.html" class="${active === 'category' ? 'on' : ''}">全部分类</a>
        <a href="cart.html" class="${active === 'cart' ? 'on' : ''}">购物车</a>
        <a href="orders.html" class="${active === 'orders' ? 'on' : ''}">我的订单</a>
      </div>
    </nav>`;

  // 搜索
  const input = document.getElementById('searchInput');
  const doSearch = () => {
    const kw = input.value.trim();
    location.href = 'index.html' + (kw ? '?keyword=' + encodeURIComponent(kw) : '');
  };
  document.getElementById('searchBtn').addEventListener('click', doSearch);
  input.addEventListener('keydown', (e) => { if (e.key === 'Enter') doSearch(); });

  // 顶部导航补上分类
  try {
    const categories = await api.get('/api/categories');
    const nav = document.getElementById('navLinks');
    categories.slice(0, 6).forEach((c) => {
      const a = document.createElement('a');
      a.href = 'category.html?categoryId=' + c.id;
      a.textContent = c.name;
      nav.appendChild(a);
    });
  } catch (e) {
    /* 分类没拿到就不显示，不影响主流程 */
  }

  refreshCartCount();
}

/** 底部 */
function initFooter() {
  const host = document.getElementById('app-footer');
  if (!host) return;
  host.innerHTML = `
    <footer>
      <div class="wrap">
        <div><h5>购物指南</h5><p>购物流程<br>会员介绍<br>常见问题<br>联系客服</p></div>
        <div><h5>配送方式</h5><p>上门自提<br>配送范围<br>运费说明<br>配送时效</p></div>
        <div><h5>售后服务</h5><p>7 天无理由<br>退换货流程<br>价格保护<br>取消订单</p></div>
        <div><h5>关于我们</h5><p>公司介绍<br>隐私政策<br>用户协议</p></div>
      </div>
      <div class="copy">© 2026 蓝汐商城 · 课程实训项目 · Spring Boot + MySQL + 原生 JS</div>
    </footer>`;
}

/** 通用商品卡片 HTML */
function productCard(p) {
  return `
    <div class="card" onclick="location.href='product.html?id=${p.id}'">
      <img class="thumb" src="${imgUrl(p.mainImage)}" alt="${esc(p.name)}" loading="lazy">
      <div class="body">
        <div class="name">${esc(p.name)}</div>
        <div class="desc">${esc(p.subtitle || '')}</div>
        <div class="foot">
          <div class="price"><small>¥</small>${money(p.price)}</div>
          <div class="sold">已售 ${p.sales || 0}</div>
        </div>
        <button class="add" onclick="event.stopPropagation();addToCart(${p.id},1)">加入购物车</button>
      </div>
    </div>`;
}
