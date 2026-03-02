package com.minichain.miniassistant.assistant.wake_word


import ai.onnxruntime.OrtException
import java.io.IOException
import java.util.ArrayDeque
import java.util.Random
import kotlin.math.max

class OpenWakeWordModel(
  private val modelRunner: OnnxModelRunner
) {

  private var nPreparedSamples: Int = 1280
  private val sampleRate: Int = 16000
  private val melSpectrogramMaxLength: Int = 10 * 97
  private val featureBufferMaxLength: Int = 120

  private var featureBuffer: Array<FloatArray> = emptyArray()
  private var rawDataBuffer: ArrayDeque<Float> = ArrayDeque(sampleRate * 10)
  private var rawDataRemainder: FloatArray = FloatArray(0)
  private var melSpectrogramBuffer: Array<FloatArray> =
    Array(76) { FloatArray(32) }.apply {
      for (i in indices) {
        for (j in this@apply[i].indices) {
          this@apply[i][j] = 1.0f // Assign 1.0f to simulate numpy.ones
        }
      }
    }

  private var accumulatedSamples: Int = 0

  init {
    try {
      getEmbeddings(generateRandomIntArray(16000 * 4), 76, 8)?.let {
        featureBuffer = it
      }
    } catch (e: Exception) {
      print(e.message)
    }
  }

  private fun getFeatures(
    nFeatureFrames: Int,
    initialStartNdx: Int
  ): Array<Array<FloatArray>> {
    var startNdx = initialStartNdx
    val endNdx: Int
    if (startNdx != -1) {
      endNdx = if (startNdx + nFeatureFrames != 0) (startNdx + nFeatureFrames) else featureBuffer.size
    } else {
      startNdx = max(0.0, (featureBuffer.size - nFeatureFrames).toDouble()).toInt() // Ensure startNdx is not negative
      endNdx = featureBuffer.size
    }

    val length = endNdx - startNdx
    val result = Array(1) { Array(length) { FloatArray(featureBuffer[0].size) } }  // Assuming the second dimension has fixed size.

    for (i in 0..<length) {
      System.arraycopy(featureBuffer[startNdx + i], 0, result[0][i], 0, featureBuffer[startNdx + i].size)
    }

    return result
  }

  // Java equivalent to _get_embeddings method
  @Throws(OrtException::class, IOException::class)
  private fun getEmbeddings(
    x: FloatArray,
    windowSize: Int,
    stepSize: Int
  ): Array<FloatArray>? {
    val spec = modelRunner.getMelSpectrogram(x) // Assuming this method exists and returns float[][]
    val windows = ArrayList<Array<FloatArray>>()

    run {
      var i = 0
      while (i <= spec.size - windowSize) {
        val window = Array(windowSize) { FloatArray(spec[0].size) }

        for (j in 0..<windowSize) {
          System.arraycopy(spec[i + j], 0, window[j], 0, spec[0].size)
        }

        // Check if the window is full-sized (not truncated)
        if (window.size == windowSize) {
          windows.add(window)
        }
        i += stepSize
      }
    }

    // Convert ArrayList to array and add the required extra dimension
    val batch = Array(windows.size) { Array(windowSize) { Array(spec[0].size) { FloatArray(1) } } }
    for (i in windows.indices) {
      for (j in 0..<windowSize) {
        for (k in spec[0].indices) {
          batch[i][j][k][0] = windows[i][j][k] // Add the extra dimension here
        }
      }
    }

    try {
      return modelRunner.generateEmbeddings(batch)
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
    // Assuming embeddingModelPredict is defined and returns float[][]
  }

  // Utility function to generate random int array, equivalent to np.random.randint
  private fun generateRandomIntArray(size: Int): FloatArray {
    val arr = FloatArray(size)
    val random = Random()
    for (i in 0..<size) {
      arr[i] = random.nextInt(2000).toFloat() - 1000 // range [-1000, 1000)
    }
    return arr
  }

  private fun bufferRawData(x: FloatArray?) { // Change double[] to match your actual data type
    // Check if input x is not null
    if (x != null) {
      // Check if raw_data_buffer has enough space, if not, remove old data
      while (rawDataBuffer.size + x.size > sampleRate * 10) {
        rawDataBuffer.poll() // or pollFirst() - removes and returns the first element of this deque
      }
      for (value in x) {
        rawDataBuffer.offer(value) // or offerLast() - Inserts the specified element at the end of this deque
      }
    }
  }

  private fun streamingMelSpectrogram(nSamples: Int) {
    require(rawDataBuffer.size >= 400) { "The number of input frames must be at least 400 samples @ 16kHz (25 ms)!" }

    // Converting the last n_samples + 480 (3 * 160) samples from raw_data_buffer to an ArrayList
    val tempArray = FloatArray(nSamples + 480) // 160 * 3 = 480
    val rawDataArray: Array<Any> = rawDataBuffer.toTypedArray()
    for (i in max(0, (rawDataArray.size - nSamples - 480)) until rawDataArray.size) {
      tempArray[i - max(0, (rawDataArray.size - nSamples - 480))] = rawDataArray[i] as Float
    }

    // Assuming getMelSpectrogram returns a two-dimensional float array
    val newMelSpectrogram: Array<FloatArray>
    try {
      newMelSpectrogram = modelRunner.getMelSpectrogram(tempArray)
    } catch (e: OrtException) {
      throw RuntimeException(e)
    } catch (e: IOException) {
      throw RuntimeException(e)
    }

    val combined = Array(melSpectrogramBuffer.size + newMelSpectrogram.size) { floatArrayOf() }

    System.arraycopy(melSpectrogramBuffer, 0, combined, 0, melSpectrogramBuffer.size)
    System.arraycopy(newMelSpectrogram, 0, combined, melSpectrogramBuffer.size, newMelSpectrogram.size)
    melSpectrogramBuffer = combined

    if (melSpectrogramBuffer.size > melSpectrogramMaxLength) {
      val trimmed = Array(melSpectrogramMaxLength) { floatArrayOf() }
      System.arraycopy(melSpectrogramBuffer, melSpectrogramBuffer.size - melSpectrogramMaxLength, trimmed, 0, melSpectrogramMaxLength)
      melSpectrogramBuffer = trimmed
    }
  }

  private fun streamingFeatures(audiobuffer: FloatArray): Int {
    var audiobuffer = audiobuffer
    var processed_samples = 0
    this.accumulatedSamples = 0
    if (rawDataRemainder.size != 0) {
      // Create a new array to hold the result of concatenation
      val concatenatedArray = FloatArray(rawDataRemainder.size + audiobuffer.size)

      // Copy elements from raw_data_remainder to the new array
      System.arraycopy(rawDataRemainder, 0, concatenatedArray, 0, rawDataRemainder.size)

      // Copy elements from x to the new array, starting right after the last element of raw_data_remainder
      System.arraycopy(audiobuffer, 0, concatenatedArray, rawDataRemainder.size, audiobuffer.size)

      // Assign the concatenated array back to x
      audiobuffer = concatenatedArray

      // Reset raw_data_remainder to an empty array
      rawDataRemainder = FloatArray(0)
    }

    if (this.accumulatedSamples + audiobuffer.size >= 1280) {
      val remainder = (this.accumulatedSamples + audiobuffer.size) % 1280
      if (remainder != 0) {
        // Create an array for x_even_chunks that excludes the last 'remainder' elements of 'x'
        val xEvenChunks = FloatArray(audiobuffer.size - remainder)
        System.arraycopy(audiobuffer, 0, xEvenChunks, 0, audiobuffer.size - remainder)

        // Buffer the even chunks of data
        this.bufferRawData(xEvenChunks)

        // Update accumulated_samples by the length of x_even_chunks
        this.accumulatedSamples += xEvenChunks.size

        // Set raw_data_remainder to the last 'remainder' elements of 'x'
        this.rawDataRemainder = FloatArray(remainder)
        System.arraycopy(audiobuffer, audiobuffer.size - remainder, this.rawDataRemainder, 0, remainder)
      } else {
        // Buffer the entire array 'x'
        this.bufferRawData(audiobuffer)

        // Update accumulated_samples by the length of 'x'
        this.accumulatedSamples += audiobuffer.size

        // Set raw_data_remainder to an empty array
        this.rawDataRemainder = FloatArray(0)
      }
    } else {
      this.accumulatedSamples += audiobuffer.size
      this.bufferRawData(audiobuffer) // Adapt this method according to your class
    }

    if (this.accumulatedSamples >= 1280 && this.accumulatedSamples % 1280 == 0) {
      this.streamingMelSpectrogram(this.accumulatedSamples)

      val x = Array(1) { Array(76) { Array(32) { FloatArray(1) } } }

      for (i in (accumulatedSamples / 1280) - 1 downTo 0) {
        var ndx = -8 * i
        if (ndx == 0) {
          ndx = melSpectrogramBuffer.size
        }
        // Calculate start and end indices for slicing
        val start = max(0.0, (ndx - 76).toDouble()).toInt()
        val end = ndx

        var j = start
        var k = 0
        while (j < end) {
          for (w in 0..31) {
            x[0][k][w][0] = melSpectrogramBuffer[j][w]
          }
          j++
          k++
        }
        if (x[0].size == 76) {
          try {
            val newFeatures = modelRunner.generateEmbeddings(x)
            newFeatures?.let {
              val totalRows = featureBuffer.size + newFeatures.size
              val numColumns = featureBuffer[0].size // Assuming all rows have the same length
              val updatedBuffer = Array(totalRows) { FloatArray(numColumns) }

              // Copy original featureBuffer into updatedBuffer
              for (l in featureBuffer.indices) {
                System.arraycopy(featureBuffer[l], 0, updatedBuffer[l], 0, featureBuffer[l].size)
              }

              // Copy newFeatures into the updatedBuffer, starting after the last original row
              for (k in newFeatures.indices) {
                System.arraycopy(newFeatures[k], 0, updatedBuffer[k + featureBuffer.size], 0, newFeatures[k].size)
              }

              featureBuffer = updatedBuffer
            }
          } catch (e: Exception) {
            throw RuntimeException(e)
          }
        }
      }
      processed_samples = this.accumulatedSamples
      this.accumulatedSamples = 0
    }
    if (featureBuffer.size > featureBufferMaxLength) {
      val trimmedFeatureBuffer = Array(featureBufferMaxLength) { FloatArray(featureBuffer[0].size) }

      // Copy the last featureBufferMaxLen rows of featureBuffer into trimmedFeatureBuffer
      for (i in 0..<featureBufferMaxLength) {
        trimmedFeatureBuffer[i] = featureBuffer[featureBuffer.size - featureBufferMaxLength + i]
      }

      // Update featureBuffer to point to the new trimmedFeatureBuffer
      featureBuffer = trimmedFeatureBuffer
    }
    return if (processed_samples != 0) processed_samples else this.accumulatedSamples
  }

  fun predictWakeWord(audioBuffer: FloatArray): Float {
    nPreparedSamples = streamingFeatures(audioBuffer)
    val res = getFeatures(16, -1)
    var result = 0f
    try {
      result = modelRunner.predictWakeWord(res)
    } catch (e: OrtException) {
      throw RuntimeException(e)
    }
    return result
  }
}
