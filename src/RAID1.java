import java.io.*;

public class RAID1 {

    public static void WriteToFile(String Info, String filepath1,String filepath2) throws InterruptedException { //备份文件
        Thread t1=new writeThread(filepath1, Info);
        Thread t2=new writeThread(filepath2,Info);
        t1.start();t2.start();
        t1.join();t2.join();
    }

        static class writeThread extends Thread //多线程写文件
    {
        String filepath;
        String Info;
        public writeThread(String filepath,String Info)
        {
            this.filepath=filepath;
            this.Info=Info;
        }
        public void run()
        {
            try (FileOutputStream outputStream=new FileOutputStream(filepath)){
                outputStream.write(Info.getBytes());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String ReadFromFile(String filepath1, String filepath2) //读文件
    {
        StringBuffer temp=new StringBuffer();
        File file = null;String filepath="";
        File file1=new File(filepath1);
        File file2=new File(filepath2);
        if(file1.length()>file2.length())
        {
            file=file1; //找到更大的文件
            filepath=filepath2;//要重新写入的文件
        }
        else
        {
            file=file2;
            filepath=filepath1;
        }
        try (FileReader reader=new FileReader(file);FileWriter writer=new FileWriter(filepath)){
            int ch=0;
            while ((ch=reader.read())!=-1)
            {
                temp.append((char)ch);
                writer.append((char)ch); //修复损坏的文件,达到
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(temp);
    }



    }