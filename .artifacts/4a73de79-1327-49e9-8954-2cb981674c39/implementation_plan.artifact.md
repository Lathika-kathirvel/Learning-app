# Aptitude Quiz Result Page with AI Analysis

Create a dedicated result activity for Aptitude Tests (`AptitudeResultActivity`) that replaces the current AlertDialog popup. This page will display the final score, percentage, a detailed AI performance analysis via Groq LLM, and actionable recommendations on what the candidate needs to improve.

## User Review Required

> [!NOTE]
> The new results page follows the established UI pattern of `TechnicalMcqResultActivity`, including score cards, loading indicators for AI analysis, and structured feedback.

## Proposed Changes

### [Aptitude Test Result Feature]

#### [NEW] [AptitudeResultActivity.kt](file:///C:/Users/lathi/AndroidStudioProjects/LearningApp/app/src/main/java/com/example/learningapp/AptitudeResultActivity.kt)
- New Activity to display aptitude test results, score percentage, and asynchronous Groq AI analysis on candidate performance and areas for improvement.

#### [NEW] [activity_aptitude_result.xml](file:///C:/Users/lathi/AndroidStudioProjects/LearningApp/app/src/main/res/layout/activity_aptitude_result.xml)
- Layout XML file for the aptitude result screen with score summary cards, AI analysis section, and finish action button.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/lathi/AndroidStudioProjects/LearningApp/app/src/main/AndroidManifest.xml)
- Register `AptitudeResultActivity`.

#### [MODIFY] [AptitudeQuestionsActivity.kt](file:///C:/Users/lathi/AndroidStudioProjects/LearningApp/app/src/main/java/com/example/learningapp/AptitudeQuestionsActivity.kt)
- Update `showResult()` to start `AptitudeResultActivity` with score, topic, difficulty, and question count data instead of showing an AlertDialog.

## Verification Plan

### Automated Tests
- Build project with `gradle_build` (`app:assembleDebug`).

### Manual Verification
- Launch Aptitude Test, complete the quiz, and verify that upon completion, the app navigates to the separate `AptitudeResultActivity` screen showing score, percentage, and Groq AI analysis & improvement recommendations.
