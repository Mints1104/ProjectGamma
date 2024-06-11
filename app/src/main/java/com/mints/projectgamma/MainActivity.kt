package com.mints.projectgamma

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mints.projectgamma.api.ApiClient
import com.mints.projectgamma.api.ApiService
import com.mints.projectgamma.api.Invasion
import com.mints.projectgamma.mapping.DataMappings
import com.mints.projectgamma.ui.theme.ProjectGammaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.core.text.HtmlCompat

class MainActivity : ComponentActivity() {
    private lateinit var resultTextView: TextView
    private lateinit var allInvasions: List<Invasion>
    private lateinit var filteredInvasions: MutableList<Invasion>

    private lateinit var checkBoxGrunt: CheckBox
    private lateinit var checkBoxLeader: CheckBox
    private lateinit var checkBoxShowcase: CheckBox
    var hello = 1

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultTextView = findViewById(R.id.text_view_result)
        checkBoxGrunt = findViewById(R.id.checkBoxGrunt)
        checkBoxLeader = findViewById(R.id.checkBoxLeader)
        checkBoxShowcase = findViewById(R.id.checkBoxShowcase)

        val button: Button = findViewById(R.id.button_make_api_call)
        val buttonFilter: Button = findViewById(R.id.buttonApplyFilter)

        button.setOnClickListener {
            makeApiCall()
        }

        // Initialize allInvasions with an empty list
        allInvasions = emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeApiCall() {
        val apiService = ApiClient.retrofit.create(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getInvasions()
                withContext(Dispatchers.Main) {
                    handleSuccess(response.invasions)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleFailure(e)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatInvasionEndTime(invasionEnd: Long, timeZone: ZoneId = ZoneId.systemDefault()): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(timeZone)

        val formattedTime = formatter.format(Instant.ofEpochSecond(invasionEnd))
        val timeZoneId = timeZone.id

        return "$formattedTime Timezone:$timeZoneId"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSuccess(invasions: List<Invasion>) {
        filteredInvasions = mutableListOf()
        var gruntFlag = false

        val sortedInvasions = sortInvasionsByEndTime(invasions)

        if (sortedInvasions.isNotEmpty()) {
            val stringBuilder = StringBuilder()
            for (invasion in sortedInvasions) {
                if (DataMappings.characterNamesMap.containsKey(invasion.character)) {
                    if (checkBoxGrunt.isChecked && invasion.type == 1) {
                        filteredInvasions.add(invasion)
                        val link = "https://ipogo.app/?coords=${invasion.lat},${invasion.lng}"
                        val newEndTime = formatInvasionEndTime(invasion.invasion_end)
                        stringBuilder.append("Name: ${invasion.name}<br>")
                        stringBuilder.append("Location: <a href=\"$link\">Teleport</a><br>")
                        stringBuilder.append("Ending at: $newEndTime<br>")
                        stringBuilder.append("Character: ${invasion.characterName}<br>")
                        stringBuilder.append("Type: ${invasion.typeDescription}<br><br>")
                        gruntFlag = true
                    } else if ((checkBoxLeader.isChecked) && (invasion.type == 2 || invasion.type == 3)) {
                        filteredInvasions.add(invasion)
                        val link = "https://ipogo.app/?coords=${invasion.lat},${invasion.lng}"
                        val newEndTime = formatInvasionEndTime(invasion.invasion_end)
                        stringBuilder.append("Name: ${invasion.name}<br>")
                        stringBuilder.append("Location: <a href=\"$link\">Teleport</a><br>")
                        stringBuilder.append("Ending at: $newEndTime<br>")
                        stringBuilder.append("Character: ${invasion.characterName}<br>")
                        stringBuilder.append("Type: ${invasion.typeDescription}<br><br>")
                    }
                }
            }

            resultTextView.text = HtmlCompat.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            resultTextView.movementMethod = LinkMovementMethod.getInstance()
        } else {
            resultTextView.text = "No invasions found."
        }
    }

    private fun handleFailure(exception: Exception) {
        resultTextView.text = "API call failed: ${exception.message}"
    }

    private fun sortInvasionsByEndTime(invasions: List<Invasion>): List<Invasion> {
        return invasions.sortedByDescending { it.invasion_end }  // Sort by invasion end time in descending order
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        ProjectGammaTheme {
            Greeting("Android")
        }
    }
}
