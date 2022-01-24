<?php

require_once('for_php7.php');

class knjx_tuuchisyoken_selectQuery extends Query {
    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'Z010' ";
        $query .= "   AND NAMECD2 = '00' ";

        return $query;
    }

    // 学期取得
    function getSemesterMst($model) {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= "   , SEMESTER ";
        $query .= "   , SEMESTERNAME ";
        $query .= " FROM SEMESTER_MST ";
        $query .= " WHERE YEAR = '{$model->exp_year}' ";

        return $query;
    }

    //学校種別取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T1.GRADE = T2.GRADE ";
        $query .= " WHERE T1.YEAR     = '$model->exp_year' ";
        $query .= "   AND T1.SEMESTER = '$model->exp_semester' ";
        $query .= "   AND T1.SCHREGNO = '$model->schregno' ";

        return $query;
    }

    // 通知表データ取得
    function getHreportremarkDat($model, $field) {
        $query  = " SELECT ";
        $query .= "    YEAR ";
        $query .= "  , SEMESTER ";
        $query .= "  , SCHREGNO ";
        $query .= "  , {$field} AS VALUE ";
        $query .= " FROM HREPORTREMARK_DAT ";
        $query .= " WHERE YEAR = '{$model->exp_year}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    // 通知表データ取得
    function getHreportremarkDetailDat($model, $field, $div, $code) {
        $query  = " SELECT ";
        $query .= "    YEAR ";
        $query .= "  , SEMESTER ";
        $query .= "  , SCHREGNO ";
        $query .= "  , {$field} AS VALUE ";
        $query .= " FROM HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE YEAR = '{$model->exp_year}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";
        $query .= "   AND DIV = '{$div}' ";
        $query .= "   AND CODE = '{$code}' ";

        return $query;
    }
}
?>
