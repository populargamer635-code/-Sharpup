package com.example

import com.example.domain.MathQuestionGenerator
import com.example.model.DifficultyTier
import com.example.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MathQuestionGeneratorTest {

    @Test
    fun allModesAndTiersProduceValidQuestionsWithUniqueOptions() {
        val random = Random(42)
        for (mode in GameMode.entries) {
            for (tier in DifficultyTier.entries) {
                repeat(25) { index ->
                    val q = MathQuestionGenerator.generateQuestion(index, mode, tier, random)
                    assertEquals("Expected 4 options for $mode/$tier", 4, q.options.size)
                    assertEquals("Expected unique options for $mode/$tier", 4, q.options.distinct().size)
                    assertTrue(
                        "Correct answer ${q.correctAnswer} missing from ${q.options}",
                        q.correctAnswer in q.options
                    )
                    assertTrue("Options must be non-negative: ${q.options}", q.options.all { it >= 0 })
                    assertTrue("Main display must not be blank", q.mainDisplay.isNotBlank())
                }
            }
        }
    }
}
