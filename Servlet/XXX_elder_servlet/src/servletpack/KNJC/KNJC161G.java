// kanji=漢字
package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id:$
 */
public class KNJC161G {

    private static final Log log = LogFactory.getLog("KNJC161G.class");

    private boolean _hasData;

    private Param _param;
    private final int maxGyo = 45; //1ページの最大行数

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            _hasData = false;

            _hasData = printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map hrclassMap = getStudentMap(db2); //出力生徒Map
        if(hrclassMap.size() == 0) {
            return false;
        }

        svf.VrSetForm("KNJC161G.frm", 1);
        //クラス毎のループ
        for(Iterator ite = hrclassMap.keySet().iterator(); ite.hasNext();) {
        	final String ghrKey = (String)ite.next();
        	final GradeHrclass ghrclass = (GradeHrclass)hrclassMap.get(ghrKey);
        	Map printMap = printMapSeikei(ghrclass);

        	//1ページ内に整形された分のループ
        	for(Iterator ite2 = printMap.keySet().iterator(); ite2.hasNext();) {
            	final String key = (String)ite2.next();
            	final List<Student> printList = (List)printMap.get(key);
            	setTitle(svf, ghrclass);
            	int gyo = 1; //行

            	//生徒毎のループ
            	for(Student student : printList) {
            		svf.VrsOutn("HR_NAME", gyo, ghrclass._hrName);
                	svf.VrsOutn("ATTENDNO", gyo, Integer.valueOf(student._attendno).toString());
                	svf.VrsOutn("NAME1", gyo, student._name); //30,40
                	String date = "";
                	//勤怠毎のループ
                	for(Iterator ite3 = student._attendMap.keySet().iterator(); ite3.hasNext();) {
                		//単独出力分
                    	if(gyo > maxGyo) {
                    		svf.VrEndPage();
                    		gyo = 1;
                    		date = "";
                    		setTitle(svf, ghrclass);
                    		svf.VrsOutn("HR_NAME", gyo, ghrclass._hrName);
                        	svf.VrsOutn("ATTENDNO", gyo, Integer.valueOf(student._attendno).toString());

                        	final String field = getField(student._name);
                        	svf.VrsOutn("NAME" + field, gyo, student._name);
                    	}
                		final String attendKey = (String)ite3.next();
                    	final Attend attend = (Attend)student._attendMap.get(attendKey);
                        if(!attend._date.equals(date)) {
                        	date = attend._date;
                        	final String youbi = KNJ_EditDate.h_format_W(date);
                        	svf.VrsOutn("ATTENDDATE", gyo, date.replace("-", "/") + " (" + youbi + ")"); //実施日付
                        }
                    	svf.VrsOutn("PERIODCD", gyo, attend._jigen); //時限
                    	final String chairName = attend._smallClass != null ? attend._chairName + " " + attend._smallClass : attend._chairName;
                    	final String field = getField(chairName);
                    	svf.VrsOutn("CHAIR_NAME" + field, gyo, chairName); //講座名
                    	gyo++;
                	}
            	}
            	svf.VrEndPage();
        	}
        }

        return true;
    }

    private void setTitle(final Vrw32alp svf, final GradeHrclass ghrclass) {
    	svf.VrsOut("TITLE", "欠席届未届けリスト");
    	svf.VrsOut("RANGE", _param._sDate.replace("-", "/") + "～" + _param._eDate.replace("-", "/"));
    }

    private Map printMapSeikei(final GradeHrclass ghrclass) {
    	Map retMap = new LinkedMap();

    	//指示画面でクラス選択
    	if("1".equals(_param._disp)) {
    		int key = 1; //全体の連番
    		int no = 1; //1ページ内の連番
        	int size = maxGyo; //1ページの残容量
        	List<Student> list = new ArrayList();

        	for(Iterator ite = ghrclass._studentMap.keySet().iterator(); ite.hasNext();) {
            	final String stuKey = (String)ite.next();
            	final Student student = (Student)ghrclass._studentMap.get(stuKey);

            	//一人の生徒の出力数が1ページ最大出力数を超える場合
            	//次ページから単独出力する
            	if(student._attendMap.size() > maxGyo && no != 1) {
            		retMap.put(String.valueOf(key), list);
            		no = 1;
            		size = maxGyo;
            		key++;
            		list = new ArrayList();
            	}
            	//一人の生徒の出力数が1ページの残容量を超える場合
            	else if(student._attendMap.size() > size && no != 1) {
            		retMap.put(String.valueOf(key), list);
            		no = 1;
            		size = maxGyo;
            		key++;
            		list = new ArrayList();
            	}
            	list.add(student);
        		size -= student._attendMap.size();
        		no++;
        	}

        	if(!list.isEmpty()) {
        		retMap.put(String.valueOf(key), list);
        	}
    	}
    	//指示画面で個人選択
        else {
    		for(Iterator ite = ghrclass._studentMap.keySet().iterator(); ite.hasNext();) {
            	final String stuKey = (String)ite.next();
            	final Student student = (Student)ghrclass._studentMap.get(stuKey);

            	List<Student> list = new ArrayList();
            	list.add(student);
            	retMap.put(String.valueOf(student._schregno), list);
    		}
    	}

    	return retMap;
    }

    private String getField(final String str) {
    	final int keta = KNJ_EditEdit.getMS932ByteLength(str);
    	return keta <= 30 ? "1" : "2";
    }

    private Map getStudentMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        GradeHrclass ghrclass = null;
        Student student = null;

        try{
        	final StringBuffer stb = new StringBuffer();

        	//対象生徒
        	stb.append(" WITH SCHREG_DATA AS (SELECT ");
        	stb.append("   REGD.SCHREGNO, ");
        	stb.append("   REGD.HR_CLASS, ");
        	stb.append("   BASE.NAME, ");
        	stb.append("   REGD.ATTENDNO, ");
        	stb.append("   REGD_H.HR_NAME ");
        	stb.append(" FROM ");
        	stb.append("   SCHREG_REGD_DAT REGD   ");
        	stb.append(" INNER JOIN ");
        	stb.append("   SCHREG_REGD_HDAT REGD_H ");
        	stb.append("    ON REGD_H.YEAR = REGD.YEAR ");
        	stb.append("   AND REGD_H.SEMESTER = REGD.SEMESTER ");
        	stb.append("   AND REGD_H.GRADE = REGD.GRADE ");
        	stb.append("   AND REGD_H.HR_CLASS = REGD.HR_CLASS   ");
        	stb.append(" INNER JOIN ");
        	stb.append("   SCHREG_BASE_MST BASE ");
        	stb.append("    ON BASE.SCHREGNO = REGD.SCHREGNO ");
        	stb.append(" WHERE ");
        	stb.append("     REGD.YEAR = '" + _param._year + "'    AND ");
        	stb.append("     REGD.SEMESTER = '" + _param._semester + "'    AND ");
        	if("1".equals(_param._disp)) {
        		stb.append("   REGD.GRADE || REGD.HR_CLASS IN" + SQLUtils.whereIn(true, _param._categorySelected));
        	} else {
        		stb.append("   REGD.SCHREGNO IN" + SQLUtils.whereIn(true, _param._categorySelected));
        	}
        	stb.append(" ORDER BY ");
        	stb.append("     REGD.HR_CLASS, ");
        	stb.append("     REGD.ATTENDNO ");
        	stb.append(" ) ");
        	//メイン表
        	stb.append(" SELECT ");
        	stb.append("   SCHREG.SCHREGNO, ");
        	stb.append("   SCHREG.HR_NAME, ");
        	stb.append("   SCHREG.ATTENDNO, ");
        	stb.append("   SCHREG.NAME, ");
        	stb.append("   SCHREG.HR_CLASS, ");
        	stb.append("   ATTEND.ATTENDDATE, ");
        	stb.append("   ATTEND.PERIODCD, ");
        	stb.append("   ATTEND.TMP_FLG, ");
        	stb.append("   CHAIR.CHAIRNAME, ");
        	stb.append("   CHAIR_D.REMARK2 ");
        	stb.append(" FROM ");
        	stb.append("   ATTEND_DAT ATTEND ");
        	stb.append(" INNER JOIN ");
        	stb.append("   SCHREG_DATA SCHREG ");
        	stb.append("    ON SCHREG.SCHREGNO = ATTEND.SCHREGNO ");
        	stb.append(" INNER JOIN ");
        	stb.append("   ATTEND_CHAIR_STF_DAT CSTAF ");
        	stb.append("    ON CSTAF.SCHREGNO = ATTEND.SCHREGNO ");
        	stb.append("   AND CSTAF.ATTENDDATE = ATTEND.ATTENDDATE ");
        	stb.append("   AND CSTAF.PERIODCD = ATTEND.PERIODCD ");
        	stb.append(" INNER JOIN ");
        	stb.append("   CHAIR_DAT CHAIR ");
        	stb.append("    ON CHAIR.YEAR = '" + _param._year + "' ");
        	stb.append("   AND CHAIR.SEMESTER = '" + _param._semester + "' ");
        	stb.append("   AND CHAIR.CHAIRCD = ATTEND.CHAIRCD ");
        	stb.append(" LEFT JOIN ");
        	stb.append("   CHAIR_DETAIL_DAT CHAIR_D ");
        	stb.append("    ON CHAIR_D.YEAR = CHAIR.YEAR ");
        	stb.append("   AND CHAIR_D.SEMESTER = CHAIR.SEMESTER ");
        	stb.append("   AND CHAIR_D.CHAIRCD = CHAIR.CHAIRCD ");
        	stb.append("   AND CHAIR_D.SEQ = '004' ");
        	stb.append(" WHERE ");
        	stb.append("   ATTEND.DI_CD IS NOT NULL AND ");
        	stb.append("   ATTEND.ATTENDDATE BETWEEN '"+ _param._sDate + "' AND '" + _param._eDate + "' AND ");
        	stb.append("   ATTEND.TMP_FLG = '1' ");
        	stb.append(" ORDER BY ");
        	stb.append("   SCHREG.HR_CLASS, SCHREG.ATTENDNO, ATTEND.ATTENDDATE, ATTEND.PERIODCD ");

            log.debug(" sql =" + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String schregno = rs.getString("SCHREGNO");
            	final String hrclass = rs.getString("HR_CLASS");
            	final String hr_Name = rs.getString("HR_NAME");
            	final String attendno = rs.getString("ATTENDNO");
            	final String name = rs.getString("NAME");
            	final String attenddate = rs.getString("ATTENDDATE");
            	final String periodcd = rs.getString("PERIODCD");
            	final String chairname = rs.getString("CHAIRNAME");
            	final String remark2 = rs.getString("REMARK2");

                if(retMap.containsKey(hrclass)) {
                	ghrclass = (GradeHrclass)retMap.get(hrclass);
               	} else {
               		ghrclass = new GradeHrclass(hrclass, hr_Name);
               		retMap.put(hrclass, ghrclass);
               	}

                if(ghrclass._studentMap.containsKey(schregno)) {
                	student = (Student)ghrclass._studentMap.get(schregno);
                } else {
                	student = new Student(schregno, attendno, name);
                	ghrclass._studentMap.put(schregno, student);
                }

                final String key = attenddate + periodcd;
                if(!student._attendMap.containsKey(key)) {
                	final Attend attend = new Attend(attenddate, periodcd, chairname, remark2);
                	student._attendMap.put(key, attend);
                }
            }

        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;

    }

    private class GradeHrclass {
    	final String _hrclass;
    	final String _hrName;
    	final Map _studentMap;

    	public GradeHrclass(final String hrclass, final String hrName) {
    		_hrclass = hrclass;
    		_hrName = hrName;
    		_studentMap = new LinkedMap();
    	}
    }

    private class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final Map _attendMap;

        public Student(final String schregno, final String attendno, final String name) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _attendMap = new LinkedMap();
        }
    }

    private class Attend {
    	final String _date;
        final String _jigen;
        final String _chairName;
        final String _smallClass;

        public Attend(final String date, final String jigen, final String chairName, final String smallClass) {
        	_date = date;
        	_jigen = jigen;
        	_chairName = chairName;
        	_smallClass = smallClass;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id:$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _loginDate;
        final String _semester;
        final String _disp; //1:クラス 2:個人
        final String _sDate; // 対象日付範囲start
        final String _eDate; // 対象日付範囲end
        final String _categorySelected[];

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("LOGIN_YEAR");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            _semester = request.getParameter("SEMESTER");
            _disp = request.getParameter("DISP");
            _sDate = request.getParameter("SDATE").replace("/", "-");
            _eDate = request.getParameter("EDATE").replace("/", "-");
        }
    }
}

// eof
