package co.wileyedge.docgen;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Controller
public class DocgenApplication {

	private String documentPath;
	private String name;
	private String mail;

	@GetMapping("/")
	public String index() {
		// Schedule cleanup task to run every 3 minutes
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(this::cleanupUserDataFolder, 0, 3, TimeUnit.MINUTES);
		return "form";
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
							   @RequestParam("education_start_month") String educationStartMonth,
							   @RequestParam("educationStart") String educationStartYear,
							   @RequestParam("education_end_month") String educationEndMonth,
							   @RequestParam("education_end_year") String educationEndYear,
							   @RequestParam("education_start_month_2") String educationStartMonth2,
							   @RequestParam("educationStart2") String educationStartYear2,
							   @RequestParam("education_end_month_2") String educationEndMonth2,
							   @RequestParam("education_end_year_2") String educationEndYear2,
							   @RequestParam("secondaryEducation") String secondaryEducation,
							   @RequestParam("software_engineer") String softwareEngineer,
							   @RequestParam("internship") String internship,
							   @RequestParam("final_year_project") String finalYearProject,
							   @RequestParam("personal_project") String personalProject,
							   @RequestParam("achievements") String achievements,
							   @RequestParam("action") String action) {
		try {
			// Path to the template document
			File templateFile = new File("src/main/resources/templates/template.docx");

			// Path to the new document directory
			String directoryPath = "CV_Builder";

			// Generate unique file name for the new document
			UUID uuid = UUID.randomUUID();
			String fileName = name + uuid + ".docx";

			// Path to the new document
			Path newDocumentPath = Paths.get(directoryPath, fileName);
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
			replacePlaceholder(document, "userEducationStart", educationStartMonth + " " + educationStartYear);
			replacePlaceholder(document, "userEducationEnd", educationEndMonth + " " + educationEndYear);
			replacePlaceholder(document, "userEducation", education);
			replacePlaceholder(document, "userSecondaryEducationStart", educationStartMonth2 + " " + educationStartYear2);
			replacePlaceholder(document, "educationEnd", educationEndMonth2 + " " + educationEndYear2);
			replacePlaceholder(document, "userSecondaryEducation", secondaryEducation);
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

			this.documentPath = newDocumentPath.toString();
			this.name = name;
			this.mail = email;

		} catch (IOException e) {
			System.err.println("Error creating document: " + e.getMessage());
			return "redirect:/";
		}
		if (action.equals("Download")) {
			return "redirect:/download";
		}
		return "redirect:/downloadAndMail";
	}

	@GetMapping("/downloadAndMail")
	public ResponseEntity<Resource> downloadAndMail() {
		Email mailTo = new Email();
		mailTo.sendEmail(this.name, this.mail, this.documentPath);
		Resource resource = new FileSystemResource(documentPath);
		String fileName = documentPath.substring(documentPath.lastIndexOf(File.separator) + 1); // Get only the filename
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}

	@GetMapping("/download")
	public ResponseEntity<Resource> download() {
		Resource resource = new FileSystemResource(documentPath);
		String fileName = documentPath.substring(documentPath.lastIndexOf(File.separator) + 1); // Get only the filename
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}


	private void replacePlaceholder(XWPFDocument document, String placeholder, String value) {
		List<XWPFParagraph> paragraphsToRemove = new ArrayList<>();

		for (XWPFParagraph paragraph : document.getParagraphs()) {
			for (XWPFRun run : paragraph.getRuns()) {
				String text = run.getText(0);
				if (text != null && text.contains(placeholder)) {
					if (value.isEmpty()) {
						// If the value is empty, add the paragraph to the list to be removed
						paragraphsToRemove.add(paragraph);
					} else {
						// Otherwise, replace the placeholder with the value
						text = text.replace(placeholder, value);
						run.setText(text, 0);
					}
				}
			}
		}


		// Remove the paragraphs that need to be removed
		for (XWPFParagraph paragraph : paragraphsToRemove) {
			document.removeBodyElement(document.getPosOfParagraph(paragraph));
		}
	}

	private void cleanupUserDataFolder() {
		System.out.println("Running cleanupUserDataFolder()...");
		File userDataFolder = new File("CV_Builder");
		if (userDataFolder.exists() && userDataFolder.isDirectory()) {
			File[] files = userDataFolder.listFiles();
			if (files != null) {
				Instant now = Instant.now();
				for (File file : files) {
					Instant lastModified = Instant.ofEpochMilli(file.lastModified());
					Duration elapsedTime = Duration.between(lastModified, now);
					// Delete files older than 3 minutes (1800000 milliseconds)
					if (elapsedTime.toMillis() > 1800000) {
						if (file.delete()) {
							System.out.println("Deleted file: " + file.getName());
						} else {
							System.err.println("Failed to delete file: " + file.getName());
						}
					}
				}
			}
		}
	}

	@GetMapping("/sample")
	public ResponseEntity<Resource> getSampleHtml() {
		try {
			// Load the content of sample.html
			Resource resource = new ClassPathResource("templates/sample.html");
			if (!resource.exists()) {
				throw new FileNotFoundException("Sample HTML file not found");
			}
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
			return ResponseEntity.ok().headers(headers).body(resource);
		} catch (IOException e) {
			System.err.println("Error reading sample.html: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}


	public static void main(String[] args) {
		SpringApplication.run(DocgenApplication.class, args);
	}
}