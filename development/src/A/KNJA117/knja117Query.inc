<?php

require_once('for_php7.php');

class knja117Query extends Query {
    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //学年取得
    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒一覧取得
    function getSchList($model, $flg) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || ':' || T1.SCHREGNO AS VALUE, ";
        $query .= "     T3.HR_NAME || ' ' || T1.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "          ON T1.YEAR     = T3.YEAR ";
        $query .= "         AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "         AND T1.GRADE    = T3.GRADE ";
        $query .= "         AND T1.HR_CLASS = T3.HR_CLASS ";
        if ($flg == "left") {
            $query .= "     INNER JOIN SCHREG_BASE_DETAIL_MST T4 ";
            $query .= "          ON T4.SCHREGNO     = T1.SCHREGNO ";
            $query .= "         AND T4.BASE_SEQ     = '003' ";
            $query .= "         AND T4.BASE_REMARK2 = '".$model->field["TESTDIV"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE        = '".$model->field["GRADE"]."' ";
        if ($flg == "right") {
            $query .= " AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if ($flg == "select") {
            $query .= " AND T1.SCHREGNO IN ('".implode(explode(',', $model->selectdata),"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function checkSchregBaseDetailMst() {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     BASE_SEQ = '003' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データクリア
        $query = knja117Query::getSchList($model, "left");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["BASE_REMARK2"][TEXT]     = NULL;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "      SCHREGNO       = '".$row["SCHREGNO"]."' AND ";
            $where .= "      BASE_SEQ       = '003' ";
            $db->query(Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where));
        }

        //データの存在チェック
        $schArray = $db->getCol(knja117Query::checkSchregBaseDetailMst());

        //更新
        $selectdata = explode(",", $model->selectdata);
        if ($selectdata[0] != "") {
            for ($i = 0; $i < get_count($selectdata); $i++) {
                $data = array();
                $data["BASE_REMARK2"][TEXT]     = $model->field["TESTDIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                if (in_array($selectdata[$i], $schArray)) {
                    $where  = " WHERE ";
                    $where .= "      SCHREGNO       = '".$selectdata[$i]."' AND ";
                    $where .= "      BASE_SEQ       = '003' ";
                    $db->query(Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where));
                } else {
                    $data["SCHREGNO"][TEXT]     = $selectdata[$i];
                    $data["BASE_SEQ"][TEXT]     = '003';
                    $db->query(Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST"));
                }
            }
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
