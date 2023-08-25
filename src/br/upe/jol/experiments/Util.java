/**
 * 
 */
package br.upe.jol.experiments;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.NumberFormat;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * @author Danilo
 * 
 */
public class Util {
	public static final NumberFormat decimalFormat = NumberFormat.getInstance();
	
	static {
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setMaximumFractionDigits(2);
	}
	
	/**
	 * Retorna uma imagem, considerando a possibilidade de estar empacotado em
	 * um arquivo JAR ou não.
	 * 
	 * @param imageName
	 *            Nome da imagem
	 * @return Objeto representando a imagem
	 */
	public static Image loadImage(final String imageName) {
		final ClassLoader loader = Util.class.getClassLoader();
		Image image = null;
		InputStream is = (InputStream) AccessController
				.doPrivileged(new PrivilegedAction<InputStream>() {
					public InputStream run() {
						if (loader != null) {
							return loader.getResourceAsStream(imageName);
						} else {
							return ClassLoader
									.getSystemResourceAsStream(imageName);
						}
					}
				});
		if (is != null) {
			try {
				final int BlockLen = 256;
				int offset = 0;
				int len;
				byte imageData[] = new byte[BlockLen];
				while ((len = is.read(imageData, offset, imageData.length
						- offset)) > 0) {
					if (len == (imageData.length - offset)) {
						byte newData[] = new byte[imageData.length * 2];
						System.arraycopy(imageData, 0, newData, 0,
								imageData.length);
						imageData = newData;
						newData = null;
					}
					offset += len;
				}
				image = java.awt.Toolkit.getDefaultToolkit().createImage(
						imageData);
			} catch (java.io.IOException ex) {
			}
		}

		if (image == null) {
			image = new ImageIcon(imageName).getImage();
		}
		return image;
	}

	/**
	 * Retorna um input stream, considerando a possibilidade de estar empacotado
	 * em um arquivo JAR ou não.
	 * 
	 * @param fileName
	 *            Nome do arquivo
	 * @return Objeto InputStream
	 */
	public static InputStream getInputStream(final String fileName)
			throws FileNotFoundException {
		final ClassLoader loader = Util.class.getClassLoader();
		InputStream is = (InputStream) AccessController
				.doPrivileged(new PrivilegedAction<InputStream>() {
					public InputStream run() {
						if (loader != null) {
							return loader.getResourceAsStream(fileName);
						} else {
							return ClassLoader
									.getSystemResourceAsStream(fileName);
						}
					}
				});
		if (is == null) {
			is = new FileInputStream(new File(fileName));
		}
		return is;
	}

	public static void writeImageToJPG(File file, BufferedImage bufferedImage)
			throws IOException {
		ImageIO.write(bufferedImage, "jpg", file);
	}

	public static void writeImageToBmp(File file, BufferedImage bufferedImage)
			throws IOException {
		ImageIO.write(bufferedImage, "bmp", file);
	}

	public static void doZip(String filename, String zipfilename) {
		try {
			byte[] buf = new byte[1024];
			FileInputStream fis = new FileInputStream(filename);
			fis.read(buf, 0, buf.length);

			CRC32 crc = new CRC32();
			ZipOutputStream s = new ZipOutputStream(
					(OutputStream) new FileOutputStream(zipfilename));

			s.setLevel(6);

			ZipEntry entry = new ZipEntry(filename);
			entry.setSize((long) buf.length);
			crc.reset();
			crc.update(buf);
			entry.setCrc(crc.getValue());
			s.putNextEntry(entry);
			s.write(buf, 0, buf.length);
			s.finish();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static final String CAMINHO_BASE = "D:\\Home\\workspace\\SiacStoreWeb\\WebContent";
	private static String conteudoArquivoMAR = "";
	private static int qtdeDirs = 0;
	private static int qtdeDirsInner = 0;
	public static void main(String[] args) {
		File raiz = new File(CAMINHO_BASE);
		char[] buffer;
		try {
			File f = new File("D:\\itautecdocs\\siac store\\064405\\Documentos Auxiliares\\MAR_064405.csv");
			FileReader fr = new FileReader(f);
			buffer = new char[(int)f.length()];
			fr.read(buffer);
			conteudoArquivoMAR = new String(buffer);
			criarPlanilhaRecursivo(raiz, false);
			criarPlanilhaRecursivo(raiz, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void criarPlanilhaRecursivo(File file, boolean nome) throws IOException{
		File planilha = null;
		
		if (!file.isDirectory()){
			if (!conteudoArquivoMAR.contains(file.getName())){
//				System.out.println("- " + file.getAbsolutePath());
			}
//			if (file.getName().endsWith(".java")){
//				planilha = new File("C:\\PR063058\\Liberações\\RRT_Entrega_1\\RRT_063058_" + file.getName() + ".xls");
//				copy(new File("C:\\PR063058\\Liberações\\RRT_063058_.java.xls"), planilha);
//			}else if (file.getName().endsWith(".jsp")){
//				planilha = new File("C:\\PR063058\\Liberações\\RRT_Entrega_1\\RRT_063058_" + file.getName() + ".xls");
//				copy(new File("C:\\PR063058\\Liberações\\RRT_063058_.jsp.xls"), planilha);
//			}else{
//				planilha = new File("C:\\PR063058\\Liberações\\RRT_Entrega_1\\RRT_063058_" + file.getName() + ".xls");
//				copy(new File("C:\\PR063058\\Liberações\\RRT_063058_.xml.xls"), planilha);
//			}
		}else{
			if (!file.getAbsolutePath().contains(".svn") && !file.getAbsolutePath().contains("classes")){
				
				if (nome){
					
					qtdeDirsInner++;
					if (qtdeDirsInner < qtdeDirs/3){
						System.out.print("Danilo    ");	
					}else if (qtdeDirsInner < 2* qtdeDirs/3){
						System.out.print("João      ");
					}else{
						System.out.print("Claudianne");
					}
					System.out.print("\t" + file.getAbsolutePath());
					System.out.println();
				}else{
					qtdeDirs++;
				}
			}
			
			for (String filho : file.list()){
				criarPlanilhaRecursivo(new File(file.getAbsolutePath() + File.separator + filho), nome);
			}
		}
	}
	
	static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);   

        // Transferindo bytes de entrada para saída
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
