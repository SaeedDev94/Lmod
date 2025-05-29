package io.github.saeeddev94.lmod

import com.crossbowffs.remotepreferences.RemotePreferenceFile
import com.crossbowffs.remotepreferences.RemotePreferenceProvider

class MyPreferenceProvider : RemotePreferenceProvider(
    SharedPref.PKG,
    arrayOf(RemotePreferenceFile(SharedPref.NAME, true))
)
