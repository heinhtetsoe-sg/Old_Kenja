// kanji=漢字
/*
 * $Id: 0d01c920d1a3a7d85c319edc0527e9ae0a544392 $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD185I {
    private static final Log log = LogFactory.getLog(KNJD185I.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String SEM1_TESTITEMCD = "19900";
    private static final String SEM2_TESTITEMCD = "29900";
    private static final String SEM9_TESTITEMCD = "99900";
    private static final String SEM1_HYOUKACD = SEM1_TESTITEMCD+"08";
    private static final String SEM2_HYOUKACD = SEM2_TESTITEMCD+"08";
    private static final String SEM9_HYOUTEICD = SEM9_TESTITEMCD + "08";
    private static final String TESTCD_GAKUNEN_HYOKA = "990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "990009";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            outputPdf(svf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    public void outputPdf(
            final Vrw32alp svf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }

    protected void printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) {
    	getInfo(db2, param);

    	//生徒毎に出力する。
    	for (final Iterator it = param._outputinf._schreglist.iterator(); it.hasNext();) {
    		StudentInfo infobj = (StudentInfo) it.next();

    		//表紙を出力する。
    		svf.VrSetForm("KNJD185I_1.frm", 1);

    		svf.VrsOut("SCHOOL_NAME", param._outputinf._schoolname1);
            svf.VrsOut("SCHOOL_KIND", param._outputinf._departmentname);
            svf.VrsOut("HR_NAME", infobj._hrname + String.valueOf(Integer.parseInt(infobj._attendno)) + "番 ");
            final int namelen = KNJ_EditEdit.getMS932ByteLength(infobj._name);
            final String namefield = namelen > 30 ? "NAME3" : namelen > 20 ? "NAME2" : "NAME1";
            svf.VrsOut(namefield, infobj._name);
            svf.VrsOut("SCHOOL_LOGO", param.getSLOGOFilePath());

            svf.VrEndPage();
    		//詳細を出力する。
            if ("J".equals(param._schoolKind)) {
            	svf.VrSetForm("KNJD185I_2_1.frm", 1);
    		    outputDetail1(db2, svf, param, infobj);
            } else {
            	svf.VrSetForm("KNJD185I_2_2.frm", 1);
    		    outputDetail2(db2, svf, param, infobj);
            }
            _hasData = true;
            svf.VrEndPage();
    	}
    }

    private void outputDetail1(final DB2UDB db2, final Vrw32alp svf, final Param param, final StudentInfo infobj) {
	    svf.VrsOut("YEAR", param._year + "年度");
	    svf.VrsOut("SCHOOL_KIND", param._outputinf._departmentname);
	    svf.VrsOut("HR_NAME", infobj._hrname + String.valueOf(Integer.parseInt(infobj._attendno)) + "番");
        final int namelen = KNJ_EditEdit.getMS932ByteLength(infobj._name);
        final String namefield = namelen > 30 ? "NAME3" : namelen > 26 ? "NAME2" : "NAME1";
        svf.VrsOut(namefield, infobj._name);
        
        //学習の記録
        int reccnt = 0;
        for (final Iterator it = infobj._subclassList.iterator(); it.hasNext();) {
        	Subclass subclsobj = (Subclass) it.next();
        	if (subclsobj._subclasscd.indexOf(SUBCLASSCD999999) >= 0) continue;
        	reccnt += 1;
        	svf.VrsOutn("SUBCLASS_NAME", reccnt, subclsobj._subclassname);
            //前期評価
        	SubclassDetail subclsdet1 = (SubclassDetail)subclsobj._detailMap.get(SEM1_TESTITEMCD);
        	if (null != subclsdet1) {
        		if (NumberUtils.isDigits(subclsdet1._score) && Integer.parseInt(subclsdet1._score) == 1) {
        			svf.VrsOutn("VALUE_RENARK1", reccnt, "*");
        		}
        		svf.VrsOutn("VALUE1", reccnt, subclsdet1._score);
        	}
        	//学年評価
        	if (!"1".equals(param._semester)) {
            	SubclassDetail subclsdet2 = (SubclassDetail)subclsobj._detailMap.get(SEM9_TESTITEMCD);
            	if (null != subclsdet2) {
            		if (NumberUtils.isDigits(subclsdet2._score) && Integer.parseInt(subclsdet2._score) == 1) {
            			svf.VrsOutn("VALUE_RENARK2", reccnt, "*");
            		}
            		svf.VrsOutn("VALUE2", reccnt, subclsdet2._score);
            	}
        	}
        }

        //出欠の記録
        for (int ii = 0;ii < infobj._attendSemesDatList.size();ii++) {
        	AttendSemesDat semsattend = (AttendSemesDat)infobj._attendSemesDatList.get(ii);
        	int prm_semesval = Integer.parseInt(param._semester);
        	int sms_semesval = Integer.parseInt(semsattend._semester);
        	if (prm_semesval == 1 && sms_semesval < prm_semesval) {
        		continue;
        	} else if (sms_semesval != 9 && sms_semesval > 2) {
        		continue;
        	}
        	final int col = (sms_semesval == 9 ? 3 : sms_semesval);
        	svf.VrsOutn("LESSON", col, String.valueOf(semsattend._lesson));
        	svf.VrsOutn("SUSPEND", col, String.valueOf(semsattend._suspend+semsattend._mourning));
        	svf.VrsOutn("MUST", col, String.valueOf(semsattend._mlesson));
        	svf.VrsOutn("NOTICE", col, String.valueOf(semsattend._sick));
        	svf.VrsOutn("ATTEND", col, String.valueOf(semsattend._present));
        	svf.VrsOutn("LATE", col, String.valueOf(semsattend._late));
        	svf.VrsOutn("EARLY", col, String.valueOf(semsattend._early));
        	if (semsattend._tochuKekka < 10) {
        	    svf.VrsOutn("KEKKA", col, String.valueOf(semsattend._tochuKekka));
        	} else {
        	    svf.VrsOutn("KEKKA", col, "e");
        	}
        }

        //特別活動・資格などの記録
        String[] remarktoken = KNJ_EditEdit.get_token(infobj._specialremark, 76, 5);
        int putactcnt = 0;
        for (int kk = 0; kk < 5; kk++) {
            if (remarktoken != null && remarktoken.length > putactcnt) svf.VrsOutn("SP_ACT", kk+1, remarktoken[putactcnt]);
            putactcnt++;
        }

        //担任からの通信
        int outcnt = 0;
        //成績データ取得時に取得する情報のため、データが無ければ出力できないので、チェックしている。
        if (infobj._subclassList.size() > 0) {
            //「出欠の記録（「」）は、「」締めです。」
            Semester semname = (Semester)param._semesterMap.get(param._semester);
            String outcommstr = "";
            outcommstr = "出欠の記録（" + semname._semestername + "）は「" + KNJ_EditDate.h_format_JP_MD(infobj._endday) + "」締めです。";
            svf.VrsOutn("TEACHER_COMM", 1, outcommstr);
            outcnt++;

            //「評定（「」）のクラス順位は「」、学年順位は「」です。」
            byte nullchkflg = chkOutCommRankNullChk(infobj._detailInfo);
            if (nullchkflg < 0x0F) {
                outcommstr  = "評定（" + semname._semestername + "）の";
                if ((nullchkflg & 0x03) == 0x00) {
                    outcommstr += "クラス順位は「";
                    outcommstr += infobj._detailInfo._classavgrank + "/" + infobj._detailInfo._classcount;// + "（" + infobj._detailInfo._classavgrank+ "）";
                }
                if (nullchkflg == 0x00) {
                    outcommstr += "」、";
                }
                if ((nullchkflg & 0x0C) == 0x00) {
                    outcommstr += "学年順位は「";
                    outcommstr += infobj._detailInfo._gradeavgrank + "/" + infobj._detailInfo._gradecount;// + "（" + infobj._detailInfo._gradeavgrank+ "）";
                }
                outcommstr += "」です。";
                svf.VrsOutn("TEACHER_COMM", 2, outcommstr);
                outcnt++;
            }
        }

        //出力残はフリー。
        String[] teachercommtoken = KNJ_EditEdit.get_token(infobj._teacherremark, 76, 5);
        int putcnt = 0;
        for (int kk = outcnt;kk < 5;kk++) {
            if (teachercommtoken != null && teachercommtoken.length > putcnt) svf.VrsOutn("TEACHER_COMM", kk+1, teachercommtoken[putcnt]);
            putcnt++;
        }

        //フッタ情報
        svf.VrsOut("TEACHER_NAME", param._outputinf._hrjobname + infobj._staffname);
        svf.VrsOut("SCHOOL_NAME", param._outputinf._schoolname2);
        svf.VrsOut("JOB_NAME", param._outputinf._jobname + param._outputinf._principalname);
        svf.VrsOut("SCHOOL_STAMP", param.getSSTMPJ_FilePath());
    }

    private byte chkOutCommRankNullChk(final StudentDetailInfo chkobj) {
    	byte errflg = 0;
    	if (chkobj._classavgrank == null) {
    		errflg += 1;
    	}
    	if (chkobj._classcount == null) {
    		errflg += 2;
    	}
    	if (chkobj._gradeavgrank == null) {
    		errflg += 4;
    	}
    	if (chkobj._gradecount == null) {
    		errflg += 8;
    	}
    	return errflg;
    }

    private void outputDetail2(final DB2UDB db2, final Vrw32alp svf, final Param param, final StudentInfo infobj) {
	    svf.VrsOut("YEAR", param._year + "年度");
	    svf.VrsOut("SCHOOL_KIND", param._outputinf._departmentname);
	    svf.VrsOut("HR_NAME", infobj._hrname + String.valueOf(Integer.parseInt(infobj._attendno)) + "番");
        final int namelen = KNJ_EditEdit.getMS932ByteLength(infobj._name);
        final String namefield = namelen > 30 ? "NAME3" : namelen > 26 ? "NAME2" : "NAME1";
        svf.VrsOut(namefield, infobj._name);

        String beforeclassname = "";
        //学習の記録
        int reccnt = 0;
        for (final Iterator it = infobj._subclassList.iterator(); it.hasNext();) {
        	Subclass subclsobj = (Subclass) it.next();
        	if (subclsobj._subclasscd.indexOf(SUBCLASSCD999999) >= 0) continue;
        	reccnt += 1;
        	if (null != subclsobj._classname && !subclsobj._classname.equals(beforeclassname)) {
        		svf.VrsOutn("CLASS_NAME", reccnt, subclsobj._classname);
        	}
        	beforeclassname = subclsobj._classname;
            final int subclsnamelen = KNJ_EditEdit.getMS932ByteLength(subclsobj._subclassname);
            final String subclsnamefield = subclsnamelen > 18 ? "SUBCLASS_NAME2" : "SUBCLASS_NAME1";
            svf.VrsOutn(subclsnamefield, reccnt, subclsobj._subclassname);

            //前期評価
        	SubclassDetail subclsdet1 = (SubclassDetail)subclsobj._detailMap.get(SEM1_TESTITEMCD);
        	if (subclsdet1 != null) {
        	    svf.VrsOutn("VALUE1", reccnt, ("1".equals(subclsdet1._score) ? "*" : "") + StringUtils.defaultString(subclsdet1._score));
        	    svf.VrsOutn("KEKKA1", reccnt, subclsdet1._kekka);
        	}

        	//後期(学年末評価)評価
        	if (!"1".equals(param._semester)) {
            	SubclassDetail subclsdet2 = (SubclassDetail)subclsobj._detailMap.get(SEM2_TESTITEMCD);
        	    if ("9".equals(param._semester)) {
        	    	subclsdet2 = (SubclassDetail)subclsobj._detailMap.get(SEM9_TESTITEMCD);
        	    }
            	if (subclsdet2 != null) {
            	    svf.VrsOutn("VALUE2", reccnt, ("1".equals(subclsdet2._score) ? "*" : "") + StringUtils.defaultString(subclsdet2._score));
            	    svf.VrsOutn("KEKKA2", reccnt, subclsdet2._kekka);
            	}
        	}

        	//学年評価
        	if ("9".equals(param._semester)) {
                SubclassDetail subclsdet9 = (SubclassDetail)subclsobj._detailMap.get(SEM9_TESTITEMCD);
        		if (subclsdet9 != null) {
                    svf.VrsOutn("CREDIT", reccnt, subclsdet9._getCredit);
        		}
        	}
        }

        //出欠の記録
        for (int ii = 0;ii < infobj._attendSemesDatList.size();ii++) {
        	AttendSemesDat semsattend = (AttendSemesDat)infobj._attendSemesDatList.get(ii);
        	int prm_semesval = Integer.parseInt(param._semester);
        	int sms_semesval = Integer.parseInt(semsattend._semester);
        	if (prm_semesval == 1 && sms_semesval < prm_semesval) {
        		continue;
        	} else if (sms_semesval != 9 && sms_semesval > 2) {
        		continue;
        	}
        	final int col = (sms_semesval == 9 ? 3 : sms_semesval);
        	svf.VrsOutn("LESSON", col, String.valueOf(semsattend._lesson));
        	svf.VrsOutn("SUSPEND", col, String.valueOf(semsattend._suspend+semsattend._mourning));
        	svf.VrsOutn("MUST", col, String.valueOf(semsattend._mlesson));
        	svf.VrsOutn("NOTICE", col, String.valueOf(semsattend._sick));
        	svf.VrsOutn("ATTEND", col, String.valueOf(semsattend._present));
        	svf.VrsOutn("LATE", col, String.valueOf(semsattend._late));
        	svf.VrsOutn("EARLY", col, String.valueOf(semsattend._early));
        	if (semsattend._tochuKekka < 10) {
        	    svf.VrsOutn("KEKKA", col, String.valueOf(semsattend._tochuKekka));
        	} else {
        	    svf.VrsOutn("KEKKA", col, "e");
        	}
        }

        //特別活動・資格などの記録
        String[] remarktoken = KNJ_EditEdit.get_token(infobj._specialremark, 76, 5);
        int putactcnt = 0;
        for (int kk = 0; kk < 5; kk++) {
            if (remarktoken != null && remarktoken.length > putactcnt) svf.VrsOutn("SP_ACT", kk+1, remarktoken[putactcnt]);
            putactcnt++;
        }

        //担任からの通信
        int outcnt = 0;
        if (infobj._subclassList.size() > 0) {
            //「出欠の記録（「」）は、「」締めです。」
            Semester semname = (Semester)param._semesterMap.get(param._semester);
            String outcommstr = "";
            outcommstr = "出欠の記録（" + semname._semestername + "）は「" + KNJ_EditDate.h_format_JP_MD(infobj._endday) + "」締めです。";
            svf.VrsOutn("TEACHER_COMM", 1, outcommstr);
            outcnt++;

            //「評定（「」）のクラス順位は「」、学年順位は「」です。」
            byte nullchkflg = chkOutCommRankNullChk(infobj._detailInfo);
            if (nullchkflg < 0x0F) {
                outcommstr  = "評定（" + semname._semestername + "）の";
                if ((nullchkflg & 0x03) == 0x00) {
                outcommstr += "クラス順位は「";
                outcommstr += infobj._detailInfo._classavgrank + "/" + infobj._detailInfo._classcount;// + "（" + infobj._detailInfo._classavgrank+ "）";
                }
                if (nullchkflg == 0x00) {
                    outcommstr += "」、";
                }
                if ((nullchkflg & 0x0C) == 0x00) {
                    outcommstr += "学年順位は「";
                    outcommstr += infobj._detailInfo._gradeavgrank + "/" + infobj._detailInfo._gradecount;// + "（" + infobj._detailInfo._gradeavgrank+ "）";
                }
                outcommstr += "」です。";
                svf.VrsOutn("TEACHER_COMM", 2, outcommstr);
                outcnt++;
            }
        }

        //残はフリー。
        String[] teachercommtoken = KNJ_EditEdit.get_token(infobj._teacherremark, 76, 5);
        int putcnt = 0;
        for (int kk = outcnt;kk < 5;kk++) {
            if (teachercommtoken != null && teachercommtoken.length > putcnt) svf.VrsOutn("TEACHER_COMM", kk+1, teachercommtoken[putcnt]);
            putcnt++;
        }

        //フッタ情報
        svf.VrsOut("TEACHER_NAME", param._outputinf._hrjobname + infobj._staffname);
        svf.VrsOut("SCHOOL_NAME", param._outputinf._schoolname2);
        svf.VrsOut("JOB_NAME", param._outputinf._jobname + param._outputinf._principalname);
        svf.VrsOut("SCHOOL_STAMP", param.getSSTMPH_FilePath());
    }

    private void getInfo(final DB2UDB db2, final Param param) {
        //生徒の基本情報を取得
    	getStudentBasicInfo(db2, param);

		//成績、出欠の記録を取得
		getStudentDetail(db2, param);

    	//特別活動・資格情報を取得
    	getSpecialActInfo(db2, param);
    }

    private void getStudentBasicInfo(final DB2UDB db2, final Param param) {
    	String sql = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            sql = sqlgetStudentBasicInfo(param);
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String schregno = rs.getString("SCHREGNO");
            	final String grade = rs.getString("GRADE");
            	final String gradename = rs.getString("GRADENAME");
            	final String hrclass = rs.getString("HR_CLASS");
            	final String hrname = rs.getString("HR_NAME");
            	final String attendno = rs.getString("ATTENDNO");
            	final String name = rs.getString("NAME");
            	final String staffname = rs.getString("STAFFNAME");
            	StudentInfo addwk = new StudentInfo(schregno, grade, gradename, hrclass, hrname, attendno, name, staffname);
            	param._outputinf._schreglist.add(addwk);
            	param._outputinf._schregmap.put(schregno, addwk);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private String sqlgetStudentBasicInfo(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   SRD.SCHREGNO,");
        stb.append("   SRD.GRADE, ");
        stb.append("   SRG.GRADE_NAME1 AS GRADENAME, ");
        stb.append("   SRD.HR_CLASS, ");
        stb.append("   SRH.HR_NAME, ");
        stb.append("   SRD.ATTENDNO, ");
        stb.append("   SBM.NAME, ");
        stb.append("   SM.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT SRD ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("     ON SBM.SCHREGNO = SRD.SCHREGNO");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT SRG ");
        stb.append("     ON  SRG.YEAR = SRD.YEAR ");
        stb.append("     AND SRG.GRADE = SRD.GRADE ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("     ON SRH.YEAR = SRD.YEAR ");
        stb.append("     AND SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("     AND SRH.GRADE = SRD.GRADE ");
        stb.append("     AND SRH.HR_CLASS = SRD.HR_CLASS ");
        stb.append("   LEFT JOIN STAFF_MST SM ");
        stb.append("     ON SM.STAFFCD = SRH.TR_CD1 ");
        stb.append(" WHERE ");
        stb.append("   SRD.YEAR = '" + param._year + "' ");
        if ("9".equals(param._semester)) {
            stb.append("   AND SRD.SEMESTER = '" + param._ctrlSeme + "' ");
        } else {
            stb.append("   AND SRD.SEMESTER = '" + param._semester + "' ");
        }
        stb.append("   AND SRD.SCHREGNO IN " + param._selectedInState + " ");
        stb.append(" ORDER BY ");
        stb.append("   SRD.GRADE, SRD.HR_CLASS, SRD.ATTENDNO ");

        return stb.toString();
    }

    private void getStudentDetail(final DB2UDB db2, final Param param) {
    	//生徒のリストはBASEで取得済み。
    	//setAttendSemesDatList、getSubclassList、setAttendSubclassListの処理を行う。
        AttendSemesDat.setAttendSemesDatList(db2, param, param._outputinf._schregmap);

        for (final Iterator it = param._outputinf._schregmap.keySet().iterator(); it.hasNext();) {
        	final String kstr = (String)it.next();
            final StudentInfo student = (StudentInfo)param._outputinf._schregmap.get(kstr);
            student._subclassList = Subclass.getSubclassList(db2, param, student);
        }
        //高校の場合のみ、欠課時数を取得する。
    	if ("H".equals(param._schoolKind)) {
            Subclass.setAttendSubclassList(db2, param, param._outputinf._schregmap);
    	}
    	getAttendLimitDay(db2, param);

        return;
    }

	//特別活動・資格情報、担任からの通信を取得
	private void getSpecialActInfo(final DB2UDB db2, final Param param) {
        getClubStr(db2, param);
        getCommitteeStr(db2, param);
        getQualifyStr(db2, param);
        getRemarkStr(db2, param);
	}

    private void getClubStr (final DB2UDB db2, final Param param) {
        String sql = sqlClubStr(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!"".equals(rs.getString("CLUBNAME"))) {
                	String schregno = rs.getString("SCHREGNO");
                	StudentInfo setwk = (StudentInfo)param._outputinf._schregmap.get(schregno);
                	setwk._clubname.add(rs.getString("CLUBNAME"));
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String sqlClubStr(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  T3.SCHREGNO, ");
        stb.append("  CASE WHEN CLUB.CLUBNAME IS NULL THEN '' ELSE CLUB.CLUBNAME END AS CLUBNAME ");
        stb.append(" FROM  SCHREG_CLUB_HIST_DAT T3 ");
        stb.append("  LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("    ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("       AND T2.YEAR = '" + param._year + "' ");
        stb.append("       AND T2.SEMESTER IN ('" + (SEMEALL.equals(param._semester) ? param._lastSemester : param._semester) + "', '9') ");
        stb.append("  LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("    ON T2.YEAR = T4.YEAR ");
        stb.append("    AND T2.GRADE = T4.GRADE ");
        stb.append("  LEFT JOIN CLUB_MST CLUB ");
        stb.append("    ON T3.CLUBCD = CLUB.CLUBCD ");
        stb.append("    AND CLUB.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("  LEFT JOIN NAME_MST J001 ON J001.NAMECD1 = 'J001' ");
        stb.append("    AND T3.EXECUTIVECD = J001.NAMECD2 ");
        stb.append(" WHERE  ");
        stb.append("   T3.SCHREGNO IN " + param._selectedInState + " ");
        stb.append("   AND T2.SCHREGNO IS NOT NULL ");
        stb.append("   AND (T3.EDATE IS NULL OR '" + param._date + "' < T3.EDATE ) ");
        stb.append("   AND T3.SDATE < '" + param._date + "' ");
        stb.append("   AND T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append("   AND T4.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T3.SCHREGNO ");
        return stb.toString();
    }

    private void getCommitteeStr (final DB2UDB db2, final Param param) {
        String sql = sqlCommitteeStr(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String schregno = "";
            StudentInfo setwk = null;
            while (rs.next()) {
                if (!"".equals(rs.getString("COMMITTEENAME"))) {
                	if (!schregno.equals(rs.getString("SCHREGNO"))) {
                	    schregno = rs.getString("SCHREGNO");
                	    setwk = (StudentInfo)param._outputinf._schregmap.get(schregno);
                	}
                	if (setwk != null) {
                		setwk._committeename.add(rs.getString("COMMITTEENAME"));
                	}
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String sqlCommitteeStr(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  T1.SCHREGNO, ");
        stb.append("  CMST.COMMITTEENAME AS COMMITTEENAME ");
        stb.append(" FROM ");
        stb.append("  SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append("  LEFT JOIN COMMITTEE_MST CMST ");
        stb.append("    ON  T1.COMMITTEE_FLG = CMST.COMMITTEE_FLG ");
        stb.append("    AND T1.SCHOOL_KIND = CMST.SCHOOL_KIND ");
        stb.append("    AND T1.COMMITTEECD = CMST.COMMITTEECD ");
        stb.append("  LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("    ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("    AND T1.YEAR = T2.YEAR ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T1.GRADE = T2.GRADE ");
        stb.append("  LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("    ON T1.YEAR = T4.YEAR ");
        stb.append("    AND T1.GRADE = T4.GRADE ");
        stb.append("    AND T1.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append("  LEFT JOIN NAME_MST N1 ");
        stb.append("    ON N1.NAMECD2 = T1.COMMITTEE_FLG ");
        stb.append("    AND N1.NAMECD1 = 'J003' ");
        stb.append("  LEFT JOIN NAME_MST N2 ");
        stb.append("    ON N2.NAMECD2 = T1.EXECUTIVECD ");
        stb.append("    AND N2.NAMECD1 = 'J002' ");
        stb.append(" WHERE  ");
        stb.append("  T1.SCHREGNO IN " + param._selectedInState + " ");
        stb.append("  AND T2.SCHREGNO IS NOT NULL ");
        stb.append("  AND T1.YEAR = '" + param._year + "' ");
        stb.append("  AND T1.SEMESTER IN ('" + (SEMEALL.equals(param._semester) ? param._lastSemester : param._semester) + "', '9') ");
        stb.append("  AND T1.SEQ = (SELECT MAX(SEQ) FROM SCHREG_COMMITTEE_HIST_DAT TW1 WHERE TW1.YEAR = T1.YEAR AND TW1.SEMESTER = T1.SEMESTER AND TW1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("  AND T4.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append(" order by ");
        stb.append("  T1.SCHREGNO ");
        return stb.toString();
    }

    private void getQualifyStr (final DB2UDB db2, final Param param) {
        String sql = sqlQualifyStr(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!"".equals(rs.getString("QUALIFIED_NAME"))) {
                	String schregno = rs.getString("SCHREGNO");
                	StudentInfo setwk = (StudentInfo)param._outputinf._schregmap.get(schregno);
                	if (null == setwk) {
                		log.warn(" no info " + schregno);
                	} else {
                		setwk._sikakuname.add(rs.getString("QUALIFIED_NAME") + StringUtils.defaultString(rs.getString("RANKNAME"), ""));
                	}
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String sqlQualifyStr(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.QUALIFIED_CD, ");
        stb.append("   T2.QUALIFIED_NAME, ");
        stb.append("   H312.NAME1 AS RANKNAME");
        stb.append(" FROM ");
        stb.append("   SCHREG_QUALIFIED_HOBBY_DAT T1 ");
        stb.append("   LEFT JOIN QUALIFIED_MST T2 ");
        stb.append("     ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ");
        stb.append("   LEFT JOIN NAME_MST H312 ");
        stb.append("     ON  NAMECD1 = 'H312' ");
        stb.append("     AND NAMECD2 = T1.RANK ");
        stb.append(" WHERE ");
        //※登録した時の年度、校種は紐づけなくても良い(このデータを毎年登録しない)はずなので、無視。
        //その代わり、グループ化して重複しても良いようにしておく。名称の紐づく物のみ取得する。
        stb.append("   T1.SCHREGNO IN " + param._selectedInState + " ");
        stb.append("   AND T1.REGDDATE <= '" + param._date + "' ");
        stb.append("   AND T2.QUALIFIED_NAME IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.QUALIFIED_CD, ");
        stb.append("   T2.QUALIFIED_NAME, ");
        stb.append("   H312.NAME1 ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SCHREGNO");

        return stb.toString();
    }

    private void getRemarkStr(final DB2UDB db2, final Param param) {
        String sql = sqlgetSpecialActRemarkStr(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	String schregno = rs.getString("SCHREGNO");
            	StudentInfo setwk = (StudentInfo)param._outputinf._schregmap.get(schregno);
               	setwk._specialremark = StringUtils.defaultString(rs.getString("SPECIALACTREMARK"), "");
               	setwk._teacherremark = StringUtils.defaultString(rs.getString("COMMUNICATION"), "");
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String sqlgetSpecialActRemarkStr(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.SPECIALACTREMARK, ");
        stb.append("   T1.COMMUNICATION");
        stb.append(" FROM ");
        stb.append("   HREPORTREMARK_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._lastSemester : param._semester) +  "' ");
        stb.append("   AND T1.SCHREGNO IN " + param._selectedInState + " ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SCHREGNO");

        return stb.toString();
    }

    private void getAttendLimitDay(final DB2UDB db2, final Param param) {
        String sql = sqlgetAttendLimitDay(param);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!"".equals(rs.getString("ENDDAY"))) {
                	String schregno = rs.getString("SCHREGNO");
                	StudentInfo setwk = (StudentInfo)param._outputinf._schregmap.get(schregno);
                	if (null == setwk) {
                		log.warn(" no info " + schregno);
                	} else {
                		setwk._endday = rs.getString("ENDDAY");
                	}
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String sqlgetAttendLimitDay(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("         MAX((CASE WHEN MONTH BETWEEN '01' AND '03' ");
        stb.append("                   THEN RTRIM(CHAR(INT(T1.YEAR) + 1)) ");
        stb.append("                   ELSE T1.YEAR ");
        stb.append("              END ) || '-' || T1.MONTH || '-' || T1.APPOINTED_DAY) AS ENDDAY ");
        stb.append("     FROM ");
        stb.append("         ATTEND_SEMES_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + param._year + "' ");
        stb.append("         AND SCHREGNO IN " + param._selectedInState + " ");
        stb.append("         AND (CASE WHEN MONTH BETWEEN '01' AND '03' ");
        stb.append("                   THEN RTRIM(CHAR(INT(T1.YEAR) + 1)) ");
        stb.append("                   ELSE T1.YEAR ");
        stb.append("              END ) || '-' || T1.MONTH || '-' || T1.APPOINTED_DAY <= '" + param._date + "' ");
        stb.append(" GROUP BY ");
        stb.append("   SCHREGNO");

        return stb.toString();
    }

//★D184Gから移植_S(一部変更。変更前はコメントで残し。)

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        final int _lesson;
        final int _suspend;
        final int _mourning;
        final int _mlesson;
        final int _sick;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _transferDate;
        final int _offdays;
        final int _tochuKekka;

        public AttendSemesDat(
                final String semester,
                final int lesson,
                final int suspend,
                final int mourning,
                final int mlesson,
                final int sick,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transferDate,
                final int offdays,
                final int tochuKekka
        ) {
            _semester = semester;
            _lesson = lesson;
            _suspend = suspend;
            _mourning = mourning;
            _mlesson = mlesson;
            _sick = sick;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transferDate = transferDate;
            _offdays = offdays;
            _tochuKekka = tochuKekka;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final Map studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                param._attendParamMap.put("groupByDiv", "SEMESTER");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);
                for (final Iterator it = studentList.keySet().iterator(); it.hasNext();) {
                    final String kstr = (String) it.next();
                    StudentInfo student = (StudentInfo)studentList.get(kstr);

                    student._attendSemesDatList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");

                        if (!param._semester.equals(param._knjSchoolMst._semesterDiv) && Integer.parseInt(param._semester) < Integer.parseInt(semester)) {
                            continue;
                        }

                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int tochuKekka = rs.getInt("TOCHU_KEKKA");

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester, lesson, suspend, mourning, mlesson, sick, absent, present, late, early, transferDate, offdays, tochuKekka);

                        student._attendSemesDatList.add(attendSemesDat);
                    }

                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String toString() {
            return "AttendSemesDat(" + _semester + ": [" + _lesson + ", " + _suspend  + ", " + _mourning  + ", " + _mlesson  + ", " + _sick  + ", " + _present + ", " + _late + ", " + _early + "])";
        }
    }

    private static class Subclass {

        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final Map _detailMap = new HashMap();
        public Subclass(final String subclasscd, final String classname, final String subclassname) {
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
        }

        private static Subclass getSubclass(final List list, final String subclasscd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Subclass ss = (Subclass) it.next();
                if (subclasscd.equals(ss._subclasscd)) {
                    return ss;
                }
            }
            return null;
        }

        public static List getSubclassList(final DB2UDB db2, final Param param, final StudentInfo schinfo) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSubclassSql(param, schinfo);
                log.debug(" subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd;
                    if ("1".equals(param._useCurriculumcd) && !SUBCLASSCD999999.equals(rs.getString("SUBCLASSCD"))) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }

                    Subclass subclass = getSubclass(list, subclasscd);
                    if (null == subclass) {
                    	if ("1".equals(param._useCurriculumcd)) {
                            subclass = new Subclass(subclasscd, rs.getString("CLASSNAME"), rs.getString("SUBCLASSNAME"));
                    	} else {
                            subclass = new Subclass(subclasscd, "", rs.getString("SUBCLASSNAME"));
                    	}
                        list.add(subclass);
                    }

                    final String semester = rs.getString("SEMESTER");
                    final String semtestcd = rs.getString("SEMTESTCD");
                    final String classcd;
                    final String classname;
                    if ("1".equals(param._useCurriculumcd)) {
                        classcd = rs.getString("CLASSCD");
                        classname = rs.getString("CLASSNAME");
                    } else {
                    	classcd = "";
                    	classname = "";
                    }
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String scorediv = rs.getString("SCORE_DIV");
                    final String score = rs.getString("SCORE");
                    final String graderank = rs.getString("GRADE_RANK");
                    final String gradeavgrank = rs.getString("GRADE_AVG_RANK");
                    final String gradecount = rs.getString("GRADE_COUNT");
                    final String classrank = rs.getString("CLASS_RANK");
                    final String classavgrank = rs.getString("CLASS_AVG_RANK");
                    final String classcount = rs.getString("CLASS_COUNT");
                    final String getCredit = rs.getString("GET_CREDIT");
                    final String compCredit = rs.getString("COMP_CREDIT");
                    final String avg = new BigDecimal(StringUtils.defaultString(rs.getString("AVG"), "0")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    if (null == subclass._detailMap.get(semtestcd)) {
                        subclass._detailMap.put(semtestcd, new SubclassDetail());
                    }
                    //総合的な評価/評定データ(SUBCLASSCD=999999)を取得する。
                    if (SUBCLASSCD999999.equals(rs.getString("SUBCLASSCD"))
                    		&& ( SEM1_HYOUKACD.equals(semtestcd+scorediv)
                    		     || SEM2_HYOUKACD.equals(semtestcd+scorediv)
                    			 || SEM9_HYOUTEICD.equals(semtestcd+scorediv) ) ) {
                    	//評価データ/評定データで取得項目が違うので、分ける。(※後期データが不要であれば再検討。)
                    	if (!SEM9_HYOUTEICD.equals(semtestcd+scorediv)
                    			&& semester.equals(param._semester)
                    			) {
                    	    schinfo._detailInfo._gradeavgrank = gradeavgrank;
                    	    schinfo._detailInfo._gradecount = gradecount;
                    	    schinfo._detailInfo._classavgrank = classavgrank;
                    	    schinfo._detailInfo._classcount = classcount;
                    	} else if (SEM9_HYOUTEICD.equals(semtestcd+scorediv) && !"1".equals(param._semester)) {
                    	    schinfo._detailInfo._gradeavgrank = gradeavgrank;
                    	    schinfo._detailInfo._gradecount = gradecount;
                    	    schinfo._detailInfo._classavgrank = classavgrank;
                    	    schinfo._detailInfo._classcount = classcount;
                    	}
//                    	if (SEM9_HYOUTEICD.equals(semtestcd+scorediv) && !"1".equals(param._semester) || param._semester.equals(semester)) {
//                    		schinfo._detailInfo._avgrank = avg;
//                    	}
                    }
                    final SubclassDetail detail = (SubclassDetail) subclass._detailMap.get(semtestcd);
                    detail._subclasscd = subclasscd;
                    detail._subclassname = subclassname;
                    if ("1".equals(param._useCurriculumcd)) {
                        detail._classcd = classcd;
                        detail._classname = classname;
                    }
                    detail._semtestcd = semtestcd;
                    detail._score = score;
                    detail._graderank = graderank;
                    detail._gradecount = gradecount;
                    detail._classrank = classrank;
                    detail._classcount = classcount;
                    detail._getCredit = getCredit;
                    detail._compCredit = compCredit;
                    detail._avg = avg;
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static void setAttendSubclassList(final DB2UDB db2, final Param param, final Map studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                log.debug("attendsubcls sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.keySet().iterator(); it.hasNext();) {
                    final String kstr = (String) it.next();
                    StudentInfo student = (StudentInfo)studentList.get(kstr);

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");

                        if (!param._semester.equals(param._knjSchoolMst._semesterDiv) && Integer.parseInt(param._semester) < Integer.parseInt(semester)) {
                            continue;
                        }

                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String semtestcd = semester + "9900";
                        final Subclass subclass = getSubclass(student._subclassList, subclasscd);
                        if (null == subclass) {
                            continue;
                        }
                        if (null == subclass._detailMap.get(semtestcd)) {
                            subclass._detailMap.put(semtestcd, new SubclassDetail());
                        }
                        final SubclassDetail detail = (SubclassDetail) subclass._detailMap.get(semtestcd);
                        detail._subclasscd = subclasscd;
                        detail._semtestcd = semtestcd;
                        detail._jisu = rs.getString("MLESSON");
                        detail._kekka = String.valueOf(rs.getInt("SICK2"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getSubclassSql(final Param param, final StudentInfo schfino) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T7.CLASSNAME, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T6.SUBCLASSNAME, ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS SEMTESTCD, ");
            stb.append("     T1.SCORE_DIV, ");
            stb.append("     T1.SCORE, "); //評価(10段階->990008/5段階評価->990009)
            stb.append("     T1.AVG, "); //平均(科目別->SUBCLASSID/総合->ALL9)
            stb.append("     T1.GRADE_RANK, ");
            stb.append("     T1.GRADE_AVG_RANK, ");
            stb.append("     T3.COUNT AS GRADE_COUNT,"); //学年人数
            stb.append("     T1.CLASS_RANK, ");
            stb.append("     T1.CLASS_AVG_RANK, ");
            stb.append("     T4.COUNT AS CLASS_COUNT,"); //クラス人数
            stb.append("     T2.GET_CREDIT, ");
            stb.append("     T2.COMP_CREDIT ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("        AND T2.TESTKINDCD = T1.TESTKINDCD AND T2.TESTITEMCD = T1.TESTITEMCD AND T2.SCORE_DIV = '09' AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ");
            stb.append("       ON  T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T5.YEAR = T1.YEAR ");
            stb.append("       AND (T1.SEMESTER = '9' AND T5.SEMESTER = '" + param._ctrlSeme + "' OR T1.SEMESTER <> '9' AND T5.SEMESTER = T1.SEMESTER) ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T3 ");
            stb.append("       ON  T3.YEAR = T1.YEAR ");
            stb.append("       AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("       AND T3.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("       AND T3.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("       AND T3.SCORE_DIV = T1.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("       AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("       AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T3.AVG_DIV = '1' ");
            stb.append("       AND T3.GRADE = T5.GRADE ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T4 ");
            stb.append("       ON  T4.YEAR = T1.YEAR ");
            stb.append("       AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("       AND T4.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("       AND T4.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("       AND T4.SCORE_DIV = T1.SCORE_DIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       AND T4.CLASSCD = T1.CLASSCD ");
                stb.append("       AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T4.AVG_DIV = '2' ");
            stb.append("       AND T4.GRADE = T5.GRADE ");
            stb.append("       AND T4.HR_CLASS = T5.HR_CLASS ");
            stb.append("     LEFT JOIN SUBCLASS_MST T6 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ON  T6.CLASSCD = T1.CLASSCD ");
                stb.append("       AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("       AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("       AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     LEFT JOIN CLASS_MST T7 ");
                stb.append("       ON  T7.CLASSCD = T1.CLASSCD ");
                stb.append("       AND T7.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND ( T1.SEMESTER <= '" + param._semester + "' OR T1.SEMESTER = '9') ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ('" + SEM1_HYOUKACD + "', '"+SEM2_HYOUKACD+"', '"+SEM9_HYOUTEICD+"') ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('333333', '555555', '777777', '99999B') ");
            stb.append("     AND T1.SCHREGNO = '" + schfino._schregno + "' ");

            //subclasscdだけを取るのであれば下記コメントでも問題ないが、「成績としてのSUBCLASSCD」を取る事と、
            //成績データや関連データも合わせて取得するので、上記としている。
/*
            stb.append(" WITH MAIN AS ( ");
            stb.append(" SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS SEMTESTCD, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T1.GET_CREDIT, ");
            stb.append("     CAST(NULL AS DECIMAL(9,5)) AS AVG, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS ASSESSMARK ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("        AND T2.TESTKINDCD = T1.TESTKINDCD AND T2.TESTITEMCD = T1.TESTITEMCD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("     AND (T1.TESTKINDCD = '99' AND (T1.TESTITEMCD = '00' OR T1.TESTITEMCD = '01') AND T1.SCORE_DIV = '00') ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     CAST(NULL AS SMALLINT) AS GET_CREDIT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T3.ASSESSMARK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND REGD.YEAR = T1.YEAR ");
            stb.append("         AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._useAssessCourseMst)) {
                stb.append("     LEFT JOIN ASSESS_COURSE_MST T2 ON T2.ASSESSCD = '3' AND ROUND(T1.AVG, 0) BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ");
                stb.append("         AND T2.COURSECD = REGD.COURSECD AND T2.MAJORCD = REGD.MAJORCD AND T2.COURSECODE = REGD.COURSECODE ");
                stb.append("     LEFT JOIN ASSESS_COURSE_MST T3 ON T3.ASSESSCD = '4' AND T2.ASSESSLEVEL BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ");
                stb.append("         AND T3.COURSECD = REGD.COURSECD AND T3.MAJORCD = REGD.MAJORCD AND T3.COURSECODE = REGD.COURSECODE ");
            } else {
                stb.append("     LEFT JOIN ASSESS_MST T2 ON T2.ASSESSCD = '3' AND ROUND(T1.AVG, 0) BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ");
                stb.append("     LEFT JOIN ASSESS_MST T3 ON T3.ASSESSCD = '4' AND T2.ASSESSLEVEL BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("     AND (T1.TESTKINDCD = '99' AND (T1.TESTITEMCD = '00' OR T1.TESTITEMCD = '01')) ");
            stb.append("     AND T1.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" ), ORDER AS ( ");
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T4.CLASSNAME, ");
            stb.append("     VALUE(T3.SUBCLASSORDERNAME2, T3.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     VALUE(T4.SHOWORDER3, 99) AS ORDER1, ");
            stb.append("     VALUE(T3.SHOWORDER3, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
            stb.append(" T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN CLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append(" T4.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.SHOWORDER3, 99), VALUE(T4.SHOWORDER3, 99), T1.SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.CLASSNAME, ");
            stb.append("     T2.SUBCLASSNAME, ");
            stb.append("     T1.SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.GET_CREDIT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.ASSESSMARK, ");
            stb.append("     VALUE(T2.ORDER1, 99) AS ORDER1, ");
            stb.append("     VALUE(T2.ORDER2, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN ORDER T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("   T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("   T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
                stb.append("         SELECT ");
                stb.append("             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_DETAIL_DAT T1 ");
                stb.append("         WHERE ");
                stb.append("             YEAR = '" + param._year + "' ");
                stb.append("             AND SUBCLASS_SEQ = '007' ");
                if ("1".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK1 = '1' ");
                } else if ("2".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK2 = '1' ");
                } else if ("3".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK3 = '1' ");
                } else if ("9".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK4 = '1' ");
                }
                stb.append("         ) ");
            } else {
                stb.append("     T1.SUBCLASSCD NOT IN ( ");
                stb.append("         SELECT ");
                stb.append("             NAME1 ");
                stb.append("         FROM ");
                stb.append("             NAME_MST ");
                stb.append("         WHERE ");
                stb.append("             NAMECD1 = 'D026' ");
                if ("1".equals(param._semester)) {
                    stb.append("         AND ABBV1 = '1' ");
                } else if ("2".equals(param._semester)) {
                    stb.append("         AND ABBV2 = '1' ");
                } else if ("3".equals(param._semester)) {
                    stb.append("         AND ABBV3 = '1' ");
                } else if ("9".equals(param._semester)) {
                    stb.append("         AND NAMESPARE1 = '1' ");
                }
                stb.append("         ) ");
            }
            stb.append(" ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     VALUE(T2.ORDER1, 99), T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, VALUE(T2.ORDER2, 99) ");
            } else {
                stb.append("     VALUE(T2.ORDER1, 99), SUBSTR(T1.SUBCLASSCD, 1, 2), VALUE(T2.ORDER2, 99) ");
            }
            stb.append("     , T1.SUBCLASSCD, T1.SEMTESTCD ");
  */
            return stb.toString();
        }
        public String toString() {
            return "ScoreSubclass(" + _subclasscd + ":" + _subclassname + ":" + _detailMap + ")";
        }
    }

    private static class SubclassDetail {
        String _subclasscd;
        String _subclassname;
        String _classcd;
        String _classname;
        String _semtestcd;
        String _score;
        String _graderank;
        String _gradecount;
        String _classrank;
        String _classcount;
        String _getCredit;
        String _compCredit;
        String _avg;
        String _jisu;
        String _kekka;

        private String getKind(final int flg) {
            if (flg == 1) {
                return _jisu;
            }
            return _kekka;
        }

        public String toString() {
            return "[score=" + _score + ", avg=" + _avg + ", getCredit=" + _getCredit + ", _compCredit="+ _compCredit +"]";
        }
    }


//★D184Gから移植_E

//    private String sqlgetStudentDetail1(final Param param) {
//        final StringBuffer stb = new StringBuffer();
//        stb.append(" SELECT ");
//        stb.append("   SRD.SCHREGNO,");
//        stb.append("   SRD.GRADE, ");
//        stb.append("   SRG.GRADE_NAME1 AS GRADENAME, ");
//        stb.append("   SRD.HR_CLASS, ");
//        stb.append("   SRH.HR_NAME, ");
//        stb.append("   SRD.ATTENDNO, ");
//        stb.append("   SBM.NAME, ");
//        stb.append("   SM.STAFFNAME ");
//        stb.append(" FROM ");
//        stb.append("   SCHREG_REGD_DAT SRD ");
//        stb.append("   LEFT JOIN SCHREG_BASE_MST SBM ");
//        stb.append("     ON SBM.SCHREGNO = SRD.SCHREGNO");
//        stb.append("   LEFT JOIN SCHREG_REGD_GDAT SRG ");
//        stb.append("     ON  SRG.YEAR = SRD.YEAR ");
//        stb.append("     AND SRG.GRADE = SRD.GRADE ");
//        stb.append("   LEFT JOIN SCHREG_REGD_HDAT SRH ");
//        stb.append("     ON SRH.YEAR = SRH.YEAR ");
//        stb.append("     ON SRH.SEMESTER = SRH.SEMESTER ");
//        stb.append("     ON SRH.GRADE = SRH.GRADE ");
//        stb.append("     ON SRH.HR_CLASS = SRH.HR_CLASS ");
//        stb.append("   LEFT JOIN STAFF_MST SM ");
//        stb.append("     ON SM.STAFFCD = SRH.TR_CD1 ");
//        stb.append(" WHERE ");
//        stb.append("   SRD.YEAR = '" + param._year + "' ");
//        stb.append("   AND SRD.SEMESTER = '" + param._semester + "' ");
//        stb.append("   AND SRD.SCHREGNO IN " + param._selectedInState + " ");
//        stb.append(" ORDER BY ");
//        stb.append("   SRD.GRADE, SRD.HR_CLASS, SRD.ATTENDNO ");
//
//        return stb.toString();
//    }

//    private static class TestItem {
//        public String _testcd;
//        public String _testitemname;
//        public String _sidouinputinf;
//        public String _sidouinput;
//        public String _semester;
//        public String _scoreDivName;
//        public String _semesterDetail;
//        public DateRange _dateRange;
//        public boolean _printScore;
//        public String semester() {
//            return _testcd.substring(0, 1);
//        }
//        public String scorediv() {
//            return _testcd.substring(_testcd.length() - 2);
//        }
//        public boolean isKarihyotei() {
//            return !SEMEALL.equals(semester()) && "09".equals(scorediv());
//        }
//        public String toString() {
//            return "TestItem(" + _testcd + ":" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
//        }
//    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        //final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            //_dateRange = new DateRange(_semester, _semester, _semestername, sdate, edate);
        }
        public String toString() {
            return "Semester(" + _semester + ", " + _semestername + ")";
        }
    }

    private static class DateRange {
        final String _key;
        //final String _semester;
        final String _name;
        final String _sdate;
        final String _edate;
        //TestItem _testitem;
        //private List _childrenDateRangeList = new ArrayList();
        public DateRange(final String key, final String semester, final String name, final String sdate, final String edate) {
            _key = key;
//            _semester = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            if (!(o instanceof DateRange)) {
                return false;
            }
            final DateRange dr = (DateRange) o;
            return _key.equals(dr._key) && StringUtils.defaultString(_name).equals(StringUtils.defaultString(dr._name)) && rangeEquals(dr);
        }
        public boolean rangeEquals(final DateRange dr) {
            return StringUtils.defaultString(_sdate).equals(StringUtils.defaultString(dr._sdate)) && StringUtils.defaultString(_edate).equals(StringUtils.defaultString(dr._edate));
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _name + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class OutputInfo {
        private String _jobname;
        private String _principalname;
        private String _schoolname1;
        private String _schoolname2;
        private String _departmentname;
        private String _hrjobname;

        private List _schreglist;
        private Map _schregmap; //StudentInfoをput
        OutputInfo (
        		) {
        	_schreglist = new ArrayList();
        	_schregmap = new HashMap();
        }
    }

    private class StudentInfo {
        //基本情報
        private final String _schregno;
        private final String _grade;
        private final String _gradename;
        private final String _hrclass;
        private final String _hrname;
        private final String _attendno;
        private final String _name;
        private final String _staffname;
        //詳細情報
        private final List _committeename; //Stringで入れる。
        private final List _clubname;      //Stringで入れる。
        private final List _sikakuname;    //Stringで入れる。

        //取得タイミングの違いで、別口で設定
        private String _specialremark;
        private String _teacherremark;

        private List _attendSemesDatList;
        private List _subclassList;
        private StudentDetailInfo _detailInfo;

        private String _endday;

        StudentInfo(final String schregno, final String grade, final String gradename,
                    final String hrclass, final String hrname, final String attendno,
                    final String name, final String staffname) {
        	_schregno = schregno;
        	_grade = grade;
        	_gradename = gradename;
        	_hrclass = hrclass;
        	_hrname = hrname;
        	_attendno = attendno;
        	_name = name;
        	_staffname = staffname;
        	_committeename = new ArrayList();
        	_clubname = new ArrayList();
        	_sikakuname = new ArrayList();
        	_attendSemesDatList = new ArrayList();
        	_subclassList = new ArrayList();
        	_detailInfo = new StudentDetailInfo();
        	_endday = "";
        }
    }

    private class StudentDetailInfo {
        //各種出力情報
        //private String _simeday;     //SEMES_DATにデータとして最終日が登録されているので、それを設定
//        private String _classrank;    //クラス順位(担任からの通信で利用)
        private String _classavgrank; //評定平均(担任からの通信で利用)
        private String _classcount;   //クラス人数(担任からの通信で利用)
//        private String _graderank;    //学年順位(担任からの通信で利用)
        private String _gradeavgrank; //評定平均(担任からの通信で利用)
        private String _gradecount;   //学年人数(担任からの通信で利用)
//        private String _avgrank;      //評定平均(担任からの通信で利用)
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 72198 $ $Date: 2020-02-05 10:23:21 +0900 (水, 05 2 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        String _selectedInState;
        /** 出欠集計日付 */
        final String _date;
        final String _schoolKind;
        final String _documentRoot;
        final String _imagePath;

        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useAssessCourseMst;

        KNJSchoolMst _knjSchoolMst;

        private Map _semesterMap;
        private final String _lastSemester;
        private OutputInfo _outputinf;

        final Map _attendParamMap;

        Param(final HttpServletRequest request, final DB2UDB db2) {
        	_outputinf = new OutputInfo();
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _schoolKind = getSchoolKind(db2);
            _useCurriculumcd  = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useAssessCourseMst = request.getParameter("useAssessCourseMst");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _imagePath = null == returnval ? null : returnval.val4;

            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _lastSemester = StringUtils.defaultString(getLastSemester(db2, _year, _grade), "");
            _semesterMap = loadSemester(db2, _year, _grade);
            setCertifSchoolDat(db2);
            _selectedInState = "";
            String sep = "";
            for (int ii = 0; ii < _categorySelected.length;ii++) {
            	if (_categorySelected[ii] != null && !"".equals(_categorySelected[ii])) {
            	    _selectedInState += sep + "'" + _categorySelected[ii] + "' ";
            	    sep = ", ";
            	}
            }
            _selectedInState = "(" + _selectedInState + ")";

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }
        
        public String getSLOGOFilePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLLOGO." + "jpg";
            if (new java.io.File(path).exists()) {
                return path;
            }
            log.warn(" path not exists: " + path);
            return null;
        }

        public String getSSTMPH_FilePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLSTAMP_H." + "bmp";
            if (new java.io.File(path).exists()) {
                return path;
            }
            log.warn(" path not exists: " + path);
            return null;
        }

        public String getSSTMPJ_FilePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLSTAMP_H." + "bmp";
            if (new java.io.File(path).exists()) {
                return path;
            }
            log.warn(" path not exists: " + path);
            return null;
        }

        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        /**
         * 最後の学期を取得する
         */
        private String getLastSemester(final DB2UDB db2, final String year, final String grade) {
            final String sql = "select"
                    + "   MAX(SEMESTER) AS SEMESTER "
                    + " from"
                    + "   V_SEMESTER_GRADE_MST"
                    + " where"
                    + "   YEAR='" + year + "'"
                    + "   AND SEMESTER <> '9'"
                    + "   AND GRADE='" + grade + "'"
                    + " order by SEMESTER"
                ;

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            final Map map = new TreeMap();
            final String sql = "select"
                    + "   SEMESTER,"
                    + "   SEMESTERNAME,"
                    + "   SDATE,"
                    + "   EDATE"
                    + " from"
                    + "   V_SEMESTER_GRADE_MST"
                    + " where"
                    + "   YEAR='" + year + "'"
                    + "   AND GRADE='" + grade + "'"
                    + " order by SEMESTER"
                ;

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map rs = (Map) it.next();

            	String convStr = ("9".equals(KnjDbUtils.getString(rs, "SEMESTER")) || _lastSemester.equals(KnjDbUtils.getString(rs, "SEMESTER"))) ? "学年" : KnjDbUtils.getString(rs, "SEMESTERNAME");
                map.put(KnjDbUtils.getString(rs, "SEMESTER"), new Semester(KnjDbUtils.getString(rs, "SEMESTER"), convStr, KnjDbUtils.getString(rs, "SDATE"), KnjDbUtils.getString(rs, "EDATE")));
            }
            return map;
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
//            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK3, REMARK4 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' ");
            if ("J".equals(_schoolKind)) {
            	sql.append("   AND CERTIF_KINDCD = '103' ");
            } else {
            	sql.append("   AND CERTIF_KINDCD = '104' ");
            }
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _outputinf._schoolname1 = KnjDbUtils.getString(row, "REMARK3");
            _outputinf._departmentname = KnjDbUtils.getString(row, "REMARK4");

            _outputinf._schoolname2 = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _outputinf._jobname = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _outputinf._principalname = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _outputinf._hrjobname = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK2"), "担任");
        }
    }
}
