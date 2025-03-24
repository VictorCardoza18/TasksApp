package com.example.tasks

data class User(
    val name: String,
    val email: String
) {
    companion object {
        const val USER_TYPE = "App User"
        fun createUserWithDefaultEmail(): User {
            return User("Default User", "default@example.com")
        }
    }
}

data class Task(
    val id: String?,
    val title: String,
    val description: String,
    val user: User.Companion,
    val date: String?
)