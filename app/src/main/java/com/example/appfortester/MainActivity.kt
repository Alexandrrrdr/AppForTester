package com.example.appfortester

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.appfortester.broadcasts.DownloadCompleteReceiver
import com.example.appfortester.broadcasts.FirebaseReceiver
import com.example.appfortester.databinding.ActivityMainBinding
import com.example.appfortester.notification.MyFirebaseMessagingService
import com.example.appfortester.utils.Constants.INTENT_INSTALLATION
import com.example.appfortester.utils.Constants.PACKAGE_INSTALLATION
import com.example.appfortester.utils.Constants.PERMISSION_REQUEST_STORAGE
import com.example.appfortester.utils.Extensions.showSnackbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class MainActivity : MvpAppCompatActivity(), MainView {

    @InjectPresenter
    lateinit var mainPresenter: MainPresenter
    private val downloader = Downloader(this)
    private lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    private lateinit var firebaseReceiver: FirebaseReceiver
    private var permissionsGranted = false

    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter{
        return MainPresenter(downloader = downloader, context = this)
    }

    private val unknownSourceResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            startActivity(intent)
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()){ grants ->
        permissionsGranted = grants.entries.all { it.value }
    }

    private var isDownloaded = false

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            installUnknownSourcePermission()
        }
        createFirebaseToken()
        registerReceivers()
        checkPermissions()

        binding.btnInstallIntent.setOnClickListener {
            if (permissionsGranted) {
                mainPresenter.downloadFile(INTENT_INSTALLATION)
            } else {
                checkPermissions()
            }
        }

        binding.btnInstallPackInstaller.setOnClickListener {
            if (permissionsGranted) {
                mainPresenter.downloadFile(PACKAGE_INSTALLATION)
            } else {
                checkPermissions()
            }
        }
    }

    private fun checkPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES
            ))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.REQUEST_INSTALL_PACKAGES,
                Manifest.permission.REQUEST_DELETE_PACKAGES
            ))
        } else {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    private fun registerReceivers(){
        firebaseReceiver = FirebaseReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(MyFirebaseMessagingService.INTENT_FILTER)
        registerReceiver(firebaseReceiver, intentFilter)
        downloadCompleteReceiver = DownloadCompleteReceiver()
        registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun createFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener{ task ->
            if (!task.isSuccessful){
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("info", "Token - $token")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun installUnknownSourcePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !this.packageManager.canRequestPackageInstalls()){
            val intent = Intent().setAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse(String.format("package:%s", this.packageName)))
            unknownSourceResult.launch(intent)
        }
    }
//
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PERMISSION_REQUEST_STORAGE){
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted, try download again", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.root.showSnackbar(R.string.storage_permission_denied, Snackbar.LENGTH_SHORT)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun installed() {
        Snackbar.make(binding.root, "Installation is finished successfully", Snackbar.LENGTH_SHORT).show()
        binding.btnInstallIntent.text = "File download and installed!"
        binding.btnInstallIntent.isEnabled = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun downloaded(isDownloaded: Boolean) {
        Snackbar.make(binding.root, "File downloaded, start installation", Snackbar.LENGTH_SHORT).show()
        binding.btnInstallIntent.isEnabled = true
        binding.btnInstallIntent.text = "Install via Intent"
        this.isDownloaded = isDownloaded
    }

    override fun onDestroy() {
        _binding = null
        unregisterReceiver(firebaseReceiver)
        unregisterReceiver(downloadCompleteReceiver)
        super.onDestroy()
    }
}