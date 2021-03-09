package com.example.gossipwars.ui.messenger

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.example.gossipwars.InGameActivity
import com.example.gossipwars.R
import com.example.gossipwars.logic.entities.Alliance
import org.apache.commons.lang3.SerializationUtils

class MessengerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)
        setSupportActionBar(findViewById(R.id.toolbar))

        var alliance: Alliance = (intent.getSerializableExtra("alliance") as Alliance)
        Toast.makeText(this, alliance.name, Toast.LENGTH_LONG).show()

        supportActionBar?.title = alliance.name
        supportActionBar?.subtitle = alliance.playersInvolved.map { player -> player.username }
            .joinToString(",")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.messenger_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.actionQuitAlliance) {
            Toast.makeText(this, "Quit alliance", Toast.LENGTH_LONG).show()
            return true
        } else {
            onBackPressed()
        }
        return true
    }
}