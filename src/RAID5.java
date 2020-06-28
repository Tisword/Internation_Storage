import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RAID5 {

    //将内容和校验码分别存放到三个磁盘当中
    public static void WriteToFile(String Info,String filepath1,String filepath2,String filepath3) throws InterruptedException {
        char[] temp=Info.toCharArray();
        int perlen=temp.length/2; //由于length是8的倍数，因此肯定能被2整除
        char[] temp1=new char[perlen];
        char[] temp2=new char[perlen];
        char[] temp3=new char[perlen];
        for (int i=0,index=0;i<temp.length;index++)
        {
            if((index%3)==0) //此时第三个磁盘放校验码
            {
                temp1[index]=temp[i++];
                temp2[index]=temp[i++];
                temp3[index]=(char)((temp1[index]-48)^(temp2[index]-48)+48);

            }
            else if((index%3)==1) //此时第一个磁盘放校验码
            {
                temp2[index]=temp[i++];
                temp3[index]=temp[i++];
                temp1[index]=(char)((temp2[index]-48)^(temp3[index]-48)+48);
            }
            //此时第二个磁盘放校验码
            else
            {
                temp1[index]=temp[i++];
                temp3[index]=temp[i++];
                temp2[index]=(char)((temp1[index]-48)^(temp3[index]-48)+48);
            }
        }

        //多线程写入文件
        Thread wf1=new RAID0.writeThread(temp1,filepath1);
        Thread wf2=new RAID0.writeThread(temp2,filepath2);
        Thread wf3=new RAID0.writeThread(temp3,filepath3);
        wf1.start();wf2.start();wf3.start();
        wf1.join();wf2.join();wf3.join();

    }

    //从三个文件中读取
    public static String ReadFromFile(String filepath1, String filepath2, String filepath3) throws InterruptedException {
        StringBuffer temp=new StringBuffer();
        //先使用多线程读取三个文件中的内容
        RAID0.readThread rf1=new RAID0.readThread(filepath1);
        RAID0.readThread rf2=new RAID0.readThread(filepath2);
        RAID0.readThread rf3=new RAID0.readThread(filepath3);
        Thread t1=new Thread(rf1);Thread t2=new Thread(rf2);Thread t3=new Thread(rf3);
        t1.start();t2.start();t3.start();
        t1.join();t2.join();t3.join();

        //校验并合并,考虑到可能会出现一个磁盘损坏的情况，因此要选择最长的长度，并且找出损坏的那个磁盘
        int length=Math.max(Math.max(rf1.temp.length(),rf2.temp.length()),rf3.temp.length()) ;
        if((rf1.temp.length()==rf2.temp.length())&&(rf2.temp.length()==rf3.temp.length())) //此时没有磁盘损坏
        {
            for (int  index = 0; index < length; index++) {
                if ((index % 3) == 0)//校验码位于第三个磁盘
                {
                    temp.append(rf1.temp.charAt(index));
                    temp.append(rf2.temp.charAt(index));
                }
                if ((index % 3) == 1)//校验码位于第一个磁盘
                {
                    temp.append(rf2.temp.charAt(index));
                    temp.append(rf3.temp.charAt(index));
                }
                if ((index % 3) == 2)//校验码位于第二个磁盘
                {
                    temp.append(rf1.temp.charAt(index));
                    temp.append(rf3.temp.charAt(index));
                }
            }
        }
        else //此时会有磁盘损坏，因此需要在读取时通过奇偶检验修复文件
        {
            char ch;
            StringBuffer recover=new StringBuffer();//用于修复文件
            RAID0.readThread rf=finderrordisk(rf1,rf2,rf3);//找到损坏的那个磁盘
            if(rf==rf1)//当损坏的是第一个时
            {
                for(int index=0;index<length;index++)//进行信息复原
                {
                    if((index%3)==0)//校验码位于第三个磁盘,则通过校验码得到第一个磁盘的原本字符
                    {
                        ch=(char) ((rf2.temp.charAt(index)-48)^(rf3.temp.charAt(index)-48)+48);
                        temp.append(ch);recover.append(ch);
                        temp.append(rf2.temp.charAt(index));
                    }
                    if ((index % 3) == 1)//校验码位于第一个磁盘
                    {
                        ch=(char) ((rf2.temp.charAt(index)-48)^(rf3.temp.charAt(index)-48)+48);
                        recover.append(ch);
                        temp.append(rf2.temp.charAt(index));
                        temp.append(rf3.temp.charAt(index));
                    }
                    if ((index % 3) == 2)//校验码位于第二个磁盘
                    {
                        ch=(char) ((rf2.temp.charAt(index)-48)^(rf3.temp.charAt(index)-48)+48);
                        temp.append(ch);recover.append(ch);
                        temp.append(rf3.temp.charAt(index));
                    }

                }
                //将正确信息重新写入损坏的磁盘
                try(FileOutputStream fos = new FileOutputStream(filepath1))
                {
                    fos.write(String.valueOf(recover).getBytes());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(rf==rf2)//当损坏的是第二个时
            {
                for(int index=0;index<length;index++)
                {
                    if((index%3)==0)//校验码位于第三个磁盘,则通过校验码得到第二个磁盘的原本字符
                    {
                        ch=(char) ((rf1.temp.charAt(index)-48)^(rf3.temp.charAt(index)-48)+48);
                        temp.append(rf1.temp.charAt(index));
                        temp.append(ch);recover.append(ch);
                    }
                    if ((index % 3) == 1)//校验码位于第一个磁盘
                    {
                        ch=(char) ((rf1.temp.charAt(index)-48)^(rf3.temp.charAt(index)-48)+48);
                        temp.append(ch);recover.append(ch);
                        temp.append(rf3.temp.charAt(index));
                    }
                    if ((index % 3) == 2)//校验码位于第二个磁盘
                    {
                        ch=(char) ((rf1.temp.charAt(index)-48)^(rf3.temp.charAt(index)-48)+48);
                        recover.append(ch);
                        temp.append(rf1.temp.charAt(index));
                        temp.append(rf3.temp.charAt(index));
                    }

                }
                //将正确信息重新写入损坏的磁盘
                try(FileOutputStream fos = new FileOutputStream(filepath2))
                {
                    fos.write(String.valueOf(recover).getBytes());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else //当损坏的是第三个时
            {
                for(int index=0;index<length;index++)
                {
                    if((index%3)==0)//校验码位于第三个磁盘,则通过校验码得到第二个磁盘的原本字符
                    {
                        ch=(char) ((rf1.temp.charAt(index)-48)^(rf2.temp.charAt(index)-48)+48);
                        recover.append(ch);
                        temp.append(rf1.temp.charAt(index));
                        temp.append(rf2.temp.charAt(index));
                    }
                    if ((index % 3) == 1)//校验码位于第一个磁盘
                    {
                        ch=(char) ((rf1.temp.charAt(index)-48)^(rf2.temp.charAt(index)-48)+48);
                        temp.append(rf2.temp.charAt(index));
                        temp.append(ch);recover.append(ch);
                    }
                    if ((index % 3) == 2)//校验码位于第二个磁盘
                    {
                        ch=(char) ((rf1.temp.charAt(index)-48)^(rf2.temp.charAt(index)-48)+48);
                        temp.append(rf1.temp.charAt(index));
                        temp.append(ch);recover.append(ch);
                    }

                }
                //将正确信息重新写入损坏的磁盘
                try(FileOutputStream fos = new FileOutputStream(filepath3))
                {
                    fos.write(String.valueOf(recover).getBytes());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

        return String.valueOf(temp);

    }

    public static RAID0.readThread finderrordisk(RAID0.readThread rf1,RAID0.readThread rf2,RAID0.readThread rf3)//找到损坏的磁盘
    {
        int len1=rf1.temp.length(),len2=rf2.temp.length(),len3=rf3.temp.length();
        int min = Math.min(len1, len2);
        min=Math.min(min,len3);
        if(min==len1)return rf1;
        else if(min==len2)return rf2;
        else return rf3;
    }

}
