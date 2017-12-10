package de.avwc.decoder;

import j2html.TagCreator;
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

    private static SpiegelDecoder instance;

    public String decodeFromURL(String url) {
        Document document = getDocumentFromURL(url);
        String decodedDocument = decodeDocument(document);
        return decodedDocument;
    }

    public Document getDocumentFromURL(String url) {
        Document document = null;

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException ex) {
            Logger.getLogger(SpiegelDecoder.class.getName()).log(Level.SEVERE, null, ex);
        }

        return document;
    }

    private String decodeDocument(Document document) {
        Objects.requireNonNull(document);

        // remove unwanted stuff
        document.select("div.article-image-description").remove();
        document.select("div.js-module-box-image").remove();
        document.select("div.asset-box").remove();
        document.select("div.module-box").remove();

        String decodedDocument = TagCreator.document(
                TagCreator.html(
                        TagCreator.head(
                                TagCreator.meta().withCharset("utf-8"),
                                TagCreator.title(document.title())),
                        TagCreator.body(
                                TagCreator.h3(document.select("h2.article-title span.headline-intro").text()),
                                TagCreator.h1(document.select("h2.article-title span.headline").text()),
                                TagCreator.h2(document.select("p.article-intro").text()),
                                TagCreator.p(TagCreator.each(getText(document, !ENCODED), e -> e)),
                                TagCreator.p(TagCreator.each(getText(document, ENCODED), e -> e))

                        )
                )
        );

        return decodedDocument;
    }

    private List<DomContent> getText(Document document, boolean encoded) {
        String filter;
        if (encoded) {
            filter = "div.obfuscated-content > p.obfuscated";
        } else {
            filter = "div.article-section > p";
        }

        Elements ps = document.select(filter);

        List<DomContent> normalText = new ArrayList<>();
        ps.stream().forEach(e -> {
            List<Node> nodes = e.childNodes();
            for (Node n : nodes) {
                if (n instanceof Element) {
                    Element element = (Element) n;
                    //LOG.info("TAG: " + element.nodeName() + " => " + element.ownText());

                    switch(element.nodeName()) {
                        case "b":
                            normalText.add(TagCreator.b(encoded ? decode(element.ownText()) : element.ownText()));
                            break;
                        case "br":
                            normalText.add(TagCreator.br());
                            break;
                        case "i":
                            normalText.add(TagCreator.i(encoded ? decode(element.ownText()) : element.ownText()));
                            break;
                        case "a":
                            normalText.add(TagCreator.i(element.ownText()));
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
            normalText.add(TagCreator.br());
            normalText.add(TagCreator.br());
        });

        return normalText;
    }

    public String decode(String text) {
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

    public static SpiegelDecoder getInstance() {
        if (instance == null) {
            instance = new SpiegelDecoder();
        }

        return instance;
    }

    private SpiegelDecoder() {}
}
