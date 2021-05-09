package com.example.piller.listAdapters

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R

//  the dataset is a list of pairs<question, answer>
class HelpAdapter(private var dataSet: MutableList<Pair<String, String>>) :
    RecyclerView.Adapter<HelpAdapter.ViewHolder>() {
    private val underlineStart = 0
    private val underlineFlags = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val question: TextView = view.findViewById(R.id.hi_question_tv)
        val answer: TextView = view.findViewById(R.id.hi_answer_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.settings_help_qa_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataSet[position]

        //  add the question with underline
        val content = SpannableString(currentItem.first)
        content.setSpan(UnderlineSpan(), underlineStart, content.length, underlineFlags)
        holder.question.text = content
        holder.answer.text = currentItem.second
    }

    override fun getItemCount() = dataSet.size
}
