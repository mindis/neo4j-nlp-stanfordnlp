package com.graphaware.nlp.unit;

import com.graphaware.nlp.domain.AnnotatedText;
import com.graphaware.nlp.dsl.request.PipelineSpecification;
import com.graphaware.nlp.processor.TextProcessor;
import com.graphaware.nlp.processor.stanford.StanfordTextProcessor;
import com.graphaware.nlp.util.ServiceLoader;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class DependencyParserTest {

    private static TextProcessor textProcessor;

    @BeforeClass
    public static void setUp() {
        textProcessor = ServiceLoader.loadTextProcessor("com.graphaware.nlp.processor.stanford.StanfordTextProcessor");
        textProcessor.init();
    }

    @Test
    public void testStanfordTypedDependenciesParsing() {
        String annotators = "tokenize,ssplit,pos,depparse";
        Properties properties = new Properties();
        properties.setProperty("annotators", annotators);
        //properties.setProperty("depparse.model", DependencyParser.DEFAULT_MODEL);
        properties.setProperty("threads", "4");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

        String text = "Show me Josh Wedhon latest movies";
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        CoreMap sentence = sentences.get(0);
        System.out.println(sentence.toString());
        SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation.class);
        System.out.println(graph);

        List<SemanticGraphEdge> edges = graph.edgeListSorted();
        for (SemanticGraphEdge edge : edges) {
            System.out.println(edge.getRelation().getSpecific());
            System.out.println(edge.getRelation().getShortName());
            System.out.println(String.format("Source is : %s - Target is : %s - Relation is : %s", edge.getSource(), edge.getTarget(), edge.getRelation()));
        }
    }

    @Test
    public void testStanfordNLPWithPredefinedProcessors() throws Exception {
        StanfordCoreNLP pipeline = ((StanfordTextProcessor) textProcessor).getPipeline(StanfordTextProcessor.DEPENDENCY_GRAPH);
        String text = "Donald Trump flew yesterday to New York City";

        AnnotatedText at = textProcessor.annotateText(text, StanfordTextProcessor.TOKENIZER, "en", Collections.EMPTY_MAP);

        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        CoreMap sentence = sentences.get(0);
        System.out.println(sentence.toString());
        SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class);
        System.out.println(graph);

        List<SemanticGraphEdge> edges = graph.edgeListSorted();
        for (SemanticGraphEdge edge : edges) {
            System.out.println(edge.getRelation().getShortName());
            System.out.println(String.format("Source is : %s - Target is : %s - Relation is : %s", edge.getSource(), edge.getTarget(), edge.getRelation()));
        }
    }

    @Test
    public void testEnhancedDependencyParsingWithComplexTest() throws Exception {
        String text = "Softfoot and Small Paul would kill the Old Beard, Dirk would do Blane, and Lark and his cousins would silence Bannen and old Dywen, to keep them from sniffing after their trail.";
        StanfordCoreNLP pipeline = ((StanfordTextProcessor) textProcessor).getPipeline(StanfordTextProcessor.DEPENDENCY_GRAPH);

        AnnotatedText at = textProcessor.annotateText(text, StanfordTextProcessor.DEPENDENCY_GRAPH, "en", Collections.EMPTY_MAP);

        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            System.out.println(sentence.toString());
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class);
            System.out.println(graph);
            for (SemanticGraphEdge edge : graph.edgeListSorted()) {
                System.out.println(String.format("Source is : %s - Target is : %s - Relation is : %s", edge.getSource(), edge.getTarget(), edge.getRelation()));
            }
        }
    }

    @Test
    public void testEnhancedDependencyParsingWithQuestion() throws Exception {
        String text = "In what area was Frederic born in";
        StanfordCoreNLP pipeline = ((StanfordTextProcessor) textProcessor).getPipeline(StanfordTextProcessor.DEPENDENCY_GRAPH);

        Map<String, Object> customPipeline = new HashMap<>();
        customPipeline.put("textProcessor", "com.graphaware.nlp.processor.stanford.StanfordTextProcessor");
        customPipeline.put("name", "custom");
        customPipeline.put("stopWords", "start,starts");
        customPipeline.put("processingSteps", Collections.singletonMap("dependency", true));
        PipelineSpecification pipelineSpecification = PipelineSpecification.fromMap(customPipeline);
        ((StanfordTextProcessor) textProcessor).createPipeline(pipelineSpecification);

        textProcessor.annotateText(text, "custom", "en", Collections.EMPTY_MAP);

        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            System.out.println(sentence.toString());
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class);
            graph.getRoots().forEach(root -> {
                System.out.println(root);
            });
            System.out.println(graph);
            for (SemanticGraphEdge edge : graph.edgeListSorted()) {
                System.out.println(String.format("Source is : %s - Target is : %s - Relation is : %s", edge.getSource(), edge.getTarget(), edge.getRelation()));
            }
        }
    }

    @Test
    public void testTagMerging() throws Exception {
        StanfordCoreNLP pipeline = ((StanfordTextProcessor) textProcessor).getPipeline(StanfordTextProcessor.DEPENDENCY_GRAPH);
        String text = "Donald Trump flew yesterday to New York City";

        AnnotatedText at = textProcessor.annotateText(text, StanfordTextProcessor.TOKENIZER, "en", Collections.EMPTY_MAP);
    }
}
