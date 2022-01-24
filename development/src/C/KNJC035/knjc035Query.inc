<?php

require_once('for_php7.php');

class knjc035query extends Query
{

    //勤怠
    public function getDicd()
    {
        $query  = " SELECT ";
        $query .= "     DI_CD AS VALUE, ";
        $query .= "     DI_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ATTEND_DI_CD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND REP_DI_CD = '2' ";
        $query .= " ORDER BY ";
        $query .= "     smallint(DI_CD) ";

        return $query;
    }

    //生徒の異動日付を取得
    public function getTransData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     TRANSFER_SDATE, ";
        $query .= "     TRANSFER_EDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }


    //データの追加（学籍異動データ）
    public function insertQueryCsv(&$db, $data)
    {
        $data1["SCHREGNO"][TEXT]            = $data["SCHREGNO"];
        $data1["TRANSFERCD"][TEXT]          = "3";
        $data1["TRANSFER_SDATE"][TEXT]      = $data["SDATE"];
        $data1["TRANSFER_EDATE"][TEXT]      = $data["EDATE"];
        $data1["TRANSFERREASON"][TEXT]      = $data["REASON"];
        $data1["TRANSFERPLACE"][TEXT]       = "";
        $data1["TRANSFERADDR"][TEXT]        = "";
        $data1["ABROAD_CLASSDAYS"][TEXT]    = "";
        $data1["ABROAD_CREDITS"][TEXT]      = "";

        $data1["REGISTERCD"][TEXT]          = STAFFCD;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "schreg_transfer_dat");

        $result = $db->query($query);
    }

    //データの追加（出欠データ）
    public function insertQueryCsv2(&$db, $data, $model)
    {
        $updated = date("Y-m-d H:i:s", time());     //更新日付

        $query  = " DELETE FROM ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO = '".$data["SCHREGNO"]."' AND ";
        $query .= "     ATTENDDATE BETWEEN '".$data["SDATE"]."' AND '".$data["EDATE"]."' ";
        $db->query($query);

        $query  = " INSERT INTO ";
        $query .= "     ATTEND_DAT(SCHREGNO,ATTENDDATE,PERIODCD,CHAIRCD,DI_CD,DI_REMARK,YEAR,REGISTERCD,UPDATED)";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.EXECUTEDATE AS ATTENDDATE, ";
        $query .= "     T2.PERIODCD, ";
        $query .= "     MIN(T1.CHAIRCD) AS CHAIRCD, ";
        $query .= "     '".$model->field["KINTAI"]."' AS DI_CD, ";
        $query .= "     '' AS DI_REMARK, ";
        $query .= "     '" .CTRL_YEAR ."' AS YEAR, ";
        $query .= "     '" .STAFFCD ."' AS REGISTERCD, ";
        $query .= "     '" .$updated ."' AS UPDATED ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCH_CHR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        $query .= "     T2.EXECUTEDATE BETWEEN '".$data["SDATE"]."' AND '".$data["EDATE"]."' AND ";
        $query .= "     T1.SCHREGNO = '".$data["SCHREGNO"]."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.EXECUTEDATE, ";
        $query .= "     T2.PERIODCD ";

        $db->query($query);
    }


    //生徒有無
    public function checkSchreg($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(SCHREGNO) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //出欠済みフラグ更新
    public function insertQueryCsv3(&$db, $data)
    {
        //削除 -- SCH_CHR_HRATE_DAT
        $query  = " DELETE FROM  ";
        $query .= "     SCH_CHR_HRATE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ( ";
        $query .=                   knjc035Query::getHrateQuery($data);
        $query .= "                  ) T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.EXECUTEDATE  = T2.EXECUTEDATE AND ";
        $query .= "                 T1.PERIODCD     = T2.PERIODCD AND ";
        $query .= "                 T1.CHAIRCD      = T2.CHAIRCD AND ";
        $query .= "                 T1.GRADE        = T2.GRADE AND ";
        $query .= "                 T1.HR_CLASS     = T2.HR_CLASS ";
        $query .= "             ) ";
        $db->query($query);

        //追加 -- SCH_CHR_HRATE_DAT
        $query  = " INSERT INTO ";
        $query .= "     SCH_CHR_HRATE_DAT(EXECUTEDATE,PERIODCD,CHAIRCD,GRADE,HR_CLASS,EXECUTED,ATTESTOR,REGISTERCD,UPDATED) ";
        $query .=   knjc035Query::getHrateQuery($data);
        $db->query($query);

        //更新 -- SCH_CHR_DAT
        $query  = knjc035Query::getSchChrQuery($data);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data1 = array();
            $data1["EXECUTED"][TEXT]    = '1';
            $data1["ATTESTOR"][TEXT]    = STAFFCD;
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][FUNC]     = "sysdate()";

            $where  = " WHERE ";
            $where .= "     EXECUTEDATE = '".$row["EXECUTEDATE"]."' AND ";
            $where .= "     PERIODCD    = '".$row["PERIODCD"]."' AND ";
            $where .= "     CHAIRCD     = '".$row["CHAIRCD"]."' ";

            $query = Query::updateSQL($data1, "SCH_CHR_DAT", $where);
            $db->query($query);
        }
        $result->free();
    }

    //対象時間割取得（SCH_CHR_HRATE_DAT更新用）
    public function getHrateQuery($data)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     S4.EXECUTEDATE, ";
        $query .= "     S4.PERIODCD, ";
        $query .= "     S4.CHAIRCD, ";
        $query .= "     S1.GRADE, ";
        $query .= "     S1.HR_CLASS, ";
        $query .= "     '1' AS EXECUTED, ";
        $query .= "     '".STAFFCD."' AS ATTESTOR, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT S1, ";
        $query .= "     SEMESTER_MST S2, ";
        $query .= "     CHAIR_STD_DAT S3, ";
        $query .= "     SCH_CHR_DAT S4 ";
        $query .= " WHERE ";
        $query .= "     S1.YEAR     = S2.YEAR AND ";
        $query .= "     S1.YEAR     = S3.YEAR AND ";
        $query .= "     S1.YEAR     = S4.YEAR AND ";
        $query .= "     S1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     S1.SEMESTER = S2.SEMESTER AND ";
        $query .= "     S1.SEMESTER = S3.SEMESTER AND ";
        $query .= "     S1.SEMESTER = S4.SEMESTER AND ";
        $query .= "     S2.SEMESTER <> '9' AND ";
        $query .= "     ('".$data["SDATE"]."' BETWEEN S2.SDATE AND S2.EDATE OR ";
        $query .= "      '".$data["EDATE"]."' BETWEEN S2.SDATE AND S2.EDATE OR ";
        $query .= "      S2.SDATE BETWEEN '".$data["SDATE"]."' AND '".$data["EDATE"]."') AND ";
        $query .= "     ('".$data["SDATE"]."' BETWEEN S3.APPDATE AND S3.APPENDDATE OR ";
        $query .= "      '".$data["EDATE"]."' BETWEEN S3.APPDATE AND S3.APPENDDATE OR ";
        $query .= "      S3.APPDATE BETWEEN '".$data["SDATE"]."' AND '".$data["EDATE"]."') AND ";
        $query .= "     S1.SCHREGNO = S3.SCHREGNO AND ";
        $query .= "     S1.SCHREGNO = '".$data["SCHREGNO"]."' AND ";
        $query .= "     S3.CHAIRCD  = S4.CHAIRCD AND ";
        $query .= "     S4.EXECUTEDATE BETWEEN '".$data["SDATE"]."' AND '".$data["EDATE"]."' ";

        return $query;
    }

    //対象時間割取得（SCH_CHR_DAT更新用）
    public function getSchChrQuery($data)
    {
        $query  = " WITH SCH_CHR AS ( ";
        $query .=       knjc035Query::getHrateQuery($data);
        $query .= " ), SCH_CHR_HRATE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T3.GRADE, ";
        $query .= "         T3.HR_CLASS ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR T1, ";
        $query .= "         CHAIR_STD_DAT T2, ";
        $query .= "         SCHREG_REGD_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "         T2.YEAR     = T3.YEAR AND ";
        $query .= "         T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T2.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ";
        $query .= "         T2.SCHREGNO = T3.SCHREGNO ";
        $query .= " ), SUBMAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.EXECUTED, ";
        $query .= "         T3.EXECUTED AS H_EXECUTED ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_HRATE T1 ";
        $query .= "         LEFT JOIN SCH_CHR_DAT T2 ON T1.EXECUTEDATE  = T2.EXECUTEDATE ";
        $query .= "                                 AND T1.PERIODCD     = T2.PERIODCD ";
        $query .= "                                 AND T1.CHAIRCD      = T2.CHAIRCD ";
        $query .= "         LEFT JOIN SCH_CHR_HRATE_DAT T3 ON T1.EXECUTEDATE = T3.EXECUTEDATE ";
        $query .= "                                       AND T1.PERIODCD    = T3.PERIODCD ";
        $query .= "                                       AND T1.CHAIRCD     = T3.CHAIRCD ";
        $query .= "                                       AND T1.GRADE       = T3.GRADE ";
        $query .= "                                       AND T1.HR_CLASS    = T3.HR_CLASS ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD, ";
        $query .= "         EXECUTED, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(INT(VALUE(H_EXECUTED,'0'))) AS EXE ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     GROUP BY ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD, ";
        $query .= "         EXECUTED ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     EXECUTED = '0' AND ";
        $query .= "     CNT = EXE ";

        return $query;
    }

    public function getCountTransData($data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$data["SCHREGNO"]."' AND ";
        $query .= "     TRANSFER_SDATE = '".$data["SDATE"]."' ";
        return $query;
    }
}
