package com.contactpicker

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
class ContactPhoneReaderTest {
  private val uri = Uri.parse("content://com.example.picker/selected-phone/42")
  private lateinit var provider: SelectedPhoneProvider

  @Before fun setup() {
    provider = SelectedPhoneProvider()
    ShadowContentResolver.registerProviderInternal("com.example.picker", provider)
  }

  @Test fun `选择电话号码而非整个联系人`() {
    val intent = ContactPhoneReader.createIntent()
    assertEquals(Intent.ACTION_PICK, intent.action)
    assertEquals(Phone.CONTENT_URI, intent.data)
  }

  @Test fun `只读取选中 URI 的姓名和号码且不需要通讯录权限`() {
    val result = ContactPhoneReader.read(RuntimeEnvironment.getApplication().contentResolver, uri)
    assertEquals(ContactPhone("测试联系人", "+66 812 345 678"), result)
    assertEquals(listOf(uri), provider.queries)
    assertArrayEquals(arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER), provider.projection)
  }

  @Test fun `没有姓名时仍返回用户选择的号码`() {
    provider.name = null
    assertEquals("", ContactPhoneReader.read(RuntimeEnvironment.getApplication().contentResolver, uri).name)
  }

  @Test fun `没有号码时报告读取失败而不是取消`() {
    provider.phone = " "
    assertThrows(IllegalStateException::class.java) {
      ContactPhoneReader.read(RuntimeEnvironment.getApplication().contentResolver, uri)
    }
  }

  @Test fun `空记录和空游标都报告读取失败`() {
    provider.empty = true
    assertThrows(IllegalStateException::class.java) {
      ContactPhoneReader.read(RuntimeEnvironment.getApplication().contentResolver, uri)
    }
    provider.nullCursor = true
    assertThrows(IllegalStateException::class.java) {
      ContactPhoneReader.read(RuntimeEnvironment.getApplication().contentResolver, uri)
    }
  }
}

class SelectedPhoneProvider : ContentProvider() {
  val queries = mutableListOf<Uri>()
  var projection: Array<out String>? = null
  var name: String? = "测试联系人"
  var phone: String? = "+66 812 345 678"
  var empty = false
  var nullCursor = false
  override fun onCreate() = true
  override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
    check(uri.path == "/selected-phone/42") { "Must read the granted URI directly" }
    check(selection == null && selectionArgs == null && sortOrder == null)
    queries.add(uri)
    this.projection = projection
    if (nullCursor) return null
    return MatrixCursor(arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER)).apply {
      if (!empty) addRow(arrayOf(name, phone))
    }
  }
  override fun getType(uri: Uri) = Phone.CONTENT_ITEM_TYPE
  override fun insert(uri: Uri, values: ContentValues?): Uri? = error("Not supported")
  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = error("Not supported")
  override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = error("Not supported")
}
