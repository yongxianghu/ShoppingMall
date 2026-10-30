/* 登录 / 注册：界面校验 + 调后端 */

initHeader('');
initFooter();

const query = new URLSearchParams(location.search);
const redirect = query.get('redirect') || 'index.html';

let mode = query.get('tab') === 'register' ? 'register' : 'login';

const els = {
  title: document.getElementById('title'),
  tip: document.getElementById('tip'),
  tabLogin: document.getElementById('tabLogin'),
  tabRegister: document.getElementById('tabRegister'),
  confirmField: document.getElementById('confirmField'),
  nicknameField: document.getElementById('nicknameField'),
  username: document.getElementById('username'),
  password: document.getElementById('password'),
  confirm: document.getElementById('confirm'),
  nickname: document.getElementById('nickname'),
  errUsername: document.getElementById('errUsername'),
  errPassword: document.getElementById('errPassword'),
  errConfirm: document.getElementById('errConfirm'),
  submitBtn: document.getElementById('submitBtn'),
  form: document.getElementById('form')
};

function setMode(next) {
  mode = next;
  const isRegister = next === 'register';
  els.tabLogin.classList.toggle('on', !isRegister);
  els.tabRegister.classList.toggle('on', isRegister);
  els.confirmField.classList.toggle('hidden', !isRegister);
  els.nicknameField.classList.toggle('hidden', !isRegister);
  els.title.textContent = isRegister ? '注册新账号' : '欢迎回来';
  els.tip.textContent = isRegister ? '注册成功会自动登录' : '登录后就能加购物车、下单啦';
  els.submitBtn.textContent = isRegister ? '注册并登录' : '登录';
  clearErrors();
}

function clearErrors() {
  els.errUsername.textContent = '';
  els.errPassword.textContent = '';
  els.errConfirm.textContent = '';
}

function validate() {
  clearErrors();
  let ok = true;
  const username = els.username.value.trim();
  const password = els.password.value;

  if (!username) {
    els.errUsername.textContent = '请输入用户名';
    ok = false;
  } else if (username.length < 3 || username.length > 20) {
    els.errUsername.textContent = '用户名长度需在 3-20 之间';
    ok = false;
  }

  if (!password) {
    els.errPassword.textContent = '请输入密码';
    ok = false;
  } else if (password.length < 6) {
    els.errPassword.textContent = '密码至少 6 位';
    ok = false;
  }

  if (mode === 'register' && password !== els.confirm.value) {
    els.errConfirm.textContent = '两次输入的密码不一致';
    ok = false;
  }
  return ok;
}

els.tabLogin.addEventListener('click', () => setMode('login'));
els.tabRegister.addEventListener('click', () => setMode('register'));

els.form.addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!validate()) return;

  els.submitBtn.disabled = true;
  try {
    const body = {
      username: els.username.value.trim(),
      password: els.password.value
    };

    let user;
    if (mode === 'register') {
      body.nickname = els.nickname.value.trim() || body.username;
      user = await api.post('/api/auth/register', body);
      toast('注册成功');
    } else {
      user = await api.post('/api/auth/login', body);
      toast('登录成功');
    }

    setUser(user);
    setTimeout(() => { location.href = redirect; }, 500);
  } catch (err) {
    toast(err.message);
    els.submitBtn.disabled = false;
  }
});

setMode(mode);
