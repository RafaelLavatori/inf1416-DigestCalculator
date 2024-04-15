import java.security.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DigestCalculator {
    // Calculate digest of a file
    private static String file_digest(String file_path, String tipo_digest) throws Exception {
        try {         
            MessageDigest messageDigest = MessageDigest.getInstance(tipo_digest);
            byte[] data = Files.readAllBytes(Paths.get(file_path));
            messageDigest.update(data);

            byte [] digest = messageDigest.digest();
            
            StringBuffer buf = new StringBuffer();
            for(int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1);
                buf.append((hex.length() < 2 ? "0" : "") + hex);
            }
            
            String digest_string = buf.toString();
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

        File fObj = new File(directory_path);
        if (!fObj.exists() || !fObj.isDirectory()) {
            System.err.println("O caminho da pasta indicado não é um diretório.");
            System.exit(3);
        }

        XmlHandler xml_handler = new XmlHandler(xml_path);           
        File a[] = fObj.listFiles();
        
        for(File arquivo : a){                
            String inputFile = arquivo.getPath();

            String digest = file_digest(inputFile, digest_type);
            XmlHandler.Status file_status = xml_handler.queryFileDigest(arquivo.getName(), digest_type, digest);
            
            System.out.println(String.format("%s %s %s (%s)",arquivo.getName(),digest_type,digest,file_status));
        }

        xml_handler.SaveFile();
    }
}
