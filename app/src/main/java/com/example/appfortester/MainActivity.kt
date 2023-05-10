package com.example.appfortester

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.appfortester.databinding.ActivityMainBinding
import com.example.appfortester.installers.PackageInstallerUninstall
import com.example.appfortester.installers.PackageInstallerVersion
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class MainActivity : MvpAppCompatActivity(), MainView {

    @InjectPresenter
    lateinit var mainPresenter: MainPresenter
    private val libreDownloader = LibreDownloader(this)
    private val packageInstallerUninstall = PackageInstallerUninstall(this)
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
            packageInstallerUninstall = packageInstallerUninstall
        )
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        installUnknownSourcePermission()
//        createFirebaseToken()
        registerReceivers()

        binding.btnUninstallApp.setOnClickListener {
            mainPresenter.uninstallApplication()
        }

        binding.btnInstallPackInstaller.setOnClickListener {
            mainPresenter.downloadFile()
        }
    }

    private fun registerReceivers() {
//        firebaseReceiver = FirebaseReceiver()
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(MyFirebaseMessagingService.INTENT_FILTER)
//        registerReceiver(firebaseReceiver, intentFilter)
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
        super.onDestroy()
    }
}