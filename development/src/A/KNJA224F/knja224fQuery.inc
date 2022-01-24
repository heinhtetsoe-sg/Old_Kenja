<?php

require_once('for_php7.php');


class knja224fQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .CTRL_YEAR ."' ";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //CSV出力
    function selectCsvQuery($grd_cls, $model)
    {
        $query .= " SELECT ";
        $query .= "   T1.HR_NAME,";
        $query .= "   T2.HR_CLASS,";
        $query .= "   T2.ATTENDNO,";
        // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）
        if (strlen($model->field["NAME_NASI"])) {
            $query .= "   CASE WHEN T3.GRD_DIV IN ('1','2','3') THEN '' ELSE CASE WHEN T3.SEX = '2' THEN '*' ELSE '' END END AS SEX, ";
            $query .= "   CASE WHEN T3.GRD_DIV IN ('1','2','3') THEN '' ELSE T3.NAME END AS NAME, ";
            $query .= "   CASE WHEN T3.GRD_DIV IN ('1','2','3') THEN '' ELSE T3.NAME_KANA END AS NAME_KANA ";
        } else {
            $query .= "   CASE WHEN T3.SEX = '2' THEN '*' ELSE '' END AS SEX, ";
            $query .= "   T3.NAME,";
            $query .= "   T3.NAME_KANA ";
        }
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT T1";
        $query .= "   INNER JOIN SCHREG_REGD_DAT T2   ";
        $query .= "      ON T2.YEAR     = T1.YEAR     ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.GRADE    = T1.GRADE    ";
        $query .= "     AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "   INNER JOIN SCHREG_BASE_MST T3 ";
        $query .= "      ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR                 = '".$model->field["YEAR"]."' ";
        $query .= "   AND T1.SEMESTER             = '".$model->field["GAKKI"]."' ";
        $query .= "   AND T1.GRADE || T1.HR_CLASS = '" .$grd_cls. "' ";
        $query .= " ORDER BY T2.ATTENDNO";

        return $query;
    }
}
?>
