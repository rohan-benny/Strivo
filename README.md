# Strivo 📱  
**Department Management System for Academic Institutions**

Strivo is an Android-based department management application designed to streamline academic and administrative operations within a college department. The system is primarily built for **HODs and faculty members** to manage events, meetings, timetables, circulars, room availability, and departmental communication in a centralized platform.

---

## 📌 Project Overview

In many educational institutions, department-level operations are handled using emails, paper records, and manual coordination, leading to inefficiencies and miscommunication. **Strivo** addresses these challenges by providing a **digital, centralized, and real-time solution** for managing departmental activities.

This project was developed as part of an **MCA academic project** with a focus on real-world problem solving using modern Android and Firebase technologies.

---


## 🚀 Features

### 👨‍🏫 Faculty & HOD Module
- Secure login using Firebase Authentication
- Role-based access (HOD / Faculty)
- View and manage personal timetable
- Schedule and manage meetings
- Create and view circulars and bulletins
- Event coordination with assigned faculty
- Room availability checking
- Push notifications for important updates

### 🗓️ Event & Meeting Management
- Create and manage department events
- Schedule meetings with agenda and venue
- Automatic listing under “My Meetings”
- Real-time updates using Firebase

### 🏫 Academic Utilities
- Timetable management
- Room allocation tracking
- Centralized department dashboard
- Profile and role-based data handling

---

## 🛠️ Tech Stack

### Android App
- **Language:** Kotlin  
- **IDE:** Android Studio  
- **UI:** XML, Material Components  
- **Architecture:** Activity & Fragment based  

### Backend & Services
- **Firebase Authentication**
- **Firebase Realtime Database**
- **Firebase Cloud Messaging (FCM)**

### Tools & Libraries
- ViewBinding
- RecyclerView
- Firebase SDK
- Gradle (Kotlin DSL)

---

## 📂 Project Structure


Strivo
├── app
│ ├── src/main
│ │ ├── java/com/example/strivo
│ │ ├── res
│ │ └── AndroidManifest.xml
│ ├── build.gradle.kts
│ └── google-services.json
├── gradle
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── .gitignore
└── README.md

---

## 🖼️ Screenshots

### Login Screen
![Login Screen](screenshots/login_screen.png)

### Home Dashboard
![Home Dashboard](screenshots/home_dashboard.png)

### Meeting Management
![Meeting Management](screenshots/meeting_management.png)

### Event Management
![Event Management](screenshots/event_management.png)

---

## ⚙️ Setup Instructions

Clone the repository
1. git clone https://github.com/your-username/Strivo.git

2. Open the project in Android Studio

3. Add your Firebase configuration

4. Replace google-services.json with your own Firebase project file

5. Sync Gradle files and run the application on an emulator or physical device
---

🔐 Firebase Configuration

**Enable Email/Password Authentication

**Configure Realtime Database

**Set appropriate database rules

**Enable Firebase Cloud Messaging for notifications

🎯 Future Enhancements

**Student module integration

**Admin web dashboard

**Attendance management system

**Analytics and reporting

**Multi-department support

👨‍💻 Developer

Rohan Benny
MCA Student
Kristu Jayanti College (Autonomous), Bangalore

📜 License
This project was developed for academic and learning purposes.

This project is developed for academic and learning purposes.
You are free to fork, modify, and enhance the project.
