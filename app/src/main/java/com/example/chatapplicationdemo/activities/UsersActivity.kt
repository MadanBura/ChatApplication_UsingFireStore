package com.example.chatapplicationdemo.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.chatapplicationdemo.R
import com.example.chatapplicationdemo.adapters.UserAdapter
import com.example.chatapplicationdemo.databinding.ActivityUsersBinding
import com.example.chatapplicationdemo.listeners.UserListeners
import com.example.chatapplicationdemo.models.User
import com.example.chatapplicationdemo.utility.Constants
import com.example.chatapplicationdemo.utility.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UsersActivity : BaseActivity(), UserListeners {

    private lateinit var binding : ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        setListeners()

    }

    override fun onUserClicked(user: User) {
        Intent(applicationContext, ChatActivity::class.java).also {
            it.putExtra(Constants.KEY_USER, user)
            startActivity(it)
            finish()
        }
    }

    private fun setListeners(){
        binding.imageBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        getUser()
    }


    private fun getUser(){
        loading(true)
        val fireStoreInstance = FirebaseFirestore.getInstance()

        fireStoreInstance.collection(Constants.KEY_COLLECTION_USER)
            //Executes the query and returns the results as a QuerySnapshot.
            .get().addOnCompleteListener { task ->
                loading(false)
                val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)

                if(task.isSuccessful && task.result !=null){
                    val userList = mutableListOf<User>()

                    //A QueryDocumentSnapshot contains data read from a document in your Cloud Firestore database as part of a query.
                    // The document is guaranteed to exist and its data can be extracted using the getData() or the various get()
                    // methods in DocumentSnapshot
                    for(queryDocumentSnapShot in task.result){
                        if(currentUserId.equals(queryDocumentSnapShot.id)){
                            continue
                        }
                        val user = User()
                        user.name = queryDocumentSnapShot.getString(Constants.KEY_NAME)
                        user.email = queryDocumentSnapShot.getString(Constants.KEY_EMAIL)
                        user.image = queryDocumentSnapShot.getString(Constants.KEY_IMAGE)
                        user.token = queryDocumentSnapShot.getString(Constants.KEY_FCM_TOKEN)
                        user.id = queryDocumentSnapShot.id
                        userList.add(user)
                    }
                    if(userList.size>0){
                        val userAdapter = UserAdapter(userList, this)
                        binding.userRecyclerView.adapter = userAdapter
                        binding.userRecyclerView.visibility = View.VISIBLE
                        userAdapter.notifyDataSetChanged()
                    }else{
                        showErrorMessage()
                    }
                }else{
                    showErrorMessage()
                }

            }
    }


    private fun showErrorMessage(){
        binding.textErrorMessage.text = String.format("%s", "No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isloading: Boolean) {
        if (isloading) {
            binding.ProgressBarA.visibility = View.VISIBLE
        } else {
            binding.ProgressBarA.visibility = View.INVISIBLE

        }
    }
}