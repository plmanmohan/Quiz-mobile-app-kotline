import android.content.Context
import com.quiz.app.Question

object DailyPracticeGenerator {

    fun generateQuestions(
        context: Context
    ): List<Question> {

        val englishFiles = listOf(
            "daily_en_active.json",
            "daily_en_tense.json",
            "daily_en_antony.json",
            "daily_en_sentence.json",
            "daily_en_spelling.json",
            "daily_en_idiom.json",

            )

        val mathFiles = listOf(
            "daily_math_fraction.json",
            "daily_math_percentage.json",
            "daily_math_series.json",
            "daily_math_word.json",
            "daily_math_geometry.json",
            "daily_math_proportion.json",
            "daily_math_exercise.json",
            "daily_math_table.json",
        )

        val englishQuestions =
            englishFiles
                .flatMap {
                    JsonHelper.loadQuestions(context, it)
                }
                .shuffled()
                .take(10)

        val mathQuestions =
            mathFiles
                .flatMap {
                    JsonHelper.loadQuestions(context, it)
                }
                .shuffled()
                .take(10)

        return (
                englishQuestions +
                        mathQuestions
                ).shuffled()
    }
}