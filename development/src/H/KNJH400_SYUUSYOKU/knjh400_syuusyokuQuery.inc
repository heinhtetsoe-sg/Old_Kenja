<?php

require_once('for_php7.php');

class knjh400_syuusyokuQuery extends Query
{
    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験報告データ（就職）取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TOROKU_DATE, ";
        $query .= "     T1.SENKOU_NO, ";
        $query .= "     T2.COMPANY_CD AS STAT_CD, ";
        $query .= "     T2.COMPANY_NAME AS STAT_NAME, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDR1, ";
        $query .= "     T2.ADDR2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.INDUSTRY_LCD, ";
        $query .= "     T2.INDUSTRY_MCD, ";
        $query .= "     T3.INDUSTRY_MNAME, ";
        $query .= "     T1.JOBTYPE_LCD  , ";
        $query .= "     T1.JOBTYPE_MCD, ";
        $query .= "     T1.JOBTYPE_SCD, ";
        $query .= "     T1.PREF_CD, ";
        $query .= "     T1.CITY_CD, ";
        $query .= "     T1.DECISION, ";
        $query .= "     T1.PLANSTAT, ";
        $query .= "     T1.INTRODUCTION_DIV, ";
        $query .= "     T1.JOB_THINK, ";
        $query .= "     T4.REMARK1, ";
        $query .= "     T4.REMARK2 ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     LEFT JOIN COMPANY_MST T2 ON T1.STAT_CD = T2.COMPANY_CD ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST T3 ON T2.INDUSTRY_LCD = T3.INDUSTRY_LCD AND T2.INDUSTRY_MCD = T3.INDUSTRY_MCD ";
        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT T4 ON T1.YEAR = T4.YEAR AND T1.SEQ = T4.SEQ AND T4.DETAIL_SEQ = 2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' AND ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     T1.TOROKU_DATE IS NOT NULL AND ";
        $query .= "     T1.SENKOU_KIND = '1' ";
        if ($model->gouhi == '2' || $model->gouhi == '3') {
            $query .= "     AND T1.DECISION = '" . ($model->gouhi - 1) . "' ";
        }
        if ($model->sinro == '2') {
            $query .= "     AND T1.PLANSTAT = '1' ";
        }

        return $query;
    }

    // １レコード取得(会社マスタ)
    public function &getCollegeOrCompanyMst($stat_cd)
    {
        $query  = " SELECT ";
        $query .= "     T1.COMPANY_CD AS STAT_CD, ";
        $query .= "     T1.COMPANY_NAME AS STAT_NAME, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2, ";
        $query .= "     T1.TELNO, ";
        $query .= "     T1.INDUSTRY_LCD, ";
        $query .= "     T1.INDUSTRY_MCD, ";
        $query .= "     T2.INDUSTRY_MNAME ";
        $query .= " FROM ";
        $query .= "     COMPANY_MST T1 ";
        $query .= "     LEFT JOIN INDUSTRY_M_MST T2 ON   ";
        $query .= "         T1.INDUSTRY_LCD = T2.INDUSTRY_LCD AND  ";
        $query .= "         T1.INDUSTRY_MCD = T2.INDUSTRY_MCD  ";
        $query .= " WHERE ";
        $query .= "     COMPANY_CD = '{$stat_cd}'";

        return $query;
    }
    //都道府県一覧取得
    public function getPrefList($prefcd = "")
    {
        $query  = " SELECT ";
        $query .= "     PREF_CD || '-' AS VALUE, ";
        $query .= "     PREF_CD || '：' || PREF_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        if ($prefcd) {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     PREF_CD || '-' || CITY_CD AS VALUE, ";
            $query .= "     PREF_CD || CITY_CD || '：' || CITY_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     CITY_MST ";
            $query .= " WHERE ";
            $query .= "     PREF_CD = '".$prefcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //進路調査データ一覧取得
    public function selectQuery2($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.SCHOOL_NAME AS SCHOOL_NAME1, ";
        $query .= "     L2.SCHOOL_NAME AS SCHOOL_NAME2, ";
        $query .= "     L3.JOBTYPE_LNAME AS JOBTYPE_LNAME1, ";
        $query .= "     L4.JOBTYPE_LNAME AS JOBTYPE_LNAME2, ";
        $query .= "     L6.JOBTYPE_SNAME AS JOBTYPE_SNAME1, ";
        $query .= "     L7.JOBTYPE_SNAME AS JOBTYPE_SNAME2, ";
        $query .= "     T1.QUESTIONNAIRECD || '：' || L5.QUESTIONNAIRENAME AS QUESTIONNAIRENAME ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT T1 ";
        $query .= "     LEFT JOIN COLLEGE_MST L1 ON T1.SCHOOL_CD1 = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_MST L2 ON T1.SCHOOL_CD2 = L2.SCHOOL_CD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST L3 ON T1.JOBTYPE_LCD1 = L3.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST L4 ON T1.JOBTYPE_LCD2 = L4.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST L5 ON T1.QUESTIONNAIRECD = L5.QUESTIONNAIRECD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST L6 ON T1.JOBTYPE_LCD1 = L6.JOBTYPE_LCD AND  T1.JOBTYPE_MCD1 = L6.JOBTYPE_MCD AND  T1.JOBTYPE_SCD1 = L6.JOBTYPE_SCD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST L7 ON T1.JOBTYPE_LCD2 = L7.JOBTYPE_LCD AND  T1.JOBTYPE_MCD2 = L7.JOBTYPE_MCD AND  T1.JOBTYPE_SCD2 = L7.JOBTYPE_SCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     T1.YEAR     = '{$model->year}' AND ";
        $query .= "     T1.COURSE_KIND = '2' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COURSE_KIND, ";
        $query .= "     T1.ENTRYDATE DESC, ";
        $query .= "     T1.SEQ DESC ";

        return $query;
    }
    //職種（大分類）一覧取得
    public function getJobtypeLList($lcd = "")
    {
        $query  = " SELECT ";
        $query .= "     JOBTYPE_LCD AS VALUE, ";
        $query .= "     JOBTYPE_LCD || '：' || JOBTYPE_LNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_L_MST ";
        if ($lcd) {
            $query .= " WHERE ";
            $query .= "     JOBTYPE_LCD = '".$lcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
