package Stemmer;
import org.tartarus.snowball.ext.PorterStemmer;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
public class Stemmer {

    private final static String ILLEGAL_REGEX_PATTERN = "([^a-zA-Z0-9])|(\\b\\d{1}\\b)|(\\b\\w{1}\\b)";
    private static HashMap<String, Integer> map = new HashMap<>();

    public Stemmer(){
        LoadStopWords();
    }

    public String stem(String s){
        StringBuilder stringBuilder = new StringBuilder();
        for (String word : replaceIllegalCharacter(s).split(" "))
        {
            word = RemvoeStopWords(word);
            String stemmedWord = stemPrivate(word);
            if (StringUtils.isNotEmpty(stemmedWord)) {
                if (stringBuilder.length() > 0)
                    stringBuilder.append(' ');

                stringBuilder.append(stemmedWord);
            }
        }
        return (stringBuilder.toString());
    }

    private static String stemPrivate(String word)
    {
        PorterStemmer porterStemmer = new PorterStemmer();
        porterStemmer.setCurrent(word);
        porterStemmer.stem();
        return porterStemmer.getCurrent();
    }

    private String RemvoeStopWords(String s){
        if (map.containsKey(s))
            return "";
        else
            return s;
    }

    private void LoadStopWords(){
        map.put("on", 1);
        map.put("in", 1);
        map.put("to", 1);
        map.put("for", 1);
        map.put("of", 1);
        map.put("i",1);
        map.put("me",1);
        map.put("my",1);
        map.put("myself",1);
        map.put("we",1);
        map.put("our",1);
        map.put("ours",1);
        map.put("ourselves",1);
        map.put("you",1);
        map.put("your",1);
        map.put("yours",1);
        map.put("yourself",1);
        map.put("yourselves",1);
        map.put("he",1);
        map.put("him",1);
        map.put("his",1);
        map.put("himself",1);
        map.put("she",1);
        map.put("her",1);
        map.put("hers",1);
        map.put("herself",1);
        map.put("it",1);
        map.put("its",1);
        map.put("itself",1);
        map.put("they",1);
        map.put("them",1);
        map.put("their",1);
        map.put("theirs",1);
        map.put("themselves",1);
        map.put("what",1);
        map.put("which",1);
        map.put("who",1);
        map.put("whom",1);
        map.put("this",1);
        map.put("that",1);
        map.put("these",1);
        map.put("those",1);
        map.put("am",1);
        map.put("is",1);
        map.put("are",1);
        map.put("was",1);
        map.put("were",1);
        map.put("be",1);
        map.put("been",1);
        map.put("being",1);
        map.put("have",1);
        map.put("has",1);
        map.put("had",1);
        map.put("having",1);
        map.put("do",1);
        map.put("does",1);
        map.put("did",1);
        map.put("doing",1);
        map.put("a",1);
        map.put("an",1);
        map.put("the",1);
        map.put("and",1);
        map.put("but",1);
        map.put("if",1);
        map.put("or",1);
        map.put("because",1);
        map.put("as",1);
        map.put("until",1);
        map.put("while",1);
        map.put("of",1);
        map.put("at",1);
        map.put("by",1);
        map.put("for",1);
        map.put("with",1);
        map.put("about",1);
        map.put("against",1);
        map.put("between",1);
        map.put("into",1);
        map.put("through",1);
        map.put("during",1);
        map.put("before",1);
        map.put("after",1);
        map.put("above",1);
        map.put("below",1);
        map.put("to",1);
        map.put("from",1);
        map.put("up",1);
        map.put("down",1);
        map.put("in",1);
        map.put("out",1);
        map.put("on",1);
        map.put("off",1);
        map.put("over",1);
        map.put("under",1);
        map.put("again",1);
        map.put("further",1);
        map.put("then",1);
        map.put("once",1);
        map.put("here",1);
        map.put("there",1);
        map.put("when",1);
        map.put("where",1);
        map.put("why",1);
        map.put("how",1);
        map.put("all",1);
        map.put("any",1);
        map.put("both",1);
        map.put("each",1);
        map.put("few",1);
        map.put("more",1);
        map.put("most",1);
        map.put("other",1);
        map.put("some",1);
        map.put("such",1);
        map.put("no",1);
        map.put("nor",1);
        map.put("not",1);
        map.put("only",1);
        map.put("own",1);
        map.put("same",1);
        map.put("so",1);
        map.put("than",1);
        map.put("too",1);
        map.put("very",1);
        map.put("s",1);
        map.put("t",1);
        map.put("can",1);
        map.put("will",1);
        map.put("just",1);
        map.put("don",1);
        map.put("should",1);
        map.put("now",1);
    }

    private static String replaceIllegalCharacter(String string)
    {
        return string.replaceAll(ILLEGAL_REGEX_PATTERN, " ").replaceAll(" +", " ").trim().toLowerCase();
    }


    public static void main(String[] args){
        String test = "this main function for just testing, enjoy ;) 1 2 3 4 1234 a b c d abcd #$%^";
        Stemmer S = new Stemmer();
        System.out.print(S.stem(test));
    }

}
