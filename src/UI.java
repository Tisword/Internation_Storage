
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class UI {
    public JPanel rootplane;
    private JLabel topLabel;
    private JLabel writeLabel;
    private JLabel reafLable;
    private JButton Wraid0;
    private JButton Wraid5;
    private JButton Wraid10;
    private JButton Rraid0;
    private JButton Rraid1;
    private JButton Rraid5;
    private JButton Rraid10;
    private JButton Wraid1;
    private JTextArea readTextArea;
    private JTextField pathTextField2;
    private JTextArea writeTextArea;
    private JButton readbutton;
    private JButton writebutton;
    private JTextField pathTextField1;
    private JButton Wraid01;
    private JButton Rraid01;

    static int type=0;


    public UI(Socket socket) throws IOException {
        DataOutputStream outputStream=new DataOutputStream(socket.getOutputStream());
        DataInputStream inputStream=new DataInputStream(socket.getInputStream());

        Wraid0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = selectfile();
                pathTextField1.setText(path);
                type=0;
            }
        });
        Wraid1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = selectfile();
                pathTextField1.setText(path);
                type=1;

            }
        });
        Wraid5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = selectfile();
                pathTextField1.setText(path);
                type=2;
            }
        });
        Wraid10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = selectfile();
                pathTextField1.setText(path);
                type=3;
            }

        });
        writebutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path=pathTextField1.getText();//得到要写入的文件路径
                System.out.println(path);
                try {
                    outputStream.writeInt(type);//写入要进行的操作
                    byte[] data= ImageTran.image2byte(path);
                    outputStream.writeInt(data.length);//写入数据长度
                    outputStream.writeUTF(writeTextArea.getText());//写入图片名称
                    outputStream.write(data);//写入图片数据
                    outputStream.flush();
                    System.out.println("已发送");
                    System.out.println(inputStream.readUTF());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        Rraid0.addActionListener(new ActionListener() { //读取RAID0硬盘中有的图片
            @Override
            public void actionPerformed(ActionEvent e) {
                    type=4;
                    getfilname(outputStream,inputStream);
            }
        });
        Rraid1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                   type=5;
                   getfilname(outputStream,inputStream);
            }
        });
        Rraid5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                type=6;
                getfilname(outputStream,inputStream);
            }
        });


        Rraid10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                type=7;
                getfilname(outputStream,inputStream);
            }
        });
        readbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    getfile(outputStream,inputStream);
            }
        });


    }
    private String selectfile() {
        String path = "";
        JFileChooser chooser = new JFileChooser();             //设置选择器
        chooser.setMultiSelectionEnabled(false);             //设为单
        int returnVal = chooser.showOpenDialog(null);        //是否打开文件选择框
        System.out.println("returnVal=" + returnVal);
        //  chooser.setFileFilter(new FileNameExtensionFilter("image(*.jpg)", "jpg"));
        if (returnVal == JFileChooser.APPROVE_OPTION) {          //如果符合文件类型
            path = chooser.getSelectedFile().getAbsolutePath();
            System.out.println(path);
        }
        return path;
    }

    private  void getfilname(DataOutputStream outputStream,DataInputStream inputStream)
    {
        try {
            outputStream.writeInt(type);
            outputStream.flush();
            String filenamelist= inputStream.readUTF();
            readTextArea.setText(filenamelist);
            readTextArea.append("\n");
            readTextArea.append(inputStream.readUTF());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void getfile(DataOutputStream outputStream,DataInputStream inputStream)
    {
        String filenmae=pathTextField2.getText();//获取要读取的文件名
        try {
            outputStream.writeUTF(filenmae);
            outputStream.flush();
            if (inputStream.readBoolean()) //找到了文件
            {
                int len=inputStream.readInt();
                byte[] data=new byte[len];
                inputStream.read(data);
                savefile(data,filenmae);
                readTextArea.append("\n"+inputStream.readUTF());
            }
            else readTextArea.append("\n"+inputStream.readUTF());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    private void savefile(byte[] data,String filename)
    {
        String filepath="/home/tisword/Documents/"+filename;
        ImageTran.byte2image(data,filepath);
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket= new Socket("localhost", 6666);
        Thread.sleep(100);//确保能够得到客户端的socket
        JFrame frame = new JFrame("UI");
        frame.setContentPane(new UI(socket).rootplane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}




