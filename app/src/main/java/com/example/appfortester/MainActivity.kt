package com.example.appfortester

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.example.appfortester.databinding.ActivityMainBinding
import com.example.appfortester.installers.IntentInstallerVersion
import com.example.appfortester.installers.PackageInstallerVersion
import com.example.appfortester.utils.Constants.MAIN_URL
import com.example.appfortester.utils.Constants.PERMISSION_REQUEST_STORAGE
import com.example.appfortester.utils.Extensions.checkSelfPermissionCompat
import com.example.appfortester.utils.Extensions.requestPermissionsCompat
import com.example.appfortester.utils.Extensions.shouldShowRequestPermissionRationaleCompat
import com.example.appfortester.utils.Extensions.showSnackbar
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class MainActivity : MvpAppCompatActivity(), MainView {

    val temp = "https://androidwave.com/download-and-install-apk-programmatically/"

    @InjectPresenter
    lateinit var mainPresenter: MainPresenter
    private val intentInstallerVersion =  IntentInstallerVersion(context = this)
    private val packageInstallerVersion =  PackageInstallerVersion(context = this)
    private val downloader = Downloader(this, MAIN_URL, intentInstallerVersion, packageInstallerVersion)

    @ProvidePresenter
    fun provideMainPresenter(): MainPresenter{
        return MainPresenter(downloader = downloader)
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInstallIntent.setOnClickListener {
            checkPermission()
        }

        binding.btnInstallPackInstaller.setOnClickListener {
            mainPresenter.getAppAndInstallViaPackInstaller()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mainPresenter.getAppAndInstallViaIntent()
            } else {
                binding.root.showSnackbar(R.string.storage_permission_denied, Snackbar.LENGTH_SHORT)
            }
        }
    }

    private fun checkPermission(){
        if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            mainPresenter.getAppAndInstallViaIntent()
        } else {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            binding.root.showSnackbar(
                R.string.storage_access_required,
                Snackbar.LENGTH_INDEFINITE, R.string.ok
            ) {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            }
        } else {
            requestPermissionsCompat(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        }
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun checkPermission(){
//        when{
//            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//                -> mainPresenter.getAppAndInstallViaIntent()
//
//            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
//                showPermissionDialog()
//            }
//            else -> {
//                pLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(applicationContext)
        builder.setTitle("Permission required")
        builder.setMessage("Some permissions are needed to be allowed to use this app without any problems.")
        builder.setPositiveButton("Grant") { dialog, which ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package", applicationContext.packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

//    private fun registerPermissionLauncher(){
//        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
//            if (it){
//                Snackbar.make(binding.root, "Permissions confirmed", Snackbar.LENGTH_SHORT).show()
//            } else {
//                Snackbar.make(binding.root, "Permissions denied", Snackbar.LENGTH_SHORT).show()
//            }
//        }
//    }

    override fun installed() {
        Snackbar.make(binding.root, "Installation is finished successful", Snackbar.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun downloaded() {
        Snackbar.make(binding.root, "Download is finished successful", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}