package com.example.chatapplicationdemo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.chatapplicationdemo.R
import com.example.chatapplicationdemo.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import com.example.chatapplicationdemo.MainActivity
import com.example.chatapplicationdemo.utility.Constants
import com.example.chatapplicationdemo.utility.PreferenceManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)


        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent(applicationContext, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        setUpSignUpListener()
    }

    private fun setUpSignUpListener() {
        binding.createNewAccount.setOnClickListener {
            Intent(applicationContext, SignUpActivity::class.java).also { startActivity(it) }
        }

        binding.btnSignIn.setOnClickListener {
            if (isValidSignInDetails()) {
                signIn()
            }
        }


    }


    private fun signIn() {
        loading(true)

        val fireBaseDb = FirebaseFirestore.getInstance()
        fireBaseDb.collection(Constants.KEY_COLLECTION_USER)
            .whereEqualTo(Constants.KEY_EMAIL, binding.email.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.userPass.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
                    val documentSnapShot = task.result.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapShot.id)
                    documentSnapShot.getString(Constants.KEY_NAME)
                        ?.let { preferenceManager.putString(Constants.KEY_NAME, it) }

                    documentSnapShot.getString(Constants.KEY_IMAGE)
                        ?.let { preferenceManager.putString(Constants.KEY_IMAGE, it) }

                    Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                } else {
                    loading(false)
                    showToast("Please check with your credentials")
                }
            }
    }



    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }


    private fun isValidSignInDetails(): Boolean {

        if (binding.email.text.toString().trim().isEmpty()) {
            showToast("Enter an email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
            showToast("Enter a valid email ID")
            return false
        } else if (binding.userPass.text.toString().trim().isEmpty()) {
            showToast("Enter a password")
            return false
        }
        // If all checks pass, return true
        return true
    }


//    private fun addDataToFireStore() {
//        // Get the Firestore instance
//        val fireStoreInstance = FirebaseFirestore.getInstance()
//
//        // Create a map to store student data
//        val studentHashMap = mutableMapOf<String, Any>(
//            "first_name" to "Tony",
//            "last_name" to "Stark"
//        )
//
//        // Add the data to the "users" collection
//        fireStoreInstance.collection("users")
//            .add(studentHashMap)
//            .addOnSuccessListener { documentReference ->
//                // Handle success
////                Log.d("Firestore", "Document added with ID: ${documentReference.id}")
//                Toast.makeText(applicationContext, "Data Inserted", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                // Handle failure
//                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
//
//            }
//    }

    private fun loading(isloading: Boolean) {
        if (isloading) {
            binding.btnSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.btnSignIn.visibility = View.VISIBLE

        }
    }


}