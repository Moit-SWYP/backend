package pyws.swyp.auth.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 네이버 로그인 연결 끊기(unlink) 콜백 검증을 위한 암호화/서명 유틸리티
 *
 * <pre>
 * - AES128 / CBC / PKCS5Padding : encryptUniqueId 복호화
 * - HmacSHA256(HS256)           : 요청 위변조 방지 서명 검증
 *
 * 1. clientSecret → MD5 → 앞 16byte → AES/HMAC 공통 키 생성
 * 2. encryptUniqueId 복호화
 *    - base64url 디코딩
 *    - 앞 16byte = IV
 *    - 나머지 = AES128-CBC 암호문
 * 3. HMAC 서명 검증
 *    - "clientId=...&encryptUniqueId=...&timestamp=..." 문자열로 서명 생성
 * </pre>
 */
public class NaverUnlinkCrypto {

    private static final String ALGORITHM_AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM_AES = "AES";
    private static final String ALGORITHM_HS256 = "HmacSHA256";
    private static final int BLOCK_SIZE = 16;

    /**
     * 네이버 규격에 따른 암호화/서명 공통 키 생성
     *
     * <pre>
     * encryptKey (16byte) = md5(clientSecret)[0..15]
     *
     * - AES128-CBC encryptUniqueId 복호화 키
     * - HmacSHA256(HS256) 서명 검증 키
     *
     * ※ 네이버 규격상 AES와 HMAC 모두 동일한 키를 사용한다.
     * </pre>
     *
     * @param clientSecret 네이버에서 발급받은 Client Secret
     * @return 16바이트 AES/HMAC 공통 키
     */
    public static byte[] generateKey(String clientSecret) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(clientSecret.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOfRange(md.digest(), 0, BLOCK_SIZE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate MD5 key", e);
        }
    }

    /**
     * encryptUniqueId 복호화
     *
     * <pre>
     * encryptUniqueId = base64url( IV(16byte) + AES128-CBC(cipherText) )
     *
     * 1. base64url 디코딩
     * 2. 앞 16byte → IV
     * 3. 나머지 → 암호문
     * 4. AES128 / CBC / PKCS5Padding 방식으로 복호화
     * </pre>
     *
     * @param encrypted 네이버에서 전달한 암호화된 uniqueId
     * @param key       generateKey()로 생성한 16byte 키
     * @return 복호화된 네이버 사용자 uniqueId
     */
    public static String decryptUniqueId(String encrypted, byte[] key) {
        try {
            byte[] encryptedWithIv = Base64.getUrlDecoder().decode(encrypted);
            byte[] iv = Arrays.copyOfRange(encryptedWithIv, 0, BLOCK_SIZE);
            byte[] encryptedUniqueId = Arrays.copyOfRange(encryptedWithIv, BLOCK_SIZE, encryptedWithIv.length);

            SecretKeySpec skeySpec = new SecretKeySpec(key, ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance(ALGORITHM_AES_CBC_PKCS5);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

            byte[] decrypted = cipher.doFinal(encryptedUniqueId);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decrypt encryptUniqueId", e);
        }
    }

    /**
     * HMAC 서명 생성 (요청 위변조 검증용)
     *
     * <pre>
     * signature = base64url(
     *   HmacSHA256("clientId=...&encryptUniqueId=...&timestamp=...", key)
     * )
     * </pre>
     */
    public static String generateMac(String signatureBaseString, byte[] key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM_HS256);
            Mac mac = Mac.getInstance(ALGORITHM_HS256);
            mac.init(secretKey);

            byte[] hash = mac.doFinal(signatureBaseString.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate HMAC", e);
        }
    }

    /**
     * HMAC 서명 원문 생성
     *
     * <pre>
     * clientId={clientId}&encryptUniqueId={encryptUniqueId}&timestamp={timestamp}
     * </pre>
     */
    public static String signatureBaseString(String clientId, String encryptUniqueId, String timestamp) {
        String baseStringFmt = "clientId=%s&encryptUniqueId=%s&timestamp=%s";
        return String.format(baseStringFmt, clientId, encryptUniqueId, timestamp);
    }

    /**
     * 타이밍 공격 방지용 상수시간 비교 -> 모든 문자 비교
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) {
            return false;
        }

        // 한 번이라도 다르면 r은 0이 될 수 없음
        int r = 0;
        for (int i = 0; i < x.length; i++) {
            r |= (x[i] ^ y[i]);
        }
        return r == 0;
    }
}

