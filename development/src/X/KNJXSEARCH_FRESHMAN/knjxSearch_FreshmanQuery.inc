<?php

require_once('for_php7.php');

class knjxSearch_FreshmanQuery extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->exeYear."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->setSchoolKind != "") {
                $query .= "     AND NAME1 = '".$model->setSchoolKind."' ";
            } else if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "                 T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') AS VALUE, ";
        $query .= "     '新入生' || T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') AS LABEL ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.ENTERYEAR = T2.YEAR AND T1.GRADE = T2.GRADE AND T2.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.ENTERYEAR = T2.YEAR AND T1.GRADE = T2.GRADE AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTERYEAR = '".$model->exeYear."' ";
        $query .= "     AND T1.GRADE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function SearchStudent($fields, $model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        $query .= "     VALUE(T1.HR_CLASS, '000') AS HR_CLASS, ";
        $query .= "     VALUE(T1.ATTENDNO, '000') AS ATTENDNO, ";
        $query .= "     T1.ENTERYEAR, ";
        $query .= "     T1.NAME AS NAME, ";
        $query .= "     T1.NAME_KANA AS KANA, ";
        $query .= "     T1.SEX AS SEXNUM, ";
        $query .= "     Z002.NAME2 AS SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '新入生' || T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') AS HR_NAME ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.ENTERYEAR = T2.YEAR AND T1.GRADE = T2.GRADE AND T2.SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.ENTERYEAR = T2.YEAR AND T1.GRADE = T2.GRADE AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND Z002.NAMECD2 = T1.SEX ";
        $query .= " WHERE ";
        $query .= "     T1.ENTERYEAR = '".$model->exeYear."' ";
        $query .= "     AND T1.GRADE IS NOT NULL ";
        if (strlen($fields["class"]))
            $query .= "   AND T1.GRADE || '-' || VALUE(T1.HR_CLASS, '000') = '".$fields["class"]."'";
        if (strlen($fields["kanji"]))
            $query .= "   AND T1.NAME LIKE '%".$fields["kanji"]."%'";
        if (strlen($fields["kana"]))
            $query .= "   AND T1.NAME_KANA LIKE '%".$fields["kana"]."%'";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO ";

        return $query;
    }
}
?>
