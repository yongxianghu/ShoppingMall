/* 商品详情 */

const productId = Number(new URLSearchParams(location.search).get('id'));
let product = null;
let quantity = 1;

initHeader('');
initFooter();

async function load() {
  const host = document.getElementById('detail');
  if (!productId) {
    host.innerHTML = '<div class="empty">商品不存在</div>';
    return;
  }

  try {
    product = await api.get('/api/products/' + productId);
    document.title = product.name + ' · 蓝汐商城';

    let categoryName = product.categoryId;
    try {
      const cats = await api.get('/api/categories');
      const hit = cats.find((c) => c.id === product.categoryId);
      if (hit) categoryName = hit.name;
    } catch (e) { /* 分类拿不到就显示 id */ }
    host.innerHTML = `
      <div class="detail">
        <img class="pic" src="${imgUrl(product.mainImage)}" alt="${esc(product.name)}">
        <div>
          <h1>${esc(product.name)}</h1>
          <div class="sub">${esc(product.subtitle || '')}</div>

          <div class="price-bar">
            <span class="price"><small>¥</small>${money(product.price)}</span>
            ${product.oldPrice ? `<span class="price-old">¥${money(product.oldPrice)}</span>` : ''}
          </div>

          <div class="meta">
            已售 ${product.sales || 0} 件　·　库存 ${product.stock || 0} 件<br>
            分类：${esc(categoryName)}　·　商品编号：${product.id}
          </div>

          <div style="display:flex;align-items:center;gap:14px;margin-top:22px;">
            <span style="font-size:13px;color:#7a8296;">数量</span>
            <div class="stepper">
              <span id="minus">−</span><b id="qty">1</b><span id="plus">+</span>
            </div>
          </div>

          <div class="buy">
            <button class="add" id="addBtn">加入购物车</button>
            <button class="pay" id="buyBtn">立即购买</button>
          </div>
        </div>
      </div>`;

    const qtyEl = document.getElementById('qty');
    document.getElementById('minus').onclick = () => {
      quantity = Math.max(1, quantity - 1);
      qtyEl.textContent = quantity;
    };
    document.getElementById('plus').onclick = () => {
      quantity = Math.min(product.stock || 99, quantity + 1);
      qtyEl.textContent = quantity;
    };

    document.getElementById('addBtn').onclick = () => addToCart(product.id, quantity);
    document.getElementById('buyBtn').onclick = async () => {
      if (!getUser()) return goLogin();
      await addToCart(product.id, quantity);
      setTimeout(() => { location.href = 'cart.html'; }, 600);
    };
  } catch (e) {
    host.innerHTML = '<div class="empty">' + esc(e.message) + '</div>';
  }
}

load();
