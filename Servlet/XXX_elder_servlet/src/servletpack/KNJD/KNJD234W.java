/*
 * $Id: 1d51f3d585f393cbecc427d082db6862c693f0c3 $
 *
 * 作成日: 2013/07/25
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績会議資料
 */

public class KNJD234W {

    private static final Log log = LogFactory.getLog(KNJD234W.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String HYOTEI_TESTCD = "9900";
    private static final String ATTRIBUTE_CENTERING = "Hensyu=3";

//    private static final String OUTPUTRANK_HR = "1";
    private static final String OUTPUTRANK_GRADE = "2";
//    private static final String OUTPUTRANK_COURSE = "3";
//    private static final String OUTPUTRANK_MAJOR = "4";

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _hasData = false;

            _param = createParam(db2, request);
            _param._subclassReplaceCombinedDatListMap = getSubclassReplaceCombinedDatListMap(db2);

            final List studentList = getStudentList(db2, _param);
            final List hrClassList = HrClass.getHrClassList(studentList);
            setData(db2, _param, studentList, hrClassList);

            svf = new Vrw32alp();
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            printMain(db2, svf, studentList, hrClassList);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final List studentList, final List hrClassList) {

        final String form = "KNJD234W.frm";

        svf.VrSetForm(form, 4);

        svf.VrsOut("TITLE", getTitle(db2)); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); //

        //(1)在籍数
    	svf.VrsOut("TEXT1", "(1)在籍数");
        svf.VrEndRecord();

        final List totalZaisekiList = new ArrayList();
        for (int i = 0; i < hrClassList.size(); i++) {
        	final HrClass hr = (HrClass) hrClassList.get(i);
        	final List zaisekiList = HrClass.getZaisekiList(hr._studentList, null, _param, _param._date);
        	svf.VrsOutn("COUNT_HR", i + 1, hr._hrNameabbv);
        	svf.VrsOutn("COUNT_COUNT", i + 1, String.valueOf(zaisekiList.size()));
        	totalZaisekiList.addAll(zaisekiList);
        }
    	svf.VrsOutn("COUNT_HR", 8, "合計");
    	svf.VrsOutn("COUNT_COUNT", 8, String.valueOf(totalZaisekiList.size()));
        svf.VrEndRecord();
        svf.VrsOut("SPACE_DUMMY", "1");
        svf.VrEndRecord();

        //(2)成績優秀者
    	svf.VrsOut("TEXT1", "(2)成績優秀者");
        svf.VrEndRecord();
		final List scoreStudentList = new ArrayList(studentList);
        Collections.sort(scoreStudentList, new StudentRankComparator(OUTPUTRANK_GRADE, _param));
        printSeiseki(svf, new ArrayList(scoreStudentList.size() <= _param._yuryo ? scoreStudentList : scoreStudentList.subList(0, _param._yuryo)));

        final List tokutaiStudentList = new ArrayList();
        final List restStudentList = scoreStudentList.size() <= _param._yuryo ? new ArrayList() : scoreStudentList.subList(_param._yuryo + 1, studentList.size());
        for (int i = 0; i < restStudentList.size(); i++) {
        	final Student student = (Student) restStudentList.get(i);
        	if (null != student._scholarship) {
        		tokutaiStudentList.add(student);
        	}
        }
        // 特待生
        if (tokutaiStudentList.size() > 0) {
        	svf.VrsOut("TEXT1", "   特待生");
        	svf.VrEndRecord();
        	printSeiseki(svf, tokutaiStudentList);
        }
        svf.VrsOut("SPACE_DUMMY", "1");
        svf.VrEndRecord();

        // (3)成績不良者(評価に2または1がある者、評価の平均が" + _param._hyokaHeikin + "未満の者)
    	svf.VrsOut("TEXT1", "(3)成績不良者(評価に2または1がある者、評価の平均が" + _param._hyokaHeikin + "未満の者)");
        svf.VrEndRecord();
        final List furyoStudentList = new ArrayList();
        for (int i = 0; i < studentList.size(); i++) {
        	final Student student = (Student) studentList.get(i);
        	if (null != student._subclassScore999999 && null != student._subclassScore999999._avg && student._subclassScore999999._avg.compareTo(_param._hyokaHeikin) < 0) {
    			if (_param._isOutputDebug) {
    				log.info(" 平均 " + student._subclassScore999999._avg + " : " + student);
    			}
        		furyoStudentList.add(student);
        	} else {
        		final List target = new ArrayList();
        		for (final Iterator it = student._subclassScore.values().iterator(); it.hasNext();) {
        			final SubclassScore subScore = (SubclassScore) it.next();
        			if (NumberUtils.isNumber(subScore._score)) {
        				int score = Integer.parseInt(subScore._score);
        				if (score == 2 || score == 1) {
        					target.add(subScore);
        				}
        			} else {
    					target.add(subScore);
        			}
        		}
        		if (target.size() > 0) {
        			if (_param._isOutputDebug) {
        				log.info(" " + target + " : " + student);
        			}
        			furyoStudentList.add(student);
        		}
        	}
        }
        printSeiseki(svf, furyoStudentList);
        svf.VrsOut("SPACE_DUMMY", "1");
        svf.VrEndRecord();

        // (4)出席不良者(欠席日数が" + String.valueOf(_param._kesseki) + "日を超える者)
    	svf.VrsOut("TEXT1", "(4)出席不良者(欠席日数が" + String.valueOf(_param._kesseki) + "日を超える者)");
        svf.VrEndRecord();
        final List shussekiFuryoStudentList = new ArrayList();
        for (int i = 0; i < studentList.size(); i++) {
        	final Student student = (Student) studentList.get(i);
        	if (null != student._attendance && student._attendance._absence > _param._kesseki) {
        		shussekiFuryoStudentList.add(student);
        	}
        }
        printShusseki(svf, shussekiFuryoStudentList);
        svf.VrsOut("SPACE_DUMMY", "1");
        svf.VrEndRecord();

        _hasData = true;
    }

    private List getStudentSubclasscdList(final List studentList) {
		final Set subclasscdSet = new TreeSet();
		for (final Iterator it = studentList.iterator(); it.hasNext();) {
			final Student student = (Student) it.next();
			subclasscdSet.addAll(student._subclassScore.keySet());
		}
		return new ArrayList(subclasscdSet);
	}

	private void printSeiseki(final Vrw32alp svf, final List studentList) {
        final int maxSubclass = 21;
		final List subclasscdList = getStudentSubclasscdList(studentList);

		final List subclassnameArrayList = new ArrayList();
		for (int i = 0; i < subclasscdList.size(); i++) {
			final String subclasscd = (String) subclasscdList.get(i);
			final Subclass subclass = (Subclass) _param._subclassMap.get(subclasscd);
			if (null == subclass) {
				continue;
			}
			if (subclassnameArrayList.size() < i) {
				for (int j = 0; j < i - subclassnameArrayList.size(); i++) {
					subclassnameArrayList.add(new char[] {});
				}
			}
			final char[] arr = StringUtils.defaultString(StringUtils.defaultString(subclass._subclassabbv, subclass._subclassname)).toCharArray();
			subclassnameArrayList.add(arr);
		}
		int maxLength = 0;
		for (int i = 0; i < subclassnameArrayList.size(); i++) {
			final char[] arr = (char[]) subclassnameArrayList.get(i);
			maxLength = Math.max(maxLength, arr.length);
		}
		for (int gi = 0; gi < Math.max(1, maxLength); gi++) {
			final String noPrintLine = gi == 0 ? "" : "_NL";
			if (gi == 0) {
		        svf.VrsOut("HANTEI_1" + noPrintLine, "順位"); // 判定
		        svf.VrsOut("HR_CLASS_NAME1_1" + noPrintLine, "組"); // 組
		        svf.VrsOut("ATTENDNO1" + noPrintLine, "番"); // 番
		        svf.VrsOut("NAME_1" + noPrintLine, "氏名"); // 生徒氏名
		        svf.VrsOut("SUISEN_1" + noPrintLine, "特待"); // 特待

		        svf.VrsOut("AVG_1" + noPrintLine, "平均"); // 科目
		    	svf.VrsOut("FINSCHOOL1" + noPrintLine, "出身校名"); // 出身学校名
		        svf.VrsOut("COMMIITTEE1" + noPrintLine, "委員会"); // 委員会
		        svf.VrsOut("CLUB1" + noPrintLine, "部活動"); // 部活動
		        svf.VrAttribute("FINSCHOOL1" + noPrintLine, ATTRIBUTE_CENTERING);
		        svf.VrAttribute("COMMIITTEE1" + noPrintLine, ATTRIBUTE_CENTERING);
		    	svf.VrAttribute("CLUB1" + noPrintLine, "Hensyu=3");
			}

            for (int j = 0; j < maxSubclass; j++) {
                final int column = j + 1;
                if (j < subclassnameArrayList.size()) {
                	final char[] arr = (char[]) subclassnameArrayList.get(j);
                	if (gi < arr.length) {
                		svf.VrsOutn("SUBCLASS_1" + noPrintLine, column, String.valueOf(arr[gi])); // 科目
                		svf.VrAttributen("SUBCLASS_1" + noPrintLine, column, ATTRIBUTE_CENTERING); // 科目
                	}
                }
            }
			svf.VrEndRecord();
		}

        for (int i = 0; i < studentList.size(); i++) {
        	final Student student = (Student) studentList.get(i);
            svf.VrsOut("LINE1", String.valueOf(i + 1)); // 判定
            final SubclassScore subScore999999 = student._subclassScore999999;
            if (null != subScore999999) {
                svf.VrsOut("HANTEI_1", subScore999999.getRank(_param._outputKijun, _param)); // 判定
            }
            svf.VrsOut("HR_CLASS_NAME1_1", StringUtils.defaultString(student._hrNameabbv, student._hrName)); // 組
            svf.VrsOut("ATTENDNO1", NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番
            svf.VrsOut("NAME_1", student._name); // 生徒氏名
            svf.VrsOut("SUISEN_1", student._scholarshipAbbv); // 特待

            for (int j = 0; j < Math.min(maxSubclass,  subclasscdList.size()); j++) {
                final int line = j + 1;
                final String subclasscd = (String) subclasscdList.get(j);
                final SubclassScore subScore = (SubclassScore) student._subclassScore.get(subclasscd);
                if (null != subScore) {
                	svf.VrsOutn("SUBCLASS_1", line, subScore._score); // 科目
                }
            }
            if (null != subScore999999) {
                svf.VrsOut("AVG_1", subScore999999.getAvg()); // 科目
            }
        	svf.VrsOut("FINSCHOOL1" + (KNJ_EditEdit.getMS932ByteLength(student._finschoolName) <= 12 ? "" : KNJ_EditEdit.getMS932ByteLength(student._finschoolName) <= 20 ? "_2" : "_3"), student._finschoolName); // 出身学校名
            svf.VrsOut("COMMIITTEE1" + (KNJ_EditEdit.getMS932ByteLength(student._committeename) <= 12 ? "" : KNJ_EditEdit.getMS932ByteLength(student._committeename) <= 20 ? "_2" : "_3"), student._committeename); // 委員会
            svf.VrsOut("CLUB1" + (KNJ_EditEdit.getMS932ByteLength(student._clubname) <= 12 ? "" : KNJ_EditEdit.getMS932ByteLength(student._clubname) <= 20 ? "_2" : "_3"), student._clubname); // 部活動
            svf.VrEndRecord();
        }

        svf.VrsOut("TAB_LINE_DUMMY1", "1"); // テーブル下線ダミー
        svf.VrEndRecord();
	}

	private void printShusseki(final Vrw32alp svf, final List studentList) {

        svf.VrsOut("HANTEI_2", "順位"); // 順位
        svf.VrsOut("HR_CLASS_NAME2_1", "組"); // 組
        svf.VrsOut("ATTENDNO2", "番"); // 番
        svf.VrsOut("NAME_2", "氏名"); // 生徒氏名
        svf.VrsOut("SUISEN_2", "特待"); // 特待

        svf.VrsOutn("ATTEND_1", 1, "欠席"); // 欠席
        svf.VrsOutn("ATTEND_1", 2, "遅刻"); // 遅刻
        svf.VrsOutn("ATTEND_1", 3, "早退"); // 早退
        svf.VrsOut("AVG_2", "平均"); // 科目
    	svf.VrsOut("FINSCHOOL2", "出身校名"); // 出身学校名
        svf.VrsOut("COMMIITTEE2", "委員会"); // 委員会
        svf.VrsOut("CLUB2", "部活動"); // 部活動
        svf.VrAttribute("FINSCHOOL2", "Hensyu=3");
        svf.VrAttribute("COMMIITTEE2", "Hensyu=3");
    	svf.VrAttribute("CLUB2", "Hensyu=3");
        svf.VrEndRecord();

        // record RECORD1_TAB2
        for (int i = 0; i < studentList.size(); i++) {
        	final Student student = (Student) studentList.get(i);
            final SubclassScore subScore999999 = student._subclassScore999999;

            svf.VrsOut("LINE2", String.valueOf(i + 1)); // 判定
            if (null != subScore999999) {
            	svf.VrsOut("HANTEI_2", subScore999999.getRank(OUTPUTRANK_GRADE, _param)); // 順位
            }
            svf.VrsOut("HR_CLASS_NAME2_1", StringUtils.defaultString(student._hrNameabbv, student._hrName)); // 組
            svf.VrsOut("ATTENDNO2", NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番
            svf.VrsOut("NAME_2", student._name); // 生徒氏名
            svf.VrsOut("SUISEN_2", student._scholarshipAbbv); // 特待

            if (null != student._attendance) {
            	svf.VrsOutn("ATTEND_1", 1, String.valueOf(student._attendance._absence)); // 欠席
            	svf.VrsOutn("ATTEND_1", 2, String.valueOf(student._attendance._late)); // 遅刻
            	svf.VrsOutn("ATTEND_1", 3, String.valueOf(student._attendance._early)); // 早退
            }
            if (null != subScore999999) {
                svf.VrsOut("AVG_2", subScore999999.getAvg()); // 科目
            }
        	svf.VrsOut("FINSCHOOL2" + (KNJ_EditEdit.getMS932ByteLength(student._finschoolName) <= 12 ? "" : KNJ_EditEdit.getMS932ByteLength(student._finschoolName) <= 20 ? "_2" : "_3"), student._finschoolName); // 出身学校名
            svf.VrsOut("COMMIITTEE2" + (KNJ_EditEdit.getMS932ByteLength(student._committeename) <= 12 ? "" : KNJ_EditEdit.getMS932ByteLength(student._committeename) <= 20 ? "_2" : "_3"), student._committeename); // 委員会
            svf.VrsOut("CLUB2" + (KNJ_EditEdit.getMS932ByteLength(student._clubname) <= 12 ? "" : KNJ_EditEdit.getMS932ByteLength(student._clubname) <= 20 ? "_2" : "_3"), student._clubname); // 部活動
            svf.VrEndRecord();
        }
        svf.VrsOut("TAB_LINE_DUMMY2", "1"); // テーブル下線ダミー
        svf.VrEndRecord();
    }

    private List notTargetSubclasscdList(final String gradeCourse) {
        final List notTargetSubclassCdList;
        if (_param._testcd.startsWith("99")) {
            notTargetSubclassCdList = Collections.EMPTY_LIST; // getMappedList(getMappedMap(_param._courseWeightingSubclassCdListMap, gradeCourse), "ATTEND_SUBCLASS");
        } else {
            // [学期末、学年末]以外は先を表示しない
            notTargetSubclassCdList = getMappedList(getMappedMap(_param._subclassReplaceCombinedDatListMap, gradeCourse), "COMBINED_SUBCLASS");
        }
        return notTargetSubclassCdList;
    }

    private Map getSubclassReplaceCombinedDatListMap(final DB2UDB db2) {
        final Map subclassCdListMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            } else {
                stb.append("       T1.ATTEND_SUBCLASSCD ");
            }
            stb.append("       , ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
            } else {
                stb.append("       T1.COMBINED_SUBCLASSCD ");
            }
            stb.append("   FROM ");
            stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                getMappedList(subclassCdListMap, "ATTEND_SUBCLASS").add(rs.getString("ATTEND_SUBCLASSCD"));
                getMappedList(subclassCdListMap, "COMBINED_SUBCLASS").add(rs.getString("COMBINED_SUBCLASSCD"));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return subclassCdListMap;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private String getTitle(final DB2UDB db2) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        final String gradename = StringUtils.defaultString(_param._gradename1);
        final String title = nendo + " " + StringUtils.defaultString(_param._schoolname) + StringUtils.defaultString(gradename) + "　成績会議資料（" + StringUtils.defaultString(_param._testItem._testitemname) + "）";
        return title;
    }

//    private void printGaihyou12(final DB2UDB db2, final Vrw32alp svf, final List hrClassList, int page) {
//        final boolean hyotei5Dankai = "H".equals(_param._schoolKind);
//        final String form1 = hyotei5Dankai ? "KNJD234G_3.frm" : "KNJD234G_1.frm";
//        svf.VrSetForm(form1, 4);
//
//        final BigDecimal[][] table;
//        table = new BigDecimal[10][];
//        for (int i = 0; i < table.length; i++) {
//        	final int upper = i == 0 ? 100 : 100 - i * 10 - 1;
//        	final int lower = 100 - (i + 1) * 10;
//        	table[i] = new BigDecimal[] {new BigDecimal(lower), new BigDecimal(upper)};
//        }
//
//        svf.VrsOut("TITLE", getTitle(db2)); // タイトル
//        svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); // 印刷日付
//        svf.VrsOut("PAGE", String.valueOf(page)); // ページ
//        svf.VrsOut("SUB_TITLE2", ". クラス別成績上位者"); // サブタイトル2
//
//
//        for (int k = 0, min = Math.min(hrClassList.size(), 9); k < min; k++) {
//            final HrClass hrClass = (HrClass) hrClassList.get(k);
//            final String hrn = String.valueOf(k + 1);
//            svf.VrsOut("HR_NAME2_" + hrn, hrClass._hrNameabbv); // クラス名
//            final int maxLine = 10;
//            final List scoreStudentList = new ArrayList(hrClass._studentList);
//            Collections.sort(scoreStudentList, new StudentRankComparator(OUTPUTRANK_HR, _param)); // HRランクでソート
//            for (int j = 0, max = Math.min(maxLine, scoreStudentList.size()); j < max; j++) {
//                final Student student = (Student) scoreStudentList.get(j);
//                final SubclassScore subScore = student._subclassScore999999;
//                if (null == subScore) {
//                    break;
//                }
//                final int line = j + 1;
//                svf.VrsOutn("RANK" + hrn, line, subScore.getRank(OUTPUTRANK_HR, _param)); // 順位
//                svf.VrsOutn("SCORE" + hrn, line, subScore._score); // 得点
//                svf.VrsOutn("AVERAGE" + hrn, line, subScore.getAvg()); // 平均
//                final int keta = getMS932ByteLength(student._name);
//                svf.VrsOutn("NAME" + hrn + (keta <= 14 ? "_1" : keta <= 20 ? "_2" : "_3"), line, student._name); // 氏名
//            }
//        }
//    }

//    private List getGaihyou345RecordList(final List studentList) {
//        final List recordList = new ArrayList();
//
//        final Map blankRecord = new HashMap();
//        blankRecord.put("BRANK", "-"); // 空白行用
//
//        final List studentListZaiseki = HrClass.getZaisekiList(studentList, null, _param, _param._edate);
//
//        newRecord(recordList).put("SUB_TITLE1", ". 成績不振者"); // サブタイトル1
//        addGaihyou3Record(studentListZaiseki, recordList, blankRecord);
//        recordList.add(blankRecord);
//
//
//        newRecord(recordList).put("SUB_TITLE1", ". 欠席・遅刻・早退"); // サブタイトル1
//        addGaihyou4Record(studentListZaiseki, recordList, blankRecord);
//        recordList.add(blankRecord);
//
//        return recordList;
//    }

    private void addGaihyou3Record(final List studentList, final List recordList, final Map blankRecord) {

//        final Map rec3 = newRecord(recordList);
//        rec3.put("CONDITION", String.valueOf(_param._fushinScore) + "点未満(" + String.valueOf(_param._fushinSu) + "科目以上)"); // 条件
//
//        final Map rec4 = newRecord(recordList);
//        rec4.put("HEADER1_1", "組"); // ヘッダ1
//        rec4.put("HEADER1_2", "氏名"); // ヘッダ2
//        rec4.put("HEADER1_3", "欠点科目・得点"); // ヘッダ3
//        rec4.put("HEADER1_4", "科目数"); // ヘッダ4
//
//        final List fushinStudentList = getFushinKamokuStudentList(studentList, _param._fushinScore, _param._fushinSu);
//        if (fushinStudentList.size() == 0) {
//            putName(newRecord(recordList), "NAME6", "該当なし");
//
//        } else {
//            for (int i = 0; i < fushinStudentList.size(); i++) {
//                final Student student = (Student) fushinStudentList.get(i);
//
//                final List fushinAllSubclassList = getFushinKamokuList(Collections.singletonList(student), _param._fushinScore);
//                final List fushinSubclassGroup = getGroupListByCount(fushinAllSubclassList, 10);
//                for (int li = 0, max = fushinSubclassGroup.size(); li < max; li++) {
//                    final Map rec11 = newRecord(recordList);
//                    if (li == 0) {
//                        rec11.put("HR_NAME4", student._hrClassInt); // クラス名
//                        putName(rec11, "NAME6", student._name);
//                        rec11.put("SUBJECT_NUM2", String.valueOf(fushinAllSubclassList.size())); // 科目数
//
//                    }
//                    final List kessiSubclassList = (List) fushinSubclassGroup.get(li);
//                    for (int si = 0; si < kessiSubclassList.size(); si++) {
//                        final SubclassScore subScore = (SubclassScore) kessiSubclassList.get(si);
//                        final String abbv = subScore._subclass._subclassabbv;
//                        rec11.put("SHORT_SUBJECT" + String.valueOf(si + 1), abbv + StringUtils.repeat(" ", 6 - getMS932ByteLength(abbv)) + subScore._score); // 不足科目
//                    }
//                }
//            }
//        }
//
//        final Map rec3_2 = newRecord(recordList);
//        rec3_2.put("CONDITION", " 平均 " + String.valueOf(_param._fushinHeikin) + "点未満"); // 条件
//
//        final Map rec6 = newRecord(recordList);
//        rec6.put("HEADER2_1", "組"); // ヘッダ1
//        rec6.put("HEADER2_2", "氏名"); // ヘッダ2
//        rec6.put("HEADER2_3", "平均点"); // ヘッダ3
//
//        final List fushinHeikinStudentList = getFushinHeikinStudentList(studentList, _param._fushinHeikin);
//
//        if (fushinHeikinStudentList.size() == 0) {
//            putName(newRecord(recordList), "NAME2", "該当なし");
//
//        } else {
//            for (int i = 0, max = Math.max(fushinHeikinStudentList.size(), 1); i < max; i++) {
//                final Student student = (Student) fushinHeikinStudentList.get(i);
//
//                final Map rec7 = newRecord(recordList);
//                rec7.put("HR_NAME2", student._hrClassInt); // クラス名
//                rec7.put("AVERAGE2", student._subclassScore999999.getAvg()); // 平均
//                putName(rec7, "NAME2", student._name);
//            }
//        }
    }

    private void addGaihyou4Record(final List studentList, final List recordList, final Map blankRecord) {

        final Map rec8 = newRecord(recordList);
        rec8.put("CONDITION2_1", "欠席　" + String.valueOf(_param._kesseki) + "日以上"); // 条件

        final Map rec9 = newRecord(recordList);
        rec9.put("HEADER3_1", "組"); // ヘッダ1
        rec9.put("HEADER3_2", "氏名"); // ヘッダ2
        rec9.put("HEADER3_3", "日数"); // ヘッダ3
        rec9.put("HEADER3_4", "組"); // ヘッダ4
        rec9.put("HEADER3_5", "氏名"); // ヘッダ5
        rec9.put("HEADER3_6", "日数"); // ヘッダ6
        rec9.put("HEADER3_7", "組"); // ヘッダ7
        rec9.put("HEADER3_8", "氏名"); // ヘッダ8
        rec9.put("HEADER3_9", "日数"); // ヘッダ9

        final List studentListKesseki = getAttendOverStudentList(_param, studentList);

        for (int i = 0, max = Math.max(1, studentListKesseki.size()); i < max; i++) {
            final Map rec10 = newRecord(recordList);
            if (i < studentListKesseki.size()) {
                final Student student = (Student) studentListKesseki.get(i);
                rec10.put("HR_NAME3_1", student._hrClassInt); // クラス名
                putName(rec10, "NAME3", student._name);
                rec10.put("DAY1", String.valueOf(student._attendance._absence)); // 日数
            } else if (i == 0) {
                putName(rec10, "NAME3", "該当なし"); // 氏名
            }
        }
    }

    private static boolean isSubclass999999(final String subclasscd, final Param param) {
        if ("1".equals(param._useCurriculumcd)) {
            final String split = StringUtils.split(subclasscd, "-")[3];
            if (SUBCLASSCD999999.equals(split)) {
                return true;
            }
        }
        if (SUBCLASSCD999999.equals(subclasscd)) {
            return true;
        }
        return false;
    }

    private void setData(final DB2UDB db2, final Param param, final List studentList, final List hrClassList) {
        log.debug(" setData ");
        PreparedStatement ps = null;

        // １日出欠
        try {
            param._attendParamMap.put("grade", "?");
            param._attendParamMap.put("hrClass", "?");
            String sql = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    param._semester,
                    param._sdate,
                    param._edate,
                    param._attendParamMap
                    );
            //log.debug(" attend semes sql = " + sql);
            ps = db2.prepareStatement(sql);
            final Integer zero = new Integer(0);
            for (final Iterator hIt = hrClassList.iterator(); hIt.hasNext();) {
                final HrClass hrClass = (HrClass) hIt.next();
                log.debug(" set Attendance " + hrClass);

                //log.debug(" attend semes sql = " + sql);
                for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {hrClass._grade, hrClass._hrClass}).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final Student student = Student.getStudent(KnjDbUtils.getString(row, "SCHREGNO"), hrClass._studentList);
                    if (student == null || !"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }
                    final int lesson = KnjDbUtils.getInt(row, "LESSON", zero).intValue();
                    final int mourning = KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
                    final int suspend = KnjDbUtils.getInt(row, "SUSPEND", zero).intValue() + KnjDbUtils.getInt(row, "VIRUS", zero).intValue() + KnjDbUtils.getInt(row, "KOUDOME", zero).intValue();
                    final int abroad = KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue();
                    final int mlesson = KnjDbUtils.getInt(row, "MLESSON", zero).intValue();
                    final int absence = KnjDbUtils.getInt(row, "SICK", zero).intValue();
                    final int attend = KnjDbUtils.getInt(row, "PRESENT", zero).intValue();
                    final int late = KnjDbUtils.getInt(row, "LATE", zero).intValue();
                    final int early = KnjDbUtils.getInt(row, "EARLY", zero).intValue();

                    final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early);
                    // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                    student._attendance = attendance;
                }
            }

        } catch (SQLException e) {
            log.error("sql exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }

        // 成績
        final String sql = SubclassScore.getSubclassScoreSql(param);
        if (_param._isOutputDebug) {
        	log.info(" setRecord  sql = " + sql);
        }
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            final Student student = Student.getStudent(schregno, studentList);
            if (null == student) {
                continue;
            }

            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            if (notTargetSubclasscdList(student.gradeCourse()).contains(subclasscd)) {
                continue;
            }
            final String classabbv = KnjDbUtils.getString(row, "CLASSABBV");
            final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
            final String subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
            final String score = KnjDbUtils.getString(row, "SCORE");
            final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
            final SubclassScore.DivRank gradeRank = new SubclassScore.DivRank(KnjDbUtils.getString(row, "GRADE_RANK"), KnjDbUtils.getString(row, "GRADE_AVG_RANK"));
            final SubclassScore.DivRank classRank = new SubclassScore.DivRank(KnjDbUtils.getString(row, "CLASS_RANK"), KnjDbUtils.getString(row, "CLASS_AVG_RANK"));
            final SubclassScore.DivRank courseRank = new SubclassScore.DivRank(KnjDbUtils.getString(row, "COURSE_RANK"), KnjDbUtils.getString(row, "COURSE_AVG_RANK"));
            final SubclassScore.DivRank majorRank = new SubclassScore.DivRank(KnjDbUtils.getString(row, "MAJOR_RANK"), KnjDbUtils.getString(row, "MAJOR_AVG_RANK"));

            if (!param._subclassMap.containsKey(subclasscd)) {
                param._subclassMap.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv));
            }
            final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
            subclass._subclassScoreAllNull = KnjDbUtils.getString(row, "SCORE_ALL_NULL");

            final SubclassScore subclassscore = new SubclassScore(student, subclass, score, avg, gradeRank,
                    classRank, courseRank, majorRank);

            if (isSubclass999999(subclasscd, param)) {
                student._subclassScore999999 = subclassscore;
            } else {
                student._subclassScore.put(subclasscd, subclassscore);
            }
        }

        final TreeSet entDateSet = new TreeSet();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._entdate) {
                entDateSet.add(student._entdate);
            }
        }
        if (!entDateSet.isEmpty()) {
            final String entDateMin = (String) entDateSet.first();
            if (param._yearSdate.compareTo(entDateMin) < 1) {
                param._yearSdate = entDateMin;
            }
        }
    }

    private static List getStudentList(final DB2UDB db2, final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_TRANSFER1 AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
        stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
        stb.append("   LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = '" + param._year + "' AND T2.SEMESTER = '9' ");
        stb.append("   WHERE ");
        stb.append("     T2.SDATE BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
        stb.append("   GROUP BY T1.SCHREGNO ");
        stb.append(" ), SCHREG_TRANSFER2 AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
        stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
        stb.append("   WHERE ");
        stb.append("     '" + param._edate + "' BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
        stb.append("   GROUP BY T1.SCHREGNO ");
        stb.append(" ), T_SCHOLARSHIP AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHOOLCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.SCHOLARSHIP, ");
        stb.append("     T1.SCHREGNO ");
        stb.append("   FROM SCHREG_SCHOLARSHIP_HIST_DAT T1 ");
        stb.append("   INNER JOIN (SELECT SCHOOLCD, SCHOOL_KIND, SCHREGNO, MAX(SCHOLARSHIP) AS SCHOLARSHIP ");
        stb.append("               FROM SCHREG_SCHOLARSHIP_HIST_DAT T1 ");
        stb.append("               INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = '" + param._year + "' AND SEME.SEMESTER = '" + param._semester + "' ");
        stb.append("               WHERE ");
        stb.append("                ( ");
        stb.append("                DATE(CAST(LEFT(RTRIM(LTRIM(CHAR(DATE(T1.FROM_DATE)))), 7) || '-01' AS CHAR(10))) BETWEEN SEME.SDATE AND SEME.EDATE ");
        stb.append("                OR DATE(CAST(LEFT(RTRIM(LTRIM(CHAR(DATE(T1.TO_DATE)))), 7) || '-01' AS CHAR(10))) BETWEEN SEME.SDATE AND SEME.EDATE ");
        stb.append("                OR DATE(CAST(LEFT(RTRIM(LTRIM(CHAR(DATE(T1.FROM_DATE)))), 7) || '-01' AS CHAR(10))) <= SEME.SDATE AND SEME.EDATE <= DATE(CAST(LEFT(RTRIM(LTRIM(CHAR(DATE(T1.TO_DATE)))), 7) || '-01' AS CHAR(10))) ");
        stb.append("                OR SEME.SDATE <= DATE(CAST(LEFT(RTRIM(LTRIM(CHAR(DATE(FROM_DATE)))), 7) || '-01' AS CHAR(10))) AND DATE(CAST(LEFT(RTRIM(LTRIM(CHAR(DATE(TO_DATE)))), 7) || '-01' AS CHAR(10))) <= SEME.EDATE ");
        stb.append("                ) ");
        stb.append("               GROUP BY SCHOOLCD, SCHOOL_KIND, SCHREGNO ");
        stb.append("              ) T2 ON T2.SCHOOLCD = T1.SCHOOLCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SCHREGNO = T1.SCHREGNO AND T2.SCHOLARSHIP = T1.SCHOLARSHIP ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SCHOOLCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.SCHOLARSHIP, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     T2.HR_NAMEABBV, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.SEX, ");
        stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
        stb.append("     ENTGRD.ENT_DATE, ");
        stb.append("     ENTGRD.ENT_DIV, ");
        stb.append("     NMA002.NAME1 AS ENT_DIV_NAME, ");
        stb.append("     ENTGRD.GRD_DATE, ");
        stb.append("     ENTGRD.GRD_DIV, ");
        stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
        stb.append("     T5.TRANSFERCD AS TRANSFERCD1, ");
        stb.append("     NMA004_1.NAME1 AS TRANSFER_NAME1, ");
        stb.append("     T5.TRANSFERREASON AS TRANSFERREASON1, ");
        stb.append("     T5.TRANSFER_SDATE AS TRANSFER_SDATE1, ");
        stb.append("     T5.TRANSFER_EDATE AS TRANSFER_EDATE1, ");
        stb.append("     T7.TRANSFERCD AS TRANSFERCD2, ");
        stb.append("     NMA004_2.NAME1 AS TRANSFER_NAME2, ");
        stb.append("     T7.TRANSFERREASON AS TRANSFERREASON2, ");
        stb.append("     T7.TRANSFER_SDATE AS TRANSFER_SDATE2, ");
        stb.append("     T7.TRANSFER_EDATE AS TRANSFER_EDATE2, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T9.MAJORNAME, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T8.COURSECODENAME, ");
        stb.append("     T10.SCHOLARSHIP, ");
        stb.append("     FIN.FINSCHOOL_NAME, ");
        stb.append("     SCHSM.SCHOLARSHIP_ABBV ");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.GRADE = T1.GRADE ");
        stb.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append(" LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_TRANSFER1 T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T5.TRANSFER_SDATE = T4.TRANSFER_SDATE ");
        stb.append(" LEFT JOIN SCHREG_TRANSFER2 T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T7.TRANSFER_SDATE = T6.TRANSFER_SDATE ");
        stb.append(" LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = T1.COURSECODE ");
        stb.append(" LEFT JOIN MAJOR_MST T9 ON T9.COURSECD = T1.COURSECD AND T9.MAJORCD = T1.MAJORCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
        stb.append(" LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append(" LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = ENTGRD.FINSCHOOLCD ");
        stb.append(" LEFT JOIN T_SCHOLARSHIP T10 ON T10.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHOLARSHIP_MST SCHSM ON SCHSM.SCHOOLCD = T10.SCHOOLCD AND SCHSM.SCHOOL_KIND = T10.SCHOOL_KIND AND SCHSM.SCHOLARSHIP = T10.SCHOLARSHIP ");
        stb.append(" LEFT JOIN NAME_MST NMA004_1 ON NMA004_1.NAMECD1 = 'A004' AND NMA004_1.NAMECD2 = T5.TRANSFERCD ");
        stb.append(" LEFT JOIN NAME_MST NMA004_2 ON NMA004_2.NAMECD1 = 'A004' AND NMA004_2.NAMECD2 = T7.TRANSFERCD ");
        stb.append(" LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T3.SEX ");
        stb.append(" LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = ENTGRD.ENT_DIV ");
        stb.append(" LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = ENTGRD.GRD_DIV ");
        stb.append(" WHERE ");
        stb.append(" T1.YEAR = '" + param._year + "' ");
        stb.append(" AND T1.SEMESTER = '" + param.regdSemester() + "' ");
        stb.append(" AND T1.GRADE = '" + param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");

        if (param._isOutputDebug) {
        	log.info(" regd sql = " + stb.toString());
        }
        final List studentList = new ArrayList();
        final Map schregMap = new HashMap();
        for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
        	final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
        	if (null != schregMap.get(schregno)) {
        		continue;
        	}
            final String grade = KnjDbUtils.getString(row, "GRADE");
            final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
            final String hrClassInt = NumberUtils.isDigits(KnjDbUtils.getString(row, "HR_CLASS")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "HR_CLASS"))) : KnjDbUtils.getString(row, "HR_CLASS");
            final String hrNameabbv = KnjDbUtils.getString(row, "HR_NAMEABBV");
            final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
            final String hrName = KnjDbUtils.getString(row, "HR_NAME");
            final String name = KnjDbUtils.getString(row, "NAME");
            final String sex = KnjDbUtils.getString(row, "SEX");
            final String sexName = KnjDbUtils.getString(row, "SEX_NAME");
            final String entdiv = KnjDbUtils.getString(row, "ENT_DIV");
            final String entdivName = KnjDbUtils.getString(row, "ENT_DIV_NAME");
            final String entdate = KnjDbUtils.getString(row, "ENT_DATE");
            final String grddiv = KnjDbUtils.getString(row, "GRD_DIV");
            final String grddivName = KnjDbUtils.getString(row, "GRD_DIV_NAME");
            final String grddate = KnjDbUtils.getString(row, "GRD_DATE");
            final String transfercd1 = KnjDbUtils.getString(row, "TRANSFERCD1");
            final String transferName1 = KnjDbUtils.getString(row, "TRANSFER_NAME1");
            final String transferreason1 = KnjDbUtils.getString(row, "TRANSFERREASON1");
            final String transferSdate1 = KnjDbUtils.getString(row, "TRANSFER_SDATE1");
            final String transferEdate1 = KnjDbUtils.getString(row, "TRANSFER_EDATE1");
            final String transfercd2 = KnjDbUtils.getString(row, "TRANSFERCD2");
            final String transferName2 = KnjDbUtils.getString(row, "TRANSFER_NAME2");
            final String transferreason2 = KnjDbUtils.getString(row, "TRANSFERREASON2");
            final String transferSdate2 = KnjDbUtils.getString(row, "TRANSFER_SDATE2");
            final String transferEdate2 = KnjDbUtils.getString(row, "TRANSFER_EDATE2");
            final String coursecd = KnjDbUtils.getString(row, "COURSECD");
            final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
            final String majorname = KnjDbUtils.getString(row, "MAJORNAME");
            final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
            final String coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
            final String scholarship = KnjDbUtils.getString(row, "SCHOLARSHIP");
            final String scholarshipAbbv = KnjDbUtils.getString(row, "SCHOLARSHIP_ABBV");
            final String finschoolName = KnjDbUtils.getString(row, "FINSCHOOL_NAME");
            final Student student = new Student(grade, hrClass, hrClassInt, hrNameabbv, attendno, schregno, hrName, name, sex, sexName, entdiv, entdivName, entdate, grddiv, grddivName, grddate,
                    transfercd1, transferName1, transferreason1 ,transferSdate1, transferEdate1,
                    transfercd2, transferName2, transferreason2, transferSdate2, transferEdate2,
                    coursecd, majorcd, majorname, coursecode, coursecodename, scholarship, scholarshipAbbv, finschoolName);
            studentList.add(student);
            schregMap.put(schregno, student);
        }
        Student.setClub(db2,  param,  schregMap);
        Student.setCommittee(db2, param, schregMap);
        return studentList;
    }

    /**
     * 生徒のリストから生徒の科目の得点のリストを得る
     * @param studentList 生徒のリスト
     * @param subclasscd 科目
     * @return 生徒のリストから生徒の科目の得点のリストを得る
     */
    private static List getScoreList(final List studentList, final String subclasscd) {
        final List scoreList = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String score = student.getScore(subclasscd);
            if (null != score) {
                scoreList.add(score);
            }
        }
        return scoreList;
    }

    /**
     * 生徒のリストから生徒の科目の平均のリストを得る
     * @param studentList 生徒のリスト
     * @param subclasscd 科目
     * @return 生徒のリストから生徒の科目の平均のリストを得る
     */
    private static List getAvgList(final List studentList, final String subclasscd) {
        final List avgList = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final String avg = student.getAvg(subclasscd);
            if (null != avg) {
                avgList.add(avg);
            }
        }
        return avgList;
    }

    private static List getGroupListByCount(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private static List getSubclassList(final Param param, final List studentList) {
        final Set set = new TreeSet();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            for (final Iterator sit = student._subclassScore.values().iterator(); sit.hasNext();) {
                final SubclassScore subScore = (SubclassScore) sit.next();
                if (null != subScore._subclass._subclasscd) {
                    set.add(subScore._subclass._subclasscd);
                }
            }
        }
        final List rtn = new ArrayList();
        for (final Iterator it = set.iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
            if (null != subclass) {
                rtn.add(subclass);
            }
        }
        return rtn;
    }

    private static class Student {

        private static DecimalFormat attendnodf = new DecimalFormat("00");

        final String _grade;
        final String _hrClass;
        final String _hrClassInt;
        final String _hrNameabbv;
        final String _attendno;
        final String _schregno;
        final String _hrName;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _entdiv;
        final String _entdivName;
        final String _entdate;
        final String _grddiv;
        final String _grddivName;
        final String _grddate;
        // 年度開始日時点の異動データ
        final String _transfercd1;
        final String _transfername1;
        final String _transferreason1;
        final String _transferSdate1;
        final String _transferEdate1;
        // パラメータ指定日付時点の異動データ
        final String _transfercd2;
        final String _transfername2;
        final String _transferreason2;
        final String _transferSdate2;
        final String _transferEdate2;
        final String _coursecd;
        final String _majorcd;
        final String _majorname;
        final String _coursecode;
        final String _coursecodename;
        final String _scholarship;
        final String _scholarshipAbbv;
        final String _finschoolName;
        final Map _subclassScore;

        private String _clubname;
        private String _committeename;
        private SubclassScore _subclassScore999999;
        private Attendance _attendance;

        Student(
            final String grade,
            final String hrClass,
            final String hrClassInt,
            final String hrNameabbv,
            final String attendno,
            final String schregno,
            final String hrName,
            final String name,
            final String sex,
            final String sexName,
            final String entdiv,
            final String entdivName,
            final String entdate,
            final String grddiv,
            final String grddivName,
            final String grddate,
            final String transfercd1,
            final String transfername1,
            final String transferreason1,
            final String transferSdate1,
            final String transferEdate1,
            final String transfercd2,
            final String transfername2,
            final String transferreason2,
            final String transferSdate2,
            final String transferEdate2,
            final String coursecd,
            final String majorcd,
            final String majorname,
            final String coursecode,
            final String coursecodename,
            final String scholarship,
            final String scholarshipAbbv,
            final String finschoolName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrClassInt = hrClassInt;
            _hrNameabbv = hrNameabbv;
            _attendno = attendno;
            _schregno = schregno;
            _hrName = hrName;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _entdiv = entdiv;
            _entdivName = entdivName;
            _entdate = entdate;
            _grddiv = grddiv;
            _grddivName = grddivName;
            _grddate = grddate;
            _transfercd1 = transfercd1;
            _transfername1 = transfername1;
            _transferreason1 = transferreason1;
            _transferSdate1 = transferSdate1;
            _transferEdate1 = transferEdate1;
            _transfercd2 = transfercd2;
            _transfername2 = transfername2;
            _transferreason2 = transferreason2;
            _transferSdate2 = transferSdate2;
            _transferEdate2 = transferEdate2;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _majorname = majorname;
            _coursecode = coursecode;
            _coursecodename = coursecodename;
            _scholarship = scholarship;
            _scholarshipAbbv = scholarshipAbbv;
            _finschoolName = finschoolName;
            _subclassScore = new TreeMap();
        }


        private static void setClub(final DB2UDB db2, final Param param, final Map studentMap) {
            PreparedStatement ps = null;
            final StringBuffer stb = new StringBuffer();
            stb.append("  ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME, ");
            stb.append("     CASE WHEN T1.SDATE BETWEEN TSEM.SDATE AND TSEM.EDATE OR ");
            stb.append("                       VALUE(T1.EDATE, '9999-12-31') BETWEEN TSEM.SDATE AND TSEM.EDATE OR ");
            stb.append("                       TSEM.SDATE <= T1.SDATE AND T1.EDATE <= TSEM.EDATE OR ");
            stb.append("                       T1.SDATE <= TSEM.SDATE AND TSEM.EDATE <=  VALUE(T1.EDATE, TSEM.EDATE) THEN 1 END AS FLG ");
            stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
            stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append(" INNER JOIN SEMESTER_MST TSEM ON TSEM.YEAR = '" + param._year + "' ");
            stb.append("     AND TSEM.SEMESTER <> '9' ");
            stb.append("     AND TSEM.SEMESTER <= '" + param._semester + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    final Set yet = new HashSet();
                    for (final Iterator rowIt = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rowIt.hasNext();) {
                    	final Map row = (Map) rowIt.next();

                        final String clubname = KnjDbUtils.getString(row, "CLUBNAME");
                        final String flg = KnjDbUtils.getString(row, "FLG");

                        if (!"1".equals(flg) || StringUtils.isBlank(clubname) || yet.contains(clubname)) {
                            continue;
                        }
                        if (StringUtils.isBlank(student._clubname)) {
                            student._clubname = clubname;
                        } else {
                            student._clubname += "、" + clubname;
                        }
                        yet.add(clubname);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        private static void setCommittee(final DB2UDB db2, final Param param, final Map studentMap) {
            PreparedStatement ps = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD, ");
            stb.append("     T1.CHARGENAME, ");
            stb.append("     T2.COMMITTEENAME ");
            stb.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
            stb.append(" LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
            stb.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.COMMITTEE_FLG IN ('1', '2') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD ");
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    final Set yet = new HashSet();
                    for (final Iterator rowIt = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rowIt.hasNext();) {
                    	final Map row = (Map) rowIt.next();

//                        final String semester = rs.getString("SEMESTER");
//                        final String committeeFlg = rs.getString("COMMITTEE_FLG");
                        String name = null;
//                        if ("2".equals(committeeFlg)) {
//                            name = rs.getString("CHARGENAME");
//                        } else if ("1".equals(committeeFlg)) {
                            name = KnjDbUtils.getString(row, "COMMITTEENAME");
//                        }
                        if (StringUtils.isBlank(name) || yet.contains(name)) {
                            continue;
                        }
                        if (StringUtils.isBlank(student._committeename)) {
                        	student._committeename = name;
                        } else {
                        	student._committeename += "、" + name;
                        }
                        yet.add(name);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        public String gradeCourse() {
            return _grade + _coursecd + _majorcd + _coursecode;
        }

        public String majorcd() {
            return _coursecd + _majorcd;
        }

        public String getHrclassAttendnoCd() {
            return
            StringUtils.defaultString((NumberUtils.isDigits(_hrClass)) ? String.valueOf(Integer.parseInt(_hrClass)) : _hrClass) +
            StringUtils.defaultString((NumberUtils.isDigits(_attendno)) ? attendnodf.format(Integer.parseInt(_attendno)) : _attendno);
        }

//        public List getKesshiSubclassList() {
//            final List list = new ArrayList();
//            for (final Iterator it = _subclassScore.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                final SubclassScore subScore = (SubclassScore) _subclassScore.get(subclasscd);
//                if (subScore._subclass._combinedSubclassCourse.contains(gradeCourse())) {
//                    continue;
//                }
//                if (null == subScore._score && !"1".equals(subScore._subclass._subclassScoreAllNull)) {
//                    list.add(subScore);
//                }
//            }
//            return list;
//        }

//        private List getKettenSubclassList(final Param param) {
//            final List list = new ArrayList();
//            for (final Iterator it = _subclassScore.keySet().iterator(); it.hasNext();) {
//                final String subclasscd = (String) it.next();
//                final SubclassScore subScore = (SubclassScore) _subclassScore.get(subclasscd);
//                if (subScore.isKetten(param)) {
//                    list.add(subScore);
//                }
//            }
//            return list;
//        }

        public String getScore(final String subclasscd) {
            final SubclassScore subScore;
            if (SUBCLASSCD999999.equals(subclasscd)) {
                subScore = _subclassScore999999;
            } else {
                subScore = (SubclassScore) _subclassScore.get(subclasscd);
            }
            return null == subScore ? null : subScore._score;
        }

        public String getAvg(final String subclasscd) {
            final SubclassScore subScore;
            if (SUBCLASSCD999999.equals(subclasscd)) {
                subScore = _subclassScore999999;
            } else {
                subScore = (SubclassScore) _subclassScore.get(subclasscd);
            }
            return null == subScore ? null : subScore.getAvg();
        }

        public static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }

//        /**
//         *
//         * @param flg 1:学年開始日 2:指定日付 0:どちらか
//         * @return
//         */
//        public boolean isRyugakuKyugaku(final int flg) {
//            return flg == 1 && null != _transfercd1 || flg == 2 && null != _transfercd2 || flg == 0 && (null != _transfercd1 || null != _transfercd2);
//        }
//
//        public boolean isTenhennyuugaku(final Param param) {
//            final boolean isTenhennyuugaku = ("4".equals(_entdiv) || "5".equals(_entdiv)) && (null == _entdate || param._yearSdate.compareTo(_entdate) <= 0);
////            if (null != _entdate) {
////                log.info(" " + toString() + " Tenhennyuugaku " + _entdate + " ( " + param._yearSdate + ")");
////            }
//            return isTenhennyuugaku;
//        }

        public boolean isJoseki(final Param param, final String date) {
            final boolean isJoseki = null != _grddiv && !"4".equals(_grddiv) && null != _grddate && ((param._yearSdate.compareTo(_grddate) <= 0 && (null == date || _grddate.compareTo(date) <= 0)));
//            if (isJoseki) {
//                log.debug(" " + toString() + " joseki = " + isJoseki + " : " + _grddiv + " / "   + _grddate + " ( " + param._yearSdate + ", " + date + ")");
//            }
            return isJoseki;
        }

        public String toString() {
            return "Student(" + _schregno + ")";
        }

    }

    /**
     * 1日出欠データ
     */
    private static class Attendance {

        final int _lesson;
        /** 忌引 */
        final int _mourning;
        /** 出停 */
        final int _suspend;
        /** 留学 */
        final int _abroad;
        /** 出席すべき日数 */
        final int _mlesson;
        /** 公欠 */
        final int _absence;
        final int _attend;
        /** 遅刻 */
        final int _late;
        /** 早退 */
        final int _early;

        public Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "[lesson=" + _lesson +
            ",mlesson=" + _mlesson +
            ",mourning=" + _mourning +
            ",suspend=" + _suspend +
            ",abroad=" + _abroad +
            ",absence=" + _absence +
            ",attend=" + _attend +
            ",late=" + _late +
            ",leave=" + _early;
        }
    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrNameabbv;
        final List _studentList;

        HrClass(
            final String grade,
            final String hrClass,
            final String hrNameabbv
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _studentList = new ArrayList();
        }

        public String getCode() {
            return _grade + _hrClass;
        }

        public static List getZaisekiList(final List studentList, final String sex, final Param param, final String date) {
            final List list = new ArrayList();
            for (final Iterator it =studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if ((null == sex || sex.equals(student._sex)) && !student.isJoseki(param, date)) {
                    list.add(student);
                }
            }
            return list;
        }

        public static HrClass getHrClass(final String grade, final String hrClass, final List hrClassList) {
            for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
                HrClass hrclass = (HrClass) it.next();
                if (hrclass._grade.equals(grade) && hrclass._hrClass.equals(hrClass)) {
                    return hrclass;
                }
            }
            return null;
        }

        public static List getHrClassList(final List studentList) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == getHrClass(student._grade, student._hrClass, list)) {
                    list.add(new HrClass(student._grade, student._hrClass, student._hrNameabbv));
                }
                final HrClass hrclass = getHrClass(student._grade, student._hrClass, list);
                hrclass._studentList.add(student);
            }
            return list;
        }

        public String toString() {
            return "HrClass(" + _grade + _hrClass + ":" + _hrNameabbv + ")";
        }
    }

    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _classabbv;
        final String _subclassname;
        final String _subclassabbv;
        String _subclassScoreAllNull;
        // この科目を合併元科目とするコース
        final Set _attendSubclassFlg = new HashSet();
        // この科目を合併先科目とするコース
        final Set _combinedSubclassFlg = new HashSet();
        Subclass(
            final String subclasscd,
            final String classabbv,
            final String subclassname,
            final String subclassabbv
        ) {
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Subclass)) return -1;
            final Subclass s = (Subclass) o;
            return _subclasscd.compareTo(s._subclasscd);
        }

        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname + ")";
        }
    }

    /**
     * 生徒の科目の得点
     */
    private static class SubclassScore {
        final Student _student;
        final Subclass _subclass;
        final String _score;
        final BigDecimal _avg;
        final DivRank _gradeRank;
        final DivRank _classRank;
        final DivRank _courseRank;
        final DivRank _majorRank;


        SubclassScore(
            final Student student,
            final Subclass subclass,
            final String score,
            final BigDecimal avg,
            final DivRank gradeRank,
            final DivRank classRank,
            final DivRank courseRank,
            final DivRank majorRank
        ) {
            _student = student;
            _subclass = subclass;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _classRank = classRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
        }

        public String getRank(final String rankDiv, final Param param) {
            final DivRank divRank;
//            if (OUTPUTRANK_HR.equals(rankDiv)) {
//                divRank = _classRank;
//            } else if (OUTPUTRANK_MAJOR.equals(rankDiv)) {
//                divRank = _majorRank;
//            } else if (OUTPUTRANK_COURSE.equals(rankDiv)) {
//                divRank = _courseRank;
//            } else {
                divRank = _gradeRank;
//            }
            return divRank.get(param);
        }

        public String getAvg() {
            return null == _avg ? null : _avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        public static String getSubclassScoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
//            stb.append("   WITH REL_COUNT AS (");
//            stb.append("   SELECT SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("     , COUNT(*) AS COUNT ");
//            stb.append("          FROM RELATIVEASSESS_MST ");
//            stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
//            stb.append("   GROUP BY SUBCLASSCD");
//            stb.append("     , CLASSCD ");
//            stb.append("     , SCHOOL_KIND ");
//            stb.append("     , CURRICULUM_CD ");
//            stb.append("   ) ");

            stb.append("   WITH SCORE_ALL_NULL_SUBCLASS AS (");
            stb.append("   SELECT T1.SUBCLASSCD");
            stb.append("     , T1.CLASSCD ");
            stb.append("     , T1.SCHOOL_KIND ");
            stb.append("     , T1.CURRICULUM_CD ");
            stb.append("    FROM RECORD_SCORE_DAT T1 ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T1.YEAR ");
//                stb.append("         AND PROV.CLASSCD = T1.CLASSCD ");
//                stb.append("         AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                stb.append("         AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
//                stb.append("         AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
//                stb.append("         AND PROV.SCHREGNO = T1.SCHREGNO ");
//            }
            stb.append("    WHERE T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("        AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testcd + "' ");
            stb.append("   GROUP BY T1.SUBCLASSCD");
            stb.append("     , T1.CLASSCD ");
            stb.append("     , T1.SCHOOL_KIND ");
            stb.append("     , T1.CURRICULUM_CD ");
            stb.append("    HAVING MAX(T1." + (param._testcd.startsWith("99") ? "VALUE" : "SCORE") + ") IS NULL ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                if ("1".equals(param._kariHyotei)) {
//                    stb.append("    AND MIN(PROV.PROV_FLG) IS NOT NULL ");
//                } else {
//                    stb.append("    OR MIN(PROV.PROV_FLG) IS NOT NULL ");
//                }
//            }
            stb.append("   ) ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                getHyoteiDataSql(param, stb);
//            }
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T20.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T6.CLASSABBV, ");
            stb.append("     T3.SUBCLASSNAME, ");
            stb.append("     T3.SUBCLASSABBV, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T2.AVG, ");
            stb.append("     T2.GRADE_RANK, ");
            stb.append("     T2.GRADE_AVG_RANK, ");
            stb.append("     T2.CLASS_RANK, ");
            stb.append("     T2.CLASS_AVG_RANK, ");
            stb.append("     T2.COURSE_RANK, ");
            stb.append("     T2.COURSE_AVG_RANK, ");
            stb.append("     T2.MAJOR_RANK, ");
            stb.append("     T2.MAJOR_AVG_RANK, ");
            stb.append("     CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN '1' END AS SCORE_ALL_NULL ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND BASE.ENT_DATE > '" + param._edate + "') ");
            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
            stb.append(" INNER JOIN RECORD_SCORE_DAT T20 ON T20.YEAR = T1.YEAR ");
            stb.append("     AND T20.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T20.TESTKINDCD || T20.TESTITEMCD || T20.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T20.SCHREGNO = T1.SCHREGNO ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                stb.append(" LEFT JOIN HYOTEI_DATA T2 ON T2.SUBCLASSCD = T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || T20.SUBCLASSCD ");
//                stb.append("     AND T2.SCHREGNO = T20.SCHREGNO ");
//            } else {
                stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T20.YEAR ");
                stb.append("     AND T2.SEMESTER = T20.SEMESTER ");
                stb.append("     AND T2.TESTKINDCD = T20.TESTKINDCD ");
                stb.append("     AND T2.TESTITEMCD = T20.TESTITEMCD ");
                stb.append("     AND T20.SCORE_DIV = T2.SCORE_DIV ");
                stb.append("     AND T2.CLASSCD = T20.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T20.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T20.CURRICULUM_CD ");
                stb.append("     AND T2.SUBCLASSCD = T20.SUBCLASSCD ");
                stb.append("     AND T2.SCHREGNO = T20.SCHREGNO ");
//            }
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T20.SUBCLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T20.CURRICULUM_CD ");
            stb.append("     AND T3.CLASSCD = T20.CLASSCD ");
            stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ON T4.YEAR = T20.YEAR ");
            stb.append("     AND T4.SEMESTER = T20.SEMESTER ");
            stb.append("     AND T4.TESTKINDCD = T20.TESTKINDCD ");
            stb.append("     AND T4.TESTITEMCD = T20.TESTITEMCD ");
            stb.append("     AND T4.SCORE_DIV = T20.SCORE_DIV ");
            stb.append("  LEFT JOIN CLASS_MST T6 ON T6.CLASSCD = T20.CLASSCD ");
            stb.append("     AND T6.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("  LEFT JOIN SCORE_ALL_NULL_SUBCLASS T9 ON T9.SUBCLASSCD = T20.SUBCLASSCD ");
            stb.append("     AND T9.CLASSCD = T20.CLASSCD ");
            stb.append("     AND T9.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("     AND T9.CURRICULUM_CD = T20.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + param.regdSemester() + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" AND T20.CLASSCD <= '90' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     '00' || '-' || '00' || '-' || '00' || '-' || ");
            stb.append("     T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSABBV, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T2.AVG, ");
            stb.append("     T2.GRADE_RANK, ");
            stb.append("     T2.GRADE_AVG_RANK, ");
            stb.append("     T2.CLASS_RANK, ");
            stb.append("     T2.CLASS_AVG_RANK, ");
            stb.append("     T2.COURSE_RANK, ");
            stb.append("     T2.COURSE_AVG_RANK, ");
            stb.append("     T2.MAJOR_RANK, ");
            stb.append("     T2.MAJOR_AVG_RANK, ");
//            stb.append("     CAST(NULL AS SMALLINT) AS SLUMP_SCORE, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK_CD, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCORE_ALL_NULL ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND BASE.ENT_DATE > '" + param._edate + "') ");
            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
//            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
//                stb.append(" INNER JOIN HYOTEI_DATA T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//            } else {
                stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
//            }
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + param.regdSemester() + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" AND T2.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            return stb.toString();
        }

        public String toString() {
            return "SubclassScore(" + _subclass + ", " + _score + ", " + _avg + ")";
        }

        public Number compareValue(final String rankDiv, final Param param) {
            final String rank = getRank(rankDiv, param);
            if (!NumberUtils.isDigits(rank)) {
                return null;
            }
            return new Integer(rank);
        }

        static class DivRank {
            final String _rank;
            final String _avgRank;
            DivRank(final String rank, final String avgRank) {
                _rank = rank;
                _avgRank = avgRank;
            }
            String get(final Param param) {
                final String rank;
                if ("2".equals(param._outputKijun)) {
                    rank = _avgRank;
                } else {
                    rank = _rank;
                }
                return rank;
            }
        }
    }

    private static List getFushinKamokuStudentList(final List studentList, final int fushinScore, final int fushinSu) {
        final List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final List fushinKamokuList = getFushinKamokuList(Collections.singletonList(student), fushinScore);
            if (fushinKamokuList.size() >= fushinSu) {
                rtn.add(student);
            }
        }
        return rtn;
    }

    private static List getFushinKamokuList(final List studentList, final int fushinScore) {
        final List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            for (final Iterator sit = student._subclassScore.values().iterator(); sit.hasNext();) {
                final SubclassScore subScore = (SubclassScore) sit.next();
                if (subScore._subclass._attendSubclassFlg.contains("1")) {
                    // 元科目は含まない
                    continue;
                }
                if (NumberUtils.isNumber(subScore._score) && Double.parseDouble(subScore._score) < fushinScore) {
                    rtn.add(subScore);
                }
            }
        }
        return rtn;
    }

    private static List getFushinHeikinStudentList(final List studentList, final int fushinHeikin) {
        final List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._subclassScore999999 && null != student._subclassScore999999._avg && student._subclassScore999999._avg.doubleValue() < fushinHeikin) {
                rtn.add(student);
            }
        }
        return rtn;
    }

    private static List getAttendOverStudentList(final Param param, final List studentList) {
        List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._attendance) {
                if (student._attendance._absence >= param._kesseki) {
                    rtn.add(student);
                }
            }
        }
        return rtn;
    }

    private static Map newRecord(final List recordList) {
        final Map record = new HashMap();
        recordList.add(record);
        return record;
    }

    private static void putName(final Map record, final String field, final String name) {
        final int keta = getMS932ByteLength(name);
        record.put(field + (keta <= 14 ? "_1" : keta <= 20 ? "_2" : "_3"), name);
    }

    private static class StudentRankComparator implements Comparator {

        final String _rankDiv;
        final Param _param;

        public StudentRankComparator(final String rankDiv, final Param param) {
            _rankDiv = rankDiv;
            _param = param;
        }

        public int compare(final Object o1, final Object o2) {
            final Student std1 = (Student) o1;
            final Student std2 = (Student) o2;
            if (null == std1._subclassScore999999 && null == std2._subclassScore999999) {
                return std1.getHrclassAttendnoCd().compareTo(std2.getHrclassAttendnoCd());
            } else if (null == std1._subclassScore999999) {
                return 1;
            } else if (null == std2._subclassScore999999) {
                return -1;
            } else if (null == std1._subclassScore999999.compareValue(_rankDiv, _param) && null == std2._subclassScore999999.compareValue(_rankDiv, _param)) {
                return std1.getHrclassAttendnoCd().compareTo(std2.getHrclassAttendnoCd());
            } else if (null == std1._subclassScore999999.compareValue(_rankDiv, _param)) {
                return 1;
            } else if (null == std2._subclassScore999999.compareValue(_rankDiv, _param)) {
                return -1;
            }
            final Double v1 = new Double(std1._subclassScore999999.compareValue(_rankDiv, _param).doubleValue());
            final Double v2 = new Double(std2._subclassScore999999.compareValue(_rankDiv, _param).doubleValue());
            final int rtn = v1.compareTo(v2);
            if (rtn != 0) { // 昇順
                return rtn;
            }
            return std1.getHrclassAttendnoCd().compareTo(std2.getHrclassAttendnoCd());
        }
    }

    private static class TestItem {
        final String _year;
        final String _semester;
        final String _semestername;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        public TestItem(final String year, final String semester, final String semestername,
                final String testkindcd, final String testitemcd, final String scoreDiv, final String testitemname
                ) {
            _year = year;
            _semester = semester;
            _semestername = semestername;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
        }
        public String getSemeTestcd() {
            return _semester + "-" +_testkindcd + "-" +_testitemcd + "-" + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester + _testkindcd + _testitemcd + _scoreDiv + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 68412 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _grade;
        final String _testcd;
        final String _sdate;
        final String _dateDiv;
        final String _date;
        final String _edate;
        final int _kesseki;
        final int _yuryo;
        final String _outputKijun; // 1:総計 2:平均点
        final String _useCurriculumcd;
        final TestItem _testItem;
        private String _yearSdate;
        final String _gradename1;
        final String _schoolKind;
        final String _schoolname;
        final BigDecimal _hyokaHeikin;
        final boolean _isOutputDebug;

        final Map _subclassMap;

        private Map _subclassReplaceCombinedDatListMap = Collections.EMPTY_MAP;

        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");

            _testcd = request.getParameter("TEST_CD");
            _edate = request.getParameter("EDATE").replace('/', '-');
            _kesseki = toInt(request.getParameter("KESSEKI"), 0);
            _yuryo = Math.max(toInt(request.getParameter("YURYO"), 0), 0);
            _hyokaHeikin = toBigDecimal(request.getParameter("HYOKA_HEIKIN"));
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _yearSdate = getYearSdate(db2);
            _subclassMap = getSubclassMap(db2);
            _dateDiv = request.getParameter("DATE_DIV");
            _date = request.getParameter("DATE").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE");

            _testItem = getTestKindItem(db2);
            _gradename1 = getRegdGdat(db2, "GRADE_NAME1");
            _schoolKind = getRegdGdat(db2, "SCHOOL_KIND");

            _schoolname = getSchoolName(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getSchoolName(final DB2UDB db2) {
        	final boolean hasSchoolKind = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");

        	String sql = "";
        	sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
        	if (hasSchoolKind) {
        		sql += " AND SCHOOL_KIND = '" + _schoolKind + "' ";
        	}

			return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
		}

		private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD234W' AND NAME = '" + propName + "' "));
        }

        public String regdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }

        private static int toInt(final String s, final int defaultInt) {
            return NumberUtils.isNumber(s) ? Integer.parseInt(s) : defaultInt;
        }

        private static BigDecimal toBigDecimal(final String s) {
            return NumberUtils.isNumber(s) ? new BigDecimal(s) : null;
        }

        private String getYearSdate(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SDATE ");
            stb.append(" FROM SEMESTER_MST T1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER = '9' ");
            String yearSdate = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            if (null == yearSdate) {
                yearSdate = _year + "-04-01";
            }
            return yearSdate;
        }

        private String getRegdGdat(final DB2UDB db2, final String field) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     " + field + " ");
            stb.append(" FROM SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");
            final String gradename1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            log.info(" " + field + " = " + gradename1);
            return gradename1;
        }

        private TestItem getTestKindItem(final DB2UDB db2) {
            TestItem testItem = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T2.SEMESTERNAME, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME ");
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append(" LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("   AND T2.SEMESTER = T1.SEMESTER ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
                stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testcd + "' ");
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    testItem = new TestItem(year, semester, semestername, testkindcd, testitemcd, scoreDiv, testitemname);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testItem = " + testItem);
            return testItem;
        }

        private Map getSubclassMap(DB2UDB db2) {
            Map map = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();

                stb.append(" WITH ATTEND AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.ATTEND_CLASSCD, T1.ATTEND_SCHOOL_KIND, T1.ATTEND_CURRICULUM_CD, T1.ATTEND_SUBCLASSCD ");
                stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
                stb.append(" WHERE YEAR = '" + _year + "' ");
                stb.append(" ), COMBINED AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.COMBINED_CLASSCD, T1.COMBINED_SCHOOL_KIND, T1.COMBINED_CURRICULUM_CD, T1.COMBINED_SUBCLASSCD ");
                stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
                stb.append(" WHERE YEAR = '" + _year + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.CLASSABBV, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSABBV, ");
                stb.append("     'ATTEND' AS DIV ");
                stb.append(" FROM SUBCLASS_MST T1 ");
                stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" LEFT JOIN ATTEND T3 ON T3.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("   AND T3.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND T3.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.CLASSABBV, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSABBV, ");
                stb.append("     'COMBINED' AS DIV ");
                stb.append(" FROM SUBCLASS_MST T1 ");
                stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" LEFT JOIN COMBINED T3 ON T3.COMBINED_CLASSCD = T1.CLASSCD ");
                stb.append("   AND T3.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("   AND T3.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("   AND T3.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
                log.debug(" subclass sql ="  + stb.toString());

                for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    if (!map.containsKey(subclasscd)) {
                        final String classabbv = KnjDbUtils.getString(row, "CLASSABBV");
                        final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                        final String subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                        map.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv));
                    }
                    if ("ATTEND".equals(KnjDbUtils.getString(row, "DIV"))) {
                    	final Subclass subclass = (Subclass) map.get(subclasscd);
                    	subclass._attendSubclassFlg.add("1");
                    }
                    if ("COMBINED".equals(KnjDbUtils.getString(row, "DIV"))) {
                    	final Subclass subclass = (Subclass) map.get(subclasscd);
                    	subclass._combinedSubclassFlg.add("1");
                    }
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            return map;
        }
    }
}

// eof

