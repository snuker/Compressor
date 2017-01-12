import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;


class ArchiveFile{
    public static final int magic1=0xfe;
    public static final int magic2=0x77;
    public static final int SEGMENT_TYPE_PURE_HUFF=0x01;
    public static final int SEGMENT_TYPE_HUFF_TREE=0x02;
    public static final int SEGMENT_TYPE_LZZ77_HUFF=0x02;
}

class ArchiveFileWriter extends ArchiveFile{
    DataOutputStream fs;

    void writeMagic() throws IOException {
        fs.writeByte(magic1);
        fs.writeByte(magic2);

    }
    public void writeSegment(int type,int dec_length,byte[] data) throws IOException {
        fs.writeByte(type);
        fs.writeInt(dec_length);
        fs.writeInt(data.length);

        for (byte b: data)
            fs.writeByte(b);
    }
    public void close() throws IOException {
        fs.close();
    }
    public ArchiveFileWriter(FileOutputStream s) throws IOException {
        fs=new DataOutputStream(s);
        writeMagic();
    }
}

class ArchiveFileReader extends ArchiveFile{
    DataInputStream fs;
    public boolean end() throws IOException {
        return fs.available()==0;
    }
    boolean readMagic() throws IOException{
        int b1= Huffman.unsignedToBytes(fs.readByte());
        int b2= Huffman.unsignedToBytes(fs.readByte());

        return (b1==magic1&b2==magic2);
    }
    Pair<Integer,Integer> getSegmentOps() throws IOException {
        int type=fs.readByte();
        int dec_length=fs.readInt();
        Pair p=new Pair(type,dec_length);
        return p;
    }
    byte[] getSegmentData() throws IOException {
        int len=fs.readInt();
        byte[] data=new byte[len];
        fs.read(data,0,len);
        return data;
    }
    public ArchiveFileReader(FileInputStream s) throws IOException {
        fs=new DataInputStream(s);

    }

}
public class Compress{
    public static String rm_ext(String s) {
        int i=s.lastIndexOf(".");
        return s.substring(0,i);
    }
    public static void main(String[] args) throws IOException, LZ77Exception {
        if (args[0].equals("-c")) {
            File in = new File(args[1]);
            ;
            File out = new File(args[1] + ".cpr");
            compressFile(in, out);
        } else if (args[0].equals( "-x")) {
            File in = new File(args[1]);

            File out = new File(rm_ext(args[1]));
            decompressFile(in, out);
        } else {
            System.out.println("Usage: -x or -c filename");
        }
    }




        public static void compressFile(File in,File out) throws IOException, LZ77Exception {
            FileInputStream is=new FileInputStream(in);
            ArchiveFileWriter ar=new ArchiveFileWriter(new FileOutputStream(out));

            byte[] buff=new byte[1024*1024*10];
            int x;
            while ((x=is.read(buff))!=-1){
                byte b[] =new byte[x];
                int[] freq=new int[256];

                System.arraycopy(buff,0,b,0,x);
                b=LZ77.compress(b);
                for (byte n:b)
                    freq[Huffman.unsignedToBytes(n)]++;
                HuffTree t= Huffman.buildTree(freq);

                HashMap mp=Huffman.buildCodeTable(t);

                ar.writeSegment(ArchiveFile.SEGMENT_TYPE_HUFF_TREE,0,Huffman.treeShape(t).getBytes());


                ar.writeSegment(ArchiveFile.SEGMENT_TYPE_LZZ77_HUFF,b.length,Huffman.encode(b,mp).getBytes());


            }
            ar.close();

        }
        public static void decompressFile(File in,File out) throws IOException, LZ77Exception {
            ArchiveFileReader comp =new ArchiveFileReader(new FileInputStream(in));
            DataOutputStream decomp = new DataOutputStream(new FileOutputStream(out));
            if (!comp.readMagic()) {
                System.out.println("Invalid magic");
                return;
            }

            while (!comp.end()) {
                Pair<Integer, Integer> a = comp.getSegmentOps();
                byte[] c_tree = comp.getSegmentData();
                Pair<Integer, Integer> d = comp.getSegmentOps();
                byte[] c_data = comp.getSegmentData();
                HuffTree c = Huffman.reconstructTree(new bitBuffReader(c_tree));
                byte[] decop = Huffman.decode(new bitBuffReader(c_data), c, d.getValue());

                decomp.write(LZ77.decompress(decop));

            }
                decomp.close();



                   }
}

