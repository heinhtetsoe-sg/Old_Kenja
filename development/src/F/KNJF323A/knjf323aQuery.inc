<?php

require_once('for_php7.php');


class knjf323aQuery extends Query
{
    //クラス一覧取得
    public function getHrClassList($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->ctrlYear."' ";
        $query .= " AND SEMESTER = '".$model->ctrlSemester."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部活動一覧取得
    public function getClubList($model)
    {
        $query  = " SELECT ";
        $query .= "     MST.SCHOOL_KIND || '-' || MST.CLUBCD AS VALUE, ";
        $query .= "     MST.CLUBCD || '　' || MST.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST MST ";
        $query .= "     INNER JOIN CLUB_YDAT YDAT ";
        $query .= "             ON YDAT.SCHOOLCD    = MST.SCHOOLCD ";
        $query .= "            AND YDAT.SCHOOL_KIND = MST.SCHOOL_KIND ";
        $query .= "            AND YDAT.CLUBCD      = MST.CLUBCD ";
        $query .= "            AND YDAT.YEAR        = '".$model->ctrlYear."' ";
        $query .= " WHERE ";
        $query .= "     MST.SCHOOLCD    = '".$model->schoolcd."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //クラス・部活動一覧取得
    public function selectCsvQuery($model)
    {
        $query  = " WITH SCHREG AS ( ";
        if ($model->field["KUBUN"] == "1") {
            $query .= "     SELECT ";
            $query .= "         DAT.YEAR, ";
            $query .= "         DAT.SEMESTER, ";
            $query .= "         DAT.SCHREGNO, ";
            $query .= "         DAT.GRADE || DAT.HR_CLASS AS PAGE_ITEM_CD, ";
            $query .= "         HDAT.HR_NAME AS PAGE_ITEM_NAME ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT DAT ";
            $query .= "         INNER JOIN SCHREG_REGD_HDAT HDAT ";
            $query .= "                 ON HDAT.YEAR     = DAT.YEAR ";
            $query .= "                AND HDAT.SEMESTER = DAT.SEMESTER ";
            $query .= "                AND HDAT.GRADE    = DAT.GRADE ";
            $query .= "                AND HDAT.HR_CLASS = DAT.HR_CLASS ";
            $query .= "     WHERE ";
            $query .= "         DAT.YEAR     = '".$model->ctrlYear."' ";
            $query .= "     AND DAT.SEMESTER = '".$model->ctrlSemester."' ";
            $query .= "     AND DAT.GRADE || DAT.HR_CLASS IN ('".implode("','", $model->selectdata)."') ";
        } else {
            $query .= "     SELECT ";
            $query .= "         '".$model->ctrlYear."' AS YEAR, ";
            $query .= "         '".$model->ctrlSemester."' AS SEMESTER, ";
            $query .= "         HIST.SCHREGNO, ";
            $query .= "         CLUB.SCHOOL_KIND || '-' || CLUB.CLUBCD AS PAGE_ITEM_CD, ";
            $query .= "         CLUB.CLUBNAME AS PAGE_ITEM_NAME ";
            $query .= "     FROM ";
            $query .= "         CLUB_MST CLUB ";
            $query .= "         INNER JOIN SCHREG_CLUB_HIST_DAT HIST ";
            $query .= "                 ON HIST.SCHOOLCD    = CLUB.SCHOOLCD ";
            $query .= "                AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ";
            $query .= "                AND HIST.CLUBCD      = CLUB.CLUBCD ";
            $query .= "                AND (HIST.EDATE IS NULL OR (TO_DATE('".$model->ctrlDate."', 'YYYY/MM/DD') BETWEEN HIST.SDATE AND HIST.EDATE)) ";
            $query .= "     WHERE ";
            $query .= "         CLUB.SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "     AND CLUB.SCHOOL_KIND || '-' || CLUB.CLUBCD IN ('".implode("','", $model->selectdata)."') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCHREG.PAGE_ITEM_CD, ";
        $query .= "     SCHREG.PAGE_ITEM_NAME, ";
        $query .= "     VALUE(DAT.GRADE, 0) || '年' || VALUE(DAT.HR_CLASS, 0) || '組' || VALUE(DAT.ATTENDNO, 0) || '番' AS GRADE_HR_CLASS_ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     GUARDIAN.GUARD_TELNO, ";
        $query .= "     F143_1.NAME1 AS MEDICAL_HISTORY1, ";
        $query .= "     F143_2.NAME1 AS MEDICAL_HISTORY2, ";
        $query .= "     F143_3.NAME1 AS MEDICAL_HISTORY3, ";
        $query .= "     F090.NAME1 AS HEARTDISEASE, ";
        $query .= "     MEDEXAM.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT DAT ";
        $query .= "             ON DAT.YEAR     = SCHREG.YEAR ";
        $query .= "            AND DAT.SEMESTER = SCHREG.SEMESTER ";
        $query .= "            AND DAT.SCHREGNO = SCHREG.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ";
        $query .= "            ON BASE.SCHREGNO = DAT.SCHREGNO ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "            ON Z002.YEAR    = DAT.YEAR ";
        $query .= "           AND Z002.NAMECD1 = 'Z002' ";
        $query .= "           AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN GUARDIAN_DAT GUARDIAN ";
        $query .= "            ON GUARDIAN.SCHREGNO = DAT.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_DET_DAT MEDEXAM ";
        $query .= "            ON MEDEXAM.YEAR     = DAT.YEAR ";
        $query .= "           AND MEDEXAM.SCHREGNO = DAT.SCHREGNO ";
        $query .= "     LEFT JOIN V_NAME_MST F143_1 ";
        $query .= "            ON F143_1.YEAR    = DAT.YEAR ";
        $query .= "           AND F143_1.NAMECD1 = 'F143' ";
        $query .= "           AND F143_1.NAMECD2 = MEDEXAM.MEDICAL_HISTORY1 ";
        $query .= "     LEFT JOIN V_NAME_MST F143_2 ";
        $query .= "            ON F143_2.YEAR    = DAT.YEAR ";
        $query .= "           AND F143_2.NAMECD1 = 'F143' ";
        $query .= "           AND F143_2.NAMECD2 = MEDEXAM.MEDICAL_HISTORY2 ";
        $query .= "     LEFT JOIN V_NAME_MST F143_3 ";
        $query .= "            ON F143_3.YEAR    = DAT.YEAR ";
        $query .= "           AND F143_3.NAMECD1 = 'F143' ";
        $query .= "           AND F143_3.NAMECD2 = MEDEXAM.MEDICAL_HISTORY3 ";
        $query .= "     LEFT JOIN V_NAME_MST F090 ";
        $query .= "            ON F090.YEAR    = DAT.YEAR ";
        $query .= "           AND F090.NAMECD1 = 'F090' ";
        $query .= "           AND F090.NAMECD2 = MEDEXAM.HEARTDISEASECD ";
        $query .= " ORDER BY ";
        $query .= "     SCHREG.PAGE_ITEM_CD, ";
        $query .= "     DAT.GRADE, ";
        $query .= "     DAT.HR_CLASS, ";
        $query .= "     DAT.ATTENDNO ";

        return $query;
    }
}
