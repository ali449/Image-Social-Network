package com.shediz.spamdetect;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ModelHelper
{
    public static void saveModel(Model model, String savePath)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(savePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(model);
            oos.close();
            fos.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static Model loadModel(String modelPath)
    {
        try
        {
            return loadModel(new FileInputStream(modelPath));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Model loadModel(InputStream modelInputStream)
    {
        Model model = null;
        try
        {
            ObjectInputStream ois = new ObjectInputStream(modelInputStream);
            model = (Model) ois.readObject();
            ois.close();
            modelInputStream.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return model;
    }

    public static List<String> getListFileNames(ClassLoader classLoader, String path)
    {
        List<String> filenames = new ArrayList<>();

        try (InputStream in = classLoader.getResourceAsStream(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(in)))
        {
            String resource;

            while ((resource = br.readLine()) != null)
                filenames.add(resource);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return filenames;
    }
}

