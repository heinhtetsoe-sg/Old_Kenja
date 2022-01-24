// kanji=漢字
/*
 * $Id: 40e522b50ec4594f095fc2fe208c13b0dcb87555 $
 *
 * 作成日: 2008/01/15 17:57:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 月次処理。
 * @author takaesu
 * @version $Id: 40e522b50ec4594f095fc2fe208c13b0dcb87555 $
 */
public class KNJWP300B extends KNJWP300Abstract {
    /*pkg*/static final Log log = LogFactory.getLog(KNJWP300B.class);
    static final String FQCN = KNJWP300B.class.getName();

    public static void main(final String[] args) {
        final KNJWP300Param param = new KNJWP300Param(args);
        final KNJWP300B batch = new KNJWP300B(); // final KNJWP300Abstract batch = new KNJWP300B();

        // サーブレットからの呼び出しは、
        // batch.doBatch(servlet, request, response)
        // を呼び出す...
        batch.invokeBatch(param);
    }

    /**
     * バッチ処理。
     * @param servlet サーブレット自身
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     */
    public synchronized void doBatch(
            final HttpServlet servlet,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        final KNJWP300Param param = new KNJWP300Param(servlet, request, response);
        invokeBatch(param);
    }

    private void invokeBatch(final KNJWP300Param param) {
        log.fatal("パラメータ: " + param);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        
        // 1. ロック状態の確認。ロック中ならその旨返して終了。
        final boolean isLock;
        try {
            isLock = isLock(param);
        } catch (Exception e) {
            outputHtml(param, "<FONT COLOR='RED'>起動に失敗しました</FONT><BR/>理由: DBエラー");
            return;
        }

        if (isLock) {
            outputHtml(param, "<FONT COLOR='RED'>起動に失敗しました</FONT><BR/>理由: 処理中のため。");
        } else {
            int sts = 0;
            // 2. スレッドで実行
            final HogeThread thread = new HogeThread(param, FQCN);
            if (false) {    // TODO: 実運用での処理件数と時間の兼ね合いで調整せよ
                thread.start();
            } else {
                sts = thread.logic();   // スレッドで実行しない
            }

            // 3. 実行した旨の応答
            if (sts == 0) {
                outputHtml(param, "完了しました");
            } else {
                outputHtml(param, "エラーが発生しました");
            }
        }
    }

    private boolean isLock(final KNJWP300Param param) throws Exception {
        boolean isLock = false;

        DB2UDB db2 = null;
        BatchLock lockObj = null;
        try {
            db2 = param.createDb();
            db2.open();
            lockObj = new BatchLock(db2, param._staffcd, FQCN);
            isLock = lockObj.isLock();
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

        return isLock;
    }

    private void outputHtml(final KNJWP300Param param, final String msg) {
        final PrintWriter out = param.getPrintWriter();
        if (null == out) {
            return;
        }

        final String ContentType = "text/html; charset=Shift_JIS";
        if (null != param._response) {
            param._response.setContentType(ContentType);
        }

        final Calendar cal = Calendar.getInstance();

        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<HTML><HEAD>");
        out.println("<meta http-equiv=\"Pragma\" content=\"no-cache\">");
        out.println("<meta http-equiv=\"Content-Type\" content=\"" + ContentType + "\">");
        out.println("<script language=\"JavaScript\">");
        out.println("function closeMethod() {");
        out.println("    window.opener.btn_submit('');");
        out.println("    window.close();");
        out.println("}");
        out.println("</script>");
        out.println("<!-- 漢字 -->");
        out.println("<TITLE>月次処理</TITLE></HEAD>");

        out.println("<BODY>");
        out.println("<tr>");
        out.println("<td>");
        out.println(msg);
        out.println(cal.getTime());
        out.println("</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td>");
        out.println("<input type='button' name='btn_end' value='終 了' onclick=\"closeMethod();\">");
        out.println("</td>");
        out.println("</tr>");

        out.println("</BODY></HTML>");

        out.flush();
    }

    //========================================================================

    private class HogeThread extends Thread {
        private final KNJWP300Param _param;
        private final String _uniqId;

        HogeThread(final KNJWP300Param param, final String uniqId) {
            _param = param;
            _uniqId = uniqId;
        }

        public void run() {
            logic();
        }

        private int logic() {
            DB2UDB db2 = null;
            BatchLock lockObj = null;
            try {
                db2 = _param.createDb();
                db2.open();
                log.fatal("db open");

                lockObj = new BatchLock(db2, _param._staffcd, _uniqId);
                if (lockObj.lock()) {
                    invoke(_param);
                } else {
                    log.fatal("ロックできない!現在はロック中です。");
                }
            } catch (final Exception e) {
                log.error("Exception:", e);
                return 1;   // 異常終了
            } finally {
                if (null != lockObj) {
                    lockObj.unlock();
                }
                if (null != db2) {
                    db2.commit();
                    db2.close();
                    log.fatal("db close");
                }
            }
            return 0;   // 正常終了
        }

        /**
         * 月次処理
         */
        void invoke(final KNJWP300Param param) throws KNJWP300Exception, SQLException {
            if (param._disableLogic) {
                return;
            }
            final DB2UDB db2 = param.createDb();
            KNJWP300SubAbstract[] subProgram = null;
            try {
                db2.open();
                db2.query("SELECT * FROM SCHOOL_MST");  // nao_package.db.Database#executeUpdate の不具合対策。
    
                subProgram = getSubProgram(param);
            } catch (final Exception e) {
                log.fatal("更新前にエラー発生");
                throw new KNJWP300Exception();
            }

            for (int i = 0; i < subProgram.length; i++) {
                subProgram[i].createSqls();
            }

            try {
                for (int i = 0; i < subProgram.length; i++) {
                    updateExe(db2, subProgram[i].getSqlList());
                }
            } catch (final SQLException e) {
                db2.conn.rollback();
                log.fatal("更新処理中にエラー! rollback した。");
                db2.close();
                throw e;
            }

            db2.commit();
            log.fatal("更新処理が完了した。commit した。");
            db2.close();
        }

        private KNJWP300SubAbstract[] getSubProgram(final KNJWP300Param param) throws Exception {
            if (_param._tightens.equals("0") || _param._tightens.equals("1")) {
                final KNJWP300SubAbstract[] subProgram = {
                        new KNJWP300SubOverPay(param),
                        new KNJWP300SubKeepMoney(param),
                        new KNJWP300SubNoSalse(param),
                        new KNJWP300SubPayMent(param),
                        new KNJWP300SubSales(param),
                        new KNJWP300SubSchPayMent(param),
                        new KNJWP300SubSchSales(param),
                        new KNJWP300SubSalesTightens(param),
                };
                return subProgram;
            } else {
                final KNJWP300SubAbstract[] subProgram = {
                        new KNJWP300SubOverPay(param),
                        new KNJWP300SubKeepMoney(param),
                        new KNJWP300SubNoSalse(param),
                        new KNJWP300SubPayMent(param),
                        new KNJWP300SubSales(param),
                        new KNJWP300SubSalesTightens(param),
                        new KNJWP300SubSchPayMent(param),
                        new KNJWP300SubSchSales(param),
                        new KNJWP300SubKeijou(param),
                };
                return subProgram;
            }
        }

        private void updateExe(final DB2UDB db2, final List exeList) throws SQLException {
            for (final Iterator it = exeList.iterator(); it.hasNext();) {
                final String exeSql = (String) it.next();
                // TODO:以下の処理(executeUpdate)は、本来nao_packageを使うべき
                //      nao_packageの修正が終わり次第、修正要
                executeUpdate(db2, exeSql);
            }
        }

        private int executeUpdate(final DB2UDB db2, final String sql) throws SQLException {
            int sts=0;
            try{
                sts = db2.stmt.executeUpdate( sql );
            } catch(SQLException e) {
                System.err.println("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            } catch(Exception e) {
                System.out.println(">>>"+ e +"<<<");
                e.printStackTrace();
            }
            return sts;
        }
    }

    class KNJWP300Exception extends Exception {
    }
} // KNJWP300B

// eof
