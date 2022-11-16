package ru.mirea.cloudclients

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.TextKeyListener.clear
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ViewPagerAdapter
    private lateinit var tabs: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        adapter = ViewPagerAdapter(this, listOf("Задачи", "Сотрудники"))
        viewPager = findViewById(R.id.viewpager)
        viewPager.adapter = adapter
        tabs = findViewById(R.id.tabs)
        TabLayoutMediator(tabs, viewPager) {tab, position ->
            tab.text = adapter.getName(position)
        }.attach()
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.appbar)))
        supportActionBar?.title = "ID: ${DatabaseController.id} Логин: ${DatabaseController.sLogin}"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.log_out -> {
                getSharedPreferences("AUTH", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        //Toast.makeText(this, (findViewById<FloatingActionButton>(R.id.addEmp) == null).toString(), Toast.LENGTH_SHORT).show()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            DatabaseController.closeConnection()
        }
        Log.i("LIFECYCLE", "MainActivity Destroyed")
    }
}