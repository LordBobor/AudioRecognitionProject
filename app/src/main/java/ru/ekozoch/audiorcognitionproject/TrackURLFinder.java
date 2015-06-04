package ru.ekozoch.audiorcognitionproject;


import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by ekozoch on 02.06.15.
 */
public class TrackURLFinder {
    public TrackURLFinder(String lovedTrack) {
        this.lovedTrack = lovedTrack;
        StringBuffer buff = new StringBuffer();
        buff.append("https://api.vk.com/method/");
        buff.append(VK_COM_API_METHOD);
        buff.append("?q=");
        buff.append(lovedTrack);
        buff.append("&");
        buff.append("access_token=");
        buff.append("f23a612afab1aa4b74da40f925dc08472efbc2f10d57967795aa1aa7648842bc14198db71d4a98fe5954c");
        connectToUrl = buff.toString();
        connectToUrl = connectToUrl.replaceAll(" ", "%20");
        Log.e("VK URL", connectToUrl);
    }

    public String addUrlToLovedTrack() throws SAXException, IOException, ParserConfigurationException {
        if(lovedTrack != null) {
            URL url = new URL(connectToUrl);
            URLConnection conn = url.openConnection();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(conn.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList audioList = doc.getElementsByTagName("audio");
            Node currentNode = audioList.item(0);

            Element currentElement = (Element) currentNode;

            NodeList urlList = currentElement.getElementsByTagName("url");
            Element urlElement = (Element) urlList.item(0);
            NodeList urlFormattedList = urlElement.getChildNodes();

            String urlAddressOfTheMP3File = urlFormattedList.item(0).getNodeValue().trim();

            Log.e("VK URL", urlAddressOfTheMP3File);
            return urlAddressOfTheMP3File;
        }
        return "";
    }

    private String connectToUrl = null;
    private String lovedTrack = null;

    public static final String VK_COM_API_METHOD = "audio.search.xml";
}
