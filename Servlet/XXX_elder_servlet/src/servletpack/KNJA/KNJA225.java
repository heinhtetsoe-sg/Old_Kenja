// kanji=漢字
/*
 * $Id: 471524698e5cfc5f939a00a4f0003993d93c58b8 $
 */

package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/************************************************************************************
 *
 *  学校教育システム 賢者 [学籍管理システム]
 *
 *                  ＜ＫＮＪＡ２２５＞ 名簿出力 [名列票]
 *
 ************************************************************************************/

public class KNJA225 {
    Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private static final Log log = LogFactory.getLog(KNJA225.class);
    private Param _param;
    
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        _param = new Param(request);

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                          //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

        // ＤＢ接続
        DB2UDB db2 = new DB2UDB(_param._dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("[KNJA225]DB2 open error!");
            log.error(ex);
        }

        /*-----------------------------------------------------------------------------
          ＳＶＦ作成処理       
          -----------------------------------------------------------------------------*/
        boolean nonedata = false;
        for(int c=0; c<_param._gradeClass.length; c++) {
            for(int k=0; k<_param._kensuu; k++) {
                log.debug("grade-class = " + _param._gradeClass[c]);
                nonedata = printMain(db2, svf, _param._gradeClass[c]) || nonedata ;
            }
        }
        //該当データ無し
        if(nonedata == false){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndRecord();
            svf.VrEndPage();
        }

        // 終了処理
        db2.close();        // DBを閉じる
        svf.VrQuit();
        outstrm.close();    // ストリームを閉じる 
    }
    

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final String gradeClass) {
        
        boolean nonedata = false;
        svf.VrSetForm("KNJA225.frm", 4);
        final int MAX_LINE_NUM = 50;
        
        try {
            db2.query(getSqlSchregRegdDat(_param._year, _param._semester, gradeClass));
            ResultSet rs = db2.getResultSet();
            int count=0;
            while(rs.next()) {
                nonedata = true;
                svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));
                svf.VrsOut("ATTENDNO", rs.getString("ATTENDNO"));
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("KANA", rs.getString("NAME_KANA"));
                svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                svf.VrEndRecord();
                count++;
                if (count > MAX_LINE_NUM) {
                    count %= MAX_LINE_NUM;
                }
            }
            if (count != 0 && count < MAX_LINE_NUM) {
                for(; count < MAX_LINE_NUM; count++) {
                    svf.VrEndRecord();
                }
            }
        } catch (SQLException ex) {
            log.error(" [printMain] SQLException !", ex);
        }
        
        return nonedata;
    }
    
    /**
     * 指定の年度、学期、学年クラスの生徒情報のSQLを得る
     */
    private String getSqlSchregRegdDat(final String year, final String semester, final String gradeClass) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append(    " T1.SCHREGNO, ");
        stb.append(    " T3.NAME, ");
        stb.append(    " T3.NAME_KANA, ");
        stb.append(    " T2.HR_NAME, ");
        stb.append(    " T1.ATTENDNO ");
        stb.append(" FROM ");
        stb.append(    " SCHREG_REGD_DAT T1 ");
        stb.append(    " INNER JOIN SCHREG_REGD_HDAT T2 ON ");
        stb.append(        " T2.YEAR = T1.YEAR AND ");
        stb.append(        " T2.SEMESTER = T1.SEMESTER AND ");
        stb.append(        " T2.GRADE = T1.GRADE AND ");
        stb.append(        " T2.HR_CLASS = T1.HR_CLASS ");
        stb.append(    " INNER JOIN SCHREG_BASE_MST T3 ON ");
        stb.append(        " T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append(    " T1.YEAR = '" + year + "' AND ");
        stb.append(    " T1.SEMESTER = '" + semester + "' AND ");
        stb.append(    " T1.GRADE || T1.HR_CLASS = '" + gradeClass + "' ");
        stb.append(" ORDER BY ");
        stb.append(    " T1.YEAR, ");
        stb.append(    " T1.SEMESTER, ");
        stb.append(    " T1.GRADE, ");
        stb.append(    " T1.HR_CLASS, ");
        stb.append(    " T1.ATTENDNO ");
        return stb.toString();
    }
    
    private class Param {
        final String _year;
        final String _semester;
        final String _dbname;
        final String[] _gradeClass;
        final int _kensuu;
        
        Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _dbname = request.getParameter("DBNAME");
            _gradeClass = request.getParameterValues("CLASS_SELECTED");
            _kensuu = Integer.valueOf(request.getParameter("KENSUU")).intValue();
        }
    }
}  //クラスの括り
