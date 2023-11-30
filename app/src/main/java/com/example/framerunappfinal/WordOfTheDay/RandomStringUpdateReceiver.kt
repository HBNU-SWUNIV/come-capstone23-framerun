package com.example.framerunappfinal.WordOfTheDay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.framerunappfinal.R
import java.util.*

class RandomStringUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val randomString = generateRandomString(context)
        val wordOfTodayActivity = context as WordOfThedayFragment
        wordOfTodayActivity.onReceiveRandomString(randomString)
    }
    private fun generateRandomString(context: Context): String {
        val adjectiveVerbPhrases = context.resources.getStringArray(R.array.adjective_verb_phrases)
        val nouns = context.resources.getStringArray(R.array.nouns)


        val randomPhrase1 = adjectiveVerbPhrases.random()
        val randomNoun1 = nouns.random()
        val randomPhrase2 = adjectiveVerbPhrases.random()
        val randomNoun2 = nouns.random()

        return "$randomPhrase1 $randomNoun1 $randomPhrase2 $randomNoun2"
    }

}
