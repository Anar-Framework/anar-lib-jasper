/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package af.gov.anar.lib.jasper.api;

import java.io.File;

import java.util.List;
import javax.validation.Valid;
import java.util.*; 

import java.sql.SQLException;
import com.fasterxml.jackson.databind.ObjectMapper;
import af.gov.anar.lib.jasper.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import net.sf.jasperreports.engine.JRException;
import af.gov.anar.lib.jasper.entity.Report;
import af.gov.anar.lib.jasper.service.ReportService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/api/config/reports")
public class ReportController {

    @Autowired
    private ReportService service;

    ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public List findAll() {
        return service.findAll();
    }

    @GetMapping(value = "/{id}")
    public Report findOne(@PathVariable("id") String id){
        return service.findById(id);
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Report create(@Valid @RequestBody Report report) {
        return service.create(report);
    }

    @PutMapping(value = "/{id}")
    public Report update(@PathVariable("id") String id, @RequestBody Report report){
        report.setId(id);
        return service.update(report);
    }

    @DeleteMapping(value = "/{id}")
    public Report delete(@PathVariable("id") Long id) {
        return service.delete(id);
    }


    @GetMapping(value = "/download/{id}/{reportType}")
	public void downloadReport(final HttpServletResponse response, final HttpServletRequest request,
			@PathVariable("id") String id, @PathVariable("reportType") String reportType) throws IOException, JRException,SQLException ,Exception
     {
        System.out.println("Entry>>>>downloadReport");
        try{
            Report reportRecord = this.service.findById(id);
            String returnedPath = this.service.generatePdfJasperReportFromDBRecord(reportRecord, reportType);
            System.out.println("report id +++++++++++++++++++++" + id);
            System.out.println("report type +++++++++++++++++++++" + reportType);

            this.service.downloadReport(returnedPath, response, id, reportType);
        }
        catch(Exception e){
            System.out.println("Some error has occurred while preparing the pdf Report.---------------" + e.getMessage());
            e.printStackTrace();
        }
    }
}