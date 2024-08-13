package dev.bacecek.launcher.make_default

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import dev.bacecek.launcher.di.CoroutineDispatchers
import dev.bacecek.launcher.utils.SimpleActivityLifecycleCallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface DefaultLauncherRepository {
    val isDefault: StateFlow<Boolean>
}

class DefaultLauncherRepositoryImpl(
    private val scope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val context: Application,
) : DefaultLauncherRepository {

    private val _isDefault = MutableStateFlow(false)
    override val isDefault: StateFlow<Boolean> = _isDefault

    init {
        context.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityResumed(activity: Activity) = fetchDefaultState()
        })
        fetchDefaultState()
    }

    private fun fetchDefaultState() {
        scope.launch(dispatchers.io) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val homePackage = try {
                val packageManager = context.packageManager
                val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                resolveInfo?.activityInfo?.packageName
            } catch (e: Exception) {
                null
            }
            _isDefault.value = homePackage == context.packageName
        }
    }

}
