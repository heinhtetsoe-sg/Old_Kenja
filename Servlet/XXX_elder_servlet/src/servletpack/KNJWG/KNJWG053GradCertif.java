// kanji=漢字
/*
 * $Id: a19fce3239753213e721bb248a36a08ef1f89c5c $
 *
 * 作成日: 2008/03/06 15:20:37 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWG;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_EditDate;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: a19fce3239753213e721bb248a36a08ef1f89c5c $
 */
public class KNJWG053GradCertif {

    /**
     *  KNJWG053.classから最初に起動されるクラス
     */
    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response,
            final List students,
            final String schoolName,
            final String staffName)
    throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        try {
            //  print設定
            response.setContentType("application/pdf");

            //  svf設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            //  該当データ無し
            if (students.size() == 0) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            } else {
                svf.VrSetForm("KNJWG010_1.frm", 1);
                for (final Iterator iter = students.iterator(); iter.hasNext();) {
                    final Map student = (Map) iter.next();
                    
                    svf.VrsOut("NAME", (String) student.get("NAME")); // 氏名
                    svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP_Bth((String) student.get("BIRTHDAY"))); // 生年月日
                    svf.VrsOut("GRADUATION", KNJ_EditDate.h_format_JP_M(request.getParameter("GRADUATE_DATE"))); // 卒業年月日
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(request.getParameter("NOTICEDAY"))); // 卒業年月日
                    svf.VrsOut("SCHOOLNAME", schoolName);
                    svf.VrsOut("STAFFNAME", staffName);

                    svf.VrEndPage();
                }
            }
        } finally {
            if (null != svf) {
                svf.VrQuit();
            }
        }

    }

}
 // KNJWG053GradCertif

// eof
