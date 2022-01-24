/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ１２６Ｐ＞  観点別成績・評価チェックリスト（小学）
 *
 *	2011/01/07 nakamoto 作成日
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import jp.co.fit.vfreport.Vrw32;
import nao_package.db.DB2UDB;
//import nao_package.db.Database;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD126P {

    private static final Log log = LogFactory.getLog(KNJD126P.class);

    private static final String TESTITEM_MST_COUNTFLG_NEW_SDIV = "TESTITEM_MST_COUNTFLG_NEW_SDIV";
    private static final String SEMESTER9 = "9";

    private String _paramCtrlY;
    private String _paramCtrlS;
    private String _paramCtrlD;
    private String _paramSemester;
    private String _paramClasscd;
    private String _paramGradeHrClass;
    private String _viewcdString;
    private String _nendo;
    private String _printDateString;
    private String _z009Name1;
    private String _classname;
    private String _hrName;
    private String _paramSubclasscd;
    private String _paramUseCurriculumcd;
    private String _paramKantenHyouji;
    private String _kanten2keta;
    private String _z009;
    private String _useJviewStatusNotCd;
    private String _notUseJviewStatusText;
    private String _useTestCountflg;
    private String _useHyoukaHyouteiFlg;
    private String _useRecordDat;
	private boolean _isSeireki;

    public void svf_out(HttpServletRequest request, HttpServletResponse httpservletresponse) throws ServletException, IOException {
        log.fatal(" $Revision: 72500 $ $Date: 2020-02-20 10:34:32 +0900 (木, 20 2 2020) $");

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        HashMap hashmap = new HashMap();
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }
        try {
            _paramCtrlY = request.getParameter("CTRL_Y");//今年度
            _paramCtrlS = request.getParameter("CTRL_S");//学期 観点データ以外用 1:前期,2:後期
            _paramCtrlD = request.getParameter("CTRL_D");//生徒を抽出する日付 学籍処理日または学期終了日
            _paramSemester = request.getParameter("SEMESTER");//学期 観点データ用 1:前期,9:学年
            _paramClasscd = request.getParameter("CLASSCD");//教科
            _paramGradeHrClass = request.getParameter("GRADE_HR_CLASS");//年組
            _paramSubclasscd = request.getParameter("SUBCLASSCD");//科目
            _paramUseCurriculumcd = request.getParameter("useCurriculumcd");//プロパティ(教育課程コード)(1:教育課程対応)
            _paramKantenHyouji = request.getParameter("kantenHyouji");//プロパティ(6:観点の種類が6種類)
            _kanten2keta = "1".equals(_paramUseCurriculumcd) ? StringUtils.split(_paramClasscd, "-")[0] : _paramClasscd;//観点の頭2桁
            _z009 = request.getParameter("Z009");
            _useJviewStatusNotCd =  request.getParameter("useJviewStatus_NotHyoji"); //表示しないコード
            _useTestCountflg = request.getParameter("useTestCountflg");
            _useHyoukaHyouteiFlg = request.getParameter("useHyoukaHyouteiFlg");
        	_useRecordDat = request.getParameter("useRecordDat");
        	_isSeireki = KNJ_EditDate.isSeireki(db2);
        } catch (Exception ex) {
            log.warn("parameter error!", ex);
        }
        PrintWriter os = new PrintWriter(httpservletresponse.getOutputStream());
        httpservletresponse.setContentType("application/pdf");
        svf.VrInit();
        svf.VrSetSpoolFileStream(httpservletresponse.getOutputStream());
        boolean flag = false;
        getHeaderData(db2, svf);
        KNJServletUtils.debugParam(request, log);

        if (printMain(db2, svf, hashmap)) {
            flag = true;
        }
        log.debug("nonedata=" + flag);
        if (!flag) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "");
            svf.VrEndPage();
        }
        svf.VrQuit();
        db2.commit();
        db2.close();
        os.close();
    }

    private void getHeaderData(DB2UDB db2, Vrw32alp svf) {
        //観点の種類が6種類
        if ("6".equals(_paramKantenHyouji)) {
            svf.VrSetForm("KNJD126P_2.frm", 1);
        } else {
            svf.VrSetForm("KNJD126P.frm", 1);
        }
        if ("1".equals(_useHyoukaHyouteiFlg)) {
        	svf.VrsOut("TITLE_HYOTEI", "評価");
        }
        try {
			if(_isSeireki) {
                _nendo = _paramCtrlY + "年度"; //XXXX年度
        	} else {
                _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_paramCtrlY)) + "年度";//平成XX年度
        	}
        } catch (Exception ex) {
            log.warn("nendo get error!", ex);
        }
        try {
            String s = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
            db2.query(s);
            ResultSet rs = db2.getResultSet();
            String as1[] = new String[3];
            for (int j = 0; rs.next(); j++) {
                as1[j] = rs.getString(1);
            }

            db2.commit();
			if(_isSeireki) {
				_printDateString = KNJ_EditDate.h_format_SeirekiJP(as1[0]) + as1[1] + "時" + as1[2] + "分" + "　現在";//XXXX年XX月XX日XX時XX分　現在
			} else {
				_printDateString = KNJ_EditDate.h_format_JP(db2, as1[0]) + as1[1] + "時" + as1[2] + "分" + "　現在";//平成XX年XX月XX日XX時XX分　現在
			}
        } catch (Exception ex) {
            log.warn("ctrl_date get error!", ex);
        }
        try {
            db2.query("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + _z009 + "' AND NAMECD2 = '" + _paramSemester + "'");
            for (ResultSet rs = db2.getResultSet(); rs.next();) {
                _z009Name1 = rs.getString("NAME1");//管理者コントロール 観点名称 1:前期,9:学年
            }

            db2.commit();
        } catch (Exception ex) {
            log.warn("name1 get error!", ex);
        }
        try {
            final String sqlClassMst;
            if ("1".equals(_paramUseCurriculumcd)) {
                sqlClassMst = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND = '" + _paramClasscd + "'";
            } else {
                sqlClassMst = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD = '" + _paramClasscd + "'";
            }
            db2.query(sqlClassMst);
            for (ResultSet rs = db2.getResultSet(); rs.next();) {
                _classname = rs.getString("CLASSNAME");//教科名称
            }

            db2.commit();
        } catch (Exception ex) {
            log.warn("classname get error!", ex);
        }
        try {
            db2.query("SELECT HR_NAME FROM SCHREG_REGD_HDAT WHERE YEAR = '" + _paramCtrlY + "' AND SEMESTER = '" + _paramCtrlS + "' AND GRADE || HR_CLASS = '" + _paramGradeHrClass + "'");
            for (ResultSet rs = db2.getResultSet(); rs.next();) {
                _hrName = rs.getString("HR_NAME");//年組名称
            }

            db2.commit();
        } catch (Exception ex) {
            log.warn("chairname get error!", ex);
        }
        try {
            final String setNameCd1 = "9".equals(_paramSemester) ? "D028": "D029";
            db2.query("SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = '" + setNameCd1 + "' AND NAMECD2 = '" + _useJviewStatusNotCd + "'");
            for(ResultSet rs = db2.getResultSet(); rs.next();)
                _notUseJviewStatusText = rs.getString("ABBV1");//表示しないコード

            db2.commit();
        }
        catch(Exception exception5) {
            log.warn("d028d029 get error!", exception5);
        }
    }

    private boolean printMain(DB2UDB db2, Vrw32alp svf, Map map) {
        boolean flag = false;
        try {
            getJviewname(db2, svf, map);
            if (printMeisai(db2, svf, map)) {
                flag = true;
            }
        } catch (Exception ex) {
            log.warn("printMain read error!", ex);
        }
        return flag;
    }

    private void getJviewname(DB2UDB db2, Vrw32alp svf, Map map) {
        try {
            log.debug("Jviewname start!");
            db2.query(statementJviewname());
            ResultSet rs = db2.getResultSet();
            log.debug("Jviewname end!");
            int i = 0;
            String s = "";
            int maxlen = ("6".equals(_paramKantenHyouji)) ? 6 : 5;
            for (; rs.next(); map.put(rs.getString("VIEWCD"), String.valueOf(++i))) {
                if (i == maxlen)
                    break;
                if (i == 0)
                    _viewcdString = "";
                _viewcdString = _viewcdString + s + rs.getString("VIEWCD");//観点コード 観点状況
                s = "','";
            }

            if (0 < i) {
                _viewcdString = "('" + _viewcdString + s + _kanten2keta + "99" + "')";//観点コード 評定
                map.put(_kanten2keta + "99", "9");
            }
            log.debug("param[6]=" + _viewcdString);
            rs.close();
            db2.commit();
        } catch (Exception ex) {
            log.warn("getJviewname read error!", ex);
        }
    }

    private boolean printMeisai(DB2UDB db2, Vrw32alp svf, Map map) {
        boolean flag = false;
        try {
            log.debug("Meisai start!");
            PreparedStatement ps = db2.prepareStatement(statementMeisai());
            ResultSet rs = ps.executeQuery();
            log.debug("Meisai end!");
            int k = 0;
            String s = "0";
            while(rs.next())  {
                if (!s.equals("0") && !s.equals(rs.getString("SCHREGNO"))) {
                    k++;
                }
                if (49 < k) {
                    printHeader(svf);
                    if (_viewcdString != null) {
                        getJviewname2(db2, svf);
                    }
                    svf.VrEndPage();
                    k = 0;
                }
                printExam(svf, rs, k, map);
                s = rs.getString("SCHREGNO");
                flag = true;
            }
            if (flag) {
                printHeader(svf);
                if (_viewcdString != null) {
                    getJviewname2(db2, svf);
                }
                svf.VrEndPage();
            }
            rs.close();
            ps.close();
            db2.commit();
        }
        catch (Exception ex) {
            log.warn("printMeisai read error!", ex);
        }
        return flag;
    }

    private void printHeader(Vrw32alp svf) {
        svf.VrsOut("NENDO", _nendo);//年度
        svf.VrsOut("DATE", _printDateString);//作成日時
        svf.VrsOut("SEMESTER", _z009Name1);//学期
        svf.VrsOut("CLASS", _classname);//教科
        svf.VrsOut("CHAIRNAME", _hrName);//講座
        svf.VrsOut("CHAIRTITLE", "クラス");//年組タイトル
    }

    private void printExam(Vrw32alp svf, ResultSet rs, int i, Map map) {
        try {
            svf.VrsOutn("HR_NAME", i + 1, rs.getString("HR_NAME") + "-" + rs.getString("ATTENDNO"));//クラス
            svf.VrsOutn("NAME", i + 1, rs.getString("SCHREGNO") + "\u3000" + rs.getString("NAME_SHOW"));//氏名
            if (rs.getString("VIEWCD") != null && _viewcdString != null) {
                if (null != _notUseJviewStatusText) {
                    if (!"9".equals((String)map.get(rs.getString("VIEWCD")))) {
                        final String setStatus = (_notUseJviewStatusText.equals(rs.getString("STATUS"))) ? "": rs.getString("STATUS");
                        svf.VrsOutn("STATUS" + (String)map.get(rs.getString("VIEWCD")), i + 1, setStatus);//観点状況および評定
                    } else {
                        svf.VrsOutn("STATUS" + (String)map.get(rs.getString("VIEWCD")), i + 1, rs.getString("STATUS"));//観点状況および評定
                    }
                } else {
                    svf.VrsOutn("STATUS" + (String)map.get(rs.getString("VIEWCD")), i + 1, rs.getString("STATUS"));//観点状況および評定
                }
            }
        } catch (Exception ex) {
            log.warn("printExam read error!", ex);
        }
    }

    private void getJviewname2(DB2UDB db2, Vrw32alp svf) {
        try {
            log.debug("Jviewname2 start!");
            PreparedStatement ps = db2.prepareStatement(statementJviewname());
            ResultSet rs = ps.executeQuery();
            log.debug("Jviewname2 end!");
            int i1 = 0;
            int j1 = 1;
            String as1[] = {"1", "2", "3", "4", "5", "6"};
            int maxlen = ("6".equals(_paramKantenHyouji)) ? 6 : 5;
            while(rs.next())  {
                if (i1 == maxlen)
                    break;
                svf.VrsOut("VIEWCD1_" + String.valueOf(i1 + 1), as1[i1]);

                String viewname = rs.getString("VIEWNAME");
                int mojisuu = viewname.length();
                for (int k = 0; k < 7; k++) {
                    int s = 10 * k;
                    int e = 10 * (k + 1);
                    if (0 < k) {
                        j1++;
                        svf.VrsOutn("VIEWCD2", i1 + j1, "");
                    } else {
                        svf.VrsOutn("VIEWCD2", i1 + j1, as1[i1]);//観点コード
                    }
                    if (e < mojisuu) {
                        svf.VrsOutn("VIEWNAME", i1 + j1, viewname.substring(s, e));
                    } else {
                        svf.VrsOutn("VIEWNAME", i1 + j1, viewname.substring(s));//観点名称
                        break;
                    }
                }

                i1++;
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            log.warn("getJviewname2 read error!", ex);
        }
    }

    private String statementMeisai() {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH SCHNO AS ( ");
            stb.append("SELECT ");
            stb.append("    t1.hr_name, t2.grade, t2.hr_class, t2.attendno, t2.schregno, t3.name_show ");
            stb.append("FROM ");
            stb.append("    schreg_regd_hdat t1, ");
            stb.append("    schreg_regd_dat t2, ");
            stb.append("    schreg_base_mst t3 ");
            stb.append("WHERE ");
            stb.append("    t1.year      = t2.year AND ");
            stb.append("    t1.semester  = t2.semester AND ");
            stb.append("    t1.grade     = t2.grade AND ");
            stb.append("    t1.hr_class  = t2.hr_class AND ");
            stb.append("    t1.grade || t1.hr_class  = '" + _paramGradeHrClass + "' AND ");
            stb.append("    t2.year      = '" + _paramCtrlY + "' AND ");
            stb.append("    t2.semester  = '" + _paramCtrlS + "' AND ");
            stb.append("    t2.schregno  = t3.schregno ");
            stb.append(" ) ");
            stb.append(",JVIEWSTAT AS ( ");
            stb.append("SELECT ");
            stb.append("    schregno, viewcd, status ");
            stb.append("FROM ");
            stb.append("    jviewstat_record_dat ");
            stb.append("WHERE ");
            stb.append("    year = '" + _paramCtrlY + "' AND ");
            stb.append("    semester = '" + _paramSemester + "' AND ");
            //教育課程対応
            if ("1".equals(_paramUseCurriculumcd)) {
                stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _paramSubclasscd + "' AND ");
            } else {
                stb.append("    SUBCLASSCD = '" + _paramSubclasscd + "' AND ");
            }
            if (_viewcdString != null) {
                stb.append("    viewcd in " + _viewcdString + " AND ");
            } else {
                stb.append("    SUBSTR(VIEWCD,1,2) = '" + _kanten2keta + "' AND ");
            }
            stb.append("    viewcd not like '%99' AND ");
            stb.append("    status is not null ");
            stb.append("UNION ALL ");
            stb.append("SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    '" + _kanten2keta + "99' AS VIEWCD, ");
            if ("KIN_RECORD_DAT".equals(_useRecordDat)) {
                if ("1".equals(_paramSemester)) {
                    stb.append("    RTRIM(SEM1_ASSESS) AS STATUS ");
                } else if ("2".equals(_paramSemester)) {
                    stb.append("    RTRIM(SEM2_ASSESS) AS STATUS ");
                } else if ("3".equals(_paramSemester)) {
                    stb.append("    RTRIM(SEM3_ASSESS) AS STATUS ");
                } else {
                    stb.append("    RTRIM(GRADE_ASSESS) AS STATUS ");
                }
                stb.append("FROM ");
                stb.append("    KIN_RECORD_DAT ");
                stb.append("WHERE ");
                stb.append("    YEAR = '" + _paramCtrlY + "' AND ");
                stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _paramSubclasscd + "' AND ");
                if ("1".equals(_paramSemester)) {
                	stb.append("    SEM1_ASSESS IS NOT NULL ");
                } else if ("2".equals(_paramSemester)) {
                	stb.append("    SEM2_ASSESS IS NOT NULL ");
                } else if ("3".equals(_paramSemester)) {
                	stb.append("    SEM3_ASSESS IS NOT NULL ");
                } else {
                	stb.append("    GRADE_ASSESS IS NOT NULL ");
                }
            } else {
                if (TESTITEM_MST_COUNTFLG_NEW_SDIV.equals(_useTestCountflg)) {
                    stb.append("    RTRIM(CHAR(SCORE)) AS STATUS ");
                } else {
                    stb.append("    RTRIM(CHAR(VALUE)) AS STATUS ");
                }
                stb.append("FROM ");
                stb.append("    RECORD_SCORE_DAT ");
                stb.append("WHERE ");
                stb.append("    YEAR = '" + _paramCtrlY + "' AND ");
                stb.append("    SEMESTER = '" + _paramSemester + "' AND ");
                stb.append("    TESTKINDCD = '99' AND ");
                stb.append("    TESTITEMCD = '00' AND ");
                if (TESTITEM_MST_COUNTFLG_NEW_SDIV.equals(_useTestCountflg)) {
                    if ("2".equals(_useHyoukaHyouteiFlg)) {
                        stb.append("    SCORE_DIV  = '09' AND ");
                    } else {
                        if (SEMESTER9.equals(_paramSemester) && !"1".equals(_useHyoukaHyouteiFlg)) {
                            stb.append("    SCORE_DIV  = '09' AND ");
                        } else {
                            stb.append("    SCORE_DIV  = '08' AND ");
                        }
                    }
                } else {
                    stb.append("    SCORE_DIV  = '00' AND ");
                }
                //教育課程対応
                if ("1".equals(_paramUseCurriculumcd)) {
                    stb.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _paramSubclasscd + "' AND ");
                } else {
                    stb.append("    SUBCLASSCD = '" + _paramSubclasscd + "' AND ");
                }
                if (TESTITEM_MST_COUNTFLG_NEW_SDIV.equals(_useTestCountflg)) {
                    stb.append("    SCORE IS NOT NULL ");
                } else {
                    stb.append("    VALUE IS NOT NULL ");
                }
            }
            stb.append(" ) ");
            stb.append("SELECT  TBL1.hr_name,TBL1.grade,TBL1.hr_class,TBL1.attendno,TBL1.schregno,TBL1.name_show, ");
            stb.append("        TBL2.viewcd, TBL2.status ");
            stb.append("FROM    SCHNO TBL1 ");
            stb.append("        LEFT JOIN JVIEWSTAT TBL2 ON TBL2.SCHREGNO = TBL1.SCHREGNO ");
            stb.append("ORDER BY TBL1.grade, TBL1.hr_class, TBL1.attendno, TBL2.viewcd ");
        } catch (Exception ex) {
            log.warn("statementMeisai error!", ex);
        }
        log.info("MeisaiSQL:" + stb);
        return stb.toString();
    }

    private String statementJviewname() {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT T1.VIEWCD, T2.VIEWNAME ");
            stb.append("FROM   JVIEWNAME_GRADE_YDAT T1, JVIEWNAME_GRADE_MST T2 ");
            stb.append("WHERE  T1.YEAR = '" + _paramCtrlY + "' AND ");
            stb.append("       T1.GRADE = '" + _paramGradeHrClass.substring(0, 2) + "' AND ");
            stb.append("       T1.GRADE = T2.GRADE AND ");
            //教育課程対応
            if ("1".equals(_paramUseCurriculumcd)) {
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _paramSubclasscd + "' AND ");
                stb.append("       T1.CLASSCD = T2.CLASSCD AND ");
                stb.append("       T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ");
                stb.append("       T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ");
                stb.append("       T1.SUBCLASSCD = T2.SUBCLASSCD AND ");
            } else {
                stb.append("       T1.SUBCLASSCD = '" + _paramSubclasscd + "' AND ");
                stb.append("       T1.SUBCLASSCD = T2.SUBCLASSCD AND ");
            }
            stb.append("       T1.VIEWCD = T2.VIEWCD AND ");
            stb.append("       SUBSTR(T1.VIEWCD,1,2) = '" + _kanten2keta + "' ");
            stb.append("ORDER BY T1.VIEWCD ");
        } catch (Exception ex) {
            log.warn("statementJviewname error!", ex);
        }
        return stb.toString();
    }
}
