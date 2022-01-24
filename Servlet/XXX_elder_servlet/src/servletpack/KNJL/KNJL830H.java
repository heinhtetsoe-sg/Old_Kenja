// kanji=漢字
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 08e301445beb9de76a2f6435bebf6c168794cee8 $
 */
public class KNJL830H {

    private static final Log log = LogFactory.getLog("KNJL830H.class");

    private boolean _hasData;

    private Param _param;

    /** 推薦 */
    private static final String SUISEN = "推薦";

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

    	final Map applicantMap = getApplicantMap(db2); //志願者Map

    	if(applicantMap.isEmpty()) {
        	return false;
        }

    	log.debug(applicantMap.size());
    	final int maxLine = 30; //最大印字行
    	int page = 0; // ページ
    	int line = 1; //印字行

    	for (Iterator ite = applicantMap.keySet().iterator(); ite.hasNext();) {
        	final String Key = (String)ite.next();
        	final Applicant applicant = (Applicant)applicantMap.get(Key);

        	if (line > maxLine || page == 0) {
        		if(line > maxLine) svf.VrEndPage();
				page++;
				line = 1;
		    	svf.VrSetForm("KNJL830H.frm", 1);
		    	final String date = _param._date != null ? _param._date.replace("-", "/") : "";
		    	svf.VrsOut("DATE", date); //日付
				svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
				final String sort = "2".equals(_param._sortDiv) ? "（受験番号順）" : "（成績順）";
				svf.VrsOut("TITLE", _param._examYear + "年度　" + "□ □ □　志願者成績一覧表" + sort + "□ □ □"); //タイトル
				final String schoolkind = "1".equals(_param._applicantDiv) ? "中学" : "高校";
				final String div = "・" + ("3".equals(_param._outputDiv) ? "共通" : "1".equals(_param._outputDiv) ? "男子" : "女子");
				svf.VrsOut("EXAM_DIV", schoolkind + _param._testName + _param._kindName + div); //入試区分

				//内申点・教科
				int cnt = 1;
				for(Iterator naishinite = _param._naishinMap.values().iterator(); naishinite.hasNext();) {
					final String className = (String)naishinite.next();
					svf.VrsOut("DIV_HEADER" + cnt, className);
					cnt++;
				}
				svf.VrsOut("DIV_HEADER10", "評定");
				svf.VrsOut("DIV_HEADER11", "行動");
				svf.VrsOut("DIV_HEADER12", "欠席");

				//推薦
				if(_param._suisenFlg) {
					svf.VrsOut("DIV_HEADER13", "面接");
					svf.VrsOut("DIV_HEADER14", "加点");
					svf.VrsOut("DIV_HEADER15", "作文");
					cnt = 16;
					for(Iterator sakubunite = _param._subclassMap.keySet().iterator(); sakubunite.hasNext();) {
						final String key = (String) sakubunite.next();
						final SubclassMst subclass = (SubclassMst) _param._subclassMap.get(key);
						//2:作文科目
						if("2".equals(subclass._remark2)) {
							svf.VrsOut("DIV_HEADER" + cnt, subclass._subclassName);
							cnt++;
						}
					}
				}
				//推薦以外
				else {
					cnt = 13;
					for(Iterator sakubunite = _param._subclassMap.keySet().iterator(); sakubunite.hasNext();) {
						final String key = (String) sakubunite.next();
						final SubclassMst subclass = (SubclassMst) _param._subclassMap.get(key);
						//null:試験科目
						if(subclass._remark2 == null) {
							svf.VrsOut("DIV_HEADER" + cnt, subclass._subclassName);
							cnt++;
						}
					}
					svf.VrsOut("DIV_HEADER" + cnt, "面接");
					cnt++;
					svf.VrsOut("DIV_HEADER" + cnt, "加点");
				}
		    }

        	svf.VrsOutn("EXAM_NO1", line, applicant._examno); //受験番号
        	final String fieldName = getFieldName(applicant._name);
        	svf.VrsOutn("NAME" + fieldName, line, applicant._name); //氏名
        	svf.VrsOutn("SEX", line, applicant._abbv1); //性別

        	//出身校
        	final String schooldiv = applicant._name1 != null ? applicant._name1.substring(0,1) : "";
        	final String schoolName = schooldiv + " " + applicant._finschool_Name;
        	final String fieldSchool = getFieldSchoolName(schoolName);
        	svf.VrsOutn("FINSCHOOL_NAME" + fieldSchool, line, schoolName);

        	//内申点
        	svf.VrsOutn("DIV1", line, applicant._confidential_Rpt01);
        	svf.VrsOutn("DIV2", line, applicant._confidential_Rpt02);
        	svf.VrsOutn("DIV3", line, applicant._confidential_Rpt03);
        	svf.VrsOutn("DIV4", line, applicant._confidential_Rpt04);
        	svf.VrsOutn("DIV5", line, applicant._confidential_Rpt05);
        	svf.VrsOutn("DIV6", line, applicant._confidential_Rpt06);
        	svf.VrsOutn("DIV7", line, applicant._confidential_Rpt07);
        	svf.VrsOutn("DIV8", line, applicant._confidential_Rpt08);
        	svf.VrsOutn("DIV9", line, applicant._confidential_Rpt09);
        	svf.VrsOutn("DIV10", line, "0".equals(applicant._total_All) ? "" : applicant._total_All); //評定合計

        	svf.VrsOutn("DIV11", line, applicant._specialactrec); //行動
    		svf.VrsOutn("DIV12", line, applicant._absence_Days3); //欠席

        	//推薦
        	if(_param._suisenFlg) {
        		//面接
        		if(applicant._interviewMap.containsKey(_param._interviewCd)) {
        			final Score interview = (Score) applicant._interviewMap.get(_param._interviewCd);
        			svf.VrsOutn("DIV13", line, interview._score);
        		}
        		svf.VrsOutn("DIV14", line, getKaten(applicant._remark10, applicant._remark5)); //加点

        		//作文
        		int total = 0;
        		int cnt = 16; //印字列
        		for(Iterator sakubunite = applicant._sakubunMap.keySet().iterator(); sakubunite.hasNext();) {
        			final String key = (String) sakubunite.next();
					final Score sakubun = (Score) applicant._sakubunMap.get(key);
					svf.VrsOutn("DIV" + cnt, line, sakubun._score); //作文スコア
					total += sakubun._score != null ? Integer.valueOf(sakubun._score) : 0;
					cnt++;
        		}
        		BigDecimal b1 = new BigDecimal(total);
        		BigDecimal b2 = new BigDecimal("5.0");
        		BigDecimal b3 = b1.divide(b2,1,BigDecimal.ROUND_HALF_UP);
        		svf.VrsOutn("DIV15", line, "0.0".equals(b3.toString()) ? "" : b3.toString()); //作文平均

        	} //推薦以外
        	else {
        		int cnt = 13; //印字列

        		//試験
        		for(Iterator scoreite = applicant._scoreMap.keySet().iterator(); scoreite.hasNext();) {
        			final String key = (String) scoreite.next();
					final Score score = (Score) applicant._scoreMap.get(key);
					svf.VrsOutn("DIV" + cnt, line, score._score); //試験スコア
					cnt++;
        		}

        		//面接
        		if(applicant._interviewMap.containsKey(_param._interviewCd)) {
        			final Score interview = (Score) applicant._interviewMap.get(_param._interviewCd);
        			svf.VrsOutn("DIV" + cnt, line, interview._score);
        			cnt++;
        		}
        		svf.VrsOutn("DIV" + cnt, line, getKaten(applicant._remark10, applicant._remark5)); //加点
        	}

        	svf.VrsOutn("TOTAL", line, applicant._total4); //合計
        	svf.VrsOutn("RANK", line, applicant._total_Rank4); //順位
        	svf.VrsOutn("REMARK1", line, applicant._remark1); //備考

        	//合否
        	final String status;
        	if("1".equals(applicant._entdiv)) {
        		status = "入　学";
        	} else if("2".equals(applicant._entdiv)) {
        		status = "辞　退";
        	} else if("1".equals(applicant._judgement)) {
        		status = "合　格";
        	} else if("2".equals(applicant._judgement)) {
        		status = "不合格";
        	} else status = "";
            svf.VrsOutn("JUDGE", line, status);

            line++;
		}
    	svf.VrEndPage();

    return true;
    }

    // 加点算出
    private String getKaten(final String str1, final String str2) {
    	BigDecimal b1;
		BigDecimal b2;
		b1 = str1 != null ? new BigDecimal(str1) : new BigDecimal("0.0");
		b2 = str2 != null ? new BigDecimal(str2) : new BigDecimal("0.0");
		BigDecimal b3 = b1.add(b2);
		return "0.0".equals(b3.toString()) ? "" : b3.toString();
    }

    private String getFieldSchoolName(final String str) {
    	final int keta = KNJ_EditEdit.getMS932ByteLength(str);
    	return keta <= 30 ? "1" : "2";
    }

    private String getFieldName(final String str) {
    	final int keta = KNJ_EditEdit.getMS932ByteLength(str);
    	return keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
    }

    // 志願者取得
    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("   BASE.ENTEXAMYEAR, ");
			stb.append("   BASE.APPLICANTDIV, ");
			stb.append("   BASE.EXAMNO, ");
			stb.append("   BASE.TESTDIV, ");
			stb.append("   BASE.NAME, ");
			stb.append("   BASE.SEX, ");
			stb.append("   NAME1.ABBV1, ");
			stb.append("   BASE.FS_CD, ");
			stb.append("   STG.NAME1, ");
			stb.append("   SCH.FINSCHOOL_NAME, ");
			stb.append("   BASE.JUDGEMENT, ");
			stb.append("   RPT.CONFIDENTIAL_RPT01, ");
			stb.append("   RPT.CONFIDENTIAL_RPT02, ");
			stb.append("   RPT.CONFIDENTIAL_RPT03, ");
			stb.append("   RPT.CONFIDENTIAL_RPT04, ");
			stb.append("   RPT.CONFIDENTIAL_RPT05, ");
			stb.append("   RPT.CONFIDENTIAL_RPT06, ");
			stb.append("   RPT.CONFIDENTIAL_RPT07, ");
			stb.append("   RPT.CONFIDENTIAL_RPT08, ");
			stb.append("   RPT.CONFIDENTIAL_RPT09, ");
			stb.append("   RPT.TOTAL_ALL, ");
			stb.append("   RPT.SPECIALACTREC, ");
			stb.append("   RPT.ABSENCE_DAYS3, ");
			stb.append("   DTL1.REMARK10, ");
			stb.append("   RECD.REMARK5, ");
			stb.append("   RECD.REMARK6 AS TOTAL4, ");
			stb.append("   REC.TOTAL_RANK4, ");
			stb.append("   DTL2.REMARK1, ");
			stb.append("   BASE.ENTDIV, ");
			stb.append("   DTL3.REMARK1 AS KINDDIV ");
			stb.append(" FROM ");
			stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT DTL3 ON DTL3.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DTL3.APPLICANTDIV = BASE.APPLICANTDIV AND DTL3.EXAMNO = BASE.EXAMNO AND DTL3.SEQ = '005' ");
			stb.append(" LEFT JOIN ");
			stb.append("   V_NAME_MST NAME1 ON NAME1.YEAR = BASE.ENTEXAMYEAR AND NAME1.NAMECD1 = 'Z002' AND NAME1.NAMECD2 = BASE.SEX ");
			stb.append(" LEFT JOIN ");
			stb.append("   FINSCHOOL_MST SCH ON SCH.FINSCHOOLCD = BASE.FS_CD ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_SETTING_MST STG ON STG.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND STG.APPLICANTDIV = BASE.APPLICANTDIV AND STG.SETTING_CD = 'L015' AND STG.SEQ = SCH.FINSCHOOL_DIV ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_APPLICANTCONFRPT_DAT RPT ON RPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RPT.APPLICANTDIV = BASE.APPLICANTDIV AND RPT.EXAMNO = BASE.EXAMNO ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT DTL1 ON DTL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DTL1.APPLICANTDIV = BASE.APPLICANTDIV AND DTL1.EXAMNO = BASE.EXAMNO AND DTL1.SEQ = '031' ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT DTL2 ON DTL2.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND DTL2.APPLICANTDIV = BASE.APPLICANTDIV AND DTL2.EXAMNO = BASE.EXAMNO AND DTL2.SEQ = '033' ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_RECEPT_DAT REC ON REC.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND REC.APPLICANTDIV = BASE.APPLICANTDIV AND REC.TESTDIV = BASE.TESTDIV AND REC.EXAM_TYPE = '1' AND REC.EXAMNO = BASE.EXAMNO ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_RECEPT_DETAIL_DAT RECD ON RECD.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND RECD.APPLICANTDIV = BASE.APPLICANTDIV AND RECD.TESTDIV = BASE.TESTDIV AND RECD.EXAM_TYPE = '1' AND RECD.SEQ = '009' AND RECD.RECEPTNO = BASE.EXAMNO ");
			stb.append(" WHERE ");
			stb.append("   BASE.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
			stb.append("   BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
			stb.append("   BASE.TESTDIV = '" + _param._testDiv + "' AND ");
			stb.append("   REC.TOTAL_RANK4 IS NOT NULL ");
			if(!"3".equals(_param._outputDiv)) {
				stb.append("     AND BASE.SEX = '" + _param._outputDiv + "' ");
			}
			if(!"ALL".equals(_param._kindDiv)) {
				stb.append("     AND DTL3.REMARK1 = '" + _param._kindDiv + "' ");
			}
			stb.append(" ORDER BY ");
			if("2".equals(_param._sortDiv)) {
				stb.append(" BASE.EXAMNO");
			} else {
				stb.append(" REC.TOTAL_RANK4,BASE.EXAMNO");
			}

			log.debug(" applicant sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				final String entexamyear = rs.getString("ENTEXAMYEAR");
				final String applicantdiv = rs.getString("APPLICANTDIV");
				final String examno = rs.getString("EXAMNO");
				final String testdiv = rs.getString("TESTDIV");
				final String name = rs.getString("NAME");
				final String sex = rs.getString("SEX");
				final String abbv1 = rs.getString("ABBV1");
				final String fs_Cd = rs.getString("FS_CD");
				final String name1 = rs.getString("NAME1");
				final String finschool_Name = rs.getString("FINSCHOOL_NAME");
				final String judgement = rs.getString("JUDGEMENT");
				final String confidential_Rpt01 = rs.getString("CONFIDENTIAL_RPT01");
				final String confidential_Rpt02 = rs.getString("CONFIDENTIAL_RPT02");
				final String confidential_Rpt03 = rs.getString("CONFIDENTIAL_RPT03");
				final String confidential_Rpt04 = rs.getString("CONFIDENTIAL_RPT04");
				final String confidential_Rpt05 = rs.getString("CONFIDENTIAL_RPT05");
				final String confidential_Rpt06 = rs.getString("CONFIDENTIAL_RPT06");
				final String confidential_Rpt07 = rs.getString("CONFIDENTIAL_RPT07");
				final String confidential_Rpt08 = rs.getString("CONFIDENTIAL_RPT08");
				final String confidential_Rpt09 = rs.getString("CONFIDENTIAL_RPT09");
				final String total_All = rs.getString("TOTAL_ALL");
				final String specialactrec = rs.getString("SPECIALACTREC");
				final String absence_Days3 = rs.getString("ABSENCE_DAYS3");
				final String remark10 = rs.getString("REMARK10");
				final String remark5 = rs.getString("REMARK5");
				final String total4 = rs.getString("TOTAL4");
				final String total_Rank4 = rs.getString("TOTAL_RANK4");
				final String entdiv = rs.getString("ENTDIV");
				final String remark1 = rs.getString("REMARK1");

				final Applicant applicant = new Applicant(entexamyear, applicantdiv, examno, testdiv, name, sex, abbv1,
						fs_Cd, name1, finschool_Name, judgement, confidential_Rpt01, confidential_Rpt02,
						confidential_Rpt03, confidential_Rpt04, confidential_Rpt05, confidential_Rpt06,
						confidential_Rpt07, confidential_Rpt08, confidential_Rpt09, total_All, specialactrec,
						absence_Days3, remark10, remark5, total4, total_Rank4, entdiv, remark1);


				applicant.setEntexamScoreInterview(db2); //面接スコア

				if(_param._suisenFlg) {
					applicant.setEntexamScoreSakubun(db2); //作文スコア
				} else {
					applicant.setEntexamScoreShiken(db2); //試験スコア
				}

			    if(!retMap.containsKey(examno)) {
			    	retMap.put(examno, applicant);
			    }
			}
		} catch (final SQLException e) {
			log.error("志願者の基本情報取得でエラー", e);
			throw e;
		} finally {
			db2.commit();
			DbUtils.closeQuietly(null, ps, rs);
		}

    	return retMap;
    }

    private class Score {
    	final String _subclassCd;
    	final String _attendFlg;
    	final String _score;
    	public Score(final String subclassCd, final String attendFlg, final String score) {
    		_subclassCd = subclassCd;
    		_attendFlg = attendFlg;
    		_score = score;
    	}
    }

    private class SubclassMst {
    	final String _subclassCd;
    	final String _subclassName;
    	final String _remark2;

    	public SubclassMst(final String subclassCd, final String subclassName, final String remark2) {
    		_subclassCd = subclassCd;
    		_subclassName = subclassName;
    		_remark2 = remark2;
    	}
    }

    private class Applicant {
        final String _entexamyear;
        final String _applicantdiv;
        final String _examno;
        final String _testdiv;
        final String _name;
        final String _sex;
        final String _abbv1;
        final String _fs_Cd;
        final String _name1;
        final String _finschool_Name;
        final String _judgement;
        final String _confidential_Rpt01; //内申点
        final String _confidential_Rpt02;
        final String _confidential_Rpt03;
        final String _confidential_Rpt04;
        final String _confidential_Rpt05;
        final String _confidential_Rpt06;
        final String _confidential_Rpt07;
        final String _confidential_Rpt08;
        final String _confidential_Rpt09;
        final String _total_All; //9教科合計
        final String _specialactrec; //行動
        final String _absence_Days3; //欠席
        final String _remark10; // 加点1
        final String _remark5; //加点2
        final String _total4; //合計点
        final String _total_Rank4; //順位
        final String _entdiv; //入辞
        final String _remark1; //備考
        final Map _scoreMap; //試験スコア
        final Map _interviewMap; //面接スコア
        final Map _sakubunMap; //作文スコア

		public Applicant(final String entexamyear, final String applicantdiv, final String examno, final String testdiv,
				final String name, final String sex, final String abbv1, final String fs_Cd, final String name1,
				final String finschool_Name, final String judgement, final String confidential_Rpt01,
				final String confidential_Rpt02, final String confidential_Rpt03, final String confidential_Rpt04,
				final String confidential_Rpt05, final String confidential_Rpt06, final String confidential_Rpt07,
				final String confidential_Rpt08, final String confidential_Rpt09, final String total_All,
				final String specialactrec, final String absence_Days3, final String remark10, final String remark5,
				final String total4, final String total_Rank4, final String entdiv, final String remark1) {
		    _entexamyear = entexamyear;
		    _applicantdiv = applicantdiv;
		    _examno = examno;
		    _testdiv = testdiv;
		    _name = name;
		    _sex = sex;
		    _abbv1 = abbv1;
		    _fs_Cd = fs_Cd;
		    _name1 = name1;
		    _finschool_Name = finschool_Name;
		    _judgement = judgement;
		    _confidential_Rpt01 = confidential_Rpt01;
		    _confidential_Rpt02 = confidential_Rpt02;
		    _confidential_Rpt03 = confidential_Rpt03;
		    _confidential_Rpt04 = confidential_Rpt04;
		    _confidential_Rpt05 = confidential_Rpt05;
		    _confidential_Rpt06 = confidential_Rpt06;
		    _confidential_Rpt07 = confidential_Rpt07;
		    _confidential_Rpt08 = confidential_Rpt08;
		    _confidential_Rpt09 = confidential_Rpt09;
		    _total_All = total_All;
		    _specialactrec = specialactrec;
		    _absence_Days3 = absence_Days3;
		    _remark10 = remark10;
		    _remark5 = remark5;
		    _total4 = total4;
		    _total_Rank4 = total_Rank4;
		    _entdiv = entdiv;
		    _remark1 = remark1;
			_scoreMap = new LinkedMap();
			_interviewMap = new LinkedMap();
			_sakubunMap = new LinkedMap();
		}

    	public void setEntexamScoreShiken(final DB2UDB db2) throws SQLException {
    		final StringBuffer stb = new StringBuffer();
    		stb.append(" SELECT ");
    		stb.append("   T1.TESTSUBCLASSCD, ");
    		stb.append("   T1.ATTEND_FLG, ");
    		stb.append("   T1.SCORE ");
    		stb.append(" FROM ");
    		stb.append("   ENTEXAM_SCORE_DAT T1 ");
    		stb.append(" WHERE ");
    		stb.append("   T1.ENTEXAMYEAR = '" + _entexamyear + "' AND ");
    		stb.append("   T1.APPLICANTDIV = '" + _applicantdiv + "' AND ");
    		stb.append("   T1.TESTDIV = '" + _testdiv + "' AND ");
    		stb.append("   T1.EXAM_TYPE = '1' AND ");
    		stb.append("   T1.RECEPTNO = '" + _examno + "' ");
    		if(_param._sakubunList.length != 0) {
    			stb.append("   AND T1.TESTSUBCLASSCD NOT IN " + SQLUtils.whereIn(true, _param._sakubunList));
    		}
    		if(_param._interviewCd != null) {
    			stb.append("   AND T1.TESTSUBCLASSCD <> " + _param._interviewCd );
    		}


    		for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
    			final Score score = new Score(
    					KnjDbUtils.getString(row, "TESTSUBCLASSCD"),
    					KnjDbUtils.getString(row, "ATTEND_FLG"),
    					KnjDbUtils.getString(row, "SCORE")
    				);
    			if(!_scoreMap.containsKey(score._subclassCd)) {
    				_scoreMap.put(score._subclassCd, score);
    			}
    		}
    	}

    	public void setEntexamScoreInterview(final DB2UDB db2) throws SQLException {
    		final StringBuffer stb = new StringBuffer();
    		stb.append(" SELECT ");
    		stb.append("   T1.TESTSUBCLASSCD, ");
    		stb.append("   T1.ATTEND_FLG, ");
    		stb.append("   T1.SCORE, ");
    		stb.append("   T2.NAME1 ");
    		stb.append(" FROM ");
    		stb.append("   ENTEXAM_SCORE_DAT T1 ");
    		stb.append(" LEFT JOIN ");
    		stb.append("   ENTEXAM_SETTING_MST T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.APPLICANTDIV = T1.APPLICANTDIV AND T2.SETTING_CD = 'L027' AND T2.SEQ = T1.SCORE ");
    		stb.append(" WHERE ");
    		stb.append("   T1.ENTEXAMYEAR = '" + _entexamyear + "' AND ");
    		stb.append("   T1.APPLICANTDIV = '" + _applicantdiv + "' AND ");
    		stb.append("   T1.TESTDIV = '" + _testdiv + "' AND ");
    		stb.append("   T1.EXAM_TYPE = '1' AND ");
    		stb.append("   T1.RECEPTNO = '" + _examno + "' ");
   			stb.append("   AND T1.TESTSUBCLASSCD = " + _param._interviewCd );


    		for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
    			final Score score = new Score(
    					KnjDbUtils.getString(row, "TESTSUBCLASSCD"),
    					KnjDbUtils.getString(row, "ATTEND_FLG"),
    					KnjDbUtils.getString(row, "NAME1")
    				);
    			if(!_interviewMap.containsKey(score._subclassCd)) {
    				_interviewMap.put(score._subclassCd, score);
    			}
    		}
    	}

    	public void setEntexamScoreSakubun(final DB2UDB db2) throws SQLException {
    		final StringBuffer stb = new StringBuffer();
    		stb.append(" SELECT ");
    		stb.append("   T1.TESTSUBCLASSCD, ");
    		stb.append("   T1.ATTEND_FLG, ");
    		stb.append("   T1.SCORE ");
    		stb.append(" FROM ");
    		stb.append("   ENTEXAM_SCORE_DAT T1 ");
    		stb.append(" WHERE ");
    		stb.append("   T1.ENTEXAMYEAR = '" + _entexamyear + "' AND ");
    		stb.append("   T1.APPLICANTDIV = '" + _applicantdiv + "' AND ");
    		stb.append("   T1.TESTDIV = '" + _testdiv + "' AND ");
    		stb.append("   T1.EXAM_TYPE = '1' AND ");
    		stb.append("   T1.RECEPTNO = '" + _examno + "' ");
   			stb.append("   AND T1.TESTSUBCLASSCD IN " + SQLUtils.whereIn(true, _param._sakubunList) );

    		for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
    			final Score score = new Score(
    					KnjDbUtils.getString(row, "TESTSUBCLASSCD"),
    					KnjDbUtils.getString(row, "ATTEND_FLG"),
    					KnjDbUtils.getString(row, "SCORE")
    				);
    			if(!_sakubunMap.containsKey(score._subclassCd)) {
    				_sakubunMap.put(score._subclassCd, score);
    			}
    		}
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76037 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _kindDiv; //入試種別
        final String _sortDiv;  //並び区分　1:成績順 2:受験番号順
        final String _outputDiv;  //抽出区分　1:全員 2:男子のみ 3:女子のみ
        final String _testName; //入試区分名称
        final String _kindName; //入試種別名称
        final String _date;
        final Map _subclassMap; //試験教科名
        final Map _naishinMap; //内申教科名
        final String _interviewCd; //面接科目
        final String[] _sakubunList; //作文科目
        final boolean _suisenFlg; //推薦フラグ

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _kindDiv = request.getParameter("KINDDIV");
            _outputDiv = request.getParameter("OUTPUT");
            _sortDiv = request.getParameter("SORT");
          	_date = request.getParameter("LOGIN_DATE");
          	_testName = getTestDivAbbv(db2);
          	_kindName = "ALL".equals(_kindDiv) ? "全て" : getKindDivAbbv(db2);
          	_naishinMap = getL008Name(db2);
          	_interviewCd = getInterviewCd(db2);
          	_sakubunList = getSakubunCd(db2);
          	_subclassMap = getSubclassName(db2);
          	_suisenFlg = ("2".equals(_applicantDiv) && _testName.contains(SUISEN)) ? true : false;
        }

        private String[] getSakubunCd(final DB2UDB db2) {
        	ArrayList retList = new ArrayList();
        	for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT TESTSUBCLASSCD FROM ENTEXAM_TESTSUBCLASSCD_DAT WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' AND EXAM_TYPE = '1' AND REMARK2 = '2' ")) {
    			retList.add(KnjDbUtils.getString(row, "TESTSUBCLASSCD"));
    		}
        	String[] str = new String[retList.size()];
        	for(int i = 0; i < retList.size(); i++) {
        		str[i] = retList.get(i).toString();
        	}
        	return str;
        }

        private String getInterviewCd(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTSUBCLASSCD FROM ENTEXAM_TESTSUBCLASSCD_DAT WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' AND EXAM_TYPE = '1' AND REMARK2 = '1' "));
        }

        private String getTestDivAbbv(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }

        private String getKindDivAbbv(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT KINDDIV_NAME FROM ENTEXAM_KINDDIV_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND KINDDIV = '" + _kindDiv + "' "));
        }

        private Map getL008Name(final DB2UDB db2) {
        	final Map retMap = new LinkedMap();
        	for (final Map<String, String> row : KnjDbUtils.query(db2, "SELECT SEQ,NAME1 FROM ENTEXAM_SETTING_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND SETTING_CD = 'L008' ORDER BY SEQ")) {
    			if(!retMap.containsKey(KnjDbUtils.getString(row, "SEQ"))) {
    				retMap.put(KnjDbUtils.getString(row, "SEQ"), KnjDbUtils.getString(row, "NAME1"));
    			}
    		}
        	return retMap;
        }

        private Map getSubclassName(final DB2UDB db2) {
        	final Map retMap = new LinkedMap();
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("     T1.TESTSUBCLASSCD, ");
        	stb.append("     T1.TESTSUBCLASS_NAME, ");
        	stb.append("     T1.REMARK2 ");
        	stb.append(" FROM ");
        	stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT T1 ");
        	stb.append(" WHERE ");
        	stb.append("     T1.ENTEXAMYEAR = '" + _examYear + "' ");
        	stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
        	stb.append("     AND T1.TESTDIV = '" + _testDiv + "' ");
        	stb.append("     AND T1.EXAM_TYPE= '1' ");
        	stb.append(" ORDER BY ");
        	stb.append("     T1.TESTSUBCLASSCD ");

        	for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
        		final String cd = KnjDbUtils.getString(row, "TESTSUBCLASSCD");
        		final String name = KnjDbUtils.getString(row, "TESTSUBCLASS_NAME");
        		final String remark2 = KnjDbUtils.getString(row, "REMARK2");
        		if(!retMap.containsKey(cd)) {
        			SubclassMst subclass = new SubclassMst(cd,name,remark2);
        			retMap.put(cd, subclass);
        		}
        	}
        	return retMap;
        }
    }

    public static String h_format_Seireki_MD(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }
}

// eof
