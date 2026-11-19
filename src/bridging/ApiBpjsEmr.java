package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import bridging.ApiBPJSEnc;
import bridging.ApiBPJSAesKeySpec;
import bridging.ApiBPJSLZString;

public class ApiBpjsEmr {

    private String cons_id, secretKey, userKey, baseUrl;
    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;

    private ObjectMapper mapper = new ObjectMapper();

    public ApiBpjsEmr() {
        try {
            cons_id  = koneksiDB.CONSIDBPJSEMR();
            secretKey= koneksiDB.SECRETKEYBPJSEMR();
            userKey  = koneksiDB.USERKEYBPJSEMR();
            baseUrl  = koneksiDB.URLBPJSEMR();
        } catch (Exception e) {
            System.out.println("Error config BPJS: " + e);
        }
    }

    // ==========================
    // 🔥 TIMESTAMP
    // ==========================
    public String getTimestamp() {
        long millis = System.currentTimeMillis() / 1000;
        return String.valueOf(millis);
    }

    // ==========================
    // 🔥 SIGNATURE HMAC SHA256
    // ==========================
    public String generateSignature(String timestamp) {
        try {
            String data = cons_id + "&" + timestamp;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secret);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            System.out.println("Error signature: " + e);
            return "";
        }
    }

    // ==========================
    // 🔥 SSL (trust all)
    // ==========================
    public RestTemplate getRest() {
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");

            TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            };

            sslContext.init(null, trustManagers, new SecureRandom());
            sslFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            scheme = new Scheme("https", 443, sslFactory);

            factory = new HttpComponentsClientHttpRequestFactory();
            factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);

            return new RestTemplate(factory);

        } catch (Exception e) {
            System.out.println("SSL Error: " + e);
            return new RestTemplate();
        }
    }

    // ==========================
    // 🔥 DECRYPT RESPONSE BPJS
    // ==========================
    public String decryptResponse(String encrypted, String timestamp) {
        try {
            String key = cons_id + secretKey + timestamp;

            ApiBPJSAesKeySpec aesKey = ApiBPJSEnc.generateKey(key);

            String decrypted = ApiBPJSEnc.decrypt(
                    encrypted,
                    aesKey.getKey(),
                    aesKey.getIv()
            );

            decrypted = ApiBPJSLZString.decompressFromEncodedURIComponent(decrypted);

            return decrypted;

        } catch (Exception e) {
            System.out.println("Decrypt Error: " + e);
            return null;
        }
    }

    // ==========================
    // 🔥 POST REQUEST + HANDLE RESPONSE
    // ==========================
    public String post(String endpoint, String jsonRequest) {
        try {

            String timestamp = getTimestamp();
            String signature = generateSignature(timestamp);

            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.add("X-cons-id", cons_id);
            headers.add("X-timestamp", timestamp);
            headers.add("X-signature", signature);
            headers.add("user_key", userKey);
            
            // 🔥 TAMBAHKAN DI SINI
            System.out.println("===== HEADER BPJS =====");
            headers.forEach((key, value) -> {
                System.out.println(key + " : " + value);
            });

            HttpEntity<String> request = new HttpEntity<>(jsonRequest, headers);

            System.out.println("===== REQUEST BPJS =====");
            System.out.println(jsonRequest);

            String finalUrl = baseUrl.endsWith("/") 
                ? baseUrl.substring(0, baseUrl.length()-1) + endpoint
                : baseUrl + endpoint;

                System.out.println("===== FINAL URL =====");
                System.out.println(finalUrl);

                String response = getRest().exchange(
                        finalUrl,
                        HttpMethod.POST,
                        request,
                        String.class
                ).getBody();

            if (response == null || response.trim().isEmpty()) {
                return "ERROR: Empty response from BPJS";
            }

            System.out.println("===== RAW RESPONSE BPJS =====");
            System.out.println(response);

            JsonNode root = mapper.readTree(response);

            String code = root.path("metaData").path("code").asText();
            String message = root.path("metaData").path("message").asText();

            System.out.println("CODE: " + code + " | MESSAGE: " + message);

            // ❌ ERROR
            if (!code.equals("200") && !code.equals("201")) {
                return "ERROR: " + message;
            }

            // 🔥 RESPONSE ENCRYPTED
            String encrypted = root.path("response").asText();

            System.out.println("TIMESTAMP: " + timestamp);
            System.out.println("ENCRYPTED: " + encrypted);

            if (encrypted == null || encrypted.equals("") || encrypted.equals("null")) {
                return "SUCCESS";
            }

            String decrypted = decryptResponse(encrypted, timestamp);

            if (decrypted == null || decrypted.trim().isEmpty()) {
                return "ERROR: Failed to decrypt response";
            }

            System.out.println("===== DECRYPTED =====");
            System.out.println(decrypted);

            return decrypted;

        } catch (Exception e) {
            System.out.println("POST ERROR: " + e);
            return "ERROR: " + e.getMessage();
        }
    }
    
    public String get(String endpoint) {
        try {

            String timestamp = getTimestamp();
            String signature = generateSignature(timestamp);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-cons-id", cons_id);
            headers.add("X-timestamp", timestamp);
            headers.add("X-signature", signature);
            headers.add("user_key", userKey);

            System.out.println("===== HEADER BPJS =====");
            headers.forEach((k, v) -> System.out.println(k + " : " + v));

            String finalUrl = baseUrl.endsWith("/") 
                ? baseUrl.substring(0, baseUrl.length()-1) + endpoint
                : baseUrl + endpoint;

            System.out.println("===== FINAL URL =====");
            System.out.println(finalUrl);

            HttpEntity<String> request = new HttpEntity<>(headers);

            String response = getRest().exchange(
                    finalUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            ).getBody();

            System.out.println("===== RAW RESPONSE BPJS =====");
            System.out.println(response);

            return response;

        } catch (Exception e) {
            System.out.println("GET ERROR: " + e);
            return "ERROR: " + e.getMessage();
        }
    }
}