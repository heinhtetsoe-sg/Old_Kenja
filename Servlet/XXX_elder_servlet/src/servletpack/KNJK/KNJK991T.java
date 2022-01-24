// kanji=漢字
/*
 * $Id: 87314584b30a1e9ad2d7d0945ac02a080ada90a3 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJK;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import nao_package.db.DB2UDB;
import servletpack.KNJG.KNJG030_2;
import servletpack.KNJI.KNJI060_1;
import servletpack.KNJI.KNJI060_2;
import servletpack.KNJI.KNJI070_1;
import servletpack.KNJI.KNJI070_2;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;


/*
 *  学校教育システム 賢者 [進路情報管理] 高校用調査書
 */
public class KNJK991T {
    private static final Log log = LogFactory.getLog(KNJK991T.class);

    private static final Set _threadSet = new HashSet();

    private static int OUTPUT_TYOUSASHO_SHINGAKU = 1;
    private static int OUTPUT_TYOUSASHO_SHUSHOKU = 2;
    private static int OUTPUT_SEISEKI_SHOMEISHO_JPN = 3;
    private static int OUTPUT_SEISEKI_SHOMEISHO_ENG = 4;
    private static int OUTPUT_TANNI_SHUTOKU_SHOMEISHO = 5;

    private static final String MS932 = "MS932";
    private static final String Shift_JIS = "Shift_JIS";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Param param = createParam(request);

        // ＤＢ接続
        final DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2); //Databaseクラスを継承したクラス
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        param.load(db2);

        InputStream is = null;
        OutputStream os = null;
        try {
            final List regdList = OutputPdfThread.getRegdHList(OutputPdfThread.getRegdList(db2, param._year, param));
            if (regdList.size() == 0) {

                response.setContentType("text/html; charset=utf-8");

                final byte[] buff = "<script type='text/javascript'>alert('対象データがありません。');window.close();</script>".getBytes();

                os = new BufferedOutputStream(response.getOutputStream());
                os.write(buff);
                os.flush();

            } else {
                final String filename = param.getZipFilename();
                final String filenameEncoding = isIE(param) ? "MS932" : "UTF-8";
//                log.info(" filenameEncoding = " + filenameEncoding + " / " + filename);
                final String encodedFilename = new String(filename.getBytes(filenameEncoding), "ISO8859-1");
                response.setContentType("text/octet-stream");
                response.setHeader("Accept-Ranges", "none");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
                response.setHeader("Content-Transfer-Encoding", "binary");

                final OutputPdfThread th = new OutputPdfThread(db2, param, null, regdList);
                final File file = th.createZipFile();

                final long filelength = file.length();
                //response.setContentLength((int) filelength);
                response.setHeader("Content-Length", String.valueOf(filelength));
                log.info(" file length = " + filelength);

                final byte[] buff = new byte[1024];
                is = new BufferedInputStream(new FileInputStream(file), buff.length);

//                int totalSize = 0;
                int readSize = 0;
                os = new BufferedOutputStream(response.getOutputStream());
                while ((readSize = is.read(buff)) > 0) {
                    os.write(buff, 0, readSize);
//                    totalSize += readSize;
//                    if ((totalSize % 100 == 0) || filelength - totalSize < 10000) {
//                        log.info(" read size = " + readSize + "(" + totalSize + ")");
//                    }
                }
                os.flush();
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static boolean isIE(final Param param) {
        boolean isIE = true; // デフォルトはtrue
        isIE = -1 != param._headerUserAgent.indexOf("Trident") || -1 != param._headerUserAgent.indexOf("MSIE");
        log.info(" " + param._headerUserAgent + " -> isIE? " + isIE);
        return isIE;
    }

//    // 他の動作中のスレッドに割り込みする（停止する）
//    private void interruptOtherThreads() {
//        final List names = new ArrayList();
//        for (final Iterator it = _threadSet.iterator(); it.hasNext();) {
//            final Thread th = (Thread) it.next();
//            th.interrupt();
//            it.remove();
//            names.add(th.getName());
//        }
//        if (names.size() == 0) {
//            log.info(" none interrupt");
//        } else {
//            log.info(" interrupt " + names);
//        }
//    }

//    private static void preprocess(final Param param) {
//        try {
//            // 以前に作成したファイルを削除する
//            final File beforeFile = new File(param.getReceiveSendZipFilePath());
//            if (beforeFile.exists()) {
//                beforeFile.delete();
//            }
//        } catch (IOException e) {
//            log.error("exception!", e);
//        }
//
//        FileOutputStream fos = null;
//        try {
//            // 「出力中」ファイルを作成する
//            final File creatingFile = new File(param.getReceiveSendZipProcessingFilePath());
//            fos = new FileOutputStream(creatingFile);
//            byte[] bs = "出力中".getBytes(MS932);
//            fos.write(bs, 0, bs.length);
//        } catch (IOException e) {
//            log.error("exception!", e);
//        } finally {
//            IOUtils.closeQuietly(fos);
//        }
//    }

//    private void invokeThread(final DB2UDB db2, final Param param) {
//        final Thread th = new OutputPdfThread(db2, param, _threadSet);
//        th.setName(param._year + "_" + param._kindname);
//        log.info(" threadSet size = " + _threadSet.size() + "/" + _threadSet);
//        _threadSet.add(th);
//        th.start();
//    }

//    private void setMessage(final HttpServletResponse response, final Param param) throws IOException {
//        response.setContentType("text/html; charset=utf-8");
//        final String message = "<script type='text/javascript'>alert('" + param._kindname + " " + param._year + "年度 出力処理を開始しました。');window.open('', '_self').close();</script>";
//        response.setContentLength(message.getBytes().length);
//        final PrintStream ps = new PrintStream(response.getOutputStream());
//        ps.println(message);
//    }

    private static class OutputPdfThread extends Thread {
        final DB2UDB _db2;
        final Param _param;
        final Set _threadSet;
        final List _regdList;
        OutputPdfThread(final DB2UDB db2, final Param param, final Set threadSet, final List regdList) {
            _db2 = db2;
            _param = param;
            _threadSet = threadSet;
            _regdList = regdList;
        }

        public File createZipFile() {
            log.info(" スレッド開始:" + getName());
            final long startTime = System.currentTimeMillis();
            File file = createZipFile(_db2, _param, _regdList);

            final long endTime = System.currentTimeMillis();
            Util.logElapsedTime(startTime, endTime);
            return file;
        }

        /**
         *
         */
        public void run() {
            setReceiveSendFile(_param, createZipFile());

            setThreadSet(this);
        }

        private static List getRegdHList(final List regdList) {
            final List hrRegdListList = new ArrayList();
            Map before = null;
            for (int i = 0; i < regdList.size(); i++) {
                final Map regd = (Map) regdList.get(i);
                final String grade = KnjDbUtils.getString(regd, "GRADE");
                final String hrClass = KnjDbUtils.getString(regd, "HR_CLASS");
                final List hrRegdList;
                if (null == before || null != grade && !grade.equals(KnjDbUtils.getString(before, "GRADE")) || null != hrClass && !hrClass.equals(KnjDbUtils.getString(before, "HR_CLASS"))) {
                    hrRegdList = new ArrayList();
                    hrRegdListList.add(hrRegdList);
                } else {
                    hrRegdList = (List) hrRegdListList.get(hrRegdListList.size() - 1);
                }
                hrRegdList.add(regd);
                before = regd;
            }
            log.info(" hrRegdListList : " + hrRegdListList.size());
            return hrRegdListList;
        }

        private boolean isConvertTarget(final String ch) {
            boolean isTarget = false;
            try {
//                final String[] _3f = {"3F"};
//                final String[] serverEnc = castb(ch.getBytes());
                final String[] ms932Enc = castb(ch.getBytes("MS932"));
                int ms932EncInt = 0;
                for (int i = 0; i < ms932Enc.length; i++) {
                    ms932EncInt += Integer.parseInt(ms932Enc[i], 16) << (8 * (ms932Enc.length - (i + 1)));
                }
                final boolean isGaiji = 0xF040 <= ms932EncInt && ms932EncInt < 0xFA30;
                if (isGaiji) {
                    log.info(" gaiji " + Integer.toString(ms932EncInt, 16) + " => " + isGaiji);
                }
//                isTarget = ArrayUtils.isEquals(serverEnc, _3f) || ArrayUtils.isEquals(ms932Enc, _3f) || isGaiji; // 変換しない
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return isTarget;
        }

        public File createZipFile(final DB2UDB db2, final Param param, final List regdListList) {
            File zipFile = null;
            try {
                for (int hri = 0; hri < regdListList.size(); hri++) {
                    final List hrRegdList = (List) regdListList.get(hri);

                    final Map regd0 = (Map) hrRegdList.get(0);
                    final String hrname = KnjDbUtils.getString(regd0, "HR_NAME");
                    if (null == hrname) {
                        continue;
                    }
                    final File tmpDir = param.getTmpPdfFileDir(hrname);
                    // 以前に作成したものを削除
                    if (tmpDir.exists()) {
                        Util.deleteRecursively(tmpDir);
                    }
                }

                hrloop:
                for (int hri = 0; hri < regdListList.size(); hri++) {
                    final List hrRegdList = (List) regdListList.get(hri);

                    final Map regd0 = (Map) hrRegdList.get(0);
                    final String hrname = KnjDbUtils.getString(regd0, "HR_NAME");
                    if (null == hrname) {
                        log.error("null hrname : " + regd0);
                        continue;
                    }

                    for (int i = 0; i < hrRegdList.size(); i++) {
                        final Map regd = (Map) hrRegdList.get(i);

                        if (isInterrupted()) {
                            log.info(getName() + " is interrupted. ");
                            break hrloop;
                        }

                        final String schregno = KnjDbUtils.getString(regd, "SCHREGNO");
//                        final String filename = StringUtils.defaultString(KnjDbUtils.getString(regd, "ATTENDNO")) + " " + StringUtils.defaultString(KnjDbUtils.getString(regd, "NAME"));
                        final String filename = schregno + " " + StringUtils.defaultString(KnjDbUtils.getString(regd, "NAME"));
                        final StringBuffer checkedfilename = new StringBuffer();
                        for (int ni = 0; ni < filename.length(); ni++) {
                            final String ch = String.valueOf(filename.charAt(ni));

                            final String[] serverEnc = castb(ch.getBytes());
                            final String[] ms932Enc = castb(ch.getBytes("MS932"));
                            if (param._debugSchregnoList.contains(schregno)) {
                                log.info(" schregno " + schregno + " ch = " + ch + ", ch.bytes = " + ArrayUtils.toString(serverEnc) + ", ch.bytes MS932 = " + ArrayUtils.toString(ms932Enc) + ", ch bytes Shift_JIS = " + ArrayUtils.toString(castb(ch.getBytes("Shift_JIS"))));
                            }
//                            if (isConvertTarget(ch)) {
//                                log.info(" 文字化置換 " + schregno + " " + filename + " at " + ni);
//                                checkedfilename.append("？");
//                            } else {
                                checkedfilename.append(ch);
//                            }
                        }

                        final String filepath = param.getTmpPdfFileDir(hrname).getAbsolutePath() + "/" + checkedfilename + param._fileext;
                        log.info(" filepath = " + filepath);
                        final FileOutputStream fos = new FileOutputStream(new File(filepath));

                        final PrintFileSession pfs = PrintFileSession.getPrintSession(db2, fos, param);

                        log.info(" ROW_NUM = " + KnjDbUtils.getString(regd, "ROW_NUM") + " / " + KnjDbUtils.getString(regd, "TOTAL"));
                        pfs.print(db2, param, regd);
                        pfs.closeSession(param);
                    }
                }

                zipFile = new File(param.getTmpZipFilePath());
                final File target = param.getTmpCompessTargetDirectory();
                ZipCompressUtils.compressDirectory(zipFile, target, MS932);

            } catch (IOException e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                db2.close();
            }
            return zipFile;
        }

        public void setReceiveSendFile(final Param param, final File zipFile) {
            try {
                final File fileCreating = new File(param.getReceiveSendZipProcessingFilePath());
                if (!fileCreating.exists()) {
                    log.warn("ファイルがない:" + fileCreating.getAbsolutePath());
                } else {
                    log.debug("ファイル削除:" + fileCreating.getAbsolutePath());
                    fileCreating.delete();
                }

                final String receiveSendFilePath = param.getReceiveSendZipFilePath();
                log.info(" receiveSendFilePath = " + receiveSendFilePath);
                Util.copyFile(zipFile, new File(receiveSendFilePath));

            } catch (IOException e) {
                log.error("exception!", e);
            }
        }

        private String[] castb(byte[] bytes) {
            final String[] rtn = new String[bytes.length];
            final String tab = "0123456789ABCDEF";
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                int n = (int) b + (b < 0 ? 256 : 0);
                int u = n / 16;
                int l = n % 16;
                rtn[i] = String.valueOf(tab.charAt(u)) + String.valueOf(tab.charAt(l));
            }
            return rtn;
        }

        private static List getRegdList(final DB2UDB db2, final String year, final Param param) {
            String sql = "";
            sql += " SELECT T1.GRADE, T1.HR_CLASS, HDAT.HR_NAME, BASE.NAME, T1.SCHREGNO, T2.SEMESTER, ROW_NUMBER() OVER(ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO) AS ROW_NUM ";
            sql += " FROM GRD_REGD_DAT T1 ";

            sql += " INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM GRD_REGD_DAT GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO ";
            sql += "     AND T2.YEAR = T1.YEAR ";
            sql += "     AND T2.SEMESTER = T1.SEMESTER ";

            sql += " INNER JOIN GRD_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ";
            sql += "     AND HDAT.SEMESTER = T1.SEMESTER ";
            sql += "     AND HDAT.GRADE = T1.GRADE ";
            sql += "     AND HDAT.HR_CLASS = T1.HR_CLASS ";

            sql += " INNER JOIN GRD_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ";

            sql += " WHERE FISCALYEAR(BASE.GRD_DATE) = '" + year + "' AND T1.YEAR = '" + year + "' ";

            final int ioutput = Integer.parseInt(param._output);
            if (ioutput == OUTPUT_TYOUSASHO_SHINGAKU || ioutput == OUTPUT_TYOUSASHO_SHUSHOKU) {
                sql += " AND T1.GRADE >= '03' ";
            }

            if (param._debugSchregnoList.size() > 0) {
                sql += " AND ( ";

                for (int i = 0; i < param._debugSchregnoList.size(); i++) {
                    if (i > 0) {
                        sql += " OR ";
                    }
                    sql += " T1.SCHREGNO = '"  + param._debugSchregnoList.get(i) + "' ";
                }
                sql += " ) ";
            }

            sql += " ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

            List rtn = KnjDbUtils.query(db2, sql);
            if (param._debugCount > 0) {
                rtn = rtn.subList(0, Math.min(param._debugCount, rtn.size()));
                log.warn(" DEBUG:reduce size = " + rtn.size());
            }
            for (int i = 0; i < rtn.size(); i++) {
                final Map regd = (Map) rtn.get(i);
                regd.put("TOTAL", String.valueOf(rtn.size()));
            }
            log.info(" regd list size = " + rtn.size());
            return rtn;
        }

        private void setThreadSet(final Thread th) {
            synchronized (_threadSet) {
                if (!_threadSet.contains(th)) {
                    log.error("終了したスレッドが登録されていない:" + _threadSet);
                } else {
                    _threadSet.remove(th);
                    log.info(" 終了:" + th.getName());
                    if (_threadSet.isEmpty()) {
                        log.info(" 全てのスレッドが終了しました。");
                    } else {
                        log.info(" 残りスレッド: " + _threadSet.size() + "件, " + _threadSet);
                    }
                }
            }
        }
    }

    private static class PrintFileSession {

        private static String KNJI060_1 = "KNJI060_1";
        private static String KNJI060_2 = "KNJI060_2";
        private static String KNJI070_1 = "KNJI070_1";
        private static String KNJI070_2 = "KNJI070_2";
        private static String KNJG030_2 = "KNJG030_2";

        final Vrw32alpWrap _svf;
        final Map _map = new HashMap();

        PrintFileSession(final Param param, final OutputStream os) {
            _svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            _svf.VrInit();                         //クラスの初期化
            _svf.VrSetSpoolFileStream(os);
        }

        private static PrintFileSession getPrintSession(final DB2UDB db2, final OutputStream os, final Param param) {
            final PrintFileSession session = new PrintFileSession(param, os);
            final String useSyojikou3 = (String) param._paramap.get("useSyojikou3");
            final int ioutput = Integer.parseInt(param._output);
            final String hyoteiDummy = null;
            if (ioutput == OUTPUT_TYOUSASHO_SHINGAKU) {
                final KNJI060_1 pobj = new KNJI060_1(db2, session._svf, param.definecode, useSyojikou3);
                pobj.pre_stat(param._hyotei, param._paramap);
                session._map.put(KNJI060_1, pobj);
            } else if (ioutput == OUTPUT_TYOUSASHO_SHUSHOKU) {
                final KNJI060_2 pobj = new KNJI060_2(db2, session._svf, param.definecode, useSyojikou3);
                pobj.pre_stat(param._hyotei, param._paramap);
                session._map.put(KNJI060_2, pobj);
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_JPN) {
                final KNJI070_1 pobj = new KNJI070_1(db2, session._svf, param.definecode);
                pobj.pre_stat(hyoteiDummy, param._paramap);
                session._map.put(KNJI070_1, pobj);
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_ENG) {
                final KNJI070_2 pobj = new KNJI070_2(db2, session._svf, param.definecode);
                pobj.pre_stat(hyoteiDummy, param._paramap);
                session._map.put(KNJI070_2, pobj);
            } else if (ioutput == OUTPUT_TANNI_SHUTOKU_SHOMEISHO) {
                final KNJG030_2 pobj = new KNJG030_2(db2, session._svf, param.definecode);
                pobj.pre_stat(hyoteiDummy, param._paramap);
                session._map.put(KNJG030_2, pobj);
            }
            return session;
        }

        private void setCertifKind(final DB2UDB db2, final Param param, final String schregno) {
            String certifKind = null;
            final boolean isGrd = useCertifKindGrd(db2, param._year, schregno, null); // param._date);
            final int ioutput = Integer.parseInt(param._output);
            if (ioutput == OUTPUT_TYOUSASHO_SHINGAKU) {
                if (isGrd) {
                    certifKind = "025";
                } else {
                    certifKind = "008";
                }
            } else if (ioutput == OUTPUT_TYOUSASHO_SHUSHOKU) {
                if (isGrd) {
                    certifKind = "026";
                } else {
                    certifKind = "009";
                }
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_JPN) {
                if (isGrd) {
                    certifKind = "027";
                } else {
                    certifKind = "006";
                }
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_ENG) {
                certifKind = "007";
            } else if (ioutput == OUTPUT_TANNI_SHUTOKU_SHOMEISHO) {
                if (isGrd) {
                    certifKind = "028";
                } else {
                    certifKind = "011";
                }
            }
            param._paramap.put("CERTIFKIND", certifKind);  // 証明書種別
            param._paramap.put("CERTIFKIND2", certifKind);  // 証明書学校データ備考
        }

        /**
         * 証明書種別は卒業生のものを使用するか
         * @param db2
         * @param string
         * @return
         */
        private boolean useCertifKindGrd(final DB2UDB db2, final String year, final String schregno, final String date) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            boolean boo = false;
//            try {
//                // 卒業区分が[nullではなく'4'(卒業見込み)以外]なら証明書種別は卒業生のものを使用する。
//                final StringBuffer stb = new StringBuffer();
//                stb.append(" WITH T_SCHOOL_KIND AS ( ");
//                stb.append("     SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T2.SCHOOL_KIND ");
//                stb.append("     FROM SCHREG_REGD_DAT T1 ");
//                stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
//                stb.append("         AND T2.GRADE = T1.GRADE ");
//                stb.append("     WHERE ");
//                stb.append("         T1.SCHREGNO = '" + schregno + "' ");
//                stb.append("         AND T2.YEAR = '" + year + "' ");
//                stb.append(" ) ");
//                stb.append(" SELECT CASE WHEN (T1.GRD_DIV IS NOT NULL AND T1.GRD_DIV <> '4' ");
//                if (null != date) {
//                    stb.append("          AND T1.GRD_DATE <= '" + date.replace('/', '-') + "' ");
//                }
//                stb.append("        ) THEN 1 ELSE 0 END AS DIV ");
//                stb.append(" FROM SCHREG_ENT_GRD_HIST_DAT T1 ");
//                stb.append(" INNER JOIN T_SCHOOL_KIND T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//                stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                final String sql = stb.toString();
//                log.debug(" sql = " + sql);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                    boo = "1".equals(rs.getString("DIV"));
//                }
//            } catch (Exception e) {
//                log.error(e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return boo;
            return true;
        }

        private void print(final DB2UDB db2, final Param param, final Map regd) {

            final String schregno = KnjDbUtils.getString(regd, "SCHREGNO");
            setCertifKind(db2, param, schregno);

            final String staffcdBlank = "";
            final String semester = KnjDbUtils.getString(regd, "SEMESTER");
            final String dateNull = null; // param._date;
            final String certifNumberBlank = ""; // (String) paramap.get("NUMBER");
            final int certifKindInt = Integer.parseInt((String) param._paramap.get("CERTIFKIND"));
            final String kanjiDummy = null;
            final int ioutput = Integer.parseInt(param._output);

            // 調査書校長名を印刷しない

            if (ioutput == OUTPUT_TYOUSASHO_SHINGAKU) {
                // 校長名を出力しないダミーパラメータ
                param._paramap.put("OUTPUT_PRINCIPAL", "2");
                param._paramap.put("PRGID", "KNJE070");
                param._paramap.put("2017SP", "1");

                final KNJI060_1 pobj = (KNJI060_1) _map.get(KNJI060_1);
                pobj.printSvf(schregno, param._year, semester, dateNull, staffcdBlank, param._kanji, param._comment, param._os, certifNumberBlank, param._paramap);
            } else if (ioutput == OUTPUT_TYOUSASHO_SHUSHOKU) {
                // 校長名を出力しないダミーパラメータ
                param._paramap.put("OUTPUT_PRINCIPAL", "2");
                param._paramap.put("PRGID", "KNJE070");
                param._paramap.put("2017SP", "1");

                final KNJI060_2 pobj = (KNJI060_2) _map.get(KNJI060_2);
                pobj.printSvf(schregno, param._year, semester, dateNull, staffcdBlank, param._kanji, param._comment, param._os, certifNumberBlank, param._paramap);
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_JPN) {
                // 校長名を出力しないダミーパラメータ
                param._paramap.put("OUTPUT_PRINCIPAL", "2");
                param._paramap.put("PRGID", "KNJE080");

                final KNJI070_1 pobj = (KNJI070_1) _map.get(KNJI070_1);
                pobj.printSvf(param._year, semester, dateNull, schregno, param._paramap, staffcdBlank, certifKindInt, kanjiDummy, certifNumberBlank);
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_ENG) {
                // 校長名を出力しないダミーパラメータ
                param._paramap.put("OUTPUT_PRINCIPAL", "2");
                param._paramap.put("PRGID", "KNJE080");

                final KNJI070_2 pobj = (KNJI070_2) _map.get(KNJI070_2);
                pobj.printSvf(param._year, semester, dateNull, schregno, param._paramap, staffcdBlank, certifKindInt, kanjiDummy, certifNumberBlank);
            } else if (ioutput == OUTPUT_TANNI_SHUTOKU_SHOMEISHO) {
                final KNJG030_2 pobj = (KNJG030_2) _map.get(KNJG030_2);
                pobj.printSvf(param._year, semester, schregno, dateNull, (String) param._paramap.get("CERTIFKIND"), certifNumberBlank, param._paramap);
            }
        }

        private void closeSession(final Param param) {
            final int ioutput = Integer.parseInt(param._output);
            if (ioutput == OUTPUT_TYOUSASHO_SHINGAKU) {
                final KNJI060_1 pobj = (KNJI060_1) _map.get(KNJI060_1);
                pobj.pre_stat_f();
            } else if (ioutput == OUTPUT_TYOUSASHO_SHUSHOKU) {
                final KNJI060_2 pobj = (KNJI060_2) _map.get(KNJI060_2);
                pobj.pre_stat_f();
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_JPN) {
                final KNJI070_1 pobj = (KNJI070_1) _map.get(KNJI070_1);
                pobj.pre_stat_f();
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_ENG) {
                final KNJI070_2 pobj = (KNJI070_2) _map.get(KNJI070_2);
                pobj.pre_stat_f();
            } else if (ioutput == OUTPUT_TANNI_SHUTOKU_SHOMEISHO) {
                final KNJG030_2 pobj = (KNJG030_2) _map.get(KNJG030_2);
                pobj.pre_stat_f();
            }
            _svf.VrQuit();
        }
    }

    private static class Util {
        public static void logElapsedTime(final long startTime, final long endTime) {
            final DecimalFormat df = new DecimalFormat("00");
            final Calendar calStart = Calendar.getInstance();
            calStart.setTimeInMillis(startTime);
            log.info(" start time = " + df.format(calStart.get(Calendar.HOUR_OF_DAY)) + ":" + df.format(calStart.get(Calendar.MINUTE)) + ":" + df.format(calStart.get(Calendar.SECOND)));

            final Calendar calEnd = Calendar.getInstance();
            calEnd.setTimeInMillis(endTime);
            log.info(" end   time = " + df.format(calEnd.get(Calendar.HOUR_OF_DAY)) + ":" + df.format(calEnd.get(Calendar.MINUTE)) + ":" + df.format(calEnd.get(Calendar.SECOND)));

            final long elapsedMillis = endTime - startTime;
            long sec = elapsedMillis / 1000;
            long min = sec / 60;
            long hour = min / 60;
            sec -= min * 60;
            min -= hour * 60;
            log.info(" elapsed time = " + df.format(hour) + ":" + df.format(min) + ":" + df.format(sec));
        }

        public static void deleteRecursively(final File path) {
            if (path.isDirectory()) {
                File[] list = path.listFiles();
                for (int i = 0; i < list.length; i++) {
                    deleteRecursively(list[i]);
                }
            }
            path.delete();
            log.info(" delete " + path);
        }

        public static File createDirectory(final File root, final List deces) {
            if (deces.isEmpty()) {
                return root;
            }
            final String path = (String) deces.get(0);
            final String newPath = root.getAbsolutePath() + "/" + path;
            final File newDir = new File(newPath);
            if (!newDir.exists()) {
                newDir.mkdir();
            }
            return createDirectory(newDir, deces.subList(1, deces.size()));
        }

        public static void copyFile(final File src, final File dest) {
            try {
                FileUtils.copyFile(src, dest);
                log.info("ファイルコピー:" + src + " -> " + dest);
            } catch (IOException e) {
                log.error("exception!", e);
            }
        }
    }

//    /**
//     * 証明書番号の年度を取得します。
//     */
//    private String getCertificateNum(
//            final DB2UDB db2,
//            final String pnendo
//    ) {
//        final int intNendo = Integer.parseInt(pnendo);
//        final String gengou = nao_package.KenjaProperties.gengou(intNendo);
//
//        final String str = gengou.substring(2, 4);
//        if( str == null ) {
//            return "";
//        }
//        return str;
//    }

    /**
     * http://www.saka-en.com/java/java-zip-compress/
     * ZipCompressUtils は、ZIP 圧縮をおこなう上で利便性の高い機能を提供します。
     *
     * @author saka-en.
     * @version $Revision: 63879 $ $Date: 2018-12-11 11:25:40 +0900 (火, 11 12 2018) $ $Description: 新規作成 $
     */
    public static class ZipCompressUtils {

        /**
         * 指定されたディレクトリ内のファイルを ZIP アーカイブし、指定されたパスに作成します。
         *
         * @param fullPath 圧縮後の出力ファイル名をフルパスで指定 ( 例: C:/sample.zip )
         * @param directory 圧縮するディレクトリ ( 例; C:/sample )
         * @param enc enコーディングファイル名
         * @return 処理結果 true:圧縮成功 false:圧縮失敗
         */
        public static boolean compressDirectory(final File file, final File directory, final String enc) {
            log.info(" ディレクトリ圧縮 " + directory.getAbsolutePath() + " -> " + file);
            File baseFile = file;
            ZipOutputStream outZip = null;
            try {
                // ZIPファイル出力オブジェクト作成
                outZip = new ZipOutputStream(new FileOutputStream(baseFile));

                archive(outZip, baseFile, directory, enc);
            } catch ( Exception e ) {
                log.error("ZIP圧縮失敗", e);
                return false;
            } finally {
                // ZIPエントリクローズ
                if ( outZip != null ) {
                    try { outZip.closeEntry(); } catch (Exception e) {}
                    try { outZip.flush(); } catch (Exception e) {}
                    try { outZip.close(); } catch (Exception e) {}
                }
            }
            return true;
        }

        /**
         * 指定された ArrayList のファイルを ZIP アーカイブし、指定されたパスに作成します。
         *
         * @param filePath 圧縮後のファイル名をフルパスで指定 ( 例: C:/sample.zip )
         * @param fileList 圧縮するファイルリスト  ( 例; {C:/sample1.txt, C:/sample2.txt} )
         * @enc ファイルエンコーディング名
         * @return 処理結果 true:圧縮成功 false:圧縮失敗
         */
        public static boolean compressFileList(final String filePath, final ArrayList fileList, final String enc) {

            ZipOutputStream outZip = null;
            File baseFile = new File(filePath);
            try {
                // ZIPファイル出力オブジェクト作成
                outZip = new ZipOutputStream(new FileOutputStream(baseFile));
                // 圧縮ファイルリストのファイルを連続圧縮
                for ( int i = 0 ; i < fileList.size() ; i++ ) {
                    // ファイルオブジェクト作成
                    File file = new File((String)fileList.get(i));
                    archive(outZip, baseFile, file, file.getName(), enc);
                }
            } catch (Exception e) {
                log.error("ZIP圧縮失敗", e);
                return false;
            } finally {
                // ZIPエントリクローズ
                if ( outZip != null ) {
                    try { outZip.closeEntry(); } catch (Exception e) {}
                    try { outZip.flush(); } catch (Exception e) {}
                    try { outZip.close(); } catch (Exception e) {}
                }
            }
            return true;
        }

        /**
         * ディレクトリ圧縮のための再帰処理
         *
         * @param outZip ZipOutputStream
         * @param baseFile File 保存先ファイル
         * @param file File 圧縮したいファイル
         * @param enc ファイルエンコーディング名
         */
        private static void archive(final ZipOutputStream outZip, final File baseFile, final File targetFile, final String enc) {
            if ( targetFile.isDirectory() ) {
                File[] files = targetFile.listFiles();
                for (int fi = 0; fi < files.length; fi++) {
                    File f = files[fi];
                    if ( f.isDirectory() ) {
                        archive(outZip, baseFile, f, enc);
                    } else {
                        if ( !f.getAbsoluteFile().equals(baseFile)  ) {
                            // 圧縮処理
                            archive(outZip, baseFile, f, StringUtils.replace(f.getAbsolutePath(), baseFile.getParent(), "").substring(1), enc);
                        }
                    }
                }
            }
        }

        /**
         * 圧縮処理
         *
         * @param outZip ZipOutputStream
         * @param baseFile File 保存先ファイル
         * @param targetFile File 圧縮したいファイル
         * @parma entryName 保存ファイル名
         * @param enc 文字コード
         */
        private static boolean archive(final ZipOutputStream outZip, final File baseFile, final File targetFile, final String entryName, final String enc) {
            // 圧縮レベル設定
            outZip.setLevel(5);

            // 文字コードを指定
            outZip.setEncoding(enc);
            try {

                // ZIPエントリ作成
                outZip.putNextEntry(new ZipEntry(entryName));

                // 圧縮ファイル読み込みストリーム取得
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(targetFile));

                // 圧縮ファイルをZIPファイルに出力
                int readSize = 0;
                byte buffer[] = new byte[1024]; // 読み込みバッファ
                while ((readSize = in.read(buffer, 0, buffer.length)) != -1) {
                    outZip.write(buffer, 0, readSize);
                }
                // クローズ処理
                in.close();
                // ZIPエントリクローズ
                outZip.closeEntry();
            } catch ( Exception e ) {
                // ZIP圧縮失敗
                return false;
            }
            return true;
        }
    }


    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 63879 $ $Date: 2018-12-11 11:25:40 +0900 (火, 11 12 2018) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _hyotei;
        final String _kanji;
//        final String _date;
        final String _comment;
        final String _os;
        final String _output;
        final KNJDefineSchool definecode = new KNJDefineSchool();  //各学校における定数等設定

        final String _nendo;
        final String _tmpRoot;
        final String _monthDateHourMinuteSecond;
        final String _fileext = ".pdf";
        String _kindname;
        final Map _paramap;
        final String _dbname;

        final String _DOCUMENTROOT;
        final File _receiveSendDir;
        final String _headerUserAgent;

        private boolean _isDebug = false;
        private int _debugCount = -1;
        private List _debugSchregnoList = new ArrayList();

        public Param(final HttpServletRequest request) {

//            //記載日がブランクの場合桁数０で渡される事に対応
//            String param6 = null;
//            if (request.getParameter("DATE") != null  &&  3 < request.getParameter("DATE").length()) {
//                param6 = request.getParameter("DATE");                            //処理日付
//            }

            _year = request.getParameter("YEAR");                            //年度
            _hyotei = StringUtils.defaultString(request.getParameter("HYOTEI"), "off");                          //評定の読み替え
            _kanji = request.getParameter("KANJI");                           //漢字出力
//            _date = param6;
            _comment = request.getParameter("COMMENT");                         //学習成績概評
            _os = request.getParameter("OS");                              //ＯＳ区分 1:XP 2:WINDOWS2000
            _output = NumberUtils.isDigits(request.getParameter("OUTPUT")) ? request.getParameter("OUTPUT") : "-1";
            _paramap = Param.createParamap(request, this);

            _dbname = request.getParameter("DBNAME");
            _nendo = _year + "年度";
            _tmpRoot = "/tmp";
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MMddHHmmss");
            _monthDateHourMinuteSecond = df.format(cal.getTime());

            final int ioutput = Integer.parseInt(_output);
            _kindname = "";
            if (ioutput == OUTPUT_TYOUSASHO_SHINGAKU) {
                _kindname = "調査書進学用";
            } else if (ioutput == OUTPUT_TYOUSASHO_SHUSHOKU) {
                _kindname = "調査書就職用";
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_JPN) {
                _kindname = "成績証明書和文";
            } else if (ioutput == OUTPUT_SEISEKI_SHOMEISHO_ENG) {
                _kindname = "成績証明書英文";
            } else if (ioutput == OUTPUT_TANNI_SHUTOKU_SHOMEISHO) {
                _kindname = "単位修得証明書";
            }

            _DOCUMENTROOT = request.getParameter("DOCUMENTROOT");
            _receiveSendDir = new File(_DOCUMENTROOT + "/" + "receive_send");

            _headerUserAgent = StringUtils.defaultString(request.getHeader("User-Agent"));
        }

        public void load(final DB2UDB db2) {
            _isDebug = "1".equals(getDbPrginfoProperties(db2, "debug"));
            final String debugCount = getDbPrginfoProperties(db2, "debugCount");
            if (NumberUtils.isDigits(debugCount)) {
                _debugCount = Integer.parseInt(debugCount);
            }
            final String debugSchregno = getDbPrginfoProperties(db2, "debugSchregno");
            if (null != debugSchregno) {
                final String[] array = StringUtils.split(debugSchregno, " ");
                for (int i = 0; i < array.length; i++) {
                    _debugSchregnoList.add(array[i]);
                }
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJK991T' AND NAME = '" + propName + "' "));
        }

        private static Map createParamap(final HttpServletRequest req, final Param param) {
            final Map map = new HashMap();

            if ("TOK".equals(param.definecode.schoolmark)) {
                map.put("NUMBER", "");
            }

            putParam(map, req, "OS");  //ＯＳ区分
            putParam(map, req, "CTRL_YEAR");  //今年度
            putParam(map, req, "MIRISYU");  // 未履修科目を出力する:1 しない:2
            putParam(map, req, "SONOTAJUUSYO");  // その他住所を優先して表示する。
            putParam(map, req, "HYOTEI");  // 評定の読み替え offの場合はparamapに追加しない。
            putParam(map, req, "FORM6");  // ６年生用フォーム offの場合はparamapに追加しない。
            putParam(map, req, "PRGID");                          // プログラムID
            putParam(map, req, "RISYU");  // 履修のみ科目出力　1:する／2:しない
            putParam(map, req, "useCurriculumcd");
            putParam(map, req, "useClassDetailDat");
            putParam(map, req, "useAddrField2");
            putParam(map, req, "useProvFlg");
            putParam(map, req, "useGakkaSchoolDiv");
            putParamDef(map, req, "TANIPRINT_SOUGOU", "1");
            putParamDef(map, req, "TANIPRINT_RYUGAKU", "1");
            putParamBl(map, req, "useSyojikou3");
            putParamBl(map, req, "certifNoSyudou");
            putParamBl(map, req, "useCertifSchPrintCnt");
            putParamBl(map, req, "gaihyouGakkaBetu");
            putParamBl(map, req, "train_ref_1_2_3_field_size");
            putParamBl(map, req, "train_ref_1_2_3_gyo_size");
            putParamBl(map, req, "3_or_6_nenYoForm");
            putParamBl(map, req, "tyousasyoSougouHyoukaNentani");
            putParamBl(map, req, "NENYOFORM");
            putParamBl(map, req, "tyousasyoTokuBetuFieldSize");
            putParamBl(map, req, "tyousasyoEMPTokuBetuFieldSize");
            putParamBl(map, req, "tyousasyoKinsokuForm");
            putParamBl(map, req, "tyousasyoNotPrintAnotherAttendrec");
            putParamBl(map, req, "tyousasyoNotPrintAnotherStudyrec");
            putParamReplace(map, req, "tyousasyoAttendrecRemarkFieldSize");
            putParamReplace(map, req, "tyousasyoTotalstudyactFieldSize");
            putParamReplace(map, req, "tyousasyoTotalstudyvalFieldSize");
            putParamReplace(map, req, "tyousasyoSpecialactrecFieldSize");
            putParamWithName(map, "CTRL_DATE", req, "DATE");  //学籍処理日
            putParamWithName(map, "OUTPUT_PRINCIPAL", req, "KOTYO");

            final List containedParam = Arrays.asList(new String[] {"CTRL_DATE", "OUTPUT_PRINCIPAL", "category_selected"});
            for (final Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
                final String name = (String) en.nextElement();
                if (map.containsKey(name) || containedParam.contains(name)) {
                    continue;
                }
                map.put(name, req.getParameter(name));
            }
            return map;
        }

        // パラメータで' 'が'+'に変換されるのでもとに戻す
        private static void putParamReplace(final Map paramap, final HttpServletRequest req, final String name) {
            paramap.put(name, StringUtils.replace(StringUtils.defaultString(req.getParameter(name), ""), "+", " "));
        }

        // パラメータがnullの場合デフォルト値に置換
        private static void putParamDef(final Map paramap, final HttpServletRequest req, final String name, final String defVal) {
            paramap.put(name, StringUtils.defaultString(req.getParameter(name), defVal));
        }

        // パラメータがnullの場合""に置換
        private static void putParamBl(final Map paramap, final HttpServletRequest req, final String name) {
            putParamDef(paramap, req, name, "");
        }

        private static void putParam(final Map paramap, final HttpServletRequest req, final String name) {
            paramap.put(name, req.getParameter(name));
        }

        private static void putParamWithName(final Map paramap, final String paramname, final HttpServletRequest req, final String name) {
            paramap.put(paramname, req.getParameter(name));
        }

        /**
         * 「作成中」のファイルを置いておく
         * @return
         */
        private String getReceiveSendZipProcessingFilePath() throws UnsupportedEncodingException {
            return getReceiveSendZipFilePath() + ".出力中.txt";
        }

        private String getZipFilename() {
            return _kindname + _nendo + ".zip";
        }

        private String getReceiveSendZipFilePath() throws UnsupportedEncodingException {
            return _receiveSendDir.getAbsolutePath() + "/" + getZipFilename();
        }

        private String getTmpZipFilePath() {
            final File dir = Util.createDirectory(new File(_tmpRoot), Arrays.asList(new String[] {"KNJK991T", _dbname + _monthDateHourMinuteSecond, }));
            return dir.getAbsoluteFile() + "/" + getZipFilename();
        }

        private File getTmpCompessTargetDirectory() {
            final File dir = Util.createDirectory(new File(_tmpRoot), Arrays.asList(new String[] {"KNJK991T", _dbname + _monthDateHourMinuteSecond, _kindname + _nendo}));
            return dir;
        }

        private File getTmpPdfFileDir(final String hrname) {
            final File dir = Util.createDirectory(new File(_tmpRoot), Arrays.asList(new String[] {"KNJK991T", _dbname + _monthDateHourMinuteSecond, _kindname + _nendo, hrname}));
            return dir;
        }
    }
}
