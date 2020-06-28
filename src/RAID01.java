import com.sun.org.apache.xerces.internal.impl.xpath.XPath;

import java.io.*;

public class RAID01 {
    public static void WriteToFile(String Info, String filepath1, String filepath2, String filepath3, String filepath4) throws InterruptedException, IOException //先进行镜像，后进行条带
    {
        RAID0.WirteToFile(Info, filepath1, filepath2); //先将要存储的信息条带化存储到两个磁盘中
        String Info1 = readfile(filepath1);
        String Info2 = readfile(filepath2);
        RAID0.WirteToFile(Info1, filepath1, filepath2);//再分别镜像化
        RAID0.WirteToFile(Info2, filepath3, filepath4);

    }

    public static String ReadFromFile(String filepath1, String filepath2, String filepath3, String filepath4) throws InterruptedException {
        //先将其合并在新的磁盘中
        String a = RAID0.ReadFromFile(filepath1, filepath2);
        String b = RAID0.ReadFromFile(filepath3, filepath4);
        Thread t1 = new RAID1.writeThread(filepath1, a);
        Thread t2 = new RAID1.writeThread(filepath2, b);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        //再读取从未被损坏的磁盘里读取的信息
        return RAID1.ReadFromFile(filepath1, filepath2);
    }

    public static String readfile(String filepath) throws FileNotFoundException {
        StringBuffer temp = new StringBuffer();
        File file = new File(filepath);
        try {
            FileReader fr = new FileReader(file);
            int ch = 0;
            while ((ch = fr.read()) != -1) {
                temp.append((char) ch);
            }

            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp.toString();
    }
}
