package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.KNJ_Semester;
import servletpack.KNJZ.detail.KNJ_Staff;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [学籍管理]
 *                                                               　                *
 *                  ＜ＫＮＪＡ２１０＞ クラス名簿印刷（同時展開クラス）            *
 *                                                                   　            *
 * 2003/11/12 nakamoto 和暦変換に対応
 * 2004/08/11 nakamoto 画面パラメータ（適用開始日付）を追加
 * $Id: 75e592e6409acc35c7dd2bd0f2dfa4054a1706cd $
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJA210 extends HttpServlet {

    private static final Log log = LogFactory.getLog("KNJE380.class");
    Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB    db2;      // Databaseクラスを継承したクラス
    String dbname = new String();
    boolean nonedata = false; //該当データなしフラグ
    String _z010;

    Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {

        dbname   = request.getParameter("DBNAME");                          // データベース名
        System.out.println("[KNJA210]dbname= " + dbname);


    // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    // svf設定
        svf.VrInit();                        //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

    // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);

        try {
            db2.open();
            System.out.println("[KNJA210]DB2 opened ok");
        } catch( Exception ex ) {
            System.out.println("[KNJA210]DB2 open error!");
            System.out.println(ex);
        }

        _param = createParam(db2, request);

        _z010 = setZ010Name1(db2);

    /*-----------------------------------------------------------------------------
        ＳＶＦ作成処理
      -----------------------------------------------------------------------------*/

    // ＳＶＦフォーム出力
        /*クラス名簿印刷*/
        nonedata = false ; //該当データなしフラグ
        final String formName;
        if ("kumamoto".equals(_z010)) {
            formName = "1".equals(_param._formSentaku) ? "KNJA210_1_2.frm" : "KNJA210_2_2.frm";
        } else {
            formName = "1".equals(_param._formSentaku) ? "KNJA210.frm" : "KNJA210_2.frm";
        }
        svf.VrSetForm(formName, 4);
        set_head();
        set_meisai();

        /*該当データ無し*/
        System.out.println("[KNJA210]nonedata="+nonedata);
        if(nonedata == false){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndRecord();
            svf.VrEndPage();
        }

        svf.VrPrint();

    // 終了処理
        db2.close();        // DBを閉じる
        svf.VrQuit();
        outstrm.close();    // ストリームを閉じる

    }    //doGetの括り

    /**
     * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
     */
    private String setZ010Name1(DB2UDB db2) {
        String name1 = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
            rs = ps.executeQuery();
            if (rs.next()) {
                name1 = rs.getString("NAME1");
            }
        } catch (SQLException ex) {
            log.debug("getZ010 exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return name1;
    }

    /*------------------------------------*
     * ４．明細ＳＶＦ出力　　　           *
     *------------------------------------*/
    public void set_meisai()
                     throws ServletException, IOException
    {
        try {
            String sql = new String();
            sql = "SELECT DISTINCT "
                    + "T8.CHAIRNAME,"
                    + "T1.SCHREGNO,"
                    + "T1.NAME_SHOW,"
                    + "T1.NAME_KANA,"
                    + "T6.HR_NAMEABBV,"
                    + "T2.GRADE,"
                    + "T2.HR_CLASS,"
                    + "T2.ATTENDNO, "
                    + "SCHOOL.FINSCHOOL_NAME "
                + "FROM "
                    + "CHAIR_STD_DAT    T7 "
                    + "INNER JOIN CHAIR_DAT T8 ON T8.YEAR = T7.YEAR "
                      + "AND T8.SEMESTER = T7.SEMESTER "
                      + "AND T8.CHAIRCD = T7.CHAIRCD, "
                    + "SCHREG_BASE_MST  T1 "
                    + "LEFT JOIN FINSCHOOL_MST SCHOOL ON T1.FINSCHOOLCD  = SCHOOL.FINSCHOOLCD, "
                    + "SCHREG_REGD_DAT  T2,"
                    + "SCHREG_REGD_HDAT T6 "
                + "WHERE "
                        + "T7.YEAR      = '" +  _param._year + "' "
                    + "AND T7.SEMESTER  = '" +  _param._semester + "' "
                    + "AND T7.CHAIRCD   = '" +  _param._chairCd + "' "
                    + "AND T7.APPDATE   = '" +  _param._appDate + "' "
                    + "AND T1.SCHREGNO  = T7.SCHREGNO "
                    + "AND T2.SCHREGNO  = T7.SCHREGNO "
                    + "AND T2.YEAR      = T7.YEAR "
                    + "AND T2.SEMESTER  = T7.SEMESTER "
                    + "AND T6.YEAR      = T2.YEAR "
                    + "AND T6.SEMESTER  = T2.SEMESTER "
                    + "AND T6.GRADE     = T2.GRADE "
                    + "AND T6.HR_CLASS  = T2.HR_CLASS "
                + "ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ";

            System.out.println("[KNJA210]set_meisai sql="+sql);
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            System.out.println("[KNJA210]set_meisai sql ok!");

            /** SVFフォームへデータをセット **/
            String strdir1 = new String();   //写真データ存在チェック用
            String strdir2 = new String();   //写真データ存在チェック用
            int i;
            final int maxGyo = "1".equals(_param._formSentaku) ? 5 : 6;
            final int maxLine = "1".equals(_param._formSentaku) ? 6 : 8;
            for (i = 1; rs.next(); i++) {
                if (!StringUtils.isBlank(rs.getString("CHAIRNAME"))) {
                    svf.VrsOut("CHAIR_NAME" , "（" + rs.getString("CHAIRNAME") + "）");
                }

                final String schregNo = rs.getString("SCHREGNO");
                strdir1 = "P" + schregNo + "." + _param._fileKakuTyouSi;  //本番用
                strdir2 = _param._documentroot + "/" + _param._fileFolder + "/" + strdir1;   //本番用
                File f1 = new File(strdir2);   //写真データ存在チェック用

                final String printSchregNo = null != _param._printSchregNo ? "(" + schregNo + ")" : "";

                final String finschool = (rs.getString("FINSCHOOL_NAME") != null) ? rs.getString("FINSCHOOL_NAME") : "";
                final String kana = StringUtils.defaultString(rs.getString("NAME_KANA"), "");
                final String printInfo = null != _param._printSchoolName ? ((null != _param._printInfo && "2".equals(_param._printInfo)) ? kana : finschool) : "";

                final String setFieldVal = rs.getString("HR_NAMEABBV") + "-" + String.valueOf(rs.getInt("ATTENDNO"));
                final String setNameShow = rs.getString("NAME_SHOW");
                setPrintData(1, 1, strdir1, strdir2, i, maxGyo, f1, printSchregNo, setFieldVal, setNameShow, printInfo);
                setPrintData(2, 2, strdir1, strdir2, i, maxGyo, f1, printSchregNo, setFieldVal, setNameShow, printInfo);
                setPrintData(3, 3, strdir1, strdir2, i, maxGyo, f1, printSchregNo, setFieldVal, setNameShow, printInfo);
                setPrintData(4, 4, strdir1, strdir2, i, maxGyo, f1, printSchregNo, setFieldVal, setNameShow, printInfo);
                if ("1".equals(_param._formSentaku)) {
                    setPrintData(0, 5, strdir1, strdir2, i, maxGyo, f1, printSchregNo, setFieldVal, setNameShow, printInfo);
                } else {
                    setPrintData(5, 5, strdir1, strdir2, i, maxGyo, f1, printSchregNo, setFieldVal, setNameShow, printInfo);
                    setPrintData(0, 6, strdir1, strdir2, i, maxGyo, f1, printSchregNo, setFieldVal, setNameShow, printInfo);
                }
                if(i % maxGyo == 0){
                    svf.VrEndRecord();
                    for (int ic = 1; ic < maxLine; ic++) {
                        svf.VrsOut("field1_" + ic            , "");
                        svf.VrsOut("field2_" + ic            , "");
                        svf.VrsOut("NAME_SHOW_" + ic         , "");
                        svf.VrsOut("Bitmap_Field" + ic         , "");
                        svf.VrsOut("NO_DATA" + ic              , "");
                        svf.VrsOut("ATTENDNO" + ic             , "");
                    }
                }
                nonedata = true ; //該当データなしフラグ
            }
            System.out.println("[KNJA210]set_meisai i="+i);
            i = i - 1;
            if((i > 0) && (i % maxGyo != 0)){
                svf.VrEndRecord();
            }
            db2.commit();
            System.out.println("[KNJA210]set_meisai read ok!");
        } catch( Exception ex ) {
            System.out.println("[KNJA210]set_meisai read error!");
            System.out.println(ex);
        }
        System.out.println("[KNJA210]set_meisai path!");

    }  //set_meisaiの括り

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    private void setPrintData(
            final int amari,
            final int fieldSoeji,
            final String strdir1,
            final String strdir2,
            final int i,
            final int maxLine,
            final File file,
            final String printSchregNo,
            final String setFieldVal,
            final String setNameShow,
            final String printSchoolName
    ) {
        if(i % maxLine == amari){
            if (null != _param._printSchoolName) {
                final String finSchoolField = getMS932ByteLength(printSchoolName) > 20 ? "_2" : "";
                svf.VrsOut("FINSCHOOL" + fieldSoeji + finSchoolField, printSchoolName);
            }
            svf.VrsOut("field1_" + fieldSoeji, setFieldVal);
            svf.VrsOut("field2_" + fieldSoeji, printSchregNo);
            svf.VrsOut("NAME_SHOW_" + fieldSoeji, setNameShow);
            if (file.exists()) {
                svf.VrsOut("Bitmap_Field" + fieldSoeji, strdir2);
                svf.VrsOut("NO_DATA" + fieldSoeji, "");
                svf.VrsOut("ATTENDNO" + fieldSoeji, "");
            } else {
                svf.VrsOut("Bitmap_Field" + fieldSoeji, "");
                svf.VrsOut("NO_DATA" + fieldSoeji, "イメージデータなし");
                svf.VrsOut("ATTENDNO" + fieldSoeji, strdir1);
            }
        }
    }


    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    public void set_head()
                     throws ServletException, IOException
    {
        try {
            svf.VrsOut("TODAY"          , KNJ_EditDate.h_format_JP(db2, _param._date));
            svf.VrsOut("GRADE_HR_CLASS" , _param._hrName);
            svf.VrsOut("STAFFNAME"      , _param._staffName);
            svf.VrsOut("NENDO"  , KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
            svf.VrsOut("SEMESTER"       , _param._semesterName);
            System.out.println("[KNJA210]set_head read ok!");
        } catch( Exception ex ) {
            System.out.println("[KNJA210]set_head read error!");
            System.out.println(ex);
        }

    }  //set_headの括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69984 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    public class Param {
        final String _year;
        final String _semester;
        final String _semesterName;
        final String _date;
        final String _printDate;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _documentroot;
        final String _staffCd;
        final String _staffName;
        final String _chairCd;
        final String _groupCd;
        final String _appDate;
        final String _fileFolder;
        final String _fileKakuTyouSi;
        final String _formSentaku;
        final String _printSchregNo;
        final String _printSchoolName;
        final String _printInfo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE");
            KNJ_EditDate edit_date = new KNJ_EditDate();
            _printDate = edit_date.h_format_sec(_date);
            _documentroot   = request.getParameter("DOCUMENTROOT");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _staffCd = request.getParameter("NAME_SHOW");
            _chairCd  = request.getParameter("ATTENDCLASSCD");
            _groupCd = request.getParameter("GROUPCD");
            _appDate = request.getParameter("APPDATE");
            _formSentaku = request.getParameter("FORM_SENTAKU");
            _printSchregNo = request.getParameter("PRINT_SCHREGNO");
            _printSchoolName = request.getParameter("SCHOOLNAME");                           //学校名
            _printInfo = request.getParameter("PRINT_INFO");                           //学校名

            KNJ_Semester semester = new KNJ_Semester();
            KNJ_Semester.ReturnVal returnval = semester.Semester(db2, _year, _semester);
            _semesterName = returnval.val1;

            KNJ_Grade_Hrclass hrclass = new KNJ_Grade_Hrclass();
            KNJ_Grade_Hrclass.ReturnVal hrReturnval = hrclass.hrclass_name(db2, _year, _semester, _grade, _hrClass);
            if (_groupCd.equalsIgnoreCase("0000")) {
                _hrName = hrReturnval.val1;
            } else {
                _hrName = hrReturnval.val1 + "*";
            }

            KNJ_Staff staff = new KNJ_Staff();
            KNJ_Staff.ReturnVal staffReturnval = staff.Staff_name_show(db2, _staffCd);
            _staffName = staffReturnval.val1;

            KNJ_Control imagepath_extension = new KNJ_Control();
            KNJ_Control.ReturnVal controlReturnval = imagepath_extension.Control(db2);
            _fileFolder = controlReturnval.val4;
            _fileKakuTyouSi = controlReturnval.val5;
        }
    }

}  //クラスの括り

