<?php

require_once('for_php7.php');

class knjm530Query extends Query {

    //講座登録確認
    function getCheckout($model, $field)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND SEMESTER = '" .$model->semester. "' ";
        $query .= "     AND CHAIRCD = '" .$field["CHAIRCD"]. "' ";
        $query .= "     AND SCHREGNO = '" .$field["SCHREGNO"]. "' ";

        return $query;
    }

    //重複登録確認
    function getCheckoutHr($model, $field)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     HR_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND SCHREGNO = '".$field["SCHREGNO"]."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$field["RECEIPT_DATE"])."' ";
        $query .= "     AND CHAIRCD = '".$field["CHAIRCD"]."' ";
        $query .= "     AND PERIODCD = '" .$field["PERIODCD"]."' ";

        return $query;
    }

    //登録確認
    function checkSchChrT($field)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SCHOOLING_SEQ) AS SCHOOLING_SEQ, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$field["RECEIPT_DATE"])."' ";
        $query .= "     AND CHAIRCD = '".$field["CHAIRCD"]."' ";
        $query .= "     AND PERIODCD = '" .$field["PERIODCD"]."' ";

        return $query;
    }

    //登録済みチェック（登校スクーリングのみ）
    function getCheckIns($model, $field)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCH_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$field["SCHREGNO"]."' ";
        $query .= "     AND EXECUTEDATE = '" .str_replace("/","-",$field["RECEIPT_DATE"])."' ";
        $query .= "     AND CHAIRCD = '".$field["CHAIRCD"]."' ";
        $query .= "     AND PERIODCD = '" .$field["PERIODCD"]."' ";
        $query .= "     AND SCHOOLING_SEQ = " .$field["SCHOOLING_SEQ"]." ";
        $query .= "     AND SCHOOLINGKINDCD = '".$field["SCHOOLINGKINDCD"]."' ";

        return $query;
    }

    //レポート提出基準日確認
    function getCheckout2($field)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     REP_STANDARDDATE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND SUBCLASSCD = '".substr($field["REPNO"], 1, 6)."' ";
        $query .= "     AND STANDARD_SEQ = ".substr($field["REPNO"], 7, 2)." ";

        return $query;
    }

    //レポート提出基準日確認
    function getCheckout3($model, $field)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND SEMESTER = '".$model->semester."' ";
        $query .= "     AND CHAIRCD = '".$field["CHAIRCD"]."' ";
        $query .= "     AND SCHREGNO = '".$field["SCHREGNO"]."' ";

        return $query;
    }

    //ハンディーターミナル講座読替
    function getHandyCaircd($model, $field)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_HANDY_REPLACE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR. "' ";
        $query .= "     AND SEMESTER = '" .$model->semester. "' ";
        $query .= "     AND HANDY_CHAIRCD = '" .$field["HANDY_CHAIRCD"]. "' ";

        return $query;
    }

    //講座登録確認
    function getCheckoutch($field)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$field["RECEIPT_DATE"])."' ";
        $query .= "     AND CHAIRCD = '".$field["CHAIRCD"]."' ";
        $query .= "     AND PERIODCD = '" .$field["PERIODCD"]."' ";
        $query .= "     AND SCHOOLING_SEQ = 0 ";

        return $query;
    }

    //講座登録削除
    function getDeleteReportIns($field)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     REP_PRESENT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" .CTRL_YEAR."' ";
        $query .= "     AND SUBCLASSCD = '".substr($field["REPNO"], 1, 6)."' ";
        $query .= "     AND STANDARD_SEQ = ".substr($field["REPNO"], 7, 2)." ";
        $query .= "     AND REPRESENT_SEQ = ".substr($field["REPNO"], 9, 1)." ";
        $query .= "     AND SCHREGNO = '".$field["SCHREGNO"]."' ";
        $query .= "     AND RECEIPT_DATE = '".str_replace("/", "-", $field["RECEIPT_DATE"])."' ";

        $db->query($query);
        $db->commit();

        Query::dbCheckIn($db);

        return ;
    }

    /**
     * INSERT
     */
    function &getInsertReportQuery($field)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SUBCLASSCD"][TEXT]       = substr($field["REPNO"],1,6);
        $data["STANDARD_SEQ"][NUMBER]   = substr($field["REPNO"],7,2);
        $data["REPRESENT_SEQ"][NUMBER]  = substr($field["REPNO"],9,1);
        $data["SCHREGNO"][TEXT]         = $field["SCHREGNO"];
        $data["RECEIPT_DATE"][DATE]     = $field["RECEIPT_DATE"];
        $data["RECEIPT_TIME"][TEXT]     = $field["RECEIPT_TIME"];
        $data["CHAIRCD"][TEXT]          = $field["CHAIRCD"];
        $data["REGISTERCD"][TEXT]       = $field["STAFFCD"];
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $db->query(Query::insertSQL($data, "REP_PRESENT_DAT"));
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getInsertAttendQuery($field)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]         = $field["SCHREGNO"];
        $data["EXECUTEDATE"][DATE]      = $field["RECEIPT_DATE"];
        $data["CHAIRCD"][TEXT]          = $field["CHAIRCD"];
        if ($field["SCHOOLINGKINDCD"] == 1) {
            $data["PERIODCD"][TEXT]         = $field["PERIODCD"];
            $data["SCHOOLING_SEQ"][NUMBER]  = $field["SCHOOLING_SEQ"];
        } else {
            $data["SCHOOLING_SEQ"][NUMBER]  = $field["SCHOOLING_SEQ"];
        }
        $data["RECEIPT_DATE"][DATE]     = $field["RECEIPT_DATE"];
        $data["RECEIPT_TIME"][TEXT]     = $field["RECEIPT_TIME"];
        $data["STAFFCD"][TEXT]          = $field["STAFFCD"];
        $data["TERMINAL_CD"][TEXT]      = $field["TERMINAL_CD"];
        $data["SCHOOLINGKINDCD"][TEXT]  = $field["SCHOOLINGKINDCD"];
        $data["REMARK"][TEXT]           = "無";
        $data["REGISTERCD"][TEXT]       = $field["STAFFCD"];
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $db->query(Query::insertSQL($data, "SCH_ATTEND_DAT"));

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //実施区分設定
    function getUpdateAttendSchchr($field)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $query  = "UPDATE  ";
        $query .= "  SCH_CHR_T_DAT SET  ";
        $query .= "  EXECUTED = '1' ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '" .str_replace("/","-",$field["RECEIPT_DATE"])."' ";
        $query .= "     AND PERIODCD = '" .$field["PERIODCD"]."' ";
        $query .= "     AND CHAIRCD = '".$field["CHAIRCD"]."' ";
        $query .= "     AND SCHOOLING_SEQ = ".$field["SCHOOLING_SEQ"]." ";

        $db->query($query);
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    function &getInsertAttendHrQuery($field)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SCHREGNO"][TEXT]     = $field["SCHREGNO"];
        $data["EXECUTEDATE"][DATE]  = $field["RECEIPT_DATE"];
        $data["CHAIRCD"][TEXT]      = $field["CHAIRCD"];
        $data["PERIODCD"][TEXT]     = $field["PERIODCD"];
        $data["RECEIPT_DATE"][DATE] = $field["RECEIPT_DATE"];
        $data["RECEIPT_TIME"][TEXT] = $field["RECEIPT_TIME"];
        $data["REGISTERCD"][TEXT]   = $field["STAFFCD"];
        $data["UPDATED"][NUMBER]    = "sysdate()";


        $db->query(Query::insertSQL($data, "HR_ATTEND_DAT"));

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //生徒
    function getSeitoCsv() {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CASE WHEN length(L1.NAME) > 24 ";
        $query .= "          THEN substr(L1.NAME, 1, 24) ";
        $query .= "          ELSE L1.NAME ";
        $query .= "     END AS NAME, ";
        $query .= "     L3.HANDY_CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN CHAIR_STD_DAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "          AND T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "     LEFT JOIN CHAIR_HANDY_REPLACE_DAT L3 ON L2.YEAR = L3.YEAR ";
        $query .= "          AND L2.SEMESTER = L3.SEMESTER ";
        $query .= "          AND L2.CHAIRCD = L3.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND T1.SEMESTER = '" . CTRL_SEMESTER . "' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     L3.HANDY_CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L3.HANDY_CHAIRCD ";

        return $query;
    }

    //職員
    function getStaffCsv() {
        $query  = " SELECT ";
        $query .= "     STAFFCD, ";
        $query .= "     CASE WHEN length(STAFFNAME) > 15 ";
        $query .= "          THEN substr(STAFFNAME, 1, 15) ";
        $query .= "          ELSE STAFFNAME ";
        $query .= "     END AS STAFFNAME ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . CTRL_YEAR . "' ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //校時
    function getKoujiCsv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     CASE WHEN length(NAME1) > 15 ";
        $query .= "          THEN substr(NAME1, 1, 15) ";
        $query .= "          ELSE NAME1 ";
        $query .= "     END AS NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //講座
    function getKouzaCsv() {
        $query  = " SELECT ";
        $query .= "     L1.HANDY_CHAIRCD, ";
        $query .= "     CASE WHEN length(T1.CHAIRNAME) > 15 ";
        $query .= "          THEN substr(T1.CHAIRNAME, 1, 15) ";
        $query .= "          ELSE T1.CHAIRNAME ";
        $query .= "     END AS CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_HANDY_REPLACE_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND T1.SEMESTER = '" . CTRL_SEMESTER . "' ";
        $query .= " ORDER BY ";
        $query .= "     L1.HANDY_CHAIRCD ";

        return $query;
    }

    //時間割
    function getJikanwariCsv() {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     L1.HANDY_CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_T_DAT T1 ";
        $query .= "     LEFT JOIN CHAIR_HANDY_REPLACE_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "          AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . CTRL_YEAR . "' ";
        $query .= "     AND T1.SEMESTER = '" . CTRL_SEMESTER . "' ";
        $query .= " GROUP BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     L1.HANDY_CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     L1.HANDY_CHAIRCD ";

        return $query;
    }
}
?>
