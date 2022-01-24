<?php

require_once('for_php7.php');

class knjd232xQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJD232X' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = "SELECT T1.GRADE, T1.GRADE_NAME1 FROM SCHREG_REGD_GDAT T1 ";
        $query .= "WHERE T1.YEAR = '" .CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "ORDER BY T1.GRADE";

        return $query;
    }

    //学期取得
    function getSelectSeme() {
        $query  = "SELECT DISTINCT SEMESTER,SEMESTERNAME FROM SEMESTER_MST ";
        $query .= "WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= "ORDER BY SEMESTER";

        return $query;
    }

    //CSV作成（成績優良者）
    function selectCsvQuery1($model) {
        $value = ($model->field["GAKKI2"] == 9) ? "GRAD_VALUE" : "SEM". $model->field["GAKKI2"]."_VALUE";
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);
        $ctrl_year = CTRL_YEAR;

        $query  = " WITH ";
        $query .= " SCHNO AS (SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T3.NAME, ";
        $query .= "             T2.GRADE, ";
        $query .= "             T2.HR_CLASS, ";
        $query .= "             T2.ATTENDNO,T2.COURSECD,T2.MAJORCD,T2.COURSECODE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T2, ";
        $query .= "             SCHREG_BASE_MST T3 ";
        $query .= "         WHERE ";
        $query .= "             T2.YEAR     = '{$ctrl_year}' AND ";
        $query .= "             T2.GRADE    = '{$model->field["GRADE"]}' AND ";
        $query .= "             T2.SEMESTER = '{$semester}' AND ";
        $query .= "             T2.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "             NOT (T3.GRD_DIV  IS NOT NULL AND ";
        $query .= "                  T3.GRD_DATE IS NOT NULL AND ";
        $query .= "                  T3.GRD_DIV IN ('2','3') AND ";
        $query .= "                  T3.GRD_DATE < '{$date}') ";
        $query .= " ) ";

        $query .= " ,RECORD AS (SELECT ";
        $query .= "                 T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T1.CLASSCD, ";
            $query .= "             T1.SCHOOL_KIND, ";
            $query .= "             T1.CURRICULUM_CD, ";
        }
        $query .= "                 T1.SUBCLASSCD, ";
        $query .= "                 T2.SUBCLASSNAME, ";
        $query .= "                 {$value} AS VALUE, ";
        $query .= "                 T4.CREDITS, ";
        $query .= "                 T1.COMP_CREDIT, ";
        $query .= "                 T1.GET_CREDIT, ";
        $query .= "                 T3.VALUATION ";
        $query .= "             FROM ";
        $query .= "                 RECORD_DAT T1 ";
        $query .= "             LEFT OUTER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND T1.CLASSCD          = T2.CLASSCD ";
            $query .= "                                        AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
            $query .= "                                        AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        }
        $query .= "             LEFT OUTER JOIN (SELECT ";
        $query .= "                                 T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             T1.CLASSCD, ";
            $query .= "                             T1.SCHOOL_KIND, ";
            $query .= "                             T1.CURRICULUM_CD, ";
        }
        $query .= "                                 T1.SUBCLASSCD , ";
        $query .= "                                 T1.CREDITS ";
        $query .= "                              FROM ";
        $query .= "                                 CREDIT_MST T1, ";
        $query .= "                                 SCHNO T2 ";
        $query .= "                              WHERE ";
        $query .= "                                 T1.YEAR         = '{$ctrl_year}' AND ";
        $query .= "                                 T1.GRADE        = T2.GRADE       AND ";
        $query .= "                                 T1.COURSECD     = T2.COURSECD    AND ";
        $query .= "                                 T1.MAJORCD      = T2.MAJORCD     AND ";
        $query .= "                                 T1.COURSECODE   = T2.COURSECODE ";
        $query .= "                             ) T4 ON  T1.SCHREGNO   = T4.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND T1.CLASSCD         = T4.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND     = T4.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD   = T4.CURRICULUM_CD ";
        }
        $query .= "                                  AND T1.SUBCLASSCD = T4.SUBCLASSCD, ";
        $query .= "             (SELECT ";
        $query .= "                 SCHREGNO, ";
        $query .= "                 DECIMAL(ROUND(DECIMAL(AVG(FLOAT({$value})),5,2),1),5,1) AS VALUATION ";
        $query .= "             FROM ";
        $query .= "                 RECORD_DAT T1 ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '{$ctrl_year}'  AND ";
        $query .= "                 SUBSTR(SUBCLASSCD,1,2) <= '89'  AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                 ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                 COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
            }
        } else {
            $query .= "             SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                    $query .= "                             ATTEND_SUBCLASSCD ";
            } else {
                    $query .= "                             COMBINED_SUBCLASSCD ";
            }
        }
        $query .= "                                   FROM ";
        $query .= "                                         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                   WHERE ";
        $query .= "                                         YEAR = '{$ctrl_year}' ";
        $query .= "                                  ) AND ";
        $query .= "                 EXISTS(SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHNO T2 ";
        $query .= "                         WHERE ";
        $query .= "                             T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                         ) ";
        $query .= "             GROUP BY ";
        $query .= "                 T1.SCHREGNO ";
        $query .= "             HAVING ";
        $query .= "                 {$model->field["ASSESS1"]} <= ROUND(AVG(FLOAT({$value})) * 10 ,0) / 10) T3 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR = '{$ctrl_year}' AND ";
        $query .= "                 SUBSTR(T1.SUBCLASSCD,1,2) <= '89' AND ";
        $query .= "                 T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "                 EXISTS(SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHNO T3 ";
        $query .= "                         WHERE ";
        $query .= "                             T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                         ) ";
        $query .= " ) ";

        $query .= " , STUDYREC AS (SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.COMP_CREDIT, 0)) AS ST_COMP, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT, 0)) AS ST_GET_ADD ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR <= '{$ctrl_year}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.CREDITS, ";
        $query .= "     T1.COMP_CREDIT, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.VALUATION, ";
        $query .= "     T3.ST_COMP, ";
        $query .= "     T3.ST_GET_ADD ";
        $query .= " FROM ";
        $query .= "     RECORD T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     STUDYREC T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                         ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                         COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
            }
        } else {
            $query .= "     T1.SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                         ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                         COMBINED_SUBCLASSCD ";
            }
        }
        $query .= "                           FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                                 YEAR = '{$ctrl_year}' ";
        $query .= "                          ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.VALUATION DESC, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //CSV作成(成績不振者)
    function selectCsvQuery2($model) {
        $value = ($model->field["GAKKI2"] == 9) ? "GRAD_VALUE" : "SEM". $model->field["GAKKI2"]."_VALUE";
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);
        $ctrl_year = CTRL_YEAR;

        $query  = " WITH ";
        $query .= " SCHNO AS(SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T3.NAME, ";
        $query .= "             T2.GRADE, ";
        $query .= "             T2.HR_CLASS, ";
        $query .= "             T2.ATTENDNO, ";
        $query .= "             T2.COURSECD, ";
        $query .= "             T2.MAJORCD, ";
        $query .= "             T2.COURSECODE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T2, ";
        $query .= "             SCHREG_BASE_MST T3 ";
        $query .= "         WHERE ";
        $query .= "            T2.YEAR      = '{$ctrl_year}' AND ";
        $query .= "            T2.GRADE     = '{$model->field["GRADE"]}' AND ";
        $query .= "            T2.SEMESTER  = '{$semester}' AND ";
        $query .= "            T2.SCHREGNO  = T3.SCHREGNO AND ";
        $query .= "            NOT (T3.GRD_DIV IS NOT NULL  AND ";
        $query .= "                 T3.GRD_DATE IS NOT NULL AND ";
        $query .= "                 T3.GRD_DIV IN('2','3')  AND ";
        $query .= "                 T3.GRD_DATE < '{$date}') ";
        $query .= " ) ";

        $query .= " , BAD_SCHNO AS(SELECT ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '{$ctrl_year}' AND ";
        $query .= "                     SUBSTR(SUBCLASSCD,1,2) <= '89'  AND ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "                     NOT EXISTS (SELECT ";
            $query .= "                                     'X' ";
            $query .= "                                 FROM ";
            $query .= "                                     CLASS_DETAIL_DAT T2 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR         = T2.YEAR AND ";
            $query .= "                                     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "                                     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "                                     T2.CLASS_SEQ    = '003') AND ";
        } else {
            $query .= "                     NOT EXISTS (SELECT ";
            $query .= "                                     'X' ";
            $query .= "                                 FROM ";
            $query .= "                                     V_NAME_MST T2 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR = T2.YEAR AND ";
            $query .= "                                     T2.NAMECD1 = 'D008' AND ";
            $query .= "                                     T2.NAMECD2 = SUBSTR(T1.SUBCLASSCD,1,2)) AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                 ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                 COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
            }
        } else {
            $query .= "                 SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                 ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                 COMBINED_SUBCLASSCD ";
            }
        }
        $query .= "                                   FROM ";
        $query .= "                                         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                   WHERE ";
        $query .= "                                         YEAR = '{$ctrl_year}' ";
        $query .= "                                  ) AND ";
        $query .= "                     ({$value} <= {$model->field["ASSESS2"]} OR {$value} IS NULL) ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 HAVING ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 {$model->field["COUNT2"]} <= COUNT(CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD) ";
        } else {
            $query .= "                 {$model->field["COUNT2"]} <= COUNT(SUBCLASSCD) ";
        }
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '{$ctrl_year}' AND ";
        $query .= "                     SUBSTR(SUBCLASSCD,1,2) <= '89'  AND ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "                     NOT EXISTS (SELECT ";
            $query .= "                                     'X' ";
            $query .= "                                 FROM ";
            $query .= "                                     CLASS_DETAIL_DAT T2 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR         = T2.YEAR AND ";
            $query .= "                                     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "                                     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "                                     T2.CLASS_SEQ    = '003') AND ";
        } else {
            $query .= "                     NOT EXISTS (SELECT ";
            $query .= "                                     'X' ";
            $query .= "                                 FROM ";
            $query .= "                                     V_NAME_MST T2 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR = T2.YEAR AND ";
            $query .= "                                     T2.NAMECD1 = 'D008' AND ";
            $query .= "                                     T2.NAMECD2 = SUBSTR(T1.SUBCLASSCD,1,2)) AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                     ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND  || '-' || COMBINED_CURRICULUM_CD  || '-' || COMBINED_SUBCLASSCD ";
            }
        } else {
            $query .= "                 SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                     ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                     COMBINED_SUBCLASSCD ";
            }
        }
        $query .= "                                         FROM ";
        $query .= "                                             SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                         WHERE ";
        $query .= "                                             YEAR = '{$ctrl_year}' ";
        $query .= "                                         ) ";
        if($model->field["GAKKI2"] == 9) {
            $query .= "                     AND (COMP_CREDIT IS NULL OR COMP_CREDIT = 0) ";
            $query .= "                     AND (GET_CREDIT IS NULL OR GET_CREDIT = 0) ";
        } else {
            $query .= "                     AND ({$value} IS NULL OR {$value} = 0 ) ";
        }
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 HAVING ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 {$model->field["UNSTUDY2"]} <= COUNT(CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD) ";
        } else {
            $query .= "                 {$model->field["UNSTUDY2"]} <= COUNT(SUBCLASSCD) ";
        }
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     RECORD_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '{$ctrl_year}' AND ";
        $query .= "                     SUBSTR(SUBCLASSCD,1,2) <= '89' AND ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "                     NOT EXISTS (SELECT ";
            $query .= "                                     'X' ";
            $query .= "                                 FROM ";
            $query .= "                                     CLASS_DETAIL_DAT T2 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR         = T2.YEAR AND ";
            $query .= "                                     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "                                     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "                                     T2.CLASS_SEQ    = '003') AND ";
        } else {
            $query .= "                     NOT EXISTS (SELECT ";
            $query .= "                                     'X' ";
            $query .= "                                 FROM ";
            $query .= "                                     V_NAME_MST T2 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR = T2.YEAR AND ";
            $query .= "                                     T2.NAMECD1 = 'D008' AND ";
            $query .= "                                     T2.NAMECD2 = SUBSTR(T1.SUBCLASSCD,1,2)) AND ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                                     ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                                     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND  || '-' || COMBINED_CURRICULUM_CD  || '-' || COMBINED_SUBCLASSCD ";
            }
        } else {
            $query .= "                 SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                     ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                     COMBINED_SUBCLASSCD ";
            }
        }
        $query .= "                                         FROM ";
        $query .= "                                             SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                         WHERE ";
        $query .= "                                             YEAR = '{$ctrl_year}' ";
        $query .= "                                         ) ";
        $query .= "                 GROUP BY ";
        $query .= "                     SCHREGNO ";
        $query .= "                 HAVING ";
        $query .= "                     {$model->field["ASSESS_AVE2"]} >= ROUND(AVG(FLOAT({$value})) * 10 ,0) / 10 ";
        $query .= " ) ";

        $query .= " , RECORD AS (SELECT ";
        $query .= "                 T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T1.CLASSCD, ";
            $query .= "             T1.SCHOOL_KIND, ";
            $query .= "             T1.CURRICULUM_CD, ";
        }
        $query .= "                 T1.SUBCLASSCD, ";
        $query .= "                 T3.SUBCLASSNAME, ";
        $query .= "                 T1.{$value} AS VALUE, ";
        $query .= "                 T4.CREDITS, ";
        $query .= "                 T1.COMP_CREDIT, ";
        $query .= "                 T1.GET_CREDIT, ";
        $query .= "                 T5.VALUATION ";
        $query .= "             FROM ";
        $query .= "                 RECORD_DAT T1 ";
        $query .= "                 INNER JOIN BAD_SCHNO T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                 LEFT JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                      AND T1.CLASSCD         = T3.CLASSCD ";
            $query .= "                                      AND T1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                                      AND T1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= "                 LEFT JOIN (SELECT ";
        $query .= "                               T2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           T1.CLASSCD , ";
            $query .= "                           T1.SCHOOL_KIND , ";
            $query .= "                           T1.CURRICULUM_CD , ";
        }
        $query .= "                               T1.SUBCLASSCD , ";
        $query .= "                               T1.CREDITS ";
        $query .= "                            FROM ";
        $query .= "                               CREDIT_MST T1, ";
        $query .= "                               SCHNO T2 ";
        $query .= "                            WHERE ";
        $query .= "                               T1.YEAR          = '{$ctrl_year}' AND ";
        $query .= "                               T1.GRADE         = T2.GRADE AND ";
        $query .= "                               T1.COURSECD      = T2.COURSECD AND ";
        $query .= "                               T1.MAJORCD       = T2.MAJORCD AND ";
        $query .= "                               T1.COURSECODE    = T2.COURSECODE ";
        $query .= "                            ) T4 ON  T1.SCHREGNO   = T4.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T1.CLASSCD          = T4.CLASSCD ";
            $query .= "                             AND T1.SCHOOL_KIND      = T4.SCHOOL_KIND ";
            $query .= "                             AND T1.CURRICULUM_CD    = T4.CURRICULUM_CD ";
        }
        $query .= "                                 AND T1.SUBCLASSCD = T4.SUBCLASSCD ";
        $query .= "                 LEFT JOIN (SELECT ";
        $query .= "                                SCHREGNO, ";
        $query .= "                                DECIMAL(ROUND(DECIMAL(AVG(FLOAT({$value})),5,2),1),5,1) AS VALUATION ";
        $query .= "                            FROM ";
        $query .= "                                RECORD_DAT T1 ";
        $query .= "                            WHERE ";
        $query .= "                                YEAR = '{$ctrl_year}'  AND ";
        $query .= "                                SUBSTR(SUBCLASSCD,1,2) <= '89'  AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                                 ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                                 COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND  || '-' || COMBINED_CURRICULUM_CD  || '-' || COMBINED_SUBCLASSCD ";
            }
        } else {
            $query .= "                             SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                                 ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                                 COMBINED_SUBCLASSCD ";
            }
        }
        $query .= "                                                     FROM ";
        $query .= "                                                         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         YEAR = '{$ctrl_year}' ";
        $query .= "                                                     ) AND ";
        $query .= "                                EXISTS(SELECT ";
        $query .= "                                            'X' ";
        $query .= "                                        FROM ";
        $query .= "                                            SCHNO T2 ";
        $query .= "                                        WHERE ";
        $query .= "                                            T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                        ) ";
        $query .= "                            GROUP BY ";
        $query .= "                                T1.SCHREGNO ";
        $query .= "                            ) T5 ON T1.SCHREGNO = T5.SCHREGNO ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR = '{$ctrl_year}' AND ";
        $query .= "                 SUBSTR(T1.SUBCLASSCD,1,2) <= '89' AND ";
        $query .= "                 EXISTS(SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHNO T3 ";
        $query .= "                         WHERE ";
        $query .= "                             T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                         ) ";
        $query .= " ) ";

        $query .= " , STUDYREC AS (SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUM(VALUE(T1.COMP_CREDIT, 0)) AS ST_COMP, ";
        $query .= "     SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT, 0)) AS ST_GET_ADD ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR <= '{$ctrl_year}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     T1.VALUE, ";
        $query .= "     T1.CREDITS, ";
        $query .= "     T1.COMP_CREDIT, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.VALUATION, ";
        $query .= "     T3.ST_COMP, ";
        $query .= "     T3.ST_GET_ADD ";
        $query .= " FROM ";
        $query .= "     RECORD T1 ";
        $query .= " INNER JOIN SCHNO    T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT  JOIN STUDYREC T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                 ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                 COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND  || '-' || COMBINED_CURRICULUM_CD  || '-' || COMBINED_SUBCLASSCD ";
            }
        } else {
            $query .= "     T1.SUBCLASSCD NOT IN (SELECT ";
            if($model->field["GAKKI2"] == 9) {
                $query .= "                                 ATTEND_SUBCLASSCD ";
            } else {
                $query .= "                                 COMBINED_SUBCLASSCD ";
            }
        }
        $query .= "                           FROM ";
        $query .= "                                 SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "                           WHERE ";
        $query .= "                                 YEAR = '{$ctrl_year}' ";
        $query .= "                          ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.VALUATION DESC, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";
        return $query;
    }

    // 遅刻何回で欠課とするかの指数取得
    function getScAbsentCov($model) {
        $db = Query::dbCheckOut();
        $query = "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '".CTRL_YEAR."'";
        $absent = array();
        $absent = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $absent;
    }
    //出欠集計開始日付などを取得
    function getAttendDate($model) {
        $query  = "SELECT SEMESTER ";
        $query .= "      ,MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR ";
        $query .= "      ,MONTH ";
        $query .= "      ,MAX(APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM   ATTEND_SEMES_DAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        if ($model->field["GAKKI2"] == 9) {
            $query .= "   AND SEMESTER != '".$model->field["GAKKI2"]."' ";
        } else {
            $query .= "   AND SEMESTER <= '".$model->field["GAKKI2"]."' ";
        }
        $query .= "GROUP BY SEMESTER,MONTH ";
        $query .= "ORDER BY 1,2,3 ";

        return $query;
    }

    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //出欠不振者ＣＳＶ
    //CVS作成用のQUERY(科目別:対象学期までの累計)
    function selectCsvQuery3($model,$attend_seme,$month,$attend_sdate,$knjSchoolMst) {
        $semester = ($model->field["GAKKI2"] == 9) ? CTRL_SEMESTER : $model->field["GAKKI2"];
        $date = str_replace("/","-",$model->field["DATE"]);
        $absent_cov = $model->absent_cov;
        $absent_cov_late = $model->absent_cov_late;
        $ctrl_year = CTRL_YEAR;

        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         ATTENDNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             YEAR     = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER = '{$semester}' ";
        $query .= "         AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ) ";

        $query .= " ,ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR = '{$ctrl_year}' ";
        $query .= "         AND SEMESTER <= '{$attend_seme}' ";
        $query .= "         AND MONTH IN ('" .implode($month, "','") ."') ";
        $query .= " ) ";

        $query .= " ,SCHEDULE AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXECUTEDATE, ";
        $query .= "         PERIODCD, ";
        $query .= "         CHAIRCD, ";
        $query .= "         DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT ";
        $query .= "     WHERE ";
        $query .= "             EXECUTEDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND PERIODCD != '0' ";
        $query .= " ) ";

        $query .= " ,T_attend_dat AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        $query .= "         W1.ATTENDDATE, ";
        $query .= "         W1.PERIODCD, ";
        $query .= "         CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ";
        $query .= "         L1.MULTIPLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_DAT W1 ";
        $query .= "         LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W1.YEAR AND L1.DI_CD = W1.DI_CD ";
        $query .= "     WHERE ";
        $query .= "             W1.ATTENDDATE BETWEEN DATE('{$attend_sdate}') AND DATE('{$date}') ";
        $query .= "         AND NOT EXISTS( SELECT ";
        $query .= "                             'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_TRANSFER_DAT T7 ";
        $query .= "                         WHERE ";
        $query .= "                                 T7.SCHREGNO = W1.SCHREGNO ";
        $query .= "                             AND T7.TRANSFERCD IN('1','2') ";
        $query .= "                             AND W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ";
        $query .= " ) ";

        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "     ) ";

        $query .= " ,T_attend AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.CLASSCD, ";
            $query .= "     S1.SCHOOL_KIND, ";
            $query .= "     S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER, ";
        $query .= "         COUNT(S3.SCHREGNO)  AS OFFDAYS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8'  THEN 1 ELSE 0 END)  AS  ABSENT, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9'  THEN 1 ELSE 0 END)  AS  SUSPEND, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ";
        $query .= "         SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE,  ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS, ";
        $query .= "         SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME ";
        $query .= "     FROM ";
        $query .= "         (SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             T1.EXECUTEDATE, ";
        $query .= "             T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "             SUBCLASSCD, ";
        $query .= "             T2.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SCHEDULE T1, ";
        $query .= "             CHAIR_STD_DAT T2, ";
        $query .= "             CHAIR_DAT T3, ";
        $query .= "             SCHNO T4 ";
        $query .= "         WHERE ";
        $query .= "                 T1.CHAIRCD  = T3.CHAIRCD ";
        $query .= "             AND T3.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "             AND T2.YEAR     = '{$ctrl_year}' ";
        $query .= "             AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "             AND T2.CHAIRCD  = T1.CHAIRCD ";
        $query .= "             AND T4.SCHREGNO = T2.SCHREGNO ";
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCH_CHR_COUNTFLG T5 ";
        $query .= "                             WHERE ";
        $query .= "                                 T5.EXECUTEDATE  = T1.EXECUTEDATE AND ";
        $query .= "                                 T5.PERIODCD     = T1.PERIODCD AND ";
        $query .= "                                 T5.CHAIRCD      = T1.CHAIRCD AND ";
        $query .= "                                 T5.GRADE        = T4.GRADE AND ";
        $query .= "                                 T5.HR_CLASS     = T4.HR_CLASS AND ";
        $query .= "                                 T1.DATADIV      IN ('0','1') AND "; //テスト(DATADIV=2)以外
        $query .= "                                 T5.COUNTFLG     = '0' ";
        $query .= "                           ) ";
        $query .= "             AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                             WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                               AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                               AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                               AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        $query .= "             AND NOT EXISTS( SELECT ";
        $query .= "                                 'X' ";
        $query .= "                             FROM ";
        $query .= "                                 SCHREG_BASE_MST T6 ";
        $query .= "                             WHERE ";
        $query .= "                                 T6.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ";
        $query .= "                                  (T6.ENT_DIV IN('4','5')     AND T6.ENT_DATE > T1.EXECUTEDATE)) ";
        $query .= "                           ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '27' "; // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "    AND NOT EXISTS(SELECT ";
        $query .= "                       'X' ";
        $query .= "                   FROM ";
        $query .= "                       ATTEND_DAT L4 ";
        $query .= "                       LEFT JOIN ATTEND_DI_CD_DAT ATT_DI ON L4.YEAR = ATT_DI.YEAR AND L4.DI_CD = ATT_DI.DI_CD ";
        $query .= "                   WHERE ";
        $query .= "                       L4.SCHREGNO = T2.SCHREGNO ";
        $query .= "                       AND L4.ATTENDDATE = T1.EXECUTEDATE ";
        $query .= "                       AND L4.PERIODCD = T1.PERIODCD ";
        $query .= "                       AND ATT_DI.REP_DI_CD = '28' "; // 勤怠コード'28'は時間割にカウントしない
        $query .= "                  ) ";
        $query .= "     GROUP BY ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         T2.SEMESTER)S1 ";
        $query .= "     LEFT JOIN T_attend_dat        S2 ON  S2.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                                      AND S2.PERIODCD   = S1.PERIODCD ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON  S3.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S3.TRANSFERCD = '2' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ";
        $query .= "     LEFT JOIN SCHREG_TRANSFER_DAT S4 ON  S4.SCHREGNO   = S1.SCHREGNO ";
        $query .= "                                      AND S4.TRANSFERCD = '1' ";
        $query .= "                                      AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ";
        $query .= "     GROUP BY ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     S1.CLASSCD, ";
            $query .= "     S1.SCHOOL_KIND, ";
            $query .= "     S1.CURRICULUM_CD, ";
        }
        $query .= "         S1.SUBCLASSCD, ";
        $query .= "         S1.SEMESTER ";
        $query .= " ) ";

        $query .= " ,ATTEND_SUM AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W1.CLASSCD, ";
            $query .= "     W1.SCHOOL_KIND, ";
            $query .= "     W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER, ";
        $query .= "         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .=           "+ VALUE(W1.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .=           "+ VALUE(W1.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .=           "+ VALUE(W1.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .=           "+ VALUE(W1.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .=           "+ VALUE(W1.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .=           "+ VALUE(W1.KOUDOME,0)";
        }
        $query .= "            ) AS SICK, ";
        $query .= "         SUM(VALUE(W1.NOTICE,0))     NOTICE, ";
        $query .= "         SUM(VALUE(W1.NONOTICE,0))   NONOTICE, ";
        $query .= "         SUM(VALUE(W1.LATE,0))       LATE, ";
        $query .= "         SUM(VALUE(W1.EARLY,0))      EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS W1, ";
        $query .= "         SCHNO W0 ";
        $query .= "     WHERE ";
        $query .= "         W1.SCHREGNO = W0.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         W1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W1.CLASSCD, ";
            $query .= "     W1.SCHOOL_KIND, ";
            $query .= "     W1.CURRICULUM_CD, ";
        }
        $query .= "         W1.SUBCLASSCD, ";
        $query .= "         W1.SEMESTER ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         W2.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W2.CLASSCD, ";
            $query .= "     W2.SCHOOL_KIND, ";
            $query .= "     W2.CURRICULUM_CD, ";
        }
        $query .= "         W2.SUBCLASSCD, ";
        $query .= "         W2.SEMESTER, ";
        $query .= "         VALUE(W2.SICK,0) + VALUE(W2.NURSEOFF,0) ";
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $query .= "          + VALUE(W2.ABSENT,0)";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $query .= "          + VALUE(W2.SUSPEND,0)";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $query .= "          + VALUE(W2.MOURNING,0)";
        }
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $query .= "          + VALUE(W2.OFFDAYS,0)";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $query .= "          + VALUE(W2.VIRUS,0)";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $query .= "          + VALUE(W2.KOUDOME,0)";
        }
        $query .= "                AS SICK, ";
        $query .= "         VALUE(W2.NOTICE,0)      NOTICE, ";
        $query .= "         VALUE(W2.NONOTICE,0)    NONOTICE, ";
        $query .= "         VALUE(W2.LATE,0)        LATE, ";
        $query .= "         VALUE(W2.EARLY,0)       EARLY ";
        $query .= "     FROM ";
        $query .= "         T_attend W2 ";
        $query .= " ) ";

        //学期毎に清算
        $query .= " ,ATTEND_SUM2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY ";
        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((sum(LATE) + sum(EARLY)) / {$absent_cov_late}) + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE ";
        } else {
            $query .= "      ,sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SEMESTER ";
        $query .= " ) ";

        //年間で清算
        $query .= " ,ATTEND_SUM3 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SUM(SICK)       SICK, ";
        $query .= "         SUM(NOTICE)     NOTICE, ";
        $query .= "         SUM(NONOTICE)   NONOTICE, ";
        $query .= "         SUM(LATE)       LATE, ";
        $query .= "         SUM(EARLY)      EARLY, ";
        $query .= "         SUM(NOTICE_LATE) NOTICE_LATE1 ";
        if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)),4,1) AS NOTICE_LATE2 ";
        } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((SUM(LATE) + SUM(EARLY)) / {$absent_cov_late}) + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) AS NOTICE_LATE2 ";
        } else {
            $query .= "      ,SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2 ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM2 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= " ) ";

        //学籍番号でグルーピング
        $query .= ",ATTEND_SUM4 AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         0 AS LATE, ";
        $query .= "         0 AS EARLY, ";
        $query .= "         0 AS NOTICE, ";
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "  SUM(NOTICE_LATE1) AS NOTICE_LATE ";
        } else {
            $query .= "  SUM(NOTICE_LATE2) AS NOTICE_LATE ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUM3 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        //遅刻・早退・欠席取得
        $query .= " ,ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(VALUE(LATE,0))  LATE, ";
        $query .= "         SUM(VALUE(EARLY,0)) EARLY, ";
        $query .= "         SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ";
        if ($knjSchoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "        + VALUE(OFFDAYS,0) ";
        }
        $query .= "         ) AS NOTICE, ";
        $query .= "         0 AS NOTICE_LATE ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$ctrl_year}' AND ";
        $query .= "         SEMESTER <= '{$attend_seme}' AND ";
        $query .= "         MONTH IN ('" .implode($month, "','") ."') ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        //メイン
        $query .= " ,MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.NAME, ";
        $query .= "         SUM(T2.LATE) AS LATE, ";
        $query .= "         SUM(T2.EARLY) AS EARLY, ";
        $query .= "         SUM(T2.NOTICE) AS NOTICE, ";
        $query .= "         SUM(T2.NOTICE_LATE) AS NOTICE_LATE ";
        $query .= "     FROM ";
        $query .= "         SCHNO T1, ";
        $query .= "         (SELECT * FROM ATTEND_SUM4 UNION ALL SELECT * FROM ATTEND_SEMES) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.NAME ";
        $query .= " ) ";

        $late   = ($model->field["LATE5"] == "")            ? 99999 : $model->field["LATE5"];
        $early  = ($model->field["EARLY5"] == "")           ? 99999 : $model->field["EARLY5"];
        $absent = ($model->field["ABSENT5"] == "")          ? 99999 : $model->field["ABSENT5"];
        $kekka  = ($model->field["SUBCLASS_ABSENT5"] == "") ? 99999 : $model->field["SUBCLASS_ABSENT5"];

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     (LATE        >= {$late}  OR ";
        $query .= "      EARLY       >= {$early} OR ";
        $query .= "      NOTICE      >= {$absent} OR ";
        $query .= "      NOTICE_LATE >= {$kekka} ) ";
        $query .= " ORDER BY ";
        $query .= "     5 DESC, ";
        $query .= "     6 DESC, ";
        $query .= "     7 DESC, ";
        $query .= "     8 DESC, ";
        $query .= "     1, ";
        $query .= "     2 ";

        return $query;
    }
}
?>
