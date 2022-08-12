package com.intellisoft.nndak.data

import android.content.Context
import android.util.Log
import com.intellisoft.nndak.FhirApplication
import com.intellisoft.nndak.MainActivity
import com.intellisoft.nndak.api.AuthInterceptor
import com.intellisoft.nndak.api.AuthService
import com.intellisoft.nndak.charts.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class RestManager {
    private lateinit var apiService: AuthService

    private fun getService(context: Context): AuthService {
        val base = FhirApplication.getServerURL(context)

        // Initialize ApiService if not initialized yet
        if (!::apiService.isInitialized) {
            val retrofit = base?.let {
                Retrofit.Builder()
                    .baseUrl(it)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okhttpClient(context))
                    .build()
            }

            if (retrofit != null) {
                apiService = retrofit.create(AuthService::class.java)
            }
        }

        return apiService
    }

    /**
     * Initialize OkhttpClient with our interceptor
     */
    private fun okhttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()
    }

    fun loginUser(context: Context, data: LoginData, onResult: (AuthResponse?) -> Unit) {
        getService(context).loginUser(data).enqueue(
            object : Callback<AuthResponse> {
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    onResult(null)
                }

                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    onResult(response.body())
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }

                }
            }
        )
    }

    fun loadUser(context: Context, onResult: (UserResponse?) -> Unit) {
        getService(context).loadUser().enqueue(
            object : Callback<UserResponse> {
                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    onResult(null)

                }

                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.code() == 401) {
                        (context as MainActivity).sessionTimeOut()
                    }
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun resetPassword(context: Context, data: LoginData, onResult: (AuthResponse?) -> Unit) {
        getService(context).resetPassword(data).enqueue(
            object : Callback<AuthResponse> {
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    onResult(null)

                }

                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun addDHMStock(context: Context, data: DHMStock, onResult: (AuthResponse?) -> Unit) {
        getService(context).addDHMStock(data).enqueue(
            object : Callback<AuthResponse> {
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    onResult(null)

                }

                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    Timber.e("onResponse $response")
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun dispenseStock(context: Context, data: DispenseData, onResult: (AuthResponse?) -> Unit) {
        getService(context).dispenseStock(data).enqueue(
            object : Callback<AuthResponse> {
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    onResult(null)

                }

                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun loadStatistics(context: Context, onResult: (Statistics?) -> Unit) {
        getService(context).loadStatistics().enqueue(
            object : Callback<Statistics> {
                override fun onFailure(call: Call<Statistics>, t: Throwable) {
                    onResult(null)

                }

                override fun onResponse(
                    call: Call<Statistics>,
                    response: Response<Statistics>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun loadOrders(context: Context, onResult: (OrderData?) -> Unit) {
        getService(context).loadOrders().enqueue(
            object : Callback<OrderData> {
                override fun onFailure(call: Call<OrderData>, t: Throwable) {
                    onResult(null)

                }

                override fun onResponse(
                    call: Call<OrderData>,
                    response: Response<OrderData>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun loadDonorMilk(context: Context, onResult: (DHMModel?) -> Unit) {
        getService(context).loadDonorMilk().enqueue(
            object : Callback<DHMModel> {
                override fun onFailure(call: Call<DHMModel>, t: Throwable) {
                    onResult(null)
                }

                override fun onResponse(
                    call: Call<DHMModel>,
                    response: Response<DHMModel>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun loadExpressedMilk(context: Context, ip: String, onResult: (MilkExpression?) -> Unit) {
        getService(context).loadExpressedMilk(ip).enqueue(
            object : Callback<MilkExpression> {
                override fun onFailure(call: Call<MilkExpression>, t: Throwable) {
                    onResult(null)

                }

                override fun onResponse(
                    call: Call<MilkExpression>,
                    response: Response<MilkExpression>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun loadFeedDistribution(context: Context, ip: String, onResult: (FeedsDistribution?) -> Unit) {
        getService(context).loadFeedDistribution(ip).enqueue(
            object : Callback<FeedsDistribution> {
                override fun onFailure(call: Call<FeedsDistribution>, t: Throwable) {
                    onResult(null)
                }

                override fun onResponse(
                    call: Call<FeedsDistribution>,
                    response: Response<FeedsDistribution>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }

    fun loadWeights(context: Context, ip: String, onResult: (WeightsData?) -> Unit) {

        getService(context).loadWeights(ip).enqueue(
            object : Callback<WeightsData> {
                override fun onFailure(call: Call<WeightsData>, t: Throwable) {
                    onResult(null)
                }

                override fun onResponse(
                    call: Call<WeightsData>,
                    response: Response<WeightsData>
                ) {
                    if (response.isSuccessful) {
                        onResult(response.body())
                    } else {
                        onResult(null)
                    }
                }
            }
        )
    }


}