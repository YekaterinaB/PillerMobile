package com.example.piller.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.User
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import com.example.piller.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var compositeDisposable = CompositeDisposable()


    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppPreferences.init(this)
        ServiceBuilder.updateRetrofit("http://10.0.2.2:3000")



        setOnClickListeners()
        //  update fields if user chose to remember email and password
        if (AppPreferences.isLogin) {
            login_remember.isChecked = true
            edt_email.setText(AppPreferences.email)
            edt_password.setText(AppPreferences.password)
        }
    }

    private fun setOnClickListeners() {
        btn_login.setOnClickListener {
            loginUserWindow(edt_email.text.toString(), edt_password.text.toString())
        }

        txt_create_account.setOnClickListener {
            showRegistrationWindow()
        }
    }

    private fun showRegistrationWindow() {
        val itemView = LayoutInflater.from(this@MainActivity)
            .inflate(R.layout.register_layout, null)

        MaterialStyledDialog.Builder(this@MainActivity)
            .setIcon(R.drawable.ic_user)
            .setTitle("REGISTRATION")
            .setDescription("Please fill all fields")
            .setCustomView(itemView)
            .setNegativeText("CANCEL")
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveText("REGISTER")
            .onPositive(MaterialDialog.SingleButtonCallback { _, _ ->
                val edtEmail = itemView.findViewById<View>(R.id.edt_email) as MaterialEditText
                val edtName = itemView.findViewById<View>(R.id.edt_name) as MaterialEditText
                val edtPassword =
                    itemView.findViewById<View>(R.id.edt_password) as MaterialEditText

                when {
                    TextUtils.isEmpty(edtEmail.text.toString()) -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Email cannot be null or empty",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@SingleButtonCallback
                    }
                    TextUtils.isEmpty(edtName.text.toString()) -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Name cannot be null or empty",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@SingleButtonCallback
                    }
                    TextUtils.isEmpty(edtPassword.text.toString()) -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Password cannot be null or empty",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@SingleButtonCallback
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    registerUser(
                        edtEmail.text.toString(),
                        edtName.text.toString(),
                        edtPassword.text.toString()
                    )
                }
            })
            .build()
            .show()
    }


    private fun loginUserWindow(email: String, password: String) {
        //  Check if empty
        when {
            TextUtils.isEmpty(email) -> {
                Toast.makeText(
                    this@MainActivity,
                    "Email cannot be null or empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
            TextUtils.isEmpty(password) -> {
                Toast.makeText(
                    this@MainActivity,
                    "Password cannot be null or empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                //  remember email and password if the user wants to
                if (login_remember.isChecked) {
                    updateAppPreferences(true, email, password)
                } else {
                    updateAppPreferences(false, "", "")
                }
                CoroutineScope(Dispatchers.IO).launch {
                    loginUser(email, password)
                }
            }
        }
    }

    private fun updateAppPreferences(stayLogged: Boolean, email: String, password: String) {
        AppPreferences.isLogin = stayLogged
        AppPreferences.email = email
        AppPreferences.password = password
    }


    private fun registerUser(email: String, name: String, password: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        val user = User(email = email, name = name, password = password)
        retrofit.registerUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    SnackBar.showSnackBar(
                        this@MainActivity,
                        "Could not connect to server."
                    )
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        SnackBar.showSnackBar(
                            this@MainActivity,
                            "A user with this email already exists."
                        )

                    }
                }
            }
        )
    }

    private fun loginUser(email: String, password: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        val user = User(email = email, name = "", password = password)
        retrofit.loginUser(user).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    SnackBar.showSnackBar(this@MainActivity, "Could not connect to server.")
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        SnackBar.showSnackBar(
                            this@MainActivity,
                            "User does not exist, check your login information."
                        )
                    } else {

                        val jObject = JSONObject(response.body()!!.string())
                        //go to the next activity
                        val intent = Intent(
                            this@MainActivity,
                            CalendarActivity::class.java
                        )
                        intent.putExtra(
                            DbConstants.LOGGED_USER_EMAIL,
                            jObject.get("email").toString()
                        )
                        intent.putExtra(
                            DbConstants.LOGGED_USER_NAME,
                            jObject.get("name").toString()
                        )
                        startActivity(intent)
                    }
                }
            }
        )

    }
}