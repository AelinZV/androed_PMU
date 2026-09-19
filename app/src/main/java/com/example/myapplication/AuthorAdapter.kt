package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class AuthorAdapter(
    private val context: Context,
    private val authors: List<Author>
) : BaseAdapter() {

    override fun getCount(): Int {
        return authors.size
    }

    override fun getItem(position: Int): Author {
        return authors[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {

        val view = convertView
            ?: LayoutInflater.from(context)
                .inflate(
                    R.layout.item_author,
                    parent,
                    false
                )

        val imageAuthor =
            view.findViewById<ImageView>(
                R.id.imageAuthor
            )

        val textAuthorName =
            view.findViewById<TextView>(
                R.id.textAuthorName
            )

        val author = authors[position]

        imageAuthor.setImageResource(
            author.photoResId
        )

        textAuthorName.text =
            author.name

        return view
    }
}