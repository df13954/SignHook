package cn.xihan.sign.hook

import android.content.pm.PackageInfo
import android.content.pm.Signature
import cn.xihan.sign.BuildConfig
import cn.xihan.sign.utli.loge
import cn.xihan.sign.utli.readOptionModel
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.log.YukiHookLogger
import com.highcapable.yukihookapi.hook.type.android.PackageInfoClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

/**
 * @项目名 : 签名助手
 * @作者 : MissYang
 * @创建时间 : 2023/6/19 18:10
 * @介绍 :
 */
@InjectYukiHookWithXposed
class HookEntry : IYukiHookXposedInit {

    override fun onInit() {
        YukiHookAPI.configs {
            YukiHookLogger.Configs.tag = "yuki"
            YukiHookLogger.Configs.isEnable = BuildConfig.DEBUG
        }
    }

    override fun onHook() = YukiHookAPI.encase {

        if (packageName in PACKAGE_NAME_LIST) {
            loadApp(packageName) {
                findClass("android.app.ApplicationPackageManager").hook {
                    injectMember {
                        method {
                            name = "getPackageInfo"
                            param(StringClass, IntType)
                            returnType = PackageInfoClass
                        }
                        afterHook {
                            val packageInfo = result as PackageInfo
                            if (apkSignatureList.isEmpty()) {
                                "apkSignatureList is empty".loge()
                                return@afterHook
                            }
                            runCatching {
                                apkSignatureList.forEach { apkSignature ->
                                    if (packageInfo.packageName == apkSignature.packageName) {
                                        packageInfo.signatures =
                                            arrayOf(Signature(apkSignature.forgedSignature))
                                    }
                                }
                            }

                            result = packageInfo
                        }
                    }
                }
            }
        }

    }

    companion object {

        val PACKAGE_NAME_LIST by lazy {
            optionModel.packageNameList
        }

        val apkSignatureList by lazy {
            optionModel.apkSignatureList
        }

        val optionModel by lazy {
            readOptionModel()
        }

    }

}