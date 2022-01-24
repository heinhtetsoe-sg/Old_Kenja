<?php

require_once('for_php7.php');

class knjd137hQuery extends Query {
    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //データ取得
    function getHreportConditionDat($model) {
        $query .= " SELECT ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_GDAT G1 ";
            $query .= "     ON T1.SCHOOL_KIND = G1.SCHOOL_KIND ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.GRADE = '".sprintf("%02d", "")."' ";
            $query .= "     AND T1.COURSECD = '".sprintf("%01d", "")."' ";
            $query .= "     AND T1.MAJORCD = '".sprintf("%03d", "")."' ";
            $query .= "     AND T1.COURSECODE = '".sprintf("%04d", "")."' ";
            $query .= "     AND T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND T1.SEQ  = '001'  ";
            $query .= "     AND G1.YEAR='{$model->exp_year}' ";
            $query .= "     AND G1.GRADE='{$model->exp_grade}' ";
        } else {
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND GRADE = '".sprintf("%02d", "")."' ";
            $query .= "     AND COURSECD = '".sprintf("%01d", "")."' ";
            $query .= "     AND MAJORCD = '".sprintf("%03d", "")."' ";
            $query .= "     AND COURSECODE = '".sprintf("%04d", "")."' ";
            $query .= "     AND SEQ  = '001'  ";
        }
        return $query;
    }

    //１レコード取得
    function getTrainRow($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }
    
    function getTrainRow2($model,$div,$code) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER    = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND DIV    = '".$div."' ";
        $query .= "     AND CODE    = '".$code."' ";

        return $query;
    }
    
    function getCourseMajorCd($model) {
        $query .= " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->exp_year}' ";
        $query .= "     AND SEMESTER = '{$model->exp_semester}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        
        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd137hQuery::getTrainRow($model));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
    function &getInsertQuery2($model) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd137hQuery::getTrainRow($model));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
            if($model->pattern == '2'){
                $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            }
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["TOTALSTUDYTIME"][TEXT]       = $model->field["TOTALSTUDYTIME"];
            $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
            if($model->pattern == '2'){
                $data["COMMUNICATION"][TEXT]    = $model->field["COMMUNICATION"];
            }
            $data["ATTENDREC_REMARK"][TEXT]     = $model->field["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR        = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO    = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }
        
        //存在チェック
        $check = $db->getCol(knjd137hQuery::getTrainRow2($model,'01','01'));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["DIV"][TEXT]                  = '01';
            $data["CODE"][TEXT]                 = '01';
            $data["REMARK1"][TEXT]              = $model->field["SPECIAL1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data["REMARK1"][TEXT]              = $model->field["SPECIAL1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR            = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER    = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO    = '".$model->schregno."' ";
            $where .= "     AND DIV         = '01' ";
            $where .= "     AND CODE        = '01' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }
        
        //存在チェック
        $check = $db->getCol(knjd137hQuery::getTrainRow2($model,'01','02'));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["DIV"][TEXT]                  = '01';
            $data["CODE"][TEXT]                 = '02';
            $data["REMARK1"][TEXT]              = $model->field["SPECIAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data["REMARK1"][TEXT]              = $model->field["SPECIAL2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR            = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER    = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO    = '".$model->schregno."' ";
            $where .= "     AND DIV         = '01' ";
            $where .= "     AND CODE        = '02' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }
        
        //存在チェック
        $check = $db->getCol(knjd137hQuery::getTrainRow2($model,'01','03'));

        $data = array();
        if (get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["DIV"][TEXT]                  = '01';
            $data["CODE"][TEXT]                 = '03';
            $data["REMARK1"][TEXT]              = $model->field["SPECIAL3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $data["REMARK1"][TEXT]              = $model->field["SPECIAL3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR            = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER    = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO    = '".$model->schregno."' ";
            $where .= "     AND DIV         = '01' ";
            $where .= "     AND CODE        = '03' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
    
    //印刷学期
    function getPrintSemester() {
        $query  = " SELECT ";
        $query .= "   NM.NAME1 AS LABEL, ";
        $query .= "   NM.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "   NAME_MST NM ";
        $query .= " WHERE ";
        $query .= "   NM.NAMECD1 = 'DP78' ";

        return $query;
    }

    //総合所見の使用不可(2020年度以降は使用しない)
    function getTotalRemarkDisable($model) {
        $query  = " WITH GRADE_2018 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '2018' ";
        $query .= "         AND SCHOOL_KIND = 'H' ";
        $query .= "     ORDER BY ";
        $query .= "         GRADE ";
        $query .= "         FETCH FIRST 1 ROWS ONLY ";
        $query .= " ) ";
        $query .= " , GRADE_2019 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '2019' ";
        $query .= "         AND SCHOOL_KIND = 'H' ";
        $query .= "     ORDER BY ";
        $query .= "         GRADE ";
        $query .= "         FETCH FIRST 2 ROWS ONLY ";
        $query .= " ) ";
        $query .= " SELECT * FROM GRADE_2018 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM GRADE_2019 ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        $query .= "     , GRADE ";

        return $query;
    }

}
?>
