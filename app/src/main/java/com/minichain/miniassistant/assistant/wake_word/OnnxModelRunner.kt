package com.minichain.miniassistant.assistant.wake_word


import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import android.content.res.AssetManager
import android.util.Log
import java.io.IOException
import java.nio.FloatBuffer
import java.util.Collections

class OnnxModelRunner(
  wakeWordModelBytes: ByteArray,
  private val embeddingModelBytes: ByteArray,
  private val melSpectrogramBytes: ByteArray
) {

  private var ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
  private var ortSession: OrtSession = ortEnvironment.createSession(wakeWordModelBytes)

  @Throws(OrtException::class, IOException::class)
  fun getMelSpectrogram(inputArray: FloatArray): Array<FloatArray> {
    val session: OrtSession = OrtEnvironment.getEnvironment().createSession(melSpectrogramBytes)
    lateinit var outputArray: Array<FloatArray>
    // Convert the input array to ONNX Tensor
    val floatBuffer = FloatBuffer.wrap(inputArray)
    val inputTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), floatBuffer, longArrayOf(BATCH_SIZE.toLong(), inputArray.size.toLong()))

    // Run the model
    // Adjust this based on the actual expected output shape
    try {
      session.run(Collections.singletonMap(session.inputNames.iterator().next(), inputTensor))?.use { results ->
        val outputTensor = results[0].value as Array<Array<Array<FloatArray>>>
        // Here you need to cast the output appropriately
        //            Object outputObject = outputTensor.getValue();

        // Check the actual type of 'outputObject' and cast accordingly
        // The following is an assumed cast based on your error message
        val squeezed = squeeze(outputTensor)
        outputArray = applyMelSpecTransform(squeezed)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      inputTensor?.close()
      session.close()
    }
    OrtEnvironment.getEnvironment().close()
    return outputArray
  }

  @Throws(OrtException::class, IOException::class)
  fun generateEmbeddings(input: Array<Array<Array<FloatArray>>>): Array<FloatArray>? {
    val environment = OrtEnvironment.getEnvironment()
    val session = environment.createSession(embeddingModelBytes)
    val inputTensor = OnnxTensor.createTensor(environment, input)
    try {
      session.run(Collections.singletonMap("input_1", inputTensor)).use { results ->
        // Extract the output tensor
        val rawOutput = results[0].value as Array<Array<Array<FloatArray>>>

        // Assuming the output shape is (41, 1, 1, 96), and we want to reshape it to (41, 96)
        val reshapedOutput = Array(rawOutput.size) { FloatArray(rawOutput[0][0][0].size) }
        for (i in rawOutput.indices) {
          System.arraycopy(rawOutput[i][0][0], 0, reshapedOutput[i], 0, rawOutput[i][0][0].size)
        }
        return reshapedOutput
      }
    } catch (e: Exception) {
      Log.d("exception", "not_predicted " + e.message)
    } finally {
      inputTensor?.close() // You're doing this, which is good.

      session?.close() // This should be added to ensure the session is also closed.
    }
    environment.close()
    return null
  }

  @Throws(OrtException::class)
  fun predictWakeWord(inputArray: Array<Array<FloatArray>>): Float {
    var result = arrayOfNulls<FloatArray>(0)
    var resultant = 0f
    var inputTensor: OnnxTensor? = null

    try {
      // Create a tensor from the input array
      inputTensor = OnnxTensor.createTensor(ortEnvironment, inputArray)
      // Run the inference
      val outputs = ortSession.run(Collections.singletonMap(ortSession.inputNames.iterator().next(), inputTensor))
      // Extract the output tensor, convert it to the desired type
      result = outputs[0].value as Array<FloatArray?>
      resultant = result[0]!![0]
    } catch (e: OrtException) {
      e.printStackTrace()
    } finally {
      inputTensor?.close()
      // Add this to ensure the session is properly closed.
    }
    return resultant
  }

  @Throws(IOException::class)
  private fun readModelFile(assetManager: AssetManager, filename: String): ByteArray {
    assetManager.open(filename).use { bytes ->
      val buffer = ByteArray(bytes.available())
      bytes.read(buffer)
      return buffer
    }
  }

  companion object {
    private const val BATCH_SIZE = 1 // Replace with your batch size

    fun squeeze(originalArray: Array<Array<Array<FloatArray>>>): Array<FloatArray> {
      val squeezedArray = Array(originalArray[0][0].size) { FloatArray(originalArray[0][0][0].size) }
      for (i in originalArray[0][0].indices) {
        for (j in originalArray[0][0][0].indices) {
          squeezedArray[i][j] = originalArray[0][0][i][j]
        }
      }

      return squeezedArray
    }

    fun applyMelSpecTransform(array: Array<FloatArray>): Array<FloatArray> {
      val transformedArray = Array(array.size) { FloatArray(array[0].size) }

      for (i in array.indices) {
        for (j in array[i].indices) {
          transformedArray[i][j] = array[i][j] / 10.0f + 2.0f
        }
      }

      return transformedArray
    }
  }
}
