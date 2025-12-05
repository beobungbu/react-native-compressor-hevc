package com.reactnativecompressor.Video

import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.reactnativecompressor.Utils.Utils
import com.reactnativecompressor.Video.VideoCompressorHelper.Companion.video_activateBackgroundTask_helper
import com.reactnativecompressor.Video.VideoCompressorHelper.Companion.video_deactivateBackgroundTask_helper
import java.io.File

class VideoMain(private val reactContext: ReactApplicationContext) {
    //Video
    fun compress(
            fileUrl: String,
            optionMap: ReadableMap,
            promise: Promise) {
        var fileUrl: String? = fileUrl
        val options = VideoCompressorHelper.fromMap(optionMap)
        fileUrl = Utils.getRealPath(fileUrl, reactContext, options.uuid,options.progressDivider)
        if (options.compressionMethod === VideoCompressorHelper.CompressionMethod.auto) {
            VideoCompressorHelper.VideoCompressAuto(fileUrl, options, promise, reactContext)
        } else {
            VideoCompressorHelper.VideoCompressManual(fileUrl, options, promise, reactContext)
        }
    }

    fun cancelCompression(
            uuid: String) {
        Utils.cancelCompressionHelper(uuid)
        Log.d("cancelCompression", uuid)
    }

     fun activateBackgroundTask(
            options: ReadableMap,
            promise: Promise) {
        try {
            val response: String = video_activateBackgroundTask_helper(options, reactContext)
            promise.resolve(response)
        } catch (ex: Exception) {
            promise.reject(ex)
        }
    }

   fun deactivateBackgroundTask(
            options: ReadableMap,
            promise: Promise) {
        try {
            val response: String = video_deactivateBackgroundTask_helper(options, reactContext)
            promise.resolve(response)
        } catch (ex: Exception) {
            promise.reject(ex)
        }
    }

  fun getVideoMetaData(filePath: String, promise: Promise) {
    var filePath: String? = filePath
    try {
      filePath = Utils.getRealPath(filePath, reactContext)
      val uri = Uri.parse(filePath)
      val srcPath = uri.path
      val metaRetriever = MediaMetadataRetriever()
      metaRetriever.setDataSource(srcPath)
      val file = File(srcPath)
      val sizeInBytes = (file.length()).toDouble()
      val actualHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
      val actualWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
      val duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toDouble()
      val creationTime = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
      val extension = filePath!!.substring(filePath.lastIndexOf(".") + 1)
      val params = Arguments.createMap()
      params.putDouble("size", sizeInBytes)
      params.putInt("width", actualWidth)
      params.putInt("height", actualHeight)
      params.putDouble("duration", duration / 1000)
      params.putString("extension", extension)
      params.putString("creationTime", creationTime.toString())

      // Get video codec and bitrate using MediaExtractor
      try {
        val extractor = MediaExtractor()
        extractor.setDataSource(srcPath!!)
        for (i in 0 until extractor.trackCount) {
          val format = extractor.getTrackFormat(i)
          val mime = format.getString(MediaFormat.KEY_MIME)
          if (mime?.startsWith("video/") == true) {
            val codecName = getCodecName(mime)
            params.putString("codec", codecName)
            params.putString("mimeType", mime)
            if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
              params.putInt("bitrate", format.getInteger(MediaFormat.KEY_BIT_RATE))
            }
            break
          }
        }
        extractor.release()
      } catch (e: Exception) {
        Log.w("VideoMain", "Could not extract codec info: ${e.message}")
      }

      metaRetriever.release()
      promise.resolve(params)
    } catch (e: Exception) {
      promise.reject(e)
    }
  }

  private fun getCodecName(mimeType: String): String {
    return when (mimeType) {
      "video/hevc", "video/dolby-vision" -> "HEVC"
      "video/avc" -> "H.264"
      "video/mp4v-es" -> "MPEG-4"
      "video/3gpp" -> "H.263"
      "video/x-vnd.on2.vp8" -> "VP8"
      "video/x-vnd.on2.vp9" -> "VP9"
      "video/av01" -> "AV1"
      else -> mimeType.removePrefix("video/").uppercase()
    }
  }
}
