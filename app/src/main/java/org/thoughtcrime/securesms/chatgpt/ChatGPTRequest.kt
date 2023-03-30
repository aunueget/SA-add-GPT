package org.thoughtcrime.securesms.chatgpt

import android.content.Context
import android.content.res.Resources
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import org.thoughtcrime.securesms.R
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.launchIn


@OptIn(BetaOpenAI::class)
class ChatGPTRequest constructor (val appContext: Context){

  private val context: Context = appContext
  private val resources: Resources
  private val apiKey: String
  private val chatAI: OpenAI
  var response: String
  // initializer block
  init {
    resources = context.resources
    apiKey = getAPIkey()
    chatAI = getOpenAI(apiKey.toString())
    response = ""
  }

  val resourcesPrefix = "src/nativeMain/resources"
  private fun getAPIkey(): String {
    //val sharedPref = context?.getSharedPreferences(resources.getString(R.string.chat_gpt_api_key),Context.MODE_PRIVATE) ?: return 123456789
    //val defaultValue = 123456789
    //return sharedPref.getString(resources.getString(R.string.chat_gpt_api_key), defaultValue)
    return "your-api-key-here"
  }
  private fun getOpenAI(apiKeyForChatGPT: String): OpenAI{
    val token = requireNotNull(apiKeyForChatGPT) { "OPENAI_API_KEY environment variable must be set." }
    //if(token=defaultValue){have user registar for chatGPT}
    return OpenAI(token = token)
  }
  public fun requestResponse(dialog: String) = runBlocking {
    launch {
      //println("\n> Create chat completions...")
      val chatCompletionRequest = ChatCompletionRequest(
        model = ModelId("gpt-3.5-turbo"),
        messages = listOf(
          ChatMessage(
            role = ChatRole.System,
            content = "Could you answer a question for me?."
          ),
          ChatMessage(
            role = ChatRole.User,
            content = dialog.replace("chatGPT","")
          )
        )
      )
      chatAI.chatCompletion(chatCompletionRequest).choices.forEach(::println)

      println("\n>️ Creating chat completions stream...")
      chatAI.chatCompletions(chatCompletionRequest)
        .onEach {
          print(it.choices.first().delta?.content.orEmpty())
          response+=it.choices.first().delta?.content.orEmpty()
        }
        .onCompletion {
          println()
          response=response.prependIndent(dialog.replace("chatGPT","")+'\n')
        }
        .launchIn(this)
        .join()
    }

  }

}