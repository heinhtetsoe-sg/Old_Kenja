<?php

require_once('for_php7.php');

class knjb300Query extends Query {

    //学期
    function getSemesterInfo($model) {
        $query .= " SELECT SEME.* ";
        $query .= " FROM SEMESTER_MST SEME ";
        $query .= " WHERE SEME.YEAR = '{$model->year}' ";
        $query .= "   AND SEME.SEMESTER = '{$model->semester}' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE ";
        $query .= "   , ABBV1 AS LABEL ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE NAMECD1 = 'A023' ";

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   AND NAME1 IN ('".SCHOOLKIND."') ";
        }

        $query .= " ORDER BY ";
        $query .= "     ABBV3, NAMECD2 ";
        $query .= "  ";

        return $query;
    }

    //年組取得
    function getHrClass($model) {

        $query  = " SELECT ";
        $query .= "     REGDH.GRADE || '-' || REGDH.HR_CLASS AS VALUE ";
        $query .= "   , REGDH.HR_NAME AS LABEL ";
        $query .= " FROM SCHREG_REGD_HDAT REGDH ";
        $query .= " INNER JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "    ON REGDH.YEAR = REGDG.YEAR ";
        $query .= "   AND REGDH.GRADE = REGDG.GRADE ";

        $query .= " WHERE REGDH.YEAR='{$model->year}'";
        $query .= "   AND REGDH.SEMESTER='{$model->semester}'";
        if ($model->field['SCHOOL_KIND']) {
            $query .= "   AND REGDG.SCHOOL_KIND='{$model->field['SCHOOL_KIND']}'";
        }

        return $query;
    }

    // 校時取得(名称マスタ)
    function getPeriod($model) {
        $query  = " SELECT ";
        $query .= "     ROW_NUMBER() OVER (ORDER BY NAMECD2) ROWNUM ";
        $query .= "   , NAMECD2 PERIODCD ";
        $query .= "   , NAME1 PERIODNAME ";
        $query .= " FROM V_NAME_MST T1 ";
        $query .= " WHERE T1.YEAR = '{$model->year}' ";
        $query .= "   AND T1.NAMECD1 = 'B001' ";
        $query .= " ORDER BY T1.NAMECD2 ";

        return $query;
    }

    // CSV出力用データ取得
    function selectCsvQuery($model) {

        $executeDate = str_replace("/", "-", $model->field['EXECUTEDATE']);
        $startDate = str_replace("/", "-", $model->field['START_DATE']);
        $endDate = str_replace("/", "-", $model->field['END_DATE']);

        $query  = "";

        $query .= " WITH PERIOD AS ( ";
        $query .= "   SELECT ";
        $query .= "       NAMECD2 PERIODCD ";
        $query .= "     , NAME1 PERIODNAME ";
        $query .= "   FROM V_NAME_MST ";
        $query .= "   WHERE YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   SCHCHR.YEAR ";
        $query .= "   , SCHCHR.SEMESTER ";
        $query .= "   , SCHCHR.EXECUTEDATE ";
        $query .= "   , SCHCHR.PERIODCD ";
        $query .= "   , PERIOD.PERIODNAME ";
        $query .= "   , REGD.GRADE ";
        $query .= "   , REGD.HR_CLASS ";
        $query .= "   , REGDG.GRADE_NAME1 ";
        $query .= "   , REGDH.HR_NAME ";
        $query .= "   , REGD.ATTENDNO ";
        $query .= "   , SCHREG.SCHREGNO ";
        $query .= "   , SCHREG.NAME ";
        $query .= "   , SCHCHR.CHAIRCD ";
        $query .= "   , CHAIR.CHAIRNAME ";
        $query .= "   , SUBCLASS.SUBCLASSNAME ";

        $query .= " FROM ";
        $query .= "   SCH_CHR_DAT SCHCHR ";
        $query .= "   INNER JOIN PERIOD ";
        $query .= "     ON SCHCHR.PERIODCD = PERIOD.PERIODCD ";
        $query .= "   INNER JOIN CHAIR_DAT CHAIR ";
        $query .= "     ON SCHCHR.YEAR = CHAIR.YEAR ";
        $query .= "     AND SCHCHR.SEMESTER = CHAIR.SEMESTER ";
        $query .= "     AND SCHCHR.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= "   INNER JOIN SUBCLASS_MST SUBCLASS ";
        $query .= "     ON CHAIR.CLASSCD = SUBCLASS.CLASSCD ";
        $query .= "     AND CHAIR.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ";
        $query .= "     AND CHAIR.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ";
        $query .= "     AND CHAIR.SUBCLASSCD = SUBCLASS.SUBCLASSCD ";
        $query .= "   INNER JOIN CHAIR_STD_DAT CHRSTD ";
        $query .= "     ON SCHCHR.YEAR = CHRSTD.YEAR ";
        $query .= "     AND SCHCHR.SEMESTER = CHRSTD.SEMESTER ";
        $query .= "     AND SCHCHR.CHAIRCD = CHRSTD.CHAIRCD ";
        $query .= "     AND SCHCHR.EXECUTEDATE BETWEEN CHRSTD.APPDATE AND CHRSTD.APPENDDATE ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "     ON CHRSTD.YEAR = REGD.YEAR ";
        $query .= "     AND CHRSTD.SEMESTER = REGD.SEMESTER ";
        $query .= "     AND CHRSTD.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   INNER JOIN SCHREG_BASE_MST SCHREG ";
        $query .= "     ON CHRSTD.SCHREGNO = SCHREG.SCHREGNO ";
        $query .= "   INNER JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "     ON REGD.YEAR = REGDG.YEAR ";
        $query .= "     AND REGD.GRADE = REGDG.GRADE ";
        $query .= "   INNER JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "     ON REGD.YEAR = REGDH.YEAR ";
        $query .= "     AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "     AND REGD.GRADE = REGDH.GRADE ";
        $query .= "     AND REGD.HR_CLASS = REGDH.HR_CLASS ";

        $query .= " WHERE ";
        if ($model->field['TYEP_DATE'] == '1') {
            $query .= "   SCHCHR.EXECUTEDATE = '{$executeDate}' ";
        } else {
            $query .= "   SCHCHR.EXECUTEDATE BETWEEN '{$startDate}' AND '{$endDate}' ";
        }
        if ($model->selectHrClass) {
            $hrClassList = array();
            for ($i=0; $i < get_count($model->selectHrClass); $i++) { 
                list($grade, $hrClass) = explode('-', $model->selectHrClass[$i]);
                $hrClassList[] = "VALUES('{$grade}', '{$hrClass}') ";
            }

            $query .= "   AND (REGD.GRADE, REGD.HR_CLASS) IN ( ";
            $query .= implode($hrClassList, " UNION ");
            $query .= "   ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     SCHCHR.YEAR ";
        $query .= "   , SCHCHR.SEMESTER ";
        $query .= "   , SCHCHR.EXECUTEDATE ";
        $query .= "   , REGD.GRADE ";
        $query .= "   , REGD.HR_CLASS ";
        $query .= "   , REGD.ATTENDNO ";
        $query .= "   , SCHREG.SCHREGNO ";
        $query .= "   , SCHCHR.PERIODCD ";
        $query .= "   , SCHCHR.CHAIRCD ";

        return $query;
    }

    // 施設取得
    function getFacility($model, $executeDate, $period, $chairCd) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "   SCHCHR.EXECUTEDATE ";
        $query .= "   , SCHCHR.PERIODCD ";
        $query .= "   , SCHCHR.CHAIRCD ";
        $query .= "   , CASE WHEN SCHFAC.FACCD IS NOT NULL ";
        $query .= "       THEN SCHFAC.FACCD ";
        $query .= "       ELSE CHRFAC.FACCD ";
        $query .= "     END FACCD ";
        $query .= "   , CASE WHEN SCHFAC.FACCD IS NOT NULL ";
        $query .= "       THEN FAC2.FACILITYNAME ";
        $query .= "       ELSE FAC1.FACILITYNAME ";
        $query .= "     END FACILITYNAME ";
        $query .= " FROM ";
        $query .= "   SCH_CHR_DAT SCHCHR ";
        $query .= "   LEFT JOIN CHAIR_FAC_DAT CHRFAC ";
        $query .= "     ON SCHCHR.YEAR = CHRFAC.YEAR ";
        $query .= "     AND SCHCHR.SEMESTER = CHRFAC.SEMESTER ";
        $query .= "     AND SCHCHR.CHAIRCD = CHRFAC.CHAIRCD ";
        $query .= "   LEFT JOIN FACILITY_MST FAC1 ";
        $query .= "     ON CHRFAC.FACCD = FAC1.FACCD ";
        $query .= "   LEFT JOIN SCH_FAC_DAT SCHFAC ";
        $query .= "     ON SCHCHR.EXECUTEDATE = SCHFAC.EXECUTEDATE ";
        $query .= "     AND SCHCHR.PERIODCD = SCHFAC.PERIODCD ";
        $query .= "     AND SCHCHR.CHAIRCD = SCHFAC.CHAIRCD ";
        $query .= "   LEFT JOIN FACILITY_MST FAC2 ";
        $query .= "     ON SCHFAC.FACCD = FAC2.FACCD ";

        $query .= " WHERE SCHCHR.EXECUTEDATE = '{$executeDate}' ";
        $query .= "   AND SCHCHR.PERIODCD = '{$period}' ";
        $query .= "   AND SCHCHR.CHAIRCD = '{$chairCd}' ";

        return $query;
    }
}
?>
