<?php

require_once('for_php7.php');


class knja195aQuery extends Query {
    //クラス一覧取得
    function getHrClassList($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER    = '".CTRL_SEMESTER."' ";
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
                $query .= "                     G1.YEAR = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
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
        $query .= " AND EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT D1 INNER JOIN AFT_GRAD_COURSE_DAT I1 ON I1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                         AND I1.SCHREGNO = D1.SCHREGNO ";
        $query .= "                         AND I1.SENKOU_KIND = '0' ";
        $query .= "                         AND I1.DECISION = '5' ";
        $query .= "                 WHERE ";
        $query .= "                         D1.YEAR = T1.YEAR ";
        $query .= "                     AND D1.SEMESTER = T1.SEMESTER ";
        $query .= "                     AND D1.GRADE = T1.GRADE ";
        $query .= "                     AND D1.HR_CLASS = T1.HR_CLASS ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒データ
    function getSchno($model)
    {
        $query  = "SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        if ($model->schoolName == "rithumeikan"){
            $query .= "     CASE WHEN E002.NAMESPARE3 = '1' THEN '●' ELSE '　' END AS MARK ";
        } else {
            $query .= "     '' AS MARK ";
        }
        $query .= "FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "             ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "            AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "            AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "            AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ";
        $query .= "             ON T2.YEAR     = T3.YEAR ";
        $query .= "            AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "            AND T2.GRADE    = T3.GRADE ";
        $query .= "            AND T2.HR_CLASS = T3.HR_CLASS ";
        $query .= "     INNER JOIN ( SELECT DISTINCT ";
        $query .= "                    YEAR, SCHREGNO, HOWTOEXAM  ";
        $query .= "                  FROM AFT_GRAD_COURSE_DAT ";
        $query .= "                  WHERE YEAR        = '".CTRL_YEAR."' ";
        $query .= "                    AND SENKOU_KIND = '0' ";
        $query .= "                    AND DECISION    = '5' ";
        $query .= "                ) I1 ";
        $query .= "             ON I1.YEAR        = T2.YEAR ";
        $query .= "            AND I1.SCHREGNO    = T2.SCHREGNO ";
        if ($model->schoolName == "rithumeikan"){
            $query .= "     LEFT JOIN NAME_MST E002 ";
            $query .= "            ON E002.NAMECD2 = I1.HOWTOEXAM ";
            $query .= "           AND E002.NAMECD1 = 'E002' ";
        }
        $query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }

    function GetYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     FISCALYEAR(T1.GRD_DATE) AS VALUE, ";
        $query .= "     FISCALYEAR(T1.GRD_DATE) AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRD_BASE_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC";
        return $query;
    }

    //卒業時組取得
    function getHrclass($model) {
        $query  = " WITH HR_CLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS ";
        $query .= "     FROM ";
        $query .= "         V_GRD_REGDYEAR_GRADE_DAT ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS ";
        $query .= "     FROM ";
        $query .= "         V_GRD_REGDYEAR_UNIT_DAT ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS || '：' || T2.HR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     HR_CLASS T1, ";
        $query .= "     GRD_REGD_HDAT T2 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T2.YEAR = REG_G.YEAR ";
                $query .= "           AND T2.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T2.YEAR AND T6.GRADE = T2.GRADE ";
            $query .= " AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '{$model->field["G_YEAR"]}' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";

        return $query;
    }

    //学校区分抽出
    function GetSchoolDiv($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOLDIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["G_YEAR"]}'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    function getGrdSchno($model) {
        $query  = " SELECT ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T5.HR_NAME, ";
        $query .= "     T3.SEMESTER, ";
        $query .= "     T1.NAME, ";
        $query .= "     T3.YEAR AS GRADUATEYEAR, ";
        $query .= "     T1.NAME_KANA AS KANA, ";
        $query .= "     T1.SEX AS SEXNUM, ";
        $query .= "     CASE T1.SEX WHEN T4.NAMECD2 THEN T4.NAME2 ELSE T1.SEX END AS SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '' AS MARK ";
        $query .= " FROM ";
        $query .= "     GRD_BASE_MST T1 LEFT OUTER JOIN ";
        $query .= "     (SELECT NAME2, NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'Z002') T4 ON T1.SEX = T4.NAMECD2";
        //単位制
        if($model->schooldiv == "1") {
            $query .= "      ,V_GRD_REGDYEAR_UNIT_DAT T3 ";
        } else {
        //学年制
            $query .= "      ,V_GRD_REGDYEAR_GRADE_DAT T3 ";
        }
        $query .= "      ,GRD_REGD_HDAT T5 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON T5.YEAR = REG_G.YEAR ";
                $query .= "           AND T5.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T5.YEAR AND T6.GRADE = T5.GRADE ";
            $query .= " AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     AND FISCALYEAR(T1.GRD_DATE) = T3.YEAR ";
        $query .= "     AND T5.YEAR = T3.YEAR ";
        $query .= "     AND T5.SEMESTER = T3.SEMESTER ";
        $query .= "     AND T5.GRADE = T3.GRADE ";
        $query .= "     AND T5.HR_CLASS = T3.HR_CLASS ";
        if (strlen($model->field["G_YEAR"])) {
            $query .= "     AND T3.YEAR = '{$model->field["G_YEAR"]}' ";
        }
        if (strlen($model->field["GRADE_HR_CLASS"])) {
            $query .= "     AND T3.GRADE || '-' || T3.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        if (strlen($model->field["KANJI"])) {
            $query .= "     AND T1.NAME_SHOW LIKE '%{$model->field["KANJI"]}%' ";
        }
        if (strlen($model->field["KANA"])) {
            $query .= "     AND T1.NAME_KANA LIKE '%{$model->field["KANA"]}%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADUATEYEAR, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO ";

        return $query;
    }
    
    //名称マスタの取得
    function getNameMst($namecd1, $namecd2, $field)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "       NAMECD1 = '".$namecd1."' ";
        $query .= "   AND NAMECD2 = '".$namecd2."' ";
        return $query;
    }

}
?>
