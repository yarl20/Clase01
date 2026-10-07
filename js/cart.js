/* Luxe Glow Cosmetics — cart.js | Gestión de carrito con localStorage, offcanvas y toast */
const Cart = {
  storageKey: 'luxe_cart',
  products: [
    { id:101, name:"Sérum Facial Rose Radiance", category:"facial", categoryName:"Cuidado Facial", price:49.99, rating:5.0, reviews:124, badge:"Más Vendido", image:"assets/images/serum_rose.png", description:"Sérum concentrado con extractos puros de rosas botánicas y ácido hialurónico para una piel luminosa e hidratada." },
    { id:102, name:"Labial Velvet Gold Matte", category:"maquillaje", categoryName:"Maquillaje", price:29.50, rating:4.8, reviews:89, badge:"Edición Lujo", image:"assets/images/lipstick_velvet.png", description:"Labial mate de textura sedosa de larga duración en estuche dorado bañado con pigmentos intensos." },
    { id:103, name:"Crema Hidratante Hydra Glow", category:"facial", categoryName:"Cuidado Facial", price:58.00, rating:4.9, reviews:96, badge:"Orgánico", image:"assets/images/cream_hydra.png", description:"Crema nutritiva ultra ligera que restaura la barrera cutánea con péptidos naturales y acabado sedoso." },
    { id:104, name:"Perfume Éclat D'Or Luxury", category:"fragancias", categoryName:"Fragancias", price:85.00, rating:5.0, reviews:67, badge:"Exclusivo", image:"assets/images/perfume_luxe.png", description:"Fragancia envolvente con notas de jazmín real, ambar cálido y flor de azahar en frasco de cristal tallado." }
  ],
  cart: [],
  _load(){ try{ const raw=localStorage.getItem(this.storageKey); if(raw) this.cart=JSON.parse(raw);}catch(e){ this.cart=[]; } },
  _save(){ try{ localStorage.setItem(this.storageKey, JSON.stringify(this.cart)); }catch(e){} },
  _count(){ return this.cart.reduce((t,i)=>t+i.qty,0); },
  _subtotal(){ return this.cart.reduce((s,i)=>s+i.product.price*i.qty,0); },
  init(){
    this._load();
    this.updateBadge();
    this.renderOffcanvas();
  },
  updateBadge(){
    const el=document.getElementById('cart-count');
    if(el) el.textContent=this._count();
    const el2=document.getElementById('navbar-cart-count');
    if(el2) el2.textContent=this._count();
  },
  add(productId){
    const product=this.products.find(p=>p.id===productId);
    if(!product) return;
    const idx=this.cart.findIndex(i=>i.id===productId);
    if(idx>-1) this.cart[idx].qty+=1; else this.cart.push({id:productId, product, qty:1});
    this._save(); this.updateBadge(); this.renderOffcanvas();
    this.showToast(`¡<b>${product.name}</b> añadido al carrito!`);
    // abrir offcanvas si Bootstrap disponible
    const off=document.getElementById('cartOffcanvas');
    if(off && window.bootstrap){ try{ bootstrap.Offcanvas.getOrCreateInstance(off).show(); }catch(e){} }
  },
  remove(productId){
    this.cart=this.cart.filter(i=>i.id!==productId);
    this._save(); this.updateBadge(); this.renderOffcanvas();
  },
  updateQty(productId, delta){
    const item=this.cart.find(i=>i.id===productId);
    if(!item) return;
    item.qty+=delta;
    if(item.qty<=0) this.remove(productId);
    else { this._save(); this.updateBadge(); this.renderOffcanvas(); }
  },
  checkout(){
    this.showToast("¡Pedido simulado con éxito! Gracias por tu preferencia en Luxe Glow.");
    this.cart=[]; this._save(); this.updateBadge(); this.renderOffcanvas();
    const off=document.getElementById('cartOffcanvas');
    if(off && window.bootstrap){ try{ bootstrap.Offcanvas.getInstance(off)?.hide(); }catch(e){} }
  },
  renderOffcanvas(){
    const body=document.getElementById('cart-body');
    const footer=document.getElementById('cart-footer');
    if(!body || !footer) return;
    const count=this._count();
    if(count===0){
      body.innerHTML=`<div class="text-center py-5"><i class="fas fa-shopping-basket fs-1 text-muted mb-3 opacity-50"></i><p class="text-muted fw-500">Tu carrito está vacío actualmente.</p><button class="btn btn-luxe-outline btn-sm mt-2" data-bs-dismiss="offcanvas">Explorar Cosméticos</button></div>`;
      footer.innerHTML='';
      footer.classList.add('d-none');
    } else {
      footer.classList.remove('d-none');
      body.innerHTML=`<div class="list-group list-group-flush">${this.cart.map(item=>`
        <div class="list-group-item px-0 py-3 border-bottom">
          <div class="d-flex align-items-center">
            <img src="${item.product.image}" alt="${item.product.name}" class="cart-item-img me-3">
            <div class="flex-grow-1">
              <h6 class="mb-0 text-burgundy fw-bold fs-6">${item.product.name}</h6>
              <small class="text-gold fw-bold">$${item.product.price.toFixed(2)} c/u</small>
              <div class="d-flex align-items-center mt-2">
                <button class="btn btn-sm btn-outline-secondary py-0 px-2 rounded-circle" onclick="Cart.updateQty(${item.id},-1)">-</button>
                <span class="mx-2 fw-bold text-dark">${item.qty}</span>
                <button class="btn btn-sm btn-outline-secondary py-0 px-2 rounded-circle" onclick="Cart.updateQty(${item.id},1)">+</button>
              </div>
            </div>
            <div class="text-end">
              <span class="fw-bold text-burgundy fs-6">$${(item.product.price*item.qty).toFixed(2)}</span>
              <div><button class="btn btn-sm text-danger border-0 p-0 mt-1" onclick="Cart.remove(${item.id})"><i class="fas fa-trash-alt"></i></button></div>
            </div>
          </div>
        </div>`).join('')}</div>`;
      footer.innerHTML=`<div class="d-flex justify-content-between align-items-center mb-3"><span class="fw-bold text-muted">Subtotal:</span><span class="fs-4 fw-bold text-burgundy">$${this._subtotal().toFixed(2)}</span></div><button class="btn btn-luxe-primary w-100 py-2" onclick="Cart.checkout()">Procesar Compra <i class="fas fa-arrow-right ms-2"></i></button>`;
    }
  },
  showToast(message){
    const container=document.getElementById('toast-container');
    if(!container) return;
    const id='toast-'+Date.now();
    const html=`<div id="${id}" class="toast align-items-center text-white bg-burgundy border-0 show shadow-lg" role="alert" aria-live="assertive" aria-atomic="true"><div class="d-flex"><div class="toast-body"><i class="fas fa-check-circle text-gold me-2"></i> ${message}</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button></div></div>`;
    container.insertAdjacentHTML('beforeend', html);
    const el=document.getElementById(id);
    setTimeout(()=>{ if(el) el.remove(); },4000);
  }
};
document.addEventListener('DOMContentLoaded', ()=> Cart.init());
