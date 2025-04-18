# 💬 Chat Application

A real-time Chat App built with **Android (Kotlin)** using **Cloud Firestore** for backend storage and synchronization. This app allows users to sign in or sign up, view their recent chats, and start new conversations with other registered users.

---

## 🚀 Features

### 📱 Android App (Frontend)
✅ User Authentication (Sign In / Sign Up)  
✅ Displays list of recent chats after login  
✅ Start new chat with other users from "New Chat" screen  
✅ Real-time messaging using Firestore listeners  
✅ Unique chat rooms for each user pair  
✅ MVVM Architecture  
✅ Smooth UI experience with LiveData & ViewModel  
✅ Token/session management using SharedPreferences  
✅ Optimized for performance and responsiveness

---

## ☁️ Backend (Firebase - Firestore)

✅ Firebase Authentication for user login/signup  
✅ Cloud Firestore used as NoSQL database  
✅ Firestore Collections:
- `users`: stores user profiles  
- `chats`: stores recent chats  
- `messages`: stores messages between users

✅ Real-time sync and updates using Firestore snapshot listeners  
✅ Secure data rules to restrict unauthorized access

---

## 📲 App Flow

1. **Sign In / Sign Up**:  
   New users can create an account or sign in with their credentials.

2. **Recent Chats Screen**:  
   After authentication, users are navigated to the recent chat list.

3. **Start New Chat**:  
   Tap "New Chat" to see a list of users and start a new conversation.

4. **Real-Time Messaging**:  
   Messages are instantly synced and displayed using Firestore's real-time listeners.

5. **Logout**:  
   Users can log out to clear their session.

---

