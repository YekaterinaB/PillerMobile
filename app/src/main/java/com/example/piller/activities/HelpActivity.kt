package com.example.piller.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.piller.R
import com.example.piller.listAdapters.HelpAdapter


class HelpActivity : AppCompatActivity() {
    private lateinit var qAndARV: RecyclerView
    private lateinit var qAndAAdapter: HelpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        initViews()
    }

    private fun getQANDAList(): MutableList<Pair<String, String>> {
        val qAndA = mutableListOf<Pair<String, String>>()
        qAndA.add(Pair("How Can I Contact Piller?", "Send an email to: piller.inc1@gmail.com"))
        qAndA.add(
            Pair(
                "How Can I Add a New Medicine?",
                "In home view, tap on the plus button on bottom right and then there are 2 options: \n1) By name \n2) By box image\n3) By pill image"
            )
        )
        qAndA.add(
            Pair(
                "How to stop receiving notifications?",
                "Press on the three dots on top right, and turn off the notifications"
            )
        )

        return qAndA
    }

    private fun initViews() {
        qAndARV = findViewById(R.id.help_q_and_a_rv)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        qAndARV.layoutManager = layoutManager
        qAndAAdapter = HelpAdapter(getQANDAList())
        qAndARV.adapter = qAndAAdapter

        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        qAndARV.addItemDecoration(dividerItemDecoration)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}