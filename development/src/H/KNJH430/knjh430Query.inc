<?php

require_once('for_php7.php');

class knjh430Query extends Query {
    //校種取得
    function getSchoolKind($selectSchoolKind) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.NAME1 AS VALUE, ";
        $query .= "     T1.ABBV1 AS LABEL, ";
        $query .= "     INT(T1.ABBV3) AS SORT, ";
        $query .= "     T1.NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= "     INNER JOIN PORTFOLIO_HEAD_MST T2 ";
        $query .= "          ON T2.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "         AND T1.NAME1    = T2.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'A023' ";
        if ($selectSchoolKind) {
            $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $selectSchoolKind),"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     SORT, T1.NAMECD2 ";

        return $query;
    }

    //ポートフォリオ項目マスタから取得
    function getPortfolioHeadMst($model, $flg) {
        $query  = " SELECT DISTINCT ";
        if ($flg == "course") {
            $query .= "     T1.COURSECD AS VALUE, ";
            $query .= "     T2.COURSENAME AS LABEL ";
        } else {
            $query .= "     VALUE(T1.SHOWORDER, '00') || ':' || T1.SEQ AS VALUE, ";
            $query .= "     T1.HEADNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     PORTFOLIO_HEAD_MST T1 ";
        if ($flg == "course") {
            $query .= "     INNER JOIN V_COURSE_MST T2 ON T1.COURSECD = T2.COURSECD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        if ($flg != "course") {
            $query .= " AND T1.COURSECD     = '".$model->field["COURSECD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データクリア
        $data = array();
        $data["SHOWORDER"][TEXT]        = NULL;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "      SCHOOLCD       = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $where .= "      SCHOOL_KIND    = '".$model->field["SCHOOL_KIND"]."' AND ";
        $where .= "      COURSECD       = '".$model->field["COURSECD"]."' ";
        $db->query(Query::updateSQL($data, "PORTFOLIO_HEAD_MST", $where));

        //更新
        $selectdata = explode (",", $model->selectdata);
        if ($selectdata[0] != "") {
            for ($i = 0; $i < get_count($selectdata); $i++) {
                $data = array();
                $data["SHOWORDER"][TEXT]        = sprintf("%02d", ($i+1));
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $where  = " WHERE ";
                $where .= "      SCHOOLCD       = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $where .= "      SCHOOL_KIND    = '".$model->field["SCHOOL_KIND"]."' AND ";
                $where .= "      COURSECD       = '".$model->field["COURSECD"]."' AND ";
                $where .= "      SEQ            = '".$selectdata[$i]."' ";
                $db->query(Query::updateSQL($data, "PORTFOLIO_HEAD_MST", $where));
            }
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>