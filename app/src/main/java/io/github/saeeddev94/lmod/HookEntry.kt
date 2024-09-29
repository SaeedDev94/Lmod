package io.github.saeeddev94.lmod

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
    }

}
