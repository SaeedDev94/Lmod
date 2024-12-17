package io.github.saeeddev94.lmod

import android.content.Context
import android.content.Intent
import android.os.Message
import android.provider.Telephony
import android.webkit.WebView
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.topjohnwu.superuser.Shell

@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        isDebug = false
    }

    override fun onHook() = encase {
        loadApp(name = "com.android.launcher3") {
            "com.android.launcher3.touch.WorkspaceTouchListener".toClassOrNull()?.apply {
                method {
                    name = "onDoubleTap"
                    superClass()
                }.hook {
                    after {
                        Shell.cmd("input keyevent 223").exec()
                    }
                }
            }
        }

        loadApp(name = "com.simplemobiletools.smsmessenger") {
            "com.simplemobiletools.smsmessenger.receivers.SmsReceiver".toClassOrNull()?.apply {
                method {
                    name = "onReceive"
                    param(Context::class.java, Intent::class.java)
                }.hook {
                    after {
                        val context = args[0] as Context
                        val intent = args[1] as Intent
                        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                        var text = ""
                        messages.forEach { text += it.messageBody }
                        val otp = OtpExtractor.extract(text) ?: return@after
                        Clipboard.copy(context, otp)
                    }
                }
            }
        }

        loadApp(name = "org.lineageos.jelly") {
            "android.webkit.WebView".toClassOrNull()?.apply {
                method {
                    name = "getSettings"
                }.hook {
                    after {
                        val webSettings = result as android.webkit.WebSettings
                        webSettings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                    }
                }
            }

            "org.lineageos.jelly.webview.ChromeClient".toClassOrNull()?.apply {
                method {
                    name = "onCreateWindow"
                    param(WebView::class.java, Boolean::class.java, Boolean::class.java, Message::class.java)
                }.hook {
                    replaceAny {
                        val ref = instance::class.java
                        val activity = ref.getDeclaredField("activity")
                        val incognito = ref.getDeclaredField("incognito")
                        activity.isAccessible = true
                        incognito.isAccessible = true

                        val view = args[0] as WebView
                        val isDialog = args[1] as Boolean
                        val isUserGesture = args[2] as Boolean
                        val resultMsg = args[3] as Message

                        ChromeClient(
                            activity.get(instance) as Context,
                            incognito.get(instance) as Boolean,
                        ).onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                    }
                }
            }
        }
    }

}
