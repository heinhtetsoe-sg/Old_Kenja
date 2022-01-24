package servletpack.KNJB;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Calendar;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ０４２＞  学級・生徒別時間割表
 *
 * 2009/10/15 nakamoto 新規作成
 **/

public class KNJB042 {

    private static final Log log = LogFactory.getLog(KNJB042.class);

    Vrw32alp svf = new Vrw32alp();
    DB2UDB   db2;
    int ret;

    Param _param;

    public void svf_out(
            HttpServletRequest request, 
            HttpServletResponse response
    ) throws ServletException, IOException {
        dumpParam(request);
        _param = createParam(request);

        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            try {
                db2.open();
            } catch( Exception ex ) {
                log.debug("DB2 open error!", ex);
            }

            _param.load(db2);

            // 印字メイン
            boolean hasData = false;   // 該当データ無しフラグ
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(svf, db2);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws ServletException, IOException {
        boolean rtnflg = false;

        ret = svf.VrSetForm("KNJB042.frm", 4);
        log.debug("印刷するフォーム:KNJB042.frm");

        String param[] = new String[7];

        set_head(param);

        for(int ia=0 ; ia<param.length ; ia++)    log.debug("param[" + ia + "]=" + param[ia]);

        for (int i = 0; i < _param._hrClassArray.length; i++) {
            final String hrClass = _param._hrClassArray[i];
            if (null == hrClass) break;
            log.fatal("クラス=" + hrClass);
            if (set_chapter1(db2, svf, param, hrClass)) rtnflg = true;
        }

        return rtnflg;
    }

    /*------------------------------------*
     * 学級・生徒別時間割表ＳＶＦ出力           *
     *------------------------------------*/
    private boolean set_chapter1(
            final DB2UDB db2,
            final Vrw32alp svf,
            String param[],
            final String hrClass
    ) throws ServletException, IOException {
        boolean rtnflg = false;

        try {
            db2.query(Pre_Stat3(hrClass));
            ResultSet rs = db2.getResultSet();
//            log.fatal("sql = "+Pre_Stat3(hrClass));
            log.fatal("sql ok!");

            int cntPeriod = _param._cntPeriod;
            /** SVFフォームへデータをセット **/
            String schno = "0";
            int totaljisu = 0; //合計時数
            int gyo = 0; //行No
            int datadiv[][][]       = new int[7][cntPeriod][40]; //テスト
            String target[][][] = new String[7][cntPeriod][40]; //職員名or施設名
            String subclass[][][]   = new String[7][cntPeriod][40]; //科目名
            String monthday[]       = {param[0],param[1],param[2],param[3],param[4],param[5],param[6]};//月日(通常)
            String monthday1[]      = {"(MON)","(TUE)","(WED)","(THU)","(FRI)","(SAT)","(SUN)"};//月日(基本)・・・枠線を表示するために必要
            String day[]            = {"月","火","水","木","金","土","日"};//曜日
            String period[]         = new String[cntPeriod]; //校時

            while( rs.next() ){
                if (!schno.equals(rs.getString("SCHREGNO"))){
                    if (!schno.equals("0")){
                        totaljisu = 0;
                        //４０名分出力
                        if (gyo == 40) {
                            int ret_brank = 0;//空列カウント
                            for (int ia = 0; ia < 7; ia++) {
                                for (int ib = 0; ib < cntPeriod; ib++) {
                                    int flg = 0;
                                    for (int ic = 0; ic < 40; ic++) {
                                        if (target[ia][ib][ic] != null) flg = 1;//パターン１
                                        ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(0,0,0),Bold=0");//ペイント・クリア
                                        if (datadiv[ia][ib][ic] == 2) 
                                            ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(2,70,2),Bold=1");//ペイント：テスト
                                        ret=svf.VrsOutn("SEL_TR_FAC",ic+1 , target[ia][ib][ic]);    //職員名or施設名
                                        ret=svf.VrsOutn("SUBCLASS"  ,ic+1 , subclass[ia][ib][ic]); //科目名
                                    }
                                    if (flg == 1) {
                                        ret=svf.VrsOut("DATE", _param._isKihon ? monthday1[ia] : monthday[ia]); //月日
                                        ret=svf.VrsOut("WEEK"           , day[ia]);                        //曜日
                                        ret=svf.VrsOut("PERIOD"         , period[ib]);                    //校時
                                        if (!_param._isKihon) 
                                            ret = svf.VrAttribute("MARK1_1",  "Paint=(2,70,1),Bold=1");//テスト
                                        ret = svf.VrEndRecord();//１列出力
                                        ret_brank++;//空列カウント
                                    }
                                }
                            }
                            //空列出力
                            for ( ; ret_brank%42 > 0; ret_brank++) ret = svf.VrEndRecord();
                            //初期化
                            gyo = 0;
                            for (int ia = 0; ia < 7; ia++) {
                                for (int ib = 0; ib < cntPeriod; ib++) {
                                    for (int ic = 0; ic < 40; ic++) {
                                        target[ia][ib][ic] = null;    //職員名or施設名
                                        subclass[ia][ib][ic] = null;    //科目名
                                        datadiv[ia][ib][ic] = 0;        //テスト
                                    }
                                }
                            }
                            for (int i = 0; i < 40; i++) {
                                ret=svf.VrsOutn("ATTEND_NO" ,i+1    ,""); //出席番号
                                ret=svf.VrsOutn("name"      ,i+1    ,""); //氏名
                                ret = svf.VrsOutn("total"   ,i+1    ,""); //合計時数
                            }
                        }
                    }
                    schno = rs.getString("SCHREGNO");
                    gyo++;
                }
                ret=svf.VrsOut("HR_NAME"    ,rs.getString("HR_NAME")); //クラス
                ret=svf.VrsOutn("ATTEND_NO" ,gyo    ,String.valueOf(rs.getInt("ATTENDNO"))); //出席番号
                ret=svf.VrsOutn("name"      ,gyo    ,rs.getString("NAME")); //氏名
                totaljisu++;
                ret = svf.VrsOutn("total"   ,gyo    ,String.valueOf(totaljisu)); //合計時数

                int iday = rs.getInt("DAYCD");
                int iper = rs.getInt("PERIODCD");
                if (iday == 1) iday = 8;
//                log.debug("schno="+schno+", iday="+iday+", iper="+iper+", gyo="+gyo);
                target[iday-2][iper][gyo-1] = rs.getString("TARGETNAME");
                subclass[iday-2][iper][gyo-1] = rs.getString("SUBCLASSABBV");
                datadiv[iday-2][iper][gyo-1] = rs.getInt("DATADIV"); //テスト
                period[iper] = rs.getString("ABBV1");
            }
            log.fatal("while ok!");
            //最後のレコード出力
            if (!schno.equals("0")){
                int ret_brank = 0;//空列カウント
                for (int ia = 0; ia < 7; ia++) {
                    for (int ib = 0; ib < cntPeriod; ib++) {
                        int flg = 0;
                        for (int ic = 0; ic < 40; ic++) {
                            if (target[ia][ib][ic] != null) flg = 1;//パターン１
                            ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(0,0,0),Bold=0");//ペイント・クリア
                            if (datadiv[ia][ib][ic] == 2) 
                                ret=svf.VrAttributen("SUBCLASS"  ,ic+1 , "Paint=(2,70,2),Bold=1");//ペイント：テスト
                            ret=svf.VrsOutn("SEL_TR_FAC",ic+1 , target[ia][ib][ic]);    //職員名or施設名
                            ret=svf.VrsOutn("SUBCLASS"  ,ic+1 , subclass[ia][ib][ic]);        //科目名
                        }
                        if (flg == 1) {
                            ret=svf.VrsOut("DATE", _param._isKihon ? monthday1[ia] : monthday[ia]); //月日
                            ret=svf.VrsOut("WEEK"           , day[ia]);                        //曜日
                            ret=svf.VrsOut("PERIOD"         , period[ib]);                    //校時
                            if (!_param._isKihon) 
                                ret = svf.VrAttribute("MARK1_1",  "Paint=(2,70,1),Bold=1");//テスト
                            ret = svf.VrEndRecord();//１列出力
                            ret_brank++;//空列カウント
                        }
                    }
                }
                //空列出力
                for ( ; ret_brank%42 > 0; ret_brank++) ret = svf.VrEndRecord();
                //初期化
                for (int i = 0; i < 40; i++) {
                    ret=svf.VrsOutn("ATTEND_NO" ,i+1    ,""); //出席番号
                    ret=svf.VrsOutn("name"      ,i+1    ,""); //氏名
                    ret = svf.VrsOutn("total"   ,i+1    ,""); //合計時数
                }
                rtnflg  = true; //該当データなしフラグ
            }
            db2.commit();
            log.fatal("read ok!");
        } catch( Exception ex ) {
            log.error("set_chapter1 read error!", ex);
        }

        return rtnflg;
    }

    private void set_head(String param[])
                     throws ServletException, IOException
    {
        try {
            ret = svf.VrsOut("NENDO"    , nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
            ret = svf.VrsOut("TODAY"    , KNJ_EditDate.h_format_JP(_param._ctrlDate));
            if (_param._isKihon) {
                ret = svf.VrsOut("TITLE"    , "(" + _param._titleKihon + ")");
            } else {
                ret = svf.VrsOut("term"     , "(" + KNJ_EditDate.h_format_JP(_param._sDate) + " \uFF5E " + KNJ_EditDate.h_format_JP(_param._eDate) + ")");
                ret = svf.VrsOut("MARK1_1",  "  " );
                ret = svf.VrsOut("MARK1_2",  "　：テスト時間割" );
                //週の日付
                int nen  = Integer.parseInt(_param._sDateWeek.substring(0,4));
                int tuki = Integer.parseInt(_param._sDateWeek.substring(5,7));
                int hi   = Integer.parseInt(_param._sDateWeek.substring(8));
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                for (int ia=0 ; ia<7 ; ia++) {
                    param[ia] = "(" + tuki + "/" + hi + ")";
                    if (ia != 6) {
                        cals.add(Calendar.DATE,+1);
                        nen  = cals.get(Calendar.YEAR);
                        tuki = cals.get(Calendar.MONTH);
                        tuki++;
                        hi   = cals.get(Calendar.DATE);
                    }
                }
            }
            log.debug("set_head read ok!");
        } catch( Exception ex ) {
            log.debug("set_head read error!", ex);
        }

    }

    /*----------------*
     * 月の前ゼロ挿入 *
     *----------------*/
    private String h_tuki(int intx)
                     throws ServletException, IOException
    {
        String strx = null;
        try {
            strx = "00" + String.valueOf(intx);
            strx = strx.substring(strx.length()-2);
        } catch( Exception ex ) {
            log.debug("h_tuki error!", ex);
        }
        return strx;
    }

    /**学級・生徒別時間割表**/
    private String Pre_Stat3(final String hrClass)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+_param._year+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");
            //在籍
            stb.append(",SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR='"+_param._year+"' AND ");
            stb.append("           SEMESTER='"+_param._semester+"' AND ");
                stb.append("       GRADE||HR_CLASS = '"+hrClass+"' ) ");
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (_param._isKihon) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ");
                stb.append("       ,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD,'0' AS DATADIV ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
                stb.append("       LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
                stb.append("                                         W5.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                         W5.BSCSEQ = W1.BSCSEQ AND ");
                stb.append("                                         W5.DAYCD = W1.DAYCD AND ");
                stb.append("                                         W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                         W5.CHAIRCD = W1.CHAIRCD) ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");
                stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD,W1.DATADIV ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
                stb.append("       LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                    W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                    W5.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_FAC_DAT W2 ON (W2.YEAR = W1.YEAR AND ");
            stb.append("                                          W2.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W2.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                          W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            if (_param._isKihon) {   //基本
                stb.append("WHERE  W1.YEAR='"+_param._year+"' AND  ");
                stb.append("       W1.SEMESTER='"+_param._semester+"' AND  ");
                stb.append("       W1.BSCSEQ = "+_param._seq+" AND  ");
            } else {                                //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+_param._sDate+"' AND '"+_param._eDate+"' AND  ");
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            //講座生徒
            stb.append(",CHAIR_STD AS ( ");
            stb.append("    SELECT W1.CHAIRCD, W1.SCHREGNO, W1.APPDATE, W1.APPENDDATE ");
            stb.append("    FROM   CHAIR_STD_DAT W1,SCHNO W2 ");
            stb.append("    WHERE  W1.YEAR=W2.YEAR AND  ");
            stb.append("           W1.SEMESTER=W2.SEMESTER AND  ");
            stb.append("           W1.SCHREGNO=W2.SCHREGNO ) ");
            stb.append(",MAX_STD AS ( ");
            stb.append("    SELECT CHAIRCD, SCHREGNO, MAX(APPDATE) AS APPDATE ");
            stb.append("    FROM   CHAIR_STD ");
            stb.append("    GROUP BY CHAIRCD, SCHREGNO ) ");

            //メイン
            stb.append("SELECT DISTINCT T1.DAYCD ");
            stb.append("       ,f_period(T1.PERIODCD) as PERIODCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,T1.CHAIRCD ");
            stb.append("       ,T2.SCHREGNO ");
            stb.append("       ,L1.GRADE ");
            stb.append("       ,L1.HR_CLASS ");
            stb.append("       ,L1.ATTENDNO ");
            stb.append("       ,L2.NAME ");
            stb.append("       ,L3.HR_NAME ");
            if (_param._isStaff) {
                stb.append("       ,T1.STAFFCD ");
                stb.append("       ,VALUE(L4.STAFFNAME_SHOW,'') AS TARGETNAME ");
            } else {
                stb.append("       ,T1.FACCD ");
                stb.append("       ,VALUE(L5.FACILITYABBV,'') AS TARGETNAME ");
            }
            if (_param._isSubclass) {
                stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSABBV ");
            } else {
                stb.append("       ,VALUE(T1.CHAIRABBV,'') AS SUBCLASSABBV ");
            }
            stb.append("       ,L7.ABBV1 ");
            stb.append("       ,CASE WHEN T1.DATADIV = '2' THEN 2 ELSE 0 END AS DATADIV "); //データ区分 2:テスト
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ");
            stb.append("       LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ");
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            if (_param._isKihon)    //基本
                stb.append("   ,MAX_STD T2 ");
            if (!_param._isKihon)    //通常
                stb.append("   ,CHAIR_STD T2 ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN SCHNO L1 ON L1.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR=L1.YEAR AND  ");
            stb.append("                                        L3.SEMESTER=L1.SEMESTER AND  ");
            stb.append("                                        L3.GRADE=L1.GRADE AND  ");
            stb.append("                                        L3.HR_CLASS=L1.HR_CLASS ");
            stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
            if (!_param._isKihon)    //通常
                stb.append("   AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append("ORDER BY L1.GRADE,L1.HR_CLASS,L1.ATTENDNO,T1.DAYCD,2, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
            } else {
                stb.append("        T1.SUBCLASSCD, ");
            }
            stb.append("        T1.CHAIRCD ");
            if (_param._isStaff) {
                stb.append("       ,T1.STAFFCD ");
            } else {
                stb.append("       ,T1.FACCD ");
            }
        } catch( Exception e ){
            log.debug("Pre_Stat3 error!", e);
        }
        return stb.toString();
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private Param createParam(final HttpServletRequest request) {
        //時間割種別・指定日付 1:基本時間割 2:通常時間割
        final String syubetu = request.getParameter("JIKANWARI_SYUBETU");
        //基本時間割
        final String tYear = request.getParameter("T_YEAR");
        final String tSeq  = request.getParameter("T_BSCSEQ");
        final String tSeme = request.getParameter("T_SEMESTER");
        //通常時間割
        final String date = request.getParameter("SDATE");
        //学年
//        final String grade = request.getParameter("GRADE");
        //出力対象クラス
        final String[] hrClassArray = request.getParameterValues("CATEGORY_SELECTED");
        //出力項目(上段) 1:科目名 2:講座名
        final String printJodanDiv = request.getParameter("SUBCLASS_CHAIR_DIV");
        //出力項目(下段) 1:職員名 2:施設名
        final String printGedanDiv = request.getParameter("SYUTURYOKU_KOUMOKU");
        //その他
        final String ctrlYear = request.getParameter("CTRL_YEAR");
        final String ctrlSeme = request.getParameter("CTRL_SEMESTER");
        final String ctrlDate = request.getParameter("CTRL_DATE");
        final String useCurriculumcd = request.getParameter("useCurriculumcd");

        return new Param(
                syubetu,
                tYear,
                tSeq,
                tSeme,
                date,
                hrClassArray,
                printJodanDiv,
                printGedanDiv,
                ctrlYear,
                ctrlSeme,
                ctrlDate,
                useCurriculumcd
                );
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * パラメータクラス
     */
    private class Param {
        //時間割種別・指定日付 1:基本時間割 2:通常時間割
        final private boolean _isKihon;
        //基本時間割
        private String _seq;
        private String _semester;
        //通常時間割
        private String _date;
        //学年
        //出力対象クラス
        private String[] _hrClassArray;
        //出力項目(上段) 1:科目名 2:講座名
        final private boolean _isSubclass;
        //出力項目(下段) 1:職員名 2:施設名
        final private boolean _isStaff;
        //その他
        private String _ctrlYear;
        private String _ctrlSeme;
        private String _ctrlDate;
        //年度・週開始日・週終了日
        private String _year;
        private String _sDate;
        private String _eDate;
        private String _sDateWeek;
        private String _eDateWeek;
        //基本・タイトル
        private String _titleKihon;
        //校時のカウント
        private int _cntPeriod;
        private final String _useCurriculumcd;

//        private String _semester;
//        private String _semesterName;
//        private String _testCd;
//        private String _testName;

        Param(
                final String syubetu,
                final String tYear,
                final String tSeq,
                final String tSeme,
                final String date,
                final String[] hrClassArray,
                final String printJodanDiv,
                final String printGedanDiv,
                final String ctrlYear,
                final String ctrlSeme,
                final String ctrlDate,
                final String useCurriculumcd
        ) {
            _isKihon = "1".equals(syubetu);
            if (_isKihon) {
                _year = tYear;
                _seq = tSeq;
                _semester = tSeme;
            } else {
                _date = date.substring(0,4) + "-" + date.substring(5,7) + "-" + date.substring(8);
                setYear();
            }
            _hrClassArray = hrClassArray;
            _isSubclass = "1".equals(printJodanDiv);
            _isStaff = "1".equals(printGedanDiv);
            _ctrlYear = ctrlYear;
            _ctrlSeme = ctrlSeme;
            _ctrlDate = ctrlDate.substring(0,4) + "-" + ctrlDate.substring(5,7) + "-" + ctrlDate.substring(8);
            _useCurriculumcd = useCurriculumcd;
        }

        private void setYear() {
            try {
                //年度
                int nen  = Integer.parseInt(_date.substring(0,4));
                int tuki = Integer.parseInt(_date.substring(5,7));
                int hi   = Integer.parseInt(_date.substring(8));
                _year = (tuki <= 3) ? String.valueOf(nen - 1) : String.valueOf(nen);
                //週開始日を取得
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                while(cals.get(Calendar.DAY_OF_WEEK) != 2){
                    cals.add(Calendar.DATE,-1);
                }
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                _sDate = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
                _sDateWeek = _sDate;
                //週終了日の取得
                cals.add(Calendar.DATE,+6);
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                _eDate = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
                _eDateWeek = _eDate;
            } catch (final Exception ex) {
                log.error("年度・週開始日・週終了日の取得でエラー:", ex);
            } finally {
            }
            log.debug("年度:" + _year);
            log.debug("週開始日:" + _sDateWeek);
            log.debug("週終了日:" + _eDateWeek);
        }

        private void load(final DB2UDB db2) {
            if (_isKihon) {
                loadTitleKihon(db2);
            } else {
                loadSemesterTuujou(db2);
            }
            loadCountPeriod(db2);
        }

        private void loadTitleKihon(final DB2UDB db2) {
            _titleKihon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT TITLE "
                             + "FROM SCH_PTRN_HDAT "
                             + "WHERE YEAR = '" + _year + "' AND BSCSEQ = " + _seq + " AND SEMESTER = '" + _semester + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _titleKihon = rs.getString("TITLE");
                }
            } catch (final Exception ex) {
                log.error("基本・タイトルのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("基本・タイトル:" + _titleKihon);
        }

        private void loadSemesterTuujou(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT SEMESTER "
                             + "      ,case when '"+_sDate+"' < SDATE then SDATE else null end as SDATE "
                             + "      ,case when '"+_eDate+"' > EDATE then EDATE else null end as EDATE "
                             + "FROM   SEMESTER_MST "
                             + "WHERE  SDATE <= date('"+_date+"') AND EDATE >= date('"+_date+"') AND YEAR = '"+_year+"' AND SEMESTER <> '9'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semester = rs.getString("SEMESTER");
                    if (rs.getString("SDATE") != null) _sDate = rs.getString("SDATE"); //学期開始日
                    if (rs.getString("EDATE") != null) _eDate = rs.getString("EDATE"); //学期終了日
                }
            } catch (final Exception ex) {
                log.error("通常・学期のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("通常・学期:" + _semester);
            log.debug("開始日:" + _sDate);
            log.debug("終了日:" + _eDate);
        }

        private void loadCountPeriod(final DB2UDB db2) {
            _cntPeriod = 36;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT f_period(NAMECD2) as PERIODCD "
                             + "FROM   NAME_MST "
                             + "WHERE  NAMECD1='B001' "
                             + "ORDER BY 1 ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _cntPeriod = rs.getInt("PERIODCD") + 1;
                }
            } catch (final Exception ex) {
                log.error("校時コードのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("校時のMAX値:" + _cntPeriod);
        }
    }


}  //クラスの括り
