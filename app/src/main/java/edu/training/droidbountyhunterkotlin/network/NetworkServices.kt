package edu.training.droidbountyhunterkotlin.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

const val TAG = "Curso"

class NetworkServices {

    companion object {
        private const val ENDPOINT_FUGITIVOS = "http://3.13.226.218/droidBHServices.svc/fugitivos"
        private const val ENDPOINT_ATRAPADOS = "http://3.13.226.218/droidBHServices.svc/atrapados"
        private var JSONStr: String = ""
        private var tipo: SERVICE_TYPE = SERVICE_TYPE.FUGITIVOS
        private var codigo: Int = 0
        private var mensaje: String = ""
        private var error: String = ""

        suspend fun execute(param: String?, listener: OnTaskListener, uuid: String? = null) {
            val result = withContext(Dispatchers.IO) {
                execute(param, uuid)
            }
            if (result) {
                listener.tareaCompletada(JSONStr)
            } else {
                listener.tareaConError(codigo, mensaje, error)
            }
        }


        private fun execute(param: String?, uuid: String? = null): Boolean {
            val esFugitivo = param.equals("Fugitivos", true)
            tipo = if (esFugitivo) SERVICE_TYPE.FUGITIVOS else SERVICE_TYPE.ATRAPADOS
            var urlConnection: HttpURLConnection? = null
            try {
                urlConnection = getStructuredRequest(
                    tipo,
                    if (esFugitivo) ENDPOINT_FUGITIVOS else ENDPOINT_ATRAPADOS,
                    uuid ?: ""
                )
                val inputStream = urlConnection?.inputStream ?: return false
                val reader = BufferedReader(InputStreamReader(inputStream))
                val buffer = StringBuffer()
                do {
                    val line: String? = reader.readLine()
                    if (line != null) buffer.append(line).append("\n")
                } while (line != null)
                if (buffer.isEmpty()) return false
                JSONStr = buffer.toString()
                Log.d(TAG, "Respuesta del servidor: $JSONStr")
                return true
            } catch (exception: FileNotFoundException) {
                manageError(urlConnection)
                return false
            } catch (exception: IOException) {
                manageError(urlConnection)
                return false
            } catch (exception: Exception) {
                manageError(urlConnection)
                return false
            } finally {
                urlConnection?.disconnect()
            }
        }

        @Throws(IOException::class, JSONException::class)
        private fun getStructuredRequest(
            type: SERVICE_TYPE, endpoint: String, id:
            String
        ): HttpURLConnection {
            val TIME_OUT = 5000
            val urlConnection: HttpURLConnection
            val url: URL?
            if (type === SERVICE_TYPE.FUGITIVOS) { //-------- GET Fugitivos ----------
                url = URL(endpoint)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.setReadTimeout(TIME_OUT)
                urlConnection.setRequestMethod("GET")
                urlConnection.setRequestProperty("Content-Type", "application/json")
                urlConnection.connect()
            } else { //--------------------- POST Atrapados ------------------------
                url = URL(endpoint)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.setRequestMethod("POST")
                urlConnection.setReadTimeout(TIME_OUT)
                urlConnection.setRequestProperty("Content-Type", "application/json")
                urlConnection.setDoInput(true)
                urlConnection.setDoOutput(true)
                urlConnection.connect()
                val `object` = JSONObject()
                `object`.put("UDIDString", id)
                val dataOutputStream = DataOutputStream(urlConnection.getOutputStream())
                dataOutputStream.write(`object`.toString().toByteArray())
                dataOutputStream.flush()
                dataOutputStream.close()
            }
            Log.d(TAG, url.toString())
            return urlConnection
        }

        private fun manageError(urlConnection: HttpURLConnection?) {
            if (urlConnection != null) {
                try {
                    codigo = urlConnection.responseCode
                    if (urlConnection.errorStream != null) {
                        val inputStream = urlConnection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val buffer = StringBuffer()
                        do {
                            val line: String? = reader.readLine()
                            if (line != null) buffer.append(line).append("\n")
                        } while (line != null)
                        error = buffer.toString()
                    } else {
                        mensaje = urlConnection.responseMessage
                    }
                    error = urlConnection.errorStream.toString()
                    Log.e(TAG, "Error: ${error}, code: $codigo")
                } catch (e1: IOException) {
                    e1.printStackTrace()
                    Log.e(TAG, "Error")
                }
            } else {
                codigo = 105
                mensaje = "Error: No internet connection"
                Log.e(TAG, "code: $codigo, $mensaje")
            }
        }

    }

}


enum class SERVICE_TYPE {
    FUGITIVOS, ATRAPADOS
}


interface OnTaskListener {
    fun tareaCompletada(respuesta: String)
    fun tareaConError(codigo: Int, mensaje: String, error: String)
}