import java.util.ArrayList;

/**
 * Created by snuker on 15.01.17.
 */
public class RLE {
    public static byte[] encode(byte[] src){
        int x=0;
        ArrayList<Byte> res=new ArrayList();
        for (int i=0;i<src.length;i++){
            if (i+1<src.length&&x<256&&src[i]==src[i+1]){
                x++;
            }
            else{
                res.add(src[i]);
                res.add((byte)((x==0)? 0:x-1));
                x=0;
            }
        }
        byte[] b=new byte[res.size()];
        for (int i=0;i<b.length;i++)
            b[i]=(byte)res.get(i);

        return  b;
    }
    public static byte[] decode(byte src[]){
        ArrayList res=new ArrayList();
        for (int i=0;i<src.length;i+=2){
            for (int j=0;j<src[i+1]+1;j++){
                res.add(src[i]);
            }
        }
        byte[] b=new byte[res.size()];
        for (int i=0;i<b.length;i++)
            b[i]=(byte)res.get(i);
        return b;
    }
}
