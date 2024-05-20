package Controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.io.InputStream;

import java.util.Arrays;

import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


import model.ProductModel;
import model.game;

/**
 * Servlet implementation class AddGame
 */
@WebServlet("/AddGame")
@MultipartConfig()
public class UploadGame extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static String SAVE_DIR = "img";
	static ProductModel GameModels = new ProductModelDM();
	
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	LocalDateTime now = LocalDateTime.now();
	
	
    public UploadGame() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");

		out.write("Error: GET method is used but POST method is required");
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    // Percorso dove salvare i file caricati
	    String savePath = request.getServletContext().getRealPath("") + File.separator + SAVE_DIR;

	    // Lista di estensioni permesse
	    List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");

	    // Inizializzazione dell'oggetto game
	    game g1 = new game();

	    // Validazione del file caricato
	    boolean isValidFile = true;
	    String fileName = null;
	    String message = "upload =\n";
	    
	    if (request.getParts() != null && request.getParts().size() > 0) {
	        for (Part part : request.getParts()) {
	            fileName = extractFileName(part);

	            if (fileName != null && !fileName.equals("")) {
	                // Ottieni l'estensione del file
	                String fileExtension = getFileExtension(fileName);
	                
	                // Verifica se l'estensione è permessa
	                if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
	                    isValidFile = false;
	                    request.setAttribute("error", "Errore: Estensione del file non permessa");
	                    break;
	                }

	                // Verifica il contenuto del file
	                if (!isValidFileContent(part, fileExtension)) {
	                    isValidFile = false;
	                    request.setAttribute("error", "Errore: Contenuto del file non valido");
	                    break;
	                }

	                // Salva il file se valido
	                if (isValidFile) {
	                    part.write(savePath + File.separator + fileName);
	                    g1.setImg(fileName);
	                    message = message + fileName + "\n";
	                }
	            } else {
	                request.setAttribute("error", "Errore: Bisogna selezionare almeno un file");
	            }
	        }
	    }

	    if (isValidFile) {
	        // Procedi con il salvataggio del gioco
	        g1.setName(request.getParameter("nomeGame"));
	        g1.setYears(request.getParameter("years"));
	        g1.setAdded(dtf.format(now));
	        g1.setQuantity(Integer.valueOf(request.getParameter("quantita")));
	        g1.setPEG(Integer.valueOf(request.getParameter("PEG")));
	        g1.setIva(Integer.valueOf(request.getParameter("iva")));
	        g1.setGenere(request.getParameter("genere"));
	        g1.setDesc(request.getParameter("desc"));
	        g1.setPrice(Float.valueOf(request.getParameter("price")));

	        try {
	            GameModels.doSave(g1);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        request.setAttribute("stato", "success!");
	        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/gameList?page=admin&sort=added DESC");
	        dispatcher.forward(request, response);
	    }
	}

	// Metodo per estrarre l'estensione del file
	private String getFileExtension(String fileName) {
	    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
	        return fileName.substring(fileName.lastIndexOf(".") + 1);
	    } else {
	        return "";
	    }
	}

	// Metodo per validare il contenuto del file (implementazione di base)
	private boolean isValidFileContent(Part part, String fileExtension) throws IOException {
	    // Implementare un controllo di validità del contenuto basato sul tipo di file
	    // Ad esempio, verificare l'intestazione del file per i tipi di file binari
	    // oppure usare librerie esterne per verificare l'integrità del file.
	    
	    // Questo è un esempio semplificato per immagini
	    if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
	        // Controllare se il file è un JPEG valido
	        try (InputStream input = part.getInputStream()) {
	            BufferedImage image = ImageIO.read(input);
	            return (image != null);
	        } catch (IOException e) {
	            return false;
	        }
	    }
	    // Implementare altre verifiche per altri tipi di file
	    return true;
	}

	private String extractFileName(Part part) {
		// content-disposition: form-data; name="file"; filename="file.txt"
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return "";
	}
	

}