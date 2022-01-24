<?php

require_once('for_php7.php');

class knjc120aQuery extends Query
{

    //学年名コンボ取得
    public function getGradeName()
    {
        $query   = " SELECT ";
        $query  .= "     H.GRADE AS VALUE, ";
        $query  .= "     G.GRADE_NAME1 AS LABEL ";
        $query  .= " FROM ";
        $query  .= "     SCHREG_REGD_HDAT AS H ";
        $query  .= "     JOIN SCHREG_REGD_GDAT AS G ";
        $query  .= " ON ( ";
        $query  .= "         G.YEAR  = H.YEAR ";
        $query  .= "     AND G.GRADE = H.GRADE) ";
        $query  .= " WHERE ";
        $query  .= "     H.YEAR = '".CTRL_YEAR."' ";
        $query  .= "     AND G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query  .= " GROUP BY ";
        $query  .= "     H.GRADE, ";
        $query  .= "     G.GRADE_NAME1 ";
        $query  .= " ORDER BY ";
        $query  .= "     H.GRADE ";

        return $query;
    }

    //年組取得コンボ
    public function getGradeHrclassName($model)
    {
        $query   = " SELECT ";
        $query  .= "        SRH.GRADE || SRH.HR_CLASS AS VALUE ";
        $query  .= "      , SRH.HR_NAME AS LABEL ";
        $query  .= "   FROM ";
        $query  .= "        SCHREG_REGD_HDAT SRH";
        $query  .= "  LEFT JOIN SCHREG_REGD_GDAT SRG ";
        $query  .= "         ON SRH.YEAR  = SRG.YEAR ";
        $query  .= "        AND SRH.GRADE = SRG.GRADE ";
        $query  .= "  WHERE ";
        $query  .= "        SRH.YEAR        = '".CTRL_YEAR."' ";
        $query  .= "    AND SRH.SEMESTER    = '".CTRL_SEMESTER."' ";
        $query  .= "    AND SRH.GRADE       = '".$model->field["GRADE_NAME"]."' ";
        $query  .= " ORDER BY ";
        $query  .= "        SRH.GRADE ";

        return $query;
    }

    //NAME_MSTを取得
    public function getMonth()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMESPARE1 = '01' AND ";
        if (SCHOOLKIND == 'H') {
            $query .= "     NAMECD1 = 'ZH05' ";
        } else {
            $query .= "     NAMECD1 = 'ZJ05' ";
        }

        return $query;
    }

    //年組取得
    public function getHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME  AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND";
        $query .= "     GRADE    = '".$model->field["GRADE_NAME"]."' AND";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     HR_CLASS ";

        return $query;
    }

    //生徒データ
    public function getSchno($model)
    {
        $query  = " SELECT";
        $query .= "        T2.SCHREGNO || ' ' || T2.ATTENDNO || '番' || ' ' || T1.NAME_SHOW AS LABEL ";
        $query .= "      , T2.SCHREGNO || '-' || T2.GRADE || T2.HR_CLASS || T2.ATTENDNO AS VALUE ";
        $query .= " FROM ";
        $query .= "        SCHREG_BASE_MST T1";
        $query .= "      , SCHREG_REGD_DAT T2";
        $query .= "      , SCHREG_REGD_HDAT T3 ";
        $query .= " WHERE";
        $query .= "       T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "   AND T2.GRADE || T2.HR_CLASS  = '".$model->field["GRADE_HRCLASS_NAME"]."' ";
        $query .= "   AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "   AND T2.YEAR = T3.YEAR AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "   AND T2.GRADE = T3.GRADE AND T2.HR_CLASS = T3.HR_CLASS ";
        $query .= " ORDER BY";
        $query .= "       T2.GRADE ";
        $query .= "     , T2.HR_CLASS ";
        $query .= "     , T2.ATTENDNO ";

        return $query;
    }
}
