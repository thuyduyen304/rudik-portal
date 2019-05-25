package com.rudik.controller;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/download")
public class FileController {

	@Autowired
    private ServletContext servletContext;

	@RequestMapping(path = "/template", method = RequestMethod.GET)
	public ResponseEntity<Resource> download(@RequestParam(required = false, defaultValue = "test.pdf") String file_name,
			@Value("${app.downloadPath}") String directory) throws Exception {
		String mine_type = servletContext.getMimeType(file_name);
		MediaType media_type;
        try {
            media_type = MediaType.parseMediaType(mine_type);
        } catch (Exception e) {
        	media_type = MediaType.APPLICATION_OCTET_STREAM;
        }
	 
        File file = new File(directory + "/templates" + "/" + file_name);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
 
        return ResponseEntity.ok()
                // Content-Disposition
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                // Content-Type
                .contentType(media_type)
                // Contet-Length
                .contentLength(file.length()) //
                .body(resource);
	}
}
