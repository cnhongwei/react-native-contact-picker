package com.contactpicker

import android.app.Activity
import android.content.Intent
import android.os.Looper
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import java.lang.reflect.Proxy
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
class ContactPickerModuleTest {
  private lateinit var context: ReactApplicationContext
  private lateinit var module: ContactPickerModule
  private lateinit var activity: Activity
  private lateinit var listener: ActivityEventListener

  @Before fun setup() {
    context = mock(ReactApplicationContext::class.java)
    activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    `when`(context.currentActivity).thenReturn(activity)
    module = ContactPickerModule(context)
    val captor = ArgumentCaptor.forClass(ActivityEventListener::class.java)
    verify(context).addActivityEventListener(captor.capture())
    listener = captor.value
  }

  @Test fun `用户取消后返回空值并允许重新打开`() {
    val first = PromiseResult()
    module.pickContactPhone(first.promise)
    shadowOf(Looper.getMainLooper()).idle()
    val started = shadowOf(activity).nextStartedActivityForResult
    assertNotNull(started)
    listener.onActivityResult(activity, started.requestCode, Activity.RESULT_CANCELED, null)
    shadowOf(Looper.getMainLooper()).idle()
    assertTrue(first.resolved)
    assertNull(first.value)
    val second = PromiseResult()
    module.pickContactPhone(second.promise)
    shadowOf(Looper.getMainLooper()).idle()
    assertNotNull(shadowOf(activity).nextStartedActivityForResult)
    assertNull(second.errorCode)
    module.invalidate()
  }

  @Test fun `重复调用拒绝第二个请求而不覆盖第一个`() {
    val first = PromiseResult()
    val second = PromiseResult()
    module.pickContactPhone(first.promise)
    module.pickContactPhone(second.promise)
    shadowOf(Looper.getMainLooper()).idle()
    assertEquals("E_PICKER_BUSY", second.errorCode)
    assertFalse(first.resolved)
    assertNull(first.errorCode)
    module.invalidate()
  }

  @Test fun `缺少前台页面时返回明确错误`() {
    `when`(context.currentActivity).thenReturn(null)
    val result = PromiseResult()
    module.pickContactPhone(result.promise)
    shadowOf(Looper.getMainLooper()).idle()
    assertEquals("E_NO_ACTIVITY", result.errorCode)
    module.invalidate()
  }

  @Test fun `系统成功但没有 URI 时返回错误并释放忙碌状态`() {
    val result = PromiseResult()
    module.pickContactPhone(result.promise)
    shadowOf(Looper.getMainLooper()).idle()
    val started = shadowOf(activity).nextStartedActivityForResult
    listener.onActivityResult(activity, started.requestCode, Activity.RESULT_OK, Intent())
    shadowOf(Looper.getMainLooper()).idle()
    assertEquals("E_INVALID_RESULT", result.errorCode)
    val next = PromiseResult()
    module.pickContactPhone(next.promise)
    shadowOf(Looper.getMainLooper()).idle()
    assertNull(next.errorCode)
    module.invalidate()
  }

  @Test fun `模块销毁结束待处理请求并拒绝后续调用`() {
    val result = PromiseResult()
    module.pickContactPhone(result.promise)
    module.invalidate()
    shadowOf(Looper.getMainLooper()).idle()
    assertEquals("E_INVALIDATED", result.errorCode)
    val next = PromiseResult()
    module.pickContactPhone(next.promise)
    shadowOf(Looper.getMainLooper()).idle()
    assertEquals("E_INVALIDATED", next.errorCode)
  }
}

private class PromiseResult {
  var resolved = false
  var value: Any? = null
  var errorCode: String? = null
  val promise = Proxy.newProxyInstance(Promise::class.java.classLoader, arrayOf(Promise::class.java)) { _, method, args ->
    when (method.name) {
      "resolve" -> { resolved = true; value = args?.get(0) }
      "reject" -> { errorCode = args?.get(0) as? String }
    }
    null
  } as Promise
}
