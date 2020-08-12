/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package af.gov.anar.lib.jasper.service;

import java.sql.SQLException;
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
import af.gov.anar.lib.jasper.service.AnarReportService;
import af.gov.anar.lib.jasper.entity.AnarReport;
import af.gov.anar.lib.jasper.repository.AnarReportRepository;
import javax.servlet.http.HttpServletResponse;
import af.gov.anar.lib.jasper.util.AnarFileDownloadUtil;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRParameter;

@Service
@PropertySource(value = "classpath:application.properties")
public class AnarReportService {

    @Autowired
    private AnarReportRepository repository;
    
    @Autowired
    AnarFileDownloadUtil fileDownloadUtil;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    public AnarReport create(AnarReport report){
        return repository.save(report);
    }

    public AnarReport update(AnarReport report){
        return repository.save(report);
    }

    public List<AnarReport> findAll(){
        return repository.findAll();
    }

    public AnarReport findById(String id){
        return repository.findById(id);
    }

    public AnarReport delete(Long id)
    {
        AnarReport obj = repository.findById(id).get();
        obj.setDeleted(true);
        obj.setDeletedAt(new Date());
        return repository.save(obj);
    }

    public String generatePdfJasperReportFromJRXMLFile(HashMap<String, Object> parameters, String jrxmlFile, String reportType, String locale) throws IOException, JRException,SQLException {
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
        String pdfFilePath = this.printJasperReport(stream, parameters, reportType);

        return pdfFilePath;
    }

    public String generatePdfJasperReportFromDBRecord(AnarReport reportRecord, String reportType, String locale) throws IOException, JRException, SQLException {
        /**
            This function generates a pdf report from a AnarReport record that has the parameters and xml content

            A usage Example can be:
            
            this.reportService.generatePdfJasperReportFromDBRecord(this.service.findById("DABS1207395515951"));
            
            @return - returns the path of the destination file.
         */

        String xmlContent = reportRecord.getXmlContent();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> parameters = mapper.readValue(reportRecord.getParameters(), Map.class);

        parameters.put(JRParameter.REPORT_LOCALE, new Locale(locale));

        // First write the xmlContent string to a temporary file
        File tempJRXMLFile = File.createTempFile("ebreshna-temp-report", ".jrxml");
        FileWriter fw = new FileWriter(tempJRXMLFile);
        fw.write(xmlContent);
        fw.close();
        tempJRXMLFile.deleteOnExit();

        //  Read the xmlContent code from the file
        final InputStream stream = new FileInputStream(tempJRXMLFile.getPath());

        String pdfFilePath = this.printJasperReport(stream, parameters, reportType);

        return pdfFilePath;
    }
    

    public void downloadReport(String filePath, HttpServletResponse response) throws Exception{
        File file = new File(filePath);
   
        if (file.exists()) {
            fileDownloadUtil.fileDownload(file, response);
        }
    }

    private String printJasperReport(InputStream stream, Map<String, Object> parameters, String reportType) throws IOException, JRException,SQLException{
        
        /**
            This function generates a pdf file from the given stream with the given parameters and saves it into a 
            a temporary file.

            @return - returns the path of the temp file.
         */

        System.out.println("parameters+++++++++++++++++++++++++++++" + parameters);
        // Compile the Jasper report from .jrxml to .japser
        final JasperReport report = JasperCompileManager.compileReport(stream);
 
        // The URL would look something like "jdbc:postgresql://localhost:5432/ebreshna_test?user=some_user&password=some_password"
        String url = String.format("%s?user=%s&password=%s", this.datasourceUrl, this.datasourceUsername, this.datasourcePassword);
        
        // The Generated PDF file will be stored in a temp directory
        String destFile = "";

        try{
            Connection conn = DriverManager.getConnection(url);
            
            final JasperPrint print = JasperFillManager.fillReport(report, parameters, conn);
            // Export the report to a PDF file.

            if(reportType.equals("pdf")){
                destFile = File.createTempFile("temp-jasper-report", ".pdf").getPath();
                JasperExportManager.exportReportToPdfFile(print, destFile);
            }
            else if(reportType.equals("excel")){

                destFile = File.createTempFile("temp-jasper-report", ".xlsx").getPath();

                // JRXlsExporter exporter = new JRXlsExporter();
                JRExporter exporter = new JRXlsExporter();
                exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
                exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
                //we set the one page per sheet parameter here
                exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
                
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
                exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile);
  
                // exporter.setParameter(JRExporterParameter.INPUT_STREAM, print);
                // exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile);
                exporter.exportReport();
            }
            else if(reportType.equals("csv")){
                destFile = File.createTempFile("temp-jasper-report", ".csv").getPath();
                
                JRCsvExporter exporter = new JRCsvExporter();
                exporter = new JRCsvExporter();
                exporter.setExporterInput(new SimpleExporterInput(print));
                exporter.setExporterOutput(new SimpleWriterExporterOutput(new File(destFile)));
                SimpleCsvExporterConfiguration configuration = new SimpleCsvExporterConfiguration();
                configuration.setWriteBOM(Boolean.TRUE);
                configuration.setRecordDelimiter("\r\n");
                exporter.setConfiguration(configuration);
                exporter.exportReport();
            }
        }catch(Exception e){
            System.out.println("Some error has occurred while preparing the pdf AnarReport." + e.getMessage());
            e.printStackTrace();
        }

        return destFile;
    } 
}