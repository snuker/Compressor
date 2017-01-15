import com.sun.org.apache.bcel.internal.generic.NEW;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by snuker on 15.01.17.
 */

//:(
class MTFTable extends ArrayList{

    void fill(){
        for (int i=0;i<256;i++){
            add(i,(byte)i);


        }

    }
    public MTFTable(){
        super();
        fill();
    }

    public void moveToFront(int pos){
        if (pos==0)
            return;
        byte front = (byte) get(pos);
        while (pos>0){
            set(pos,get(pos-1));
            pos--;
        }
        set(0,front);

    }
    public byte lookup(byte x){
        int pos=indexOf(x);
        moveToFront(pos);
        byte j=(byte) pos;
        return (byte) pos;
    }
    public boolean add(byte x){
        if(super.size()>=256){
            return false;
        }
        super.add(x);
        moveToFront(super.size()-1);
        return true;

    }
    public Object getElem(int i){
        byte x= (byte) super.get(i);
        moveToFront(i);
        return x;
    }
    public boolean hasKey(byte x){
        int pos = Huffman.unsignedToBytes(x);
        if (pos<size())
            return true;
        else
            return false;
    }

}
public class MTF {
    public static byte[] encode(byte[] src){
        MTFTable t=new MTFTable();
        byte[] res=new byte[src.length];
        for (int i=0;i<src.length;i++){
            res[i]=t.lookup(src[i]);
        }
        return  res;
    }
    public static byte[] decode(byte[] src){
        MTFTable t= new MTFTable();
        byte [] res=new byte[src.length];
        for (int i=0;i<src.length;i++){

                int pos=Huffman.unsignedToBytes(src[i]);
                res[i] = (byte) t.getElem(pos);


        }
        return res;

    }

}
