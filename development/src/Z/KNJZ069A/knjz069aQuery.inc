<?php

require_once('for_php7.php');

class knjz069aQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //Unit区分取得
    function getUnit($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z056' ";
        if($model->field["SEMESTER"] != ''){
            $query .= "     AND NAMESPARE1 = '".$model->field["SEMESTER"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getPypUnitRow($model) {
        $query  = " SELECT ";
        $query .= "     UNIT_THEME, ";
        $query .= "     UNIT_IDEA ";
        $query .= " FROM ";
        $query .= "     PYP_UNIT_DAT  ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND UNIT_CD  = '".$model->field["UNIT_CD"]."' ";

        return $query;
    }

    //存在チェック
    function getPypUnitCnt($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PYP_UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND UNIT_CD  = '".$model->field["UNIT_CD"]."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //HREPORTREMARK_DAT
        //存在チェック
        $check = $db->getCol(knjz069aQuery::getPypUnitCnt($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]       = CTRL_YEAR;
            $data["SEMESTER"][TEXT]   = $model->field["SEMESTER"];
            $data["GRADE"][TEXT]      = $model->field["GRADE"];
            $data["UNIT_CD"][TEXT]    = $model->field["UNIT_CD"];
            $data["UNIT_THEME"][TEXT] = $model->field["UNIT_THEME"];
            $data["UNIT_IDEA"][TEXT]  = $model->field["UNIT_IDEA"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $db->query(Query::insertSQL($data, "PYP_UNIT_DAT"));
        } else {
            $data["UNIT_THEME"][TEXT] = $model->field["UNIT_THEME"];
            $data["UNIT_IDEA"][TEXT]  = $model->field["UNIT_IDEA"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $where  = " WHERE ";
            $where .= "         YEAR     = '".CTRL_YEAR."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND GRADE    = '".$model->field["GRADE"]."' ";
            $where .= "     AND UNIT_CD  = '".$model->field["UNIT_CD"]."' ";

            $db->query(Query::updateSQL($data, "PYP_UNIT_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
