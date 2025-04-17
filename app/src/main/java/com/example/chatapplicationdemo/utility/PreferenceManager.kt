package com.example.chatapplicationdemo.utility

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context : Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

     private val editor: SharedPreferences.Editor = sharedPreferences.edit()


    fun putBoolean(key:String, value:Boolean){
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String) : Boolean{
        return sharedPreferences.getBoolean(key, false)
    }

    fun putString(key:String, value: String){
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String) : String? {
        return sharedPreferences.getString(key, null)
    }


//    fun saveUser(name: String, email: String, image: String) {
//        editor.putString(Constants.KEY_NAME, name)
//        editor.putString(Constants.KEY_EMAIL, email)
//        editor.putString(Constants.KEY_IMAGE, image)
//        editor.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
//        editor.apply()
//    }

    fun getUserName(): String? {
        return sharedPreferences.getString(Constants.KEY_NAME, null)
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(Constants.KEY_EMAIL, null)
    }

    fun getUserImage(): String? {
        return sharedPreferences.getString(Constants.KEY_IMAGE, null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.KEY_IS_SIGNED_IN, false)
    }

    fun clearUser() {
        editor.clear()
        editor.apply()
    }

}