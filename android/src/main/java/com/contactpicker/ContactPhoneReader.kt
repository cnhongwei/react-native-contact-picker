package com.contactpicker

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone

data class ContactPhone(val name: String, val phoneNumber: String)

internal object ContactPhoneReader {
  fun createIntent(): Intent = Intent(Intent.ACTION_PICK, Phone.CONTENT_URI)

  fun read(resolver: ContentResolver, uri: Uri): ContactPhone {
    // Never query Contacts/Data by ID or traverse this URI: the grant is scoped
    // to the user-selected row. Do not read photos, emails or other numbers.
    return resolver.query(uri, arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER), null, null, null)
      ?.use { cursor ->
        check(cursor.moveToFirst()) { "The selected phone record is empty." }
        val number = cursor.getString(cursor.getColumnIndexOrThrow(Phone.NUMBER))
        check(!number.isNullOrBlank()) { "The selected record has no phone number." }
        ContactPhone(
          cursor.getString(cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME)).orEmpty(),
          number
        )
      } ?: error("The selected phone record is unavailable.")
  }
}
