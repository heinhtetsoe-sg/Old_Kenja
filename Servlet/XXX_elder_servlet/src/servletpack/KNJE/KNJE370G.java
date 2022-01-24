// kanji=漢字
/*
 * $Id: 73883fda83d486b29bb3b4397d0b003eaf508137 $
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
import java.util.Collections;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 73883fda83d486b29bb3b4397d0b003eaf508137 $
 */
public class KNJE370G {

    private static final Log log = LogFactory.getLog("KNJE370E.class");

    private static final String SEMEALL = "9";

    private boolean _hasData;

    Param _param;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
            _hasData = printSvf(db2,svf);
    }

    public boolean printSvf(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
    	final Map schregMap = new HashMap();
        boolean hasData = false;
        final List printData = getPrintData(db2, schregMap);
        svf.VrSetForm("KNJE370G.frm", 1);
        int pageno = 1;
        for (final Iterator it = printData.iterator(); it.hasNext();) {
            final PrintData data = (PrintData)it.next();
            data.load(db2, _param);
            final String selectDiv = !"".equals(data._select_div) ? "（" + data._select_div + "）" : "";
            svf.VrsOut("TITLE", _param._nendo + "　指定校推薦入試 選考会資料" + selectDiv); //タイトル
            String collegeName = data._stat_name;
            if(!"".equals(data._acceptance_criterion_b)) collegeName = collegeName + "(" + data._acceptance_criterion_b + ")";
            svf.VrsOut("COLLEGE_NAME", collegeName); //学校名
            svf.VrsOut("FACULTY_NAME", data._facultyname); //学部名
            svf.VrsOut("DEPARTMENT_NAME", data._departmentname); //学科名
            svf.VrsOut("HR_NAME", data._hr_name + data.getAttendNo()); //年組版
            svf.VrsOut("NAME1", data._name); //氏名
            svf.VrsOut("KANA", data._name_kana); //氏名かな
            svf.VrsOut("PAGE1", String.valueOf(pageno));
            svf.VrsOut("PAGE2", String.valueOf(printData.size()));

            int avgHyouteiLine = 1;
            for (final Iterator hit = data._avgHyouteiList.iterator(); hit.hasNext();) {
            	final AvgHyoutei avgHyoutei = (AvgHyoutei) hit.next();
            	if ("TOTAL".equals(avgHyoutei._cd)) {
                    svf.VrsOut("RATE_AVE", avgHyoutei._avg1); //評定平均
                    svf.VrsOut("RANK", avgHyoutei._hyotei); //ランク
                    svf.VrsOut("CREDIT", avgHyoutei._credit); //修得単位数
                    svf.VrsOut("RATE_AVE_DETAIL", avgHyoutei._avg2); //評定平均
                    svf.VrsOut("RATE_AVE_RANK", avgHyoutei._rank1); //評定平均順位
            	} else {
                    svf.VrsOutn(KNJ_EditEdit.getMS932ByteLength(avgHyoutei._name) > 6 ? "CLASS_NAME2" : "CLASS_NAME", avgHyouteiLine, avgHyoutei._name); //教科名
                    svf.VrsOutn("CLASS_AVE", avgHyouteiLine, avgHyoutei._avg1); //教科平均
                    avgHyouteiLine += 1;
            	}
            }

            VrsOutnRenban(svf, "CHURCH", KNJ_EditKinsoku.getTokenList(data._church_name, 50, 3)); //教会名
            svf.VrsOut("RECIEVE_DATE", data._baptism_day); //受洗日
            VrsOutnRenban(svf, "SERVICE", KNJ_EditKinsoku.getTokenList(data._houshi_tou, 60, 3)); //奉仕等
            VrsOutnRenban(svf, "REMARK", KNJ_EditKinsoku.getTokenList(data._remark, 100, 2)); //備考

            svf.VrsOut("DATE", _param._ctrlDate.replace('-', '.'));//日付
            svf.VrsOut("TIME", _param._loginTime); //時間

            //評定平均順位
            printSvfSdiv(svf, data);

            //出欠
            printSvfAbsent(svf, data);

            //特別活動の記録、指導上参考となる諸事項
            printSvfHexamEntremark(svf, data);

            svf.VrEndPage();
            pageno++;
            hasData = true;
        }
        if (hasData) {
            svf.VrEndPage();
        }
        return hasData;
    }

    /**
     * 評定平均順位を印字する
     * @param svf
     * @param student
     */
    private void printSvfSdiv(final Vrw32alp svf, final PrintData data) {

    	svf.VrsOut("TOTAL_ABSENT", "10"); //"10段階成績"
    	for (int i = 0; i < _param._printYearList.size(); i++) {
    		final String year = _param._printYearList.get(i);
            final PrintSdiv printSdiv = data._printSdivMap.get(year);
            if (null != printSdiv) {
            	final String field = String.valueOf(i + 1);
            	final String val = printSdiv._grade_avg_rank + "/" + printSdiv._count;
            	svf.VrsOut("ABSENT" + field, val); //評定平均順位
            }
    	}

    }

    /**
     * 出欠を印字する
     * @param svf
     * @param student
     */
    private void printSvfAbsent(final Vrw32alp svf, final PrintData data) {
    	for (int i = 0; i < _param._printYearList.size(); i++) {
    		final String year = _param._printYearList.get(i);

            final PrintAbsent printAbsent = data._printAbsentMap.get(year);
            if (null != printAbsent) {
            	final String field = String.valueOf(i + 1);
            	int atSick = NumberUtils.isDigits(printAbsent._sick) ? Integer.parseInt(printAbsent._sick) : 0;
            	int atLate = NumberUtils.isDigits(printAbsent._late) ? Integer.parseInt(printAbsent._late) : 0;
            	int atEarly = NumberUtils.isDigits(printAbsent._early) ? Integer.parseInt(printAbsent._early) : 0;
            	final String atAbsent = printAbsent._absent != null ? kekka(printAbsent._absent) : "0";

            	String attend;

            	if(atSick == 0 && atLate == 0 && atEarly == 0 && "0".equals(atAbsent)) {
            		attend = "皆勤";
            	} else {
            		attend = "欠" + atSick + "、遅" + atLate + "、早" + atEarly + "、欠課" + atAbsent;
            	}

            	final int aflen = KNJ_EditEdit.getMS932ByteLength(attend);
            	final String affield = aflen > 26 ? "_2" : "_1";
            	svf.VrsOut("ATTEND" + field + affield, attend); //出欠詳細
            }
    	}

    }
    private String kekka(final BigDecimal absent) {
        final int scale = "3".equals(_param._knjSchoolMst._absentCov) || "4".equals(_param._knjSchoolMst._absentCov) ? 1 : 0;
        final String k = null == absent ? "" : absent.setScale(scale, BigDecimal.ROUND_HALF_UP).toString(); // 欠課時数総合計
        return k;
    }

    /**
     * 特別活動の記録、指導上参考となる諸事項を印字する
     * @param svf
     * @param student
     */
    private void printSvfHexamEntremark(final Vrw32alp svf, final PrintData data) {
    	for (int i = 0; i < _param._printYearList.size(); i++) {
    		final String year = _param._printYearList.get(i);

    		final HexamEntremarkDat recordTotalStudytimeDat = data._hexamEntremarkDatMap.get(year);
    		if (null != recordTotalStudytimeDat) {
                final String field = String.valueOf(i + 1);
                VrsOutnRenban(svf, "SP_ACT" + field, KNJ_EditKinsoku.getTokenList(recordTotalStudytimeDat._specialactrec, 26, 10)); //特別活動の記録
    		}
    	}

        // 指導上参考となる諸事項(6段)
        for (int i = 0; i < _param._printYearList.size(); i++) {
            final String year = _param._printYearList.get(i);

            final HexamEntremarkTrainrefDat hexamEntremarkTrainrefDat1 = data._hexamEntremarkTrainrefDatMap.get(year + "-" + "101");
            final HexamEntremarkTrainrefDat hexamEntremarkTrainrefDat2 = data._hexamEntremarkTrainrefDatMap.get(year + "-" + "102");
            final HexamEntremarkTrainrefDat hexamEntremarkTrainrefDat3 = data._hexamEntremarkTrainrefDatMap.get(year + "-" + "103");
            final HexamEntremarkTrainrefDat hexamEntremarkTrainrefDat4 = data._hexamEntremarkTrainrefDatMap.get(year + "-" + "104");
            final HexamEntremarkTrainrefDat hexamEntremarkTrainrefDat5 = data._hexamEntremarkTrainrefDatMap.get(year + "-" + "105");
            final HexamEntremarkTrainrefDat hexamEntremarkTrainrefDat6 = data._hexamEntremarkTrainrefDatMap.get(year + "-" + "106");

            String remark12 = null;
            if (null != hexamEntremarkTrainrefDat1 && null != hexamEntremarkTrainrefDat2) {
                remark12 = getReplaceLF(hexamEntremarkTrainrefDat1._remark) + "\n" + getReplaceLF(hexamEntremarkTrainrefDat2._remark);
            } else if (null != hexamEntremarkTrainrefDat1) {
                remark12 = getReplaceLF(hexamEntremarkTrainrefDat1._remark);
            } else if (null != hexamEntremarkTrainrefDat2) {
                remark12 = getReplaceLF(hexamEntremarkTrainrefDat2._remark);
            }
            String remark34 = null;
            if (null != hexamEntremarkTrainrefDat3 && null != hexamEntremarkTrainrefDat4) {
                remark34 = getReplaceLF(hexamEntremarkTrainrefDat3._remark) + "\n" + getReplaceLF(hexamEntremarkTrainrefDat4._remark);
            } else if (null != hexamEntremarkTrainrefDat3) {
                remark34 = getReplaceLF(hexamEntremarkTrainrefDat3._remark);
            } else if (null != hexamEntremarkTrainrefDat4) {
                remark34 = getReplaceLF(hexamEntremarkTrainrefDat4._remark);
            }
            String remark56 = null;
            if (null != hexamEntremarkTrainrefDat5 && null != hexamEntremarkTrainrefDat6) {
                remark56 = getReplaceLF(hexamEntremarkTrainrefDat5._remark) + "\n" + getReplaceLF(hexamEntremarkTrainrefDat6._remark);
            } else if (null != hexamEntremarkTrainrefDat5) {
                remark56 = getReplaceLF(hexamEntremarkTrainrefDat5._remark);
            } else if (null != hexamEntremarkTrainrefDat6) {
                remark56 = getReplaceLF(hexamEntremarkTrainrefDat6._remark);
            }

            if (null != remark12) {
                final String field = String.valueOf(i + 1);
                VrsOutnRenban(svf, "LEAD" + field + "_1", KNJ_EditKinsoku.getTokenList(remark12, 64, 7)); //（1）学習における特徴等 （2）行動の特徴、特技等
            }
            if (null != remark34) {
                final String field = String.valueOf(i + 1);
                VrsOutnRenban(svf, "LEAD" + field + "_2", KNJ_EditKinsoku.getTokenList(remark34, 48, 7)); //（3）部活動、ボランティア活動等 （4）資格取得、検定等
            }
            if (null != remark56) {
                final String field = String.valueOf(i + 1);
                VrsOutnRenban(svf, "LEAD" + field + "_3", KNJ_EditKinsoku.getTokenList(remark56, 36, 7)); //（5）その他
            }
        }
    }

    private String getReplaceLF(final String text) {
        if (null != text) {
            return StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(text, "\u000b", ""), "\r\n", ""), "\r", ""), "\n", "");
        } else {
            return text;
        }
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List value) {
        for (int i = 0 ; i < value.size(); i++) {
        	svf.VrsOutn(field, i + 1, (String) value.get(i));
        }
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

    private List getPrintData(final DB2UDB db2, final Map schregMap) throws SQLException {
    	final Map printDataMap = new HashMap();
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
                final String school_kind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                final String attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String name_kana = StringUtils.defaultString(rs.getString("NAME_KANA"));
                final String school_cd = StringUtils.defaultString(rs.getString("SCHOOL_CD"));
                final String stat_name = StringUtils.defaultString(rs.getString("STAT_NAME"));
                final String facultycd = StringUtils.defaultString(rs.getString("FACULTYCD"));
                final String facultyname = StringUtils.defaultString(rs.getString("FACULTYNAME"));
                final String departmentcd = StringUtils.defaultString(rs.getString("DEPARTMENTCD"));
                final String departmentname = StringUtils.defaultString(rs.getString("DEPARTMENTNAME"));
                final String acceptance_criterion_b = StringUtils.defaultString(rs.getString("ACCEPTANCE_CRITERION_B"));
                final String church_name = StringUtils.defaultString(rs.getString("CHURCH_NAME"));
                final String baptism_day = StringUtils.defaultString(rs.getString("BAPTISM_DAY"));
                final String houshi_tou = StringUtils.defaultString(rs.getString("HOUSHI_TOU"));
                final String remark = StringUtils.defaultString(rs.getString("REMARK"));
                final String select_div = StringUtils.defaultString(rs.getString("SELECT_DIV"));
                final PrintData printData = new PrintData(grade, hr_class, hr_name, school_kind, attendno, schregno, seq, name, name_kana, school_cd, stat_name, facultycd, facultyname, departmentcd, departmentname, acceptance_criterion_b, church_name, baptism_day, houshi_tou, remark, select_div);
                rtnList.add(printData);
                if (!printDataMap.containsKey(schregno)) {
                	printDataMap.put(schregno, printData);
                }
                if (!schregMap.containsKey(schregno)) {
                    schregMap.put(schregno, new AvgHyoutei());
                }
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (schregMap.size() > 0) {
        	setGradeStudent(db2, schregMap);
        	log.info(" schregMap size = " + schregMap.size());
        	gethyoteiRankData(db2, printDataMap, schregMap);
        	setAvgRank(schregMap);
        }
        return rtnList;
    }

    private void setAvgRank(final Map<String, AvgHyoutei> schregMap) {
		final Map<BigDecimal, List<AvgHyoutei>> avgListMap = new TreeMap(); // avg1ごとのAvgHyouteiのリスト
		for (final AvgHyoutei wk : schregMap.values()) {
			if (null == wk || !NumberUtils.isNumber(wk._avg1)) {
				continue;
			}
			final BigDecimal avgBd = new BigDecimal(wk._avg1).setScale(1, BigDecimal.ROUND_HALF_UP);
			if (!avgListMap.containsKey(avgBd)) {
				avgListMap.put(avgBd, new ArrayList<AvgHyoutei>());
			}
			avgListMap.get(avgBd).add(wk);
		}

		int rank = 1;
		int count = 0;
		final List<BigDecimal> avgList = new ArrayList<BigDecimal>(avgListMap.keySet());
		// ランクはavg1降順
		for (final ListIterator<BigDecimal> lit = avgList.listIterator(avgList.size()); lit.hasPrevious();) {
			final BigDecimal avg = lit.previous();
			final List<AvgHyoutei> wkList = avgListMap.get(avg);
			for (final AvgHyoutei wk : wkList) {
				wk._rank1 = String.valueOf(rank);
			}
			rank += wkList.size();
			count += wkList.size();
		}
		if (0 != count) {
			for (final AvgHyoutei wk : schregMap.values()) {
				if (!StringUtils.isBlank(wk._rank1)) {
					wk._rank1 = wk._rank1 + "/" + String.valueOf(count);
				}
			}
		}
	}

    // 学年順位をセットするため指定学年の生徒全員分のAvgHyouteiを作成
    private void setGradeStudent(final DB2UDB db2, final Map schregMap) {
    	final String sql = getGradeSql();
    	for (final Iterator it = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "SCHREGNO").iterator(); it.hasNext();) {
    		final String schregno = (String) it.next();
    		if (!schregMap.containsKey(schregno)) {
    			schregMap.put(schregno, new AvgHyoutei());
    		}
    	}
	}

    private String getGradeSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO ");

        return stb.toString();
    }
    private void gethyoteiRankData(final DB2UDB db2, final Map printDataMap, final Map schregMap) {

        KNJDefineSchool defineSchool = new KNJDefineSchool();
        defineSchool.defineCode(db2, _param._ctrlYear);
        final KNJE070_1 knje070_1 = new KNJE070_1(db2, (Vrw32alp) null, defineSchool, (String) null);

        try {
        	final int size = schregMap.size();
        	int count = 0;
            for(Iterator ite = schregMap.keySet().iterator();ite.hasNext();) {
                String schregNo = (String)ite.next();

                List addList = null;
                if (printDataMap.containsKey(schregNo)) {
                	final PrintData printData = (PrintData) printDataMap.get(schregNo);
                	printData._avgHyouteiList = new ArrayList();
					addList = printData._avgHyouteiList;
                }

                final List hyoteiHeikinList = knje070_1.getHyoteiHeikinList(schregNo, _param._ctrlYear, _param._ctrlSemester, _param._knje070Paramap);
                count += 1;
                if (size >= 50) {
                	if (count % (size / 10) == 0) {
                		log.info(" (" + count + " / " + size + ")");
                	}
                }
                for (int i = 0; i < hyoteiHeikinList.size(); i++) {
                	final KNJE070_1.HyoteiHeikin heikin = (KNJE070_1.HyoteiHeikin) hyoteiHeikinList.get(i);
                    AvgHyoutei setWk = new AvgHyoutei();
                    if (null != addList) {
                    	addList.add(setWk);
                    }
            		setWk._cd = heikin.classkey();
            		setWk._name = heikin.classname();
            		final String avg = heikin.avg();
                	setWk._avg2 = NumberUtils.isNumber(avg) ? new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP).toString() : null;
                	setWk._avg1 = NumberUtils.isNumber(avg) ? new BigDecimal(avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString() : null;
                	if ("TOTAL".equals(setWk._cd)) {
                		setWk._credit = heikin.credit();
                		setWk._hyotei = heikin.gaihyo();
                		//log.info(" schregNo " + schregNo + ", heikin = " + heikin + " (" + count + " / " + size + ")");
                		schregMap.put(schregNo, setWk);
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
    	String _cd;
    	String _name; // 教科名
        String _avg1; // 評定平均 (小数点以下1桁)
        String _avg2; // 評定平均 (小数点以下2桁)
        String _hyotei; // 概評
        String _credit; // 単位数 (_cd="TOTAL"のみ)
        String _rank1; // 評定平均順位
        AvgHyoutei() {
        	_cd = new String();
        	_name = new String();
            _avg2 = new String();
            _hyotei = new String();
            _credit = new String();
            _rank1 = new String();
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
        stb.append("   I4.SCHOOL_KIND, ");
        // 出席番号
        stb.append("   I2.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.SEQ, ");
        // 氏名
        stb.append("   I1.NAME, ");
        stb.append("   I1.NAME_KANA, ");
        // 受験(予定)学校名
        stb.append("   L1.SCHOOL_CD AS SCHOOL_CD, ");
        stb.append("   L1.SCHOOL_NAME AS STAT_NAME, ");
        // 学部名
        stb.append("   T1.FACULTYCD, ");
        stb.append("   L2.FACULTYNAME, ");
        // 学科名
        stb.append("   T1.DEPARTMENTCD, ");
        stb.append("   L3.DEPARTMENTNAME, ");
        // 偏差値
        stb.append("   C1.ACCEPTANCE_CRITERION_B, ");
        // 出席協会
        stb.append("   I6.CHURCH_NAME, ");
        stb.append("   I6.BAPTISM_DAY, ");
        stb.append("   I6.HOUSHI_TOU, ");
        stb.append("   I6.REMARK, ");
        // 選考分類
        stb.append("   E054.NAME1 AS SELECT_DIV ");
        stb.append(" FROM ");
        stb.append("   AFT_GRAD_COURSE_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("   INNER JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = T1.YEAR AND I2.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT I4 ON I4.YEAR = I2.YEAR AND I4.GRADE = I2.GRADE ");
        stb.append("   LEFT JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("   LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = T1.STAT_CD AND L2.FACULTYCD = T1.FACULTYCD ");
        stb.append("   LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD = T1.STAT_CD AND L3.FACULTYCD = T1.FACULTYCD AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("   LEFT JOIN SCHREG_CHURCH_REMARK_DAT I6 ON I6.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T2 ");
        stb.append("        ON T2.YEAR          = T1.YEAR ");
        stb.append("       AND T2.SEQ           = T1.SEQ ");
        stb.append("       AND T2.DETAIL_SEQ    = 1 ");
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
        stb.append("   INNER JOIN AFT_GRAD_COURSE_DETAIL_DAT T3 ");
        stb.append("        ON T3.YEAR          = T1.YEAR ");
        stb.append("       AND T3.SEQ           = T1.SEQ ");
        stb.append("       AND T3.DETAIL_SEQ    = 6 ");
        stb.append("       AND T3.REMARK1       = '" + _param._selectDiv + "' ");
        stb.append("   LEFT JOIN NAME_MST E054 ON E054.NAMECD1 = 'E054' AND E054.NAMECD2 = T3.REMARK1 ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
        if ("2".equals(_param._disp)) {
            stb.append("   AND T1.SCHREGNO || '-' || I2.GRADE || I2.HR_CLASS || I2.ATTENDNO IN " + SQLUtils.whereIn(true, _param._categorySelectedIn) + " ");
        } else {
            stb.append("   AND I2.GRADE || I2.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelectedIn) + " ");
        }
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
        final String _school_kind;
        final String _attendno;
        final String _schregno;
        final String _seq;
        final String _name;
        final String _name_kana;
        final String _school_cd;
        final String _stat_name;
        final String _facultycd;
        final String _facultyname;
        final String _departmentcd;
        final String _departmentname;
        final String _acceptance_criterion_b;
        final String _church_name;
        final String _baptism_day;
        final String _houshi_tou;
        final String _remark;
        final String _select_div;

        Map<String, PrintSdiv> _printSdivMap = Collections.emptyMap(); // 評定平均順位
        Map<String, PrintAbsent> _printAbsentMap = Collections.emptyMap();
        Map<String, HexamEntremarkDat> _hexamEntremarkDatMap = Collections.emptyMap(); // 部活動の記録,指導上参考となる諸事項
        Map<String, HexamEntremarkTrainrefDat> _hexamEntremarkTrainrefDatMap = Collections.emptyMap(); // 指導上参考となる諸事項(6段)
        List _avgHyouteiList = Collections.EMPTY_LIST;

        PrintData(final String grade,
                final String hr_class,
                final String hr_name,
                final String school_kind,
                final String attendno,
                final String schregno,
                final String seq,
                final String name,
                final String name_kana,
                final String school_cd,
                final String stat_name,
                final String facultycd,
                final String facultyname,
                final String departmentcd,
                final String departmentname,
                final String acceptance_criterion_b,
                final String church_name,
                final String baptism_day,
                final String houshi_tou,
                final String remark,
                final String select_div
        ) {
            _grade = grade;
            _hr_class = hr_class;
            _hr_name = hr_name;
            _school_kind = school_kind;
            _attendno = attendno;
            _schregno = schregno;
            _seq = seq;
            _name = name;
            _name_kana = name_kana;
            _school_cd = school_cd;
            _stat_name = stat_name;
            _facultycd = facultycd;
            _facultyname = facultyname;
            _departmentcd = departmentcd;
            _departmentname = departmentname;
            _acceptance_criterion_b = acceptance_criterion_b;
            _church_name = church_name;
            _baptism_day = baptism_day;
            _houshi_tou = houshi_tou;
            _remark = remark;
            _select_div = select_div;
        }

        public void load(final DB2UDB db2, final Param param) {
            _printSdivMap = PrintSdiv.getPrintSdivMap(db2, param, _schregno, _school_kind);
            _printAbsentMap = PrintAbsent.getPrintAbsentMap(db2, param, _schregno);
            _hexamEntremarkDatMap = HexamEntremarkDat.getHexamEntremarkDatMap(db2, param, _schregno);
            _hexamEntremarkTrainrefDatMap = HexamEntremarkTrainrefDat.getHexamEntremarkTrainrefDatMap(db2, param, _schregno);
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

    /**
     * 評定平均順位
     */
    private static class PrintSdiv {
        final String _year;
        final String _grade_avg_rank; //順位
        final String _count; //学年数
        public PrintSdiv(
                final String year,
                final String grade_avg_rank,
                final String count
                ) {
            _year = year;
            _grade_avg_rank = grade_avg_rank;
            _count = count;
        }

        public static Map<String, PrintSdiv> getPrintSdivMap(final DB2UDB db2, final Param param, final String schregno, final String schoolKind) {
            final Map<String, PrintSdiv> map = new HashMap<String, PrintSdiv>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSdivSql(param, schregno, schoolKind);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String grade_avg_rank = StringUtils.defaultString(rs.getString("GRADE_AVG_RANK"),"0");
                    final String count = StringUtils.defaultString(rs.getString("COUNT"),"0");
                    final PrintSdiv printSdiv = new PrintSdiv(year, grade_avg_rank, count);
                    map.put(year, printSdiv);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private static String getSdivSql(final Param param, final String schregno, final String schoolKind) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.GRADE_AVG_RANK, ");
            stb.append("   T2.COUNT ");
            stb.append(" FROM ");
            stb.append("   RECORD_RANK_SDIV_DAT T1 ");
            stb.append("   LEFT JOIN (SELECT YEAR, SCHREGNO, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT GROUP BY YEAR, SCHREGNO) REGD ON REGD.YEAR = T1.YEAR AND REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T2 ");
            stb.append("         ON T2.YEAR          = T1.YEAR ");
            stb.append("        AND T2.SEMESTER = T1.SEMESTER AND T2.TESTKINDCD = T1.TESTKINDCD AND T2.TESTITEMCD = T1.TESTITEMCD AND T2.SCORE_DIV = T1.SCORE_DIV  ");
            stb.append("        AND T2.CLASSCD       = T1.CLASSCD ");
            stb.append("        AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("        AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("        AND T2.AVG_DIV       = '1' ");
            stb.append("        AND T2.GRADE         = REGD.GRADE ");
            stb.append("        AND T2.HR_CLASS      = '000' ");
            stb.append("        AND T2.COURSECD      = '0' ");
            stb.append("        AND T2.MAJORCD       = '000' ");
            stb.append("        AND T2.COURSECODE    = '0000' ");
            stb.append(" WHERE ");
            stb.append("  (");
            stb.append("   T1.YEAR              = '" + param._ctrlYear + "' ");
            stb.append("   AND T1.SEMESTER = '1' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '08' ");
            stb.append("   OR ");
            stb.append("   T1.YEAR              < '" + param._ctrlYear + "' ");
            stb.append("   AND T1.SEMESTER = '9' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '08' ");
            stb.append("  )");
            stb.append("   AND T1.CLASSCD       = '99' ");
            stb.append("   AND T1.SCHOOL_KIND   = '" + schoolKind + "' ");
            stb.append("   AND T1.CURRICULUM_CD = '99' ");
            stb.append("   AND T1.SUBCLASSCD    = '999999' ");
            stb.append("   AND T1.SCHREGNO     = '" + schregno + "' ");

            return stb.toString();
        }
    }

    /**
     * 出欠
     */
    private static class PrintAbsent {
        final String _year;
        final String _sick;
        final String _late;
        final String _early;
        final BigDecimal _absent;
        public PrintAbsent(
                final String year,
                final String sick,
                final String late,
                final String early,
                final BigDecimal absent
                ) {
            _year = year;
            _sick = sick;
            _late = late;
            _early = early;
            _absent = absent;
        }

        public static Map<String, PrintAbsent> getPrintAbsentMap(final DB2UDB db2, final Param param, final String schregno) {
            final Map<String ,PrintAbsent> map = new HashMap<String, PrintAbsent>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getAbsentSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String sick = StringUtils.defaultString(rs.getString("SICK"),"0");
                    final String late = StringUtils.defaultString(rs.getString("LATE"),"0");
                    final String early = StringUtils.defaultString(rs.getString("EARLY"),"0");
                    final BigDecimal absent = rs.getBigDecimal("ABSENT");
                    final PrintAbsent printAbsent = new PrintAbsent(year, sick, late, early, absent);
                    map.put(year, printAbsent);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private static String getAbsentSql(final Param param, final String schregno) {
            final String[] dateArray;
            dateArray = StringUtils.split(param._ctrlDate, "-");
            final String setYM = dateArray[0] + dateArray[1];
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH ATTENDREC AS ( ");
            stb.append("   SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SICK, ");
            stb.append("     ACCIDENTNOTICE, ");
            stb.append("     NOACCIDENTNOTICE ");
            stb.append("   FROM SCHREG_ATTENDREC_DAT ");
            stb.append("   WHERE  ");
            stb.append("     YEAR <= '" + param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + schregno + "' ");
            stb.append("     AND SCHOOLCD = '0' ");
            stb.append(" ), ATTENDSEMES AS ( ");
            stb.append("   SELECT  ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(LATE) AS LATE, ");
            stb.append("     SUM(EARLY) AS EARLY ");
            stb.append("   FROM ATTEND_SEMES_DAT ");
            stb.append("   WHERE  ");
            stb.append("     YEAR <= '" + param._ctrlYear + "'  ");
            stb.append("     AND SCHREGNO = '" + schregno + "' ");
            stb.append("   GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" ), ATTENDSEMESDDAT AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(VALUE(CNT, 0)) AS ABSENT ");
            stb.append(" FROM ");
            stb.append("     ATTEND_SEMES_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR <= '" + param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + schregno + "' ");
            stb.append("     AND YEAR || MONTH <= '" + setYM + "' ");
            stb.append("     AND SEQ = '102' ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T3.ABSENT, ");
            stb.append("   CASE WHEN COALESCE(T1.SICK, T1.ACCIDENTNOTICE, T1.NOACCIDENTNOTICE) IS NULL THEN NULL ELSE VALUE(T1.SICK, 0) + VALUE(T1.ACCIDENTNOTICE, 0) + VALUE(T1.NOACCIDENTNOTICE, 0) END AS SICK, ");
            stb.append("   T2.LATE, ");
            stb.append("   T2.EARLY ");
            stb.append(" FROM ");
            stb.append("   ATTENDREC T1 ");
            stb.append("   LEFT JOIN ATTENDSEMES T2 ");
            stb.append("          ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN ATTENDSEMESDDAT T3 ");
            stb.append("          ON T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("   YEAR ");


            return stb.toString();
        }
    }

    /**
     * 特別活動の記録、指導上参考となる諸事項
     */
    private static class HexamEntremarkDat {
        final String _year;
        final String _specialactrec; //特別活動の記録
        final String _train_ref1;    //(1)学習における特徴等 (2)行動の特徴、特技等
        final String _train_ref2;    //(3)部活動、ボランティア活動等 (4)資格取得、検定
        final String _train_ref3;    //(5)その他

        public HexamEntremarkDat(
                final String year,
                final String specialactrec,
                final String train_ref1,
                final String train_ref2,
                final String train_ref3
                ) {
            _year = year;
            _specialactrec = specialactrec;
            _train_ref1 = train_ref1;
            _train_ref2 = train_ref2;
            _train_ref3 = train_ref3;
        }

        public static Map<String, HexamEntremarkDat> getHexamEntremarkDatMap(final DB2UDB db2, final Param param, final String schregno) {
            final Map<String, HexamEntremarkDat> map = new HashMap<String, HexamEntremarkDat>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHexamEntremarkDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String specialactrec = StringUtils.defaultString(rs.getString("SPECIALACTREC"));
                    final String train_ref1 = StringUtils.defaultString(rs.getString("TRAIN_REF1"));
                    final String train_ref2 = StringUtils.defaultString(rs.getString("TRAIN_REF2"));
                    final String train_ref3 = StringUtils.defaultString(rs.getString("TRAIN_REF3"));
                    final HexamEntremarkDat hexamEntremarkDat = new HexamEntremarkDat(year, specialactrec, train_ref1, train_ref2, train_ref3);
                    map.put(year, hexamEntremarkDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private static String getHexamEntremarkDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   YEAR, ");
            stb.append("   SPECIALACTREC, ");
            stb.append("   TRAIN_REF1, ");
            stb.append("   TRAIN_REF2, ");
            stb.append("   TRAIN_REF3 ");
            stb.append(" FROM ");
            stb.append("   HEXAM_ENTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR <= '" + param._ctrlYear + "' ");
            stb.append("   AND SCHREGNO = '" + schregno + "' ");

            return stb.toString();
        }
    }

    /**
     * 指導上参考となる諸事項(6段)
     */
    private static class HexamEntremarkTrainrefDat {
        final String _year;
        final String _train_seq; //001～006
        final String _remark;

        public HexamEntremarkTrainrefDat(
                final String year,
                final String train_seq,
                final String remark
                ) {
            _year = year;
            _train_seq = train_seq;
            _remark = remark;
        }

        public static Map<String, HexamEntremarkTrainrefDat> getHexamEntremarkTrainrefDatMap(final DB2UDB db2, final Param param, final String schregno) {
            final Map<String, HexamEntremarkTrainrefDat> map = new HashMap<String, HexamEntremarkTrainrefDat>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHexamEntremarkTrainrefDatSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String train_seq = StringUtils.defaultString(rs.getString("TRAIN_SEQ"));
                    final String remark = StringUtils.defaultString(rs.getString("REMARK"));
                    final HexamEntremarkTrainrefDat hexamEntremarkTrainrefDat = new HexamEntremarkTrainrefDat(year, train_seq, remark);
                    map.put(year + '-' + train_seq, hexamEntremarkTrainrefDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private static String getHexamEntremarkTrainrefDatSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   YEAR, ");
            stb.append("   TRAIN_SEQ, ");
            stb.append("   REMARK ");
            stb.append(" FROM ");
            stb.append("   HEXAM_ENTREMARK_TRAINREF_DAT ");
            stb.append(" WHERE ");
            stb.append("   YEAR <= '" + param._ctrlYear + "' ");
            stb.append("   AND SCHREGNO = '" + schregno + "' ");
            stb.append("   AND TRAIN_SEQ BETWEEN '101' AND '106' ");
            stb.append(" ORDER BY ");
            stb.append("   YEAR, ");
            stb.append("   TRAIN_SEQ ");

            return stb.toString();
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
        log.fatal("$Revision: 77176 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String[] _categorySelectedIn;
        final String _selectDiv;

        private boolean _isSeireki;
        private final String _nendo;
        private final String _loginTime;

        final String _schoolCd;
        final String _schoolKind;
        final String _disp;
        final String _grade;
        final String _passOnly;
        final List<String> _printYearList;

        final String _selectSchoolKind;
        final String _useSchoolKindField;
        final String _useprgschoolkind;
        final String _semesterName;
        final Map _psMap = new HashMap();

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        final Map _knje070Paramap;

        private KNJSchoolMst _knjSchoolMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _categorySelectedIn = request.getParameterValues("CATEGORY_SELECTED");
            _selectDiv = request.getParameter("SELECT_DIV");

            setSeirekiFlg(db2);
            _nendo = changePrintYear(db2, _ctrlYear, _ctrlDate);
            Date date = new Date();
            SimpleDateFormat sdf_t = new SimpleDateFormat("HH:mm:ss");
            sdf_t.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
            _loginTime = sdf_t.format(date);

            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _disp = request.getParameter("DISP");

            if ("2".equals(_disp)) {
                _grade = request.getParameter("GRADE_HR_CLASS").substring(0, 2);
            } else {
                _grade = request.getParameter("GRADE");
            }
            final String gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' "));
            _printYearList = new ArrayList<String>();
            if (NumberUtils.isDigits(gradeCd)) {
            	for (int i = Integer.parseInt(gradeCd) - 1; i >= 1; i--) {
                    _printYearList.add(String.valueOf(Integer.parseInt(_ctrlYear) - i));
            	}
            }
            _printYearList.add(_ctrlYear);
            log.info(" _printYearList = " + _printYearList);
            _passOnly = request.getParameter("PASS_ONLY");

            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _useSchoolKindField = request.getParameter("useSchool_KindField");
            _useprgschoolkind = request.getParameter("use_prg_schoolkind");
            _semesterName = getSemesterMst(db2, "SEMESTERNAME", _ctrlYear, _ctrlSemester);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }

            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _knje070Paramap = new KNJE070().createParamMap(request);
            _knje070Paramap.put("avgGradesScale", "5");
            _knje070Paramap.put("CERTIFKIND", "008");
            _knje070Paramap.put("CERTIFKIND2", "008");
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

        private String getSemesterMst(final DB2UDB db2, final String field, final String year, final String semester) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = StringUtils.defaultString(rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof
