/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package af.gov.anar.lib.jasper.service;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;
import java.util.*; 
import java.io.InputStream;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import java.sql.Connection;
import java.sql.DriverManager;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import java.io.FileInputStream;
import com.google.gson.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import af.gov.anar.lib.jasper.service.ReportService;
import af.gov.anar.lib.jasper.entity.Report;
import af.gov.anar.lib.jasper.repository.ReportRepository;

@Service
@PropertySource(value = "classpath:application.properties")
public class ReportService {

    @Autowired
    private ReportRepository repository;
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;
    
    public Report create(Report report){
        return repository.save(report);
    }

    public Report update(Report report){
        return repository.save(report);
    }

    public List<Report> findAll(){
        return repository.findAll();
    }

    public Report findById(String id){
        return repository.findById(id);
    }

    public Report delete(Long id)
    {
        Report obj = repository.findById(id).get();
        obj.setDeleted(true);
        obj.setDeletedAt(new Date());
        return repository.save(obj);
    }

    public String generatePdfJasperReportFromJRXMLFile(HashMap<String, Object> parameters, String jrxmlFile) throws IOException, JRException {
        /**
            This function generates a pdf report from a .jrxml file stored in the classpath
            
            A Usage Example can be:

                final Map<String, Object> parameters = new HashMap<>();
                parameters.put("name", "province name");
                String returnedPath = service.generatePdfJasperReportFromJRXMLFile(new HashMap<>(parameters), "/test-report.jrxml")

            @return - returns the path of the destination file.
         */

        // Fetching the .jrxml file from the resources folder.
        final InputStream stream = this.getClass().getResourceAsStream(jrxmlFile);
 
        String pdfFilePath = this.printJasperReport(stream, parameters);

        return pdfFilePath;
    }

    public String generatePdfJasperReportFromDBRecord(Report reportRecord) throws IOException, JRException {
        /**
            This function generates a pdf report from a Report record that has the parameters and xml content

            A usage Example can be:
            
            this.reportService.generatePdfJasperReportFromDBRecord(this.service.findById("DABS1207395515951"));
            
            @return - returns the path of the destination file.
         */

        String xmlContent = reportRecord.getXmlContent();

        // HashMap<String, Object> parameters = new Gson().fromJson(reportRecord.getParameters(), HashMap.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> parameters = mapper.readValue(reportRecord.getParameters(), Map.class);

        // First write the xmlContent string to a temporary file
        File tempJRXMLFile = File.createTempFile("ebreshna-temp-report", ".jrxml");
        FileWriter fw = new FileWriter(tempJRXMLFile);
        fw.write(xmlContent);
        fw.close();
        tempJRXMLFile.deleteOnExit();

        //  Read the xmlContent code from the file
        final InputStream stream = new FileInputStream(tempJRXMLFile.getPath());

        String pdfFilePath = this.printJasperReport(stream, parameters);

        return pdfFilePath;
    }
    
    private String printJasperReport(InputStream stream, Map<String, Object> parameters) throws IOException, JRException{
        
        /**
            This function generates a pdf file from the given stream with the given parameters and saves it into a 
            a temporary file.

            @return - returns the path of the temp file.
         */

        // The Generated PDF file will be stored in a temp directory
        String destFile = File.createTempFile("ebreshna-temp-report", ".pdf").getPath();

        // Compile the Jasper report from .jrxml to .japser
        final JasperReport report = JasperCompileManager.compileReport(stream);
 
        // The URL would look something like "jdbc:postgresql://localhost:5432/ebreshna_test?user=some_user&password=some_password"
        String url = String.format("%s?user=%s&password=%s", this.datasourceUrl, this.datasourceUsername, this.datasourcePassword);
        
        try{
            Connection conn = DriverManager.getConnection(url);
            
            final JasperPrint print = JasperFillManager.fillReport(report, parameters, conn);
            // Export the report to a PDF file.
            JasperExportManager.exportReportToPdfFile(print, destFile);
        }catch(Exception e){
            System.out.println("Some error has occurred while preparing the pdf Report." + e.getMessage());
            e.printStackTrace();
        }

        return destFile;
    }
}