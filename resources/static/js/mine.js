/* 个人中心：资料 + 收货地址 */

const user = getUser();
if (requireLogin()) {
  initHeader('');
  initFooter();
  loadUser();
  loadAddresses();
}

async function loadUser() {
  const card = document.getElementById('userCard');
  try {
    const u = await api.get('/api/user/' + user.id);
    card.innerHTML = `
      <div class="avatar">${esc((u.nickname || u.username || '?').slice(0, 1))}</div>
      <div style="text-align:center;font-size:15px;font-weight:600;">${esc(u.nickname || u.username)}</div>
      <div style="text-align:center;font-size:12px;color:#9aa3b5;margin:6px 0 18px;">@${esc(u.username)}</div>

      <div class="field">
        <label>昵称</label>
        <input id="nickname" value="${esc(u.nickname || '')}">
      </div>
      <div class="field">
        <label>手机号</label>
        <input id="tel" value="${esc(u.tel || '')}" placeholder="选填">
      </div>
      <button class="btn-primary" id="saveBtn" style="height:40px;font-size:14px;">保存资料</button>

      <div style="margin-top:20px;font-size:13px;color:#5a6478;line-height:2;">
        <a href="orders.html" style="color:#1a6bff;">我的订单 →</a><br>
        <a href="cart.html" style="color:#1a6bff;">我的购物车 →</a>
      </div>`;

    document.getElementById('saveBtn').addEventListener('click', async () => {
      try {
        await api.put('/api/user/' + user.id, {
          nickname: document.getElementById('nickname').value.trim(),
          tel: document.getElementById('tel').value.trim()
        });
        const stored = getUser();
        stored.nickname = document.getElementById('nickname').value.trim();
        stored.tel = document.getElementById('tel').value.trim();
        setUser(stored);
        toast('已保存');
        initHeader('');
      } catch (e) {
        toast(e.message);
      }
    });
  } catch (e) {
    card.innerHTML = '<div class="empty">' + esc(e.message) + '</div>';
  }
}

async function loadAddresses() {
  const panel = document.getElementById('addrPanel');
  try {
    const list = await api.get('/api/addresses?userId=' + user.id);

    panel.innerHTML = `
      <h4>收货地址</h4>
      <div id="addrList">
        ${list.length ? list.map((a) => `
          <div class="addr" data-id="${a.id}">
            <div>
              <b>${esc(a.receiver)}</b>　${esc(a.tel)}
              ${a.isDefault === 1 ? '<span class="tag">默认</span>' : ''}
              <div style="color:#7a8296;font-size:12px;margin-top:4px;">
                ${esc((a.province || '') + (a.city || '') + (a.district || '') + (a.detailAddress || ''))}
              </div>
            </div>
            <div class="ops">
              ${a.isDefault === 1 ? '' : '<span data-act="default">设为默认</span>'}
              <span data-act="del">删除</span>
            </div>
          </div>`).join('') : '<div style="color:#9aa3b5;font-size:13px;padding:10px 0;">还没有地址，在下面添加一个吧</div>'}
      </div>

      <h4 style="margin-top:26px;">新增地址</h4>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
        <div class="field"><label>收件人</label><input id="receiver" placeholder="姓名"></div>
        <div class="field"><label>手机号</label><input id="atel" placeholder="11 位手机号"></div>
        <div class="field"><label>省</label><input id="province" placeholder="如 江西省"></div>
        <div class="field"><label>市</label><input id="city" placeholder="如 南昌市"></div>
        <div class="field"><label>区/县</label><input id="district" placeholder="如 新建区"></div>
        <div class="field"><label>详细地址</label><input id="detail" placeholder="街道、门牌号"></div>
      </div>
      <button class="btn-primary" id="addAddrBtn" style="height:40px;font-size:14px;">保存地址</button>`;

    bindAddressActions();
  } catch (e) {
    panel.innerHTML = '<div class="empty">' + esc(e.message) + '</div>';
  }
}

function bindAddressActions() {
  document.querySelectorAll('.addr').forEach((el) => {
    const id = Number(el.dataset.id);

    const defBtn = el.querySelector('[data-act="default"]');
    if (defBtn) {
      defBtn.addEventListener('click', async () => {
        try {
          await api.put('/api/addresses/' + id + '/default?userId=' + user.id);
          toast('已设为默认');
          loadAddresses();
        } catch (e) { toast(e.message); }
      });
    }

    el.querySelector('[data-act="del"]').addEventListener('click', async () => {
      if (!confirm('确定删除这个地址吗？')) return;
      try {
        await api.del('/api/addresses/' + id + '?userId=' + user.id);
        toast('已删除');
        loadAddresses();
      } catch (e) { toast(e.message); }
    });
  });

  document.getElementById('addAddrBtn').addEventListener('click', async () => {
    const body = {
      receiver: document.getElementById('receiver').value.trim(),
      tel: document.getElementById('atel').value.trim(),
      province: document.getElementById('province').value.trim(),
      city: document.getElementById('city').value.trim(),
      district: document.getElementById('district').value.trim(),
      detailAddress: document.getElementById('detail').value.trim()
    };
    if (!body.receiver || !body.tel) return toast('收件人和手机号不能为空');

    try {
      await api.post('/api/addresses?userId=' + user.id, body);
      toast('地址已保存');
      loadAddresses();
    } catch (e) {
      toast(e.message);
    }
  });
}
