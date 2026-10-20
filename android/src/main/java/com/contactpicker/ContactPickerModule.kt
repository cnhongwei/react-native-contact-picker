package com.contactpicker

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import java.util.concurrent.Executors

class ContactPickerModule(private val context: ReactApplicationContext) :
  NativeContactPickerSpec(context) {

  private var pending: Promise? = null
  private var invalidated = false
  private val reader = Executors.newSingleThreadExecutor()
  private val listener = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
      if (requestCode != REQUEST_PICK_PHONE) return
      UiThreadUtil.runOnUiThread {
        val promise = pending ?: return@runOnUiThread
        if (resultCode == Activity.RESULT_CANCELED) {
          pending = null
          promise.resolve(null)
          return@runOnUiThread
        }
        val uri = data?.data
        if (resultCode != Activity.RESULT_OK || uri == null || uri.scheme != "content") {
          pending = null
          promise.reject("E_INVALID_RESULT", "The contact picker returned no readable phone record.")
          return@runOnUiThread
        }
        reader.execute {
          try {
            val phone = ContactPhoneReader.read(context.contentResolver, uri)
            UiThreadUtil.runOnUiThread {
              if (pending !== promise) return@runOnUiThread
              pending = null
              promise.resolve(Arguments.createMap().apply {
                putString("name", phone.name)
                putString("phoneNumber", phone.phoneNumber)
              })
            }
          } catch (error: Exception) {
            UiThreadUtil.runOnUiThread {
              if (pending !== promise) return@runOnUiThread
              pending = null
              promise.reject("E_READ_CONTACT", "Could not read the selected phone number.", error)
            }
          }
        }
      }
    }
  }

  init {
    context.addActivityEventListener(listener)
  }

  override fun pickContactPhone(promise: Promise) {
    UiThreadUtil.runOnUiThread {
      if (invalidated) {
        promise.reject("E_INVALIDATED", "The contact picker is no longer available.")
        return@runOnUiThread
      }
      if (pending != null) {
        promise.reject("E_PICKER_BUSY", "A contact picker is already open.")
        return@runOnUiThread
      }
      val activity = context.currentActivity
      if (activity == null || activity.isFinishing || activity.isDestroyed) {
        promise.reject("E_NO_ACTIVITY", "There is no active activity to present the contact picker.")
        return@runOnUiThread
      }
      pending = promise
      try {
        // The returned phone-row URI grants access to this row only.
        activity.startActivityForResult(ContactPhoneReader.createIntent(), REQUEST_PICK_PHONE)
      } catch (error: ActivityNotFoundException) {
        pending = null
        promise.reject("E_UNAVAILABLE", "No phone contact picker is installed.", error)
      } catch (error: Exception) {
        pending = null
        promise.reject("E_PRESENT_PICKER", "Could not open the contact picker.", error)
      }
    }
  }

  override fun invalidate() {
    context.removeActivityEventListener(listener)
    UiThreadUtil.runOnUiThread {
      invalidated = true
      pending?.reject("E_INVALIDATED", "The contact picker was invalidated.")
      pending = null
      reader.shutdown()
    }
    super.invalidate()
  }

  companion object {
    const val NAME = NativeContactPickerSpec.NAME
    private const val REQUEST_PICK_PHONE = 46271
  }
}
