package com.example.piller

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.example.piller.Retrofit.IMyService
import com.example.piller.Retrofit.RetrofitClient
import com.example.piller.accountManagement.AppPreferences
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.rengwuxian.materialedittext.MaterialEditText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    lateinit var iMyService: IMyService
    private var compositeDisposable = CompositeDisposable()

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppPreferences.init(this)

        val retrofit: Retrofit = RetrofitClient.getInstance()
        iMyService = retrofit.create(IMyService::class.java)

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
            loginUser(edt_email.text.toString(), edt_password.text.toString())
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

                registerUser(
                    edtEmail.text.toString(),
                    edtName.text.toString(),
                    edtPassword.text.toString()
                )
            })
            .build()
            .show()
    }

    private fun registerUser(email: String, name: String, password: String) {
        compositeDisposable.add(
            iMyService.registerUser(email, name, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    Toast.makeText(this@MainActivity, "" + result, Toast.LENGTH_SHORT)
                        .show()
                }
        )
    }


    private fun loginUser(email: String, password: String) {
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

                compositeDisposable.add(
                    iMyService.loginUser(email, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { result ->
                            Toast.makeText(this@MainActivity, "" + result, Toast.LENGTH_SHORT)
                                .show()
                        }
                )
            }
        }
    }

    private fun updateAppPreferences(stayLogged: Boolean, email: String, password: String) {
        AppPreferences.isLogin = stayLogged
        AppPreferences.email = email
        AppPreferences.password = password
    }
}