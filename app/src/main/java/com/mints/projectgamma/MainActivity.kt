package com.mints.projectgamma

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.mints.projectgamma.api.ApiClient
import com.mints.projectgamma.api.ApiService
import com.mints.projectgamma.api.Invasion
import com.mints.projectgamma.mapping.DataMappings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private lateinit var resultTextView: TextView
    private lateinit var allInvasions: List<Invasion>
    private lateinit var filteredInvasions: MutableList<Invasion>
    private lateinit var selectedItems: MutableList<String>
    private lateinit var visitedInvasions: MutableList<Invasion>

    private var cliffCheck = false
    private var arloCheck = false
    private var sierraCheck = false
    private var giovanniCheck = false
    private var dragonFemaleCheck = true
    private var darkFemaleCheck = true
    private var bugMaleCheck = true
    private var fairyFemaleCheck = true
    private var fightingFemaleCheck = true
    private var fireFemaleCheck = true
    private var flyingFemaleCheck = true
    private var ghostCheck = true
    private var grassMaleCheck = true
    private var groundMaleCheck = true
    private var iceFemaleCheck = true
    private var normalMaleCheck = true
    private var poisonFemaleCheck = true
    private var psychicMaleCheck = true
    private var rockMaleCheck = true
    private var metalMaleCheck = true
    private var waterFemaleCheck = true
    private var waterMaleCheck = true
    private var electricCheck = true
    private var typelessFemaleCheck = true
    private var typelessMaleCheck = true
    private var showcaseCheck = false





    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val showMultiSelectDialogButton: Button = findViewById(R.id.showMultiSelectDialogButton)
        val selectedItemsTextView: TextView = findViewById(R.id.selectedItemsTextView)
        resultTextView = findViewById(R.id.text_view_result)
        val button: Button = findViewById(R.id.button_make_api_call)
        val items = arrayOf("Cliff","Arlo","Sierra","Giovanni","Dragon Female", "Dark Female",
            "Bug Male","Fairy Female", "Fighting Female", "Fire Female", "Flying Female",
            "Ghost","Grass Male","Ground Male","Ice Female","Normal Male","Poison Female",
            "Psychic Male","Rock Male","Steel Male","Water Female","Water Male",
            "Electric","Typeless Female","Typeless Male","Showcase")
        selectedItems = items.toMutableList()
        val selectedBooleanArray = BooleanArray(items.size) { true }

        showMultiSelectDialogButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Items")
            builder.setMultiChoiceItems(items, selectedBooleanArray) { _, which, isChecked ->
                if (isChecked) {
                    selectedItems.add(items[which])
                } else {
                    selectedItems.remove(items[which])
                }
                selectedBooleanArray[which] = isChecked
            }
            builder.setPositiveButton("OK") { _, _ ->
                selectedItemsTextView.text = "Selected items: ${selectedItems.joinToString(", ")}"
                newFilter()
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }


        makeApiCall()
        button.setOnClickListener {
            makeApiCall()
        }

        allInvasions = emptyList()
    }



    private fun newFilter() {
        cliffCheck = false
        arloCheck = false
        sierraCheck = false
        giovanniCheck = false
        dragonFemaleCheck = false
        darkFemaleCheck = false
        bugMaleCheck = false
        fairyFemaleCheck = false
        fightingFemaleCheck = false
        fireFemaleCheck = false
        flyingFemaleCheck = false
        ghostCheck = false
        grassMaleCheck = false
        groundMaleCheck = false
        iceFemaleCheck = false
        normalMaleCheck = false
        poisonFemaleCheck = false
        psychicMaleCheck = false
        rockMaleCheck = false
        metalMaleCheck = false
        waterFemaleCheck = false
        waterMaleCheck = false
        electricCheck = false
        typelessFemaleCheck = false
        typelessMaleCheck = false
        showcaseCheck = false

        selectedItems.forEach { item ->
            when (item) {
                "Cliff" -> cliffCheck = true
                "Arlo" -> arloCheck = true
                "Sierra" -> sierraCheck = true
                "Giovanni" -> giovanniCheck = true
                "Dragon Female" -> dragonFemaleCheck = true
                "Dark Female" -> darkFemaleCheck = true
                "Bug Male" -> bugMaleCheck = true
                "Fairy Female" -> fairyFemaleCheck = true
                "Fighting Female" -> fightingFemaleCheck = true
                "Fire Female" -> fireFemaleCheck = true
                "Flying Female" -> flyingFemaleCheck = true
                "Ghost" -> ghostCheck = true
                "Grass Male" -> grassMaleCheck = true
                "Ground Male" -> groundMaleCheck = true
                "Ice Female" -> iceFemaleCheck = true
                "Normal Male" -> normalMaleCheck = true
                "Poison Female" -> poisonFemaleCheck = true
                "Psychic Male" -> psychicMaleCheck = true
                "Rock Male" -> rockMaleCheck = true
                "Metal Male" -> metalMaleCheck = true
                "Water Female" -> waterFemaleCheck = true
                "Water Male" -> waterMaleCheck = true
                "Electric" -> electricCheck = true
                "Typeless Female" -> typelessFemaleCheck = true
                "Typeless Male" -> typelessMaleCheck = true
                "Showcase" -> showcaseCheck = true
            }
        }
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
        var counter = 0
        val maxAtOnce = 200
        val sortedInvasions = sortInvasionsByEndTime(invasions)

        for (invasion in sortedInvasions) {
            if (counter >= maxAtOnce) {
                break
            }

            val characterName = DataMappings.characterNamesMap[invasion.character] ?: continue

            val isSelectedCharacter = when (characterName) {
                "Cliff" -> cliffCheck
                "Arlo" -> arloCheck
                "Sierra" -> sierraCheck
                "Giovanni" -> giovanniCheck
                "Dragon Female" -> dragonFemaleCheck
                "Dark Female" -> darkFemaleCheck
                "Bug Male" -> bugMaleCheck
                "Fairy Female" -> fairyFemaleCheck
                "Fighting Female" -> fightingFemaleCheck
                "Fire Female" -> fireFemaleCheck
                "Flying Female" -> flyingFemaleCheck
                "Ghost" -> ghostCheck
                "Grass Male" -> grassMaleCheck
                "Ground Male" -> groundMaleCheck
                "Ice Female" -> iceFemaleCheck
                "Normal Male" -> normalMaleCheck
                "Poison Female" -> poisonFemaleCheck
                "Psychic Male" -> psychicMaleCheck
                "Rock Male" -> rockMaleCheck
                "Metal Male" -> metalMaleCheck
                "Water Female" -> waterFemaleCheck
                "Water Male" -> waterMaleCheck
                "Electric" -> electricCheck
                "Typeless Female" -> typelessFemaleCheck
                "Typeless Male" -> typelessMaleCheck
                "Showcase" -> showcaseCheck
                else -> false
            }

            if (isSelectedCharacter) {
                filteredInvasions.add(invasion)
                counter += 1
            }
        }

        updateResultTextView()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun postData(invasion: Invasion, stringBuilder: SpannableStringBuilder, index: Int) {
        visitedInvasions = mutableListOf()
        val link = "https://ipogo.app/?coords=${invasion.lat},${invasion.lng}"
        val newEndTime = formatInvasionEndTime(invasion.invasion_end)

        val teleportText = "Teleport"
        val invasionText = "Name: ${invasion.name}\nLocation: $teleportText\nEnding at: $newEndTime\nCharacter: ${invasion.characterName}\nType: ${invasion.typeDescription}\n\n"

        val start = stringBuilder.length
        stringBuilder.append(invasionText)
        val end = stringBuilder.length

        val spanStart = start + "Name: ${invasion.name}\nLocation: ".length
        val spanEnd = spanStart + teleportText.length

        stringBuilder.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Open the link
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                widget.context.startActivity(intent)

                filteredInvasions.removeAt(index)
                visitedInvasions.add(invasion)
                updateResultTextView()
            }
        }, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateResultTextView() {
        val stringBuilder = SpannableStringBuilder()
        for ((index, invasion) in filteredInvasions.withIndex()) {
            postData(invasion, stringBuilder, index)
        }
        resultTextView.text = stringBuilder
        resultTextView.movementMethod = LinkMovementMethod.getInstance()
    }



    private fun handleFailure(exception: Exception) {
        resultTextView.text = "API call failed: ${exception.message}"
    }


    private fun sortInvasionsByEndTime(invasions: List<Invasion>): List<Invasion> {
        return invasions.sortedByDescending { it.invasion_end }
    }


}
