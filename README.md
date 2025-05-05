<h1>Setting up ClickShop - Backend</h1>

<p>ClickShop is a sleek and scalable eCommerce web app built with Angular 19, Tailwind CSS and TypeScript. It connects to a Spring Boot backend to deliver a seamless shopping experience with features like secure OAuth2 + JWT authentication, Razorpay payments, AI-powered personalization using Gemini and email notifications via Mailtrap.</p>

<h2>Features</h2>

Here're some of the project's best features:

*   Secure Authentication – OAuth 2.0 with JWT for protected and role-based access
*   Smart Order Management – Real-time order tracking with dynamic status updates (Pending Shipped Delivered)
*   Auto-generated Invoices – Downloadable invoices after each successful payment
*   Admin Reports Dashboard – Sales revenue and order analytics for admins
*   Order Alert Emails – Automated order confirmation and shipping update emails via Mailtrap
*   Razorpay Integration – Seamless secure online payments with transaction tracking
*   AI Assistance (Gemini) – Smart product recommendations and contextual suggestions
*   User-Friendly Cart & Checkout – Clean UI for cart updates address management and order placement

<h2>Installation Steps:</h2>

<p>1. Clone the repository:</p>

```
git clone https://github.com/chiranjeevsehgal/Clickshop---Backend.git
cd Clickshop---Backend
```

<p>2. Navigate to the src/main/resources directory:</p>
<p>(If the /resources folder doesn't exist, create it manually.)</p>

<p>3. Inside that folder, create a new file named application.properties and add the following configuration.</p>

```
spring.application.name=Clickshop
server.port=8081

# Spring DB+JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.datasource.url=<DB_URL>
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASSWORD>

# Razorpay Configuration
razorpay.key.id=<RAZORPAY_KEY_ID>
razorpay.key.secret=<RAZORPAY_KEY_SECRET>

# Mailtrap Configuration
mailtrap.from.name=ClickShop
mailtrap.api.token=<MAILTRAP_API_TOKEN>
mailtrap.template.order-confirmation-customer=<MAILTRAP_TEMPLATE_CUSTOMER>
mailtrap.template.order-confirmation-admin=<MAILTRAP_TEMPLATE_ADMIN>
mailtrap.template.otp-verification=<MAILTRAP_TEMPLATE_OTP>
mailtrap.from.email=<MAILTRAP_FROM_EMAIL>
mailtrap.from.email.authenticate=<MAILTRAP_AUTH_EMAIL>
mailtrap.admin.email=<MAILTRAP_ADMIN_EMAIL>

# JWT Secret
jwt.secret=<JWT_SECRET>

# Gemini AI Configuration
gemini.api.key=<GEMINI_API_KEY>
gemini.api.model=<GEMINI_API_MODEL>

# OAuth Configuration
spring.security.oauth2.client.registration.google.scope=email,profile
spring.security.oauth2.client.registration.google.client-id=<GOOGLE_CLIENT_ID>
spring.security.oauth2.client.registration.google.client-secret=<GOOGLE_CLIENT_SECRET>
spring.security.oauth2.client.registration.google.redirect-uri=<GOOGLE_REDIRECT_URI>

```

<p>4. Start the Springboot application:</p>

```
mvnw spring-boot:run
```

<p>5. The server will now be running on:</p>

```
http://localhost:8081
```

<p>6. You can find and configure the frontend from the following repository:</p>

```
https://github.com/chiranjeevsehgal/Clickshop---Frontend
```

<h2>Built with</h2>

Technologies used in the project:

*  Spring Boot

*  MySQL

*  Spring Data JPA

*  Razorpay

*  Gemini AI

*  Mailtrap

*  OAuth 2.0

*  JWT (JSON Web Token)
