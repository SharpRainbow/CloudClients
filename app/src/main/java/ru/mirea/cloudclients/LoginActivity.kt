package ru.mirea.cloudclients

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.postgresql.util.PSQLException
import java.sql.DriverManager
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {

    lateinit var enter: Button
    lateinit var login: EditText
    lateinit var password: EditText
    lateinit var progress: ProgressBar
    private val sharedPref by lazy {
        this.getSharedPreferences("AUTH", MODE_PRIVATE)
    }
    //private val jdbcUrl = "jdbc:postgresql://dpg-cdcpslmn6mpsbhf0ehdg-a.frankfurt-postgres.render.com:5432/practice_0odm"
    //val connection by lazy { DriverManager.getConnection(jdbcUrl, "admin",
    //    "dAN5AHwzE3YXwrKu7tUXzcMxy2pwaPyx") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.appbar)))
        bindViews()
        bindListeners()
        val log = sharedPref.getString("LOGIN", null)
        if (log != null) {
            val pass = sharedPref.getString("PASS", "")
            login.setText(log)
            password.setText(pass)
            enter.callOnClick()
        }
    }

    private fun bindViews(){
        enter = findViewById(R.id.enterBtn)
        login = findViewById(R.id.loginFld)
        password = findViewById(R.id.passFld)
        progress = findViewById(R.id.progressBar)
    }

    private fun bindListeners(){
        enter.setOnClickListener {
            lifecycleScope.launch {
                progress.isVisible = true
                //progress.setProgress(0, true)
                when (val msg :Result<String> = DatabaseController.login(login.text.toString().trim(),
                    password.text.toString().trim())) {
                    is Result.Success<String> -> {
                        //Toast.makeText(applicationContext, msg.data, Toast.LENGTH_SHORT).show()
                        with (sharedPref.edit()){
                            putString("LOGIN", login.text.toString().trim())
                            putString("PASS", password.text.toString().trim())
                            apply()
                        }
                        goToMainScreen()
                    }
                    is Result.Error -> Toast.makeText(applicationContext, msg.exception.message, Toast.LENGTH_SHORT).show()
                }
                progress.isVisible = false
            }
        }
    }

    private fun goToMainScreen(){
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("LIFECYCLE", "Destroyed")
    }
}