/*
 * $Id: 6aed4dfc0aed27c7ac7b0e1a85f680e69c719dee $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import servletpack.pdf.AlpPdf;

/**
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ２３３Ｃ＞  講座別名列
 */
public class KNJA233C_AlpPdf extends KNJA233C {

    public Boolean print(final String basePath, final HttpServletRequest request, final OutputStream outputStream) throws Exception {
        return AlpPdf.print(basePath, request, outputStream, null, null, this);
    }
    
}
