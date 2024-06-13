package com.mints.projectgamma

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import android.widget.Toast
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
    private lateinit var items: Array<String>
    private lateinit var selectedBooleanArray: BooleanArray
    private lateinit var selectedItemsTextView: TextView
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
    private var kecleonCheck = false
    private var allSelected = false






    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val showMultiSelectDialogButton: Button = findViewById(R.id.showMultiSelectDialogButton)
         selectedItemsTextView  = findViewById(R.id.selectedItemsTextView)
        resultTextView = findViewById(R.id.text_view_result)
        val button: Button = findViewById(R.id.button_make_api_call)
        val selectAllButton : Button = findViewById(R.id.select_deselect)
        visitedInvasions = mutableListOf()

         items = arrayOf(
            "Cliff","Arlo","Sierra","Giovanni","Dragon Female", "Dark Female",
            "Bug Male","Fairy Female", "Fighting Female", "Fire Female", "Flying Female",
            "Ghost","Grass Male","Ground Male","Ice Female","Normal Male","Poison Female",
            "Psychic Male","Rock Male","Steel Male","Water Female","Water Male",
            "Electric","Typeless Female","Typeless Male","Showcase", "Kecleon"
        )

        // Initialize selectedItems excluding "Cliff", "Arlo", "Sierra", "Giovanni", "Showcase"
        selectedItems = arrayOf(
            "Dragon Female", "Dark Female",
            "Bug Male","Fairy Female", "Fighting Female", "Fire Female", "Flying Female",
            "Ghost","Grass Male","Ground Male","Ice Female","Normal Male","Poison Female",
            "Psychic Male","Rock Male","Steel Male","Water Female","Water Male",
            "Electric","Typeless Female","Typeless Male"
        ).toMutableList()
        // Initialize selectedBooleanArray based on selectedItems
         selectedBooleanArray = BooleanArray(items.size) { index ->
            when (items[index]) {
                "Cliff", "Arlo", "Sierra", "Giovanni", "Showcase" -> false
                else -> true // or set to false if you want none to be checked by default
            }
        }

        selectedItemsTextView.text = "Selected items: ${selectedItems.joinToString(", ")}"


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

                if(selectedItems.containsAll(items.toList())) {
                    selectedItemsTextView.text = "Selected items: All"
                    newFilter()

                } else {
                    selectedItemsTextView.text = "Selected items: ${selectedItems.joinToString(", ")}"
                    newFilter()
                }



            }
            builder.setNegativeButton("Cancel", null)
            builder.show()




        }

        // Example makeApiCall and button click handling
        makeApiCall()
        button.setOnClickListener {
            makeApiCall()
        }

        selectAllButton.setOnClickListener {
            selectAll()
        }

        allInvasions = emptyList()
    }





    private fun selectAll() {
        if (!allSelected) {
            // Select All
            selectedBooleanArray.fill(true)
            selectedItems.clear()
            selectedItems.addAll(items)
            selectedItemsTextView.text = "Selected items: All"
        } else {
            // Deselect All
            selectedBooleanArray.fill(false)
            selectedItems.clear()
            selectedItemsTextView.text = "Selected items: None"
        }
        allSelected = !allSelected
        newFilter()
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
        kecleonCheck = false

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
                "Kecleon" -> kecleonCheck = true
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
                "Kecleon" -> kecleonCheck
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

        if (!visitedInvasions.contains(invasion)) {
            val link = "https://ipogo.app/?coords=${invasion.lat},${invasion.lng}"
            val newEndTime = formatInvasionEndTime(invasion.invasion_end)

            val teleportText = "Teleport"
            val copyText = "Copy"
            val deleteText = "Delete"
            val invasionText =
                "Name: ${invasion.name}\nLocation: $teleportText | $copyText | $deleteText\nEnding at: $newEndTime\nCharacter: ${invasion.characterName}\nType: ${invasion.typeDescription}\n\n"

            val start = stringBuilder.length
            stringBuilder.append(invasionText)
            val end = stringBuilder.length

            val spanStartTeleport = start + "Name: ${invasion.name}\nLocation: ".length
            val spanEndTeleport = spanStartTeleport + teleportText.length

            val spanStartCopy = spanEndTeleport + " | ".length
            val spanEndCopy = spanStartCopy + copyText.length

            val spanStartDelete = spanEndCopy + " | ".length
            val spanEndDelete = spanStartDelete + deleteText.length



            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Open the link
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    widget.context.startActivity(intent)

                    filteredInvasions.removeAt(index)
                    visitedInvasions.add(invasion)
                    updateResultTextView()
                }
            }, spanStartTeleport, spanEndTeleport, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Copy the coordinates to clipboard
                    val clipboard = widget.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Coordinates", "${invasion.lat},${invasion.lng}")
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(widget.context, "Coordinates copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            }, spanStartCopy, spanEndCopy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            stringBuilder.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    filteredInvasions.removeAt(index)
                    visitedInvasions.add(invasion)
                    updateResultTextView()

                }
            }, spanStartDelete, spanEndDelete, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
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
