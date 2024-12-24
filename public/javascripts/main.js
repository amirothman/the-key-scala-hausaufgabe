const socket = new WebSocket(`ws://${window.location.host}/ws/wordCount`);
const status = document.getElementById('status');
const tableBody = document.querySelector('#word-count-table tbody');

socket.onopen = () => (status.textContent = 'Status: Connected');
socket.onclose = () => (status.textContent = 'Status: Disconnected');
socket.onmessage = (event) => {
  const wordCounts = JSON.parse(event.data);
  tableBody.innerHTML = '';
  for (const [word, count] of Object.entries(wordCounts)) {
    const row = document.createElement('tr');
    row.innerHTML = `<td>${word}</td><td>${count}</td>`;
    tableBody.appendChild(row);
  }
};
