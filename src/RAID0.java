import java.io.*;


public class RAID0 {

    static boolean fileexit=true;

    public static void WirteToFile(String Info, String filePath1, String filePath2) throws IOException, InterruptedException {//文件条带化写入
        char[] temp = Info.toCharArray();
        char[] temp1 = new char[((temp.length & 1) == 0 ? temp.length / 2 : temp.length / 2 + 1)];
        char[] temp2 = new char[temp.length / 2];
        int index = 0;

        for (int i = 0; i < temp.length; i++, index++) //将二进制序列条带化为两部分
        {
            temp1[index] = temp[i];
            if (++i < temp.length)
                temp2[index] = temp[i];
        }

        //这里可以使用多线程
        Thread wf1 = new writeThread(temp1, filePath1);
        Thread wf2 = new writeThread(temp2, filePath2);
        wf1.start();
        wf2.start();
        wf1.join();
        wf2.join();
    }


    static class writeThread extends Thread //多线程写文件类
    {
        char[] temp;
        String filepath;

        public writeThread(char[] temp, String filepath) {
            this.temp = temp;
            this.filepath = filepath;
        }

        public void run() {
            try {
                FileOutputStream fos = new FileOutputStream(filepath);
                fos.write(String.valueOf(temp).getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static String ReadFromFile(String filePath1, String filePath2) throws InterruptedException {
        //读取文件内容到字符串
        StringBuffer temp = new StringBuffer();

        //多线程读取被条带化的文件并且将其合并
        readThread rf1 = new readThread(filePath1);
        readThread rf2 = new readThread(filePath2);
        Thread t1 = new Thread(rf1);
        Thread t2 = new Thread(rf2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();


        for (int i = 0; i < rf1.temp.length(); i++)//将两个合并
        {
            temp.append(rf1.temp.charAt(i));
            if (i < rf2.temp.length())//防止超过第二个文件的大小
                temp.append(rf2.temp.charAt(i));
        }

        return String.valueOf(temp);

    }

    public static class readThread implements Runnable {
        String filepath;
        StringBuffer temp=null;

        public readThread(String filepath) {
            this.filepath = filepath;
        }

        public void run() {
            File file = new File(filepath);
            temp = new StringBuffer();
            try {
                FileReader fr = new FileReader(file);
                int ch = 0;
                while ((ch = fr.read()) != -1) {
                    temp.append((char) ch);
                }

                fr.close();
            } catch (FileNotFoundException e) {
                fileexit=false;
                temp.append(1);
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}




