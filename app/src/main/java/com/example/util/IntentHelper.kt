package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object IntentHelper {

    fun handleExternalUri(context: Context, url: String): Boolean {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // Standard web link, keep inside webview
            return false
        }

        try {
            if (url.startsWith("intent://")) {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                if (intent != null) {
                    val packageManager = context.packageManager
                    val info = packageManager.resolveActivity(intent, 0)
                    if (info != null) {
                        context.startActivity(intent)
                        return true
                    } else {
                        // Try fallback URL if intent specified one
                        val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                        if (!fallbackUrl.isNullOrEmpty()) {
                            return handleExternalUri(context, fallbackUrl)
                        }
                        // Open Play Store for package
                        val appPackage = intent.`package`
                        if (!appPackage.isNullOrEmpty()) {
                            openPlayStore(context, appPackage)
                            return true
                        }
                    }
                }
            } else if (url.startsWith("market://") || url.contains("play.google.com/store/apps/details")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } else {
                // Scheme like whatsapp://, tg://, phonepe://, paytmmp://, upi://, mailto:, tel:
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val packageManager = context.packageManager
                if (intent.resolveActivity(packageManager) != null) {
                    context.startActivity(intent)
                    return true
                } else {
                    // Specific app fallback notifications
                    val appName = when {
                        url.startsWith("whatsapp://") -> "WhatsApp"
                        url.startsWith("tg://") || url.startsWith("telegram://") -> "Telegram"
                        url.startsWith("phonepe://") -> "PhonePe"
                        url.startsWith("paytmmp://") || url.startsWith("paytm://") -> "Paytm"
                        url.startsWith("upi://") -> "UPI Payment App"
                        else -> "Required App"
                    }
                    Toast.makeText(context, "$appName is not installed on this device.", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Cannot open external application.", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    private fun openPlayStore(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
