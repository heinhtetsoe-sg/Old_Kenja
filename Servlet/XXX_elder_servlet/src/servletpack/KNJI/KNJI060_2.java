// kanji=漢字
/*
 * $Id: d7df5277fab0f98495d196186a6acb763ff57ccb $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI;

import java.util.Map;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE070_2;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [卒業生情報管理] 調査書（高校・卒業生・就職用）
 *
 *  2005/07/22 yamashiro KNJ_PersonalinfoSql.sql_info_regメソッドの引数変更
 *                       KNJ_ExamremarkSqlをKNJ_ExamremarkSql_Grdへ変更
 *  2005/07/25 yamashiro KNJ_AttendrecSqlをKNJ_AttendrecSql_Grdへ変更
 *                       KNJ_StudyrecSqlをKNJ_StudyrecSql_Grdへ変更
 *  2005/11/18 yamashiro 学校情報を'今年度'と'卒業年度'の２種類を取得( =>Edit_SchoolInfoSqlにおいて )
 */

public class KNJI060_2 extends KNJE070_2 {

    private static final Log log = LogFactory.getLog(KNJI060_2.class);

    public KNJI060_2(
            final DB2UDB db2,
            final Vrw32alpWrap svf,
            final KNJDefineSchool definecode,
            final String useSyojikou3
    ){
        super(db2,svf,definecode, useSyojikou3);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }

    /**
     *  PrepareStatement作成
     **/
    public void pre_stat(String hyotei, Map paramap)
    {
        paramap.put("PRINT_GRD", "1");
        super.pre_stat(hyotei, paramap);
    }

    /**
     * {@inheritDoc}
     */
    protected SqlStudyrec getPreStatementStudyrec(
            final String schregno,
            final String year,
            final Map paramap
    ) {
        paramap.put("PRINT_GRD", "1");
        return super.getPreStatementStudyrec(schregno, year, paramap);
    }

}
