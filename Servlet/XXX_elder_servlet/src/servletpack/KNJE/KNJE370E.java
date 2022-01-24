// kanji=漢字
/*
 * $Id: 58a25c4f579606ec9ca4b4136e12970f674e86ce $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 58a25c4f579606ec9ca4b4136e12970f674e86ce $
 */
public class KNJE370E {

    private static final Log log = LogFactory.getLog("KNJE370E.class");

    private boolean _hasData;

    Param _param;

    private static final String csv = "csv";

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            init(response, svf);

            if (csv.equals(_param._cmd)) {
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                // CSV出力処理
                final List outputLines = new ArrayList();
                _hasData = printCsv(db2, outputLines);
                final String fileName = _param._nendo + "　選考会リスト";
                CsvUtils.outputLines(log, response, fileName + ".csv", outputLines, csvParam);
            }else {
            	printMain(db2, svf);
            }

            if (!csv.equals(_param._cmd)) {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            if (!csv.equals(_param._cmd)) svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        if (!csv.equals(_param._cmd)) {
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
            _hasData = printSvf(db2,svf);
    }

    public boolean printSvf(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        boolean hasData = false;
        Map<String, Map<String, AvgHyoutei>> schregMap = new HashMap<String, Map<String, AvgHyoutei>>();
        final List printData = getPrintData(db2, schregMap);
        svf.VrSetForm("KNJE370E.frm", 1);
        int fieldCnt = 1;
        int renban = 1;
        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final PrintData data = (PrintData) it.next();
            //学校毎に改ページしないように変更
            if (fieldCnt > 30) {
                svf.VrEndPage();
                fieldCnt = 1;
            }
            //ヘッダ
            svf.VrsOut("TITLE", _param._nendo + "　選考会リスト"); //タイトル
            svf.VrsOut("DATE", _param.changePrintDate(db2, _param._ctrlDate)); //日付
            svf.VrsOut("TIME", _param._loginTime); //時間

            //明細
            svf.VrsOutn("NO", fieldCnt, String.valueOf(renban)); //番号
            svf.VrsOutn("COLLEGE_NAME", fieldCnt, data._stat_name); //学校名
            svf.VrsOutn("FACULTY_NAME", fieldCnt, data._facultyname); //学部名
            svf.VrsOutn("DEPARTMENT_NAME", fieldCnt, data._departmentname); //学科名
            svf.VrsOutn("KIND", fieldCnt, data._kind); //文理
            svf.VrsOutn("DIV", fieldCnt, data._div); //学校類別
            svf.VrsOutn("STS", fieldCnt, data._acceptance_criterion_b); //STS
            svf.VrsOutn("NAME1", fieldCnt, data._name); //氏名
            AvgHyoutei wk = null;
            if (schregMap.containsKey(data._grade)) {
            	wk = schregMap.get(data._grade).get(data._schregno);
            }
            if (null != wk) {
                svf.VrsOutn("RATE_AVE", fieldCnt, wk._avg); //評定平均
                svf.VrsOutn("RANK", fieldCnt, wk._hyotei); //概評
                svf.VrsOutn("RATE_AVE_RANK", fieldCnt, wk._rank); //評定平均順位
            }
            svf.VrsOutn("COURSE", fieldCnt, data._course); //玉聖コード
            svf.VrsOutn("SELECT_DIV", fieldCnt, data._select_div); //選考分類
            svf.VrsOutn("SELECT_RESULT", fieldCnt, data._select_result); //選考結果

            hasData = true;
            fieldCnt++;
            renban++;
        }
        if (hasData) {
            svf.VrEndPage();
        }
        return hasData;
    }

    public boolean printCsv(final DB2UDB db2, final List csvlist) throws SQLException {
        boolean hasData = false;
        Map<String, Map<String, AvgHyoutei>> schregMap = new HashMap<String, Map<String, AvgHyoutei>>();
        final List printData = getPrintData(db2, schregMap);

        List csvDataList = new ArrayList();

        //CSVのヘッダ生成
        final String fileName = _param._nendo + "　選考会リスト";

        csvDataList.add(fileName + "　" + _param.changePrintDate(db2, _param._ctrlDate) + " " + _param._loginTime);
        csvlist.add(csvDataList);

        csvDataList = new ArrayList();
        String[] header =  {"No.","大学コード","学校名","学部コード","学部名","学科コード","学科名","日程コード","方式コード","文理","学校類別コード","学校類別","STS","年組番","氏名","評定平均","概評","評定平均順位","玉聖コース","選考分類コード", "選考分類","選考結果"};
        for(int idx = 0; idx < header.length; idx++) {
            csvDataList.add(header[idx]);
        }
        csvlist.add(csvDataList);

        int renban = 1;
        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final PrintData data = (PrintData) it.next();
            csvDataList = new ArrayList(); //CSV出力用リストの初期化

            final String schoolCd = !"".equals(data._school_cd) ? data._school_cd.substring(4) : "";
            final String facultyCd = !"".equals(data._facultycd) ? data._facultycd.substring(1) : "";
            final String departmentCd = !"".equals(data._departmentcd) ? data._departmentcd.substring(1) : "";
            final String grade = !"".equals(data._grade) ? data._grade.substring(1) : "";
            final String hrClass = !"".equals(data._hr_class) ? data._hr_class.substring(1) : "";
            final String attendNo = !"".equals(data._attendno) ? data._attendno.substring(1) : "";
            final String gradeHrClass = grade + hrClass + attendNo;

            //明細
            csvDataList.add(String.valueOf(renban)); //番号
            csvDataList.add(schoolCd); //大学コード
            csvDataList.add(data._stat_name); //学校名
            csvDataList.add(facultyCd); //学部コード
            csvDataList.add(data._facultyname); //学部名
            csvDataList.add(departmentCd); //学科コード
            csvDataList.add(data._departmentname); //学科名
            csvDataList.add(data._program_cd); //日程コード
            csvDataList.add(data._form_cd); //方式コード
            csvDataList.add(data._kind); //文理
            csvDataList.add(data._school_sort); //学校類別コード
            csvDataList.add(data._div); //学校類別
            csvDataList.add(data._acceptance_criterion_b); //STS
            csvDataList.add(gradeHrClass); //年組番号
            csvDataList.add(data._name); //氏名
            AvgHyoutei wk = null;
            if (schregMap.containsKey(data._grade)) {
            	wk = schregMap.get(data._grade).get(data._schregno);
            }
            if (null != wk) {
                csvDataList.add(wk._avg); //評定平均
                csvDataList.add(wk._hyotei); //概評
                csvDataList.add(wk._rank); //評定平均順位
            } else {
                csvDataList.add(""); //評定平均
                csvDataList.add(""); //概評
                csvDataList.add(""); //評定平均順位
            }
            csvDataList.add(data._course); //玉聖コース
            csvDataList.add(data._select_div); //選考分類コード
            csvDataList.add(data._select_div_name); //選考分類
            csvDataList.add(data._select_result); //選考結果

            csvlist.add(csvDataList);
            renban++;
        }

        return hasData;
    }


    /**
     * 文字数によるフォームフィールド名を取得
     * @param str：データ
     * @param field1：フィールド１（小さい方）
     * @param field2：フィールド２（大きい方）
     * @param len：フィールド１の文字数
     */
    private String getFieldName(final String str, final String field1, final String field2, final int len) {
        if (null == str) return field1;
        return len < KNJ_EditEdit.getMS932ByteLength(str) ? field2 : field1;
    }

    private List getPrintData(final DB2UDB db2, final Map<String, Map<String, AvgHyoutei>> schregMap) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String printSql = getPrintSql();
        log.debug(" sql = " + printSql);
        try {
            ps = db2.prepareStatement(printSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
                final String hr_name = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String renban = StringUtils.defaultString(rs.getString("RENBAN"));
                final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String acceptance_criterion_b = StringUtils.defaultString(rs.getString("ACCEPTANCE_CRITERION_B"));
                final String school_cd = StringUtils.defaultString(rs.getString("SCHOOL_CD"));
                final String stat_name = StringUtils.defaultString(rs.getString("STAT_NAME"));
                final String facultycd = StringUtils.defaultString(rs.getString("FACULTYCD"));
                final String facultyname = StringUtils.defaultString(rs.getString("FACULTYNAME"));
                final String departmentcd = StringUtils.defaultString(rs.getString("DEPARTMENTCD"));
                final String departmentname = StringUtils.defaultString(rs.getString("DEPARTMENTNAME"));
                final String school_sort = StringUtils.defaultString(rs.getString("SCHOOL_SORT"));
                final String div = StringUtils.defaultString(rs.getString("DIV"));
                final String kind = StringUtils.defaultString(rs.getString("KIND"));
                final String course = StringUtils.defaultString(rs.getString("COURSE"));
                final String select_div = StringUtils.defaultString(rs.getString("SELECT_DIV"));
                final String select_div_name = StringUtils.defaultString(rs.getString("SELECT_DIV_NAME"));
                final String select_result = StringUtils.defaultString(rs.getString("SELECT_RESULT"));
                final String program_cd = StringUtils.defaultString(rs.getString("PROGRAM_CD"));
                final String form_cd = StringUtils.defaultString(rs.getString("FORM_CD"));
                final PrintData printData = new PrintData(grade, hr_class, hr_name, attendno, schregno, renban, seq, name, acceptance_criterion_b, school_cd, stat_name, facultycd, facultyname, departmentcd, departmentname, school_sort, div, kind, course, select_div, select_div_name, select_result, program_cd, form_cd);
                rtnList.add(printData);
                if (!schregMap.containsKey(grade)) {
                	schregMap.put(grade, new TreeMap<String, AvgHyoutei>());
                }
                if (!schregMap.get(grade).containsKey(schregno)) {
                    schregMap.get(grade).put(schregno, new AvgHyoutei());
                }
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (rtnList.size() > 0) {
        	setGradeStudent(db2, schregMap);
        	for (final Map<String, AvgHyoutei> schregMapGradeGroup : schregMap.values()) {
        		gethyoteiRankData(db2, schregMapGradeGroup);
        		setAvgRank(schregMapGradeGroup);
        	}
        }
        return rtnList;
    }

    private void setAvgRank(final Map<String, AvgHyoutei> schregMap) {
		final Map<BigDecimal, List<AvgHyoutei>> avgListMap = new TreeMap<BigDecimal, List<AvgHyoutei>>(); // avgごとのAvgHyouteiのリスト
		for (final AvgHyoutei wk : schregMap.values()) {
			if (null == wk || !NumberUtils.isNumber(wk._avg)) {
				continue;
			}
			final BigDecimal avgBd = new BigDecimal(wk._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
			if (!avgListMap.containsKey(avgBd)) {
				avgListMap.put(avgBd, new ArrayList<AvgHyoutei>());
			}
			avgListMap.get(avgBd).add(wk);
		}

		int rank = 1;
		final List<BigDecimal> avgList = new ArrayList<BigDecimal>(avgListMap.keySet());
		// ランクはavg降順
		for (final ListIterator<BigDecimal> lit = avgList.listIterator(avgList.size()); lit.hasPrevious();) {
			final BigDecimal avg = lit.previous();
			final List<AvgHyoutei> wkList = avgListMap.get(avg);
			for (final AvgHyoutei wk : wkList) {
				wk._rank = String.valueOf(rank);
			}
			rank += wkList.size();
		}
	}

    // 学年順位をセットするため指定学年の生徒全員分のAvgHyouteiを作成
    private void setGradeStudent(final DB2UDB db2, final Map<String, Map<String, AvgHyoutei>> schregMap) {
    	final String sql = getGradeSql();
    	for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
    		final Map row = (Map) it.next();
    		final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
    		final String grade = KnjDbUtils.getString(row, "GRADE");
    		if (!schregMap.containsKey(grade)) {
    			schregMap.put(grade, new TreeMap<String, AvgHyoutei>());
    		}
    		if (!schregMap.get(grade).containsKey(schregno)) {
    			schregMap.get(grade).put(schregno, new AvgHyoutei());
    		}
    	}
	}

    private String getGradeSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("   T1.GRADE ");
        stb.append(" , T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = 'H' ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO ");

        return stb.toString();
    }

    private void gethyoteiRankData(final DB2UDB db2, final Map<String, AvgHyoutei> schregMap) {

        KNJDefineSchool defineSchool = new KNJDefineSchool();
        defineSchool.defineCode(db2, _param._year);
        final KNJE070_1 knje070_1 = new KNJE070_1(db2, (Vrw32alp) null, defineSchool, (String) null);

        try {
        	final int size = schregMap.size();
        	int count = 0;
            for(String schregNo : schregMap.keySet()) {
                AvgHyoutei setWk = schregMap.get(schregNo);

                final List<KNJE070_1.HyoteiHeikin> hyoteiHeikinList = knje070_1.getHyoteiHeikinList(schregNo, _param._year, _param._semester, _param._knje070Paramap);
                for (int i = 0; i < hyoteiHeikinList.size(); i++) {
                	final KNJE070_1.HyoteiHeikin heikin = hyoteiHeikinList.get(i);
                	if ("TOTAL".equals(heikin.classkey())) {
                		setWk._avg = heikin.avg();
                		setWk._hyotei = heikin.gaihyo();
                		count += 1;
                		log.info(" schregNo " + schregNo + ", heikin = " + heikin + " (" + count + " / " + size + ")");
                	}
                }
            }

        } catch (Throwable t) {
        	log.warn("KNJE070 failed.", t);
        }
        knje070_1.pre_stat_f();
    }

    /*
     * AvgHyoutei
     */
    private class AvgHyoutei {
        String _avg;
        String _hyotei;
        String _rank;
        AvgHyoutei() {
            _avg = new String();
            _hyotei = new String();
            _rank = new String();
        }
    }
    /**
     * StudyRec
     */
    private class StudyRec {
        final String _year;
        final String _fieldNo;
        final String _gradeName;
        final String _classcd;
        final String _className;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        StudyRec(
                final String year,
                final String fieldNo,
                final String gradeName,
                final String classcd,
                final String className,
                final String subclasscd,
                final String subclassname,
                final String value) {
            _year = year;
            _fieldNo = fieldNo;
            _gradeName = gradeName;
            _classcd = classcd;
            _className = className;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
        }
    }

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        // 年組
        stb.append("   I2.GRADE, ");
        stb.append("   I2.HR_CLASS, ");
        stb.append("   I3.HR_NAME, ");
        // 出席番号
        stb.append("   I2.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        // 連番 ※単純な連番
        stb.append("   ROW_NUMBER() over (ORDER BY I2.GRADE, I2.HR_CLASS, I2.ATTENDNO, L1.SCHOOL_CD, T1.SEQ) AS RENBAN, ");
        stb.append("   T1.SEQ, ");
        // 氏名
        stb.append("   I1.NAME, ");
        // STS
        stb.append("   C1.ACCEPTANCE_CRITERION_B, ");
        // 受験(予定)学校名
        stb.append("   L1.SCHOOL_CD AS SCHOOL_CD, ");
        stb.append("   L1.SCHOOL_NAME_SHOW1 AS STAT_NAME, ");
        // 学部名
        stb.append("   T1.FACULTYCD, ");
        stb.append("   L2.FACULTYNAME_SHOW1 AS FACULTYNAME, ");
        // 学科名
        stb.append("   T1.DEPARTMENTCD, ");
        stb.append("   L3.DEPARTMENTNAME_SHOW1 AS DEPARTMENTNAME, ");
        // 学校類別
        stb.append("   L1.SCHOOL_SORT, ");
        stb.append("   E001.NAME1 AS DIV, ");
        // 文理
        stb.append("   CASE WHEN C1.BACHELOR_DIV = '1' THEN '文' ELSE ");
        stb.append("   CASE WHEN C1.BACHELOR_DIV = '2' THEN '理' ELSE ");
        stb.append("   '' END END AS KIND, ");
        // コース
        stb.append("   I4.COURSECODEABBV1 AS COURSE, ");
        // 選考分類
        stb.append("   T3.REMARK1 AS SELECT_DIV, ");
        stb.append("   E054.NAME1 AS SELECT_DIV_NAME, ");
        // 選考結果
        stb.append("   E055.ABBV1 AS SELECT_RESULT, ");
        // 入試カレンダー
        stb.append("   C1.PROGRAM_CD, "); //日程コード
        stb.append("   C1.FORM_CD ");     //方式コード
        stb.append(" FROM ");
        stb.append("   AFT_GRAD_COURSE_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("   INNER JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = T1.YEAR AND I2.SEMESTER = '" + _param._semester + "' ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("   LEFT JOIN COURSECODE_MST I4 ON I4.COURSECODE = I2.COURSECODE ");
        stb.append("   INNER JOIN SCHREG_REGD_GDAT I5 ON I5.YEAR = I2.YEAR AND I5.GRADE = I2.GRADE");
        stb.append("   LEFT JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("   LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = T1.STAT_CD AND L2.FACULTYCD = T1.FACULTYCD ");
        stb.append("   LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD = T1.STAT_CD AND L3.FACULTYCD = T1.FACULTYCD AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("   LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T2 ");
        stb.append("        ON T2.YEAR          = T1.YEAR ");
        stb.append("       AND T2.SEQ           = T1.SEQ ");
        stb.append("       AND T2.DETAIL_SEQ    = 1 ");
        stb.append("   INNER JOIN AFT_GRAD_COURSE_DETAIL_DAT T3 ");
        stb.append("        ON T3.YEAR          = T1.YEAR ");
        stb.append("       AND T3.SEQ           = T1.SEQ ");
        stb.append("       AND T3.DETAIL_SEQ    = 6 ");
        stb.append("       AND T3.REMARK1       = '" + _param._selectDiv + "' ");
        stb.append("   LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ");
        stb.append("        ON C1.YEAR          = T1.YEAR ");
        stb.append("       AND C1.SCHOOL_CD     = T1.STAT_CD ");
        stb.append("       AND C1.FACULTYCD     = T1.FACULTYCD ");
        stb.append("       AND C1.DEPARTMENTCD  = T1.DEPARTMENTCD ");
        stb.append("       AND C1.ADVERTISE_DIV = T2.REMARK1 ");
        stb.append("       AND C1.PROGRAM_CD    = T2.REMARK2 ");
        stb.append("       AND C1.FORM_CD       = T2.REMARK3 ");
        stb.append("       AND C1.L_CD1         = T2.REMARK4 ");
        stb.append("       AND C1.S_CD          = T2.REMARK5 ");
        stb.append("   LEFT JOIN NAME_MST E001 ON E001.NAMECD1 = 'E001' AND E001.NAMECD2 = L1.SCHOOL_SORT ");
        stb.append("   LEFT JOIN NAME_MST E054 ON E054.NAMECD1 = 'E054' AND E054.NAMECD2 = T3.REMARK1 ");
        stb.append("   LEFT JOIN NAME_MST E055 ON E055.NAMECD1 = 'E055' AND E055.NAMECD2 = T3.REMARK2 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SENKOU_KIND = '0' ");
        stb.append("     AND I5.SCHOOL_KIND = 'H' ");
        stb.append(" ORDER BY ");
        stb.append("   SCHOOL_CD, ");
        stb.append("   FACULTYCD, ");
        stb.append("   DEPARTMENTCD, ");
        stb.append("   GRADE, ");
        stb.append("   HR_CLASS, ");
        stb.append("   ATTENDNO, ");
        stb.append("   SEQ ");

        return stb.toString();
    }

    private class PrintData {
        final String _grade;
        final String _hr_class;
        final String _hr_name;
        final String _attendno;
        final String _schregno;
        final String _renban;
        final String _seq;
        final String _name;
        final String _acceptance_criterion_b;
        final String _school_cd;
        final String _stat_name;
        final String _facultycd;
        final String _facultyname;
        final String _departmentcd;
        final String _departmentname;
        final String _kind;
        final String _school_sort;
        final String _div;
        final String _course;
        final String _select_div;
        final String _select_div_name;
        final String _select_result;
        final String _program_cd;
        final String _form_cd;

        PrintData(final String grade,
                final String hr_class,
                final String hr_name,
                final String attendno,
                final String schregno,
                final String renban,
                final String seq,
                final String name,
                final String acceptance_criterion_b,
                final String school_cd,
                final String stat_name,
                final String facultycd,
                final String facultyname,
                final String departmentcd,
                final String departmentname,
                final String school_sort,
                final String div,
                final String kind,
                final String course,
                final String select_div,
                final String select_div_name,
                final String select_result,
                final String program_cd,
                final String form_cd
        ) {
            _grade = grade;
            _hr_class = hr_class;
            _hr_name = hr_name;
            _attendno = attendno;
            _schregno = schregno;
            _renban = renban;
            _seq = seq;
            _name = name;
            _acceptance_criterion_b = acceptance_criterion_b;
            _school_cd = school_cd;
            _stat_name = stat_name;
            _facultycd = facultycd;
            _facultyname = facultyname;
            _departmentcd = departmentcd;
            _departmentname = departmentname;
            _school_sort = school_sort;
            _div = div;
            _kind = kind;
            _course = course;
            _select_div = select_div;
            _select_div_name = select_div_name;
            _select_result = select_result;
            _program_cd = program_cd;
            _form_cd = form_cd;
        }

        /**
         * {@inheritDoc}
         */
        public String getAttendNo() {
            if (null == _attendno || "".equals(_attendno)) return "  番";
            return String.valueOf(Integer.parseInt(_attendno)) + "番";
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregno + " = " + _name;
        }
    }



    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69497 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final String _selectDiv;
        private final String _cmd;
        private boolean _isSeireki;
        private final String _nendo;
        private final String _loginTime;
        private final Map _knje070Paramap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _selectDiv = request.getParameter("SELECT_DIV");
            _cmd = request.getParameter("cmd");
            setSeirekiFlg(db2);
            _nendo = changePrintYear(db2, _year, _ctrlDate);

            Date date = new Date();
            SimpleDateFormat sdf_t = new SimpleDateFormat("HH:mm:ss");
            sdf_t.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            _loginTime = sdf_t.format(date);
            _knje070Paramap = new KNJE070().createParamMap(request);
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintDate(final DB2UDB db2, final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(db2, date);
            }
        }

        public String changePrintYear(final DB2UDB db2, final String year, final String date) {
            if (null == year) {
                return "";
            }
            if (_isSeireki) {
                return year + "年度";
            } else {
                final String[] gengou = KNJ_EditDate.tate_format4(db2, date);
                return gengou[0] + gengou[1] + "年度";
            }
        }

    }
}

// eof
