package com.zfun.funmodule.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class ManifestEditor {
    interface ITransformFilter {
        void startElement(TransformerHandler handler, String uri, String localName, String qName, Attributes attributes);

        void endElement(TransformerHandler handler, String uri, String localName, String qName);

        void characters(TransformerHandler handler, char[] ch, int start, int length);
    }//

    private final String manifestFilePath;
    private final String editResultFilePath;

    public ManifestEditor(String manifestFilePath, String editResultFilePath) {
        this.manifestFilePath = manifestFilePath;
        this.editResultFilePath = editResultFilePath;
    }

    public void transform(ITransformFilter filter) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            //输出xml内容到指定文件
            final TransformerHandler xmlWriteHandler = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).newTransformerHandler();
            xmlWriteHandler.setResult(new StreamResult(new File(editResultFilePath)));
            //解析原始xml内容
            SAXParser saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(new File(manifestFilePath), new MySAXHandler(xmlWriteHandler, filter));
        } catch (Exception ignore) {
        }
    }

    private static class MySAXHandler extends DefaultHandler {
        private final TransformerHandler xmlWriteHandler;
        private final ITransformFilter filter;

        public MySAXHandler(TransformerHandler xmlWriteHandler, ITransformFilter filter) {
            this.xmlWriteHandler = xmlWriteHandler;
            this.filter = filter;
        }

        @Override
        public void startDocument() throws SAXException {
            xmlWriteHandler.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            xmlWriteHandler.endDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (null != filter) {
                filter.startElement(xmlWriteHandler, uri, localName, qName, attributes);
            } else {
                xmlWriteHandler.startElement(uri, localName, qName, attributes);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (null != filter) {
                filter.characters(xmlWriteHandler, ch, start, length);
            } else {
                xmlWriteHandler.characters(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (null != filter) {
                filter.endElement(xmlWriteHandler, uri, localName, qName);
            } else {
                xmlWriteHandler.endElement(uri, localName, qName);
            }
        }
    }//

    public static class RemoveActivityLauncher implements ITransformFilter {
        private String curActivityElementName = "";
        private boolean skipActivityCategory = false;

        @Override
        public void startElement(TransformerHandler handler, String uri, String localName, String qName, Attributes attributes){
            if (curActivityElementName.length() == 0 && "activity".equals(qName)) {
                curActivityElementName = qName;
            }
            if (curActivityElementName.length() > 0) {//处于activity
                if("category".equals(qName) && "android.intent.category.LAUNCHER".equalsIgnoreCase(attributes.getValue("android:name"))){
                    skipActivityCategory = true;
                    return;//不保存
                }
            }
            skipActivityCategory = false;
            try {
                handler.startElement(uri,localName,qName,attributes);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void endElement(TransformerHandler handler, String uri, String localName, String qName) {
            try {
                if (curActivityElementName.length() == 0) {//未处于activity
                    handler.endElement(uri, localName, qName);
                    return;
                }
                if (curActivityElementName.equals(qName)) {//activity解析完毕
                    curActivityElementName = "";
                    handler.endElement(uri, localName, qName);
                    return;
                }

                if("category".equals(qName) && skipActivityCategory){
                    return;//不保存
                }
                handler.endElement(uri, localName, qName);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void characters(TransformerHandler handler, char[] ch, int start, int length) {
            try {
                handler.characters(ch,start,length);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }//
}
