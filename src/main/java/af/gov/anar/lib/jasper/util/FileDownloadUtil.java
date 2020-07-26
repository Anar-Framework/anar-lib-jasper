package af.gov.anar.lib.jasper.util;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;

/*
 *  @author Jalil
 *  The FileDownloadUtil class is to be used for file downloads, Centralizing
 *  this function is necessary because we need it in many controllers. 
 *    
 */
@Component
public class FileDownloadUtil {

    /**
     * Method to do all the downloads
     * 
     * @param file     file to be downloaded
     * @param response HttpServetResponse object
     * 
     * 
     */

    public void fileDownload(File file, HttpServletResponse response) throws Exception {
        /**
         * The absolute path to the file, in case download fails for some reason we can
         * show the user a link to click to download the file
         */

        /*
         * String path = file.getAbsolutePath(); return path;
         */

        if (file.exists()) {
            response.setContentType(this.getContentTypeHeader(response, file.getName()));
            /**
             * In a regular HTTP response, the Content-Disposition response header is a
             * header indicating if the content is expected to be displayed inline in the
             * browser, that is, as a Web page or as part of a Web page, or as an
             * attachment, that is downloaded and saved locally.
             * 
             */

            /**
             * Here we have mentioned it to show attachment
             */
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + file.getName() + "\""));
            response.setContentLength((int) file.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());

        }
    }

    /**
     * Returns the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param path     file path to extract content type
     */
    private String getContentTypeHeader(HttpServletResponse response, String path) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        ////////////////////////////////////////
        // NOTE: this does not work and behaves different between Java releases!!!!!
        ///////////////////////////////////////
        String contentType = mimeTypesMap.getContentType(path);
        System.out.println("Content type of the file is: >>>>>>>>>>"+contentType);
        // patching mistake from the above broken code.
        if (path.endsWith("xlsx")) {
            contentType = "xlsx";
        } else if (path.endsWith("doc")) {
            contentType = "doc";
        } else if (path.endsWith("jpg")) {
            contentType = "image/jpeg";
        } else if( path.endsWith("pdf")){
            contentType = "pdf";
        }
        return contentType;
    }

}