# Implementation Plan - Fix Resume Upload in Interview Preparation

Fix the resume upload and loading issue in `InterviewPreparationActivity` where selecting a PDF resume opens the file picker but fails to upload/extract text correctly or process it for AI interview preparation.

## User Review Required

> [!IMPORTANT]
> We will enhance `InterviewPreparationActivity` to match the robust PDF picker and extraction logic used in `HRInterviewActivity`, including broad MIME type support (`application/pdf`, `application/octet-stream`, `*/*`), file validation, local text extraction with backend API fallback (`uploadResume` / `extractResume`), and visual loading feedback.

## Proposed Changes

### Interview Preparation Feature

#### [MODIFY] [InterviewPreparationActivity.kt](file:///C:/Users/lathi/AndroidStudioProjects/LearningApp/app/src/main/java/com/example/learningapp/InterviewPreparationActivity.kt)
- Update `selectPdfLauncher` to support broader MIME types (`application/pdf`, `application/octet-stream`, `*/*`) and validate the file.
- Add robust resume extraction (local `PdfTextExtractor` with backend API upload/extraction fallback).
- Update status text and UI feedback while processing the uploaded resume.

## Verification Plan

### Automated Tests
- Build project using `./gradlew :app:assembleDebug`.

### Manual Verification
- Open Interview Preparation screen.
- Click "Choose PDF Resume" / "Upload Resume".
- Select a PDF resume and verify that it loads successfully, shows the correct file name, extracts text, and successfully passes the resume context to the AI interview generation.
