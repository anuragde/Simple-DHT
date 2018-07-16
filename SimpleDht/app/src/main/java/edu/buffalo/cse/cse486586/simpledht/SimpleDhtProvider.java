package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.ContactsContract;
import android.renderscript.Sampler;
import android.telephony.TelephonyManager;
import android.util.Log;

import static android.content.ContentValues.TAG;
import static java.lang.Float.parseFloat;

class MyContextWrapper extends ContextWrapper{
    public MyContextWrapper(Context base){
        super(base);
    }
    public String getPort(){
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        return myPort;
    }
}
public class SimpleDhtProvider extends ContentProvider {
    public static boolean connected[] = new boolean[]{false,false,false,false,false};
    Context context = SimpleDhtActivity.getAppContext();



    public static String portNo="";
    static Node myNode;

    public static TreeMap<String,Node> tv = new TreeMap<String,Node>();
    public static ArrayList<Node> nodeList= new ArrayList<Node>();

    static ArrayList<Node> getNodeList(){
        return  nodeList;
    }

    static void setNodeList(ArrayList<Node> nodeList1){
        nodeList = nodeList1;
    }

    static Node getMyNode(){
        return myNode;
    }
    static void setMyNode(Node node){
         myNode = node;
    }
    public static void setTreeMap(TreeMap<String,Node> tv1){
        tv = tv1;
    }

    public static TreeMap getTreeMap(){
        return tv;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        String fileName = selection;

        if (selection.equals("@")) {
            Log.v("File input stream", fileName);
            String[] fileList = getContext().fileList();
            Log.v("File input stream", Integer.toString(fileList.length));

            for (int i = 0; i < fileList.length; i++) {
                Log.v("File input stream", fileList[i]);
                try {
                    fileName = fileList[i];
                    getContext().deleteFile(fileName);
                } catch (Exception e) {
                    Log.e("Exception Thrown", "Exception Thrown");
                }
            }
        }
        else if (selection.equals("*")) {
            sendDeleteReq();
        }
        else {
            try {
                getContext().deleteFile(fileName);
            } catch (Exception e) {
                Log.e("Exception Thrown", "Exception Thrown");
            }
        }
        return 0;

    }
    public int deleteReq() {

        // TODO Auto-generated method stub
        String fileName = "";

            Log.v("File input stream", fileName);
            String[] fileList = getContext().fileList();
            Log.v("File input stream", Integer.toString(fileList.length));

            for (int i = 0; i < fileList.length; i++) {
                Log.v("File input stream", fileList[i]);
                try {
                    fileName = fileList[i];
                    context.deleteFile(fileName);
                } catch (Exception e) {
                    Log.e("Exception Thrown", "Exception Thrown");
                }
            }
            return 0;
    }
    public void respondQuery(String reqNode,String filename) {
        String line="";
        try {
            Log.v("File input stream", filename);
            FileInputStream in = context.openFileInput(filename);
            /*  Log.e(TAG, "File inputStreamReader.");*/
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            line = bufferedReader.readLine();
            sb.append(line);
            line = sb.toString();
            in.close();

        } catch (Exception e) {
            Log.e(TAG, "File read failed...");
            e.printStackTrace();
        }
     
        try {

            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(reqNode));
            OutputStream outToServer = socket.getOutputStream();

            DataOutputStream out1 = new DataOutputStream(outToServer);
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String sendReq ="aDel+queryResponse+aDel+1";
            out1.writeUTF(sendReq+"\n");
            out1.flush();
            out1.writeUTF("aDel"+filename+"aDel"+line+"\n");
            out1.flush();

        }
        catch(Exception ex){
            ex.printStackTrace();
            Log.e("ClientTask","sendToResponsibleNode Error");
        }


    }


    public  void respondQueryTotal(String reqNode) {
        String filename = "";
        String line = "";
        Log.v("File input stream", "respondQueryTotal");
        String[] fileList = context.fileList();
        Log.v("File input stream", Integer.toString(fileList.length));

       try {
           Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(reqNode));
           DataOutputStream out1 = new DataOutputStream(socket.getOutputStream());
           String sendReq = "aDel"+"queryResponseTotal"+"aDel" + Integer.toString(fileList.length) + "aDel";
           out1.writeUTF(sendReq + "\n");
           out1.flush();

           Log.v("File input stream", "queryResponseTotal");
           for (int i = 0; i < fileList.length; i++) {
               Log.v("File input stream", fileList[i]);

               try {
                   filename = fileList[i];
                   FileInputStream in = context.openFileInput(filename);
                   /*  Log.e(TAG, "File inputStreamReader.");*/
                   InputStreamReader inputStreamReader = new InputStreamReader(in);
                   BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                   StringBuilder sb = new StringBuilder();
                   line = bufferedReader.readLine();
                   sb.append(line);
                   in.close();
                   line = sb.toString();
                   out1.writeUTF(filename+"aDel"+line + "\n");
                   out1.flush();
               } catch (Exception e) {
                   Log.e(TAG, "File read failed...");
                   e.printStackTrace();
               }


           }
       }
       catch (Exception e){

       }

    }




    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    public void insertOtherNode(String key,String value) {


        // TODO Auto-generated method stub
        Log.v("insert", value);
        String filename = key;
        String string = value;
        String hashedKey="";
            Log.v("Created " + hashedKey, "with value " + string + "Before hash " + filename);
            FileOutputStream outputStream;
            try {//Context.MODE_PRIVATE
                System.out.println(filename);
                System.out.println(context);
                outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File write failed");
            }


    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        Log.v("insert", values.toString());
        String filename = values.getAsString("key");
        String string = values.getAsString("value");
        String hashedKey="";
        Node responsibleNode;
        try{
            hashedKey = genHash(filename);
        }
        catch(Exception e){
        }
        responsibleNode = myNode.lookUp(hashedKey);
        if ((myNode.node_id).equals(responsibleNode.node_id)) {
            Log.v("Created " + filename, "with value " + string );
            FileOutputStream outputStream;
            try {//Context.MODE_PRIVATE
                outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                Log.e("Exception Thrown", "Exception Thrown");
                Log.e(TAG, "File write failed");
            }
        }
        else
           sendToResponsibleNode(filename,string,responsibleNode.node_id);
        return uri;
    }
    void sendToResponsibleNode(String key, String value, String node_id) {
        try {

            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(node_id));
            DataOutputStream out1 = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String sendReq ="aDel+insertRequest+aDel"+key+"aDel"+value;
            out1.writeUTF(sendReq+"\n");
            out1.flush();
            Log.v("send To R Node","sent insert req");

        }
        catch(Exception ex){
            ex.printStackTrace();
            Log.e("ClientTask","sendToResponsibleNode Error");
        }
    }
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    void sendDeleteReq(){

        Log.v("ServerTask", "Sending updated node list");
        String remotePort[] = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};

        for (int i = 0; i < 5; i++) {

            if(SimpleDhtHelper.getConnected(remotePort[i])) {
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort[i]));

                    String sendReq = "deleteReq";
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("aDel" + sendReq + "\n");
                    out.flush();

                } catch (Exception ex) {
                    Log.e("ServerTask", "Sending deleteReq to " + remotePort[i] + " fail");
                    ex.printStackTrace();
                }
            }
            else
            {
                Log.e("ServerTask", "Sending deleteReq to " + remotePort[i] + " skipped");
            }
        }
        return;
    }



    public boolean onCreate() {
        String hashedPort="";
        portNo = (new MyContextWrapper(getContext())).getPort();
        try{
            Log.v(TAG,"Attempting to hash port no.");
            hashedPort = genHash(Integer.toString(Integer.parseInt(portNo)/2));
            Log.v(TAG + "portNo",portNo);
            Log.v(TAG,"Hash Success");
        }
        catch(Exception exception){
            if((hashedPort!=null && !hashedPort.isEmpty()) || (portNo!=null&& !portNo.isEmpty()))
                Log.v("PortNumber: "+portNo,hashedPort);
            else if(hashedPort!=null && !hashedPort.isEmpty())
                Log.v(TAG,"portNo is null");
            else if(portNo!=null || !hashedPort.isEmpty())
                Log.v(TAG,"hashedPort is null");
            Log.e("onCreate()","Node creation Exception");
            Log.e("exception",exception.toString());
        }

        myNode = new Node(portNo,hashedPort);
        Log.v("onCreate",getNodeList().toString());


        try {
            Log.v(TAG, "Attempting to create a ServerSocket");
            ServerSocket serverSocket = new ServerSocket(10000);
            Log.v(TAG, "Creating server task");
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            Log.v(TAG, "ServerSocket created successfully");
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            e.printStackTrace();
            return false;
        }
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        return true;
    }
    boolean waitingFlag = true;

    static BlockingQueue<String[]> reqQue = new LinkedBlockingDeque<String[]>();
    static BlockingQueue<ArrayList<String[]>> reqQueTotal = new LinkedBlockingDeque<ArrayList<String[]>>();
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});
        String filename = selection;
        String line = "";
              if (selection.equals("@")) {

            Log.v("File input stream", filename);
            String[] fileList = getContext().fileList();
            Log.v("File input stream", Integer.toString(fileList.length));

            for (int i = 0; i < fileList.length; i++) {
                Log.v("File input stream", fileList[i]);

                try {
                    filename = fileList[i];
                    FileInputStream in = getContext().openFileInput(filename);
                    /*  Log.e(TAG, "File inputStreamReader.");*/
                    InputStreamReader inputStreamReader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder sb = new StringBuilder();
                    line = bufferedReader.readLine();
                    sb.append(line);
                    in.close();
                } catch (Exception e) {
                    Log.e(TAG, "File read failed...");
                    e.printStackTrace();
                }
                MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                builder.add("key", filename);
                builder.add("value", line);
                Log.v(filename, line);

            }
            matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);


            return matrixCursor;


        }
        else if (selection.equals("*")) {

            try {
                int count = 0;
                sendQueryReq();
                Log.v("sendQueryReq",Integer.toString(connected.length));

                String[] fileList = getContext().fileList();
                Log.v("File input stream", Integer.toString(fileList.length));

                for (int i = 0; i < fileList.length; i++) {
                    Log.v("File input stream", fileList[i]);

                    try {
                        filename = fileList[i];
                        FileInputStream in = getContext().openFileInput(filename);
                        /*  Log.e(TAG, "File inputStreamReader.");*/
                        InputStreamReader inputStreamReader = new InputStreamReader(in);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        StringBuilder sb = new StringBuilder();
                        line = bufferedReader.readLine();
                        sb.append(line);
                        in.close();
                    } catch (Exception e) {
                        Log.e(TAG, "File read failed...");
                        e.printStackTrace();
                    }
                    MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                    builder.add("key", filename);
                    builder.add("value", line);
                    Log.v(filename, line);

                }

                for(int i=0;i<connected.length;i++){
                    if(connected[i] == true)
                        count++;
                }
                Log.v("sendQueryReq",Integer.toString(count));

                ArrayList<String[]> temp;

                for(int i=0;i<count-1;i++){
                    temp = reqQueTotal.take();
                    Log.v("waiting","waiting completed");
                    for(String[] result : temp){
                        MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                        builder.add("key",result[0] );
                        builder.add("value", result[1]);
                        Log.v(result[0],result[1]);

                    }

                }

            } catch (Exception e) {
                Log.e(TAG, "File read failed...");
                e.printStackTrace();
            }
                  matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);

                  return matrixCursor;


            //Log.v("query", selection);
        } else {
            String hashedKey="";
            Node responsibleNode;
            try{
                hashedKey = genHash(filename);
            }
            catch(Exception e){
            }
            responsibleNode = myNode.lookUp(hashedKey);
            if ((myNode.node_id).equals(responsibleNode.node_id)) {
                try {
                    Log.v("File input stream", filename);
                    FileInputStream in = getContext().openFileInput(filename);
                    /*  Log.e(TAG, "File inputStreamReader.");*/
                    InputStreamReader inputStreamReader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    StringBuilder sb = new StringBuilder();
                    line = bufferedReader.readLine();
                    sb.append(line);
                    in.close();
                } catch (Exception e) {
                    Log.e(TAG, "File read failed...");
                    e.printStackTrace();
                }


                MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                builder.add("key", selection);
                builder.add("value", line);

                matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);
                //Log.e("query", line);
                return matrixCursor;
                //Log.v("query", selection);
            }
            else{
                try {

                    getResponsibleNode(myNode.succ.node_id,filename);
                    MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                    String result[]  =reqQue.take();
                    Log.v("waiting","waiting completed");
                    builder.add("key",result[1] );
                    builder.add("value", result[2]);
                    matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);
                } catch (Exception e) {
                    Log.e(TAG, "File read failed...");
                    e.printStackTrace();
                }

                return matrixCursor;


            }
        }
    }
    public void sendQueryReq(){
        Log.v("ServerTask", "Sending updated node list");
        String remotePort[] = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};

        for (int i = 0; i < 5; i++) {
                sendQueryReq1(remotePort[i]);

        }
        return;
    }
    void sendQueryReq1(String nodeId){

            if(SimpleDhtHelper.getConnected(nodeId) && (Integer.parseInt(nodeId) != Integer.parseInt(myNode.node_id))) {
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(nodeId));

                    String sendReq = "queryReqTotal"+"aDel"+myNode.node_id+"aDel";
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("aDel" + sendReq + "\n");
                    out.flush();
                    DataInputStream in = new DataInputStream(socket.getInputStream());

                    try{
                        socket.setSoTimeout(1500);
                        String messageReceived = in.readUTF();
                    }
                    catch (Exception e){
                        sendQueryReq1(nodeId);
                        e.printStackTrace();

                    }
                    Log.e("ServerTask", "Sending queryReqTotal to " + nodeId + " success");

                } catch (Exception ex) {
                    Log.e("ServerTask", "Sending queryReqTotal to " + nodeId + " fail");
                    ex.printStackTrace();
                }
            }
            else
            {
                Log.e("ServerTask", "Sending queryReqTotal to " + nodeId + " skipped");
            }


        return;
    }

    public String getHash(String s) {
        try {
            return genHash(s);
        } catch (Exception e) {

        }
        return null;
    }
    public void getResponsibleNode(String nodeID,String filename){
        try {

            Log.v(myNode.node_id,myNode.succ.node_id);
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(nodeID));
            DataOutputStream out1 = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String sendReq ="aDel+queryRequest+aDel"+filename+"aDel"+myNode.node_id+"aDel";
            out1.writeUTF(sendReq+"\n");
            out1.flush();
            Log.v("send To R Node","sent insert req");
        }
        catch(Exception ex){
            ex.printStackTrace();
            Log.e("ClientTask","sendToResponsibleNode Error");
        }

    }
    public void getResponsibleNode1(String reqNode,String nodeID,String filename){
        try {

            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(nodeID));
            DataOutputStream out1 = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String sendReq ="aDel+queryRequest+aDel"+filename+"aDel"+reqNode+"aDel";
            out1.writeUTF(sendReq+"\n");
            out1.flush();
            Log.v("send To R Node","sent insert req");
        }
        catch(Exception ex){
            ex.printStackTrace();
            Log.e("ClientTask","sendToResponsibleNode Error");
        }

    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}

class ServerTask extends AsyncTask<ServerSocket, String, Void> {

    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    SimpleDhtProvider sdp = new SimpleDhtProvider();


    private void joinRequest(String newNodeID, String newNodeHashId,Socket serverS) {
        Log.v("ServerTask", "Accepting JoinRequest");
        Node newNode = new Node(newNodeID, newNodeHashId);

        try{
        DataOutputStream out = new DataOutputStream(serverS.getOutputStream());
        Log.v("ServerTask", "JoinRequest completed");
        sendJoinAck(out,newNode.node_id);
        sendIntialList(out,newNode.node_id);
            SimpleDhtHelper.updateConnected(newNode.node_id);
        String[] nodeDetails = new String[]{newNodeID, newNodeHashId};
        sendUpdatedList(nodeDetails);
        }
        catch (Exception e){
            //e.printStackTrace();
        }
    }
    
    
    void sendUpdatedList(String[] nodeDetails){
        Log.v("ServerTask", "Sending updated node list");
        String remotePort[] = {REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4};
        SimpleDhtHelper.printConnected();
        for (int i = 0; i < 5; i++) {
            if(SimpleDhtHelper.getConnected(remotePort[i])) {
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(remotePort[i]));

                    String sendReq = "updatedNodeList";
                    SimpleDhtHelper.printNodeList(SimpleDhtProvider.getNodeList());
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF("aDel" + sendReq + "aDel" + nodeDetails[0] + "aDel" + nodeDetails[1] + "\n");
                    out.flush();

                } catch (Exception ex) {
                    Log.e("ServerTask", "Sending updated node list to " + remotePort[i] + " fail");
                    ex.printStackTrace();
                }
            }
            else
            {
                Log.e("ServerTask", "Sending updated node list to " + remotePort[i] + " skipped");
            }
        }
        return;
    }
    void sendIntialList(DataOutputStream out,String REMOTE_PORT){
        Log.v("ServerTask", "Sending intial node list");

        try {

            out.writeUTF("aDel" + "initialNodeList" + "\n" );
            String sendReq = "intialNodeList";
            SimpleDhtHelper.printNodeList(SimpleDhtProvider.getNodeList());
            out.write(SimpleDhtProvider.getNodeList().size());
            for (Node n : SimpleDhtProvider.getNodeList()) {
                out.writeUTF("aDel" + sendReq + "aDel" + n.node_id + "aDel" + n.hashedId + "\n" );
                out.flush();


            }
            Log.v("ServerTask", "Sending initial node list to " + REMOTE_PORT + " success");

        } catch (Exception ex) {
            Log.e("ServerTask", "Sending initial node list to " + " fail");
        }
        return;
    }

    void sendJoinAck(DataOutputStream out,String nodeId) {
        try {
            Log.v("ServerTask","Sending joinAck" + "\n");
            out.writeUTF("aDel"+"joinReqCompleted" + "\n" );
            out.flush();

        } catch (Exception ex) {
            Log.e("ServerTask","sendJoinAck error");
        }
        return;
    }

   
   
    @Override
    protected Void doInBackground(ServerSocket... sockets) {
        ServerSocket serverSocket = sockets[0];
        try {

            while (true) {
                try {
                    Log.v("ServerTask", "serverS waiting for data");
                    Socket serverS = serverSocket.accept();
                    Log.v("ServerTask", "Data received");
                    InputStream inputStream = serverS.getInputStream();
                    DataInputStream in = new DataInputStream(inputStream);
                    String msgReceived = "";
                    msgReceived = in.readUTF();
                    String receivedMessage = msgReceived;

                    if (receivedMessage.isEmpty() || receivedMessage.equals("")) {
                        Log.v("ServerTask", "Received msg empty");

                    }

                    Log.v("ServerTask", "receivedMessage from " + serverS.getInetAddress() + " " + receivedMessage);

                    String newNodeID = "", newNodeHashId = "";
                    String nodeIds[];
                    String fileName = "";
                    if (receivedMessage.contains("joinRequest")) {
                        try {
                            nodeIds = receivedMessage.split("aDel");
                            newNodeID = nodeIds[2];
                            newNodeHashId = nodeIds[3];
                        } catch (Exception e) {
                            Log.e("ServerTask", "EOFException");
                            e.printStackTrace();
                        }

                        Log.v("ServerTask", "Received joinRequest from " + newNodeID);
                        joinRequest(newNodeID, newNodeHashId, serverS);
                    }
                    if (receivedMessage.contains("queryRequest")) {
                        SimpleDhtProvider sdh = new SimpleDhtProvider();

                        nodeIds = receivedMessage.split("aDel");
                        fileName = nodeIds[2];
                        String reqNode = nodeIds[3];
                        String hashedKey = "";
                        Node responsibleNode;
                        try {
                            hashedKey = sdh.getHash(fileName);
                        } catch (Exception e) {
                        }
                        responsibleNode = SimpleDhtProvider.myNode.lookUp(hashedKey);
                        Log.v(hashedKey, fileName);
                        if ((SimpleDhtProvider.myNode.node_id).equals(reqNode)) {
                            Log.e("ServerTask", "Received queryRequest to self");

                        } else if ((SimpleDhtProvider.myNode.node_id).equals(responsibleNode.node_id)) {
                            sdh.respondQuery(reqNode, fileName);
                        } else {
                            sdh.getResponsibleNode1(reqNode, SimpleDhtProvider.myNode.succ.node_id, fileName);
                        }

                        Log.v("ServerTask", "Received queryRequest  ");

                    }

                    if (receivedMessage.contains("queryResponseTotal")) {
                        ArrayList<String[]> arrayList = new ArrayList<String[]>();
                        String numLines = receivedMessage.split("aDel")[1];
                        String numLines1 = receivedMessage.split("aDel")[2];
                        Log.v(numLines, numLines1);
                        String result[] = {};
                        for (int i = 0; i < Integer.parseInt(numLines1); i++) {
                            receivedMessage = in.readUTF();
                            result = receivedMessage.split("aDel");
                            arrayList.add(result);
                            Log.v("ServerTask Res", result[0] + " " + result[1]);

                        }

                        SimpleDhtProvider.reqQueTotal.add(arrayList);
                        Log.v("ServerTask", "Received queryResponseTotal ");

                    } else if (receivedMessage.contains("queryResponse")) {
                        msgReceived = in.readUTF();
                        String result[] = msgReceived.split("aDel");
                        try {
                            SimpleDhtProvider.reqQue.put(result);
                        } catch (Exception e) {

                        }
                        Log.v("ServerTask Res", result[1] + " " + result[2]);
                        Log.v("ServerTask", "Received queryResponse ");

                    }

                    if (receivedMessage.contains("queryReqTotal")) {
                        DataOutputStream outputStream = new DataOutputStream(serverS.getOutputStream());
                        outputStream.writeUTF("Ack" + "\n");
                        outputStream.flush();
                        SimpleDhtProvider sdh = new SimpleDhtProvider();
                        String reqNodeId = msgReceived.split("aDel")[2];
                        sdh.respondQueryTotal(reqNodeId);
                        Log.v("ServerTask", "Received queryRequestTotal ");

                    }
                    if (receivedMessage.contains("insertRequest")) {
                        Log.v("ServerTask", "Received insertRequest ");

                        nodeIds = msgReceived.split("aDel");
                        String key = nodeIds[2];
                        String value = nodeIds[3];
                        SimpleDhtProvider sdh = new SimpleDhtProvider();
                        sdh.insertOtherNode(key, value);

                    }
                    if (receivedMessage.contains("deleteRequest")) {
                        SimpleDhtProvider sdh = new SimpleDhtProvider();
                        sdh.deleteReq();
                    }
                    if (msgReceived.contains("updatedNodeList")) {
                        Log.v("ServerTask", " before updatedNodeList");
                        SimpleDhtHelper.printNodeList(SimpleDhtProvider.getNodeList());
                        try {
                            nodeIds = msgReceived.split("aDel");
                            newNodeID = nodeIds[2];
                            SimpleDhtHelper.updateConnected(newNodeID);
                            newNodeHashId = nodeIds[3];
                            SimpleDhtHelper.addNewNodeTreeMap(newNodeID, newNodeHashId);
                        } catch (Exception ex) {
                            Log.e("ServerTask", "Class Not found");
                            ex.printStackTrace();
                        }
                        Log.v("ServerTask", " after updatedNodeList");
                        SimpleDhtHelper.printNodeList(SimpleDhtProvider.getNodeList());
                    }
                    in.close();
                }catch (IOException e) {
                    Log.e("ServerTask", "Server IO Exception");
                    e.printStackTrace();
                }
            }
            }
        catch (Exception e) {
            Log.e("ServerTask", "Server IO Exception");
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    protected void onProgressUpdate(String... strings) {
        String strReceived = strings[0].trim();

        ContentValues keyValueToInsert = new ContentValues();
        keyValueToInsert.put("value", strReceived);

    }
}



class ClientTask extends AsyncTask<String, Void, Void> {
    static final String REMOTE_PORT0 = "11108";
    void sendJoinRequest( DataOutputStream out) {
        try {
            String sendReq ="joinRequest";
            Log.v("ClientTask" ,"sending"+ sendReq);
            Log.v("ClientTask",SimpleDhtProvider.getMyNode().node_id);
            out.writeUTF("aDel"+sendReq+"aDel"+SimpleDhtProvider.getMyNode().node_id+"aDel"+SimpleDhtProvider.getMyNode().hashedId +"\n");

           Log.v("ClientTask","aDel"+sendReq+"aDel"+SimpleDhtProvider.getMyNode().node_id+"aDel"+SimpleDhtProvider.getMyNode().hashedId +"\n");

            out.flush();

            Log.v("ClientTask" ,"sendJoinRequest"+" Success");
        } catch (IOException io) {
            Log.e("Exception Thrown",io.toString());
        }
        return;
    }
   
 


    @Override
    protected Void doInBackground(String... msgs) {
        Log.v("ClientTask","ClientTask invoked");
        try {
            Log.v("ClientTask","ClientTask invoked1");
            OutputStream outToServer;

            Log.v("ClientTask","Before If");

            Log.v("ClientTask portNo",SimpleDhtProvider.portNo);
            Log.v("ClientTask REMOTE_PORT0",REMOTE_PORT0);
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(REMOTE_PORT0));
            outToServer = socket.getOutputStream();
            String nodeIds[],newNodeID,newNodeHashId;

            DataOutputStream out = new DataOutputStream(outToServer);

            sendJoinRequest(out);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            String msgReceived="";
            msgReceived = in.readUTF();
            if(msgReceived!=null) {

                Log.v("Client", msgReceived);
            }
            msgReceived = in.readUTF();
            if(msgReceived!=null) {

                Log.v("Client", msgReceived);
            }
            if (msgReceived.contains("initialNodeList")) {
                int size =0;
                size = in.read();

                Log.v("ServerTask", " before initialNodeList");
                SimpleDhtHelper.printNodeList(SimpleDhtProvider.getNodeList());
                for(int i=0;i<size;i++) {
                    try {
                        msgReceived = in.readUTF();

                        nodeIds = msgReceived.split("aDel");
                        Log.v("Client", msgReceived);
                        newNodeID = nodeIds[2];
                        newNodeHashId = nodeIds[3];
                        SimpleDhtHelper.updateConnected(newNodeID);
                        SimpleDhtHelper.addNewNodeTreeMap(newNodeID, newNodeHashId);
                    } catch (Exception ex) {
                        Log.e("ClientTask", "Class Not found");
                        ex.printStackTrace();
                    }

                }
                Log.v("ClientTask", " after intialNodeList");
                SimpleDhtHelper.printNodeList(SimpleDhtProvider.getNodeList());
            }

            out.close();

        }
        catch (IOException e) {
            Log.e(TAG, "ClientTask socket IOException");
                e.printStackTrace();
        }
        return null;
    }
}
