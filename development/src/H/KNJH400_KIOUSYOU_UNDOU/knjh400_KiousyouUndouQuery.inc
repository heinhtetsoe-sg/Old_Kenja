<?php

require_once('for_php7.php');

class knjh400_KiousyouUndouQuery extends Query
{
    //年度一覧取得
    public function getYearList($model)
    {
        $query  = " WITH THIS_YEAR (YEAR) AS (VALUES('".CTRL_YEAR."')) ";
        $query .= " , MAIN AS ( ";
        $query .= "     SELECT YEAR FROM MEDEXAM_CARE_HDAT WHERE SCHREGNO = '".$model->schregno."' ";
        $query .= "     UNION ";
        $query .= "     SELECT YEAR FROM MEDEXAM_CARE_DAT WHERE SCHREGNO = '".$model->schregno."' ";
        $query .= "     UNION ";
        $query .= "     SELECT YEAR FROM THIS_YEAR ";
        $query .= " ) ";
        $query .= " SELECT YEAR || '年度' AS LABEL, YEAR AS VALUE FROM MAIN ORDER BY VALUE DESC ";

        return $query;
    }

    //データを取得（メイン画面）
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S2.HR_NAME, ";
        $query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "     S1.NAME, ";
        $query .= "     S1.BIRTHDAY, ";
        $query .= "     S3.NAME2 AS SEX, ";
        $query .= "     S1.BLOODTYPE, ";
        $query .= "     S1.BLOOD_RH, ";
        $query .= "     S4.MEDICAL_HISTORY1, ";
        $query .= "     S4.MEDICAL_HISTORY2, ";
        $query .= "     S4.MEDICAL_HISTORY3, ";
        $query .= "     S4.DIAGNOSIS_NAME, ";
        $query .= "     S4.GUIDE_DIV, ";
        $query .= "     S4.JOINING_SPORTS_CLUB, ";
        $query .= "     M1.CARE_FLG AS CARE_FLG01, ";
        $query .= "     M2.CARE_FLG AS CARE_FLG02, ";
        $query .= "     M3.CARE_FLG AS CARE_FLG03, ";
        $query .= "     M4.CARE_FLG AS CARE_FLG04, ";
        $query .= "     M5.CARE_FLG AS CARE_FLG05, ";
        $query .= "     M6.CARE_FLG AS CARE_FLG06 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ";
        $query .= "          ON T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ";
        $query .= "          ON T1.YEAR     = S2.YEAR ";
        $query .= "         AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "         AND T1.GRADE    = S2.GRADE ";
        $query .= "         AND T1.HR_CLASS = S2.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST S3 ";
        $query .= "          ON S3.NAMECD1  = 'Z002' ";
        $query .= "         AND S3.NAMECD2  = S1.SEX ";
        $query .= "     LEFT JOIN MEDEXAM_DET_DAT S4 ";
        $query .= "          ON T1.YEAR     = S4.YEAR ";
        $query .= "         AND T1.SCHREGNO = S4.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M1 ";
        $query .= "          ON T1.YEAR     = M1.YEAR ";
        $query .= "         AND T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "         AND M1.CARE_DIV = '01' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M2 ";
        $query .= "          ON T1.YEAR     = M2.YEAR ";
        $query .= "         AND T1.SCHREGNO = M2.SCHREGNO ";
        $query .= "         AND M2.CARE_DIV = '02' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M3 ";
        $query .= "          ON T1.YEAR     = M3.YEAR ";
        $query .= "         AND T1.SCHREGNO = M3.SCHREGNO ";
        $query .= "         AND M3.CARE_DIV = '03' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M4 ";
        $query .= "          ON T1.YEAR     = M4.YEAR ";
        $query .= "         AND T1.SCHREGNO = M4.SCHREGNO ";
        $query .= "         AND M4.CARE_DIV = '04' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M5 ";
        $query .= "          ON T1.YEAR     = M5.YEAR ";
        $query .= "         AND T1.SCHREGNO = M5.SCHREGNO ";
        $query .= "         AND M5.CARE_DIV = '05' ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M6 ";
        $query .= "          ON T1.YEAR     = M6.YEAR ";
        $query .= "         AND T1.SCHREGNO = M6.SCHREGNO ";
        $query .= "         AND M6.CARE_DIV = '06' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //名称マスタ取得
    public function getNameMst($namecd1, $label = "")
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($label) {
            $query .= "     ".$label." || ':' || NAME1 AS LABEL ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス名称取得
    public function getHrName($model)
    {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     HR_CLASS    = '".$model->hr_class."' ";

        return $query;
    }

    //ヘッダデータ取得
    public function getCareHData($model, $year, $care_div)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_CARE_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     CARE_DIV = '".$care_div."' ";

        return $query;
    }

    //データ取得
    public function getCareData($model, $year, $care_div)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO, ";
        $query .= "     CARE_KIND, ";
        $query .= "     CARE_ITEM, ";
        $query .= "     CARE_SEQ, ";
        $query .= "     CARE_REMARK1, ";
        $query .= "     CARE_REMARK2 ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_CARE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     CARE_DIV = '".$care_div."' ";
        $query .= " ORDER BY ";
        $query .= "     CARE_KIND, ";
        $query .= "     CARE_ITEM, ";
        $query .= "     CARE_SEQ ";

        return $query;
    }

    //データ取得
    public function getMedexamData($model, $table, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     * ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }
    
    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S1.HR_NAME || T1.ATTENDNO || '番' AS ATTENDNO, ";
        $query .= "     S2.NAME, ";
        $query .= "     L1.ABBV1 AS SEX, ";
        $query .= "     S2.BIRTHDAY, ";
        $query .= "     M1.CARE_DIV, ";
        $query .= "     M1.CARE_FLG, ";
        $query .= "     M1.EMERGENCYNAME, ";
        $query .= "     M1.EMERGENCYTELNO, ";
        $query .= "     M1.EMERGENCYNAME2, ";
        $query .= "     M1.EMERGENCYTELNO2, ";
        $query .= "     M1.DATE, ";
        $query .= "     M1.DOCTOR, ";
        $query .= "     M1.HOSPITAL, ";
        $query .= "     M1.REMARK, ";
        $query .= "     M2.CARE_KIND, ";
        $query .= "     M2.CARE_ITEM, ";
        $query .= "     M2.CARE_SEQ, ";
        $query .= "     M2.CARE_REMARK1, ";
        $query .= "     M2.CARE_REMARK2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT S1 ";
        $query .= "          ON T1.YEAR     = S1.YEAR ";
        $query .= "         AND T1.SEMESTER = S1.SEMESTER ";
        $query .= "         AND T1.GRADE    = S1.GRADE ";
        $query .= "         AND T1.HR_CLASS = S1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST S2 ";
        $query .= "          ON T1.SCHREGNO = S2.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1  = 'Z002' ";
        $query .= "         AND L1.NAMECD2  = S2.SEX ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_HDAT M1 ";
        $query .= "          ON T1.YEAR     = M1.YEAR ";
        $query .= "         AND T1.SCHREGNO = M1.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_CARE_DAT M2 ";
        $query .= "          ON T1.YEAR     = M2.YEAR ";
        $query .= "         AND T1.SCHREGNO = M2.SCHREGNO ";
        $query .= "         AND M2.CARE_DIV = M1.CARE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     M1.CARE_DIV, ";
        $query .= "     M2.CARE_KIND, ";
        $query .= "     M2.CARE_ITEM, ";
        $query .= "     M2.CARE_SEQ ";

        return $query;
    }
}
