// kanji=漢字
/*
 * $Id: 462ec9734c377d323434bbb1bceee6a0d638fe58 $
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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ４００Ｙ＞  グループラベル
 **/
public class KNJL400Y {

    private static final Log log = LogFactory.getLog(KNJL400Y.class);

    private Param _param = null;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        
        //  print設定
        PrintWriter outstrm = new PrintWriter(response.getOutputStream());
        response.setContentType("application/pdf");
        
        //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }
        
        //  パラメータの取得
        KNJServletUtils.debugParam(request, log);
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
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }
    
    /** 帳票出力 **/
    private boolean printSvfMain(Vrw32alp svf, DB2UDB db2) {

        final String formname = "KNJL400Y.frm";
        svf.VrSetForm(formname, 1);

        final List mapList = getList(db2);
        
        boolean nonedata = false;
        try {
            int line = _param._line;   //行番号
            int row = _param._row;   //列番号
            
            for (final Iterator it = mapList.iterator(); it.hasNext();) {
                final Map map = (Map) it.next();
                final String examno = (String) map.get("EXAMNO");
                final String examhallcdRecptNo = (String) map.get("EXAMHALLCD_RECEPT_NO");
                final String examhallName = (String) map.get("EXAMHALL_NAME");
                final String name = (String) map.get("NAME");
                final String nameKana = (String) map.get("NAME_KANA");
                final String sexName = (String) map.get("SEX_NAME");
                
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
                
                final String sk = String.valueOf(row);
                
                svf.VrsOutn("GROUP_NO" + sk, line, null != examhallcdRecptNo && examhallcdRecptNo.length() > 2 ? examhallcdRecptNo.substring(examhallcdRecptNo.length() - 2) : examhallcdRecptNo); // グループ番号
                svf.VrsOutn("GROUP_NAME" + sk + (getMS932ByteLength(examhallName) > 14 ? "_2" : "_1"), line, examhallName); // グループ名称
                svf.VrsOutn("KANA" + sk + (getMS932ByteLength(nameKana) > 30 ? "_2" : "_1"), line, nameKana); // 氏名
                svf.VrsOutn("NAME" + sk + (getMS932ByteLength(name) > 30 ? "_3" : getMS932ByteLength(name) > 20 ? "_2" : "_1"), line, name); // 氏名
                svf.VrsOutn("SEX" + sk, line, sexName); // 性別

                final String printExamno = StringUtils.defaultString(examno);
                svf.VrsOutn("TESTDIV" + sk, line, printExamno); // 年度 + 受験番号
                
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
    
    private List getList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List examnos = new ArrayList();
        try {
            final String sql = getExamnoSql();
            //log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            final ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                examnos.add(resultSetToMap(meta, rs));
            }
        } catch (Exception ex) {
            log.error("setNameMst set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return examnos;
    }
    
    public static Map resultSetToMap(final ResultSetMetaData meta, final ResultSet rs) throws SQLException {
        final Map map = new HashMap();
        for (int i = 0; i < meta.getColumnCount(); i++) {
            final String columnName = meta.getColumnName(i + 1);
            final String val = rs.getString(columnName);
            map.put(columnName, val);
        }
        return map;
    }
    
    private String getExamnoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.EXAMHALLCD, ");
        stb.append("    T1.EXAMHALL_NAME, ");
        stb.append("    T3.RECEPTNO, ");
        stb.append("    T3.EXAMNO, ");
        stb.append("    BASE.NAME, ");
        stb.append("    BASE.NAME_KANA, ");
        stb.append("    NMZ002.ABBV1 AS SEX_NAME, ");
        stb.append("    ROW_NUMBER() OVER(PARTITION BY T1.EXAMHALLCD ORDER BY T3.RECEPTNO) AS EXAMHALLCD_RECEPT_NO ");
        stb.append("FROM ");
        stb.append("    ENTEXAM_HALL_YDAT T1 ");
        stb.append("    INNER JOIN ENTEXAM_HALL_LIST_YDAT HLY ON HLY.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("        AND HLY.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("        AND HLY.TESTDIV = T1.TESTDIV ");
        stb.append("        AND HLY.EXAMHALLCD = T1.EXAMHALLCD ");
        stb.append("        AND HLY.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("    INNER JOIN ENTEXAM_RECEPT_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("        AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("        AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("        AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("        AND T3.RECEPTNO = HLY.RECEPTNO ");
        stb.append("    INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("        AND BASE.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("        AND BASE.EXAMNO = T3.EXAMNO ");
        stb.append("    LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = BASE.SEX ");
        stb.append("WHERE ");
        stb.append("    T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("    AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("    AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("    AND T1.EXAMHALLCD IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("ORDER BY ");
        stb.append("    T1.EXAMHALLCD, ");
        stb.append("    T3.RECEPTNO ");
        return stb.toString();
    }
    
    private static class Param {
        final String _entexamyear;        // 年度
        final String _applicantDiv;       // 入試制度
        final String _testDiv;            // 入試区分
        final String _testDivName;
        final String[] _categorySelected;
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
            for (int i = 0; i < _categorySelected.length; i++) {
                _categorySelected[i] = StringUtils.split(_categorySelected[i], "-")[0];
            }
            _line = Integer.parseInt(value(request.getParameter("LINE"), "1"));
            _row  = Integer.parseInt(value(request.getParameter("ROW"), "1"));
            _maxLine = 5;
            _maxRow = 2;
        }
        
        private static String value(String str, String alt) {
            return null == str || "".equals(str) ? alt : str;
        }
        
        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "L004";
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
