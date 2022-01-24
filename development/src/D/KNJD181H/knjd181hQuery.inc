<?php

require_once('for_php7.php');

class knjd181hQuery extends Query {

    //「欠点(評価)は、不振チェック参照するか？」のフラグを取得
    function getNameMstD048()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D048' AND ";
        $query .= "     NAMECD2 = '01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //学期数を取得
    function getCountSemester() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";

        return $query;
    }

    //学期を取得
    function getSemester($getCountsemester) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '".$getCountsemester."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学期を取得
    function getSemester2($semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".$semester."' ";

        return $query;
    }

    //年組取得(権限チェック)
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
            if ($model->semester == '9') {
                $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
            }
            $query .= "     AND T1.SCHOOL_KIND = 'H' ";
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
                $query .= "     GHR_CD || '-ghr' AS VALUE, ";
                $query .= "     GHR_NAME AS LABEL ";
            } else {
                $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
                $query .= "     HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT ";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT ";
            }
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            if ($model->semester == '9') {
                $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "     AND SEMESTER = '".$model->semester."' ";
            }
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     REGD_G.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT REGD_G ";
                $query .= "                 WHERE ";
                $query .= "                         REGD_G.YEAR        = '".CTRL_YEAR."' ";
                $query .= "                     AND REGD_G.SCHOOL_KIND = 'H' ) ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
                $query .= "          TR_CD2     = '".STAFFCD."' OR ";
                $query .= "          TR_CD3     = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            //参照・更新可
            if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
                $query .= "SELECT ";
                $query .= "    GRADE || '-' || HR_CLASS AS VALUE, ";
                $query .= "    HR_NAME AS LABEL ";
                $query .= "FROM ";
                $query .= "    SCHREG_REGD_HDAT T1 ";
                $query .= "WHERE ";
                $query .= "    EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT WHERE YEAR = T1.YEAR AND GRADE = T1.GRADE AND SCHOOL_KIND = 'H')  ";
                $query .= "    AND YEAR         = '".CTRL_YEAR."' ";
                if ($model->semester == '9') {
                    $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
                } else {
                    $query .= "    AND SEMESTER = '".$model->semester."' ";
                }
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query  = "SELECT ";
                $query .= "    GRADE || '-' || HR_CLASS AS VALUE, ";
                $query .= "    HR_NAME AS LABEL ";
                $query .= "FROM ";
                $query .= "    SCHREG_REGD_HDAT T1 ";
                $query .= "WHERE ";
                $query .= "    EXISTS (SELECT 'X' FROM SCHREG_REGD_GDAT WHERE YEAR = T1.YEAR AND GRADE = T1.GRADE AND SCHOOL_KIND = 'H')  ";
                $query .= "    AND YEAR           = '".CTRL_YEAR."' ";
                if ($model->semester == '9') {
                    $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
                } else {
                    $query .= "    AND SEMESTER = '".$model->semester."' ";
                }
                $query .= "    AND (   TR_CD1 = '" .STAFFCD ."' ";
                $query .= "         OR TR_CD2 = '" .STAFFCD ."' ";
                $query .= "         OR TR_CD3 = '" .STAFFCD ."') ";
            }
        }

        return $query;
    }

    //学校名取得の取得(テストコンボで使う)
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        list ($grade, $hr_class) = explode('-', $model->hrClass);

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
            $query .= "     (CASE WHEN ADDR.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) ";
            $query .= "     || value(HDAT.HR_NAMEABBV,'') || '　' || value(REGD.{$setGhr}ATTENDNO, '   ') || '番　' || BASE.NAME_SHOW AS LABEL,  ";
        } else {
            $query .= "     (CASE WHEN ADDR.SEND_ADDR1 IS NULL THEN '　' ELSE '☆' END) ";
            $query .= "     || REGD.SCHREGNO || '　' || value(REGD.{$setGhr}ATTENDNO, '   ') || '番　' || BASE.NAME_SHOW AS LABEL,  ";
        }
        $query .= "     REGD.{$setGhr}ATTENDNO AS ATTENDNO, ";
        $query .= "     REGD.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     {$tablename} REGD ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_SEND_ADDRESS_DAT ADDR ON ADDR.SCHREGNO = REGD.SCHREGNO AND ADDR.DIV = '1' ";
        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == "1") {
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ";
            $query .= "                                    AND HDAT.SEMESTER = REGD.SEMESTER ";
            $query .= "                                    AND HDAT.GRADE    = REGD.GRADE ";
            $query .= "                                    AND HDAT.HR_CLASS = REGD.HR_CLASS, ";
            $query .= "     {$tablename2} STFF ";
        }
        $query .= " WHERE ";
        $query .= "     REGD.YEAR                 = '".CTRL_YEAR."' ";
        if ($model->semester == '9') {
            $query .= "     AND REGD.SEMESTER         = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND REGD.SEMESTER         = '".$model->semester."' ";
        }
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

    function getTest($semester) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$semester}' ";

        return $query;
    }

    //中高区分
    function getGdat($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     GRADE = '".$grade."' ";

        return $query;
    }
    
    //データ取得
    function getHreportConditionDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = 'H' AND ";
        }
        $query .= "     GRADE       = '".sprintf("%02d", "")."' AND ";
        $query .= "     COURSECD    = '".sprintf("%01d", "")."' AND ";
        $query .= "     MAJORCD     = '".sprintf("%03d", "")."' AND ";
        $query .= "     COURSECODE  = '".sprintf("%04d", "")."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SEQ) ";

        return $query;
    }
}
?>
