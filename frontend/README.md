# Clear Chain - Frontend

Next.js frontend for the Clear Chain system.

## Getting Started

### Prerequisites

- Node.js 18+ installed
- Spring Boot backend running on `http://localhost:8080`

### Installation

```bash
# Install dependencies
npm install
# or
yarn install
```

### Development

```bash
# Start development server
npm run dev
# or
yarn dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build for Production

```bash
npm run build
npm start
```

## Project Structure

```
frontend/
├── app/                    # Next.js App Router pages
│   ├── page.tsx           # Dashboard (home page)
│   ├── vendors/           # Vendors page
│   ├── purchase-orders/   # Purchase Orders page
│   └── contracts/         # Contracts page
├── components/            # React components
│   └── Navigation.tsx     # Navigation bar
├── lib/                   # Utilities and API
│   └── api.ts            # API client functions
└── public/               # Static files
```

## Features

- ✅ Dashboard with statistics
- ✅ Vendors list view
- ✅ Purchase Orders list view
- ✅ Navigation between pages
- ✅ Responsive design with Tailwind CSS
- ✅ TypeScript for type safety

## API Connection

The frontend communicates with the Spring Boot backend at `http://localhost:8080/api`.

Make sure:
1. Backend is running on port 8080
2. CORS is configured (already added to backend)
3. Backend API endpoints are working (test with Postman first)

## Next Steps

- Add forms for creating vendors, purchase orders, etc.
- Add vendor detail page
- Add purchase order detail page
- Add filtering and search
- Add user authentication (if needed)

