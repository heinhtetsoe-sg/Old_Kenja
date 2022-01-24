/*
 * $Id: 4f053edb49ba7eb09e4edcf63c3bc67d0a27c9a6 $
 *
 * 作成日: 2019/05/07
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知票
 */

public class KNJD185L {

    private static final Log log = LogFactory.getLog(KNJD185L.class);

    private static String SEMEALL = "9";

    private boolean _hasData;

    private Param _param;

    private static final String STATUS = "STATUS";

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

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            print(db2, svf, student);
        }
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static String mkString(final List textList, final String comma1) {
        final StringBuffer stb = new StringBuffer();
        String comma = "";
        for (final Iterator it = textList.iterator(); it.hasNext();) {
            final String text = (String) it.next();
            if (StringUtils.isBlank(text)) {
                continue;
            }
            stb.append(comma).append(text);
            comma = comma1;
        }
        return stb.toString();
    }

	private void print(final DB2UDB db2, final Vrw32alp svf, final Student student) {
		//1ページ目
		svf.VrSetForm("KNJD185L_1.frm", 4);

		printKanten(db2, svf, student);
		svf.VrEndPage();

		//2ページ目
		svf.VrSetForm("KNJD185L_2.frm", 4);
		//所見欄(2.特別活動・3.行動の記録・7.総合所見)
		printShoken(db2, svf, student);

		svf.VrEndPage();
		if (SEMEALL.equals(_param._semester)) {
			//3ページ目
			svf.VrSetForm("KNJD185L_3.frm", 1);
			printSyuuryousyou(db2, svf, student);
			svf.VrEndPage();
		}

		_hasData = true;
	}

	private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student) {
		final String yStr = (_param._isSeireki ? _param._year : KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year))) + "年";
        svf.VrsOut("TITLE", yStr + "度通知票（"+(String) _param._semesternameMap.get(_param._semester)+"） "); //タイトル
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno) + "番"): student._attendno;
        svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName) + attendno); //年組番号
        final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nlen > 30 ? "2" : "1";
        svf.VrsOut("NAME"+ nfield, student._name); //年組番号
	}

	private void printKanten(final DB2UDB db2, final Vrw32alp svf, final Student student) {
		//ヘッダ設定
		printHeader(db2, svf, student);

		//2-1.総合的な学習の時間
		if (student._totalStudyTimeStr != null && !"".equals(student._totalStudyTimeStr)) {
		    String[] tstwk = KNJ_EditEdit.get_token(student._totalStudyTimeStr, 60, 2);
            for (int ii = 0;ii < 2;ii++) {
                if (ii < tstwk.length && !"".equals(tstwk[ii])) {
                    svf.VrsOutn("TOTAL_ACT", ii+1, tstwk[ii]);
                }
            }
		}

		//2-2.道徳
		if (student._moralStr != null && !"".equals(student._moralStr)) {
		    String[] tstwk = KNJ_EditEdit.get_token(student._moralStr, 60, 2);
            for (int ii = 0;ii < 2;ii++) {
                if (ii < tstwk.length && !"".equals(tstwk[ii])) {
                    svf.VrsOutn("MORAL", ii+1, tstwk[ii]);
                }
            }
		}

		//3.実力テストの記録
		//科目名称を出力
		int ncnt = 1;
		for (Iterator its = student._proficiencySubclsName.keySet().iterator();its.hasNext();) {
			final String kstr = (String)its.next();
			final String outstr = (String)student._proficiencySubclsName.get(kstr);
			svf.VrsOut("SUBCLASS_NAME" + ncnt, outstr);
			if (ncnt >= 5) {
				break;
			}
			ncnt++;
		}

		//実力テスト成績
		int rowCnt = 1;
		//実力テスト名称分、ループ
		for (Iterator ite = student._proficiencyTestName.keySet().iterator();ite.hasNext();) {
			final String k1str = (String)ite.next();
			//科目数分、ループ
			int colCnt = 1;
			for (Iterator itr = student._proficiencySubclsName.keySet().iterator();itr.hasNext();) {
				final String k2str = (String)itr.next();
				final String  kstr = k1str + "-" + k2str;
				for (Iterator itps = student._proficiencyScore.keySet().iterator();itps.hasNext();) {
					final String kkstr = (String)itps.next();
					//対象データがあるか、チェック(※学期は見ない)
					if (kkstr.endsWith(kstr)) {
					    final ProficiencyDat outwk = (ProficiencyDat)student._proficiencyScore.get(kkstr);
						svf.VrsOutn("MOCK_SCORE"+colCnt, rowCnt, outwk._score);
						svf.VrsOutn("MOCK_DEVI"+colCnt, rowCnt, outwk._deviation);
					}

				}
				if (colCnt >= 5) {
					break;
				}
				colCnt++;
			}
			if (rowCnt >= 3) {
				break;
			}
			rowCnt++;
		}
		//実力テスト(合計、順位)
		//実力テスト名称分、ループ
		rowCnt = 1;
		for (Iterator ite = student._proficiencyTestName.keySet().iterator();ite.hasNext();) {
			final String kstr = (String)ite.next();
			for (Iterator itptr = student._proficiencyTotalRank.keySet().iterator();itptr.hasNext();) {
				final String kkstr = (String)itptr.next();
				//データが存在するか、チェック
				if (kkstr.endsWith(kstr)) {
					ProficiencyTotalRank outwk = (ProficiencyTotalRank)student._proficiencyTotalRank.get(kkstr);
					svf.VrsOutn("MOCK_SCORE_TOTAL", rowCnt, outwk._score);
					svf.VrsOutn("MOCK_DEVI_TOTAL", rowCnt, outwk._deviation);
					svf.VrsOutn("MOCK_RANK1", rowCnt, outwk._rank);
					svf.VrsOutn("MOCK_RANK2", rowCnt, outwk._count);
				}
			}
			if (rowCnt >= 3) {
				break;
			}
			rowCnt++;
		}

		//学習の記録の順位
		final String rankStr1 = (String)student._totalRankMap.get("1");
		if (!"".equals(rankStr1)) {
			final String[] cutwk = StringUtils.split(rankStr1, "/");
			if (cutwk != null && cutwk.length > 1 && !"".equals(cutwk[0].trim()) && !"".equals(cutwk[1].trim())) {
		        svf.VrsOut("RANK1_1", cutwk[0].trim());
		        svf.VrsOut("RANK1_2", cutwk[1].trim());
			}
		}
		final String rankStr2 = (String)student._totalRankMap.get("9");
		if (!"".equals(rankStr2)) {
			final String[] cutwk = StringUtils.split(rankStr2, "/");
			if (cutwk != null && cutwk.length > 1 && !"".equals(cutwk[0].trim()) && !"".equals(cutwk[1].trim())) {
                svf.VrsOut("RANK2_1", cutwk[0].trim());
                svf.VrsOut("RANK2_2", cutwk[1].trim());
			}
		}

		//※パターン4のフォームのため、学習の記録は最後に出力。
		//1.学習の記録
		boolean printLineFlg = false;
		for (int i = 0; i < Math.min(student._viewClassList.size(), 9); i++) {
			final ViewClass vc = (ViewClass) student._viewClassList.get(i);
			int nclsnamelen = KNJ_EditEdit.getMS932ByteLength(vc._classname);
			final String[] token = KNJ_EditEdit.get_token(vc._classname, 2, (int)Math.ceil((double)nclsnamelen / 2.0));
			int vi = 0;
    		for (vi = 0; vi <  vc.getViewSize(); vi++) {
    			svf.VrsOut("GRPCD", String.valueOf(i));
    			if (null != token) {
    				if (vi < token.length) {
    					svf.VrsOut("CLASS_NAME", token[vi]); // 教科名
    				}
    			}
    			final String viewcd = vc.getViewCd(vi);
    			svf.VrsOut("VIEW_NAME", vc.getViewName(vi)); // 観点
        		final Map stat1 = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), "1");
        		if (stat1 != null) {
    			    svf.VrsOut("EVA1", StringUtils.defaultString(KnjDbUtils.getString(stat1, STATUS), "")); // 評価(前期)
        		}
        		final Map stat9 = getMappedMap(getMappedMap(vc._viewcdSemesterStatDatMap, viewcd), "9");
        		if (stat9 != null) {
    			    svf.VrsOut("EVA9", StringUtils.defaultString(KnjDbUtils.getString(stat9, STATUS), "")); // 評価(学年)
        		}
        		final String smKey1 = "1-" + vc._subclasscd;  //固定文字列は取得するデータの学期
        		if (student._scoreMap.containsKey(smKey1)) {
  			        svf.VrsOut("VAL1", StringUtils.defaultString((String)student._scoreMap.get(smKey1), "")); // 評定(前期)
        		}
	            if (!"1".equals(_param._semester)) {
	        		final String smKey2 = "2-" + vc._subclasscd;  //固定文字列は取得するデータの学期
        		    if (student._scoreMap.containsKey(smKey2)) {
   			            svf.VrsOut("VAL9", StringUtils.defaultString((String)student._scoreMap.get(smKey2), "")); // 評定(学年)
  			        }
        		}
        		svf.VrEndRecord();
    		}
    		if (vi < token.length) {
    			for (;vi < token.length;vi++) {
        			svf.VrsOut("GRPCD", String.valueOf(i));
					svf.VrsOut("CLASS_NAME", token[vi]); // 教科名
		    		svf.VrEndRecord();
    			}
    		}
    		//svf.VrEndRecord();
    		printLineFlg = true;
		}
		//表の出力が無い場合でも他の出力があれば帳票出力しないといけないので、設定。
		if (!printLineFlg) {
		    svf.VrEndRecord();
		}
	}

	private void printAttendance(final Vrw32alp svf, final Student student) {
        if (student._attendSemesDatList.size() > 0) {
        	for (Iterator ite = student._attendSemesDatList.iterator();ite.hasNext();) {
        		final AttendSemesDat att = (AttendSemesDat)ite.next();
        		final int colLine = "9".equals(att._semester) ? 3 : Integer.parseInt(att._semester);
        	    svf.VrsOutn("LESSON", colLine, String.valueOf(att._lesson)); // 出欠 授業日数
        	    svf.VrsOutn("SUSPEND", colLine, String.valueOf(att._suspend + att._mourning)); // 出欠 忌引・出席停止日数
        	    svf.VrsOutn("MUST", colLine, String.valueOf(att._mlesson)); // 出欠 出席すべき日数
        	    svf.VrsOutn("ABSENT", colLine, String.valueOf(att._sick)); // 出欠 欠席日数
        	    svf.VrsOutn("ATTEND", colLine, String.valueOf(att._present)); // 出欠 出席日数
        	    svf.VrsOutn("LATE", colLine, String.valueOf(att._late)); // 出欠 遅刻回数
        	    svf.VrsOutn("EARLY", colLine, String.valueOf(att._early)); // 出欠 早退回数
        	}
        }
	}

	private void printTsushinaran(final Vrw32alp svf, final Student student, final Param param) {
		final String semesStr = "9".equals(_param._semester) ? "2" : _param._semester; //学年末は後期のデータ?
		final HReportRemarkDat dat = (HReportRemarkDat) student._hReportRemarkDatMap.get(semesStr);
	    if (null != dat) {
	    	final int keta;
	    	final int gyo;
	    	if ("P".equals(param._schoolKind) && param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P != null && param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P.indexOf("*") > 0) {
	    		String[] cutrng = StringUtils.split(param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_P, '*');
	    		keta = Integer.parseInt(cutrng[0].trim());
	    		gyo = Integer.parseInt(cutrng[1].trim());
	    	} else if ("J".equals(param._schoolKind) && param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J != null && param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J.indexOf("*") > 0) {
	    		String[] cutrng = StringUtils.split(param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, '*');
	    		keta = Integer.parseInt(cutrng[0].trim());
	    		gyo = Integer.parseInt(cutrng[1].trim());
	    	} else if ("H".equals(param._schoolKind) && param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_H != null && param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_H.indexOf("*") > 0) {
	    		String[] cutrng = StringUtils.split(param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_H, '*');
	    		keta = Integer.parseInt(cutrng[0].trim());
	    		gyo = Integer.parseInt(cutrng[1].trim());
	    	} else {
	    		keta = 20;
	    		gyo = 4;
	    	}
	    	final List tokenList = KNJ_EditKinsoku.getTokenList(dat._communication, keta * 2, gyo);
	    	for (int j = 0; j < Math.min(10, tokenList.size()); j++) {
	    		final int line = j + 1;
	    		svf.VrsOutn("NOTE", line, (String) tokenList.get(j)); // 通信欄
	    	}
	    }
	}

	private void printShoken(final DB2UDB db2, final Vrw32alp svf, final Student student) {
		//ヘッダ設定
		printHeader(db2, svf, student);

		//2.特別活動の記録
		svf.VrsOut("SP_ACT1_1", student._committee2_1); // 学級活動1
		svf.VrsOut("SP_ACT1_2", student._committee2_2); // 学級活動2
		svf.VrsOut("SP_ACT2", student._committee1);     // 生徒会活動
		svf.VrsOut("SP_ACT3", student._committee3);     // 学校行事
		svf.VrsOut("SP_ACT4", student._club);           // 部活動

        //4.出欠の記録
		printAttendance(svf, student);

		//5.賞および任命の記録
		int ridx = 1;
		for (Iterator its = student._certAndCommList.iterator();its.hasNext();) {
			CertAndCommit outwk = (CertAndCommit)its.next();
			svf.VrsOutn("PRIZE_DATE", ridx, outwk._detailSDate);
			svf.VrsOutn("PRIZE", ridx, outwk._content);
			ridx++;
		}

		//6.身体の記録
		if (student._medexamDate != null && !"".equals(student._medexamDate)) {
		    final String outDate = KNJ_EditDate.h_format_JP(db2, student._medexamDate);
		    svf.VrsOut("MEASURING_DATE", outDate);
		}
		svf.VrsOut("HIGHT", StringUtils.defaultString(student._height, ""));
		svf.VrsOut("WEIGHT", StringUtils.defaultString(student._weight, ""));
		svf.VrsOut("SEAT_HIGHT", StringUtils.defaultString(student._seatHeight, ""));
		//7.総合所見と認印
		//総合所見
        printTsushinaran(svf, student, _param);
		//特に指示が無かったので、認印は出力制御不要となった。

		//特別活動
		final HReportRemarkDat dat = (HReportRemarkDat) student._hReportRemarkDatMap.get(_param._semester);
		if (null != dat) {
			final List tokenList = KNJ_EditKinsoku.getTokenList(dat._remark1, 40);
			for (int j = 0; j < Math.min(3, tokenList.size()); j++) {
				final int line = j + 1;
				svf.VrsOutn("SPECIAL5", line, (String) tokenList.get(j)); // 特別活動の記録
			}
		}

		final List tokenList = KNJ_EditKinsoku.getTokenList(student._attendSemesRemark, 20);
		for (int j = 0; j < Math.min(4, tokenList.size()); j++) {
			final int line = j + 1;
			svf.VrsOutn("ATTEND_REMARK1", line, (String) tokenList.get(j)); // 出欠備考
		}

		//3.行動の記録
		//※パターン4のフォームのため、行動の記録は最後に出力。
		int lineCnt = 1;
		boolean printLineFlg = false;
		for (Iterator ite = student._behaviorSemesDatMap.keySet().iterator();ite.hasNext();) {
			final String kstr = (String)ite.next();
			BehaviorSemesDat outwk = (BehaviorSemesDat)student._behaviorSemesDatMap.get(kstr);
			final int nDetailStrWkLen =  KNJ_EditEdit.getMS932ByteLength(outwk._viewname);
			String[] detailStrWk = KNJ_EditEdit.get_token(outwk._viewname, 76, (int)(Math.ceil((double)nDetailStrWkLen / 76.0)));
			for (int lpcnt = 0;lpcnt < detailStrWk.length;lpcnt++) {
				svf.VrsOut("GRPCD", String.valueOf(lineCnt));
			    svf.VrsOut("ACT_ITEM", outwk._codename);
			    svf.VrsOut("ACT_EFFECT", detailStrWk[lpcnt]);
			    if (outwk._semesterRecordMap.containsKey("1")) {
                    svf.VrsOut("VAL1", (String)outwk._semesterRecordMap.get("1"));
			    }
			    if (!"1".equals(_param._semester)) {
				    if (outwk._semesterRecordMap.containsKey("2")) {
                        svf.VrsOut("VAL9", (String)outwk._semesterRecordMap.get("2"));
	    			}
		    	}
				svf.VrEndRecord();
			}
			lineCnt++;
			printLineFlg = true;
		}
		if (!printLineFlg) {
			svf.VrEndRecord();
		}
	}

    private void printSyuuryousyou(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
    	final String nfield = nlen > 30 ? "2" : "1";
        svf.VrsOut("NAME" + nfield, student._name);
        final String outBirthStr = KNJ_EditDate.h_format_JP(db2, student._birthday) + " 生";
        svf.VrsOut("BIRTHDAY", outBirthStr);
        svf.VrsOut("GRADE", _param._gradeName);
        final String outDateStr = KNJ_EditDate.h_format_JP(db2, _param._compDate);
        svf.VrsOut("DATE", outDateStr);
        svf.VrsOut("PRINCIPAL_NAME", "　　　　　　　　　　　　　　" + _param._certifSchoolPrincipalName);
        svf.VrsOut("SCHOOL_NAME", "　　　　　　 　　　　" + _param._certifSchoolSchoolName);
//        if (_param._schoolstampFilePath != null) {
//            svf.VrsOut("SCHOOL_STAMP", _param._schoolstampFilePath);
//        }
    }

    private static class Student {
		final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _birthday;
//        final String _gradeCourse;
//        final String _hrClassName1;
        final String _staffname;
        List _attendSemesDatList = new ArrayList(); //AttendSemesDat
        String _totalStudyTimeStr; //総合的な学習の時間(評価のみ)
        String _moralStr; //道徳(評価のみ)
        List _certAndCommList = new ArrayList(); //賞及び任命の記録
        Map _totalRankMap = new HashMap(); //学習の記録の順位
        Map _proficiencyTestName = new LinkedMap();  //実力テスト実施回名称
        Map _proficiencySubclsName = new LinkedMap(); //実力テスト科目名(選択科目等を考慮すると、生徒別に保持するのが良いと判断)
        Map _proficiencyScore = new LinkedMap(); //実力テスト成績
        Map _proficiencyTotalRank = new LinkedMap(); //実力テスト合計
        String _medexamDate; //検査年月日
        String _height; //身長
        String _weight; //体重
        String _seatHeight; //座高
        final Map _hReportRemarkDatMap = new HashMap(); // 所見
        final List _viewClassList = new ArrayList(); //観点情報
        final Map _scoreMap = new LinkedMap(); //成績(990008)情報
        final Map _semesterRankMap = new HashMap();
        final Map _behaviorSemesDatMap = new TreeMap();
		private String _attendSemesRemark = "";
        private String _club;
		private String _committee1;
		private String _committee2_1;
		private String _committee2_2;
		private String _committee3;

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String gradeCourse, final String hrClassName1, final String staffname, final String birthday) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
//            _gradeCourse = gradeCourse;
//            _hrClassName1 = hrClassName1;
            _staffname = staffname;
            _birthday = birthday;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            final String sql = getStudentSql(param);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map rs = (Map) it.next();
                final String schregno = KnjDbUtils.getString(rs, "SCHREGNO");
                final String name = "1".equals(KnjDbUtils.getString(rs, "USE_REAL_NAME")) ? KnjDbUtils.getString(rs, "REAL_NAME") : KnjDbUtils.getString(rs, "NAME");
                final String hrName = KnjDbUtils.getString(rs, "HR_NAME");
                final String attendno = KnjDbUtils.getString(rs, "ATTENDNO");
                final String gradeCourse = KnjDbUtils.getString(rs, "COURSE");
                final String hrClassName1 = KnjDbUtils.getString(rs, "HR_CLASS_NAME1");
                final String staffname = KnjDbUtils.getString(rs, "STAFFNAME");
                final String birthday = KnjDbUtils.getString(rs, "BIRTHDAY");
                final Student student = new Student(schregno, name, hrName, attendno, gradeCourse, hrClassName1, staffname, birthday);
                studentList.add(student);
            }

            AttendSemesDat.setAttendSemesDatList(db2, param, studentList);
            ViewClass.setViewClassList(db2, param, studentList);
            HReportRemarkDat.setHReportRemarkDatMap(db2, param, studentList);
            BehaviorSemesDat.setBehaviorSemesDatMap(db2, param, studentList);
            setClubCommittee(db2, param, studentList);
            setProficiencyInfo(db2, param, studentList);
            setSubclassScoreMap(db2, param, studentList);

            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("  SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.SEMESTER ");
            stb.append("  FROM    SCHREG_REGD_DAT T1 ");
            stb.append("          , SEMESTER_MST T2 ");
            stb.append("  WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
//            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
//            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
//            stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
//            stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append("                         AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
//            stb.append("                           OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
//            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//            stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//            stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append("                         AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(" ) ");
            //メイン表
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T7.HR_NAME, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    T1.COURSE, ");
            stb.append("    T5.NAME, ");
            stb.append("    T5.REAL_NAME, ");
            stb.append("    T5.BIRTHDAY, ");
            stb.append("    T7.HR_CLASS_NAME1, ");
            stb.append("    CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("    STF.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("    SCHNO_A T1 ");
            stb.append("    INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append("    LEFT JOIN STAFF_MST STF ON STF.STAFFCD = T7.TR_CD1 ");
            stb.append(" ORDER BY ATTENDNO");
            return stb.toString();
        }
        private static void setProficiencyInfo(final DB2UDB db2, final Param param, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                getProficiencyTestName(db2, param, student);
                getProficiencySubclsName(db2, param, student);
                getProficiencyDat(db2, param, student);
                getProficiencyTotalRank(db2, param, student);
                getStudentRank(db2, param, student);
                getCertificationAndCommition(db2, param, student);
                getTotalStudyTime(db2, param, student);
                getMedexDat(db2, param, student);
            }
        }

        private static void setClubCommittee(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            final StringBuffer stb = new StringBuffer();
            stb.append("  ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME, ");
            stb.append("     CASE WHEN T1.SDATE BETWEEN SEM1.SDATE AND TSEM.EDATE OR ");
            stb.append("               T1.EDATE BETWEEN SEM1.SDATE AND TSEM.EDATE OR ");
            stb.append("               T1.SDATE < SEM1.SDATE AND TSEM.EDATE < VALUE(T1.EDATE, '9999-12-31') THEN 1 END AS FLG ");
            stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
            stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append(" INNER JOIN SEMESTER_MST SEM1 ON SEM1.YEAR = '" + param._year + "' ");
            stb.append("     AND SEM1.SEMESTER = '1' ");
            stb.append(" INNER JOIN SEMESTER_MST TSEM ON TSEM.YEAR = '" + param._year + "' ");
            stb.append("     AND TSEM.SEMESTER = '" + param._semester + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("     AND T2.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD ");
            try {
            	final String sql = stb.toString();
            	if (param._isOutputDebug) {
            		log.info(" club sql = " + stb.toString());
            	}

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final List list = new ArrayList();
                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map rs = (Map) rit.next();
                        final String clubname = KnjDbUtils.getString(rs, "CLUBNAME");
                        final String flg = KnjDbUtils.getString(rs, "FLG");

                        if (!"1".equals(flg) || StringUtils.isBlank(clubname) || list.contains(clubname)) {
                            continue;
                        }
                        list.add(clubname);
                    }
                    student._club = mkString(list, "　");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            final StringBuffer stb2 = new StringBuffer();
            stb2.append(" SELECT ");
            stb2.append("     T1.SEMESTER, ");
            stb2.append("     T1.SCHREGNO, ");
            stb2.append("     T1.COMMITTEE_FLG, ");
            stb2.append("     T1.COMMITTEECD, ");
            stb2.append("     T1.CHARGENAME, ");
            stb2.append("     T2.COMMITTEENAME ");
            stb2.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb2.append(" LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
            stb2.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
            stb2.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb2.append(" WHERE ");
            stb2.append("     T1.YEAR = '" + param._year + "' ");
            //stb2.append("     AND T1.SEMESTER <> '9' ");
            stb2.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb2.append("     AND T1.COMMITTEE_FLG IN ('1', '2', '3') ");
            stb2.append("     AND T1.SCHREGNO = ? ");
            stb2.append(" ORDER BY ");
            stb2.append("     T1.SEMESTER, ");
            stb2.append("     T1.COMMITTEE_FLG, ");
            stb2.append("     T1.COMMITTEECD ");
            try {

            	final String sql = stb2.toString();
            	if (param._isOutputDebug) {
            		log.info(" comm sql = " + stb2.toString());
            	}

                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final List list1 = new ArrayList();
                    final List list2_1 = new ArrayList();
                    final List list2_2 = new ArrayList();
                    final List list3 = new ArrayList();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map rs = (Map) rit.next();
                    	final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                        final String committeeFlg = KnjDbUtils.getString(rs, "COMMITTEE_FLG");
                        if ("9".equals(semester) && !"2".equals(committeeFlg)) continue;
                        List list;
                        String name;
                        if ("2".equals(committeeFlg)) {  //学級活動
                            name = KnjDbUtils.getString(rs, "COMMITTEENAME");
                            if ("0".equals(semester)) {  //semester="0"は、この学校特殊のコード。"9"と同じ意味。
                            	list2_1.add(name);
                            	list2_2.add(name);
                            	continue;
                            } else if ("1".equals(semester)) {
                                list = list2_1;
                            } else {
                                list = list2_2;
                            }
                        } else if ("3".equals(committeeFlg)) {  //学校行事
                        	name = KnjDbUtils.getString(rs, "COMMITTEENAME");
                            list = list3;
                        } else if ("1".equals(committeeFlg)) {  //生徒会
                            name = KnjDbUtils.getString(rs, "COMMITTEENAME");
                            list = list1;
                        } else {
                        	continue;
                        }
                        if (StringUtils.isBlank(name) || list.contains(name)) {
                            continue;
                        }
                        list.add(name);
                    }
                    student._committee1 = mkString(list1, "　");
                    student._committee2_1 = mkString(list2_1, "　");
                    student._committee2_2 = mkString(list2_2, "　");
                    student._committee3 = mkString(list3, "　");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static void setSubclassScoreMap(final DB2UDB db2, final Param param, final List studentList) {

        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   T10.SEMESTER, ");
        	stb.append("   T10.SCHREGNO, ");
        	stb.append("   T10.CLASSCD, ");
        	stb.append("   T10.SCHOOL_KIND, ");
        	stb.append("   T10.CURRICULUM_CD, ");
        	stb.append("   T10.SUBCLASSCD, ");
        	stb.append("   T10.SCORE ");
            stb.append(" FROM ");
            stb.append("   RECORD_SCORE_DAT T10 ");
            stb.append(" WHERE ");
            stb.append("   T10.YEAR = '" + param._year + "' ");
            stb.append("   AND T10.SEMESTER <= '" + param._semester + "' ");
            stb.append("   AND T10.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("   AND T10.TESTKINDCD = '99' ");
            stb.append("   AND T10.TESTITEMCD = '00' ");
            stb.append("   AND T10.SCORE_DIV = '08' ");
            stb.append("   AND T10.SCHREGNO = ? ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                	final Student student = (Student) it.next();

                	student._scoreMap.clear();
                	for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                		final Map row = (Map) rit.next();

                		final String semester     = KnjDbUtils.getString(row, "SEMESTER");
                		final String classcd      = KnjDbUtils.getString(row, "CLASSCD");
                		final String schoolKind   = KnjDbUtils.getString(row, "SCHOOL_KIND");
                		final String curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
                		final String subclasscd   = KnjDbUtils.getString(row, "SUBCLASSCD");
                		final String score        = KnjDbUtils.getString(row, "SCORE");

                		final String kstr = semester + "-" + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                		student._scoreMap.put(kstr, score);
                	}
                }
            } catch (Exception e) {
            	log.error("exception!", e);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }
    }

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
                final int offdays
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
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        param._sdate,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                final Integer zero = new Integer(0);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    	final Map rs = (Map) rit.next();

                        final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                        //合計もあるので、"9"も取得しておく。
//                        if (!"9".equals(semester)) {
//                        	continue;
//                        }
                        final int lesson = KnjDbUtils.getInt(rs, "LESSON", zero).intValue();
                        final int suspend = KnjDbUtils.getInt(rs, "SUSPEND", zero).intValue() + KnjDbUtils.getInt(rs, "VIRUS", zero).intValue() + KnjDbUtils.getInt(rs, "KOUDOME", zero).intValue();
                        final int mourning = KnjDbUtils.getInt(rs, "MOURNING", zero).intValue();
                        final int mlesson = KnjDbUtils.getInt(rs, "MLESSON", zero).intValue();
                        final int sick = KnjDbUtils.getInt(rs, "SICK", zero).intValue();
                        final int absent = KnjDbUtils.getInt(rs, "ABSENT", zero).intValue();
                        final int present = KnjDbUtils.getInt(rs, "PRESENT", zero).intValue();
                        final int late = KnjDbUtils.getInt(rs, "LATE", zero).intValue();
                        final int early = KnjDbUtils.getInt(rs, "EARLY", zero).intValue();
                        final int transferDate = KnjDbUtils.getInt(rs, "TRANSFER_DATE", zero).intValue();
                        final int offdays = KnjDbUtils.getInt(rs, "OFFDAYS", zero).intValue();

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester, lesson, suspend, mourning, mlesson, sick, absent, present, late, early, transferDate, offdays);
                        student._attendSemesDatList.add(attendSemesDat);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }

            try {
            	final String attendSemesInState = (String) AttendAccumulate.getHasuuMap(db2, param._year, param._sdate, param._date).get("attendSemesInState");

            	final StringBuffer sql = new StringBuffer();
                if (!SEMEALL.equals(param._semester)) {
                    sql.append(" SELECT SEMESTER, MONTH, REMARK1 FROM ATTEND_SEMES_REMARK_DAT ");
                    sql.append(" WHERE YEAR = '" + param._year + "' ");
                    sql.append("   AND SEMESTER || MONTH IN " + attendSemesInState);
                    sql.append("   AND SCHREGNO = ? ");
                    sql.append(" ORDER BY SEMESTER, INT(MONTH) + CASE WHEN INT(MONTH) < 4 THEN 12 ELSE 0 END ");

                    ps = db2.prepareStatement(sql.toString());

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        student._attendSemesRemark = "";

                        for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                            final Map row = (Map) rit.next();
                            final String remark1 = KnjDbUtils.getString(row, "REMARK1");
                            if (StringUtils.isBlank(remark1)) {
                                continue;
                            }
                            if (StringUtils.isBlank(student._attendSemesRemark)) {
                                student._attendSemesRemark += " ";
                            }
                            student._attendSemesRemark += remark1;
                        }
                    }
                } else {
                    sql.append(" SELECT ");
                    sql.append("    ATTENDREC_REMARK ");
                    sql.append(" FROM ");
                    sql.append("    HREPORTREMARK_DAT ");
                    sql.append(" WHERE ");
                    sql.append("    YEAR = '" + param._year + "' ");
                    sql.append("    AND SEMESTER = '" + param._semester + "' ");
                    sql.append("    AND SCHREGNO = ? ");

                    ps = db2.prepareStatement(sql.toString());

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        student._attendSemesRemark = "";

                        for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                            final Map row = (Map) rit.next();
                            final String remark1 = KnjDbUtils.getString(row, "ATTENDREC_REMARK");
                            if (StringUtils.isBlank(remark1)) {
                                continue;
                            }
                            student._attendSemesRemark = remark1;
                        }
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        public String toString() {
            return "AttendSemesDat(" + _semester + ": [" + _lesson + ", " + _suspend  + ", " + _mourning  + ", " + _mlesson  + ", " + _sick  + ", " + _present + ", " + _late + ", " + _early + "])";
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _totalstudytime;     // 総合的な学習の時間
        final String _specialactremark;   // 特別活動の記録・その他
        final String _communication;      // 担任からの所見
        final String _remark1;            // 特別活動の記録・学級活動
        final String _remark2;            // 特別活動の記録・生徒会活動
        final String _remark3;            // 特別活動の記録・学校行事
        final String _attendrecRemark;    // 出欠備考

        public HReportRemarkDat(
                final String semester,
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String attendrecRemark) {
            _semester = semester;
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _attendrecRemark = attendrecRemark;
        }

        public static void setHReportRemarkDatMap(final DB2UDB db2, final Param param, final List studentList) {
            final String sql = getHReportRemarkDatSql(param);

            PreparedStatement ps = null;

            try {
            	ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                	final Student student = (Student) it.next();

                	student._hReportRemarkDatMap.clear();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    	final Map rs = (Map) rit.next();
                        final String semester = KnjDbUtils.getString(rs, "SEMESTER");
                        final String totalstudytime = KnjDbUtils.getString(rs, "TOTALSTUDYTIME");
                        final String specialactremark = KnjDbUtils.getString(rs, "SPECIALACTREMARK");
                        final String communication = KnjDbUtils.getString(rs, "COMMUNICATION");
                        final String remark1 = KnjDbUtils.getString(rs, "REMARK1");
                        final String remark2 = KnjDbUtils.getString(rs, "REMARK2");
                        final String remark3 = KnjDbUtils.getString(rs, "REMARK3");
                        final String attendrecRemark = KnjDbUtils.getString(rs, "ATTENDREC_REMARK");
                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication, remark1, remark2, remark3, attendrecRemark);
                        student._hReportRemarkDatMap.put(semester, hReportRemarkDat);
                    }
                }
            } catch (Exception e) {
            	log.error("exception!", e);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }

        private static String getHReportRemarkDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }

        public String toString() {
            return "HReportRemarkDat(" + _semester + ": totalstudytime=" + _totalstudytime + ", specialactremark=" + _specialactremark + ", communication=" + _communication + ", remark1=" + _remark1 + ", remark2=" + _remark2 + ", remark3=" + _remark3 + ", attendrecRemark= " + _attendrecRemark + ")";
        }
    }

    private static class BehaviorSemesDat {
        final String _code;
        final String _codename;
        final String _viewname;
        final Map _semesterRecordMap = new HashMap();

        public BehaviorSemesDat(
            final String code,
            final String codename,
            final String viewname
        ) {
            _code = code;
            _codename = codename;
            _viewname = viewname;
        }

        public static void setBehaviorSemesDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            try {
            	final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.CODE, ");
                stb.append("     T1.CODENAME, ");
                stb.append("     T1.VIEWNAME, ");
                stb.append("     L1.SCHREGNO, ");
                stb.append("     L1.SEMESTER, ");
                stb.append("     L1.RECORD, ");
                stb.append("     L2.ABBV1 ");
                stb.append(" FROM BEHAVIOR_SEMES_MST T1 ");
                stb.append(" LEFT JOIN BEHAVIOR_SEMES_DAT L1 ON ");
                stb.append("    L1.YEAR = T1.YEAR ");
                stb.append("    AND L1.SEMESTER <= '" + param._semester + "' ");
                stb.append("    AND L1.SCHREGNO = ? ");
                stb.append("    AND L1.CODE = T1.CODE ");
                stb.append(" LEFT JOIN NAME_MST L2 ON ");
                stb.append("    L2.NAMECD1 = 'D036' ");
                stb.append("    AND L2.NAMECD2 = L1.RECORD ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param._year + "' ");
                stb.append("    AND T1.GRADE = '" + param._grade + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.CODE ");
                stb.append("   , L1.SEMESTER ");

                final String sql = stb.toString();
                if (param._isOutputDebug) {
                	log.info(" behavior sql = " + sql);
                }
                ps = db2.prepareStatement(sql);

            	for (final Iterator it = studentList.iterator(); it.hasNext();) {
            		final Student student = (Student) it.next();

            		student._behaviorSemesDatMap.clear();

            		for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
            			final Map row = (Map) rit.next();
            			final String code = KnjDbUtils.getString(row, "CODE");
            			if (null == student._behaviorSemesDatMap.get(code)) {
            				final String codename = KnjDbUtils.getString(row, "CODENAME");
            				final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
            				final BehaviorSemesDat bsd = new BehaviorSemesDat(code, codename, viewname);
            				student._behaviorSemesDatMap.put(code, bsd);
            			}
        				final String semester = KnjDbUtils.getString(row, "SEMESTER");
        				if (null != semester) {
//        				    final String record = KnjDbUtils.getString(row, "RECORD");
        					final BehaviorSemesDat bsd = (BehaviorSemesDat) student._behaviorSemesDatMap.get(code);
//        					bsd._semesterRecordMap.put(semester, KnjDbUtils.getString(row, "ABBV1"));
        					if ("1".equals(KnjDbUtils.getString(row, "RECORD"))) {
        						bsd._semesterRecordMap.put(semester, "〇");
        					}
        				}
            		}
            	}
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }

        public String toString() {
        	return "BehaviorSemesDat(" + _code + ", " + _viewname + ", " + _semesterRecordMap + ")";
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _electDiv;
        final String _classname;
        final String _subclassname;
        final List _viewList;
//        final Map _semesterScoreMap = new HashMap();
        final Map _viewcdSemesterStatDatMap = new HashMap();

        ViewClass(
                final String classcd,
                final String subclasscd,
                final String electDiv,
                final String classname,
                final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _electDiv = electDiv;
            _classname = classname;
            _subclassname = subclassname;
            _viewList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public String toString() {
        	return "ViewClass(" + _subclasscd + ", " + _classname + ")";
        }

        public static void setViewClassList(final DB2UDB db2, final Param param, final List studentList) {
            final String sql = getViewClassSql(param);
            if (param._isOutputDebug) {
            	log.info(" view class sql = " + sql);
            }

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                	final Student student = (Student) it.next();

                	student._viewClassList.clear();

                	for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                		final Map row = (Map) rit.next();

                		final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                		final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                		final String viewcd = KnjDbUtils.getString(row, "VIEWCD");

                		ViewClass viewClass = null;
                		for (final Iterator vit = student._viewClassList.iterator(); vit.hasNext();) {
                			final ViewClass viewClass0 = (ViewClass) vit.next();
                			if (viewClass0._subclasscd.equals(subclasscd)) {
                				viewClass = viewClass0;
                				break;
                			}
                		}

                		if (null == viewClass) {
                			final String electDiv = KnjDbUtils.getString(row, "ELECTDIV");
                			final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                			final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");

                			viewClass = new ViewClass(classcd, subclasscd, electDiv, classname, subclassname);
                			student._viewClassList.add(viewClass);
                		}

                		final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                		if (viewname != null && !"".equals(viewname)) {
                			boolean findflg = false;
                			for (int ii = 0;ii < viewClass.getViewSize();ii++) {
                				if (viewcd.equals(viewClass.getViewCd(ii))) {
                				    findflg = true;
                				}
                			}
                			if (!findflg) {
                		        viewClass.addView(viewcd, viewname);
                			}
                		}

                		final String semester = KnjDbUtils.getString(row, "SEMESTER");
                		if (null == semester) {
                			continue;
                		}
                		//viewClass._semesterScoreMap.put(semester, KnjDbUtils.getString(row, "SCORE"));

                		final Map stat = getMappedMap(getMappedMap(viewClass._viewcdSemesterStatDatMap, viewcd), semester);
                		stat.put(STATUS, KnjDbUtils.getString(row, STATUS));
                	}
                }

            } catch (Exception e) {
            	log.error("exception!", e);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }

        private static String getViewClassSql(final Param param) {

            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.GRADE ");
            stb.append("   , CLM.CLASSCD ");
            stb.append("   , VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("   , VALUE(SCLM.SUBCLASSORDERNAME2, SCLM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , VALUE(SCLM.ELECTDIV, '0') AS ELECTDIV ");
            stb.append("   , T1.VIEWCD ");
            stb.append("   , T1.VIEWNAME ");
            stb.append("   , REC.SEMESTER ");
            stb.append("   , REC.SCHREGNO ");
            stb.append("   , REC.STATUS ");
            stb.append("   , NM_D029.NAME1 AS STATUS_NAME1 ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
            stb.append("         AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN SUBCLASS_MST SCLM ON SCLM.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND SCLM.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND SCLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND SCLM.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT REC ON REC.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND REC.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND REC.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND REC.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("         AND REC.VIEWCD = T2.VIEWCD ");
            stb.append("         AND REC.YEAR = T2.YEAR ");
            stb.append("         AND REC.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND REC.SCHREGNO = ? ");
            stb.append("     LEFT JOIN NAME_MST NM_D029 ON NM_D029.NAMECD1 = 'D029' ");
            stb.append("         AND NM_D029.ABBV1 = REC.STATUS ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(SCLM.ELECTDIV, '0'), ");
            stb.append("     VALUE(CLM.SHOWORDER3, -1), ");
            stb.append("     CLM.CLASSCD, ");
            stb.append("     VALUE(SCLM.SHOWORDER3, -1), ");
            stb.append("     SCLM.CLASSCD, ");
            stb.append("     SCLM.SCHOOL_KIND, ");
            stb.append("     SCLM.CURRICULUM_CD, ");
            stb.append("     SCLM.SUBCLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }

    }

///////////////////////////

    //実力テスト実施回名称
    private static void getProficiencyTestName(final DB2UDB db2, final Param param, final Student student) {
        final String sql = getProficiencyTestNameSql(param, student);
        if (param._isOutputDebug) {
            log.debug(" getProficiencyTestName sql = "+sql);
        }
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	student._proficiencyTestName.put(KnjDbUtils.getString(rs, "PROFICIENCYCD"), KnjDbUtils.getString(rs, "PROFICIENCYNAME1"));
        }
    }
    private static String getProficiencyTestNameSql(final Param param, final Student student) {
        final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT DISTINCT ");
    	stb.append("  T1.PROFICIENCYCD, ");
    	stb.append("  T2.PROFICIENCYNAME1 ");
    	stb.append(" FROM ");
    	stb.append("   PROFICIENCY_SUBCLASS_YDAT T1 ");
    	stb.append("   LEFT JOIN PROFICIENCY_MST T2 ");
    	stb.append("     ON T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
    	stb.append("    AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ");
    	stb.append("   INNER JOIN SCHREG_REGD_DAT T3 ");
    	stb.append("      ON T3.YEAR = T1.YEAR ");
    	stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
    	stb.append("     AND T3.COURSECD = T1.COURSECD ");
    	stb.append("     AND T3.MAJORCD = T1.MAJORCD ");
    	stb.append("     AND T3.COURSECODE = T1.COURSECODE ");
    	stb.append("     AND T3.GRADE = T1.GRADE ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + param._year + "' ");
    	stb.append("   AND T1.SEMESTER <= '" + param._semester + "' ");
    	stb.append("   AND T1.DIV = '03' ");
    	stb.append("   AND T1.PROFICIENCYDIV = '02' ");
        stb.append("   AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("   AND T3.SCHREGNO = '" + student._schregno + "' ");
    	stb.append(" ORDER BY ");
    	stb.append("  T1.PROFICIENCYCD ");
        return stb.toString();
    }

    //実力テスト科目名称
    private static void getProficiencySubclsName(final DB2UDB db2, final Param param, final Student student) {
        final String sql = getProficiencySubclsNameSql(param, student);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	student._proficiencySubclsName.put(KnjDbUtils.getString(rs, "PROFICIENCY_SUBCLASS_CD"), KnjDbUtils.getString(rs, "SUBCLASS_ABBV"));
        }

    }
    private static String getProficiencySubclsNameSql(final Param param, final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.PROFICIENCY_SUBCLASS_CD, ");
        stb.append("   T2.SUBCLASS_ABBV ");
        stb.append(" FROM ");
        stb.append("   PROFICIENCY_SUBCLASS_YDAT T1 ");
        stb.append("   LEFT JOIN PROFICIENCY_SUBCLASS_MST T2 ");
        stb.append("     ON T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("   INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("      ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T3.COURSECD = T1.COURSECD ");
        stb.append("     AND T3.MAJORCD = T1.MAJORCD ");
        stb.append("     AND T3.COURSECODE = T1.COURSECODE ");
        stb.append("     AND T3.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SEMESTER <= '" + param._semester + "' ");
        stb.append("   AND T1.DIV = '03' ");
        stb.append("   AND T1.PROFICIENCYDIV = '02' ");
        stb.append("   AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("   AND T3.SCHREGNO = '" + student._schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.PROFICIENCY_SUBCLASS_CD ");
        return stb.toString();
    }

    //実力テスト成績
    private static void getProficiencyDat(final DB2UDB db2, final Param param, final Student student) {
        final String sql = getProficiencyDatSql(param, student);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	final String semester = KnjDbUtils.getString(rs, "SEMESTER");
        	final String proficiencyCd = KnjDbUtils.getString(rs, "PROFICIENCYCD");
        	final String profSubclsCd = KnjDbUtils.getString(rs, "PROFICIENCY_SUBCLASS_CD");
        	final String profSubclsAbbv = KnjDbUtils.getString(rs, "SUBCLASS_ABBV");
        	final String score = KnjDbUtils.getString(rs, "SCORE");
        	final String deviation = NumberUtils.isNumber(KnjDbUtils.getString(rs, "DEVIATION")) ? new BigDecimal(KnjDbUtils.getString(rs, "DEVIATION")).setScale(1, BigDecimal.ROUND_HALF_UP).toString() : null;
        	ProficiencyDat addwk = new ProficiencyDat(semester, proficiencyCd, profSubclsCd, profSubclsAbbv, score, deviation);
        	student._proficiencyScore.put(semester + "-" + proficiencyCd + "-" + profSubclsCd, addwk);
        }
    }
    private static String getProficiencyDatSql(final Param param, final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("  T1.SEMESTER, ");
        stb.append("  T1.PROFICIENCYCD, ");
        stb.append("  T1.PROFICIENCY_SUBCLASS_CD, ");
        stb.append("  T2.SUBCLASS_ABBV, ");
        stb.append("  T1.SCORE, ");
        stb.append("  T3.DEVIATION ");
        stb.append(" from ");
        stb.append("  PROFICIENCY_DAT T1 ");
        stb.append("  LEFT JOIN PROFICIENCY_SUBCLASS_MST T2 ");
        stb.append("    ON T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("  LEFT JOIN PROFICIENCY_RANK_DAT T3 ");
        stb.append("    ON  T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T3.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
        stb.append("    AND T3.PROFICIENCYCD = T1.PROFICIENCYCD ");
        stb.append("    AND T3.RANK_DATA_DIV = '03' ");
        stb.append("    AND T3.RANK_DIV = '01' ");
        stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T3.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append(" WHERE ");
        stb.append("  T1.YEAR = '" + param._year + "' ");
        stb.append("  AND T1.SEMESTER <= '" + param._semester + "' ");
        stb.append("  AND T1.PROFICIENCYDIV = '02' ");
        stb.append("  AND T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("  T1.SEMESTER, ");
        stb.append("  T1.PROFICIENCYCD, ");
        stb.append("  T1.PROFICIENCY_SUBCLASS_CD ");
        return stb.toString();
    }
    private static class ProficiencyDat {
    	final String _semester;
    	final String _proficiencyCd;
    	final String _profSubclsCd;
    	final String _profSubclsAbbv;
    	final String _score;
    	final String _deviation;
    	ProficiencyDat(
    	    	final String semester,
    	    	final String proficiencyCd,
    	    	final String profSubclsCd,
    	    	final String profSubclsAbbv,
    	    	final String score,
    	    	final String deviation
    			) {
        	_semester = semester;
        	_proficiencyCd = proficiencyCd;
        	_profSubclsCd = profSubclsCd;
        	_profSubclsAbbv = profSubclsAbbv;
        	_score = score;
        	_deviation = deviation;
    	}
    }

    //実力テスト合計(得点/偏差値)/順位(SEMESTER、PROFICIENCYCDをキーとして保持。)
    private static void getProficiencyTotalRank(final DB2UDB db2, final Param param, final Student student) {
        final String sql = getProficiencyTotalRankSql(param, student);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	final String semester = KnjDbUtils.getString(rs, "SEMESTER");
        	final String proficiencyCd = KnjDbUtils.getString(rs, "PROFICIENCYCD");
        	final String rank = KnjDbUtils.getString(rs, "RANK");
        	final String score = KnjDbUtils.getString(rs, "SCORE");
        	final String deviation = NumberUtils.isNumber(KnjDbUtils.getString(rs, "DEVIATION")) ? new BigDecimal(KnjDbUtils.getString(rs, "DEVIATION")).setScale(1, BigDecimal.ROUND_HALF_UP).toString() : null;
        	final String count = KnjDbUtils.getString(rs, "COUNT");
        	ProficiencyTotalRank addwk = new ProficiencyTotalRank(semester, proficiencyCd, rank, score, deviation, count);
        	student._proficiencyTotalRank.put(semester + proficiencyCd, addwk);
        }
    }
    private static String getProficiencyTotalRankSql(final Param param, final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.PROFICIENCYCD, ");
        stb.append("   T1.RANK, ");
        stb.append("   T1.SCORE, ");
        stb.append("   T1.DEVIATION, ");
        stb.append("   T2.COUNT ");
        stb.append(" FROM ");
        stb.append("  PROFICIENCY_RANK_DAT T1 ");
        stb.append("  INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("    ON T3.YEAR = T1.YEAR ");
        stb.append("   AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("   AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("  LEFT JOIN PROFICIENCY_AVERAGE_DAT T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("   AND T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
        stb.append("   AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ");
        stb.append("   AND T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append("   AND T2.DATA_DIV = '1' ");
        stb.append("   AND T2.AVG_DIV = '03' ");
        stb.append("   AND T2.GRADE = T3.GRADE ");
        stb.append("   AND T2.HR_CLASS = '000' ");
        stb.append("   AND T2.COURSECD = T3.COURSECD ");
        stb.append("   AND T2.MAJORCD = T3.MAJORCD ");
        stb.append("   AND T2.COURSECODE = T3.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '" + param._year + "' ");
        stb.append("    AND T1.SEMESTER <= '" + param._semester + "' ");
        stb.append("    AND T1.PROFICIENCYDIV = '02' ");
        stb.append("    AND T1.RANK_DATA_DIV = '03' ");
        stb.append("    AND T1.RANK_DIV = '01' ");
        stb.append("    AND T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append("    AND T1.PROFICIENCY_SUBCLASS_CD = '999999' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.PROFICIENCYCD ");
        return stb.toString();
    }
    private static class ProficiencyTotalRank {
    	final String _semester;
    	final String _proficiencyCd;
    	final String _rank;
    	final String _score;
    	final String _deviation;
    	final String _count;
    	ProficiencyTotalRank(
    	    	final String semester,
    	    	final String proficiencyCd,
    	    	final String rank,
    	    	final String score,
    	    	final String deviation,
    	    	final String count
    			) {
        	_semester = semester;
        	_proficiencyCd = proficiencyCd;
        	_rank = rank;
        	_score = score;
        	_deviation = deviation;
        	_count = count;
    	}
    }

    //学習の観点の順位
    private static void getStudentRank(final DB2UDB db2, final Param param, final Student student) {
        final String sql = getStudentRankSql(param, student);
        if (param._isOutputDebug) {
            log.debug(" getStudentRank sql = " + sql);
        }
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	student._totalRankMap.put(KnjDbUtils.getString(rs, "SEMESTER"), KnjDbUtils.getString(rs, "RANK"));
        }
    }
    private static String getStudentRankSql(final Param param, final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T11.SEMESTER, ");
        stb.append("   RTRIM(CHAR(T11.GRADE_RANK)) || '/' || RTRIM(CHAR(T12.COUNT)) AS RANK ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT T11 ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T12 ON T12.YEAR = T11.YEAR ");
        stb.append("         AND T12.SEMESTER = T11.SEMESTER ");
        stb.append("         AND T12.TESTKINDCD = T11.TESTKINDCD ");
        stb.append("         AND T12.TESTITEMCD = T11.TESTITEMCD ");
        stb.append("         AND T12.SCORE_DIV = T11.SCORE_DIV ");
        stb.append("         AND T12.SCHOOL_KIND = T11.SCHOOL_KIND ");
        stb.append("         AND T12.CLASSCD = T11.CLASSCD ");
        stb.append("         AND T12.CURRICULUM_CD = T11.CURRICULUM_CD ");
        stb.append("         AND T12.SUBCLASSCD = T11.SUBCLASSCD ");
        stb.append("         AND T12.AVG_DIV = '1' ");
        stb.append("         AND T12.GRADE = '" + param._grade + "' ");
        stb.append("         AND T12.HR_CLASS = '000' ");
        stb.append("         AND T12.COURSECD = '0' ");
        stb.append("         AND T12.MAJORCD = '000' ");
        stb.append("         AND T12.COURSECODE = '0000' ");
        stb.append(" WHERE ");
        stb.append("   T11.YEAR = '" + param._year + "' ");
        stb.append("   AND T11.SEMESTER <= '" + param._semester + "' ");
        stb.append("   AND T11.TESTKINDCD = '99' ");
        stb.append("   AND T11.TESTITEMCD = '00' ");
        stb.append("   AND T11.SCORE_DIV = '08' ");
        stb.append("   AND T11.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("   AND T11.CLASSCD = '99' ");
        stb.append("   AND T11.CURRICULUM_CD = '99' ");
        stb.append("   AND T11.SUBCLASSCD = '999999' ");
        stb.append("   AND T11.SCHREGNO = '" + student._schregno + "' ");
        return stb.toString();
    }

    //賞及び任命の記録
    private static void getCertificationAndCommition(final DB2UDB db2, final Param param, final Student student) {
        final String sql = getCertificationAndCommitionSql(param, student);
        if (param._isOutputDebug) {
            log.debug(" getCertificationAndCommition sql = " + sql);
        }
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	final String detailCd = KnjDbUtils.getString(rs, "DETAILCD");
        	final String detailSDate = StringUtils.substring(KnjDbUtils.getString(rs, "DETAIL_SDATE"), 5).replace('-', '・');
        	final String content = KnjDbUtils.getString(rs, "CONTENT");
        	CertAndCommit addwk = new CertAndCommit(detailCd, detailSDate, content);
        	student._certAndCommList.add(addwk);
        }
    }
    private static String getCertificationAndCommitionSql(final Param param, final Student student) {
		final String semesStr = "9".equals(param._semester) ? "2" : param._semester; //学年末は後期のデータ?
        final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("   T1.DETAILCD, ");
    	stb.append("   T1.DETAIL_SDATE, ");
    	stb.append("   T2.NAME1 || ':' || T1.CONTENT AS CONTENT ");
    	stb.append(" FROM ");
    	stb.append("   SCHREG_DETAILHIST_DAT T1 ");
    	stb.append("   LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'H303' AND T2.NAMECD2 = T1.DETAILCD ");
    	stb.append("   LEFT JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = '" + semesStr + "' ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + param._year + "' ");
    	stb.append("   AND T1.DETAIL_DIV = '1' ");
    	//ログイン年度分取得するので、YEAR指定で事足りると判断して、登録日付は指定不要 -> 学期分を取得。年度末は最終学期で取得。
    	//stb.append("   AND T1.DETAIL_SDATE BETWEEN '" + param._year + "-04-01' AND '" + param._date + "' ");
    	stb.append("   AND T1.SCHREGNO = '" + student._schregno + "' ");
    	stb.append("   AND T1.DETAIL_SDATE BETWEEN T3.SDATE AND T3.EDATE ");
    	stb.append(" ORDER BY ");
    	stb.append("   T1.DETAIL_SDATE, ");
    	stb.append("   T1.DETAILCD ");
        return stb.toString();
    }
    private static class CertAndCommit {
    	final String _detailCd;
    	final String _detailSDate;
    	final String _content;
    	CertAndCommit(
    			final String detailCd,
    			final String detailSDate,
    			final String content
    			) {
    		_detailCd = detailCd;
    		_detailSDate = detailSDate;
    		_content = content;
    	}
    }

    //総合的な学習の時間、道徳
    private static void getTotalStudyTime(final DB2UDB db2, final Param param, final Student student) {
    	student._totalStudyTimeStr = "";
        final String sql1 = getTotalStudyTimeSql(param, student, "1");  //総合的な学習の時間を指定
        for (final Iterator it = KnjDbUtils.query(db2, sql1).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	student._totalStudyTimeStr = KnjDbUtils.getString(rs, KnjDbUtils.getString(rs,"COLUMNNAME"));
        	break; //先頭データだけ取得。
        }
        student._moralStr = "";
        final String sql2 = getTotalStudyTimeSql(param, student, "2");  //道徳を指定
        for (final Iterator it = KnjDbUtils.query(db2, sql2).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	student._moralStr = KnjDbUtils.getString(rs, KnjDbUtils.getString(rs,"COLUMNNAME"));
        	break; //先頭データだけ取得。
        }
    }
    private static String getTotalStudyTimeSql(final Param param, final Student student, final String setRemark2) {
        final StringBuffer stb = new StringBuffer();
        final String semeStr = "9".equals(param._semester) ? "2" : param._semester;
    	stb.append(" SELECT ");
    	stb.append("  T1.*, ");
    	stb.append("  T2.COLUMNNAME ");
    	stb.append(" FROM ");
    	stb.append("  RECORD_TOTALSTUDYTIME_DAT T1 ");
    	stb.append("  INNER JOIN RECORD_TOTALSTUDYTIME_ITEM_MST T2 ");
    	stb.append("    ON T2.CLASSCD = T1.CLASSCD ");
    	stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
    	stb.append("   AND T2.REMARK2 = '" + setRemark2 + "' ");
    	stb.append(" WHERE ");
    	stb.append("  T1.YEAR = '" + param._year + "' ");
    	stb.append("  AND T1.SEMESTER = '" + semeStr + "' ");
    	//stb.append("  AND (SUBSTR(T1.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = '" + param._d008Namecd1 + "') OR T1.SUBCLASSCD LIKE '90%') ");
    	stb.append("  AND T1.SCHREGNO = '" + student._schregno + "' ");
    	stb.append(" ORDER BY T2.COLUMNNAME DESC ");  //優先度は、TOTALSTUDYTIME > TOTALSTUDYACT > REMARK1。既存の総合的な学習の時間がTOTALSTUDYTIMEだったので、TOTALSTUDYTIMEを最優先としている。
        return stb.toString();
    }

    private static void getMedexDat(final DB2UDB db2, final Param param, final Student student) {
        final String sql = getMedexDatSql(param, student);
        if (param._isOutputDebug) {
            log.debug("getMedexDat sql = "+ sql);
        }
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map rs = (Map) it.next();
        	student._medexamDate = KnjDbUtils.getString(rs, "DATE");
        	student._height = KnjDbUtils.getString(rs, "HEIGHT");
        	student._weight = KnjDbUtils.getString(rs, "WEIGHT");
        	student._seatHeight = KnjDbUtils.getString(rs, "SITHEIGHT");
        }
    }
    private static String getMedexDatSql(final Param param, final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  T1.DATE, ");
        stb.append("  T2.HEIGHT, ");
        stb.append("  T2.WEIGHT, ");
        stb.append("  T2.SITHEIGHT ");
        stb.append(" FROM ");
        stb.append("  MEDEXAM_HDAT T1 ");
        stb.append("  LEFT JOIN MEDEXAM_DET_DAT T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("  T1.YEAR = '" + param._year + "' ");
        stb.append("  AND T1.SCHREGNO = '" + student._schregno + "' ");
        return stb.toString();
    }

/////////////////////////

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 76174 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _sdate;
        final String _date;
        final String _compDate;
        final String _gradeHrclass;
        final String _grade;
        final String _gradeName;
        final String[] _categorySelected;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;

        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark8;
        final String _certifSchoolPrincipalName;

        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_H;

        /** 各学校における定数等設定 */
        private KNJSchoolMst _knjSchoolMst;

        final Map _attendParamMap;
        final Map _semesternameMap;
        final boolean _isOutputDebug;
        final boolean _isSeireki;

        final String _schoolKind;
        final String _selectedInState;
        final String _schoolstampFilePath;
        final String _d008Namecd1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _compDate = request.getParameter("COMP_DATE");

            _sdate = getFirstDate(db2);
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            if (_grade != null) {
            	_gradeName = getGradeName(db2);
            } else {
            	_gradeName = "";
            }
            _categorySelected = request.getParameterValues("category_selected");
            _documentRoot = request.getParameter("DOCUMENTROOT");

            _schoolKind = getSchoolKind(db2);

            String selectedInStateWk = "";
            String sep = "";
            for (int ii = 0; ii < _categorySelected.length;ii++) {
            	if (_categorySelected[ii] != null && !"".equals(_categorySelected[ii])) {
            		selectedInStateWk += sep + "'" + _categorySelected[ii] + "' ";
            	    sep = ", ";
            	}
            }
            _selectedInState = "(" + selectedInStateWk + ")";

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _imagePath = null == returnval ? null : returnval.val4;
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _schoolstampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");

            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark8 = getCertifSchoolDat(db2, "REMARK8");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");

            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_P = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_P");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_H = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _isOutputDebug = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD185L' AND NAME = 'outputDebug' ")));
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '01' ")));
            _semesternameMap = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' "), "SEMESTER", "SEMESTERNAME");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
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

        private String getFirstDate(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '1' ");
            String retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
            if (!"".equals(retStr)) {
            	retStr = retStr.replace('/', '-');
            }

            return retStr;
        }
        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }


        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
        	final String kCd = "J".equals(_schoolKind) ? "103" : "104";
            final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + kCd + "' ";
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
        }
        private String getGradeName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT GRADE_NAME2 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getImageFilePath(final String filename) {
            if (null == _documentRoot || null == _imagePath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentRoot).append("/").append(_imagePath).append("/").append(filename);
            final File file = new File(path.toString());
            log.warn("画像ファイル:" + path + " exists? " + file.exists());
            if (!file.exists()) {
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }
}

// eof

