package com.destroflyer.nordicworld.shared;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;

public class FileManager {

    public static boolean existsFile(String filePath){
        File file = new File(filePath);
        return file.exists();
    }

    public static String getFileContent(String filePath) {
        String text = "";
        String[] lines = getFileLines(filePath);
        for(int i=0;i<lines.length;i++){
            if(i != 0){
                text += "\n";
            }
            text += lines[i];
        }
        return text;
    }

    public static String[] getFileLines(String filePath) {
        LinkedList<String> linesList = new LinkedList<>();
        try{
            BufferedReader reader;
            if(filePath.startsWith("http:")){
                reader = new BufferedReader(new InputStreamReader(new URL(filePath).openStream()));
            }
            else{
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
            }
            String line;
            while((line = reader.readLine()) != null){
                linesList.add(line);
            }
            reader.close();
        }catch(Exception ex){
            System.out.println("Error while reading file: " + ex.toString());
        }
        String[] lines = new String[linesList.size()];
        linesList.toArray(lines);
        return lines;
    }

    public static void putFileContent(String filePath, String content){
        try{
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));
            String[] lines = content.split("\n");
            for(int i=0;i<lines.length;i++){
                if(i != 0){
                    writer.newLine();
                }
                writer.write(lines[i]);
            }
            writer.close();
        }catch(Exception ex){
            System.out.println("Error while writing file: " + ex.toString());
        }
    }
}
