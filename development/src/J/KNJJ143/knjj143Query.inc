<?php

require_once('for_php7.php');

class knjj143Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //委員会区分
    function getCommitteeFlg() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'J003' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getSchkindStr($schKind) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 IN ('".$schKind."') ";

        return $query;
    }

    function getCouncilInfo($model, $schKind) {
        $query  = " WITH COMMITTEE_TBL as ( ";
        $query  .= "     SELECT ";
        $query  .= "         NAMESPARE1 AS COMMITTEE_FLG, ";
        $query  .= "         NAMESPARE2 AS COMMITTEECD ";
        $query  .= "     FROM ";
        $query  .= "         NAME_MST ";
        $query  .= "     WHERE ";
        $query  .= "         NAMECD1 = 'J009' ";
        $query  .= "     ) ";
        $query  .= " SELECT ";
        $query  .= "     SBM.NAME AS NAME, ";
        $query  .= "     SBM.NAME_KANA AS NAME_KANA, ";
        $query  .= "     SRH.HR_NAMEABBV AS HR_NAMEABBV, ";
        $query  .= "     J002_NM.NAME1 AS POSITION_NM, ";
        $query  .= "     J002_NM.ABBV3 AS POSITION_MARK, ";
        $query  .= "     J003_NM.NAME1 AS COMMITTEE_NM, ";
        $query  .= "     SC1.SCHREGNO AS SCHREGNO, ";
        $query  .= "     SC1.GRADE AS GRADE, ";
        $query  .= "     SC2.COMMITTEE_FLG AS COMMITTEE_FLG, ";
        $query  .= "     SC2.COMMITTEECD AS COMMITTEECD, ";
        $query  .= "     SC2.EXECUTIVECD AS EXECUTIVECD ";
        $query  .= " FROM ";
        $query  .= " COMMITTEE_TBL, ";
        $query  .= " SCHREG_REGD_DAT SC1 ";
        $query  .= " INNER JOIN SCHREG_BASE_MST SC3 ON SC3.SCHREGNO = SC1.SCHREGNO ";
        $query  .= " LEFT JOIN SCHREG_COMMITTEE_HIST_DAT SC2 ON SC2.YEAR = SC1.YEAR AND SC2.SCHREGNO = SC1.SCHREGNO AND SC2.SEMESTER = SC1.SEMESTER AND SC2.GRADE = SC1.GRADE ";
        $query  .= " LEFT JOIN SCHREG_BASE_MST SBM ON SBM.SCHREGNO = SC1.SCHREGNO ";
        $query  .= " LEFT JOIN NAME_MST J002_NM ON J002_NM.NAMECD1 = 'J002' AND J002_NM.NAMECD2 = SC2.EXECUTIVECD ";
        $query  .= " LEFT JOIN NAME_MST J003_NM ON J003_NM.NAMECD1 = 'J003' AND J003_NM.NAMECD2 = SC2.COMMITTEE_FLG AND J003_NM.NAMESPARE1 = '1' ";
        $query  .= " LEFT JOIN SCHREG_REGD_HDAT SRH ON  SRH.YEAR = SC1.YEAR AND SRH.SEMESTER = SC1.SEMESTER AND SRH.GRADE = SC1.GRADE AND SRH.HR_CLASS = SC1.HR_CLASS ";
        $query  .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ";
        $query  .= "   AND GDAT.SCHOOL_KIND = '".$schKind."' ";
        $query  .= " WHERE ";
        $query  .= "     SC1.YEAR = '".CTRL_YEAR."' ";
        $query  .= "     AND SC1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query  .= "     AND SC2.COMMITTEE_FLG = COMMITTEE_TBL.COMMITTEE_FLG ";
        $query  .= "     AND SC2.COMMITTEECD = COMMITTEE_TBL.COMMITTEECD ";
        $query  .= "     AND GDAT.SCHOOL_KIND = '".$schKind."' ";
        $query  .= " ORDER BY ";
        $query  .= " GDAT.SCHOOL_KIND DESC, SC2.COMMITTEECD, EXECUTIVECD DESC  ";

        return $query;
    }

    function getMaxCouncilTypeCnt($model) {
        $query  = " WITH COMMITTEE_TBL as ( ";
        $query  .= " SELECT ";
        $query  .= "     NAMESPARE1 AS COMMITTEE_FLG, ";
        $query  .= "     NAMESPARE2 AS COMMITTEECD ";
        $query  .= " FROM ";
        $query  .= "     NAME_MST ";
        $query  .= " WHERE ";
        $query  .= "     NAMECD1 = 'J009' ";
        $query  .= "), EXECUTIVE_CNT_DATA AS ( ";
        $query  .= " SELECT ";
        $query  .= "     GDAT.SCHOOL_KIND, ";
        $query  .= "     SC2.EXECUTIVECD AS EXECUTIVECD, ";
        $query  .= "     COUNT(*) AS CNT ";
        $query  .= " FROM ";
        $query  .= "     COMMITTEE_TBL CT1, ";
        $query  .= "     SCHREG_REGD_DAT SC1 ";
        $query  .= "     LEFT JOIN SCHREG_COMMITTEE_HIST_DAT SC2 ON SC2.YEAR = SC1.YEAR AND SC2.SCHREGNO = SC1.SCHREGNO AND SC2.SEMESTER = SC1.SEMESTER AND SC2.GRADE = SC1.GRADE ";
        $query  .= "     INNER JOIN SCHREG_BASE_MST SC3 ON SC3.SCHREGNO = SC1.SCHREGNO ";
        if ("1" === $model->Properties["use_prg_schoolkind"]) {
            if ("" !== $model->getSchoolKindInState($model)) {
                $query  .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ";
                $query  .= "   AND GDAT.SCHOOL_KIND IN (".$model->getSchoolKindInState($model).") ";
            }
        } else if ("1" === $model->Properties["useSchool_KindField"] && "" !== SCHOOLKIND) {
                $query  .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ";
                $query  .= "   AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query  .= " WHERE ";
        $query  .= "     SC1.YEAR = '".CTRL_YEAR."' ";
        $query  .= "     AND SC1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query  .= "     AND SC2.COMMITTEE_FLG = CT1.COMMITTEE_FLG ";
        $query  .= "     AND SC2.COMMITTEECD IS NOT NULL ";
        $query  .= " GROUP BY ";
        $query  .= "     GDAT.SCHOOL_KIND, SC2.EXECUTIVECD ";
        $query  .= " ORDER BY ";
        $query  .= "     SCHOOL_KIND ";
        $query  .= ") ";
        $query  .= "SELECT ";
        $query  .= "  A1.EXECUTIVECD, ";
        $query  .= "  J002_NM.NAME1 AS POSITION_NM, ";
        $query  .= "  MAX(A1.CNT) AS WRITE_MAX ";
        $query  .= "FROM EXECUTIVE_CNT_DATA A1 ";
        $query  .= "  LEFT JOIN NAME_MST J002_NM ON J002_NM.NAMECD1 = 'J002' AND J002_NM.NAMECD2 = A1.EXECUTIVECD ";
        $query  .= "WHERE ";
        $query  .= "  A1.EXECUTIVECD IS NOT NULL ";
        $query  .= "GROUP BY ";
        $query  .= "  A1.EXECUTIVECD, J002_NM.NAME1 ";
        $query  .= "ORDER BY ";
        $query  .= "  SUBSTR(CHAR(DECIMAL(A1.EXECUTIVECD, 4, 0)),1,4) DESC ";

        return $query;
    }

    function getCommitteeInfo($model, $schKind) {
        $query  = " WITH COMMITTEE_TBL as ( ";
        $query  .= " SELECT ";
        $query  .= "     NAMESPARE1 AS COMMITTEE_FLG, ";
        $query  .= "     NAMESPARE2 AS COMMITTEECD ";
        $query  .= " FROM ";
        $query  .= "     NAME_MST ";
        $query  .= " WHERE ";
        $query  .= "     NAMECD1 = 'J009' ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "     SBM.NAME AS NAME, ";
        $query  .= "     SBM.NAME_KANA AS NAME_KANA, ";
        $query  .= "     SRH.HR_CLASS AS HR_CLASS, ";
        $query  .= "     SRH.HR_NAMEABBV AS HR_NAMEABBV, ";
        $query  .= "     J002_NM.NAME1 AS POSITION_NM, ";
        $query  .= "     J002_NM.ABBV3 AS POSITION_MARK, ";
        $query  .= "     J003_NM.NAME1 AS COMMITTEE_NM, ";
        $query  .= "     SC1.SCHREGNO AS SCHREGNO, ";
        $query  .= "     SC1.GRADE AS GRADE, ";
        $query  .= "     SC2.COMMITTEE_FLG AS COMMITTEE_FLG, ";
        $query  .= "     SC2.COMMITTEECD AS COMMITTEECD, ";
        $query  .= "     SC2.EXECUTIVECD AS EXECUTIVECD ";
        $query  .= " FROM ";
        $query  .= " COMMITTEE_TBL, ";
        $query  .= " SCHREG_REGD_DAT SC1 ";
        $query  .= " LEFT JOIN SCHREG_COMMITTEE_HIST_DAT SC2 ON SC2.YEAR = SC1.YEAR AND SC2.SCHREGNO = SC1.SCHREGNO AND SC2.SEMESTER = SC1.SEMESTER AND SC2.GRADE = SC1.GRADE ";
        $query  .= " INNER JOIN SCHREG_BASE_MST SC3 ON SC3.SCHREGNO = SC1.SCHREGNO ";
        $query  .= " LEFT JOIN SCHREG_BASE_MST SBM ON SBM.SCHREGNO = SC1.SCHREGNO ";
        $query  .= " LEFT JOIN NAME_MST J002_NM ON J002_NM.NAMECD1 = 'J002' AND J002_NM.NAMECD2 = SC2.EXECUTIVECD ";
        $query  .= " LEFT JOIN NAME_MST J003_NM ON J003_NM.NAMECD1 = 'J003' AND J003_NM.NAMECD2 = SC2.COMMITTEE_FLG ";
        $query  .= " LEFT JOIN SCHREG_REGD_HDAT SRH ON  SRH.YEAR = SC1.YEAR AND SRH.SEMESTER = SC1.SEMESTER AND SRH.GRADE = SC1.GRADE AND SRH.HR_CLASS = SC1.HR_CLASS ";
        $query  .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ";
        $query  .= "   AND GDAT.SCHOOL_KIND = '" . $schKind . "' ";
        $query  .= " WHERE ";
        $query  .= "     SC1.YEAR = '".CTRL_YEAR."' ";
        $query  .= "     AND SC1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query  .= "     AND SC2.COMMITTEE_FLG <> COMMITTEE_TBL.COMMITTEE_FLG ";
        $query  .= "     AND SC2.COMMITTEECD IS NOT NULL";
        $query  .= "     AND GDAT.SCHOOL_KIND = '".$schKind."' ";
        $query  .= " ORDER BY ";
        $query  .= "     GDAT.SCHOOL_KIND DESC, SC1.GRADE DESC, SC1.HR_CLASS, SC2.COMMITTEE_FLG, CASE WHEN SC2.EXECUTIVECD IS NULL THEN 0 ELSE 1 END DESC, SC2.EXECUTIVECD DESC, SC2.COMMITTEECD ";

        return $query;
    }

    function getMaxCommitteeTypeCnt($model) {
        $query  = " WITH COMMITTEE_TBL as ( ";
        $query  .= " SELECT ";
        $query  .= "     NAMESPARE1 AS COMMITTEE_FLG, ";
        $query  .= "     NAMESPARE2 AS COMMITTEECD ";
        $query  .= " FROM ";
        $query  .= "     NAME_MST ";
        $query  .= " WHERE ";
        $query  .= "     NAMECD1 = 'J009' ";
        $query  .= "), COMMITTEE_CNT_DATA AS ( ";
        $query  .= " SELECT  ";
        $query  .= "     GDAT.SCHOOL_KIND, ";
        $query  .= "     SC1.GRADE AS GRADE, ";
        $query  .= "     SC1.HR_CLASS AS HR_CLASS, ";
        $query  .= "     SC2.COMMITTEE_FLG AS COMMITTEE_FLG, ";
        $query  .= "     COUNT(*) AS CNT ";
        $query  .= " FROM ";
        $query  .= "     COMMITTEE_TBL CT1, ";
        $query  .= "     SCHREG_REGD_DAT SC1 ";
        $query  .= "     LEFT JOIN SCHREG_COMMITTEE_HIST_DAT SC2 ON SC2.YEAR = SC1.YEAR AND SC2.SCHREGNO = SC1.SCHREGNO AND SC2.SEMESTER = SC1.SEMESTER AND SC2.GRADE = SC1.GRADE ";
        $query  .= "     INNER JOIN SCHREG_BASE_MST SC3 ON SC3.SCHREGNO = SC1.SCHREGNO ";
        if ("1" === $model->Properties["use_prg_schoolkind"]) {
            if ("" !== $model->getSchoolKindInState($model)) {
                $query  .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ";
                $query  .= "   AND GDAT.SCHOOL_KIND IN (".$model->getSchoolKindInState($model).") ";
            }
        } else if ("1" === ($model->Properties["useSchool_KindField"]) && "" !== (SCHOOLKIND)) {
                $query  .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SC1.YEAR AND GDAT.GRADE = SC1.GRADE ";
                $query  .= "   AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query  .= " WHERE ";
        $query  .= "     SC1.YEAR = '".CTRL_YEAR."' ";
        $query  .= "     AND SC1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query  .= "     AND SC2.COMMITTEE_FLG <> CT1.COMMITTEE_FLG ";
        $query  .= "     AND SC2.COMMITTEECD IS NOT NULL ";
        $query  .= " GROUP BY ";
        $query  .= "     GDAT.SCHOOL_KIND, SC1.GRADE, SC1.HR_CLASS, SC2.COMMITTEE_FLG ";
        $query  .= ") ";
        $query  .= "SELECT ";
        $query  .= "  A1.COMMITTEE_FLG, ";
        $query  .= "  J003_NM.NAME1 AS COMMITTEE_NM, ";
        $query  .= "  MAX(A1.CNT) AS WRITE_MAX ";
        $query  .= "FROM COMMITTEE_CNT_DATA A1 ";
        $query  .= "  LEFT JOIN NAME_MST J003_NM ON J003_NM.NAMECD1 = 'J003' AND J003_NM.NAMECD2 = A1.COMMITTEE_FLG ";
        $query  .= "GROUP BY ";
        $query  .= "  A1.COMMITTEE_FLG, J003_NM.NAME1 ";

        return $query;
    }
}
?>
