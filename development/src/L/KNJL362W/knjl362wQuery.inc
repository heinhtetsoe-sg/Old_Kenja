<?php

require_once('for_php7.php');

class knjl362wQuery extends Query {
    //学校一覧取得
    function getSchoolData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EDBOARD_SCHOOLCD, ";
        $query .= "     T1.EDBOARD_SCHOOLNAME, ";
        $query .= "     L1.EXECUTE_DATE, ";
        $query .= "     CASE WHEN L1.EDBOARD_SCHOOLCD IS NOT NULL ";
        $query .= "          THEN '　　　' ";
        $query .= "          ELSE '【未】' ";
        $query .= "     END AS MITEISHUTSU ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN REPORT_ENTEXAM_L340W_DAT L1 ";
        $query .= "          ON T1.EDBOARD_SCHOOLCD = L1.EDBOARD_SCHOOLCD ";
        $query .= "         AND L1.ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $query .= "         AND L1.CSV_PRG          = 2 ";
        $query .= "         AND L1.CSVDIV           = ".$model->field["CSVDIV"]." ";
        $query .= " ORDER BY ";
        $query .= "     T1.EDBOARD_SCHOOLCD ";

        return $query;
    }

    //学校情報取得
    function getSchoolInfo($model, $schoolCd) {
        $query  = " SELECT ";
        $query .= "     T1.EDBOARD_SCHOOLNAME, ";
        $query .= "     L1.NAME1 AS DIST_NAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1, ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z015' AND T2.FINSCHOOL_DISTCD2 = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = T2.FINSCHOOLCD AND ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$schoolCd."' ";

        return $query;
    }

    //課程名取得
    function getCourseName($model, $schoolCd) {
        $query  = " SELECT ";
        $query .= "     COURSENAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$schoolCd."' AND ";
        $query .= "     COURSECD IN (SELECT ";
        $query .= "                     MIN(COURSECD) AS COURSECD ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_COURSE_MST ";
        $query .= "                 WHERE ";
        $query .= "                     EDBOARD_SCHOOLCD = '".$schoolCd."' AND ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";

        return $query;
    }

    //CSVデータ
    function selectCsvQuery($model, $schoolCd, $testDiv, $testDiv2) {

        $query  = " WITH BASE_ALL(YEAR, CMCD, CMNAME) AS ( ";
        $query .= "                 VALUES('".$model->ObjYear."', '99999999', '総　合　計') ";
        $query .= " ), BASE AS ( ";
        $query .= "                 SELECT DISTINCT ";
        $query .= "                     V1.ENTEXAMYEAR AS YEAR, ";
        $query .= "                     V1.COURSECD || V1.MAJORCD || V1.EXAMCOURSECD AS CMCD, ";
        $query .= "                     V2.MAJORNAME || '・' || V1.EXAMCOURSE_NAME AS CMNAME ";
        $query .= "                 FROM ";
        $query .= "                     EDBOARD_ENTEXAM_COURSE_MST V1 ";
        $query .= "                     INNER JOIN V_EDBOARD_COURSE_MAJOR_MST V2 ";
        $query .= "                          ON V1.EDBOARD_SCHOOLCD = V2.EDBOARD_SCHOOLCD ";
        $query .= "                         AND V1.ENTEXAMYEAR = V2.YEAR ";
        $query .= "                         AND V1.COURSECD || V1.MAJORCD = V2.COURSECD || V2.MAJORCD ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND V1.TESTDIV    IN ('{$testDiv}') ";
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     BASE_ALL ";
        $query .= "                 ORDER BY ";
        $query .= "                     CMCD ";
        $query .= " ), DAI1_INT AS ( ";//試験受験者数
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE AS DAI1_CMCD, ";
        $query .= "                     COUNT(*) AS DAI1_CM_INT ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE ";
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     '99999999' AS DAI1_CMCD, ";
        $query .= "                     COUNT(*) AS DAI1_CM_INT ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR ";
        $query .= " ), SUC_INT AS ( ";//試験合格者数
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE AS SUC_CMCD, ";
        $query .= "                     COUNT(*) AS SUC_CM_INT ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND V1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE ";
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     '99999999' AS SUC_CMCD, ";
        $query .= "                     COUNT(*) AS SUC_CM_INT ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND V1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR ";
        $query .= " ), SUB_99 AS ( ";
        $query .= "                  SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE AS CMCD, ";
        $query .= "                     SUM(E1.SCORE) AS TOTAL99 ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ";
        $query .= "                          ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ";
        $query .= "                         AND V1.ENTEXAMYEAR      = E1.ENTEXAMYEAR ";
        $query .= "                         AND V1.TESTDIV          = E1.TESTDIV ";
        $query .= "                         AND V1.EXAMNO           = E1.RECEPTNO ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                     AND E1.TESTSUBCLASSCD   IN ('1', '2', '3', '4', '5') ";
        } else {
            $query .= "                     AND E1.TESTSUBCLASSCD   IN ('1', '3', '5') ";
        }
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE, ";
        $query .= "                     V1.EXAMNO  ";
        $query .= " ), SUC_SUB_99 AS ( ";
        $query .= "                  SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE AS CMCD, ";
        $query .= "                     SUM(E1.SCORE) AS SUC_TOTAL99 ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ";
        $query .= "                          ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ";
        $query .= "                         AND V1.ENTEXAMYEAR      = E1.ENTEXAMYEAR ";
        $query .= "                         AND V1.TESTDIV          = E1.TESTDIV ";
        $query .= "                         AND V1.EXAMNO           = E1.RECEPTNO ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND V1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                     AND E1.TESTSUBCLASSCD   IN ('1', '2', '3', '4', '5') ";
        } else {
            $query .= "                     AND E1.TESTSUBCLASSCD   IN ('1', '3', '5') ";
        }
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE, ";
        $query .= "                     V1.EXAMNO  ";
        $query .= " ), DAI1_DATA AS ( ";//全教科（受験者総得点、総平均）
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.CMCD AS DAI1_CMCD, ";
        $query .= "                     SUM(V1.TOTAL99) AS DAI1_TOTAL_3, ";
        $query .= "                     DECIMAL(ROUND(AVG(V1.TOTAL99 * 1.0),1),4,1) AS DAI1_AVG_3 ";
        $query .= "                 FROM ";
        $query .= "                     SUB_99 V1 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.CMCD ";
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     '99999999' AS DAI1_CMCD, ";
        $query .= "                     SUM(V1.TOTAL99) AS DAI1_TOTAL_3, ";
        $query .= "                     DECIMAL(ROUND(AVG(V1.TOTAL99 * 1.0),1),4,1) AS DAI1_AVG_3 ";
        $query .= "                 FROM ";
        $query .= "                     SUB_99 V1 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR ";
        $query .= " ), SUC_DATA AS ( ";//全教科（合格者総得点、総平均）
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.CMCD AS SUC_CMCD, ";
        $query .= "                     SUM(V1.SUC_TOTAL99) AS SUC_TOTAL_3, ";
        $query .= "                     DECIMAL(ROUND(AVG(V1.SUC_TOTAL99 * 1.0),1),4,1) AS SUC_AVG_3 ";
        $query .= "                 FROM ";
        $query .= "                     SUC_SUB_99 V1 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.CMCD ";
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     '99999999' AS SUC_CMCD, ";
        $query .= "                     SUM(V1.SUC_TOTAL99) AS SUC_TOTAL_3, ";
        $query .= "                     DECIMAL(ROUND(AVG(V1.SUC_TOTAL99 * 1.0),1),4,1) AS SUC_AVG_3 ";
        $query .= "                 FROM ";
        $query .= "                     SUC_SUB_99 V1 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR ";
        $query .= " ), DAI1_SUB_DATA AS ( ";//各教科（受験者総得点、総平均）
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE AS DAI1_CMCD, ";
        $query .= "                     E1.TESTSUBCLASSCD, ";
        $query .= "                     CASE  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '1' THEN SUM(E1.SCORE) ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '3' THEN SUM(E1.SCORE) ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '5' THEN SUM(E1.SCORE) ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '2' THEN SUM(E1.SCORE) ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '4' THEN SUM(E1.SCORE) ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '6' THEN SUM(E1.SCORE) ";
        }
        $query .= "                     END AS DAI1_TOTAL_SUB, ";
        $query .= "                     CASE  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '1' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '3' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '5' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '2' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '4' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '6' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        }
        $query .= "                     END AS DAI1_AVG_SUB ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ";
        $query .= "                          ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ";
        $query .= "                         AND V1.ENTEXAMYEAR      = E1.ENTEXAMYEAR ";
        $query .= "                         AND V1.TESTDIV          = E1.TESTDIV ";
        $query .= "                         AND V1.EXAMNO           = E1.RECEPTNO ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.DAI1_COURSECD || V1.DAI1_MAJORCD || V1.DAI1_COURSECODE, ";
        $query .= "                     E1.TESTSUBCLASSCD ";
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     '99999999' AS DAI1_CMCD, ";
        $query .= "                     E1.TESTSUBCLASSCD, ";
        $query .= "                     CASE  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '1' THEN SUM(E1.SCORE) ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '3' THEN SUM(E1.SCORE) ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '5' THEN SUM(E1.SCORE) ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '2' THEN SUM(E1.SCORE) ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '4' THEN SUM(E1.SCORE) ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '6' THEN SUM(E1.SCORE) ";
        }
        $query .= "                     END AS DAI1_TOTAL_SUB, ";
        $query .= "                     CASE  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '1' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '3' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '5' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '2' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '4' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '6' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        }
        $query .= "                     END AS DAI1_AVG_SUB ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ";
        $query .= "                          ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ";
        $query .= "                         AND V1.ENTEXAMYEAR      = E1.ENTEXAMYEAR ";
        $query .= "                         AND V1.TESTDIV          = E1.TESTDIV ";
        $query .= "                         AND V1.EXAMNO           = E1.RECEPTNO ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND VALUE(V1.JUDGEMENT, '') NOT IN ('4', '5') ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     E1.TESTSUBCLASSCD ";
        $query .= " ), SUC_SUB_DATA AS ( ";//各教科（合格者総得点、総平均）
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE AS SUC_CMCD, ";
        $query .= "                     E1.TESTSUBCLASSCD, ";
        $query .= "                     CASE  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '1' THEN SUM(E1.SCORE) ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '3' THEN SUM(E1.SCORE) ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '5' THEN SUM(E1.SCORE) ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '2' THEN SUM(E1.SCORE) ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '4' THEN SUM(E1.SCORE) ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '6' THEN SUM(E1.SCORE) ";
        }
        $query .= "                     END AS SUC_TOTAL_SUB, ";
        $query .= "                     CASE  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '1' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '3' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '5' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '2' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '4' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '6' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        }
        $query .= "                     END AS SUC_AVG_SUB ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ";
        $query .= "                          ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ";
        $query .= "                         AND V1.ENTEXAMYEAR      = E1.ENTEXAMYEAR ";
        $query .= "                         AND V1.TESTDIV          = E1.TESTDIV ";
        $query .= "                         AND V1.EXAMNO           = E1.RECEPTNO ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND V1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     V1.SUC_COURSECD || V1.SUC_MAJORCD || V1.SUC_COURSECODE, ";
        $query .= "                     E1.TESTSUBCLASSCD ";
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     '99999999' AS SUC_CMCD, ";
        $query .= "                     E1.TESTSUBCLASSCD, ";
        $query .= "                     CASE  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '1' THEN SUM(E1.SCORE) ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '3' THEN SUM(E1.SCORE) ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '5' THEN SUM(E1.SCORE) ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '2' THEN SUM(E1.SCORE) ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '4' THEN SUM(E1.SCORE) ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '6' THEN SUM(E1.SCORE) ";
        }
        $query .= "                     END AS SUC_TOTAL_SUB, ";
        $query .= "                     CASE  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '1' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '3' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        $query .= "                         WHEN E1.TESTSUBCLASSCD = '5' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '2' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '4' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
            $query .= "                         WHEN E1.TESTSUBCLASSCD = '6' THEN DECIMAL(ROUND(AVG(E1.SCORE * 1.0),1),4,1)  ";
        }
        $query .= "                     END AS SUC_AVG_SUB ";
        $query .= "                 FROM ";
        $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT V1 ";
        $query .= "                     JOIN EDBOARD_ENTEXAM_SCORE_DAT E1 ";
        $query .= "                          ON V1.EDBOARD_SCHOOLCD = E1.EDBOARD_SCHOOLCD ";
        $query .= "                         AND V1.ENTEXAMYEAR      = E1.ENTEXAMYEAR ";
        $query .= "                         AND V1.TESTDIV          = E1.TESTDIV ";
        $query .= "                         AND V1.EXAMNO           = E1.RECEPTNO ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND V1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         V1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND V1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND V1.TESTDIV     IN ('{$testDiv}') ";
        $query .= "                     AND VALUE(V1.TESTDIV2, '') = '{$testDiv2}' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     V1.ENTEXAMYEAR, ";
        $query .= "                     E1.TESTSUBCLASSCD ";
        $query .= " ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     B1.CMCD, ";
        $query .= "     B1.CMNAME, ";
        $query .= "     DINT.DAI1_CM_INT, ";
        $query .= "     SINT.SUC_CM_INT, ";
        $query .= "     DD.DAI1_TOTAL_3, ";
        $query .= "     RTRIM(CHAR(DD.DAI1_AVG_3)) AS DAI1_AVG_3, ";
        $query .= "     SD.SUC_TOTAL_3, ";
        $query .= "     SD.SUC_AVG_3, ";
        $query .= "     DSD1.DAI1_TOTAL_SUB AS JAP_D1_TOTAL, ";
        $query .= "     DSD1.DAI1_AVG_SUB   AS JAP_D1_AVG, ";
        $query .= "     SSD1.SUC_TOTAL_SUB  AS JAP_SUC_TOTAL, ";
        $query .= "     SSD1.SUC_AVG_SUB    AS JAP_SUC_AVG, ";
        $query .= "     DSD3.DAI1_TOTAL_SUB AS MATH_D1_TOTAL, ";
        $query .= "     DSD3.DAI1_AVG_SUB   AS MATH_D1_AVG, ";
        $query .= "     SSD3.SUC_TOTAL_SUB  AS MATH_SUC_TOTAL, ";
        $query .= "     SSD3.SUC_AVG_SUB    AS MATH_SUC_AVG, ";
        $query .= "     DSD5.DAI1_TOTAL_SUB AS ENG_D1_TOTAL, ";
        $query .= "     DSD5.DAI1_AVG_SUB   AS ENG_D1_AVG, ";
        $query .= "     SSD5.SUC_TOTAL_SUB  AS ENG_SUC_TOTAL, ";
        $query .= "     SSD5.SUC_AVG_SUB    AS ENG_SUC_AVG ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "    ,DSD2.DAI1_TOTAL_SUB AS SOC_D1_TOTAL, ";
            $query .= "     DSD2.DAI1_AVG_SUB   AS SOC_D1_AVG, ";
            $query .= "     SSD2.SUC_TOTAL_SUB  AS SOC_SUC_TOTAL, ";
            $query .= "     SSD2.SUC_AVG_SUB    AS SOC_SUC_AVG, ";
            $query .= "     DSD4.DAI1_TOTAL_SUB AS SCI_D1_TOTAL, ";
            $query .= "     DSD4.DAI1_AVG_SUB   AS SCI_D1_AVG, ";
            $query .= "     SSD4.SUC_TOTAL_SUB  AS SCI_SUC_TOTAL, ";
            $query .= "     SSD4.SUC_AVG_SUB    AS SCI_SUC_AVG, ";
            $query .= "     DSD6.DAI1_TOTAL_SUB AS PRA_D1_TOTAL, ";
            $query .= "     DSD6.DAI1_AVG_SUB   AS PRA_D1_AVG, ";
            $query .= "     SSD6.SUC_TOTAL_SUB  AS PRA_SUC_TOTAL, ";
            $query .= "     SSD6.SUC_AVG_SUB    AS PRA_SUC_AVG ";
        }
        $query .= " FROM ";
        $query .= "     BASE B1 ";
        $query .= "     LEFT JOIN DAI1_INT DINT ON B1.YEAR = DINT.ENTEXAMYEAR ";
        $query .= "                            AND B1.CMCD = DINT.DAI1_CMCD ";
        $query .= "     LEFT JOIN SUC_INT SINT  ON B1.YEAR = SINT.ENTEXAMYEAR ";
        $query .= "                            AND B1.CMCD = SINT.SUC_CMCD ";
        $query .= "     LEFT JOIN DAI1_DATA DD  ON B1.YEAR = DD.ENTEXAMYEAR ";
        $query .= "                            AND B1.CMCD = DD.DAI1_CMCD ";
        $query .= "     LEFT JOIN SUC_DATA SD   ON B1.YEAR = SD.ENTEXAMYEAR ";
        $query .= "                            AND B1.CMCD = SD.SUC_CMCD ";
        $query .= "     LEFT JOIN DAI1_SUB_DATA DSD1 ON B1.YEAR = DSD1.ENTEXAMYEAR ";
        $query .= "                                 AND B1.CMCD = DSD1.DAI1_CMCD ";
        $query .= "                                 AND DSD1.TESTSUBCLASSCD = '1' ";
        $query .= "     LEFT JOIN DAI1_SUB_DATA DSD3 ON B1.YEAR = DSD3.ENTEXAMYEAR ";
        $query .= "                                 AND B1.CMCD = DSD3.DAI1_CMCD ";
        $query .= "                                 AND DSD3.TESTSUBCLASSCD = '3' ";
        $query .= "     LEFT JOIN DAI1_SUB_DATA DSD5 ON B1.YEAR = DSD5.ENTEXAMYEAR ";
        $query .= "                                 AND B1.CMCD = DSD5.DAI1_CMCD ";
        $query .= "                                 AND DSD5.TESTSUBCLASSCD = '5' ";
        $query .= "     LEFT JOIN SUC_SUB_DATA SSD1 ON B1.YEAR = SSD1.ENTEXAMYEAR ";
        $query .= "                                AND B1.CMCD = SSD1.SUC_CMCD ";
        $query .= "                                AND SSD1.TESTSUBCLASSCD = '1' ";
        $query .= "     LEFT JOIN SUC_SUB_DATA SSD3 ON B1.YEAR = SSD3.ENTEXAMYEAR ";
        $query .= "                                AND B1.CMCD = SSD3.SUC_CMCD ";
        $query .= "                                AND SSD3.TESTSUBCLASSCD = '3' ";
        $query .= "     LEFT JOIN SUC_SUB_DATA SSD5 ON B1.YEAR = SSD5.ENTEXAMYEAR ";
        $query .= "                                AND B1.CMCD = SSD5.SUC_CMCD ";
        $query .= "                                AND SSD5.TESTSUBCLASSCD = '5' ";
        if ($model->field["CSVDIV"] == "2") {
            $query .= "     LEFT JOIN DAI1_SUB_DATA DSD2 ON B1.YEAR = DSD2.ENTEXAMYEAR ";
            $query .= "                                 AND B1.CMCD = DSD2.DAI1_CMCD ";
            $query .= "                                 AND DSD2.TESTSUBCLASSCD = '2' ";
            $query .= "     LEFT JOIN DAI1_SUB_DATA DSD4 ON B1.YEAR = DSD4.ENTEXAMYEAR ";
            $query .= "                                 AND B1.CMCD = DSD4.DAI1_CMCD ";
            $query .= "                                 AND DSD4.TESTSUBCLASSCD = '4' ";
            $query .= "     LEFT JOIN DAI1_SUB_DATA DSD6 ON B1.YEAR = DSD6.ENTEXAMYEAR ";
            $query .= "                                 AND B1.CMCD = DSD6.DAI1_CMCD ";
            $query .= "                                 AND DSD6.TESTSUBCLASSCD = '6' ";
            $query .= "     LEFT JOIN SUC_SUB_DATA SSD2 ON B1.YEAR = SSD2.ENTEXAMYEAR ";
            $query .= "                                AND B1.CMCD = SSD2.SUC_CMCD ";
            $query .= "                                AND SSD2.TESTSUBCLASSCD = '2' ";
            $query .= "     LEFT JOIN SUC_SUB_DATA SSD4 ON B1.YEAR = SSD4.ENTEXAMYEAR ";
            $query .= "                                AND B1.CMCD = SSD4.SUC_CMCD ";
            $query .= "                                AND SSD4.TESTSUBCLASSCD = '4' ";
            $query .= "     LEFT JOIN SUC_SUB_DATA SSD6 ON B1.YEAR = SSD6.ENTEXAMYEAR ";
            $query .= "                                AND B1.CMCD = SSD6.SUC_CMCD ";
            $query .= "                                AND SSD6.TESTSUBCLASSCD = '6' ";
        }
        $query .= " WHERE  ";
        $query .= "     B1.YEAR = '".$model->ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     B1.CMCD ";

        return $query;
    }

    //DELETE
    function &getDeleteReportFixed($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
        foreach ($selectdata as $key => $schoolCd) {
            //県側報告テーブル
            $query  = " DELETE FROM ";
            $query .= "     REPORT_ENTEXAM_L340W_DAT ";
            $query .= " WHERE ";
            $query .= "     EDBOARD_SCHOOLCD    = '".$schoolCd."' AND ";
            $query .= "     ENTEXAMYEAR         = '".$model->ObjYear."' AND ";
            $query .= "     CSV_PRG             = 2 AND ";
            $query .= "     CSVDIV              = ".$model->field["CSVDIV"]." ";

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
