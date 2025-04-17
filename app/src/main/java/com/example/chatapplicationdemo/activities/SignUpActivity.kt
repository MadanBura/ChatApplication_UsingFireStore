package com.example.chatapplicationdemo.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Base64
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplicationdemo.databinding.ActivitySignUpBinding
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import android.view.View
import com.example.chatapplicationdemo.MainActivity
import com.example.chatapplicationdemo.utility.Constants
import com.example.chatapplicationdemo.utility.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private var encodeImage: String = ""
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        setLoginLinkListener()
    }

    private fun setLoginLinkListener() {
        binding.loginLink.setOnClickListener {
            Intent(applicationContext, LoginActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.btnSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                showToast("Sign-up successful!")
                signUp()
            }
        }

        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }

    }


    private fun signUp() {
        loading(true)
            val fireStoreInstance = FirebaseFirestore.getInstance()

            val user: MutableMap<String, Any> = mutableMapOf()
        user[Constants.KEY_NAME] = binding.etName.text.toString().trim()
        user[Constants.KEY_EMAIL] = binding.etEmail.text.toString().trim()
        user[Constants.KEY_PASSWORD] = binding.etPassword.text.toString().trim()
        user[Constants.KEY_IMAGE] = encodeImage

        fireStoreInstance.collection(Constants.KEY_COLLECTION_USER)
                .add(user)
                .addOnSuccessListener { documentReference ->
//                    Toast.makeText(applicationContext, "Data Inserted", Toast.LENGTH_SHORT).show()

                        loading(false)
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.id)
                    preferenceManager.putString(Constants.KEY_NAME, binding.etName.text.toString())
                    preferenceManager.putString(Constants.KEY_IMAGE, encodeImage)

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or  Intent.FLAG_ACTIVITY_CLEAR_TASK )
                    startActivity(intent)

                }
                .addOnFailureListener { e ->
//                    Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
                    loading(false)
                    showToast(e.message.toString())
                }
    }

    fun encodeImage(bitmap: Bitmap): String {

        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    private val pickImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                val imageUri: Uri? = result.data!!.data
                try {
                    if (imageUri != null) {
                        val inputStream = contentResolver.openInputStream(imageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imgProfilePic.setImageBitmap(bitmap)
                        binding.tvAddImage.visibility = View.GONE
                        encodeImage = encodeImage(bitmap)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

    }


    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignUpDetails(): Boolean {
        // Check if the profile image is selected (this could be based on the ImageView's drawable or a flag)
        if (binding.imgProfilePic.drawable == null) {
            showToast("Please add profile pic")
            return false
        } else if (binding.etName.text.toString().trim().isEmpty()) {
            showToast("Enter a name")
            return false
        } else if (binding.etEmail.text.toString().trim().isEmpty()) {
            showToast("Enter an email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()) {
            showToast("Enter a valid email ID")
            return false
        } else if (binding.etPassword.text.toString().trim().isEmpty()) {
            showToast("Enter a password")
            return false
        } else if (binding.etConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Confirm your password")
            return false
        } else if (binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString()) {
            showToast("Passwords do not match")
            return false
        }

        // If all checks pass, return true
        return true
    }

    private fun loading(isloading: Boolean) {
        if (isloading) {
            binding.btnSignUp.visibility = View.INVISIBLE
            binding.progressBar1.visibility = View.VISIBLE
        } else {
            binding.progressBar1.visibility = View.INVISIBLE
            binding.btnSignUp.visibility = View.VISIBLE

        }
    }
}
