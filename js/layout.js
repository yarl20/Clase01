/* Luxe Glow Cosmetics — layout.js (header + footer global) */
const Layout = {
  navItems: [
    { href: 'index.html', label: 'Inicio', icon: 'fa-store', key: 'index' },
    { href: 'tienda.html', label: 'Tienda', icon: 'fa-bag-shopping', key: 'tienda' },
    { href: 'nosotros.html', label: 'Nosotros', icon: 'fa-leaf', key: 'nosotros' }
  ],
  dropdownItems: [
    { href: 'ritual.html', label: 'Ritual de Cuidado', icon: 'fa-wand-magic-sparkles', key: 'ritual' },
    { href: 'ingredientes.html', label: 'Ingredientes', icon: 'fa-flask', key: 'ingredientes' },
    { href: 'diario.html', label: 'Diario', icon: 'fa-book-open', key: 'diario' },
    { href: 'faq.html', label: 'FAQ', icon: 'fa-circle-question', key: 'faq' },
    { href: 'envios.html', label: 'Envíos & Devoluciones', icon: 'fa-truck', key: 'envios', dividerBefore: true }
  ],
  getCurrentKey() {
    const file = (window.location.pathname.split('/').pop() || 'index.html').toLowerCase();
    if (file === '' || file === '/') return 'index';
    const name = file.replace('.html','');
    return name || 'index';
  },
  renderHeader() {
    const current = this.getCurrentKey();
    const isDescubreActive = this.dropdownItems.some(i => i.key === current);
    const navLinks = this.navItems.map(item => {
      const active = current === item.key ? ' active" aria-current="page"' : '"';
      return `<li class="nav-item"><a class="nav-link nav-link-luxe${active} href="${item.href}"><i class="fas ${item.icon} me-1"></i> ${item.label}</a></li>`;
    }).join('');
    const dropdownLinks = this.dropdownItems.map(item => {
      if (item.dividerBefore) return `<li><hr class="dropdown-divider"></li><li><a class="dropdown-item${current===item.key?' active':''}" href="${item.href}"><i class="fas ${item.icon} me-2 text-gold"></i>${item.label}</a></li>`;
      return `<li><a class="dropdown-item${current===item.key?' active':''}" href="${item.href}"><i class="fas ${item.icon} me-2 text-gold"></i>${item.label}</a></li>`;
    }).join('');
    return `
  <div class="topbar-luxe">
    <div class="container d-flex flex-wrap justify-content-between align-items-center gap-2">
      <span><i class="fas fa-truck me-2 text-gold"></i>Envío Express Gratis en compras superiores a $50</span>
      <span class="d-none d-md-inline"><i class="fas fa-envelope me-1"></i> contacto@luxeglow.com &nbsp;|&nbsp; <i class="fas fa-phone me-1"></i> +52 (55) 8900-3456</span>
    </div>
  </div>
  <nav class="navbar navbar-expand-lg navbar-luxe sticky-top">
    <div class="container">
      <a class="navbar-brand navbar-brand-luxe" href="index.html">
        <span class="brand-icon"><i class="fas fa-spa"></i></span>
        Luxe <span>Glow</span>
      </a>
      <button class="navbar-toggler border-0" type="button" data-bs-toggle="collapse" data-bs-target="#navbarLuxeContent" aria-controls="navbarLuxeContent" aria-expanded="false" aria-label="Toggle navigation">
        <i class="fas fa-bars text-burgundy fs-4"></i>
      </button>
      <div class="collapse navbar-collapse" id="navbarLuxeContent">
        <ul class="navbar-nav mx-auto mb-2 mb-lg-0 align-items-center">
          ${navLinks}
          <li class="nav-item dropdown">
            <a class="nav-link nav-link-luxe dropdown-toggle${isDescubreActive?' active':''}" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false"><i class="fas fa-sparkles me-1"></i> Descubre</a>
            <ul class="dropdown-menu dropdown-menu-luxe">
              ${dropdownLinks}
            </ul>
          </li>
          <li class="nav-item"><a class="nav-link nav-link-luxe${current==='contacto'?' active" aria-current="page"':''}" href="contacto.html"><i class="fas fa-envelope me-1"></i> Contacto</a></li>
        </ul>
        <div class="d-flex align-items-center justify-content-center mt-3 mt-lg-0">
          <button class="btn cart-icon-btn d-flex align-items-center" data-bs-toggle="offcanvas" data-bs-target="#cartOffcanvas" aria-label="Abrir carrito">
            <i class="fas fa-shopping-bag fs-5 me-2"></i>
            <span>Mi Carrito</span>
            <span class="cart-badge" id="cart-count">0</span>
          </button>
        </div>
      </div>
    </div>
  </nav>`;
  },
  renderFooter() {
    return `
  <footer class="footer-luxe">
    <div class="container">
      <div class="row g-4 mb-4">
        <div class="col-lg-4 col-md-6">
          <h3 class="footer-brand">Luxe <span>Glow</span></h3>
          <p class="small text-white-50 mb-3">Cosmética de lujo formulada con ingredientes naturales de alta eficacia para realzar tu belleza intrínseca.</p>
          <div>
            <a href="#" class="social-icon" aria-label="Instagram"><i class="fab fa-instagram"></i></a>
            <a href="#" class="social-icon" aria-label="Facebook"><i class="fab fa-facebook-f"></i></a>
            <a href="#" class="social-icon" aria-label="Pinterest"><i class="fab fa-pinterest-p"></i></a>
            <a href="#" class="social-icon" aria-label="TikTok"><i class="fab fa-tiktok"></i></a>
          </div>
        </div>
        <div class="col-lg-2 col-md-6">
          <h6 class="text-uppercase text-gold fw-bold mb-3">Navegación</h6>
          <ul class="list-unstyled">
            <li><a href="index.html" class="footer-link">Inicio</a></li>
            <li><a href="tienda.html" class="footer-link">Tienda</a></li>
            <li><a href="nosotros.html" class="footer-link">Nosotros</a></li>
            <li><a href="contacto.html" class="footer-link">Contacto</a></li>
          </ul>
        </div>
        <div class="col-lg-3 col-md-6">
          <h6 class="text-uppercase text-gold fw-bold mb-3">Categorías</h6>
          <ul class="list-unstyled">
            <li><a href="index.html#catalogo" class="footer-link">Cuidado Facial</a></li>
            <li><a href="index.html#catalogo" class="footer-link">Maquillaje Velvet</a></li>
            <li><a href="index.html#catalogo" class="footer-link">Fragancias Exclusivas</a></li>
          </ul>
        </div>
        <div class="col-lg-3 col-md-6">
          <h6 class="text-uppercase text-gold fw-bold mb-3">Atención al Cliente</h6>
          <p class="small text-white-50 mb-2"><i class="fas fa-map-marker-alt text-gold me-2"></i> Av. Luxe Beauty 102, Spa District</p>
          <p class="small text-white-50 mb-2"><i class="fas fa-phone text-gold me-2"></i> +52 (55) 8900-3456</p>
          <p class="small text-white-50 mb-0"><i class="fas fa-envelope text-gold me-2"></i> contacto@luxeglow.com</p>
        </div>
      </div>
      <div class="d-flex flex-wrap gap-3 justify-content-center small py-3" style="border-top:1px solid rgba(255,255,255,.1);border-bottom:1px solid rgba(255,255,255,.1);">
        <a href="ritual.html" class="footer-link mb-0">Ritual</a>
        <a href="ingredientes.html" class="footer-link mb-0">Ingredientes</a>
        <a href="diario.html" class="footer-link mb-0">Diario</a>
        <a href="faq.html" class="footer-link mb-0">FAQ</a>
        <a href="envios.html" class="footer-link mb-0">Envíos &amp; Devoluciones</a>
      </div>
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-center pt-3">
        <p class="small text-white-50 mb-2 mb-md-0">&copy; 2026 Luxe Glow Cosmetics. Todos los derechos reservados.</p>
        <p class="small text-white-50 mb-0">Diseñado con Bootstrap 5 &amp; Vanilla JS</p>
      </div>
    </div>
  </footer>
  <div class="offcanvas offcanvas-end rounded-start-4" tabindex="-1" id="cartOffcanvas" aria-labelledby="cartOffcanvasLabel">
    <div class="offcanvas-header border-bottom">
      <h5 class="offcanvas-title font-serif text-burgundy fw-bold" id="cartOffcanvasLabel"><i class="fas fa-shopping-bag text-gold me-2"></i> Bolsa de Compras</h5>
      <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body" id="cart-body"></div>
    <div class="offcanvas-footer p-3 border-top bg-light" id="cart-footer"></div>
  </div>
  <div id="toast-container" class="toast-container position-fixed bottom-0 end-0 p-3"></div>`;
  },
  init() {
    const headerEl = document.getElementById('site-header');
    const footerEl = document.getElementById('site-footer');
    if (headerEl) headerEl.innerHTML = this.renderHeader();
    if (footerEl) footerEl.innerHTML = this.renderFooter();
  }
};
document.addEventListener('DOMContentLoaded', () => Layout.init());
