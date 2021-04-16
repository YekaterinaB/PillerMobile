package com.example.piller.fragments

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
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.piller.BuildConfig
import com.example.piller.R
import com.example.piller.SnackBar
import com.example.piller.viewModels.DrugSearchViewModel
import com.squareup.picasso.Picasso
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DrugByImageFragment : Fragment() {
    private val searchViewModel: DrugSearchViewModel by activityViewModels()
    private lateinit var captureImageBtn: Button
    private lateinit var searchDrugImageBtn: Button
    private lateinit var drugIV: ImageView
    private var imageFilePath: String = ""
    private val REQUEST_CAPTURE_IMAGE = 100
    private val PERMISSION_REQUEST_CODE = 102

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
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile
                    )
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE)
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initListeners() {
        captureImageBtn.setOnClickListener {
            if (checkCameraPermission()) {
                openCameraIntent()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        searchDrugImageBtn.setOnClickListener {
            if (imageFilePath.isNotEmpty()) {
                searchViewModel.searchDrugByImage(imageFilePath)
            } else {
                SnackBar.showToastBar(requireContext(), "Please take an image first!")
            }
        }
    }

    private fun initViews(fragmentView: View) {
        captureImageBtn = fragmentView.findViewById(R.id.dbi_take_picture_btn)
        searchDrugImageBtn = fragmentView.findViewById(R.id.dbi_select_image)
        drugIV = fragmentView.findViewById(R.id.dbi_drug_image)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAPTURE_IMAGE) {
            //don't compare the data to null, it will always come as  null because we are providing a file URI, so load with the imageFilePath we obtained before opening the cameraIntent
            Picasso.get().load(imageFilePath).into(drugIV)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image: File = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        imageFilePath = image.absolutePath
        //  delete this file after closing app because we don't need it anymore
        image.deleteOnExit()
        return image
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //  permission granted
                openCameraIntent()
            } else {
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DrugByImageFragment().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }
}