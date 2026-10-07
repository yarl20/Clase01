/**
 * Luxe Glow Cosmetics - Application Orchestrator
 * Maneja el estado global, el renderizado de componentes y el carrito de compras
 */

const App = {
  currentTab: 'home',
  cart: [],

  init: function() {
    this.render();
  },

  navigateTo: function(tab) {
    this.currentTab = tab;
    this.render();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  },

  refreshView: function() {
    this.render();
  },

  render: function() {
    const appElement = document.getElementById('app');
    if (!appElement) return;

    // Calcular cantidad de items en el carrito
    const cartCount = this.cart.reduce((total, item) => total + item.qty, 0);

    // Obtener vista activa
    let viewContent = '';
    switch (this.currentTab) {
      case 'nosotros':
        viewContent = NosotrosComponent.render();
        break;
      case 'contacto':
        viewContent = ContactoComponent.render();
        break;
      case 'home':
      default:
        viewContent = HomeComponent.render();
        break;
    }

    // Ensamblar interfaz principal
    appElement.innerHTML = `
      ${NavbarComponent.render(this.currentTab, cartCount)}
      
      <main class="container flex-grow-1 py-4">
        ${viewContent}
      </main>

      ${FooterComponent.render()}
      ${this.renderCartOffcanvas()}
      ${this.renderToastContainer()}
    `;
  },

  /* ==========================================================================
     Gestión de Carrito de Compras
     ========================================================================== */
  addToCart: function(productId) {
    const product = HomeComponent.products.find(p => p.id === productId);
    if (!product) return;

    const existingIndex = this.cart.findIndex(item => item.id === productId);
    if (existingIndex > -1) {
      this.cart[existingIndex].qty += 1;
    } else {
      this.cart.push({ id: productId, product: product, qty: 1 });
    }

    this.render();
    this.showToast(`¡<b>${product.name}</b> añadido al carrito!`);
  },

  removeFromCart: function(productId) {
    this.cart = this.cart.filter(item => item.id !== productId);
    this.render();
  },

  updateQty: function(productId, delta) {
    const item = this.cart.find(item => item.id === productId);
    if (item) {
      item.qty += delta;
      if (item.qty <= 0) {
        this.removeFromCart(productId);
      } else {
        this.render();
      }
    }
  },

  renderCartOffcanvas: function() {
    const subtotal = this.cart.reduce((sum, item) => sum + (item.product.price * item.qty), 0);

    return `
      <div class="offcanvas offcanvas-end rounded-start-4" tabindex="-1" id="cartOffcanvas" aria-labelledby="cartOffcanvasLabel">
        <div class="offcanvas-header border-bottom">
          <h5 class="offcanvas-title font-serif text-burgundy fw-bold" id="cartOffcanvasLabel">
            <i class="fas fa-shopping-bag text-gold me-2"></i> Bolsa de Compras
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>
        <div class="offcanvas-body">
          ${this.cart.length === 0 ? `
            <div class="text-center py-5">
              <i class="fas fa-shopping-basket fs-1 text-muted mb-3 opacity-50"></i>
              <p class="text-muted fw-500">Tu carrito está vacío actualmente.</p>
              <button class="btn btn-luxe-outline btn-sm mt-2" data-bs-dismiss="offcanvas">Explorar Cosméticos</button>
            </div>
          ` : `
            <div class="list-group list-group-flush">
              ${this.cart.map(item => `
                <div class="list-group-item px-0 py-3 border-bottom">
                  <div class="d-flex align-items-center">
                    <img src="${item.product.image}" alt="${item.product.name}" class="cart-item-img me-3">
                    <div class="flex-grow-1">
                      <h6 class="mb-0 text-burgundy fw-bold fs-6">${item.product.name}</h6>
                      <small class="text-gold fw-bold">$${item.product.price.toFixed(2)} c/u</small>
                      <div class="d-flex align-items-center mt-2">
                        <button class="btn btn-sm btn-outline-secondary py-0 px-2 rounded-circle" onclick="App.updateQty(${item.id}, -1)">-</button>
                        <span class="mx-2 fw-bold text-dark fs-7">${item.qty}</span>
                        <button class="btn btn-sm btn-outline-secondary py-0 px-2 rounded-circle" onclick="App.updateQty(${item.id}, 1)">+</button>
                      </div>
                    </div>
                    <div class="text-end">
                      <span class="fw-bold text-burgundy fs-6">$${(item.product.price * item.qty).toFixed(2)}</span>
                      <div>
                        <button class="btn btn-sm text-danger border-0 p-0 mt-1" onclick="App.removeFromCart(${item.id})">
                          <i class="fas fa-trash-alt"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              `).join('')}
            </div>
          `}
        </div>
        ${this.cart.length > 0 ? `
          <div class="offcanvas-footer p-3 border-top bg-light">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <span class="fw-bold text-muted">Subtotal:</span>
              <span class="fs-4 fw-bold text-burgundy">$${subtotal.toFixed(2)}</span>
            </div>
            <button class="btn btn-luxe-primary w-100 py-2" onclick="App.checkout()">
              Procesar Compra <i class="fas fa-arrow-right ms-2"></i>
            </button>
          </div>
        ` : ''}
      </div>
    `;
  },

  checkout: function() {
    this.showToast("¡Pedido simulado con éxito! Gracias por tu preferencia en Luxe Glow.");
    this.cart = [];
    this.render();
  },

  renderToastContainer: function() {
    return `<div id="toast-container" class="toast-container position-fixed bottom-0 end-0 p-3"></div>`;
  },

  showToast: function(message) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toastId = 'toast-' + Date.now();
    const toastHtml = `
      <div id="${toastId}" class="toast align-items-center text-white bg-burgundy border-0 show shadow-lg" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
          <div class="toast-body">
            <i class="fas fa-check-circle text-gold me-2"></i> ${message}
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
      </div>
    `;

    container.insertAdjacentHTML('beforeend', toastHtml);
    const toastElem = document.getElementById(toastId);

    setTimeout(() => {
      if (toastElem) toastElem.remove();
    }, 4000);
  }
};

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
  App.init();
});
