package com.example.japanmap.data.local.file

import android.content.Context
import com.example.japanmap.domain.repository.PhotoStorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * 사진 원본을 앱 내부 저장소 `filesDir/Photos/`에 저장. iOS `PhotoFileSystemDataSource` 대응.
 * (Coil이 파일 경로에서 직접 썸네일 디코딩하므로 별도 썸네일 캐시는 두지 않음)
 */
class PhotoFileStorage(context: Context) : PhotoStorageRepository {

    private val photosDir: File = File(context.filesDir, "Photos").apply { mkdirs() }

    override suspend fun save(imageData: ByteArray): String = withContext(Dispatchers.IO) {
        val fileName = "${UUID.randomUUID()}.jpg"
        File(photosDir, fileName).writeBytes(imageData)
        fileName
    }

    override suspend fun loadImageData(fileName: String): ByteArray = withContext(Dispatchers.IO) {
        File(photosDir, fileName).readBytes()
    }

    override fun fileFor(fileName: String): File = File(photosDir, fileName)

    override suspend fun delete(fileName: String) = withContext(Dispatchers.IO) {
        File(photosDir, fileName).delete()
        Unit
    }
}
