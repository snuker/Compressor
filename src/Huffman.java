import javafx.util.Pair;

import java.io.*;
import java.util.*;





class bitBuffReader{
    ArrayList<Byte> l;
    byte curr;
    int pos;
    public bitBuffReader(byte bytes[]){
        l=new ArrayList();
        for (Byte b :bytes)
            l.add(b);
        curr=l.get(0);
        l.remove(0);
        pos=0;
    }

    public bitBuffReader(ArrayList<Byte> bytes){
        l = new ArrayList<>(bytes);
        curr=l.get(0);
        l.remove(0);
        pos=0;
    }
    public int getBit(){
        int res= (curr>>pos)&1;
        pos++;
        if (pos>7){
            curr=l.get(0);
            l.remove(0);
            pos=0;
        }
        return res;

    }
    public int getByte(){
        int res=0;
        for (int i=0;i<8;i++){
            res|=(getBit()<<8);
            res>>=1;
        }
        return res;
    }
    public boolean end(){
        return pos==7 && l.size()==0;
    }
    public String getBitRepr(){
        StringBuilder s=new StringBuilder();
        while (!end()){
            int x=getBit();
            s.append((x==1) ? '1':'0');
        }
        return s.toString();

    }
}
class bitBuff{
    ArrayList<Byte> l;
    byte curr;
    int pos;
    public int size(){
        if (pos==0)
            return l.size();
        else
            return l.size()+1;

    }
    public bitBuff(){
        curr=0;
        pos=0;
        l=new ArrayList<>();
    }
    public void writeBit(int x){
        curr|= ((x&1) << pos);
        pos++;
        if (pos>7){
            l.add(curr);
            pos=0;
            curr=0;
        }
    }
    public void writeByte( int x){
        for (int i=0;i<8;i++){
            writeBit(x>>i);
        }
    }
    public byte[] getBytes(){
        l.add(curr);
        byte[] bytes=new byte[l.size()];
        for(int i = 0; i < l.size(); i++) {
            bytes[i] = l.get(i).byteValue();
        }
        return bytes;
    }
    public static String byte_repr(byte x){
        StringBuilder s = new StringBuilder();
        for (int i=0;i<8;i++){
            if ((x & (1<<i))==0)
                s.append('0');

            else
                s.append('1');
        }
        return  s.toString();

    }
    public String repr(){
        StringBuilder s = new StringBuilder();
        l.stream().forEach(x->s.append(byte_repr(x)));
        s.append(byte_repr(curr));
        return s.toString();

    }
    public void putBitString(String s){
        if( s==null){
            return ;
        }

        for (char c: s.toCharArray()){
            if (c=='0')
                writeBit(0);

            else if (c=='1')
                writeBit(1);

        }
    }
}



class HuffTree implements Comparable<HuffTree> {
    public int freq;
    public HuffTree(int freq){
        this.freq=freq;
    }

    public int compareTo(HuffTree o) {
        return this.freq-o.freq;
    }


    ;


}

class HuffTreeLeaf extends HuffTree {
    public int value;
    public HuffTreeLeaf(int freq,int c) {
        super(freq);
        value=c;

    }
}
class HuffTreeNode extends HuffTree {
    public HuffTree left,right;
    public HuffTreeNode(HuffTree l, HuffTree r) {
        super(l.freq+r.freq);
        left=l;
        right =r;
    }
}

public class Huffman {


    public static HuffTree buildTree(int freqs[] ){
        PriorityQueue<HuffTree> pq = new PriorityQueue<HuffTree>();
        HuffTree top;

        for (int i=0;i<256;i++){
            if (freqs[i]>0)
             pq.add(new HuffTreeLeaf(freqs[i],i));
        }
        while(pq.size()>1){
            HuffTree l=pq.poll();
            HuffTree r=pq.poll();
            HuffTreeNode p = new HuffTreeNode(l,r);
            pq.add(p);
        }
        return pq.poll();
    }

    public static bitBuff treeShape(HuffTree t){
        bitBuff bf=new bitBuff();
        treeShape(t,bf);
        return bf;
    }

    public static String treeShape_string(HuffTree t){
        StringBuilder s=new StringBuilder();
        treeShape_string(t,s);
        return s.toString();
    }

    public static void treeShape_string(HuffTree t, StringBuilder s){
        if (t instanceof HuffTreeLeaf){
        s.append('1');
            s.append('!');
        s.append(((HuffTreeLeaf) t).value);
            s.append('!');
    }
        else {
        HuffTree l  =((HuffTreeNode)t).left;
        HuffTree r  =((HuffTreeNode)t).right;
        s.append('0');
        treeShape_string(l,s);
        treeShape_string(r,s);
    }
}

    public static void treeShape(HuffTree t, bitBuff bf){
        if (t instanceof HuffTreeLeaf){
            bf.writeBit(1);
            bf.writeByte(((HuffTreeLeaf) t).value);
        }
        else {
            HuffTree l  =((HuffTreeNode)t).left;
            HuffTree r  =((HuffTreeNode)t).right;
            bf.writeBit(0);
            treeShape(l,bf);
            treeShape(r,bf);
        }
    }

    public static HuffTree reconstructTree(bitBuffReader shape){
        int x=shape.getBit();
        if (x==1){
            int b=shape.getByte();

            return new HuffTreeLeaf(0,b);
        }
        else {

            HuffTree l = reconstructTree(shape);
            HuffTree r = reconstructTree(shape);
            return new HuffTreeNode(l, r);
        }
        }

    public static void buildCodeTable(HashMap tbl, HuffTree t, String s){
        if (t instanceof HuffTreeLeaf) {
            tbl.put((byte)((HuffTreeLeaf) t).value,s);
        } else {
            buildCodeTable(tbl, ((HuffTreeNode) t).left, s + '0');
            buildCodeTable(tbl, ((HuffTreeNode) t).right, s + '1');
        }
    }

    public static HashMap buildCodeTable(HuffTree t){
        HashMap m = new HashMap<Integer,Integer>();
        buildCodeTable(m,t,"");
        return m;
    }

    public static  bitBuff encode(byte[] bytes, HashMap<Integer,String> k){

        bitBuff bf = new bitBuff();
        for (byte c: bytes){

            bf.putBitString((String) k.get((Object)c));
        }
        return bf;
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public static byte[] decode(bitBuffReader b, HuffTree root, int total_len){
        ArrayList<Byte> res=new ArrayList<>();
        while (!b.end()&&total_len!=0){
            HuffTree t=root;
            while (!(t instanceof HuffTreeLeaf)){
                int x=b.getBit();
                if (x==1){
                    t= ((HuffTreeNode)t).right;

                }else {
                    t = ((HuffTreeNode) t).left;
                }

                }
            res.add((Byte ) (byte)((HuffTreeLeaf) t).value);
            total_len--;
            }

        byte[] bytes=new byte[res.size()];
        for(int i = 0; i < res.size(); i++) {
            bytes[i] = res.get(i).byteValue();
        }
        return bytes;
    }
}

