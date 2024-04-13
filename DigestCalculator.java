import java.security.*;
import javax.crypto.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// Generate a DigestCalculator
public class DigestCalculator {
    
    //Cria arquivo xml 
    private static void CreateXMLFileInJava () {
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            
            // add elements to Document
            Element rootElement = doc.createElement("CATALOG");
            
            // append root element to document
            doc.appendChild(rootElement);

            // append first child element to root element
            rootElement.appendChild(createFile_EntryElement(doc, "name1", "MD5", "ffffeeee"));

            // append second child
            rootElement.appendChild(createFile_EntryElement(doc, "name2", "SHA256", "eeeeffff"));

            // for output to file, console
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            // for pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            // write to console or file
            StreamResult console = new StreamResult(System.out);
            StreamResult file = new StreamResult(new File("list_digest.xml"));

            // write data
            transformer.transform(source, console);
            transformer.transform(source, file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //Cria Uma FileEnrty
    private static Node createFile_EntryElement(Document doc, String file_name, String tipo_digest, String digest_hex) {
            
        Element file_entry = doc.createElement("FILE_ENTRY");

        // Cria file_name element
        file_entry.appendChild(createFinalElements(doc, "FILE_NAME", file_name));

        // Cria digest_entry element
        file_entry.appendChild(createDigest_EntryElement(doc, tipo_digest, digest_hex));


        return file_entry;
    }
    
    //Cria Uma Digest Entry
    private static Node createDigest_EntryElement(Document doc, String tipo_digest, String digest_hex) {
            
        Element digest_entry = doc.createElement("DIGEST_ENTRY");

        // Cria tipo_digest element
        digest_entry.appendChild(createFinalElements(doc, "DIGEST_TYPE", tipo_digest));

        // Cria digest_hex element
        digest_entry.appendChild(createFinalElements(doc, "DIGEST_HEX", digest_hex));


        return digest_entry;
    }

    // Cria nó da arvore
    private static Node createFinalElements(Document doc, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }
    
    private static void file_digest(String inputFile, String tipo_digest) throws Exception {
        try (
            InputStream inputStream = new FileInputStream(inputFile);
        ) {
            long fileSize = new File(inputFile).length();
            byte[] allBytes = new byte[(int) fileSize];
         
            int bytesRead = inputStream.read(allBytes);
            
            //imprime os bytes lidos do arquivo        
            //for(int i =0; i<(int) fileSize; i++){
                //System.out.println(allBytes[i]);    
            //}
            
            MessageDigest messageDigest = MessageDigest.getInstance(tipo_digest);
            System.out.println( "\n" + messageDigest.getProvider().getInfo() );
            messageDigest.update(allBytes);
            byte [] digest = messageDigest.digest();
            System.out.println( "\nDigest length: " + digest.length * 8 + "bits" );
            
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1);
                buf.append((hex.length() < 2 ? "0" : "") + hex);
            }     
            
            // imprime o digest em hexadecimal
            System.out.println( "\nDigest(hex): " );
            System.out.println( buf.toString() );
            
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        
    }
    public static void main (String[] args) throws Exception {
        
        //Cria arquivo xml no formato especificado
        CreateXMLFileInJava();
        
        //check args
        if (args.length != 4) {
          System.err.println("Usage: DigestCalculator<SP>Tipo_Digest<SP>Caminho_da_Pasta_dos_Arquivos<SP>Caminho_ArqListaDigest");
          System.exit(1);
        }
        
        if (!args[1].equals("MD5") &&
                !args[1].equals("SHA1") &&
                !args[1].equals("SHA256") &&
                !args[1].equals("SHA512")) {
            System.err.println("tipo de digest invalido");
            System.exit(2);
        }
        
        String tipo_digest = args[1];
        String dir_path = args[2];
        
        File fObj = new File(dir_path);  
        if(fObj.exists() && fObj.isDirectory()){
            
            File a[] = fObj.listFiles();
            
            for(File arquivo : a){
                System.out.println("\n" + arquivo.getPath());
                System.out.println(arquivo.getName());
                String inputFile = arquivo.getPath();
                file_digest(inputFile, tipo_digest);
                
            }
        }
        else{
            System.err.println("O caminho indicado não é um diretório.");
            System.exit(3);
        }
    }
}
