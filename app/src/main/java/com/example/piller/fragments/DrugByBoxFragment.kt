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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.piller.BuildConfig
import com.example.piller.R
import com.example.piller.viewModels.DrugSearchViewModel
import com.squareup.picasso.Picasso
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DrugByBoxFragment : Fragment() {
    private lateinit var boxImage: ImageView
    private lateinit var noteTV: TextView
    private val searchViewModel: DrugSearchViewModel by activityViewModels()
    private lateinit var imageFilePath: String
//    private val ORIENTATIONS = SparseIntArray()

    private val MAX_DIMENSION = 640
    private val REQUEST_CAPTURE_IMAGE = 100
    private val PERMISSION_REQUEST_CODE = 102

//    init {
//        ORIENTATIONS.append(Surface.ROTATION_0, 0)
//        ORIENTATIONS.append(Surface.ROTATION_90, 90)
//        ORIENTATIONS.append(Surface.ROTATION_180, 180)
//        ORIENTATIONS.append(Surface.ROTATION_270, 270)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_drug_by_box, container, false)

        initViews(fragmentView)
        initListeners()
        return fragmentView
    }

    private fun initListeners() {
        boxImage.setOnClickListener {
            if (checkCameraPermission()) {
                openCameraIntent()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun initViews(fragmentView: View) {
        boxImage = fragmentView.findViewById(R.id.ocr_image)
        noteTV = fragmentView.findViewById(R.id.ocr_note)
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

//    /**
//     * Get the angle by which an image must be rotated given the device's current orientation.
//     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Throws(CameraAccessException::class)
//    private fun getRotationCompensation(
//        cameraId: String,
//        activity: Activity,
//        isFrontFacing: Boolean
//    ): Int {
//        // Get the device's current rotation relative to its "native" orientation.
//        // Then, from the ORIENTATIONS table, look up the angle the image must be
//        // rotated to compensate for the device's rotation.
//        val deviceRotation = activity.windowManager.defaultDisplay.rotation
//        var rotationCompensation = ORIENTATIONS.get(deviceRotation)
//
//        // Get the device's sensor orientation.
//        val cameraManager =
//            activity.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager
//        val sensorOrientation = cameraManager
//            .getCameraCharacteristics(cameraId)
//            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!
//
//        if (isFrontFacing) {
//            rotationCompensation = (sensorOrientation + rotationCompensation) % 360
//        } else { // back-facing
//            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360
//        }
//        return rotationCompensation
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAPTURE_IMAGE) {
            searchViewModel.searchDrugByBox(imageFilePath)
            //don't compare the data to null, it will always come as  null because we are providing a file URI, so load with the imageFilePath we obtained before opening the cameraIntent
            Picasso.get().load(imageFilePath).into(boxImage)
            noteTV.text = "Press again to change the picture."
        }
    }

//    @SuppressLint("Recycle")
//    private fun getPath(uri: Uri?): String {
//        var result: String? = null
//        val proj = arrayOf(MediaStore.Images.Media.DATA)
//        val cursor: Cursor =
//            requireActivity().contentResolver.query(uri!!, proj, null, null, null)!!
//        if (cursor.moveToFirst()) {
//            val columnIndex: Int = cursor.getColumnIndexOrThrow(proj[0])
//            result = cursor.getString(columnIndex)
//        }
//        cursor.close()
//        if (result == null) {
//            result = "Not found"
//        }
//        return result
//    }

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
            DrugByBoxFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}