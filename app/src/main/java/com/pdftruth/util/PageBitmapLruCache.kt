package com.pdftruth.util

import android.graphics.Bitmap
import android.util.LruCache

/**
 * 현재 페이지 중심 ±2 페이지만 유지하는 LRU 캐시
 */
class PageBitmapLruCache(maxSize: Int) : LruCache<Int, Bitmap>(maxSize) {
    override fun entryRemoved(evicted: Boolean, key: Int, oldValue: Bitmap, newValue: Bitmap?) {
        if (evicted) {
            oldValue.recycle()
        }
    }
}