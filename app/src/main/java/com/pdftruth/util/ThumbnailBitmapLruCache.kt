package com.pdftruth.util

import android.graphics.Bitmap
import android.util.LruCache

/**
 * 썸네일 전용 저해상도 Bitmap 캐시
 */
class ThumbnailBitmapLruCache(maxSize: Int) : LruCache<Int, Bitmap>(maxSize) {
    override fun entryRemoved(evicted: Boolean, key: Int, oldValue: Bitmap, newValue: Bitmap?) {
        if (evicted && !oldValue.isRecycled) {
            oldValue.recycle()
        }
    }
}