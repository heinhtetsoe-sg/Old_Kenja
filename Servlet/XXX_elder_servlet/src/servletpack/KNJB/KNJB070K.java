package servletpack.KNJB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ０７０Ｋ＞  学級別試験時間割表
 *
 * 2008/04/24 nakamoto 新規作成
 **/

public class KNJB070K {

    private static final Log log = LogFactory.getLog(KNJB070K.class);

    Vrw32alp svf = new Vrw32alp();
    DB2UDB   db2;
    int ret;
    boolean nonedata  = false;

    Param _param;

    final int maxPeriod = 36;
    final int maxHr = 27;

    final String GAKKYUU = "1";
    final String SEITO = "2";
    final String SISETU = "3";
    final String SIKEN_KANTOKU = "4";

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

        String form = "KINDAI".equals(_param._z010Name1) ? "KNJB070K.frm" : "KNJB070K_2.frm";
        svf.VrSetForm(form, 4);
        log.debug("印刷するフォーム:"+form);

        set_head();

        if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
            for (Iterator itg = _param._gradeMap.values().iterator(); itg.hasNext();) {
                final Grades grades = (Grades) itg.next();
                final String grade = grades._grade;
                if (null == grade) break;
                log.debug("学年=" + grade);

                final List dataList = getList(db2, sqlHrClass(grade));
                for (int i = 0; i < dataList.size(); i++) {
                    final Map m = (Map) dataList.get(i);
                    final String hrClass = getString(m, "HR_CLASS");
                    if (null == hrClass) break;
                    log.debug("組=" + hrClass);

                    svf.VrsOut("GRADE", getString(m, "HR_NAME"));
                    if (set_chapter1(db2, svf, grade + hrClass)) {
                        rtnflg = true;
                    }
                }
            }
        } else {
            for (Iterator itg = _param._gradeMap.values().iterator(); itg.hasNext();) {
                final Grades grades = (Grades) itg.next();
                final String grade = grades._grade;
                if (null == grade) break;
                log.debug("学年=" + grade);
                svf.VrsOut("GRADE" , grades._gradeName1);
                if (set_chapter1(db2, svf, grade)) {
                    rtnflg = true;
                }
            }
        }

        return rtnflg;
    }

    /*------------------------------------*
     * 学級別時間割表ＳＶＦ出力           *
     *------------------------------------*/
    private boolean set_chapter1(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String grade
    ) throws ServletException, IOException {
        boolean rtnflg = false;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = Pre_Stat3(grade);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            log.debug("sql = " + sql);
            log.debug("set_chapter1 sql ok!");

                /** SVFフォームへデータをセット **/
            String g_hr_class = "0"; // 学年＋組
            String cmpData = ""; //比較用
            int gyo = 0;             //行No
            int dayMaxCnt = _param._testDateMap.size();
            if (dayMaxCnt == 0) dayMaxCnt = 7;
            String period[]         = new String[maxPeriod];           //校時
            int subcnt = 0;
            int iday_flg = 0;
            int iper_flg = 0;
            int igyo_flg = 0;
            Subclass subclass[][][]   = new Subclass[dayMaxCnt][maxPeriod][maxHr];    //科目名
            for (int ia = 0; ia < subclass.length; ia++) {
                for (int ib = 0; ib < subclass[ia].length; ib++) {
                    for (int ic = 0; ic < subclass[ia][ib].length; ic++) {
                        subclass[ia][ib][ic] = new Subclass();
                    }
                }
            }
            final List subclassList = new ArrayList();
            
            while( rs.next() ){
                if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
                    cmpData = rs.getString("SCHREGNO");
                } else if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
                    cmpData = rs.getString("FACCD");
                } else if (_param._isPrintTest && SIKEN_KANTOKU.equals(_param._outDiv)) {
                    cmpData = rs.getString("STAFFCD");
                } else {
                    cmpData = rs.getString("GRADE") + rs.getString("HR_CLASS");
                }
                //学年＋組のブレイク時
                if (!g_hr_class.equals(cmpData)) {
                    if (!g_hr_class.equals("0")) {
                        //３０名分出力
                        if (gyo == maxHr) {
                            subclassList.add(copy(dayMaxCnt, subclass));
                            //初期化
                            gyo = 0;
                            for (final Iterator it = _param._testDateMap.values().iterator(); it.hasNext();) {
                                final ExecuteDates executeDates = (ExecuteDates) it.next();
                                int ia = executeDates._executeNo;
                                for (int ib = 0; ib < maxPeriod; ib++) {
                                    for (int ic = 0; ic < maxHr; ic++) {
                                        subclass[ia][ib][ic] = new Subclass();    //科目名
                                    }
                                }
                            }
                        }
                    }
                    g_hr_class = cmpData;
                    gyo++;
                }

                final ExecuteDates executeDates = (ExecuteDates) _param._testDateMap.get(rs.getString("EXECUTEDATE"));
                int iday = executeDates._executeNo;
                int iper = rs.getInt("PERIODCD");

                period[iper] = rs.getString("ABBV1");
                
                if (iday_flg != iday || iper_flg != iper || igyo_flg != gyo) {
                    subcnt = 0;
                }
                if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
                    subclass[iday][iper][gyo-1]._targetClass = rs.getString("NAME");
                } else if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
                    subclass[iday][iper][gyo-1]._targetClass = rs.getString("FACILITYNAME");
                } else if (_param._isPrintTest && SIKEN_KANTOKU.equals(_param._outDiv)) {
                    subclass[iday][iper][gyo-1]._targetClass = rs.getString("STAFFNAME");
                } else {
                    subclass[iday][iper][gyo-1]._targetClass = rs.getString("TARGETCLASS");
                }
                subclass[iday][iper][gyo-1]._flg = rs.getString("SUBCLASSABBV");
                subclass[iday][iper][gyo-1]._cnt = subcnt;
                subclass[iday][iper][gyo-1]._abbv[subcnt] = rs.getString("SUBCLASSABBV");
                subclass[iday][iper][gyo-1]._subclasscd[subcnt] = rs.getString("SUBCLASSCD");
                if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
                    subclass[iday][iper][gyo-1]._studentCount[subcnt] = 1;
                } else {
                    subclass[iday][iper][gyo-1]._studentCount[subcnt] = rs.getInt("STUDENT_COUNT");
                }
                subclass[iday][iper][gyo-1]._gedan[subcnt] = rs.getString("GEDAN_NAME");
                if (subcnt < 4) {
                    subcnt++;
                }
                iday_flg = iday;
                iper_flg = iper;
                igyo_flg = gyo;
            }
            //最後のレコード出力
            if (!g_hr_class.equals("0")) {
                subclassList.add(subclass);
                for (int i = 0; i < subclassList.size(); i++) {
                    final Subclass[][][] s = (Subclass[][][]) subclassList.get(i);
                    print(svf, s, period);
                    rtnflg  = true; //該当データなしフラグ
                    //初期化
                    for (int j = 0; j < maxHr; j++) {
                        svf.VrsOutn("HR_CLASS"     ,j+1    ,"");     //20桁
                        svf.VrsOutn("HR_CLASS2"    ,j+1    ,"");     //14桁
                        svf.VrsOutn("HR_CLASS3"    ,j+1    ,"");     //10桁
                    }
                }
            }
            log.debug("set_chapter1 read ok!");
        } catch (Exception ex) {
            log.debug("set_chapter1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return rtnflg;
    }
    
    private Subclass[][][] copy(final int dayMaxCnt, final Subclass[][][] subclass) {
        Subclass copy[][][]   = new Subclass[dayMaxCnt][maxPeriod][maxHr];    //科目名
        for (int ia = 0; ia < subclass.length; ia++) {
            for (int ib = 0; ib < subclass[ia].length; ib++) {
                for (int ic = 0; ic < subclass[ia][ib].length; ic++) {
                    copy[ia][ib][ic] = new Subclass();
                    copy[ia][ib][ic]._targetClass = subclass[ia][ib][ic]._targetClass;
                    copy[ia][ib][ic]._flg = subclass[ia][ib][ic]._flg;
                    copy[ia][ib][ic]._cnt = subclass[ia][ib][ic]._cnt;
                    for (int i = 0; i < subclass[ia][ib][ic]._abbv.length; i++) {
                        copy[ia][ib][ic]._abbv[i] = subclass[ia][ib][ic]._abbv[i];
                    }
                    for (int i = 0; i < subclass[ia][ib][ic]._subclasscd.length; i++) {
                        copy[ia][ib][ic]._subclasscd[i] = subclass[ia][ib][ic]._subclasscd[i];
                    }
                    for (int i = 0; i < subclass[ia][ib][ic]._studentCount.length; i++) {
                        copy[ia][ib][ic]._studentCount[i] = subclass[ia][ib][ic]._studentCount[i];
                    }
                    for (int i = 0; i < subclass[ia][ib][ic]._gedan.length; i++) {
                        copy[ia][ib][ic]._gedan[i] = subclass[ia][ib][ic]._gedan[i];
                    }
                }
            }
        }
        return copy;
    }

    private static int getMS932ByteCount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private void print(final Vrw32alp svf, final Subclass[][][] subclass, final String[] period) {
        int ret_brank = 0;//空列カウント
        for (final Iterator it = _param._testDateMap.values().iterator(); it.hasNext();) {
            final ExecuteDates executeDates = (ExecuteDates) it.next();
            int ia = executeDates._executeNo;
            for (int ib = 0; ib < maxPeriod; ib++) {
                int maxPrintHr = -1;
                final InsertOrderMap subclassStudentCount = new InsertOrderMap();
                final Map subclassAbbv = new HashMap();
                for (int ic = 0; ic < maxHr; ic++) {
                    //明細項目セット
                    final Subclass s = subclass[ia][ib][ic];
                    if (null != s._targetClass) {
                        final int strLen = getMS932ByteCount(s._targetClass);
                        if (strLen <= 10) {
                            svf.VrsOutn("HR_CLASS",  ic + 1, s._targetClass);
                        } else if (strLen <= 14) {
                            svf.VrsOutn("HR_CLASS2", ic + 1, s._targetClass);
                        } else {
                            svf.VrsOutn("HR_CLASS3", ic + 1, s._targetClass);
                        }
                    }
                    if (s._flg != null) {
                        maxPrintHr = ic;
                        if (s._cnt == 0) {
                            svf.VrsOutn("SUBCLASS2_1"  ,ic+1 , s._abbv[0]);
                            svf.VrsOutn("SUBCLASS2_2"  ,ic+1 , s._gedan[0]);
                        } else if (s._cnt >= 1) {
                            svf.VrsOutn("SUBCLASS2_1"  ,ic+1 , s._abbv[0] + "," + s._abbv[1]);
                            svf.VrsOutn("SUBCLASS2_2"  ,ic+1 , s._gedan[0] + "," + s._gedan[1]);
                        }
//                        if (s._cnt == 0) {
//                            svf.VrsOutn("SUBCLASS1"  ,ic+1 , s._abbv[0]);
//                        } else if (s._cnt == 1) {
//                            svf.VrsOutn("SUBCLASS1"  ,ic+1 , s._abbv[0] + "," + s._abbv[1]);
//                        } else if (s._cnt == 2) {
//                            svf.VrsOutn("SUBCLASS2_1"  ,ic+1 , s._abbv[0] + "," + s._abbv[1]);
//                            svf.VrsOutn("SUBCLASS2_2"  ,ic+1 , s._abbv[2]);
//                        } else if (s._cnt >= 3) {
//                            svf.VrsOutn("SUBCLASS2_1"  ,ic+1 , s._abbv[0] + "," + s._abbv[1]);
//                            svf.VrsOutn("SUBCLASS2_2"  ,ic+1 , s._abbv[2] + "," + s._abbv[3]);
//                        }
                        for (int i = 0; i <= s._cnt; i++) {
                            if (null != s._subclasscd[i]) {
                                subclassAbbv.put(s._subclasscd[i], s._abbv[i]);
                            }
                            final Integer cnt = null == subclassStudentCount.get(s._subclasscd[i]) ? new Integer(0) : (Integer) subclassStudentCount.get(s._subclasscd[i]);
                            subclassStudentCount.put(s._subclasscd[i], new Integer(cnt.intValue() + s._studentCount[i]));
                        }
                    }
                }
                if (maxPrintHr >= 0) {
                    final int maxStudentSubclass = 3;
                    final List subclassList = new ArrayList(subclassStudentCount.entrySet());
                    for (int ic = 0; ic < subclassList.size(); ic++) {
                        if (ic >= maxStudentSubclass) {
                            continue;
                        }
                        svf.VrsOutn("HR_CLASS", ic + 1 + maxHr + 3 - maxStudentSubclass, "人数");
                        final Map.Entry e = (Map.Entry)subclassList.get(ic); 
                        final String subclasscd = (String) e.getKey();
                        final Integer studentCount = (Integer) e.getValue();
                        //svf.VrsOutn("SUBCLASS2_1", ic + maxHr - maxStudentSubclass, subclasscd);
                        svf.VrsOutn("SUBCLASS2_1", ic + 1 + maxHr + 3 - maxStudentSubclass, (String) subclassAbbv.get(subclasscd));
                        if (null != studentCount) {
                            svf.VrsOutn("SUBCLASS2_2", ic + 1 + maxHr + 3 - maxStudentSubclass, studentCount.toString());
                        }
                    }
                    
                    svf.VrsOut("T_DATE"         , executeDates._executeMD + "(" + executeDates._executeW + ")");
                    svf.VrsOut("PERIOD"         , period[ib]);                    //校時
                    svf.VrEndRecord();//１列出力
                    ret_brank++;//空列カウント
                }
            }
        }
        //空列出力
        for ( ; ret_brank%20 > 0; ret_brank++) svf.VrEndRecord();
    }

    private void set_head()
                     throws ServletException, IOException
    {
        try {
            svf.VrsOut("NENDO"    , nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
            svf.VrsOut("TITLE"    , StringUtils.defaultString(_param._semesterName) + StringUtils.defaultString(_param._testName) + "試験時間割一覧" + _param._outDivName);
            svf.VrsOut("DATE"     , KNJ_EditDate.h_format_JP(_param._ctrlDate));
            svf.VrsOut("TERM"     , "(" + KNJ_EditDate.h_format_JP(_param._dateS) + " \uFF5E " + KNJ_EditDate.h_format_JP(_param._dateE) + ")");
            svf.VrsOut("HR_CLASS_HEADER"    , _param._outDivName2);
            log.debug("set_head read ok!");
        } catch( Exception ex ) {
            log.debug("set_head read error!", ex);
        }

    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sqlHrClass(final String grade) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '"+_param._year+"' ");
        stb.append("     AND SEMESTER = '"+_param._semester+"' ");
        stb.append("     AND GRADE = '" + grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS ");
        return stb.toString();
    }

    /**学級別時間割表**/
    private String Pre_Stat3(final String grade)
    {
        StringBuffer stb = new StringBuffer();
        //名称マスタ（校時：ＳＨＲを取得）
        stb.append("WITH PERIOD AS ( ");
        stb.append("    SELECT NAMECD2 AS PERIODCD ");
        stb.append("    FROM   V_NAME_MST ");
        stb.append("    WHERE  YEAR = '" + _param._year + "' AND ");
        stb.append("           NAMECD1 = 'B001' AND ");
        stb.append("           NAMESPARE2 IS NOT NULL ) ");
        //講座時間割
        stb.append(",SCH_DAT AS ( ");
            stb.append("SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ");
            stb.append("       ,W1.PERIODCD,W1.CHAIRCD,W4.SUBCLASSCD,W4.GROUPCD,W1.DATADIV ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       ,W4.CLASSCD ");
                stb.append("       ,W4.SCHOOL_KIND ");
                stb.append("       ,W4.CURRICULUM_CD ");
            }
            if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
                stb.append("       ,W2.FACCD ");
            }
            if (_param._isPrintTest && SIKEN_KANTOKU.equals(_param._outDiv)) {
                stb.append("       ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ");
            }
            stb.append("FROM   SCH_CHR_DAT W1 ");
            stb.append("     INNER JOIN SCH_CHR_TEST T2 ");
            stb.append("        ON  T2.EXECUTEDATE = W1.EXECUTEDATE ");
            stb.append("        AND T2.PERIODCD = W1.PERIODCD ");
            stb.append("        AND T2.CHAIRCD = W1.CHAIRCD ");
            stb.append("        AND T2.YEAR = '" + _param._year + "' ");
            stb.append("        AND T2.SEMESTER = '" + _param._semester + "' ");
            if (_param._isPrintTest) {
                stb.append("    AND T2.TESTKINDCD || T2.TESTITEMCD = '" + _param._testCd + "' ");
            } else {
                stb.append("    AND T2.TESTKINDCD || T2.TESTITEMCD = '" + _param._proficiencyCd + "' ");
            }
        stb.append("           LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ");
        stb.append("                                      W4.SEMESTER = W1.SEMESTER AND ");
        stb.append("                                      W4.CHAIRCD = W1.CHAIRCD) ");
        if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
            stb.append("           LEFT JOIN " + _param._tableNameChairTestFacDat + " W2 ON (W2.YEAR = W1.YEAR AND ");
            stb.append("                                          W2.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W2.CHAIRCD = W1.CHAIRCD) ");
        }
        if (_param._isPrintTest && SIKEN_KANTOKU.equals(_param._outDiv)) {
            stb.append("           LEFT JOIN CHAIR_STF_DAT W3 ON (W3.YEAR = W1.YEAR AND ");
            stb.append("                                          W3.SEMESTER = W1.SEMESTER AND ");
            stb.append("                                          W3.CHAIRCD = W1.CHAIRCD) ");
            stb.append("           LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ");
            stb.append("                                        W5.PERIODCD = W1.PERIODCD AND ");
            stb.append("                                        W5.CHAIRCD = W1.CHAIRCD) ");
        }
            stb.append("WHERE  W1.DATADIV='" + _param._dataDiv + "' AND  ");
        stb.append("           W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ");
        //講座クラス
        stb.append(",CHAIR_CLS AS ( ");
        stb.append("    SELECT W2.CHAIRCD,W1.TRGTGRADE,W1.TRGTCLASS,W1.YEAR,W1.SEMESTER ");
        stb.append("    FROM   CHAIR_CLS_DAT W1 ");
        stb.append("           LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ");
        stb.append("                                     W2.SEMESTER=W1.SEMESTER AND  ");
        stb.append("                                     W1.GROUPCD=W2.GROUPCD AND  ");
        stb.append("                                    (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ");
        stb.append("    WHERE  W1.YEAR = '"+_param._year+"' AND ");
        stb.append("           W1.SEMESTER  = '" + _param._semester + "' AND ");
        if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
            stb.append("           W1.TRGTGRADE || W1.TRGTCLASS = '" + grade + "' ");
        } else {
            stb.append("           W1.TRGTGRADE = '" + grade + "' ");
        }
        stb.append(" ) ");
        //講座生徒 NO011Add
        stb.append(",EXISTS_SCHNO AS ( ");
        stb.append("    select chaircd, grade, hr_class, count(distinct w1.schregno) as student_count ");
        stb.append("    from chair_std_dat w1 ");
        stb.append("         left join schreg_regd_dat w2 on w2.year = w1.year ");
        stb.append("                                     and w2.semester = w1.semester ");
        stb.append("                                     and w2.schregno = w1.schregno ");
        stb.append("    where w1.year='" + _param._year + "' ");
        stb.append("      and w1.semester='" + _param._semester + "' ");
        stb.append("    group by chaircd, grade, hr_class ");
        stb.append(" ) ");
        if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
            stb.append(",CHAIR_STD AS ( ");
            stb.append("    select chaircd, grade, hr_class, attendno, w1.schregno ");
            stb.append("    from chair_std_dat w1 ");
            stb.append("         left join schreg_regd_dat w2 on w2.year = w1.year ");
            stb.append("                                     and w2.semester = w1.semester ");
            stb.append("                                     and w2.schregno = w1.schregno ");
            stb.append("    where w1.year='" + _param._year + "' ");
            stb.append("      and w1.semester='" + _param._semester + "' ");
            stb.append("    group by chaircd, grade, hr_class, attendno, w1.schregno ");
            stb.append(" ) ");
        } else if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
            stb.append(",FACILITY AS ( ");
            stb.append("    SELECT W1.FACCD, W1.FACILITYNAME ");
            stb.append("    FROM   V_FACILITY_MST W1 ");
            stb.append("    WHERE  W1.YEAR='" + _param._year + "' ");
            stb.append(" ) ");
        } else if (_param._isPrintTest && SIKEN_KANTOKU.equals(_param._outDiv)) {
            stb.append(",STAFF AS ( ");
            stb.append("    SELECT W1.STAFFCD, W1.STAFFNAME ");
            stb.append("    FROM   V_STAFF_MST W1 ");
            stb.append("    WHERE  W1.YEAR='" + _param._year + "' ");
            stb.append(" ) ");
        }
        //コマ下段表示用
        if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
            stb.append(",CHAIR_CLS_MIN AS ( ");
            stb.append("    SELECT ");
            stb.append("        CHAIRCD, ");
            stb.append("        MIN(TRGTGRADE||TRGTCLASS) AS TRGTGRCL ");
            stb.append("    FROM ");
            stb.append("        CHAIR_CLS ");
            stb.append("    GROUP BY ");
            stb.append("        CHAIRCD ");
            stb.append(" ) ");
        } else {
            stb.append(",CHAIR_FAC_MAX AS ( ");
            stb.append("    SELECT ");
            stb.append("        CHAIRCD, ");
            stb.append("        MAX(FACCD) AS FACCD ");
            stb.append("    FROM ");
            stb.append("        " + _param._tableNameChairTestFacDat + " ");
            stb.append("    WHERE ");
            stb.append("        YEAR = '" + _param._year + "' ");
            stb.append("    GROUP BY ");
            stb.append("        CHAIRCD ");
            stb.append(" ) ");
        }

        //メイン
        stb.append("SELECT DISTINCT T1.DAYCD ");
        stb.append("       ,f_period(T1.PERIODCD) as PERIODCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
        } else {
            stb.append("       ,T1.SUBCLASSCD ");
        }
        stb.append("       ,L3.GRADE ");
        stb.append("       ,L3.HR_CLASS ");
        stb.append("       ,L3.HR_NAME AS TARGETCLASS ");
        if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
            stb.append("       ,L1.ATTENDNO ");
            stb.append("       ,L1.SCHREGNO ");
            stb.append("       ,L2.NAME ");
        } else if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
            stb.append("       ,L1.FACCD ");
            stb.append("       ,L1.FACILITYNAME ");
        } else if (_param._isPrintTest && SIKEN_KANTOKU.equals(_param._outDiv)) {
            stb.append("       ,L1.STAFFCD ");
            stb.append("       ,L1.STAFFNAME ");
        }
        //コマ下段表示用
        if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
            stb.append("       ,CASE WHEN T1.GROUPCD > '0000' THEN L9.HR_NAMEABBV||'*' ");
            stb.append("             ELSE L9.HR_NAMEABBV END AS GEDAN_NAME ");
        } else {
            stb.append("       ,VALUE(L5.FACILITYABBV,'') AS GEDAN_NAME ");
        }
        stb.append("       ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSABBV ");
        stb.append("       ,L7.ABBV1 ");
        stb.append("       ,T1.EXECUTEDATE ");
        stb.append("       ,E1.STUDENT_COUNT ");
        stb.append("FROM   SCH_DAT T1 ");
        if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
            stb.append("      INNER JOIN FACILITY L1 ON L1.FACCD=T1.FACCD ");
        }
        if (_param._isPrintTest && SIKEN_KANTOKU.equals(_param._outDiv)) {
            stb.append("      INNER JOIN STAFF L1 ON L1.STAFFCD=T1.STAFFCD ");
        }
        //コマ下段表示用
        if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
            stb.append("       LEFT JOIN CHAIR_CLS_MIN C9 ON C9.CHAIRCD = T1.CHAIRCD ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT L9 ON L9.YEAR = '" + _param._year + "' ");
            stb.append("                                    AND L9.SEMESTER = '" + _param._semester + "' ");
            stb.append("                                    AND L9.GRADE||L9.HR_CLASS = C9.TRGTGRCL ");
        } else {
            stb.append("       LEFT JOIN CHAIR_FAC_MAX F5 ON F5.CHAIRCD = T1.CHAIRCD ");
            stb.append("       LEFT JOIN FACILITY_MST L5 ON L5.FACCD = F5.FACCD ");
        }
        stb.append("       LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("            AND L6.CLASSCD = T1.CLASSCD ");
            stb.append("            AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("            AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("       LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ");
        stb.append("       ,CHAIR_CLS T2 ");
        stb.append("       LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR=T2.YEAR AND  ");
        stb.append("                                        L3.SEMESTER=T2.SEMESTER AND  ");
        stb.append("                                        L3.GRADE=T2.TRGTGRADE AND  ");
        stb.append("                                        L3.HR_CLASS=T2.TRGTCLASS ");
        stb.append("      INNER JOIN EXISTS_SCHNO E1 ON E1.CHAIRCD=T2.CHAIRCD AND  ");
        stb.append("                                    E1.GRADE=T2.TRGTGRADE AND  ");
        stb.append("                                    E1.HR_CLASS=T2.TRGTCLASS ");
        if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
            stb.append("      INNER JOIN CHAIR_STD L1 ON L1.CHAIRCD=T2.CHAIRCD AND  ");
            stb.append("                                 L1.GRADE=T2.TRGTGRADE AND  ");
            stb.append("                                 L1.HR_CLASS=T2.TRGTCLASS AND ");
            stb.append("                                 L1.SCHREGNO IS NOT NULL  ");
            stb.append("      LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO=L1.SCHREGNO  ");
        }
        stb.append("WHERE  T1.CHAIRCD=T2.CHAIRCD  ");
        if (_param._isPrintTest && SEITO.equals(_param._outDiv)) {
            stb.append("ORDER BY L3.GRADE,L3.HR_CLASS,L1.ATTENDNO,T1.EXECUTEDATE,PERIODCD, ");
        } else if (_param._isPrintTest && SISETU.equals(_param._outDiv)) {
            stb.append("ORDER BY L1.FACCD,T1.EXECUTEDATE,PERIODCD, ");
        } else if (_param._isPrintTest && SIKEN_KANTOKU.equals(_param._outDiv)) {
            stb.append("ORDER BY L1.STAFFCD,T1.EXECUTEDATE,PERIODCD, ");
        } else {
            stb.append("ORDER BY L3.GRADE,L3.HR_CLASS,T1.EXECUTEDATE,PERIODCD, ");
        }
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        } else {
            stb.append("       T1.SUBCLASSCD ");
        }
        return stb.toString();
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private Param createParam(final HttpServletRequest request) {
        return new Param(request);
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

    private static class Subclass {
        int _cnt = 0;
        String _flg = null;
        String _targetClass = null;
        String[] _abbv = new String[5];
        String[] _subclasscd = new String[5];
        String[] _gedan = new String[5];
        int[] _studentCount = new int[5];
    }
    
    private static class InsertOrderMap {
        List _keys = new ArrayList();
        Map _values = new HashMap();
        public void put(final Object key, final Object value) {
            if (!_keys.contains(key)) {
                _keys.add(key);
            }
            _values.put(key, value);
        }
        public Object get(final Object key) {
            if (!_keys.contains(key)) {
                return null;
            }
            return _values.get(key);
        }
        public Collection entrySet() {
            final Collection col = new ArrayList();
            for (final Iterator it = _keys.iterator(); it.hasNext();) {
                final Object key = it.next();
                col.add(new Map.Entry() {
                    public Object setValue(Object value) {
                        throw new IllegalAccessError("not implemented.");
                    }
                    
                    public Object getValue() {
                        return _values.get(key);
                    }
                    
                    public Object getKey() {
                        return key;
                    }
                });
            }
            return col;
        }
    }

    private class ExecuteDates {
        final String _executeDate;
        final String _executeMD;
        final String _executeW;
        final int _executeNo;

        ExecuteDates(final String executeDate, final String executeMD, final String executeW, final int executeNo) {
            _executeDate = executeDate;
            _executeMD = executeMD;
            _executeW = executeW;
            _executeNo = executeNo;
        }
    }

    private class Grades {
        final String _grade;
        final String _gradeName1;

        Grades(final String grade, final String gradeName1) {
            _grade = grade;
            _gradeName1 = gradeName1;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * パラメータクラス
     */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _dataDiv;
        private final boolean _isPrintTest;
        private final boolean _isPrintPro;
        private final String _testCd;
        private final String _outDiv;
        private final String _proficiencyDiv;
        private final String _proficiencyCd;
        private final String[] _grade;
        private final String _gradeIn;
        private final String _useCurriculumcd;
        private final String _ctrlDate;
        private final String _tableName;
        private final String _tableNameChairTestFacDat;

        private String _semesterName;
        private String _testName;
        private String _dateS;
        private String _dateE;
        Map _testDateMap;
        Map _gradeMap;
        private String _z010Name1;
        private String _outDivName;
        private String _outDivName2;

        Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            final String dataDiv = request.getParameter("DATA_DIV");
            _dataDiv = "1".equals(dataDiv) ? "2" : "3";
            _isPrintTest = "1".equals(dataDiv);
            _isPrintPro = !"1".equals(dataDiv);
            _testCd = request.getParameter("TESTCD");
            _outDiv = request.getParameter("OUT_DIV");
            _outDivName = "";
            _outDivName2 = "学　級";
            if (_isPrintTest) {
                if ("1".equals(_outDiv)) {
                    _outDivName = "（クラス別）";
                    _outDivName2 = "学　級";
                } else if ("2".equals(_outDiv)) {
                    _outDivName = "（生徒別）";
                    _outDivName2 = "生　徒";
                } else if ("3".equals(_outDiv)) {
                    _outDivName = "（施設別）";
                    _outDivName2 = "施　設";
                } else if ("4".equals(_outDiv)) {
                    _outDivName = "（試験監督別）";
                    _outDivName2 = "試験監督";
                }
            }
            _proficiencyDiv = request.getParameter("PROFICIENCYDIV");
            _proficiencyCd = request.getParameter("PROFICIENCYCD");
            _ctrlDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _grade = request.getParameterValues("GRADE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _tableName = request.getParameter("useTestCountflg");
            final String useTestFacility = request.getParameter("useTestFacility");
        	_tableNameChairTestFacDat = "1".equals(useTestFacility) ? "CHAIR_TEST_FAC_DAT" : "CHAIR_FAC_DAT";
            _gradeIn = createGradeIn();
        }

        private String createGradeIn() {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < _grade.length; i++) {
                if (null == _grade[i]) break;
                if (0 < i) stb.append(",");
                stb.append("'");
                stb.append(_grade[i]);
                stb.append("'");
            }
            stb.append(")");
            log.debug("学年=" + stb.toString());
            return stb.toString();
        }

        private void load(final DB2UDB db2) {
            setSemesterName(db2);
            setTestName(db2);
            setTestDateMap(db2);
            setGradeMap(db2);
            setZ010Name1(db2);
        }

        private void setSemesterName(final DB2UDB db2) {
            _semesterName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (final Exception ex) {
                log.error("学期名のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学期名:" + _semesterName);
        }

        private void setTestName(final DB2UDB db2) {
            _testName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = sqlTestName();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _testName = rs.getString("TESTNAME");
                }
            } catch (final Exception ex) {
                log.error("試験名のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("試験名:" + _testName);
        }

        private String sqlTestName() {
            final StringBuffer stb = new StringBuffer();
            if (_isPrintTest) {
                stb.append(" SELECT ");
                stb.append("     TESTITEMNAME AS TESTNAME ");
                stb.append(" FROM ");
                stb.append("     " + _tableName + " ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _year + "' ");
                stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _testCd + "' ");
                if ("TESTITEM_MST_COUNTFLG_NEW".equals(_tableName) || "TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_tableName)) {
                    stb.append("     AND SEMESTER = '" + _semester + "' ");
                }
                if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_tableName)) {
                    stb.append("     AND SCORE_DIV = '01' ");
                }
            } else {
                stb.append(" SELECT ");
                stb.append("     PROFICIENCYNAME1 AS TESTNAME ");
                stb.append(" FROM ");
                stb.append("     PROFICIENCY_MST ");
                stb.append(" WHERE ");
                stb.append("     PROFICIENCYDIV = '" + _proficiencyDiv + "' ");
                stb.append("     AND PROFICIENCYCD = '" + _proficiencyCd + "' ");
            }
            return stb.toString();
        }

        private void setTestDateMap(final DB2UDB db2) {
            _testDateMap = new TreeMap();
            _dateS = null;
            _dateE = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = sqlTestDate();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int executeNo = 0;
                while (rs.next()) {
                    final String executeDate = rs.getString("EXECUTEDATE");
                    final String executeMD = KNJ_EditDate.h_format_JP_MD(executeDate);
                    final String executeW = KNJ_EditDate.h_format_W(executeDate);

                    if (executeNo == 0) _dateS = executeDate;
                    _dateE = executeDate;
                    
                    final ExecuteDates executeDates = new ExecuteDates(executeDate, executeMD, executeW, executeNo);
                    _testDateMap.put(executeDate, executeDates);

                    executeNo++;
                }
            } catch (final Exception ex) {
                log.error("試験期間のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("試験期間開始日:" + _dateS);
            log.debug("試験期間終了日:" + _dateE);
        }

        private String sqlTestDate() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.EXECUTEDATE ");
            stb.append(" FROM ");
            stb.append("     SCH_CHR_TEST T1 ");
            stb.append("     INNER JOIN SCH_CHR_DAT T2 ");
            stb.append("        ON  T2.EXECUTEDATE = T1.EXECUTEDATE ");
            stb.append("        AND T2.PERIODCD = T1.PERIODCD ");
            stb.append("        AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("        AND T2.DATADIV = '" + _dataDiv + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
            if (_isPrintTest) {
                stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _testCd + "' ");
            } else {
                stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _proficiencyCd + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.EXECUTEDATE ");
            return stb.toString();
        }

        private void setGradeMap(final DB2UDB db2) {
            _gradeMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT GRADE,GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE IN " + _gradeIn + " ORDER BY GRADE";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String gradeName1 = rs.getString("GRADE_NAME1");

                    final Grades grades = new Grades(grade, gradeName1);
                    _gradeMap.put(grade, grades);
                }
            } catch (final Exception ex) {
                log.error("学年名称のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setZ010Name1(final DB2UDB db2) {
            _z010Name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                }
            } catch (final Exception ex) {
                log.error("名称マスタZ010のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("z010Name1:" + _z010Name1);
        }
    }


}  //クラスの括り
