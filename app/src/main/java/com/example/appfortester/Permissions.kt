package com.example.appfortester

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appfortester.utils.Constants
import com.example.appfortester.utils.Extensions.requestPermissionsCompat
import com.example.appfortester.utils.Extensions.shouldShowRequestPermissionRationaleCompat

class Permissions(private val context: Context) {

    private fun download() {
        if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            binding.btnInstallPackInstaller.isEnabled = true
            Log.d("info", "download - permission granted")
        } else {
            requestDownloadPermission()
            Log.d("info", "download - permission denied")
        }
    }

    private fun requestDownloadPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Permission is denied can show some alert here
                showPermissionDeniedDialog(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Constants.PERMISSION_REQUEST_STORAGE
                )
            } else {
                //ask permission
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Constants.PERMISSION_REQUEST_STORAGE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode)
        {
            Constants.PERMISSION_REQUEST_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    binding.btnInstallPackInstaller.isEnabled = true
                } else {
                    if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        permissionsGranted = true
                        showPermissionDeniedDialog(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Constants.PERMISSION_REQUEST_STORAGE
                        )
                    } else {
                        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) && permissionsGranted){
                            showMandatoryPermissionsNeedDialog()
                        }
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showMandatoryPermissionsNeedDialog() {
        AlertDialog.Builder(this).apply {
            setCancelable(true)
            setMessage("Writeable external storage is important for good working app")
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }.show()
    }

    private fun showPermissionDeniedDialog(permission: String, code: Int) {
        AlertDialog.Builder(this).apply {
            setCancelable(true)
            setTitle("Permission")
            setMessage("Camera access is required for sending image attachments, please enable it")
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), code)
            }
        }.show()
    }

}