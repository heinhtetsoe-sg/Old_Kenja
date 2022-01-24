/*
 * $Id: e88e2655f12d048b7fbd31b715db8d182185d3f1 $
 *
 * 作成日: 2013/07/02
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD620V {

    private static final Log log = LogFactory.getLog(KNJD620V.class);

    private static final String FROM_TO_MARK = "\uFF5E";

    private static final int _charsPerColumn = 2; // 1列あたりの文字数
    private int MAX_COLUMN1 = 26;
    private static final int MAX_COLUMN2 = 27;
    private static final String ALL_SEME = "9";
    private static final String HYOTEI_TESTCD = "9990009";

    private static final String KARIHYOUTEI_SCORE_DIV = "09";
    private static final String SIDOU_INPUT_INF_SCORE = "2";
    private static final String SIDOU_INPUT_INF_MARK = "1";

    private static final String PRGID_KNJD128L = "KNJD128L"; // 札幌開成 成績入力
    private static final String PRGID_KNJD128V = "KNJD128V"; // 宮城県その他 成績入力
    private static final String PRGID_KNJD129D = "KNJD129D"; // 文京中学 成績入力
    private static final String PRGID_KNJD129E = "KNJD129E"; // 武蔵野東高校 成績入力
    private static final String PRGID_KNJD620L = "KNJD620L"; // 札幌開成帳票
    private static final String PRGID_KNJD620V = "KNJD620V";

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

            final Subclass subclass = _param._subclass;
            printMain(db2, svf, subclass);
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

    private static int getMS932ByteLength(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

//	private static String[] tobytes(final char ch, final String encoding) throws UnsupportedEncodingException {
//    	final String xtab = "0123456789ABCDEF";
//		final byte[] bytes = String.valueOf(ch).getBytes(encoding);
//		final String[] sbytes = new String[bytes.length];
//		for (int bi = 0; bi < bytes.length; bi++) {
//			final int b = bytes[bi] + (bytes[bi] < 0 ? 256 : 0);
//			sbytes[bi] = String.valueOf(xtab.charAt(b / 16)) + String.valueOf(xtab.charAt(b % 16));
//		}
//		return sbytes;
//	}

    private static String getByteSubstring(final String str, final int keta, final int range) {
        if (null == str) {
            return null;
        }
        int totalbytelen = 0;
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            final String ch = str.substring(i, i + 1);
            totalbytelen += getMS932ByteLength(ch);
            if (keta < totalbytelen && totalbytelen <= keta + range) {
                stb.append(ch);
            }
        }
        return stb.toString();
    }

    private List groupList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student s = (Student) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            s._gyo = current.size() + 1;
            current.add(s);
        }
        return rtn;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Subclass subclass) {
        int lineMax = _param._useStudentLine55 ? 55 : 50;
        String form1 = _param._useStudentLine55 ? "KNJD620V_1_2.frm" : _param._useFormA3Yoko ? "KNJD620V_1_3.frm" : "KNJD620V.frm";
        String form2 = _param._useStudentLine55 ? "KNJD620V_2_2.frm" : "KNJD620V_2.frm";
        if (null != _param._useFormNameKNJD620V && !"".equals(_param._useFormNameKNJD620V)) {
            form1 = _param._useFormNameKNJD620V + ".frm";
            if ("KNJD620V_1_2.frm".equals(form1)) {
                lineMax = 55;
                form2 = "KNJD620V_2_2.frm";
            } else {
                lineMax = 50;
                form2 = "KNJD620V_2.frm";
            }
        }
        final List chairList = getChairList(db2, _param);
        if (_param._useFormA3Yoko || "KNJD620V_1_3.frm".equals(form1)) {
            MAX_COLUMN1 = 63;
        }
        if (_param._isOutputDebug) {
        	log.info(" form1 = " + form1 + ", form2 = " + form2);
        }

        for (final Iterator it = chairList.iterator(); it.hasNext();) {
            final Chair chair = (Chair) it.next();

            if (_param._isOutputDebug) {
            	log.info(" chair = " + chair._chaircd + ":" + chair._chairname);
            }

            for (final Iterator ith = chair._hrClassList.iterator(); ith.hasNext();) {
                final Hrclass hrclass = (Hrclass) ith.next();
                final Map hyoteiBunpuMap = _param.getHyoteiBunpuMap(db2, hrclass._studentList);

                if (_param._isOutputDebug) {
                	log.info(" hrclass = " + hrclass._gradehrclass);
                }

                for (final Iterator sit = groupList(hrclass._studentList, lineMax).iterator(); sit.hasNext();) {
                    final List studentList = (List) sit.next();

                    svf.VrSetForm(form1, 4);

                    printHeader(db2, svf, subclass, chair, hrclass, studentList, hrclass._studentList, hyoteiBunpuMap);

                    int column = 0;
                    // 成績
                    for (int i = 0, colsize = _param._testKindItemList.size(); i < colsize; i++) {
                        final TestItem testItem = (TestItem) _param._testKindItemList.get(i);
                        printColumn1(svf, subclass, studentList, hrclass._studentList, "成績", i, testItem, colsize, column, hyoteiBunpuMap);
                        column += 1;
                        svf.VrEndRecord();
                    }

                    if (PRGID_KNJD128L.equals(_param._prgid) || PRGID_KNJD620L.equals(_param._prgid)) {
                        final String n = "N";
                        // 履修単位数
                        printColumn3Tanni(svf, studentList, subclass, column, n, 0);
                        column += 1;
                        svf.VrEndRecord();
                        // 修得単位数
                        printColumn3Tanni(svf, studentList, subclass, column, n, 1);
                        column += 1;
                        svf.VrEndRecord();
                    }

                    // 追指導の列リスト
                    final List testKindItemSidouInputList = _param.getTestKindItemSidouInputList();
                    final Map sidouInputSemes = new HashMap();
                    for (int i = 0, colsize = testKindItemSidouInputList.size(); i < colsize; i++) {
                        final TestItem testItem = (TestItem) testKindItemSidouInputList.get(i);
                        final String key = testItem._semester._cdSemester;
                        if (null == sidouInputSemes.get(key)) {
                            sidouInputSemes.put(key, new ArrayList());
                        }
                        ((List) sidouInputSemes.get(key)).add(testItem);
                    }

                    // 下の"追指導者数合計"
                    final int subtitle8line = (_param._attendSemesterDetailList.size() + ("1".equals(_param._printJugyoJisu) ? 1 : 0) + 1); // 欠課時数 + ("1".equals(_param._printJugyoJisu) ? 授業時数) + 出席すべき時数
                    final String tuisidoushaSuGoukeiText = testKindItemSidouInputList.size() == 0 ? "" : "追指導者数合計";
                    final String tuisidoushaSubtitle = center(tuisidoushaSuGoukeiText, subtitle8line);
                    final String[] tuisidoushaSubtitleArray = new String[subtitle8line];
                    for (int i = 0; i < subtitle8line; i++) {
                        if (i < tuisidoushaSubtitleArray.length && Math.min(tuisidoushaSubtitle.length(), (i + 1) * 2) > i * 2) {
                            tuisidoushaSubtitleArray[i] = tuisidoushaSubtitle.substring(i * 2, Math.min(tuisidoushaSubtitle.length(), (i + 1) * 2));
                        }
                    }

                    if (_param._isPrintAttendInfo) {
                        final String n = 0 == testKindItemSidouInputList.size() ? "N" : ""; // "追指導者数合計"を表示しない場合、下線を表示しない
                        int subtitle8Idx = 0;

                        // 欠課時数
                        for (int i = 0, colsize = _param._attendSemesterDetailList.size(); i < colsize; i++) {
                            final SemesterDetail semesterDetail = (SemesterDetail) _param._attendSemesterDetailList.get(i);
                            printColumn2(svf, studentList, subclass, "欠課時数", i, semesterDetail, colsize, column, tuisidoushaSubtitleArray, subtitle8Idx, n);
                            column += 1;
                            subtitle8Idx += 1;
                            svf.VrEndRecord();
                        }

                        if ("1".equals(_param._printJugyoJisu)) {
                            // 授業時数
                            printColumn3(svf, studentList, subclass, "授業時数", "JUGYO_JISU", "J", column, tuisidoushaSubtitleArray, subtitle8Idx, "N".equals(n) ? "N" : "_2");
                            column += 1;
                            subtitle8Idx += 1;
                            svf.VrEndRecord();
                        }

                        // 出席すべき時数
                        printColumn3(svf, studentList, subclass, "出席すべき時数", "MUST", "M", column, tuisidoushaSubtitleArray, subtitle8Idx, n);
                        column += 1;
                        subtitle8Idx += 1;
                        svf.VrEndRecord();
                    }

                    // 追指導
                    for (int i = 0, colsize = testKindItemSidouInputList.size(); i < colsize; i++) {
                        final TestItem testItem = (TestItem) testKindItemSidouInputList.get(i);
                        printColumn4(svf, studentList, hrclass._studentList, subclass, "追指導", i, testItem, (List) sidouInputSemes.get(testItem._semester._cdSemester), colsize, column);
                        column += 1;
                        svf.VrEndRecord();
                    }

//                    // 仮評定
//                    final List testKindItemKariHyouteiList = _param._kariHyouteiList;
//                    for (int i = 0, colsize = testKindItemKariHyouteiList.size(); i < colsize; i++) {
//                        final TestItem testItem = (TestItem) testKindItemKariHyouteiList.get(i);
//                        printColumn5(svf, chair._studentList, subclass, "仮評定", i, testItem, colsize);
//                        column += 1;
//                        svf.VrEndRecord();
//                    }

                    // 判定
                    if (!_param._notPrintHantei) {
                    	printColumn6(svf, studentList, subclass, column);
                    	column += 1;
                    	svf.VrEndRecord();
                    }

                    if (column < MAX_COLUMN1 * 1) {
                        // 茗溪学園の場合、2ページに印刷しない(1ページに印刷する)
                        if (_param._notPrintPage2) {
                            int BIKO_KETA = 4;
                            for (int j = 0; j < studentList.size(); j++) {
                                final Student student = (Student) studentList.get(j);
                                final String remark = student.getRemark(subclass, _param);
                                final String recinfoRemark = student.getRecinfoRemark(subclass, _param);
                                final String remarkAll = StringUtils.defaultString(remark) + StringUtils.defaultString(recinfoRemark);
                                if (!StringUtils.isBlank(remarkAll)) {
                                    final String[] token = KNJ_EditEdit.get_token(remarkAll, BIKO_KETA, 999);
                                    student._remarkArrayMap.put(subclass._subclasscd, token);
                                    int printKeta = 0;
                                    for (int i = 0; i < token.length; i++) {
                                        printKeta += getMS932ByteLength(token[i]);
                                        getMappedMap(student._remarkAttributeXMap, subclass._subclasscd).put(new Integer(i + 1), new Integer(printKeta));
                                    }
                                }
                            }
                        }
                        // 備考
                        final int bikoLen = MAX_COLUMN1 * 1 - column;
                        final int bikoStartColumn = column;
                        if (bikoLen > 2) {
                            for (int i = 0; i < bikoLen; i++) {
                                // log.debug(" biko " + column);
                                printColumn7(1, svf, studentList, subclass, "備考", i, bikoLen, _param._notPrintPage2 ? true : false, bikoStartColumn);
                                column += 1;
                                svf.VrEndRecord();
                            }
                        }
                    }

                    printLine(svf);
                    column += 1;
                    svf.VrEndRecord();

                    // 備考専用ページ印刷
                    boolean printPage2 = true;
                    if (_param._notPrintPage2) {
                        // 茗溪学園の場合、2ページに印刷しない(1ページに印刷する)
                        printPage2 = false;
                    } else if (StringUtils.isBlank(_param._remarkTestcd) && StringUtils.isBlank(_param._remarkFlgTestcd)) {
                        // 備考(指導理由等)考査がブランクの場合、印刷しない
                        printPage2 = false;
                    } else {
                        // すべての生徒の備考がブランクの場合、印刷しない
                        boolean remarkIsNotBlank = false;
                        for (int j = 0; j < studentList.size(); j++) {
                            final Student student = (Student) studentList.get(j);
                            final String remark = student.getRemark(subclass, _param);
                            final String recinfoRemark = student.getRecinfoRemark(subclass, _param);
                            final String remarkAll = StringUtils.defaultString(remark) + StringUtils.defaultString(recinfoRemark);
                            if (!StringUtils.isBlank(remarkAll)) {
                                remarkIsNotBlank = true;
                                break;
                            }
                        }
                        if (!remarkIsNotBlank) {
                            printPage2 = false;
                        }
                    }
                    if (printPage2) {
                        // 備考専用ページ
                    	int BIKO_KETA = 4;
                        svf.VrSetForm(form2, 4);

                        printHeader(db2, svf, subclass, chair, hrclass, studentList, hrclass._studentList, hyoteiBunpuMap);

                        for (int j = 0; j < studentList.size(); j++) {
                            final Student student = (Student) studentList.get(j);
                            final String remark = student.getRemark(subclass, _param);
                            final String recinfoRemark = student.getRecinfoRemark(subclass, _param);
                            final String remarkAll = StringUtils.defaultString(remark) + StringUtils.defaultString(recinfoRemark);
                            if (!StringUtils.isBlank(remarkAll)) {
                            	final String[] token = KNJ_EditEdit.get_token(remarkAll, BIKO_KETA, 999);
                                student._remarkArrayMap.put(subclass._subclasscd, token);
                                int printKeta = 0;
                                for (int i = 0; i < token.length; i++) {
                                	printKeta += getMS932ByteLength(token[i]);
                                	getMappedMap(student._remarkAttributeXMap, subclass._subclasscd).put(new Integer(i + 1), new Integer(printKeta));
                                }
                            }
                        }

                        // 備考
                        //for (int i = 0, bikoLen = MAX_COLUMN2 - (0 == column ? MAX_COLUMN2 : column % MAX_COLUMN2); i < bikoLen; i++) {
                        final int bikoStartColumn = 0;
                        for (int i = 0, bikoLen = MAX_COLUMN2; i < bikoLen; i++) {
                            printColumn7(2, svf, studentList, subclass, "備考", i, bikoLen, true, bikoStartColumn);
                            svf.VrEndRecord();
                        }
                    }
                }
            }
        }
    }

    private static String center(final String text, final int columnSize) {
        final boolean isOdd = (columnSize * _charsPerColumn - StringUtils.defaultString(text).length()) % 2 == 1;
        return StringUtils.center(text, columnSize * _charsPerColumn - (isOdd ? 1 : 0), "　") + (isOdd ? "　" : "");
    }

    private static String mkString(final List list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String c = "";
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String s = (String) it.next();
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            stb.append(c).append(s);
            c = comma;
        }
        return stb.toString();
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Subclass subclass, final Chair chair, final Hrclass hrclass, final List studentList, final List studentAllList, final Map hyoteiBunpuMap) {
        svf.VrsOut("TITLE", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度　成績小表"); // タイトル
        svf.VrsOut("SUBCLASSCD", "(" + subclass.getKeySubclassCd() + ")"); // 科目コード
        final String kuroMaru = subclass._isSaki ? "●" : "";
        svf.VrsOut("SUBCLASS_NAME" + (getMS932ByteLength(subclass._subclassname) > 20 ? "2" : "")  , kuroMaru + subclass._subclassname); // 科目名
        svf.VrsOut("PRINTDAY", _param._now); // 印刷日
        svf.VrsOut("CLASS_TNAME", "教科担任氏名"); // 教科担当職名名称

        if ("1".equals(_param._printTannin)) {
            final String staffName = mkString(chair._chairStfNameList, "、");
            svf.VrsOut("CLASS_TEACHER" + (getMS932ByteLength(staffName) > 40 ? "2" : ""), staffName); // 教科担任名
        } else {
            svf.VrsOut("INKAN1", "印");
            svf.VrsOut("INKAN_MARU1", "○");
            svf.VrsOut("INKAN2", "印");
            svf.VrsOut("INKAN_MARU2", "○");
            if (chair._chairStfNameList.size() > 0) {
                final String staffName = (String) chair._chairStfNameList.get(0);
                svf.VrsOut("CLASS_TEACHER" + (getMS932ByteLength(staffName) > 20 ? "2" : ""), staffName); // 教科担任名
            }
            if (chair._chairStfNameList.size() > 1 && PRGID_KNJD128V.equals(_param._prgid)) {
                final String staffName = (String) chair._chairStfNameList.get(1);
                svf.VrsOut("HR_TNAME", "教科担任氏名"); // 教科担当職名名称
                svf.VrsOut("HR_TEACHER" + (getMS932ByteLength(staffName) > 20 ? "2" : ""), staffName); // 教科担任名
            } else if (chair._chairStfNameFukuList.size() > 0 && PRGID_KNJD128V.equals(_param._prgid)) {
                final String staffName = (String) chair._chairStfNameFukuList.get(0);
                svf.VrsOut("HR_TNAME", "教科副担任氏名"); // 教科副担当職名名称
                svf.VrsOut("HR_TEACHER" + (getMS932ByteLength(staffName) > 20 ? "2" : ""), staffName); // 教科副担任名
            }
        }

        String course = "";
        if ("1".equals(_param._printDiv)) {
            svf.VrsOut("HR_TNAME", "学級担任氏名"); // 学級担任職名名称
            svf.VrsOut("HR_TEACHER" + (getMS932ByteLength(hrclass._hrStfStfName) > 20 ? "2" : ""), hrclass._hrStfStfName); // 学級担任名
            final String hrName = StringUtils.defaultString(hrclass._hrName);
            String majorname = "";
            if (!studentAllList.isEmpty()) {
            	final Map majornameMap = new TreeMap();
            	for (final Iterator it = studentAllList.iterator(); it.hasNext();) {
            		final Student student = (Student) it.next();
            		if (null != student._courseMajor && null != student._majorname) {
            			majornameMap.put(student._courseMajor, student._majorname);
            		}
            	}
            	if (majornameMap.size() > 1) {
            		log.info(" 学科名表示しない:" + majornameMap);
            	} else if (majornameMap.size() == 1) {
            		majorname = StringUtils.defaultString((String) majornameMap.values().iterator().next()) + "　";
            	}
            }
            course = majorname + hrName;
        } else {
            course = "講座名　：" + StringUtils.defaultString(chair._chairname);
        }
        svf.VrsOut("COURSE",   course); // コース年組講座名
        if (!studentAllList.isEmpty()) {
            final TreeSet creditsSet = new TreeSet();
            for (final Iterator it = studentAllList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String credits = (String) student._creditMstCreditsMap.get(subclass.getKeySubclassCd());
                if (NumberUtils.isDigits(credits)) {
                    creditsSet.add(Integer.valueOf(credits));
                }
            }
            if (!creditsSet.isEmpty()) {
                final Integer creditMin = (Integer) creditsSet.first();
                final Integer creditMax = (Integer) creditsSet.last();
                if (creditMin.equals(creditMax)) {
                    svf.VrsOut("CREDIT", creditMin.toString()); // 単位数
                } else {
                    svf.VrsOut("CREDIT", creditMin.toString() + FROM_TO_MARK + creditMax.toString()); // 単位数
                }
            }
        }

        svf.VrsOut("NO_TITLE", "年組番"); // 番号タイトル

        // 生徒氏名
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            // svf.VrsOutn("NO", student._gyo, "1".equals(_param._printDiv) ? student.getAttendNoStr() : student.getGyoNoStr()); // 年組番号
            svf.VrsOutn("NO", student._gyo, student.getAttendNoStr()); // 年組番号
            final int len = getMS932ByteLength(student._name);
            if ("1".equals(_param._use_SchregNo_hyoji)) {
                svf.VrsOutn("NAME" + (len > 30 ? "3" : len > 20 ? "2" : "1") + "_2", student._gyo, student._name); // 氏名
                svf.VrsOutn("SCHREGNO", student._gyo, student._schregno); // 学籍番号
            } else  {
                svf.VrsOutn("NAME" + (len > 30 ? "3" : len > 20 ? "2" : "1"), student._gyo, student._name); // 氏名
            }
        }


        if (_param._notPrintHyoteiBunpu) {
        	if (null != _param._whitespacePath) {
        		svf.VrsOut("HIDE_HYOTEI_BUNPU", _param._whitespacePath); // 評定分布
        	}
        } else {
        	final List hyoteiList = new ArrayList();
        	for (int i = 0; i < studentAllList.size(); i++) {
        		final Student student = (Student) studentAllList.get(i);
        		final String hyotei = student.getScore(subclass, HYOTEI_TESTCD);
        		if (null != hyotei) {
        			hyoteiList.add(hyotei);
        		}
        	}

        	for (int i = 0, max = 5; i < max; i++) {
        		final String hyotei = String.valueOf(i + 1);
        		final String sn = String.valueOf(i + 1);
        		svf.VrsOut("HYOTEI_BUNPU" + sn, formatHyoteiBunpu(hyoteiBunpuMap, hyotei)); // 評定分布
        	}
        	if (hyoteiList.size() != 0) {
        		for (int i = 0, max = 5; i < max; i++) {
        			final String hyotei = String.valueOf(i + 1);
        			final String sn = String.valueOf(i + 1);
        			svf.VrsOut("DEV_NUM" + sn, String.valueOf(count(hyotei, hyoteiList))); // 評定分布
        			svf.VrsOut("DEV_PER" + sn, String.valueOf(percent(count(hyotei, hyoteiList), hyoteiList.size()))); // 評定分布％
        		}
        	}
        }
    }

    private String formatHyoteiBunpu(final Map hyoteiBunpuMap, final String hyotei) {
        if (null == hyoteiBunpuMap.get(hyotei)) {
            return null;
        }
        final Map hyoteiBunpu = (Map) hyoteiBunpuMap.get(hyotei);
        final String high = null == hyoteiBunpu.get("ASSESSHIGH") ? "   " : hyoteiBunpu.get("ASSESSHIGH").toString();
        final String low = null == hyoteiBunpu.get("ASSESSLOW") ? "   " : hyoteiBunpu.get("ASSESSLOW").toString();
        return StringUtils.leftPad(high, 3, ' ') + " " + FROM_TO_MARK + " " + StringUtils.leftPad(low, 3, ' ');
    }

    private static String take(final String name, final int count) {
        return null == name || name.length() < count ? name : name.substring(0, count);
    }

    private static String[] split(final String name, final int keta) {
    	int blankIdx = -1;
    	String[] token0 = {};
    	if (null != name) {
    		token0 = KNJ_EditEdit.get_token(name, keta, 99);
    		for (int i = token0.length - 1; i >= 0; i--) {
    			if (null == token0[i] || "".equals(token0[i])) {
    				blankIdx = i;
    				continue;
    			}
    			break;
    		}
    	}
    	String[] token;
    	if (-1 == blankIdx) {
    		token = token0;
    	} else if (0 == blankIdx) {
    		token = new String[] {};
    	} else {
    		token = new String[blankIdx];
    		for (int i = 0; i < blankIdx; i++) {
    			token[i] = token0[i];
    		}
    	}
    	//log.info(" split(\"" + name + "\" (keta = " + KNJ_EditEdit.getMS932ByteLength(name) + "), " + keta + ") = " + ArrayUtils.toString(token));
    	return token;
    }

    private String ketaCenter(final String semestername, final int keta) {
    	final StringBuffer stb = new StringBuffer();
    	stb.append(StringUtils.repeat(" ", (keta - KNJ_EditEdit.getMS932ByteLength(semestername)) / 2));
    	stb.append(StringUtils.defaultString(semestername));
    	stb.append(StringUtils.repeat(" ", (keta - KNJ_EditEdit.getMS932ByteLength(stb.toString()))));
    	final String rtn = StringUtils.replace(stb.toString(), "  ", "　"); // 半角スペース2つを全角スペース1つに置換
    	//log.info(" ketaCenter \"" + semestername + "\", " + keta + " = \"" + rtn + "\" (keta = " + getMS932ByteLength(rtn) + ")");
		return rtn;
	}

    // 成績
    private void printColumn1(final Vrw32alp svf, final Subclass subclass, final List studentlist, final List studetAllList, final String text, int i, final TestItem testItem, final int colsize, final int column, final Map hyoteiBunpuMap) {
        final boolean last = isMaxColumn(column) || i == colsize - 1;
        final int testItemIdxInSemester = testItem._semester.getTestItemIdx(testItem);
		final boolean start = testItemIdxInSemester == 0;
        final String suf1 = start && last ? "_4" : last ? "_3" : start ? "" : "_2";
        final String suf2 = start && last ? "_4" : last ? "_3" : start ? "_1" : "_2";
        final String title = center(text, colsize);
        svf.VrsOut("SUBTITLE1" + suf1, title.substring(i * _charsPerColumn, i * _charsPerColumn + 2)); // 成績
        final String fieldSem;
        final int columnZenkakuMojisu;
        final String semestername = StringUtils.defaultString(testItem._semester._semestername);
        final int maxColumn = testItem._semester._testItemList.size();
		if (getMS932ByteLength(semestername) <= maxColumn * 2) {
            columnZenkakuMojisu = 2;
            fieldSem = "SEM1" + (start && last ? "_7" : last ? "_5" : start ? "_1" : "_3");
        } else {
            columnZenkakuMojisu = 3;
            fieldSem = "SEM1" + (start && last ? "_8" : last ? "_6" : start ? "_2" : "_4");
        }
        final String[] splittedName = split(ketaCenter(semestername, maxColumn * columnZenkakuMojisu * 2), columnZenkakuMojisu * 2);
        if (testItemIdxInSemester < splittedName.length) {
			svf.VrsOut(fieldSem, splittedName[testItemIdxInSemester]); // 学期
        }
        svf.VrsOut("ITEM1" + suf1, take(testItem._testitemabbv1, 5)); // 素点・評価他
        // log.debug(" ITEM1" + (suf1.length() == 0 ? "  " : suf1) + " = " + testItem._semester._semestername + ":" + testItem._testitemabbv1);

        for (int j = 0; j < studentlist.size(); j++) {
            final Student student = (Student) studentlist.get(j);
            final String score = student.getScore(subclass, testItem);
            final String mikomi = student.getMikomi(subclass, testItem);
            if (NumberUtils.isNumber(mikomi)) {
                svf.VrsOutn("GRADING1" + suf2, student._gyo, score + mikomi); // * + 見込点
            } else {
                final String yomikaeScore = student.getScoreYomikae(_param, subclass, score);
                svf.VrsOutn("GRADING1" + suf2, student._gyo, yomikaeScore); // 評価
            }
        }

        final List scoreList = new ArrayList();
        for (int j = 0; j < studetAllList.size(); j++) {
            final Student student = (Student) studetAllList.get(j);
            final String score = student.getScore(subclass, testItem);
            final String mikomi = student.getMikomi(subclass, testItem);
            if (NumberUtils.isNumber(score)) {
                scoreList.add(score);
            }
            if (NumberUtils.isNumber(mikomi)) {
                scoreList.add(mikomi);
            }
        }

        svf.VrsOutn("TOTAL51" + suf2, 1, "0".equals(sum(scoreList)) ? "" : sum(scoreList)); // 合計
        svf.VrsOutn("TOTAL1"  + suf2, 2, 0 == scoreList.size() ? "" : String.valueOf(scoreList.size())); // 人数
        svf.VrsOutn("TOTAL51" + suf2, 3, avg(scoreList)); // 平均
        svf.VrsOutn("TOTAL1"  + suf2, 4, max(scoreList)); // 最高
        svf.VrsOutn("TOTAL1"  + suf2, 5, min(scoreList)); // 最低
        svf.VrsOutn("TOTAL1"  + suf2, 6, kettenCount(scoreList, testItem, hyoteiBunpuMap)); // 欠点者
    }

	private String kettenCount(final List scoreList, final TestItem testItem, final Map hyoteiBunpuMap) {
        if (scoreList.size() == 0) {
            return null;
        }
        if (testItem._printKettenFlg == -1) {
            return null;
        }
        int high = 0, low = 0;
        if (testItem._printKettenFlg == 1) {
            high = 1;
            low = 1;
        } else if (testItem._printKettenFlg == 2) {
            final Map map = (Map) hyoteiBunpuMap.get("1");
            if (null == map) {
            	log.info("評定マスタ無し");
            } else {
            	high = (int) (map.get("ASSESSHIGH") == null ? 0 : Double.parseDouble((String) map.get("ASSESSHIGH")));
            	low = (int) (map.get("ASSESSLOW") == null ? 0 : Double.parseDouble((String) map.get("ASSESSLOW")));
            }
        }
        final List kettenList = new ArrayList();
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            if (low <= Integer.parseInt(score) && Integer.parseInt(score) <= high) {
                kettenList.add(score);
            }
        }
        return String.valueOf(kettenList.size());
    }

    private String percent(final int count, final int size) {
        if (count == 0 || size == 0) {
            return "0";
        }
        return new BigDecimal(count).multiply(new BigDecimal("100")).divide(new BigDecimal(size), 0, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String min(List scoreList) {
        int min = Integer.MAX_VALUE;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            min = Math.min(min, Integer.parseInt(score));
        }
        return min == Integer.MAX_VALUE ? null : String.valueOf(min);
    }

    private String max(List scoreList) {
        int max = Integer.MIN_VALUE;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            max = Math.max(max, Integer.parseInt(score));
        }
        return max == Integer.MIN_VALUE ? null : String.valueOf(max);
    }

    private String avg(List scoreList) {
        return scoreList.isEmpty() ? null : new BigDecimal(sum(scoreList)).divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private String sum(List scoreList) {
        int sum = 0;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            sum += Integer.parseInt(score);
        }
        return String.valueOf(sum);
    }

    private int count(String hyotei, List hyoteiList) {
        int count = 0;
        for (final Iterator it = hyoteiList.iterator(); it.hasNext();) {
            final String e = (String) it.next();
            if (hyotei.equals(e)) {
                count ++;
            }
        }
        return count;
    }

    private boolean isMaxColumn(final int i) {
        return i % MAX_COLUMN1 == MAX_COLUMN1 - 1;
    }

    // 欠課時数
    private void printColumn2(final Vrw32alp svf, final List studentlist, final Subclass subclass, final String text, int i, final SemesterDetail semesterDetail, final int colsize, final int column, final String[] tuisidoushaSubtitleArray, final int subtitle8Idx, final String n) {
        final int semesterDetailIdx = semesterDetail._semester.getSemesterDetailIdx(semesterDetail);
		final boolean start = semesterDetailIdx == 0;
        final boolean last = isMaxColumn(column) || i == colsize - 1;
        final String suf1 = (start ? "" : last ? "_3" : "_2") + n;
        final String suf2 = (start ? "_1" : last ? "_3" : "_2") + n;
        final String title = center(text, colsize);
        svf.VrsOut("SUBTITLE2" + suf1, title.substring(i * _charsPerColumn, i * _charsPerColumn + 2)); // 欠課
        final int keta;
        final String fieldSem;
        final int maxColumnInSemester = semesterDetail._semester._semesterDetailList.size();
		final String semestername = StringUtils.defaultString(semesterDetail._semester._semestername);
		//log.info(" semester " + semesterDetail._semester + " = " + maxColumnInSemester + "(len = " + getMS932ByteLength(semestername) + ")");
		if (getMS932ByteLength(semestername) <= maxColumnInSemester * 4) {
            keta = 4;
            fieldSem = "SEM2" + (start ? "_1" : last ? "_5" : "_3") + n;
        } else {
            keta = 6;
            fieldSem = "SEM2" + (start ? "_2" : last ? "_6" : "_4") + n;
        }
        final String[] splittedName = split(ketaCenter(semestername, maxColumnInSemester * keta), keta);
        if (semesterDetailIdx < splittedName.length) {
            svf.VrsOut(fieldSem, splittedName[semesterDetailIdx]); // 学期
        }
        svf.VrsOut("ITEM2" + suf1, take(semesterDetail._semestername, 5)); // 素点・評価他
        // log.debug(" ITEM2" + (suf1.length() == 0 ? "  " : suf1) + " = " + semesterDetail._semester._semestername + ":" + semesterDetail._semestername);

        final boolean absentIsJissu = "3".equals(_param._knjSchoolMst._absentCov) || "4".equals(_param._knjSchoolMst._absentCov);
        final String absentFieldNum = absentIsJissu ? "2" : "1";
        for (int k = 0; k < studentlist.size(); k++) {
            final Student student = (Student) studentlist.get(k);
            svf.VrsOutn("ABSENT" + absentFieldNum + suf2, student._gyo, student.getSick2(subclass, semesterDetail._cdSemesterDetail, absentIsJissu, _param)); // 欠課
        }
        if (subtitle8Idx < tuisidoushaSubtitleArray.length) {
            svf.VrsOut("SUBTITLET8" + suf1, tuisidoushaSubtitleArray[subtitle8Idx]);
        }
    }

    // 出席すべき時数
    private void printColumn3(final Vrw32alp svf, final List studentlist, final Subclass subclass, final String title, final String div, final String groupDiv, final int column, final String[] tuisidoushaSubtitleArray, final int subtitle8Idx, final String n) {
        svf.VrsOut("SUBTITLE3" + n, title);
        svf.VrsOut("GRPCD3_1" + n, groupDiv); // グループ1

        if (_param._isOutputDebug) {
        	log.info(" column3 groupDiv = " + groupDiv);
        }

        for (int j = 0; j < studentlist.size(); j++) {
            final Student student = (Student) studentlist.get(j);
            String val = null;
            if ("MUST".equals(div)) {
                val = student.getMust(subclass, ALL_SEME); //  出席すべき時数
            } else if ("JUGYO_JISU".equals(div)) {
                val = student.getJugyoJisu(subclass, ALL_SEME); //  授業時数
            }
            svf.VrsOutn("MUST" + n, student._gyo, val);
        }
        if (subtitle8Idx < tuisidoushaSubtitleArray.length) {
            svf.VrsOut("SUBTITLE8_4" + n, tuisidoushaSubtitleArray[subtitle8Idx]);
        }
    }

    // 追指導
    private void printColumn4(final Vrw32alp svf, final List studentlist, final List studentAllList, final Subclass subclass, final String text, int i, final TestItem testItem, final List sameSemesterItems, final int colsize, final int column) {
        final int scolsize = sameSemesterItems.size();
        final int si = sameSemesterItems.indexOf(testItem);
        final boolean start = i == 0;
        final boolean last = isMaxColumn(column) || i == colsize - 1;
        final boolean starts = si == 0;
        final boolean lasts = si == scolsize - 1;

        String[] sufs;
        if (last) {
            sufs = new String[] {"_5", "_3", "_3", "3", "_5", "_6"};
        } else if (lasts) {
        	sufs = new String[] {"_9", "_5", "_5", "5" , "_9", "_10"};
        } else if (start) {
            sufs = new String[] {"_4", "_4", "_4", "4" , "_7", "_8"};
        } else if (starts) {
            sufs = new String[] {"_1", ""  , "_1", ""  , "_1", "_2"};
        } else {
            sufs = new String[] {"_3", "_2", "_2", "2" , "_3", "_4"};
        }
        if (_param._isOutputDebug) {
        	log.info(" column4 " + ArrayUtils.toString(sufs));
        }

        final String fieldGrpCd1 = "GRPCD4" + sufs[0];

        final String fieldSem1 = "SEM4" + sufs[4];
        final String fieldSem2 = "SEM4" + sufs[5];
        final String title = center(text, colsize);
        svf.VrsOut("SUBTITLE4" + sufs[1], title.substring(i * _charsPerColumn, i * _charsPerColumn + 2)); // 追指導
        final int keta;
        final String field;
		final String semestername = StringUtils.defaultString(testItem._semester._semestername);
		if (getMS932ByteLength(semestername) <= scolsize * 2) {
            keta = 4;
            field = fieldSem1;
        } else {
            keta = 6;
            field = fieldSem2;
        }
        final String[] splittedName = split(ketaCenter(semestername, scolsize * keta), keta);
        if (si < splittedName.length) {
            svf.VrsOut(field, splittedName[si]); // 学期
        }
        svf.VrsOut("ITEM4" + sufs[1], take(testItem._testitemabbv1, 5)); // 素点・評価他
        svf.VrsOut(fieldGrpCd1, "4"); // グループ1
        for (int j = 0; j < studentlist.size(); j++) {
            final Student student = (Student) studentlist.get(j);
            final String tuishido = student.getTuishido(subclass, testItem);
            if (null != tuishido) {
                svf.VrsOutn("LEAD1" + sufs[2], student._gyo, tuishido); // 追指導
            }
        }
        int cnt = 0;
        for (int j = 0; j < studentAllList.size(); j++) {
            final Student student = (Student) studentAllList.get(j);
            final String tuishido = student.getTuishido(subclass, testItem);
            if (null != tuishido) {
                cnt += 1;
            }
        }
        if (cnt > 0) {
            svf.VrsOut("TOTAL_LEAD" + sufs[3], String.valueOf(cnt)); // 追指導
        }
    }

//    // 仮評定
//    private void printColumn5(final Vrw32alp svf, final List studentlist, final Subclass subclass, final String text, int i, final TestItem testItem, final int colsize) {
//        final String title = center(text, colsize);
//        svf.VrsOut("SUBTITLE5", title.substring(i * _charsPerColumn, i * _charsPerColumn + 2)); // 仮評定
//        svf.VrsOut("ITEM5", take(testItem._testitemabbv1, 5); // 素点・評価他
//        svf.VrsOut("GRPCD5_1", "5"); // グループ1
//        for (int j = 0; j < studentlist.size(); j++) {
//            final Student student = (Student) studentlist.get(j);
//            svf.VrsOutn("TVALUE1_1", student._gyo, student.getScore(subclass, testItem.getTestcd())); // 仮評定
//        }
//    }

    // 取得単位数
    private void printColumn3Tanni(final Vrw32alp svf, final List studentlist, final Subclass subclass, final int column, final String n, final int flg) {
        final String subtitle = flg == 0 ? "　履修単位数　" : flg == 1 ? "　修得単位数　" : "";
        svf.VrsOut("SUBTITLE3" + n, subtitle); // 単位数
        svf.VrsOut("GRPCD3_1" + n, "3" + String.valueOf(flg)); // グループ1

        for (int j = 0; j < studentlist.size(); j++) {
            final Student student = (Student) studentlist.get(j);
            final SubclassScore subclassScore = student.getSubclassScore(subclass, HYOTEI_TESTCD);
            if (null != subclassScore) {
                if (flg == 0) {
                    svf.VrsOutn("MUST" + n, student._gyo, subclassScore._compCredit); // 判定
                } else if (flg == 1) {
                    svf.VrsOutn("MUST" + n, student._gyo, subclassScore._getCredit); // 判定
                }
            }
        }
    }

    // 判定
    private void printColumn6(final Vrw32alp svf, final List studentlist, final Subclass subclass, final int column) {
        svf.VrsOut("SUBTITLE6", "判定"); // 判定
        svf.VrsOut("ITEM6", "履修・修得"); // 素点・評価他
        for (int j = 0; j < studentlist.size(); j++) {
            final Student student = (Student) studentlist.get(j);
            final SubclassScore subclassScore = student.getSubclassScore(subclass, HYOTEI_TESTCD);
            if (null != subclassScore) {
                final boolean hasGetCredit = null != subclassScore._getCredit && Integer.parseInt(subclassScore._getCredit) >= 1;
                final boolean hasCompCredit = null != subclassScore._compCredit && Integer.parseInt(subclassScore._compCredit) >= 1;
                if (hasGetCredit) {
                    svf.VrsOutn("JUDGE", student._gyo, "修"); // 判定
                } else if (!hasGetCredit && hasCompCredit) {
                    svf.VrsOutn("JUDGE", student._gyo, "履"); // 判定
                }
            }
        }
    }

    // 備考
    private void printColumn7(final int flg, final Vrw32alp svf, final List studentlist, final Subclass subclass, final String text, final int i, final int colsize, final boolean printRemark, final int bikoStartColumn) {
        final boolean first = i == 0;
        final boolean last = i == colsize - 1;
        final String suf1 = last ? "_2" : flg == 2 ? "" : first ? "_3" : "";
        final String suf2 = last ? "2" : flg == 2 ? "1" : first ? "3" : "1";
        final String fieldGrpCd1 = "GRPCD7" + (last ? "_3" : flg == 2 ? "_1" : first ? "_5" : "_1");
        final String fieldGrpCd2 = "GRPCD7" + (last ? "_4" : flg == 2 ? "_2" : first ? "_6" : "_2");
        svf.VrsOut(fieldGrpCd1, "7"); // グループ1
        svf.VrsOut(fieldGrpCd2, "7"); // グループ1
        final String title = StringUtils.center(text, colsize, "　");
        svf.VrsOut("SUBTITLE7" + suf1, String.valueOf(title.charAt(i))); // 判定
        // log.debug(" filed = " + "SUBTITLE7" + suf1);
        if (printRemark) {
        	final int columnWidth = 88;
        	final int subformx = _param._notPrintPage2 ? 826 + columnWidth * bikoStartColumn : 826;
        	final int ketaWidth = columnWidth / 4;
            for (int j = 0; j < studentlist.size(); j++) {
                final Student student = (Student) studentlist.get(j);
                final String[] remarkArray = (String[]) student._remarkArrayMap.get(subclass._subclasscd);
                if (null != remarkArray && remarkArray.length > i) {
                    svf.VrsOutn("REMARK" + suf2, (j + 1), remarkArray[i]);
                    final Integer keta = (Integer) getMappedMap(student._remarkAttributeXMap, subclass._subclasscd).get(new Integer(i));
                    String attribute = "Hensyu=2"; // 左寄せ
                    if (null != keta) {
                    	attribute += ",X=" + String.valueOf(subformx + ketaWidth * keta.intValue());
                    }
                    svf.VrAttributen("REMARK" + suf2, (j + 1), attribute);
                }
            }
        }
    }

//    private void printBlankColumn(final Vrw32alp svf) {
//        svf.VrsOut("GRPCD8_1", "8"); // グループ1
//        svf.VrsOut("GRPCD8_2", "8"); // グループ1
//    }

    private void printLine(final Vrw32alp svf) {
        svf.VrsOut("GRPCD9", "9"); // グループ1
    }

    private static class Student {
        private static DecimalFormat df1 = new DecimalFormat("0");
        private static DecimalFormat df2 = new DecimalFormat("00");
        final String _schregno;
        final String _name;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _hrStfStaffname;
        final String _courseMajor;
        final String _majorname;
        final String _attendno;
        int _gyo;
        final Map _subclassMap;
        final Map _creditMstCreditsMap;
        final Map _remarkArrayMap;
        final Map _remarkAttributeXMap; // 備考のX調整

        Student(String schregno, String name, String grade, String hrClass, String hrName, String hrNameAbbv, String hrStfStaffname,
                String attendno, String courseMajor, String majorname) {
            _schregno = schregno;
            _name = name;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _hrStfStaffname = hrStfStaffname;
            _attendno = attendno;
            _courseMajor = courseMajor;
            _majorname = majorname;
            _subclassMap = new HashMap();
            _creditMstCreditsMap = new HashMap();
            _remarkArrayMap = new HashMap();
            _remarkAttributeXMap = new HashMap();
        }

        public String getScoreYomikae(final Param param, final Subclass subclass, final String score) {
            String retStr = score;
            if (param._d065Map.containsKey(subclass.getKeySubclassCd())) {
                if (param._d001Map.containsKey(score)) {
                    final NameMst nameMst = (NameMst) param._d001Map.get(score);
                    retStr = nameMst._name1;
                }
            }
            return retStr;
        }

        public String getGyoNoStr() {
            return "　" + (_gyo < 10 ? " " : "") + String.valueOf(_gyo);
        }

        public String getAttendNoStr() {
            final String gr = !NumberUtils.isDigits(_grade) ? " " : df1.format(Integer.parseInt(_grade));
            final String hr = !NumberUtils.isDigits(_hrClass) ? " " : df1.format(Integer.parseInt(_hrClass));
            final String at = !NumberUtils.isDigits(_attendno) ? "  " : df2.format(Integer.parseInt(_attendno));
            final String grhr = !StringUtils.isBlank(_hrNameAbbv) ? _hrNameAbbv : gr + "-" + hr;
            return grhr + "-" + at;
        }

        public String getRemark(final Subclass subclass, final Param param) {
            final SubclassScore subclassScore = getSubclassScore(subclass, param._remarkTestcd);
            if (null == subclassScore) {
                return null;
            }
            return subclassScore._slumpRemark;
        }

        public String getRecinfoRemark(final Subclass subclass, final Param param) {
            final SubclassScore subclassScore = getSubclassScore(subclass, param._remarkFlgTestcd);
            if (null == subclassScore) {
                return null;
            }
            return subclassScore._recinfoRemark;
        }

        private StudentSubclass createStudentSubclass(final Subclass subclass) {
            if (null == getStudentSubclass(subclass, _subclassMap)) {
                _subclassMap.put(subclass, new StudentSubclass(subclass));
            }
            return getStudentSubclass(subclass, _subclassMap);
        }

        public SubclassScore getSubclassScore(final Subclass subclass, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclass);
            return (SubclassScore) studentSubclass._scoreMap.get(testcd);
        }

        public String getScore(final Subclass subclass, final TestItem testItem) {
            final String score = getScore(subclass, testItem.getTestcd());
            String rtn = null;
            if (HYOTEI_TESTCD.equals(testItem.getTestcd())) {
                final SubclassScore subclassScore = getSubclassScore(subclass, testItem.getTestcd());
                if (null != subclassScore) {
                    if (testItem._isGakunenKariHyotei && "1".equals(subclassScore._provFlg)
                    || !testItem._isGakunenKariHyotei && !"1".equals(subclassScore._provFlg)) {
                        rtn = score;
                    } else {
                        rtn = null;
                    }
                }
            } else {
                rtn = score;
            }
            return rtn;
        }

        public String getScore(final Subclass subclass, final String testcd) {
            final SubclassScore subclassScore = getSubclassScore(subclass, testcd);
//            log.debug(" " + _schregno + ", " + testcd + ", " + subclassScore + ", " + _subclassMap);
            if (null == subclassScore) {
                return null;
            }
            if (null != subclassScore._valueDi) {
                return subclassScore._valueDi;
            }
            return subclassScore._score;
        }

        public String getJugyoJisu(final Subclass subclass, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclass);
            return (String) studentSubclass._jugyoJisuMap.get(testcd);
        }

        public String getMust(final Subclass subclass, final String testcd) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclass);
            return (String) studentSubclass._mustMap.get(testcd);
        }

        public String getSick2(final Subclass subclass, final String testcd, final boolean isJissu, final Param param) {
            final StudentSubclass studentSubclass = createStudentSubclass(subclass);
            final String sick2 = (String) studentSubclass._sick2Map.get(testcd);
            return null == sick2 ? null : 0.0 == Double.parseDouble(sick2) && !"1".equals(param._printKekka0)  ? "" : new BigDecimal(sick2).setScale(isJissu ? 1 : 0, BigDecimal.ROUND_HALF_UP).toString();
        }

        public String getTuishido(final Subclass subclass, final TestItem testItem) {
            final SubclassScore subclassScore = getSubclassScore(subclass, testItem.getTestcd());
            if (null != subclassScore) {
                if ("1".equals(testItem._sidouInput)) {
                    if (SIDOU_INPUT_INF_SCORE.equals(testItem._sidouInputInf)) { // 記号
                        return subclassScore._slumpScore;
                    } else if (SIDOU_INPUT_INF_MARK.equals(testItem._sidouInputInf)) { // 得点
                        return subclassScore._slumpMark;
                    } else {
                        log.debug(" unknown sidouInputInf : " + testItem._sidouInputInf);
                    }
                }
            }
            return null;
        }

        public String getMikomi(final Subclass subclass, final TestItem testItem) {
            final SubclassScore subclassScore = getSubclassScore(subclass, testItem.getTestcd());
            if (null == subclassScore) {
                return null;
            }
            if (null != subclassScore._valueDi && null != subclassScore._suppScore) {
                return subclassScore._suppScore;
            }
            return null;
        }

        public String toString() {
            return "Student(" + _schregno + ":" + _name + ")";
        }
    }

    private static class Subclass {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;

        Subclass(
            final String classcd,
            final String schoolKind,
            final String curriculumCd,
            final String subclasscd,
            final String subclassname,
            final boolean isSaki,
            final boolean isMoto
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
        }

        public String getKeySubclassCd() {
            return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
        }
    }

    private static class Chair {
        final String _chaircd;
        final String _chairname;
        final List _chairStfNameList;
        final List _hrClassList;
        final List _chairStfNameFukuList;
        public Chair(String chaircd, String chairname) {
            _chaircd = chaircd;
            _chairname = chairname;
            _chairStfNameList = new ArrayList();
            _hrClassList = new ArrayList();
            _chairStfNameFukuList = new ArrayList();
        }
    }

    private static class Hrclass {
        final String _gradehrclass;
        final String _hrName;
        final String _hrStfStfName;
        final List _studentList;
        public Hrclass(final String gradehrclass, final String hrName, final String hrStfStfName) {
            _gradehrclass = gradehrclass;
            _hrName = hrName;
            _hrStfStfName = hrStfStfName;
            _studentList = new ArrayList();
        }
    }

    private static class StudentSubclass {
        final Subclass _subclass;
        final Map _scoreMap;
        final Map _jugyoJisuMap;
        final Map _mustMap;
        final Map _sick2Map;

        StudentSubclass(
            final Subclass subclass
        ) {
            _subclass = subclass;
            _scoreMap = new HashMap();
            _jugyoJisuMap = new HashMap();
            _mustMap = new HashMap();
            _sick2Map = new HashMap();
        }
    }

    private static class SubclassScore {
        final String _year;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _score;
        final String _valueDi;
        final String _getCredit;
        final String _compCredit;
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;
        final String _slumpRemark;
        final String _recinfoRemark;
        final String _suppScore;
        final String _provFlg;

        SubclassScore(
            final String year,
            final String semester,
            final String testkindcd,
            final String testitemcd,
            final String scoreDiv,
            final String score,
            final String valueDi,
            final String getCredit,
            final String compCredit,
            final String slump,
            final String slumpMark,
            final String slumpScore,
            final String slumpRemark,
            final String recinfoRemark,
            final String suppScore,
            final String provFlg
        ) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _score = score;
            _valueDi = valueDi;
            _getCredit = getCredit;
            _compCredit = compCredit;
            _slump = slump;
            _slumpMark = slumpMark;
            _slumpScore = slumpScore;
            _slumpRemark = slumpRemark;
            _recinfoRemark = recinfoRemark;
            _suppScore = suppScore;
            _provFlg = provFlg;
        }

        public String getTestcd() {
            return _semester + _testkindcd + _testitemcd + _scoreDiv;
        }
        public String toString() {
            return "SubclassScore(" + getTestcd() + ": "+ _score + ")";
        }
    }

    private Chair getChair(final String chaircd, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Chair chair = (Chair) it.next();
            if (chair._chaircd.equals(chaircd)) {
                return chair;
            }
        }
        return null;
    }

    public Hrclass getHrclass(final String gradehrclass, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Hrclass hrclass = (Hrclass) it.next();
            if (hrclass._gradehrclass.equals(gradehrclass)) {
                return hrclass;
            }
        }
        return null;
    }

    private Student getStudent(final String schregno, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    private static StudentSubclass getStudentSubclass(final Subclass subclass, Map subclassMap) {
        return (StudentSubclass) subclassMap.get(subclass.getKeySubclassCd());
    }

    private boolean hasData(final DB2UDB db2, final String sql) throws SQLException {
        boolean hasData = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(sql);
        rs = ps.executeQuery();
        while (rs.next()) {
            hasData = true;
            break;
        }
        DbUtils.closeQuietly(null, ps, rs);
        return hasData;
    }

    private String getCtrlSemester(final DB2UDB db2) throws SQLException {
        String ctrlSemester = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ps = db2.prepareStatement(" SELECT CTRL_SEMESTER FROM CONTROL_MST WHERE CTRL_NO = '01' ");
        rs = ps.executeQuery();
        while (rs.next()) {
            ctrlSemester = rs.getString("CTRL_SEMESTER");
            break;
        }
        DbUtils.closeQuietly(null, ps, rs);
        return ctrlSemester;
    }

    public List getChairList(final DB2UDB db2, final Param param) {
        final Subclass subclass = param._subclass;
        final List chairlist = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String paramSemester = null;
        String paramDate = null;
        try {
            paramSemester = param._semester;
            paramDate = param._ctrlDate;
            if (!hasData(db2, getRecordSql(paramSemester, paramDate))) {
                log.warn("講座名簿がない: semester = " + paramSemester + ", date = " + paramDate);
                // DBのCTRL_SEMESTERとCTRL_SEMESTER学期の終了日に設定
                paramSemester = getCtrlSemester(db2);
                if (null != paramSemester) {
                    for (final Iterator it = _param._semesterList.iterator(); it.hasNext();) {
                        final Semester s = (Semester) it.next();
                        if (paramSemester.equals(s._cdSemester)) {
                            paramDate = s._edate;
                            break;
                        }
                    }
                    log.warn("講座名簿取得学期/日付: semester = " + paramSemester + ", date = " + paramDate);
                }
            }

            final String sql = getRecordSql(paramSemester, paramDate);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String chaircd = rs.getString("CHAIRCD");
                if (null == getChair(chaircd, chairlist)) {
                    final String chairname = rs.getString("CHAIRNAME");
                    chairlist.add(new Chair(chaircd, chairname));
                }
                final Chair chair = getChair(chaircd, chairlist);

                final String key = "1".equals(param._printDiv) ? rs.getString("GRADE") + rs.getString("HR_CLASS") : "ALL";
                if (null == getHrclass(key, chair._hrClassList)) {
                    final String hrName = rs.getString("HR_NAME");
                    final String hrStfStaffname = rs.getString("HR_STF_STAFFNAME");
                    chair._hrClassList.add(new Hrclass(key, hrName, hrStfStaffname));
                }
                final Hrclass hrclass = getHrclass(key, chair._hrClassList);

                final String schregno = rs.getString("SCHREGNO");
                if (null == getStudent(schregno, hrclass._studentList)) {
                    final String name = rs.getString("NAME");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                    final String hrStfStaffname = rs.getString("HR_STF_STAFFNAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String courseMajor = rs.getString("COURSE_MAJOR");
                    final String majorname = rs.getString("MAJORNAME");
                    final Student student = new Student(schregno, name, grade, hrClass, hrName, hrNameAbbv, hrStfStaffname, attendno, courseMajor, majorname);
                    hrclass._studentList.add(student);
                }

                final Student student = getStudent(schregno, hrclass._studentList);

                StudentSubclass studentSubclass = getStudentSubclass(subclass, student._subclassMap);
                if (null == studentSubclass) {
                    studentSubclass = new StudentSubclass(subclass);
                    student._subclassMap.put(subclass.getKeySubclassCd(), studentSubclass);
                    final String credits = rs.getString("CREDITS");
                    student._creditMstCreditsMap.put(subclass.getKeySubclassCd(), credits);
                }

                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String testkindcd = rs.getString("TESTKINDCD");
                final String testitemcd = rs.getString("TESTITEMCD");
                final String scoreDiv = rs.getString("SCORE_DIV");
                final String score = rs.getString("SCORE");
                final String valueDi = rs.getString("VALUE_DI");
                final String getCredit = rs.getString("GET_CREDIT");
                final String compCredit = rs.getString("COMP_CREDIT");
                final String slump = rs.getString("SLUMP");
                final String slumpMark = rs.getString("SLUMP_MARK");
                final String slumpScore = rs.getString("SLUMP_SCORE");
                final String slumpRemark = rs.getString("SLUMP_REMARK");
                final String recinfoRemark = rs.getString("REC_INFO_REMARK");
                final String suppScore = rs.getString("SUPP_SCORE");
                final String provFlg = rs.getString("PROV_FLG");
                final SubclassScore subclassScore = new SubclassScore(year, semester, testkindcd, testitemcd, scoreDiv, score, valueDi, getCredit, compCredit, slump, slumpMark, slumpScore, slumpRemark, recinfoRemark, suppScore, provFlg);
                studentSubclass._scoreMap.put(subclassScore.getTestcd(), subclassScore);
                _hasData = true;
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
//        for (final Iterator it = chairlist.iterator(); it.hasNext();) {
//            final Chair chair = (Chair) it.next();
//            for (final Iterator it2 = chair._hrClassList.iterator(); it2.hasNext();) {
//                final Hrclass hr = (Hrclass) it2.next();
//                for (final Iterator it3 = hr._studentList.iterator(); it3.hasNext();) {
//                    final Student st = (Student) it3.next();
//                    for (final Iterator it4 = st._subclassMap.values().iterator(); it4.hasNext();) {
//                        final StudentSubclass ssub = (StudentSubclass) it4.next();
//                        for (final Iterator it5 = ssub._scoreMap.values().iterator(); it5.hasNext();) {
//                            final SubclassScore sscore = (SubclassScore) it5.next();
//                            if (null != sscore._slumpRemark) {
//                                log.info(st._schregno + " " + sscore._year + sscore._semester + sscore._testkindcd + sscore._testitemcd + " remark = " + sscore._slumpRemark);
//                            }
//                        }
//                    }
//                }
//            }
//        }

        param.setChairStaff(db2, chairlist);
        param.setChairStaffFuku(db2, chairlist);

        if (null != _param._date) {
            try {
                final Set set = new HashSet();
                for (final Iterator testit = _param._attendSemesterDetailList.iterator(); testit.hasNext();) {
                    final SemesterDetail sd = (SemesterDetail) testit.next();
                    if (null == sd) {
                        continue;
                    }
                    if (set.contains(sd._cdSemesterDetail) || null == sd._sdate || null == sd._edate || sd._sdate.compareTo(_param._date) >= 0) {
                        continue;
                    }
                    final String sdate = _param._sdate.compareTo(sd._sdate) > 0 ? _param._sdate : sd._sdate;
                    final String edate = sd._edate.compareTo(_param._date) > 0 ? _param._date : sd._edate;
                    log.fatal(" semesdet = " + sd._cdSemesterDetail + ", sdate = " + sdate + ", edate = " + edate);
                    setAttendance(param, db2, subclass, chairlist, sd._semester._cdSemester, sd._cdSemesterDetail, sdate, edate);
                    set.add(sd._cdSemesterDetail);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                db2.commit();
            }
        }
        return chairlist;
    }

    private void setAttendance(final Param param, final DB2UDB db2, final Subclass subclass, final List chairlist, final String semester, final String mapKey, final String sdate, final String edate) throws Exception {
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;

        param._attendParamMap.put("schregno", "?");
        final String sql = AttendAccumulate.getAttendSubclassSql(
                param._year,
                semester,
                sdate,
                edate,
                param._attendParamMap
        );
        ps1 = db2.prepareStatement(sql);

        for (final Iterator it = chairlist.iterator(); it.hasNext();) {
            final Chair chair = (Chair) it.next();

            // log.debug(" attend subclass sql = " + sql);
            for (final Iterator hit = chair._hrClassList.iterator(); hit.hasNext();) {
                final Hrclass hrclass = (Hrclass) hit.next();

                for (final Iterator srit = hrclass._studentList.iterator(); srit.hasNext();) {

                    final Student student = (Student) srit.next();

                    if (null == getStudentSubclass(subclass, student._subclassMap)) {
                        student._subclassMap.put(subclass.getKeySubclassCd(), new StudentSubclass(subclass));
                    }
                    if (getStudentSubclass(subclass, student._subclassMap)._sick2Map.containsKey(mapKey)) {
                        continue;
                    }

                    ps1.setString(1, student._schregno);
                    rs1 = ps1.executeQuery();

                    while (rs1.next()) {
                        final String subclassCd = rs1.getString("SUBCLASSCD");
                        if (!"9".equals(rs1.getString("SEMESTER")) || !subclassCd.equals(subclass.getKeySubclassCd())) {
                            continue;
                        }

                        final String lesson = rs1.getString("LESSON");
                        final String must = rs1.getString("MLESSON");
                        final String sick = subclass._isSaki ? rs1.getString("REPLACED_SICK") : rs1.getString("SICK2");
                        StudentSubclass studentSubclass = getStudentSubclass(subclass, student._subclassMap);
                        studentSubclass._jugyoJisuMap.put(mapKey, lesson);
                        studentSubclass._mustMap.put(mapKey, must);
                        studentSubclass._sick2Map.put(mapKey, sick);
                    }
                    DbUtils.closeQuietly(rs1);
                    db2.commit();
                }
            }
        }
        DbUtils.closeQuietly(ps1);
        db2.commit();
    }

    public String getRecordSql(final String semester, final String date) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_STD AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     CHAIRCD ");
        stb.append(" FROM CHAIR_STD_DAT T1 ");
        stb.append(" WHERE YEAR = '" + _param._year + "' ");
        stb.append("  AND SEMESTER = '" + semester + "' ");
        stb.append("  AND CHAIRCD IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("  AND '" + date + "' BETWEEN APPDATE AND VALUE(APPENDDATE, '9999-12-31') ");
        stb.append(" ), TESTCDS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T12.YEAR, ");
        stb.append("     T12.SEMESTER, ");
        stb.append("     T12.TESTKINDCD, ");
        stb.append("     T12.TESTITEMCD, ");
        stb.append("     T12.SCORE_DIV, ");
        stb.append("     T12.CLASSCD, T12.SCHOOL_KIND,  T12.CURRICULUM_CD,  T12.SUBCLASSCD ");
        stb.append(" FROM CHAIR_STD T1 ");
        stb.append(" INNER JOIN CHAIR_DAT T11 ON T11.YEAR = '" + _param._year + "' ");
        stb.append("     AND T11.SEMESTER = '" + semester + "' ");
        stb.append("     AND T11.CHAIRCD = T1.CHAIRCD ");
        stb.append(" INNER JOIN RECORD_SCORE_DAT T12 ON T12.YEAR = T11.YEAR ");
        stb.append("     AND T12.CLASSCD = T11.CLASSCD ");
        stb.append("     AND T12.SCHOOL_KIND = T11.SCHOOL_KIND ");
        stb.append("     AND T12.CURRICULUM_CD = T11.CURRICULUM_CD ");
        stb.append("     AND T12.SUBCLASSCD = T11.SUBCLASSCD ");
        stb.append("     AND T12.SCHREGNO = T1.SCHREGNO ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T13.YEAR, ");
        stb.append("     T13.SEMESTER, ");
        stb.append("     T13.TESTKINDCD, ");
        stb.append("     T13.TESTITEMCD, ");
        stb.append("     T13.SCORE_DIV, ");
        stb.append("     T13.CLASSCD, T13.SCHOOL_KIND,  T13.CURRICULUM_CD,  T13.SUBCLASSCD ");
        stb.append(" FROM CHAIR_STD T1 ");
        stb.append(" INNER JOIN CHAIR_DAT T11 ON T11.YEAR = '" + _param._year + "' ");
        stb.append("     AND T11.SEMESTER = '" + semester + "' ");
        stb.append("     AND T11.CHAIRCD = T1.CHAIRCD ");
        stb.append(" INNER JOIN RECORD_SLUMP_SDIV_DAT T13 ON T13.YEAR = T11.YEAR ");
        stb.append("     AND T13.CLASSCD = T11.CLASSCD ");
        stb.append("     AND T13.SCHOOL_KIND = T11.SCHOOL_KIND ");
        stb.append("     AND T13.CURRICULUM_CD = T11.CURRICULUM_CD ");
        stb.append("     AND T13.SUBCLASSCD = T11.SUBCLASSCD ");
        stb.append("     AND T13.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T11.CHAIRNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     T3.HR_NAMEABBV, ");
        stb.append("     HR_STF.STAFFNAME AS HR_STF_STAFFNAME, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T21.NAME, ");
        stb.append("     T2.COURSECD || T2.MAJORCD AS COURSE_MAJOR, ");
        stb.append("     T4.MAJORNAME, ");
        stb.append("     T6.CREDITS, ");
        stb.append("     TTEST.YEAR, ");
        stb.append("     TTEST.SEMESTER, ");
        stb.append("     TTEST.TESTKINDCD, ");
        stb.append("     TTEST.TESTITEMCD, ");
        stb.append("     TTEST.SCORE_DIV, ");
        stb.append("     TTEST.CLASSCD || '-' || TTEST.SCHOOL_KIND || '-' || TTEST.CURRICULUM_CD || '-' || TTEST.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T12.SCORE, ");
        stb.append("     T12.VALUE_DI, ");
        stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.GET_CREDIT END AS GET_CREDIT, ");
        stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.ADD_CREDIT END AS ADD_CREDIT, ");
        stb.append("     CASE WHEN T15.PROV_FLG IS NOT NULL THEN CAST(NULL AS SMALLINT) ELSE T12.COMP_CREDIT END AS COMP_CREDIT, ");
        stb.append("     T13.SLUMP, ");
        stb.append("     T14.NAME1 AS SLUMP_MARK, ");
        stb.append("     T13.SCORE AS SLUMP_SCORE, ");
        stb.append("     T13.REMARK AS SLUMP_REMARK, ");
        if ("1".equals(_param._useRemarkFlg)) {
            stb.append("     T16.REMARK AS REC_INFO_REMARK, ");
        } else  {
            stb.append("     CAST(NULL AS VARCHAR(300)) AS REC_INFO_REMARK, ");
        }
        if ("1".equals(_param._useMikomiFlg)) {
            stb.append("     T17.SCORE AS SUPP_SCORE, ");
        } else  {
            stb.append("     CAST(NULL AS SMALLINT) AS SUPP_SCORE, ");
        }
        stb.append("     T15.PROV_FLG ");
        stb.append(" FROM CHAIR_STD T1 ");
        stb.append(" INNER JOIN CHAIR_DAT T11 ON T11.YEAR = '" + _param._year + "' ");
        stb.append("     AND T11.SEMESTER = '" + semester + "' ");
        stb.append("     AND T11.CHAIRCD = T1.CHAIRCD ");
        stb.append(" LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + _param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + semester + "' ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '" + _param._year + "' ");
        stb.append("     AND GDAT.GRADE = T2.GRADE ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T21 ON T21.SCHREGNO = T2.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T3.GRADE = T2.GRADE ");
        stb.append("     AND T3.HR_CLASS = T2.HR_CLASS ");
        stb.append(" LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T2.COURSECD ");
        stb.append("     AND T4.MAJORCD = T2.MAJORCD ");
        stb.append(" LEFT JOIN STAFF_MST HR_STF ON HR_STF.STAFFCD = T3.TR_CD1 ");
        stb.append(" LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T11.YEAR ");
        stb.append("     AND T6.COURSECD = T2.COURSECD ");
        stb.append("     AND T6.MAJORCD = T2.MAJORCD ");
        stb.append("     AND T6.GRADE = T2.GRADE ");
        stb.append("     AND T6.COURSECODE = T2.COURSECODE ");
        stb.append("     AND T6.CLASSCD = T11.CLASSCD ");
        stb.append("     AND T6.SCHOOL_KIND = T11.SCHOOL_KIND ");
        stb.append("     AND T6.CURRICULUM_CD = T11.CURRICULUM_CD ");
        stb.append("     AND T6.SUBCLASSCD = T11.SUBCLASSCD ");
        stb.append(" LEFT JOIN TESTCDS TTEST ON TTEST.YEAR = '" + _param._year + "' ");
        stb.append("     AND TTEST.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND TTEST.CLASSCD = T11.CLASSCD ");
        stb.append("     AND TTEST.SCHOOL_KIND = T11.SCHOOL_KIND ");
        stb.append("     AND TTEST.CURRICULUM_CD = T11.CURRICULUM_CD ");
        stb.append("     AND TTEST.SUBCLASSCD = T11.SUBCLASSCD ");
        stb.append("     AND TTEST.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN RECORD_SCORE_DAT T12 ON T12.YEAR = TTEST.YEAR ");
        stb.append("     AND T12.SEMESTER = TTEST.SEMESTER ");
        stb.append("     AND T12.TESTKINDCD = TTEST.TESTKINDCD ");
        stb.append("     AND T12.TESTITEMCD = TTEST.TESTITEMCD ");
        stb.append("     AND T12.SCORE_DIV = TTEST.SCORE_DIV ");
        stb.append("     AND T12.CLASSCD = TTEST.CLASSCD ");
        stb.append("     AND T12.SCHOOL_KIND = TTEST.SCHOOL_KIND ");
        stb.append("     AND T12.CURRICULUM_CD = TTEST.CURRICULUM_CD ");
        stb.append("     AND T12.SUBCLASSCD = TTEST.SUBCLASSCD ");
        stb.append("     AND T12.SCHREGNO = TTEST.SCHREGNO ");
        stb.append(" LEFT JOIN RECORD_SLUMP_SDIV_DAT T13 ON T13.YEAR = TTEST.YEAR ");
        stb.append("     AND T13.SEMESTER = TTEST.SEMESTER ");
        stb.append("     AND T13.TESTKINDCD = TTEST.TESTKINDCD ");
        stb.append("     AND T13.TESTITEMCD = TTEST.TESTITEMCD ");
        stb.append("     AND T13.SCORE_DIV = TTEST.SCORE_DIV ");
        stb.append("     AND T13.CLASSCD = TTEST.CLASSCD ");
        stb.append("     AND T13.SCHOOL_KIND = TTEST.SCHOOL_KIND ");
        stb.append("     AND T13.CURRICULUM_CD = TTEST.CURRICULUM_CD ");
        stb.append("     AND T13.SUBCLASSCD = TTEST.SUBCLASSCD ");
        stb.append("     AND T13.SCHREGNO = TTEST.SCHREGNO ");
        stb.append("  LEFT JOIN NAME_MST T14 ON T14.NAMECD1 = 'D054' ");
        stb.append("     AND T14.NAMECD2 = T13.MARK ");
        stb.append(" LEFT JOIN RECORD_PROV_FLG_DAT T15 ON T15.YEAR = T12.YEAR ");
        stb.append("     AND T15.CLASSCD = T12.CLASSCD ");
        stb.append("     AND T15.SCHOOL_KIND = T12.SCHOOL_KIND ");
        stb.append("     AND T15.CURRICULUM_CD = T12.CURRICULUM_CD ");
        stb.append("     AND T15.SUBCLASSCD = T12.SUBCLASSCD ");
        stb.append("     AND T15.SCHREGNO = T12.SCHREGNO ");
        if ("1".equals(_param._useRemarkFlg)) {
            stb.append(" LEFT JOIN RECORD_INFO_SDIV_DAT T16 ON T16.YEAR = T12.YEAR ");
            stb.append("     AND T16.SEMESTER = T12.SEMESTER ");
            stb.append("     AND T16.TESTKINDCD = T12.TESTKINDCD ");
            stb.append("     AND T16.TESTITEMCD = T12.TESTITEMCD ");
            stb.append("     AND T16.SCORE_DIV = T12.SCORE_DIV ");
            stb.append("     AND T16.CLASSCD = T12.CLASSCD ");
            stb.append("     AND T16.SCHOOL_KIND = T12.SCHOOL_KIND ");
            stb.append("     AND T16.CURRICULUM_CD = T12.CURRICULUM_CD ");
            stb.append("     AND T16.SUBCLASSCD = T12.SUBCLASSCD ");
            stb.append("     AND T16.SCHREGNO = T12.SCHREGNO ");
            stb.append("     AND T16.SEQ = '002' "); //002:備考入力
        }
        if ("1".equals(_param._useMikomiFlg)) {
            stb.append(" LEFT JOIN SUPP_EXA_SDIV_DAT T17 ON T17.YEAR = T12.YEAR ");
            stb.append("     AND T17.SEMESTER = T12.SEMESTER ");
            stb.append("     AND T17.TESTKINDCD = T12.TESTKINDCD ");
            stb.append("     AND T17.TESTITEMCD = T12.TESTITEMCD ");
            stb.append("     AND T17.SCORE_DIV = T12.SCORE_DIV ");
            stb.append("     AND T17.CLASSCD = T12.CLASSCD ");
            stb.append("     AND T17.SCHOOL_KIND = T12.SCHOOL_KIND ");
            stb.append("     AND T17.CURRICULUM_CD = T12.CURRICULUM_CD ");
            stb.append("     AND T17.SUBCLASSCD = T12.SUBCLASSCD ");
            stb.append("     AND T17.SCHREGNO = T12.SCHREGNO ");
            stb.append("     AND T17.SCORE_FLG = '2' "); //2:見込点入力
        }
        if ("1".equals(_param._use_school_detail_gcm_dat)) {
            stb.append(" WHERE ");
            stb.append("     T2.COURSECD || '-' || T2.MAJORCD = '" + _param._COURSE_MAJOR + "' ");
            stb.append("     AND GDAT.SCHOOL_KIND = '" + _param._PRINT_SCHOOLKIND + "' ");
        }

        stb.append(" ORDER BY ");
        stb.append(" T1.CHAIRCD, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
        return stb.toString();
    }

    private static class Semester implements Comparable {
        final String _year;
        final String _cdSemester;
        final String _semestername;
        final String _sdate;
        final String _edate;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(String year, String semester, String semestername, final String sdate, final String edate) {
            _year = year;
            _cdSemester = semester;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//            log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
            return _semesterDetailList.indexOf(semesterDetail);
        }
        public int compareTo(final Object o) {
        	if (!(o instanceof Semester)) {
        		return 0;
        	}
        	Semester s = (Semester) o;
        	return _cdSemester.compareTo(s._cdSemester);
        }
        public String toString() {
        	return "Semester(" + _cdSemester + ":" + _semestername + ")";
        }
    }

    private static class SemesterDetail implements Comparable {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final Object o) {
        	if (!(o instanceof SemesterDetail)) {
        		return 0;
        	}
    		SemesterDetail sd = (SemesterDetail) o;
    		int rtn;
        	rtn = _semester.compareTo(sd._semester);
        	if (rtn != 0) {
        		return rtn;
        	}
        	rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
        	return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._cdSemester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._cdSemester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._cdSemester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    private static class NameMst {
        final String _namecd2;
        final String _name1;
        final String _name2;
        final String _abbv1;
        public NameMst(
                final String namecd2,
                final String name1,
                final String name2,
                final String abbv1
        ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
            _abbv1 = abbv1;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 75381 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _subclasscd;
        final String[] _categorySelected;
        String _sdate;
        final String _date;
        final String _printDiv;
        final String _printKekka0;
        final String _printTannin;
        final String _ctrlDate;
        final String _prgid;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        final String _printJugyoJisu;
        final String _use_SchregNo_hyoji;
        final String _documentroot;
        final boolean _isRakunan;
        final boolean _isFukuiken;
        final boolean _isMeikei;
        final boolean _useFormA3Yoko;
        final String _useFormNameKNJD620V;
        final boolean _useStudentLine55;
        final boolean _notPrintPage2;
        final boolean _notPrintHantei;
        final boolean _notPrintHyoteiBunpu;
        final String _COURSE_MAJOR;
        final String _PRINT_SCHOOLCD;
        final String _PRINT_SCHOOLKIND;
        final String _useSchool_KindField;
        final String _use_school_detail_gcm_dat;
        final String _useChairStaffOrder;

        final List _semesterList;
        final Map _semesterDetailMap;
        final List _testKindItemList;
        final List _attendTestKindItemList;
//        final List _kariHyouteiList;
        final List _attendSemesterDetailList;
        final Subclass _subclass;
        final String _remarkTestcd;
        final String _remarkFlgTestcd;
        final String _useRemarkFlg;
        final String _useMikomiFlg;
        final String _now;
        final Map _d065Map;
        final Map _d001Map;
        final String _imagepath;
        final String _whitespacePath;

        final boolean _isPrintAttendInfo;

        private KNJSchoolMst _knjSchoolMst;

        final Map _attendParamMap;

        final boolean _isOutputDebug;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _categorySelected = request.getParameterValues("category_selected");
            _subclasscd = null == request.getParameter("SUBCLASSCD") ? request.getParameter("H_SUBCLASSCD") : request.getParameter("SUBCLASSCD");
            _sdate = null == request.getParameter("SDATE") ? null : KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _date = null == request.getParameter("DATE") ? null : KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _printDiv = request.getParameter("PRINT_DIV");
            _printKekka0 = request.getParameter("PRINT_KEKKA0");
            _printTannin = request.getParameter("PRINT_TANNIN");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _remarkTestcd = request.getParameter("REMARK_TESTCD");
            _remarkFlgTestcd = request.getParameter("REMARK_FLG_TESTCD");
            _useRemarkFlg = request.getParameter("useRemarkFlg");
            _useMikomiFlg = request.getParameter("useMikomiFlg");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _printJugyoJisu = request.getParameter("PRINT_JUGYO_JISU");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _documentroot = request.getParameter("DOCUMENTROOT");

            _COURSE_MAJOR = request.getParameter("COURSE_MAJOR");
            _PRINT_SCHOOLCD = request.getParameter("PRINT_SCHOOLCD");
            _PRINT_SCHOOLKIND = request.getParameter("PRINT_SCHOOLKIND");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useChairStaffOrder = request.getParameter("useChairStaffOrder");

            _subclass = getSubclass(db2, _subclasscd, _year);
            _semesterList = getSemesterList(db2);
            _semesterDetailMap = new HashMap();
            if (null == _sdate) {
                for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                    final Semester s = (Semester) it.next();
                    if ("1".equals(s._cdSemester) || "9".equals(s._cdSemester)) {
                        _sdate = s._sdate;
                    }
                    if (null != _sdate) {
                        break;
                    }
                }
            }
            log.info(" _sdate = " + _sdate);
            _testKindItemList = getTestKindItemList(db2, true, true);
            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            for (int i = 0; i < _semesterList.size(); i++) {
            	final Semester seme = (Semester) _semesterList.get(i);
            	Collections.sort(seme._semesterDetailList);
            }
            _attendSemesterDetailList = getAttendSemesterDetailList();
 //           _kariHyouteiList = getTestKindItemKariHyouteiList(_testKindItemList);

            final String z010 = setZ010Name1(db2);
            log.info(" z010 name1 = " + z010);
            _isRakunan = "rakunan".equals(z010);
            _isFukuiken = "fukuiken".equals(z010);
            _isMeikei = "meikei".equals(z010);
            _useFormA3Yoko = _isMeikei;
            _useFormNameKNJD620V = request.getParameter("useFormNameKNJD620V");
            _notPrintPage2 = _isMeikei;
            _useStudentLine55 = _isRakunan;
            final String subclassSchoolKind = StringUtils.split(_subclasscd, "-").length == 4 ? StringUtils.split(_subclasscd, "-")[1] : null;
            final boolean subclassSchoolKindIsJ = "J".equals(subclassSchoolKind);
            _notPrintHantei = _isFukuiken && subclassSchoolKindIsJ;
            _notPrintHyoteiBunpu = _isFukuiken && subclassSchoolKindIsJ;

            _now = StringUtils.replace(getNow(db2), "null", "");
            _isPrintAttendInfo = !(PRGID_KNJD129D.equals(_prgid) || _isFukuiken && subclassSchoolKindIsJ);
            try {
                final Map smParamMap = new HashMap();
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                	smParamMap.put("SCHOOL_KIND", subclassSchoolKind);
                }
                _knjSchoolMst = new KNJSchoolMst(db2, _year, smParamMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            if ("1".equals(_use_school_detail_gcm_dat)) {
                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");
            } else {
                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            }
            _d001Map = getNameMst(db2, "D001", "NAMECD2");
            _d065Map = getNameMst(db2, "D065", "NAME1");
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _whitespacePath = getImagePath("whitespace.png");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }


        private String getImagePath(String filename) {
        	File file = new File(_documentroot + "/" + _imagepath + "/" + filename);
        	if (file.exists()) {
        		return file.getAbsolutePath();
        	} else {
        		log.warn(" file not found : " + file.getAbsolutePath());
        	}
			return null;
		}


		private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            try {
                return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD620V' AND NAME = '" + propName + "' "));
            } catch (Throwable t) {
                log.info("getDbPrginfoProperties error.");
            }
            return null;
        }

        private void setChairStaff(final DB2UDB db2, final List chairlist) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SEMESTER, ");
            if ("1".equals(_useChairStaffOrder)) {
                stb.append("     STF_DETAIL.REMARK_SINT1, ");
            }
            stb.append("     T1.STAFFCD, ");
            stb.append("     STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT T1 ");
            stb.append("     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
            if ("1".equals(_useChairStaffOrder)) {
                stb.append("     LEFT JOIN CHAIR_STF_DETAIL_DAT STF_DETAIL ON T1.YEAR = STF_DETAIL.YEAR ");
                stb.append("          AND T1.SEMESTER = STF_DETAIL.SEMESTER ");
                stb.append("          AND T1.CHAIRCD = STF_DETAIL.CHAIRCD ");
                stb.append("          AND T1.STAFFCD = STF_DETAIL.STAFFCD ");
                stb.append("          AND STF_DETAIL.SEQ = '001' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + _semester + "' ");
            stb.append("     AND T1.CHAIRCD = ? ");
            stb.append("     AND T1.CHARGEDIV = 1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEMESTER DESC, ");
            if ("1".equals(_useChairStaffOrder)) {
                stb.append("     STF_DETAIL.REMARK_SINT1, ");
            }
            stb.append("     T1.STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                for (final Iterator it = chairlist.iterator(); it.hasNext();) {
                    final Chair chair = (Chair) it.next();
                    chair._chairStfNameList.clear();
                    ps.setString(1, chair._chaircd);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (StringUtils.isEmpty(rs.getString("STAFFNAME")) || chair._chairStfNameList.contains(rs.getString("STAFFNAME"))) {
                            continue;
                        }
                        chair._chairStfNameList.add(rs.getString("STAFFNAME"));
                        if (chair._chairStfNameList.size() >= 4) { // 最大4件まで
                            break;
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setChairStaffFuku(final DB2UDB db2, final List chairlist) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SEMESTER, ");
            if ("1".equals(_useChairStaffOrder)) {
                stb.append("     STF_DETAIL.REMARK_SINT1, ");
            }
            stb.append("     T1.STAFFCD, ");
            stb.append("     STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT T1 ");
            stb.append("     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
            if ("1".equals(_useChairStaffOrder)) {
                stb.append("     LEFT JOIN CHAIR_STF_DETAIL_DAT STF_DETAIL ON T1.YEAR = STF_DETAIL.YEAR ");
                stb.append("          AND T1.SEMESTER = STF_DETAIL.SEMESTER ");
                stb.append("          AND T1.CHAIRCD = STF_DETAIL.CHAIRCD ");
                stb.append("          AND T1.STAFFCD = STF_DETAIL.STAFFCD ");
                stb.append("          AND STF_DETAIL.SEQ = '001' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + _semester + "' ");
            stb.append("     AND T1.CHAIRCD = ? ");
            stb.append("     AND T1.CHARGEDIV = 0 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEMESTER DESC, ");
            if ("1".equals(_useChairStaffOrder)) {
                stb.append("     STF_DETAIL.REMARK_SINT1, ");
            }
            stb.append("     T1.STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                for (final Iterator it = chairlist.iterator(); it.hasNext();) {
                    final Chair chair = (Chair) it.next();
                    chair._chairStfNameFukuList.clear();
                    ps.setString(1, chair._chaircd);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (StringUtils.isEmpty(rs.getString("STAFFNAME")) || chair._chairStfNameFukuList.contains(rs.getString("STAFFNAME"))) {
                            continue;
                        }
                        chair._chairStfNameFukuList.add(rs.getString("STAFFNAME"));
                        if (chair._chairStfNameFukuList.size() >= 4) { // 最大4件まで
                            break;
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getNow(final DB2UDB db2) {
        	final boolean isSeireki = KNJ_EditDate.isSeireki(db2);
    		final Calendar cal = Calendar.getInstance();
    		final DecimalFormat z2 = new DecimalFormat("00");
    		final String hour = z2.format(cal.get(Calendar.HOUR_OF_DAY));
    		final String minute = z2.format(cal.get(Calendar.MINUTE));
    		cal.setTime(Date.valueOf(_ctrlDate));
    		final String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
    		final String dayOfMonth = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    		final String nengo;
    		final String nen;
        	if (isSeireki) {
        		nengo = "";
        		nen = String.valueOf(cal.get(Calendar.YEAR));
        	} else {
            	final String[] tate = KNJ_EditDate.tate_format4(db2, _ctrlDate.replace('/', '-'));
            	nengo = tate[0];
            	nen = "元".equals(tate[1]) ? "元年" : tate[1];
        	}

        	return nengo + nen + "." + month + "." + dayOfMonth + ". " + hour + ":" + minute;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private Map getNameMst(final DB2UDB db2, final String namecd1, final String keyName) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + namecd1 + "' ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setKey = rs.getString(keyName);
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    final String name2 = rs.getString("NAME2");
                    final String abbv1 = rs.getString("ABBV1");
                    NameMst nameMst = new NameMst(namecd2, name1, name2, abbv1);
                    retMap.put(setKey, nameMst);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private List getAttendSemesterDetailList() {
            final Map rtn = new TreeMap();
            for (final Iterator it = _attendTestKindItemList.iterator(); it.hasNext();) {
                final TestItem item = (TestItem) it.next();
                if (null != item._semesterDetail && null != item._semesterDetail._cdSemesterDetail) {
                    rtn.put(item._semesterDetail._cdSemesterDetail, item._semesterDetail);
                }
            }
            Semester semester9 = null;
            for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                final Semester semester = (Semester) it.next();
                if (semester._cdSemester.equals(ALL_SEME)) {
                    semester9 = semester;
                    final SemesterDetail semesterDetail9 = new SemesterDetail(semester9, ALL_SEME, "学年", semester9._sdate, semester9._edate);
                    semester9._semesterDetailList.add(semesterDetail9);
                    rtn.put(ALL_SEME, semesterDetail9);
                    break;
                }
            }
            return new ArrayList(rtn.values());
        }

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(year, semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private Map getHyoteiBunpuMap(final DB2UDB db2, final List studentList) {
            final TreeSet gradeSet = new TreeSet();
            for (int i = 0; i < studentList.size(); i++) {
                final Student student = (Student) studentList.get(i);
                if (null != student._grade) {
                    gradeSet.add(student._grade);
                }
            }
            final String maxGrade = gradeSet.size() > 0 ? (String) gradeSet.last() : null;
            final Map assessMstMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int relatvieassessMstCount = 0;
                final String countSql = " SELECT COUNT(*) AS COUNT FROM RELATIVEASSESS_MST WHERE GRADE = '" + maxGrade + "' AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _subclasscd + "' AND ASSESSCD = '3' ";
                ps = db2.prepareStatement(countSql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    relatvieassessMstCount = rs.getInt("COUNT");
                }
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();

                final String sql;
                if (relatvieassessMstCount > 0) {
                    log.info(" use RELATIVEASSESS_MST.");
                    sql = " SELECT ASSESSLEVEL, ASSESSLOW, ASSESSHIGH FROM RELATIVEASSESS_MST WHERE GRADE = '" + maxGrade + "' AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _subclasscd + "' AND ASSESSCD = '3' ORDER BY ASSESSLEVEL";
                } else {
                    sql = " SELECT ASSESSLEVEL, ASSESSLOW, ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '3' ORDER BY ASSESSLEVEL";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("ASSESSHIGH", null == rs.getBigDecimal("ASSESSHIGH") ? null : String.valueOf(rs.getBigDecimal("ASSESSHIGH").intValue()));
                    m.put("ASSESSLOW", null == rs.getBigDecimal("ASSESSLOW") ? null : String.valueOf(rs.getBigDecimal("ASSESSLOW").intValue()));
                    assessMstMap.put(rs.getString("ASSESSLEVEL"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return assessMstMap;
        }

        private List getTestKindItemSidouInputList() {
            final List rtn = new ArrayList();
            for (int i = 0; i < _testKindItemList.size(); i++) {
                final TestItem testItem = (TestItem) _testKindItemList.get(i);
                if ("1".equals(testItem._sidouInput)) {
                    rtn.add(testItem);
                }
            }
            return rtn;
        }

//        private List getTestKindItemKariHyouteiList(final List testKindItemList) {
//            final List rtn = new ArrayList();
//            for (final Iterator it = testKindItemList.iterator(); it.hasNext();) {
//                final TestItem testItem = (TestItem) it.next();
//                if (!ALL_SEME.equals(testItem._semester._cdSemester) && "99".equals(testItem._testkindcd) && "00".equals(testItem._testitemcd) && KARIHYOUTEI_SCORE_DIV.equals(testItem._scoreDiv)) {
//                    rtn.add(testItem);
//                    // it.remove();
//                    // testItem._semester._testItemList.remove(testItem);
//                }
//            }
//            return rtn;
//        }

        private List getTestKindItemList(DB2UDB db2, final boolean useSubclassControl, final boolean addSemester) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                    final Semester semester = (Semester) it.next();
                    semesterMap.put(semester._cdSemester, semester);
                }
                String[] subclassArray = StringUtils.split(_subclasscd, "-");
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                if (useSubclassControl) {
                    stb.append("   SELECT DISTINCT ");
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                    if ("1".equals(_use_school_detail_gcm_dat)) {
                        stb.append("   FROM ADMIN_CONTROL_GCM_SDIV_DAT T1 ");
                    } else {
                        stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                    }
                    stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                    stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _subclasscd + "' ");
                    if ("1".equals(_use_school_detail_gcm_dat)) {
                        stb.append("     AND T1.GRADE = '00' ");
                        stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
                    }
                    stb.append("   UNION ALL ");
                }
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("   FROM ADMIN_CONTROL_GCM_SDIV_DAT T1 ");
                } else {
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                }
                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("     AND T1.GRADE = '00' ");
                    stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
                }
                stb.append("   UNION ALL ");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("   FROM ADMIN_CONTROL_GCM_SDIV_DAT T1 ");
                } else {
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                }
                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-" + subclassArray[1] + "-00-000000' ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("     AND T1.GRADE = '00' ");
                    stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
                }
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                }
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T11.GRADE = T1.GRADE ");
                    stb.append("    AND T11.COURSECD = T1.COURSECD ");
                    stb.append("    AND T11.MAJORCD = T1.MAJORCD ");
                }
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T1.SCHOOLCD = '" + _PRINT_SCHOOLCD + "' ");
                    stb.append("    AND T1.SCHOOL_KIND = '" + _PRINT_SCHOOLKIND + "' ");
                    stb.append("    AND T1.GRADE = '00' ");
                    stb.append("    AND T1.COURSECD || '-' || T1.MAJORCD = '"  + _COURSE_MAJOR + "' ");
                }
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    SemesterDetail semesterDetail = null;
                    final String cdsemesterDetail = rs.getString("SEMESTER_DETAIL");
                    if (null != cdsemesterDetail) {
                        if (_semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) _semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            _semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = HYOTEI_TESTCD.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!ALL_SEME.equals(semester._cdSemester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
                    if (isGakunenHyotei) {
                        final TestItem testItemKari = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
                                semesterDetail);
                        testItemKari._printKettenFlg = -1;
                        testItemKari._isGakunenKariHyotei = true;
                        if (addSemester) {
                            semester._testItemList.add(testItemKari);
                        }
                        list.add(testItemKari);
                    }
                    if (addSemester) {
                        semester._testItemList.add(testItem);
                    }
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testcd = " + list);
            return list;
        }

        private Subclass getSubclass(final DB2UDB db2, final String paramSubclasscd, final String paramYear) {
            Subclass subclass = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" WITH REPLACE AS ( ");
                sql.append(" SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + paramYear + "' ");
                sql.append(" UNION ");
                sql.append(" SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + paramYear + "' ");
                sql.append(" ) ");
                sql.append(" SELECT ");
                sql.append(" T1.*, ");
                sql.append(" CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ");
                sql.append(" CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ");
                sql.append(" FROM SUBCLASS_MST T1 ");
                sql.append(" LEFT JOIN REPLACE L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ");
                sql.append(" LEFT JOIN REPLACE L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ");
                sql.append(" WHERE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + paramSubclasscd + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    subclass = new Subclass(classcd, schoolKind, curriculumCd, subclasscd, subclassname, isSaki, isMoto);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return subclass;
        }
    }
}

// eof

