package com.contactpicker

import com.facebook.react.bridge.ReactApplicationContext

class ContactPickerModule(reactContext: ReactApplicationContext) :
  NativeContactPickerSpec(reactContext) {

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = NativeContactPickerSpec.NAME
  }
}
