<?php

require_once('for_php7.php');

class knjc035bQuery extends Query {
    //学年取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期・月取得
    function selectSemesAll() {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月データの取得
    function selectMonthQuery($month, $flg, $model) {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' ";
        if ($flg == "combo") {
            $query .= " AND NAMESPARE2 = '1' ";
            $query .= " AND NAMECD2 IN (SELECT ";
            $query .= "                     CONTROL_CODE ";
            $query .= "                 FROM ";
            $query .= "                     ADMIN_CONTROL_DAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            }
            $query .= "                     CONTROL_FLG = '2') ";
            $query .= " ORDER BY ";
            $query .= "     NAMESPARE1 ";
        }

        return $query;
    }

    //データ取得
    function getTableData($model, $table, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T2.* ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .=       $table." T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.GRADE    = '".$model->field["GRADE"]."' AND ";
        $query .= "     T2.MONTH    = '".$model->field["MONTH"]."' ";

        return $query;
    }

    //フィールド一覧取得
    function getFieldList($table) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLNO, ";
        $query .= "         NAME, ";
        $query .= "         TYPENAME ";
        $query .= "     FROM ";
        $query .= "         SYSIBM.SYSCOLUMNS ";
        $query .= "     WHERE ";
        $query .= "         TBNAME = '".$table."' ";
        $query .= "     ORDER BY ";
        $query .= "         COLNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     NAME, ";
        $query .= "     TYPENAME ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        return $query;
    }

    //削除履歴のMAXSEQ取得
    function getMaxSeq($model) {
        $query  = " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DEL_HDAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = DATE(SYSDATE()) ";
        $query .= " GROUP BY ";
        $query .= "     EXECUTEDATE ";

        return $query;
    }

    //データ削除処理
    function getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //セットするEXECUTEDATE、SEQ取得
        $max = $db->getRow(knjc035bQuery::getMaxSeq($model), DB_FETCHMODE_ASSOC);
        $executedate = ($max["EXECUTEDATE"]) ? "DATE('".$max["EXECUTEDATE"]."')" : "DATE(SYSDATE())";
        $seq = ($max["SEQ"]) ? $max["SEQ"]+1 : 1;

        //型の区分 1:TEXT 2:NUMBER
        $type_div = array("DATE"        => 1,
                          "VARCHAR"     => 1,
                          "SMALLINT"    => 2,
                          "DECIMAL"     => 2,
                          "TIMESTAMP"   => 1);

        /************************/
        /*  削除履歴データ作成  */
        /************************/
        //追加 -- ATTEND_SEMES_DEL_HDAT
        $data = array();
        $data["EXECUTEDATE"][FUNC]  = $executedate;
        $data["SEQ"][TEXT]          = $seq;
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["MONTH"][TEXT]        = $model->field["MONTH"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "ATTEND_SEMES_DEL_HDAT");
        $db->query($query);

        /**********************************/
        /*  出欠データのバックアップ作成  */
        /**********************************/
        //フィールド一覧取得 -- ATTEND_SEMES_DAT
        $semes_field = array();
        $query = knjc035bQuery::getFieldList("ATTEND_SEMES_DAT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semes_field[$row["NAME"]] = $type_div[$row["TYPENAME"]];
        }
        $result->free();

        //コピー -- ATTEND_SEMES_DAT → ATTEND_SEMES_DEL_DAT
        $query = knjc035bQuery::getTableData($model, "ATTEND_SEMES_DAT", "all");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EXECUTEDATE"][FUNC]  = $executedate;
            $data["SEQ"][TEXT]          = $seq;
            foreach ($semes_field as $fldname => $type) {
                $addname = "";
                if (in_array($fldname, array("REGISTERCD", "UPDATED"))) $addname = "DEL_";
                if ($type == 1) {
                    $data[$addname.$fldname][TEXT]      = $row[$fldname];
                } else if ($type == 2) {
                    $data[$addname.$fldname][NUMBER]    = $row[$fldname];
                }
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "ATTEND_SEMES_DEL_DAT");
            $db->query($query);
        }
        $result->free();

        //フィールド一覧取得 -- ATTEND_SEMES_DETAIL_DAT
        $detail_field = array();
        $query = knjc035bQuery::getFieldList("ATTEND_SEMES_DETAIL_DAT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $detail_field[$row["NAME"]] = $type_div[$row["TYPENAME"]];
        }
        $result->free();

        //コピー -- ATTEND_SEMES_DETAIL_DAT → ATTEND_SEMES_DETAIL_DEL_DAT
        $query = knjc035bQuery::getTableData($model, "ATTEND_SEMES_DETAIL_DAT", "all");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EXECUTEDATE"][FUNC]  = $executedate;
            $data["SEQ"][TEXT]          = $seq;
            foreach ($detail_field as $fldname => $type) {
                $addname = "";
                if (in_array($fldname, array("SEQ", "REGISTERCD", "UPDATED"))) $addname = "DEL_";
                if ($type == 1) {
                    $data[$addname.$fldname][TEXT]      = $row[$fldname];
                } else if ($type == 2) {
                    $data[$addname.$fldname][NUMBER]    = $row[$fldname];
                }
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DEL_DAT");
            $db->query($query);
        }
        $result->free();

        /********************/
        /*  出欠データ削除  */
        /********************/
        //削除 -- ATTEND_SEMES_DAT
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SEMES_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO IN (SELECT ";
        $query .= "                     S1.SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR     = T1.YEAR AND ";
        $query .= "                     S1.SEMESTER = T1.SEMESTER AND ";
        $query .= "                     S1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "                 ) ";

        $db->query($query);

        //削除 -- ATTEND_SEMES_DETAIL_DAT
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_SEMES_DETAIL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO IN (SELECT ";
        $query .= "                     S1.SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.YEAR     = T1.YEAR AND ";
        $query .= "                     S1.SEMESTER = T1.SEMESTER AND ";
        $query .= "                     S1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "                 ) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除履歴取得
    function getHistData($model) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     L1.SEMESTERNAME, ";
        $query .= "     L2.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_DEL_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "              ON T1.YEAR         = G1.YEAR ";
        $query .= "             AND T1.GRADE        = G1.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SEMESTER_MST L1 ";
        $query .= "              ON T1.YEAR         = L1.YEAR ";
        $query .= "             AND T1.SEMESTER     = L1.SEMESTER ";
        $query .= "     LEFT JOIN STAFF_MST L2 ";
        $query .= "              ON T1.REGISTERCD   = L2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.MONTH, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //履歴データ削除処理
    function getHistDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->checked); $i++) {
            list ($executedate, $seq) = preg_split("/_/", $model->checked[$i]);

            //削除 -- ATTEND_SEMES_DEL_HDAT
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DEL_HDAT ";
            $query .= " WHERE ";
            $query .= "     EXECUTEDATE = '".$executedate."' AND ";
            $query .= "     SEQ         = '".$seq."' ";
            $db->query($query);

            //削除 -- ATTEND_SEMES_DEL_DAT
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DEL_DAT ";
            $query .= " WHERE ";
            $query .= "     EXECUTEDATE = '".$executedate."' AND ";
            $query .= "     SEQ         = '".$seq."' ";
            $db->query($query);

            //削除 -- ATTEND_SEMES_DETAIL_DEL_DAT
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SEMES_DETAIL_DEL_DAT ";
            $query .= " WHERE ";
            $query .= "     EXECUTEDATE = '".$executedate."' AND ";
            $query .= "     SEQ         = '".$seq."' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>