<?php

require_once('for_php7.php');

class knjp724Query extends Query {
    //データ取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     PG_M.PREFECTURESCD, ";
        $query .= "     G202.NAME1 AS PREF_NAME, ";
        $query .= "     PG_M.GRADE, ";
        $query .= "     P003_1.NAME1 AS REFER_YEAR_DIV1, ";
        $query .= "     P003_2.NAME1 AS REFER_YEAR_DIV2, ";
        $query .= "     PG_M.STANDARD_SCHOOL_FEE ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PREF_GRADE_MST PG_M ";
        $query .= " LEFT JOIN V_NAME_MST G202 ON G202.YEAR    = PG_M.YEAR ";
        $query .= "                          AND G202.NAMECD1 = 'G202' ";
        $query .= "                          AND G202.NAMECD2 = PG_M.PREFECTURESCD ";
        $query .= " LEFT JOIN V_NAME_MST P003_1 ON P003_1.YEAR    = PG_M.YEAR ";
        $query .= "                            AND P003_1.NAMECD1 = 'P003' ";
        $query .= "                            AND P003_1.NAMECD2 = PG_M.REFER_YEAR_DIV1 ";
        $query .= " LEFT JOIN V_NAME_MST P003_2 ON P003_2.YEAR    = PG_M.YEAR ";
        $query .= "                            AND P003_2.NAMECD1 = 'P003' ";
        $query .= "                            AND P003_2.NAMECD2 = PG_M.REFER_YEAR_DIV2 ";
        $query .= " WHERE ";
        $query .= "         PG_M.SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND PG_M.SCHOOL_KIND   = '".$model->schoolKind."' ";
        $query .= "     AND PG_M.YEAR          = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     PG_M.PREFECTURESCD, ";
        $query .= "     PG_M.GRADE ";

        return $query;
    }

    //名称取得
    function getNameMst($year, $namecd, $namecd2="") {
        $query  = " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || ':' || NAME1 AS LABEL";
        $query .= " FROM  ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' ";
        $query .= " AND NAMECD1 = '".$namecd."' ";
        if ($namecd2){
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        return $query;
    }

    //学年
    function getGrade($model, $year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $flg) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REDUCTION_PREF_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND YEAR        = '".$model->year."' ";
        $query .= " ";
        if ($flg == 1) {
            $query .= "    AND PREFECTURESCD = '".$model->prefecturescd."' ";
            $query .= "    AND GRADE         = '".$model->grade."' ";
        } elseif ($flg == 2) {
            $query .= "    AND PREFECTURESCD = '".$model->field["PREFECTURESCD"]."' ";
            $query .= "    AND GRADE         = '".$model->field["GRADE"]."' ";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year, $model) {
        $Lyear = $year - 1; //前年度チェック用

        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN year = '".$year."'  THEN 'X' ";
        $query .= "          WHEN year = '".$Lyear."' THEN 'Y' end  AS CNT";
        $query .= " FROM ";
        $query .= "     REDUCTION_PREF_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";

        $result = $db->query($query);
        $opt_year = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_year[] = $row["CNT"];
        }

        Query::dbCheckIn($db);
        return $opt_year;
    }

    //前年度のデータをコピー
    function &getCopyQuery($year, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO REDUCTION_PREF_GRADE_MST ";
        $query .= "            ( ";
        $query .= "             SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             PREFECTURESCD, ";
        $query .= "             GRADE, ";
        $query .= "             REFER_YEAR_DIV1, ";
        $query .= "             TOTALL_MONEY_1, ";
        $query .= "             REFER_YEAR_DIV2, ";
        $query .= "             TOTALL_MONEY_2, ";
        $query .= "             USE_RANK, ";
        $query .= "             ZENKI_KAISI_YEAR, ";
        $query .= "             KOUKI_KAISI_YEAR, ";
        $query .= "             STANDARD_SCHOOL_FEE, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ";
        $query .= "            ) ";
        $query .= "    (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$year."', ";
        $query .= "         PREFECTURESCD, ";
        $query .= "         GRADE, ";
        $query .= "         REFER_YEAR_DIV1, ";
        $query .= "         TOTALL_MONEY_1, ";
        $query .= "         REFER_YEAR_DIV2, ";
        $query .= "         TOTALL_MONEY_2, ";
        $query .= "         USE_RANK, ";
        $query .= "         ZENKI_KAISI_YEAR, ";
        $query .= "         KOUKI_KAISI_YEAR, ";
        $query .= "         STANDARD_SCHOOL_FEE, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         SYSDATE() ";
        $query .= "     FROM ";
        $query .= "         REDUCTION_PREF_GRADE_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND YEAR        = '".($year - 1)."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &getInsertQuery($fields, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["YEAR"][TEXT]             = $fields["YEAR"];
        $data["PREFECTURESCD"][TEXT]    = $fields["PREFECTURESCD"];
        $data["GRADE"][TEXT]            = $fields["GRADE"];
        $data["REFER_YEAR_DIV1"][TEXT]  = $fields["REFER_YEAR_DIV1"];
        $data["REFER_YEAR_DIV2"][TEXT]  = $fields["REFER_YEAR_DIV2"];
        $data["STANDARD_SCHOOL_FEE"][NUMBER] = $fields["STANDARD_SCHOOL_FEE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_PREF_GRADE_MST");
        $db->query($query);

        $db->commit();

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["REFER_YEAR_DIV1"][TEXT]  = $fields["REFER_YEAR_DIV1"];
        $data["REFER_YEAR_DIV2"][TEXT]  = $fields["REFER_YEAR_DIV2"];
        $data["STANDARD_SCHOOL_FEE"][NUMBER] = $fields["STANDARD_SCHOOL_FEE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $where .= "   AND YEAR          = '".$fields["YEAR"]."' ";
        $where .= "   AND PREFECTURESCD = '".$fields["PREFECTURESCD"]."' ";
        $where .= "   AND GRADE         = '".$fields["GRADE"]."' ";

        $query = Query::updateSQL($data, "REDUCTION_PREF_GRADE_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM REDUCTION_PREF_GRADE_MST ";
        $query .= " WHERE SCHOOLCD      = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $query .= "   AND YEAR          = '".$fields["YEAR"]."' ";
        $query .= "   AND PREFECTURESCD = '".$fields["PREFECTURESCD"]."' ";
        $query .= "   AND GRADE         = '".$fields["GRADE"]."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
