
/**
 * Base64 encode
 * @author miyabe
 */
public class Base64enc {

    /**
     * This array is a lookup table that translates 6-bit positive integer index values into their "Base64 Alphabet"
     * equivalents as specified in Table 1 of RFC 2045.
     * 
     * Thanks to "commons" project in ws.apache.org for this code.
     * http://svn.apache.org/repos/asf/webservices/commons/trunk/modules/util/
     */
    private static final byte[] STANDARD_ENCODE_TABLE = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };
    /**
     * Byte used to pad output.
     */
    private static final byte PAD = '=';;
    /** Mask used to extract 6 bits, used when encoding */
    private static final int MASK_6BITS = 0x3f;

    /**
     * 対象バイナリーデータのバイト長からエンコード後のバイト長を求める
     * @param binarylength 対象バイナリーデータのバイト長
     * @return  エンコード後のバイト長(終端0コード+1を含む)
     */
    public static int getencodelength(int binarylength){
    	return ((binarylength+2)/3)*4+1;
    }
    /**
     * バイナリーデータをBase64エンコード
     * @param in エンコード対象バイナリーデータ
     * @param buffer エンコード後の格納バッファ
     * @param in_length 対象バイナリーデータのバイト長
     * @return エンコードバイト長(終端0コード+1を含む)
     */
    public static int encode(byte in[], byte buffer[], int in_length){
    	int modulus=0, x=0, b, pos=0;
    	for(int i=1; i<=in_length; i++){
            modulus = i % 3;
    		b=in[i-1];
    		if (b<0) b += 256;
            x = (x << 8) + b;
            if (0 == modulus) {
                buffer[pos++] = STANDARD_ENCODE_TABLE[(x >> 18) & MASK_6BITS];
                buffer[pos++] = STANDARD_ENCODE_TABLE[(x >> 12) & MASK_6BITS];
                buffer[pos++] = STANDARD_ENCODE_TABLE[(x >> 6) & MASK_6BITS];
                buffer[pos++] = STANDARD_ENCODE_TABLE[x & MASK_6BITS];
                x=0;
            }
    	}
    	if (modulus==1){
            buffer[pos++] = STANDARD_ENCODE_TABLE[(x >> 2) & MASK_6BITS];
            buffer[pos++] = STANDARD_ENCODE_TABLE[(x << 4) & MASK_6BITS];
            buffer[pos++] = PAD;
            buffer[pos++] = PAD;
    	}
    	else if (modulus==2){
            buffer[pos++] = STANDARD_ENCODE_TABLE[(x >> 10) & MASK_6BITS];
            buffer[pos++] = STANDARD_ENCODE_TABLE[(x >> 4) & MASK_6BITS];
            buffer[pos++] = STANDARD_ENCODE_TABLE[(x << 2) & MASK_6BITS];
            buffer[pos++] = PAD;   		
    	}
    	if (pos>0)
    		buffer[pos++]=0;
    	return pos;
    }
}
