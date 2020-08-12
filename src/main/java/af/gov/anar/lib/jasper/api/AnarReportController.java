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
import af.gov.anar.lib.jasper.service.AnarReportService;
import af.gov.anar.lib.jasper.entity.AnarReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import net.sf.jasperreports.engine.JRException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController
@RequestMapping(value = "/api/config/reports")
public class AnarReportController {

    @Autowired
    private AnarReportService service;

    ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public List findAll() {
        return service.findAll();
    }

    @GetMapping(value = "/{id}")
    public AnarReport findOne(@PathVariable("id") String id){
        return service.findById(id);
    }

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public AnarReport create(@Valid @RequestBody AnarReport report) {
        return service.create(report);
    }

    @PutMapping(value = "/{id}")
    public AnarReport update(@PathVariable("id") String id, @RequestBody AnarReport report){
        report.setId(id);
        return service.update(report);
    }

    @DeleteMapping(value = "/{id}")
    public AnarReport delete(@PathVariable("id") Long id) {
        return service.delete(id);
    }

    @GetMapping(value = "/download/{id}/{reportType}/{locale}")
	public void downloadReport(final HttpServletResponse response, final HttpServletRequest request,
			@PathVariable("id") String id, @PathVariable("reportType") String reportType, @PathVariable("locale") String locale) throws IOException, JRException,SQLException ,Exception
    {
        try{
            AnarReport reportRecord = this.service.findById(id);
            String returnedPath = this.service.generatePdfJasperReportFromDBRecord(reportRecord, reportType, locale);

            this.service.downloadReport(returnedPath, response);
        }
        catch(Exception e){
            System.out.println("Some error has occurred while preparing the pdf AnarReport.---------------" + e.getMessage());
            e.printStackTrace();
        }
    }
}