<?php

require_once('for_php7.php');

class knjd292wQuery extends Query
{

    //学期取得
    public function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getHrClass($model)
    {
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$seme."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    public function getStudent($model)
    {
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO || '　' || L1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$seme."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
