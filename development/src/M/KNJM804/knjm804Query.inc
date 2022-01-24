<?php

require_once('for_php7.php');

class knjm804Query extends Query {

    //年組取得（権限チェック）
    function getAuth($year, $semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query .= "  SELECT DISTINCT ";
            $query .= "      GRADE || HR_CLASS AS VALUE, ";
            $query .= "      HR_NAME AS LABEL  ";
            $query .= "  FROM   ";
            $query .= "      SCHREG_REGD_HDAT ";
            $query .= "  WHERE  ";
            $query .= "      YEAR = '" .$year ."' ";
            $query .= "  AND SEMESTER   = '".$semester."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "  SELECT DISTINCT ";
            $query .= "      GRADE || HR_CLASS AS VALUE, ";
            $query .= "      HR_NAME AS LABEL  ";
            $query .= "  FROM   ";
            $query .= "      SCHREG_REGD_HDAT ";
            $query .= "  WHERE  ";
            $query .= "      YEAR = '" .$year ."' ";
            $query .= "  AND SEMESTER   = '".$semester."' ";
            $query .= "  AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "  OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "  OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //会計グループCD取得
    function getGrpcd($year, $grade_hr_class)
    {
        $query .= "  SELECT ";
        $query .= "      T1.COLLECT_GRP_CD || ':' || COLLECT_GRP_NAME AS LABEL, ";
        $query .= "      T1.COLLECT_GRP_CD AS VALUE ";
        $query .= "  FROM ";
        $query .= "      COLLECT_GRP_HR_DAT T1 ";
        $query .= "  LEFT JOIN COLLECT_GRP_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                              AND L1.COLLECT_GRP_CD = T1.COLLECT_GRP_CD ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR = '" .$year ."' ";
        $query .= "  AND T1.GRADE || T1.HR_CLASS = '" .$grade_hr_class ."' ";

        return $query;
    }

    //生徒取得
    function getSchno($model, $year, $semester)
    {
        if ($model->field["GRADE_HR_CLASS"] != '00000') {
            $query .= " SELECT DISTINCT";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T2.NAME_SHOW, ";
            $query .= "     T3.HR_NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_BASE_MST T2, ";
            $query .= "     SCHREG_REGD_DAT T1  ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND ";
            $query .= "                                      T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ";
            $query .= "     INNER JOIN COLLECT_MONEY_DUE_M_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                          AND L2.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "WHERE  T1.YEAR               = '".$year."' AND ";
            $query .= "       T1.SEMESTER           = '".$semester."' AND ";
            $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
            $query .= "       L2.COLLECT_GRP_CD     = '".$model->field["COLLECT_GRP_CD"]."' AND ";
            $query .= "       T2.SCHREGNO           = T1.SCHREGNO ";
            if ($model->field["OUTPUT_NO"] == "1") {
                $query .= "       AND L2.CHANGE_CD   = '01' ";
            } else if ($model->field["OUTPUT_NO"] == "2") {
                $query .= "       AND L2.CHANGE_CD   = '02' ";
            } else if ($model->field["OUTPUT_NO"] == "3") {
                $query .= "       AND L2.CHANGE_CD   = '03' ";
            }
            $query .= "ORDER BY T1.ATTENDNO ";
        } else {
            $query .= "  SELECT DISTINCT";
            $query .= "      T1.SCHREGNO, ";
            $query .= "      T1.NAME AS NAME_SHOW, ";
            $query .= "      '' AS GRADE, ";
            $query .= "      T1.HR_CLASS, ";
            $query .= "      T1.ATTENDNO ";
            $query .= "  FROM ";
            $query .= "      FRESHMAN_DAT T1";
            $query .= "  INNER JOIN COLLECT_MONEY_DUE_M_DAT L2 ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                          AND L2.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "  WHERE ";
            $query .= "      T1.ENTERYEAR = '".($year + 1)."' ";
            $query .= "  AND L2.COLLECT_GRP_CD     = '".$model->field["COLLECT_GRP_CD"]."' ";
            if ($model->field["OUTPUT_NO"] == "1") {
                $query .= "       AND L2.CHANGE_CD   = '01' ";
            } else if ($model->field["OUTPUT_NO"] == "2") {
                $query .= "       AND L2.CHANGE_CD   = '02' ";
            } else if ($model->field["OUTPUT_NO"] == "3") {
                $query .= "       AND L2.CHANGE_CD   = '03' ";
            }
            $query .= "ORDER BY T1.ATTENDNO ";
        }

        return $query;
    }

}
?>
