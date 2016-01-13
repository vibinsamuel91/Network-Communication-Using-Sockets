import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.*;

/**
 *
 * @author Vibin
 */

public class WebBrowser extends javax.swing.JFrame {

    private static Boolean gui;
    private static Socket s;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Boolean pre;
    private static Boolean script;
    private static String image[] = {".jpg", ".jpeg", ".png", ".gif", ".tiff"};
    private static String host;
    private static String port;
    private static String page;
    private static String imageFileName;
    private static String domain[] = {".org", ".com", ".edu", ".net"};
    public static String url;
    private static WebBrowser ui;
    private String viewHtml; //saving everything to be shown in GUI browser in a string
    						//before seting it as text in jEditorPane
    
    //contructor
    public WebBrowser() {
        gui = false; //to open GUI or command line, default command line
        s = null;
        out = null;
        in = null;
        pre = false;
        script = false;
        host = null;
        port = "80";
        page = "/";
        imageFileName = "";
        url = "";
        initComponents();
        viewArea.setText("");
        ui = null;
        viewHtml = "<html>\n<body>\n";
    }

    
    /*this method removes the tags and adds empty line where ever necessary
    script blocks are also removed
    for empty line in gui, <br> is used*/
    private void parseHtml(String line) throws IOException {
        //addText("\nParse "+line+"GUI "+gui);
        String im = line;
        Boolean tLine = false;
        
        //set of tags which require empty line after
        String trailLine[] = {"</p>", "</h", "</ul>", "</li>", "</title>"};
        //set of tags which after which an empty line is added
        String leadLine[] = {"<p", "<h", "<ul"};
        if (line.contains("<br/>") || line.contains("<hr")) {
            System.out.println("\n");
            addText("<br>");
        }
        if (line.toLowerCase().contains("<pre>")) {
            pre = true;
        }
        if (line.toLowerCase().contains("</pre>")) {
            pre = false;
        }
        if (line.toLowerCase().contains("<script")) {
            script = true;
        }
        if (line.toLowerCase().contains("</script>")) {
            script = false;
        }
        if (script) {
            return;
        }
        
        for (String i : leadLine) {
            if (line.toLowerCase().contains(i)) {
                System.out.println();
                if (gui) {
                    addText("<br>");
                }
            }
        }
        for (String i : trailLine) {
            if (line.toLowerCase().contains(i)) {
                tLine = true;
            }
        }
        
        //regular expressions to remove tags
        line = line.replaceAll("\\>", "\\> ");
        line = line.replaceAll("\\<", " \\<");
        line = line.replaceAll("\\<.*?\\>", "");
        line = line.replaceAll("(.*)\\<.*+", "$1");
        line = line.replaceAll(".*?\\>(.*?)", "$1");
        line = line.replaceAll("\\&.*\\;", "");
        
        
        System.out.print(line.trim());
        if (gui) {
            addText(line.trim());
        }

        if (im.toLowerCase().contains("src=")) {
            //System.out.println(im);
            parseImage(im);
        }

        if (tLine) {
            System.out.println("");
            if (gui) {
                addText("<br>");
            }
        }
        if (pre) {
            System.out.println("");
            addText("<br>");
        }
    }

    //checks whether the url is image or not
    public void loadURL(String url) throws IOException {
        Boolean isImage = false;
        for (String i : image) {
            if (url.contains(i)) {
                parseImage(url);
                addText("\n</body>\n</html>");
                isImage = true;
            if(!gui) System.exit(0);
            }
        }
        if (!isImage) {
            connectSocket(url);
        }
    }

    /*This method removes tags
    gets filename with extension
    gets url*/
    public void parseImage(String url) throws IOException {
        //addText("\nParse Image");
        try {
            url = url.split("src=")[1];
            //addText("URL 1+" + url);
        } catch (Exception e) {

        }
        String f, l;
        l = ".png";
        for (String i : image) {
            if (url.contains(i)) {
                l = i;
            }
        }
        url = url.replaceAll("\"", "");
        url = url.replaceAll("\\'", "");
        f = url.split(l)[0];
        f = f.replaceAll(".*/(.*?)", "$1");
        imageFileName = f + l;
        System.out.print(" Image: " + imageFileName);
        System.out.println();

        url = url.split(l)[0];
        url = url + l;
        url = url.replaceAll("http://", "");
        Boolean nodomanin = true;
        for (String i : domain) {
            if (url.contains(i)) {
                nodomanin = false;
            }
        }
        if (nodomanin) {
            try {
                page = page.split("/")[1];
                page = page + "/";
            } catch (Exception e) {
                page = "";
            }
            url = host + "/" + page + url;
        }
        //System.out.println(url);
        // addText(" IMage URL:" + url);
        if (url.contains("utdallas")) {
            addText(" Image: " + imageFileName);
        } else {
            if (gui) {
                addImage(imageFileName);
            }
            connectImage(url);
        }
        return;
    }

    //creating new Socket to download image
    private void connectImage(String url) throws IOException {
        Socket im = null;
        DataInputStream is = null;
        OutputStream os = null;
        DataOutputStream send = null;
        try {
            host = url.split("/")[0];
        } catch (Exception e) {
            host = "";
        }
        page = url.replace(host, "");
        if (page.equalsIgnoreCase("")) {
            page = "/";
        }
        if (host.toLowerCase().contains(":")) {
            port = host.split(":")[1];
            host = host.split(":")[0];
        }
        try {
            im = new Socket(host, 80);
            send = new DataOutputStream(im.getOutputStream());
            is = new DataInputStream(im.getInputStream());
            os = new FileOutputStream(imageFileName);
        } catch (UnknownHostException e) {
            addText(url + ": Host not connected\n</body></html>");
            System.err.println("Host not connected");
            System.exit(0);
        }
        send.writeBytes("GET " + page + " HTTP/1.0\r\nHOST: " + host + "\r\n\r\n");
        //System.out.println("GET " + page + " HTTP/1.0\r\nHOST: " + host + "\r\n\r\n");
        send.flush();

        
        //downloads image and saves it in a file
        byte[] b = new byte[2048];
        int length = 0;
        String buffer;
        if ((length = is.read(b)) != -1) {
            buffer = new String(b);
            if (buffer.contains("\r\n\r\n")) {
                int j = getIndex(buffer.toCharArray());
                os.write(b, j, length - j);
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                    os.flush();
                }
            }
        }

//        os.close();
//        is.close();
//        im.close();
    }

    /*To remove the headers which come along with the image
    \r\n\r\n is checked in the stream of bytes
    returns the value for the first hit of the query*/
    private int getIndex(char[] s) {
        for (int i = 0; i < s.length; i++) {
            if (s[i] == '\r'
                    && s[i + 1] == '\n'
                    && s[i + 2] == '\r'
                    && s[i + 3] == '\n') {
                return i + 4;
            }
        }
        return 0;
    }

    //this methods connects to the the socket
    private void connectSocket(String url) throws IOException {
        //addText("Entering connect Socket");
        try {
            host = url.split("/")[0];
        } catch (Exception e) {
            host = "";
        }
        page = url.replace(host, "");
        if (page.equalsIgnoreCase("")) {
            page = "/";
        }
        if (host.toLowerCase().contains(":")) {
            port = host.split(":")[1];
            host = host.split(":")[0];
        }
        try {
            s = new Socket(host, Integer.parseInt(port));
            out = new PrintWriter(s.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            //addText("\nSocket connected\n\n");
        } catch (UnknownHostException e) {
            if (gui) {
                addText("<br>" + url + ": Host not connected\n</body></html>");
            }
            System.err.println("Host not connected");
            System.exit(0);
        }
        out.println("GET " + page + " HTTP/1.1\r\nHOST:" + host + "\r\nPort: "+port+"\r\n");
        readLine();
    }

    //this methods reads the messages sent from the socket line by line
    private void readLine() throws IOException {
        String reply = in.readLine();
        //addText("\nReadLine");

        while (!reply.toLowerCase().contains("<title>")) {
            reply = in.readLine();
        }

        while (true) {
            if (reply.toLowerCase().contains("</title>")) {
                //addText("\nRead /Title 2\n"+reply);
                parseHtml(reply);
                reply = in.readLine();
                break;
            } else {
                parseHtml(reply);
                reply = in.readLine();
            }
        }

        while (!reply.toLowerCase().contains("<body>")) {
            reply = in.readLine();
            //addText("Body Reply + Gui " + gui);
        }

        while (true) {
            if (reply.toLowerCase().contains("</html>")) {
                parseHtml(reply);
                break;
            } else {
                parseHtml(reply);
                reply = in.readLine();
            }
        }
//        System.out.println("isConnected: " + s.isConnected()
//                + " isBound: " + s.isBound()
//                + " isClosed: " + s.isClosed());

        addText("</body>\n</html>");
        if(!gui) System.exit(0);
    }
    
    //sets url from address bar
    public void setURL(String address) {
        url = address;
    }

    //add text to the string viewHtml
    public void addText(String line) {
        viewHtml += line;
        //viewArea.setText(viewArea.getText() + line);
    }

    //adds tags to adding a file
    public void addImage(String name) {
        addText("<br><img src=\"file:" + name + "\">");
    }

    /*this is called in the constructor and initializes the whole form*/
    private void initComponents() {

        addressBar = new javax.swing.JTextField();
        goButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        viewArea = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        goButton.setText("GO");
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        viewArea.setContentType("text/html");
        viewArea.setFont(new java.awt.Font("Calibri", 0, 24)); // NOI18N
        jScrollPane1.setViewportView(viewArea);
        viewArea.getAccessibleContext().setAccessibleDescription("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(addressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 960, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(goButton))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(goButton, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 608, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }

    
    //gives action to goButton
    private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            //viewArea.setText(null);
            setURL(addressBar.getText());
            loadURL(url.replaceFirst("http://", ""));
            viewArea.setText(viewHtml);
            //readLine(); 
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    
    //main function
    public static void main(String args[]) throws IOException {
        ui = new WebBrowser();
        
        //if  no arguments, show gui browser
        if (args.length == 0) {
            gui = true;
            ui.setVisible(gui);
            //ui.addText("Entering while + gui " + gui);
            while (url == null) {
            }
        }
        
        //if there is an argument, keep gui browser hidden
        if (args.length == 1) {
            url = args[0];

            String a = "http://www.december.com/html/demo/hello.html";
            String b = "http://www.utdallas.edu/~ozbirn/image.html";
            String c = "http://assets.climatecentral.org/images/uploads/news/Earth.jpg";
            String d = "http://htmldog.com/examples/images1.html";
            String e = "http://portquiz.net:8080/";
            String f = "http://www.utdallas.edu/os.html";
            //System.out.println(url);
            ui.loadURL(url.replaceFirst("http://", ""));
        }
    }

    // Form variables
    private JTextField addressBar;
    private JButton goButton;
    private JScrollPane jScrollPane1;
    public JEditorPane viewArea;
}