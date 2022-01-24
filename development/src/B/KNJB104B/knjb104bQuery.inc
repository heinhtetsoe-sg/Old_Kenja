<?php

require_once('for_php7.php');

class knjb104bQuery extends Query
{
    //学期
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }
    //学年
    public function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }
    //考査
    public function getKousa($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || ':' || TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCORE_DIV = '01' ";
        $query .= " ORDER BY ";
        $query .= "     TESTKINDCD, TESTITEMCD, SCORE_DIV ";

        return $query;
    }
    //試験会場
    public function getFacility($model)
    {
        $query .= " SELECT ";
        $query .= "     FACCD AS VALUE, ";
        $query .= "     FACILITYNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_FACILITY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     FACCD ";

        return $query;
    }

    //登録データ
    public function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T2.TESTITEMNAME, ";
        $query .= "     T3.NAME1 AS PERIODNAME, ";
        $query .= "     T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || T5.SUBCLASSCD || ':' || T5.SUBCLASSNAME AS SUBCLASSNAME, ";
        $query .= "     T4.CHAIRCD || ':' || T4.CHAIRNAME AS CHAIRNAME, ";
        $query .= "     LISTAGG(CAST(T7.GRADE || T7.HR_CLASS || ':' || T7.HR_NAME AS VARCHAR(10000)), '<br>') WITHIN GROUP(ORDER BY T1.EXECUTEDATE) AS HR_NAME, ";
        $query .= "     T8.FACCD, ";
        $query .= "     T4.CHAIRCD, ";
        $query .= "     T1.PERIODCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST T1 ";
        $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "     AND T1.TESTKINDCD = T2.TESTKINDCD ";
        $query .= "     AND T1.TESTITEMCD = T2.TESTITEMCD ";
        $query .= "     AND T2.SCORE_DIV = '01' ";
        $query .= " LEFT JOIN V_NAME_MST T3 ";
        $query .= "     ON T1.YEAR = T3.YEAR ";
        $query .= "     AND T3.NAMECD1 = 'B004' ";
        $query .= "     AND T1.PERIODCD = T3.NAMECD2 ";
        $query .= " LEFT JOIN CHAIR_DAT T4 ";
        $query .= "     ON T1.YEAR = T4.YEAR ";
        $query .= "     AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T4.CHAIRCD ";
        $query .= " LEFT JOIN SUBCLASS_MST T5 ";
        $query .= "     ON T4.CLASSCD = T5.CLASSCD ";
        $query .= "     AND T4.SCHOOL_KIND = T5.SCHOOL_KIND ";
        $query .= "     AND T4.CURRICULUM_CD = T5.CURRICULUM_CD ";
        $query .= "     AND T4.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= " LEFT JOIN CHAIR_CLS_DAT AS T6 ";
        $query .= "     ON T1.YEAR = T6.YEAR ";
        $query .= "     AND T1.SEMESTER = T6.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T6.CHAIRCD ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T7 ";
        $query .= "     ON T1.YEAR = T7.YEAR ";
        $query .= "     AND T1.SEMESTER = T7.SEMESTER ";
        $query .= "     AND T6.TRGTGRADE = T7.GRADE ";
        $query .= "     AND T6.TRGTCLASS = T7.HR_CLASS ";
        $query .= " LEFT JOIN CHAIR_TEST_FAC_DAT T8 ";
        $query .= "     ON T1.YEAR = T8.YEAR ";
        $query .= "     AND T1.SEMESTER = T8.SEMESTER ";
        $query .= "     AND T1.CHAIRCD = T8.CHAIRCD ";
        $query .= "     AND T8.FACCD = (SELECT MIN(FACCD) FROM CHAIR_TEST_FAC_DAT WHERE YEAR = T1.YEAR AND SEMESTER = T1.SEMESTER AND CHAIRCD = T1.CHAIRCD) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        if ($model->semester != '') {
            $query .= "     AND T1.SEMESTER ='{$model->semester}' ";
        }
        if ($model->grade != '') {
            $query .= "     AND T7.GRADE ='{$model->grade}' ";
        }
        if ($model->kousa != '') {
            $query .= "     AND T2.TESTKINDCD ='".substr($model->kousa, 0, 2)."' ";
            $query .= "     AND T2.TESTITEMCD ='".substr($model->kousa, 2, 2)."' ";
            $query .= "     AND T2.SCORE_DIV ='".substr($model->kousa, 4, 2)."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T2.TESTITEMNAME, ";
        $query .= "     T3.NAME1, ";
        $query .= "     T5.CLASSCD, ";
        $query .= "     T5.SCHOOL_KIND, ";
        $query .= "     T5.CURRICULUM_CD, ";
        $query .= "     T5.SUBCLASSCD, ";
        $query .= "     T5.SUBCLASSNAME, ";
        $query .= "     T4.CHAIRCD, ";
        $query .= "     T4.CHAIRNAME, ";
        $query .= "     T8.FACCD, ";
        $query .= "     T1.PERIODCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T4.CHAIRCD ";

        return $query;
    }

    public function updateFacility($db, $model, $indata)
    {
        foreach ($indata as $chaircd => $facilitys) {
            $query  = " DELETE FROM ";
            $query .= "     CHAIR_TEST_FAC_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR ='{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND CHAIRCD = '{$chaircd}' ";

            $db->query($query);

            $idx = null;
            for ($i = 0; $i < get_count($facilitys); $i++) {
                if ($facilitys[$i] != '') {
                    $idx = $i;
                    break;
                }
            }
            if (is_null($idx)) {
                continue;
            }

            $data = array();
            $data["YEAR"][TEXT]       = $model->year;
            $data["SEMESTER"][TEXT]   = $model->semester;
            $data["CHAIRCD"][TEXT]    = $chaircd;
            $data["FACCD"][TEXT]      = $facilitys[$idx];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC]    = "sysdate()";

            $db->query(Query::insertSQL($data, "CHAIR_TEST_FAC_DAT"));
        }
    }
}
