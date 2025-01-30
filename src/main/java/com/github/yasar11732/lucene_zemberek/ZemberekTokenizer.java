package com.github.yasar11732.lucene_zemberek;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.AttributeFactory;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SentenceAnalysis;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.tokenization.TurkishSentenceExtractor;
import zemberek.tokenization.TurkishTokenizer;

import java.io.IOException;
import java.util.LinkedList;

public final class ZemberekTokenizer extends Tokenizer {

    public void Extractor(TurkishSentenceExtractor extractor) {
        this.extractor = extractor;
    }

    public void Morphology(TurkishMorphology morphology) {
        this.morphology = morphology;
    }

    public void Tokenizer(TurkishTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    private TurkishSentenceExtractor extractor;
    private TurkishMorphology morphology;
    private TurkishTokenizer tokenizer;

    private LinkedList<zemberek.tokenization.Token> zemberekTokens;
    private LinkedList<String> sentences;
    private LinkedList<SingleAnalysis> singleAnalysisList;

    private int sentenceNo;
    private String ending;
    private int endingOffset;

    // attributes
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final SentenceAttribute sentenceAtt = addAttribute(SentenceAttribute.class);

    public ZemberekTokenizer() {
        init();
    }

    public  ZemberekTokenizer(AttributeFactory factory) {
        super(factory);
        init();
    }

    private void init() {
        char[] _buffer = new char[4096];
        StringBuilder _builder = new StringBuilder();
        String inputString;
        int n;
        while(true) {
            try {
                if ((n = input.read(_buffer, 0, _buffer.length)) == -1)
                    break;
            } catch (IOException e) {
                break;
            }
            _builder.append(_buffer, 0, n);
        }

        inputString = _builder.toString();

        zemberekTokens = new LinkedList<>(tokenizer.tokenize(inputString));
        sentences = new LinkedList<>(extractor.fromDocument(inputString));
        singleAnalysisList = new LinkedList<>();

        sentenceNo = -1;
        ending = null;
        endingOffset = -1;
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();

        if(ending != null) {
            termAtt.append("##");
            termAtt.append(ending);
            offsetAtt.setOffset(endingOffset, endingOffset + ending.length());
            typeAtt.setType("<ending>");
            sentenceAtt.setSentenceIndex(sentenceNo);
            return true;
        }

        if(zemberekTokens.isEmpty()) {
            return false;
        }

        zemberek.tokenization.Token zemberekToken = zemberekTokens.removeFirst();

        if(singleAnalysisList.isEmpty()) {
            sentenceNo++;
            SentenceAnalysis s = morphology.analyzeAndDisambiguate(sentences.removeFirst());
            singleAnalysisList = new LinkedList<>(s.bestAnalysis());
        }

        SingleAnalysis tokenAnalysis = singleAnalysisList.removeFirst();
        String stem = tokenAnalysis.getStem();
        String ending = tokenAnalysis.getEnding();

        int startOffset = zemberekToken.getStart();

        termAtt.append(stem);
        offsetAtt.setOffset(startOffset, startOffset+stem.length());
        typeAtt.setType(tokenAnalysis.getPos().name());
        sentenceAtt.setSentenceIndex(sentenceNo);

        if(ending.isBlank()) {
            ending = null;
        } else {
            endingOffset = startOffset + stem.length();
        }

        return true;
    }
}