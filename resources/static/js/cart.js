/* 购物车：勾选、改数量、删除、结算 */

const user = getUser();
if (!requireLogin()) {
  // 已经跳登录页了，不再往下执行
} else {
  initHeader('cart');
  initFooter();
  start();
}

let rows = [];
let addresses = [];
let selectedAddressId = null;

async function start() {
  await Promise.all([loadCart(), loadAddresses()]);
}

/* ---------------- 地址 ---------------- */
async function loadAddresses() {
  const box = document.getElementById('addrBox');
  try {
    addresses = await api.get('/api/addresses?userId=' + user.id);
    if (!addresses.length) {
      box.innerHTML = '还没有收货地址，<a href="mine.html" style="color:#1a6bff">去个人中心添加 →</a>';
      return;
    }
    const def = addresses.find((a) => a.isDefault === 1) || addresses[0];
    selectedAddressId = def.id;

    box.innerHTML = `
      <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap;">
        <span id="addrText">${addrText(def)}</span>
        ${addresses.length > 1 ? '<select id="addrSelect" style="height:32px;border:1px solid #dfe5f0;border-radius:8px;padding:0 8px;"></select>' : ''}
        <a href="mine.html" style="color:#1a6bff;font-size:12px;">管理地址</a>
      </div>`;

    const sel = document.getElementById('addrSelect');
    if (sel) {
      sel.innerHTML = addresses.map((a) => `<option value="${a.id}" ${a.id === def.id ? 'selected' : ''}>${esc(a.receiver)} · ${esc(a.detailAddress || '')}</option>`).join('');
      sel.addEventListener('change', () => {
        selectedAddressId = Number(sel.value);
        const a = addresses.find((x) => x.id === selectedAddressId);
        document.getElementById('addrText').textContent = addrText(a);
      });
    }
  } catch (e) {
    box.textContent = e.message;
  }
}

function addrText(a) {
  if (!a) return '';
  return `${esc(a.receiver)} ${esc(a.tel)}　${esc(a.province || '')}${esc(a.city || '')}${esc(a.district || '')}${esc(a.detailAddress || '')}`;
}

/* ---------------- 购物车 ---------------- */
async function loadCart() {
  const area = document.getElementById('cartArea');
  try {
    rows = await api.get('/api/cart?userId=' + user.id);
    render();
  } catch (e) {
    area.innerHTML = '<div class="empty">' + esc(e.message) + '</div>';
  }
}

function render() {
  const area = document.getElementById('cartArea');
  if (!rows.length) {
    area.innerHTML = `<div class="panel empty">购物车还是空的，<a href="index.html">去逛逛 →</a></div>`;
    return;
  }

  const allChecked = rows.every((r) => r.checked === 1);
  area.innerHTML = `
    <div class="cart-box">
      <div class="cart-head">
        <div><div class="ck ${allChecked ? 'on' : ''}" id="checkAll"></div></div>
        <div>商品信息</div><div>单价</div><div>数量</div><div>小计</div><div>操作</div>
      </div>
      ${rows.map((r) => `
        <div class="cart-row" data-id="${r.id}">
          <div><div class="ck ${r.checked === 1 ? 'on' : ''}" data-role="check"></div></div>
          <div class="ci">
            <img src="${imgUrl(r.image)}" alt="">
            <div class="t">${esc(r.name)}<br>
              <span style="font-size:12px;color:#9aa3b5;">库存 ${r.stock}</span>
            </div>
          </div>
          <div>¥${money(r.price)}</div>
          <div>
            <div class="stepper">
              <span data-role="minus">−</span><b>${r.quantity}</b><span data-role="plus">+</span>
            </div>
          </div>
          <div class="price" style="font-size:15px;">¥${money(r.price * r.quantity)}</div>
          <div><a style="color:#9aa3b5;font-size:12px;cursor:pointer;" data-role="del">删除</a></div>
        </div>`).join('')}
    </div>

    <div class="cart-foot">
      <div class="ck ${allChecked ? 'on' : ''}" id="checkAll2"></div>
      <span style="font-size:13px;color:#5a6478;">全选</span>
      <span id="selInfo" style="font-size:13px;color:#9aa3b5;"></span>
      <div class="total">合计（不含运费）：<b id="total">¥0.00</b></div>
      <button class="pay" id="checkout">去结算</button>
    </div>`;

  bindEvents();
  updateTotal();
}

function bindEvents() {
  document.getElementById('checkAll').addEventListener('click', toggleAll);
  document.getElementById('checkAll2').addEventListener('click', toggleAll);
  document.getElementById('checkout').addEventListener('click', checkout);

  document.querySelectorAll('.cart-row').forEach((row) => {
    const id = Number(row.dataset.id);
    const item = rows.find((r) => r.id === id);

    row.querySelector('[data-role="check"]').addEventListener('click', () => updateRow(id, { checked: item.checked === 1 ? 0 : 1 }));
    row.querySelector('[data-role="minus"]').addEventListener('click', () => updateRow(id, { quantity: Math.max(1, item.quantity - 1) }));
    row.querySelector('[data-role="plus"]').addEventListener('click', () => updateRow(id, { quantity: item.quantity + 1 }));
    row.querySelector('[data-role="del"]').addEventListener('click', () => removeRow(id));
  });
}

async function updateRow(id, patch) {
  try {
    const q = new URLSearchParams({ userId: user.id });
    if (patch.quantity !== undefined) q.set('quantity', patch.quantity);
    if (patch.checked !== undefined) q.set('checked', patch.checked);
    await api.put('/api/cart/' + id + '?' + q.toString());
    await loadCart();
    refreshCartCount();
  } catch (e) {
    toast(e.message);
  }
}

async function removeRow(id) {
  try {
    await api.del('/api/cart/' + id + '?userId=' + user.id);
    await loadCart();
    refreshCartCount();
    toast('已删除');
  } catch (e) {
    toast(e.message);
  }
}

async function toggleAll() {
  const target = rows.every((r) => r.checked === 1) ? 0 : 1;
  try {
    for (const r of rows) {
      if (r.checked !== target) {
        await api.put('/api/cart/' + r.id + '?userId=' + user.id + '&checked=' + target);
      }
    }
    await loadCart();
  } catch (e) {
    toast(e.message);
  }
}

function updateTotal() {
  const checked = rows.filter((r) => r.checked === 1);
  const total = checked.reduce((sum, r) => sum + r.price * r.quantity, 0);
  const info = document.getElementById('selInfo');
  const totalEl = document.getElementById('total');
  if (info) info.textContent = '已选 ' + checked.length + ' 件';
  if (totalEl) totalEl.textContent = '¥' + money(total);
}

/* ---------------- 结算 ---------------- */
async function checkout() {
  const checked = rows.filter((r) => r.checked === 1);
  if (!checked.length) return toast('先勾选要结算的商品');
  if (!selectedAddressId) return toast('请先添加收货地址');

  try {
    await api.post('/api/orders?userId=' + user.id + '&addressId=' + selectedAddressId);
    toast('下单成功');
    setTimeout(() => { location.href = 'orders.html'; }, 800);
  } catch (e) {
    toast(e.message);
  }
}
