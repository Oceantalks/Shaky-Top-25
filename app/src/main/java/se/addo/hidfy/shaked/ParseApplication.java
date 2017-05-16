package se.addo.hidfy.shaked;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Ado on 2016-11-07.
 */

public class ParseApplication {

    private String xmlData;
    private ArrayList<Application> applications;


    public ParseApplication(String xmlData) {
        this.xmlData = xmlData;
        applications = new ArrayList<Application>();
    }

    public ArrayList<Application> getApplications() {
        return applications;
    }

    public boolean process() {
        boolean status = true;
        Application currentRecord = null;
        boolean inEntry = false;
        String textValue = "";

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(this.xmlData));
            int evenType = xpp.getEventType();
            while (evenType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                switch (evenType) {
                    case XmlPullParser.START_TAG:
//                        Log.d("ParsApplications", "Starting tag for " + tagName);
                        if (tagName.equalsIgnoreCase("entry")) {
                            inEntry = true;
                            currentRecord = new Application();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
//                        Log.d("ParsApplications", "Ending tag for " + tagName);
                        if (inEntry) {
                            if (tagName.equalsIgnoreCase("entry")) {
                                applications.add(currentRecord);
                                inEntry = false;
                            } else if (tagName.equalsIgnoreCase("name")) {
                                currentRecord.setName(textValue);
//                                System.out.println("*************");
//                                System.out.println("Name: " + currentRecord.getName());
                            } else if (tagName.equalsIgnoreCase("artist")) {
                                currentRecord.setArtist(textValue);
//                                System.out.println("Artist: " + currentRecord.getArtist());
                            } else if (tagName.equalsIgnoreCase("releaseDate")) {
                                currentRecord.setReleaseDate(textValue);
//                                System.out.println("Release Date: " + currentRecord.getReleaseDate());
                            }
                        }
                        break;
                    default:
                        //do nothing
                }
                evenType = xpp.next();
            }
            return true;
        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        return true;
    }
}
