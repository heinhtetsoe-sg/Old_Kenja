// kanji=漢字
/*
 * $Id: 916a003f20c0d7ca481190b2c3e047f7c27224fe $
 *
 * 作成日: 2004/02/11 17:57:00 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [指導情報管理]
 *
 *                  ＜ＫＮＪＨ０８０＞  個人指導記録簿
 *
 * 2004/02/11 nakamoto 名称マスタのコード変更('F001'→'H301','F002'→'H302')
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH080 extends HttpServlet {

    private static final Log log = LogFactory.getLog("KNJM701.class");

    private boolean _hasData;

    private static final String FORMNAME = "KNJH080.frm";
    private static final int MAX_LINE = 17;

    Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

                    /*
                        2   GRADE_HR_CLASS      101 
                        3   category_selected   202040 
                            category_selected   202004 
                            category_selected   201145 
                        0   YEAR                2002 
                        1   GAKKI               2 

                        [4]作成日
                    */

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }


    /*----------------------------*
     * ＳＶＦ出力                 *
     *----------------------------*/
    public void printMain(final DB2UDB db2, final Vrw32alp svf)
                     throws ServletException, IOException
    {
        String sql = new String();
        sql = "SELECT "
            + "T1.GRADE,"
            + "T1.HR_CLASS,"
            + "T5.HR_NAME,"
            + "T1.ATTENDNO,"
            + "T4.NAME_SHOW as name_show,"
            + "CHAR(TRAINDATE) AS TRAINDAY,"
            + "(SELECT NAME1 FROM NAME_MST ST1 WHERE ST1.NAMECD1 = 'H302' "     // 2004/02/11 nakamoto
            + "AND ST1.NAMECD2 = T2.HOWTOTRAINCD) AS OWTOTRAIN,"
            + "(SELECT NAME1 FROM NAME_MST ST1 WHERE ST1.NAMECD1 = 'H301' "     // 2004/02/11 nakamoto
            + "AND ST1.NAMECD2 = T2.PATIENTCD) AS PATIENT,"
            + "(SELECT STAFFNAME_SHOW FROM V_STAFF_MST T3 WHERE T3.YEAR = '" + _param._year + "' "
            + "AND T3.STAFFCD = T2.STAFFCD) AS STAFFNAME_SHOW,"
            + "T2.CONTENT "
            + "FROM "
            + "SCHREG_REGD_HDAT     T5,"
            + "SCHREG_REGD_DAT      T1,"
            + "SCHREG_TRAINHIST_DAT T2,"
            + "SCHREG_BASE_MST      T4 "
            + "WHERE "
            + "T1.SCHREGNO  IN " + _param._schregNoInStatement + " "
            + "AND T1.YEAR      = '" + _param._year + "' "
            + "AND T1.SEMESTER  = '" + _param._semester + "' "
            + "AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' "
            + "AND T2.YEAR      = T1.YEAR "
            + "AND T2.SCHREGNO  = T1.SCHREGNO "
            + "AND T4.SCHREGNO  = T1.SCHREGNO "
            + "AND T5.YEAR      = T1.YEAR "
            + "AND T5.SEMESTER  = T1.SEMESTER "
            + "AND T5.GRADE     = T1.GRADE "
            + "AND T5.HR_CLASS  = T1.HR_CLASS "
            + "ORDER BY "
            + "T1.ATTENDNO,"
            + "TRAINDATE";
        
        System.out.println("[KNJH080]set_detail sql="+sql);
        try {
            db2.query(sql);
            java.sql.ResultSet rs = db2.getResultSet();
            System.out.println("[KNJH080]set_detail sql ok!");
            
            setHead(svf);
            
            int lineCnt = 1;
            while( rs.next() ){
                if (lineCnt > MAX_LINE) {
                    svf.VrEndPage();
                    lineCnt = 1;
                    setHead(svf);
                }
                svf.VrsOutn("HR_NAME", lineCnt, rs.getString("HR_NAME"));         //組名称
                svf.VrsOutn("attendno", lineCnt, rs.getString("ATTENDNO"));        //出席番号
                svf.VrsOutn("NAME", lineCnt, rs.getString("name_show"));       //氏名
                svf.VrsOutn("FIELD1", lineCnt, KNJ_EditDate.h_format_JP(rs.getString("TRAINDAY")));  //指導日
                svf.VrsOutn("FIELD2", lineCnt, rs.getString("OWTOTRAIN"));       //指導事項
                svf.VrsOutn("FIELD3", lineCnt, rs.getString("STAFFNAME_SHOW"));  //対応者
                svf.VrsOutn("FIELD4", lineCnt, rs.getString("PATIENT"));         //相談者

                final String remark = rs.getString("CONTENT") != null ? rs.getString("CONTENT") : "";
                String a_str[] = null;
                a_str = KNJ_EditEdit.get_token(remark, 60, 5);
                if (a_str != null) {
                    int dataAriLine = 0;
                    for(int i = 0 ; i < a_str.length ; i++) {
                        dataAriLine = a_str[i] != null ? dataAriLine + 1 : dataAriLine;
                    }
                    final String setField = dataAriLine > 3 ? "_2" : "";
                    for(int i = 0 ; i < a_str.length ; i++) {
                        svf.VrsOutn("consultant" + (i + 1) + setField, lineCnt, a_str[i]);
                    }
                    a_str = null;
                }
                _hasData = true; //該当データなしフラグ
                lineCnt++;
            }
            if (lineCnt > 1) {
                svf.VrEndPage();
            }
            db2.commit();
            System.out.println("[KNJH080]set_detail read ok!");
        } catch( Exception ex ){
            System.out.println("[KNJH080]set_detail read error!");
            System.out.println( ex );
        }

    }   //set_detailの括り

    private void setHead(final Vrw32alp svf) {
        svf.VrSetForm(FORMNAME, 1);
        /** 照会結果の取得とsvf_formへ出力 **/
        //年度
        svf.VrsOut("nendo"    , nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        //作成日
        svf.VrsOut("TODAY"    , KNJ_EditDate.h_format_JP(_param._printDate));
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Student {
        private final String _schregNo;
        private final String _name;
        private final String _hrName;
        private final String _attendNo;
        private final List _printDataList;
        private final List _lastPrintDataList;
        BigDecimal _totalCreditTimeNow = new BigDecimal(0.0);
        BigDecimal _totalCreditTimeLast = new BigDecimal(0.0);

        public Student(
                final String schregNo,
                final String name,
                final int attendNo,
                final String hrName
        ) {
            _schregNo = schregNo;
            _name = name;
            _attendNo = String.valueOf(attendNo);
            _hrName = hrName;
            _printDataList = new ArrayList();
            _lastPrintDataList = new ArrayList();
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _gradeHrClass;
        private final String[] _selectDatas;
        private final String _schregNoInStatement;
        private final String _printDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _selectDatas = request.getParameterValues("category_selected");
            _schregNoInStatement = getSchregNoInState(_selectDatas);

            _printDate = getPrintDate(db2);
        }

        private String getSchregNoInState(String[] selectDatas) {
            final StringBuffer retStr = new StringBuffer();
            retStr.append("(");
            String sep = "";
            for (int i = 0; i < selectDatas.length; i++) {
                retStr.append(sep + "'" + selectDatas[i] + "'");
                sep = ",";
            }
            retStr.append(")");
            return retStr.toString();
        }

        private String getPrintDate(final DB2UDB db2) {
            String ret = "";
            KNJ_Control date = new KNJ_Control();
            KNJ_Control.ReturnVal returnval = date.Control(db2);
            ret = returnval.val3;

            return ret;
        }

    }

}   //クラスの括り

