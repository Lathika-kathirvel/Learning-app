package com.example.learningapp.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

object PdfTextExtractor {

    // Keeps the AI request small and fast.
    private const val MAX_CHARS = 6000
    private const val MAX_PAGES = 5

    @Volatile
    private var initialised = false

    /**
     * Reads real text from a PDF (handles compressed PDFs).
     * Returns "" for scanned/image-only PDFs. Call from a background thread.
     */
    fun extractTextFromPdf(context: Context, uri: Uri): String {
        return try {
            if (!initialised) {
                PDFBoxResourceLoader.init(context.applicationContext)
                initialised = true
            }

            val text = context.contentResolver.openInputStream(uri)?.use { input ->
                PDDocument.load(input).use { doc ->
                    val stripper = PDFTextStripper().apply {
                        startPage = 1
                        endPage = minOf(doc.numberOfPages, MAX_PAGES)
                    }
                    stripper.getText(doc)
                }
            }.orEmpty()

            text.replace(Regex("\\s+"), " ").trim().take(MAX_CHARS)

        } catch (e: Throwable) {
            Log.e("PDF_EXTRACT", "Could not read PDF text", e)
            ""
        }
    }
}