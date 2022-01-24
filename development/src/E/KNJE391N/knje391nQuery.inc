<?php

require_once('for_php7.php');


class knje391nQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model) {
        $query = "";
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     V_STAFF_HR_DAT T1 ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR        = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                     MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR         = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER     = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS     = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND  = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD      = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.HR_CLASS_NAME1, ";
            $query .= "     T1.SCHOOL_KIND ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHOOL_KIND DESC, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     LABEL ";
        } else if ($model->field["HR_CLASS_TYPE"] == "2") {
            $query  = " SELECT DISTINCT ";
            if ($model->Properties["useFi_Hrclass"] != '1' && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     T1.GHR_CD || '-ghr' AS VALUE, ";
                $query .= "     T1.GHR_NAME AS LABEL ";
            } else {
                $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
                $query .= "     T1.HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT T1 ";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
            }
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "    INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            }
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (T1.TR_CD1     = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD2  = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD3  = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            //参照・更新可
            if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "SELECT ";
                $query .= "    T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
                $query .= "    T1.HR_NAME AS LABEL ";
                $query .= "FROM ";
                $query .= "    SCHREG_REGD_HDAT T1 ";
                $query .= "    INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= "WHERE ";
                $query .= "    T1.YEAR         = '".CTRL_YEAR."' ";
                $query .= "    AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "    AND (   T1.TR_CD1 = '" .STAFFCD ."' ";
                $query .= "         OR T1.TR_CD2 = '" .STAFFCD ."' ";
                $query .= "         OR T1.TR_CD3 = '" .STAFFCD ."' ";
                $query .= "         OR T1.SUBTR_CD1 = '" .STAFFCD ."' ";
                $query .= "         OR T1.SUBTR_CD2 = '" .STAFFCD ."' ";
                $query .= "         OR T1.SUBTR_CD3 = '" .STAFFCD ."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    function getStudentSql($model) {
        list ($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);

        $setGhr = "";
        if ($model->field["HR_CLASS_TYPE"] == "2") {
            if ($hr_class == 'ghr') {
                $tablename = "SCHREG_REGD_GHR_DAT";
                $setGhr = "GHR_";
            } else {
                $tablename = "SCHREG_REGD_FI_DAT";
            }
        } else if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $tablename  = "SCHREG_REGD_DAT";
            $tablename2 = "V_STAFF_HR_DAT";
        } else {
            $tablename = "SCHREG_REGD_DAT";
        }

        $query  = " SELECT ";
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= " DISTINCT ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     value(HDAT.HR_NAMEABBV,'') || '　' || value(REGD.{$setGhr}ATTENDNO, '   ') || '番　' || BASE.NAME_SHOW AS LABEL,  ";
        } else {
            $query .= "     REGD.SCHREGNO || '　' || value(REGD.{$setGhr}ATTENDNO, '   ') || '番　' || BASE.NAME_SHOW AS LABEL,  ";
        }
        $query .= "     REGD.{$setGhr}ATTENDNO AS ATTENDNO, ";
        if ($model->field["HR_CLASS_TYPE"] == "2" && $hr_class == 'ghr') {
            $query .= "     REGD.GHR_CD || VALUE(REGD.{$setGhr}ATTENDNO, '') || '-' || REGD.SCHREGNO AS VALUE ";
        } else {
            $query .= "     REGD.GRADE || REGD.HR_CLASS || VALUE(REGD.{$setGhr}ATTENDNO, '') || '-' || REGD.SCHREGNO AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     {$tablename} REGD ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
            $query .= "                                    AND HDAT.SEMESTER = REGD.SEMESTER ";
            $query .= "                                    AND HDAT.GRADE    = REGD.GRADE ";
            $query .= "                                    AND HDAT.HR_CLASS = REGD.HR_CLASS, ";
            $query .= "     {$tablename2} STFF ";
        }
        $query .= " WHERE ";
        $query .= "     REGD.YEAR                 = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER         = '".CTRL_SEMESTER."' ";
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= "     AND REGD.YEAR     = STFF.YEAR ";
            $query .= "     AND REGD.SEMESTER = STFF.SEMESTER ";
            $query .= "     AND REGD.GRADE    = STFF.GRADE ";
            $query .= "     AND REGD.HR_CLASS = STFF.HR_CLASS ";
        }
        if ($model->field["HR_CLASS_TYPE"] == "2" && $hr_class == 'ghr') {
            $query .= "     AND REGD.GHR_CD = '{$grade}' ";
        } else if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= "     AND STFF.SCHOOL_KIND  = '{$grade}' ";
            $query .= "     AND REGD.HR_CLASS     = '{$hr_class}' ";
        } else {
            $query .= "     AND REGD.GRADE    = '{$grade}' ";
            $query .= "     AND REGD.HR_CLASS = '{$hr_class}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
        }
        $query .= "     ATTENDNO ";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1, $namecd2)
    {
        $query  = "SELECT * ";
        $query .= "FROM V_NAME_MST ";
        $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
        $query .= "AND NAMECD1='".$namecd1 ."'";
        $query .= "AND NAMECD2='".$namecd2 ."'";
        return $query;
    }

}
?>
