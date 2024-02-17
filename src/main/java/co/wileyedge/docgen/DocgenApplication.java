package co.wileyedge.docgen;

import org.apache.poi.xwpf.usermodel.*;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@SpringBootApplication
@Controller
public class DocgenApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocgenApplication.class, args);
	}

	@GetMapping("/")
	public String index() {
		return "form"; // This will return the HTML form page
	}

	@PostMapping("/generateDocx")
	public String generateDocx(@RequestParam("email") String email,
							   @RequestParam("phoneNumber") String phoneNumber,
							   @RequestParam("name") String name,
							   @RequestParam("linkedin") String linkedin,
							   @RequestParam("summary") String summary,
							   @RequestParam("languages") String languages,
							   @RequestParam("technologies") String technologies,
							   @RequestParam("education") String education,
							   @RequestParam("software_engineer") String softwareEngineer,
							   @RequestParam("internship") String internship,
							   @RequestParam("final_year_project") String finalYearProject,
							   @RequestParam("personal_project") String personalProject,
							   @RequestParam("achievements") String achievements
							   ) {
		try {
			// Path to the template document
			File templateFile = new File("src/main/resources/templates/template.docx");

			// Path to the new document directory
			String directoryPath = "UserData";

			// Generate unique file name for the new document
			UUID uuid = UUID.randomUUID();
			String fileName = "user_" + uuid.toString() + ".docx";

			// Path to the new document
			Path newDocumentPath = Paths.get(directoryPath, fileName);

			// Load the template document
			FileInputStream fis = new FileInputStream(templateFile);
			XWPFDocument document = new XWPFDocument(fis);
			fis.close();

			// Replace placeholders in the document
			replacePlaceholder(document, "userEmail", email);
			replacePlaceholder(document, "userNumber", phoneNumber);
			replacePlaceholder(document, "candidateName", name);
			replacePlaceholder(document, "userLinkedin", linkedin);
			replacePlaceholder(document, "userSummary", summary);
			replacePlaceholder(document, "userKnownLanguages", languages);
			replacePlaceholder(document, "userKnownTechnologies", technologies);
			replacePlaceholder(document, "userEducation", education);
			replacePlaceholder(document, "userSoftwareEngineer", softwareEngineer);
			replacePlaceholder(document, "userInternship", internship);
			replacePlaceholder(document, "userFinalYearProject", finalYearProject);
			replacePlaceholder(document, "userPersonalProject", personalProject);
			replacePlaceholder(document, "userAchievements", achievements);

			// Save the modified document
			FileOutputStream fos = new FileOutputStream(newDocumentPath.toFile());
			document.write(fos);
			fos.close();
			document.close();

			System.out.println("New document created successfully: " + newDocumentPath);
		} catch (IOException e) {
			System.err.println("Error creating document: " + e.getMessage());
			return "redirect:/"; // Redirect to form page with an error message
		}

		return "redirect:/"; //
	}

	private void replacePlaceholder(XWPFDocument document, String placeholder, String value) {
		for (XWPFParagraph paragraph : document.getParagraphs()) {
			for (XWPFRun run : paragraph.getRuns()) {
				String text = run.getText(0);
				if (text != null && text.contains(placeholder)) {
					text = text.replace(placeholder, value);
					run.setText(text, 0);
				}
			}
		}
	}

}





