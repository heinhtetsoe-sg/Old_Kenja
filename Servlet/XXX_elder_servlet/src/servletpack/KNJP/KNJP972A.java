/*
 * $Id$
 *
 * 作成日: 2015/04/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

public class KNJP972A {

    private static final Log log = LogFactory.getLog(KNJP972A.class);

    private final int MAX_COL = 29;
    private final String HENKIN = "HENKIN";
    private final String CARRY_OVER = "CARRY_OVER";
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws ParseException, UnsupportedEncodingException {
        for (Iterator iterator = _param._lList.iterator(); iterator.hasNext();) {
            Lmst lmst = (Lmst) iterator.next();
            final List hrClassList = getList(db2, lmst._lCd);

            for (Iterator itPrint = hrClassList.iterator(); itPrint.hasNext();) {
                HrClass hrClass = (HrClass) itPrint.next();
                int inComeCnt   = 0;
                final Map totalIncomeMap = new HashMap();
                final Map utiwakeIncomeMap = new HashMap();
                final Map totalOutGoMap = new HashMap();
                final Map utiwakeOutgoMap = new HashMap();
                final Map utiwakeSagakuMap = new HashMap();
                final Map totalCntMap = new HashMap();
                final Map utiwakeIncomeCntMap = new HashMap();
                final Map utiwakeOuntgoCntMap = new HashMap();
                final Map utiwakeSagakuCntMap = new HashMap();
                final Map incomeUtiwakeSchMap = new TreeMap();
                final Map outgoUtiwakeSchMap = new TreeMap();
                final Map totalOutGoCarryMap = new TreeMap();
                final Map totalCntCarryMap   = new TreeMap();
                int totalIncome = 0;
                int totalOutGoHenkin = 0;
                int totalCntHenkin   = 0;
                int totalOutGoGedan = 0;
                int outGoCntGedan   = 0;
                int totalHasuuGedan = 0;
                int totalSagakuGedan = 0;
                int sagakuCntGedan   = 0;
                final int totalSchMapCnt = hrClass._schMap.size();
                final int outgoMeisaiCnt = hrClass._outgosMstMap.size();
                for (Iterator itSchMap = hrClass._schMap.keySet().iterator(); itSchMap.hasNext();) {
                    final String schKey = (String) itSchMap.next();
                    final List schList = (List) hrClass._schMap.get(schKey);
                    svf.VrSetForm(_param._setForm, 4);

                    //基本情報
                    int lineCnt = 1;
                    for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
                        svf.VrsOut("TITLE", gengou + "年度　収入-支出細目一覧");
                        svf.VrsOut("SUBTITLE", "(" + lmst._lName + ")");
                        svf.VrsOut("HR_NAME", hrClass._hrName);
                        svf.VrsOut("TEACHER_NAME", hrClass._staffName);
                        svf.VrsOut("DATE", _param.getNow(db2));

                        final PrintSchregData schregData = (PrintSchregData) itSch.next();
                        svf.VrsOutn("NO", lineCnt, schregData._attendno);
                        svf.VrsOutn("SCHREGNO", lineCnt, schregData._schregNo);
                        final int nameByte = KNJ_EditEdit.getMS932ByteLength(schregData._schName);
                        final String fieldSuffix = nameByte <= 16 ? "1" : "2";
                        svf.VrsOutn("NAME" + fieldSuffix, lineCnt, schregData._schName);
                        lineCnt++;
                    }

                    //収入
                    int incomeColCnt = 1;
                    for (Iterator itHr = hrClass._incomeMstMap.keySet().iterator(); itHr.hasNext();) {
                        final String incomeDivLMcd = (String) itHr.next();
                        final String mName = (String) hrClass._incomeMstMap.get(incomeDivLMcd);

                        if (incomeColCnt > MAX_COL) {
                            svf.VrSetForm(_param._setForm, 4);
                            incomeColCnt = 1;
                            lineCnt = 1;
                            //名前再セット
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                svf.VrsOutn("NO", lineCnt, schregData._attendno);
                                svf.VrsOutn("SCHREGNO", lineCnt, schregData._schregNo);
                                final int nameByte = KNJ_EditEdit.getMS932ByteLength(schregData._schName);
                                final String fieldSuffix = nameByte <= 16 ? "1" : "2";
                                svf.VrsOutn("NAME" + fieldSuffix, lineCnt, schregData._schName);
                                lineCnt++;
                            }
                        }

                        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
                        svf.VrsOut("TITLE", gengou + "年度　収入-支出細目一覧");
                        svf.VrsOut("SUBTITLE", "(" + lmst._lName + ")");
                        svf.VrsOut("HR_NAME", hrClass._hrName);
                        svf.VrsOut("TEACHER_NAME", hrClass._staffName);
                        svf.VrsOut("DATE", _param.getNow(db2));
                        svf.VrsOut("BLANK", "1");

                        lineCnt = 1;
                        int calTotalIncome = 0;
                        int calTotalCnt = 0;
                        for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                            final PrintSchregData schregData = (PrintSchregData) itSch.next();
                            if (1 == incomeColCnt) {
                                svf.VrsOut("ITEM_TITLE", "収");
                            } else if (2 == incomeColCnt) {
                                svf.VrsOut("ITEM_TITLE", "入");
                            } else if (3 == incomeColCnt) {
                                svf.VrsOut("ITEM_TITLE", "明");
                            } else if (4 == incomeColCnt) {
                                svf.VrsOut("ITEM_TITLE", "細");
                            }
                            final int mNameLen = mName.length();
                            if (mNameLen <= 8) {
                                svf.VrsOut("ITEM2", mName);
                            } else {
                                final List mNameList = getNameList(mName, 10);

                                int check_len  = mNameList.size();
                                String setField = "";
                                if (check_len > 2) {
                                    svf.VrsOut("ITEM2_3_1", mName);
                                    setField = "2_3_";
                                } else if (check_len > 1) {
                                    setField = "2_2_";
                                } else {
                                    setField = "2_1_";
                                }
                                int mNameCnt = 1;
                                for (Iterator itmName = mNameList.iterator(); itmName.hasNext();) {
                                    final String setmName = (String) itmName.next();
                                    svf.VrsOut("ITEM" + setField + mNameCnt, setmName);
                                    mNameCnt++;
                                }
                            }

                            final IncomeMoney incomeMoney = (IncomeMoney) schregData._incomeMap.get(incomeDivLMcd);
                            if (null != incomeMoney) {
                                if (-999 <= incomeMoney._incomeMoney && incomeMoney._incomeMoney <= 9999) {
                                    svf.VrsOutn("MONEY2_1", lineCnt, String.valueOf(incomeMoney._incomeMoney));
                                } else {
                                    svf.VrsOutn("MONEY2_2", lineCnt, String.valueOf(incomeMoney._incomeMoney));
                                }

                                //預り金項目毎の合計値を計算し、mapに格納
                                final String incomeLMcd = incomeDivLMcd.substring(1, 5);
                                if (!incomeUtiwakeSchMap.containsKey(incomeLMcd)) {
                                    incomeUtiwakeSchMap.put(incomeLMcd, new TreeMap());
                                }
                                Map schlmMoneyMap = (Map)incomeUtiwakeSchMap.get(incomeLMcd);
                                if (!schlmMoneyMap.containsKey(schregData._schregNo)) {
                                    schlmMoneyMap.put(schregData._schregNo, 0);
                                }
                                int schLmMoney = ((Integer)schlmMoneyMap.get(schregData._schregNo)).intValue();
                                schLmMoney += incomeMoney._incomeMoney;
                                schlmMoneyMap.put(schregData._schregNo, schLmMoney);

                                calTotalIncome += incomeMoney._incomeMoney;
                                calTotalCnt++;

                            }
                            lineCnt++;
                        }

                        //人数
                        int totalCnt = 0;
                        if (totalCntMap.containsKey(incomeDivLMcd)) {
                            totalCnt = ((Integer) totalCntMap.get(incomeDivLMcd)).intValue();
                        }
                        totalCnt += calTotalCnt;
                        totalCntMap.put(incomeDivLMcd, new Integer(totalCnt));
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            svf.VrsOutn("MONEY2_1", 46, String.valueOf(totalCnt));
                        }

                        //合計
                        int totalLIncome = 0;
                        if (totalIncomeMap.containsKey(incomeDivLMcd)) {
                            totalLIncome = ((Integer) totalIncomeMap.get(incomeDivLMcd)).intValue();
                        }
                        totalLIncome += calTotalIncome;
                        totalIncomeMap.put(incomeDivLMcd, new Integer(totalLIncome));
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            if (-999 <= totalLIncome && totalLIncome <= 9999) {
                                svf.VrsOutn("MONEY2_1", 47, String.valueOf(totalLIncome));
                            } else {
                                   svf.VrsOutn("MONEY2_2", 47, String.valueOf(totalLIncome));
                            }
                        }

                        svf.VrEndRecord();

                        incomeColCnt++;
                    }

                    //収入タイトル用 (収入明細列が最低4列になるように調整)
                    while (incomeColCnt <= 4) {
                        if (1 == incomeColCnt) {
                            svf.VrsOut("ITEM_TITLE", "収");
                        } else if (2 == incomeColCnt) {
                            svf.VrsOut("ITEM_TITLE", "入");
                        } else if (3 == incomeColCnt) {
                            svf.VrsOut("ITEM_TITLE", "明");
                        } else if (4 == incomeColCnt) {
                            svf.VrsOut("ITEM_TITLE", "細");
                        }
                        svf.VrsOut("BLANK", "1");
                        svf.VrEndRecord();
                        incomeColCnt++;
                    }

                    //収入計内訳
                    int utiwakeIncomeCol = 1;
                    for (Iterator lmIt = incomeUtiwakeSchMap.keySet().iterator(); lmIt.hasNext();) {
                        final String incomeLMcd = (String) lmIt.next();
                        final String utiwakeMName = (String) hrClass._incomeMstLMCdMap.get(incomeLMcd);
                        final Map schlmMoneyMap = (Map)incomeUtiwakeSchMap.get(incomeLMcd);

                        if (incomeColCnt > MAX_COL) {
                            svf.VrSetForm(_param._setForm, 4);
                            incomeColCnt = 1;
                            lineCnt = 1;
                            //名前再セット
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                svf.VrsOutn("NO", lineCnt, schregData._attendno);
                                svf.VrsOutn("SCHREGNO", lineCnt, schregData._schregNo);
                                final int nameByte = KNJ_EditEdit.getMS932ByteLength(schregData._schName);
                                final String fieldSuffix = nameByte <= 16 ? "1" : "2";
                                svf.VrsOutn("NAME" + fieldSuffix, lineCnt, schregData._schName);
                                lineCnt++;
                            }
                        }

                        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
                        svf.VrsOut("TITLE", gengou + "年度　収入-支出細目一覧");
                        svf.VrsOut("SUBTITLE", "(" + lmst._lName + ")");
                        svf.VrsOut("HR_NAME", hrClass._hrName);
                        svf.VrsOut("TEACHER_NAME", hrClass._staffName);
                        svf.VrsOut("DATE", _param.getNow(db2));
                        svf.VrsOut("BLANK", "2");

                        lineCnt = 1;
                        int calUtiwakeTotalIncome = 0;
                        int calUtiwakeTotalCnt = 0;
                        for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                            final PrintSchregData schregData = (PrintSchregData) itSch.next();
                            if (1 == utiwakeIncomeCol) {
                                svf.VrsOut("ITEM_TITLE_2", "収入計");
                            } else if (2 == utiwakeIncomeCol) {
                                svf.VrsOut("ITEM_TITLE_2", " 内訳");
                            }
                            final int mNameLen = utiwakeMName.length();
                            if (mNameLen <= 8) {
                                svf.VrsOut("ITEM2", utiwakeMName);
                            } else {
                                final List mNameList = getNameList(utiwakeMName, 10);

                                int check_len  = mNameList.size();
                                String setField = "";
                                if (check_len > 2) {
                                    svf.VrsOut("ITEM2_3_1", utiwakeMName);
                                    setField = "2_3_";
                                } else if (check_len > 1) {
                                    setField = "2_2_";
                                } else {
                                    setField = "2_1_";
                                }
                                int mNameCnt = 1;
                                for (Iterator itmName = mNameList.iterator(); itmName.hasNext();) {
                                    final String setmName = (String) itmName.next();
                                    svf.VrsOut("ITEM" + setField + mNameCnt, setmName);
                                    mNameCnt++;
                                }
                            }

                            final Integer utiwakeIncomeMoney = (Integer) schlmMoneyMap.get(schregData._schregNo);
                            if (null != utiwakeIncomeMoney) {
                                if (-999 <= utiwakeIncomeMoney && utiwakeIncomeMoney <= 9999) {
                                    svf.VrsOutn("MONEY2_1", lineCnt, String.valueOf(utiwakeIncomeMoney));
                                } else {
                                    svf.VrsOutn("MONEY2_2", lineCnt, String.valueOf(utiwakeIncomeMoney));
                                }
                                calUtiwakeTotalIncome += utiwakeIncomeMoney;
                                calUtiwakeTotalCnt++;

                            }
                            lineCnt++;
                        }

                        //人数
                        int utiwakeTotalCnt = 0;
                        if (utiwakeIncomeCntMap.containsKey(incomeLMcd)) {
                            utiwakeTotalCnt = ((Integer)utiwakeIncomeCntMap.get(incomeLMcd)).intValue();
                        }
                        utiwakeTotalCnt += calUtiwakeTotalCnt;
                        utiwakeIncomeCntMap.put(incomeLMcd, utiwakeTotalCnt);
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            svf.VrsOutn("MONEY2_1", 46, String.valueOf(utiwakeTotalCnt));
                        }

                        //合計
                        int utiwakeTotalLIncome = 0;
                        if (utiwakeIncomeMap.containsKey(incomeLMcd)) {
                            utiwakeTotalLIncome = ((Integer) utiwakeIncomeMap.get(incomeLMcd)).intValue();
                        }
                        utiwakeTotalLIncome += calUtiwakeTotalIncome;
                        utiwakeIncomeMap.put(incomeLMcd, new Integer(utiwakeTotalLIncome));
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            if (-999 <= utiwakeTotalLIncome && utiwakeTotalLIncome <= 9999) {
                                svf.VrsOutn("MONEY2_1", 47, String.valueOf(utiwakeTotalLIncome));
                            } else {
                                   svf.VrsOutn("MONEY2_2", 47, String.valueOf(utiwakeTotalLIncome));
                            }
                        }

                        svf.VrEndRecord();

                        utiwakeIncomeCol++;
                        incomeColCnt++;
                    }
                    //収入タイトル用 (収入計内訳列が最低2列になるように調整)
                    while (utiwakeIncomeCol <= 2) {
                        if (1 == utiwakeIncomeCol) {
                            svf.VrsOut("ITEM_TITLE_2", "収入計");
                        } else if (2 == utiwakeIncomeCol) {
                            svf.VrsOut("ITEM_TITLE_2", " 内訳");
                        }
                        svf.VrsOut("BLANK", "2");
                        svf.VrEndRecord();
                        utiwakeIncomeCol++;
                        incomeColCnt++;
                    }
                    //収入トータル
                    lineCnt = 1;
                    for (Iterator itSch = schList.iterator(); itSch.hasNext();) {

                        final PrintSchregData schregData = (PrintSchregData) itSch.next();

                        svf.VrsOut("ITEM1", "収入");
                        svf.VrsOutn("MONEY1", lineCnt, String.valueOf(schregData._incomeMoney));

                        if (0 != schregData._incomeMoney) {
                            inComeCnt++;
                        }
                        totalIncome += schregData._incomeMoney;
                        lineCnt++;
                    }

                    svf.VrsOut("ITEM1", "収入");
                    if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                        svf.VrsOutn("MONEY1", 46, String.valueOf(inComeCnt)); // 人数
                        svf.VrsOutn("MONEY1", 47, String.valueOf(totalIncome));
                    }
                    svf.VrEndRecord();
                    incomeColCnt++;

                    //支出明細
                    int colCnt = incomeColCnt;
                    List kurikosiKeys = new ArrayList();
                    for (Iterator itHr = hrClass._outgosMstMap.keySet().iterator(); itHr.hasNext();) {
                        final String requestLineCd = (String) itHr.next();
                        final Map outgosMstMap = (Map) hrClass._outgosMstMap.get(requestLineCd);
                        final String sName = (String) outgosMstMap.get("S_NAME");
                        final String sortDate = (String) outgosMstMap.get("SORT_DATE");
                        final String approval = (String) outgosMstMap.get("OUTGO_APPROVAL");
                        final String[] outgoDateArray = getDateArray(sortDate);
                        if (colCnt > MAX_COL) {
                            svf.VrSetForm(_param._setForm, 4);
                            colCnt = 1;
                            lineCnt = 1;
                            //名前再セット
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                svf.VrsOutn("NO", lineCnt, schregData._attendno);
                                svf.VrsOutn("SCHREGNO", lineCnt, schregData._schregNo);
                                final int nameByte = KNJ_EditEdit.getMS932ByteLength(schregData._schName);
                                final String fieldSuffix = nameByte <= 16 ? "1" : "2";
                                svf.VrsOutn("NAME" + fieldSuffix, lineCnt, schregData._schName);
                                lineCnt++;
                            }
                        }

                        // 繰越は最後の列 - 1列に出力する
                        if (0 <= requestLineCd.indexOf(CARRY_OVER)) {
                            kurikosiKeys.add(requestLineCd);
                            continue;
                        }

                        // 返金は最後の列に出力する
                        if (0 == requestLineCd.indexOf(HENKIN) && colCnt != MAX_COL) continue;

                        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
                        svf.VrsOut("TITLE", gengou + "年度　収入-支出細目一覧");
                        svf.VrsOut("SUBTITLE", "(" + lmst._lName + ")");
                        svf.VrsOut("HR_NAME", hrClass._hrName);
                        svf.VrsOut("TEACHER_NAME", hrClass._staffName);
                        svf.VrsOut("DATE", _param.getNow(db2));
                        svf.VrsOut("BLANK3", "4");

                        lineCnt = 1;
                        int calTotalOutGo = 0;
                        int calTotalCnt = 0;
                        for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                            final PrintSchregData schregData = (PrintSchregData) itSch.next();
                            if (12 == colCnt) {
                                svf.VrsOut("ITEM_TITLE3", "支");
                            } else if (14 == colCnt) {
                                svf.VrsOut("ITEM_TITLE3", "出");
                            } else if (16 == colCnt) {
                                svf.VrsOut("ITEM_TITLE3", "明");
                            } else if (18 == colCnt) {
                                svf.VrsOut("ITEM_TITLE3", "細");
                            }
                            //支出日(未決済なら非表示)
                            if ("1".equals(approval)) {
                                if (outgoDateArray != null && outgoDateArray.length == 3) {
                                    String m = String.valueOf(Integer.parseInt(outgoDateArray[1]));
                                    String d = String.valueOf(Integer.parseInt(outgoDateArray[2]));
                                    svf.VrsOut("DATE33_1", m + "/" + d);
                                }
                            }
                            final int sNameLen = sName.length();
                            if (sNameLen <= 6) {
                                svf.VrsOut("ITEM3", sName);
                            } else {
                                final List snameList = getNameList(sName, 8);

                                int check_len  = snameList.size();
                                String setField = "";
                                if (check_len > 2) {
                                    svf.VrsOut("ITEM3_3_1", sName);
                                    setField = "3_3_";
                                } else if (check_len > 1) {
                                    setField = "3_2_";
                                } else {
                                    setField = "3_1_";
                                }
                                int sNameCnt = 1;
                                for (Iterator itSname = snameList.iterator(); itSname.hasNext();) {
                                    final String setSname = (String) itSname.next();
                                    svf.VrsOut("ITEM" + setField + sNameCnt, setSname);
                                    sNameCnt++;
                                }
                            }

                            final OutGoMoney outGoMoney = (OutGoMoney) schregData._outGoMap.get(requestLineCd);

                            if (null != outGoMoney) {
                                if (-999 <= outGoMoney._outGoMoney && outGoMoney._outGoMoney <= 9999) {
                                    svf.VrsOutn("MONEY3_1", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                } else {
                                    svf.VrsOutn("MONEY3_2", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                }

                                //支出計内訳作成
                                makeOutgoUtiwakeMap(outgoUtiwakeSchMap, outGoMoney._outGoIncomeLCd + outGoMoney._outGoIncomeMCd, schregData._schregNo, outGoMoney._outGoMoney);

                                calTotalOutGo += outGoMoney._outGoMoney;
                                calTotalCnt++;
                            }
                            lineCnt++;
                        }

                        //人数
                        int totalCnt = 0;
                        if (totalCntMap.containsKey(requestLineCd)) {
                            totalCnt = ((Integer) totalCntMap.get(requestLineCd)).intValue();
                        }
                        totalCnt += calTotalCnt;
                        totalCntMap.put(requestLineCd, new Integer(totalCnt));
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            svf.VrsOutn("MONEY3_1", 46, String.valueOf(totalCnt));
                        }

                        //合計
                        int totaloutGo = 0;
                        if (totalOutGoMap.containsKey(requestLineCd)) {
                            totaloutGo = ((Integer) totalOutGoMap.get(requestLineCd)).intValue();
                        }
                        totaloutGo += calTotalOutGo;
                        totalOutGoMap.put(requestLineCd, new Integer(totaloutGo));
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            if (-999 <= totaloutGo && totaloutGo <= 9999) {
                                svf.VrsOutn("MONEY3_1", 47, String.valueOf(totaloutGo));
                            } else {
                                svf.VrsOutn("MONEY3_2", 47, String.valueOf(totaloutGo));
                            }
                        }

                        //端数
                        final String key = lmst._lCd + requestLineCd;
                        final String hasuu = (String) _param._hasuuMap.get(key);
                        final int setHasuu = hasuu == null ? 0: Integer.parseInt(hasuu);
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            if (!_param._husuuPrintBooleanMap.containsKey(key)) { // 一度印字した端数は印字しない
                                svf.VrsOutn("MONEY3_1", 48, String.valueOf(setHasuu));
                                totalHasuuGedan += setHasuu;
                                if (0 < setHasuu) {
                                    _param._husuuPrintBooleanMap.put(key, "1");
                                }
                            } else {
                                svf.VrsOutn("MONEY3_1", 48, "0");
                            }
                        }

                        svf.VrEndRecord();

                        colCnt++;
                    }

                    //空白レコード埋め＋返金セット（最終列）
                    int kurikosiCnt = kurikosiKeys.size();
                    for (int i = colCnt; i <= MAX_COL; i++) {
                        if (12 == i) {
                            svf.VrsOut("ITEM_TITLE3", "支");
                        } else if (14 == i) {
                            svf.VrsOut("ITEM_TITLE3", "出");
                        } else if (16 == i) {
                            svf.VrsOut("ITEM_TITLE3", "明");
                        } else if (18 == i) {
                            svf.VrsOut("ITEM_TITLE3", "細");
                        }
                        svf.VrsOut("BLANK3", "4");

                        //最後の列に返金、(最後-1)までの列に繰越をあるだけ出力
                        final String henkinKey = HENKIN + '1'; // 1はLINECD
                        if (MAX_COL - kurikosiCnt <= i && i < MAX_COL) { //繰越
                            final String carryKey = (String)kurikosiKeys.get(kurikosiCnt - (MAX_COL - i));
                            final String carryLmCd = StringUtils.split(carryKey, "_")[2];
                            final String headerSymbol = StringUtils.defaultIfEmpty(_param._nmstP009Map.get(carryLmCd), "");
                            final String carryMName = (String) hrClass._incomeMstLMCdMap.get(carryLmCd);

                            final String sName = headerSymbol + "繰越（" + carryMName + "）";

                            lineCnt = 1;
                            int calTotalOutGoCarry = 0;
                            int calTotalCntCarry = 0;
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                final int sNameLen = sName.length();
                                if (sNameLen <= 6) {
                                    svf.VrsOut("ITEM3", sName);
                                } else {
                                    final List snameList = getNameList(sName, 8);

                                    int check_len  = snameList.size();
                                    String setField = "";
                                    if (check_len > 2) {
                                        svf.VrsOut("ITEM3_3_1", sName);
                                        setField = "3_3_";
                                    } else if (check_len > 1) {
                                        setField = "3_2_";
                                    } else {
                                        setField = "3_1_";
                                    }
                                    int sNameCnt = 1;
                                    for (Iterator itSname = snameList.iterator(); itSname.hasNext();) {
                                        final String setSname = (String) itSname.next();
                                        svf.VrsOut("ITEM" + setField + sNameCnt, setSname);
                                        sNameCnt++;
                                    }
                                }

                                final OutGoMoney outGoMoney = (OutGoMoney) schregData._outGoMap.get(carryKey);
                                if (null != outGoMoney) {
                                    if (-999 <= outGoMoney._outGoMoney && outGoMoney._outGoMoney <= 9999) {
                                        svf.VrsOutn("MONEY3_1", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                    } else {
                                        svf.VrsOutn("MONEY3_2", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                    }

                                    //支出計内訳作成
                                    makeOutgoUtiwakeMap(outgoUtiwakeSchMap, outGoMoney._outGoIncomeLCd + outGoMoney._outGoIncomeMCd, schregData._schregNo, outGoMoney._outGoMoney);

                                    calTotalOutGoCarry += outGoMoney._outGoMoney;
                                    calTotalCntCarry++;
                                }
                                lineCnt++;
                            }

                            //人数
                            int totalCnt = 0;
                            if (totalCntCarryMap.containsKey(carryKey)) {
                                totalCnt = ((Integer) totalCntCarryMap.get(carryKey)).intValue();
                            }
                            totalCnt += calTotalCntCarry;
                            totalCntCarryMap.put(carryKey, new Integer(totalCnt));
                            if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                                svf.VrsOutn("MONEY3_1", 46, String.valueOf(totalCnt));
                            }

                            //合計
                            int totalOutGoCarry = 0;
                            if (totalOutGoCarryMap.containsKey(carryKey)) {
                                totalOutGoCarry = ((Integer) totalOutGoCarryMap.get(carryKey)).intValue();
                            }
                            totalOutGoCarry += calTotalOutGoCarry;
                            totalOutGoCarryMap.put(carryKey, new Integer(totalOutGoCarry));
                            if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                                if (-999 <= totalOutGoCarry && totalOutGoCarry <= 9999) {
                                    svf.VrsOutn("MONEY3_1", 47, String.valueOf(totalOutGoCarry));
                                } else {
                                    svf.VrsOutn("MONEY3_2", 47, String.valueOf(totalOutGoCarry));
                                }
                            }

                            svf.VrEndRecord();

                        } else if (i == MAX_COL && null != hrClass._outgosMstMap.get(henkinKey)) {
                            final Map outgosMstMap = (Map) hrClass._outgosMstMap.get(henkinKey);
                            final String sName = (String) outgosMstMap.get("S_NAME");

                            lineCnt = 1;
                            totalOutGoHenkin = 0;
                            totalCntHenkin = 0;
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                final int sNameLen = sName.length();
                                if (sNameLen <= 6) {
                                    svf.VrsOut("ITEM3", sName);
                                } else {
                                    final List snameList = getNameList(sName, 8);

                                    int check_len  = snameList.size();
                                    String setField = "";
                                    if (check_len > 2) {
                                        svf.VrsOut("ITEM3_3_1", sName);
                                        setField = "3_3_";
                                    } else if (check_len > 1) {
                                        setField = "3_2_";
                                    } else {
                                        setField = "3_1_";
                                    }
                                    int sNameCnt = 1;
                                    for (Iterator itSname = snameList.iterator(); itSname.hasNext();) {
                                        final String setSname = (String) itSname.next();
                                        svf.VrsOut("ITEM" + setField + sNameCnt, setSname);
                                        sNameCnt++;
                                    }
                                }

                                final OutGoMoney outGoMoney = (OutGoMoney) schregData._outGoMap.get(henkinKey);
                                if (null != outGoMoney) {
                                    if (-999 <= outGoMoney._outGoMoney && outGoMoney._outGoMoney <= 9999) {
                                        svf.VrsOutn("MONEY3_1", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                    } else {
                                        svf.VrsOutn("MONEY3_2", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                    }

                                    //支出計内訳作成
                                    makeOutgoUtiwakeMap(outgoUtiwakeSchMap, outGoMoney._outGoIncomeLCd + outGoMoney._outGoIncomeMCd, schregData._schregNo, outGoMoney._outGoMoney);

                                    totalOutGoHenkin += outGoMoney._outGoMoney;
                                    totalCntHenkin++;
                                }
                                lineCnt++;
                            }

                            //人数
                            if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                                svf.VrsOutn("MONEY3_1", 46, String.valueOf(totalCntHenkin));
                            }

                            //合計
                            if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                                if (-999 <= totalOutGoHenkin &&totalOutGoHenkin <= 9999) {
                                    svf.VrsOutn("MONEY3_1", 47, String.valueOf(totalOutGoHenkin));
                                } else {
                                    svf.VrsOutn("MONEY3_2", 47, String.valueOf(totalOutGoHenkin));
                                }
                            }
                            svf.VrEndRecord();

                        } else {
                            svf.VrEndRecord();
                        }
                    }


                    //支出計
                    lineCnt = 1;
                    for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                        final PrintSchregData schregData = (PrintSchregData) itSch.next();
                        svf.VrsOut("ITEM1", "支出計");
                        svf.VrsOutn("MONEY1", lineCnt, String.valueOf(schregData._totalOutGoMoney));
                        if (0 != schregData._totalOutGoMoney) {
                            outGoCntGedan++;
                        }
                        totalOutGoGedan += schregData._totalOutGoMoney;
                        lineCnt++;
                    }
                    svf.VrsOut("ITEM1", "支出計");
                    if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                        svf.VrsOutn("MONEY1", 46, String.valueOf(outGoCntGedan));
                        svf.VrsOutn("MONEY1", 47, String.valueOf(totalOutGoGedan));
                        svf.VrsOutn("MONEY1", 48, String.valueOf(totalHasuuGedan));
                    }
                    svf.VrEndRecord();


                    //支出計内訳
                    int utiwakeOutgoCol = 1;
                    for (Iterator lmIt = outgoUtiwakeSchMap.keySet().iterator(); lmIt.hasNext();) {
                        final String incomeLMcd = (String) lmIt.next();
                        final String utiwakeMName = (String) hrClass._incomeMstLMCdMap.get(incomeLMcd);
                        final Map schlmMoneyMap = (Map)outgoUtiwakeSchMap.get(incomeLMcd);

                        lineCnt = 1;
                        int calUtiwakeTotalOutgo = 0;
                        int calUtiwakeTotalCnt = 0;
                        for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                            final PrintSchregData schregData = (PrintSchregData) itSch.next();
                            if (1 == utiwakeOutgoCol) {
                                svf.VrsOut("ITEM_TITLE_2", "支出計");
                            } else if (2 == utiwakeOutgoCol) {
                                svf.VrsOut("ITEM_TITLE_2", " 内訳");
                            }
                            svf.VrsOut("BLANK", "5");
                            final int mNameLen = utiwakeMName.length();
                            if (mNameLen <= 8) {
                                svf.VrsOut("ITEM2", utiwakeMName);
                            } else {
                                final List mNameList = getNameList(utiwakeMName, 10);

                                int check_len  = mNameList.size();
                                String setField = "";
                                if (check_len > 2) {
                                    svf.VrsOut("ITEM2_3_1", utiwakeMName);
                                    setField = "2_3_";
                                } else if (check_len > 1) {
                                    setField = "2_2_";
                                } else {
                                    setField = "2_1_";
                                }
                                int mNameCnt = 1;
                                for (Iterator itmName = mNameList.iterator(); itmName.hasNext();) {
                                    final String setmName = (String) itmName.next();
                                    svf.VrsOut("ITEM" + setField + mNameCnt, setmName);
                                    mNameCnt++;
                                }
                            }

                            final Integer utiwakeOutgoMoney = (Integer) schlmMoneyMap.get(schregData._schregNo);
                            if (null != utiwakeOutgoMoney) {
                                if (-999 <= utiwakeOutgoMoney && utiwakeOutgoMoney <= 9999) {
                                    svf.VrsOutn("MONEY2_1", lineCnt, String.valueOf(utiwakeOutgoMoney));
                                } else {
                                    svf.VrsOutn("MONEY2_2", lineCnt, String.valueOf(utiwakeOutgoMoney));
                                }
                                calUtiwakeTotalOutgo += utiwakeOutgoMoney;
                                calUtiwakeTotalCnt++;

                            }
                            lineCnt++;
                        }

                        //人数
                        int utiwakeTotalCnt = 0;
                        if (utiwakeOuntgoCntMap.containsKey(incomeLMcd)) {
                            utiwakeTotalCnt = ((Integer)utiwakeOuntgoCntMap.get(incomeLMcd)).intValue();
                        }
                        utiwakeTotalCnt += calUtiwakeTotalCnt;
                        utiwakeOuntgoCntMap.put(incomeLMcd, utiwakeTotalCnt);
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            svf.VrsOutn("MONEY2_1", 46, String.valueOf(utiwakeTotalCnt));
                        }

                        //合計
                        int utiwakeTotalLOutgo = 0;
                        if (utiwakeOutgoMap.containsKey(incomeLMcd)) {
                            utiwakeTotalLOutgo = ((Integer) utiwakeOutgoMap.get(incomeLMcd)).intValue();
                        }
                        utiwakeTotalLOutgo += calUtiwakeTotalOutgo;
                        utiwakeOutgoMap.put(incomeLMcd, new Integer(utiwakeTotalLOutgo));
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            if (-999 <= utiwakeTotalLOutgo && utiwakeTotalLOutgo <= 9999) {
                                svf.VrsOutn("MONEY2_1", 47, String.valueOf(utiwakeTotalLOutgo));
                            } else {
                                   svf.VrsOutn("MONEY2_2", 47, String.valueOf(utiwakeTotalLOutgo));
                            }
                        }

                        svf.VrEndRecord();

                        utiwakeOutgoCol++;
                        colCnt++;
                    }

                    //収入タイトル用 (収入計内訳列が最低2列になるように調整)
                    while (utiwakeOutgoCol <= 2) {
                        if (1 == utiwakeOutgoCol) {
                            svf.VrsOut("ITEM_TITLE_2", "支出計");
                        } else if (2 == utiwakeOutgoCol) {
                            svf.VrsOut("ITEM_TITLE_2", " 内訳");
                        }
                        svf.VrsOut("BLANK", "5");
                        svf.VrEndRecord();
                        utiwakeOutgoCol++;
                        colCnt++;
                    }

                    //差額
                    lineCnt = 1;
                    for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                        final PrintSchregData schregData = (PrintSchregData) itSch.next();

                        svf.VrsOut("ITEM1", "差額");
                        final int sagaku = schregData._incomeMoney - schregData._totalOutGoMoney;

                        svf.VrsOutn("MONEY1", lineCnt, String.valueOf(sagaku));
                        if (0 != sagaku) {
                            sagakuCntGedan++;
                        }
                        totalSagakuGedan += sagaku;
                        lineCnt++;
                    }
                    svf.VrsOut("ITEM1", "差額");
                    if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                        svf.VrsOutn("MONEY1", 46, String.valueOf(sagakuCntGedan));
                        svf.VrsOutn("MONEY1", 47, String.valueOf(totalSagakuGedan));
                    }
                    svf.VrEndRecord();
                    colCnt++;

                    //差額内訳
                    int utiwakeSagakuCol = 1;
                    for (Iterator lmIt = incomeUtiwakeSchMap.keySet().iterator(); lmIt.hasNext();) {
                        final String incomeLMcd = (String) lmIt.next();
                        final String utiwakeMName = (String) hrClass._incomeMstLMCdMap.get(incomeLMcd);
                        final Map schlmIncomeMoneyMap = (Map)incomeUtiwakeSchMap.get(incomeLMcd);
                        final Map schlmOutgoMoneyMap = (Map)outgoUtiwakeSchMap.get(incomeLMcd);

                        lineCnt = 1;
                        int calUtiwakeTotalSagaku = 0;
                        int calUtiwakeSagakuTotalCnt = 0;
                        for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                            final PrintSchregData schregData = (PrintSchregData) itSch.next();
                            if (1 == utiwakeSagakuCol) {
                                svf.VrsOut("ITEM_TITLE_2", "　差額");
                            } else if (2 == utiwakeSagakuCol) {
                                svf.VrsOut("ITEM_TITLE_2", "内訳");
                            }
                            svf.VrsOut("BLANK", "6");
                            final int mNameLen = utiwakeMName.length();
                            if (mNameLen <= 8) {
                                svf.VrsOut("ITEM2", utiwakeMName);
                            } else {
                                final List mNameList = getNameList(utiwakeMName, 10);

                                int check_len  = mNameList.size();
                                String setField = "";
                                if (check_len > 2) {
                                    svf.VrsOut("ITEM2_3_1", utiwakeMName);
                                    setField = "2_3_";
                                } else if (check_len > 1) {
                                    setField = "2_2_";
                                } else {
                                    setField = "2_1_";
                                }
                                int mNameCnt = 1;
                                for (Iterator itmName = mNameList.iterator(); itmName.hasNext();) {
                                    final String setmName = (String) itmName.next();
                                    svf.VrsOut("ITEM" + setField + mNameCnt, setmName);
                                    mNameCnt++;
                                }
                            }
                            Integer utiwakeIncomeMoney = 0;
                            if (schlmIncomeMoneyMap != null && schlmIncomeMoneyMap.containsKey(schregData._schregNo)) {
                                utiwakeIncomeMoney = (Integer) schlmIncomeMoneyMap.get(schregData._schregNo);
                            }
                            Integer utiwakeOutgoMoney = 0;
                            if (schlmOutgoMoneyMap != null && schlmOutgoMoneyMap.containsKey(schregData._schregNo)) {
                                utiwakeOutgoMoney = (Integer) schlmOutgoMoneyMap.get(schregData._schregNo);
                            }
                            final Integer utiwakeSagakuMoney = utiwakeIncomeMoney - utiwakeOutgoMoney;
                            if (null != utiwakeSagakuMoney) {
                                if (-999 <= utiwakeSagakuMoney && utiwakeSagakuMoney <= 9999) {
                                    svf.VrsOutn("MONEY2_1", lineCnt, String.valueOf(utiwakeSagakuMoney));
                                } else {
                                    svf.VrsOutn("MONEY2_2", lineCnt, String.valueOf(utiwakeSagakuMoney));
                                }
                                calUtiwakeTotalSagaku += utiwakeSagakuMoney;
                                if (utiwakeSagakuMoney != 0) {
                                    calUtiwakeSagakuTotalCnt++;
                                }

                            }
                            lineCnt++;
                        }

                        //人数
                        int utiwakeTotalCnt = 0;
                        if (utiwakeSagakuCntMap.containsKey(incomeLMcd)) {
                            utiwakeTotalCnt = ((Integer)utiwakeSagakuCntMap.get(incomeLMcd)).intValue();
                        }
                        utiwakeTotalCnt += calUtiwakeSagakuTotalCnt;
                        utiwakeSagakuCntMap.put(incomeLMcd, utiwakeTotalCnt);
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            svf.VrsOutn("MONEY2_1", 46, String.valueOf(utiwakeTotalCnt));
                        }

                        //合計
                        int utiwakeTotalLSagaku = 0;
                        if (utiwakeSagakuMap.containsKey(incomeLMcd)) {
                            utiwakeTotalLSagaku = ((Integer) utiwakeSagakuMap.get(incomeLMcd)).intValue();
                        }
                        utiwakeTotalLSagaku += calUtiwakeTotalSagaku;
                        utiwakeSagakuMap.put(incomeLMcd, new Integer(utiwakeTotalLSagaku));
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            if (-999 <= utiwakeTotalLSagaku && utiwakeTotalLSagaku <= 9999) {
                                svf.VrsOutn("MONEY2_1", 47, String.valueOf(utiwakeTotalLSagaku));
                            } else {
                                   svf.VrsOutn("MONEY2_2", 47, String.valueOf(utiwakeTotalLSagaku));
                            }
                        }

                        svf.VrEndRecord();

                        utiwakeSagakuCol++;
                        colCnt++;
                    }

                    //収入タイトル用 (収入計内訳列が最低2列になるように調整)
                    while (utiwakeSagakuCol <= 2) {
                        if (1 == utiwakeSagakuCol) {
                            svf.VrsOut("ITEM_TITLE_2", "　差額");
                        } else if (2 == utiwakeSagakuCol) {
                            svf.VrsOut("ITEM_TITLE_2", "内訳");
                        }
                        svf.VrsOut("BLANK", "6");
                        svf.VrEndRecord();
                        utiwakeSagakuCol++;
                        colCnt++;
                    }

                    _hasData = true;
                }
            }
        }
    }

    private static void makeOutgoUtiwakeMap(Map outgoUtiwakeMap, String incomeLMCd, String schregNo, int outgoMoney) {
        //預り金項目毎の合計値を計算し、mapに格納
        if (!outgoUtiwakeMap.containsKey(incomeLMCd)) {
            outgoUtiwakeMap.put(incomeLMCd, new TreeMap());
        }
        Map schlmMoneyMap = (Map)outgoUtiwakeMap.get(incomeLMCd);
        if (!schlmMoneyMap.containsKey(schregNo)) {
            schlmMoneyMap.put(schregNo, 0);
        }
        int schLmMoney = ((Integer)schlmMoneyMap.get(schregNo)).intValue();
        schLmMoney += outgoMoney;
        schlmMoneyMap.put(schregNo, schLmMoney);
    }

    private static String[] getDateArray(final String date) {
       if (date == null || !isYmd(date)) return null;
       return StringUtils.split(date, "-");
    }

    //日付の形式が正しいかチェック
    private static boolean isYmd(String ymd) {
          try{
            // 日付チェック
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(ymd);

            return true;

          }catch(Exception ex){
            return false;
          }
    }

    private static List getNameList(final String sName, final int max) {
        final List rtn = new ArrayList();
        String current = sName;
        while (current.length() > max) {
            rtn.add(current.substring(0, max));
            current = current.substring(max);
        }
        if (current.length() > 0) {
            rtn.add(current);
        }
        return rtn;
    }


    private List getList(final DB2UDB db2, final String lCd) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregSql(lCd);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befGradeHrClass = "";
            int schListCnt = 1;
            int listCnt = 1;
            HrClass setHrClass = null;
            while (rs.next()) {
                final String schregno   = rs.getString("SCHREGNO");
                final String grade      = rs.getString("GRADE");
                final String hrClass    = rs.getString("HR_CLASS");
                final String hrName     = rs.getString("HR_NAME");
                final String attendno   = rs.getString("ATTENDNO");
                final String schName    = rs.getString("SCH_NAME");
                final String staffName  = rs.getString("STAFFNAME");
                final int incomeMoneyTotal   = rs.getInt("INCOME_MONEY");
                final PrintSchregData printSchregData = new PrintSchregData(schregno, grade, hrClass, hrName, attendno, schName, staffName, incomeMoneyTotal);
                printSchregData.setIncomeList(db2, lCd);
                printSchregData.setOutGoList(db2, lCd);
                if (!befGradeHrClass.equals(grade + hrClass)) {
                    setHrClass = new HrClass(grade, hrClass, hrName, staffName);
                    retList.add(setHrClass);
                    schListCnt = 1;
                    listCnt = 1;
                }
                if (listCnt > 45) {
                    schListCnt++;
                    listCnt = 1;
                }
                for (Iterator pri = printSchregData._incomeMap.keySet().iterator(); pri.hasNext();) {
                    final String divlmCd = (String) pri.next();
                    IncomeMoney incomeMoney = (IncomeMoney) printSchregData._incomeMap.get(divlmCd);
                    setHrClass._incomeMstMap.put(incomeMoney._collectDiv + incomeMoney._incomeLCd + incomeMoney._incomeMCd, "2".equals(incomeMoney._collectDiv) ? incomeMoney._mName + "(繰越)" : incomeMoney._mName);
                    setHrClass._incomeMstLMCdMap.put(incomeMoney._incomeLCd + incomeMoney._incomeMCd, incomeMoney._mName);
                }
                for (Iterator pri = printSchregData._outGoMap.keySet().iterator(); pri.hasNext();) {
                    final String lmsCd = (String) pri.next();
                    OutGoMoney outGoMoney = (OutGoMoney) printSchregData._outGoMap.get(lmsCd);
                    final String setKey = CARRY_OVER.equals(outGoMoney._requestNo) ? outGoMoney._requestNo + "_" + outGoMoney._outgoLCd + outGoMoney._outgoMCd : HENKIN.equals(outGoMoney._requestNo) ? outGoMoney._requestNo + outGoMoney._lineNo : outGoMoney._outgoApp + outGoMoney._sortDate + outGoMoney._requestNo + outGoMoney._lineNo;
                    Map outGoMoneyMap = new TreeMap();
                    outGoMoneyMap.put("S_NAME", outGoMoney._sName);
                    outGoMoneyMap.put("OUTGO_APPROVAL", outGoMoney._outgoApp);
                    outGoMoneyMap.put("SORT_DATE", outGoMoney._sortDate);
                    setHrClass._outgosMstMap.put(setKey, outGoMoneyMap);
                }
                final List schList;
                if (setHrClass._schMap.containsKey(String.valueOf(schListCnt))) {
                    schList = (List) setHrClass._schMap.get(String.valueOf(schListCnt));
                } else {
                    schList = new ArrayList();
                }
                schList.add(printSchregData);
                setHrClass._schMap.put(String.valueOf(schListCnt), schList);
                befGradeHrClass = grade + hrClass;
                listCnt++;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSchregSql(final String lCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME AS SCH_NAME, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     SUM(VALUE(INCOME_SCH.INCOME_MONEY, 0)) AS INCOME_MONEY ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN LEVY_REQUEST_INCOME_DAT INCOME ON REGD.YEAR = INCOME.YEAR ");
        stb.append("          AND INCOME.SCHOOLCD    = '" + _param._schoolcd + "' ");
        stb.append("          AND INCOME.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("          AND INCOME.INCOME_L_CD = '" + lCd + "' ");
        stb.append("          AND VALUE(INCOME.INCOME_CANCEL, '0') = '0' ");
        stb.append("     LEFT JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON INCOME.YEAR = INCOME_SCH.YEAR ");
        stb.append("          AND INCOME.SCHOOLCD    = INCOME_SCH.SCHOOLCD ");
        stb.append("          AND INCOME.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ");
        stb.append("          AND INCOME.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ");
        stb.append("          AND INCOME.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ");
        stb.append("          AND INCOME.REQUEST_NO  = INCOME_SCH.REQUEST_NO ");
        stb.append("          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("           AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + _param._gradeHrInState);
        stb.append(" GROUP BY ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     STAFF.STAFFNAME ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _staffName;
        private final Map _schMap;
        private final Map _incomeMstMap;
        private final Map _incomeMstLMCdMap;
        private final Map _outgosMstMap;
        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffName
        ) {
            _grade        		= grade;
            _hrClass      		= hrClass;
            _hrName       		= hrName;
            _staffName    		= staffName;
            _schMap       		= new TreeMap();
            _incomeMstMap 		= new TreeMap();
            _incomeMstLMCdMap 	= new TreeMap();
            _outgosMstMap      	= new TreeMap();
        }
    }

    private class PrintSchregData {
        private final String _schregNo;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendno;
        private final String _schName;
        private final String _staffName;
        private final int _incomeMoney;
        int _totalIncomeMoney;
        int _totalOutGoMoney;
        private final Map _incomeMap;
        private final Map _outGoMap;
        public PrintSchregData(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String schName,
                final String staffName,
                final int incomeMoney
        ) {
            _schregNo    = schregno;
            _grade       = grade;
            _hrClass     = hrClass;
            _hrName      = hrName;
            _attendno    = attendno;
            _schName     = schName;
            _staffName   = staffName;
            _incomeMoney = incomeMoney;
            _totalIncomeMoney = 0;
            _totalOutGoMoney  = 0;
            _incomeMap = new TreeMap();
            _outGoMap = new TreeMap();
        }

        public void setIncomeList(final DB2UDB db2, final String lCd) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getIncomeSql(lCd);
                log.debug("IncomeSQL:" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String collectDiv = rs.getString("COLLECT_DIV");
                    final String incomeLCd = rs.getString("INCOME_L_CD");
                    final String incomeMCd = rs.getString("INCOME_M_CD");
                    final String mName     = StringUtils.defaultString(rs.getString("LEVY_M_NAME"));
                    final String setMName  = mName;
                    final int incomeMoney  = rs.getInt("INCOME_MONEY");
                    final IncomeMoney objIncomeMoney = new IncomeMoney(collectDiv, incomeLCd, incomeMCd, setMName, incomeMoney);
                    _incomeMap.put(collectDiv + incomeLCd + incomeMCd, objIncomeMoney);
                    _totalIncomeMoney += incomeMoney;
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private String getIncomeSql(final String lCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     INCOME.COLLECT_DIV, ");
            stb.append("     INCOME.INCOME_L_CD, ");
            stb.append("     INCOME.INCOME_M_CD, ");
            stb.append("     MMST.LEVY_M_NAME, ");
            stb.append("     SUM(INCOME_SCH.INCOME_MONEY) AS INCOME_MONEY ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_INCOME_DAT INCOME ");
            stb.append("     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON INCOME.YEAR = INCOME_SCH.YEAR ");
            stb.append("          AND INCOME.SCHOOLCD    = INCOME_SCH.SCHOOLCD ");
            stb.append("          AND INCOME.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ");
            stb.append("          AND INCOME.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ");
            stb.append("          AND INCOME.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ");
            stb.append("          AND INCOME.REQUEST_NO  = INCOME_SCH.REQUEST_NO ");
            stb.append("          AND INCOME_SCH.SCHREGNO    = '" + _schregNo + "' ");
            stb.append("     LEFT JOIN LEVY_M_MST MMST ON INCOME.YEAR = MMST.YEAR ");
            stb.append("          AND INCOME.SCHOOLCD    = MMST.SCHOOLCD ");
            stb.append("          AND INCOME.SCHOOL_KIND = MMST.SCHOOL_KIND ");
            stb.append("          AND INCOME.INCOME_L_CD  = MMST.LEVY_L_CD ");
            stb.append("          AND INCOME.INCOME_M_CD  = MMST.LEVY_M_CD ");
            stb.append(" WHERE ");
            stb.append("     INCOME.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND INCOME.SCHOOLCD    = '" + _param._schoolcd + "' ");
            stb.append("     AND INCOME.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND INCOME.INCOME_L_CD = '" + lCd + "' ");
            stb.append("     AND VALUE(INCOME.INCOME_CANCEL, '0') = '0' ");
            stb.append(" GROUP BY ");
            stb.append("     INCOME.COLLECT_DIV, ");
            stb.append("     INCOME.INCOME_L_CD, ");
            stb.append("     INCOME.INCOME_M_CD, ");
            stb.append("     MMST.LEVY_M_NAME ");
            stb.append(" ORDER BY ");
            stb.append("     INCOME.COLLECT_DIV, ");
            stb.append("     INCOME.INCOME_M_CD ");

            return stb.toString();
        }

        public void setOutGoList(final DB2UDB db2, final String lCd) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getOutGoSql(lCd);
                log.debug("OutGoSQL:" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String outgoApp   = rs.getString("OUTGO_APPROVAL");
                    final String sortDate   = rs.getString("SORT_DATE");
                    final String requestNo  = rs.getString("REQUEST_NO");
                    final String lineNo     = rs.getString("LINE_NO");
                    final String outgoLCd   = rs.getString("OUTGO_L_CD");
                    final String outgoMCd   = rs.getString("OUTGO_M_CD");
                    final String outgoSCd   = rs.getString("OUTGO_S_CD");
                    final String outgoIncomeLCd   = rs.getString("INCOME_L_CD");
                    final String outgoIncomeMCd   = rs.getString("INCOME_M_CD");
                    final String headerName = ("2".equals(outgoApp)) ? "*": ""; // 未決済時、頭に"*"を付加
                    final String headerSymbol = StringUtils.defaultIfEmpty(_param._nmstP009Map.get(outgoIncomeLCd + outgoIncomeMCd), "");
                    final String sName      = headerSymbol + headerName + StringUtils.defaultString(rs.getString("LEVY_S_NAME"));
                    final int outGoMoney    = rs.getInt("OUTGO_MONEY");

                    final OutGoMoney goMoney = new OutGoMoney(outgoApp, sortDate, requestNo, lineNo, outgoLCd, outgoMCd, outgoSCd, sName, outGoMoney, outgoIncomeLCd, outgoIncomeMCd);
                    final String setKey = CARRY_OVER.equals(requestNo) ? requestNo + "_" + outgoLCd + outgoMCd: HENKIN.equals(requestNo) ? requestNo + lineNo : outgoApp + sortDate + requestNo + lineNo;
                    _outGoMap.put(setKey, goMoney);
                    _totalOutGoMoney += outGoMoney;
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private String getOutGoSql(final String lCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     OUTGO_SCH.REQUEST_NO, ");
            stb.append("     OUTGO_SCH.LINE_NO, ");
            stb.append("     OUTGO_SCH.OUTGO_L_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_M_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_S_CD, ");
            stb.append("     VALUE(T1.OUTGO_APPROVAL, '2') AS OUTGO_APPROVAL, "); // ソートの為未決済を'2'としておく
            stb.append("     CASE WHEN VALUE(T1.OUTGO_APPROVAL, '0') = '1' THEN T1.OUTGO_DATE ELSE T1.REQUEST_DATE END AS SORT_DATE, ");
            stb.append("     SMST.LEVY_S_NAME, ");
            stb.append("     VALUE(OUTGO_SCH.OUTGO_MONEY, 0) AS OUTGO_MONEY, ");
            stb.append("     T1.INCOME_L_CD, ");
            stb.append("     T1.INCOME_M_CD ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_DAT T1 ");
            stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ON T1.YEAR = OUTGO_SCH.YEAR ");
            stb.append("           AND T1.SCHOOLCD    = OUTGO_SCH.SCHOOLCD ");
            stb.append("           AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ");
            stb.append("           AND T1.OUTGO_L_CD  = OUTGO_SCH.OUTGO_L_CD ");
            stb.append("           AND T1.OUTGO_M_CD  = OUTGO_SCH.OUTGO_M_CD ");
            stb.append("           AND T1.REQUEST_NO  = OUTGO_SCH.REQUEST_NO ");
            stb.append("           AND OUTGO_SCH.SCHREGNO = '" + _schregNo + "' ");
            stb.append("     LEFT JOIN LEVY_S_MST SMST ON OUTGO_SCH.YEAR = SMST.YEAR ");
            stb.append("          AND OUTGO_SCH.SCHOOLCD    = SMST.SCHOOLCD ");
            stb.append("          AND OUTGO_SCH.SCHOOL_KIND = SMST.SCHOOL_KIND ");
            stb.append("          AND OUTGO_SCH.OUTGO_L_CD  = SMST.LEVY_L_CD ");
            stb.append("          AND OUTGO_SCH.OUTGO_M_CD  = SMST.LEVY_M_CD ");
            stb.append("          AND OUTGO_SCH.OUTGO_S_CD  = SMST.LEVY_S_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHOOLCD = '" + _param._schoolcd + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND VALUE(T1.OUTGO_CANCEL, '0') = '0' ");
            stb.append("     AND T1.INCOME_L_CD = '" + lCd + "' ");
            stb.append("     AND OUTGO_SCH.OUTGO_L_CD <> '99' ");
            //返金伝票（合算で出力する）
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '"+ HENKIN +"' as REQUEST_NO, ");
            stb.append("     OUTGO_SCH.LINE_NO, ");
            stb.append("     OUTGO_SCH.OUTGO_L_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_M_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_S_CD, ");
            stb.append("     VALUE(T1.OUTGO_APPROVAL, '2') AS OUTGO_APPROVAL, ");
            stb.append("     CASE WHEN VALUE(T1.OUTGO_APPROVAL, '0') = '1' THEN T1.OUTGO_DATE ELSE T1.REQUEST_DATE END AS SORT_DATE, ");
            stb.append("     SMST.LEVY_S_NAME, ");
            stb.append("     sum(VALUE(OUTGO_SCH.OUTGO_MONEY, 0)) AS OUTGO_MONEY, ");
            stb.append("     T1.INCOME_L_CD, ");
            stb.append("     T1.INCOME_M_CD ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_DAT T1 ");
            stb.append("     LEFT JOIN LEVY_M_MST MMST ON T1.YEAR = MMST.YEAR  ");
            stb.append("     	AND T1.SCHOOLCD     = MMST.SCHOOLCD ");
            stb.append("     	AND T1.SCHOOL_KIND  = MMST.SCHOOL_KIND ");
            stb.append("     	AND T1.INCOME_L_CD  = MMST.LEVY_L_CD ");
            stb.append("     	AND T1.INCOME_M_CD  = MMST.LEVY_M_CD ");
            stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ON T1.YEAR = OUTGO_SCH.YEAR ");
            stb.append("           AND T1.SCHOOLCD    = OUTGO_SCH.SCHOOLCD ");
            stb.append("           AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ");
            stb.append("           AND T1.OUTGO_L_CD  = OUTGO_SCH.OUTGO_L_CD ");
            stb.append("           AND T1.OUTGO_M_CD  = OUTGO_SCH.OUTGO_M_CD ");
            stb.append("           AND T1.REQUEST_NO  = OUTGO_SCH.REQUEST_NO ");
            stb.append("           AND OUTGO_SCH.SCHREGNO = '" + _schregNo + "' ");
            stb.append("     LEFT JOIN LEVY_S_MST SMST ON OUTGO_SCH.YEAR = SMST.YEAR ");
            stb.append("          AND OUTGO_SCH.SCHOOLCD    = SMST.SCHOOLCD ");
            stb.append("          AND OUTGO_SCH.SCHOOL_KIND = SMST.SCHOOL_KIND ");
            stb.append("          AND OUTGO_SCH.OUTGO_L_CD  = SMST.LEVY_L_CD ");
            stb.append("          AND OUTGO_SCH.OUTGO_M_CD  = SMST.LEVY_M_CD ");
            stb.append("          AND OUTGO_SCH.OUTGO_S_CD  = SMST.LEVY_S_CD ");
            stb.append(" WHERE ");
            stb.append("         T1.YEAR        = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHOOLCD    = '" + _param._schoolcd + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND VALUE(T1.OUTGO_APPROVAL, '0') = '1' ");
            stb.append("     AND VALUE(T1.HENKIN_APPROVAL, '0') = '1' "); // CSV取込で完了したデータのみ
            stb.append("     AND VALUE(T1.OUTGO_CANCEL, '0')   = '0' ");
            stb.append("     AND T1.INCOME_L_CD       = '" + lCd + "' ");
            stb.append("     AND OUTGO_SCH.OUTGO_L_CD = '99' ");
            stb.append(" GROUP BY ");
            stb.append("     OUTGO_SCH.LINE_NO, ");
            stb.append("     OUTGO_SCH.OUTGO_L_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_M_CD, ");
            stb.append("     OUTGO_SCH.OUTGO_S_CD, ");
            stb.append("     VALUE(T1.OUTGO_APPROVAL, '2'), ");
            stb.append("     CASE WHEN VALUE(T1.OUTGO_APPROVAL, '0') = '1' THEN T1.OUTGO_DATE ELSE T1.REQUEST_DATE END, ");
            stb.append("     SMST.LEVY_S_NAME, ");
            stb.append("     T1.INCOME_L_CD, ");
            stb.append("     T1.INCOME_M_CD ");
            // 繰越伝票
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '"+ CARRY_OVER +"' as REQUEST_NO, ");
            stb.append("     1 as LINE_NO, ");
            stb.append("     INCOME_L_CD as OUTGO_L_CD, ");
            stb.append("     INCOME_M_CD as OUTGO_M_CD, ");
            stb.append("     '888' as OUTGO_S_CD, ");
            stb.append("     '1' AS OUTGO_APPROVAL, ");
            stb.append("     '9999-12-31' AS SORT_DATE, ");
            stb.append("     '繰越' as LEVY_S_NAME, ");
            stb.append("     sum(value(CARRY_OVER_MONEY, 0)) AS OUTGO_MONEY, ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD ");
            stb.append(" FROM ");
            stb.append("     LEVY_CARRY_OVER_DAT ");
            stb.append(" WHERE ");
            stb.append("         SCHOOLCD    = '" + _param._schoolcd + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND YEAR        = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO    = '" + _schregNo + "' ");
            stb.append("     AND value(CARRY_CANCEL, '0') = '0' ");
            stb.append("     AND INCOME_L_CD = '" + lCd + "' ");
            stb.append(" GROUP BY ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD ");
            stb.append(" ORDER BY ");
            stb.append("     SORT_DATE, ");
            stb.append("     OUTGO_L_CD, ");
            stb.append("     OUTGO_M_CD, ");
            stb.append("     OUTGO_S_CD ");

            return stb.toString();
        }
    }

    private class IncomeMoney {
        private final String _collectDiv;
        private final String _incomeLCd;
        private final String _incomeMCd;
        private final String _mName;
        private final int _incomeMoney;
        public IncomeMoney(
                final String collectDiv,
                final String incomeLCd,
                final String incomeMCd,
                final String mName,
                final int incomeMoney
        ) {
            _collectDiv  = collectDiv;
            _incomeLCd   = incomeLCd;
            _incomeMCd   = incomeMCd;
            _mName       = mName;
            _incomeMoney = incomeMoney;
        }
    }

    private class OutGoMoney {
        private final String _outgoApp;
        private final String _sortDate;
        private final String _requestNo;
        private final String _lineNo;
        private final String _outgoLCd;
        private final String _outgoMCd;
        private final String _outgoSCd;
        private final String _sName;
        private final int _outGoMoney;
        private final String _outGoIncomeLCd;
        private final String _outGoIncomeMCd;
        public OutGoMoney(
                final String outgoApp,
                final String sortDate,
                final String requestNo,
                final String lineNo,
                final String outgoLCd,
                final String outgoMCd,
                final String outgoSCd,
                final String sName,
                final int outGoMoney,
                final String outGoIncomeLCd,
                final String outGoIncomeMCd
        ) {
            _outgoApp   = outgoApp;
            _sortDate   = sortDate;
            _requestNo  = requestNo;
            _lineNo     = lineNo;
            _outgoLCd   = outgoLCd;
            _outgoMCd   = outgoMCd;
            _outgoSCd   = outgoSCd;
            _sName      = sName;
            _outGoMoney = outGoMoney;
            _outGoIncomeLCd = outGoIncomeLCd;
            _outGoIncomeMCd = outGoIncomeMCd;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74660 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
        private final String _grade;
        private final String _schoolcd;
        private final String _schoolKind;
        private final String _incomeLCd;
        private final String[] _gradeHrSelected;
        private final String _gradeHrInState;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;
        private final List _lList;
        private final String _setForm;

        private final Map<String, String> _nmstP009Map;
        private final Map _hasuuMap;
        private final Map _husuuPrintBooleanMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _schoolcd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _incomeLCd = request.getParameter("INCOME_L_CD");
            //リストToリスト
            _gradeHrSelected = request.getParameterValues("CATEGORY_SELECTED");
            _setForm		 = "KNJP972A.frm";
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
            _lList = getLList(db2, _ctrlYear, _incomeLCd, _semester, _grade);
            _nmstP009Map = getNMstP009(db2, _ctrlYear);
            for(Object hoge : _nmstP009Map.keySet()) {
                String key = (String)hoge;
                String valu = _nmstP009Map.get(key);
            }
            _hasuuMap = getHasuu(db2);
            _husuuPrintBooleanMap = new TreeMap();
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
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

        private Map getNMstP009(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtnMap = new TreeMap();
            try {
                final String sql = "SELECT NAMESPARE1 AS LMCD, NAME1 AS SYMBOL FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'P009' AND NAMESPARE2 = '" + _schoolKind + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put(rs.getString("LMCD"), rs.getString("SYMBOL"));
                }
            } catch (SQLException ex) {
                log.debug("getP009 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private List getLList(final DB2UDB db2, final String year, final String incomeLCd, final String semester, final String grade) {
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
                if ("99".equals(incomeLCd)) {
                    stb.append("     INNER JOIN LEVY_REQUEST_INCOME_SCHREG_DAT INCOME_SCH ON T1.YEAR = INCOME_SCH.YEAR ");
                    stb.append("          AND T1.SCHOOLCD    = INCOME_SCH.SCHOOLCD ");
                    stb.append("          AND T1.SCHOOL_KIND = INCOME_SCH.SCHOOL_KIND ");
                    stb.append("          AND T1.INCOME_L_CD = INCOME_SCH.INCOME_L_CD ");
                    stb.append("          AND T1.INCOME_M_CD = INCOME_SCH.INCOME_M_CD ");
                    stb.append("          AND T1.REQUEST_NO = INCOME_SCH.REQUEST_NO ");
                    stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON T1.YEAR = REGD.YEAR ");
                    stb.append("          AND REGD.SEMESTER = '" + semester + "' ");
                    stb.append("          AND REGD.GRADE = '" + grade + "' ");
                    stb.append("          AND REGD.SCHREGNO = INCOME_SCH.SCHREGNO ");
                }
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND T1.SCHOOLCD = '" + _schoolcd + "' ");
                stb.append("     AND T1.SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("     AND VALUE(T1.INCOME_APPROVAL, '0') = '1' ");
                stb.append("     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ");
                if (!"99".equals(incomeLCd)) {
                    stb.append("     AND T1.INCOME_L_CD = '" + incomeLCd + "' ");
                }
                stb.append(" ORDER BY ");
                stb.append("     T1.INCOME_L_CD ");

                final String sql = stb.toString();
                log.debug(" Llistsql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd = rs.getString("INCOME_L_CD");
                    final String lName = rs.getString("LEVY_L_NAME");
                    final Lmst lmst = new Lmst(lCd, lName);
                    retList.add(lmst);
                }
            } catch (SQLException ex) {
                log.debug("CERTIF_SCHOOL_DAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
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
                stb.append("     VALUE(OUTG.OUTGO_APPROVAL, '2') AS OUTGO_APPROVAL, ");
                stb.append("     CASE WHEN VALUE(OUTG.OUTGO_APPROVAL, '0') = '1' THEN OUTG.OUTGO_DATE ELSE OUTG.REQUEST_DATE END AS SORT_DATE, ");
                stb.append("     MSAI.REQUEST_NO, ");
                stb.append("     MSAI.LINE_NO, ");
                stb.append("     value(MSAI.HASUU, 0) AS HASUU ");
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
                stb.append("     AND OUTG.OUTGO_CANCEL   is null ");
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

                final String sql = stb.toString();
                log.debug(" HasuuSQL =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String lCd        = rs.getString("INCOME_L_CD");
                    final String oApp       = rs.getString("OUTGO_APPROVAL");
                    final String sortDate   = rs.getString("SORT_DATE");
                    final String requestNo  = rs.getString("REQUEST_NO");
                    final String lineNo     = rs.getString("LINE_NO");
                    final String hasuu      = rs.getString("HASUU");
                    final String setKey = lCd + oApp + sortDate + requestNo + lineNo;

                    retMap.put(setKey, hasuu);
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

