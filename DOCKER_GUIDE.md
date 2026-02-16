# Running the Student Management System with Docker

## Prerequisites
- Docker Desktop installed and running on Windows
- No need for Maven, Java, or PostgreSQL installed locally!

## Quick Start

### 1. Build and Start Everything
Open PowerShell in the project directory and run:
```powershell
docker compose up --build
```

This single command will:
- Build your Spring Boot application inside a Docker container
- Start PostgreSQL database
- Start your application
- Connect them together

### 2. Access Your Application
Once you see "Started StudentManageementSystemApplication" in the logs:
- Application: http://localhost:8080
- PostgreSQL: localhost:5432

### 3. Stop Everything
Press `Ctrl+C` in the terminal, then run:
```powershell
docker compose down
```

To also remove the database data:
```powershell
docker compose down -v
```

## Useful Commands

### View Logs
```powershell
# All services
docker compose logs -f

# Just the app
docker compose logs -f app

# Just the database
docker compose logs -f postgres
```

### Rebuild After Code Changes
```powershell
docker compose up --build
```

### Run in Background (Detached Mode)
```powershell
docker compose up -d --build
```

### Stop Background Services
```powershell
docker compose down
```

### Check Running Containers
```powershell
docker compose ps
```

### Access Database Shell
```powershell
docker exec -it sms-postgres psql -U postgres -d postgres
```

## How It Works

1. **Dockerfile**: Defines how to build your Spring Boot app
   - Uses Maven to compile your code
   - Creates a lightweight runtime image
   - No need for Maven or Java on your machine!

2. **compose.yaml**: Orchestrates all services
   - PostgreSQL database service
   - Your Spring Boot application service
   - Automatic networking between them
   - Health checks to ensure proper startup order

3. **.dockerignore**: Excludes unnecessary files from Docker build
   - Makes builds faster
   - Reduces image size

## Troubleshooting

### Port Already in Use
If port 8080 or 5432 is already in use:
```powershell
# Find and stop the process using the port
netstat -ano | findstr :8080
```

### Database Connection Issues
- Wait 30 seconds after starting for PostgreSQL to initialize
- Check logs: `docker compose logs postgres`

### Rebuild from Scratch
```powershell
docker compose down -v
docker compose build --no-cache
docker compose up
```

## What You DON'T Need Locally
❌ Maven  
❌ Java JDK  
❌ PostgreSQL  

## What You DO Need
✅ Docker Desktop  
✅ Your code  

That's it! 🚀
