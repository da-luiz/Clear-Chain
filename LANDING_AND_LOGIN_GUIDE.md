# Landing Page & Login Page Redesign Guide

Use this as a step-by-step guide. You do the edits; this tells you **what** to change and **where**.

---

## Quick reference

| You want to… | File to edit | Section below |
|--------------|--------------|---------------|
| Top bar: logo left, Products/Features/Login links right | `deployment/landing/index.html` | 1.1 |
| Light green + white instead of purple/blue | `deployment/landing/index.html` | 1.2 |
| Add hero image + feature images | `deployment/landing/index.html` | 1.3 |
| Fade-in hero, hover animation on cards | `deployment/landing/index.html` | 1.4 |
| Login: white left panel (branding) + dark right panel (form) | `frontend/app/login/page.tsx` | 2 |
| Use the app logo | Landing + login | 3 + 4 |

---

## 1. Landing Page (`deployment/landing/index.html`)

### 1.1 Top bar: logo left, nav right

**Current:** The header only has `<div class="logo">Clear Chain</div>` inside the nav.

**What to do:**

- **Left side:** Replace the text logo with an **image** (your new Clear Chain logo). Use an `<img>` tag, e.g. `<img src="logo.png" alt="Clear Chain" class="logo-img" />`. Add a class like `.logo-img { height: 40px; }` (or whatever size you prefer) so it doesn’t overwhelm the header.
- **Right side:** Add a `<ul>` or div with links, for example:
  - **Products** (or “Solutions”) – can be a dropdown later: e.g. “Vendor Management”, “Contracts”, “Purchase Orders”
  - **Features** – anchor to `#features` (add `id="features"` to your features section)
  - **How it works** – anchor to `#how-it-works`
  - **Login** – link to `https://app.<domain>.com/login`
  - **Get Started** – link to `https://app.<domain>.com` (primary CTA)

**Layout:** In the nav `.container`, use `display: flex; justify-content: space-between; align-items: center;`. Put the logo in one div, the nav links in another. Style links with a bit of spacing (`margin-left` or `gap`) and remove underlines; use a hover state (e.g. color change or underline).

**Optional dropdown:** For “Products”, use a `<div>` that shows on hover (or click on mobile) with `position: absolute` and a list of sub-links. Start with simple links; add dropdown behavior once the structure is there.

---

### 1.2 Color scheme: light green and white

**Current:** Purple gradient hero (`#667eea`, `#764ba2`), blue accents (`#2563eb`).

**What to change:**

- **Hero:** Replace the gradient with light green and white, e.g.  
  `linear-gradient(135deg, #d4edda 0%, #ffffff 50%, #c3e6cb 100%)`  
  or a soft green like `#e8f5e9` to white. Keep text dark (`#1a1a1a` or `#2d5016`) so it’s readable.
- **Accent color:** Replace blue (`#2563eb`) with a green, e.g. `#2e7d32` or `#388e3c` for buttons, feature icons, and step numbers.
- **Header/footer:** Keep header white; you can give the footer a dark green (`#1b5e20`) or keep dark gray and use green for links/hover.
- **Feature cards:** Keep background light (e.g. `#f1f8e9` or white) and use the new green for headings and borders.

Do a find-replace in the `<style>` block: e.g. `#2563eb` → your green, and update the `.hero` background.

---

### 1.3 Add images (ClearChain-related)

**Where:**

- **Hero:** Right side of the hero (on desktop) – e.g. illustration of dashboard, vendors, or workflow. Use a `<div>` or `<img>` next to the text. Make the hero a two-column grid: left = heading + description + CTA; right = image.
- **Features:** In each `.feature-card`, add a small image or icon above the title (icons can be emoji, SVG, or image files).
- **How it works / Security:** Optional image or icon per step.

**How:**  
Put images in the same folder as `index.html` (e.g. `deployment/landing/`) or in a subfolder like `images/`. Use relative paths: `src="images/hero.svg"`. Use `max-width: 100%` and `height: auto` so they’re responsive.

**What to use:** Any ClearChain-related art (dashboard mockup, workflow diagram, chain/supply icon). You can use free stock (e.g. Undraw) or your own; the guide in the repo can suggest “vendor / contract / dashboard” as search terms.

---

### 1.4 Add animation to captivate users

**Simple options (CSS only):**

- **Hero text:** Fade-in or slide-up on load:  
  Add a class e.g. `.hero-content { opacity: 0; transform: translateY(20px); animation: fadeUp 0.8s ease forwards; }`  
  and define `@keyframes fadeUp { to { opacity: 1; transform: translateY(0); } }`.
- **Hero image:** Slight float or pulse:  
  `animation: float 4s ease-in-out infinite` with `@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }`.
- **Feature cards:** `transition: transform 0.2s;` and `:hover { transform: translateY(-4px); }` (you can add a light box-shadow on hover too).
- **Scroll:** Optionally, use `Intersection Observer` in a small script to add a class when sections enter the viewport and animate them (e.g. fade-in). Start with hero + cards; add scroll later if you want.

**Where:** Put keyframes and transition rules in the same `<style>` block in `index.html`. Add the animation class to the hero div and to `.feature-card`.

---

## 2. Login Page – match the reference image (`frontend/app/login/page.tsx`)

The reference has: **left panel** = white, branding (logo + product name + tagline + footer links); **right panel** = dark, form (Welcome, Email, Password, Remember me, Forgot password?, Login button).

**What to do:**

1. **Layout:** One wrapper with two columns (e.g. `grid` or `flex`):
   - **Left column (e.g. 55%):** White background, logo at top-left, “Clear Chain” (or “CLEAR CHAIN”) large in the center, tagline below, and at the bottom small links (Privacy, Legal, etc.) if you want.
   - **Right column (e.g. 45%):** Dark gray/black background (`#1f2937` or `#111827`), form on the right.

2. **Left panel:**  
   - Top: Your logo image (same as landing).  
   - Center: Big heading “Clear Chain” (or “CLEAR CHAIN” with one letter accented, e.g. green “C”).  
   - Bottom: Small text like “Copyright …”, “Privacy Policy”, “Legal terms”, etc., in muted gray.

3. **Right panel:**  
   - “Welcome” (large, white).  
   - “Please login using your ID to start” (or “Enter your credentials to access the system”).  
   - **Email** and **Password** inputs: white/light background, rounded, full width.  
   - **Remember me** checkbox + **Forgot your password?** link (teal/light blue).  
   - **Login** button: prominent (e.g. green or red accent to match your brand), rounded, full width.

4. **Responsive:** On small screens, stack: e.g. show the form first, then branding below, or hide the left panel and show only the form with a small logo above.

5. **Technical:** Keep your existing `handleSubmit`, `handleRegisterSubmit`, and “Create first admin” flow; only change the JSX layout and Tailwind (or CSS) classes. Replace “Username” with “Email” in the label/placeholder if you want to match the image exactly (backend may still use username – that’s fine).

**Colors for right panel:** Background `bg-gray-900` or `bg-[#1f2937]`, text white, inputs `bg-white` or `bg-gray-100`, button your accent (e.g. `bg-green-600` or red). Link “Forgot your password?” in `text-teal-400` or similar.

---

## 3. App logo

**Spec for your logo:** Circular badge, “CC” or “Clear Chain” text, light green and white, clean and professional (suitable for header and login). You can create it in Figma/Canva, use a favicon/logo generator, or have it designed.

**Where to put it:**

- **Landing:** `deployment/landing/` (e.g. `logo.png` or `images/logo.png`) and reference it in the header.
- **Login:** In the Next.js app, put the file in `frontend/public/` (e.g. `frontend/public/logo.png`) and use `/logo.png` in an `<img>` in the login left panel.
- **Favicon:** You can use the same asset as favicon by placing it as `favicon.ico` or linking it in `<head>`.

---

## 4. File checklist

| Goal                         | File(s) to edit                    |
|-----------------------------|-------------------------------------|
| Landing navbar + colors     | `deployment/landing/index.html`     |
| Landing images + animation  | `deployment/landing/index.html`     |
| Login two-panel layout      | `frontend/app/login/page.tsx`      |
| Use logo on landing         | `deployment/landing/index.html`     |
| Use logo on login           | `frontend/app/login/page.tsx` + `frontend/public/` |

---

## 5. Order of work (suggested)

1. Add the logo asset to `frontend/public/` and `deployment/landing/` (or link from one place).
2. Landing: Update header (logo + nav links), then switch colors to light green/white.
3. Landing: Add one hero image and one simple animation (e.g. hero fade-in + card hover).
4. Login: Implement two-panel layout and style form to match the reference; keep all existing behavior.

If you want, we can go step-by-step (e.g. “do only the navbar” or “only the login layout”) and I can give you the exact code snippets for that step next.
