package com.sta.biometric.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet para descargar archivos Excel generados dinámicamente.
 * 
 * Lee el contenido del archivo desde la sesión HTTP y lo envía al cliente.
 * 
 * @author Sistema STARH
 */
public class DownloadExcelServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // Obtener datos del archivo desde la sesión
        byte[] fileContent = (byte[]) session.getAttribute("EXCEL_FILE_CONTENT");
        String fileName = (String) session.getAttribute("EXCEL_FILE_NAME");
        String fileType = (String) session.getAttribute("EXCEL_FILE_TYPE");

        if (fileContent == null || fileName == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado en sesión");
            return;
        }

        // Configurar headers para descarga
        response.setContentType(
                fileType != null ? fileType : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentLength(fileContent.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Escribir contenido al response
        OutputStream out = response.getOutputStream();
        out.write(fileContent);
        out.flush();

        // Limpiar sesión
        session.removeAttribute("EXCEL_FILE_CONTENT");
        session.removeAttribute("EXCEL_FILE_NAME");
        session.removeAttribute("EXCEL_FILE_TYPE");
    }
}
