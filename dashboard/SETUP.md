# Quick Setup Guide 🚀

Get your light theme Job Processing Dashboard up and running in 5 minutes!

## Step 1: Extract (30 seconds)
```bash
unzip dashboard-light.zip
cd dashboard-light
```

## Step 2: Install (1 minute)
```bash
npm install
```

## Step 3: Configure (1 minute)
```bash
cp .env.example .env
```

Edit `.env`:
```
VITE_API_BASE_URL=http://localhost:8888/api
```

## Step 4: Start (30 seconds)
```bash
npm run dev
```

**Opens at:** http://localhost:3000

## ✅ You're Done!

Your dashboard is now running with the beautiful light theme! 🎉

## Backend Setup (Optional but Recommended)

Add CORS to your Spring Boot app:

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
        }
    };
}
```

## What's Included

✓ Modern light theme (clean, professional aesthetic)
✓ Real-time job monitoring dashboard
✓ 5 main tabs (Overview, Jobs, Analytics, Workers, DLQ)
✓ Responsive design (mobile, tablet, desktop)
✓ Auto-refresh every 5 seconds
✓ Interactive charts and metrics
✓ Worker pool management
✓ Dead letter queue management

## Common Commands

```bash
npm run dev       # Start development server
npm run build     # Build for production
npm run preview   # Preview production build
```

## Troubleshooting

### Port 3000 in use?
Change in `vite.config.js`:
```javascript
server: {
  port: 3001,
}
```

### Cannot connect to API?
1. Verify backend is running
2. Check CORS is enabled
3. Verify .env has correct URL

### No data showing?
1. Check browser console for errors
2. Verify API endpoints exist
3. Check network tab in DevTools

## Next Steps

1. ✅ Dashboard is running
2. Configure your backend API endpoints
3. Test all 5 tabs
4. Deploy to production

## Production Deployment

Build and deploy:
```bash
npm run build
# Deploy the 'dist' folder to your hosting
```

Supported platforms:
- Vercel (recommended)
- Netlify
- AWS S3
- Docker
- Any static hosting

## Need Help?

See `README.md` for detailed documentation or check the component files in `src/components/`

---

**You're all set! Happy monitoring! 🚀**
