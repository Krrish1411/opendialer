package com.opendialer.app.data.repository

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.opendialer.app.core.common.PhoneNumberUtils
import com.opendialer.app.data.local.dao.ContactSimPreferenceDao
import com.opendialer.app.data.local.dao.FavoritesDao
import com.opendialer.app.data.model.ContactUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoritesDao: FavoritesDao,
    private val simPrefDao: ContactSimPreferenceDao
) {

    private fun hasReadContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getContacts(query: String = ""): List<ContactUiModel> = withContext(Dispatchers.IO) {
        if (!hasReadContactsPermission()) return@withContext emptyList()

        val contactList = mutableListOf<ContactUiModel>()
        val seenNumbers = mutableSetOf<String>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )

        val selection = if (query.isNotBlank()) {
            "(${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?)"
        } else null

        val selectionArgs = if (query.isNotBlank()) {
            arrayOf("%$query%", "%$query%")
        } else null

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"

        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use { c ->
            val idCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val lookupCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)
            val nameCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
            val numberCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val starredCol = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

            while (c.moveToNext()) {
                val rawNumber = if (numberCol >= 0) c.getString(numberCol) ?: "" else ""
                val normalized = PhoneNumberUtils.normalize(rawNumber)
                if (normalized.isEmpty() || seenNumbers.contains(normalized)) continue
                seenNumbers.add(normalized)

                val id = if (idCol >= 0) c.getLong(idCol) else 0L
                val lookupKey = if (lookupCol >= 0) c.getString(lookupCol) ?: "" else ""
                val name = if (nameCol >= 0) c.getString(nameCol) ?: "Unknown" else "Unknown"
                val photoUri = if (photoCol >= 0) c.getString(photoCol) else null
                val isStarred = if (starredCol >= 0) c.getInt(starredCol) == 1 else false

                val preferredSim = simPrefDao.getPreferredSlot(normalized)

                contactList.add(
                    ContactUiModel(
                        id = id,
                        lookupKey = lookupKey,
                        displayName = name,
                        phoneNumber = rawNumber,
                        normalizedNumber = normalized,
                        photoUri = photoUri,
                        isFavorite = isStarred,
                        preferredSimSlot = preferredSim
                    )
                )
            }
        }

        contactList
    }

    suspend fun getContactByNumber(number: String): ContactUiModel? = withContext(Dispatchers.IO) {
        if (!hasReadContactsPermission()) return@withContext null
        val normalized = PhoneNumberUtils.normalize(number)
        if (normalized.isEmpty()) return@withContext null

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )

        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val idCol = c.getColumnIndex(ContactsContract.PhoneLookup._ID)
                val nameCol = c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val photoCol = c.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)

                val id = if (idCol >= 0) c.getLong(idCol) else 0L
                val name = if (nameCol >= 0) c.getString(nameCol) ?: number else number
                val photoUri = if (photoCol >= 0) c.getString(photoCol) else null
                val preferredSim = simPrefDao.getPreferredSlot(normalized)

                return@withContext ContactUiModel(
                    id = id,
                    displayName = name,
                    phoneNumber = number,
                    normalizedNumber = normalized,
                    photoUri = photoUri,
                    preferredSimSlot = preferredSim
                )
            }
        }
        null
    }

    suspend fun updateContact(
        contactId: Long,
        displayName: String,
        phoneNumber: String,
        email: String? = null,
        customRingtone: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val ops = ArrayList<android.content.ContentProviderOperation>()

            // 1. Update Display Name
            ops.add(
                android.content.ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                        arrayOf(contactId.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                    .build()
            )

            // 2. Update Phone Number
            ops.add(
                android.content.ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                        arrayOf(contactId.toString(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .build()
            )

            // 3. Update Custom Ringtone if provided
            if (customRingtone != null) {
                val contactUri = android.content.ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
                val values = android.content.ContentValues().apply {
                    put(ContactsContract.Contacts.CUSTOM_RINGTONE, customRingtone)
                }
                context.contentResolver.update(contactUri, values, null, null)
            }

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            com.opendialer.app.core.telephony.ContactLookupHelper.clearCache()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteContact(contactId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = android.content.ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val rows = context.contentResolver.delete(uri, null, null)
            com.opendialer.app.core.telephony.ContactLookupHelper.clearCache()
            rows > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createContact(
        displayName: String,
        phoneNumber: String,
        email: String? = null,
        customRingtone: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val ops = ArrayList<android.content.ContentProviderOperation>()

            ops.add(
                android.content.ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            ops.add(
                android.content.ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                    .build()
            )

            ops.add(
                android.content.ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            if (!email.isNullOrBlank()) {
                ops.add(
                    android.content.ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                        .build()
                )
            }

            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            com.opendialer.app.core.telephony.ContactLookupHelper.clearCache()
            results.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
