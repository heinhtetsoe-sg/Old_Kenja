<?php

require_once('for_php7.php');

class knjf030jQuery extends Query
{
    //クラス一覧取得
    public function getHrClassList($model)
    {
        $query = '';
        $query .= " SELECT GRADE||HR_CLASS AS VALUE ";
        $query .= "      , HR_NAME         AS LABEL ";
        $query .= "   FROM SCHREG_REGD_HDAT T1 ";
        $query .= "  WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    AND (";
            $query .= "            TR_CD1    = '".STAFFCD."' ";
            $query .= "         OR TR_CD2    = '".STAFFCD."' ";
            $query .= "         OR TR_CD3    = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "         OR SUBTR_CD3 = '".STAFFCD."' ";
            $query .= "        ) ";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "    AND T1.GRADE IN ( ";
                $query .= "            SELECT G1.GRADE ";
                $query .= "              FROM SCHREG_REGD_GDAT G1 ";
                $query .= "             WHERE G1.YEAR        = T1.YEAR ";
                $query .= "               AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "        ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T1.GRADE IN ( ";
            $query .= "            SELECT G1.GRADE ";
            $query .= "              FROM SCHREG_REGD_GDAT G1 ";
            $query .= "             WHERE G1.YEAR        = T1.YEAR AND ";
            $query .= "                   G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "        ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒データ
    public function getSchno($model)
    {
        $query = '';
        $query .= "   SELECT T2.SCHREGNO ";
        $query .= "        , T2.ATTENDNO ";
        $query .= "        , T1.NAME_SHOW ";
        $query .= "        , T2.GRADE ";
        $query .= "        , T2.HR_CLASS ";
        $query .= "        , T3.HR_NAME ";
        $query .= "     FROM SCHREG_BASE_MST  T1 ";
        $query .= "        , SCHREG_REGD_DAT  T2 ";
        $query .= "        , SCHREG_REGD_HDAT T3 ";
        $query .= "    WHERE T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "      AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "      AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "      AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "      AND T3.YEAR     = T2.YEAR ";
        $query .= "      AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "      AND T3.GRADE    = T2.GRADE ";
        $query .= "      AND T3.HR_CLASS = T2.HR_CLASS ";
        $query .= " ORDER BY T2.ATTENDNO ";

        return $query;
    }

    //名称マスタ
    public function getZ010()
    {
        $query = '';
        $query .= "SELECT NAME1 ";
        $query .= "  FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'Z010' ";
        $query .= "   AND NAMECD2 = '00' ";

        return $query;
    }
}
