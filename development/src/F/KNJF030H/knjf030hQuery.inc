<?php

require_once('for_php7.php');

class knjf030hQuery extends Query {
    //クラス一覧取得
    function getHrClassList($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER    = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   TR_CD1      = '".STAFFCD."' ";
            $query .= "      OR TR_CD2      = '".STAFFCD."' ";
            $query .= "      OR TR_CD3      = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD1   = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD2   = '".STAFFCD."' ";
            $query .= "      OR SUBTR_CD3   = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
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

    //生徒データ
    function getSchno($model)
    {
        $query  = "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW,T2.GRADE,T2.HR_CLASS,T3.HR_NAME ";
        $query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2,SCHREG_REGD_HDAT T3 ";
        $query .= "WHERE T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "      T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "      T2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "      T2.YEAR = T3.YEAR AND T2.SEMESTER = T3.SEMESTER AND ";
        $query .= "      T2.GRADE = T3.GRADE AND T2.HR_CLASS = T3.HR_CLASS ";
        $query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }

    //名称マスタ
    function getZ010()
    {
        $query  = "SELECT NAME1 ";
        $query .= "FROM   NAME_MST ";
        $query .= "WHERE  NAMECD1 = 'Z010' AND NAMECD2 = '00' ";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1)
    {
        $query  = "SELECT NAMECD2 AS VALUE, NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= "FROM   V_NAME_MST ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "       NAMECD1 = '".$namecd1."' ";
        $query .= "ORDER BY NAMECD2 ";

        return $query;
    }

    //校種
    function getNameMstA023()
    {
        $query  = "SELECT NAME1 AS VALUE, ABBV1 AS LABEL ";
        $query .= "FROM   NAME_MST ";
        $query .= "WHERE  NAMECD1 = 'A023' ";
        $query .= "ORDER BY VALUE ";

        return $query;
    }

}
?>
