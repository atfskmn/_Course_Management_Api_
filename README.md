# Course Management API

Spring Boot tabanlı kurs yönetim sistemi backend API'si.

## 🚀 Özellikler

- Öğrenci ve öğretmen yönetimi
- Kurs oluşturma ve yönetimi
- Sepet sistemi
- Sipariş takibi
- JWT tabanlı kimlik doğrulama
- RESTful API

## 📋 Gereksinimler

- Java 17 veya üzeri
- Maven 3.6+
- MySQL/PostgreSQL

## 🛠️ Kurulum

1. Projeyi klonlayın:

```bash
git clone https://github.com/atfskmn/_Course_Management_Api_.git
cd _Course_Management_Api_
```

2. Veritabanı ayarlarını `src/main/resources/application.properties` dosyasından yapılandırın.

3. Projeyi derleyin ve çalıştırın:

```bash
./mvnw spring-boot:run
```

veya

```bash
mvn spring-boot:run
```

## 🌐 Frontend

Frontend uygulaması [course-Management-front](https://github.com/atfskmn/course-Management-front) deposunda bulunmaktadır.

Frontend'i başlatmak için:

```bash
npm run dev
```

## 📚 API Dokümantasyonu

Postman collection dosyası: `Course Management System API.postman_collection.json`

## 🔐 Güvenlik

Proje JWT (JSON Web Token) tabanlı kimlik doğrulama kullanmaktadır.

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.
