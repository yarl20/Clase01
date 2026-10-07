/* Luxe Glow Cosmetics — contacto.js | Validación y toast para contacto.html */
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('contact-form');
  const newsletterForm = document.getElementById('newsletter-form');
  if (form) {
    form.addEventListener('submit', e => {
      e.preventDefault();
      if (!form.checkValidity()) { form.classList.add('was-validated'); return; }
      // Simular envío sin backend
      if (window.Cart) Cart.showToast('¡Mensaje enviado! Te responderemos pronto.');
      else {
        const c = document.getElementById('toast-container');
        if (c) { const id='t-'+Date.now(); c.insertAdjacentHTML('beforeend', `<div id="${id}" class="toast show bg-burgundy text-white"><div class="toast-body">¡Mensaje enviado!</div></div>`); setTimeout(()=>document.getElementById(id)?.remove(),3000); }
      }
      form.reset(); form.classList.remove('was-validated');
    });
  }
  if (newsletterForm) {
    newsletterForm.addEventListener('submit', e => {
      e.preventDefault();
      const input = newsletterForm.querySelector('input[type="email"]');
      if (!input.value || !input.checkValidity()) { input.reportValidity(); return; }
      if (window.Cart) Cart.showToast('¡Gracias! Te avisaremos apenas el módulo esté completo.');
      newsletterForm.reset();
    });
  }
});
