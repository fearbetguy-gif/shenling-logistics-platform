const toast = document.querySelector('#toast');
let toastTimer;
const loginScreen = document.querySelector('#loginScreen');
const appShell = document.querySelector('.app-shell');
const loginError = document.querySelector('#loginError');
const apiBase = 'http://localhost:8080/api';
function enterDashboard(user) {
  loginScreen.hidden = true;
  appShell.hidden = false;
  if (user?.nickname) document.querySelector('#profileName').textContent = user.nickname;
  if (localStorage.getItem('sl_token')) loadOrders();
}
function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char]));
}
async function loadOrders() {
  try {
    const response = await fetch(`${apiBase}/order/list`, { headers: { Authorization: `Bearer ${localStorage.getItem('sl_token')}` } });
    const result = await response.json();
    if (!response.ok || result.code !== 200 || !Array.isArray(result.data) || result.data.length === 0) return;
    document.querySelector('#orderTable').innerHTML = result.data.slice(0, 8).map((order) => {
      const statusClass = { TRANSPORTING: 'delivering', WAIT_PICKUP: 'pickup', SIGNED: 'signed', CANCELLED: 'cancelled' }[order.status] || 'pickup';
      const statusText = { TRANSPORTING: '运输中', WAIT_PICKUP: '待揽收', SIGNED: '已签收', CANCELLED: '已取消' }[order.status] || order.status;
      const search = escapeHtml(`${order.orderNo} ${order.senderName} ${order.receiverName}`);
      return `<tr data-search="${search}"><td><strong class="order-no">${escapeHtml(order.orderNo)}</strong></td><td><strong>${escapeHtml(order.senderName)} <span class="arrow">→</span> ${escapeHtml(order.receiverName)}</strong><small>${escapeHtml(order.goodsType)}</small></td><td>${escapeHtml(order.senderAddress)} → ${escapeHtml(order.receiverAddress)}</td><td><span class="status ${statusClass}">${escapeHtml(statusText)}</span></td><td>${order.createdAt ? escapeHtml(order.createdAt.slice(11, 16)) : '-'}</td><td><button class="row-more" data-order="${escapeHtml(order.orderNo)}">···</button></td></tr>`;
    }).join('');
    document.querySelectorAll('.row-more').forEach((button) => button.addEventListener('click', () => showToast(`已选中订单 ${button.dataset.order}`)));
  } catch (error) {
    console.info('订单服务暂不可用，继续使用页面样例数据', error);
  }
}
if (localStorage.getItem('sl_demo_mode') === 'true' || localStorage.getItem('sl_token')) {
  enterDashboard(JSON.parse(localStorage.getItem('sl_user') || 'null'));
} else {
  appShell.hidden = true;
}
document.querySelector('#togglePassword').addEventListener('click', (event) => {
  const input = document.querySelector('#passwordInput');
  input.type = input.type === 'password' ? 'text' : 'password';
  event.currentTarget.textContent = input.type === 'password' ? '显示' : '隐藏';
});
document.querySelector('#demoBtn').addEventListener('click', () => {
  localStorage.setItem('sl_demo_mode', 'true');
  enterDashboard({ nickname: '林一舟' });
  showToast('已进入演示模式');
});
document.querySelector('#loginForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  loginError.textContent = '';
  const submit = event.currentTarget.querySelector('.login-submit');
  submit.disabled = true;
  submit.querySelector('span').textContent = '正在登录…';
  try {
    const response = await fetch(`${apiBase}/user/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ phone: phoneInput.value.trim(), password: passwordInput.value }) });
    const result = await response.json();
    if (!response.ok || result.code !== 200) throw new Error(result.message || '登录失败');
    localStorage.setItem('sl_token', result.data.token);
    localStorage.setItem('sl_user', JSON.stringify(result.data.user));
    localStorage.removeItem('sl_demo_mode');
    enterDashboard(result.data.user);
  } catch (error) {
    loginError.textContent = error.message.includes('Failed to fetch') ? '无法连接用户服务，请先启动 Gateway；也可以使用演示模式。' : error.message;
  } finally {
    submit.disabled = false;
    submit.querySelector('span').textContent = '登录运营台';
  }
});
document.querySelector('#logoutBtn').addEventListener('click', () => {
  localStorage.removeItem('sl_token');
  localStorage.removeItem('sl_user');
  localStorage.removeItem('sl_demo_mode');
  appShell.hidden = true;
  loginScreen.hidden = false;
  showToast('已退出登录');
});
function showToast(message) {
  toast.textContent = message;
  toast.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('show'), 2200);
}

document.querySelector('#newOrderBtn').addEventListener('click', () => showToast('订单创建入口将在接入订单服务后启用'));
document.querySelectorAll('.row-more').forEach((button) => {
  button.addEventListener('click', () => showToast(`已选中订单 ${button.dataset.order}`));
});

document.querySelector('#searchInput').addEventListener('input', (event) => {
  const keyword = event.target.value.trim().toLowerCase();
  document.querySelectorAll('#orderTable tr').forEach((row) => {
    row.hidden = keyword && !row.dataset.search.toLowerCase().includes(keyword);
  });
});
