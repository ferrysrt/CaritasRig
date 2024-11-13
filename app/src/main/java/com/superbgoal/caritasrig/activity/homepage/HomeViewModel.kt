package com.superbgoal.caritasrig.activity.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.superbgoal.caritasrig.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _isProfileDialogVisible = MutableStateFlow(false)
    val isProfileDialogVisible: StateFlow<Boolean> = _isProfileDialogVisible

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val databaseUrl = "https://caritas-rig-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val database = FirebaseDatabase.getInstance(databaseUrl).reference

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            database.child("users").child(userId).child("userData").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        _user.value = snapshot.getValue(User::class.java)
                    } else {
                        _errorMessage.value = "User not found"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _errorMessage.value = "Failed to load data: ${error.message}"
                }
            })
        }
    }

    fun updateSearchText(newText: String) {
        _searchText.value = newText
    }

    fun toggleProfileDialog() {
        _isProfileDialogVisible.value = !_isProfileDialogVisible.value
    }
}
