const state = { token: null, productId: null, bestOffer: null };
const $ = id => document.getElementById(id);
const countrySelect = $('countrySelect');
const searchInput = $('searchInput');
const context = $('priceChart').getContext('2d');

const money = (amount, currency) => new Intl.NumberFormat(undefined, {
  style: 'currency', currency
}).format(amount);

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  const response = await fetch(path, { ...options, headers });
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    throw new Error(problem.detail || `Request failed (${response.status})`);
  }
  return response.json();
}

async function search() {
  const query = searchInput.value.trim();
  if (query.length < 2) return;
  $('product-name').textContent = 'Searching…';
  try {
    const results = await api(`/api/products/search?query=${encodeURIComponent(query)}&country=${countrySelect.value}`);
    if (!results.length) throw new Error('No matching products in this market');
    state.productId = results[0].id;
    await loadRecommendation();
  } catch (error) {
    $('product-name').textContent = error.message;
    $('offers-list').innerHTML = '';
  }
}

async function loadRecommendation() {
  const data = await api(`/api/products/${state.productId}/recommendations?country=${countrySelect.value}`);
  const best = data.recommendations[0];
  state.bestOffer = best;
  $('product-name').textContent = `${data.brand} ${data.product}`;
  $('best-price').textContent = money(best.totalPrice, best.currency);
  $('trust-score').textContent = `${Math.round(best.scoreBreakdown.trust)}/100`;
  $('delivery-time').textContent = `${best.deliveryDays} day${best.deliveryDays === 1 ? '' : 's'}`;
  $('explanation-list').innerHTML = best.explanation.map(reason => `<li>${escapeHtml(reason)}</li>`).join('');
  $('offers-list').innerHTML = data.recommendations.map(offer => `
    <div class="offer-item">
      <div><strong>${escapeHtml(offer.retailer)}</strong>
        <div class="offer-meta">${escapeHtml(offer.dealLabel || 'Standard price')} · ${offer.deliveryDays}-day delivery</div>
      </div>
      <span class="offer-chip">${offer.score.toFixed(1)}</span>
      <div class="offer-price">${money(offer.totalPrice, offer.currency)}
        <div class="offer-meta">incl. shipping</div>
      </div>
    </div>`).join('');
  await Promise.all([loadHistory(best.offerId), loadDealInsight(best.offerId, best.explanation)]);
}

async function loadDealInsight(offerId, rankingReasons) {
  const insight = await api(`/api/products/${state.productId}/offers/${offerId}/deal-insight?days=90`);
  $('dealVerdict').textContent = insight.verdict.replaceAll('_', ' ');
  const dealReason = `${insight.explanation} (${insight.sampleSize} observations, median ${money(insight.medianPrice, insight.currency)}).`;
  $('explanation-list').innerHTML = [...rankingReasons, dealReason]
    .map(reason => `<li>${escapeHtml(reason)}</li>`).join('');
}

async function loadHistory(offerId) {
  const data = await api(`/api/products/${state.productId}/offers/${offerId}/price-history?days=30`);
  renderChart(data.history.map(point => Number(point.price)));
}

function renderChart(history) {
  const canvas = $('priceChart');
  const { width, height } = canvas;
  context.clearRect(0, 0, width, height);
  if (!history.length) return;
  const max = Math.max(...history) + 5;
  const min = Math.min(...history) - 5;
  context.strokeStyle = '#38bdf8'; context.lineWidth = 3; context.beginPath();
  history.forEach((point, index) => {
    const x = history.length === 1 ? width / 2 : 12 + index / (history.length - 1) * (width - 24);
    const y = height - 18 - (point - min) / (max - min || 1) * (height - 36);
    index ? context.lineTo(x, y) : context.moveTo(x, y);
  });
  context.stroke();
}

async function loginAndLoadAlerts() {
  try {
    const auth = await api('/api/auth/login', { method: 'POST', body: JSON.stringify({
      username: 'demo@priceintel.dev', password: 'demo-password'
    }) });
    state.token = auth.accessToken;
    await loadAlerts();
  } catch (error) { $('watchlist').innerHTML = `<li>${escapeHtml(error.message)}</li>`; }
}

async function loadAlerts() {
  const alerts = await api('/api/alerts');
  $('watchlist').innerHTML = alerts.length ? alerts.map(alert => `
    <li class="watch-item"><div><strong>${escapeHtml(alert.product)}</strong>
      <div class="offer-meta">${alert.country} · ${escapeHtml(alert.channel)}</div></div>
      <span class="offer-chip">Target ${alert.targetPrice}</span></li>`).join('')
    : '<li class="offer-meta">No alerts yet. Search, then add one.</li>';
}

async function addAlert() {
  if (!state.productId || !state.bestOffer) return;
  await api('/api/alerts', { method: 'POST', body: JSON.stringify({
    productId: state.productId, country: countrySelect.value,
    targetPrice: (state.bestOffer.totalPrice * 0.95).toFixed(2), channel: 'email'
  }) });
  await loadAlerts();
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[char]);
}

$('searchBtn').addEventListener('click', search);
searchInput.addEventListener('keydown', event => { if (event.key === 'Enter') search(); });
countrySelect.addEventListener('change', search);
$('addAlertBtn').addEventListener('click', () => addAlert().catch(error => alert(error.message)));

loginAndLoadAlerts();
search();
