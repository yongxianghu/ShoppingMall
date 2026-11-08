/* 我的订单：列表 + 支付 + 取消 */

const user = getUser();
if (requireLogin()) {
  initHeader('orders');
  initFooter();
  loadOrders();
}

const STATUS = {
  0: { text: '待支付', color: '#f5333f' },
  1: { text: '待发货', color: '#1a6bff' },
  2: { text: '待收货', color: '#1a6bff' },
  3: { text: '已完成', color: '#8a94a8' },
  4: { text: '已取消', color: '#8a94a8' }
};

async function loadOrders() {
  const host = document.getElementById('orders');
  host.innerHTML = '<div class="empty">加载中…</div>';

  try {
    const list = await api.get('/api/orders?userId=' + user.id);
    if (!list.length) {
      host.innerHTML = '<div class="panel empty">还没有订单，<a href="index.html">去逛逛 →</a></div>';
      return;
    }

    // 每个订单再取一次明细
    const details = await Promise.all(list.map((o) => api.get('/api/orders/' + o.id + '?userId=' + user.id)));
    host.innerHTML = details.map(renderOrder).join('');
    bindActions();
  } catch (e) {
    host.innerHTML = '<div class="empty">' + esc(e.message) + '</div>';
  }
}

function renderOrder(data) {
  const order = data.order;
  const items = data.items || [];
  const status = STATUS[order.status] || { text: '未知', color: '#8a94a8' };
  const addr = parseAddress(order.addressSnapshot);
  const count = items.reduce((sum, i) => sum + i.quantity, 0);

  return `
    <div class="order" data-id="${order.id}">
      <div class="head">
        <span>订单号：<span class="no">${esc(order.orderNo || order.id)}</span></span>
        <span>下单时间：${(order.createdAt || '').replace('T', ' ')}</span>
        <span class="status" style="color:${status.color}">${status.text}</span>
      </div>

      <div class="items">
        ${items.map((i) => `
          <div class="item">
            <img src="${imgUrl(i.image)}" alt="">
            <div>
              <div class="n">${esc(i.productName)}</div>
              <div class="p">¥${money(i.price)} × ${i.quantity}</div>
            </div>
            <div style="margin-left:auto;font-size:13px;color:#f5333f;">¥${money(i.subtotal)}</div>
          </div>`).join('')}
        ${addr ? `<div style="font-size:12px;color:#8a94a8;padding:10px 0;">收货信息：${addr}</div>` : ''}
      </div>

      <div class="foot">
        <span style="font-size:12px;color:#9aa3b5;">共 ${count} 件</span>
        ${cancelHint(order)}
        <div class="amount">合计：<b>¥${money(order.totalAmount)}</b></div>
        ${order.status === 0
      ? `<button class="mini-btn" data-act="pay" style="border-color:#f5333f;color:#f5333f;">立即支付</button>`
      : ''}
        ${order.cancelable
      ? `<button class="mini-btn danger" data-act="cancel">${order.status === 1 ? '取消并退款' : '取消订单'}</button>`
      : ''}
      </div>
    </div>`;
}

/** 已支付的单子：显示「几点前可取消 + 还剩多少分钟」，窗口由后端给 */
function cancelHint(order) {
  if (order.status !== 1 || !order.cancelDeadline) return '';
  const end = new Date(order.cancelDeadline);
  const left = Math.floor((end.getTime() - Date.now()) / 60000);
  if (left <= 0) return '';
  const hm = String(end.getHours()).padStart(2, '0') + ':' + String(end.getMinutes()).padStart(2, '0');
  return `<span style="font-size:12px;color:#e07b39;">${hm} 前可取消，剩余 ${left} 分钟</span>`;
}

function parseAddress(json) {
  if (!json) return '';
  try {
    const a = JSON.parse(json);
    return `${esc(a.receiver || '')} ${esc(a.tel || '')} ${esc((a.province || '') + (a.city || '') + (a.district || '') + (a.detailAddress || ''))}`;
  } catch (e) {
    return '';
  }
}

function bindActions() {
  document.querySelectorAll('.order').forEach((el) => {
    const id = Number(el.dataset.id);

    const payBtn = el.querySelector('[data-act="pay"]');
    if (payBtn) {
      payBtn.addEventListener('click', async () => {
        try {
          await api.put('/api/orders/' + id + '/pay?userId=' + user.id);
          toast('支付成功');
          loadOrders();
        } catch (e) {
          toast(e.message);
        }
      });
    }

    const cancelBtn = el.querySelector('[data-act="cancel"]');
    if (cancelBtn) {
      cancelBtn.addEventListener('click', async () => {
        const paid = !el.querySelector('[data-act="pay"]');
        const msg = paid
          ? '确定取消吗？已支付的金额会原路退回（模拟），库存也会还回去。'
          : '确定要取消这个订单吗？库存会退回。';
        if (!confirm(msg)) return;
        try {
          await api.put('/api/orders/' + id + '/cancel?userId=' + user.id);
          toast('订单已取消');
          loadOrders();
        } catch (e) {
          toast(e.message);
        }
      });
    }
  });
}
