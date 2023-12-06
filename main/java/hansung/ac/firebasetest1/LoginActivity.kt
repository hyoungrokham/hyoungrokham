package hansung.ac.firebasetest1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val createAccountButton = findViewById<Button>(R.id.button_create_account)

        createAccountButton.setOnClickListener {
            val newEmail = findViewById<EditText>(R.id.username)?.text.toString()
            val newpassword = findViewById<EditText>(R.id.password)?.text.toString()
            createAccount(newEmail, newpassword)
        }

        findViewById<Button>(R.id.login)?.setOnClickListener {
            val userEmail = findViewById<EditText>(R.id.username)?.text.toString()
            val password = findViewById<EditText>(R.id.password)?.text.toString()
            doLogin(userEmail, password)
        }
    }

    private fun createAccount(email: String, password: String) {
        val auth = Firebase.auth

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    val user = auth.currentUser

                    startActivity(Intent(this, FirestoreActivity::class.java))
                    finish()
                } else {
                    val exception = task.exception
                }
            }
    }

    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    startActivity(
                        Intent(this, FirestoreActivity::class.java))
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", it.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}