package servletpack.KNJB;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ０６０＞  時間割表(職員/学級/生徒)
 *
 * 2005/04/04 nakamoto 東京都版として修正（１６校時対応）
 * 2005/05/06 nakamoto バグ修正---NO001
 * 2005/06/27 nakamoto 名称マスタの「B001」の予備2は、Null以外は時間割表で出力しない校時との意味で追加
 * 2005/07/05 nakamoto 授業が無い「校時」を詰める、詰めないの対応（職員別のみ）
 * 2005/08/22 nakamoto 表示の不具合を修正。職員の変更（ダンスの先生）に対応。
 * 2006/01/20 nakamoto NO002 職員別　職員の変更（ダンスの先生）に対応。
 * 2006/02/20 nakamoto NO004 職員別の表示の不具合を修正
 * 2006/04/21 nakamoto NO005 特別職員が設定されている場合、特別職員のみを表示するよう修正
 * 2006/07/07 nakamoto NO006 ＳＥＱが2桁以上の場合、帳票が出力されない不具合を修正。
 *                     ------パラメータを正常に取得できていなかった。
 *                     ------パラメータを分けて取得するようにした。
 * 2006/10/18 nakamoto NO007 「学期」を取得するための日付を「週開始日付」から指示画面の「指定日付」に変更した。
 *                     ------一週間の間に前期後期両方ある場合の対応である
 * 2006/10/24 nakamoto NO008 通常時間割：タイトル下の日付範囲は、学期範囲内を表示するよう修正した。
 * 2006/10/25 nakamoto NO009 通常時間割：「年度」を取得するための日付を「週開始日付」から指示画面の「指定日付」に変更した。
 * 2006/11/01 nakamoto NO010 通常時間割：テスト時間割の表記を変更した。「施設名 → テスト種別名」
 *                     NO011 通常時間割：テスト時間割のみ出力の対応。==> 仮修正です。
 * 2006/11/07 nakamoto NO012 通常時間割：NO011をさらに修正。指定日から１週間を出力。列は「日付順」に表示。
 * 2007/08/14 nakamoto NO013 時間割表の各コマに、必履修記号を表示（例、◎科目名）
 * 2007/08/20 nakamoto NO014 フォームフィールド変更に伴い修正---科目名（メイン）の桁数を14桁→16桁に変更した
 **/

public class KNJB060 {

    private static final Log log = LogFactory.getLog(KNJB060.class);

    Vrw32alp svf = new Vrw32alp();
    DB2UDB    db2;
    String dbname = new String();
    int ret;
    boolean nonedata  = false;
    int daycd_keep[] = new int[6]; //NO012 曜日コード保管 日曜日以外
    boolean _isKindai = false;
    String _useTestCountflg;
    String _notShowStaffcd;
    String _title2;
    String _tYear2;
    String _tBscseq2;
    String _tSemester2;
    String _printCheck = null;  //A週,B週出力・・・基本時間割の指定対応(AB,AB,・・・と出力)
    String _printCd = null;     //各出力区分CD(職員、学級、生徒、施設)
    String _tYear;
    String _tBscseq;
    String _tSemester;
    String _printCheck2 = null; //2週間出力・・・通常時間割の2週間単位の指定対応
    String _fromDate;
    String _toDate;
    String _fromDate2;
    String _toDate2;
    String _courseName;
    String _course;             //出力区分「学級」に「課程学科コースコンボ」追加
    String _noRequireFlgCheck;  //出力条件に「科目名は必履修区分を出力しない」チェックボックス追加
    String _subclassAbbvCheck;  //出力条件に「科目名または講座名を略称名で出力する」チェックボックス追加
    String _staffAbbvCheck;     //出力条件に「教員名を略称名(職員氏名表示用)で出力する」チェックボックス追加
    String _noStaffCheck;
    String _schoolName;
    String _heijitsuCheck;
    String _useTestFacility;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException, SQLException
    {
        log.fatal("$Revision: 72490 $"); // CVSキーワードの取り扱いに注意

        String param[] = new String[22]; // NO007 16→17

    // パラメータの取得
        try {
            dbname   = request.getParameter("DBNAME");          // データベース名
            param[0] = request.getParameter("RADIO");                    // 1:基本 2:通常
            param[15] = request.getParameter("CHECK");                    //授業が無い「校時」を詰める、詰めないのチェックボックス :on
            param[10] = request.getParameter("CTRL_DATE");		//作成日
            _useTestFacility = request.getParameter("useTestFacility");
            if(param[0].equals("1")){
                param[1] = request.getParameter("T_YEAR");        //年度 NO006
                param[2] = request.getParameter("T_BSCSEQ");    //ＳＥＱ NO006
                param[6] = request.getParameter("T_SEMESTER");    //学期 NO006
                //基本時間割2
                _title2 = request.getParameter("TITLE2");
                _tYear2 = request.getParameter("T_YEAR2");
                _tBscseq2 = request.getParameter("T_BSCSEQ2");
                _tSemester2 = request.getParameter("T_SEMESTER2");
                _printCheck = request.getParameter("PRINT_CHECK");
                _tYear = request.getParameter("T_YEAR");
                _tBscseq = request.getParameter("T_BSCSEQ");
                _tSemester = request.getParameter("T_SEMESTER");
            } else{
                param[8] = request.getParameter("TEST_CHECK"); //NO011Add on:テスト時間割のみ出力, null:通常出力
                String strx = request.getParameter("DATE");                //指定日付
                param[16] = strx.substring(0,4) + "-" + strx.substring(5,7) + "-" + strx.substring(8); // NO007
                //週始めの日付を取得
                int nen  = Integer.parseInt(strx.substring(0,4));
                int tuki = Integer.parseInt(strx.substring(5,7));
                int hi   = Integer.parseInt(strx.substring(8));
                param[1] = (tuki <= 3) ? Integer.toString(nen - 1) : Integer.toString(nen); // 年度の取得 NO009
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                //NO012Modify 通常は、処理する。テストのみ出力の場合、処理しない。
                if (param[8] == null) {
                    while(cals.get(Calendar.DAY_OF_WEEK) != 2){
                        cals.add(Calendar.DATE,-1);
                    }
                }
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                param[2] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
                _fromDate = param[2];
                //週最終日の取得---通常、土曜日。
                //NO012Modify テストのみ出力の場合、開始日が'月'以外なら６日後。開始日が'月'なら５日後。
                if (cals.get(Calendar.DAY_OF_WEEK) != 2) {
                    cals.add(Calendar.DATE,+6);
                } else {
                    cals.add(Calendar.DATE,+5);
                }
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                param[7] = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
                _toDate = param[7];
                _printCheck2 = request.getParameter("PRINT_CHECK2");
                if (_printCheck2 != null) {
                    _fromDate2 = getDate7(_fromDate);
                    _toDate2 = getDate7(_toDate);
                }
            }
            param[5] = request.getParameter("KUBUN");
            if(param[5].equals("1")){
                param[3] = request.getParameter("SECTION_CD_NAME1");        //所属from
                param[4] = request.getParameter("SECTION_CD_NAME2");           //所属to
            }
            if(param[5].equals("2")){
                param[3] = request.getParameter("GRADE_HR_CLASS1");         //クラスfrom
                param[4] = request.getParameter("GRADE_HR_CLASS2");           //クラスto
            }
            if(param[5].equals("3")){
                param[3] = request.getParameter("GRADE_HR_CLASS3");         //クラスfrom
                param[4] = request.getParameter("GRADE_HR_CLASS4");           //クラスto
            }
            if(param[5].equals("4")){
                param[3] = request.getParameter("FACCD_NAME1");             //施設from
                param[4] = request.getParameter("FACCD_NAME2");             //施設to
            }
            //出力項目 1:科目名 2:講座名
            param[17] = request.getParameter("SUBCLASS_CHAIR_DIV");
            //(生徒別のみ) on:職員は正担任(MAX職員番号)のみ出力, null:通常出力
            param[18] = request.getParameter("STAFF_CHECK");
            //(職員別のみ) on:クラス名は出力しない, null:通常出力
            param[19] = request.getParameter("NO_CLASS_CHECK");
            param[20] = request.getParameter("useCurriculumcd");
            param[21] = request.getParameter("useProficiency"); // 実力テスト使用 1:使用 それ以外:未使用
            _useTestCountflg = request.getParameter("useTestCountflg");
            _notShowStaffcd  = request.getParameter("notShowStaffcd");
            _course = request.getParameter("COURSE");
            _noRequireFlgCheck = request.getParameter("NO_REQUIRE_FLG_CHECK");
            _subclassAbbvCheck = request.getParameter("SUBCLASS_ABBV_CHECK");
            _staffAbbvCheck = request.getParameter("STAFF_ABBV_CHECK");
            _noStaffCheck = request.getParameter("NO_STAFF_CHECK");
            _schoolName = request.getParameter("SCHOOL_NAME");
            _heijitsuCheck = request.getParameter("HEIJITSU_CHECK");
        } catch( Exception ex ) {
            log.error("parameter error! ",ex);
        }

        for(int ia=0 ; ia<param.length ; ia++) log.debug("param[" + ia + "]=" + param[ia]);

    // ＤＢ接続
        db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
        }

        //課程学科コース名を取得
        if (!"".equals(_course) && null != _course) {
            _courseName = "";
            try {
                String sql = " SELECT COURSENAME FROM COURSE_MST WHERE COURSECD = '" + _course.substring(0,1) + "' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    _courseName += rs.getString(1);
                }
                rs.close();
                ps.close();
            } catch( Exception e ){
                log.error("COURSE_MST error!",e);
            }
            try {
                String sql = " SELECT MAJORNAME FROM MAJOR_MST WHERE COURSECD = '" + _course.substring(0,1) + "' AND MAJORCD = '" + _course.substring(1,4) + "' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    _courseName += rs.getString(1);
                }
                rs.close();
                ps.close();
            } catch( Exception e ){
                log.error("MAJOR_MST error!",e);
            }
            try {
                String sql = " SELECT COURSECODENAME FROM COURSECODE_MST WHERE COURSECODE = '" + _course.substring(4) + "' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    _courseName += rs.getString(1);
                }
                rs.close();
                ps.close();
            } catch( Exception e ){
                log.error("COURSECODE_MST error!",e);
            }
        }

    // ＤＢ検索（基本時間割Ｈ）
        /* 期間の取得 */
        if(param[0].equals("1")){
            try {
                String sql = "SELECT TITLE "
                           + "FROM SCH_PTRN_HDAT "
                           + "WHERE YEAR = '" + param[1] + "' AND BSCSEQ = " + param[2] + " AND SEMESTER = '" + param[6] + "'";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if( rs.next() ){
                    param[14] = rs.getString(1);
                }
                rs.close();
                ps.close();
            } catch( Exception e ){
                log.error("SCH_PTRN_HDAT error!",e);
            }
        }

        /* テストのみ出力の場合 NO012 */
        if (param[8] != null) {
            /* テスト名の取得 NO012 */
            try {
                String dataDivIN = ("1".equals(param[21])) ? "('2','3')" : "('2')";
                String sql = "SELECT DISTINCT "
                           + "       L2.SEMESTERNAME, "
                           + "       T1.SEMESTER "
                           + "FROM   SCH_CHR_TEST T1 "
                           + "       INNER JOIN SCH_CHR_DAT T2 ON T2.EXECUTEDATE = T1.EXECUTEDATE "
                           + "                                AND T2.PERIODCD = T1.PERIODCD "
                           + "                                AND T2.CHAIRCD = T1.CHAIRCD "
                           + "                                AND T2.DATADIV IN "+dataDivIN+" "
                           + "       LEFT JOIN SEMESTER_MST L2 ON L2.YEAR=T1.YEAR AND L2.SEMESTER=T1.SEMESTER "
                           + "WHERE  T1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[7]+"' ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if( rs.next() ){
                    param[14] = rs.getString(1);
                }
                rs.close();
                ps.close();
            } catch( Exception e ){
                log.error("TEST TITLE error!",e);
            }

            /* 曜日コード保管 NO012 */
            try {
                int nen  = Integer.parseInt(param[2].substring(0,4));
                int tuki = Integer.parseInt(param[2].substring(5,7));
                int hi   = Integer.parseInt(param[2].substring(8));
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);
                int date_no = 1;
                for(int ia=0 ; ia<7 ; ia++){
                    if(ia != 0){
                        cals.add(Calendar.DATE,+1);
                        nen  = cals.get(Calendar.YEAR);
                        tuki = cals.get(Calendar.MONTH);
                        tuki++;
                        hi   = cals.get(Calendar.DATE);
                    }
                    int week_no = cals.get(Calendar.DAY_OF_WEEK);
                    if (week_no == 1) {
                        date_no = 0;
                        continue;//日曜日は処理しない。
                    }
                    daycd_keep[week_no-2] = ia+date_no;
                }
            } catch( Exception e ){
                log.error("daycd_keep error!",e);
            }
            for(int ia=0 ; ia<daycd_keep.length ; ia++) log.debug("daycd_keep[" + ia + "]=" + daycd_keep[ia]);
        }

        KNJDefineSchool defineSchool = new KNJDefineSchool();
        defineSchool.setSchoolCode(db2, param[0]);
        _isKindai = "KIN".equals(defineSchool.schoolmark) || "KINJUNIOR".equals(defineSchool.schoolmark);

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        ret = svf.VrInit();                           //クラスの初期化
        ret = svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

        // ＳＶＦフォーム出力

        //A週,B週出力・・・基本時間割の指定対応(AB,AB,・・・と出力)
        //2週間出力・・・通常時間割の2週間単位の指定対応
        if (_printCheck != null && !"".equals(_title2) && _title2 != null || _printCheck2 != null) {
            //A週,B週出力チェックあり OR 2週間出力チェックあり
            final List printCdList = getPrintCdList(db2, param);
            for (final Iterator it = printCdList.iterator(); it.hasNext();) {
                _printCd = (String) it.next();

                //Ａ週
                if (param[0].equals("1")) setParamK(param, "A");
                if (param[0].equals("2")) setParamT(param, "A");
                if (param[0].equals("2")) get_semester(param);

                //時間割出力
                printMain(param);

                //Ｂ週
                if (!"".equals(_title2) && _title2 != null || _printCheck2 != null) {

                    if (param[0].equals("1")) setParamK(param, "B");
                    if (param[0].equals("2")) setParamT(param, "B");
                    if (param[0].equals("2")) get_semester(param);

                    //時間割出力
                    printMain(param);
                }
            }//for
        } else {
            //A週,B週出力チェックなし

            if (param[0].equals("2")) get_semester(param);

            //時間割出力
            printMain(param);

            //Ｂ週
            if (!"".equals(_title2) && _title2 != null) {

                if (param[0].equals("1")) setParamK(param, "B");

                //時間割出力
                printMain(param);
            }
        }

        /*該当データ無し*/
        if(nonedata == false){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndRecord();
            ret = svf.VrEndPage();
        }

        ret = svf.VrPrint();

    // 終了処理
        db2.close();        // DBを閉じる
        ret = svf.VrQuit();
        outstrm.close();    // ストリームを閉じる

    }    //doGetの括り

    private List getPrintCdList(final DB2UDB db2, String param[]) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getPrintCdSql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String printCd = rs.getString("PRINT_CD");
                rtnList.add(printCd);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getPrintCdSql(String param[]) {
        final StringBuffer stb = new StringBuffer();
        //職員別
        if (param[5].equals("1")) {
            stb.append("SELECT W2.STAFFCD AS PRINT_CD ");
            stb.append("FROM   V_SECTION_MST W1,V_STAFF_MST W2 ");
            stb.append("WHERE  W1.YEAR='"+param[1]+"' AND ");
            stb.append("       W1.SECTIONCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND ");
            stb.append("       W2.YEAR=W1.YEAR AND ");
            stb.append("       W2.SECTIONCD=W1.SECTIONCD ");
            stb.append("ORDER BY PRINT_CD ");
        }
        //学級別
        if (param[5].equals("2")) {
            stb.append("SELECT GRADE||HR_CLASS AS PRINT_CD ");
            stb.append("FROM   SCHREG_REGD_HDAT ");
            stb.append("WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("       SEMESTER='"+param[6]+"' AND ");
            stb.append("       GRADE||HR_CLASS BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");
            stb.append("ORDER BY PRINT_CD ");
        }
        //生徒別
        if (param[5].equals("3")) {
            stb.append("SELECT SCHREGNO AS PRINT_CD ");
            stb.append("FROM   SCHREG_REGD_DAT ");
            stb.append("WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("       SEMESTER='"+param[6]+"' AND ");
            stb.append("       GRADE||HR_CLASS BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");
            stb.append("ORDER BY GRADE, HR_CLASS, ATTENDNO ");
        }
        //施設別
        if (param[5].equals("4")) {
            stb.append("SELECT W1.FACCD AS PRINT_CD ");
            stb.append("FROM   V_FACILITY_MST W1 ");
            stb.append("WHERE  W1.YEAR='"+param[1]+"' AND ");
            stb.append("       W1.FACCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");
            stb.append("ORDER BY PRINT_CD ");
        }
        return stb.toString();
    }


    //時間割出力
    public void printMain(String param[])
                     throws ServletException, IOException
    {
        try {
            if(param[5].equals("1")){
                /*職員別時間割表*/
                final String form = ("on".equals(_heijitsuCheck)) ? "KNJB060_2.frm" : _isKindai ? "KNJB060K.frm" : "KNJB060.frm";
                ret = svf.VrSetForm(form, 4);
                setTeacher2(param);
            }
            if(param[5].equals("2")){
                /*学級別時間割表*/
                final String form = ("on".equals(_heijitsuCheck)) ? "KNJB060_2.frm" : _isKindai ? "KNJB060K.frm" : "KNJB060.frm";
                ret = svf.VrSetForm(form, 4);
                setClassStudent(param);
            }
            if(param[5].equals("3")){
                /*生徒別時間割表*/
                final String form = ("on".equals(_heijitsuCheck)) ? "KNJB060_2.frm" : _isKindai ? "KNJB060K.frm" : "KNJB060.frm";
                ret = svf.VrSetForm(form, 4);
                setClassStudent(param);
            }
            if(param[5].equals("4")){
                /*施設別時間割表*/
                final String form = ("on".equals(_heijitsuCheck)) ? "KNJB060_2.frm" : _isKindai ? "KNJB060K.frm" : "KNJB060.frm";
                ret = svf.VrSetForm(form, 4);
                setFacility2(param);
            }
        } catch( Exception e ){
            log.error("printMain error!",e);
        }
    }  //printMainの括り


    /**
      * 職員別時間割表 NO004
      */
    public void setTeacher2(String param[])
                     throws ServletException, IOException
    {
        try {
                /** 配列定義(名称マスタの校時)---2005.07.05 **/
            PreparedStatement psn = db2.prepareStatement(psNameMstCommon(param));
            ResultSet rsn = psn.executeQuery();
            String period_abbv1[] = new String[36];//NO003
            int arr_max_period = 9;
            int arr_min_period = 2;
            int arr_cnt = 0;
            while( rsn.next() ){
                if (arr_cnt == 0) arr_min_period = rsn.getInt("PERIODCD");
                arr_max_period = rsn.getInt("PERIODCD");
                period_abbv1[arr_max_period] = rsn.getString("ABBV1");

                arr_max_period++;
                arr_cnt++;
            }
            rsn.close();
log.debug("arr_max_period = "+arr_max_period);
log.debug("arr_min_period = "+arr_min_period);

                /** 配列定義 **/
            PreparedStatement psc = db2.prepareStatement(psCommon(param));
            ResultSet rsc = psc.executeQuery();
            int arr_max_cnt = 90;
            while( rsc.next() ){
                arr_max_cnt = rsc.getInt("MAX_CNT");
            }
            rsc.close();
log.debug("arr_max_cnt = "+arr_max_cnt);
            /** 科目数をカウント **/
            PreparedStatement ps = db2.prepareStatement(psTeacher1(param));
            ResultSet rs2 = ps.executeQuery();
            //初期値
            String staff = "0";        //クラスＣＤ
            String day = "0";
            String period = "";
            int gyo_max = 0;
            int gyo_tmp = 0;
            int gyo_max_all = 0;
            int grcl_cnt = 0;
            int grcl_suu[] = new int[arr_max_cnt];
            for (int i=0; i<arr_max_cnt; i++) grcl_suu[i] = 99;
            String kaipagefield2 = "";
            int period_suu[] = new int[arr_max_period];
            int grcl_period_suu[][] = new int[arr_max_cnt][arr_max_period];
            for (int i=arr_min_period; i<arr_max_period; i++) period_suu[i] = 0;
            while( rs2.next() ){
                kaipagefield2 = rs2.getString("STAFFCD");
                //クラスＣＤまたは校時または曜日のブレイク時
                if (!staff.equals(kaipagefield2) && !staff.equals("0") ||
                    !period.equals(rs2.getString("PERIODCD")) && !period.equals("") ||
                    !day.equals(rs2.getString("DAYCD")) && !day.equals("0")) {
                    if (gyo_tmp > gyo_max) {
                        gyo_max = gyo_tmp;
                    }
                    gyo_tmp = 0;
                    //クラスＣＤまたは校時のブレイク時
                    if (!staff.equals(kaipagefield2) && !staff.equals("0") ||
                        !period.equals(rs2.getString("PERIODCD")) && !period.equals("")) {
                        gyo_max_all = gyo_max_all + gyo_max;
                        gyo_max = 0;
                        grcl_period_suu[grcl_cnt][Integer.parseInt(period)] = 1;
                        //クラスＣＤのブレイク時
                        if(!staff.equals(kaipagefield2) && !staff.equals("0")){
                            grcl_suu[grcl_cnt] = gyo_max_all;
                            gyo_max_all = 0;
                            grcl_cnt++;
                        }
                    }
                }
                day = rs2.getString("DAYCD");
                period = rs2.getString("PERIODCD");
                staff = kaipagefield2;
                gyo_tmp++;
            }
            //最後のレコード出力
            if( !staff.equals("0") ){
                if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                gyo_max_all = gyo_max_all + gyo_max;
                grcl_suu[grcl_cnt] = gyo_max_all;
                grcl_period_suu[grcl_cnt][Integer.parseInt(period)] = 1;//2005.07.05
            }
            rs2.close();
            log.debug("setTeacher2 read2 ok!");

            /** SVFフォームへデータをセット **/
            ResultSet rs = ps.executeQuery();

            staff = "0";        //クラスＣＤ
            day = "0";
            period = "";
            gyo_max = 0;
            gyo_tmp = 0;
            int MAX_GYO = 80;
            String day_sub_stf[][] = new String[6][MAX_GYO]; // TODO:
            for (int i=0; i<6; i++)
                for (int j=0; j<MAX_GYO; j++)
                    day_sub_stf[i][j] = "";
            int grcl_cnt2 = 0;
            boolean output_flg  = false; //同時展開出力フラグ
            String group = "0";
            String period_name = "";
            String kaipagefield = "";
            while( rs.next() ){
                kaipagefield = rs.getString("STAFFCD");
                int ia = rs.getInt("DAYCD");    //曜日コード
                int ib = rs.getInt("PERIODCD");    //校時コード
                //クラスＣＤまたは校時または曜日のブレイク時
                if (!staff.equals(kaipagefield) && !staff.equals("0") ||
                    !period.equals(String.valueOf(ib)) && !period.equals("") ||
                    !day.equals(String.valueOf(ia)) && !day.equals("0")) {
                    if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                    gyo_tmp = 0;
                    //クラスＣＤまたは校時のブレイク時
                    if (!staff.equals(kaipagefield) && !staff.equals("0") ||
                        !period.equals(String.valueOf(ib)) && !period.equals("")) {
                        //ブランク出力メソッド
                        if (param[15] == null)
                            setBrank(1,arr_min_period,arr_max_period,grcl_cnt2,period_suu,grcl_period_suu,period,period_abbv1);
                        setTimeTable(gyo_max,day_sub_stf,period,period_name,param);        //出力メソッド//---2005.04.05
                        gyo_max = 0;
                        //クラスＣＤのブレイク時
                        if(!staff.equals(kaipagefield) && !staff.equals("0")){
                            //ブランク出力メソッド
                            if (param[15] == null) {
                                setBrank(2,arr_min_period,arr_max_period,grcl_cnt2,period_suu,grcl_period_suu,period,period_abbv1);
                            }
                            for (int i=arr_min_period; i<arr_max_period; i++) {
                                period_suu[i] = 0;
                            }
                            grcl_cnt2++;
                        }
                    }
                }
                //ページオーバーフラグ
                output_flg = (grcl_suu[grcl_cnt2] > 15) ? true : false ;
                if( output_flg ){
                    if(!rs.getString("GROUPCD").equals("0000")){
                        if( group.equals(rs.getString("GROUPCD")) && gyo_tmp > 0 ) gyo_tmp--;
                        day_sub_stf[ia-2][gyo_tmp*3] = rs.getString("GROUPNAME");
                        day_sub_stf[ia-2][gyo_tmp*3+1] = "（"+rs.getString("GROUPCD")+"）";
                    } else{
                        day_sub_stf[ia-2][gyo_tmp*3] = ("on".equals(param[19])) ? "" : rs.getString("TARGETCLASS");
                        day_sub_stf[ia-2][gyo_tmp*3+1] = rs.getString("SUBCLASSNAME");
                        day_sub_stf[ia-2][gyo_tmp*3+2] = rs.getString("FACILITYNAME");
                    }
                } else{
                    day_sub_stf[ia-2][gyo_tmp*3] = ("on".equals(param[19])) ? "" : rs.getString("TARGETCLASS");
                    day_sub_stf[ia-2][gyo_tmp*3+1] = rs.getString("SUBCLASSNAME");
                    day_sub_stf[ia-2][gyo_tmp*3+2] = rs.getString("FACILITYNAME");
                }
                //保管用フィールドをセット
                day = String.valueOf(ia);
                period = String.valueOf(ib);
                period_name = rs.getString("ABBV1");//---2005.04.05
                staff = kaipagefield;
                group = rs.getString("GROUPCD");
                gyo_tmp++;
                //ページヘッダー項目セット
                String staffnote = "　　　職員（　" + ("1".equals(_notShowStaffcd) ? "" :  staff+"　") + rs.getString("STAFFNAME")+"　）";
                String schnote = "所属（　"+rs.getString("SECTIONCD")+"　"+rs.getString("SECTIONNAME")+"　）";
                ret=svf.VrsOut("MASK"  , staff );   //改行
                ret=svf.VrsOut("NOTE"  , schnote + staffnote );
                if (!"1".equals(param[0]) && param[8] != null) {
                    ret = svf.VrsOut("TITLE"     , "(" + param[14] + '　' + rs.getString("FACILITYNAME") + ")");
                }
                setHeader(param);
            }
            //最後のレコード出力
            if(staff.equals("0") == false){
                if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                //ブランク出力メソッド
                if (param[15] == null) {
                    setBrank(1,arr_min_period,arr_max_period,grcl_cnt2,period_suu,grcl_period_suu,period,period_abbv1);
                }
                setTimeTable(gyo_max,day_sub_stf,period,period_name,param);
                //ブランク出力メソッド
                if (param[15] == null) {
                    setBrank(2,arr_min_period,arr_max_period,grcl_cnt2,period_suu,grcl_period_suu,period,period_abbv1);
                }
                nonedata  = true; //該当データなしフラグ
                ret = svf.VrEndPage();
            }
            rs.close();
            ps.close();
            db2.commit();
            log.debug("setTeacher2 read ok!");
        } catch( Exception ex ) {
            log.error("setTeacher2 read error!",ex);
        }

    }  //setTeacher2の括り


    /**
      * 学級別・生徒別時間割表
      */
    public void setClassStudent(String param[])
                     throws ServletException, IOException
    {
        try {

            /** 配列定義 **/
            PreparedStatement psc = db2.prepareStatement(psCommon(param));
            ResultSet rsc = psc.executeQuery();
            int arr_max_cnt = 90;
            while( rsc.next() ){
                arr_max_cnt = rsc.getInt("MAX_CNT");
            }
            rsc.close();
log.debug("arr_max_cnt = "+arr_max_cnt);
                /** 科目数をカウント **/
            PreparedStatement ps = null;
            if (param[5].equals("2")) ps = db2.prepareStatement(psClass3(param, 1));
            if (param[5].equals("3")) ps = db2.prepareStatement(psStudent5(param, 1));
            ResultSet rs2 = ps.executeQuery();
            //初期値
            String staff = "0";
            String day = "0";
            String period = "";
            int gyo_max = 0;
            int gyo_tmp = 0;
            int gyo_max_all = 0;
            int grcl_cnt = 0;
            int grcl_suu[] = new int[arr_max_cnt];
            for (int i=0; i<arr_max_cnt; i++) grcl_suu[i] = 99;
            String kaipagefield2 = "";//2005.08.23
            int cellRecMax = 15; //１つのセルに対するレコード数の最大値。
            while( rs2.next() ){
                if (param[5].equals("2")) kaipagefield2 = rs2.getString("GRADE") + rs2.getString("HR_CLASS");
                if (param[5].equals("3")) kaipagefield2 = rs2.getString("SCHREGNO");
                //クラスＣＤまたは校時または曜日のブレイク時
                if (!staff.equals(kaipagefield2) && !staff.equals("0") ||
                    !period.equals(rs2.getString("PERIODCD")) && !period.equals("") ||
                    !day.equals(rs2.getString("DAYCD")) && !day.equals("0")) {
                    if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                    gyo_tmp = 0;
                    if (cellRecMax < gyo_max) cellRecMax = gyo_max;
                    //クラスＣＤまたは校時のブレイク時
                    if (!staff.equals(kaipagefield2) && !staff.equals("0") ||
                        !period.equals(rs2.getString("PERIODCD")) && !period.equals("")) {
                        gyo_max_all = gyo_max_all + gyo_max;
                        gyo_max = 0;
                        //クラスＣＤのブレイク時
                        if(!staff.equals(kaipagefield2) && !staff.equals("0")){
                            grcl_suu[grcl_cnt] = gyo_max_all;
                            gyo_max_all = 0;
                            grcl_cnt++;
                        }
                    }
                }
                day = rs2.getString("DAYCD");
                period = rs2.getString("PERIODCD");
                staff = kaipagefield2;
                gyo_tmp++;
            }
            //最後のレコード出力
            if( !staff.equals("0") ){
                if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                gyo_max_all = gyo_max_all + gyo_max;
                grcl_suu[grcl_cnt] = gyo_max_all;
                if (cellRecMax < gyo_max) cellRecMax = gyo_max;
            }
            rs2.close();
            log.debug("setClassStudent read2 ok!");


            /** SVFフォームへデータをセット **/
            ResultSet rs = ps.executeQuery();

            staff = "0";        //クラスＣＤ
            day = "0";
            period = "";
            gyo_max = 0;
            gyo_tmp = 0;
            int cellGyoMax = cellRecMax * 3; //１つのセルに対し、データを表示する行数の最大値。
            log.debug("cellRecMax = "+cellRecMax);
            log.debug("cellGyoMax = "+cellGyoMax);
            String day_sub_stf[][] = new String[6][cellGyoMax];
            for (int i=0; i<6; i++)
                for (int j=0; j<cellGyoMax; j++)
                    day_sub_stf[i][j] = "";
            int grcl_cnt2 = 0;
            boolean output_flg  = false; //同時展開出力フラグ
            String group = "0";
            String period_name = "";
            String kaipagefield = "";
            String schnote = "";
            while( rs.next() ){
                if (param[5].equals("2")) kaipagefield = rs.getString("GRADE") + rs.getString("HR_CLASS");
                if (param[5].equals("3")) kaipagefield = rs.getString("SCHREGNO");
                int ia = rs.getInt("DAYCD");    //曜日コード
                int ib = rs.getInt("PERIODCD");    //校時コード
                //クラスＣＤまたは校時または曜日のブレイク時
                if (!staff.equals(kaipagefield) && !staff.equals("0") ||
                    !period.equals(String.valueOf(ib)) && !period.equals("") ||
                    !day.equals(String.valueOf(ia)) && !day.equals("0")) {
                    if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                    gyo_tmp = 0;
                    //クラスＣＤまたは校時のブレイク時
                    if (!staff.equals(kaipagefield) && !staff.equals("0") ||
                        !period.equals(String.valueOf(ib)) && !period.equals("")) {
                        setTimeTable(gyo_max,day_sub_stf,period,period_name,param);        //出力メソッド//---2005.04.05
                        gyo_max = 0;
                        //クラスＣＤのブレイク時
                        if(!staff.equals(kaipagefield) && !staff.equals("0")){
                               if( output_flg ){
                                   setGroupList(param,staff);        //同時展開担当者一覧出力メソッド
                                   final String form = ("on".equals(_heijitsuCheck)) ? "KNJB060_2.frm" : _isKindai ? "KNJB060K.frm" : "KNJB060.frm";
                                   ret = svf.VrSetForm(form, 4);
                               }
                               grcl_cnt2++;
                        }
                    }
                }
                //ページオーバーフラグ
                final int gyo = (!"on".equals(_noStaffCheck)) ? 3 : 2;
                output_flg = (grcl_suu[grcl_cnt2] > 15) ? true : false ;
                if( output_flg ){
                    if(!rs.getString("GROUPCD").equals("0000")){
                        if( group.equals(rs.getString("GROUPCD")) && gyo_tmp > 0 ) gyo_tmp--;
                        day_sub_stf[ia-2][gyo_tmp*gyo] = rs.getString("GROUPNAME");
                        day_sub_stf[ia-2][gyo_tmp*gyo+1] = "（"+rs.getString("GROUPCD")+"）";
                    } else{
                        day_sub_stf[ia-2][gyo_tmp*gyo] = rs.getString("SUBCLASSNAME");
                        if (!"on".equals(_noStaffCheck)) {
                            day_sub_stf[ia-2][gyo_tmp*gyo+1] = rs.getString("STAFFNAME");
                            day_sub_stf[ia-2][gyo_tmp*gyo+2] = rs.getString("FACILITYNAME");
                        } else {
                            day_sub_stf[ia-2][gyo_tmp*gyo+1] = rs.getString("FACILITYNAME");
                        }

                    }
                } else{
                    day_sub_stf[ia-2][gyo_tmp*gyo] = rs.getString("SUBCLASSNAME");
                    if (!"on".equals(_noStaffCheck)) {
                        day_sub_stf[ia-2][gyo_tmp*gyo+1] = rs.getString("STAFFNAME");
                        day_sub_stf[ia-2][gyo_tmp*gyo+2] = rs.getString("FACILITYNAME");
                    } else {
                        day_sub_stf[ia-2][gyo_tmp*gyo+1] = rs.getString("FACILITYNAME");
                    }

                }
                //保管用フィールドをセット
                day = String.valueOf(ia);
                period = String.valueOf(ib);
                period_name = rs.getString("ABBV1");
                staff = kaipagefield;
                group = rs.getString("GROUPCD");
                gyo_tmp++;
                //ページヘッダー項目セット
                ret=svf.VrsOut("MASK"  , staff );   //改行
                if (param[5].equals("3")) {
                    schnote = rs.getString("TARGETCLASS") + "　" + String.valueOf(rs.getInt("ATTENDNO")) + "番　" + staff + "　" + rs.getString("NAME");
                } else {
                    schnote = "学年／学級（　"+rs.getString("TARGETCLASS")+"　）";
                    if (!"".equals(_course) && null != _course) {
                        schnote = schnote + "　" + _courseName;
                    }
                }
                ret=svf.VrsOut("NOTE"  , schnote );
                if (!"1".equals(param[0]) && param[8] != null) {
                    ret = svf.VrsOut("TITLE"     , "(" + param[14] + '　' + rs.getString("FACILITYNAME") + ")");
                }
                setHeader(param);
            }
            //最後のレコード出力
            if(staff.equals("0") == false){
                if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                setTimeTable(gyo_max,day_sub_stf,period,period_name,param);        //出力メソッド
                if( output_flg ) setGroupList(param,staff);        //同時展開担当者一覧出力メソッド
                nonedata  = true; //該当データなしフラグ
                ret = svf.VrEndPage();
            }
            rs.close();
            ps.close();
            db2.commit();
            log.debug("setClassStudent read ok!");
        } catch( Exception ex ) {
            log.error("setClassStudent read error!",ex);
        }

    }  //setClassStudentの括り


    /**
      * 施設別時間割表
      */
    public void setFacility2(String param[])
                     throws ServletException, IOException
    {
        try {
                /** 配列定義(名称マスタの校時)---2005.07.05 **/
            PreparedStatement psn = db2.prepareStatement(psNameMstCommon(param));
            ResultSet rsn = psn.executeQuery();
            String period_abbv1[] = new String[36];//NO003
            int arr_max_period = 9;
            int arr_min_period = 2;
            int arr_cnt = 0;
            while( rsn.next() ){
                if (arr_cnt == 0) arr_min_period = rsn.getInt("PERIODCD");
                arr_max_period = rsn.getInt("PERIODCD");
                period_abbv1[arr_max_period] = rsn.getString("ABBV1");

                arr_max_period++;
                arr_cnt++;
            }
            rsn.close();
log.debug("arr_max_period = "+arr_max_period);
log.debug("arr_min_period = "+arr_min_period);

                /** 配列定義 **/
            PreparedStatement psc = db2.prepareStatement(psCommon(param));
            ResultSet rsc = psc.executeQuery();
            int arr_max_cnt = 90;
            while( rsc.next() ){
                arr_max_cnt = rsc.getInt("MAX_CNT");
            }
            rsc.close();
log.debug("arr_max_cnt = "+arr_max_cnt);
                /** 科目数をカウント **/
            PreparedStatement ps = db2.prepareStatement(psFacility1(param));
            ResultSet rs2 = ps.executeQuery();
            //初期値
            String staff = "0";        //クラスＣＤ
            String day = "0";
            String period = "";
            int gyo_max = 0;
            int gyo_tmp = 0;
            int gyo_max_all = 0;
            int grcl_cnt = 0;
            int grcl_suu[] = new int[arr_max_cnt];
            for (int i=0; i<arr_max_cnt; i++) grcl_suu[i] = 99;
            String kaipagefield2 = "";
            int period_suu[] = new int[arr_max_period];
            int grcl_period_suu[][] = new int[arr_max_cnt][arr_max_period];
            for (int i=arr_min_period; i<arr_max_period; i++) period_suu[i] = 0;
            while( rs2.next() ){
                kaipagefield2 = rs2.getString("FACCD");
                //クラスＣＤまたは校時または曜日のブレイク時
                if (!staff.equals(kaipagefield2) && !staff.equals("0") ||
                    !period.equals(rs2.getString("PERIODCD")) && !period.equals("") ||
                    !day.equals(rs2.getString("DAYCD")) && !day.equals("0")) {
                    if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                    gyo_tmp = 0;
                    //クラスＣＤまたは校時のブレイク時
                    if (!staff.equals(kaipagefield2) && !staff.equals("0") ||
                        !period.equals(rs2.getString("PERIODCD")) && !period.equals("")) {
                        gyo_max_all = gyo_max_all + gyo_max;
                        gyo_max = 0;
                        grcl_period_suu[grcl_cnt][Integer.parseInt(period)] = 1;//2005.07.05
                        //クラスＣＤのブレイク時
                        if(!staff.equals(kaipagefield2) && !staff.equals("0")){
                            grcl_suu[grcl_cnt] = gyo_max_all;
                            gyo_max_all = 0;
                            grcl_cnt++;
                        }
                    }
                }
                day = rs2.getString("DAYCD");
                period = rs2.getString("PERIODCD");
                staff = kaipagefield2;
                gyo_tmp++;
            }
            //最後のレコード出力
            if( !staff.equals("0") ){
                if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                gyo_max_all = gyo_max_all + gyo_max;
                grcl_suu[grcl_cnt] = gyo_max_all;
                grcl_period_suu[grcl_cnt][Integer.parseInt(period)] = 1;
            }
            rs2.close();
            log.debug("setFacility2 read2 ok!");

            /** SVFフォームへデータをセット **/
            ResultSet rs = ps.executeQuery();

            staff = "0";        //クラスＣＤ
            day = "0";
            period = "";
            gyo_max = 0;
            gyo_tmp = 0;
            int MAX_GYO = 80;
            String day_sub_stf[][] = new String[6][MAX_GYO];
            for (int i=0; i<6; i++)
                for (int j=0; j<MAX_GYO; j++)
                    day_sub_stf[i][j] = "";
            int grcl_cnt2 = 0;
            boolean output_flg  = false; //同時展開出力フラグ
            String group = "0";
            String period_name = "";
            String kaipagefield = "";
            while( rs.next() ){
                kaipagefield = rs.getString("FACCD");
                int ia = rs.getInt("DAYCD");    //曜日コード
                int ib = rs.getInt("PERIODCD");    //校時コード
                //クラスＣＤまたは校時または曜日のブレイク時
                if (!staff.equals(kaipagefield) && !staff.equals("0") ||
                    !period.equals(String.valueOf(ib)) && !period.equals("") ||
                    !day.equals(String.valueOf(ia)) && !day.equals("0")) {
                    if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                    gyo_tmp = 0;
                    //クラスＣＤまたは校時のブレイク時
                    if (!staff.equals(kaipagefield) && !staff.equals("0") ||
                        !period.equals(String.valueOf(ib)) && !period.equals("")) {
                        setTimeTable(gyo_max,day_sub_stf,period,period_name,param);        //出力メソッド
                        gyo_max = 0;
                        //クラスＣＤのブレイク時
                        if(!staff.equals(kaipagefield) && !staff.equals("0")){
                            for (int i=arr_min_period; i<arr_max_period; i++) {
                                period_suu[i] = 0;
                            }
                            grcl_cnt2++;
                        }
                    }
                }
                //ページオーバーフラグ
                final int gyo = (!"on".equals(_noStaffCheck)) ? 3 : 2;
                output_flg = (grcl_suu[grcl_cnt2] > 15) ? true : false ;
                if( output_flg ){
                    if(!rs.getString("GROUPCD").equals("0000")){
                        if( group.equals(rs.getString("GROUPCD")) && gyo_tmp > 0 ) gyo_tmp--;
                        day_sub_stf[ia-2][gyo_tmp*gyo] = rs.getString("GROUPNAME");
                        day_sub_stf[ia-2][gyo_tmp*gyo+1] = "（"+rs.getString("GROUPCD")+"）";
                    } else{
                        day_sub_stf[ia-2][gyo_tmp*gyo] = rs.getString("TARGETCLASS");
                        day_sub_stf[ia-2][gyo_tmp*gyo+1] = rs.getString("SUBCLASSNAME");
                        if (!"on".equals(_noStaffCheck)) {
                            day_sub_stf[ia-2][gyo_tmp*gyo+2] = rs.getString("STAFFNAME");
                        }
                    }
                } else{
                    day_sub_stf[ia-2][gyo_tmp*gyo] = rs.getString("TARGETCLASS");
                    day_sub_stf[ia-2][gyo_tmp*gyo+1] = rs.getString("SUBCLASSNAME");
                    if (!"on".equals(_noStaffCheck)) {
                        day_sub_stf[ia-2][gyo_tmp*gyo+2] = rs.getString("STAFFNAME");
                    }
                }
                //保管用フィールドをセット
                day = String.valueOf(ia);
                period = String.valueOf(ib);
                period_name = rs.getString("ABBV1");
                staff = kaipagefield;
                group = rs.getString("GROUPCD");
                gyo_tmp++;
                //ページヘッダー項目セット
                String schnote = "施設（　"+rs.getString("FACCD")+"　"+rs.getString("FACILITYNAME")+"　）";
                ret=svf.VrsOut("MASK"  , staff );   //改行
                ret=svf.VrsOut("NOTE"  , schnote );
                if (!"1".equals(param[0]) && param[8] != null) {
                    ret = svf.VrsOut("TITLE"     , "(" + param[14] + '　' + rs.getString("STAFFNAME") + ")");
                }
                setHeader(param);
            }
            //最後のレコード出力
            if(staff.equals("0") == false){
                if( gyo_tmp > gyo_max )    gyo_max = gyo_tmp;
                setTimeTable(gyo_max,day_sub_stf,period,period_name,param);        //出力メソッド//---2005.04.05
                nonedata  = true; //該当データなしフラグ
                ret = svf.VrEndPage();
            }
            rs.close();
            ps.close();
            db2.commit();
            log.debug("setFacility2 read ok!");
        } catch( Exception ex ) {
            log.error("setFacility2 read error!",ex);
        }

    }  //setFacility2の括り


    /*------------------------------------*
     * 同時展開担当者一覧出力メソッド     *
     *------------------------------------*/
    public void setGroupList(String param[],String gr_cl)
                     throws ServletException, IOException
    {
        /* 明細出力 */
        try {
            PreparedStatement ps = null;
            if (param[5].equals("2")) ps = db2.prepareStatement(psClass3(param, 2));//学級別
            if (param[5].equals("3")) ps = db2.prepareStatement(psStudent5(param, 2));//生徒別
            ps.setString(1, gr_cl);        //年組
            ResultSet rs = ps.executeQuery();

            final String form = _isKindai ? "KNJB060K_4.frm" : "KNJB060_4.frm";
            ret = svf.VrSetForm(form, 4);
            //ヘッダ出力
            String Title = "";
            if(param[0].equals("1"))        //基本
                Title = "(" + param[14] + ")";
            if(param[0].equals("2")) {       //通常
                Title = "(" + KNJ_EditDate.h_format_JP(param[2]) + "　\uFF5E　" + KNJ_EditDate.h_format_JP(param[7]) + ")";
            }
            ret=svf.VrsOut("TITLE"  , Title);
            ret=svf.VrsOut("NENDO"  , nao_package.KenjaProperties.gengou(Integer.parseInt(param[1])) + "年度");
            ret=svf.VrsOut("TODAY"  , KNJ_EditDate.h_format_JP(param[10]));
            //変数
            String Daycd = "0";                //曜日コード
            String Day_out = "";            //曜日（月・火・・土）
            String Periodcd = "0";            //校時コード-1
            String Groupcd = "0";            //群コード
            String SubStfName = "";            //科目・担当名
            String SubStfName_tmp = "";        //科目・担当名（バイト数チェック用）
            String seq = "";                //カンマ区切り
            boolean data_flg = false;        //データ有無
            int record_cnt = 0;                //改ページ用
            //マスク用変数１（変数値が同じ場合、横線を取り除く）
            int week_msk = 0;
            int pri_msk = 0;
            int grp_msk = 0;
            int sub_msk = 0;
            //マスク用変数２（変数値１と２が同じ場合、データ（曜日・校時・群）表示をしない）
            int week_msk_tmp = 1;
            int pri_msk_tmp = 1;
            int grp_msk_tmp = 1;
            while( rs.next() ){
                //改ページ行数
                if( record_cnt > 49 ) record_cnt = 0;
                //曜日・校時
                int ia = rs.getInt("DAYCD");
                int ib = rs.getInt("PERIODCD");
                //曜日・校時・群のブレイク時
                if( (!Daycd.equals(String.valueOf(ia)) && !Daycd.equals("0")) ||
                    (!Periodcd.equals(String.valueOf(ib)) && !Periodcd.equals("0")) ||
                    (!Groupcd.equals(rs.getString("GROUPCD")) && !Groupcd.equals("0")))
                {
                    //１行出力前処理メソッド
                    set_maeshori(week_msk_tmp,week_msk,pri_msk_tmp,pri_msk,grp_msk_tmp,grp_msk,record_cnt);
                    ret = svf.VrEndRecord();        //１行出力
                    //１行出力後処理
                    record_cnt++;
                    SubStfName_tmp = "";
                    SubStfName = "";
                    seq = "";
                    week_msk_tmp = week_msk;
                    pri_msk_tmp = pri_msk;
                    grp_msk_tmp = grp_msk;
                    if( !Daycd.equals(String.valueOf(ia)) && !Daycd.equals("0") ) week_msk++;
                    if( (!Daycd.equals(String.valueOf(ia)) && !Daycd.equals("0")) ||
                        (!Periodcd.equals(String.valueOf(ib)) && !Periodcd.equals("0")))
                    {
                        pri_msk++;
                    }
                    grp_msk++;
                    sub_msk++;
                }
                //１行出力の科目・担当名バイト数を超えた時
                String facilityname = (rs.getString("FACILITYNAME") != null) ? "/"+rs.getString("FACILITYNAME") : "" ;
                String substffacname = "["+rs.getString("SUBCLASSNAME")+"/"+rs.getString("STAFFNAME")+facilityname+"]";
                SubStfName_tmp = SubStfName_tmp + seq + substffacname;
                byte arr_byte[] = SubStfName_tmp.getBytes("MS932");
                if ( arr_byte.length > 80 ){
                    //１行出力前処理メソッド
                    set_maeshori(week_msk_tmp,week_msk,pri_msk_tmp,pri_msk,grp_msk_tmp,grp_msk,record_cnt);
                    ret = svf.VrEndRecord();        //１行出力
                    //１行出力後処理
                    record_cnt++;
                    SubStfName_tmp = "";
                    SubStfName = "";
                    seq = "";
                    week_msk_tmp = week_msk;
                    pri_msk_tmp = pri_msk;
                    grp_msk_tmp = grp_msk;
                    SubStfName_tmp = SubStfName_tmp + seq + substffacname;
                }
                SubStfName = SubStfName + seq + substffacname;
                seq = ",";
                Daycd         = String.valueOf(ia);
                Periodcd     = String.valueOf(ib);
                Groupcd     = rs.getString("GROUPCD");
                if( ia == 2 ) Day_out = "月";
                if( ia == 3 ) Day_out = "火";
                if( ia == 4 ) Day_out = "水";
                if( ia == 5 ) Day_out = "木";
                if( ia == 6 ) Day_out = "金";
                if( ia == 7 ) Day_out = "土";
                //明細出力フィールドのセット
                String schnote = "";
                if (param[5].equals("3")) {//生徒別
                    int attendno = rs.getInt("ATTENDNO");
                    schnote = "　出席番号（　"+String.valueOf(attendno)+"番　"+rs.getString("NAME")+"　）";
                }
                if (param[5].equals("2")) {//学級別
                    if (!"".equals(_course) && null != _course) {
                        schnote = "　" + _courseName;
                    }
                }
                ret=svf.VrsOut("HR_NAME"    , "学年／学級（　"+rs.getString("TARGETCLASS")+"　）"+schnote );
                ret=svf.VrsOut("WEEK"          , Day_out);            //曜日
                ret=svf.VrsOut("PERIOD"      , rs.getString("ABBV1"));        //校時
                ret=svf.VrsOut("GROUPCD"      , Groupcd);            //群コード
                ret=svf.VrsOut("GROUPNAME"  , rs.getString("GROUPNAME"));        //群名称
                ret=svf.VrsOut("SUBCLASS"      , SubStfName);        //科目・担当名
                //マスク用フィールドのセット
                ret=svf.VrsOut("WEEK_MSK"      , String.valueOf(week_msk));    //曜日
                ret=svf.VrsOut("PERI_MSK"      , String.valueOf(pri_msk));        //校時
                ret=svf.VrsOut("GRP_MSK"      , String.valueOf(grp_msk));        //群名称
                ret=svf.VrsOut("SUB_MSK"      , String.valueOf(sub_msk));        //科目名称

                data_flg = true;
            }
            //最終行出力
            if( data_flg ){
                //１行出力前処理メソッド
                set_maeshori(week_msk_tmp,week_msk,pri_msk_tmp,pri_msk,grp_msk_tmp,grp_msk,record_cnt);
                ret = svf.VrEndRecord();        //１行出力
            }
            rs.close();
            ps.close();
        } catch( Exception ex ) {
            log.error("setGroupList read error!",ex);
        }

    }  //setGroupListの括り



    //    １行出力前処理メソッド
    public void set_maeshori(int week_msk_tmp,int week_msk,int pri_msk_tmp,int pri_msk,int grp_msk_tmp,int grp_msk,int record_cnt)
                     throws ServletException, IOException
    {
        try {
                if( week_msk_tmp == week_msk && record_cnt != 0 ){
                    ret=svf.VrsOut("WEEK"      , "");            //曜日
                }
                if( pri_msk_tmp == pri_msk && record_cnt != 0 ){
                    ret=svf.VrsOut("PERIOD"      , "");        //校時
                }
                if( grp_msk_tmp == grp_msk && record_cnt != 0 ){
                    ret=svf.VrsOut("GROUPCD"      , "");        //群コード
                    ret=svf.VrsOut("GROUPNAME"  , "");        //群名称
                }
        } catch( Exception ex ) {
            log.error("set_maeshori read error!",ex);
        }

    }  //set_maeshoriの括り



    /**配列定義に使用(名称マスタより校時略称などを取得---2005.07.05)**/
    private String psNameMstCommon(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT f_period(NAMECD2) AS PERIODCD,ABBV1 ");
            stb.append("FROM   V_NAME_MST ");
            stb.append("WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("       NAMECD1='B001' AND ");
            stb.append("       NAMESPARE2 IS NULL ");
            stb.append("ORDER BY 1 ");
        } catch( Exception e ){
            log.error("psNameMstCommon error!",e);
        }
        return stb.toString();

    }//psNameMstCommon()の括り



    /**配列定義に使用**/
    private String psCommon(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //職員別
            if (param[5].equals("1")) {
                stb.append("SELECT COUNT(*) AS MAX_CNT ");
                stb.append("FROM   V_SECTION_MST W1,V_STAFF_MST W2 ");
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND ");
                stb.append("       W1.SECTIONCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND ");
                stb.append("       W2.YEAR=W1.YEAR AND ");
                stb.append("       W2.SECTIONCD=W1.SECTIONCD ");
            }
            //学級別
            if (param[5].equals("2")) {
                stb.append("SELECT COUNT(*) AS MAX_CNT ");
                stb.append("FROM   SCHREG_REGD_HDAT ");
                stb.append("WHERE  YEAR='"+param[1]+"' AND ");
                stb.append("       SEMESTER='"+param[6]+"' AND ");
                stb.append("       GRADE||HR_CLASS BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");
            }
            //生徒別
            if (param[5].equals("3")) {
                stb.append("SELECT COUNT(*) AS MAX_CNT ");
                stb.append("FROM   SCHREG_REGD_DAT ");
                stb.append("WHERE  YEAR='"+param[1]+"' AND ");
                stb.append("       SEMESTER='"+param[6]+"' AND ");
                stb.append("       GRADE||HR_CLASS BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");
            }
            //施設別
            if (param[5].equals("4")) {
                stb.append("SELECT COUNT(*) AS MAX_CNT ");
                stb.append("FROM   V_FACILITY_MST W1 ");
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND ");
                stb.append("       W1.FACCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");
            }
        } catch( Exception e ){
            log.error("psCommon error!",e);
        }
        return stb.toString();

    }//psCommon()の括り



    /**職員別時間割表**/
    private String psTeacher1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");//2005.06.27
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (param[0].equals("1")) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//NO002 ダンスの先生
                stb.append("       ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS TESTCD ");//NO010Add 基本はnull
                stb.append("       ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS DATADIV ");
                stb.append("       ,W4.CHAIRNAME ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
                //NO002 ダンスの先生
                stb.append("       LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
                stb.append("                                         W5.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                         W5.BSCSEQ = W1.BSCSEQ AND ");
                stb.append("                                         W5.DAYCD = W1.DAYCD AND ");
                stb.append("                                         W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                         W5.CHAIRCD = W1.CHAIRCD) ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//NO002 ダンスの先生
                stb.append("       ,W6.TESTKINDCD||W6.TESTITEMCD AS TESTCD ");//NO010Add
                stb.append("       ,W1.DATADIV ");
                if ("1".equals(_useTestFacility)) {
                    stb.append("       ,CASE WHEN W6.TESTKINDCD IS NOT NULL THEN TEST_FAC.FACCD END AS FACCD ");
                } else {
                    stb.append("       ,CASE WHEN W6.TESTKINDCD IS NULL THEN W2.FACCD END AS FACCD ");
                }
                stb.append("       ,W4.CHAIRNAME ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
                //NO002 ダンスの先生
                stb.append("       LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                    W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                    W5.CHAIRCD = W1.CHAIRCD) ");
                //NO010Add テスト
                stb.append("       LEFT JOIN SCH_CHR_TEST W6 ON (W6.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                     W6.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                     W6.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_FAC_DAT W2 ON (W2.YEAR = W1.YEAR AND ");
            stb.append("                                          W2.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W2.CHAIRCD = W1.CHAIRCD) ");
            if ("1".equals(_useTestFacility)) {
                stb.append("           LEFT JOIN CHAIR_TEST_FAC_DAT TEST_FAC ON (TEST_FAC.YEAR = W1.YEAR AND ");
                stb.append("                                          TEST_FAC.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                          TEST_FAC.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                          W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            if (param[0].equals("1")) {   //基本
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND  ");
                stb.append("       W1.SEMESTER='"+param[6]+"' AND  ");
                stb.append("       W1.BSCSEQ = "+param[2]+" AND  ");
                stb.append("       W1.DAYCD BETWEEN '2' AND '7' AND  ");
            } else {                                //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[7]+"' AND  ");
                stb.append("       DAYOFWEEK(W1.EXECUTEDATE) BETWEEN 2 AND 7 AND  ");
                //NO011Add テストのみ出力
                if (param[8] != null) {
                    if ("1".equals(param[21])) {
                        stb.append("   W1.DATADIV IN ('2','3') AND  ");
                    } else {
                        stb.append("   W1.DATADIV IN ('2') AND  ");
                    }
                }
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            //講座クラス
            stb.append(",CHAIR_CLS AS ( ");
            stb.append("    SELECT W2.CHAIRCD,MIN(W1.TRGTGRADE||W1.TRGTCLASS) AS TRGTGRCL ");
            stb.append("    FROM   CHAIR_CLS_DAT W1 ");
            stb.append("           LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ");
            stb.append("                                     W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("                                     W1.GROUPCD=W2.GROUPCD AND  ");
            stb.append("                                    (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ");
            stb.append("    WHERE  W1.YEAR = '"+param[1]+"' AND ");
            stb.append("           W1.SEMESTER  = '"+param[6]+"' ");
            stb.append("    GROUP BY W2.CHAIRCD ) ");
            //所属
            stb.append(",SECTION AS ( ");
            stb.append("    SELECT W2.SECTIONCD,W2.STAFFCD,W2.STAFFNAME,W1.SECTIONNAME ");
            stb.append("    FROM   V_SECTION_MST W1,V_STAFF_MST W2 ");
            stb.append("    WHERE  W1.YEAR='"+param[1]+"' AND ");
            stb.append("           W1.SECTIONCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND ");
            stb.append("           W2.YEAR=W1.YEAR AND ");
            stb.append("           W2.SECTIONCD=W1.SECTIONCD ) ");
            //必履修記号 NO013Add
            stb.append(psRequireMark(param, 1));

            //メイン
            stb.append("SELECT T1.DAYCD ");
            stb.append("       ,f_period(T1.PERIODCD) as PERIODCD ");
            stb.append("       ,T1.CHAIRCD ");
            stb.append("       ,T1.FACCD ");
            stb.append("       ,T1.STAFFCD ");
            if ("1".equals(param[20])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,T1.GROUPCD ");
            stb.append("       ,T3.SECTIONCD ");
            stb.append("       ,T3.SECTIONNAME ");
            stb.append("       ,L3.GRADE ");
            stb.append("       ,L3.HR_CLASS ");
            stb.append("       ,CASE WHEN T1.GROUPCD > '0000' THEN L3.HR_NAME||'*' ");
            stb.append("             ELSE L3.HR_NAME END AS TARGETCLASS ");
            if ("on".equals(_staffAbbvCheck)) {
                stb.append("       ,VALUE(L4.STAFFNAME_SHOW,'') AS STAFFNAME ");
            } else {
                stb.append("       ,VALUE(L4.STAFFNAME,'') AS STAFFNAME ");
            }
            if ("1".equals(_useTestFacility)) {
                stb.append("       ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L5.FACILITYNAME, '　') ");
            } else {
                stb.append("       ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L9.TESTITEMNAME, '　') ");
            }
            if ("1".equals(param[21])) {
                stb.append("             WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '3' THEN VALUE(P9.PROFICIENCYNAME1,'　') ");
            }
            stb.append("             ELSE VALUE(L5.FACILITYNAME,'　') END AS FACILITYNAME ");//NO010Modify
            if ("on".equals(_subclassAbbvCheck)) {
                if ("on".equals(_noRequireFlgCheck)) {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ");
                    }
                } else {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ");
                    }
                }
            } else {
                if ("on".equals(_noRequireFlgCheck)) {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ");
                    }
                } else {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ");
                    }
                }
            }
            stb.append("       ,L7.ABBV1 ");
            if ("on".equals(_noRequireFlgCheck)) {
                stb.append("       ,VALUE(L8.GROUPNAME,'') AS GROUPNAME ");
            } else {
                stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L8.GROUPNAME,'') AS GROUPNAME ");
            }
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ");
            stb.append("       LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ");
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param[20])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            stb.append("       LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_useTestCountflg)) {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.SEMESTER='"+param[6]+"'");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
                stb.append("                         AND L9.SCORE_DIV = '01' ");
            } else if ("TESTITEM_MST_COUNTFLG_NEW".equals(_useTestCountflg)) {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.SEMESTER='"+param[6]+"'");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
            } else {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
            }
            if ("1".equals(param[21])) {
                stb.append("       LEFT JOIN PROFICIENCY_MST P9 ON P9.PROFICIENCYDIV = '02' ");//固定
                stb.append("                                   AND P9.PROFICIENCYCD  = T1.TESTCD ");
            }
            stb.append("       LEFT JOIN REQUIRE_MARK R1 ON R1.CHAIRCD=T1.CHAIRCD ");
            stb.append("       ,CHAIR_CLS T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR='"+param[1]+"' AND  ");
            stb.append("                                        L3.SEMESTER='"+param[6]+"' AND  ");
            stb.append("                                        L3.GRADE||L3.HR_CLASS=T2.TRGTGRCL  ");
            stb.append("       ,SECTION T3 ");
            stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD AND ");
            stb.append("       T1.STAFFCD=T3.STAFFCD ");
            if (_printCd != null) {
                stb.append("       AND T1.STAFFCD = '" + _printCd + "' ");
            }
            stb.append("ORDER BY T3.SECTIONCD,T1.STAFFCD,2,T1.DAYCD, ");
            if ("1".equals(param[20])) {
                stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
            } else {
                stb.append("       T1.SUBCLASSCD, ");
            }
            stb.append("         T1.FACCD ");
        } catch( Exception e ){
            log.error("psTeacher1 error!",e);
        }
        return stb.toString();

    }//psTeacher1()の括り



    /**学級別時間割表**/
    private String psClass3(String param[],int flg)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");
            //在籍
            stb.append(",SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           SEMESTER='"+param[6]+"' AND ");
            stb.append("           GRADE||HR_CLASS BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");
            if (!"".equals(_course) && null != _course) {
                stb.append("       AND COURSECD || MAJORCD || COURSECODE = '"+_course+"' ");
            }
            stb.append("           ) ");
            //講座生徒
            stb.append(",CHAIR_STD AS ( ");
            stb.append("    SELECT DISTINCT CHAIRCD,GRADE,HR_CLASS ");
            stb.append("    FROM   CHAIR_STD_DAT W1,SCHNO W2 ");
            stb.append("    WHERE  W1.YEAR=W2.YEAR AND  ");
            stb.append("           W1.SEMESTER=W2.SEMESTER AND  ");
            stb.append("           W1.SCHREGNO=W2.SCHREGNO ) ");
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (param[0].equals("1")) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//ダンスの先生
                stb.append("       ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS TESTCD ");//NO010Add 基本はnull
                stb.append("       ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS DATADIV ");
                stb.append("       ,W4.CHAIRNAME ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
                //ダンスの先生
                stb.append("       LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
                stb.append("                                         W5.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                         W5.BSCSEQ = W1.BSCSEQ AND ");
                stb.append("                                         W5.DAYCD = W1.DAYCD AND ");
                stb.append("                                         W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                         W5.CHAIRCD = W1.CHAIRCD) ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//ダンスの先生
                stb.append("       ,W6.TESTKINDCD||W6.TESTITEMCD AS TESTCD ");
                stb.append("       ,W1.DATADIV ");
                if ("1".equals(_useTestFacility)) {
                    stb.append("       ,CASE WHEN W6.TESTKINDCD IS NOT NULL THEN TEST_FAC.FACCD END AS FACCD ");
                } else {
                    stb.append("       ,CASE WHEN W6.TESTKINDCD IS NULL THEN W2.FACCD END AS FACCD ");
                }
                stb.append("       ,W4.CHAIRNAME ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
                //ダンスの先生
                stb.append("       LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                    W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                    W5.CHAIRCD = W1.CHAIRCD) ");
                //NO010Add テスト
                stb.append("       LEFT JOIN SCH_CHR_TEST W6 ON (W6.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                     W6.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                     W6.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_FAC_DAT W2 ON (W2.YEAR = W1.YEAR AND ");
            stb.append("                                          W2.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W2.CHAIRCD = W1.CHAIRCD) ");
            if ("1".equals(_useTestFacility)) {
                stb.append("           LEFT JOIN CHAIR_TEST_FAC_DAT TEST_FAC ON (TEST_FAC.YEAR = W1.YEAR AND ");
                stb.append("                                          TEST_FAC.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                          TEST_FAC.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                          W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            if (param[0].equals("1")) {   //基本
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND  ");
                stb.append("       W1.SEMESTER='"+param[6]+"' AND  ");
                stb.append("       W1.BSCSEQ = "+param[2]+" AND  ");
                stb.append("       W1.DAYCD BETWEEN '2' AND '7' AND  ");
            } else {                                //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[7]+"' AND  ");
                stb.append("       DAYOFWEEK(W1.EXECUTEDATE) BETWEEN 2 AND 7 AND  ");
                //NO011Add テストのみ出力
                if (param[8] != null) {
                    if ("1".equals(param[21])) {
                        stb.append("   W1.DATADIV IN ('2','3') AND  ");
                    } else {
                        stb.append("   W1.DATADIV IN ('2') AND  ");
                    }
                }
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            //講座クラス
            stb.append(",CHAIR_CLS AS ( ");
            stb.append("    SELECT W2.CHAIRCD,W1.TRGTGRADE,W1.TRGTCLASS,W1.YEAR,W1.SEMESTER ");
            stb.append("    FROM   CHAIR_CLS_DAT W1 ");
            stb.append("           LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ");
            stb.append("                                     W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("                                     W1.GROUPCD=W2.GROUPCD AND  ");
            stb.append("                                    (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ");
            stb.append("    WHERE  W1.YEAR = '"+param[1]+"' AND ");
            stb.append("           W1.SEMESTER  = '"+param[6]+"' AND ");
            if (flg == 1) //学級別時間割表
                stb.append("       W1.TRGTGRADE||W1.TRGTCLASS BETWEEN '"+param[3]+"' AND '"+param[4]+"' ) ");
            if (flg == 2) //同時展開担当者一覧
                stb.append("       W1.TRGTGRADE||W1.TRGTCLASS = ? ) ");
            //必履修記号 NO013Add
            stb.append(psRequireMark(param, 2));

            //メイン
            stb.append("SELECT T1.DAYCD ");
            stb.append("       ,f_period(T1.PERIODCD) as PERIODCD ");
            stb.append("       ,T1.CHAIRCD ");
            stb.append("       ,T1.FACCD ");
            stb.append("       ,T1.STAFFCD ");
            if ("1".equals(param[20])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,T1.GROUPCD ");
            stb.append("       ,L3.GRADE ");
            stb.append("       ,L3.HR_CLASS ");
            stb.append("       ,L3.HR_NAME AS TARGETCLASS ");
            if ("on".equals(_staffAbbvCheck)) {
                stb.append("       ,VALUE(L4.STAFFNAME_SHOW,'') AS STAFFNAME ");
            } else {
                stb.append("       ,VALUE(L4.STAFFNAME,'') AS STAFFNAME ");
            }
            if ("1".equals(_useTestFacility)) {
                stb.append("       ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L5.FACILITYNAME, '　') ");
            } else {
                stb.append("       ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L9.TESTITEMNAME, '　') ");
            }
            if ("1".equals(param[21])) {
                stb.append("             WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '3' THEN VALUE(P9.PROFICIENCYNAME1,'　') ");
            }
            stb.append("             ELSE VALUE(L5.FACILITYNAME,'　') END AS FACILITYNAME ");//NO010Modify
            if ("on".equals(_subclassAbbvCheck)) {
                if ("on".equals(_noRequireFlgCheck)) {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ");
                    }
                } else {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ");
                    }
                }
            } else {
                if ("on".equals(_noRequireFlgCheck)) {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ");
                    }
                } else {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ");
                    }
                }
            }
            stb.append("       ,L7.ABBV1 ");
            stb.append("       ,L8.GROUPNAME ");
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ");
            stb.append("       LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ");
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param[20])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            stb.append("       LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_useTestCountflg)) {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.SEMESTER='"+param[6]+"'");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
                stb.append("                         AND L9.SCORE_DIV = '01' ");
            } else if ("TESTITEM_MST_COUNTFLG_NEW".equals(_useTestCountflg)) {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.SEMESTER='"+param[6]+"'");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
            } else {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
            }
            if ("1".equals(param[21])) {
                stb.append("       LEFT JOIN PROFICIENCY_MST P9 ON P9.PROFICIENCYDIV = '02' ");//固定
                stb.append("                                   AND P9.PROFICIENCYCD  = T1.TESTCD ");
            }
            stb.append("       ,CHAIR_CLS T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR=T2.YEAR AND  ");
            stb.append("                                        L3.SEMESTER=T2.SEMESTER AND  ");
            stb.append("                                        L3.GRADE=T2.TRGTGRADE AND  ");
            stb.append("                                        L3.HR_CLASS=T2.TRGTCLASS ");
            stb.append("       LEFT JOIN REQUIRE_MARK R1 ON R1.CHAIRCD=T2.CHAIRCD AND  ");
            stb.append("                                    R1.GRADE=T2.TRGTGRADE AND  ");
            stb.append("                                    R1.HR_CLASS=T2.TRGTCLASS ");
            stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
            stb.append("       AND (T2.CHAIRCD,T2.TRGTGRADE,T2.TRGTCLASS) IN (SELECT CHAIRCD,GRADE,HR_CLASS FROM CHAIR_STD) ");
            if (_printCd != null) {
                stb.append("       AND L3.GRADE || L3.HR_CLASS = '" + _printCd + "' ");
            }
            //同時展開担当者一覧
            if (flg == 2) {
                stb.append("   AND T1.GROUPCD > '0000' ");
            }
            //学級別時間割表
            if (flg == 1) {
                stb.append("ORDER BY L3.GRADE,L3.HR_CLASS,2,T1.DAYCD,T1.GROUPCD, ");
                if ("1".equals(param[20])) {
                    stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
                } else {
                    stb.append("       T1.SUBCLASSCD, ");
                }
                stb.append("         T1.STAFFCD,T1.FACCD ");
            }
            //同時展開担当者一覧
            if (flg == 2) {
                stb.append("ORDER BY T1.DAYCD,2,T1.GROUPCD, ");
                if ("1".equals(param[20])) {
                    stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
                } else {
                    stb.append("       T1.SUBCLASSCD, ");
                }
                stb.append("                    T1.STAFFCD ");
            }
        } catch( Exception e ){
            log.error("psClass3 error!",e);
        }
        return stb.toString();

    }//psClass3()の括り



    /**生徒別時間割表**/
    private String psStudent5(String param[],int flg)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");
            //在籍
            stb.append(",SCHNO AS ( ");
            stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
            stb.append("    FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           SEMESTER='"+param[6]+"' AND ");
            if (flg == 1) //生徒別時間割表
                stb.append("       GRADE||HR_CLASS BETWEEN '"+param[3]+"' AND '"+param[4]+"' ) ");
            if (flg == 2) //同時展開担当者一覧
                stb.append("       SCHREGNO=? ) ");
            //講座職員
            stb.append(",CHAIR_STF AS ( ");
            if ("on".equals(param[18])) {
                stb.append("    SELECT ");
                stb.append("        YEAR, ");
                stb.append("        SEMESTER, ");
                stb.append("        CHAIRCD, ");
                stb.append("        max(STAFFCD) AS STAFFCD ");
                stb.append("    FROM ");
                stb.append("        CHAIR_STF_DAT ");
                stb.append("    WHERE ");
                stb.append("        YEAR = '"+param[1]+"' ");
                stb.append("        AND CHARGEDIV = 1 ");
                stb.append("    GROUP BY ");
                stb.append("        YEAR, ");
                stb.append("        SEMESTER, ");
                stb.append("        CHAIRCD ");
            } else {
                stb.append("    SELECT ");
                stb.append("        YEAR, ");
                stb.append("        SEMESTER, ");
                stb.append("        CHAIRCD, ");
                stb.append("        STAFFCD ");
                stb.append("    FROM ");
                stb.append("        CHAIR_STF_DAT ");
                stb.append("    WHERE ");
                stb.append("        YEAR = '"+param[1]+"' ");
            }
            stb.append("    ) ");
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (param[0].equals("1")) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//ダンスの先生
                stb.append("       ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS TESTCD ");//NO010Add 基本はnull
                stb.append("       ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS DATADIV ");
                stb.append("       ,W4.CHAIRNAME ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
                //ダンスの先生
                stb.append("       LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
                stb.append("                                         W5.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                         W5.BSCSEQ = W1.BSCSEQ AND ");
                stb.append("                                         W5.DAYCD = W1.DAYCD AND ");
                stb.append("                                         W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                         W5.CHAIRCD = W1.CHAIRCD) ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//ダンスの先生
                stb.append("       ,W6.TESTKINDCD||W6.TESTITEMCD AS TESTCD ");//NO010Add
                stb.append("       ,W1.DATADIV ");
                if ("1".equals(_useTestFacility)) {
                    stb.append("       ,CASE WHEN W6.TESTKINDCD IS NOT NULL THEN TEST_FAC.FACCD END AS FACCD ");
                } else {
                    stb.append("       ,CASE WHEN W6.TESTKINDCD IS NULL THEN W2.FACCD END AS FACCD ");
                }
                stb.append("       ,W4.CHAIRNAME ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
                //ダンスの先生
                stb.append("       LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                    W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                    W5.CHAIRCD = W1.CHAIRCD) ");
                //NO010Add テスト
                stb.append("       LEFT JOIN SCH_CHR_TEST W6 ON (W6.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                     W6.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                     W6.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_FAC_DAT W2 ON (W2.YEAR = W1.YEAR AND ");
            stb.append("                                          W2.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W2.CHAIRCD = W1.CHAIRCD) ");
            if ("1".equals(_useTestFacility)) {
                stb.append("           LEFT JOIN CHAIR_TEST_FAC_DAT TEST_FAC ON (TEST_FAC.YEAR = W1.YEAR AND ");
                stb.append("                                          TEST_FAC.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                          TEST_FAC.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_STF W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                      W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            if (param[0].equals("1")) {   //基本
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND  ");
                stb.append("       W1.SEMESTER='"+param[6]+"' AND  ");
                stb.append("       W1.BSCSEQ = "+param[2]+" AND  ");
                stb.append("       W1.DAYCD BETWEEN '2' AND '7' AND  ");
            } else {                                //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[7]+"' AND  ");
                stb.append("       DAYOFWEEK(W1.EXECUTEDATE) BETWEEN 2 AND 7 AND  ");
                //NO011Add テストのみ出力
                if (param[8] != null) {
                    if ("1".equals(param[21])) {
                        stb.append("   W1.DATADIV IN ('2','3') AND  ");
                    } else {
                        stb.append("   W1.DATADIV IN ('2') AND  ");
                    }
                }
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
            //必履修記号 NO013Add
            stb.append(psRequireMark(param, 3));

            //メイン
            stb.append("SELECT T1.DAYCD ");
            stb.append("       ,f_period(T1.PERIODCD) as PERIODCD ");
            stb.append("       ,T1.CHAIRCD ");
            stb.append("       ,T1.FACCD ");
            stb.append("       ,T1.STAFFCD ");
            if ("1".equals(param[20])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,T1.GROUPCD ");
            stb.append("       ,T2.SCHREGNO ");
            stb.append("       ,L1.GRADE ");
            stb.append("       ,L1.HR_CLASS ");
            stb.append("       ,L1.ATTENDNO ");
            stb.append("       ,L2.NAME ");
            stb.append("       ,L3.HR_NAME AS TARGETCLASS ");
            if ("on".equals(_staffAbbvCheck)) {
                stb.append("       ,VALUE(L4.STAFFNAME_SHOW,'') AS STAFFNAME ");
            } else {
                stb.append("       ,VALUE(L4.STAFFNAME,'') AS STAFFNAME ");
            }
            if ("1".equals(_useTestFacility)) {
                stb.append("       ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L5.FACILITYNAME, '　') ");
            } else {
                stb.append("       ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L9.TESTITEMNAME, '　') ");
            }
            if ("1".equals(param[21])) {
                stb.append("             WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '3' THEN VALUE(P9.PROFICIENCYNAME1,'　') ");
            }
            stb.append("             ELSE VALUE(L5.FACILITYNAME,'　') END AS FACILITYNAME ");//NO010Modify
            if ("on".equals(_subclassAbbvCheck)) {
                if ("on".equals(_noRequireFlgCheck)) {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ");
                    }
                } else {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ");
                    }
                }
            } else {
                if ("on".equals(_noRequireFlgCheck)) {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ");
                    }
                } else {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ");
                    }
                }
            }
            stb.append("       ,L7.ABBV1 ");
            stb.append("       ,L8.GROUPNAME ");
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ");
            stb.append("       LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ");
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param[20])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            stb.append("       LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD ");
                               // NO010Add
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_useTestCountflg)) {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.SEMESTER='"+param[6]+"'");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
                stb.append("                         AND L9.SCORE_DIV = '01' ");
            } else if ("TESTITEM_MST_COUNTFLG_NEW".equals(_useTestCountflg)) {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.SEMESTER='"+param[6]+"'");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
            } else {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
            }
            if ("1".equals(param[21])) {
                stb.append("       LEFT JOIN PROFICIENCY_MST P9 ON P9.PROFICIENCYDIV = '02' ");//固定
                stb.append("                                   AND P9.PROFICIENCYCD  = T1.TESTCD ");
            }
            if (param[0].equals("1"))    //基本
                stb.append("   ,MAX_STD T2 ");
            if (param[0].equals("2"))    //通常
                stb.append("   ,CHAIR_STD T2 ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN SCHNO L1 ON L1.SCHREGNO=T2.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR=L1.YEAR AND  ");
            stb.append("                                        L3.SEMESTER=L1.SEMESTER AND  ");
            stb.append("                                        L3.GRADE=L1.GRADE AND  ");
            stb.append("                                        L3.HR_CLASS=L1.HR_CLASS ");
            stb.append("       LEFT JOIN REQUIRE_MARK R1 ON R1.CHAIRCD=T2.CHAIRCD AND  ");
            stb.append("                                    R1.SCHREGNO=T2.SCHREGNO ");
            stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
            if (_printCd != null) {
                stb.append("       AND T2.SCHREGNO = '" + _printCd + "' ");
            }
            //同時展開担当者一覧
            if (flg == 2) {
                stb.append("   AND T1.GROUPCD > '0000' ");
            }
            //通常
            if (param[0].equals("2")) {
                stb.append("   AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            }
            //生徒別時間割表
            if (flg == 1) {
                stb.append("ORDER BY L1.GRADE,L1.HR_CLASS,L1.ATTENDNO,2,T1.DAYCD ");
            }
            //同時展開担当者一覧
            if (flg == 2) {
                stb.append("ORDER BY T1.DAYCD,2,T1.GROUPCD, ");
                if ("1".equals(param[20])) {
                    stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
                } else {
                    stb.append("       T1.SUBCLASSCD, ");
                }
                stb.append("         T1.STAFFCD ");
            }
        } catch( Exception e ){
            log.error("psStudent5 error!",e);
        }
        return stb.toString();

    }//psStudent5()の括り



    /**施設別時間割表**/
    private String psFacility1(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //名称マスタ（校時：ＳＨＲを取得）
            stb.append("WITH PERIOD AS ( ");
            stb.append("    SELECT NAMECD2 AS PERIODCD ");
            stb.append("    FROM   V_NAME_MST ");
            stb.append("    WHERE  YEAR='"+param[1]+"' AND ");
            stb.append("           NAMECD1='B001' AND ");
            stb.append("           NAMESPARE2 IS NOT NULL ) ");
            //講座時間割
            stb.append(",SCH_DAT AS ( ");
            if (param[0].equals("1")) {   //基本
                stb.append("SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//ダンスの先生
                stb.append("       ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS TESTCD ");//NO010Add 基本はnull
                stb.append("       ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS DATADIV ");
                stb.append("       ,W4.CHAIRNAME ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_PTRN_DAT W1 ");
                //ダンスの先生
                stb.append("       LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ");
                stb.append("                                         W5.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                         W5.BSCSEQ = W1.BSCSEQ AND ");
                stb.append("                                         W5.DAYCD = W1.DAYCD AND ");
                stb.append("                                         W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                         W5.CHAIRCD = W1.CHAIRCD) ");
            } else {                                //通常
                stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");//NO005 DISTINCTをAdd
                stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W4.SUBCLASSCD,W4.GROUPCD ");
                if ("1".equals(param[20])) {
                    stb.append("       ,W4.CLASSCD ");
                    stb.append("       ,W4.SCHOOL_KIND ");
                    stb.append("       ,W4.CURRICULUM_CD ");
                }
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");//ダンスの先生
                stb.append("       ,W6.TESTKINDCD||W6.TESTITEMCD AS TESTCD ");//NO010Add
                stb.append("       ,W1.DATADIV ");
                if ("1".equals(_useTestFacility)) {
                    stb.append("       ,CASE WHEN W6.TESTKINDCD IS NOT NULL THEN TEST_FAC.FACCD ELSE W2.FACCD END AS FACCD ");
                } else {
                    stb.append("       ,W2.FACCD ");
                }
                stb.append("       ,W4.CHAIRNAME ");
                stb.append("       ,W4.CHAIRABBV ");
                stb.append("FROM   SCH_CHR_DAT W1 ");
                //NO002 ダンスの先生
                stb.append("       LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                    W5.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                    W5.CHAIRCD = W1.CHAIRCD) ");
                //NO010Add テスト
                stb.append("       LEFT JOIN SCH_CHR_TEST W6 ON (W6.EXECUTEDATE = W1.EXECUTEDATE AND ");
                stb.append("                                     W6.PERIODCD = W1.PERIODCD AND ");
                stb.append("                                     W6.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_FAC_DAT W2 ON (W2.YEAR = W1.YEAR AND ");
            stb.append("                                          W2.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W2.CHAIRCD = W1.CHAIRCD) ");
            if ("1".equals(_useTestFacility)) {
                stb.append("           LEFT JOIN CHAIR_TEST_FAC_DAT TEST_FAC ON (TEST_FAC.YEAR = W1.YEAR AND ");
                stb.append("                                          TEST_FAC.SEMESTER = W1.SEMESTER AND ");
                stb.append("                                          TEST_FAC.CHAIRCD = W1.CHAIRCD) ");
            }
            stb.append("           LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                          W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
            stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
            if (param[0].equals("1")) {   //基本
                stb.append("WHERE  W1.YEAR='"+param[1]+"' AND  ");
                stb.append("       W1.SEMESTER='"+param[6]+"' AND  ");
                stb.append("       W1.BSCSEQ = "+param[2]+" AND  ");
                stb.append("       W1.DAYCD BETWEEN '2' AND '7' AND  ");
            } else {                                //通常
                stb.append("WHERE  W1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[7]+"' AND  ");
                stb.append("       DAYOFWEEK(W1.EXECUTEDATE) BETWEEN 2 AND 7 AND  ");
                //NO011Add テストのみ出力
                if (param[8] != null) {
                    if ("1".equals(param[21])) {
                        stb.append("   W1.DATADIV IN ('2','3') AND  ");
                    } else {
                        stb.append("   W1.DATADIV IN ('2') AND  ");
                    }
                }
            }
            stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
            //講座クラス
            stb.append(",CHAIR_CLS AS ( ");
            stb.append("    SELECT W2.CHAIRCD,MIN(W1.TRGTGRADE||W1.TRGTCLASS) AS TRGTGRCL ");
            stb.append("    FROM   CHAIR_CLS_DAT W1 ");
            stb.append("           LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ");
            stb.append("                                     W2.SEMESTER=W1.SEMESTER AND  ");
            stb.append("                                     W1.GROUPCD=W2.GROUPCD AND  ");
            stb.append("                                    (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ");
            stb.append("    WHERE  W1.YEAR = '"+param[1]+"' AND ");
            stb.append("           W1.SEMESTER  = '"+param[6]+"' ");
            stb.append("    GROUP BY W2.CHAIRCD ) ");
            //施設
            stb.append(",FACILITY AS ( ");
            stb.append("    SELECT W1.FACCD, W1.FACILITYNAME ");
            stb.append("    FROM   V_FACILITY_MST W1 ");
            stb.append("    WHERE  W1.YEAR='"+param[1]+"' AND ");
            stb.append("           W1.FACCD BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");
            stb.append("            ) ");
            //必履修記号 NO013Add
            stb.append(psRequireMark(param, 4));

            //メイン
            stb.append("SELECT T1.DAYCD ");
            stb.append("       ,f_period(T1.PERIODCD) as PERIODCD ");
            stb.append("       ,T1.CHAIRCD ");
            stb.append("       ,T1.FACCD ");
            stb.append("       ,T1.STAFFCD ");
            if ("1".equals(param[20])) {
                stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("       ,T1.SUBCLASSCD ");
            }
            stb.append("       ,T1.GROUPCD ");
            stb.append("       ,T3.FACILITYNAME ");
            stb.append("       ,L3.GRADE ");
            stb.append("       ,L3.HR_CLASS ");
            stb.append("       ,CASE WHEN T1.GROUPCD > '0000' THEN L3.HR_NAME||'*' ");
            stb.append("             ELSE L3.HR_NAME END AS TARGETCLASS ");
            stb.append("       ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L9.TESTITEMNAME,'　') ");
            if ("1".equals(param[21])) {
                stb.append("             WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '3' THEN VALUE(P9.PROFICIENCYNAME1,'　') ");
            }
            if ("on".equals(_staffAbbvCheck)) {
                stb.append("             ELSE VALUE(L4.STAFFNAME_SHOW,'　') END AS STAFFNAME ");
            } else {
                stb.append("             ELSE VALUE(L4.STAFFNAME,'　') END AS STAFFNAME ");
            }
            if ("on".equals(_subclassAbbvCheck)) {
                if ("on".equals(_noRequireFlgCheck)) {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ");
                    }
                } else {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ");
                    }
                }
            } else {
                if ("on".equals(_noRequireFlgCheck)) {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ");
                    }
                } else {
                    if ("1".equals(param[17])) {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ");
                    } else {
                        stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ");
                    }
                }
            }
            stb.append("       ,L7.ABBV1 ");
            if ("on".equals(_noRequireFlgCheck)) {
                stb.append("       ,VALUE(L8.GROUPNAME,'') AS GROUPNAME ");
            } else {
                stb.append("       ,VALUE(R1.NAMESPARE1,'') || VALUE(L8.GROUPNAME,'') AS GROUPNAME ");
            }
            stb.append("FROM   SCH_DAT T1 ");
            stb.append("       LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ");
            stb.append("       LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ");
            stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param[20])) {
                stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
                stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
            stb.append("       LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='"+param[1]+"' AND L8.GROUPCD = T1.GROUPCD ");
            if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_useTestCountflg)) {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.SEMESTER='"+param[6]+"'");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
                stb.append("                         AND L9.SCORE_DIV = '01' ");
            } else if ("TESTITEM_MST_COUNTFLG_NEW".equals(_useTestCountflg)) {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.SEMESTER='"+param[6]+"'");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
            } else {
                stb.append("                     LEFT JOIN TESTITEM_MST_COUNTFLG L9 ON L9.YEAR='"+param[1]+"' ");
                stb.append("                         AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ");
            }
            if ("1".equals(param[21])) {
                stb.append("       LEFT JOIN PROFICIENCY_MST P9 ON P9.PROFICIENCYDIV = '02' ");//固定
                stb.append("                                   AND P9.PROFICIENCYCD  = T1.TESTCD ");
            }
            stb.append("       LEFT JOIN REQUIRE_MARK R1 ON R1.CHAIRCD=T1.CHAIRCD ");
            stb.append("       ,CHAIR_CLS T2 ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR='"+param[1]+"' AND  ");
            stb.append("                                        L3.SEMESTER='"+param[6]+"' AND  ");
            stb.append("                                        L3.GRADE||L3.HR_CLASS=T2.TRGTGRCL  ");
            stb.append("       ,FACILITY T3 ");
            stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD AND ");
            stb.append("       T1.FACCD=T3.FACCD ");
            if (_printCd != null) {
                stb.append("       AND T1.FACCD = '" + _printCd + "' ");
            }
            stb.append("ORDER BY T1.FACCD,2,T1.DAYCD, ");
            if ("1".equals(param[20])) {
                stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
            } else {
                stb.append("       T1.SUBCLASSCD, ");
            }
            stb.append("         T1.STAFFCD ");
        } catch( Exception e ){
            log.error("psFacility1 error!",e);
        }
        return stb.toString();

    }//psFacility1()の括り



    /**必履修記号を取得するＳＱＬ NO013Add **/
    private String psRequireMark(String param[], int outputFlg)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //単位マスタ
            stb.append(", CREDIT AS ( ");
            stb.append("    SELECT ");
            stb.append("        T1.COURSECD||T1.MAJORCD||T1.COURSECODE AS COURSE, ");
            stb.append("        T1.GRADE, ");
            if ("1".equals(param[20])) {
                stb.append("       T1.CLASSCD, ");
                stb.append("       T1.SCHOOL_KIND, ");
                stb.append("       T1.CURRICULUM_CD, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T1.REQUIRE_FLG, ");
            stb.append("        N1.NAMESPARE1 ");
            stb.append("    FROM ");
            stb.append("        CREDIT_MST T1, ");
            stb.append("        NAME_MST N1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR='"+param[1]+"' AND ");
            stb.append("        N1.NAMECD1='Z011' AND ");
            stb.append("        N1.NAMECD2=T1.REQUIRE_FLG ");
            stb.append("    ) ");
            //講座を履修している生徒の「学年・組・出席番号」の一番若い生徒を対象
            stb.append(", MIN_SCHNO AS ( ");
            stb.append("    SELECT ");
            stb.append("        T1.CHAIRCD, ");
            if ("1".equals(param[20])) {
                stb.append("       T3.CLASSCD, ");
                stb.append("       T3.SCHOOL_KIND, ");
                stb.append("       T3.CURRICULUM_CD, ");
            }
            stb.append("        T3.SUBCLASSCD, ");
            if (outputFlg == 2) //学級別
                stb.append("    T2.GRADE,T2.HR_CLASS, ");
            if (outputFlg == 3) //生徒別
                stb.append("    T2.SCHREGNO, ");
            stb.append("        MIN(T2.GRADE||T2.HR_CLASS||T2.ATTENDNO) AS MIN_NO ");
            stb.append("    FROM ");
            stb.append("        CHAIR_STD_DAT T1, ");
            stb.append("        SCHREG_REGD_DAT T2, ");
            stb.append("        CHAIR_DAT T3 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR='"+param[1]+"' AND ");
            stb.append("        T1.SEMESTER='"+param[6]+"' AND ");
            if (outputFlg == 2 || outputFlg == 3) //学級別 or 生徒別
                stb.append("    T2.GRADE||T2.HR_CLASS BETWEEN '"+param[3]+"' AND '"+param[4]+"' AND ");
            if (outputFlg == 2) {//学級別
                if (!"".equals(_course) && null != _course) {
                    stb.append("T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '"+_course+"' AND ");
                }
            }
            stb.append("        T2.YEAR=T1.YEAR AND ");
            stb.append("        T2.SEMESTER=T1.SEMESTER AND ");
            stb.append("        T2.SCHREGNO=T1.SCHREGNO AND ");
            stb.append("        T3.YEAR=T1.YEAR AND ");
            stb.append("        T3.SEMESTER=T1.SEMESTER AND ");
            stb.append("        T3.CHAIRCD=T1.CHAIRCD AND ");
            if ("1".equals(param[20])) {
                stb.append("        T3.CLASSCD || T3.SCHOOL_KIND || T3.CURRICULUM_CD || T3.SUBCLASSCD IN (SELECT W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD FROM CREDIT W1 GROUP BY W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD) ");
            } else {
                stb.append("        T3.SUBCLASSCD IN (SELECT W1.SUBCLASSCD FROM CREDIT W1 GROUP BY W1.SUBCLASSCD) ");
            }
            stb.append("    GROUP BY T1.CHAIRCD,T3.SUBCLASSCD ");
            if ("1".equals(param[20])) {
                stb.append("       ,T3.CLASSCD ");
                stb.append("       ,T3.SCHOOL_KIND ");
                stb.append("       ,T3.CURRICULUM_CD ");
            }
            if (outputFlg == 2) //学級別
                stb.append("    ,T2.GRADE,T2.HR_CLASS ");
            if (outputFlg == 3) //生徒別
                stb.append("    ,T2.SCHREGNO ");
            stb.append("    ) ");

            //サブメイン
            stb.append(", REQUIRE_MARK AS ( ");
            stb.append("    SELECT ");
            stb.append("        T1.CHAIRCD, ");
            if ("1".equals(param[20])) {
                stb.append("       T1.CLASSCD, ");
                stb.append("       T1.SCHOOL_KIND, ");
                stb.append("       T1.CURRICULUM_CD, ");
            }
            stb.append("        T1.SUBCLASSCD, ");
            stb.append("        T2.SCHREGNO, ");
            stb.append("        T2.GRADE, ");
            stb.append("        T2.HR_CLASS, ");
            stb.append("        T2.COURSECD||T2.MAJORCD||T2.COURSECODE AS COURSE, ");
            stb.append("        T3.REQUIRE_FLG, ");
            stb.append("        T3.NAMESPARE1 ");
            stb.append("    FROM ");
            stb.append("        MIN_SCHNO T1, ");
            stb.append("        SCHREG_REGD_DAT T2, ");
            stb.append("        CREDIT T3 ");
            stb.append("    WHERE ");
            stb.append("        T2.YEAR='"+param[1]+"' AND ");
            stb.append("        T2.SEMESTER='"+param[6]+"' AND ");
            stb.append("        T2.GRADE||T2.HR_CLASS||T2.ATTENDNO = T1.MIN_NO AND ");
            if ("1".equals(param[20])) {
                stb.append("        T3.CLASSCD=T1.CLASSCD AND ");
                stb.append("        T3.SCHOOL_KIND=T1.SCHOOL_KIND AND ");
                stb.append("        T3.CURRICULUM_CD=T1.CURRICULUM_CD AND ");
            }
            stb.append("        T3.SUBCLASSCD=T1.SUBCLASSCD AND ");
            stb.append("        T3.GRADE=T2.GRADE AND ");
            stb.append("        T3.COURSE = T2.COURSECD||T2.MAJORCD||T2.COURSECODE ");
            stb.append("    ) ");
        } catch( Exception e ){
            log.error("psRequireMark error!",e);
        }
        return stb.toString();

    }//psRequireMark()の括り



    /*------------------------------------*
     * 見出し項目のセット                 *
     *------------------------------------*/
    public void setHeader(String param[])
                     throws ServletException, IOException
    {
        try {
            String title = "";
            String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(param[1])) + "年度";
            if (param[5].equals("1")) title = "　職員別時間割表";
            if (param[5].equals("2")) title = "　学級別時間割表";
            if (param[5].equals("3")) title = "　生徒別時間割表";
            if (param[5].equals("4")) title = "　施設別時間割表";

            ret=svf.VrsOut("nendo"  , nendo + title );
            ret=svf.VrsOut("ymd"    , KNJ_EditDate.h_format_JP(param[10]));
            //基本
            if(param[0].equals("1")){
                ret=svf.VrsOut("TITLE"     , "(" + param[14] + ")");
            //通常
            } else{

                int nen  = Integer.parseInt(param[12].substring(0,4));
                int tuki = Integer.parseInt(param[12].substring(5,7));
                int hi   = Integer.parseInt(param[12].substring(8));
                Calendar cals = Calendar.getInstance();
                cals.set(nen,tuki-1,hi);

                //NO012Modiy
                String week[] = {"月","火","水","木","金","土"};//曜日
                int date_no = 1;
                for(int ia=0 ; ia<7 ; ia++){
                    if(ia != 0){
                        cals.add(Calendar.DATE,+1);
                        nen  = cals.get(Calendar.YEAR);
                        tuki = cals.get(Calendar.MONTH);
                        tuki++;
                        hi   = cals.get(Calendar.DATE);
                    }
                    int week_no = cals.get(Calendar.DAY_OF_WEEK);
                    if (week_no == 1) {
                        date_no = 0;
                        continue;//日曜日は処理しない。
                    }
                    ret=svf.VrsOut("date"+(ia+date_no), "(" + tuki + "/" + hi + ")");
                    ret=svf.VrsOut("WEEK"+(ia+date_no), week[week_no-2]);
                }
            }
        } catch( Exception ex ) {
            log.error("setHeader read error!",ex);
        }

    }  //setHeaderの括り



    /*--------------------*
     * 学期取得           *
     *--------------------*/
    public void get_semester(String param[])
                     throws ServletException, IOException
    {
        try {
            //指定日付を日付型へ変換
            String strx = new String();
                strx = "9";

            //学期期間の取得 NO008Modify
            String sql = new String();
            sql = "SELECT SEMESTER "
                + "      ,case when '"+param[2]+"' < SDATE then SDATE else null end as SDATE "
                + "      ,case when '"+param[7]+"' > EDATE then EDATE else null end as EDATE "
                + "FROM SEMESTER_MST "
                + "WHERE SDATE <= ? AND EDATE >= ? AND YEAR=? AND SEMESTER<>?";

            PreparedStatement ps = db2.prepareStatement(sql);
            ps.setString(1, param[16]);
            ps.setString(2, param[16]);
            ps.setString(3, param[1]);
            ps.setString(4, strx);
            ResultSet rs = ps.executeQuery();

            param[12] = param[2]; //週開始日 NO008Add
            if( rs.next() ){
                param[6] = rs.getString("SEMESTER"); //学期
                if (rs.getString("SDATE") != null) param[2] = rs.getString("SDATE"); //学期開始日 NO008Add
                if (rs.getString("EDATE") != null) param[7] = rs.getString("EDATE"); //学期終了日 NO008Add
            }
            if(param[6] == null) param[6] = "0";
            ps.close();
            rs.close();
            db2.commit();
            log.debug("get_semester ok!");
        } catch( Exception e ){
            log.error("get_semester error!",e);
        }
    }  //get_semesterの括り



    /*----------------*
     * 月の前ゼロ挿入 *
     *----------------*/
    public String h_tuki(int intx)
                     throws ServletException, IOException
    {
        String strx = null;
        try {
            strx = "00" + String.valueOf(intx);
            strx = strx.substring(strx.length()-2);
        } catch( Exception ex ) {
            log.error("h_tuki error!",ex);
        }
        return strx;
    }  //h_tukiの括り

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

    /*------------------------------------*
     * 学級別時間割表のセット             *
     *------------------------------------*/
    public void setTimeTable(int gyo_max,String day_sub_stf[][],String period,String period_name,String param[])
                     throws ServletException, IOException
    {
        //TODO
        try {
            final int gyo = (!"on".equals(_noStaffCheck) || (param[5].equals("1"))) ? 3 : 2;
            int fieldno = 0;
            for (int max_len=0; max_len<(gyo_max*gyo); max_len++){
                for (int len=0; len<6; len++){
                    fieldno = (param[8] != null) ? daycd_keep[len] : len+1;
                    int arr_bytelength = getMS932ByteLength(day_sub_stf[len][max_len]);
                    if ( arr_bytelength > 18 ){
                        ret=svf.VrsOut("CLASSNAME"+String.valueOf(fieldno)+"_2", day_sub_stf[len][max_len]);
                    } else if (12 < arr_bytelength && arr_bytelength <= 18) {
                        ret=svf.VrsOut("CLASSNAME"+String.valueOf(fieldno)+"_3", day_sub_stf[len][max_len]);
                    } else {
                        ret=svf.VrsOut("CLASSNAME"+String.valueOf(fieldno), day_sub_stf[len][max_len]);
                    }
                    ret=svf.VrsOut("CLASSCD"+String.valueOf(fieldno)  , period);        //校時（グループサプレス）
                    day_sub_stf[len][max_len] = "";
                }
                ret=svf.VrsOut("PERIOD"  , period_name);        //校時
                ret = svf.VrEndRecord();
            }
        } catch( Exception ex ) {
            log.error("setTimeTable read error!",ex);
        }

    }  //setTimeTableの括り


    /*------------------------------------*
     * 授業がない校時のセット NO004
     *------------------------------------*/
    public void setBrank(int flg,int arr_min_period,int arr_max_period,int grcl_cnt2,int period_suu[],int grcl_period_suu[][],String period,String period_abbv1[])
                     throws ServletException, IOException
    {
        try {
            if (flg == 1) {
                  for (int i=arr_min_period; i<arr_max_period; i++) {
                    if (grcl_period_suu[grcl_cnt2][i] != 1 && period_suu[i] == 0 && period_abbv1[i] != null) {
                        if (i < Integer.parseInt(period)) {
                            setBrankPeriod(String.valueOf(i),period_abbv1[i]);
                            period_suu[i] = 1;
                        }
                    }
                }
            } else {
                  for (int i=arr_min_period; i<arr_max_period; i++) {
                    if (grcl_period_suu[grcl_cnt2][i] != 1 && period_suu[i] == 0 && period_abbv1[i] != null) {
                        if (Integer.parseInt(period) < i) {
                            setBrankPeriod(String.valueOf(i),period_abbv1[i]);
                            period_suu[i] = 1;
                        }
                    }
                }
            }
        } catch( Exception ex ) {
            log.error("setBrank read error!",ex);
        }

    }  //setBrankの括り


    /*------------------------------------*
     * 授業がない校時のセット             *
     *------------------------------------*/
    public void setBrankPeriod(String period,String period_name)
                     throws ServletException, IOException
    {
        try {
            for (int max_len=0; max_len<3; max_len++){
                for (int len=0; len<6; len++){
                    ret=svf.VrsOut("CLASSNAME"+String.valueOf(len+1), "");
                    ret=svf.VrsOut("CLASSCD"+String.valueOf(len+1)  , period);        //校時（グループサプレス）
                }
                ret=svf.VrsOut("PERIOD"  , period_name);
                ret = svf.VrEndRecord();
            }
        } catch( Exception ex ) {
            log.error("setBrankPeriod read error!",ex);
        }

    }  //setBrankPeriodの括り


    //基本時間割・・・[1]年度 [2]SEQ [6]学期 [14]タイトル
    public void setParamK(String param[], String weekFlg)
                     throws ServletException, IOException
    {
        try {
            //Ａ週
            if ("A".equals(weekFlg)) {
                param[1] = _tYear;
                param[2] = _tBscseq;
                param[6] = _tSemester;
            }
            //Ｂ週
            if ("B".equals(weekFlg)) {
                param[1] = _tYear2;
                param[2] = _tBscseq2;
                param[6] = _tSemester2;
            }
            //タイトル
            String sql = "SELECT TITLE "
                       + "FROM SCH_PTRN_HDAT "
                       + "WHERE YEAR = '" + param[1] + "' AND BSCSEQ = " + param[2] + " AND SEMESTER = '" + param[6] + "'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                param[14] = rs.getString(1);
            }
            rs.close();
            ps.close();
        } catch( Exception e ){
            log.error("setParamK SCH_PTRN_HDAT error!",e);
        }

    }  //setParamKの括り


    //通常時間割・・・[2]週開始日 [7]週最終日 [1]年度 [6]学期
    public void setParamT(String param[], String weekFlg)
                     throws ServletException, IOException
    {
        try {
            //Ａ週
            if ("A".equals(weekFlg)) {
                param[2] = _fromDate;
                param[7] = _toDate;
            }
            //Ｂ週
            if ("B".equals(weekFlg)) {
                param[2] = _fromDate2;
                param[7] = _toDate2;
            }

            /* テストのみ出力の場合 */
            if (param[8] != null) {
                /* テスト名の取得 */
                try {
                    String dataDivIN = ("1".equals(param[21])) ? "('2','3')" : "('2')";
                    String sql = "SELECT DISTINCT "
                               + "       L2.SEMESTERNAME, "
                               + "       T1.SEMESTER "
                               + "FROM   SCH_CHR_TEST T1 "
                               + "       INNER JOIN SCH_CHR_DAT T2 ON T2.EXECUTEDATE = T1.EXECUTEDATE "
                               + "                                AND T2.PERIODCD = T1.PERIODCD "
                               + "                                AND T2.CHAIRCD = T1.CHAIRCD "
                               + "                                AND T2.DATADIV IN "+dataDivIN+" "
                               + "       LEFT JOIN SEMESTER_MST L2 ON L2.YEAR=T1.YEAR AND L2.SEMESTER=T1.SEMESTER "
                               + "WHERE  T1.EXECUTEDATE BETWEEN '"+param[2]+"' AND '"+param[7]+"' ";
                    PreparedStatement ps = db2.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    if( rs.next() ){
                        param[14] = rs.getString(1);
                    }
                    rs.close();
                    ps.close();
                } catch( Exception e ){
                    log.error("TEST TITLE error!",e);
                }

                /* 曜日コード保管 */
                try {
                    int nen  = Integer.parseInt(param[2].substring(0,4));
                    int tuki = Integer.parseInt(param[2].substring(5,7));
                    int hi   = Integer.parseInt(param[2].substring(8));
                    Calendar cals = Calendar.getInstance();
                    cals.set(nen,tuki-1,hi);
                    int date_no = 1;
                    for(int ia=0 ; ia<7 ; ia++){
                        if(ia != 0){
                            cals.add(Calendar.DATE,+1);
                            nen  = cals.get(Calendar.YEAR);
                            tuki = cals.get(Calendar.MONTH);
                            tuki++;
                            hi   = cals.get(Calendar.DATE);
                        }
                        int week_no = cals.get(Calendar.DAY_OF_WEEK);
                        if (week_no == 1) {
                            date_no = 0;
                            continue;//日曜日は処理しない。
                        }
                        daycd_keep[week_no-2] = ia+date_no;
                    }
                } catch( Exception e ){
                    log.error("daycd_keep error!",e);
                }
            }
        } catch( Exception e ){
            log.error("setParamT error!",e);
        }

    }  //setParamTの括り


    //7日後を取得・・・2週目の週開始日・週最終日
    public String getDate7(String prmDate)
                     throws ServletException, IOException
    {
        String rtnDate = null;
        try {
            if (prmDate != null) {
                int nen  = Integer.parseInt(prmDate.substring(0,4));
                int tuki = Integer.parseInt(prmDate.substring(5,7));
                int hi   = Integer.parseInt(prmDate.substring(8));
                Calendar cals = Calendar.getInstance();
                cals.set(nen, tuki-1, hi);
                cals.add(Calendar.DATE, +7); //7日後
                nen  = cals.get(Calendar.YEAR);
                tuki = cals.get(Calendar.MONTH);
                tuki++;
                hi   = cals.get(Calendar.DATE);
                rtnDate = Integer.toString(nen) + "-" + h_tuki(tuki) + "-" + h_tuki(hi);
            }
        } catch( Exception e ){
            log.error("getDate7 error!",e);
        }
        return rtnDate;
    }



}  //クラスの括り
