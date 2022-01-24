/*
 * $Id: 60fb022e05ba40cfa3689b35de1fe841f3b688fd $
 *
 * 作成日: 2014/12/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *  学校教育システム 賢者 IB観点相関表(MYP, DP)
 */
public class KNJZ068EF {

    private static final Log log = LogFactory.getLog(KNJZ068EF.class);

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
        FormCommon form = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            if (_param.isFormDP()) {
                form = new FormDP();
            } else {
                form = new FormMYP();
            }
            if ("csv".equals(_param._cmd)) {
                form.outputCsv(db2, response);
            } else {
                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                form.printMain(db2, svf);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if ("csv".equals(_param._cmd)) {
                
            } else {
                if (null == form || !form._hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private static Map getMappedMap(final Map m, final String key) {
        if (null == m) {
            return new HashMap();
        }
        if (null == m.get(key)) {
            m.put(key, new HashMap());
        }
        return (Map) m.get(key);
    }

    private static String getString(final Map m, final String key) {
        return (String) m.get(key);
    }

    private static String addNum(final Object s1, final Object s2) {
        if (null == s1 && null == s2) {
            return null;
        }
        final int n1 = null == s1 ? 0 : Integer.parseInt(s1.toString());
        final int n2 = null == s2 ? 0 : Integer.parseInt(s2.toString());
        final String rtn = String.valueOf(n1 + n2);
        //log.debug(" addNum " + s1 + ", " + s2 + " -> " + rtn);
        return rtn;
    }

    private abstract class FormCommon {

        protected abstract void printIbSubclass(final DB2UDB db2, final Vrw32alp svf, final IbGrade ibGrade);

        protected abstract List getCsvOutputLine(final DB2UDB db2, final IbGrade ibGrade);

        protected boolean _hasData = false;

        protected int setForm(final Vrw32alp svf, final String form, final int n) {
            return svf.VrSetForm(form, n);
        }

        public void printMain(final DB2UDB db2, final Vrw32alp svf) {

            final IbGrade ibGrade = new IbGrade();
            IBSubclass.setIBSubclassList(db2, _param, ibGrade);
            printIbSubclass(db2, svf, ibGrade);
        }
        
        public void outputCsv(final DB2UDB db2, final HttpServletResponse response) {
            final String filename;
            if (_param.isFormDP()) {
                filename = "相関表（DP）.csv";
            } else {
                filename = "相関表（MYP）.csv";
            }
            final IbGrade ibGrade = new IbGrade();
            IBSubclass.setIBSubclassList(db2, _param, ibGrade);
            CsvUtils.outputLines(log, response, filename, getCsvOutputLine(db2, ibGrade));
        }

        protected List retDividString(final String targetsrc0, final int ketamax) {
            if (targetsrc0 == null) {
                return Collections.EMPTY_LIST;
            }
            final List lines = new ArrayList();         //編集後文字列を格納する配列
            int ketacurrent = 0;
            StringBuffer stb = new StringBuffer();

            try {
                final String targetsrc;
                if (!StringUtils.replace(targetsrc0, "\r\n", "\n").equals(targetsrc0)) {
                    targetsrc = StringUtils.replace(targetsrc0, "\r\n", "\n");
                } else {
                    targetsrc = targetsrc0;
                }

                final List charList = getCharStringList(targetsrc);

                for (final Iterator it = charList.iterator(); it.hasNext();) {
                    final String c = (String) it.next();
                    //log.debug(" c = " + c);
                    final int cketa = getMS932ByteLength(c);
                    if (("\n".equals(c) || "\r".equals(c))) {
                        if (ketacurrent <= ketamax) {
                            lines.add(stb.toString());
                            ketacurrent = 0;
                            stb.delete(0, stb.length());
                        }
                    } else {
                        if (ketacurrent + cketa > ketamax) {
                            lines.add(stb.toString());
                            ketacurrent = 0;
                            stb.delete(0, stb.length());
                        }
                        stb.append(c);
                        ketacurrent += cketa;
                    }
                }
                if (0 < ketacurrent) {
                    lines.add(stb.toString());
                }
            } catch (Exception ex) {
                log.error("retDividString error! ", ex);
            }
            return lines;
        }

        protected LinkedList retDividStringMojisu(final String targetsrc0, final int mojisu) {
            final LinkedList lines = new LinkedList();         //編集後文字列を格納する配列
            if (targetsrc0 == null) {
                return lines;
            }
            int len = 0;
            StringBuffer stb = new StringBuffer();

            try {
                final String targetsrc;
                if (!StringUtils.replace(targetsrc0, "\r\n", "\n").equals(targetsrc0)) {
                    targetsrc = StringUtils.replace(targetsrc0, "\r\n", "\n");
                } else {
                    targetsrc = targetsrc0;
                }

                final List charList = getCharStringList(targetsrc);

                for (final Iterator it = charList.iterator(); it.hasNext();) {
                    final String c = (String) it.next();
                    //log.debug(" c = " + c);
                    final int clen = 1;
                    if (("\n".equals(c) || "\r".equals(c))) {
                        if (len <= mojisu) {
                            lines.add(stb.toString());
                            len = 0;
                            stb.delete(0, stb.length());
                        }
                    } else {
                        if (len + clen > mojisu) {
                            lines.add(stb.toString());
                            len = 0;
                            stb.delete(0, stb.length());
                        }
                        stb.append(c);
                        len += clen;
                    }
                }
                if (0 < len) {
                    lines.add(stb.toString());
                }
            } catch (Exception ex) {
                log.error("retDividStringMojisu error! ", ex);
            }
            return lines;
        }

        private List getCharStringList(final String source) {
            final List list = new ArrayList();
            for (int i = 0, len = source.length(); i < len; i++) {
                list.add(String.valueOf(source.charAt(i)));
            }
            return list;
        }
    }
    
    public static LinkedList nextLine(final List lines) {
        final LinkedList nextLine = new LinkedList();
        lines.add(nextLine);
        return nextLine;
    }

    private class FormMYP extends FormCommon {
        
        private List getPageList(final List _ibsubclassList, final int count) {
            final List rtn = new ArrayList();
            List current = null;
            for (final Iterator it = _ibsubclassList.iterator(); it.hasNext();) {
                final Object o = it.next();
                if (null == current || current.size() > count) {
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(o);
            }
            return rtn;
        }
        
        protected void printIbSubclass(final DB2UDB db2, final Vrw32alp svf, final IbGrade ibGrade) {
            
            final String form = "KNJZ068E.frm";
            svf.VrSetForm(form, 4);
            
            svf.VrsOut("TITLE", "IB assessment criteria と学習指導要領上の観点との相関表"); // タイトル
            svf.VrsOut("MYP_YEAR", "(" + StringUtils.defaultString(_param._gradeName3) + ")"); // MYP学年
            //svf.VrsOut("REMARL", null); // 
            
            final List pageList = getPageList(ibGrade._ibsubclassList, 9);
            for (int pi = 0, pageListSize = pageList.size(); pi < pageListSize; pi++) {
                final List ibsubclassList = (List) pageList.get(pi);
                
                for (int subclassidx = 0, size = ibsubclassList.size(); subclassidx < size; subclassidx++) {
                    final IBSubclass subclass = (IBSubclass) ibsubclassList.get(subclassidx);
                    final int line = subclassidx + 1;
                    
                    final List subclassnameList = new ArrayList(subclass.getUnitSubclassnameMap().values());
                    if (subclassnameList.size() == 1) {
                        svf.VrsOutn("CLASS1", line, (String) subclassnameList.get(0)); // 教科名
                    } else if (subclassnameList.size() == 2) {
                        svf.VrsOutn("CLASS2_1", line, (String) subclassnameList.get(0)); // 教科名
                        svf.VrsOutn("CLASS2_2", line, (String) subclassnameList.get(1)); // 教科名
                    } else if (subclassnameList.size() >= 3) {
                        svf.VrsOutn("CLASS2_1", line, (String) subclassnameList.get(0)); // 教科名
                        svf.VrsOutn("CLASS1", line, (String) subclassnameList.get(1)); // 教科名
                        svf.VrsOutn("CLASS2_2", line, (String) subclassnameList.get(2)); // 教科名
                    }
                    svf.VrsOutn("MYP_SUBJECT1", line, subclass._ibsubclassnameEng); // Subjects
                    svf.VrsOutn("MYP_SUBJECT2", line, subclass._ibsubclassname); // Subjects
                }
                
                for (int subclassidx = 0, size = ibsubclassList.size(); subclassidx < size; subclassidx++) {
                    final IBSubclass ibsubclass = (IBSubclass) ibsubclassList.get(subclassidx);
                    final int line = subclassidx + 1;
                    
                    for (int evali = 0, evallen = ibsubclass._evalList.size(); evali < evallen; evali++) {
                        final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(evali);

                        svf.VrsOutn("CRITERION" + String.valueOf(evali + 1), line, "Criterion " + StringUtils.defaultString(eval._ibevalMark)); // 科目名
                    }
                }
                
                for (int uniti = 0, unitimax = 16; uniti < unitimax; uniti++) {
                    svf.VrsOut("UNIT_NAME", "Unit " + String.valueOf(uniti + 1)); // 科目名

                    for (int subclassidx = 0, size = ibsubclassList.size(); subclassidx < size; subclassidx++) {
                        final IBSubclass ibsubclass = (IBSubclass) ibsubclassList.get(subclassidx);
                        
                        if (ibsubclass._unitList.size() > uniti) {
                            
                            final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(uniti);
                            final int line = subclassidx + 1;

                            svf.VrsOutn(getMS932ByteLength(unit.unitSemesterHeaderName()) > 10 ? "UNIT_SUBJECT2_1" : "UNIT_SUBJECT1", line, unit.unitSemesterHeaderName()); // 科目名

                            for (int evali = 0, evallen = ibsubclass._evalList.size(); evali < evallen; evali++) {
                                final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(evali);

                                final Map viewcdPerfectMap = getMappedMap(eval._unitViewcdPerfectMap, unit._ibseq);
                                for (final Iterator vit = viewcdPerfectMap.keySet().iterator(); vit.hasNext();) {
                                    final String viewcd = (String) vit.next();
                                    if (!NumberUtils.isDigits(viewcd)) {
                                        continue;
                                    }
                                    final int vn = Integer.parseInt(viewcd);
                                    final String perfect = (String) viewcdPerfectMap.get(viewcd);
                                    svf.VrsOutn("UNIT_VIEW_DIV" + String.valueOf(evali + 1) + "_" + String.valueOf(vn % 10), line, perfect); // unit観点評定
                                }
                            }
                        }
                    }

                    svf.VrEndRecord();
                    _hasData = true;
                }
            }
        }
        
        protected List getCsvOutputLine(final DB2UDB db2, final IbGrade ibGrade) {
            final List lines = new ArrayList();
            nextLine(lines).addAll(Arrays.asList(new String[] {"IB assessment criteria と学習指導要領上の観点との相関表"}));
            nextLine(lines).addAll(Arrays.asList(new String[] {"(" + StringUtils.defaultString(_param._gradeName3) + ")"}));
            
            final int maxUnit = 16;
            final List header1 = nextLine(lines);
            header1.add("教科");
            header1.add("MYP Subjects");
            header1.add("Unit");
            for (int ui = 1; ui <= maxUnit; ui++) {
                header1.add("Unit" + String.valueOf(ui));
                for (int vk = 2; vk <= 5; vk++) {
                    header1.add(null);
                }
            }
            
            final List header2 = nextLine(lines);
            header2.add("");
            header2.add("");
            header2.add("観点");
            for (int ui = 1; ui <= maxUnit; ui++) {
                for (int vk = 1; vk <= 5; vk++) {
                    header2.add(String.valueOf(vk));
                }
            }
            
            for (final Iterator iit = ibGrade._ibsubclassList.iterator(); iit.hasNext();) {
                final IBSubclass ibsubclass = (IBSubclass) iit.next();
                log.debug(" ibsubclass = " + ibsubclass._ibSubclasscd + " : " + ibsubclass._ibsubclassnameEng);

                final List subheader = nextLine(lines);
                subheader.add(null);
                subheader.add(null);
                subheader.add("教科・科目(前後期)");
                
                for (int ui = 0, size = ibsubclass._unitList.size(); ui < size; ui++) {
                    IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(ui);
                    subheader.add(StringUtils.defaultString(unit._unitSubclassabbv) + " " + unit.unitSemesterHeaderName());
                    for (int vk = 2; vk <= 5; vk++) {
                        subheader.add(null);
                    }
                }
                
                final List unitclassnameList = new ArrayList(ibsubclass.getUnitSubclassnameMap().values());
                final List mypSubjectnameList = Arrays.asList(new String[] {ibsubclass._ibclassnameEng, ibsubclass._ibclassname});
                final String kantenNo = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                final int evalSize = Math.max(Math.max(4, ibsubclass._evalList.size()), unitclassnameList.size());
                final List[] subclassLines = new List[evalSize]; 
                for (int sli = 0; sli < evalSize; sli++) {
                    subclassLines[sli] = nextLine(lines);
                    
                    // 1列目
                    if (sli < unitclassnameList.size()) {
                        subclassLines[sli].add(unitclassnameList.get(sli));
                    } else {
                        subclassLines[sli].add(null);
                    }
                    
                    // 2列目
                    if (sli < mypSubjectnameList.size()) {
                        subclassLines[sli].add(mypSubjectnameList.get(sli));
                    } else {
                        subclassLines[sli].add(null);
                    }
                    
                    if (sli < ibsubclass._evalList.size()) {
                        final IBSubclass.Eval eval = (IBSubclass.Eval) ibsubclass._evalList.get(sli);
                        // 3列目
                        subclassLines[sli].add("Criterion " + StringUtils.defaultString(eval._ibevalMark));
                        
                        // n列目
                        for (int ui = 0, size = ibsubclass._unitList.size(); ui < size; ui++) {
                            final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(ui);
                            
                            final Map perfectMap = getMappedMap(eval._unitViewcdPerfectMap, unit._ibseq);
                            
                            for (int vk = 1; vk <= 5; vk++) {
                                String perfect = null;
                                for (final Iterator it = perfectMap.keySet().iterator(); it.hasNext();) {
                                    final String viewcd = (String) it.next();
                                    if (NumberUtils.isDigits(viewcd) && vk == Integer.parseInt(viewcd) % 10) {
                                        perfect = (String) perfectMap.get(viewcd);
                                        break;
                                    }
                                }
                                subclassLines[sli].add(perfect);
                            }
                        }
                    } else {
                        // 3列目
                        subclassLines[sli].add("");
                    }
                }
            }
            
            return lines;
        }

    }

    private class FormDP extends FormCommon {

        protected void printIbSubclass(final DB2UDB db2, final Vrw32alp svf, final IbGrade ibGrade) {
            for (final Iterator iit = ibGrade._ibsubclassList.iterator(); iit.hasNext();) {
                final IBSubclass ibsubclass = (IBSubclass) iit.next();

                log.debug(" ibsubclass = " + ibsubclass._ibSubclasscd + " : " + ibsubclass._ibsubclassnameEng);

                printIbSubclass1(svf, ibGrade, ibsubclass);
            }
        }

        private void printIbSubclass1(final Vrw32alp svf, final IbGrade ibGrade, final IBSubclass ibsubclass) {
            final String form = "KNJZ068F.frm";
            svf.VrSetForm(form, 4);
            
            svf.VrsOut("TITLE", "DP assessment Criteria"); // タイトル
            svf.VrsOut("MYP_YEAR", "(" + StringUtils.defaultString(_param._gradeName3) + ")"); // MYP学年
            
//            final int maxLine = 48;
            
            for (int unii = 0, unilen = ibsubclass._unitList.size(); unii < unilen; unii++) {
                final String un = String.valueOf(unii + 1);
                final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(unii);
                //log.debug(" unit ibseq = " + unit._ibseq);

                svf.VrsOut("TASK_SUBJECT" + un + "_" + (getMS932ByteLength(unit._unitSubclassabbv) > 10 ? "2" : "1"), unit._unitSubclassabbv); // 科目名
                svf.VrsOut("TASK_NAME" + un, "Task " + un); // ユニット名
                svf.VrsOut("TASK_SEMESTER" + un, unit.unitSemesterHeaderName()); // 学期
            }
            
            

            final List groupBy12List = IBSubclass.Eval.groupByDiv12(ibsubclass._evalList);
            
            int totalline = 0;
            for (int div1i = 0, div1size = groupBy12List.size(); div1i < div1size; div1i++) {
                final Map evaldiv1Map = (Map) groupBy12List.get(div1i);
                final List evaldiv1List = (List) evaldiv1Map.get("LIST");
                
                for (int div2i = 0, div2size = evaldiv1List.size(); div2i < div2size; div2i++) {
                    final Map evaldiv2Map = (Map) evaldiv1List.get(div2i);
                    final List evaldiv2List = (List) evaldiv2Map.get("LIST");
                    
                    final int kantenSize = Math.max(4, evaldiv2List.size());
                    totalline += kantenSize;
                }
            }
            
            final LinkedList unitsubclassnameList = getCenterizedList(totalline, divideConcat(20, ibsubclass.getUnitSubclassnameMap().values()));
            final LinkedList ibsubclassnameList = getCenterizedList(totalline, divideConcat(20, Arrays.asList(new String[] {StringUtils.defaultString(ibsubclass._ibclassnameEng) + "/", ibsubclass._ibsubclassnameEng, StringUtils.defaultString(ibsubclass._ibclassname) + "/", ibsubclass._ibsubclassname})));

            int lineIdx = 0;
            for (int div1i = 0, div1size = groupBy12List.size(); div1i < div1size; div1i++) {
                final Map evaldiv1Map = (Map) groupBy12List.get(div1i);
                //log.debug(" evaldiv1map = " + evaldiv1Map);
                final List evaldiv1List = (List) evaldiv1Map.get("LIST");
                
                for (int div2i = 0, div2size = evaldiv1List.size(); div2i < div2size; div2i++) {
                    final Map evaldiv2Map = (Map) evaldiv1List.get(div2i);
                    final List evaldiv2List = (List) evaldiv2Map.get("LIST");
                    
                    final int kantenSize = Math.max(4, evaldiv2List.size());
                    
                    String div1SumPerfect = null;
                    for (int i = 0; i < kantenSize; i++) {
                        if (i < evaldiv2List.size()) {
                            final IBSubclass.Eval eval = (IBSubclass.Eval) evaldiv2List.get(i);
                            for (int ui = 0, usize = ibsubclass._unitList.size(); ui < usize; ui++) {
                                final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(ui);
                                final Map perfectMap = getMappedMap(eval._unitViewcdPerfectMap, unit._ibseq);
                                String perfect = null;
                                for (final Iterator it = perfectMap.values().iterator(); it.hasNext();) {
                                    perfect = (String) it.next();
                                }
                                div1SumPerfect = addNum(div1SumPerfect, perfect);
                            }
                        }
                    }

                    
                    for (int i = 0; i < kantenSize; i++) {
                        final String kanteni = String.valueOf(i);
                        final boolean isDiv2CenterIdx = i == (kantenSize - 1) / 2;

                        svf.VrsOut("CLASS_NAME", (String) unitsubclassnameList.get(lineIdx)); // 教科科目名
                        svf.VrsOut("GRP1", "1"); // グループ1
                        svf.VrsOut("DP_CLASS_NAME", (String) ibsubclassnameList.get(lineIdx)); // DP教科科目名
                        svf.VrsOut("GRP2", "2"); // グループ2
                        svf.VrsOut("ASSESS_COMPO1", isDiv2CenterIdx ? (String) evaldiv1Map.get("NAME1") : ""); // 評価の構成
                        final String div1 = (String) evaldiv1Map.get("DIV1");
                        svf.VrsOut("GRP3", div1); // グループ3
                        svf.VrsOut("ASSESS_COMPO2", isDiv2CenterIdx ? (String) evaldiv2Map.get("NAME2") : ""); // 評価の構成
                        final String div2 = (String) evaldiv2Map.get("DIV2");
                        svf.VrsOut("GRP4", div1 + div2); // グループ4
                        svf.VrsOut("GRP5", kanteni); // グループ5
                        svf.VrsOut("GRP6", kanteni); // グループ6
                        svf.VrsOut("GRP7", kanteni); // グループ7
                        
                        String div2SumPerfect = null;
                        if (i < evaldiv2List.size()) {

                            final IBSubclass.Eval eval = (IBSubclass.Eval) evaldiv2List.get(i);
                            
                            svf.VrsOut("CRITERION", "Criterion " + StringUtils.defaultString(eval._ibevalMark)); // 基準
                            svf.VrsOut("ENG_VIEW1", eval._ibevalNameEng); // 英語観点
                            svf.VrsOut("ENG_VIEW2", eval._ibevalName); // 英語観点
                            svf.VrsOut("VIEW_DIV", eval._ibevalPerfect); // 観点評価

                            for (int ui = 0, usize = ibsubclass._unitList.size(); ui < usize; ui++) {
                                IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(ui);
                                final Map perfectMap = getMappedMap(eval._unitViewcdPerfectMap, unit._ibseq);
                                String perfect = null;
                                for (final Iterator it = perfectMap.values().iterator(); it.hasNext();) {
                                    perfect = (String) it.next();
                                }
                                
                                svf.VrsOut("VIEW" + String.valueOf(ui + 1), perfect); // 観点評価
                                div2SumPerfect = addNum(div2SumPerfect, perfect);
                            }
                        }
                        
                        svf.VrsOut("SUB_TOTAL_VIEW", div2SumPerfect); // 観点総合値
                        svf.VrsOut("GRP24", kanteni); // グループ24
                        svf.VrsOut("TOTAL_VIEW", isDiv2CenterIdx ? div1SumPerfect : ""); // 観点合計値
                        svf.VrsOut("GRP25", div1 + div2); // グループ25
                        svf.VrEndRecord();
                        _hasData = true;
                        lineIdx += 1;
                    }
                }
            }
        }

        private Collection divideConcat(int keta, final Collection strList) {
            final List rtn = new ArrayList();
            for (final Iterator it = strList.iterator(); it.hasNext();) {
                final String s = (String) it.next();
                rtn.addAll(retDividString(s, keta));
            }
            return rtn;
        }

        // listを中央寄せする
        private LinkedList getCenterizedList(final int totalline, final Collection col) {
            final LinkedList rtn = new LinkedList(col);
            if (col.size() >= totalline) {
                return rtn;
            }
            for (int i = 0, pre = (totalline - col.size()) / 2; i < pre; i++) {
                rtn.addFirst(null); 
            }
            for (int i = col.size(); i < totalline; i++) {
                rtn.addLast(null); 
            }
            return rtn;
        }

        private String reverse(final String s) {
            if (null == s) return "";
            final StringBuffer stb = new StringBuffer();
            for (int i = s.length() - 1; i >= 0; i--) {
                stb.append(s.charAt(i));
            }
            return stb.toString();
        }
        
        protected List getCsvOutputLine(final DB2UDB db2, final IbGrade ibGrade) {
            final List lines = new ArrayList();
            
            for (final Iterator iit = ibGrade._ibsubclassList.iterator(); iit.hasNext();) {
                final IBSubclass ibsubclass = (IBSubclass) iit.next();
                log.debug(" ibsubclass = " + ibsubclass._ibSubclasscd + " : " + ibsubclass._ibsubclassnameEng);

                nextLine(lines).addAll(Arrays.asList(new String[] {"DP assessment criteria"}));
                nextLine(lines).addAll(Arrays.asList(new String[] {"(" + StringUtils.defaultString(_param._gradeName3) + ")"}));
                
                final int maxUnit = 16;
                final List header1 = nextLine(lines);
                final List header2 = nextLine(lines);
                final List header3 = nextLine(lines);
                final List header4 = nextLine(lines);
                header1.add("教科・科目"); // 1
                header2.add("");
                header3.add("");
                header4.add("");
                header1.add("DP Subjects/Course"); // 2
                header2.add("DP教科/コース");
                header3.add("");
                header4.add("");
                header1.add("Assessment component"); // 3
                header2.add("評価の構成");
                header3.add("");
                header4.add("");
                header1.add(""); // 4
                header2.add("");
                header3.add("");
                header4.add("");
                header1.add(""); // 5
                header2.add("");
                header3.add("Assessment Criteria");
                header4.add("評価基準");
                header1.add(""); // 6
                header2.add("");
                header3.add("");
                header4.add("");
                header1.add("年度・学期"); // 7
                header2.add("教科・科目");
                header3.add("");
                header4.add("");
                for (int ui = 0, size = maxUnit; ui < size; ui++) {
                    if (ui < ibsubclass._unitList.size()) {
                        final IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(ui);
                        header1.add(unit.unitSemesterHeaderName());
                        header2.add(unit._unitSubclassabbv);
                    } else {
                        header1.add("");
                        header2.add("");
                    }

                    header3.add("Task" + String.valueOf(ui + 1));
                    header4.add("");
                }
                header3.add("計");
                header3.add("合計");
                
                String divSumPerfect = null;
                final List alldivList = new ArrayList();
                final String kantenNo = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                final List groupBy12List = IBSubclass.Eval.groupByDiv12(ibsubclass._evalList);
                for (int div1i = 0, div1size = groupBy12List.size(); div1i < div1size; div1i++) {
                    final Map evaldiv1Map = (Map) groupBy12List.get(div1i);
                    log.debug(" evaldiv1map = " + evaldiv1Map);
                    final List evaldiv1List = (List) evaldiv1Map.get("LIST");
                    
                    final List allEvaldiv2List = new ArrayList();
                    for (int div2i = 0, div2size = evaldiv1List.size(); div2i < div2size; div2i++) {
                        final Map evaldiv2Map = (Map) evaldiv1List.get(div2i);
                        final List evaldiv2List = (List) evaldiv2Map.get("LIST");
                        
                        final int kantenSize = Math.max(4, evaldiv2List.size());
                        LinkedList[] kanten = new LinkedList[kantenSize * 2];
                        for (int i = 0; i < kantenSize * 2; i+= 2) {
                            kanten[i] = nextLine(lines);
                            kanten[i + 1] = nextLine(lines);
                        }

                        String[] div2SumPerfect = new String[kantenSize];
                        for (int i = 0; i < kantenSize; i++) {
                            final int i0 = 2 * i + 0;
                            final int i1 = 2 * i + 1;
                            kanten[i0].add("Criterion " + String.valueOf(kantenNo.charAt(i)));
                            kanten[i1].add("");
                            
                            if (i < evaldiv2List.size()) {
                                final IBSubclass.Eval eval = (IBSubclass.Eval) evaldiv2List.get(i);
                                
                                kanten[i0].add(eval._ibevalNameEng);
                                kanten[i1].add(eval._ibevalName);
                                kanten[i0].add(eval._ibevalPerfect);
                                kanten[i1].add("");
                                
                                for (int ui = 0, usize = ibsubclass._unitList.size(); ui < usize; ui++) {
                                    IBSubclass.Unit unit = (IBSubclass.Unit) ibsubclass._unitList.get(ui);
                                    final Map perfectMap = getMappedMap(eval._unitViewcdPerfectMap, unit._ibseq);
                                    String perfect = null;
                                    for (final Iterator it = perfectMap.values().iterator(); it.hasNext();) {
                                        perfect = (String) it.next();
                                    }
                                    kanten[i0].add(perfect);
                                    div2SumPerfect[i] = addNum(div2SumPerfect[i], perfect);
                                }
                            } else {
                                kanten[i0].add(null);
                            }
                        }
                        
                        for (int ui = ibsubclass._unitList.size(), usize = 16; ui < usize; ui++) {
                            for (int i = 0; i < kantenSize * 2; i+= 2) {
                                kanten[i].add("");
                                kanten[i + 1].add("");
                            }
                        }
                        String div1SumPerfect = null;
                        for (int i = 0; i < kantenSize; i++) {
                            final int i0 = 2 * i + 0;
                            kanten[i0].add(div2SumPerfect[i]);
                            div1SumPerfect = addNum(div1SumPerfect, div2SumPerfect[i]);
                        }
                        
                        kanten[0].add(div1SumPerfect);
                        
                        addColumnLeft(Arrays.asList(kanten), Arrays.asList(new String[] {(String) evaldiv2Map.get("NAME2")}));
                        allEvaldiv2List.addAll(Arrays.asList(kanten));
                    }
                    
                    addColumnLeft(allEvaldiv2List, Arrays.asList(new String[] {(String) evaldiv1Map.get("NAME1")}));
                    alldivList.addAll(allEvaldiv2List);
                }
                
                addColumnLeft(alldivList, Arrays.asList(new String[] {StringUtils.defaultString(ibsubclass._ibclassnameEng) + "/" + StringUtils.defaultString(ibsubclass._ibsubclassnameEng), StringUtils.defaultString(ibsubclass._ibclassname) + "/" + StringUtils.defaultString(ibsubclass._ibsubclassname)}));
                addColumnLeft(alldivList, new ArrayList(ibsubclass.getUnitSubclassnameMap().values()));
            }
            
            return lines;
        }

        private void addColumnRight(final List alldivList, final List columnList) {
            for (int i = 0; i < columnList.size(); i++) {
                ((LinkedList) alldivList.get(i)).addLast(columnList.get(i));    
            }
            for (int i = columnList.size(); i < alldivList.size(); i++) {
                ((LinkedList) alldivList.get(i)).addLast("");
            }
        }

        private void addColumnLeft(final List alldivList, final List columnList) {
            if (alldivList.size() < columnList.size()) {
                for (int i = 0, size = columnList.size() - alldivList.size(); i < size; i++) {
                    nextLine(alldivList);
                }
            }
            for (int i = 0; i < columnList.size(); i++) {
                ((LinkedList) alldivList.get(i)).addFirst(columnList.get(i));    
            }
            for (int i = columnList.size(); i < alldivList.size(); i++) {
                ((LinkedList) alldivList.get(i)).addFirst("");
            }
        }

    }

    private static class IbGrade {

        private List _ibsubclassList = Collections.EMPTY_LIST;
        private List _chairsubclassList = Collections.EMPTY_LIST;

    }

    private static class ChairSubclass {
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final String _subclassabbv;
        boolean _hasIbSubclass = false;

        ChairSubclass(final String subclasscd,
                final String classname,
                final String subclassname,
                final String subclassabbv
                ) {
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
        }

        private static ChairSubclass getChairSubclass(final List chairsubclasslist, final String subclasscd) {
            for (final Iterator it = chairsubclasslist.iterator(); it.hasNext();) {
                final ChairSubclass e = (ChairSubclass) it.next();
                if (e._subclasscd.equals(subclasscd)) {
                    return e;
                }
            }
            return null;
        }
    }

    private static class IBSubclass {
        final String _ibSubclasscd;
        final String _ibclassname;
        final String _ibclassnameEng;
        final String _ibsubclassname;
        final String _ibsubclassnameEng;

        final List _unitList = new ArrayList();
        final List _evalList = new ArrayList();

        IBSubclass(
            final String ibSubclasscd,
            final String ibclassname,
            final String ibclassnameEng,
            final String ibsubclassname,
            final String ibsubclassnameEng
        ) {
            _ibSubclasscd = ibSubclasscd;
            _ibclassname = ibclassname;
            _ibclassnameEng = ibclassnameEng;
            _ibsubclassname = ibsubclassname;
            _ibsubclassnameEng = ibsubclassnameEng;
        }

        public Map getUnitSubclassnameMap() {
            final Map rtn = new TreeMap();
            for (final Iterator uit = _unitList.iterator(); uit.hasNext();) {
                final Unit unit = (Unit) uit.next();
                if (null != unit._unitSubclasscd) {
                    rtn.put(unit._unitSubclasscd, unit._unitSubclassname);
                }
            }
            return rtn;
        }

        public static void setIBSubclassList(final DB2UDB db2, final Param param, final IbGrade ibGrade) {
            ibGrade._ibsubclassList = new ArrayList();
            ibGrade._chairsubclassList = new ArrayList();
            ResultSet rs = null;
            try {
                final String sql = getIBSubclassSql(param);
                log.info(" ib subclass sql = " + sql);
                PreparedStatement ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String subclasscd = rs.getString("CHR_SUBCLASSCD");

                    if (null == ChairSubclass.getChairSubclass(ibGrade._chairsubclassList, subclasscd)) {
                        final String classname = rs.getString("CHR_CLASSNAME");
                        final String subclassname = rs.getString("CHR_SUBCLASSNAME");
                        final String subclassabbv = rs.getString("CHR_SUBCLASSABBV");

                        ibGrade._chairsubclassList.add(new ChairSubclass(subclasscd, classname, subclassname, subclassabbv));
                    }

                    final ChairSubclass chairSubclass = ChairSubclass.getChairSubclass(ibGrade._chairsubclassList, subclasscd);

                    final String ibSubclasscd = rs.getString("IB_SUBCLASSCD");

                    if (null != ibSubclasscd) {
                        chairSubclass._hasIbSubclass = true;

                        if (null == getIBSubclass(ibGrade._ibsubclassList, ibSubclasscd)) {
                            final String ibclassname = rs.getString("IBCLASSNAME");
                            final String ibclassnameEng = rs.getString("IBCLASSNAME_ENG");
                            final String ibsubclassname = rs.getString("IBSUBCLASSNAME");
                            final String ibsubclassnameEng = rs.getString("IBSUBCLASSNAME_ENG");
                            ibGrade._ibsubclassList.add(new IBSubclass(ibSubclasscd, ibclassname, ibclassnameEng, ibsubclassname, ibsubclassnameEng));
                        }
                        final IBSubclass ibsubclass = getIBSubclass(ibGrade._ibsubclassList, ibSubclasscd);

                        final String ibseq = rs.getString("IBSEQ");

                        if (null != ibseq && null == getUnit(ibsubclass._unitList, ibseq)) {
                            final String unitYear = rs.getString("UNIT_YEAR");
                            final String unitSemester = rs.getString("UNIT_SEMESTER");
                            final String unitSemestername = rs.getString("UNIT_SEMESTERNAME");
                            final String unitSubclasscd = rs.getString("UNIT_SUBCLASSCD");
                            final String unitSubclassname = rs.getString("UNIT_SUBCLASSNAME");
                            final String unitSubclassabbv = rs.getString("UNIT_SUBCLASSABBV");

                            ibsubclass._unitList.add(new Unit(ibseq, unitYear, unitSemester, unitSemestername, unitSubclasscd, unitSubclassname, unitSubclassabbv));
                        }

                        final String ibevalDiv1 = rs.getString("IBEVAL_DIV1");
                        final String ibevalDiv2 = rs.getString("IBEVAL_DIV2");
                        final String ibevalMark = rs.getString("IBEVAL_MARK");

                        if (null != ibseq && null != ibevalDiv1 && null != ibevalDiv2 && null != ibevalMark) {
                            final Unit unit = getUnit(ibsubclass._unitList, ibseq);

                            if (null == getEval(ibsubclass._evalList, ibevalDiv1, ibevalDiv2, ibevalMark)) {
                                final String ibevalDiv1Name = rs.getString("IBEVAL_DIV1_NAME");
                                final String ibevalDiv2Name = rs.getString("IBEVAL_DIV2_NAME");
                                final String ibevalName = rs.getString("IBEVAL_NAME");
                                final String ibevalNameEng = rs.getString("IBEVAL_NAME_ENG");
                                final String ibevalPerfect = rs.getString("IBEVAL_PERFECT");
                                ibsubclass._evalList.add(new Eval(ibevalDiv1, ibevalDiv1Name, ibevalDiv2, ibevalDiv2Name, ibevalMark, ibevalName, ibevalNameEng, ibevalPerfect));
                            }

                            final Eval eval = getEval(ibsubclass._evalList, ibevalDiv1, ibevalDiv2, ibevalMark);
                            final String viewcd = rs.getString("VIEWCD");
                            if (null != viewcd) {
                                getMappedMap(eval._unitViewcdPerfectMap, unit._ibseq).put(viewcd, rs.getString("IBEVAL_PERFECT"));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private static IBSubclass getIBSubclass(final List ibsubclasslist, final String ibsubclasscd) {
            for (final Iterator it = ibsubclasslist.iterator(); it.hasNext();) {
                final IBSubclass e = (IBSubclass) it.next();
                if (e._ibSubclasscd.equals(ibsubclasscd)) {
                    return e;
                }
            }
            return null;
        }

        private static class Unit {

            final String _ibseq;
            final String _year;
            final String _semester;
            final String _semestername;
            final String _unitSubclasscd;
            final String _unitSubclassname;
            final String _unitSubclassabbv;

            Unit(
                final String ibseq,
                final String year,
                final String semester,
                final String semestername,
                final String unitSubclasscd,
                final String unitSubclassname,
                final String unitSubclassabbv
            ) {
                _ibseq = ibseq;
                _year = year;
                _semester = semester;
                _semestername = semestername;
                _unitSubclasscd = unitSubclasscd;
                _unitSubclassname = unitSubclassname;
                _unitSubclassabbv = unitSubclassabbv;
            }
            
            public String unitSemesterHeaderName() {
                final DecimalFormat _df02 = new DecimalFormat("00");
                return (NumberUtils.isDigits(_year) ? _df02.format(Integer.parseInt(_year) % 100) : "") + StringUtils.defaultString(_semestername);
            }
        }

        private static Unit getUnit(final List unitlist, final String ibseq) {
            for (final Iterator it = unitlist.iterator(); it.hasNext();) {
                final Unit u = (Unit) it.next();
                if (u._ibseq.equals(ibseq)) {
                    return u;
                }
            }
            return null;
        }

        private static class Eval {

            final String _ibevalDiv1;
            final String _ibevalDiv1Name;
            final String _ibevalDiv2;
            final String _ibevalDiv2Name;
            final String _ibevalMark;
            final String _ibevalName;
            final String _ibevalNameEng;
            final String _ibevalPerfect;

            final Map _unitViewcdPerfectMap = new HashMap();

            Eval(
                final String ibevalDiv1,
                final String ibevalDiv1Name,
                final String ibevalDiv2,
                final String ibevalDiv2Name,
                final String ibevalMark,
                final String ibevalName,
                final String ibevalNameEng,
                final String ibevalPerfect
            ) {
                _ibevalDiv1 = ibevalDiv1;
                _ibevalDiv1Name = ibevalDiv1Name;
                _ibevalDiv2 = ibevalDiv2;
                _ibevalDiv2Name = ibevalDiv2Name;
                _ibevalMark = ibevalMark;
                _ibevalName = ibevalName;
                _ibevalNameEng = ibevalNameEng;
                _ibevalPerfect = ibevalPerfect;
            }
            
            public String toString() {
                return "Eval(" + _ibevalDiv1 + "/" + _ibevalDiv2 + "/" + _ibevalName + ")";
            }

            static Map getKeyMap(final List list, final String key, final String val) {
                Map tm = null;
                for (final Iterator sit = list.iterator(); sit.hasNext();) {
                    final Map m = (Map) sit.next();
                    if (m.get(key).equals(val)) {
                        tm = m;
                        break;
                    }
                }
                return tm;
            }
            
            static List groupByDiv12(final List evalList) {
                final List rtn = new ArrayList();
                for (final Iterator it = evalList.iterator(); it.hasNext();) {
                    final Eval eval = (Eval) it.next();
//                    log.debug(" eval = " + eval._ibevalDiv1 + ", " + eval._ibevalDiv1Name + ", " + eval._ibevalDiv2 + ", " + eval._ibevalDiv2Name + ", " + eval._ibevalMark);
                    
                    if (null == getKeyMap(rtn, "DIV1", eval._ibevalDiv1)) {
                        final Map m1 = new HashMap();
                        rtn.add(m1);
                        m1.put("DIV1", eval._ibevalDiv1);
                        m1.put("NAME1", eval._ibevalDiv1Name);
                        m1.put("LIST", new ArrayList());
                    }
                    final Map m1 = getKeyMap(rtn, "DIV1", eval._ibevalDiv1);
                    final List l1 = (List) m1.get("LIST");
                    
                    if (null == getKeyMap(l1, "DIV2", eval._ibevalDiv2)) {
                        final Map m2 = new HashMap();
                        m2.put("DIV2", eval._ibevalDiv2);
                        m2.put("NAME2", eval._ibevalDiv2Name);
                        m2.put("LIST", new ArrayList());
                        l1.add(m2);
                    }
                    
                    final Map m2 = getKeyMap(l1, "DIV2", eval._ibevalDiv2);
                    final List l2 = (List) m2.get("LIST");
                    l2.add(eval);
                }
                return rtn;
            }
        }

        private static Eval getEval(final List evallist, final String ibevalDiv1, final String ibevalDiv2, final String ibevalMark) {
            for (final Iterator it = evallist.iterator(); it.hasNext();) {
                final Eval e = (Eval) it.next();
                if (e._ibevalDiv1.equals(ibevalDiv1) && e._ibevalDiv2.equals(ibevalDiv2) && e._ibevalMark.equals(ibevalMark)) {
                    return e;
                }
            }
            return null;
        }

        public static String getIBSubclassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH IBSUBCLASS AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   IBR.CLASSCD || '-' || IBR.SCHOOL_KIND AS CHR_CLASSCD, ");
            stb.append("   IBR.CLASSCD || '-' || IBR.SCHOOL_KIND || '-' || IBR.CURRICULUM_CD || '-' || IBR.SUBCLASSCD AS CHR_SUBCLASSCD, ");
            stb.append("   IBR.IBCLASSCD || '-' || IBR.IBPRG_COURSE AS IB_CLASSCD, ");
            stb.append("   IBR.IBCLASSCD || '-' || IBR.IBPRG_COURSE || '-' || IBR.IBCURRICULUM_CD || '-' || IBR.IBSUBCLASSCD AS IB_SUBCLASSCD, ");
            stb.append("   VNY.IBEVAL_DIV1, ");
            stb.append("   VNY.IBEVAL_DIV2, ");
            stb.append("   VNY.IBEVAL_MARK, ");
            stb.append("   VNY.IBEVAL_NAME, ");
            stb.append("   VNY.IBEVAL_NAME_ENG, ");
            stb.append("   VNY.IBSORT AS IBEVAL_SORT, ");
            stb.append("   VNY.IBPERFECT AS IBEVAL_PERFECT, ");
            stb.append("   UNIT.LINK_NO ");
            stb.append(" FROM IBSUBCLASS_REPLACE_DAT IBR ");
            stb.append(" LEFT JOIN IBSUBCLASS_UNIT_DAT UNIT ON UNIT.IBYEAR = IBR.IBYEAR ");
            stb.append("     AND UNIT.IBGRADE = IBR.IBGRADE ");
            stb.append("     AND UNIT.IBCLASSCD = IBR.IBCLASSCD ");
            stb.append("     AND UNIT.IBPRG_COURSE = IBR.IBPRG_COURSE ");
            stb.append("     AND UNIT.IBCURRICULUM_CD = IBR.IBCURRICULUM_CD ");
            stb.append("     AND UNIT.IBSUBCLASSCD = IBR.IBSUBCLASSCD ");
            stb.append(" LEFT JOIN IBVIEW_NAME_YMST VNY ON VNY.IBYEAR = IBR.IBYEAR ");
            stb.append("     AND VNY.IBGRADE = IBR.IBGRADE ");
            stb.append("     AND VNY.IBCLASSCD = IBR.IBCLASSCD ");
            stb.append("     AND VNY.IBPRG_COURSE = IBR.IBPRG_COURSE ");
            stb.append("     AND VNY.IBSUBCLASSCD = IBR.IBSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append(" IBR.IBYEAR = '" + param._year + "' ");
            stb.append(" AND IBR.IBGRADE = '" + param._ibGrade + "' ");
            stb.append(" AND IBR.IBPRG_COURSE = '" + param._ibPrgCourse + "' ");
            stb.append(" AND (IBR.CLASSCD <= '90' OR IBR.CLASSCD = '93') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.CHR_CLASSCD, ");
            stb.append("   T1.CHR_SUBCLASSCD, ");
            stb.append("   CM.CLASSNAME AS CHR_CLASSNAME, ");
            stb.append("   SBM.SUBCLASSNAME AS CHR_SUBCLASSNAME, ");
            stb.append("   SBM.SUBCLASSABBV AS CHR_SUBCLASSABBV, ");
            // --
            stb.append("   T1.IB_SUBCLASSCD, ");
            stb.append("   ICM.IBCLASSNAME, ");
            stb.append("   ICM.IBCLASSNAME_ENG, ");
            stb.append("   ISBM.IBSUBCLASSNAME, ");
            stb.append("   ISBM.IBSUBCLASSNAME_ENG, ");
            stb.append("   T1.IBEVAL_DIV1, ");
            stb.append("   NMZ035.NAME1 AS IBEVAL_DIV1_NAME, ");
            stb.append("   T1.IBEVAL_DIV2, ");
            stb.append("   CASE T1.IBEVAL_DIV1 WHEN '1' THEN NMZ037.NAME1 WHEN '2' THEN NMZ038.NAME1 ELSE NMZ036.NAME1 END AS IBEVAL_DIV2_NAME, ");
            stb.append("   T1.IBEVAL_MARK, ");
            stb.append("   T1.IBEVAL_NAME, ");
            stb.append("   T1.IBEVAL_NAME_ENG, ");
            stb.append("   T1.IBEVAL_SORT, ");
            stb.append("   T1.IBEVAL_PERFECT, ");
            stb.append("   UNIT.LINK_NO, ");
            stb.append("   UNIT.IBSEQ, ");
            stb.append("   UNIT.YEAR AS UNIT_YEAR, ");
            stb.append("   UNIT.SEMESTER AS UNIT_SEMESTER, ");
            stb.append("   UNITSM.SEMESTERNAME AS UNIT_SEMESTERNAME, ");
            stb.append("   UNIT.CLASSCD || '-' || UNIT.SCHOOL_KIND || '-' || UNIT.CURRICULUM_CD || '-' || UNIT.SUBCLASSCD AS UNIT_SUBCLASSCD, ");
            stb.append("   USBM.SUBCLASSNAME AS UNIT_SUBCLASSNAME, ");
            stb.append("   USBM.SUBCLASSABBV AS UNIT_SUBCLASSABBV, ");
            stb.append("   UPLAN.VIEWCD ");
            stb.append(" FROM IBSUBCLASS T1 ");
            stb.append(" INNER JOIN CLASS_MST CM ON CM.CLASSCD || '-' || CM.SCHOOL_KIND = T1.CHR_CLASSCD ");
            stb.append(" INNER JOIN SUBCLASS_MST SBM ON SBM.CLASSCD || '-' || SBM.SCHOOL_KIND || '-' || SBM.CURRICULUM_CD || '-' || SBM.SUBCLASSCD = T1.CHR_SUBCLASSCD ");
            // --
            stb.append(" LEFT JOIN IBSUBCLASS_UNIT_DAT UNIT ON UNIT.LINK_NO = T1.LINK_NO ");
            stb.append(" LEFT JOIN IBSUBCLASS_UNITPLAN_DAT UPLAN ON UPLAN.IBYEAR = UNIT.IBYEAR ");
            stb.append("     AND UPLAN.IBGRADE = UNIT.IBGRADE ");
            stb.append("     AND UPLAN.IBCLASSCD = UNIT.IBCLASSCD ");
            stb.append("     AND UPLAN.IBPRG_COURSE = UNIT.IBPRG_COURSE ");
            stb.append("     AND UPLAN.IBCURRICULUM_CD = UNIT.IBCURRICULUM_CD ");
            stb.append("     AND UPLAN.IBSUBCLASSCD = UNIT.IBSUBCLASSCD ");
            stb.append("     AND UPLAN.IBSEQ = UNIT.IBSEQ ");
            stb.append("     AND UPLAN.IBEVAL_DIV1 = T1.IBEVAL_DIV1 ");
            stb.append("     AND UPLAN.IBEVAL_DIV2 = T1.IBEVAL_DIV2 ");
            stb.append("     AND UPLAN.IBEVAL_MARK = T1.IBEVAL_MARK ");
            stb.append(" LEFT JOIN IBSUBCLASS_MST ISBM ON ISBM.IBCLASSCD || '-' || ISBM.IBPRG_COURSE || '-' || ISBM.IBCURRICULUM_CD || '-' || ISBM.IBSUBCLASSCD = T1.IB_SUBCLASSCD ");
            stb.append(" LEFT JOIN IBCLASS_MST ICM ON ICM.IBCLASSCD || '-' || ICM.IBPRG_COURSE = T1.IB_CLASSCD ");
            stb.append(" LEFT JOIN SUBCLASS_MST USBM ON USBM.CLASSCD || '-' || USBM.SCHOOL_KIND || '-' || USBM.CURRICULUM_CD || '-' || USBM.SUBCLASSCD = UNIT.CLASSCD || '-' || UNIT.SCHOOL_KIND || '-' || UNIT.CURRICULUM_CD || '-' || UNIT.SUBCLASSCD ");
            stb.append(" LEFT JOIN NAME_MST NMZ035 ON NMZ035.NAMECD1 = 'Z035' AND NMZ035.NAMECD2 = T1.IBEVAL_DIV1 ");
            stb.append(" LEFT JOIN NAME_MST NMZ036 ON NMZ036.NAMECD1 = 'Z036' AND NMZ036.NAMECD2 = T1.IBEVAL_DIV2 ");
            stb.append(" LEFT JOIN NAME_MST NMZ037 ON NMZ037.NAMECD1 = 'Z037' AND NMZ037.NAMECD2 = T1.IBEVAL_DIV2 ");
            stb.append(" LEFT JOIN NAME_MST NMZ038 ON NMZ038.NAMECD1 = 'Z038' AND NMZ038.NAMECD2 = T1.IBEVAL_DIV2 ");
            stb.append(" LEFT JOIN SEMESTER_MST UNITSM ON UNITSM.YEAR = UNIT.YEAR ");
            stb.append("     AND UNITSM.SEMESTER = UNIT.SEMESTER ");
            stb.append(" ORDER BY ");
            stb.append("   T1.LINK_NO, ");
            stb.append("   UNIT.IBSEQ, ");
            stb.append("   T1.IBEVAL_SORT, ");
            stb.append("   T1.IBEVAL_DIV1, ");
            stb.append("   T1.IBEVAL_DIV2, ");
            stb.append("   T1.IBEVAL_MARK, ");
            stb.append("   UPLAN.VIEWCD ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 69845 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _ibPrgCourse;
        final String _ibGrade;
        /** 教育課程コードを使用するか */
        final String _prgid;
        final String _cmd;

        private String _gradeCd;
        private String _gradeName3;
        private boolean _isDp2;
        private String _semesterName;

        /** 各学校における定数等設定 */
        private KNJSchoolMst _knjSchoolMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _ibPrgCourse = request.getParameter("IBPRG_COURSE");
            _ibGrade = request.getParameter("IBGRADE");
            _prgid = request.getParameter("PRGID");
            _cmd = request.getParameter("cmd");

            setIBPrgCourseGrade(db2, _year, _ibGrade);

            setSchregRegdGdat(db2, _year, _ibGrade);
        }

        public boolean isFormDP() {
            return "KNJZ068F".equals(_prgid);
        }

        private void setIBPrgCourseGrade(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean firstGrade = false;
            try {
                final String sql = " SELECT NAME1, NAME2 FROM NAME_MST WHERE NAMECD1 = 'A034' AND NAME1 = '" + _ibPrgCourse + "' AND '" + grade + "' BETWEEN NAME2 AND NAME3 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    firstGrade = StringUtils.defaultString(grade).equals(rs.getString("NAME2"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _isDp2 = isFormDP() && !firstGrade;
        }

        private void setSchregRegdGdat(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _gradeCd = "";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _gradeCd = intToString(rs.getString("GRADE_CD"), "");
                    _gradeName3 = rs.getString("GRADE_NAME3");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String intToString(final String v, final String def) {
            if (NumberUtils.isDigits(v)) {
                return String.valueOf(Integer.parseInt(v));
            }
            return StringUtils.defaultString(v, def);
        }
    }
}

// eof

