package echoclient;

/*
 This is the Cleint Side
 
 
 Travis Haycock   Kingston, Ontario
 
 
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class EchoRun extends JFrame implements ActionListener, KeyListener, Runnable {
      File newTextFile;
    JScrollPane scroll;
    /*
    VARS for send/receive messages
     */
    InetAddress ip;
    String hostName;
    String sessionKey;
    /*
    Time and Date
     */
    Date date;
    DateFormat dateFormat;
    String localTime;
    /*
    Audio Vars
    
     */
    String voiceOutput = "output.wav";
    TargetDataLine line;
    double duration, seconds;
    AudioInputStream audioInputStream;
    /*
    Send File VARS
    
     */
    private final int BUFFER_SIZE = 128000;
    private File soundFile;
    private AudioInputStream audioStream;
    private AudioFormat audioFormat;
    private SourceDataLine sourceLine;
    /*
    App VARS main
     */
    private JButton file, mic;
    String errStr;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket socket;
    private JTextField userText;
    private JTextArea chatWindow;
    private ServerSocket server;
    private Socket connection;
    private Socket connection2;
    private Socket connection3;
    private Socket sessionSocketKey;

    private String message = "";
    private String serverIP;
    Thread thread;
    JToolBar toolbar;
    String loco;
    // encrypt key
    byte[] key = "升力了山623423#&(@nds#826HLc".getBytes();

    public EchoRun(String host) throws UnknownHostException {
        super(" Echo Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLookAndFeel();
        userText = new JTextField();
        userText.setEditable(false);
        /*
        Time and date for messages
         */
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        date = new Date();
        localTime = dateFormat.format(date);

        chatWindow = new JTextArea();
        chatWindow.setEditable(false);
        chatWindow.setLineWrap(true);
        chatWindow.setForeground(Color.GREEN);
        chatWindow.setBackground(Color.BLACK);
        scroll = new JScrollPane(chatWindow);
        scroll.setPreferredSize(new Dimension(100, 50));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        Font font = new Font("Consolas", Font.BOLD, 12);
        chatWindow.setFont(font);
        mic = new JButton("Microphone");
        mic.setFont(new Font("Serif", Font.BOLD, 14));
        mic.setForeground(Color.blue);
        userText.addKeyListener(this);

        ip = InetAddress.getLocalHost();
        hostName = ip.getHostName();

        file = new JButton("Send File");
        file.addActionListener(this);
        file.setFont(new Font("Serif", Font.BOLD, 14));
        file.setForeground(Color.blue);
        toolbar = new JToolBar();
        toolbar.add(mic);
        toolbar.addSeparator();
        toolbar.add(file);
        toolbar.addSeparator();
        serverIP = host;
        userText.addActionListener(
                new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage(e.getActionCommand());
                } catch (IOException ex) {
                    Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                userText.setText(" ");
            }
        });

        add(userText, BorderLayout.SOUTH);
        add(chatWindow, BorderLayout.CENTER);
        add("North", toolbar);
        chatWindow.add("Center", scroll);
        setVisible(true);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel("UIManager.getSystemLookAndFeelClassName");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e) {
        }
    }

    public void startRunning() throws IOException {
        try {
            connectToServer();
            setUpStreams();
            whileChatting();
        } catch (EOFException eofException) {
            showMessage("Client Terminated Connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeAll();
        }

    }

    /*
    Connect to server
     */
 /*
    Will generate Random encryption key not in the program atall but possibly future implementation
    
     */
    public void generateEncryptionKey() throws IOException {
        // generate the random integer
        Random randomGenerator = new Random();
        for (int idx = 1; idx <= 1; ++idx) {
            int randomInt = randomGenerator.nextInt(100);
            byte[] array = new byte[256]; // length is bounded by 256
            new Random().nextBytes(array);
            String generatedString = new String(array, Charset.forName("UTF-8"));
            // both the int and char togetehr in a string
            sessionKey = generatedString + randomInt;
            System.out.println("session key is:" + sessionKey);

            newTextFile = new File("sessionKey.txt");
            FileWriter fileWriter = new FileWriter(newTextFile);
            fileWriter.write(sessionKey);
            fileWriter.close();

          
                // CARRY ON WITH LOGIC   
                System.out.println("New Session Key Created");
                // socket to pass the encrytion key
                sessionSocketKey = new Socket(InetAddress.getByName(serverIP),2000);

                byte[] mybytearray = new byte[(int) newTextFile.length()];
                FileInputStream fis = new FileInputStream(newTextFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(mybytearray, 0, mybytearray.length);
                OutputStream os = sessionSocketKey.getOutputStream();
                /*
        This will send the header info text.doc, hello.pdf
                 */
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeUTF(newTextFile.getName());

                System.out.println("Sending Key...");
                dos.write(mybytearray, 0, mybytearray.length);
                dos.flush();
                sessionSocketKey.close();
                System.out.println("Key Sent Sucessfully!");

      

        }

    }

    private void connectToServer() throws IOException {
        // *********connect to text to text via. server and client
        
        showMessage("\nAttempting To Connect To Server For Main Chat");
        connection = new Socket(InetAddress.getByName(serverIP), 2014);
        showMessage("\nconnected to:" + connection.getInetAddress().getHostName());
        generateEncryptionKey();
    }

    private void setUpStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\nStreams Established");
    }

    private void whileChatting() throws IOException {
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n " + message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("\n ERROR SENDING OBJECT");

            }
        } while (!message.equals("SERVER - END"));

    }

    /*
    Close func
    */
    private void closeAll() throws IOException {
        showMessage("\n closing...");
        ableToType(false);
        try {
            output.close();
            input.close();
            connection.close();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }
/*
    
    Send the text from jtextarea
    */

    private void sendMessage(String message) throws IOException {
        try {
            output.writeObject("\n" + localTime + "- " + hostName + ": \n\n" + message);
            output.flush();
            showMessage("\n " + localTime + "- " + hostName + ": \n\n" + message);
        } catch (IOException ioException) {
            chatWindow.append("\n ERROR Sending Message");
            ioException.printStackTrace();
        }

    }
/*
    Display MSG
    */
    private void showMessage(final String m) {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                chatWindow.append(m);
            }
        });

    }

    
    private void ableToType(final boolean tof) {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() {
                userText.setEditable(tof);
            }
        });

    }

    /*
    Handling the microphone
     */
    public void stop() {
        thread = null;
    }

    public void shutDown(String message) {
        if ((errStr = message) != null && thread != null) {
            thread = null;
            System.err.println(errStr);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'q') {

            System.out.println("Started Recording");
            start();

        }
    }

    public void start() {
        errStr = null;
        thread = new Thread((Runnable) this);
        thread.setName("Capture");
        thread.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Object source = e.getSource();

        if (e.getKeyChar() == 'q') {
            System.out.println("Stopped Recording");
            stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == file) {
            try {
                chooseFile();
            } catch (IOException ex) {
                Logger.getLogger(EchoRun.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(EchoRun.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /*
    Choose file method
     */
    public void chooseFile() throws IOException, Exception {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Send File");
        fc.setMultiSelectionEnabled(true);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int dialog = fc.showSaveDialog(this);
        if (dialog == JFileChooser.APPROVE_OPTION) {

            File inputFile = fc.getSelectedFile();
            inputFile.getAbsoluteFile();
            System.out.println("the extension type:" + fc.getTypeDescription(inputFile));
            /*
            will encrypt a file for you
             */

            File savedFile = new File("encrypted" + inputFile.getName());
            encryptFile(inputFile, (savedFile), key);
            //

            sendFile(savedFile);

        }
    }

    public void sendFile(File inputFile) throws IOException {
        // counting ms 
        long start = System.currentTimeMillis();

        connection3 = new Socket(InetAddress.getByName(serverIP), 1995);
        byte[] mybytearray = new byte[(int) inputFile.length()];
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(mybytearray, 0, mybytearray.length);
        OutputStream os = connection3.getOutputStream();
        /*
        This will send the header info text.doc, hello.pdf
         */
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(inputFile.getName());

        System.out.println("Sending...");
        dos.write(mybytearray, 0, mybytearray.length);
        dos.flush();
        connection3.close();
        long end = System.currentTimeMillis();

        chatWindow.append("File Name: " + inputFile.getName());
        System.out.println("Took " + (end - start) + "ms");
        JOptionPane.showMessageDialog(null, "Your File Transfer Took:" + (end - start) + "ms");
    }

    public void run() {

        duration = 0;
        audioInputStream = null;

        // define the required attributes for our line,
        // and make sure a compatible line is supported.
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float rate = 44100.0f;
        int channels = 2;
        int frameSize = 4;
        int sampleSize = 16;
        boolean bigEndian = true;

        AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            shutDown("Line matching " + info + "not supported.");
            return;
        }

        // get and open the target data line for capture.
        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        } catch (LineUnavailableException ex) {
            shutDown("Unable to open the line: " + ex);
            return;
        } catch (SecurityException ex) {
            shutDown(ex.toString());
            //JavaSound.showInfoDialog();
            return;
        } catch (Exception ex) {
            shutDown(ex.toString());
            return;
        }

        // play back the captured audio data
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int frameSizeInBytes = format.getFrameSize();
        int bufferLengthInFrames = line.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        line.start();

        while (thread != null) {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                break;
            }
            out.write(data, 0, numBytesRead);
        }

        // we reached the end of the stream.
        // stop and close the line.
        line.stop();
        line.close();
        line = null;

        // stop and close the output stream
        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // load bytes into the audio input stream for playback
        byte audioBytes[] = out.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);

        audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

        long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());

        duration = milliseconds / 1000.0;

        try {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(voiceOutput));

            //encryptFile(voiceOutput, new File( voiceOutput),key);
            System.out.println(voiceOutput);
            sendSound();

        } catch (IOException ex) {
            Logger.getLogger(EchoRun.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(EchoRun.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void sendSound() throws IOException, Exception {

        connection2 = new Socket(InetAddress.getByName(serverIP), 1990);
        System.out.println("send voice");
        OutputStream output2 = new ObjectOutputStream(connection2.getOutputStream());
        output2.flush();
        InputStream input2 = new ObjectInputStream(connection2.getInputStream());
        System.out.println(input2);

        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(connection2.getOutputStream());
            FileInputStream fis = null;
            fis = new FileInputStream(voiceOutput);

            int count = 0;
            byte[] buffer = new byte[4096];

            while ((count = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, count);
                dos.flush();
                System.out.println(fis);
            }

            fis.close();

            dos.flush();
            dos.close();

        } catch (IOException ex1) {
            Logger.getLogger(EchoRun.class.getName()).log(Level.SEVERE, null, ex1);
            System.out.println("or closing dos");
        }

    }

    public void encryptFile(File in, File output, byte[] key) throws Exception {

        Cipher cipher = getCipherEncrypt(key);
        FileOutputStream fos = null;
        CipherOutputStream cos = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(in);
            fos = new FileOutputStream(output);
            cos = new CipherOutputStream(fos, cipher);

            byte[] data = new byte[1024];
            int read = fis.read(data);
            while (read != -1) {
                cos.write(data, 0, read);
                read = fis.read(data);
                System.out.println(new String(data, "UTF-8").trim());
                chatWindow.append(new String(data, "UTF-8"));

            }
            cos.flush();

        } finally {

            System.out.println("performed encrypt method now closing streams:\n" + output.toString());
            cos.close();
            fos.close();
            fis.close();

        }
    }

    private byte[] getKeyBytes(final byte[] key) throws Exception {
        byte[] keyBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, Math.min(key.length, keyBytes.length));
        return keyBytes;
    }

    public Cipher getCipherEncrypt(final byte[] key) throws Exception {
        byte[] keyBytes = getKeyBytes(key);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher;
    }

}
