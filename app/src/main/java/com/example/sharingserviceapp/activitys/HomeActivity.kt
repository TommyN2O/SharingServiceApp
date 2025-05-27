package com.example.sharingserviceapp.activitys

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharingserviceapp.R
import com.example.sharingserviceapp.adapters.CategoryAdapter
import com.example.sharingserviceapp.models.Category
import com.example.sharingserviceapp.network.ApiServiceInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private var fullCategoryList: List<Category> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        fetchCategories()
        setupBottomNavigation()
        setupListeners()

    }

    private fun fetchCategories() {
        val apiService = ApiServiceInstance.Auth.apiServices
        apiService.getCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    fullCategoryList = categories
                    categoryAdapter = CategoryAdapter(fullCategoryList) { categoryName ->
                        val intent = Intent(this@HomeActivity, HelperListActivity::class.java)
                        intent.putExtra("category_id", categoryName.id)
                        intent.putExtra("category_name", categoryName.name)
                        startActivity(intent)
                    }
                    recyclerView.adapter = categoryAdapter
                } else {
                    Toast.makeText(this@HomeActivity, getString(R.string.failed_load_categories), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun filterCategories(query: String) {
        val filteredList = fullCategoryList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        categoryAdapter.updateList(filteredList)
    }

    private fun setupListeners() {
        val searchButton: ImageView = findViewById(R.id.search_button)
        val searchView: SearchView = findViewById(R.id.search_view)
        val title: TextView = findViewById(R.id.welcome_title)
        searchButton.setOnClickListener {
            searchButton.visibility = View.GONE
            title.visibility=View.GONE
            searchView.visibility = View.VISIBLE
            searchView.requestFocus()
        }
        searchView.setOnCloseListener {
            searchView.visibility = View.GONE
            title.visibility=View.VISIBLE
            searchButton.visibility = View.VISIBLE
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCategories(newText ?: "")
                return true
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.menu_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_tasks -> {
                    startActivity(Intent(this, PlannedTasksActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_messages -> {
                    startActivity(Intent(this, MessagesActivity::class.java))
                    finish()
                    true
                }
                R.id.menu_more -> {
                    startActivity(Intent(this, MoreActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}