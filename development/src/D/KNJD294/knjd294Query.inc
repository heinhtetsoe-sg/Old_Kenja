<?php

require_once('for_php7.php');

class knjd294Query extends Query {
    //年組取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     L1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT L1 ";
            $query .= "                 WHERE ";
            $query .= "                     L1.YEAR         = T1.YEAR AND ";
            $query .= "                     L1.SCHOOL_KIND  = '".SCHOOLKIND."') ";
        }
        //高校専用
        $query .= " AND GRADE IN (  SELECT ";
        $query .= "                     L2.GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT L2 ";
        $query .= "                 WHERE ";
        $query .= "                     L2.YEAR         = T1.YEAR AND ";
        $query .= "                     L2.SCHOOL_KIND  = 'H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ一覧取得
    function getDataList($model) {
        $query  = " WITH SCH_MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         L1.NAME_SHOW, ";
        $query .= "         L1.ENT_DATE, ";
        $query .= "         L2.BASE_REMARK1 AS GRAD_YOTEI ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "                  ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "                 AND T1.YEAR     = L2.YEAR ";
        $query .= "                 AND L2.BASE_SEQ = '001' ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE || '-' || T1.HR_CLASS = '".$model->grade_hr_class."' ";
        $query .= " ), COMP_SUBCLASS_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         '1' AS COMP_EXE_FLG ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_MST T1 ";
        $query .= "         INNER JOIN SUBCLASS_STD_SELECT_DAT S1 ";
        $query .= "                  ON S1.YEAR             = '".CTRL_YEAR."' ";
        $query .= "                 AND S1.SEMESTER         = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND S1.CLASSCD          = T1.CLASSCD ";
            $query .= "                 AND S1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "                 AND S1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "                 AND S1.SUBCLASSCD       = T1.SUBCLASSCD ";
        $query .= "         INNER JOIN SCH_MAIN S2 ON S2.SCHREGNO = S1.SCHREGNO ";
        $query .= "     WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD < '91' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "         SUBSTR(T1.SUBCLASSCD,1,2) < '91' ";
        }
        $query .= " ), ANOTHER_SCHOOL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         SUM(INT(T1.PERIOD_MONTH_CNT)) AS ANOTHER_MONTHS ";
        $query .= "     FROM ";
        $query .= "         ANOTHER_SCHOOL_HIST_DAT T1, ";
        $query .= "         FINSCHOOL_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.FORMER_REG_SCHOOLCD = T2.FINSCHOOLCD AND ";
        $query .= "         T2.FINSCHOOL_TYPE = '4' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), ANOTHER_SUBCLASS_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND_SAKI AS SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD_SAKI AS CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD_SAKI AS SUBCLASSCD, ";
        $query .= "         SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_REPLACE_DAT T1 ";
        $query .= "         INNER JOIN SCH_MAIN S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '1' AND ";
        $query .= "         T1.ANNUAL   = '00' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD < '91' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T1.SCHOOL_KIND_SAKI = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND SUBSTR(T1.SUBCLASSCD_SAKI,1,2) < '91' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND_SAKI, ";
            $query .= "         T1.CURRICULUM_CD_SAKI, ";
        }
        $query .= "         T1.SUBCLASSCD_SAKI ";
        $query .= " ), RECORD AS ( ";
        $query .= "     SELECT ";
        $query .= "         'REC' AS REC_DIV, ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         '1' AS RISYUTYU_FLG, ";
        $query .= "         SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         RECORD_DAT T1 ";
        $query .= "         INNER JOIN SCH_MAIN S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD < '91' ";
            $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ";
            $query .= "             NOT IN (SELECT ";
            $query .= "                         E1.CLASSCD || E1.SCHOOL_KIND || E1.CURRICULUM_CD || E1.SUBCLASSCD ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_STUDYREC_DAT E1 ";
            $query .= "                     WHERE ";
            $query .= "                         E1.SCHOOLCD IN ('0', '2') AND ";
            $query .= "                         T1.YEAR     = E1.YEAR AND ";
            $query .= "                         T1.SCHREGNO = E1.SCHREGNO ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                     AND T1.SCHOOL_KIND = E1.SCHOOL_KIND ";
            }
            $query .= "                     ) ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND SUBSTR(T1.SUBCLASSCD,1,2) < '91' ";
            $query .= "     AND T1.SUBCLASSCD ";
            $query .= "             NOT IN (SELECT ";
            $query .= "                         E1.SUBCLASSCD ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_STUDYREC_DAT E1 ";
            $query .= "                     WHERE ";
            $query .= "                         E1.SCHOOLCD IN ('0', '2') AND ";
            $query .= "                         T1.YEAR     = E1.YEAR AND ";
            $query .= "                         T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "                     ) ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         'REC2' AS REC_DIV, ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         '0' AS RISYUTYU_FLG, ";
        $query .= "         SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT T1 ";
        $query .= "         INNER JOIN SCH_MAIN S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD IN ('0', '2') AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD < '91' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND SUBSTR(T1.SUBCLASSCD,1,2) < '91' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         'STUDY' AS REC_DIV, ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         '0' AS RISYUTYU_FLG, ";
        $query .= "         SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT T1 ";
        $query .= "         INNER JOIN SCH_MAIN S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     < '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD < '91' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND SUBSTR(T1.SUBCLASSCD,1,2) < '91' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= " ), RECORD_SUB AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_DAT T1 ";
        $query .= "         INNER JOIN SCH_MAIN S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD < '91' ";
            $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ";
            $query .= "             NOT IN (SELECT ";
            $query .= "                         E1.CLASSCD || E1.SCHOOL_KIND || E1.CURRICULUM_CD || E1.SUBCLASSCD ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_STUDYREC_DAT E1 ";
            $query .= "                     WHERE ";
            $query .= "                         E1.SCHOOLCD IN ('0', '2') AND ";
            $query .= "                         T1.YEAR     = E1.YEAR AND ";
            $query .= "                         T1.SCHREGNO = E1.SCHREGNO ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                     AND T1.SCHOOL_KIND = E1.SCHOOL_KIND ";
            }
            $query .= "                     ) ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND SUBSTR(T1.SUBCLASSCD,1,2) < '91' ";
            $query .= "     AND T1.SUBCLASSCD ";
            $query .= "             NOT IN (SELECT ";
            $query .= "                         E1.SUBCLASSCD ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_STUDYREC_DAT E1 ";
            $query .= "                     WHERE ";
            $query .= "                         E1.SCHOOLCD IN ('0', '2') AND ";
            $query .= "                         T1.YEAR     = E1.YEAR AND ";
            $query .= "                         T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "                     ) ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT T1 ";
        $query .= "         LEFT JOIN SUBCLASS_MST L1 ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                  ON T1.CLASSCD          = L1.CLASSCD ";
            $query .= "                 AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
            $query .= "                 AND T1.CURRICULUM_CD    = L1.CURRICULUM_CD ";
            $query .= "                 AND T1.SUBCLASSCD       = L1.SUBCLASSCD ";
        } else {
            $query .= "                  ON T1.SUBCLASSCD       = L1.SUBCLASSCD ";
        }
        $query .= "         INNER JOIN SCH_MAIN S1 ON S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD IN ('0', '2') AND ";
        $query .= "         T1.YEAR <= '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND T1.CLASSCD < '91' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "     AND SUBSTR(T1.SUBCLASSCD,1,2) < '91' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= " ), SUBCLASS_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_SUB ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         COMP_SUBCLASS_T ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         ANOTHER_SUBCLASS_T ";
        $query .= " ), CREDIT_M AS ( ";
        $query .= "     SELECT ";
        $query .= "         S1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CREDITS ";
        $query .= "     FROM ";
        $query .= "         CREDIT_MST T1 ";
        $query .= "         INNER JOIN SCH_MAIN S1 ";
        $query .= "                  ON T1.COURSECD     = S1.COURSECD ";
        $query .= "                 AND T1.MAJORCD      = S1.MAJORCD ";
        $query .= "                 AND T1.GRADE        = S1.GRADE ";
        $query .= "                 AND T1.COURSECODE   = S1.COURSECODE ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ), SUBMAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "        T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "        T1.CLASSCD, ";
            $query .= "        T1.SCHOOL_KIND, ";
            $query .= "        T1.CURRICULUM_CD, ";
        }
        $query .= "        T1.SUBCLASSCD, ";
        $query .= "        CASE WHEN C1.SUBCLASSCD IS NOT NULL AND R1.RISYUTYU_FLG = '1' ";
        $query .= "             THEN R1.GET_CREDIT ";
        $query .= "             ELSE CASE WHEN C1.SUBCLASSCD IS NOT NULL AND R1.SUBCLASSCD IS NULL AND R3.SUBCLASSCD IS NULL ";
        $query .= "                       THEN C2.CREDITS ";
        $query .= "                       ELSE NULL ";
        $query .= "                       END ";
        $query .= "             END AS RISYUTYU_CREDIT, ";
        $query .= "        VALUE(R2.GET_CREDIT, 0) + VALUE(R3.GET_CREDIT, 0) AS RECORD_CREDITS, ";
        $query .= "        A1.GET_CREDIT AS ANOTHER_CREDITS ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_T T1 ";
        $query .= "         LEFT JOIN RECORD R1 ";
        $query .= "                  ON T1.SCHREGNO         = R1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND T1.CLASSCD          = R1.CLASSCD ";
            $query .= "                 AND T1.SCHOOL_KIND      = R1.SCHOOL_KIND ";
            $query .= "                 AND T1.CURRICULUM_CD    = R1.CURRICULUM_CD ";
        }
        $query .= "                 AND T1.SUBCLASSCD       = R1.SUBCLASSCD ";
        $query .= "                 AND R1.REC_DIV          = 'REC' ";
        $query .= "         LEFT JOIN RECORD R2 ";
        $query .= "                  ON T1.SCHREGNO         = R2.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND T1.CLASSCD          = R2.CLASSCD ";
            $query .= "                 AND T1.SCHOOL_KIND      = R2.SCHOOL_KIND ";
            $query .= "                 AND T1.CURRICULUM_CD    = R2.CURRICULUM_CD ";
        }
        $query .= "                 AND T1.SUBCLASSCD       = R2.SUBCLASSCD ";
        $query .= "                 AND R2.REC_DIV          = 'STUDY' ";
        $query .= "         LEFT JOIN RECORD R3 ";
        $query .= "                  ON T1.SCHREGNO         = R3.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND T1.CLASSCD          = R3.CLASSCD ";
            $query .= "                 AND T1.SCHOOL_KIND      = R3.SCHOOL_KIND ";
            $query .= "                 AND T1.CURRICULUM_CD    = R3.CURRICULUM_CD ";
        }
        $query .= "                 AND T1.SUBCLASSCD       = R3.SUBCLASSCD ";
        $query .= "                 AND R3.REC_DIV          = 'REC2' ";
        $query .= "         LEFT JOIN ANOTHER_SUBCLASS_T A1 ";
        $query .= "                  ON T1.SCHREGNO         = A1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND T1.CLASSCD          = A1.CLASSCD ";
            $query .= "                 AND T1.SCHOOL_KIND      = A1.SCHOOL_KIND ";
            $query .= "                 AND T1.CURRICULUM_CD    = A1.CURRICULUM_CD ";
        }
        $query .= "                 AND T1.SUBCLASSCD       = A1.SUBCLASSCD ";
        $query .= "         LEFT JOIN COMP_SUBCLASS_T C1 ";
        $query .= "                  ON T1.SCHREGNO         = C1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND T1.CLASSCD          = C1.CLASSCD ";
            $query .= "                 AND T1.SCHOOL_KIND      = C1.SCHOOL_KIND ";
            $query .= "                 AND T1.CURRICULUM_CD    = C1.CURRICULUM_CD ";
        }
        $query .= "                 AND T1.SUBCLASSCD       = C1.SUBCLASSCD ";
        $query .= "         LEFT JOIN CREDIT_M C2 ";
        $query .= "                  ON T1.SCHREGNO         = C2.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND T1.CLASSCD          = C2.CLASSCD ";
            $query .= "                 AND T1.SCHOOL_KIND      = C2.SCHOOL_KIND ";
            $query .= "                 AND T1.CURRICULUM_CD    = C2.CURRICULUM_CD ";
        }
        $query .= "                 AND T1.SUBCLASSCD       = C2.SUBCLASSCD ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(VALUE(RISYUTYU_CREDIT,0)) AS RISYUTYU_CREDITS, ";
        $query .= "         SUM(VALUE(ANOTHER_CREDITS,0)) + SUM(VALUE(RECORD_CREDITS,0)) AS GET_CREDITS, ";
        $query .= "         SUM(VALUE(RISYUTYU_CREDIT,0)) + SUM(VALUE(ANOTHER_CREDITS,0)) + SUM(VALUE(RECORD_CREDITS,0)) AS TOTAL_CREDITS ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.RISYUTYU_CREDITS, ";
        $query .= "     L1.GET_CREDITS, ";
        $query .= "     L1.TOTAL_CREDITS, ";
        $query .= "     L2.GRAD_CREDITS, ";
        $query .= "     CASE WHEN L2.GRAD_CREDITS <= L1.TOTAL_CREDITS ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "          END AS OVER_GRAD_CREDIT, ";
        $query .= "     L3.ANOTHER_MONTHS ";
        $query .= " FROM ";
        $query .= "     SCH_MAIN T1 ";
        $query .= "     LEFT JOIN MAIN L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHOOL_MST L2 ON L2.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND L2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "             AND L2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN ANOTHER_SCHOOL L3 ON T1.SCHREGNO = L3.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //生徒一覧取得
    function getSchList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CASE WHEN L1.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END AS CHECK_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L1 ";
        $query .= "              ON T1.YEAR     = L1.YEAR ";
        $query .= "             AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "             AND L1.BASE_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->grade_hr_class."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjd294Query::getSchList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //卒業予定チェック判定
            if (get_count($model->data_chk) > 0) {
                $flg = (in_array($row["SCHREGNO"], $model->data_chk)) ? "1" : "";
            } else {
                $flg = "";
            }

            //更新
            $data = array();
            if ($row["CHECK_FLG"] == 0) {
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["BASE_SEQ"][TEXT]         = '001';
            }
            $data["BASE_REMARK1"][TEXT]     = $flg;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if ($row["CHECK_FLG"] == 0) {
                $db->query(Query::insertSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST"));
            } else {
                $where  = " WHERE ";
                $where .= "     SCHREGNO    = '".$row["SCHREGNO"]."' AND ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     BASE_SEQ    = '001' ";
                $db->query(Query::updateSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST", $where));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
