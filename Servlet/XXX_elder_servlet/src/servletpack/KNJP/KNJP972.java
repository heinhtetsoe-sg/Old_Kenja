/*
 * $Id: ada4913e3565e74cfaada19ac69ec15b185ff8c9 $
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

public class KNJP972 {

    private static final Log log = LogFactory.getLog(KNJP972.class);

    private final int MAX_ROW = 33;
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
                final Map totalOutGoMap = new HashMap();
                final Map totalCntMap = new HashMap();
                int totalIncome = 0;
                int totalOutGoCarry = 0;
                int totalCntCarry   = 0;
                int totalOutGoHenkin = 0;
                int totalCntHenkin   = 0;
                int totalOutGoGedan = 0;
                int outGoCntGedan   = 0;
                int totalHasuuGedan = 0;
                int totalSagakuGedan = 0;
                int sagakuCntGedan   = 0;
                final int totalSchMapCnt = hrClass._schMap.size();
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
                        if ("1".equals(_param._useDispSchregno)) { //学籍番号表示 ※桐蔭
	                        svf.VrsOutn("SCHREGNO", lineCnt, schregData._schregNo);
	                        final int nameByte = KNJ_EditEdit.getMS932ByteLength(schregData._schName);
	                        final String fieldSuffix = nameByte <= 16 ? "1" : "2";
	                        svf.VrsOutn("NAME" + fieldSuffix, lineCnt, schregData._schName);
                        } else {
                        	svf.VrsOutn("NAME1", lineCnt, schregData._schName);
                        }
                        lineCnt++;
                    }

                    //収入
                    int incomeRowCnt = 1;
                    for (Iterator itHr = hrClass._incomeMstMap.keySet().iterator(); itHr.hasNext();) {
                        final String incomeDivLMcd = (String) itHr.next();
                        final String mName = (String) hrClass._incomeMstMap.get(incomeDivLMcd);

                        if (incomeRowCnt > MAX_ROW) {
                            svf.VrSetForm(_param._setForm, 4);
                            incomeRowCnt = 1;
                            lineCnt = 1;
                            //名前再セット
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                svf.VrsOutn("NO", lineCnt, schregData._attendno);
                                if ("1".equals(_param._useDispSchregno)) { //学籍番号表示 ※桐蔭
        	                        svf.VrsOutn("SCHREGNO", lineCnt, schregData._schregNo);
        	                        final int nameByte = KNJ_EditEdit.getMS932ByteLength(schregData._schName);
        	                        final String fieldSuffix = nameByte <= 16 ? "1" : "2";
        	                        svf.VrsOutn("NAME" + fieldSuffix, lineCnt, schregData._schName);
                                } else {
                                	svf.VrsOutn("NAME1", lineCnt, schregData._schName);
                                }
                                lineCnt++;
                            }
                        }

                        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
                        svf.VrsOut("TITLE", gengou + "年度　収入-支出細目一覧");
                        svf.VrsOut("SUBTITLE", "(" + lmst._lName + ")");
                        svf.VrsOut("HR_NAME", hrClass._hrName);
                        svf.VrsOut("TEACHER_NAME", hrClass._staffName);
                        svf.VrsOut("DATE", _param.getNow(db2));

                        lineCnt = 1;
                        int calTotalIncome = 0;
                        int calTotalCnt = 0;
                        for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                            final PrintSchregData schregData = (PrintSchregData) itSch.next();
                            if (1 == incomeRowCnt) {
                                svf.VrsOut("ITEM_TITLE", "収");
                            } else if (2 == incomeRowCnt) {
                                svf.VrsOut("ITEM_TITLE", "入");
                            } else if (3 == incomeRowCnt) {
                                svf.VrsOut("ITEM_TITLE", "明");
                            } else if (4 == incomeRowCnt) {
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

                        incomeRowCnt++;
                    }

                    //収入タイトル用
                    while (incomeRowCnt <= 4) {
                        if (1 == incomeRowCnt) {
                            svf.VrsOut("ITEM_TITLE", "収");
                        } else if (2 == incomeRowCnt) {
                            svf.VrsOut("ITEM_TITLE", "入");
                        } else if (3 == incomeRowCnt) {
                            svf.VrsOut("ITEM_TITLE", "明");
                        } else if (4 == incomeRowCnt) {
                            svf.VrsOut("ITEM_TITLE", "細");
                        }
                        svf.VrEndRecord();
                        incomeRowCnt++;
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
                    incomeRowCnt++;

                    //支出
                    int rowCnt = incomeRowCnt;
                    for (Iterator itHr = hrClass._outgosMstMap.keySet().iterator(); itHr.hasNext();) {
                        final String requestLineCd = (String) itHr.next();
                        final String sName = (String) hrClass._outgosMstMap.get(requestLineCd);

                        if (rowCnt > MAX_ROW) {
                            svf.VrSetForm(_param._setForm, 4);
                            rowCnt = 1;
                            lineCnt = 1;
                            //名前再セット
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                svf.VrsOutn("NO", lineCnt, schregData._attendno);
                                if ("1".equals(_param._useDispSchregno)) { //学籍番号表示 ※桐蔭
        	                        svf.VrsOutn("SCHREGNO", lineCnt, schregData._schregNo);
        	                        final int nameByte = KNJ_EditEdit.getMS932ByteLength(schregData._schName);
        	                        final String fieldSuffix = nameByte <= 16 ? "1" : "2";
        	                        svf.VrsOutn("NAME" + fieldSuffix, lineCnt, schregData._schName);
                                } else {
                                	svf.VrsOutn("NAME1", lineCnt, schregData._schName);
                                }
                                lineCnt++;
                            }
                        }

                        // 繰越は最後の列 - 1列に出力する
                        if (0 == requestLineCd.indexOf(CARRY_OVER) && rowCnt != MAX_ROW - 1) {
                            continue;
                        }

                        // 返金は最後の列に出力する
                        if (0 == requestLineCd.indexOf(HENKIN) && rowCnt != MAX_ROW) continue;

                        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear));
                        svf.VrsOut("TITLE", gengou + "年度　収入-支出細目一覧");
                        svf.VrsOut("SUBTITLE", "(" + lmst._lName + ")");
                        svf.VrsOut("HR_NAME", hrClass._hrName);
                        svf.VrsOut("TEACHER_NAME", hrClass._staffName);
                        svf.VrsOut("DATE", _param.getNow(db2));

                        lineCnt = 1;
                        int calTotalOutGo = 0;
                        int calTotalCnt = 0;
                        for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                            final PrintSchregData schregData = (PrintSchregData) itSch.next();
                            if (12 == rowCnt) {
                                svf.VrsOut("ITEM_TITLE", "支");
                            } else if (14 == rowCnt) {
                                svf.VrsOut("ITEM_TITLE", "出");
                            } else if (16 == rowCnt) {
                                svf.VrsOut("ITEM_TITLE", "明");
                            } else if (18 == rowCnt) {
                                svf.VrsOut("ITEM_TITLE", "細");
                            }
                            final int sNameLen = sName.length();
                            if (sNameLen <= 8) {
                                svf.VrsOut("ITEM2", sName);
                            } else {
                                final List snameList = getNameList(sName, 10);

                                int check_len  = snameList.size();
                                String setField = "";
                                if (check_len > 2) {
                                    svf.VrsOut("ITEM2_3_1", sName);
                                    setField = "2_3_";
                                } else if (check_len > 1) {
                                    setField = "2_2_";
                                } else {
                                    setField = "2_1_";
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
                                    svf.VrsOutn("MONEY2_1", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                } else {
                                    svf.VrsOutn("MONEY2_2", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                }
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
                            svf.VrsOutn("MONEY2_1", 46, String.valueOf(totalCnt));
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
                                svf.VrsOutn("MONEY2_1", 47, String.valueOf(totaloutGo));
                            } else {
                                svf.VrsOutn("MONEY2_2", 47, String.valueOf(totaloutGo));
                            }
                        }

                        //端数
                        final String key = lmst._lCd + requestLineCd;
                        final String hasuu = (String) _param._hasuuMap.get(key);
                        final int setHasuu = hasuu == null ? 0: Integer.parseInt(hasuu);
                        if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                            if (!_param._husuuPrintBooleanMap.containsKey(key)) { // 一度印字した端数は印字しない
                                svf.VrsOutn("MONEY2_1", 48, String.valueOf(setHasuu));
                                totalHasuuGedan += setHasuu;
                                if (0 < setHasuu) {
                                    _param._husuuPrintBooleanMap.put(key, "1");
                                }
                            } else {
                                svf.VrsOutn("MONEY2_1", 48, "0");
                            }
                        }

                        svf.VrEndRecord();

                        rowCnt++;
                    }

                    //空白レコード埋め＋返金セット（最終列）
                    for (int i = rowCnt; i <= MAX_ROW; i++) {
                        if (12 == i) {
                            svf.VrsOut("ITEM_TITLE", "支");
                        } else if (14 == i) {
                            svf.VrsOut("ITEM_TITLE", "出");
                        } else if (16 == i) {
                            svf.VrsOut("ITEM_TITLE", "明");
                        } else if (18 == i) {
                            svf.VrsOut("ITEM_TITLE", "細");
                        }

                        //最後の列に返金、(最後-1)の列に繰越を出力
                        final String henkinKey = HENKIN + '1'; // 1はLINECD
                        if (i == MAX_ROW - 1 && null != hrClass._outgosMstMap.get(CARRY_OVER)) {
                            final String sName = (String) hrClass._outgosMstMap.get(CARRY_OVER);

                            lineCnt = 1;
                            totalOutGoCarry = 0;
                            totalCntCarry = 0;
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                final int sNameLen = sName.length();
                                if (sNameLen <= 8) {
                                    svf.VrsOut("ITEM2", sName);
                                } else {
                                    final List snameList = getNameList(sName, 10);

                                    int check_len  = snameList.size();
                                    String setField = "";
                                    if (check_len > 2) {
                                        svf.VrsOut("ITEM2_3_1", sName);
                                        setField = "2_3_";
                                    } else if (check_len > 1) {
                                        setField = "2_2_";
                                    } else {
                                        setField = "2_1_";
                                    }
                                    int sNameCnt = 1;
                                    for (Iterator itSname = snameList.iterator(); itSname.hasNext();) {
                                        final String setSname = (String) itSname.next();
                                        svf.VrsOut("ITEM" + setField + sNameCnt, setSname);
                                        sNameCnt++;
                                    }
                                }

                                final OutGoMoney outGoMoney = (OutGoMoney) schregData._outGoMap.get(CARRY_OVER);
                                if (null != outGoMoney) {
                                    if (-999 <= outGoMoney._outGoMoney && outGoMoney._outGoMoney <= 9999) {
                                        svf.VrsOutn("MONEY2_1", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                    } else {
                                        svf.VrsOutn("MONEY2_2", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                    }
                                    totalOutGoCarry += outGoMoney._outGoMoney;
                                    totalCntCarry++;
                                }
                                lineCnt++;
                            }

                            //人数
                            if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                                svf.VrsOutn("MONEY2_1", 46, String.valueOf(totalCntCarry));
                            }

                            //合計
                            if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                                if (-999 <= totalOutGoCarry && totalOutGoCarry <= 9999) {
                                    svf.VrsOutn("MONEY2_1", 47, String.valueOf(totalOutGoCarry));
                                } else {
                                    svf.VrsOutn("MONEY2_2", 47, String.valueOf(totalOutGoCarry));
                                }
                            }
                            svf.VrEndRecord();

                        } else if (i == MAX_ROW && null != hrClass._outgosMstMap.get(henkinKey)) {
                            final String sName = (String) hrClass._outgosMstMap.get(henkinKey);

                            lineCnt = 1;
                            totalOutGoHenkin = 0;
                            totalCntHenkin = 0;
                            for (Iterator itSch = schList.iterator(); itSch.hasNext();) {
                                final PrintSchregData schregData = (PrintSchregData) itSch.next();
                                final int sNameLen = sName.length();
                                if (sNameLen <= 8) {
                                    svf.VrsOut("ITEM2", sName);
                                } else {
                                    final List snameList = getNameList(sName, 10);

                                    int check_len  = snameList.size();
                                    String setField = "";
                                    if (check_len > 2) {
                                        svf.VrsOut("ITEM2_3_1", sName);
                                        setField = "2_3_";
                                    } else if (check_len > 1) {
                                        setField = "2_2_";
                                    } else {
                                        setField = "2_1_";
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
                                        svf.VrsOutn("MONEY2_1", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                    } else {
                                        svf.VrsOutn("MONEY2_2", lineCnt, String.valueOf(outGoMoney._outGoMoney));
                                    }
                                    totalOutGoHenkin += outGoMoney._outGoMoney;
                                    totalCntHenkin++;
                                }
                                lineCnt++;
                            }

                            //人数
                            if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                                svf.VrsOutn("MONEY2_1", 46, String.valueOf(totalCntHenkin));
                            }

                            //合計
                            if (schKey.equals(String.valueOf(totalSchMapCnt))) {
                                if (-999 <= totalOutGoHenkin &&totalOutGoHenkin <= 9999) {
                                    svf.VrsOutn("MONEY2_1", 47, String.valueOf(totalOutGoHenkin));
                                } else {
                                    svf.VrsOutn("MONEY2_2", 47, String.valueOf(totalOutGoHenkin));
                                }
                            }
                            svf.VrEndRecord();

                        } else {
                            svf.VrsOut("BLANK", "あ");
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
                    _hasData = true;
                }
            }
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
                    setHrClass._incomeMstMap.put(incomeMoney._collectDiv + incomeMoney._incomeLCd + incomeMoney._incomeMCd, incomeMoney._mName);
                }
                for (Iterator pri = printSchregData._outGoMap.keySet().iterator(); pri.hasNext();) {
                    final String lmsCd = (String) pri.next();
                    OutGoMoney outGoMoney = (OutGoMoney) printSchregData._outGoMap.get(lmsCd);
                    final String setKey = CARRY_OVER.equals(outGoMoney._requestNo) ? outGoMoney._requestNo: HENKIN.equals(outGoMoney._requestNo) ? outGoMoney._requestNo + outGoMoney._lineNo : outGoMoney._outgoApp + outGoMoney._sortDate + outGoMoney._requestNo + outGoMoney._lineNo;
                    setHrClass._outgosMstMap.put(setKey, outGoMoney._sName);
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
        private final Map _outgosMstMap;
        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffName
        ) {
            _grade        = grade;
            _hrClass      = hrClass;
            _hrName       = hrName;
            _staffName    = staffName;
            _schMap       = new TreeMap();
            _incomeMstMap = new TreeMap();
            _outgosMstMap      = new TreeMap();
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
                    final String setMName  = "2".equals(collectDiv) ? mName + "(繰越)" : mName;
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
                    final String headerName = ("2".equals(outgoApp)) ? "*": ""; // 未決済時、頭に"*"を付加
                    final String sName      = headerName + StringUtils.defaultString(rs.getString("LEVY_S_NAME"));
                    final int outGoMoney    = rs.getInt("OUTGO_MONEY");
                    final OutGoMoney goMoney = new OutGoMoney(outgoApp, sortDate, requestNo, lineNo, outgoLCd, outgoMCd, outgoSCd, sName, outGoMoney);
                    final String setKey = CARRY_OVER.equals(requestNo) ? requestNo: HENKIN.equals(requestNo) ? requestNo + lineNo : outgoApp + sortDate + requestNo + lineNo;
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
            stb.append("     VALUE(OUTGO_SCH.OUTGO_MONEY, 0) AS OUTGO_MONEY ");
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
            stb.append("     sum(VALUE(OUTGO_SCH.OUTGO_MONEY, 0)) AS OUTGO_MONEY ");
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
            stb.append("     SMST.LEVY_S_NAME ");
            // 繰越伝票
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '"+ CARRY_OVER +"' as REQUEST_NO, ");
            stb.append("     1 as LINE_NO, ");
            stb.append("     '88' as OUTGO_L_CD, ");
            stb.append("     '88' as OUTGO_M_CD, ");
            stb.append("     '888' as OUTGO_S_CD, ");
            stb.append("     '1' AS OUTGO_APPROVAL, ");
            stb.append("     '9999-12-31' AS SORT_DATE, ");
            stb.append("     '繰越' as LEVY_S_NAME, ");
            stb.append("     sum(value(CARRY_OVER_MONEY, 0)) AS OUTGO_MONEY ");
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
            stb.append("     INCOME_L_CD ");
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
        public OutGoMoney(
                final String outgoApp,
                final String sortDate,
                final String requestNo,
                final String lineNo,
                final String outgoLCd,
                final String outgoMCd,
                final String outgoSCd,
                final String sName,
                final int outGoMoney
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
        private final String _useDispSchregno;
        private final String _setForm;

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
            _useDispSchregno = request.getParameter("useDispSchregno_KNJP972");
            _setForm		 = "1".equals(_useDispSchregno) ? "KNJP972_2.frm" : "KNJP972.frm"; 
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

