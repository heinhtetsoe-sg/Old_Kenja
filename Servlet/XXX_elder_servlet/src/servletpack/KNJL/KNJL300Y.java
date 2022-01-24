// kanji=漢字
/*
 * $Id: c5d926bf2eab54f6475e52c84e170384d484b37b $
 *
 * 作成日: 2010/11/04 16:10:26 - JST
 * 作成者: maeshiro
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３００Ｙ＞  机上タックシール
 **/
public class KNJL300Y {

    private static final Log log = LogFactory.getLog(KNJL300Y.class);

    private Param _param = null;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        
        final PrintWriter outstrm = init(response, svf);
        
        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }
        
        //  パラメータの取得
        _param = new Param(db2, request);
        
        boolean hasData = false;
        try {
            hasData = printSvfMain(svf, db2);
            
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
        } finally {
            //  終了処理
            db2.commit();
            db2.close();
            svf.VrQuit();
            outstrm.close(); 
        }
    }
    
    private PrintWriter init(final HttpServletResponse response, Vrw32alp svf) throws IOException {
        //  print設定
        PrintWriter outstrm = new PrintWriter(response.getOutputStream());
        response.setContentType("application/pdf");
        
        //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        return outstrm;
    }
    
    private boolean printSvfMain(Vrw32alp svf, DB2UDB db2) {
        
        final String formname = "1".equals(_param._label) ? "KNJL300Y_1.frm" : "KNJL300Y_2.frm";
        svf.VrSetForm(formname, 1);
        boolean hasData = false;
        for (int i = 0; i < _param._categorySelected.length; i++) {
            if (setSvfout(db2, _param._categorySelected[i], svf)) {
                hasData = true;
            }
        }
        return hasData;
    }
    
    /** 帳票出力 **/
    private boolean setSvfout(final DB2UDB db2, final String classcd, final Vrw32alp svf) {
        final List examnoList = getExamnoList(db2, classcd);
        
        boolean nonedata = false;
        try {
            int line = _param._line;   //行番号
            int row = _param._row;   //列番号
            
            for (final Iterator it = examnoList.iterator(); it.hasNext();) {
                final String examno = (String) it.next();
                
                //最終列
                if (row > _param._maxRow) {
                    row = 1;
                    line++;
                    //最終行
                    if (line > _param._maxLine) {
                        svf.VrEndPage();
                        line = 1;
                    }
                }
                
                svf.VrsOutn("TESTDIV" + String.valueOf(row), line, _param._testDivName);
                svf.VrsOutn("EXAMNO"  + String.valueOf(row), line, examno);
                row++;
                nonedata = true;
            }
            
            //最終ページを出力
            if (nonedata) svf.VrEndPage();
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        }
        return nonedata;
    }
    
    private List getExamnoList(final DB2UDB db2, final String classcd) {
        PreparedStatement ps1 = null;
        final List examnos = new ArrayList();
        try {
            //連結されているパラメータの分割（試験会場コード + '-' + 開始受付番号（MIN）+ '-' + 終了受付番号（MAX））
            final String hallCd = classcd.substring(0,4);                 //試験会場コード
//            final String sExamNo = classcd.substring(5,10);              //開始受付番号（MIN）
//            final String eExamNo = classcd.substring(11);               //終了受付番号（MAX）
            
            final String sql = getExamnoSql(hallCd);
            // log.debug(" sql = " + sql);
            ps1 = db2.prepareStatement(sql);
            final ResultSet rs = ps1.executeQuery();
            
            while (rs.next()) {
                examnos.add(rs.getString("EXAMNO"));
            }
            rs.close();
        } catch (Exception ex) {
            log.error("setNameMst set error!", ex);
        } finally {
            DbUtils.closeQuietly(ps1);
            db2.commit();
        }
        return examnos;
    }
    
    private String getExamnoSql(final String hallCd)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T3.EXAMNO ");
        stb.append("FROM ");
        stb.append("    ENTEXAM_HALL_YDAT T1 ");
        stb.append("    INNER JOIN ENTEXAM_HALL_DETAIL_YDAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("        AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("        AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("        AND T2.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("        AND T2.EXAMHALLCD = T1.EXAMHALLCD ");
        stb.append("    INNER JOIN ENTEXAM_RECEPT_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("        AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("        AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("        AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("        AND T3.RECEPTNO BETWEEN T2.S_RECEPTNO AND T2.E_RECEPTNO ");
        if (_param._receptNoFrom != null && _param._receptNoTo != null) {
            stb.append("        AND T3.RECEPTNO BETWEEN '" + _param._receptNoFrom + "' AND '" + _param._receptNoTo + "' ");
        }
        stb.append("WHERE ");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("    AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("    AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("    AND T1.EXAM_TYPE = '1' ");
        stb.append("    AND T1.EXAMHALLCD = '" + hallCd + "' ");
        stb.append("ORDER BY ");
        stb.append("    T3.RECEPTNO ");
        return stb.toString();
    }
    
    private class Param {
        final String _entexamyear;        // 年度
        final String _applicantDiv;       // 入試制度
        final String _testDiv;            // 入試区分
        final String _testDivName;
        final String[] _categorySelected;
        final String _receptNoFrom;       // 開始受付番号（画面入力）
        final String _receptNoTo;         // 終了受付番号（画面入力）
        final String _label;              // ラベル選択 (1: 会場、2: 礼拝堂)
        final int _line;                  // 開始位置（行）
        final int _row;                   // 開始位置（列）
        final int _maxLine;
        final int _maxRow;
        
        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _testDivName = getTestDivName(db2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            if (1 == _categorySelected.length) {
                _receptNoFrom = value(request.getParameter("RECEPTNO_FROM"), null);
                _receptNoTo =   value(request.getParameter("RECEPTNO_TO"), null);
            } else {
                _receptNoFrom = null;
                _receptNoTo =   null;
            }
            _label = request.getParameter("LABEL");
            _line = Integer.parseInt(value(request.getParameter("LINE"), "1"));
            _row  = Integer.parseInt(value(request.getParameter("ROW"), "1"));
            _maxLine = "1".equals(_label) ? 7 : 4;
            _maxRow = "1".equals(_label) ? 3 : 2;
        }
        
        private String value(String str, String alt) {
            return null == str || "".equals(str) ? alt : str;
        }
        
        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }
    }
}

// EOF
