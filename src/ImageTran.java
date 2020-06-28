import java.io.*;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;


public class ImageTran {


    public static byte[] image2byte(String path){
        byte[] data = null;
        FileImageInputStream input = null;
        try {
            input = new FileImageInputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while ((numBytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();
        }
        catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        }
        catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return data;
    }


    public static void byte2image(byte[] data,String path){  //将byte[]数组转化为图片
        if(data.length<3||path.equals("")) return;
        try{
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();
            System.out.println("Make Picture success,Please find image in " + path);
        } catch(Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }
    }

    public static String byteArrToBinStr(byte[] b) { //将byte数组转化为二进制序列，每个二进制序列(由逗号隔开)固定8位
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            //result.append(Long.toString(b[i] & 0xff, 2) + ",");
            String temp=Long.toString(b[i] & 0xff, 2);
            if(temp.length()<8) //若分割的二进制序列不足8位，则为其在前面补0
            {
                temp=String.format("%08d",Integer.valueOf(temp));
            }
            result.append(temp);
        }
       // return result.toString().substring(0, result.length() - 1);//去掉最后一个逗号
        return result.toString();
    }

    public static String[] splitString_per8(String s)//将字符串以每8位分割并且返回数组
    {
        int splitnum=s.length()/8; //根据之前的设定，字符串长度应为8的整数倍，因此可以直接除
        String[] subs=new String[splitnum];
        int startindex=0;int endindex=8;
        for(int i=0;i<splitnum;i++)
        {
            subs[i]=s.substring(startindex,endindex);
            startindex=endindex;
            endindex+=8;
        }
        return subs;
    }

    public static byte[] binStrToByteArr(String binStr) {//将以(逗号)每8位隔开的二进制序列转化为byte[]
       // String[] temp = binStr.split(",");
        String[] temp=splitString_per8(binStr);
        byte[] b = new byte[temp.length];
        for (int i = 0; i < b.length; i++) {
            String s=temp[i].replaceAll("^(0+)", "");//消去前面多余的0
            if(s.equals(""))s="0";//防止全部消除
            b[i] = Long.valueOf(s, 2).byteValue();
            //b[i] = Long.valueOf(temp[i], 2).byteValue();
        }
        return b;
    }


    public static void main(String[] argvs) throws IOException, InterruptedException {
        byte[] code=ImageTran.image2byte("/home/tisword/Desktop/qnv.jpg");//将图片转为byte序列
        System.out.println(code.length);
        String info= ImageTran.byteArrToBinStr(code); //将byte序列转为二进制序列

        System.out.println(info.length());

      // RAID0.WirterToFile(info,"/home/tisword/Desktop/1","/home/tisword/Desktop/2"); //使用RAID0条带化保存文件

    //    String BinStr= RAID0.ReadFromFile("/home/tisword/Desktop/1","/home/tisword/Desktop/2");

      //  byte[] ByteArr=ImageTran.binStrToByteArr(BinStr);

        //ImageTran.byte2image(ByteArr,"/home/tisword/Desktop/qnv1.jpg");

       // RAID1.WriteToFile(info,"/home/tisword/Desktop/3","/home/tisword/Desktop/4"); //使用镜像化保存文件

//        String BinStr= RAID1.ReadFromFile("/home/tisword/Desktop/3","/home/tisword/Desktop/4");
//
//        byte[] ByteArr=ImageTran.binStrToByteArr(BinStr);
//
//        ImageTran.byte2image(ByteArr,"/home/tisword/Desktop/qnv2.jpg");

        RAID5.WriteToFile(info,"/home/tisword/Desktop/5","/home/tisword/Desktop/6","/home/tisword/Desktop/7");
       String BinStr=RAID5.ReadFromFile("/home/tisword/Desktop/5","/home/tisword/Desktop/6","/home/tisword/Desktop/7");

        byte[] ByteArr=ImageTran.binStrToByteArr(BinStr);
        ImageTran.byte2image(ByteArr,"/home/tisword/Desktop/qnv3.jpg");

    }


}
