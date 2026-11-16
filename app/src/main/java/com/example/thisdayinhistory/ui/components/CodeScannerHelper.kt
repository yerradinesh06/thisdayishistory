package com.example.thisdayinhistory.ui.components

import android.content.Context
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.util.Locale

object CodeScannerHelper {

    fun scanQrCode(
        context: Context,
        onDateScanned: (month: String, day: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue ?: ""
                val parsed = parseMonthAndDay(rawValue)
                if (parsed != null) {
                    onDateScanned(parsed.first, parsed.second)
                } else {
                    onError("Invalid QR code date: '$rawValue'. Expected format MM-DD (e.g. 10-25)")
                }
            }
            .addOnCanceledListener {
                // User cancelled scan
            }
            .addOnFailureListener { e ->
                onError("Scanner error: ${e.localizedMessage}")
            }
    }

    fun parseMonthAndDay(input: String): Pair<String, String>? {
        val clean = input.trim()

        val regexMmDd = Regex("""^(\d{1,2})[/\-](\d{1,2})$""")
        val matchMmDd = regexMmDd.find(clean)
        if (matchMmDd != null) {
            val (m, d) = matchMmDd.destructured
            val monthInt = m.toIntOrNull()
            val dayInt = d.toIntOrNull()
            if (monthInt != null && monthInt in 1..12 && dayInt != null && dayInt in 1..31) {
                val formattedMonth = String.format(Locale.US, "%02d", monthInt)
                val formattedDay = String.format(Locale.US, "%02d", dayInt)
                return Pair(formattedMonth, formattedDay)
            }
        }

        val regexYmd = Regex("""^\d{4}[/\-](\d{1,2})[/\-](\d{1,2})$""")
        val matchYmd = regexYmd.find(clean)
        if (matchYmd != null) {
            val (m, d) = matchYmd.destructured
            val monthInt = m.toIntOrNull()
            val dayInt = d.toIntOrNull()
            if (monthInt != null && monthInt in 1..12 && dayInt != null && dayInt in 1..31) {
                val formattedMonth = String.format(Locale.US, "%02d", monthInt)
                val formattedDay = String.format(Locale.US, "%02d", dayInt)
                return Pair(formattedMonth, formattedDay)
            }
        }
        return null
    }
}
