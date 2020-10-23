package com.shediz.score.service;

import ir.ac.iust.nlp.jhazm.Lemmatizer;
import ir.ac.iust.nlp.jhazm.Normalizer;
import ir.ac.iust.nlp.jhazm.WordTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.NBSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.*;
import weka.core.converters.ConverterUtils;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.RemoveWithValues;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("unused")
public class SentimentAnalysis
{
    private static Classifier nbsvmModel;

    private static Normalizer normalizer;

    private static WordTokenizer wordTokenizer;

    private static List<String> stopWords;

    private static Lemmatizer lemmatizer;

    private static Attribute classAttr;

    private static  ArrayList<Attribute> attrs;

    private static final String positiveEmojis = "[\uD83D\uDE02❤♥\uD83D\uDE0D\uD83D\uDE18\uD83D\uDE0A" +
            "\uD83D\uDC4C\uD83D\uDC95\uD83D\uDC4D\uD83D\uDC4B]";

    private static final String negativeEmojis = "[\uD83D\uDE2D\uD83D\uDE29\uD83D\uDE12\uD83D\uDE14\uD83D\uDE11" +
            "\uD83D\uDE13\uD83D\uDC4E]";

    @Autowired
    public SentimentAnalysis(ResourceLoader resourceLoader)
    {
        ArrayList<String> classes = new ArrayList<>(2);
        classes.add("pos");
        classes.add("neg");
        classAttr = new Attribute("Class", classes);

        attrs = new ArrayList<>(2);
        attrs.add(new Attribute("Text", (ArrayList<String>) null));
        attrs.add(classAttr);


        normalizer = new Normalizer();
        try
        {
            wordTokenizer = new WordTokenizer();
            lemmatizer = new Lemmatizer();
            //This causes: cannot be resolved to absolute file path because it does not reside in the file system
            //stopWords = Files.readAllLines(resourceLoader.getResource("classpath:stopwords.txt").getFile().toPath());
            stopWords = new BufferedReader(new InputStreamReader(
                    resourceLoader.getResource("classpath:stopwords.txt").getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.toList());
            nbsvmModel = loadModel(resourceLoader.getResource("classpath:nbsvm.model").getInputStream());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        //stopWords = Files.readAllLines(Paths.get("score/src/main/resources/stopwords.txt"));

        //nbsvmModel = loadModel("score/src/main/resources/nbsvm.model");
    }

    public static Classifier getModel()
    {
        return nbsvmModel;
    }

    public static String predictOnce(Classifier model, String text) throws Exception
    {
        // create "test-set" of size 1
        Instances testSet = new Instances("prediction", attrs, 1);
        if (testSet.classIndex() == -1)
            testSet.setClassIndex(testSet.numAttributes()-1);

        Instance instance = new SparseInstance(testSet.numAttributes());
        instance.setValue(attrs.get(0), cleanPersianText(text));
        instance.setDataset(testSet);

        double pred = model.classifyInstance(instance);

        return classAttr.value((int) pred);
    }

    private static String cleanPersianText(final String inputText)
    {
        //Replace Emojis
        String text = inputText.replaceAll(positiveEmojis, " عالی ");
        text = text.replaceAll(negativeEmojis, " آشغال ");

        //Remove all non persian characters
        text = text.replaceAll("[^آ-ی \u200c]", "").trim();


        //Remove stop words
        StringBuilder stopWordsRegex = new StringBuilder("\\b(");
        for (String stopWord : stopWords)
            stopWordsRegex.append(stopWord).append("|");
        stopWordsRegex.append(")\\b");
        text = text.replaceAll(stopWordsRegex.toString(), "");


        //Normalize text
        text = normalizer.run(text);


        //Convert text to tokens
        List<String> tokens = wordTokenizer.tokenize(text);

        // If we remove stop words, accuracy decreases but predictOnce will get better - Why?
        //tokens.removeIf(stopWords::contains);

        //Remove tokens less than 1 in length
        tokens.removeIf(token -> token.length() <= 1);

        //Lemmatize tokens
        tokens = tokens.stream().map(lemmatizer::lemmatize).collect(Collectors.toList());

        return String.join(" ", tokens);
    }

    //if we need package jar, we most use resource loader for load files
    private static Instances loadDataSet(String path) throws Exception
    {
        ConverterUtils.DataSource dataSource =
                new ConverterUtils.DataSource(path);

        Instances data = dataSource.getDataSet();

        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        //This remove nat instances but not that class (dataset already have 3 classes)
        //data = removeNatInstances(data);

        int index = data.attribute("Text").index();

        data.forEach(instance -> instance.setValue(index, cleanPersianText(instance.stringValue(index))));

        return data;
    }

    private static Classifier train(Instances data) throws Exception
    {
        StringToWordVector filter = new StringToWordVector();
        filter.setInputFormat(data);
        filter.setIDFTransform(true);
        filter.setTFTransform(true);

        NGramTokenizer tokenizer = new NGramTokenizer();
        tokenizer.setNGramMinSize(1);
        tokenizer.setNGramMaxSize(2);
        filter.setTokenizer(tokenizer);

        WordsFromFile stopWords = new WordsFromFile();
        //if we need package jar, we most use resource loader for load files
        stopWords.setStopwords(new File("score/src/main/resources/stopwords.txt"));
        filter.setStopwordsHandler(stopWords);

        filter.setAttributeIndices("first-last");
        filter.setWordsToKeep(1000000);


        /*LibSVM svm = new LibSVM();
        svm.setProbabilityEstimates(false);
        svm.setDoNotReplaceMissingValues(true);
        svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));*/

        FilteredClassifier classifier = new FilteredClassifier();
        classifier.setFilter(filter);
        classifier.setClassifier(new NBSVM());

        classifier.buildClassifier(data);

        return classifier;
    }

    private static void crossValidation(Classifier model, Instances data) throws Exception
    {
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(model, data, 10, new Random(1));
        System.out.println(eval.toSummaryString());
    }

    private static Instances removeNatInstances(Instances data) throws Exception
    {
        RemoveWithValues filter = new RemoveWithValues();

        String[] options = new String[4];
        options[0] = "-C";   // attribute index
        options[1] = "last";    // Suggestion
        options[2] = "-L";   // match if value is equals
        options[3] = "3";   // index of nat view indices by calling printClassIndices()
        filter.setOptions(options);

        filter.setInputFormat(data);

        return Filter.useFilter(data, filter);
    }

    private static void testDataSet(Classifier model) throws Exception
    {
        Instances testData = loadDataSet("score/src/main/resources/dataset_test_labeled.arff");

        Evaluation eval = new Evaluation(testData);
        eval.evaluateModel(model, testData);
        System.out.println(eval.toSummaryString());
    }

    private static void predictDataSet(Classifier model) throws Exception
    {
        Instances unlabeled = loadDataSet("score/src/main/resources/dataset_test_unlabeled.arff");

        // create copy
        Instances labeled = new Instances(unlabeled);

        // label instances
        for (int i = 0; i < unlabeled.numInstances(); i++)
        {
            double clsLabel = model.classifyInstance(unlabeled.instance(i));
            labeled.instance(i).setClassValue(clsLabel);
        }
        // save labeled data
        BufferedWriter writer = new BufferedWriter(new FileWriter("score/src/main/resources/labeled.arff"));
        writer.write(labeled.toString());
        writer.newLine();
        writer.flush();
        writer.close();
    }

    private static FilteredClassifier loadModel(InputStream inputStream) throws Exception
    {
        return (FilteredClassifier) SerializationHelper.read(inputStream);
        /*return (FilteredClassifier) SerializationHelper
                .read(new FileInputStream("score/src/main/resources/nbsvm.model"));*/
    }

    private static void saveModel(Classifier model) throws Exception
    {
        SerializationHelper.write("score/src/main/resources/nbsvm.model", model);
    }

    private static void printClassIndices(Instances data)
    {
        for (int i = 0; i < data.classAttribute().numValues(); i++)
        {
            System.out.println("Index: " + (i + 1) + " Class: " + data.classAttribute().value(i));
        }
    }
}
