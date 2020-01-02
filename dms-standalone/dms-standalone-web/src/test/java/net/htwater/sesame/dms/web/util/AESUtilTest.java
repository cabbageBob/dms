package net.htwater.sesame.dms.web.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jokki
 */
public class AESUtilTest {

    @Test
    public void testEncryptDecrypt() {
        String content = "bus@htwater";
        String encryptContent = AESUtil.encrypt(content, AESUtil.ENCODE_KEY);
        System.out.println(encryptContent);
        Assert.assertTrue(content.equals(AESUtil.decrypt(encryptContent, AESUtil.ENCODE_KEY)));
    }
}