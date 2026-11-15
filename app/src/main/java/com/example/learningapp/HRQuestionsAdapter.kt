package com.example.learningapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.learningapp.api.HRQuestionItem

class HRQuestionsAdapter(
    private val questions: List<HRQuestionItem>
) : RecyclerView.Adapter<HRQuestionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tvQuestionNumberBadge)
        val tvCategory: TextView = view.findViewById(R.id.tvQuestionCategoryBadge)
        val tvContent: TextView = view.findViewById(R.id.tvQuestionContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hr_question, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questions[position]
        val qNum = item.questionNumber ?: (position + 1)
        holder.tvNumber.text = "Question $qNum"
        holder.tvCategory.text = item.category?.ifBlank { "General HR" } ?: "General HR"
        holder.tvContent.text = item.question ?: ""
    }

    override fun getItemCount(): Int = questions.size
}
