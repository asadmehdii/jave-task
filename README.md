# BioSteel Teams - Backend API

## Tech Stack
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT authentication
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven
- **Java Version**: 17+

## API Endpoints
### Swagger/OpenAPI Documentation
- Local: http://localhost:8080/swagger-ui/index.html
- Local API Docs: http://localhost:8080/v1/api-docs
- DEV Environment: 
  - http://18.219.198.202/swagger-ui/index.html
  - http://18.219.198.202/v1/api-docs

## Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL 12+
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/MobiStreamSolutions/biosteel-teams-api.git
   cd biosteel-teams-api
   ```

2. **Database Setup**
   ```bash
   # Navigate to database scripts
   cd src/main/resources/db
   
   # Run database setup script
   bash script.bat
   ```

3. **Configure Application**
   
   Set the required environment variables (get values from backend developer):
   
   ```bash
   # Minimum required variables for development
   export DB_HOST=localhost
   export DB_USERNAME=biosteel
   export DB_PASSWORD=Mobi.biosteel.1
   export JWT_SECRET=dev-secret-key-change-in-production
   
   # Additional required variables (ask backend developer for values)
   export MAIL_HOST=smtp.gmail.com
   export MAIL_USERNAME=your-email@gmail.com
   export MAIL_PASSWORD=your-app-password
   export FROM_EMAIL=noreply@biosteel.com
   export SUPPORT_EMAIL=support@biosteel.com
   export VERIFICATION_BASE_URL=http://localhost:8080/verify
   export RESET_PASSWORD_BASE_URL=http://localhost:8080/reset
   export OPEN_API_SERVER=http://localhost:8080
   ```

4. **Build and Run**
   ```bash
   # Clean and build the project
   mvn clean install
   
   # Run the application
   mvn spring-boot:run
   ```

## API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui/index.html
```

## Environment Variables

The application requires several environment variables. For a complete list, check the `application.properties` file. Key variables include:

- **Database**: `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`
- **Security**: `JWT_SECRET`
- **Email**: `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- **AWS S3**: `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`
- **Firebase**: `FIREBASE_PROJECT_ID`, `FIREBASE_PRIVATE_KEY`
- **Stream**: `STREAM_API_KEY`, `STREAM_API_SECRET`

Contact the backend developer for production values.