// kanji=漢字
/*
 * $Id: df316ca9a75ebe819d12b4f95a4c25320d3dcc68 $
 *
 * 作成日: 2009/03/20
 * 作成者: nakamoto
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [学籍管理] ＜ＫＮＪＡ１４６＞ 身分証明書（自修館）
 */

public class KNJA430S {

    private static final Log log = LogFactory.getLog(KNJA430S.class);

    Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //  ＳＶＦ作成処理
            boolean hasData = false; //該当データなしフラグ

            //SVF出力
            hasData = printMain(db2, svf);

            log.debug("hasData=" + hasData);

            //  該当データ無し
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            svf.VrQuit();
            db2.commit();
            db2.close();
        }

    }// doGetの括り

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _staffCd;
        private final String _stampNo;
        private final String _formId;
        private final String _formName;
        private final boolean _isKakuninHyou;
        private final String _documentRoot;
        private final String _imageFolder;
        private final String _fileExtension;

        private final boolean _isSeireki;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            // 印影コード
            _staffCd = request.getParameter("HID_STAFFCD");
            _stampNo = request.getParameter("HID_STAMP_NO");
            // フォーム
            _formId = request.getParameter("FORMID");
            _formName = _formId + ".frm";
            _isKakuninHyou = "KNJA430S_2".equals(_formId) ? true : false;
            // 印影
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imageFolder = "image/stamp"; // 格納フォルダ
            _fileExtension = "bmp"; // 拡張子
            _isSeireki = true;
        }

        private String changePrintDate(final String date) {
            if (date == null) return "";
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        private String getPrintDate(final String date, final String div) {
            if (date == null) return "";
            if ("y".equals(div)) return date.substring(0, 4);
            if ("m".equals(div)) return String.valueOf(Integer.parseInt(date.substring(5, 7)));
            if ("d".equals(div)) return String.valueOf(Integer.parseInt(date.substring(8,10)));
            return "";
        }
    }

    private boolean printMain(DB2UDB db2, Vrw32alp svf) throws ParseException, SQLException {
        svf.VrSetForm(_param._formName, 1);
        final List printData = getPrintData(db2);
        boolean hasData = printOut(svf, printData);
        return hasData;
    }

    private class Stamp {
        final String _stampNo;
        final String _staffCd;
        final String _staffName;
        final String _sDate;
        final String _eDate;
        final String _sReason;
        final String _eReason;

        public Stamp(
                final String stampNo,
                final String staffCd,
                final String staffName,
                final String sDate,
                final String eDate,
                final String sReason,
                final String eReason
        ) {
            _stampNo = stampNo;
            _staffCd = staffCd;
            _staffName = staffName;
            _sDate = sDate;
            _eDate = eDate;
            _sReason = sReason;
            _eReason = eReason;
        }

        public String toString() {
            return "印鑑番号:" + _stampNo
            + " 職員番号:" + _staffCd
            + " 名前:" + _staffName;
        }

    }

    private List getPrintData(final DB2UDB db2) throws ParseException, SQLException {
        final List rtnList = new ArrayList();
        final String stampSql = getStampInfoSql();
        log.debug(stampSql);
        PreparedStatement psStamp = null;
        ResultSet rsStamp = null;

        try {
            psStamp = db2.prepareStatement(stampSql);
            rsStamp = psStamp.executeQuery();
            while (rsStamp.next()) {
                final Stamp stamp = new Stamp(rsStamp.getString("STAMP_NO"),
                                                      rsStamp.getString("STAFFCD"),
                                                      rsStamp.getString("STAFFNAME"),
                                                      rsStamp.getString("START_DATE"),
                                                      rsStamp.getString("STOP_DATE"),
                                                      rsStamp.getString("START_REASON"),
                                                      rsStamp.getString("STOP_REASON"));
                rtnList.add(stamp);
                log.debug(stamp);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, psStamp, rsStamp);
        }
        
        return rtnList;
    }

    private String getStampInfoSql() {
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.STAMP_NO, ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     L1.STAFFNAME, ");
        stb.append("     T1.DIST, ");
        stb.append("     T1.DATE, ");
        stb.append("     T1.START_DATE, ");
        stb.append("     T1.STOP_DATE, ");
        stb.append("     T1.START_REASON, ");
        stb.append("     T1.STOP_REASON ");
        stb.append(" FROM ");
        stb.append("     ATTEST_INKAN_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.STAMP_NO='" + _param._stampNo + "' ");

        return stb.toString();
    }

    private boolean printOut(final Vrw32alp svf, final List printData) {
        boolean hasData = false;
        for (final Iterator iter = printData.iterator(); iter.hasNext();) {
            final Stamp stamp = (Stamp) iter.next();

            svf.VrsOut("STAFFNAME", stamp._staffName);
            svf.VrsOut("STAFFCD", stamp._staffCd);
            svf.VrsOut("STAMP_NO", stamp._stampNo);
            svf.VrsOut("START_YEAR", _param.getPrintDate(stamp._sDate, "y"));
            svf.VrsOut("START_MONTH", _param.getPrintDate(stamp._sDate, "m"));
            svf.VrsOut("START_DAY", _param.getPrintDate(stamp._sDate, "d"));
            svf.VrsOut("REGI_REASON", stamp._sReason);

            if (_param._isKakuninHyou) {
                svf.VrsOut("STOP_YEAR", _param.getPrintDate(stamp._eDate, "y"));
                svf.VrsOut("STOP_MONTH", _param.getPrintDate(stamp._eDate, "m"));
                svf.VrsOut("STOP_DAY", _param.getPrintDate(stamp._eDate, "d"));
                svf.VrsOut("STOP_REASON", stamp._eReason);
                
                final String image_pass = _param._documentRoot + "/" + _param._imageFolder + "/";   //イメージパス
                String stampCheck = image_pass + stamp._stampNo + "." + _param._fileExtension;    //印影
                setImage(svf, stampCheck, "BITMAP");
            }

            svf.VrEndPage();
            hasData = true;
        }
        return hasData;
    }

    private void setImage(final Vrw32alp svf, final String file_check, final String fieldName) {
        File file = new File(file_check);
        if (file.exists()) {
            svf.VrsOut(fieldName, file_check );
        }
    }

}// クラスの括り
