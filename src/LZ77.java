import java.util.ArrayList;

/**
 * Created by snuker on 12.01.17.
 */
class LZ77Exception extends Exception{

    LZ77Exception(String s){
        super(s);
    }

}
public class LZ77 {
    public static final int MAX_WINDOWS_SIZE=255;
    public static int unsignedToBytes(byte a)
    {
        int b = a & 0xFF;
        return b;
    }
    public static byte[] decompress(byte[] src) throws LZ77Exception {
        ArrayList<Byte> res=new ArrayList();
        for (int i=0;i<src.length;i+=3) {

            int start = unsignedToBytes(src[i]);
            if (start>MAX_WINDOWS_SIZE)
                throw new LZ77Exception("Corrupted archive");
            int copy = unsignedToBytes(src[i + 1]);
            byte v = src[i + 2];

            if (start != 0) {
                int old_s = res.size();
                for (int j = old_s - start; j < old_s + copy-start; j++)
                    res.add(res.get(j));


            }
            if (v==0) break;
            res.add(v);
        }


        byte[] ret=new byte[res.size()];
        for (int i=0;i<res.size();i++)
            ret[i] =res.get(i).byteValue();
        return ret;

    }
    public static byte[] compress(byte[] src) throws LZ77Exception {
        return compress( src,MAX_WINDOWS_SIZE);
    }
    public static byte[] compress(byte[] src,int windows_size) throws LZ77Exception {
        if (windows_size>MAX_WINDOWS_SIZE){
            throw new LZ77Exception("MAX_WINDOW_SIZE=".concat(String.valueOf(MAX_WINDOWS_SIZE)));
        }
        ArrayList<Byte> res=new ArrayList();
        for (int i=0;i<src.length;i++) {
            int step = 0;
            int sz = 0;
            for (int j = Math.max(0, i - windows_size); j < i; j++) {
                int ss = substr(src, j, i);
                if (ss > sz) {
                    sz = ss;
                    step = i - j;
                }
            }
            res.add((byte) step);
            res.add((byte) sz);
            res.add(i+sz<src.length?src[i + sz]:0);
            i += sz;
        }


        byte[] ret=new byte[res.size()];
        for (int i=0;i<res.size();i++)
            ret[i] =res.get(i).byteValue();
        return ret;
    }
    public static int substr(byte[]arr,int a, int b){
        int sz=0;
        while ((b<arr.length)&&(a<arr.length)&&(arr[a]==arr[b])){
            sz++;
            a++;
            b++;

        }
        return
                (sz>255) ? 255:sz;
    }
    public static void main(String args[]) throws LZ77Exception {

    }
}
