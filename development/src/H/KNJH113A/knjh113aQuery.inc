<?php

require_once('for_php7.php');

class knjh113aQuery extends Query {

    //資格データ取得
    function getQualifiedList() {
        $query  = " SELECT ";
        $query .= "     QUALIFIED_CD AS VALUE, ";
        $query .= "     QUALIFIED_CD || '  ' || QUALIFIED_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ作成
    function selectCsvQuery($model) {

        $query .= " WITH SCHINFO AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.SEX ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO, ";
        $query .= "     SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T3.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.SEX, ";
        $query .= "     T1.REGDDATE, ";
        $query .= "     Q1.CONDITION_DIV, ";
        $query .= "     T1.QUALIFIED_CD, ";
        $query .= "     Q1.QUALIFIED_NAME, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.SCORE, ";
        $query .= "     Q1.PROMOTER, ";
        $query .= "     T1.CERTIFICATE, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
        $query .= "     LEFT JOIN QUALIFIED_MST Q1 ON T1.QUALIFIED_CD = Q1.QUALIFIED_CD, ";
        $query .= "     SCHINFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T1.QUALIFIED_CD IN ('".implode("','", explode(',', $model->selectdata))."') AND ";
        $query .= "     T1.REGDDATE BETWEEN '".str_replace("/","-",$model->field["SDATE"])."' AND '".str_replace("/","-",$model->field["EDATE"])."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.QUALIFIED_CD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.REGDDATE, ";
        $query .= "     T1.CONDITION_DIV, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //名称マスタより取得
    function getNameMst($namecd1, $namecd2, $name)
    {
        $query  = " SELECT ";
        $query .=       $name ;
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }
}
?>
