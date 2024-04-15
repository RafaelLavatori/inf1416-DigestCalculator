import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlHandler {
    private String catalog_path;
    private Document xml_catalog;

    public enum Status {
        OK, NOT_OK, NOT_FOUND, COLLISION, ERROR
    }

    // XML Handler constructor, either creates new XML catalog or reads existing one
    public XmlHandler(String xml_path) {
        catalog_path = xml_path;
        File f = new File(catalog_path);
        try {
            if (f.exists() && !f.isDirectory()) { 
                // read from existing XML catalog
                xml_catalog = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
            } else {
                // XML catalog does not exist => create it
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                
                // add root to Document
                doc.appendChild(doc.createElement("CATALOG"));

                xml_catalog = doc;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Exports XML catalog to file at given path
    public void SaveFile() {
        try {
            // for output to file, console
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // for pretty print
            // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(xml_catalog);

            // write to file
            StreamResult file = new StreamResult(new File(catalog_path));
            transformer.transform(source, file);

            // write to console
            // StreamResult console = new StreamResult(System.out);
            //  transformer.transform(source, console);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // returns a file entry from the catalog
    // used when adding new digest codes when they're not found in the catalog
    private Element GetFileEntry(String file_name) throws Exception {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = String.format("//FILE_ENTRY[child::FILE_NAME[text()='%s']]",file_name);

        // query for existing entry
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xml_catalog, XPathConstants.NODESET);

        if (nodeList.getLength()>0) return (Element) nodeList.item(0); // return existing entry
        return null;
    }

    // creates new digest entry and returns it
    private Element createDigestEntry(String digest_type, String file_digest) {
        // create digest collection
        Element digest_entry_node = xml_catalog.createElement("DIGEST_ENTRY");

        // create digest_type
        Element digest_type_node = xml_catalog.createElement("DIGEST_TYPE");
        digest_type_node.appendChild(xml_catalog.createTextNode(digest_type));

        // create digest_hex
        Element digest_hex_node = xml_catalog.createElement("DIGEST_HEX");
        digest_hex_node.appendChild(xml_catalog.createTextNode(file_digest));

        // append those to digest entry
        digest_entry_node.appendChild(digest_type_node);
        digest_entry_node.appendChild(digest_hex_node);

        return digest_entry_node;
    }

    // checks if there is digest collision
    // returns True if there is, False if there isn't
    private boolean checkDigestCollision(String file_name, String digest_type, String file_digest) throws Exception {
        // query file names by digest
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = String.format("//FILE_ENTRY[descendant::DIGEST_HEX[text()='%s']]/FILE_NAME",file_digest);
        NodeList fileNamesQueryResult = (NodeList) xPath.compile(expression).evaluate(xml_catalog, XPathConstants.NODESET);

        // obvious cases
        if (fileNamesQueryResult.getLength() == 0) return false;
        if (fileNamesQueryResult.getLength() > 1) return true;

        // checking if the one result is the file itself or some other
        String catalog_file_name = fileNamesQueryResult.item(0).getTextContent();

        // if the catalog file name is different, there is a collision
        return !catalog_file_name.equals(file_name);
    }

    public Status queryFileDigest(String file_name, String digest_type, String file_digest) {
        try {
            // collision check
            if (checkDigestCollision(file_name, digest_type, file_digest)) return Status.COLLISION;

            Element file_entry = GetFileEntry(file_name);
            if (file_entry != null) {
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expression = String.format("//FILE_ENTRY[child::FILE_NAME[text()='%s']]/DIGEST_ENTRY[child::DIGEST_TYPE[text()='%s']]",file_name,digest_type);
                NodeList digestQueryResult = (NodeList) xPath.compile(expression).evaluate(file_entry, XPathConstants.NODESET);

                if (digestQueryResult.getLength() == 0) {
                    // new digest type for the given file
                    // create digest entry
                    Element digest_entry_node = createDigestEntry(digest_type, file_digest);
                    file_entry.appendChild(digest_entry_node);

                    return Status.NOT_FOUND; 
                }

                Element digestEntry = (Element) digestQueryResult.item(0);
                String catalogFileDigest = digestEntry.getElementsByTagName("DIGEST_HEX").item(0).getTextContent();

                if (catalogFileDigest.equals(file_digest)) return Status.OK;
                else return Status.NOT_OK;

            } else {
                // no original file entry found
                // create file entry
                Element root = xml_catalog.getDocumentElement();
                file_entry = xml_catalog.createElement("FILE_ENTRY");

                // create FILE_NAME
                Element file_name_node = xml_catalog.createElement("FILE_NAME");
                file_name_node.appendChild(xml_catalog.createTextNode(file_name));
                file_entry.appendChild(file_name_node);

                // create digest entry
                Element digest_entry_node = createDigestEntry(digest_type, file_digest);
                file_entry.appendChild(digest_entry_node);
                
                // append file entry to catalog
                root.appendChild(file_entry);
                
                return Status.NOT_FOUND; 
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return Status.ERROR;
        }
    }
}
