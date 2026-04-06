package com.wisdometer.export

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonImporter @Inject constructor() {
    suspend fun importFromUri(uri: Uri): Int {
        TODO("implement in Task 14")
    }
}
