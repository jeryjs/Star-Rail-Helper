package com.jery.starrailhelper.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.jery.starrailhelper.activity.MainActivity.Companion.binding
import com.jery.starrailhelper.activity.MainActivity.Companion.getAppContext
import com.jery.starrailhelper.data.CodeItem
import com.jery.starrailhelper.data.RewardItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date


object Utils {
    fun copyToClipboard(text: String) {
        val clipboard = getAppContext().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip!!)
        Snackbar.make(binding.root, "Copied to clipboard $text", LENGTH_SHORT).setAnimationMode(ANIMATION_MODE_SLIDE).show()
    }

    suspend fun fetchCodes(): Pair<List<CodeItem>, List<CodeItem>> = withContext(Dispatchers.IO) {
        val url = URL("https://honkai-star-rail.fandom.com/wiki/Redemption_Code")
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"
        val inputStream: InputStream = urlConnection.inputStream
        val response = inputStream.bufferedReader().use(BufferedReader::readText)
        val doc = Jsoup.parse(response)

        val ac = doc.select(".wikitable")[0]!!.select("tbody > tr:not(tr:first-child)")
        val codesTable = ac.map {
            CodeItem(
                code = it.children()[0].text().substringBefore('[').trim(),
                server = it.children()[1].text().trim(),
                rewards = parseRewards( it.children()[2].html() ),
                duration = parseDuration( it.children()[3].text() ),
                isExpired = it.children()[3].text().contains("Expired:")
            )
        }

        val (activeCodes, expiredCodes) = codesTable.partition { !it.isExpired }

        return@withContext Pair(activeCodes, expiredCodes)
    }
    private fun parseRewards(html: String): List<RewardItem> {
        val rewardsList = mutableListOf<RewardItem>()
        val doc = Jsoup.parse(html)
        for (item in doc.select("span.item")) {
            val name = item.select("span.item-text a").text()
            val amount = item.select("span.item-text").text().replace(name,"").replace(" ×","").toInt()
            val imageURL: String? = item.select("span.hidden > a > img").attr("data-src").ifEmpty { item.select("span.hidden > a > img").attr("src") }
            rewardsList.add(RewardItem(name, amount, imageURL))
        }
        return rewardsList
    }
    private fun parseDuration(duration: String): Pair<String, String> {
        return if (duration.contains("Valid until:")) {
            val discovered = duration.substringAfter("Discovered: ", "").substringBefore(" Valid until:")
            val validUntil = duration.substringAfter("Valid until: ", "").trim()
            discovered to validUntil
        } else {
            val discovered = duration.substringAfter("Discovered: ", "").substringBefore(" Expired:")
            val validUntil = duration.substringAfter("Expired: ", "").trim()
            discovered to validUntil
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun demoCodes(): Pair<List<CodeItem>, List<CodeItem>> {
        return Pair(
            listOf(
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "Unknown"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "Unknown"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "Unknown"),
            ),
            listOf(
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
                CodeItem("DEMOCODE1024", "All", listOf(RewardItem("Condensed Aether",3), RewardItem("Credit",10000)), "01-01-2023" to "01-01-2023"),
            )
        )
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showStackTrace(context: Context, e: Exception) {
        e.printStackTrace()
        val stackTrace = e.stackTraceToString()
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Stack Trace")
            builder.setMessage(stackTrace)
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.setNeutralButton("Copy") { _, _ -> copyToClipboard(stackTrace) }
            val dialog = builder.create()
            dialog.show()
        }
    }

    fun log(tag: String, message: String) {
        Log.d(tag, message)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date(System.currentTimeMillis())
        val formattedDate: String = formatter.format(date)
        val logString = "[$formattedDate]\t$tag:\t$message\n"
        val logFilePath = "/data/data/com.jery.starrailhelper/files/logs.txt"
        try {
            val outputStream = FileOutputStream(logFilePath, true) // Use append mode
            outputStream.write(logString.toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            Log.e(tag, "Error writing to log file: ${e.stackTraceToString()}")
        }
    }
}