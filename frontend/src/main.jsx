import React,{useEffect,useState}from'react';
import{createRoot}from'react-dom/client';
import{BrowserRouter,Routes,Route,Link,useNavigate,useParams,useSearchParams,useLocation}from'react-router-dom';
import axios from'axios';
import'./style.css';

const API='http://localhost:8080/api';
const api=axios.create({baseURL:API});
api.interceptors.request.use(c=>{const t=localStorage.getItem('token');if(t)c.headers.Authorization='Bearer '+t;return c});

function useRememberScroll(key){
 useEffect(()=>{
  const saved=sessionStorage.getItem('vinkart-scroll:'+key);
  if(saved!==null){const y=Number(saved);setTimeout(()=>window.scrollTo(0,y),80);}
  const save=()=>sessionStorage.setItem('vinkart-scroll:'+key,String(window.scrollY));
  window.addEventListener('scroll',save,{passive:true});
  return()=>{save();window.removeEventListener('scroll',save)};
 },[key]);
}

const offerBanners=[
 {title:'Mega Mobile Deals',text:'Up to 40% OFF on mobiles & accessories',cta:'Shop Mobiles',to:'/products',className:'offer-blue'},
 {title:'Fashion Fiesta',text:'Fresh styles from ₹499 • Multiple sizes available',cta:'Shop Fashion',to:'/products',className:'offer-pink'},
 {title:'Laptop Upgrade Sale',text:'Powerful laptops with exciting discounts',cta:'Explore Laptops',to:'/products',className:'offer-purple'},
 {title:'Vinkart Super Saver',text:'Big savings across your favourite categories',cta:'Shop Now',to:'/products',className:'offer-green'}
];
function OfferBanners(){
 const[index,setIndex]=useState(0);
 const touchStart=React.useRef(null);
 useEffect(()=>{const timer=setInterval(()=>setIndex(i=>(i+1)%offerBanners.length),4500);return()=>clearInterval(timer)},[]);
 const go=(direction)=>setIndex(i=>(i+direction+offerBanners.length)%offerBanners.length);
 return <section className="offer-carousel" aria-label="Special offers"
  onTouchStart={e=>{touchStart.current=e.touches[0].clientX}}
  onTouchEnd={e=>{if(touchStart.current===null)return;const dx=e.changedTouches[0].clientX-touchStart.current;if(Math.abs(dx)>45)go(dx<0?1:-1);touchStart.current=null}}>
  <div className="offer-track" style={{transform:`translateX(-${index*100}%)`}}>
   {offerBanners.map((b,i)=><article key={b.title} className={'offer-banner '+b.className}>
    <div className="offer-content"><span className="offer-badge">LIMITED TIME OFFER</span><h2>{b.title}</h2><p>{b.text}</p><Link className="offer-btn" to={b.to}>{b.cta} →</Link></div>
    <div className="offer-art"><span>SALE</span><strong>{i===0?'40%':i===1?'₹499':i===2?'30%':'BIG'}</strong><small>OFF</small></div>
   </article>)}
  </div>
  <button className="offer-arrow offer-prev" aria-label="Previous offer" onClick={()=>go(-1)}>‹</button>
  <button className="offer-arrow offer-next" aria-label="Next offer" onClick={()=>go(1)}>›</button>
  <div className="offer-dots">{offerBanners.map((x,i)=><button key={x.title} aria-label={'Show offer '+(i+1)} className={i===index?'active':''} onClick={()=>setIndex(i)}/>)}</div>
 </section>
}

function Nav(){const[q,setQ]=useState('');const nav=useNavigate();const user=JSON.parse(localStorage.getItem('user')||'null');return <nav><Link className="logo" to="/">Vinkart</Link><input value={q} onChange={e=>setQ(e.target.value)} onKeyDown={e=>e.key==='Enter'&&nav('/products?search='+encodeURIComponent(q))} placeholder="Search products..."/><Link to="/products">Products</Link>{user?.role==='ADMIN'&&<Link to="/admin">Admin</Link>}<Link to="/cart">🛒 Cart</Link><Link to="/wishlist">❤️</Link>{localStorage.getItem('token')?<><Link to="/orders">Orders</Link><button onClick={()=>{localStorage.clear();location.href='/'}}>Logout</button></>:<Link to="/login">Login</Link>}</nav>}

function Home(){
 const[categories,setCategories]=useState([]);const[categoryError,setCategoryError]=useState('');
 useRememberScroll('home');
 useEffect(()=>{api.get('/categories').then(r=>setCategories(r.data)).catch(()=>setCategoryError('Could not load categories'))},[]);
 return <><OfferBanners/><section className="hero"><div><h1>Shop smarter with Vinkart</h1><p>Mobiles, laptops, fashion and accessories.</p><Link className="btn" to="/products">Shop Now</Link></div></section><section className="category-section"><div className="section-heading"><div><span className="eyebrow">SHOP BY CATEGORY</span><h2>Find what you need</h2></div><Link className="category-view-all" to="/products">View All Products →</Link></div>{categoryError?<p className="error">{categoryError}</p>:<div className="category-grid">{categories.map(c=><Link key={c.id} className="category-card" to={'/products?categoryId='+c.id}><div className="category-icon">{categoryIcon(c.name)}</div><h3>{c.name}</h3><span>Explore products →</span></Link>)}</div>}</section><h2>Featured Products</h2><ProductGrid/></>
}

function categoryIcon(name){const n=name.toLowerCase();if(n.includes('mobile')&&n.includes('accessor'))return '🎧';if(n.includes('mobile'))return '📱';if(n.includes('laptop'))return '💻';if(n.includes('men'))return '👔';if(n.includes('women'))return '👗';return '🛍️'}

function ProductGrid(){const[p,setP]=useState([]);const[error,setError]=useState('');useEffect(()=>{api.get('/products').then(r=>setP(r.data.slice(0,12))).catch(e=>setError(e.response?.data?.message||'Could not load products'))},[]);if(error)return <p className="error">{error}</p>;if(!p.length)return <p>No products available yet.</p>;return <div className="grid">{p.map(x=><ProductCard key={x.id} p={x}/>)}</div>}

function splitOptions(value){
 if(!value)return [];
 return String(value).split(/[,|\/]+/).map(x=>x.trim()).filter(Boolean);
}
function specValue(specs,names){const entry=Object.entries(specs||{}).find(([k])=>names.some(n=>k.toLowerCase().replace(/\s+/g,'')===n));return entry?entry[1]:'';}
function productChoices(p){
 const specs=p.specifications||{};
 const sizeValue=specValue(specs,['size','sizes','availablesizes','availablesize']);
 const variantValue=specValue(specs,['variant','variants','varianttype','color','colour','colors','colours']);
 return {sizes:splitOptions(sizeValue),variants:splitOptions(variantValue)};
}

function ProductCard({p}){
 const location=useLocation();
 const sale=p.price*(1-p.discount/100); const specs=p.specifications||{}; const quickSpecs=Object.entries(specs).slice(0,3); const choices=productChoices(p);
 const detailState={from:location.pathname+location.search,scrollY:window.scrollY};
 return <article className="card">
  <Link className="product-image-link" to={'/products/'+p.id} state={detailState}><img src={(p.imageUrls&&p.imageUrls[0])||p.imageUrl||'https://via.placeholder.com/500x400?text=Vinkart'} alt={p.name}/></Link>
  <Link className="product-title-link" to={'/products/'+p.id} state={detailState}><h3>{p.name}</h3></Link><p>{p.description}</p>
  {quickSpecs.length>0&&<div className="spec-preview">{quickSpecs.map(([k,v])=><span key={k}><b>{k}:</b> {v}</span>)}</div>}
  {choices.sizes.length>0&&<small className="choice-hint">Sizes: {choices.sizes.join(', ')}</small>}
  {choices.variants.length>0&&<small className="choice-hint">Variants: {choices.variants.join(', ')}</small>}
  <b>₹{sale.toFixed(2)}</b>{p.discount>0&&<> <del>₹{Number(p.price).toFixed(2)}</del> <small>{p.discount}% off</small></>}
  <p>Stock: {p.stock}</p><div><Link className="btn" to={'/products/'+p.id} state={detailState}>{p.stock<=0?'Out of Stock':'Choose Options'}</Link></div>
 </article>
}

function Products(){useRememberScroll('products:'+window.location.search);const[p,setP]=useState([]);const[loading,setLoading]=useState(true);const[error,setError]=useState('');const[categories,setCategories]=useState([]);const[searchParams,setSearchParams]=useSearchParams();const q=searchParams.get('search')||'';const categoryId=searchParams.get('categoryId')||'';const categoryName=categories.find(c=>String(c.id)===String(categoryId))?.name;useEffect(()=>{api.get('/categories').then(r=>setCategories(r.data)).catch(()=>{})},[]);useEffect(()=>{setLoading(true);setError('');const params={};if(q)params.search=q;if(categoryId)params.categoryId=categoryId;api.get('/products',{params}).then(r=>setP(r.data)).catch(e=>setError(e.response?.data?.message||'Could not load products')).finally(()=>setLoading(false))},[q,categoryId]);const updateSearch=e=>{const value=e.target.value;const next={};if(value)next.search=value;if(categoryId)next.categoryId=categoryId;setSearchParams(next)};const clearCategory=()=>{const next={};if(q)next.search=q;setSearchParams(next)};return <><div className="products-heading"><div><span className="eyebrow">CATALOG</span><h2>{categoryName||'All Products'} ({p.length})</h2></div>{categoryId&&<button className="btn2" onClick={clearCategory}>Clear Category</button>}</div><div className="category-filter"><Link className={!categoryId?'active':''} to={q?'/products?search='+encodeURIComponent(q):'/products'}>All</Link>{categories.map(c=>{const params=new URLSearchParams();params.set('categoryId',c.id);if(q)params.set('search',q);return <Link key={c.id} className={String(c.id)===String(categoryId)?'active':''} to={'/products?'+params.toString()}>{categoryIcon(c.name)} {c.name}</Link>})}</div><input className="filter" value={q} onChange={updateSearch} placeholder="Search products..."/>{loading?<p>Loading products...</p>:error?<p className="error">{error}</p>:!p.length?<p>No products found in this category.</p>:<div className="grid">{p.map(x=><ProductCard key={x.id} p={x}/>)}</div>}</>}

function ProductDetails(){
 const{id}=useParams(); const location=useLocation(); const nav=useNavigate(); const[p,setP]=useState(null); const[selected,setSelected]=useState(0); const[selectedSize,setSelectedSize]=useState(''); const[selectedVariant,setSelectedVariant]=useState('');
 useEffect(()=>{api.get('/products/'+id).then(r=>setP(r.data)).catch(()=>setP(null))},[id]);
 if(!p)return <p>Loading...</p>;
 const images=(p.imageUrls&&p.imageUrls.length?p.imageUrls:[p.imageUrl]).filter(Boolean); const current=images[selected]||images[0]||'https://via.placeholder.com/600x500?text=Vinkart';
 const sale=p.price*(1-p.discount/100); const specs=Object.entries(p.specifications||{}); const choices=productChoices(p);
 const requiresSize=choices.sizes.length>0; const requiresVariant=choices.variants.length>0;
 const add=()=>{if(!localStorage.getItem('token'))return location.href='/login'; if(requiresSize&&!selectedSize)return alert('Please select a size.'); if(requiresVariant&&!selectedVariant)return alert('Please select a variant.'); api.post('/cart/'+p.id,null,{params:{size:selectedSize,variant:selectedVariant}}).then(()=>alert('Added to cart')).catch(e=>alert(e.response?.data?.message||e.response?.data?.error||'Could not add to cart'))};
 return <div className="details">
  <div className="product-gallery"><img className="details-main-image" src={current} alt={p.name}/>{images.length>1&&<div className="thumbnail-row">{images.map((url,i)=><button type="button" className={i===selected?'thumb active':'thumb'} key={url+i} onClick={()=>setSelected(i)}><img src={url} alt={`${p.name} ${i+1}`}/></button>)}</div>}</div>
  <div><h2>{p.name}</h2><p>{p.description}</p><h2>₹{sale.toFixed(2)}</h2>{p.discount>0&&<p>Discount: {p.discount}%</p>}<p>Stock: {p.stock}</p>
   {(requiresSize||requiresVariant)&&<section className="selection-panel"><h3>Select your options</h3>{requiresSize&&<div className="option-group"><label>Size</label><div className="choice-buttons">{choices.sizes.map(x=><button type="button" className={selectedSize===x?'choice active':'choice'} key={x} onClick={()=>setSelectedSize(x)}>{x}</button>)}</div></div>}{requiresVariant&&<div className="option-group"><label>Variant</label><div className="choice-buttons">{choices.variants.map(x=><button type="button" className={selectedVariant===x?'choice active':'choice'} key={x} onClick={()=>setSelectedVariant(x)}>{x}</button>)}</div></div>}<small>Your selection will be saved with the cart item and order.</small></section>}
   {specs.length>0&&<section className="spec-section"><h3>Product Specifications</h3><div className="spec-table">{specs.map(([key,value])=><div className="spec-row" key={key}><b>{key}</b><span>{value}</span></div>)}</div></section>}
   <button disabled={!p.stock||((requiresSize&&!selectedSize)||(requiresVariant&&!selectedVariant))} onClick={add}>{p.stock?'Add Selected Item to Cart':'Out of Stock'}</button> <button className="btn2 back-button" type="button" onClick={()=>location.state?.from?nav(location.state.from,{state:{restoreScroll:location.state.scrollY}}):nav(-1)}>← Back</button>
  </div>
 </div>
}

function Login({register=false}){const[email,setE]=useState(''),[password,setP]=useState(''),[name,setN]=useState('');const nav=useNavigate();const submit=e=>{e.preventDefault();api.post('/auth/'+(register?'register':'login'),register?{name,email,password}:{email,password}).then(r=>{localStorage.setItem('token',r.data.token);localStorage.setItem('user',JSON.stringify(r.data.user));nav(r.data.user?.role==='ADMIN'?'/admin':'/')}).catch(e=>alert(e.response?.data?.error||e.response?.data?.message||'Failed'))};return <form className="form" onSubmit={submit}><h2>{register?'Create account':'Login'}</h2>{register&&<input placeholder="Name" value={name} onChange={e=>setN(e.target.value)} required/>}<input type="email" placeholder="Email" value={email} onChange={e=>setE(e.target.value)} required/><input type="password" placeholder="Password" value={password} onChange={e=>setP(e.target.value)} required/><button>{register?'Register':'Login'}</button>{!register&&<Link to="/register">Create account</Link>}</form>}

function Cart(){const[c,setC]=useState([]);const load=()=>api.get('/cart').then(r=>setC(r.data)).catch(()=>{});useEffect(()=>{load()},[]);const total=c.reduce((s,x)=>s+x.product.price*x.quantity*(1-x.product.discount/100),0);return <><h2>Your Cart</h2>{!c.length?<p>Your cart is empty.</p>:c.map(x=><div className="row" key={x.id}><img src={(x.product.imageUrls&&x.product.imageUrls[0])||x.product.imageUrl}/><div className="cart-info"><b>{x.product.name}</b>{x.selectedSize&&<small>Size: {x.selectedSize}</small>}{x.selectedVariant&&<small>Variant: {x.selectedVariant}</small>}<span>Quantity: {x.quantity}</span></div><b>₹{(x.product.price*x.quantity*(1-x.product.discount/100)).toFixed(2)}</b><button disabled={x.quantity>=x.product.stock} onClick={()=>api.put('/cart/'+x.id,null,{params:{quantity:x.quantity+1}}).then(load)}>+</button><button onClick={()=>api.delete('/cart/'+x.id).then(load)}>Remove</button></div>)}{c.length>0&&<><h2>Total: ₹{total.toFixed(2)}</h2><Link className="btn" to="/checkout">Checkout</Link></>}</>}

function Checkout(){
 const user=JSON.parse(localStorage.getItem('user')||'null'); const initial={name:user?.name||'',email:user?.email||'',flatHouse:'',areaPost:'',district:'',state:'',country:'India',zipcode:'',mobile:''};
 const[address,setAddress]=useState(initial); const[params]=useSearchParams(); const[cancelled]=useState(params.get('cancelled')==='true'); const[paying,setPaying]=useState(false);
 const update=(k,v)=>setAddress({...address,[k]:v});
 const pay=async e=>{e.preventDefault();setPaying(true);const formatted=`${address.name}, ${address.email}, Mobile: ${address.mobile}, Flat/House No.: ${address.flatHouse}, Village/Area/Post: ${address.areaPost}, District: ${address.district}, State: ${address.state}, Country: ${address.country}, ZIP Code: ${address.zipcode}`;try{const r=await api.post('/payments/create-checkout-session',{address:formatted,...address});window.location.href=r.data.url}catch(e){alert(e.response?.data?.message||e.response?.data?.error||'Unable to start Stripe checkout');setPaying(false)}};
 return <form className="form checkout-form" onSubmit={pay}><h2>Secure Checkout</h2>{cancelled&&<p className="notice">Payment was cancelled. Your cart is still saved.</p>}<p className="checkout-subtitle">Enter your delivery details before payment.</p>
  <div className="checkout-grid"><label>Name<input value={address.name} onChange={e=>update('name',e.target.value)} required/></label><label>Email<input type="email" value={address.email} onChange={e=>update('email',e.target.value)} required/></label><label>Mobile Number<input type="tel" pattern="[0-9]{10}" maxLength="10" value={address.mobile} onChange={e=>update('mobile',e.target.value.replace(/\D/g,''))} required/></label><label>Flat / House No.<input value={address.flatHouse} onChange={e=>update('flatHouse',e.target.value)} required/></label><label>Village / Area / Post<input value={address.areaPost} onChange={e=>update('areaPost',e.target.value)} required/></label><label>District<input value={address.district} onChange={e=>update('district',e.target.value)} required/></label><label>State<input value={address.state} onChange={e=>update('state',e.target.value)} required/></label><label>Country<input value={address.country} onChange={e=>update('country',e.target.value)} required/></label><label>ZIP Code<input inputMode="numeric" value={address.zipcode} onChange={e=>update('zipcode',e.target.value.replace(/\D/g,''))} required/></label></div>
  <p>Payment method: <b>Stripe Secure Checkout</b></p><button disabled={paying}>{paying?'Redirecting to Stripe...':'Pay securely with Stripe'}</button></form>
}

function PaymentSuccess(){const[params]=useSearchParams();const[message,setMessage]=useState('Confirming your payment...');const nav=useNavigate();useEffect(()=>{const id=params.get('session_id');if(!id){setMessage('Missing Stripe session.');return}api.post('/payments/confirm',null,{params:{sessionId:id}}).then(()=>{setMessage('Payment successful! Your order has been placed.');setTimeout(()=>nav('/orders'),1200)}).catch(e=>setMessage(e.response?.data?.message||e.response?.data?.error||'Payment could not be confirmed.'))},[params,nav]);return <div className="form"><h2>{message}</h2><Link className="btn" to="/orders">Go to Orders</Link></div>}

function Orders(){const[o,setO]=useState([]);useEffect(()=>{api.get('/orders').then(r=>setO(r.data)).catch(()=>{})},[]);return <><h2>My Orders</h2>{!o.length?<p>No orders yet.</p>:o.map(x=><div className="order" key={x.id}><b>Order #{x.id}</b><span>₹{Number(x.total).toFixed(2)}</span><span>{x.status}</span><span>Payment: {x.paymentStatus}</span><small>{x.createdAt}</small></div>)}</>}

function Wishlist(){const[w,setW]=useState([]);useEffect(()=>{api.get('/wishlist').then(r=>setW(r.data))},[]);return <><h2>Wishlist</h2><div className="grid">{w.map(x=><ProductCard key={x.id} p={x.product}/>)}</div></>}

function Admin(){
const[d,setD]=useState(null);
const[products,setProducts]=useState([]);
const[categories,setCategories]=useState([]);
const[orders,setOrders]=useState([]);
const[editing,setEditing]=useState(null);
const[message,setMessage]=useState('');
const blank={name:'',description:'',price:'',discount:0,stock:0,imageUrls:['',''],rating:0,categoryId:'',specifications:[]};
const[form,setForm]=useState(blank);

const load=()=>{
 api.get('/admin/dashboard').then(r=>setD(r.data)).catch(()=>setMessage('Admin authorization failed. Please login again.'));
 api.get('/products').then(r=>setProducts(r.data)).catch(()=>{});
 api.get('/categories').then(r=>setCategories(r.data)).catch(()=>{});
 api.get('/admin/orders').then(r=>setOrders(r.data)).catch(()=>{});
};
useEffect(()=>{load()},[]);

const updateImage=(index,value)=>{
 const imageUrls=[...form.imageUrls];
 imageUrls[index]=value;
 setForm({...form,imageUrls});
};
const addImage=()=>setForm({...form,imageUrls:[...form.imageUrls,'']});
const removeImage=index=>{
 const imageUrls=form.imageUrls.filter((_,i)=>i!==index);
 setForm({...form,imageUrls:imageUrls.length?imageUrls:['','']});
};

const updateSpec=(index,field,value)=>{
 const specifications=[...form.specifications];
 specifications[index]={...specifications[index],[field]:value};
 setForm({...form,specifications});
};
const addSpec=()=>setForm({...form,specifications:[...form.specifications,{key:'',value:''}]});
const removeSpec=index=>setForm({...form,specifications:form.specifications.filter((_,i)=>i!==index)});
const applySpecPreset=()=>{
 const name=categories.find(c=>String(c.id)===String(form.categoryId))?.name||'';
 let keys=[];
 if(name==='Mobiles') keys=['RAM','Storage','Variants','Color','Display','Battery','Network','Warranty'];
 else if(name==='Laptops') keys=['RAM','Storage','Variants','Processor','Display','Graphics','Operating System','Warranty'];
 else if(name==='Men Clothing'||name==='Women Clothing') keys=['Available Sizes','Variants','Color','Material','Fit','Care','Warranty'];
 else keys=['Available Sizes','Variants','Color','Material','Compatibility','Warranty'];
 setForm({...form,specifications:keys.map(key=>({key,value:''}))});
};

const submit=e=>{
 e.preventDefault();
 const imageUrls=form.imageUrls.map(x=>x.trim()).filter(Boolean);
 const specifications={};
 form.specifications.forEach(({key,value})=>{
  const k=(key||'').trim();
  const v=(value||'').trim();
  if(k&&v) specifications[k]=v;
 });
 const body={
  name:form.name,
  description:form.description,
  price:Number(form.price),
  discount:Number(form.discount||0),
  stock:Number(form.stock||0),
  imageUrl:imageUrls[0]||'',
  imageUrls:imageUrls,
  rating:Number(form.rating||0),
  category:form.categoryId?{id:Number(form.categoryId)}:null,
  specifications
 };
 const request=editing?api.put('/products/'+editing,body):api.post('/products',body);
 request.then(()=>{
  setMessage(editing?'Product updated successfully':'Product added successfully');
  setEditing(null);
  setForm(blank);
  load();
 }).catch(e=>setMessage(e.response?.data?.message||e.response?.data?.error||'Could not save product'));
};

const edit=p=>{
 setEditing(p.id);
 const urls=(p.imageUrls&&p.imageUrls.length?p.imageUrls:[p.imageUrl||'']).filter(Boolean);
 setForm({
  name:p.name||'',
  description:p.description||'',
  price:p.price||'',
  discount:p.discount||0,
  stock:p.stock||0,
  imageUrls:urls.length?urls:[''],
  rating:p.rating||0,
  categoryId:p.category?.id||'',
  specifications:Object.entries(p.specifications||{}).map(([key,value])=>({key,value}))
 });
 window.scrollTo({top:0,behavior:'smooth'});
};

const del=id=>{
 if(!confirm('Delete this product?'))return;
 api.delete('/products/'+id).then(()=>{setMessage('Product deleted');load()})
 .catch(e=>setMessage(e.response?.data?.message||'Could not delete product'));
};

const statusChange=(id,value)=>
 api.put('/admin/orders/'+id+'/status',null,{params:{value}})
 .then(()=>{setMessage('Order status updated');load()})
 .catch(e=>setMessage(e.response?.data?.message||'Could not update order'));

return <>
 <div className="admin-head"><h2>Admin Dashboard</h2><Link className="btn" to="/products">View Store</Link></div>
 {message&&<p className="notice">{message}</p>}
 {d&&<div className="stats">{Object.entries(d).map(([k,v])=><div key={k}><b>{v}</b><span>{k}</span></div>)}</div>}

 <section className="admin-panel">
  <h2>{editing?'Edit Product':'Add Product'}</h2>
  <form className="admin-form" onSubmit={submit}>
   <input placeholder="Product name" value={form.name} onChange={e=>setForm({...form,name:e.target.value})} required/>
   <textarea placeholder="Description" value={form.description} onChange={e=>setForm({...form,description:e.target.value})} required/>
   <input type="number" min="0" step="0.01" placeholder="Price" value={form.price} onChange={e=>setForm({...form,price:e.target.value})} required/>
   <input type="number" min="0" max="100" step="0.01" placeholder="Discount %" value={form.discount} onChange={e=>setForm({...form,discount:e.target.value})}/>
   <input type="number" min="0" placeholder="Stock" value={form.stock} onChange={e=>setForm({...form,stock:e.target.value})} required/>

   <div className="image-url-manager">
    <label><strong>Product Images</strong> <small>Add multiple image URLs</small></label>
    {form.imageUrls.map((url,index)=>
      <div className="image-url-row" key={index}>
       <input
        type="url"
        placeholder={`Image URL ${index+1}`}
        value={url}
        onChange={e=>updateImage(index,e.target.value)}
        required={index===0}
       />
       {url&&<img className="image-url-preview" src={url} alt={`Preview ${index+1}`} onError={e=>e.currentTarget.style.display='none'}/>}
       {form.imageUrls.length>1&&<button type="button" className="danger" onClick={()=>removeImage(index)}>Remove</button>}
      </div>
    )}
    <button type="button" className="btn2" onClick={addImage}>+ Add Another Image</button>
    <small>First image is used as the main product image.</small>
   </div>

   <div className="spec-manager">
    <div className="spec-manager-head">
     <label><strong>Specifications & Variants</strong> <small>Add category-specific details</small></label>
     <button type="button" className="btn2" onClick={applySpecPreset}>Auto-fill Category Specs</button>
    </div>
    <small className="helper">Examples: RAM, Storage, Processor, Color, Available Sizes, Material, Variants, Warranty.</small>
    {form.specifications.map((spec,index)=>
      <div className="spec-row-editor" key={index}>
       <input placeholder="Specification name" value={spec.key} onChange={e=>updateSpec(index,'key',e.target.value)}/>
       <input placeholder="Value (e.g. 8GB / 12GB)" value={spec.value} onChange={e=>updateSpec(index,'value',e.target.value)}/>
       <button type="button" className="danger" onClick={()=>removeSpec(index)}>Remove</button>
      </div>
    )}
    <button type="button" className="btn2" onClick={addSpec}>+ Add Specification</button>
   </div>

   <input type="number" min="0" max="5" step="0.1" placeholder="Rating" value={form.rating} onChange={e=>setForm({...form,rating:e.target.value})}/>
   <select value={form.categoryId} onChange={e=>setForm({...form,categoryId:e.target.value})}>
    <option value="">Select category</option>
    {categories.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}
   </select>
   <div>
    <button type="submit">{editing?'Update Product':'Add Product'}</button>
    {editing&&<button type="button" className="btn2" onClick={()=>{setEditing(null);setForm(blank)}}>Cancel</button>}
   </div>
  </form>
 </section>

 <section className="admin-panel">
  <h2>Manage Products ({products.length})</h2>
  {products.length===0?<p>No products yet.</p>:<div className="admin-table">
   {products.map(p=><div className="admin-product" key={p.id}>
    <img src={(p.imageUrls&&p.imageUrls[0])||p.imageUrl}/>
    <div><b>{p.name}</b><small>₹{p.price} · Stock {p.stock} · {p.imageUrls?.length||1} image(s) · {Object.keys(p.specifications||{}).length} specs · {p.category?.name||'No category'}</small></div>
    <button onClick={()=>edit(p)}>Edit</button><button className="danger" onClick={()=>del(p.id)}>Delete</button>
   </div>)}
  </div>}
 </section>

 <section className="admin-panel">
  <h2>Order Management ({orders.length})</h2>
  {!orders.length?<p>No orders yet.</p>:<div className="admin-table">
   {orders.map(o=><div className="admin-product" key={o.id}>
    <div><b>Order #{o.id}</b><small>{o.user?.email} · ₹{Number(o.total).toFixed(2)} · {o.paymentStatus}</small><small>{o.address}</small></div>
    <select value={o.status} onChange={e=>statusChange(o.id,e.target.value)}>
     <option>PENDING</option><option>CONFIRMED</option><option>PROCESSING</option><option>SHIPPED</option><option>OUT_FOR_DELIVERY</option><option>DELIVERED</option><option>CANCELLED</option>
    </select>
   </div>)}
  </div>}
 </section>
</>;
}
function App(){return <><Nav/><main><Routes><Route path="/" element={<Home/>}/><Route path="/products" element={<Products/>}/><Route path="/products/:id" element={<ProductDetails/>}/><Route path="/login" element={<Login/>}/><Route path="/register" element={<Login register/>}/><Route path="/cart" element={<Cart/>}/><Route path="/checkout" element={<Checkout/>}/><Route path="/payment-success" element={<PaymentSuccess/>}/><Route path="/orders" element={<Orders/>}/><Route path="/wishlist" element={<Wishlist/>}/><Route path="/admin" element={<Admin/>}/></Routes></main><footer>© 2026 Vinkart</footer></>}

createRoot(document.getElementById('root')).render(<BrowserRouter><App/></BrowserRouter>);
