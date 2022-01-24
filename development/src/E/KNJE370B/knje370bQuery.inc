<?php

require_once('for_php7.php');

class knje370bQuery extends Query {

    //CSVメインデータ
    function getMainData($model) {
        //SEQの最大値を求める。
        $query  = " WITH LEAST_SEQ AS ( ";
        $query .= " SELECT ";
        $query .= "  AGCD.YEAR, ";
        $query .= "  AGCD.SCHREGNO, ";
        $query .= "  MAX(AGCD.SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "  AFT_GRAD_COURSE_DAT AGCD ";
        $query .= " WHERE ";
        $query .= "  YEAR = '".$model->year."' ";
        $query .= " GROUP BY ";
        $query .= "  AGCD.YEAR, ";
        $query .= "  AGCD.SCHREGNO ";
        //AFT_GRAD_COURSE_DATで、当年度の最新データを取得する。
        $query .= " ), AFT_GRAD_BASEDAT AS ( ";
        $query .= " SELECT ";
        $query .= "  AGCD.SCHREGNO, ";
        $query .= "  SBM.SEX AS SEXCD, ";
        $query .= "  Z002.NAME1 AS SEX, ";
        $query .= "  AGCD.STAT_CD, ";
        $query .= "  AGCD.FACULTYCD, ";
        $query .= "  AGCD.DEPARTMENTCD, ";
        $query .= "  AGCD.HOWTOEXAM, ";
        $query .= "  AGCD.DECISION, ";
        $query .= "  AGCD.PLANSTAT, ";
        $query .= "  (CASE WHEN CCAD.PREF_CD > '47' THEN '1' ELSE '0' END) AS ABROAD "; //48以降は海外として登録。
        $query .= " FROM ";
        $query .= "  AFT_GRAD_COURSE_DAT AGCD ";
        $query .= "  INNER JOIN LEAST_SEQ LS ";
        $query .= "     ON LS.SCHREGNO = AGCD.SCHREGNO ";
        $query .= "    AND LS.YEAR = AGCD.YEAR ";
        $query .= "    AND LS.SEQ = AGCD.SEQ ";
        $query .= "  LEFT JOIN SCHREG_BASE_MST SBM ";
        $query .= "    ON SBM.SCHREGNO = AGCD.SCHREGNO ";
        $query .= "  LEFT JOIN NAME_MST Z002 ";
        $query .= "    ON Z002.NAMECD1 = 'Z002' ";
        $query .= "   AND Z002.NAMECD2 = SBM.SEX  ";
        $query .= "  LEFT JOIN COLLEGE_MST COLM ";
        $query .= "     ON COLM.SCHOOL_CD = AGCD.STAT_CD ";
        $query .= "  LEFT JOIN COLLEGE_CAMPUS_ADDR_DAT CCAD ";
        $query .= "     ON CCAD.SCHOOL_CD = COLM.SCHOOL_CD ";
        $query .= "    AND CCAD.CAMPUS_ADDR_CD = COLM.CAMPUS_ADDR_CD ";
        //在校生、卒業生のデータを取得する。
        $query .= " ), SCHREG_MRG AS ( ";
        $query .= " SELECT ";
        $query .= "  '0' AS GRDFLG, "; //当年度の在校生
        $query .= "  SRD.* ";
        $query .= " FROM ";
        $query .= "  SCHREG_REGD_DAT SRD ";
        $query .= " WHERE ";
        $query .= "  SRD.YEAR = '".$model->year."' ";
        $query .= "  AND SRD.SCHREGNO NOT IN (SELECT GRDW.SCHREGNO from GRD_REGD_DAT GRDW) ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "  '1' AS GRDFLG, "; //卒業生
        $query .= "  GRD.* ";
        $query .= " FROM ";
        $query .= "  GRD_REGD_DAT GRD ";
        $query .= " ORDER BY ";
        $query .= "  YEAR, ";
        $query .= "  SEMESTER, ";
        $query .= "  SCHREGNO ";
        //生徒情報から利用する情報を選択
        $query .= " ), SEL_ITEM AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "  GRDFLG, ";
        $query .= "  SCHREGNO, ";
        $query .= "  COURSECODE ";
        $query .= " FROM ";
        $query .= "  SCHREG_MRG ";
        $query .= " ORDER BY ";
        $query .= "  SCHREGNO ";
        $query .= " ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "  AGBD.SCHREGNO, ";
        $query .= "  AGBD.SEX, ";
        $query .= "  SI.COURSECODE, ";
        $query .= "  CM.COURSECODENAME, ";
        $query .= "  SUBSTR(AGBD.SCHREGNO, 5, 1) AS SCHKINDCD, ";
        $query .= "  COLM.SCHOOL_GROUP, ";
        $query .= "  COLM.SCHOOL_NAME, ";
        $query .= "  CFM.FACULTYNAME, ";
        $query .= "  CDM.DEPARTMENTNAME, ";
        $query .= "  AGBD.STAT_CD, ";
        $query .= "  AGBD.FACULTYCD, ";
        $query .= "  AGBD.DEPARTMENTCD, ";
        $query .= "  AGBD.HOWTOEXAM, ";
        $query .= "  AGBD.DECISION, ";
        $query .= "  AGBD.PLANSTAT, ";
        $query .= "  SI.GRDFLG, ";
        $query .= "  AGBD.ABROAD ";
        $query .= " FROM ";
        $query .= "  AFT_GRAD_BASEDAT AGBD ";
        $query .= "  LEFT JOIN SEL_ITEM SI ";
        $query .= "    ON SI.SCHREGNO = AGBD.SCHREGNO ";
        $query .= "  LEFT JOIN COURSECODE_MST CM ";
        $query .= "    ON CM.COURSECODE = SI.COURSECODE ";
        $query .= "  LEFT JOIN COLLEGE_MST COLM ";
        $query .= "    ON COLM.SCHOOL_CD = AGBD.STAT_CD ";
        $query .= "  LEFT JOIN COLLEGE_FACULTY_MST CFM ";
        $query .= "    ON CFM.SCHOOL_CD = AGBD.STAT_CD ";
        $query .= "   AND CFM.FACULTYCD = AGBD.FACULTYCD ";
        $query .= "  LEFT JOIN COLLEGE_DEPARTMENT_MST CDM ";
        $query .= "    ON CDM.SCHOOL_CD = AGBD.STAT_CD ";
        $query .= "   AND CDM.FACULTYCD = AGBD.FACULTYCD ";
        $query .= "   AND CDM.DEPARTMENTCD = AGBD.DEPARTMENTCD ";
//        $query .= " WHERE ";
//        $query .= "  SI.COURSECODE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "  AGBD.SCHREGNO ";

        return $query;
    }
}
?>