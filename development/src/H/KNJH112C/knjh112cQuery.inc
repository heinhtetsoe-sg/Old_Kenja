<?php

require_once('for_php7.php');

class knjh112cQuery extends Query {
    //学期取得
    function getSemester() {
        $query  = " WITH T_ALL (VALUE, LABEL) AS ( ";
        $query .= "      VALUES('9', '--年間--') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " UNION ALL  ";
        $query .= " SELECT ";
        $query .= "      VALUE, ";
        $query .= "      LABEL ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //資格コンボ
    function getQualifiedMst() {
        $query  = " SELECT DISTINCT ";
        $query .= "     QUALIFIED_CD AS VALUE, ";
        $query .= "     QUALIFIED_CD || ':' || QUALIFIED_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     MANAGEMENT_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     QUALIFIED_CD ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $semester) {
        $semester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER: $semester;
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$semester."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GRADE IN (  SELECT ";
                    $query .= "                     GRADE ";
                    $query .= "                 FROM ";
                    $query .= "                     SCHREG_REGD_GDAT ";
                    $query .= "                 WHERE ";
                    $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                    $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    $query .= "             ) ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$semester."' ";
            $query .= "     AND (   TR_CD1    = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD2    = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD3    = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD3 = '" .STAFFCD ."') ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GRADE IN (  SELECT ";
                    $query .= "                     GRADE ";
                    $query .= "                 FROM ";
                    $query .= "                     SCHREG_REGD_GDAT ";
                    $query .= "                 WHERE ";
                    $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                    $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    $query .= "             ) ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期開始日、終了日取得
    function getSemDate($semester) {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";

        return $query;
    }

/******リストtoリスト*******/
    //生徒取得(右側に表示する生徒)
    function getStudent($model, $semester) {
        $semester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER: $semester;

        $query  = " SELECT ";
        $query .= "     H1.HR_NAME || ' ' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
        $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND H1.GRADE    = T1.GRADE ";
        $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '{$model->hrClass}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
