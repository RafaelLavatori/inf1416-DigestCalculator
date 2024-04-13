import java.security.*;
import javax.crypto.*;
import java.io.*;

// Generate a DigestCalculator
public class DigestCalculator {
    
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
