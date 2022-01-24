<?php

require_once('for_php7.php');

class knjh712Query extends Query
{

    //名称マスタ取得
    public function getNameMst($model, $namecd1)
    {
        $query  = "";
        $query  = "   SELECT NAMECD2 AS VALUE";
        $query .= "        , NAME1 AS LABEL";
        $query .= "     FROM V_NAME_MST";
        $query .= "    WHERE YEAR    = '".CTRL_YEAR."' ";
        $query .= "      AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //年組取得コンボ
    public function getGradeHrclassName($model)
    {
        $query  = "";
        $query  = "    SELECT SRH.GRADE || SRH.HR_CLASS AS VALUE ";
        $query .= "         , SRH.HR_NAME AS LABEL ";
        $query .= "      FROM SCHREG_REGD_HDAT SRH";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT SRG ";
        $query .= "        ON SRH.YEAR     = SRG.YEAR ";
        $query .= "       AND SRH.GRADE    = SRG.GRADE ";
        $query .= "     WHERE SRH.YEAR     = '".CTRL_YEAR."' ";
        $query .= "       AND SRH.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND SRH.GRADE    = '03' ";
        $query .= "  ORDER BY SRH.GRADE";

        return $query;
    }

    //年組取得
    public function getHrClass($model)
    {
        $query  = "";
        $query .= "   SELECT GRADE || HR_CLASS AS VALUE ";
        $query .= "        , HR_NAME AS LABEL";
        $query .= "     FROM SCHREG_REGD_HDAT ";
        $query .= "    WHERE YEAR     = '".CTRL_YEAR."' ";
        $query .= "      AND GRADE    = '03' ";
        $query .= "      AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY HR_CLASS ";

        return $query;
    }

    //生徒データ
    public function getSchno($model)
    {
        $query  = "";
        $query .= " WITH SAD AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "          , YEAR ";
        $query .= "          , DECLINE_FLG ";
        $query .= "       FROM SCHREG_ACADEMICTEST_DAT ";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   GROUP BY SCHREGNO ";
        $query .= "          , YEAR ";
        $query .= "          , DECLINE_FLG ";
        $query .= " ) ";
        $query .= "     SELECT SRD.SCHREGNO || ' ' || SRD.ATTENDNO || '番' || ' ' || SBM.NAME_SHOW AS LABEL ";
        $query .= "          , SRD.SCHREGNO || '-' || SRD.GRADE || SRD.HR_CLASS || SRD.ATTENDNO AS VALUE ";
        $query .= "          , SAD.DECLINE_FLG ";
        $query .= "       FROM SCHREG_REGD_DAT SRD  ";
        $query .= "       JOIN SCHREG_BASE_MST SBM ";
        $query .= "         ON SRD.SCHREGNO = SBM.SCHREGNO ";
        $query .= "  LEFT JOIN SAD ";
        $query .= "         ON SAD.YEAR = SRD.YEAR ";
        $query .= "        AND SAD.SCHREGNO = SBM.SCHREGNO ";
        $query .= "      WHERE SRD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "        AND SRD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "        AND SRD.GRADE || SRD.HR_CLASS = '".$model->field["GRADE_HRCLASS_NAME"]."' ";
        $query .= "        AND SAD.DECLINE_FLG <> '1' ";
        $query .= "   GROUP BY SRD.SCHREGNO ";
        $query .= "          , SRD.GRADE ";
        $query .= "          , SRD.HR_CLASS ";
        $query .= "          , SRD.ATTENDNO ";
        $query .= "          , SBM.NAME_SHOW ";
        $query .= "          , SAD.DECLINE_FLG ";
        $query .= "   ORDER BY SRD.GRADE ";
        $query .= "          , SRD.HR_CLASS ";
        $query .= "          , SRD.ATTENDNO ";

        return $query;
    }
}
