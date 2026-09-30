# Job Processing Dashboard - Light Theme 🌞

A modern, clean monitoring dashboard for the Notification Processing System with a professional light theme. Built with React, Tailwind CSS, and Recharts.

## ✨ Features

- **Modern Light Theme** - Clean, professional aesthetic inspired by modern SaaS applications
- **Real-time Job Monitoring** - Live job status updates every 5 seconds
- **Interactive Charts** - Visual analytics of job processing metrics
- **Worker Management** - Monitor worker pool health and performance
- **Dead Letter Queue** - Manage and retry failed jobs
- **System Health** - Overall system status and metrics
- **Responsive Design** - Works on desktop, tablet, and mobile
- **Auto-refresh** - Automatic data updates every 5 seconds

## 📊 Dashboard Sections

### 1. **Overview** 
- Key metrics cards (Total Jobs, Success Rate, Processing, Failed)
- Job status breakdown with visual indicators
- System health status (Database, Redis, Email, Workers)
- Progress bar for overall success

### 2. **All Jobs** 
- Searchable job listing by ID
- Filter by status (PENDING, PROCESSING, COMPLETED, FAILED)
- Expandable job details with full information
- Job metadata display

### 3. **Analytics** 
- Job completion timeline chart
- Channel success rates visualization
- Performance insights and recommendations

### 4. **Workers** 
- Worker pool overview with active count
- Individual worker metrics (CPU, Memory)
- Currently processing jobs list
- Worker health status indicators

### 5. **Dead Letter Queue** 
- Failed jobs requiring manual intervention
- Error messages and details
- Retry and delete actions
- Queue statistics

## 🚀 Quick Start (5 Minutes)

### 1. Extract and Install
```bash
unzip dashboard-light.zip
cd dashboard-light
npm install
```

### 2. Configure Backend
```bash
cp .env.example .env
# Edit .env and set your backend URL
VITE_API_BASE_URL=http://localhost:8888/api
```

### 3. Start Development Server
```bash
npm run dev
```

Dashboard opens at: **http://localhost:3000**

### 4. Build for Production
```bash
npm run build
```

## 🎨 Color Scheme

The light theme uses a professional color palette:
- **Primary**: Black (#000000) - for headers and key actions
- **Backgrounds**: White (#FFFFFF) and Light Gray (#F9FAFB)
- **Accents**: 
  - Green (#10b981) - Success/Completed
  - Yellow (#f59e0b) - Processing/Warning
  - Blue (#3b82f6) - Information
  - Red (#ef4444) - Errors/Failed

## 🔌 API Integration

### Required Endpoints

```
GET /api/jobs/stats              → System statistics
GET /api/jobs                    → List jobs (paginated)
GET /api/jobs/{id}               → Job details
GET /api/jobs/{id}/attempts      → Retry history
GET /api/jobs/dead-letter        → Failed jobs
GET /api/workers                 → Worker stats
POST /api/jobs/{id}/retry        → Retry job
DELETE /api/jobs/{id}            → Delete job
```

### Example Stats Response
```json
{
  "total": 1247,
  "pending": 45,
  "processing": 12,
  "completed": 1150,
  "failed": 40,
  "deadLetterCount": 8,
  "successRate": 92.3,
  "avgProcessingTime": 2.5,
  "workersActive": 5,
  "queueSize": 45
}
```

## 🛠️ Backend Configuration

### Enable CORS

Add this to your Spring Security config:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000", "https://yourdomain.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

## 📁 Project Structure

```
dashboard-light/
├── src/
│   ├── components/
│   │   ├── Dashboard.jsx          (Overview tab)
│   │   ├── JobsTable.jsx          (Jobs listing)
│   │   ├── Charts.jsx             (Analytics)
│   │   ├── WorkerStats.jsx        (Workers)
│   │   ├── DeadLetterQueue.jsx    (Failed jobs)
│   │   └── StatCard.jsx           (Metric cards)
│   ├── config/
│   │   └── api.js                 (API configuration)
│   ├── App.jsx                    (Main app)
│   ├── index.jsx                  (React entry)
│   └── index.css                  (Tailwind styles)
├── package.json                   (Dependencies)
├── vite.config.js                 (Build config)
├── tailwind.config.js             (Styling)
├── index.html                     (HTML template)
└── README.md                      (This file)
```

## 📦 Dependencies

- **React** 18.2 - UI framework
- **Tailwind CSS** 3.3 - Styling
- **Recharts** 2.10 - Data visualization
- **Lucide React** - Icons
- **Vite** 5.0 - Build tool

## 🎯 Development

### Run Development Server
```bash
npm run dev
```

### Build for Production
```bash
npm run build
```

### Preview Production Build
```bash
npm run preview
```

### Start with Custom Port
Edit `vite.config.js`:
```javascript
server: {
  port: 3001,  // Change port
}
```

## 🌐 Deployment

### Vercel (Recommended)
```bash
npm install -g vercel
vercel
```

### Netlify
```bash
npm run build
netlify deploy --prod --dir=dist
```

### AWS S3
```bash
npm run build
aws s3 sync dist/ s3://your-bucket/ --delete
```

### Docker
```bash
docker build -t job-dashboard .
docker run -p 3000:3000 -e VITE_API_BASE_URL=http://api:8888/api job-dashboard
```

## 🎨 Customization

### Change Color Scheme
Edit `tailwind.config.js` theme colors

### Modify Refresh Interval
Edit `src/App.jsx`:
```javascript
const interval = setInterval(fetchData, 10000); // 10 seconds
```

### Add New Components
1. Create component in `src/components/`
2. Import in `App.jsx`
3. Add tab in navigation

## 🔒 Security

- JWT authentication via localStorage
- CORS validation on backend
- API key protection
- Secure token transmission

## 📊 Metrics Monitored

- Total jobs processed
- Success rate percentage
- Average processing time
- Active workers
- Queue depth
- Failed jobs count
- Dead letter queue size
- Job completion timeline
- Retry distribution
- Channel success rates

## 🐛 Troubleshooting

### Cannot connect to backend
1. Verify backend is running on port 8888
2. Check CORS is enabled
3. Verify VITE_API_BASE_URL in .env

### Port 3000 already in use
1. Change port in vite.config.js
2. Or kill process: `lsof -i :3000 | grep LISTEN | awk '{print $2}' | xargs kill`

### No data displaying
1. Check browser console for API errors
2. Verify backend API endpoints exist
3. Check JWT token in localStorage

### Styling not loading
1. Clear node_modules: `rm -rf node_modules`
2. Reinstall: `npm install`
3. Restart dev server

## 📝 Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

MIT License - See LICENSE file for details

## 🙋 Support

For issues or questions:
1. Check the troubleshooting section
2. Review component files
3. Check browser console for errors
4. Verify API endpoints

---

**Dashboard Version:** 1.0.0 (Light Theme)  
**Last Updated:** January 25, 2026  
**Tested With:** Node 18+, React 18, Tailwind 3, Recharts 2

Happy monitoring! 🚀
