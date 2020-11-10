package com.example.gossipwars

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val usernameInput = findViewById<TextInputLayout>(R.id.outlinedTextField);
        val usernameText = findViewById<TextView>(R.id.usernameText);
        val username = sharedPref.getString("username", "")
        if (username.orEmpty().isNotEmpty()) {
            usernameText.text = getString(R.string.username_message) + username;
        } else {
            usernameText.text = "Please enter your username"
        }
        usernameInput.placeholderText = username;

        val lengthSpinner : Spinner = findViewById(R.id.gameLengthSpinner)
        ArrayAdapter.createFromResource(this, R.array.game_length_array,
            android.R.layout.simple_spinner_item).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lengthSpinner.adapter = adapter
        }
        lengthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                Log.d("DBG", parent.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // another interface callback
            }
        }


        val button = findViewById<Button>(R.id.username_button)
        button.setOnClickListener {
            with (sharedPref.edit()) {
                putString("username", usernameInput.editText?.text.toString())
                apply()
            }
            usernameText.text = getString(R.string.username_message) + usernameInput.editText?.text.toString()
        }
//            val intent = Intent(this, InGameActivity::class.java)
//            startActivity(intent)
    }

}