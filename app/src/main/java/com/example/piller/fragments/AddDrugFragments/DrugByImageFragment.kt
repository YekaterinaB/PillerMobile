package com.example.piller.fragments.AddDrugFragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.piller.BuildConfig
import com.example.piller.R
import com.example.piller.utilities.DbConstants
import com.example.piller.viewModels.DrugSearchViewModel
import com.squareup.picasso.Picasso
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DrugByImageFragment(private val addType: String) : Fragment() {
    private lateinit var _boxImage: ImageView
    private lateinit var _openCameraButton: ImageView
    private val _searchViewModel: DrugSearchViewModel by activityViewModels()
    private lateinit var _imageFilePath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_drug_by_image, container, false)

        initViews(fragmentView)
        initListeners()
        return fragmentView
    }

    private fun initListeners() {
        _openCameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCameraIntent()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    DbConstants.PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun initViews(fragmentView: View) {
        _boxImage = fragmentView.findViewById(R.id.ocr_image)
        _openCameraButton = fragmentView.findViewById(R.id.ocr_open_camera)
    }

    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat(
                getString(R.string.imageDateFormat),
                Locale.getDefault()
            ).format(Date())
        val imageFileName =
            getString(R.string.imageNamePrefix) + timeStamp + getString(R.string.imageNameSuffix)
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image: File = File.createTempFile(
            imageFileName,  /* prefix */
            getString(R.string.imageNameExtension),  /* suffix */
            storageDir /* directory */
        )
        _imageFilePath = image.absolutePath
        //  delete this file after closing app because we don't need it anymore
        image.deleteOnExit()
        return image
    }


    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (context?.let { pictureIntent.resolveActivity(it.packageManager) } != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: Exception) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI: Uri =
                    FileProvider.getUriForFile(
                        requireContext(),
                        BuildConfig.APPLICATION_ID + getString(R.string.dotProvider),
                        photoFile
                    )
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(pictureIntent, DbConstants.REQUEST_CAPTURE_IMAGE)
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == DbConstants.REQUEST_CAPTURE_IMAGE) {
            searchDrug()
            //  don't compare the data to null, it will always come as  null because we are providing
            //  a file URI, so load with the imageFilePath we obtained before opening the cameraIntent
            updateImage()
        }
    }

    private fun searchDrug() {
        if (addType == DbConstants.DRUG_BY_BOX) {
            //  search by box (ocr)
            _searchViewModel.searchDrugByBox(_imageFilePath)
        } else {
            //  search by pill image
            _searchViewModel.searchDrugByPillImage(_imageFilePath)
        }
    }

    private fun updateImage() {
        val imageFile = File(_imageFilePath)
        if (imageFile.exists()) {
            Picasso.get().load(imageFile).into(_boxImage)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            DbConstants.PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  permission granted
                    openCameraIntent()
                }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(addType: String) = DrugByImageFragment(addType)
    }
}