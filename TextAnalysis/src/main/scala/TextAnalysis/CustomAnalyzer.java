package TextAnalysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import static org.apache.lucene.analysis.core.StopAnalyzer.ENGLISH_STOP_WORDS_SET;

/**
 * Created by cahillt on 5/3/16.
 * http://stackoverflow.com/questions/26808062/looping-through-the-alphabet-as-string
 * http://stackoverflow.com/questions/2047228/auto-increment-alphabet-in-java
 */
public class CustomAnalyzer extends StopwordAnalyzerBase {
  protected TokenStreamComponents createComponents(String s, Reader reader) {

//    final StandardTokenizer src = new StandardTokenizer(this.getVersion(), reader);
//    src.setMaxTokenLength(this.maxTokenLength);
//    StandardFilter tok = new StandardFilter(this.getVersion(), src);
//    LowerCaseFilter tok1 = new LowerCaseFilter(this.getVersion(), tok);
//    final StopFilter tok2 = new StopFilter(this.getVersion(), tok1, this.stopwords);
//    return new TokenStreamComponents(src, tok2) {
//      protected void setReader(Reader reader) throws IOException {
//        src.setMaxTokenLength(StandardAnalyzer.this.maxTokenLength);
//        super.setReader(reader);
//      }
//    };

    final StandardTokenizer src = new StandardTokenizer(reader);
    StandardFilter tok = new StandardFilter(src);
    LowerCaseFilter tok1 = new LowerCaseFilter(tok);


    CharArraySet customStopWords = new CharArraySet(ENGLISH_STOP_WORDS_SET.size() + 56, true);

    for(char alphabet = 'A'; alphabet <= 'Z';alphabet++) {
      customStopWords.add(alphabet);
    }

    for (Object aENGLISH_STOP_WORDS_SET : ENGLISH_STOP_WORDS_SET) {
      customStopWords.add(aENGLISH_STOP_WORDS_SET);
    }

    customStopWords.add("you");
    customStopWords.add("my");
    customStopWords.add("its");
    customStopWords.add("has");

    final StopFilter tok2 = new StopFilter(tok1, customStopWords);

    SynonymMap.Builder builder = new SynonymMap.Builder(true);

    //TODO build synonym map
    builder.add(new CharsRef("large"), new CharsRef("big"), false);
    builder.add(new CharsRef("great"), new CharsRef("good"), false);
    builder.add(new CharsRef("spd"), new CharsRef("speed"), false);
    builder.add(new CharsRef("mileage"), new CharsRef("mpg"), false);
    builder.add(new CharsRef("miles" + SynonymMap.WORD_SEPARATOR + "per" + SynonymMap.WORD_SEPARATOR + "gallon"), new CharsRef("mpg"), false);

    SynonymFilter filter = null;
    try {
      filter = new SynonymFilter(tok2, builder.build(), true);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new TokenStreamComponents(src, filter);
  }

  //  private void addTo(SynonymMap.Builder builder, String[] from, String[] to) {
//    for (String input : from) {
//      for (String output : to) {
//        builder.add(new CharsRef(input), new CharsRef(output), false);
//      }
//    }
//  }
}
