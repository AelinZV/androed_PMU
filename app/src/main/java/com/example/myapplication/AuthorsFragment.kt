package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.fragment.app.Fragment

class AuthorsFragment : Fragment(R.layout.fragment_authors) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val listAuthors =
            view.findViewById<ListView>(R.id.listAuthors)

        val authors = listOf(

            Author(
                "Студент 1",
                android.R.drawable.ic_menu_myplaces
            ),

            Author(
                "Студент 2",
                android.R.drawable.ic_menu_myplaces
            ),

            Author(
                "Студент 3",
                android.R.drawable.ic_menu_myplaces
            )
        )

        listAuthors.adapter =
            AuthorAdapter(
                requireContext(),
                authors
            )
    }
}