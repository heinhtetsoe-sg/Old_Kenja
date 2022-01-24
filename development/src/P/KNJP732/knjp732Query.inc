<?php

require_once('for_php7.php');

class knjp732query extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/

    //年度取得
    function getYear($year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->field["YEAR"]."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //データ取得
    function getCollectCsvInfoDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_CSV_INFO_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     ROW_NO ";

        return $query;
    }

    //データ取得
    function selectQuery($model)
    {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.PAID_LIMIT_MONTH, ";
        $query .= "         T2.SLIP_NO, ";
        $query .= "         T2.PLAN_YEAR, ";
        $query .= "         T2.PLAN_MONTH, ";
        $query .= "         T2.COLLECT_L_CD, ";
        $query .= "         T2.COLLECT_M_CD, ";
        $query .= "         T2.PLAN_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_LIMITDATE_DAT T1, ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD     = T2.SCHOOLCD AND ";
        $query .= "         T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "         T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
        $query .= "         T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T2.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "         T1.SLIP_NO      = T2.SLIP_NO AND ";
        $query .= "         T1.PLAN_YEAR    = T2.PLAN_YEAR AND ";
        $query .= "         T1.PLAN_MONTH   = T2.PLAN_MONTH ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     SLIP_NO, ";
        $query .= "     PLAN_YEAR, ";
        $query .= "     PLAN_MONTH, ";
        $query .= "     '' AS COLLECT_L_CD, ";
        $query .= "     '' AS COLLECT_M_CD, ";
        $query .= "     SUM(PLAN_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     SLIP_NO, ";
        $query .= "     PLAN_YEAR, ";
        $query .= "     PLAN_MONTH ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     SLIP_NO, ";
        $query .= "     PLAN_YEAR, ";
        $query .= "     PLAN_MONTH, ";
        $query .= "     COLLECT_L_CD, ";
        $query .= "     COLLECT_M_CD ";

        return $query;
    }

    //更新処理
    function updateQueryCsv($model, &$data_arr2)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入金計画からデータ取得
        $setData = array();
        $query = knjp732Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setData[$row["SCHREGNO"].":".$row["PAID_LIMIT_MONTH"]][] = $row;
        }

        //更新
        foreach ($data_arr2 as $key => $val) {
            for ($i = 0; $i < get_count($setData[$key]); $i++) {
                list ($schregno, $limit_m) = explode(":", $key);

                $data = array();
                $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]          = $model->field["SCHOOL_KIND"];
                $data["YEAR"][TEXT]                 = $model->field["YEAR"];
                $data["SLIP_NO"][TEXT]              = $setData[$key][$i]["SLIP_NO"];
                if ($setData[$key][$i]["COLLECT_M_CD"]) {
                    $data["COLLECT_L_CD"][TEXT]         = $setData[$key][$i]["COLLECT_L_CD"];
                    $data["COLLECT_M_CD"][TEXT]         = $setData[$key][$i]["COLLECT_M_CD"];
                }
                $data["PLAN_YEAR"][TEXT]            = $setData[$key][$i]["PLAN_YEAR"];
                $data["PLAN_MONTH"][TEXT]           = $setData[$key][$i]["PLAN_MONTH"];
                $data["SEQ"][TEXT]                  = 1;
                $data["SCHREGNO"][TEXT]             = $schregno;
                $data["PLAN_PAID_MONEY_DATE"][TEXT] = $data_arr2[$key]["PLAN_PAID_MONEY_DATE"];
                $data["PLAN_PAID_MONEY"][NUMBER]    = $setData[$key][$i]["PLAN_MONEY"];
                $data["PLAN_PAID_MONEY_DIV"][TEXT]  = 1;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                if ($setData[$key][$i]["COLLECT_M_CD"]) {
                    $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_M_DAT");
                    $db->query($query);
                } else {
                    $query = Query::insertSQL($data, "COLLECT_SLIP_PLAN_PAID_DAT");
                    $db->query($query);
                }
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }


    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //CSV取込項目チェック
    function preCheckCsvM($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_M_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     YEAR        = '".$model->field["YEAR"]."' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     COLLECT_CSV_INFO_DAT S1, ";
        $query .= "                     COLLECT_CSV_GRP_DAT S2 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.SCHOOLCD     = T1.SCHOOLCD AND ";
        $query .= "                     S1.SCHOOLCD     = S2.SCHOOLCD AND ";
        $query .= "                     S1.SCHOOL_KIND  = T1.SCHOOL_KIND AND ";
        $query .= "                     S1.SCHOOL_KIND  = S2.SCHOOL_KIND AND ";
        $query .= "                     S1.YEAR         = T1.YEAR AND ";
        $query .= "                     S1.YEAR         = S2.YEAR AND ";
        $query .= "                     S2.COLLECT_L_CD = T1.COLLECT_L_CD AND ";
        $query .= "                     S2.COLLECT_M_CD = T1.COLLECT_M_CD ";
        $query .= "                 ) ";

        return $query;
    }

    //在籍チェック
    function schregnoCntSql($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T2.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     T1.SCHREGNO     = '".$schregno."' ";

        return $query;
    }

    //請求額取得
    function getPlanMoney($model) {
        $query  = " WITH CSV_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHOOLCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.ROW_NO, ";
        $query .= "         L1.COLLECT_L_CD, ";
        $query .= "         L1.COLLECT_M_CD ";
        $query .= "     FROM ";
        $query .= "         COLLECT_CSV_INFO_DAT T1 ";
        $query .= "         LEFT JOIN COLLECT_CSV_GRP_DAT L1 ";
        $query .= "              ON T1.SCHOOLCD     = L1.SCHOOLCD ";
        $query .= "             AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        $query .= "             AND T1.YEAR         = L1.YEAR ";
        $query .= "             AND T1.GRP_CD       = L1.GRP_CD ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         PAID_LIMIT_MONTH, ";
        $query .= "         T1.COLLECT_L_CD, ";
        $query .= "         T1.COLLECT_M_CD, ";
        $query .= "         T1.PLAN_YEAR, ";
        $query .= "         T1.PLAN_MONTH, ";
        $query .= "         T1.PLAN_MONEY, ";
        $query .= "         L1.ROW_NO ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_M_DAT T1 ";
        $query .= "         LEFT JOIN CSV_INFO L1 ";
        $query .= "              ON T1.SCHOOLCD     = L1.SCHOOLCD ";
        $query .= "             AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        $query .= "             AND T1.YEAR         = L1.YEAR ";
        $query .= "             AND T1.COLLECT_L_CD = L1.COLLECT_L_CD ";
        $query .= "             AND T1.COLLECT_M_CD = L1.COLLECT_M_CD, ";
        $query .= "         COLLECT_SLIP_PLAN_LIMITDATE_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD     = T2.SCHOOLCD AND ";
        $query .= "         T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "         T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
        $query .= "         T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SLIP_NO      = T2.SLIP_NO AND ";
        $query .= "         T1.PLAN_YEAR    = T2.PLAN_YEAR AND ";
        $query .= "         T1.PLAN_MONTH   = T2.PLAN_MONTH ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     '' AS ROW_NO, ";
        $query .= "     SUM(PLAN_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     PAID_LIMIT_MONTH ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     ROW_NO, ";
        $query .= "     SUM(PLAN_MONEY) AS PLAN_MONEY ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     ROW_NO ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     PAID_LIMIT_MONTH, ";
        $query .= "     ROW_NO ";

        return $query;
    }

    //入金額取得
    function getPaidMoney($model) {
        $query  = " WITH PAID_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         PLAN_YEAR, ";
        $query .= "         PLAN_MONTH, ";
        $query .= "         '' AS COLLECT_L_CD, ";
        $query .= "         '' AS COLLECT_M_CD, ";
        $query .= "         SUM(PLAN_PAID_MONEY) AS PLAN_PAID_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_PAID_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "         SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "         YEAR        = '".$model->field["YEAR"]."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         PLAN_YEAR, ";
        $query .= "         PLAN_MONTH ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         PLAN_YEAR, ";
        $query .= "         PLAN_MONTH, ";
        $query .= "         COLLECT_L_CD, ";
        $query .= "         COLLECT_M_CD, ";
        $query .= "         SUM(PLAN_PAID_MONEY) AS PLAN_PAID_MONEY ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SLIP_PLAN_PAID_M_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "         SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "         YEAR        = '".$model->field["YEAR"]."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         PLAN_YEAR, ";
        $query .= "         PLAN_MONTH, ";
        $query .= "         COLLECT_L_CD, ";
        $query .= "         COLLECT_M_CD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.PAID_LIMIT_MONTH, ";
        $query .= "     SUM(L1.PLAN_PAID_MONEY) AS PLAN_PAID_MONEY ";
        $query .= " FROM ";
        $query .= "     COLLECT_SLIP_PLAN_LIMITDATE_DAT T1 ";
        $query .= "     LEFT JOIN PAID_DATA L1 ";
        $query .= "          ON T1.SCHREGNO     = L1.SCHREGNO ";
        $query .= "         AND T1.PLAN_YEAR    = L1.PLAN_YEAR ";
        $query .= "         AND T1.PLAN_MONTH   = L1.PLAN_MONTH ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     T1.YEAR         = '".$model->field["YEAR"]."' ";
        $query .= "     GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.PAID_LIMIT_MONTH ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.PAID_LIMIT_MONTH ";

        return $query;
    }

    public function getDBDuplicatePaid($model, $data)
    {
        $db = Query::dbCheckOut();
        //入金計画からデータ取得
        $setData = array();
        $cnt = 0;
        $query = knjp732Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setData[$row["SCHREGNO"].":".$row["PAID_LIMIT_MONTH"]][] = $row;
        }

        foreach ($data as $key => $val) {
            for ($i = 0; $i < get_count($setData[$key]); $i++) {
                if ($setData["COLLECT_M_CD"] != "") {
                    $query  = " SELECT COUNT(*) FROM COLLECT_SLIP_PLAN_PAID_M_DAT";
                    $query .= " WHERE YEAR = '". $model->field["YEAR"] ."'";
                    $query .= "   AND SLIP_NO = '". $setData[$key][$i]["SLIP_NO"]."'";
                    if ($setData[$key][$i]["COLLECT_M_CD"]) {
                        $query .= "   AND COLLECT_L_CD = '". $setData[$key][$i]["COLLECT_L_CD"]."'";
                        $query .= "   AND COLLECT_M_CD = '". $setData[$key][$i]["COLLECT_M_CD"]."'";
                    }
                    $query .= "   AND PLAN_YEAR = '". $setData[$key][$i]["PLAN_YEAR"]."'";
                    $query .= "   AND PLAN_MONTH = '". $setData[$key][$i]["PLAN_MONTH"]."'";
                    $query .= "   AND SEQ = 1";
                    $cnt += $db->getOne($query);
                } else {
                    $query  = " SELECT COUNT(*) FROM COLLECT_SLIP_PLAN_PAID_DAT";
                    $query .= " WHERE SCHOOLCD = '". sprintf("%012d", SCHOOLCD) ."'";
                    $query .= "   AND SCHOOL_KIND = '". $model->field["SCHOOL_KIND"] ."'";
                    $query .= "   AND YEAR = '". $model->field["YEAR"] ."'";
                    $query .= "   AND SLIP_NO = '". $setData[$key][$i]["SLIP_NO"]."'";
                    $query .= "   AND PLAN_YEAR = '". $setData[$key][$i]["PLAN_YEAR"]."'";
                    $query .= "   AND PLAN_MONTH = '". $setData[$key][$i]["PLAN_MONTH"]."'";
                    $query .= "   AND SEQ = 1";
                    $cnt += $db->getOne($query);
                }
            }
        }
        Query::dbCheckIn($db);
        return $cnt;
    }
}
?>
