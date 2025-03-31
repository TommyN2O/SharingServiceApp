package com.example.sharingserviceapp.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.example.sharingserviceapp.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sharingserviceapp.adapters.CategoryAdapter
import com.example.sharingserviceapp.models.Category
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // Get the Search Button
        val searchButton: ImageView = findViewById(R.id.search_button)
//
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Sample Data for Merged List
        val categories = listOf(
            Category("Cleaning", R.drawable.clean_category),
            Category("Painting", R.drawable.clean_category),
            Category("Help Moving", R.drawable.clean_category),
            Category("Furniture Assembly", R.drawable.clean_category),
            Category("Yard Work", R.drawable.clean_category),
            Category("Heavy Lifting", R.drawable.clean_category),
            Category("Business", R.drawable.clean_category),
            Category("Travel", R.drawable.clean_category)
        )

        // Set up CategoryAdapter and pass the selected category to HelperListActivity
        val categoryAdapter = CategoryAdapter(categories) { categoryName ->
            val intent = Intent(this, HelperListActivity::class.java)
            intent.putExtra("category_name", categoryName) // Pass category name
            startActivity(intent)
        }

        recyclerView.adapter = categoryAdapter

//


        // Set an OnClickListener to trigger search functionality
        searchButton.setOnClickListener {
            // Add your search functionality here, like opening a search bar
            Toast.makeText(this, "Search button clicked", Toast.LENGTH_SHORT).show()
        }
//

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    // Handle home item click
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_tasks -> {
                    // Handle tasks item click
                    startActivity(Intent(this, TasksActivity::class.java))
                    finish()// Check here
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_messages -> {
                    // Handle messages item click
                    startActivity(Intent(this, MessagesActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.menu_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                else -> false
            }
        }
    }
}
