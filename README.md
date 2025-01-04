# 🛡️ Anti-Fraud System

A robust Spring Boot application implementing fraud detection for financial transactions.

## 🎯 Overview
This project demonstrates principles of anti-fraud systems used in the financial sector, featuring:
- User authentication and authorization
- REST API endpoints for transaction processing
- Rule-based fraud detection
- Transaction feedback system

## ⚙️ Technical Stack
- **Framework**: Spring Boot
- **Security**: Spring Security
- **Database**: MySQL
- **Build Tool**: Gradle
- **Testing**: JUnit

## 🔑 Key Features
- **Transaction Validation**: Rules-based validation for monetary transactions
- **Role-Based Access Control**: 
  - ADMINISTRATOR
  - MERCHANT  
  - SUPPORT
- **Fraud Detection Rules**:
  - Amount validation
  - IP address check
  - Card number validation (Luhn algorithm)
  - Region correlation
- **Transaction Feedback System**

## 🚀 Getting Started

1. Clone the repository
2. Build the project:
```bash
./gradlew build
```
3. Run the application:
```bash
./gradlew bootRun
```
The server will start on port 28852.

## 🔒 Security
- HTTP Basic Authentication
- Role-based authorization
- Transaction validation
- Card number verification

## 📝 API Endpoints

### Authentication
- `POST /api/auth/user` - Register new user
- `GET /api/auth/list` - Get list of users

### 👮🏽‍♀️ Administrator
- `PUT /api/auth/role` - Change user role
- `PUT /api/auth/access` - Moderate users' platform accesss
- `DELETE /api/auth/user/{username}` - Delete user

### Transaction Processing
- `POST /api/antifraud/transaction` - Process transaction
- `PUT /api/antifraud/transaction` - Update transaction
- `GET /api/antifraud/history` - Get transaction history
- - `GET /api/antifraud/history/{number}` - Get transaction history for a specific card
- `PUT /api/antifraud/transaction` - Add feedback

### Card & IP Management
- `POST /api/antifraud/suspicious-ip` - Add suspicious IP
- `DELETE /api/antifraud/suspicious-ip/{ip}` - Remove suspicious IP
- `POST /api/antifraud/stolencard` - Add stolen card
- `GET /api/antifraud/stolencard` - View all stolen cards
- `DELETE /api/antifraud/stolencard/{number}` - Remove card from stolen cards database


## 👥 Roles & Permissions
- **ADMINISTRATOR**: User management
- **MERCHANT**: Transaction processing
- **SUPPORT**: Feedback and fraud rules management

## 🔍 Validation Rules
- Transaction amount limits
- IP correlation checks
- Card number validation
- Regional payment patterns
