package com.daotrung.myapplication.abs

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import com.daotrung.myapplication.R
import com.daotrung.myapplication.util.LogInstance
import com.daotrung.myapplication.util.VersionUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getPermissionString
import com.simplemobiletools.commons.extensions.hasPermission
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.isRPlus
import me.yokeyword.fragmentation.SupportActivity

abstract class BaseActivity<VB : ViewBinding> : SupportActivity() {
    var TAG = this::class.simpleName

    protected lateinit var binding: VB
    protected abstract fun binding(): VB
    protected abstract fun initView()

    companion object {
        const val PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = binding()
        setContentView(binding.root)
        initView()
        LogInstance.e(TAG.toString())
        permissions = getPermissionsToRequest()
        hadPermissions = hasPermissions()
        permissionDeniedMessage = null
    }


    override fun onResume() {
        super.onResume()
        val hasPermissions = hasPermissions()
        if (hasPermissions != hadPermissions) {
            hadPermissions = hasPermissions
            if (VersionUtils.hasMarshmallow()) {
                onHasPermissionsChanged(hasPermissions)
            }
        }
    }

    private var permissionDeniedMessage: String? = null
    private var hadPermissions: Boolean = false

    private fun getPermissionsToRequest(): Array<String> {
        return mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE).apply {
            if (!VersionUtils.hasQ()) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    protected fun setPermissionDeniedMessage(message: String) {
        permissionDeniedMessage = message
    }

    fun getPermissionDeniedMessage(): String {
        return if (permissionDeniedMessage == null) getString(R.string.permissions_denied) else permissionDeniedMessage!!
    }

    private val snackBarContainer: View
        get() = window.decorView

    @SuppressLint("ShowToast")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@BaseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        showSnackbar(getPermissionDeniedMessage())
                    } else {
                        showSnackbar("Permission to access external storage denied.")
                    }
                    return
                }
            }
            hadPermissions = true
            onHasPermissionsChanged(true)
        }
    }


    protected fun hasPermissions(): Boolean {
        for (permission in getPermissionsToRequest()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }


    protected open fun onHasPermissionsChanged(hasPermissions: Boolean) {
        println(hasPermissions)
    }

    fun addFragment(frame_container: FrameLayout, frag: Fragment) {
        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.replace(frame_container.id, frag)
        fragTransaction.commit()
    }

    fun addFragment(frame_container: Int, frag: Fragment) {
        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.replace(frame_container, frag)
        fragTransaction.commit()
    }

    fun replaceFragment(containerId: Int, fragment: Fragment) {
        val backStateName = fragment.javaClass.name
        val fragmentPopped: Boolean = supportFragmentManager.popBackStackImmediate(backStateName, 0)
        if (!fragmentPopped && supportFragmentManager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.replace(containerId, fragment, backStateName)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
    }

    fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    protected fun showSnackbar(message: String) {
        val view = findViewById<View>(R.id.content)
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    open fun getVisibleFragment(): Fragment? {
        val fragmentManager: FragmentManager = this.supportFragmentManager
        val fragments: List<Fragment> =
            fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.isVisible) return fragment
        }
        return null
    }


    private lateinit var permissions: Array<String>
    protected open fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
    }

}