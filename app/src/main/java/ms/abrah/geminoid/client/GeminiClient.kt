package ms.abrah.geminoid.client

import android.net.Uri
import android.util.Log
import org.apache.commons.io.IOUtils
import java.io.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class Response(
    val error: Boolean,
    val errorDescription: String?,
    val body: String?,
    val mimeType: String?
)

fun loadUrl(url: Uri?): Response {
    Log.d("debug", "In the load url function")
    val trustAllCerts = arrayOf<TrustManager>(object :
        X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? = null
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
    })

    // TODO(abrahmsj): Move this to non-blocking IO
    // https://examples.javacodegeeks.com/core-java/nio/java-nio-ssl-example/ as an example
    val ctx = SSLContext.getInstance("SSL")
    ctx.init(null, trustAllCerts, SecureRandom())
    val socket = ctx.socketFactory.createSocket("sunshinegardens.org", 1965) as SSLSocket
    socket.enabledProtocols = arrayOf("TLSv1.3")
    socket.enabledCipherSuites = arrayOf("TLS_AES_128_GCM_SHA256")

    val input: InputStream =
        BufferedInputStream(socket.inputStream)
    val output: OutputStream =
        BufferedOutputStream(socket.outputStream)
    val request = "${url}\r\n"
    Log.d("debug", "request: ${request}")
    output.write(request.toByteArray())
    output.flush()

    val writer = StringWriter()
    IOUtils.copy(input, writer, "utf-8")
    socket.close()
    return responseFromPayload(writer.toString())
}

private fun responseFromPayload(response: String): Response {
    val responseLines = response.split('\n')

    val header = responseLines.first()
    val headerParts = header.split(' ')
    val status = headerParts.first()
    val meta = headerParts.subList(1, headerParts.size)

    Log.d("debug", "Status code was: ${status}")
    when {
        status.startsWith("2") -> {
            val mimeType = meta.joinToString(" ")
            val body = responseLines.subList(1, responseLines.size).joinToString("\n")
            return Response(false, null, body, mimeType)
        }
        status.startsWith("3") -> {
            // TODO(abrahms): new URL may be relative. Figure out how to support that.
            val newUrl = Uri.parse(meta.first())
            return loadUrl(newUrl)
        }
        status.startsWith("4") -> {
            val errorMessage = meta.joinToString(" ")
            return Response(true, "Temporary Error: ${errorMessage}", null, null)
        }
        status.startsWith("5") -> {
            val errorMessage = meta.joinToString(" ")
            return Response(true, "Permanent Error: ${errorMessage}", null, null)
        }
        else -> {
            // 1x (input required) and 6x (client certificate required)
            return Response(true, "Unsupported status code (sorry)", null, null)
        }
    }
}