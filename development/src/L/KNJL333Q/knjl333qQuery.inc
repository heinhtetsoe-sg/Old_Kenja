<?php

require_once('for_php7.php');

class knjl333qQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2    = '".$namecd2."' ";
        }
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得L004
    public function getNameMstL004($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL, ";
        $query .= "     T3.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ABBV3 AS NAMECD2 ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L004' ";
        $query .= "         GROUP BY ";
        $query .= "             ABBV3 ";
        $query .= "     ) T2 ON T2.NAMECD2 = T1.NAMECD2 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ABBV3 AS NAMECD2, ";
        $query .= "             '1' AS NAMESPARE2 ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L004' ";
        $query .= "             AND NAMESPARE2 = '1' ";
        $query .= "         GROUP BY ";
        $query .= "             ABBV3 ";
        $query .= "     ) T3 ON T3.NAMECD2 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR    = '".$year."' ";
        $query .= "     AND T1.NAMECD1 = 'L045' ";
        $query .= "     AND T1.NAMECD2 IN ('2', '3') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function sqlSagyouList($model, $printDiv, $selectDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        if ("CNT" == $selectDiv) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     B1.EXAMNO, ";
            $query .= "     N1.ABBV1 AS TESTDIV_ABBV1, ";
            $query .= "     B1.NAME, ";
            $query .= "     F1.FINSCHOOL_NAME, ";
            $query .= "     N2.NAME1 AS JUDGEMENT_NAME1, ";
            $query .= "     CASE WHEN B1.SCHOLAR_KIBOU = '1' THEN '特別' ";
            $query .= "          WHEN B1.SCHOLAR_KIBOU = '2' THEN '一般' ";
            $query .= "          ELSE '無' ";
            $query .= "     END AS SCHOLAR_KIBOU, ";
            $query .= "     CASE WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU = '1' THEN '採用' ";
            $query .= "          WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU IS NULL THEN '不採用' ";
            $query .= "          ELSE '' ";
            $query .= "     END AS SCHOLAR_SAIYOU, ";
            $query .= "     VALUE(B1.SCHOLAR_TOUKYU_SENGAN, '') AS SCHOLAR_TOUKYU, ";
            $query .= "     CASE WHEN B1.DORMITORY_FLG = '1' THEN 'レ' END AS DORMITORY_KIBOU ";
        }
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = B1.ENTEXAMYEAR AND N2.NAMECD1 = 'L013' AND N2.NAMECD2 = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND B1.TESTDIV0 = '".$model->field["TESTDIV"]."' ";
        if ($model->PRINT_PASS == $printDiv) {
            $query .= "     AND B1.JUDGEMENT = '1' ";
        }
        if ($model->PRINT_NAIPASS == $printDiv) {
            $query .= "     AND B1.JUDGEMENT = '1' ";
            $query .= "     AND F1.FINSCHOOL_PREF_CD = '19' ";
        }
        if ($model->PRINT_GAIPASS == $printDiv) {
            $query .= "     AND B1.JUDGEMENT = '1' ";
            $query .= "     AND F1.FINSCHOOL_PREF_CD <> '19' ";
        }
        if ($model->PRINT_UNPASS == $printDiv) {
            $query .= "     AND B1.JUDGEMENT = '2' ";
        }
        if ($model->PRINT_KESSEKI == $printDiv) {
            $query .= "     AND B1.JUDGEMENT = '4' ";
        }
        if ($model->PRINT_SCHOLAR == $printDiv) {
            $query .= "     AND B1.SCHOLAR_KIBOU IS NOT NULL ";
            $query .= "     AND B1.SCHOLAR_SAIYOU = '1' ";
        }
        if ($model->PRINT_UNSCHOLAR == $printDiv) {
            $query .= "     AND B1.SCHOLAR_KIBOU IS NOT NULL ";
            $query .= "     AND B1.SCHOLAR_SAIYOU IS NULL ";
        }
        if ($model->PRINT_SCHOLAR_HOPE == $printDiv) {
            $query .= "     AND B1.SCHOLAR_KIBOU IS NOT NULL ";
        }
        if ($model->PRINT_NYUURYOU == $printDiv) {
            $query .= "     AND B1.DORMITORY_FLG = '1' ";
        }
        if ("CNT" != $selectDiv) {
            $query .= " ORDER BY ";
            $query .= "     B1.EXAMNO ";
        }

        return $query;
    }

    public function sqlSchool($model, $printDiv, $selectDiv)
    {
        $query  = "";
        if ("CNT" == $selectDiv) {
            $query .= "     WITH MAIN_T AS ( ";
        }
        $query .= " SELECT ";
        $query .= "     B1.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     SUM(CASE WHEN B1.TESTDIV = '3' THEN 1 ELSE 0 END) AS KOUTYOU_CNT, ";
        $query .= "     SUM(CASE WHEN B1.TESTDIV = '4' THEN 1 ELSE 0 END) AS JIKO_CNT, ";
        $query .= "     SUM(CASE WHEN B1.TESTDIV = '5' THEN 1 ELSE 0 END) AS IPPAN_CNT ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND B1.TESTDIV0 = '".$model->field["TESTDIV"]."' ";
        $query .= " GROUP BY ";
        $query .= "     B1.FS_CD, ";
        $query .= "     F1.FINSCHOOL_NAME ";
        $query .= " ORDER BY ";
        $query .= "     B1.FS_CD ";
        if ("CNT" == $selectDiv) {
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     MAIN_T ";
        }

        return $query;
    }

    public function sqlHassouSchool($model, $printDiv, $selectDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        if ("CNT" == $selectDiv) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     B1.EXAMNO, ";
            $query .= "     N1.ABBV1 AS TESTDIV_ABBV1, ";
            $query .= "     B1.NAME, ";
            $query .= "     B1.FS_CD, ";
            $query .= "     F1.FINSCHOOL_NAME, ";
            $query .= "     N2.NAME1 AS JUDGEMENT_NAME1, ";
            $query .= "     CASE WHEN B1.SCHOLAR_KIBOU = '1' THEN '特別' ";
            $query .= "          WHEN B1.SCHOLAR_KIBOU = '2' THEN '一般' ";
            $query .= "          ELSE '無' ";
            $query .= "     END AS SCHOLAR_KIBOU, ";
            $query .= "     CASE WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU = '1' THEN '採用' ";
            $query .= "          WHEN B1.SCHOLAR_KIBOU IS NOT NULL AND B1.SCHOLAR_SAIYOU IS NULL THEN '不採用' ";
            $query .= "          ELSE '' ";
            $query .= "     END AS SCHOLAR_SAIYOU, ";
            $query .= "     VALUE(B1.SCHOLAR_TOUKYU_SENGAN, '') AS SCHOLAR_TOUKYU, ";
            $query .= "     CASE WHEN B1.DORMITORY_FLG = '1' THEN '希望' ELSE '-' END AS DORMITORY_KIBOU, ";
            $query .= "     N3.ABBV1 AS SEX_ABBV1, ";
            $query .= "     C1.EXAMCOURSE_ABBV, ";
            $query .= "     P1.PREF_NAME ";
        }
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ";
        $query .= "     LEFT JOIN PREF_MST P1 ON P1.PREF_CD = F1.FINSCHOOL_PREF_CD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = B1.ENTEXAMYEAR AND N1.NAMECD1 = 'L004' AND N1.NAMECD2 = B1.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON N2.YEAR = B1.ENTEXAMYEAR AND N2.NAMECD1 = 'L013' AND N2.NAMECD2 = B1.JUDGEMENT ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ON N3.YEAR = B1.ENTEXAMYEAR AND N3.NAMECD1 = 'Z002' AND N3.NAMECD2 = B1.SEX ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "             AND C1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "             AND C1.TESTDIV = B1.TESTDIV ";
        $query .= "             AND C1.COURSECD = B1.DAI1_COURSECD ";
        $query .= "             AND C1.MAJORCD = B1.DAI1_MAJORCD ";
        $query .= "             AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND B1.TESTDIV0 = '".$model->field["TESTDIV"]."' ";
        if ("CNT" != $selectDiv) {
            $query .= " ORDER BY ";
            if ($model->PRINT_HASSOU_SCHOOL == $printDiv) {
                $query .= "     B1.FS_CD, ";
            } else {
                $query .= "     B1.JUDGEMENT, ";
            }
            $query .= "     B1.EXAMNO ";
        }
        return $query;
    }

    public function sqlHassouSchoolHyoushi($model, $selectDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '1' AND B1.DAI1_COURSECODE LIKE '%".$model->COURSE_FUTSU."' THEN 1 ELSE 0 END) AS PASS_FUTSU, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '1' AND B1.DAI1_COURSECODE LIKE '%".$model->COURSE_SPORT."' THEN 1 ELSE 0 END) AS PASS_SPORT, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '1' THEN 1 ELSE 0 END) AS PASS, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '1' AND F1.FINSCHOOL_PREF_CD = '19' THEN 1 ELSE 0 END) AS PASS_NAI, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '1' AND F1.FINSCHOOL_PREF_CD <> '19' AND F1.FINSCHOOL_PREF_CD <= '47' THEN 1 ELSE 0 END) AS PASS_GAI, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '1' AND F1.FINSCHOOL_PREF_CD > '47' THEN 1 ELSE 0 END) AS PASS_KAIGAI, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '3' THEN 1 ELSE 0 END) AS PASS_HOKETSU, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '2' THEN 1 ELSE 0 END) AS UNPASS, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '4' THEN 1 ELSE 0 END) AS KESSEKI, ";
        $query .= "     SUM(CASE WHEN B1.JUDGEMENT = '1' AND (B1.PROCEDUREDIV = '2' OR B1.ENTDIV = '2') THEN 1 ELSE 0 END) AS PASS_JITAI ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND B1.TESTDIV0 = '".$model->field["TESTDIV"]."' ";

        return $query;
    }
}
