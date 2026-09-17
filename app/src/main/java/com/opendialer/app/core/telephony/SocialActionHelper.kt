package com.opendialer.app.core.telephony

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import com.opendialer.app.core.common.PhoneNumberUtils

object SocialActionHelper {

    fun openWhatsAppChat(context: Context, phoneNumber: String) {
        val cleanNumber = PhoneNumberUtils.normalize(phoneNumber).removePrefix("+")
        if (cleanNumber.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun startWhatsAppVoiceCall(context: Context, phoneNumber: String) {
        val cleanNumber = PhoneNumberUtils.normalize(phoneNumber).removePrefix("+")
        val dataId = getWhatsAppCallDataId(context, cleanNumber, "vnd.android.cursor.item/vnd.com.whatsapp.voip.call")
        if (dataId != null) {
            try {
                val intent = Intent().apply {
                    action = Intent.ACTION_VIEW
                    setDataAndType(
                        Uri.parse("content://com.android.contacts/data/$dataId"),
                        "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
                    )
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
        // Fallback to chat if direct voice call intent fails
        openWhatsAppChat(context, phoneNumber)
    }

    fun startWhatsAppVideoCall(context: Context, phoneNumber: String) {
        val cleanNumber = PhoneNumberUtils.normalize(phoneNumber).removePrefix("+")
        val dataId = getWhatsAppCallDataId(context, cleanNumber, "vnd.android.cursor.item/vnd.com.whatsapp.video.call")
        if (dataId != null) {
            try {
                val intent = Intent().apply {
                    action = Intent.ACTION_VIEW
                    setDataAndType(
                        Uri.parse("content://com.android.contacts/data/$dataId"),
                        "vnd.android.cursor.item/vnd.com.whatsapp.video.call"
                    )
                    setPackage("com.whatsapp")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) {}
        }
        // Fallback to chat if direct video call intent fails
        openWhatsAppChat(context, phoneNumber)
    }

    fun openTelegramChat(context: Context, phoneNumber: String) {
        val cleanNumber = PhoneNumberUtils.normalize(phoneNumber).removePrefix("+")
        if (cleanNumber.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("tg://msg?to=+$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://t.me/+$cleanNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Telegram is not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sendSms(context: Context, phoneNumber: String, messageBody: String = "") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:" + Uri.encode(phoneNumber))
                if (messageBody.isNotBlank()) {
                    putExtra("sms_body", messageBody)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open SMS app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getWhatsAppCallDataId(context: Context, cleanNumber: String, mimeType: String): Long? {
        val projection = arrayOf(
            ContactsContract.Data._ID,
            ContactsContract.Data.DATA1
        )
        val selection = "${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(mimeType)

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )
            cursor?.use { c ->
                val idIndex = c.getColumnIndex(ContactsContract.Data._ID)
                val data1Index = c.getColumnIndex(ContactsContract.Data.DATA1)
                while (c.moveToNext()) {
                    val rawData1 = if (data1Index >= 0) c.getString(data1Index) ?: "" else ""
                    val normalized = PhoneNumberUtils.normalize(rawData1).removePrefix("+")
                    if (normalized == cleanNumber || (cleanNumber.length >= 10 && normalized.endsWith(cleanNumber.takeLast(10)))) {
                        return if (idIndex >= 0) c.getLong(idIndex) else null
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return null
    }
}
