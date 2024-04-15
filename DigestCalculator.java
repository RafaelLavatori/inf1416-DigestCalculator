import java.security.*;
import java.io.*;

// Generate a DigestCalculator
public class DigestCalculator {
    private static String file_digest(String inputFile, String tipo_digest) throws Exception {
        try (
            InputStream inputStream = new FileInputStream(inputFile);
        ) {
            long fileSize = new File(inputFile).length();
            byte[] allBytes = new byte[(int) fileSize];
         
            // int bytesRead = inputStream.read(allBytes);
            //imprime os bytes lidos do arquivo        
            //for(int i =0; i<(int) fileSize; i++){
                //System.out.println(allBytes[i]);    
            //}
            
            MessageDigest messageDigest = MessageDigest.getInstance(tipo_digest);
            // System.out.println( "\n" + messageDigest.getProvider().getInfo() );
            messageDigest.update(allBytes);
            byte [] digest = messageDigest.digest();
            // System.out.println( "\nDigest length: " + digest.length * 8 + "bits" );
            
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1);
                buf.append((hex.length() < 2 ? "0" : "") + hex);
            }     
            
            String digest_string = buf.toString();

            // imprime o digest em hexadecimal
            // System.out.println( "\nDigest(hex): " );
            // System.out.println(digest);
            return digest_string;

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
    }
    public static void main (String[] args) throws Exception {

        //check args
        if (args.length != 3) {
          System.err.println("Usage: DigestCalculator<SP>Tipo_Digest<SP>Caminho_da_Pasta_dos_Arquivos<SP>Caminho_ArqListaDigest");
          System.exit(1);
        }
        
        if (!args[0].equals("MD5") &&
                !args[0].equals("SHA1") &&
                !args[0].equals("SHA256") &&
                !args[0].equals("SHA512")) {
            System.err.println("Tipo de digest inválido\nTipos suportados: MD5, SHA1, SHA256, SHA512");
            System.exit(2);
        }
        
        String digest_type = args[0];
        String directory_path = args[1];
        String xml_path = args[2];

        XmlHandler xml_handler = new XmlHandler(xml_path);
        
        File fObj = new File(directory_path);
        if(fObj.exists() && fObj.isDirectory()){
            
            File a[] = fObj.listFiles();
            
            for(File arquivo : a){
                System.out.println("\n" + arquivo.getPath());
                
                String inputFile = arquivo.getPath();

                String digest = file_digest(inputFile, digest_type);
                XmlHandler.Status file_status = xml_handler.queryFileDigest(arquivo.getName(), digest_type, digest);
                
                System.out.println(String.format("%s %s %s (%s)",arquivo.getName(),digest_type,digest,file_status));
            }

            xml_handler.SaveFile();
        }
        else{
            System.err.println("O caminho indicado não é um diretório.");
            System.exit(3);
        }
    }
}
