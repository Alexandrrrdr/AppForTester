package com.example.appfortester

import com.example.appfortester.utils.Constants
import moxy.InjectViewState
import moxy.MvpPresenter


@InjectViewState
class MainPresenter(
    private val downloader: Downloader
) : MvpPresenter<MainView>() {

    fun getAppAndInstallViaIntent() {
        downloader.downloadFile(Constants.MAIN_URL, Constants.INTENT_INSTALL)
    }

    fun getAppAndInstallViaPackInstaller(){
        downloader.downloadFile(Constants.MAIN_URL, Constants.PACKAGE_INSTALL)
    }
}