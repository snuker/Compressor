import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

/**
 * Created by snuker on 14.01.17.
 */


//Dynamic length codes stuff

class LZWException extends  Exception{
    public LZWException(String s){
        super(s);
    }
}
class DLEWriter {
    final static int MAX_CODE=32768;
    ArrayList<Byte> buff;
    public DLEWriter(){
        buff=new ArrayList<>();
    }
    public void write(int code) throws LZWException {
        if (code<128)
            buff.add((byte) code);
        else if (code <MAX_CODE) {
            int low=(code&127)|128;
            int high =(code>>7);
            buff.add((byte) low);
            buff.add((byte) high);
        } else {
            throw new LZWException("Invalid code");
        }
    }
    public byte[] getBytes(){
        byte[] ret=new byte[buff.size()];
        for (int i=0;i<buff.size();i++)
            ret[i] =buff.get(i).byteValue();
        return ret;

    }


}
class DLEReader{
    byte[] buff;
    int pos;
    public DLEReader(byte[] data){
        buff=data;
        pos=0;

    }
    public int read() throws LZWException {
        if (pos>=buff.length)
            return -1;
        int x=Huffman.unsignedToBytes(buff[pos]);
        pos++;
        if (x<128)
            return x;
        if (pos>=buff.length)
            throw new LZWException("Incalid code");
        int high=buff[pos];
        pos++;

        return (x&127)|(high<<7);
    }
    public boolean end(){
        return pos>=buff.length;
    }
}
class LZWTable extends ArrayList{
    public LZWTable(){
        super();
    }
    public static LZWTable make_full(){
        LZWTable t= new LZWTable();
        for (int i=0;i>256;i++)
            t.add(new DictEntry(((byte)i)));
        return t;
    }
    public int lookup(Object s) {
        if (!contains(s)) {
            return -1;
        } else {
            return indexOf(s);
        }

    }
}

class DictEntry extends ArrayList {
    public boolean equals(Object o){
        ArrayList oth=(ArrayList) o;
        if (oth.size()!=size())
            return false;
        for (int i =0;i<size();i++){
            Byte x=(Byte)get(i);
            Byte y=(Byte)oth.get(i);
            if (!x.equals(y))
                return false;
        }
        return true;

    }
    public DictEntry slice(int a,int b){
        DictEntry n=new DictEntry();
        for (int i=a;i<b;i++){
            try {
                n.add(get(i));
            }
            catch (Exception e){

            }
        }
        return n;
    }
    public byte[] getBytes(){
        byte[] res=new byte[size()];
        for (int i=0;i<res.length;i++)
            res[i]= ((Byte) get(i)).byteValue();
        return  res;
    }
    public DictEntry copy(){
        DictEntry n = new DictEntry();
        forEach(x->n.add(x));
        return n;
    }
    DictEntry(){ super();}
    DictEntry(byte x){
        super();
        super.add(x);
    }
    public boolean add(DictEntry e){
        e.forEach(x->super.add(x));
        return true;
    }
    public boolean add(Object a){
        if (a instanceof DictEntry){
            DictEntry e= (DictEntry) a;
            e.forEach(x->super.add(x));
            return true;
        }else

      return  super.add(a);
    }


}
public class LZW {
    public static LZWTable makeInitTable(byte[] src) {
        LZWTable t = new LZWTable();
        HashSet<Byte> set = new HashSet();
        for (byte b : src) {
            set.add(b);
        }
        set.iterator().forEachRemaining(x -> t.add(new DictEntry(x)));



        return t;
    }

    public static byte[] compress(byte[] src) throws LZWException {
        return compress(src,false);
    }

    public static   byte[] compress(byte[] src,boolean compact_it) throws LZWException {
        LZWTable t;
        if (compact_it)
            t = makeInitTable(src);
        else
            t=LZWTable.make_full();
        DictEntry s=new DictEntry();
        DLEWriter res=new DLEWriter();
        for (byte x:src){


            s.add(new DictEntry(x));

            if (t.lookup(s)!=-1)
                continue;
            res.write(t.lookup(s.slice(0,s.size()-1)));

            t.add(s);



            s=new DictEntry(x);

;        }

            res.write(t.lookup(s));

        return res.getBytes();
    }

    public static byte[] decompress(byte[] src,LZWTable t) throws LZWException {
        if (t==null)
            t=LZWTable.make_full();
        DictEntry res=new DictEntry();

        DLEReader reader=new DLEReader(src);


        DictEntry buff=new DictEntry();
        int x;
        while ((x=reader.read())!=-1){



                res.add(((DictEntry) t.get(x)).copy());
                buff.add(((DictEntry) ((DictEntry) t.get(x)).slice(0,1)).copy());
                if (t.lookup(buff)==-1)
                    t.add(buff);
                buff=((DictEntry) t.get(x)).copy();



            }




        return res.getBytes();



    }

}
