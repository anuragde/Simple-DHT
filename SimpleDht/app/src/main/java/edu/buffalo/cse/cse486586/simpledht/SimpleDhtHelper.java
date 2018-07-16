package edu.buffalo.cse.cse486586.simpledht;

import android.util.Log;

import java.util.ArrayList;
import java.util.TreeMap;

public class SimpleDhtHelper {
    static void printNodeList(ArrayList<Node> tA) {

        for (Node n : tA) {

            System.out.println(n.node_id +" "+ n.pred.node_id  +" "+n.succ.node_id);
        }
        try{
            System.out.println(SimpleDhtProvider.getMyNode().node_id+SimpleDhtProvider.getMyNode().pred.node_id+SimpleDhtProvider.getMyNode().succ.node_id);
        }
        catch (Exception ec){

        }


        return;
    }
    static void addNewNodeTreeMap(String newNodeID, String newNodeHashedId) {
        Node myNode = SimpleDhtProvider.getMyNode();
        Node newNode1 = new Node(newNodeID, newNodeHashedId);
        TreeMap<String,Node> tvMap = SimpleDhtProvider.getTreeMap();
        tvMap.put(newNodeHashedId, newNode1);
        SimpleDhtProvider.setTreeMap(tvMap);
        String[] mapKeys = new String[tvMap.size()];
        int pos = 0;
        int keyPos = 0;
        for(String key : tvMap.keySet()){
            mapKeys[pos]=key;

            if(key.equals(newNodeHashedId)){
                keyPos = pos;
            }
            pos++;
        }
        if(myNode.node_id.equals(newNodeID)){
            if(keyPos == 0){
                myNode.pred = tvMap.get(tvMap.lastKey());
                newNode1.pred = tvMap.get(tvMap.lastKey());
                newNode1.pred.succ =newNode1;
                tvMap.put(tvMap.lastKey(),newNode1.pred);
            }
            else{
                myNode.pred = tvMap.get(mapKeys[keyPos-1]);
                newNode1.pred = tvMap.get(mapKeys[keyPos-1]);
                newNode1.pred.succ =newNode1;
                tvMap.put(mapKeys[keyPos-1],newNode1.pred);
            }
            if(keyPos == tvMap.size()-1){
                myNode.succ = tvMap.get(mapKeys[0]);
                newNode1.succ = tvMap.get(mapKeys[0]);
                newNode1.succ.pred =newNode1;
                tvMap.put(mapKeys[0],newNode1.succ);
            }
            else{
                myNode.succ = tvMap.get(mapKeys[keyPos+1]);
                newNode1.succ = tvMap.get(mapKeys[keyPos+1]);
                newNode1.succ.pred =newNode1;
                tvMap.put(mapKeys[keyPos+1],newNode1.succ);
            }


        }
        else{
            if(keyPos == 0){
                newNode1.pred = tvMap.get(tvMap.lastKey());
                newNode1.pred.succ =newNode1;
                if(newNode1.pred.node_id.equals(myNode.node_id)){
                    myNode.succ =newNode1;
                }
                tvMap.put(tvMap.lastKey(),newNode1.pred);

            }

            else{
                newNode1.pred = tvMap.get(mapKeys[keyPos-1]);
                newNode1.pred.succ =newNode1;
                if(newNode1.pred.node_id.equals(myNode.node_id)){
                    myNode.succ =newNode1;
                }
                tvMap.put(mapKeys[keyPos-1],newNode1.pred);

            }

            if(keyPos == tvMap.size()-1){
                newNode1.succ = tvMap.get(mapKeys[0]);
                newNode1.succ.pred =newNode1;
                if(newNode1.succ.node_id.equals(myNode.node_id)){
                    myNode.pred =newNode1;
                }
                tvMap.put(mapKeys[0],newNode1.succ);

            }

            else{
                newNode1.succ = tvMap.get(mapKeys[keyPos+1]);
                newNode1.succ.pred =newNode1;
                if(newNode1.succ.node_id.equals(myNode.node_id)){
                    myNode.pred =newNode1;
                }
                tvMap.put(mapKeys[keyPos+1],newNode1.succ);

            }

        }
        SimpleDhtProvider.setMyNode(myNode);
        ArrayList<Node> temp = SimpleDhtProvider.getNodeList();
        temp.add(newNode1);
        SimpleDhtProvider.setNodeList(temp);
        tvMap.put(newNodeHashedId, newNode1);
        SimpleDhtProvider.setTreeMap(tvMap);
        return;
    }

    static boolean getConnected(String id){
        switch(Integer.parseInt(id)) {
            case 11108:
                return SimpleDhtProvider.connected[0] == true;
            case 11112:
                return SimpleDhtProvider.connected[1] == true;
            case 11116:
                return SimpleDhtProvider.connected[2] == true;
            case 11120:
                return SimpleDhtProvider.connected[3] == true;
            case 11124:
                return SimpleDhtProvider.connected[4] == true;
        }
        return false;
    }
    static void printConnected(){
        Log.v("ServerTask",Boolean.toString(SimpleDhtProvider.connected[0]));
        Log.v("ServerTask",Boolean.toString(SimpleDhtProvider.connected[1]));
        Log.v("ServerTask",Boolean.toString(SimpleDhtProvider.connected[2]));
        Log.v("ServerTask",Boolean.toString(SimpleDhtProvider.connected[3]));
        Log.v("ServerTask",Boolean.toString(SimpleDhtProvider.connected[4]));

    }
    static void updateConnected(String id){
        switch(Integer.parseInt(id)) {
            case 11108:
                SimpleDhtProvider.connected[0] = true;
                break;
            case 11112:
                SimpleDhtProvider.connected[1] = true;
                break;
            case 11116:
                SimpleDhtProvider.connected[2] = true;
                break;
            case 11120:
                SimpleDhtProvider.connected[3] = true;
                break;
            case 11124:
                SimpleDhtProvider.connected[4] = true;
                break;
        }
        return;
    }

}
