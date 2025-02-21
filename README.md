# To-Do List App

## Overview
The **To-Do List App** is a simple and efficient task management application for Android. Built using **Java** for the frontend and **Flask** for the backend, this app allows users to create and organize their daily tasks seamlessly. The backend uses **SQLite3** as the database for storing user data and tasks.

## Features
- User authentication (Register/Login)
- Create and delete tasks
- Mark tasks as completed
- Persistent task storage using a Flask-based backend
- Responsive and user-friendly UI

## Tech Stack
### Frontend (Android)
- **Java**
- **Android SDK**
- **XML** (for UI design)
- **Volley** (for API requests)
- **SQLite** (local storage)

### Backend (Flask)
- **Python**
- **Flask** (REST API)
- **SQLite3** (Database)
- **JWT Authentication**

## Installation & Setup
### Backend Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/todo-list-app.git
   cd todo-list-app/backend
   ```
2. Create and activate a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows use `venv\Scripts\activate`
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Run the Flask server:
   ```bash
   flask run
   ```

### Android Setup
1. Open the `android` folder in **Android Studio**.
2. Sync Gradle and ensure all dependencies are installed.
3. Configure the backend API URL in the Retrofit client.
4. Run the app on an emulator or a physical device.

## API Endpoints
| Method | Endpoint | Description |
|--------|-------------|----------------|
| POST   | `/register` | User Registration |
| GET   | `/register` | User Registration |
| POST   | `/login` | User Login |
| GET   | `/login` | User Login |
| GET    | `/tasks` | Fetch all tasks |
| POST   | `/tasks` | Create a new task |
| PUT    | `/tasks/<id>` | Delete a task |

Give a ⭐ if you like this project!

