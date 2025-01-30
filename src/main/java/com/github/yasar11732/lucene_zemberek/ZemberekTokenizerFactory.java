package com.github.yasar11732.lucene_zemberek;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import zemberek.morphology.TurkishMorphology;
import zemberek.tokenization.TurkishSentenceExtractor;
import zemberek.tokenization.TurkishTokenizer;

import java.util.Map;

public class ZemberekTokenizerFactory extends TokenizerFactory {

    public static final String NAME = "zemberekTokenizer";

    private static final TurkishSentenceExtractor extractor = TurkishSentenceExtractor.DEFAULT;
    private static final TurkishMorphology morphology = TurkishMorphology.createWithDefaults();
    private static final TurkishTokenizer tokenizer = TurkishTokenizer.DEFAULT;

    public ZemberekTokenizerFactory(Map<String, String> args) {
        super(args);
        if(!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters" + args);
        }
    }
    public ZemberekTokenizerFactory() {
        throw defaultCtorException();
    }

    @Override
    public Tokenizer create(AttributeFactory attributeFactory) {
        ZemberekTokenizer zemberekTokenizer = new ZemberekTokenizer(attributeFactory);

        zemberekTokenizer.Extractor(extractor);
        zemberekTokenizer.Morphology(morphology);
        zemberekTokenizer.Tokenizer(tokenizer);

        return zemberekTokenizer;
    }
}
