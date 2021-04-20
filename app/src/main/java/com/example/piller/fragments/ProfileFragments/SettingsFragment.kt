package com.example.piller.fragments.ProfileFragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.accountManagement.AppPreferences
import com.example.piller.activities.LoginActivity
import com.example.piller.fragments.FragmentWithUserObject
import com.example.piller.models.UserObject
import com.example.piller.viewModels.ManageAccountViewModel
import kotlinx.android.synthetic.main.settings_main_layout.*

class SettingsFragment : FragmentWithUserObject() {
    private lateinit var _viewModel: ManageAccountViewModel
    private lateinit var _emailEditText: EditText
    private lateinit var _passwordEditText: EditText
    private lateinit var _nameEditText: EditText
    private lateinit var _showNotificationSW: SwitchCompat
    private lateinit var _saveTextView: TextView
    private lateinit var _fragmentView: View
    private lateinit var _dimLayout: RelativeLayout
    private lateinit var _mainProfileEmailTitle: TextView
    private lateinit var _mainProfileNameTitle: TextView
    private lateinit var _backButton: ImageView
    private lateinit var _logoutTextView: TextView
    private lateinit var _deleteAccountTextView: TextView
    private lateinit var _helpTextView: TextView


    private val _mutableIsValidInfo: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentView = inflater.inflate(R.layout.settings_main_layout, container, false)
        initViewModels()
        initViews()
        setOnClickListeners()
        setViewModelsObservers()
        return _fragmentView
    }


    private fun initViewModels() {
        _viewModel = ViewModelProvider(this).get(ManageAccountViewModel::class.java)
        _viewModel._mutableEmail.value = _loggedUserObject.email
        _viewModel._mutableUsername.value = _loggedUserObject.mainProfile?.name
    }

    private fun setViewModelsObservers() {
        _mutableIsValidInfo.observe(viewLifecycleOwner, Observer { valid ->
            if (valid) {
                _saveTextView.setTextColor(getResources().getColor(R.color.colorPrimary))
            } else {
                _saveTextView.setTextColor(getResources().getColor(R.color.notComplete))
            }
        })

        _viewModel._snackBarMessage.observe(viewLifecycleOwner, Observer { message ->
            run {
                if (message.isNotEmpty()) {
                    SnackBar.showToastBar(context, message)
                }
            }
        })

        _viewModel._mutableUsername.observe(viewLifecycleOwner, Observer { name ->
            _loggedUserObject.mainProfile?.name = name
            _mainProfileNameTitle.text = name
        })

        _viewModel._mutableEmail.observe(viewLifecycleOwner, Observer { email ->
            _loggedUserObject.email = email
            _mainProfileEmailTitle.text = email
        })

        _viewModel._isDeleteSucceeded.observe(viewLifecycleOwner, Observer { isDelete ->
            if (isDelete) {
                logOut()
            }
        })


    }

    private val fieldsWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            checkIsInfoValid()
        }
    }


    private fun setOnClickListeners() {
        _emailEditText.addTextChangedListener(fieldsWatcher)
        _nameEditText.addTextChangedListener(fieldsWatcher)
        _passwordEditText.addTextChangedListener(fieldsWatcher)

        _backButton.setOnClickListener {
            onPressBack()
        }

        _saveTextView.setOnClickListener {
            if (_mutableIsValidInfo.value!!) {
                confirmChangesPopup()
            }
        }

        _logoutTextView.setOnClickListener {
            logOut()
        }
        _deleteAccountTextView.setOnClickListener {
            confirmationDeleteUser()
        }

        _showNotificationSW.setOnClickListener {
            AppPreferences.showNotifications = _showNotificationSW.isChecked
        }

        _helpTextView.setOnClickListener {
            goToHelpFragment()
        }
    }

    private fun goToHelpFragment() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(
                R.id.calender_weekly_container_fragment,
                HelpFragment.newInstance(_loggedUserObject)
            )
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    private fun confirmationDeleteUser() {
        val customView: View =
            layoutInflater.inflate(R.layout.settings_confirm_delete_account_popup, null)
        val popup = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val cancelViewText = customView.findViewById<TextView>(R.id.cancel_delete_account)
        val confirmViewText = customView.findViewById<TextView>(R.id.confirm_delete_account)
        val passEditText = customView.findViewById<EditText>(R.id.password_delete_account)

        cancelViewText.setOnClickListener {
            popup.dismiss()
            changeDarkBackgroundVisibility(false)
        }

        confirmViewText.setOnClickListener {
            val oldPassword = passEditText.text.toString()
            if (oldPassword.isNotEmpty()) {
                _viewModel.deleteUser(_loggedUserObject, createHashtFromPassword(oldPassword))
                popup.dismiss()
                changeDarkBackgroundVisibility(false)
            } else {
                _viewModel._snackBarMessage.value = "Password value is empty"
            }
        }
        changeDarkBackgroundVisibility(true)
        popup.isFocusable = true
        popup.update()
        popup.showAtLocation(_fragmentView, Gravity.CENTER, 0, 0)
    }

    private fun createHashtFromPassword(newPassword: String): HashMap<String, String> {
        val map = hashMapOf<String, String>()
        map["password"] = newPassword
        return map
    }


    private fun logOut() {
        AppPreferences.stayLoggedIn = false
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun checkIsInfoValid() {
        val email = _emailEditText.text.toString()
        val newPass = _passwordEditText.text.toString()
        val fullname = _nameEditText.text.toString()
        _mutableIsValidInfo.value = ((_loggedUserObject.email != email && email.isNotEmpty())
                || (AppPreferences.password != newPass && newPass.isNotEmpty())
                || (fullname.isNotEmpty() && fullname != _loggedUserObject.mainProfile?.name))

    }

    private fun confirmChangesPopup() {
        val customView: View = layoutInflater.inflate(R.layout.settings_confirm_changes_popup, null)
        val popup = PopupWindow(
            customView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val cancelViewText = customView.findViewById<TextView>(R.id.cancel_confirm_changes)
        val confirmViewText = customView.findViewById<TextView>(R.id.confirm_confirm_changes)
        val passEditText = customView.findViewById<EditText>(R.id.password_edit_confirm_changes)

        cancelViewText.setOnClickListener {
            popup.dismiss()
            changeDarkBackgroundVisibility(false)
        }

        confirmViewText.setOnClickListener {
            val oldPassword = passEditText.text.toString()
            if (oldPassword.isNotEmpty()) {
                _viewModel.verifyPassword(
                    _loggedUserObject, oldPassword,
                    _emailEditText.text.toString(), _passwordEditText.text.toString(),
                    _nameEditText.text.toString()
                )
                popup.dismiss()
                changeDarkBackgroundVisibility(false)
            } else {
                _viewModel._snackBarMessage.value = "Password value is empty"
            }
        }
        changeDarkBackgroundVisibility(true)
        popup.isFocusable = true
        popup.update()
        popup.showAtLocation(_fragmentView, Gravity.CENTER, 0, 0)
    }

    private fun changeDarkBackgroundVisibility(isVisible: Boolean) {
        if (isVisible) {
            _dimLayout.visibility = View.VISIBLE

        } else {
            _dimLayout.visibility = View.GONE
        }
    }


    private fun initViews() {
        _emailEditText = _fragmentView.findViewById(R.id.edt_email_settings)
        _emailEditText.setText(_loggedUserObject.email)

        _nameEditText = _fragmentView.findViewById(R.id.edt_fullname_settings)
        _nameEditText.setText(_loggedUserObject.mainProfile?.name)

        _passwordEditText = _fragmentView.findViewById(R.id.edt_password_settings)
        _passwordEditText.setText(AppPreferences.password)

        _saveTextView = _fragmentView.findViewById(R.id.save_settings)
        _deleteAccountTextView = _fragmentView.findViewById(R.id.delete_account_settings)
        _logoutTextView = _fragmentView.findViewById(R.id.log_out_settings)

        _showNotificationSW = _fragmentView.findViewById(R.id.ma_show_notifications)
        _showNotificationSW.isChecked = AppPreferences.showNotifications

        _dimLayout = _fragmentView.findViewById(R.id.settings_dim_layout)

        _mainProfileEmailTitle = _fragmentView.findViewById(R.id.email_main_profile)
        _mainProfileNameTitle = _fragmentView.findViewById(R.id.profile_name_title_item)

        _backButton = _fragmentView.findViewById(R.id.go_back_from_settings)
        _helpTextView = _fragmentView.findViewById(R.id.help_select_item_in_settings)
    }

    private fun onPressBack() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(
                R.id.calender_weekly_container_fragment,
                ProfileFragment.newInstance(_loggedUserObject)
            )
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }


    companion object {
        fun newInstance(loggedUser: UserObject) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    _loggedUserObject = loggedUser
                }
            }
    }
}