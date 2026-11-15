
# LearningApp – AI-Powered Learning and Interview Preparation

LearningApp is an Android application developed using Kotlin and Android Studio. It helps students improve their aptitude skills and prepare for HR and technical interviews with AI-assisted features.

## 🚀 Features

- User Registration and Login
- Firebase Authentication
- Student Dashboard
- Aptitude Practice
- Aptitude Questions and Results
- Technical Interview Preparation
- AI-Generated Technical Questions
- HR Interview Preparation
- Resume-Based Interview Preparation
- PDF Resume Text Extraction
- AI Interview Performance Analysis
- User Profile Management
- Recent Activity Tracking

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Android application development |
| Android Studio | Development environment |
| XML | UI design |
| Firebase Authentication | User authentication |
| Firebase Firestore | Cloud database |
| Retrofit | API integration |
| OkHttp | Network communication |
| Gson | JSON parsing |
| Kotlin Coroutines | Background operations |
| RecyclerView | Displaying lists |
| PDFBox Android | PDF text extraction |
| Groq AI | AI-powered question generation |

## 📱 Application Modules

### 1. User Authentication
- User registration
- Login functionality
- Firebase authentication

### 2. Dashboard
- Centralized learning dashboard
- Navigation to different learning modules
- Recent activity

### 3. Aptitude Practice
- Select aptitude practice settings
- Answer aptitude questions
- Calculate scores
- Display results

### 4. Technical Interview
- Configure technical interview sessions
- Generate technical questions
- Practice technical MCQs
- View interview results

### 5. HR Interview
- HR interview preparation
- Practice common HR questions

### 6. Resume-Based Interview
- Upload PDF resume
- Extract resume text
- Generate interview-related questions

### 7. AI Interview Analysis
- Analyze interview performance
- Display performance insights

### 8. Profile Management
- View user details
- Update profile information

## 📂 Project Structure

```text
LearningApp/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/example/learningapp/
│   │       │       ├── api/
│   │       │       ├── DashboardActivity.kt
│   │       │       ├── AptitudeSetupActivity.kt
│   │       │       ├── AptitudeQuestionsActivity.kt
│   │       │       ├── AptitudeResultActivity.kt
│   │       │       ├── TechnicalInterviewSetupActivity.kt
│   │       │       ├── GeneratedQuestionsActivity.kt
│   │       │       ├── HRInterviewActivity.kt
│   │       │       ├── AIResumeInterviewActivity.kt
│   │       │       ├── AIInterviewAnalysisActivity.kt
│   │       │       └── ProfileActivity.kt
│   │       │
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle.kts
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## ⚙️ Installation and Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/Lathika-kathirvel/Learning-app.git
```

### Step 2: Open in Android Studio

1. Open Android Studio.
2. Select Open Project.
3. Choose the cloned LearningApp folder.
4. Wait for Gradle synchronization.

### Step 3: Firebase Configuration

1. Create a Firebase project.
2. Register your Android application.
3. Add the `google-services.json` file inside the app directory.
4. Enable Firebase Authentication.
5. Configure Cloud Firestore if required.

### Step 4: Configure AI Integration

Configure your Groq API credentials securely.

Never upload API keys or passwords to GitHub.

### Step 5: Run the Application

1. Connect an Android device or start an emulator.
2. Select the application configuration.
3. Click the Run button.

## 🎥 Output Video

Watch the application demonstration:

[LearningApp Demo Video](assets/LearningApp-Demo.mp4)

Note: Upload your actual MP4 demonstration video to the assets folder to activate this link.


## 🔐 Security

- Do not expose API keys in source code.
- Do not upload passwords or private credentials.
- Use secure configuration for API integrations.
- Keep sensitive Firebase configuration and signing credentials protected.

## 👩‍💻 Developed By

**Lathika Kathirvel**

GitHub: https://github.com/Lathika-kathirvel

## 🔗 GitHub Repository

https://github.com/Lathika-kathirvel/Learning-app

## 📄 License

This project is developed for educational purposes.
