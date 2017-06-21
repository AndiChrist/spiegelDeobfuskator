package de.avwc;

import j2html.tags.DomContent;
import j2html.tags.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static j2html.TagCreator.*;

/**
 * http://www.spiegel.de/spiegel/jason-brennan-zu-donald-trump-wahlsieg-waehler-sind-hobbits-a-1141615.html
 * http://www.spiegel.de/spiegel/mathias-greffrath-was-die-lektuere-von-karl-marx-kapital-heute-bringt-a-1140660.html
 * http://www.spiegel.de/spiegel/gerald-hensel-aktivist-gegen-fake-news-und-hate-speech-a-1150835.html
 *
 * https://aboullaite.me/jsoup-html-parser-tutorial-examples/
 * <p>
 * Created by andichrist on 24.11.16.
 */
public class SpiegelDecoder {

    private static final Logger LOG = Logger.getLogger(SpiegelDecoder.class.getName());

    private static final boolean ENCODED = true;

    public static String decodeFromURL(String url) {
        return decodeDocument(getDocumentFromURL(url));
    }

    public static Document getDocumentFromURL(String url) {
        Document document = null;

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException ex) {
            Logger.getLogger(SpiegelDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return document;
    }

    private static String decodeDocument(Document document) {
        Objects.requireNonNull(document);

        // remove unwanted stuff
        document.select("div.article-image-description").remove();
        document.select("div.js-module-box-image").remove();
        document.select("div.asset-box").remove();
        document.select("div.module-box").remove();

        return document(
                html(
                        head(
                                meta().withCharset("utf-8"),
                                title(document.title())),
                        body(
                                h3(document.select("h2.article-title span.headline-intro").text()),
                                h1(document.select("h2.article-title span.headline").text()),
                                h2(document.select("p.article-intro").text()),
                                p(each(getText(document, !ENCODED), e -> e)),
                                p(each(getText(document, ENCODED), e -> e))

                        )
                )
        );
    }

    private static List<DomContent> getText(Document document, boolean encoded) {
        List<DomContent> normalText = new ArrayList<>();
        String filter;
        if (encoded) {
            filter = "div.obfuscated-content > p.obfuscated";
        } else {
            filter = "div.article-section > p";
        }

        Elements ps = document.select(filter);

        ps.stream().forEach(e -> {
            List<Node> nodes = e.childNodes();
            for (Node n : nodes) {
                if (n instanceof Element) {
                    Element element = (Element) n;
                    //LOG.info("TAG: " + element.nodeName() + " => " + element.ownText());

                    switch(element.nodeName()) {
                        case "b":
                            normalText.add(b(encoded ? decode(element.ownText()) : element.ownText()));
                            break;
                        case "br":
                            normalText.add(br());
                            break;
                        case "i":
                            normalText.add(i(encoded ? decode(element.ownText()) : element.ownText()));
                            break;
                        case "a":
                            normalText.add(i(element.ownText()));
                            break;
                        default:
                            normalText.add(new Text(encoded ? decode(element.ownText()) : element.ownText()));
                    }
                } else {
                    //LOG.info("NODE: " + n);
                    normalText.add(new Text(encoded ? decode(n.toString()) : n.toString()));
                }
            }

            // two breaks for one p
            normalText.add(br());
            normalText.add(br());
        });

        return normalText;
    }

    public static String decode(String text) {
        char[] caesarText = text.toCharArray();
        char[] cleanText = new char[caesarText.length];
        for (char c = 0; c < caesarText.length; c++) {
            // some excludes
            switch (caesarText[c]) {
                case ' ': // 32
                    cleanText[c] = caesarText[c];
                    break;
                case '´': // 180
                    cleanText[c] = '-'; // or ';' ?
                    break;
                case '²': // 178
                    cleanText[c] = '!';
                    break;
                case '\'': // 39; ; check '\bnq<' --> '&amp;' etc.
                    cleanText[c] = (char) (caesarText[c] - 1);
                    while (caesarText[++c] != '<') { // 60
                        cleanText[c] = (char) (caesarText[c] - 1);
                    }
                    cleanText[c] = (char) (caesarText[c] - 1);

                    break;
                case '±': // 177; filter error in SPIEGEL's HTML code
                    cleanText[c] = '&';
                    while (caesarText[++c] != '´') { // 180
                        cleanText[c] = (char) (caesarText[c] - 1);
                    }
                    cleanText[c] = ';';

                    break;
                case '<': // check HTML special char
                    cleanText[c] = caesarText[c];
                    while (caesarText[++c] != '>') {
                        cleanText[c] = caesarText[c];
                    }
                    cleanText[c] = caesarText[c];

                    break;

                default:
                    cleanText[c] = (char) (caesarText[c] - 1);
            }
        }

        return new String(cleanText);
    }

    /*
    // not used right now
    public static String encode(String text) {
        char[] cleanText = text.toCharArray();
        char[] caesarText = new char[cleanText.length];
        for (char c = 0; c < cleanText.length; c++) {
            // some excludes
            switch (cleanText[c]) {
                case ' ':
                    caesarText[c] = cleanText[c];
                    break;
                case '!':
                    caesarText[c] = '²';
                    break;
                default:
                    caesarText[c] = (char) (cleanText[c] + 1);
            }
        }

        return new String(caesarText);
    }


       private static String cssStyle() {
        final StringBuilder outBuffer = new StringBuilder();

        outBuffer.append("<style>");
        outBuffer.append(".jumbotron {");
        outBuffer.append("    background-color: #f4511e;");
        outBuffer.append("    color: #fff;");
        outBuffer.append("    padding: 100px 25px;");
        outBuffer.append("}");

        outBuffer.append(".container-fluid {");
        outBuffer.append("    padding: 60px 50px;");
        outBuffer.append("}");
        outBuffer.append("</style>");

        return outBuffer.toString();
    }

    private static String startBootstrap() {
        final StringBuilder outBuffer = new StringBuilder();

        outBuffer.append("<!-- Latest compiled and minified CSS -->");
        outBuffer.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">");

        outBuffer.append("<!-- Optional theme -->");
        outBuffer.append("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css\" integrity=\"sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp\" crossorigin=\"anonymous\">");

        //outBuffer.append("<!-- Latest compiled and minified JavaScript -->");
        //outBuffer.append("<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>");

        return outBuffer.toString();
    }

    private static String endBootstrap() {
        final StringBuilder outBuffer = new StringBuilder();

        outBuffer.append("<!-- JQuery -->");
        outBuffer.append("<script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\" integrity=\"sha256-k2WSCIexGzOj3Euiig+TlR8gA0EmPjuc79OEeY5L45g=\" crossorigin=\"anonymous\"></script>");

        outBuffer.append("<!-- Latest compiled and minified JavaScript -->");
        outBuffer.append("<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\" integrity=\"sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa\" crossorigin=\"anonymous\"></script>");

        return outBuffer.toString();
    }
     */
}
