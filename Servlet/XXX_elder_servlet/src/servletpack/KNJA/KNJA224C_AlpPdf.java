/*
 * $Id: 64ef133b79115e40042f3d6f23da5ca1e7e11121 $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import servletpack.pdf.AlpPdf;

/**
 * ＨＲ別名票
 */
public class KNJA224C_AlpPdf extends KNJA224C {

    public Boolean print(final String basePath, final HttpServletRequest request, final OutputStream outputStream) throws Exception {
        return AlpPdf.print(basePath, request, outputStream, null, null, this);
    }
    
}
