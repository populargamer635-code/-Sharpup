package com.example.domain

import com.example.model.BrainQuestion
import com.example.model.DifficultyTier
import com.example.model.GameMode
import kotlin.math.abs
import kotlin.random.Random

object MathQuestionGenerator {

    fun generateQuestion(
        index: Int,
        mode: GameMode,
        tier: DifficultyTier,
        random: Random = Random.Default
    ): BrainQuestion {
        return when (mode) {
            GameMode.ARITHMETIC -> generateArithmetic(index, tier, allowVisual = true, random = random)
            GameMode.SPEED_SPRINT -> generateArithmetic(index, tier, allowVisual = false, random = random)
            GameMode.PATTERN_LOGIC -> generatePattern(index, tier, random = random)
            GameMode.MEMORY_BALANCE -> {
                if (index % 2 == 0) {
                    generateMemoryChain(index, tier, random)
                } else {
                    generateBalanceEquation(index, tier, random)
                }
            }
        }
    }

    private fun generateArithmetic(
        index: Int,
        tier: DifficultyTier,
        allowVisual: Boolean,
        random: Random
    ): BrainQuestion {
        val opChoice = when (tier) {
            DifficultyTier.EXPLORER -> random.nextInt(3) // +, -, easy *
            DifficultyTier.CHALLENGER -> random.nextInt(4) // +, -, *, /
            DifficultyTier.GENIUS -> random.nextInt(5) // +, -, *, /, 2-step
        }

        var a = 0
        var b = 0
        var op = "+"
        var answer = 0
        var mainDisplay = ""
        var explanation = ""
        var visualA: Int? = null
        var visualB: Int? = null
        var visualOp: String? = null

        when (tier) {
            DifficultyTier.EXPLORER -> {
                when (opChoice) {
                    0 -> {
                        a = random.nextInt(1, 12)
                        b = random.nextInt(1, 11)
                        op = "+"
                        answer = a + b
                        explanation = "$a + $b equals $answer!"
                    }
                    1 -> {
                        a = random.nextInt(4, 20)
                        b = random.nextInt(1, a)
                        op = "−"
                        answer = a - b
                        explanation = "Take $b away from $a to get $answer!"
                    }
                    else -> {
                        a = random.nextInt(2, 6)
                        b = listOf(2, 3, 5, 10).random(random)
                        op = "×"
                        answer = a * b
                        explanation = "$a groups of $b equals $answer!"
                    }
                }
                mainDisplay = "$a $op $b = ?"
                if (allowVisual && op != "×" && a <= 10 && b <= 10) {
                    visualA = a
                    visualB = b
                    visualOp = op
                }
            }

            DifficultyTier.CHALLENGER -> {
                when (opChoice) {
                    0 -> {
                        a = random.nextInt(12, 65)
                        b = random.nextInt(8, 45)
                        op = "+"
                        answer = a + b
                        explanation = "$a + $b = $answer"
                    }
                    1 -> {
                        a = random.nextInt(25, 99)
                        b = random.nextInt(7, a - 2)
                        op = "−"
                        answer = a - b
                        explanation = "$a − $b = $answer"
                    }
                    2 -> {
                        a = random.nextInt(3, 13)
                        b = random.nextInt(3, 13)
                        op = "×"
                        answer = a * b
                        explanation = "$a × $b = $answer"
                    }
                    else -> {
                        b = random.nextInt(2, 11)
                        answer = random.nextInt(2, 13)
                        a = b * answer
                        op = "÷"
                        explanation = "$a ÷ $b = $answer because $b × $answer = $a"
                    }
                }
                mainDisplay = "$a $op $b = ?"
            }

            DifficultyTier.GENIUS -> {
                when (opChoice) {
                    0 -> {
                        a = random.nextInt(45, 250)
                        b = random.nextInt(25, 180)
                        op = "+"
                        answer = a + b
                        explanation = "$a + $b = $answer"
                    }
                    1 -> {
                        a = random.nextInt(90, 320)
                        b = random.nextInt(24, a - 10)
                        op = "−"
                        answer = a - b
                        explanation = "$a − $b = $answer"
                    }
                    2 -> {
                        a = random.nextInt(11, 26)
                        b = random.nextInt(4, 10)
                        op = "×"
                        answer = a * b
                        explanation = "$a × $b = $answer"
                    }
                    3 -> {
                        b = random.nextInt(4, 15)
                        answer = random.nextInt(6, 20)
                        a = b * answer
                        op = "÷"
                        explanation = "$a ÷ $b = $answer"
                    }
                    else -> {
                        val x = random.nextInt(3, 12)
                        val y = random.nextInt(3, 10)
                        val z = random.nextInt(4, 20)
                        answer = x * y + z
                        mainDisplay = "($x × $y) + $z = ?"
                        explanation = "First $x × $y = ${x * y}, then + $z = $answer!"
                    }
                }
                if (mainDisplay.isEmpty()) {
                    mainDisplay = "$a $op $b = ?"
                }
            }
        }

        return BrainQuestion(
            id = index,
            mode = if (allowVisual) GameMode.ARITHMETIC else GameMode.SPEED_SPRINT,
            promptTop = if (allowVisual) "Solve the math challenge" else "Quick! Pick the answer",
            mainDisplay = mainDisplay,
            correctAnswer = answer,
            options = buildSmartOptions(answer, random),
            explanation = explanation,
            visualLeftCount = visualA,
            visualRightCount = visualB,
            visualOperator = visualOp
        )
    }

    private fun generatePattern(
        index: Int,
        tier: DifficultyTier,
        random: Random
    ): BrainQuestion {
        val patternType = when (tier) {
            DifficultyTier.EXPLORER -> random.nextInt(2) // step +, step -
            DifficultyTier.CHALLENGER -> random.nextInt(3) // step +, step -, double
            DifficultyTier.GENIUS -> random.nextInt(4) // step, double, alternating, squares/growing step
        }

        val length = 5
        val missingIdx = random.nextInt(1, length)
        val nums = mutableListOf<Int>()
        var ruleHint = ""

        when (patternType) {
            0 -> {
                val start = when (tier) {
                    DifficultyTier.EXPLORER -> random.nextInt(1, 12)
                    DifficultyTier.CHALLENGER -> random.nextInt(4, 30)
                    DifficultyTier.GENIUS -> random.nextInt(7, 45)
                }
                val step = when (tier) {
                    DifficultyTier.EXPLORER -> listOf(2, 3, 4, 5, 10).random(random)
                    DifficultyTier.CHALLENGER -> random.nextInt(4, 15)
                    DifficultyTier.GENIUS -> random.nextInt(8, 24)
                }
                for (i in 0 until length) nums.add(start + i * step)
                ruleHint = "Rule: Add $step each step!"
            }

            1 -> {
                val step = when (tier) {
                    DifficultyTier.EXPLORER -> listOf(2, 3, 5).random(random)
                    DifficultyTier.CHALLENGER -> random.nextInt(4, 12)
                    DifficultyTier.GENIUS -> random.nextInt(7, 19)
                }
                val start = step * (length + random.nextInt(1, 5))
                for (i in 0 until length) nums.add(start - i * step)
                ruleHint = "Rule: Subtract $step each step!"
            }

            2 -> {
                val start = listOf(1, 2, 3, 4, 5).random(random)
                val mult = if (tier == DifficultyTier.GENIUS) listOf(2, 3).random(random) else 2
                var cur = start
                for (i in 0 until length) {
                    nums.add(cur)
                    cur *= mult
                }
                ruleHint = "Rule: Multiply by $mult each time!"
            }

            else -> {
                // Growing difference: +2, +4, +6, +8 or +1, +2, +3, +4
                val start = random.nextInt(2, 12)
                val base = listOf(1, 2, 3).random(random)
                var cur = start
                for (i in 0 until length) {
                    nums.add(cur)
                    cur += base * (i + 1)
                }
                ruleHint = "Rule: The jump grows by +$base each step!"
            }
        }

        val answer = nums[missingIdx]
        val chips = nums.mapIndexed { idx, value -> if (idx == missingIdx) "?" else value.toString() }

        return BrainQuestion(
            id = index,
            mode = GameMode.PATTERN_LOGIC,
            promptTop = "Spot the hidden pattern! What replaces ?",
            mainDisplay = chips.joinToString("  •  "),
            correctAnswer = answer,
            options = buildSmartOptions(answer, random),
            explanation = "$ruleHint Missing number is $answer.",
            sequenceChips = chips
        )
    }

    private fun generateBalanceEquation(
        index: Int,
        tier: DifficultyTier,
        random: Random
    ): BrainQuestion {
        var display = ""
        var answer = 0
        var explanation = ""

        when (tier) {
            DifficultyTier.EXPLORER -> {
                val a = random.nextInt(2, 12)
                answer = random.nextInt(1, 11)
                val sum = a + answer
                if (random.nextBoolean()) {
                    display = "$a + ? = $sum"
                    explanation = "$a + $answer = $sum, so ? = $answer!"
                } else {
                    display = "$sum − ? = $a"
                    explanation = "$sum − $answer = $a, so ? = $answer!"
                }
            }

            DifficultyTier.CHALLENGER -> {
                if (random.nextBoolean()) {
                    val a = random.nextInt(2, 11)
                    answer = random.nextInt(2, 12)
                    val prod = a * answer
                    display = "? × $a = $prod"
                    explanation = "$answer × $a = $prod!"
                } else {
                    val a = random.nextInt(12, 40)
                    answer = random.nextInt(8, 35)
                    val sum = a + answer
                    display = "$a + ? = $sum"
                    explanation = "$sum − $a = $answer!"
                }
            }

            DifficultyTier.GENIUS -> {
                val mult = random.nextInt(2, 8)
                answer = random.nextInt(3, 15)
                val add = random.nextInt(4, 18)
                val total = mult * answer + add
                display = "($mult × ?) + $add = $total"
                explanation = "$total − $add = ${mult * answer}, and ${mult * answer} ÷ $mult = $answer!"
            }
        }

        return BrainQuestion(
            id = index,
            mode = GameMode.MEMORY_BALANCE,
            promptTop = "Balance the mystery equation!",
            mainDisplay = display,
            correctAnswer = answer,
            options = buildSmartOptions(answer, random),
            explanation = explanation
        )
    }

    private fun generateMemoryChain(
        index: Int,
        tier: DifficultyTier,
        random: Random
    ): BrainQuestion {
        val start = when (tier) {
            DifficultyTier.EXPLORER -> random.nextInt(3, 12)
            DifficultyTier.CHALLENGER -> random.nextInt(8, 25)
            DifficultyTier.GENIUS -> random.nextInt(10, 35)
        }

        val step1 = when (tier) {
            DifficultyTier.EXPLORER -> random.nextInt(2, 8)
            DifficultyTier.CHALLENGER -> random.nextInt(4, 15)
            DifficultyTier.GENIUS -> random.nextInt(6, 20)
        }

        val step2 = when (tier) {
            DifficultyTier.EXPLORER -> random.nextInt(1, (start + step1).coerceAtLeast(2))
            DifficultyTier.CHALLENGER -> random.nextInt(3, (start + step1 - 1).coerceAtLeast(4))
            DifficultyTier.GENIUS -> random.nextInt(2, 5)
        }

        val steps: List<String>
        val answer: Int
        val explanation: String

        if (tier == DifficultyTier.GENIUS) {
            val after1 = start + step1
            answer = after1 * step2
            steps = listOf("Start with $start", "Add +$step1", "Multiply ×$step2")
            explanation = "$start + $step1 = $after1, then $after1 × $step2 = $answer!"
        } else {
            val after1 = start + step1
            answer = after1 - step2
            steps = listOf("Start with $start", "Add +$step1", "Subtract −$step2")
            explanation = "$start + $step1 = $after1, then $after1 − $step2 = $answer!"
        }

        return BrainQuestion(
            id = index,
            mode = GameMode.MEMORY_BALANCE,
            promptTop = "Follow each brain step in your head!",
            mainDisplay = "What is the final number?",
            correctAnswer = answer,
            options = buildSmartOptions(answer, random),
            explanation = explanation,
            memorySteps = steps
        )
    }

    private fun buildSmartOptions(correct: Int, random: Random): List<Int> {
        val candidates = linkedSetOf(correct)
        val deltas = listOf(-1, 1, -2, 2, -3, 3, -5, 5, -10, 10)
        var attempts = 0
        while (candidates.size < 4 && attempts < 40) {
            attempts++
            val delta = deltas.random(random)
            val option = correct + delta
            if (option >= 0 && option != correct) {
                candidates.add(option)
            }
        }
        var fallback = 1
        while (candidates.size < 4) {
            val candidate = abs(correct) + fallback
            if (candidate != correct) candidates.add(candidate)
            fallback++
        }
        return candidates.shuffled(random)
    }
}
