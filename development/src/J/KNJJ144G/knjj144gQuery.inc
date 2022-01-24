<?php

require_once('for_php7.php');


class knjj144gQuery extends Query {

    //名称マスタ取得
    function getNameMst($nameCd1, $nameCd2) {
        $query  = " SELECT ";
        $query .= "     YEAR ";
        $query .= "   , NAMECD1 ";
        $query .= "   , NAMECD2 ";
        $query .= "   , NAME1 ";
        $query .= "   , NAME2 ";
        $query .= "   , NAMECD2 AS VALUE ";
        $query .= "   , NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '".$nameCd1."' ";
        if ($nameCd2 != "") {
            $query .= " AND NAMECD2 = '".$nameCd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model, $year, $semester ) {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "      ON T2.YEAR  = T1.YEAR";
        $query .= "     AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " AND T1.GRADE    = '".$model->field["GRADE"]."' ";

        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //CSVデータ取得
    function selectQuery($model) {

        $query .= " SELECT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEQ ";
        $query .= "   , T1.SCHREGNO ";
        $query .= "   , REGDB.NAME ";
        $query .= "   , T2.NUMBER_OF_TIMES ";
        $query .= "   , T2.EVENT_NAME ";
        $query .= "   , T2.EVENT_DATE ";
        $query .= "   , T2.MAN_METERS ";
        $query .= "   , T2.WOMEN_METERS ";
        $query .= "   , T1.SEX ";
        $query .= "   , T1.TIME_H ";
        $query .= "   , T1.TIME_M ";
        $query .= "   , T1.TIME_S ";
        $query .= "   , T1.GRADE_RANK_SEX RANK ";
        $query .= "   , REGDH.HR_NAME ";
        $query .= "   , CER.PRINCIPAL_NAME ";
        $query .= " FROM ";
        $query .= "   MARATHON_EVENT_RANK_DAT T1 ";
        $query .= "   INNER JOIN MARATHON_EVENT_MST T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEQ = T2.SEQ ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "     ON T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     AND T1.YEAR = REGD.YEAR ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "     ON REGD.YEAR = REGDH.YEAR ";
        $query .= "     AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "     AND REGD.GRADE = REGDH.GRADE ";
        $query .= "     AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "   INNER JOIN SCHREG_BASE_MST REGDB ";
        $query .= "     ON REGDB.SCHREGNO = T1.SCHREGNO ";
        $query .= "   LEFT JOIN CERTIF_SCHOOL_DAT CER ";
        $query .= "     ON CER.YEAR = T1.YEAR ";
        $query .= "     AND CER.CERTIF_KINDCD = '101' ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T1.SEX = '".$model->field["SEX"]."' ";

        $hrClassList = array();
        if ($model->selectdata) {
            $selectData = explode(",", $model->selectdata);
            for ($i=0; $i < get_count($selectData); $i++) { 
                list($grade, $hrClass) = explode("-", $selectData[$i]);
                $hrClassList[] = "VALUES('{$grade}', '{$hrClass}')";
            }
            $query .= "   AND (REGD.GRADE, REGD.HR_CLASS) IN ( ";
            $query .= implode(" UNION ", $hrClassList);
            $query .= "   ) ";
        }

        $query .= " ORDER BY ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEQ ";
        $query .= "   , T1.GRADE_RANK_SEX ";
        $query .= "   , T1.SCHREGNO ";

        return $query;
    }
}
?>
