/* 统一的请求封装：所有页面都通过它访问后端 */

const API_BASE = '';   // 和页面同源，留空即可

async function apiRequest(path, options = {}) {
  const config = {
    method: options.method || 'GET',
    headers: {}
  };
  if (options.body !== undefined && options.body !== null) {
    config.headers['Content-Type'] = 'application/json';
    config.body = JSON.stringify(options.body);
  }

  let res;
  try {
    res = await fetch(API_BASE + path, config);
  } catch (e) {
    throw new Error('连不上后端，确认服务已经启动');
  }

  let json;
  try {
    json = await res.json();
  } catch (e) {
    throw new Error('返回内容不是 JSON（HTTP ' + res.status + '）');
  }

  if (json.code !== 200) {
    throw new Error(json.msg || '请求失败');
  }
  return json.data;
}

const api = {
  get: (path) => apiRequest(path),
  post: (path, body) => apiRequest(path, { method: 'POST', body }),
  put: (path, body) => apiRequest(path, { method: 'PUT', body }),
  del: (path) => apiRequest(path, { method: 'DELETE' })
};

/* 图片路径里可能有空格，转义一下再给 src */
function imgUrl(path) {
  if (!path) return '';
  return encodeURI(path);
}
