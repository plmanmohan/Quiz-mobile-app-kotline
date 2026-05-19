import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quiz.app.Question

object JsonHelper {

    fun loadQuestions(
        context: Context,
        fileName: String
    ): List<Question> {

        val json =
            context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

        val type =
            object : TypeToken<List<Question>>() {}.type

        return Gson().fromJson(json, type)
    }
}