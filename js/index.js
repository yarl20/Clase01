/* Luxe Glow Cosmetics — index.js | Filtro de categorías para index.html */
document.addEventListener('DOMContentLoaded', () => {
  const pills = document.querySelectorAll('[data-filter]');
  const cards = document.querySelectorAll('[data-category]');
  if (!pills.length || !cards.length) return;
  pills.forEach(btn => {
    btn.addEventListener('click', () => {
      const cat = btn.getAttribute('data-filter');
      pills.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      cards.forEach(card => {
        const c = card.getAttribute('data-category');
        const show = cat === 'all' || c === cat;
        card.style.display = show ? '' : 'none';
      });
    });
  });
});
