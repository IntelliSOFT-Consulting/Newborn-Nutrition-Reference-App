package com.intellisoft.nndak.data
import com.intellisoft.nndak.api.AuthService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestManager {

    fun loginUser(userData: User, onResult: (User?) -> Unit){
        val retrofit = AuthService.ServiceBuilder.buildService(AuthService::class.java)
        retrofit.loginUser(userData).enqueue(
            object : Callback<User> {
                override fun onFailure(call: Call<User>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse( call: Call<User>, response: Response<User>) {
                    val addedUser = response.body()
                    onResult(addedUser)
                }
            }
        )
    }
}