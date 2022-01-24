<?php

require_once('for_php7.php');

class knjb226Query extends Query
{

    //学期
    public function getSemester($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$model->year}' ";
        $query .= "    AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //履修履歴
    public function getRirekiCode($model, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg == "") {
            $query .= "     RIREKI_CODE AS VALUE, ";
            $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        } else {
            $query .= "     CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS DATE ";
        }
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["RISHUU_YEAR"]}' ";
        if ($flg == "") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= " AND RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        }

        return $query;
    }

    //学年
    public function getRegdGdat($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス
    public function getRegdHdat($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //履修年度
    public function getRishuuYear($model)
    {
        $ctrl_year = CTRL_YEAR;
        $next_year = CTRL_YEAR + 1;

        $query  = " WITH T_COMMON (VALUE,LABEL) AS ( ";
        $query .= "     VALUES('{$ctrl_year}','{$ctrl_year}') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('{$next_year}','{$next_year}') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //ＣＳＶ出力
    public function getCsvQuery($model)
    {
        $query  = "";

        $query .= " WITH T_SUB AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "     T2.SUBCLASSCD AS SUBCLASSCD, ";
        }
        $query .= "         L1.CREDITS ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SUBCLASS_STD_SELECT_RIREKI_DAT T2 ";
        $query .= "                 ON  T2.YEAR = '{$model->field["RISHUU_YEAR"]}' ";
        $query .= "                 AND T2.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "                 AND T2.RIREKI_CODE = '{$model->field["RIREKI_CODE"]}' ";
        $query .= "                 AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN CREDIT_MST L1  ";
        $query .= "                 ON  L1.YEAR = T2.YEAR ";
        if ($model->field["RISHUU_YEAR"] != $model->year) {
            $nextGrade = sprintf("%02d", $model->field["GRADE"] + 1);
            $query .= "             AND L1.GRADE = '{$nextGrade}' ";
        } else {
            $query .= "             AND L1.GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= "                 AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L1.CLASSCD = T2.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "                 AND L1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->year}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "         AND T1.GRADE || T1.HR_CLASS IN ('".implode("','", $model->selectdata)."') ";
        $query .= " ) ";
        $query .= " , T_SUB_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         COUNT(T1.SUBCLASSCD) AS SUB_CNT, ";
        $query .= "         SUM(T1.CREDITS) AS CREDITS_SUM ";
        $query .= "     FROM ";
        $query .= "         T_SUB T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T3.NAME_SHOW, ";
        $query .= "     L1.SUB_CNT, ";
        $query .= "     L1.CREDITS_SUM ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "             ON  T2.YEAR = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T2.GRADE = T1.GRADE ";
        $query .= "             AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN T_SUB_CNT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS IN ('".implode("','", $model->selectdata)."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
