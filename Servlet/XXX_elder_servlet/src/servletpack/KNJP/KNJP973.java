/*
 * $Id: 13900a129dcbf758022041682aaa2b09fa532eb2 $
 *
 * 作成日: 2015/04/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP973 {

    private static final Log log = LogFactory.getLog(KNJP973.class);

    private static final String GRADE_ALL = "99";

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

            printMain(db2, svf);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getList(db2);

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {

            final Map hrClassMap = (Map) iterator.next();
            List<Map> pageMapList = divideMap(hrClassMap, 50); //クラス単位マップを50人区切りのサブマップに分割する・・

            /*クラス単位に保持する変数*/

            //収入科目単位の合計
            Map<String, Integer> schCountMap = new LinkedHashMap();
            Map<String, Integer> schTotalMap = new LinkedHashMap();
            //合計(収入計、支出計等)
            Map<Integer, Integer> countMap  = new LinkedHashMap();
            Map<Integer, Integer> totalMap  = new LinkedHashMap();

            int totalHasuu = 0;

            //50人区切り単位に処理
            for (Map pageMap : pageMapList) {
	            svf.VrSetForm("KNJP973.frm", 4);
	            svf.VrsOut("TITLE", KNJ_EditDate.getAutoFormatYearNen(db2, _param._ctrlYear+"-04-01") + "度 収支総括");

	            if (pageMap.size() == 0) continue;

	            //生徒の年組番/氏名を出力する
	            int linecnt = 1;
	            for (Iterator its = pageMap.keySet().iterator(); its.hasNext();) {
	                final String kstr = (String)its.next();
	                Student stwk = (Student)pageMap.get(kstr);
	                if (stwk == null) continue;

	                //年組
	                svf.VrsOut("HR_NAME1", stwk._hrName);
	                //日付
	                svf.VrsOut("DATE", _param.getNow(db2));
	                //年組番号
	                svf.VrsOutn("HR_NAME", linecnt, stwk._attendNo);
	                //氏名
	                int namelen = KNJ_EditEdit.getMS932ByteLength(stwk._schName);
	                String namefield = namelen > 30 ? "3" : (namelen > 20 ? "2" : "1");
	                svf.VrsOutn("NAME" + namefield, linecnt, stwk._schName);
	                linecnt++;
	            }

	            int grpCd = 0;
	            final int subTtlFldSize = 6;

	            //収入科目単位に処理
	            String[] subttlstr = {"納入額", "支出額", "精算額"};
	            for (Iterator itll = _param._lList.iterator(); itll.hasNext();) {
	                Lmst lmwk = (Lmst)itll.next();
	                if (lmwk == null) continue;
	                //納入/支出/精算と3回生徒のデータをなめる
	                for (int ii = 0;ii < 3;ii++) {
	                    svf.VrsOut("GRPCD", String.valueOf(grpCd));

	                    final int strCnt = lmwk._lName.length();
	                    final String setlName = getCreateString(subTtlFldSize, ii + 1, strCnt, lmwk._lName);
	                    svf.VrsOut("ITEM0", setlName); // 科目名

	                    svf.VrsOut("ITEM1", subttlstr[ii]);
	                    //リスト内の生徒単位に処理
	                    int linewk = 1;
                        int tmpSchCount = schCountMap.get(lmwk._lCd + ii) != null ? schCountMap.get(lmwk._lCd + ii) : 0 ;
                        int tmpSchTotal = schTotalMap.get(lmwk._lCd + ii) != null ? schTotalMap.get(lmwk._lCd + ii) : 0 ;
	                    for (Iterator its = pageMap.keySet().iterator(); its.hasNext();) {
	                        final String kstr = (String)its.next();
	                        Student stwk = (Student)pageMap.get(kstr);
	                        if (stwk == null) continue;
	                        balanceSheet bswk = (balanceSheet)stwk._balanceMap.get(lmwk._lCd);
	                        if (bswk == null) continue;
	                        if (ii == 0) {
	                            //収入
	                            svf.VrsOutn("PRICE", linewk, String.valueOf(bswk._income));
	                            tmpSchCount += (0 != bswk._income) ? 1: 0;
	                            tmpSchTotal += bswk._income;
	                            svf.VrsOutn("PRICE", 51, String.valueOf(tmpSchCount)); // 人数
	                            svf.VrsOutn("PRICE", 52, String.valueOf(tmpSchTotal)); // 合計
	                        } else if (ii == 1) {
	                            //支出
	                            svf.VrsOutn("PRICE", linewk, String.valueOf(bswk._outgo));
	                            tmpSchCount += (0 != bswk._outgo) ? 1: 0;
	                            tmpSchTotal += bswk._outgo;
	                            svf.VrsOutn("PRICE", 51, String.valueOf(tmpSchCount)); // 人数
	                            svf.VrsOutn("PRICE", 52, String.valueOf(tmpSchTotal)); // 合計
	                        } else if (ii == 2) {
	                            //差額
	                            svf.VrsOutn("PRICE", linewk, String.valueOf(bswk._difference));
	                            tmpSchCount += (0 != bswk._difference) ? 1: 0;
	                            tmpSchTotal += bswk._difference;
	                            svf.VrsOutn("PRICE", 51, String.valueOf(tmpSchCount)); // 人数
	                            svf.VrsOutn("PRICE", 52, String.valueOf(tmpSchTotal)); // 合計
	                        }
	                        linewk++;
	                    }
                        schCountMap.put(lmwk._lCd + ii, tmpSchCount);
                        schTotalMap.put(lmwk._lCd + ii, tmpSchTotal);
	                    if (ii == 1) {
	                        //端数
	                        final String hasuu = (String) _param._hasuuMap.get(lmwk._lCd);
	                        final int setHasuu = hasuu == null ? 0: Integer.parseInt(hasuu);
	                        if (!_param._husuuPrintBooleanMap.containsKey(lmwk._lCd)) { // 一度印字した端数は印字しない
	                            svf.VrsOutn("PRICE", 53, String.valueOf(setHasuu));
	                            totalHasuu += setHasuu;
	                            if (0 < setHasuu) {
	                                _param._husuuPrintBooleanMap.put(lmwk._lCd, "1");
	                            }
	                        } else {
	                            svf.VrsOutn("PRICE", 53, "0");
	                        }
	                    }
	                    //収入科目単位の出力が終われば、次レコードへ
	                    svf.VrEndRecord();
	                }
	                grpCd++;
	            }
	            //合計(収入計、支出計、繰越、精算、データ入力金)を出力
	            if ("1".equals(_param._useBenefit)) {
	                //納入額総計/支出額総計/繰越総額/その他返金額/精算総額/データ入力金と6回生徒のデータをなめる
	                String[] totalttlstr = {"納入額総計", "支出額総計", "繰越総額", "端数返金額", "その他返金額", "精算総額", "データ入力金額"};
	                for (int ii = 0;ii < totalttlstr.length;ii++) {
	                    int itemlen = KNJ_EditEdit.getMS932ByteLength(totalttlstr[ii]);
	                    String itemfield = itemlen > 16 ? "6" : itemlen > 12 ? "5" : "4";
	                    svf.VrsOut("ITEM"+itemfield, totalttlstr[ii]);
	                    //リスト内の生徒単位に処理
	                    int linewk = 1;
                        int tmpCount = countMap.get(ii) != null ? countMap.get(ii) : 0 ;
                        int tmpTotal = totalMap.get(ii) != null ? totalMap.get(ii) : 0 ;
	                    for (Iterator itw = pageMap.keySet().iterator(); itw.hasNext();) {
	                        final String wstr = (String)itw.next();
	                        Student stwk = (Student)pageMap.get(wstr);
	                        if (stwk == null) continue;

	                        if (ii == 0) {
	                            //納入額総計
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._totalBalance._income));
	                            tmpCount += (0 != stwk._totalBalance._income) ? 1: 0;
	                            tmpTotal += stwk._totalBalance._income;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 1) {
	                            //支出額総計
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._totalBalance._outgo));
	                            tmpCount += (0 != stwk._totalBalance._outgo) ? 1: 0;
	                            tmpTotal += stwk._totalBalance._outgo;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                            svf.VrsOutn("PRICE2", 53, String.valueOf(totalHasuu)); // 端数
	                        } else if (ii == 2) {
	                            //繰越総額
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._carryOver));
	                            tmpCount += (0 != stwk._carryOver) ? 1: 0;
	                            tmpTotal += stwk._carryOver;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 3) {
	                            //端数返金額
	                            int hasuuHenkin = 0;
	                            if (_param._hasuuHenkinMap.get(stwk._schregNo) != null) {
	                                hasuuHenkin = Integer.parseInt((String) _param._hasuuHenkinMap.get(stwk._schregNo));
	                            }
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(hasuuHenkin));
	                            tmpCount += (0 != hasuuHenkin) ? 1: 0;
	                            tmpTotal += hasuuHenkin;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 4) {
	                            //その他返金額
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._kyufuMoney));
	                            tmpCount += (0 != stwk._kyufuMoney) ? 1: 0;
	                            tmpTotal += stwk._kyufuMoney;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 5) {
	                            //精算総額
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._totalBalance._difference));
	                            tmpCount += (0 != stwk._totalBalance._difference) ? 1: 0;
	                            tmpTotal += stwk._totalBalance._difference;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 6) {
	                            //データ入力金額（返金をここで引く）
	                            final String hen = (String) _param._henkinDataMap.get(stwk._schregNo);
	                            final int henkin = (hen == null) ? 0: Integer.parseInt(hen);
	                            final long finMoney = stwk._totalBalance._difference - henkin;
	                            final String setPrice = (finMoney <= 0) ? String.valueOf(finMoney): String.valueOf(finMoney - Integer.parseInt(_param._fee));
	                            tmpCount += (0 != Integer.parseInt(setPrice)) ? 1: 0;
	                            tmpTotal += Integer.parseInt(setPrice);
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                            if (finMoney == 0) {
	                                svf.VrsOutn("PRICE2_NAME", linewk, "返金済");
	                            } else {
	                                svf.VrsOutn("PRICE2", linewk, setPrice);
	                            }
	                        }
	                        linewk++;
	                    }
                        countMap.put(ii, tmpCount);
                        totalMap.put(ii, tmpTotal);
	                    //収入科目単位の出力が終われば、次レコードへ
	                    svf.VrEndRecord();
	                }
	            } else {
	                //収入/支出/繰越総額/精算/データ入力金と5回生徒のデータをなめる
	                String[] totalttlstr = {"納入額総計", "支出額総計", "繰越総額", "端数返金額", "精算総額", "データ入力金額"};
	                for (int ii = 0;ii < totalttlstr.length;ii++) {
	                    int itemlen = KNJ_EditEdit.getMS932ByteLength(totalttlstr[ii]);
	                    String itemfield = itemlen > 16 ? "6" : itemlen > 12 ? "5" : "4";
	                    svf.VrsOut("ITEM"+itemfield, totalttlstr[ii]);
	                    //リスト内の生徒単位に処理
	                    int linewk = 1;
                        int tmpCount = countMap.get(ii) != null ? countMap.get(ii) : 0 ;
                        int tmpTotal = totalMap.get(ii) != null ? totalMap.get(ii) : 0 ;
	                    for (Iterator itw = pageMap.keySet().iterator(); itw.hasNext();) {
	                        final String wstr = (String)itw.next();
	                        Student stwk = (Student)pageMap.get(wstr);
	                        if (stwk == null) continue;

	                        if (ii == 0) {
	                            //納入額総計
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._totalBalance._income));
	                            tmpCount += (0 != stwk._totalBalance._income) ? 1: 0;
	                            tmpTotal += stwk._totalBalance._income;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 1) {
	                            //支出額総計
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._totalBalance._outgo));
	                            tmpCount += (0 != stwk._totalBalance._outgo) ? 1: 0;
	                            tmpTotal += stwk._totalBalance._outgo;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                            svf.VrsOutn("PRICE2", 53, String.valueOf(totalHasuu)); // 端数
	                        } else if (ii == 2) {
	                            //繰越総額
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._carryOver));
	                            tmpCount += (0 != stwk._carryOver) ? 1: 0;
	                            tmpTotal += stwk._carryOver;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 3) {
	                            //端数返金額
	                            int hasuuHenkin = 0;
	                            if (_param._hasuuHenkinMap.get(stwk._schregNo) != null) {
	                                hasuuHenkin = Integer.parseInt((String) _param._hasuuHenkinMap.get(stwk._schregNo));
	                            }
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(hasuuHenkin));
	                            tmpCount += (0 != hasuuHenkin) ? 1: 0;
	                            tmpTotal += hasuuHenkin;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 4) {
	                            //精算総額
	                            svf.VrsOutn("PRICE2", linewk, String.valueOf(stwk._totalBalance._difference));
	                            tmpCount += (0 != stwk._totalBalance._difference) ? 1: 0;
	                            tmpTotal += stwk._totalBalance._difference;
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                        } else if (ii == 5) {
	                            //データ入力金額（返金をここで引く）
	                            final String hen = (String) _param._henkinDataMap.get(stwk._schregNo);
	                            final int henkin = (hen == null) ? 0: Integer.parseInt(hen);
	                            final long finMoney = stwk._totalBalance._difference - henkin;
	                            final String setPrice = (finMoney <= 0) ? String.valueOf(finMoney): String.valueOf(finMoney - Integer.parseInt(_param._fee));
	                            tmpCount += (0 != Integer.parseInt(setPrice)) ? 1: 0;
	                            tmpTotal += Integer.parseInt(setPrice);
	                            svf.VrsOutn("PRICE2", 51, String.valueOf(tmpCount)); // 人数
	                            svf.VrsOutn("PRICE2", 52, String.valueOf(tmpTotal)); // 合計
	                            if (finMoney == 0) {
	                                svf.VrsOutn("PRICE2_NAME", linewk, "返金済");
	                            } else {
	                                svf.VrsOutn("PRICE2", linewk, setPrice);
	                            }
	                        }
	                        linewk++;
	                    }
                        countMap.put(ii, tmpCount);
                        totalMap.put(ii, tmpTotal);
	                    //収入科目単位の出力が終われば、次レコードへ
	                    svf.VrEndRecord();
	                }
	            }
	            //次リスト前にEndPageを発行
	            svf.VrEndPage();
	            _hasData = true;
	        }
        }
    }

    private static List divideMap (Map map, int divideNum) {
    	List dividedMapList = new ArrayList();
    	Map subMap = new LinkedHashMap();
    	int count = 0;
    	for (Iterator its = map.keySet().iterator(); its.hasNext();) {
    		if (count >= divideNum) {
    			dividedMapList.add(subMap);
    			subMap = new LinkedHashMap();
    			count=0;
    		}

    		final String kstr = (String)its.next();
            Object obj = map.get(kstr);

    		subMap.put(kstr, obj);

    		count++;
    	}
    	//余りをlistに追加
    	dividedMapList.add(subMap);

    	return dividedMapList;
    }

    /**
     * ３枠で名称を分割して表示する
     * @param maxZen  ：１枠内の最大文字数（全角）
     * @param posiNum ：枠順
     * @param strCnt  ：名称の文字数
     * @param lName   ：名称
     * @return 分割した文字
     */
    private String getCreateString(final int maxZen, final int posiNum, final int strCnt, final String lName) {
        String retStr = "";

        if (1 > maxZen || 1 > posiNum || 3 < posiNum || 1 > strCnt || 1 > lName.length() || null == lName) {
            return retStr;
        }

        //１枠に収まる時、中央に印字する
        if (maxZen >= strCnt) {
            if (2 == posiNum) {
                final int blIdx = (maxZen - strCnt) / 2;
                final String blank  = StringUtils.repeat("　", blIdx);
                retStr = blank + lName;
            }

        // ３枠内に収まる時
        } else if (maxZen * 3 >= strCnt) {
            String blank = "";
            final int subZCnt         = strCnt - maxZen;
            final double subZCntHalf = (double) subZCnt / 2;
            final int endIdx1         = (int) Math.ceil(subZCntHalf);
            final int endIdx2         = strCnt - endIdx1 + 1;
            final int blnakIdx        = maxZen - endIdx1;
            final int startIdx3       = maxZen + endIdx1;

            if (1 == posiNum) {
                blank  = StringUtils.repeat("　", blnakIdx);// X回繰り返し
                retStr = blank + lName.substring(0, endIdx1);
            } else if (2 == posiNum) {
                retStr = lName.substring(endIdx1, endIdx2);
            } else if (3 == posiNum) {
                retStr = lName.substring(startIdx3);
            }

        // ３枠(maxZen * 3文字)を超える時は、(maxZen * 3)文字まで表示
        } else {
            final int startIdx = (1 == posiNum) ?      0: (2 == posiNum) ?     maxZen: maxZen * 2;
            final int endIdx   = (1 == posiNum) ? maxZen: (2 == posiNum) ? maxZen * 2: maxZen * 3;
            retStr = lName.substring(startIdx, endIdx);
        }

        return retStr;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        Map addMap = new LinkedHashMap();
        retList.add(addMap);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql1();
            log.debug(" mainSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String befHrClass = "";

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String schName = rs.getString("SCH_NAME");
                final String staffName = rs.getString("STAFFNAME");
                final String incomeLCd = rs.getString("INCOME_L_CD");
                final long incomeMoney = rs.getLong("INCOME_MONEY");
                final long outgoMoney = rs.getLong("OUTGO_MONEY");
                final long kyufuMoney = rs.getLong("KYUFU_MONEY");

                if (!befHrClass.equals("") && !befHrClass.equals(hrClass)) {
                    addMap = new LinkedHashMap();
                    retList.add(addMap);
                }
                final Student stuwk;
                if (addMap.get(schregNo) == null) {
                    stuwk = new Student(schregNo, grade, hrClass, hrName, attendNo, schName, staffName, incomeLCd, incomeMoney, outgoMoney);
                    if (_param._carryOverMap.get(schregNo) != null) {
                        if (!"".equals((String)_param._carryOverMap.get(schregNo))) {
                            long cowk = Long.parseLong((String)_param._carryOverMap.get(schregNo));
                            stuwk.setCarryOver(cowk);
                        }
                    }
                    if ("1".equals(_param._useBenefit)) {
                        stuwk._kyufuMoney = kyufuMoney;
                        stuwk._totalBalance._difference += kyufuMoney; // 給付金を精算総額に足しこむ
                    }
                    // 端数返金を精算総額に足しこむ
                    if (_param._hasuuHenkinMap.get(schregNo) != null) {
                        final int hasuuHenkin = Integer.parseInt((String) _param._hasuuHenkinMap.get(schregNo));
                        stuwk._totalBalance._difference += hasuuHenkin;
                    }
                    addMap.put(schregNo, stuwk);
                } else {
                    stuwk = (Student)addMap.get(schregNo);
                    stuwk.addBalanceMap(incomeLCd, incomeMoney, outgoMoney);
                }
                befHrClass = hrClass;
            }

        } catch (SQLException ex) {
            log.error("mainSql Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql1() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH MAX_SEME_REGD AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         max(SEMESTER) MAX_SEM ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO ");
        stb.append(" ), INCOMETBL AS ( ");
        stb.append("     SELECT ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGDH.HR_NAME, ");
        stb.append("         REGD.ATTENDNO, ");
        stb.append("         BASE.NAME AS SCH_NAME, ");
        stb.append("         STAFF.STAFFNAME, ");
        stb.append("         INCOME.INCOME_L_CD, ");
        stb.append("         SUM(VALUE(INCOME_SCH.INCOME_MONEY, 0)) AS INCOME_MONEY ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("              AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("              AND REGD.GRADE = REGDH.GRADE ");
        stb.append("              AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("         LEFT JOIN LEVY_REQUEST_INCOME_DAT INCOME ON REGD.YEAR = INCOME.YEAR ");
        stb.append("              AND INCOME.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("              AND INCOME.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("              AND VALUE(INCOME.INCOME_APPROVAL, '0') = '1' ");
        stb.append("              AND VALUE(INCOME.INCOME_CANCEL, '0') = '0' ");
        stb.append("         LEFT JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON INCOME.YEAR = INCOME_SCH.YEAR ");
        stb.append("              AND INCOME.SCHOOLCD    = INCOME_SCH.SCHOOLCD ");
        stb.append("              AND INCOME.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ");
        stb.append("              AND INCOME.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ");
        stb.append("              AND INCOME.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ");
        stb.append("              AND INCOME.REQUEST_NO  = INCOME_SCH.REQUEST_NO ");
        stb.append("              AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ");
        stb.append("         LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
        stb.append("         INNER JOIN MAX_SEME_REGD SEME ON SEME.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                      AND SEME.MAX_SEM  = REGD.SEMESTER ");
        stb.append("     WHERE ");
        stb.append("             REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("         AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + _param._gradeHrInState);
        stb.append("     GROUP BY ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGDH.HR_NAME, ");
        stb.append("         REGD.ATTENDNO, ");
        stb.append("         BASE.NAME, ");
        stb.append("         STAFF.STAFFNAME, ");
        stb.append("         INCOME.INCOME_L_CD ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        stb.append(" ), OUTGOTBL AS ( ");
        stb.append("     SELECT ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGDH.HR_NAME, ");
        stb.append("         REGD.ATTENDNO, ");
        stb.append("         BASE.NAME AS SCH_NAME, ");
        stb.append("         STAFF.STAFFNAME, ");
        stb.append("         OUTGO.INCOME_L_CD, ");
        stb.append("         SUM(VALUE(OUTGO_SCH.OUTGO_MONEY, 0)) AS OUTGO_MONEY ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("              AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("              AND REGD.GRADE = REGDH.GRADE ");
        stb.append("              AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("         LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
        stb.append("         LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ON OUTGO.YEAR = REGD.YEAR ");
        stb.append("              AND OUTGO.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("              AND OUTGO.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("              AND VALUE(OUTGO.OUTGO_APPROVAL, '0') = '1' ");
        stb.append("              AND VALUE(OUTGO.OUTGO_CANCEL, '0') = '0' ");
        stb.append("         LEFT JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ON OUTGO_SCH.YEAR = OUTGO.YEAR ");
        stb.append("               AND OUTGO_SCH.SCHOOLCD    = OUTGO.SCHOOLCD ");
        stb.append("               AND OUTGO_SCH.SCHOOL_KIND = OUTGO.SCHOOL_KIND ");
        stb.append("               AND OUTGO_SCH.OUTGO_L_CD  = OUTGO.OUTGO_L_CD ");
        stb.append("               AND OUTGO_SCH.OUTGO_M_CD  = OUTGO.OUTGO_M_CD ");
        stb.append("               AND OUTGO_SCH.REQUEST_NO  = OUTGO.REQUEST_NO ");
        stb.append("               AND OUTGO_SCH.SCHREGNO = REGD.SCHREGNO ");
        stb.append("         INNER JOIN MAX_SEME_REGD SEME ON SEME.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                      AND SEME.MAX_SEM  = REGD.SEMESTER ");
        stb.append("     WHERE ");
        stb.append("               REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("           AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + _param._gradeHrInState);
        stb.append("           AND OUTGO.OUTGO_L_CD <> '99' "); // 返金伝票は除く（最後に計算する）
        stb.append("     GROUP BY ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGDH.HR_NAME, ");
        stb.append("         REGD.ATTENDNO, ");
        stb.append("         BASE.NAME, ");
        stb.append("         STAFF.STAFFNAME, ");
        stb.append("         OUTGO.INCOME_L_CD ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        stb.append(" ), KYUFU_DATA as ( ");
        stb.append(" SELECT ");
        stb.append("     OUTGO_SCH.SCHREGNO, ");
        stb.append("     sum(VALUE(OUTGO_SCH.OUTGO_MONEY, 0)) AS KYUFU_MONEY ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_DAT T1 ON T1.YEAR        = OUTGO_SCH.YEAR ");
        stb.append("                                         AND T1.SCHOOLCD    = OUTGO_SCH.SCHOOLCD ");
        stb.append("                                         AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ");
        stb.append("                                         AND T1.OUTGO_L_CD  = OUTGO_SCH.OUTGO_L_CD ");
        stb.append("                                         AND T1.OUTGO_M_CD  = OUTGO_SCH.OUTGO_M_CD ");
        stb.append("                                         AND T1.REQUEST_NO  = OUTGO_SCH.REQUEST_NO ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR        = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND VALUE(T1.OUTGO_CANCEL, '0')   = '0' ");
        stb.append("     AND T1.INCOME_L_CD = '98' "); // 給付
        stb.append(" GROUP BY ");
        stb.append("     OUTGO_SCH.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.SCHREGNO ELSE T2.SCHREGNO END AS SCHREGNO, ");
        stb.append("     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.GRADE ELSE T2.GRADE END AS GRADE, ");
        stb.append("     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.HR_CLASS ELSE T2.HR_CLASS END AS HR_CLASS, ");
        stb.append("     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.HR_NAME ELSE T2.HR_NAME END AS HR_NAME, ");
        stb.append("     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.ATTENDNO ELSE T2.ATTENDNO END AS ATTENDNO, ");
        stb.append("     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.SCH_NAME ELSE T2.SCH_NAME END AS SCH_NAME, ");
        stb.append("     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.STAFFNAME ELSE T2.STAFFNAME END AS STAFFNAME, ");
        stb.append("     CASE WHEN T1.INCOME_L_CD IS NOT NULL THEN T1.INCOME_L_CD ELSE T2.INCOME_L_CD END AS INCOME_L_CD, ");
        stb.append("     T1.INCOME_MONEY, ");
        stb.append("     T2.OUTGO_MONEY, ");
        stb.append("     T3.KYUFU_MONEY ");
        stb.append(" FROM ");
        stb.append("     INCOMETBL T1 ");
        stb.append("     LEFT JOIN OUTGOTBL T2 ");
        stb.append("          ON T2.SCHREGNO    = T1.SCHREGNO ");
        stb.append("         AND T2.GRADE       = T1.GRADE ");
        stb.append("         AND T2.HR_CLASS    = T1.HR_CLASS ");
        stb.append("         AND T2.HR_NAME     = T1.HR_NAME ");
        stb.append("         AND T2.ATTENDNO    = T1.ATTENDNO ");
        stb.append("         AND T2.INCOME_L_CD = T1.INCOME_L_CD ");
        stb.append("     LEFT JOIN KYUFU_DATA T3 ON T1.SCHREGNO = T3.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO ");

        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _schName;
        final String _staffName;
        final Map _balanceMap;
        balanceSheet _totalBalance;
        long _carryOver;
        long _kyufuMoney;
        public Student(
            final String schregNo,
            final String grade,
            final String hrClass,
            final String hrName,
            final String attendNo,
            final String schName,
            final String staffName,
            final String incomeLCd,
            final long incomeMoney,
            final long outgoMoney
        ) {
            _schregNo = schregNo;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _schName = schName;
            _staffName = staffName;
            _balanceMap = new HashMap();
            _totalBalance = new balanceSheet(0,0);
            _carryOver = 0;
            _kyufuMoney = 0;
            addBalanceMap(incomeLCd, incomeMoney, outgoMoney);
        }
        private void addBalanceMap(final String incomeLCd, final long income, final long outgo) {
            balanceSheet addwk = new balanceSheet(income, outgo);
            _balanceMap.put(incomeLCd, addwk);
            _totalBalance.totaladd(addwk._income, addwk._outgo);
        }
        private void setCarryOver(final long carryOver) {
            _carryOver = carryOver;
            _totalBalance._difference -= carryOver; //繰越金はここで引く。
        }
    }

    private class balanceSheet {
        long _income;
        long _outgo;
        long _difference;
        public balanceSheet(
                final long income,
                final long outgo
        ) {
            _income = income;
            _outgo = outgo;
            _difference = income - outgo;
        }
        private void totaladd(final long income, final long outgo) {
            _income += income;
            _outgo += outgo;
            _difference += income - outgo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72735 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _grade;
        private final String _schoolcd;
        private final String _schoolKind;
        private final String[] _gradeHrSelected;
        private final String _gradeHrInState;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;
        private final List _lList;
        private final Map _carryOverMap;
        private final Map _henkinDataMap;
        private final Map _hasuuHenkinMap;
        private final String _useBenefit;
        private final String _fee;

        private final Map _hasuuMap;
        private final Map _husuuPrintBooleanMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameter("GRADE");
            _schoolcd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            //リストToリスト
            _gradeHrSelected = request.getParameterValues("CATEGORY_SELECTED");
            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _gradeHrSelected.length; ia++) {
                if (_gradeHrSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                sbx.append(_gradeHrSelected[ia]);
                sbx.append("'");
            }
            sbx.append(")");
            _gradeHrInState = sbx.toString();

            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2, _ctrlYear);
            _lList = getLList(db2, _ctrlYear, _grade);
            _carryOverMap = getCarryOverMap(db2, _ctrlYear, _grade);
            _henkinDataMap = getHenkinMap(db2, _ctrlYear, _grade);
            _hasuuHenkinMap = getHasuuHenkinMap(db2, _ctrlYear, _grade);
            _useBenefit = request.getParameter("useBenefit");
            _hasuuMap = getHasuu(db2);
            _husuuPrintBooleanMap = new TreeMap();
            _fee = getTesuryo(db2);
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

        /** 手数料 */
        private String getTesuryo(final DB2UDB db2) {
            String schoolKindFee = "";
            String allFee = "0";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     BANK_TRANSFER_FEE ");
                stb.append(" FROM ");
                stb.append("     COLLECT_SCHOOL_BANK_MST ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolcd + "' ");
                stb.append("     AND SCHOOL_KIND IN ('" + _schoolKind + "', '99') ");
                stb.append("     AND YEAR        = '" + _ctrlYear + "' ");
                stb.append("     AND FORMAT_DIV  = '2' ");   // 1:引落 2:返金
                stb.append("     AND SEQ         = '001' "); // 固定
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("99".equals(rs.getString("SCHOOL_KIND"))) {
                        allFee = StringUtils.defaultString(rs.getString("BANK_TRANSFER_FEE"));
                    } else {
                        schoolKindFee = StringUtils.defaultString(rs.getString("BANK_TRANSFER_FEE"));
                    }
                }
            } catch (SQLException ex) {
                log.debug("getTesuryo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return !"".equals(schoolKindFee) ? schoolKindFee : allFee;
        }

        private List getLList(final DB2UDB db2, final String year, final String grade) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.INCOME_L_CD, ");
                stb.append("     L1.LEVY_L_NAME ");
                stb.append(" FROM ");
                stb.append("     LEVY_REQUEST_INCOME_DAT T1 ");
                stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
                stb.append("          AND T1.SCHOOLCD = L1.SCHOOLCD ");
                stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
                stb.append("          AND T1.INCOME_L_CD = L1.LEVY_L_CD ");
                stb.append("     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON T1.YEAR = INCOME_SCH.YEAR ");
                stb.append("          AND T1.SCHOOLCD    = INCOME_SCH.SCHOOLCD ");
                stb.append("          AND T1.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ");
                stb.append("          AND T1.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ");
                stb.append("          AND T1.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ");
                stb.append("          AND T1.REQUEST_NO = INCOME_SCH.REQUEST_NO ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ");
                if (!GRADE_ALL.equals(grade)) {
                    stb.append("          AND REGD.GRADE = '" + grade + "' ");
                }
                stb.append("          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND T1.SCHOOLCD = '" + _schoolcd + "' ");
                stb.append("     AND T1.SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("     AND VALUE(T1.INCOME_APPROVAL, '0') = '1' ");
                stb.append("     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.INCOME_L_CD ");
                final String sql = stb.toString();
                log.debug(" L_ListSql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd = rs.getString("INCOME_L_CD");
                    final String lName = rs.getString("LEVY_L_NAME");
                    final Lmst lmst = new Lmst(lCd, lName);
                    retList.add(lmst);
                }
            } catch (SQLException ex) {
                log.error("LEVY_REQUEST_INCOME_DAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        /** 返金伝票 */
        private Map getHenkinMap(final DB2UDB db2, final String year, final String grade) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH MAX_SEME AS ( ");
                stb.append("     SELECT ");
                stb.append("         SCHREGNO, ");
                stb.append("         max(SEMESTER) AS MAX_SEMESTER ");
                stb.append("     FROM ");
                stb.append("         SCHREG_REGD_DAT ");
                stb.append("     WHERE ");
                stb.append("         YEAR = '" + _ctrlYear + "' ");
                stb.append("     GROUP BY ");
                stb.append("         SCHREGNO ");
                stb.append(" ), REGD_DATA AS ( ");
                stb.append("     SELECT ");
                stb.append("         T1.SCHREGNO ");
                stb.append("     FROM ");
                stb.append("         SCHREG_REGD_DAT T1 ");
                stb.append("         INNER JOIN MAX_SEME T2 ON T2.SCHREGNO     = T1.SCHREGNO ");
                stb.append("                               AND T2.MAX_SEMESTER = T1.SEMESTER ");
                stb.append("     WHERE ");
                stb.append("             T1.YEAR  = '" + _ctrlYear + "' ");
                stb.append("         AND T1.GRADE || '-' || T1.HR_CLASS in " + _gradeHrInState + " ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     OSCH.SCHREGNO, ");
                stb.append("     sum(value(OSCH.OUTGO_MONEY, 0)) AS HENKIN ");
                stb.append(" FROM ");
                stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
                stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
                stb.append("                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
                stb.append("                                          AND OUTG.YEAR        = OSCH.YEAR ");
                stb.append("                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
                stb.append("                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
                stb.append("                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
                stb.append("     INNER JOIN REGD_DATA REGD ON REGD.SCHREGNO = OSCH.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("         OSCH.SCHOOLCD       = '" + _schoolcd + "' ");
                stb.append("     AND OSCH.SCHOOL_KIND    = '" + _schoolKind + "' ");
                stb.append("     AND OSCH.YEAR           = '" + _ctrlYear + "' ");
                stb.append("     AND OUTG.OUTGO_L_CD     = '99' "); // 返金伝票
                stb.append("     AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ");// 決済済み
                stb.append("     AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ");// 未キャンセル
                stb.append("     AND VALUE(OUTG.HENKIN_APPROVAL, '0') = '1' ");// CSV取込で完了したデータのみ
                stb.append(" GROUP BY ");
                stb.append("     OSCH.SCHREGNO ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     OSCH.SCHREGNO, ");
                stb.append("     sum(value(OSCH.OUTGO_MONEY, 0)) AS HENKIN ");
                stb.append(" FROM ");
                stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
                stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
                stb.append("                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
                stb.append("                                          AND OUTG.YEAR        = OSCH.YEAR ");
                stb.append("                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
                stb.append("                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
                stb.append("                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
                stb.append("     INNER JOIN REGD_DATA REGD ON REGD.SCHREGNO = OSCH.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("         OSCH.SCHOOLCD       = '" + _schoolcd + "' ");
                stb.append("     AND OSCH.SCHOOL_KIND    = '" + _schoolKind + "' ");
                stb.append("     AND OSCH.YEAR           = '" + _ctrlYear + "' ");
                stb.append("     AND OUTG.OUTGO_L_CD    <> '99' "); // 返金伝票除く
                stb.append("     AND OUTG.INCOME_L_CD    = '98' "); // 返金伝票(給付)
                stb.append("     AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ");// 決済済み
                stb.append("     AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ");// 未キャンセル
                stb.append("     AND VALUE(OUTG.HENKIN_APPROVAL, '0') = '1' ");// CSV取込で完了したデータのみ
                stb.append(" GROUP BY ");
                stb.append("     OSCH.SCHREGNO ");

                final String sql = stb.toString();
                log.debug(" henkinSql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String henkin   = rs.getString("HENKIN");


                    if (retMap.containsKey(schregNo)) {
                        final String getHenkin = String.valueOf(Integer.valueOf((String) retMap.get(schregNo)) + Integer.valueOf(henkin));

                        retMap.put(schregNo, getHenkin);
                    } else {
                        retMap.put(schregNo, henkin);
                    }
                }
            } catch (SQLException ex) {
                log.error("HENKIN_DAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** 端数返金伝票 */
        private Map getHasuuHenkinMap(final DB2UDB db2, final String year, final String grade) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     OSCH.SCHREGNO, ");
                stb.append("     sum(value(OSCH.OUTGO_MONEY, 0)) AS HASUU_HENKIN ");
                stb.append(" FROM ");
                stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT OSCH ");
                stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = OSCH.SCHOOLCD ");
                stb.append("                                          AND OUTG.SCHOOL_KIND = OSCH.SCHOOL_KIND ");
                stb.append("                                          AND OUTG.YEAR        = OSCH.YEAR ");
                stb.append("                                          AND OUTG.OUTGO_L_CD  = OSCH.OUTGO_L_CD ");
                stb.append("                                          AND OUTG.OUTGO_M_CD  = OSCH.OUTGO_M_CD ");
                stb.append("                                          AND OUTG.REQUEST_NO  = OSCH.REQUEST_NO ");
                stb.append(" WHERE ");
                stb.append("         OSCH.SCHOOLCD       = '" + _schoolcd + "' ");
                stb.append("     AND OSCH.SCHOOL_KIND    = '" + _schoolKind + "' ");
                stb.append("     AND OSCH.YEAR           = '" + _ctrlYear + "' ");
                stb.append("     AND OUTG.OUTGO_L_CD     = '99' "); // 返金伝票
                stb.append("     AND VALUE(OUTG.OUTGO_APPROVAL, '0')  = '1' ");// 決済済み
                stb.append("     AND VALUE(OUTG.OUTGO_CANCEL, '0')    = '0' ");// 未キャンセル
                stb.append("     AND OUTG.REQUEST_NO in ( ");
                stb.append("                            SELECT ");
                stb.append("                                INS_REQUEST_NO ");
                stb.append("                            FROM ");
                stb.append("                                LEVY_REQUEST_HASUU_WORK_DAT ");
                stb.append("                            WHERE ");
                stb.append("                                    SCHOOLCD    = '" + _schoolcd + "' ");
                stb.append("                                AND SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("                                AND YEAR        = '" + _ctrlYear + "' ");
                stb.append("                            ) ");
                stb.append(" GROUP BY ");
                stb.append("     OSCH.SCHREGNO ");

                final String sql = stb.toString();
                log.debug(" hasuuHenkinSql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("SCHREGNO"), rs.getString("HASUU_HENKIN"));
                }
            } catch (SQLException ex) {
                log.error("HASUU_HENKIN_DAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** 繰越伝票 */
        private Map getCarryOverMap(final DB2UDB db2, final String year, final String grade) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();

                stb.append(" WITH MAX_SEME_REGD AS ( ");
                stb.append("     SELECT ");
                stb.append("         SCHREGNO, ");
                stb.append("         max(SEMESTER) MAX_SEM ");
                stb.append("     FROM ");
                stb.append("         SCHREG_REGD_DAT ");
                stb.append("     WHERE ");
                stb.append("         YEAR = '" + year + "' ");
                stb.append("     GROUP BY ");
                stb.append("         SCHREGNO ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("  T1.SCHREGNO, ");
                stb.append("  SUM(T2.CARRY_OVER_MONEY) AS CARRYOVER ");
                stb.append(" FROM ");
                stb.append("  SCHREG_REGD_DAT T1 ");
                stb.append("  LEFT JOIN LEVY_CARRY_OVER_DAT T2 ");
                stb.append("    ON T2.YEAR = T1.YEAR ");
                stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("   AND VALUE(T2.CARRY_CANCEL, '0') <> '1' ");
                stb.append("  INNER JOIN MAX_SEME_REGD SEME ON SEME.SCHREGNO = T1.SCHREGNO ");
                stb.append("                               AND SEME.MAX_SEM  = T1.SEMESTER ");
                stb.append(" WHERE ");
                stb.append("  T1.YEAR = '" + year + "' ");
                stb.append("  AND T1.GRADE || '-' || T1.HR_CLASS IN " + _gradeHrInState);
                stb.append(" GROUP BY ");
                stb.append("  T1.SCHREGNO ");

                final String sql = stb.toString();
                log.debug(" carrySql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("SCHREGNO"), rs.getString("CARRYOVER"));
                }
            } catch (SQLException ex) {
                log.error("CARRYOVER_DAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getHasuu(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH MAX_SEME AS ( ");
                stb.append("     SELECT ");
                stb.append("         SCHREGNO, ");
                stb.append("         max(SEMESTER) AS MAX_SEMESTER ");
                stb.append("     FROM ");
                stb.append("         SCHREG_REGD_DAT ");
                stb.append("     WHERE ");
                stb.append("         YEAR = '" + _ctrlYear + "' ");
                stb.append("     GROUP BY ");
                stb.append("         SCHREGNO ");
                stb.append(" ), REGD_DATA AS ( ");
                stb.append("     SELECT ");
                stb.append("         T1.SCHREGNO ");
                stb.append("     FROM ");
                stb.append("         SCHREG_REGD_DAT T1 ");
                stb.append("         INNER JOIN MAX_SEME T2 ON T2.SCHREGNO     = T1.SCHREGNO ");
                stb.append("                               AND T2.MAX_SEMESTER = T1.SEMESTER ");
                stb.append("     WHERE ");
                stb.append("             T1.YEAR  = '" + _ctrlYear + "' ");
                stb.append("         AND T1.GRADE || '-' || T1.HR_CLASS in " + _gradeHrInState + " ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     OUTG.INCOME_L_CD, ");
                stb.append("     sum(value(MSAI.HASUU, 0)) AS HASUU ");
                stb.append(" FROM ");
                stb.append("     LEVY_REQUEST_OUTGO_MEISAI_DAT MSAI ");
                stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON OUTG.SCHOOLCD    = MSAI.SCHOOLCD ");
                stb.append("                                          AND OUTG.SCHOOL_KIND = MSAI.SCHOOL_KIND ");
                stb.append("                                          AND OUTG.YEAR        = MSAI.YEAR ");
                stb.append("                                          AND OUTG.OUTGO_L_CD  = MSAI.OUTGO_L_CD ");
                stb.append("                                          AND OUTG.OUTGO_M_CD  = MSAI.OUTGO_M_CD ");
                stb.append("                                          AND OUTG.REQUEST_NO  = MSAI.REQUEST_NO ");
                stb.append(" WHERE ");
                stb.append("         MSAI.SCHOOLCD       = '" + _schoolcd + "' ");
                stb.append("     AND MSAI.SCHOOL_KIND    = '" + _schoolKind + "' ");
                stb.append("     AND MSAI.YEAR           = '" + _ctrlYear + "' ");
                stb.append("     AND VALUE(OUTG.OUTGO_APPROVAL, '0') = '1' ");// 決済済み
                stb.append("     AND VALUE(OUTG.OUTGO_CANCEL, '0')   = '0' ");
                stb.append("     AND value(MSAI.HASUU, 0) <> 0 ");
                stb.append("     AND MSAI.REQUEST_NO     in (SELECT ");
                stb.append("                                     REQUEST_NO ");
                stb.append("                                 FROM ");
                stb.append("                                     LEVY_REQUEST_OUTGO_SCHREG_DAT SCHD ");
                stb.append("                                     INNER JOIN REGD_DATA REGD ON REGD.SCHREGNO = SCHD.SCHREGNO ");
                stb.append("                                 WHERE ");
                stb.append("                                         SCHD.SCHOOLCD    = MSAI.SCHOOLCD ");
                stb.append("                                     AND SCHD.SCHOOL_KIND = MSAI.SCHOOL_KIND ");
                stb.append("                                     AND SCHD.YEAR        = MSAI.YEAR ");
                stb.append("                                     AND SCHD.REQUEST_NO  = MSAI.REQUEST_NO ");
                stb.append("                                     AND SCHD.LINE_NO     = MSAI.LINE_NO ");
                stb.append("                                     AND SCHD.OUTGO_L_CD  = MSAI.OUTGO_L_CD ");
                stb.append("                                     AND SCHD.OUTGO_M_CD  = MSAI.OUTGO_M_CD ");
                stb.append("                                     AND SCHD.OUTGO_S_CD  = MSAI.OUTGO_S_CD ");
                stb.append("                                 ) ");
                stb.append(" GROUP BY ");
                stb.append("     OUTG.INCOME_L_CD ");

                final String sql = stb.toString();
                log.debug(" HasuuSQL =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd        = rs.getString("INCOME_L_CD");
                    final String hasuu      = rs.getString("HASUU");

                    retMap.put(lCd, hasuu);
                }
            } catch (SQLException ex) {
                log.debug("param_HASUU exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** 作成日 */
        public String getNow(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(KNJ_EditDate.gengou(db2, Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

    }
    private class Lmst {
        private final String _lCd;
        private final String _lName;
        public Lmst(
                final String lCd,
                final String lName
        ) {
            _lCd   = lCd;
            _lName = lName;
        }
    }
}

// eof

