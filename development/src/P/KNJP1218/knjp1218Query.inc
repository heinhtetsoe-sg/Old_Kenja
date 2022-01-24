<?php

require_once('for_php7.php');

class knjp1218Query extends Query {

    function getSemester($year)
    {
        $query  = "  SELECT ";
        $query .= "      SEMESTER AS VALUE, ";
        $query .= "      SEMESTERNAME AS LABEL  ";
        $query .= "  FROM   ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE  ";
        $query .= "      YEAR = '" .$year ."' ";
        $query .= "  AND SEMESTER <> '9' ";
        $query .= "  ORDER BY ";
        $query .= "      SEMESTER ";
        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $year, $semester)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '".$year."' ";
        $query .= " AND SEMESTER   = '".$semester."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1     = '" .STAFFCD ."' ";
            $query .= "      OR TR_CD2  = '" .STAFFCD ."' ";
            $query .= "      OR TR_CD3  = '" .STAFFCD ."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //生徒取得
    function getSchno($model, $year, $semester)
    {
        if ($model->field["OUTPUT"] == '2') {
            $query  = " SELECT T1.SCHREGNO,T1.ATTENDNO,T1.GRADE,T1.HR_CLASS,T2.NAME_SHOW,T3.HR_NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_BASE_MST T2, ";
            $query .= "     SCHREG_REGD_DAT T1  ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND ";
            $query .= "                                      T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ";
            $query .= "     INNER JOIN (SELECT DISTINCT SCHREGNO, YEAR FROM SCHREG_TEXTBOOK_SUBCLASS_DAT) L2 ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                               AND L2.YEAR = T1.YEAR ";
            $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
            $query .= "       T1.SEMESTER   = '".$semester."' AND ";
            $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
            $query .= "       T2.SCHREGNO   = T1.SCHREGNO ";
            $query .= "ORDER BY T1.ATTENDNO ";
        } else {
            $query .= "  SELECT ";
            $query .= "      SCHREGNO, ";
            $query .= "      NAME AS NAME_SHOW, ";
            $query .= "      '' AS GRADE, ";
            $query .= "      HR_CLASS, ";
            $query .= "      ATTENDNO ";
            $query .= "  FROM ";
            $query .= "      FRESHMAN_DAT ";
            $query .= "  WHERE ";
            $query .= "      ENTERYEAR = '".($year + 1)."' ";
        }

        return $query;
    }

    function getDocumentPrg($seq)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM   ";
        $query .= "     DOCUMENT_PRG_DAT ";
        $query .= " WHERE  ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND PROGRAMID = '".PROGRAMID."' ";
        $query .= "     AND SEQ = '{$seq}' ";

        return $query;
    }

}
?>
