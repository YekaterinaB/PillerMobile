package com.example.piller.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.api.ServiceBuilder
import com.example.piller.api.UserAPI
import com.example.piller.models.User
import com.example.piller.utilities.DbConstants
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var compositeDisposable = CompositeDisposable()
    private lateinit var forgotPassword: TextView

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

        initiateViews()
        initiateListeners()
    }

    private fun initiateViews() {
        forgotPassword = findViewById(R.id.login_reset_password)
    }

    private fun initiateListeners() {
        forgotPassword.setOnClickListener {
            val lp = LinearLayout.LayoutParams(700, 200)
            val layout = LinearLayout(this@MainActivity)
            layout.orientation = LinearLayout.HORIZONTAL
            val emailLabel = TextView(this)
            emailLabel.text = "Enter your email:"
            emailLabel.setPadding(20, 0, 0, 0)
            val emailInput = EditText(this)
            emailInput.layoutParams = lp
            emailInput.maxLines = 1
            layout.addView(emailLabel)
            layout.addView(emailInput)
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            alertDialog.setTitle("Reset Password")
            alertDialog.setView(layout)

            alertDialog.setPositiveButton("Send") { _, _ ->
                val email = emailInput.text.toString()
                if (email.isNotEmpty()) {
                    sendEmailToResetPassword(email)
                }
            }

            alertDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            alertDialog.show()
        }
    }

    private fun sendEmailToResetPassword(email: String) {
        val retrofit = ServiceBuilder.buildService(UserAPI::class.java)
        retrofit.resetPassword(email).enqueue(
            object : retrofit2.Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    SnackBar.showToastBar(
                        this@MainActivity,
                        "Could not reset password."
                    )
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        SnackBar.showToastBar(
                            this@MainActivity,
                            jObjError["message"] as String
                        )
                    } else {
                        SnackBar.showToastBar(
                            this@MainActivity,
                            "Reset email sent!"
                        )
                    }
                }
            }
        )
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
                        SnackBar.showToastBar(
                            this@MainActivity,
                            "Email cannot be null or empty"
                        )
                        return@SingleButtonCallback
                    }
                    TextUtils.isEmpty(edtName.text.toString()) -> {
                        SnackBar.showToastBar(
                            this@MainActivity,
                            "Name cannot be null or empty"
                        )
                        return@SingleButtonCallback
                    }
                    TextUtils.isEmpty(edtPassword.text.toString()) -> {
                        SnackBar.showToastBar(
                            this@MainActivity,
                            "Password cannot be null or empty"
                        )
                        return@SingleButtonCallback
                    }
                }
                registerUser(
                    edtEmail.text.toString(),
                    edtName.text.toString(),
                    edtPassword.text.toString()
                )

            })
            .build()
            .show()
    }


    private fun loginUserWindow(email: String, password: String) {
        //  Check if empty
        when {
            TextUtils.isEmpty(email) -> {
                SnackBar.showToastBar(

                    this@MainActivity,
                    "Email cannot be null or empty"
                )
            }
            TextUtils.isEmpty(password) -> {
                SnackBar.showToastBar(
                    this@MainActivity,
                    "Password cannot be null or empty"
                )
            }
            else -> {
                //  remember email and password if the user wants to
                if (login_remember.isChecked) {
                    updateAppPreferences(true, email, password)
                } else {
                    updateAppPreferences(false, "", "")
                }
                loginUser(email, password)

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
                    SnackBar.showToastBar(
                        this@MainActivity,
                        "Could not connect to server."
                    )
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        SnackBar.showToastBar(
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
                    SnackBar.showToastBar(this@MainActivity, "Could not connect to server.")
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.raw().code() != 200) {
                        SnackBar.showToastBar(
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