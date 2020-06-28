import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException, IOException {
        ServerSocket ss = new ServerSocket(6666); // 监听指定端口
        System.out.println("server is running...");
        for (;;) {
            Socket sock = ss.accept();
            System.out.println("connected from " + ((Socket) sock).getRemoteSocketAddress());
            Thread t = new Handler(sock);
            t.start();
        }
    }
}

class Handler extends Thread {
    Socket sock;

    public Handler(Socket sock) {
        this.sock = sock;
    }

    @Override
    public void run() {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(this.sock.getInputStream()))) {
            try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(this.sock.getOutputStream()))) {

                while (true) {
                    int datatype = input.readInt();
                    if (datatype < 4) { //写图片
                        int len = input.readInt();
                        if (len <= 0) {
                            output.writeUTF("false!");
                            output.flush();
                            continue;
                        }
                        String filename = input.readUTF();
                        byte[] data = new byte[len];
                        input.read(data);//读取图片数据
                        String Info = ImageTran.byteArrToBinStr(data);
                        String forepath="/home/tisword/Internet_Stroage/RAID";
                        switch (datatype) {
                            case 0:
                                savefilename(filename, 0);
                                RAID0.WirteToFile(Info, forepath+ "0/" + filename + "1", forepath+ "0/" + filename + "2");
                                output.writeUTF("Write to RAID0 sucessfully!");
                                output.flush();
                                break;
                            case 1:
                                savefilename(filename, 1);
                                RAID1.WriteToFile(Info, forepath+ "1/" + filename + "1",forepath+ "1/" + filename + "2");
                                output.writeUTF("Write to RAID1 sucessfully!");
                                output.flush();
                                break;
                            case 2:
                                savefilename(filename, 5);
                                RAID5.WriteToFile(Info, forepath+ "5/" + filename + "1",forepath+ "5/" + filename + "2" ,forepath+ "5/" + filename + "3");
                                output.writeUTF("Write to RAID5 sucessfully!");
                                output.flush();
                                break;
                            case 3:
                                savefilename(filename, 10);
                                RAID10.WriteToFile(Info, forepath+ "10/" + filename + "1",forepath+ "10/" + filename + "2",forepath+ "10/" + filename + "3",forepath+ "10/" + filename + "4");
                                output.flush();
                                break;

                        }
                    } else {//读图片
                        switch (datatype) {
                            case 4: //从RAID0中读图片
                                    writefile(output,input,0);
                                break;
                            case 5: //从RAID1中读图片
                                    writefile(output,input,1);
                                break;
                            case 6: //从RAID5中读图片
                                writefile(output,input,5);
                                break;
                            case 7: //RAID10中读图片
                                writefile(output,input,10);
                                break;
                        }

                    }

                }


            }
        } catch (Exception e) {
            try {
                this.sock.close();
            } catch (IOException ioe) {
            }
            System.out.println("client disconnected.");
        }
    }

    public static void savefilename(String filename, int datatype) throws IOException {
        FileOutputStream fos = new FileOutputStream("/home/tisword/Internet_Stroage/RAID" +datatype + "/filename",true);
        fos.write(String.valueOf(filename).getBytes());
        fos.write("\n".getBytes());
        fos.flush();
        fos.close();
    }

    public static String getfilename(int datatype) throws IOException {
        FileInputStream  fis=new FileInputStream("/home/tisword/Internet_Stroage/RAID" +datatype + "/filename");
        BufferedReader br=new BufferedReader(new InputStreamReader(fis));
        StringBuffer temp=new StringBuffer();
        String line;
        while ((line=br.readLine())!=null)
        {
            temp.append(line);
            temp.append("\n");
        }
        return temp.toString();
    }

    public void writefile(DataOutputStream output,DataInputStream input,int datatype) throws IOException, InterruptedException {
        String filenamelist = getfilename(datatype);
        output.writeUTF(filenamelist);
        output.writeUTF("Choose file you want!");
        output.flush();
        String filename = input.readUTF();
        if(IsExist(filename,datatype)) {
            String info = null;
            String forepath="/home/tisword/Internet_Stroage/RAID";
            switch (datatype){
                case 0:
                     info= RAID0.ReadFromFile(forepath+ "0/" + filename + "1", forepath+ "0/" + filename + "2");
                break;
                case 1:
                    info=RAID1.ReadFromFile(forepath+ "1/" + filename + "1",forepath+ "1/" + filename + "2");
                    break;
                case 5:
                    info=RAID5.ReadFromFile(forepath+ "5/" + filename + "1",forepath+ "5/" + filename + "2" ,forepath+ "5/" + filename + "3");
                    break;
                case 10:
                    info=RAID10.ReadFromFile(forepath+ "10/" + filename + "1",forepath+ "10/" + filename + "2",forepath+ "10/" + filename + "3",forepath+ "10/" + filename + "4");
                    break;
            }

            byte[] data = ImageTran.binStrToByteArr(info);
            output.writeBoolean(true);
            output.writeInt(data.length);
            output.write(data);
            output.writeUTF("transmit successfully!");
            output.flush();
        }
        else
        {
            output.writeBoolean(false);
            output.writeUTF("file isn't exist!");
            output.flush();
        }
    }

    public boolean IsExist(String filename,int datatype) throws IOException {
        FileInputStream  fis=new FileInputStream("/home/tisword/Internet_Stroage/RAID" +datatype + "/filename");
        BufferedReader br=new BufferedReader(new InputStreamReader(fis));
        String line;boolean flag=false;
        while ((line=br.readLine())!=null)
        {
            if(filename.equals(line))
            {
                flag=true;
                break;
            }
        }
        return flag;
    }


}
