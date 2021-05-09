package com.example.piller.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.piller.DrugMap
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.fragments.DrugBottomDialogFragment
import com.example.piller.models.CalendarEvent
import com.example.piller.models.DrugObject
import com.example.piller.utilities.DateUtils
import com.example.piller.utilities.DbConstants
import com.example.piller.utilities.ImageUtils
import com.example.piller.viewModels.DrugInfoViewModel
import com.example.piller.viewModels.ProfileViewModel
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


class DrugInfoActivity : ActivityWithUserObject() {
    private lateinit var _viewModel: DrugInfoViewModel
    private lateinit var _profileViewModel: ProfileViewModel

    private lateinit var _drugNameTV: TextView
    private lateinit var _drugDosageTV: TextView
    private lateinit var _drugRefillsTV: TextView
    private lateinit var _drugRefillsIV: ImageView
    private lateinit var _drugIntakeTimeTV: TextView
    private lateinit var _drugTakenCB: CheckBox
    private lateinit var _drugTakenTV: TextView
    private lateinit var _drugTakenGreenTV: TextView
    private lateinit var _deleteBtn: ImageView
    private lateinit var _editBtn: ImageView
    private lateinit var _drugImageIV: ImageView
    private lateinit var _backButton: ImageView
    private lateinit var _backTV: TextView
    private lateinit var _takenCompleteAnimation: ImageView

    private lateinit var _calendarEvent: CalendarEvent
    private var _isFromNotification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Dialog)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drug_info_layout)
        setFinishOnTouchOutside(false)
        setWindowDimensions()
        initIntent()
        initViewModels()
        initViews()
        initObservers()
        initListeners()
    }

    private fun setWindowDimensions() {
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, DbConstants.windowHeight)
    }

    private fun initListeners() {
        _drugTakenCB.setOnClickListener {
            //  todo change the ms and seconds of _calendarEvent.intake_time.time to 0
            val drugObject =
                DrugMap.instance.getDrugObject(_calendarEvent.calendarId, _calendarEvent.drugId)
            _viewModel.updateDrugIntake(
                _drugTakenCB.isChecked, drugObject.taken_id, drugObject.refill.refillId,
                _calendarEvent.intakeTime.time
            )
            updateTakenViews()
            if (_drugTakenCB.isChecked) {
                animateV()
            }
        }

        _backButton.setOnClickListener { onBackPressed() }
        _backTV.setOnClickListener { onBackPressed() }

        _deleteBtn.setOnClickListener { showDeletePopup() }

        _editBtn.setOnClickListener { showEditPopup() }
    }

    private fun updateTakenViews() {
        if (_drugTakenCB.isChecked) {
            _drugTakenGreenTV.visibility = View.VISIBLE
            _drugTakenTV.text = getString(R.string.di_un_take)
        } else {
            _drugTakenGreenTV.visibility = View.INVISIBLE
            _drugTakenTV.text = getString(R.string.di_take)
        }
    }

    private fun initIntent() {
        initUserObject(intent)
        _isFromNotification = intent.extras!!.getBoolean(DbConstants.FROM_NOTIFICATION, false)

        try {
            _calendarEvent = intent.extras!!.getBundle(DbConstants.CALENDAR_EVENT_BUNDLE)
                ?.getParcelable(DbConstants.CALENDAR_EVENT)!!
        } catch (e: Exception) {
            _calendarEvent = intent.getParcelableExtra(DbConstants.CALENDAR_EVENT)!!
        }
    }

    private fun initObservers() {
        _viewModel.intakeUpdateSuccess.observe(
            this,
            Observer { success ->
                if (success) {
                    //  todo - show/hide loading screen
                    _viewModel.intakeUpdateSuccess.value = false
                }
            }
        )

        _viewModel.mutableToastError.observe(
            this,
            Observer { toastMessage ->
                toastMessage?.let { SnackBar.showToastBar(this, toastMessage) }
            })

        _viewModel.deleteSuccess.observe(
            this,
            Observer { success ->
                if (success) {
                    val drugObject = DrugMap.instance.getDrugObject(
                        _calendarEvent.calendarId,
                        _calendarEvent.drugId
                    )
                    //  remove drug image from cache
                    ImageUtils.deleteFile(drugObject.rxcui.toString(), this)
                    _viewModel.deleteSuccess.value = false
                    val returnIntent = Intent()
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            })

        _viewModel.deleteFutureSuccess.observe(
            this,
            Observer { success ->
                if (success) {
                    _viewModel.deleteFutureSuccess.value = false
                    val returnIntent = Intent()
                    returnIntent.putExtra(DbConstants.TAKEN_NEW_VALUE, _drugTakenCB.isChecked)
                    setResult(DbConstants.REMOVE_DRUG_FUTURE, returnIntent)
                    finish()
                }
            })

        _viewModel.drugImageSource.observe(
            this,
            Observer { image ->
                if (image != null) {
                    Picasso.get().load(image).into(_drugImageIV)
                }
            })

        _viewModel.pillsLeftMutable.observe(
            this,
            Observer { pillsLeft ->
                updateMedsLeft(pillsLeft)
                //  update in the map how many pills left the user has
                val drugObject =
                    DrugMap.instance.getDrugObject(_calendarEvent.calendarId, _calendarEvent.drugId)
                drugObject.refill.pillsLeft = pillsLeft
                DrugMap.instance.setDrugObject(_calendarEvent.calendarId, drugObject)
            }
        )
    }

    private fun initViews() {
        _drugNameTV = findViewById(R.id.di_drug_name)
        _drugDosageTV = findViewById(R.id.di_drug_dosage)
        _drugRefillsTV = findViewById(R.id.di_drug_refills_left)
        _drugRefillsIV = findViewById(R.id.di_drug_refills_left_icon)
        _drugIntakeTimeTV = findViewById(R.id.di_drug_intake_time)
        _drugTakenCB = findViewById(R.id.di_drug_taken)
        _drugTakenTV = findViewById(R.id.di_drug_taken_text)
        _drugTakenGreenTV = findViewById(R.id.di_drug_taken_green)
        _drugImageIV = findViewById(R.id.di_drug_image)
        _backButton = findViewById(R.id.di_toolbar_back_button)
        _backTV = findViewById(R.id.di_toolbar_title)
        _deleteBtn = findViewById(R.id.di_delete_btn)
        _editBtn = findViewById(R.id.di_edit_btn)
        _takenCompleteAnimation = findViewById(R.id.di_taken_image_animation)

        val drugObject =
            DrugMap.instance.getDrugObject(_calendarEvent.calendarId, _calendarEvent.drugId)
        if (!drugObject.refill.isToNotify) {
            _drugRefillsTV.visibility = View.GONE
            _drugRefillsIV.visibility = View.GONE
        }

        initViewsData()
    }

    private fun initViewsData() {
        val drugObject =
            DrugMap.instance.getDrugObject(_calendarEvent.calendarId, _calendarEvent.drugId)
        _drugNameTV.text = drugObject.drugName
        setIntakeTimeTextView()
        _drugTakenCB.isChecked = _calendarEvent.isTaken
        updateTakenViews()

        setDosageText(drugObject)

        updateMedsLeft(drugObject.refill.pillsLeft)
        if (_isFromNotification) {
            _backTV.visibility = View.GONE
            _backButton.setImageResource(R.drawable.ic_x_blue)
        }
    }

    private fun setDosageText(drugObject: DrugObject) {
        val dosage = "Total Dosage: ${drugObject.dose.totalDose} ${drugObject.dose.measurementType}"
        _drugDosageTV.text = dosage
    }

    private fun updateMedsLeft(currentMeds: Int) {
        val refills = "You have $currentMeds meds left"
        _drugRefillsTV.text = refills
    }

    private fun setIntakeTimeTextView() {
        val formatter = SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())
        val calTime: Date

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _calendarEvent.intakeTime.time
        calTime = calendar.time

        val sdf = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val intakeTime = "${sdf.format(_calendarEvent.intakeTime)}, ${formatter.format(calTime)}"
        _drugIntakeTimeTV.text = intakeTime
    }

    private fun initViewModels() {
        val drugObject =
            DrugMap.instance.getDrugObject(_calendarEvent.calendarId, _calendarEvent.drugId)

        _viewModel = ViewModelProvider(this).get(DrugInfoViewModel::class.java)
        _viewModel.initiateDrugImage(this, drugObject.rxcui.toString())
        _profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    private fun goToCalendarActivity() {
        val intent = Intent(this@DrugInfoActivity, MainActivity::class.java)
        putLoggedUserObjectInIntent(intent)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!_isFromNotification) {
            onBackPressed()
        } else {
            goToCalendarActivity()
        }
        return true
    }

    override fun onBackPressed() {
        if (!_isFromNotification) {
            setTakenStatusResult()
            super.onBackPressed()
        } else {
            finish()
//            goToCalendarActivity()
        }
    }

    private fun setTakenStatusResult() {
        val returnIntent = Intent()
        returnIntent.putExtra(DbConstants.TAKEN_NEW_VALUE, _drugTakenCB.isChecked)
        setResult(DbConstants.TAKEN_STATUS_UPDATE, returnIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.di_menu, menu)
        return true
    }

    private fun goToEditActivity(editOnlyFutureOccurrences: Boolean) {
        val drugObject =
            DrugMap.instance.getDrugObject(_calendarEvent.calendarId, _calendarEvent.drugId)
        val intent = Intent(this, DrugOccurrenceActivity::class.java)
        intent.putExtra(DbConstants.DRUG_OBJECT, drugObject)
        putLoggedUserObjectInIntent(intent)
        intent.putExtra(DbConstants.INTAKE_DATE, _calendarEvent.intakeTime.time)
        intent.putExtra(DbConstants.EDIT_ONLY_FUTURE_OCCURRENCES, editOnlyFutureOccurrences)

        startActivity(intent)
    }

    private fun showDeletePopup() {
        val drugObject =
            DrugMap.instance.getDrugObject(_calendarEvent.calendarId, _calendarEvent.drugId)
        DrugBottomDialogFragment(
            getString(R.string.deletePopupTitle),
            Pair(
                getString(R.string.deleteAllOccurrences),
                getString(R.string.deleteThisAndFutureOccurrences)
            ),
            clickOnFirstButtonListener = {
                _viewModel.deleteAllOccurrencesOfDrug(
                    loggedUserObject, drug = drugObject, context = this
                )
            },
            clickOnSecondButtonListener = { deleteFutureOccurrences(drugObject) }
        ).apply {
            show(supportFragmentManager, DrugBottomDialogFragment.TAG)
        }
    }

    private fun deleteFutureOccurrences(drugObject: DrugObject) {
        val today = Calendar.getInstance()
        today.timeInMillis = _calendarEvent.intakeTime.time
        DateUtils.zeroTime(today)
        _calendarEvent.intakeEndTime = today.time
        _viewModel.deleteFutureOccurrencesOfDrug(
            loggedUserObject,
            drug = drugObject,
            repeatEnd = today.timeInMillis.toString(),
            context = this
        )
    }

    private fun showEditPopup() {
        val drugObject =
            DrugMap.instance.getDrugObject(_calendarEvent.calendarId, _calendarEvent.drugId)
        DrugBottomDialogFragment(
            getString(R.string.editPopupTitle),
            Pair(
                getString(R.string.editAllOccurrences),
                getString(R.string.editThisAndFutureOccurrences)
            ),
            clickOnFirstButtonListener = { goToEditActivity(true) },
            clickOnSecondButtonListener = {
                deleteFutureOccurrences(drugObject)
                goToEditActivity(true)
            }
        ).apply {
            show(supportFragmentManager, DrugBottomDialogFragment.TAG)
        }
    }

    private fun animateV() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.taken_animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {
                _takenCompleteAnimation.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                _takenCompleteAnimation.visibility = View.INVISIBLE
            }
        })
        _takenCompleteAnimation.startAnimation(animation)
    }
}