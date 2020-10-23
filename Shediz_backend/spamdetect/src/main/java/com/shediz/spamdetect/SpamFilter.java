package com.shediz.spamdetect;

import ir.ac.iust.nlp.jhazm.Normalizer;
import ir.ac.iust.nlp.jhazm.WordTokenizer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class SpamFilter
{
    private final Normalizer normalizer = new Normalizer();
    private WordTokenizer wordTokenizer;

    private List<String> stopWords;

    private Model model;

    public SpamFilter()
    {
        try
        {
            wordTokenizer = new WordTokenizer();
            stopWords = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("stopwords.txt")),
                    StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private List<String> cleanText(final String inputText)
    {
        String text = inputText.replaceAll("((http|https)://)?[a-zA-Z0-9./?:@\\-_=#]+\\.([a-zA-Z]){2,6}([a-zA-Z0-9.&/?:@\\-_=#])*", " تارنما ");
        //text = text.replaceAll("[^آ-ی \u200ca-zA-Z0-9]", "");

        StringBuilder stopWordsRegex = new StringBuilder("\\b(");
        stopWords.forEach(stopWord -> stopWordsRegex.append(stopWord).append("|"));
        stopWordsRegex.append(")\\b");
        text = text.replaceAll(stopWordsRegex.toString(), "");

        text = normalizer.run(text);

        List<String> tokens = wordTokenizer.tokenize(text);

        tokens.removeIf(token -> token.length() <= 1);

        return tokens;
    }

    private List<String> words(InputStream inputStream)
    {
        ArrayList<String> allWords = new ArrayList<>();

        try
        {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1)
                result.write(buffer, 0, length);

            allWords.addAll(cleanText(result.toString(StandardCharsets.UTF_8.name())));
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return allWords;
    }

    void buildModel()
    {
        int k = 5;

        final HashMap<String, MutableInt> spamDistribution = new HashMap<>();
        final HashMap<String, MutableInt> hamDistribution = new HashMap<>();

        for (String fileName: ModelHelper.getListFileNames(getClass().getClassLoader(), "dataset/spamtraining"))
        {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(Paths.get("dataset/spamtraining/" + fileName).toString());
            assert inputStream != null;
            List<String> fileWords = words(inputStream);
            for (String word: fileWords)
            {
                MutableInt count = spamDistribution.get(word);
                if (count == null)
                {
                    MutableInt two = new MutableInt();
                    two.increment();
                    spamDistribution.put(word, two);
                }
                else
                    count.increment();
            }
        }

        for (String fileName: ModelHelper.getListFileNames(getClass().getClassLoader(), "dataset/hamtraining"))
        {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(Paths.get("dataset/hamtraining/" + fileName).toString());
            assert inputStream != null;
            List<String> fileWords = words(inputStream);
            for (String word: fileWords)
            {
                MutableInt count = hamDistribution.get(word);
                if (count == null)
                {
                    MutableInt two = new MutableInt();
                    two.increment();
                    hamDistribution.put(word, two);
                }
                else
                    count.increment();
            }
        }

        spamDistribution.values().removeIf(value -> value.get() < k);
        hamDistribution.values().removeIf(value -> value.get() < k);


        try
        {
            //This is not working in jar, we most first build model with normal run and then package module as jar
            File modelFile = new File("spamdetect/src/main/resources/spam_filter.model");
            if (modelFile.exists())
                modelFile.delete();
            if (modelFile.createNewFile())
                ModelHelper.saveModel(new Model(spamDistribution, hamDistribution), modelFile.getPath());
            else
                System.out.println("Error in create model file");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void loadModel()
    {
        model = ModelHelper.loadModel(getClass().getClassLoader().getResourceAsStream("spam_filter.model"));
    }

    private float probability(String word)
    {
        /*
        NS = the number of times w appeared in spam
        TS = total number of words in spam
        NH = the number of times w appeared in ham
        TH = total number of words in spam
        P(S | w) = (NS/TS) / ((NS/TS) + (2 * NH))
        */

        HashMap<String, MutableInt> spamDistribution = model.getSpamDistribution();
        HashMap<String, MutableInt> hamDistribution = model.getHamDistribution();

        Set<String> spamKeys = spamDistribution.keySet();
        Set<String> hamKeys = hamDistribution.keySet();
        int numOfHams = hamDistribution.values().stream().map(MutableInt::get).reduce(0, Integer::sum);
        int numOfSpams = spamDistribution.values().stream().map(MutableInt::get).reduce(0, Integer::sum);

        if (spamKeys.contains(word) && !hamKeys.contains(word) && spamDistribution.get(word).get() > 10)
            return 0.9999f;
        else if (hamKeys.contains(word) && !spamKeys.contains(word))
            return 0.0001f;
        else if (!(spamKeys.contains(word) || hamKeys.contains(word)))
            return 0.4f;
        else
        {
            float numerator = spamDistribution.getOrDefault(word, new MutableInt()).get() / (float) numOfSpams;
            float denominator = numerator + ((2 * hamDistribution.getOrDefault(word, new MutableInt()).get())
                    / (float) numOfHams);
            return numerator / denominator;
        }
    }

    public Boolean isSpamText(String text)
    {
        return isSpam(cleanText(text));
    }

    public Boolean isSpamFile(InputStream inputStream)
    {
        return isSpam(words(inputStream));
    }

    private Boolean isSpam(List<String> words)
    {
        if (model == null)
        {
            System.out.println("Call loadModel() first");
            return null;
        }

        HashMap<String, Float> appeal = new HashMap<>();
        for (String word: words)
            appeal.put(word, Math.abs(probability(word) - 0.5f));

        List<String> appealList = appeal.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        float spamProbability = 1.0f;
        float hamProbability = 1.0f;
        /*
        P_i = P(S|w_i)
        P(H|w_i) = P(S|w_i) = 1 - P_i
        P(S|E) = (Π P_i)/((Π P_i)+(Π 1-P_i))
        */
        int size = 15;
        if (appealList.size() < size)
            size = appealList.size();
        for (int i = 0; i < size; i++)
        {
            float p = probability(appealList.get(i));
            spamProbability *= p;
            hamProbability *= (1.0f - p);
        }

        float total = spamProbability / (spamProbability + hamProbability);

        return total > 0.9f;//Changed from 0.99999
    }

    void testFilter()
    {
        ArrayList<String> spamAsHam = new ArrayList<>();
        ArrayList<String> hamAsSpam = new ArrayList<>();

        int hamHit = 0;
        int hamTotal = 0;

        for (String fileName: ModelHelper.getListFileNames(getClass().getClassLoader(), "dataset/hamtesting"))
        {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(Paths.get("dataset/hamtesting/" + fileName).toString());
            assert inputStream != null;
            if (!isSpamFile(inputStream))
                hamHit++;
            else
                hamAsSpam.add(fileName);
            hamTotal++;
        }

        int spamHit = 0;
        int spamTotal = 0;

        for (String fileName: ModelHelper.getListFileNames(getClass().getClassLoader(), "dataset/spamtesting"))
        {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(Paths.get("dataset/spamtesting/" + fileName).toString());
            assert inputStream != null;
            if (isSpamFile(inputStream))
                spamHit++;
            else
                spamAsHam.add(fileName);
            spamTotal++;
        }

        float hamHitRatio = hamHit / (float) hamTotal;
        float spamHitRatio = spamHit / (float) spamTotal;

        System.out.println("Correct Ham Percentage:     " + hamHitRatio * 100);
        System.out.println("Correct Spam Percentage:    " + spamHitRatio * 100);
        float accuracy = (hamHitRatio * hamTotal + spamHitRatio * spamTotal) / (hamTotal + spamTotal);
        System.out.println("Correct Overall Percentage: " + accuracy * 100);

        System.out.println("Ham Incorrectly Labelled as Spam:");
        hamAsSpam.forEach(System.out::println);
        System.out.println("Spam Incorrectly Labelled as Ham:");
        spamAsHam.forEach(System.out::println);
    }
}
