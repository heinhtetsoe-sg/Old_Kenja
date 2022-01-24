/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: ba4662fa30d93942a9db6ca32686fc8cc1034f46 $
 *
 * 作成日: 2019/03/29
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;


public class KNJE390_sien {

    private static final Log log = LogFactory.getLog(KNJE390_sien.class);

    private boolean _hasData;

    private DB2UDB _db2;
    private Vrw32alp svf;
    private Param _param;


     /**
      * @param request
      *            リクエスト
      * @param response
      *            レスポンス
      */
     public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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

//             printMain(db2, svf);
         } catch (final Exception e) {
             log.error("Exception:", e);
         } finally {
        	 if (null != _param) {
        		 _param.close();
        	 }

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

    public void printMain(final DB2UDB db2, final Vrw32alp svf, final String schregNo) {
        final StudentLoc student = getInfo(db2, schregNo);
        if (student == null) return;
        String formName = "KNJE390_C_5_1.frm";
        boolean isPage2 = false;
        final int subForm1Y1 = 1564;
        final int subForm1Y2 = 4514;
        final int RECORD1height = 1684 - 1564;
        final int RECORD2height = 1782 - 1722;
        final int RECORD3height = 2060 - 1820;
        int y = subForm1Y1;
        
        for (Iterator ite = student._printPageBlockList.iterator();ite.hasNext();) {
            svf.VrSetForm(formName, 4);
            for (Iterator its = ((List)ite.next()).iterator();its.hasNext();) {
                String blockStr = (String)its.next();
                if ("1".equals(blockStr)) {
                    //各生徒毎の出力処理
                    //ページ先頭の情報を出力
                    printTopInfo(db2, svf, student);
                    //1 現在の生活および将来の生活に関する希望
                    final List<String> h1Str = KNJ_EditKinsoku.getTokenList(student._hope_Honnin, 46, 4);
                    if (!"".equals(StringUtils.defaultString(student._hope_Honnin, ""))) {
                        for (int cnt1 = 0;cnt1 < h1Str.size();cnt1++) {
                        	if (!"".equals(StringUtils.defaultString(h1Str.get(cnt1), ""))) {
                                svf.VrsOut("LIFE_HOPE1_" + (cnt1+1), h1Str.get(cnt1));
                        	}
                        }
                    }
                    final List<String> h2Str = KNJ_EditKinsoku.getTokenList(student._hope_Hogosya, 46, 4);
                    if (!"".equals(StringUtils.defaultString(student._hope_Hogosya, ""))) {
                        for (int cnt2 = 0;cnt2 < h2Str.size();cnt2++) {
                        	if (!"".equals(StringUtils.defaultString(h2Str.get(cnt2), ""))) {
                                svf.VrsOut("LIFE_HOPE2_" + (cnt2+1), h2Str.get(cnt2));
                        	}
                        }
                    }
                } else if ("2".equals(blockStr)) {
                	if (y + RECORD1height <= subForm1Y2) {
                		//2 支援をする上での基礎となる情報(診断名、障害の状況、得意なこと、苦手なこと。日常生活の状況など)
                		svf.VrsOut("TITLE", "2 支援をする上での基礎となる情報(診断名、障害の状況、得意なこと、苦手なこと。日常生活の状況など)");
                		svf.VrEndRecord();
                		y = updateY(y, RECORD1height, subForm1Y2, "2 支援をする上での基礎となる情報(診断名、障害の状況、得意なこと、苦手なこと。日常生活の状況など)" + "TITLE");
                		y = printRecord(svf, y, RECORD2height, subForm1Y2, "BASE_INFO", KNJ_EditKinsoku.getTokenList(student._base_Info, 100, 40));
                	}
                } else if ("3".equals(blockStr)) {
                	if (y + RECORD1height <= subForm1Y2) {
                		//3 合理的配慮
                		svf.VrsOut("TITLE", "3 合理的配慮");
                		svf.VrEndRecord();
                		y = updateY(y, RECORD1height, subForm1Y2, "3 合理的配慮" + "TITLE");
                		y = printRecord(svf, y, RECORD2height, subForm1Y2, "BASE_INFO", KNJ_EditKinsoku.getTokenList(student._hairyo, 100, 20));
                	}
                } else if ("4".equals(blockStr)) {
                	if (y + RECORD1height <= subForm1Y2) {
                		//4 3年後に目指したい自立の姿
                		svf.VrsOut("TITLE", "4 3年後に目指したい自立の姿");
                		svf.VrEndRecord();
                		y = updateY(y, RECORD1height, subForm1Y2, "4 3年後に目指したい自立の姿: " + "TITLE");
                        for (Iterator itr = student._goalInfoMap.keySet().iterator();itr.hasNext();) {
                            String keyStr = (String)itr.next();
                			if (y + RECORD3height <= subForm1Y2) {
                				GoalInfo prtwk = (GoalInfo)student._goalInfoMap.get(keyStr);
                    			final int gtlen = KNJ_EditEdit.getMS932ByteLength(prtwk._goal_Title);
                    			final String gtfield = gtlen > 16 ? "2" : "1";
                        		svf.VrsOut("APPEAR_ITEM" + gtfield, prtwk._goal_Title);
                        		final List<String> rmkStr = KNJ_EditKinsoku.getTokenList(prtwk._remark, 84, 3);
                        		if (!"".equals(StringUtils.defaultString(prtwk._remark, ""))) {
                        			for (int rmkidx = 0;rmkidx < rmkStr.size();rmkidx++) {
                        				svf.VrsOutn("APPEAR", rmkidx+1, rmkStr.get(rmkidx));
                        			}
                        		}
                        		svf.VrEndRecord();
                        		y = updateY(y, RECORD3height, subForm1Y2, "4 3年後に目指したい自立の姿: " + keyStr);
                        	}
                		}
                	}
                } else if ("5".equals(blockStr)) {
                    //5 各関係機関からの具体的な支援について
                    svf.VrSetForm("KNJE390_C_6.frm", 4);
                    isPage2 = true;
                    svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(student._grade_Cd)) + "年");
//                    final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
//                    final String nfield = nlen > 24 ? "3" : (nlen > 16 ? "2" : "1");
//                    svf.VrsOut("NAME" + nfield, student._name);
//                    final int gnlen = KNJ_EditEdit.getMS932ByteLength(student._guardName);
//                    final String gnfield = gnlen > 24 ? "3" : (gnlen > 16 ? "2" : "1");
//                    if (gnlen > 0) {
//                        svf.VrsOut("GUARD_NAME" + gnfield, student._guardName);
//                    }
                    if (student._hikitugi != null) {
                        final List<String> hikitugiStr = KNJ_EditKinsoku.getTokenList(student._hikitugi, 120, 4);
                        for (int hCnt=0;hCnt < hikitugiStr.size();hCnt++) {
                    	    if (!"".equals(StringUtils.defaultString(hikitugiStr.get(hCnt), ""))) {
                    		    svf.VrsOutn("NOTE", hCnt+1, hikitugiStr.get(hCnt));
                    	    }
                        }
                    }
                    int linegrp = 0;
                    int totallinecnt = 0;
                    for (final String keyStr : student._FacilityInfoMap.keySet()) {
                        final List<FacilityInfo> prtlist0 = student._FacilityInfoMap.get(keyStr);
                        final Map<String, Integer> lineRightMaxMap = new HashMap();
                        final Map<String, Integer> recMaxMap = new HashMap();
                        calcRightMaxLine(prtlist0, lineRightMaxMap, recMaxMap);
                        final Map<String, List<FacilityInfo>> facilityInfoListMap = new LinkedMap();
                        for (final FacilityInfo prtwk : prtlist0) {
                        	if (!facilityInfoListMap.containsKey(prtwk._sprt_Facility_Cd)) {
                        		facilityInfoListMap.put(prtwk._sprt_Facility_Cd, new ArrayList());
                        	}
                        	facilityInfoListMap.get(prtwk._sprt_Facility_Cd).add(prtwk);
                        }
                        for (final Map.Entry<String, List<FacilityInfo>> e : facilityInfoListMap.entrySet()) {
                        	final List<FacilityInfo> prtlist = e.getValue();

                        	int fnPutLen = 0;
                            linegrp++;
                            final String grpStr = String.valueOf(linegrp);
                            final int rightMax = lineRightMaxMap.get(e.getKey());
                            final String sprt_Facility_Name = StringUtils.defaultString((prtlist.get(0))._sprt_Facility_Name);
                            final int idx = sprt_Facility_Name.indexOf("・");
                            boolean useName2 = false;
                            List<String> name2_1 = null;
                            List<String> name2_2 = null;
                            final int namelen;
                            if (1 <= idx && sprt_Facility_Name.length() > rightMax) {
                            	useName2 = true;
                            	name2_1 = charStringList(sprt_Facility_Name.substring(0, idx + 1));
                            	name2_2 = charStringList(sprt_Facility_Name.substring(idx + 1));
                            	namelen = Math.max(name2_1.size(), name2_2.size());
                            } else {
                            	namelen = sprt_Facility_Name.length();
                            }
                            final int linemax = Math.max(rightMax, namelen);
                            final int recMax = recMaxMap.get(e.getKey());
                            int linecnt = 0;
                            int recCnt = 0;
                            log.debug("grpStr:"+grpStr);
                            log.debug("linemax:"+linemax);

                            for (final FacilityInfo prtwk : prtlist) {
                            	final List<String> detailStr = KNJ_EditKinsoku.getTokenList(prtwk._supportDetail, 86, 2);
                                log.debug("_sprt_Facility_Name:"+sprt_Facility_Name);
                                boolean bprtNYFlg = false;
                                int locOutCnt = 0;
                                for (;linecnt < linemax;linecnt++) {
                        			svf.VrsOut("GRPCD1", grpStr);
                                	if (useName2) {
                            			if (fnPutLen < namelen) {
                            				svf.VrsOut("APPEAR_ITEM2", (name2_1.size() > fnPutLen ? name2_1.get(fnPutLen) : ""));
                            				svf.VrsOut("APPEAR_ITEM3", (name2_2.size() > fnPutLen ? name2_2.get(fnPutLen) : ""));
                            				fnPutLen++;
                            			}
                                	} else {
                                		if (!"".equals(StringUtils.defaultString(sprt_Facility_Name, ""))) {
                                			if (fnPutLen < namelen) {
                                				svf.VrsOut("APPEAR_ITEM1", (sprt_Facility_Name.length() > fnPutLen ? sprt_Facility_Name.substring(fnPutLen) : ""));
                                				fnPutLen++;
                                			}
                                		}
                                	}
                                    svf.VrsOut("GRPCD2", grpStr);
                                    svf.VrsOut("GRPCD3", grpStr);
                                    if (!"".equals(StringUtils.defaultString(prtwk._supportDetail, ""))) {
                                        if (locOutCnt < 2 && locOutCnt < detailStr.size()) {//出力は2行がMAX
                                        	svf.VrsOut("APPEAR", detailStr.get(locOutCnt));
                                        }
                                    }
                                    if (!bprtNYFlg) {
                                        svf.VrsOut("NEXT_YEAR", (prtwk._nextSupport));
                                        bprtNYFlg = true;
                                    }
                                    svf.VrEndRecord();
                                    log.info("line:"+linecnt);
                                    //次レコード遷移チェック
                                    boolean nextLoadFlg = false;
                                    //出力が無い or 2行目の出力終了
                                    if (detailStr == null || locOutCnt == 1) {
                                    	//まだレコード出力があるなら、次レコードを取ってくる
                                        if (recCnt+1 < recMax) {
                                    		nextLoadFlg = true;
                                    	}
                                    }

                                    if (nextLoadFlg) {
                                    	linecnt++;  //breakすると足されないので、自前で加算
                                    	break;
                                    }
                                    locOutCnt++;
                                }
                                recCnt++;
                            }
                            totallinecnt += linecnt;
                            //log.info(" linecnt = " + linecnt + " => " + totallinecnt);
                        }
                    }
                    for (int i = totallinecnt; i < 50; i++) {
                        svf.VrsOut("APPEAR_ITEM1", "DUMMY");
                        svf.VrAttribute("APPEAR_ITEM1", "X=10000");
                    	svf.VrEndRecord();
                    }
                }
            }
            svf.VrEndPage();
            if (!isPage2) {
            	formName = "KNJE390_C_5_2.frm";
            }
        }
        _hasData = true;
    }

	private int printRecord(final Vrw32alp svf, int y, final int recordHeight, int maxY2, final String field, final List<String> array) {
		if (null != array) {
			int max = array.size();
			while (0 <= max - 1 && null == array.get(max - 1)) {
				max -= 1;
			}
		    for (int i = 0; i < max; i++) {
		    	if (y + recordHeight <= maxY2) {
		    		svf.VrsOut(field, array.get(i));
		    		svf.VrEndRecord();
		    		y = updateY(y, recordHeight, maxY2, array.get(i));
		    	}
		    }
		}
		return y;
	}
    
    private int updateY(final int y, final int recordHeight, final int maxY2, final String data) {
		final int newY = y + recordHeight;
		//log.info(" newY = " + newY + " / y = " + y + ", recordHeight = " + recordHeight + " (maxY2 = " + maxY2 + "), data = " + data);
		return newY;
	}

	private static List<String> charStringList(final String s) {
    	final List<String> rtn = new ArrayList<String>();
    	for (final char ch : s.toCharArray()) {
    		rtn.add(String.valueOf(ch));
    	}
    	return rtn;
    }

    private void calcRightMaxLine(final List<FacilityInfo> prtlist, final Map<String, Integer> lineRightMaxMap, final Map<String, Integer> recMaxMap) {
        FacilityInfo facilityBak = null;
        String keyStr = "";
        int lineCnt = 0;
        int recCnt = 0;
        FacilityInfo prtwk = null;
        for (final Iterator<FacilityInfo> itps = prtlist.iterator(); itps.hasNext();) {
            prtwk = itps.next();
            if (facilityBak != null && !facilityBak._sprt_Facility_Cd.equals(prtwk._sprt_Facility_Cd)) {
            	keyStr = facilityBak._sprt_Facility_Cd;
            	lineRightMaxMap.put(keyStr, lineCnt);
            	recMaxMap.put(keyStr, recCnt);
            	lineCnt = 0;
            	recCnt = 0;
            }
            if (!"".equals(StringUtils.defaultString(prtwk._sprt_Facility_Name, ""))) {
            	lineCnt += 2; //1レコードにつき2行確定
            }
            facilityBak = prtwk;
            recCnt++;
        }
        if (prtwk != null) {
    	    keyStr = prtwk._sprt_Facility_Cd;
    	    lineRightMaxMap.put(keyStr, lineCnt);
        	recMaxMap.put(keyStr, recCnt);
        }
    }

    private void printTopInfo(final DB2UDB db2, final Vrw32alp svf, final StudentLoc student) {
        //先頭の記入者の行
    	final int size = student._topInfoMap.size();
    	int maxGradeVal = 0;
        for (Iterator ite = student._topInfoMap.keySet().iterator();ite.hasNext();) {
            String year = (String)ite.next();
            OldInfo prtInfo = (OldInfo)student._topInfoMap.get(year);
            int gradeVal = Integer.parseInt(prtInfo._grade_Cd);
            maxGradeVal = Math.max(maxGradeVal, gradeVal);
        }
        for (Iterator ite = student._topInfoMap.keySet().iterator();ite.hasNext();) {
            String year = (String)ite.next();
            OldInfo prtInfo = (OldInfo)student._topInfoMap.get(year);
            int gradeVal = Integer.parseInt(prtInfo._grade_Cd);
            final int pos = maxGradeVal <= 3 ? gradeVal : size - (Integer.parseInt(_param._ctrlYear) - Integer.parseInt(year));  
            svf.VrsOut("ANNUAL" + pos, gradeVal + "年次");
            svf.VrsOut("NENDO" + pos, KNJ_EditDate.gengou(db2, Integer.parseInt(prtInfo._year)) + "年度");
            final int slen = KNJ_EditEdit.getMS932ByteLength(prtInfo._staffname);
            final String sfield = slen > 26 ? "2" : "1";
            svf.VrsOut("ENTRANT_NAME" + pos + "_" + sfield, prtInfo._staffname);
        }
        //生徒情報
        svf.VrsOut("SCHOOL_NAME", _param._knjSchoolMst._schoolName1);
        svf.VrsOut("FACULTY_NAME", student._schoolkind_Name);
        svf.VrsOut("GRADE_NAME", "学年");
        if ("P".equals(student._schoolkind)) {
        	if (!NumberUtils.isDigits(student._grade_Cd) || Integer.parseInt(student._grade_Cd) <= 3) {
        		svf.VrsOut("GRADE", "1年～3年");
        	} else if (4 <= Integer.parseInt(student._grade_Cd)) {
        		svf.VrsOut("GRADE", "4年～6年");
        	}
        } else if (NumberUtils.isDigits(student._schoolKindMinGradeCd) && NumberUtils.isDigits(student._schoolKindMaxGradeCd)) {
            svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(student._schoolKindMinGradeCd)) + "年～" + String.valueOf(Integer.parseInt(student._schoolKindMaxGradeCd)) + "年");
        } else if (NumberUtils.isDigits(student._schoolKindMinGradeCd)) {
            svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(student._schoolKindMinGradeCd)) + "年");
        } else if (NumberUtils.isDigits(student._schoolKindMaxGradeCd)) {
            svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(student._schoolKindMaxGradeCd)) + "年");
        }
        final int nklen = KNJ_EditEdit.getMS932ByteLength(student._name_Kana);
        final String nkfield = nklen > 44 ? "2" : "1";
        svf.VrsOut("KANA" + nkfield, student._name_Kana);
        final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nlen > 30 ? "2" : "1";
        svf.VrsOut("NAME" + nfield, student._name);
        svf.VrsOut("SEX", student._sex);
        svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, student._birthday));
        final int alen = KNJ_EditEdit.getMS932ByteLength(student._addr);
        if (alen > 58) {
            final String[] putStr = KNJ_EditEdit.get_token(student._addr, 58, 2);
            svf.VrsOut("ADDR2_1", putStr[0]);
            svf.VrsOut("ADDR2_2", putStr[1]);
        } else {
            svf.VrsOut("ADDR1", student._addr);
        }
        svf.VrsOut("TELNO", student._telno);
        if (!StringUtils.isEmpty(student._card_Class) || !StringUtils.isEmpty(student._card_Rank)) {
        	svf.VrsOut("NOTEBOOK1", "身体障害者手帳");
        	svf.VrsOut("CLASS1", append(student._card_Class, "種") + " " + append(student._card_Rank, "級"));
        }
        if (!StringUtils.isEmpty(student._card_Name)) {
        	svf.VrsOut("NOTEBOOK2", "療育手帳");
        	svf.VrsOut("CLASS2", student._card_Name);
        }
        if (!StringUtils.isEmpty(student._card_Remark)) {
        	svf.VrsOut("NOTEBOOK3", "精神障害者保健福祉手帳");
        	svf.VrsOut("CLASS3", append(student._card_Remark, "級"));
        }
    }
    
    private String append(final String a, final String b) {
    	if (StringUtils.isBlank(a)) {
    		return "";
    	}
    	return a + StringUtils.defaultString(b);
    }

    private StudentLoc getInfo(final DB2UDB db2, final String schregNo) {
    	StudentLoc student = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            final String qkey = "getStudent";
            if (!_param._sqlQueryMap.containsKey(qkey)) {
                final String studentSql = getStudentSql();
                ps = db2.prepareStatement(studentSql);
                _param._sqlQueryMap.put(qkey, ps);
                log.info(" oldInfoSql =" + studentSql);
            } else {
                ps = (PreparedStatement) _param._sqlQueryMap.get(qkey);
            }

            ps.setString(1, schregNo);
            rs = ps.executeQuery();

            if (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String schoolkind = rs.getString("SCHOOL_KIND");
                final String schoolkind_Name = rs.getString("SCHOOLKIND_NAME");
                final String grade_Cd = rs.getString("GRADE_CD");
                final String grade_Name1 = rs.getString("GRADE_NAME1");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final String addr = rs.getString("ADDR");
                final String telno = rs.getString("TELNO");
                final String challenged_Card_Class = rs.getString("CHALLENGED_CARD_CLASS");
                final String card_Class = rs.getString("CARD_CLASS");
                final String challenged_Card_Rank = rs.getString("CHALLENGED_CARD_RANK");
                final String card_Rank = rs.getString("CARD_RANK");
                final String challenged_Card_Name = rs.getString("CHALLENGED_CARD_NAME");
                final String card_Name = rs.getString("CARD_NAME");
                final String challenged_Card_Remark = rs.getString("CHALLENGED_CARD_REMARK");
                final String card_Remark = rs.getString("CARD_REMARK");
                final String hope_Honnin = rs.getString("HOPE_HONNIN");
                final String hope_Hogosya = rs.getString("HOPE_HOGOSYA");
                final String base_Info = rs.getString("BASE_INFO");
                final String hairyo = rs.getString("HAIRYO");
                final String guardName = rs.getString("GUARD_NAME");
                final String hikitugi = rs.getString("HIKITUGI");
                final String schoolKindMinGradeCd = rs.getString("SCHOOL_KIND_MIN_GRADE_CD");
                final String schoolKindMaxGradeCd = rs.getString("SCHOOL_KIND_MAX_GRADE_CD");

                student = new StudentLoc(grade, schoolkind, schoolkind_Name, grade_Cd, grade_Name1,
                                                     schregno, name, name_Kana, sex, birthday, addr, telno, challenged_Card_Class, card_Class, challenged_Card_Rank,
                                                     card_Rank, challenged_Card_Name, card_Name, challenged_Card_Remark, card_Remark,
                                                     hope_Honnin, hope_Hogosya, base_Info, hairyo, guardName, hikitugi,
                                                     schoolKindMinGradeCd, schoolKindMaxGradeCd
                                                    );

                student.setTopInfoMap(db2);
                student.setGoalInfoMap(db2);
                student.setFacilityInfoMap(db2);
                student.calcPage();
                //retList.add(student);
            }
            
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return student;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MXSCHREG AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     MAX(SEMESTER) AS SEMESTER, ");
        stb.append("     SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO ");
        stb.append(" ), MXSCHADDR AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     MAX(T1.ISSUEDATE) AS ISSUEDATE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT T1 ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _param._ctrlYear + "' GROUP BY SCHREGNO) T3 ");
        stb.append("       ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("      AND GDAT.GRADE = T3.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T2.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND (T1.EXPIREDATE IS NULL OR T2.ENT_DATE < T1.EXPIREDATE) AND T1.ISSUEDATE <= '" + _param._ctrlDate.replace('/', '-') + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T5.SCHOOL_KIND, ");
        stb.append("   A023.ABBV1 AS SCHOOLKIND_NAME, ");
        stb.append("   A023_2_G.GRADE_CD AS SCHOOL_KIND_MIN_GRADE_CD, ");
        stb.append("   A023_3_G.GRADE_CD AS SCHOOL_KIND_MAX_GRADE_CD, ");
        stb.append("   T2.GRADE, ");
        stb.append("   T5.GRADE_CD, ");
        stb.append("   T5.GRADE_NAME1, ");
        stb.append("   T2WK.SCHREGNO, ");
        stb.append("   T3.NAME, ");
        stb.append("   T3.NAME_KANA, ");
        stb.append("   Z002.ABBV1 AS SEX, ");
        stb.append("   T3.BIRTHDAY, ");
        stb.append("   VALUE(T6.ADDR1, '') || VALUE(T6.ADDR2, '') AS ADDR, ");
        stb.append("   T6.TELNO, ");
        stb.append("   T7.CHALLENGED_CARD_CLASS, ");
        stb.append("   E031.NAME1 AS CARD_CLASS, ");
        stb.append("   T7.CHALLENGED_CARD_RANK, ");
        stb.append("   E032.NAME1 AS CARD_RANK, ");
        stb.append("   T7.CHALLENGED_CARD_NAME, ");
        stb.append("   E061.NAME1 AS CARD_NAME, ");
        stb.append("   T7.CHALLENGED_CARD_REMARK, ");
        stb.append("   E063.NAME1 AS CARD_REMARK, ");
        stb.append("   T8_01.REMARK AS HOPE_HONNIN, ");
        stb.append("   T8_02.REMARK AS HOPE_HOGOSYA, ");
        stb.append("   T9.REMARK AS BASE_INFO, ");
        stb.append("   T10.REMARK AS HAIRYO, ");
        stb.append("   T11.GUARD_NAME, ");
        stb.append("   T12_01.REMARK AS HIKITUGI ");
        stb.append(" FROM ");
        stb.append("   MXSCHREG T2WK ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ");
        stb.append("     ON T1.YEAR = T2WK.YEAR ");
        stb.append("    AND T1.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("     ON T2.YEAR = T2WK.YEAR ");
        stb.append("    AND T2.SEMESTER = T2WK.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T3 ");
        stb.append("     ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_REGD_HDAT T4 ");
        stb.append("     ON T4.YEAR = T2.YEAR ");
        stb.append("    AND T4.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T4.GRADE = T2.GRADE ");
        stb.append("    AND T4.HR_CLASS = T2.HR_CLASS ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T5 ");
        stb.append("     ON T5.YEAR = T2.YEAR ");
        stb.append("    AND T5.GRADE = T2.GRADE ");
        stb.append("   LEFT JOIN SCHREG_ADDRESS_DAT T6 ");
        stb.append("     ON T6.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T6.ISSUEDATE = (SELECT TW.ISSUEDATE FROM MXSCHADDR TW WHERE TW.SCHREGNO = T6.SCHREGNO) ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_PROFILE_MAIN_DAT T7 ");
        stb.append("     ON T7.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T7.RECORD_DATE = 'NEW' ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T8_01 ");
        stb.append("     ON T8_01.YEAR = T2WK.YEAR ");
        stb.append("    AND T8_01.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T8_01.SPRT_DIV = '01' ");
        stb.append("    AND T8_01.SPRT_SEQ = '01' ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T8_02 ");
        stb.append("     ON T8_02.YEAR = T2WK.YEAR ");
        stb.append("    AND T8_02.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T8_02.SPRT_DIV = '01' ");
        stb.append("    AND T8_02.SPRT_SEQ = '02' ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T9 ");
        stb.append("     ON T9.YEAR = T2WK.YEAR ");
        stb.append("    AND T9.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T9.SPRT_DIV = '02' ");
        stb.append("    AND T9.SPRT_SEQ = '01' ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T10 ");
        stb.append("     ON T10.YEAR = T2WK.YEAR ");
        stb.append("    AND T10.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T10.SPRT_DIV = '03' ");
        stb.append("    AND T10.SPRT_SEQ = '01' ");
        stb.append("   LEFT JOIN NAME_MST A023 ");
        stb.append("     ON A023.NAMECD1 = 'A023' ");
        stb.append("    AND A023.NAME1 = T5.SCHOOL_KIND ");
        stb.append("   LEFT JOIN NAME_MST Z002 ");
        stb.append("     ON Z002.NAMECD1 = 'Z002' ");
        stb.append("    AND Z002.NAMECD2 = T3.SEX ");
        stb.append("   LEFT JOIN NAME_MST E031 ");
        stb.append("     ON E031.NAMECD1 = 'E031' ");
        stb.append("    AND E031.NAMECD2 = T7.CHALLENGED_CARD_CLASS ");
        stb.append("   LEFT JOIN NAME_MST E032 ");
        stb.append("     ON E032.NAMECD1 = 'E032' ");
        stb.append("    AND E032.NAMECD2 = T7.CHALLENGED_CARD_RANK ");
        stb.append("   LEFT JOIN NAME_MST E061 ");
        stb.append("     ON E061.NAMECD1 = 'E061' ");
        stb.append("    AND E061.NAMECD2 = T7.CHALLENGED_CARD_NAME ");
        stb.append("   LEFT JOIN NAME_MST E063 ");
        stb.append("     ON E063.NAMECD1 = 'E063' ");
        stb.append("    AND E063.NAMECD2 = T7.CHALLENGED_CARD_REMARK ");
        stb.append("   LEFT JOIN GUARDIAN_DAT T11 ");
        stb.append("     ON T11.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T12_01 ");
        stb.append("     ON T12_01.YEAR = T2WK.YEAR ");
        stb.append("    AND T12_01.SCHREGNO = T2WK.SCHREGNO ");
        stb.append("    AND T12_01.SPRT_DIV = '05' ");
        stb.append("    AND T12_01.SPRT_SEQ = '01' ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT A023_2_G ");
        stb.append("     ON T2.YEAR = A023_2_G.YEAR ");
        stb.append("    AND A023.NAME2 = A023_2_G.GRADE ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT A023_3_G ");
        stb.append("     ON T2.YEAR = A023_3_G.YEAR ");
        stb.append("    AND VALUE(A023.NAMESPARE2, A023.NAME3) = A023_3_G.GRADE ");
        stb.append(" WHERE ");
        stb.append("    T2WK.YEAR = '" + _param._ctrlYear + "' ");
//        stb.append("    AND T2WK.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("    AND T2WK.SCHREGNO = ? ");

        return stb.toString();
    }

    private int cntValidStrIdx(final List<String> cntStr) {
        int retVal = 0;
        if (cntStr == null) return 1;  //全部nullなら、空の1行だけ出力となる。
        for (int ii = 0;ii < cntStr.size();ii++) {
            if (cntStr.get(ii) != null) {
                retVal++;
            }
        }
        return retVal;
    }


    private class StudentLoc {
        final String _grade;
        final String _schoolkind;
        final String _schoolkind_Name;
        final String _grade_Cd;
        final String _grade_Name1;
        final String _schregno;
        final String _name;
        final String _name_Kana;
        final String _sex;
        final String _birthday;
        final String _addr;
        final String _telno;
        final String _challenged_Card_Class;
        final String _card_Class;
        final String _challenged_Card_Rank;
        final String _card_Rank;
        final String _challenged_Card_Name;
        final String _card_Name;
        final String _challenged_Card_Remark;
        final String _card_Remark;
        final String _hope_Honnin;
        final String _hope_Hogosya;
        final String _base_Info;
        final String _hairyo;
        final String _guardName;
        final String _hikitugi;
        Map _topInfoMap;
        Map _goalInfoMap;
        Map<String, List<FacilityInfo>> _FacilityInfoMap;
        final List _printPageBlockList;   //ListinList
        final String _schoolKindMinGradeCd;
        final String _schoolKindMaxGradeCd;

        public StudentLoc(
                final String grade, final String schoolkind, final String schoolkind_Name, final String grade_Cd, final String grade_Name1, final String schregno, final String name, final String name_Kana, final String sex, final String birthday, final String addr, final String telno, final String challenged_Card_Class, final String card_Class, final String challenged_Card_Rank, final String card_Rank, final String challenged_Card_Name, final String card_Name, final String challenged_Card_Remark, final String card_Remark, final String hope_Honnin, final String hope_Hogosya, final String base_Info, final String hairyo, final String guardName, final String hikitugi
              , final String schoolKindMinGradeCd, final String schoolKindMaxGradeCd
                ) {
            _grade = grade;
            _schoolkind = schoolkind;
            _schoolkind_Name = schoolkind_Name;
            _grade_Cd = grade_Cd;
            _grade_Name1 = grade_Name1;
            _schregno = schregno;
            _name = name;
            _name_Kana = name_Kana;
            _sex = sex;
            _birthday = birthday;
            _addr = addr;
            _telno = telno;
            _challenged_Card_Class = challenged_Card_Class;
            _card_Class = card_Class;
            _challenged_Card_Rank = challenged_Card_Rank;
            _card_Rank = card_Rank;
            _challenged_Card_Name = challenged_Card_Name;
            _card_Name = card_Name;
            _challenged_Card_Remark = challenged_Card_Remark;
            _card_Remark = card_Remark;
            _hope_Honnin = hope_Honnin;
            _hope_Hogosya = hope_Hogosya;
            _base_Info = base_Info;
            _hairyo = hairyo;
            _guardName = guardName;
            _printPageBlockList = new ArrayList();
            _hikitugi = hikitugi;
            _schoolKindMinGradeCd = schoolKindMinGradeCd;
            _schoolKindMaxGradeCd = schoolKindMaxGradeCd;
        }

		private void setTopInfoMap(final DB2UDB db2) {
            _topInfoMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String qkey = "setTopInfo";
                if (!_param._sqlQueryMap.containsKey(qkey)) {
                    final String oldInfoSql;
                    oldInfoSql = getOldInfoSql();
                    ps = db2.prepareStatement(oldInfoSql);
                    _param._sqlQueryMap.put(qkey, ps);
                    log.debug(" oldInfoSql =" + oldInfoSql);
                } else {
                    ps = (PreparedStatement) _param._sqlQueryMap.get(qkey);
                }
                ps.setString(1, _schregno);
                ps.setString(2, _schregno);
                ps.setString(3, _schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade_Cd = rs.getString("GRADE_CD");
                    final String staffname = rs.getString("STAFFNAME");
                    OldInfo addwk = new OldInfo(year, grade_Cd, staffname);
                    _topInfoMap.put(year, addwk);
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private String getOldInfoSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH MXSCHREG AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     MAX(T1.SEMESTER) AS SEMESTER, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("       ON T2.YEAR = T1.YEAR ");
            stb.append("      AND T2.GRADE = T1.GRADE ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            //校種制限はここで実施。
            stb.append("     AND T2.SCHOOL_KIND = ( ");
            stb.append("       SELECT ");
            stb.append("         TW2.SCHOOL_KIND  ");
            stb.append("       FROM ");
            stb.append("         SCHREG_REGD_DAT TW1 ");
            stb.append("         LEFT JOIN SCHREG_REGD_GDAT TW2 ");
            stb.append("           ON TW2.YEAR =TW1.YEAR ");
            stb.append("          AND TW2.GRADE = TW1.GRADE ");
            stb.append("       WHERE ");
            stb.append("         TW1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("         AND SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("         AND TW1.SCHREGNO = ? ");
            stb.append("     ) ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.YEAR, ");
            stb.append("   T2.GRADE_CD, ");
            //stb.append("   T4.STAFFNAME ");
            stb.append("   T6.STAFFNAME AS STAFFNAME ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN MXSCHREG MX ");
            stb.append("     ON MX.YEAR = T1.YEAR ");
            stb.append("    AND MX.SEMESTER = T1.SEMESTER ");
            stb.append("    AND MX.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.GRADE = T1.GRADE ");
//            stb.append("   LEFT JOIN SCHREG_REGD_HDAT T3 ");
//            stb.append("     ON T3.YEAR = T1.YEAR ");
//            stb.append("    AND T3.SEMESTER = T1.SEMESTER ");
//            stb.append("    AND T3.GRADE = T1.GRADE ");
//            stb.append("    AND T3.HR_CLASS = T1.HR_CLASS ");
//            stb.append("   LEFT JOIN STAFF_MST T4 ");
//            stb.append("     ON T4.STAFFCD = T3.TR_CD1 ");
            stb.append("  LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T5 ");
            stb.append("    ON T5.YEAR = T1.YEAR ");
            stb.append("   AND T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("   AND T5.SPRT_DIV = '01' ");
            stb.append("   AND T5.SPRT_SEQ = '03' ");
            stb.append("  LEFT JOIN STAFF_MST T6 ON T6.STAFFCD = T5.REMARK ");
            stb.append(" WHERE ");
            int beforeyear = Integer.parseInt(_param._ctrlYear) - 2;
            //過去期間3年分を対象とする。
            stb.append("   T1.YEAR BETWEEN '" + beforeyear + "' AND '" + _param._ctrlYear + "' ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("   T2.GRADE_CD ");
            return stb.toString();
        }

        private void setGoalInfoMap(final DB2UDB db2) {
            _goalInfoMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String qkey = "setGoalInfo";
                if (!_param._sqlQueryMap.containsKey(qkey)) {
                    final String goalInfoSql = getGoalInfoSql();
                    ps = db2.prepareStatement(goalInfoSql);
                    _param._sqlQueryMap.put(qkey, ps);
                    log.debug(" goalInfoSql =" + goalInfoSql);
                } else {
                    ps = (PreparedStatement) _param._sqlQueryMap.get(qkey);
                }
                ps.setString(1, _schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String sprt_Seq = rs.getString("SPRT_SEQ");
                    final String goal_Title = rs.getString("GOAL_TITLE");
                    final String remark = rs.getString("REMARK");
                    GoalInfo addwk = new GoalInfo(sprt_Seq, goal_Title, remark);
                    _goalInfoMap.put(sprt_Seq, addwk);
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private String getGoalInfoSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" select ");
            stb.append("   T1.SPRT_SEQ, ");
            stb.append("   T1.GOAL_TITLE, ");
            stb.append("   T8.REMARK ");
            stb.append(" FROM ");
            stb.append("   CHALLENGED_GOAL_YMST T1 ");
            stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T8 ");
            stb.append("     ON T8.YEAR = T1.YEAR ");
            stb.append("    AND T8.SCHREGNO = ? ");
            stb.append("    AND T8.SPRT_DIV = '04' ");
            stb.append("    AND T8.SPRT_SEQ = T1.SPRT_SEQ ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SPRT_SEQ ");
            return stb.toString();
        }

        private void setFacilityInfoMap(final DB2UDB db2) {
            _FacilityInfoMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            List addList = new ArrayList();
            try {
                final String qkey = "setFacilityInfo";
                if (!_param._sqlQueryMap.containsKey(qkey)) {
                    final String facilityInfoSql = getFacilityInfoSql();
                    log.debug(" facilityInfoSql =" + facilityInfoSql);
                    ps = db2.prepareStatement(facilityInfoSql);
                    _param._sqlQueryMap.put(qkey, ps);
                } else {
                    ps = (PreparedStatement) _param._sqlQueryMap.get(qkey);
                }
                ps.setString(1, _schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String sprt_Facility_Grp = rs.getString("SPRT_FACILITY_GRP");
                    final String sprt_Facil_Grp_Name = rs.getString("SPRT_FACIL_GRP_NAME");
                    final String sprt_Facility_Cd = rs.getString("SPRT_FACILITY_CD");
                    final String sprt_Facility_Name = rs.getString("SPRT_FACILITY_NAME");
                    final String seq = rs.getString("SEQ");
                    final String supportDetail = rs.getString("SUPPORTDETAIL");
                    final String status = rs.getString("STATUS");
                    final String nextSupport = rs.getString("NEXTSUPPORT");
                    FacilityInfo addwk = new FacilityInfo(sprt_Facility_Grp, sprt_Facil_Grp_Name, sprt_Facility_Cd, sprt_Facility_Name, seq, supportDetail, status, nextSupport);
                    if (!_FacilityInfoMap.containsKey(sprt_Facility_Grp)) {
                        addList = new ArrayList();
                        _FacilityInfoMap.put(sprt_Facility_Grp, addList);
                    }
                    addList.add(addwk);
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        private String getFacilityInfoSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T3.SPRT_FACILITY_GRP, ");
            stb.append("   T4.SPRT_FACIL_GRP_NAME, ");
            stb.append("   T2.SPRT_FACILITY_CD, ");
            stb.append("   T2.SPRT_FACILITY_NAME, ");
            stb.append("   T8.SEQ, ");
            stb.append("   T8.REMARK AS SUPPORTDETAIL, ");
            stb.append("   T8.STATUS, ");
            stb.append("   E067.NAME1 AS NEXTSUPPORT");
            stb.append(" FROM ");
            stb.append("   SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ");
            stb.append("   LEFT JOIN CHALLENGED_SUPPORT_FACILITY_GRP_DAT T3 ");
            stb.append("     ON T3.YEAR = T1.YEAR ");
            stb.append("    AND T3.SPRT_FACILITY_GRP = T1.SPRT_FACILITY_GRP ");
            stb.append("   LEFT JOIN CHALLENGED_SUPPORT_FACILITY_YMST T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SPRT_FACILITY_CD = T3.SPRT_FACILITY_CD ");
            stb.append("   LEFT JOIN CHALLENGED_SUPPORT_FACILITY_GRP_MST T4 ");
            stb.append("     ON T4.YEAR = T1.YEAR ");
            stb.append("    AND T4.SPRT_FACILITY_GRP = T1.SPRT_FACILITY_GRP ");
            stb.append("   LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT T8 ");
            stb.append("     ON T8.YEAR = T1.YEAR ");
            stb.append("    AND T8.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T8.SPRT_FACILITY_CD = T2.SPRT_FACILITY_CD ");
            stb.append("   LEFT JOIN NAME_MST E067 ");
            stb.append("     ON E067.NAMECD1 = 'E067' ");
            stb.append("    AND E067.NAMECD2 = T8.STATUS ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("   T2.SPRT_FACILITY_CD, ");
            stb.append("   T8.SEQ ");
            return stb.toString();
        }

        private void calcPage() {
            List printBlockList = new ArrayList();
            _printPageBlockList.add(printBlockList);
            int curLinePtr = 0;  //先頭ページのカウンタ
            final int fstPageLineMax = 46;  //2番目のデータ出力を実際カウント出力して設定。ヘッダは2行とカウント。
            final int sndPageLineMax = 69;
            //final int lastPageLineMax = 35;
            int curLineMax = fstPageLineMax;
            //1番目の出力行算出
            //固定行なので、無視。
            printBlockList.add("1");
            //2番目の出力行算出
            final List<String> subcut3 = KNJ_EditKinsoku.getTokenList(_base_Info, 100, 40);
            //1ページに収まらないなら、改ページ
            int subcut3len = "".equals(StringUtils.defaultString(_base_Info, "")) ? 0 : cntValidStrIdx(subcut3);
//            //※fstPageLineMaxの設定値の経緯から、2番目のヘッダ行数は加味しない。
//            if (curLinePtr + (subcut3len) > curLineMax) {
//                printBlockList = new ArrayList();
//                _printPageBlockList.add(printBlockList);
//                curLinePtr = 0;
//                curLineMax = sndPageLineMax;
//            }
               printBlockList.add("2");
               curLinePtr += subcut3len;  //※fstPageLineMaxの設定値の経緯から、2番目のヘッダ行数は加味しない。

            //3番目の出力行算出
               final List<String> subcut4 = KNJ_EditKinsoku.getTokenList(_hairyo, 100, 20);
            //行数+ヘッダ分が1ページに収まらないなら、改ページ
            int subcut4len = "".equals(StringUtils.defaultString(_hairyo, "")) ? 0 : cntValidStrIdx(subcut4);
//            if (curLinePtr + subcut4len + 2 > curLineMax) {
//                printBlockList = new ArrayList();
//                _printPageBlockList.add(printBlockList);
//                curLinePtr = 0;
//                curLineMax = sndPageLineMax;
//            }
               printBlockList.add("3");
               curLinePtr += subcut4len + 2;
            //4番目の出力行算出
               int b4SumLine = _goalInfoMap.size() * 3;  //Map1件につき3行固定出力。出力データに依存しない。
            //行数+ヘッダ分が1ページに収まらないなら、改ページ
//            if (curLinePtr + b4SumLine + 2 > curLineMax) {
//                printBlockList = new ArrayList();
//                _printPageBlockList.add(printBlockList);
//                curLinePtr = 0;
//                curLineMax = sndPageLineMax;
//            }
               printBlockList.add("4");
               curLinePtr += b4SumLine + 2;

            //5番目の出力行算出
               //ここで強制的にページを切り替える。
            printBlockList = new ArrayList();
            _printPageBlockList.add(printBlockList);
               printBlockList.add("5");
        }

    }

    private class OldInfo {
        final String _grade_Cd;
        final String _year;
        final String _staffname;
        public OldInfo (final String year, final String grade_Cd, final String staffname)
        {
            _grade_Cd = grade_Cd;
            _year = year;
            _staffname = staffname;
        }
    }

    private class GoalInfo {
        final String _sprt_Seq;
        final String _goal_Title;
        final String _remark;
        public GoalInfo (final String sprt_Seq, final String goal_Title, final String remark)
        {
            _sprt_Seq = sprt_Seq;
            _goal_Title = goal_Title;
            _remark = remark;
        }
    }
    private class FacilityInfo {
        final String _sprt_Facility_Grp;
        final String _sprt_Facil_Grp_Name;
        final String _sprt_Facility_Cd;
        final String _sprt_Facility_Name;
        final String _seq;
        final String _supportDetail;
        final String _status;
        final String _nextSupport;
        public FacilityInfo (final String sprt_Facility_Grp, final String sprt_Facil_Grp_Name, final String sprt_Facility_Cd, final String sprt_Facility_Name, final String seq, final String supportDetail, final String status, final String nextSupport)
        {
            _sprt_Facility_Grp = sprt_Facility_Grp;
            _sprt_Facil_Grp_Name = sprt_Facil_Grp_Name;
            _sprt_Facility_Cd = sprt_Facility_Cd;
            _sprt_Facility_Name = sprt_Facility_Name;
            _seq = seq;
            _supportDetail = supportDetail;
            _status = status;
            _nextSupport = nextSupport;
        }
        private int calcMaxLine() {
            int retVal = 0;
            final List<String> cutwk = KNJ_EditKinsoku.getTokenList(_supportDetail, 100, 2);
            if (!"".equals(StringUtils.defaultString(_sprt_Facility_Name, ""))) {
                if (!"".equals(StringUtils.defaultString(_supportDetail, ""))) {
                    retVal = Math.max(_sprt_Facility_Name.length(), cntValidStrIdx(cutwk));
                } else {
                    retVal = _sprt_Facility_Name.length();
                }
            }
            return retVal;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74064 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

     /** パラメータクラス */
     private class Param {
         final String[] _categorySelected;
         final String _ctrlYear;        //_year
         final String _ctrlSemester;    //_gakki
         final String _ctrlDate;
         KNJSchoolMst _knjSchoolMst;
         final Map _sqlQueryMap;

 //      final String _schoolName;

         Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
             _categorySelected = request.getParameterValues("category_selected");
             _ctrlYear = request.getParameter("YEAR");
             _ctrlSemester = request.getParameter("GAKKI");
             _ctrlDate = request.getParameter("CTRL_DATE");
             _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
             _sqlQueryMap = new HashMap();
 //          _schoolName = getSchoolName(db2);
         }
         
         public void close() {
        	 for (final Iterator it = _sqlQueryMap.values().iterator(); it.hasNext();) {
        		 final PreparedStatement ps = (PreparedStatement) it.next();
        		 DbUtils.closeQuietly(ps);
        		 it.remove();
        	 }
         }

//        private String getSchoolName(final DB2UDB db2) {
//            final StringBuffer sql = new StringBuffer();
//            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
//            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '103' ");
//            log.debug("certif_school_dat sql = " + sql.toString());
//
//            String retSchoolName = "";
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(sql.toString());
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    retSchoolName = rs.getString("SCHOOL_NAME");
//                }
//            } catch (SQLException ex) {
//                log.error("certif_school_dat exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return retSchoolName;
//        }
    }
}

// eof
