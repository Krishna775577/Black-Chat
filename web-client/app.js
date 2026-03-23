const state = {
  tab: 'chats',
  query: '',
  chats: [
    { title: 'Aarav', subtitle: 'Pinned • editable messages • secret mode ready', preview: ['You: Updated the roadmap (edited)', 'Forward trace: from Team Alpha', 'Self-destruct image: 30 seconds'] },
    { title: 'Office Team', subtitle: 'Polls, broadcasts and HD group call', preview: ['Poll: Ship folders this week?', 'Broadcast draft scheduled', 'Screen share started by Nikita'] },
    { title: 'Design Lab', subtitle: 'Profile themes and story filters', preview: ['New profile frame: Emerald Glass', 'Story filter applied: Vintage Soft', 'QR invite generated'] },
  ],
  folders: [
    { title: 'Work', subtitle: '2 chats • 4 unread', preview: ['Pinned: Office Team', 'Archive rule: muted chats auto archive', 'Advanced search: docs + links'] },
    { title: 'Family', subtitle: '1 chat • 1 unread', preview: ['Pinned: Mom', 'Silent archive enabled', 'Theme pack: Glass Mint'] },
  ],
  calls: [
    { title: 'Sprint Review', subtitle: 'HD video • WebRTC • recording on', preview: ['Participants: 4', 'Screen share: active', 'Background blur: on'] },
    { title: 'Client Walkthrough', subtitle: 'Jitsi backup room', preview: ['Waiting room ready', 'Presenter controls available', 'QR room invite available'] },
  ],
  live: [
    { title: 'Weekend Product Walkthrough', subtitle: '38 watching now', preview: ['Host: You', 'Mode: video live room', 'Poll overlay enabled'] },
    { title: 'Audio AMA', subtitle: '14 listening', preview: ['Mode: audio only', 'Moderation tools enabled', 'Broadcast clip export ready'] },
  ],
  devices: [
    { title: 'Work Laptop', subtitle: 'Chrome • active now', preview: ['QR paired', 'Secret chat mirror disabled', 'File sync enabled'] },
    { title: 'Home Desktop', subtitle: 'Edge • last seen 4 days ago', preview: ['Reconnect required', 'App lock extension pending', 'Theme sync supported'] },
  ],
};
const listContainer = document.getElementById('listContainer');
const chatPreview = document.getElementById('chatPreview');
const searchBox = document.getElementById('searchBox');
const buttons = [...document.querySelectorAll('.tab-btn')];
function getItems() {
  const all = state[state.tab] || [];
  if (!state.query.trim()) return all;
  return all.filter(item => `${item.title} ${item.subtitle}`.toLowerCase().includes(state.query.toLowerCase()));
}
function renderList() {
  const items = getItems();
  listContainer.innerHTML = items.map((item, index) => `
    <button class="list-item" data-index="${index}">
      <strong>${item.title}</strong>
      <div class="muted">${item.subtitle}</div>
    </button>
  `).join('');
  listContainer.querySelectorAll('.list-item').forEach(button => {
    button.addEventListener('click', () => renderPreview(items[Number(button.dataset.index)]));
  });
  renderPreview(items[0]);
}
function renderPreview(item) {
  if (!item) { chatPreview.innerHTML = '<div class="muted">No results</div>'; return; }
  const previewLines = item.preview || [item.subtitle, 'Preview unavailable'];
  chatPreview.innerHTML = previewLines.map(line => `<div class="list-item">${line}</div>`).join('');
}
buttons.forEach(button => {
  button.addEventListener('click', () => {
    buttons.forEach(btn => btn.classList.remove('active'));
    button.classList.add('active');
    state.tab = button.dataset.tab;
    renderList();
  });
});
searchBox.addEventListener('input', (event) => { state.query = event.target.value; renderList(); });
renderList();
