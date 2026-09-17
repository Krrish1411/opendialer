package com.opendialer.app.core.telephony

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.opendialer.app.core.common.PhoneNumberUtils
import java.util.concurrent.ConcurrentHashMap

data class ResolvedContact(
    val displayName: String,
    val photoUri: String? = null,
    val isFromLocalContacts: Boolean = false
)

object ContactLookupHelper {

    private val cache = ConcurrentHashMap<String, ResolvedContact>()

    fun resolveCaller(
        context: Context,
        rawNumber: String?,
        carrierDisplayName: String? = null
    ): ResolvedContact {
        val number = rawNumber?.trim() ?: ""
        if (number.isBlank() || number == "Unknown") {
            return ResolvedContact(displayName = "Unknown Caller", photoUri = null, isFromLocalContacts = false)
        }

        val normalized = PhoneNumberUtils.normalize(number)
        val cacheKey = if (normalized.isNotEmpty()) normalized else number

        cache[cacheKey]?.let { return it }

        // 1. Query Android ContactsContract.PhoneLookup
        var localName: String? = null
        var photoUri: String? = null

        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            val projection = arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_URI
            )

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor: Cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)
                    if (nameIndex >= 0) {
                        localName = cursor.getString(nameIndex)?.takeIf { it.isNotBlank() }
                    }
                    if (photoIndex >= 0) {
                        photoUri = cursor.getString(photoIndex)
                    }
                }
            }
        } catch (_: Exception) {
        }

        val resolved = when {
            // Local contact name has highest priority (e.g. "Mummy")
            !localName.isNullOrBlank() -> ResolvedContact(
                displayName = localName!!,
                photoUri = photoUri,
                isFromLocalContacts = true
            )
            // Fallback to network carrier caller ID (CNAM)
            !carrierDisplayName.isNullOrBlank() && carrierDisplayName != number -> ResolvedContact(
                displayName = carrierDisplayName,
                photoUri = null,
                isFromLocalContacts = false
            )
            // Fallback to formatted phone number
            else -> ResolvedContact(
                displayName = PhoneNumberUtils.format(number),
                photoUri = null,
                isFromLocalContacts = false
            )
        }

        cache[cacheKey] = resolved
        return resolved
    }

    fun clearCache() {
        cache.clear()
    }
}
