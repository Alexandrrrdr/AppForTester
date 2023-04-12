package com.example.appfortester

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appfortester.broadcasts.DownloadCompleteReceiver
import com.example.appfortester.broadcasts.FirebaseReceiver
import com.example.appfortester.broadcasts.PackageInstallReceiver
import com.example.appfortester.databinding.ActivityMainBinding
import com.example.appfortester.installers.PackageInstallerVersion
import com.example.appfortester.notification.MyFirebaseMessagingService
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Constants.R_LESS_PERMISSIONS
import com.example.appfortester.utils.Constants.R_PERMISSIONS
import com.example.appfortester.utils.Constants.TIRAMISU_PERMISSIONS
import com.google.firebase.messaging.FirebaseMessaging
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class MainActivity : MvpAppCompatActivity(), MainView {

    @InjectPresenter
    lateinit var mainPresenter: MainPresenter
    private val downloader = Downloader(this)
    private val packageInstaller = PackageInstallerVersion(this)
    private lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    private lateinit var packageInstallerReceiver: PackageInstallReceiver
    private lateinit var pLauncher: ActivityResultLauncher<String>
//    private lateinit var firebaseReceiver: FirebaseReceiver
    private var permissionsGranted = false

    private val unknownSourceResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            startActivity(intent)
        }
    }
    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter{
        return MainPresenter(downloader = downloader, context = this, packageInstaller = packageInstaller)
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerPermissionListener()
        checkNecessaryPermissions()
        installUnknownSourcePermission()
//        createFirebaseToken()
        registerReceivers()

        binding.btnInstallPackInstaller.setOnClickListener {
            mainPresenter.downloadFile()
        }
    }

    private fun checkAgainAllPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_DELETE_PACKAGES) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_DELETE_PACKAGES) == PackageManager.PERMISSION_GRANTED)
        } else {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES) == PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun registerPermissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
            if (it) {
                Toast.makeText(this, "registerPermissionListener - Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "registerPermissionListener - Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkNecessaryPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES,
            )
            checkAndRequestPermission(permissions)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val permissions = arrayOf(
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Manifest.permission.REQUEST_DELETE_PACKAGES
            )
            checkAndRequestPermission(permissions)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Manifest.permission.REQUEST_DELETE_PACKAGES
            )
            checkAndRequestPermission(permissions)
        } else {
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES
            )
            checkAndRequestPermission(permissions)
        }
    }

    private fun checkAndRequestPermission(permissions: Array<String>){
        for (i in permissions.indices){
            when{
                ContextCompat.checkSelfPermission(this, permissions[i])
                        == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("info", "checkAndRequestPermission - ${permissions[i]} - Granted")
                }
                shouldShowRequestPermissionRationale(permissions[i]) -> {
                    askUserForOpeningsSettings()
                }
                else -> {
                    pLauncher.launch(permissions[i])
                }
            }
        }
    }

private fun registerReceivers(){
//        firebaseReceiver = FirebaseReceiver()
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(MyFirebaseMessagingService.INTENT_FILTER)
//        registerReceiver(firebaseReceiver, intentFilter)
    packageInstallerReceiver = PackageInstallReceiver()
    registerReceiver(packageInstallerReceiver, IntentFilter(PackageInstaller.EXTRA_STATUS))
    downloadCompleteReceiver = DownloadCompleteReceiver()
    registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}

//    private fun createFirebaseToken() {
//        FirebaseMessaging.getInstance().token.addOnCompleteListener{ task ->
//            if (!task.isSuccessful){
//                return@addOnCompleteListener
//            }
//            val token = task.result
//            Log.d("info", "Token - $token")
//        }
//    }

private fun installUnknownSourcePermission(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !this.packageManager.canRequestPackageInstalls()){
        val intent = Intent().setAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData(Uri.parse(String.format("package:%s", this.packageName)))
        unknownSourceResult.launch(intent)
    }
}



private fun askUserForOpeningsSettings(){
    val appSettingsIntent = Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts(Constants.PACKAGE, packageName, null))
//    unknownSourceResult.launch(intent)
//    val appSettingsIntent = Intent(
//        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//        Uri.fromParts(Constants.PACKAGE, packageName, null)
//    )
    if (packageManager.resolveActivity(appSettingsIntent, PackageManager.MATCH_DEFAULT_ONLY) == null){
        Toast.makeText(this, "Permission denied forever", Toast.LENGTH_SHORT).show()
    } else {
        AlertDialog.Builder(this)
            .setTitle("Permission denied")
            .setMessage("You have denied permissions forever." +
                    "You can change your decission in settings.\n\n" +
                    "Would you like to open an App settings?")
            .setPositiveButton("Open") {
                    _, _ -> startActivity(appSettingsIntent)
            }
            .create()
            .show()
    }
}

//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    private fun tiramisuPermissionChecker(): Boolean {
//        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
//    }

//    private fun checkPermissions(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//            requestMultiplePermissions.launch(arrayOf(
//                Manifest.permission.READ_MEDIA_AUDIO,
//                Manifest.permission.READ_MEDIA_VIDEO,
//                Manifest.permission.READ_MEDIA_IMAGES
//            ))
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            requestMultiplePermissions.launch(arrayOf(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.REQUEST_INSTALL_PACKAGES,
//                Manifest.permission.REQUEST_DELETE_PACKAGES
//            ))
//        } else {
//            requestMultiplePermissions.launch(arrayOf(
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ))
//        }
//    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if(requestCode == PERMISSION_REQUEST_STORAGE){
//            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(this, "Permissions granted, try download again", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            binding.root.showSnackbar(R.string.storage_permission_denied, Snackbar.LENGTH_SHORT)
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }

    override fun installing() {
    }

    override fun downloading() {
    }

    override fun onDestroy() {
        _binding = null
//        unregisterReceiver(firebaseReceiver)
        unregisterReceiver(downloadCompleteReceiver)
        unregisterReceiver(packageInstallerReceiver)
        super.onDestroy()
    }
}