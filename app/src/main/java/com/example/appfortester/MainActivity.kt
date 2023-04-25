package com.example.appfortester

import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.appfortester.broadcasts.DownloadCompleteReceiver
import com.example.appfortester.broadcasts.PackageInstallReceiver
import com.example.appfortester.databinding.ActivityMainBinding
import com.example.appfortester.installers.PackageInstallerVersion
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class MainActivity : MvpAppCompatActivity(), MainView {

    @InjectPresenter
    lateinit var mainPresenter: MainPresenter
    private val packageInstaller = PackageInstallerVersion(this)
    private val libreDownloader = LibreDownloader(this)
    private lateinit var downloadCompleteReceiver: DownloadCompleteReceiver
    private lateinit var packageInstallerReceiver: PackageInstallReceiver

    //    private lateinit var firebaseReceiver: FirebaseReceiver

    private val unknownSourceResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            startActivity(intent)
        }
    }

    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter {
        return MainPresenter(
            context = this,
            libreDownloader = libreDownloader,
            packageInstaller = packageInstaller
        )
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        installUnknownSourcePermission()
//        createFirebaseToken()
        registerReceivers()

        binding.btnInstallPackInstaller.setOnClickListener {
            mainPresenter.downloadFile()
        }
    }

    private fun registerReceivers() {
//        firebaseReceiver = FirebaseReceiver()
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(MyFirebaseMessagingService.INTENT_FILTER)
//        registerReceiver(firebaseReceiver, intentFilter)
        packageInstallerReceiver = PackageInstallReceiver()
        registerReceiver(packageInstallerReceiver, IntentFilter(PackageInstaller.EXTRA_STATUS))
        downloadCompleteReceiver = DownloadCompleteReceiver()
        registerReceiver(
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
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

    private fun installUnknownSourcePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !this.packageManager.canRequestPackageInstalls()) {
            val intent = Intent().setAction(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse(String.format("package:%s", this.packageName)))
            unknownSourceResult.launch(intent)
        }
    }

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