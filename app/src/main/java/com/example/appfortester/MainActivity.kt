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
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.appfortester.broadcasts.DownloadCompleteReceiver
import com.example.appfortester.broadcasts.FirebaseReceiver
import com.example.appfortester.databinding.ActivityMainBinding
import com.example.appfortester.notification.MyFirebaseMessagingService
import com.example.appfortester.utils.Constants.PERMISSION_REQUEST_STORAGE
import com.example.appfortester.utils.Extensions.checkSelfPermissionCompat
import com.example.appfortester.utils.Extensions.requestPermissionsCompat
import com.example.appfortester.utils.Extensions.shouldShowRequestPermissionRationaleCompat
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

    private val unknownSourceResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            startActivity(intent)
        }
    }
    private var isDownloaded = false

    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter{
        return MainPresenter(downloader = downloader, context = this)
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        installUnknownSourcePermission()
        createFirebaseToken()
        registerReceivers()

        binding.btnInstallIntent.setOnClickListener {
            checkStoragePermission()
        }

        binding.btnInstallPackInstaller.setOnClickListener {

        }
    }

    private fun registerReceivers(){
        downloadCompleteReceiver = DownloadCompleteReceiver()
        firebaseReceiver = FirebaseReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(MyFirebaseMessagingService.INTENT_FILTER)
        registerReceiver(firebaseReceiver, intentFilter)
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

    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            binding.root.showSnackbar(
                R.string.storage_access_required,
                Snackbar.LENGTH_INDEFINITE,
                R.string.ok) {
                requestPermissionsCompat(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_STORAGE)
            }
        } else {
            requestPermissionsCompat(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PERMISSION_REQUEST_STORAGE){
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mainPresenter.downloadFile()
            }
        } else {
            binding.root.showSnackbar(R.string.storage_permission_denied, Snackbar.LENGTH_SHORT)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun installUnknownSourcePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !this.packageManager.canRequestPackageInstalls()){
            val intent = Intent().setAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse(String.format("package:%s", this.packageName)))
            unknownSourceResult.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkStoragePermission(){
        if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            mainPresenter.downloadFile()
        } else {
            requestStoragePermission()
        }
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