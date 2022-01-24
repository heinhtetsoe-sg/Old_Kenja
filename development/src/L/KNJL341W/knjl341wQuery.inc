<?php

require_once('for_php7.php');

class knjl341wQuery extends Query {
    //学校情報取得
    function getSchoolInfo($model) {
        $query  = " SELECT ";
        $query .= "     T1.KYOUIKU_IINKAI_SCHOOLCD, ";
        $query .= "     T2.FINSCHOOL_NAME, ";
        $query .= "     L1.NAME1 AS DIST_NAME ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST T1, ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z015' AND T2.FINSCHOOL_DISTCD2 = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.KYOUIKU_IINKAI_SCHOOLCD = T2.FINSCHOOLCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //課程名取得
    function getCourseName($model) {
        $query  = " SELECT ";
        $query .= "     COURSENAME ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD IN (SELECT ";
        $query .= "                     MIN(COURSECD) AS COURSECD ";
        $query .= "                 FROM ";
        $query .= "                     V_COURSE_MST ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";

        return $query;
    }

    //受検学科コース取得
    function getCouseMajor($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE AS CM_CD, ";
        $query .= "     V1.MAJORNAME || '・' || V2.COURSECODENAME AS CM_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST V1 ON B1.ENTEXAMYEAR  = V1.YEAR ";
        $query .= "                                    AND B1.SUC_COURSECD = V1.COURSECD ";
        $query .= "                                    AND B1.SUC_MAJORCD  = V1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST V2 ON B1.ENTEXAMYEAR    = V2.YEAR ";
        $query .= "                                  AND B1.SUC_COURSECODE = V2.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     CM_CD ";

        return $query;
    }

    //CSV（三重県DATA）
    function selectCsvQuery($model) {
        $query  = " WITH BASEDATA AS ( ";
        $query .= "                     SELECT ";
        $query .= "                         B1.FS_CD ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                         INNER JOIN NAME_MST N1 ";
        $query .= "                                              ON N1.NAMECD1   = 'L013' ";
        $query .= "                                             AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                     WHERE ";
        $query .= "                             B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                         AND B1.JUDGEMENT   != '5' ";
        $query .= "                         AND B1.ENTDIV       = '1' ";
        $query .= "                         AND P1.PREF_CD      = '24' ";
        $query .= "                         AND N1.NAMESPARE1   = '1' ";
        $query .= "                     GROUP BY ";
        $query .= "                         B1.FS_CD ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= " ), FINSCHCNT{$cnt} AS ( ";
            $query .= "                     SELECT ";
            $query .= "                         B1.FS_CD, ";
            $query .= "                         COUNT(*) AS ENTCNT{$cnt} ";
            $query .= "                     FROM ";
            $query .= "                         ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                               AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                     WHERE ";
            $query .= "                             B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                         AND B1.JUDGEMENT   != '5' ";
            $query .= "                         AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '".$cmVal."' ";
            $query .= "                         AND B1.ENTDIV       = '1' ";
            $query .= "                         AND P1.PREF_CD      = '24' ";
            $query .= "                         AND N1.NAMESPARE1   = '1' ";
            $query .= "                     GROUP BY ";
            $query .= "                         B1.FS_CD ";
            $query .= " ), KANENDOCNT{$cnt} AS ( ";
            $query .= "                     SELECT ";
            $query .= "                         B1.FS_CD, ";
            $query .= "                         COUNT(*) AS GRDDIVCNT{$cnt} ";
            $query .= "                     FROM ";
            $query .= "                         ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                               AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                     WHERE ";
            $query .= "                             B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                         AND B1.JUDGEMENT   != '5' ";
            $query .= "                         AND B1.ENTDIV       = '1' ";
            $query .= "                         AND B1.FS_GRDDIV    = '2' ";
            $query .= "                         AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '".$cmVal."' ";
            $query .= "                         AND P1.PREF_CD      = '24' ";
            $query .= "                         AND N1.NAMESPARE1   = '1' ";
            $query .= "                     GROUP BY ";
            $query .= "                         B1.FS_CD ";
            $cnt++;
        }
        $query .= " ), ALLFINSCHCNT AS ( ";
        $query .= "                     SELECT ";
        $query .= "                         B1.FS_CD, ";
        $query .= "                         COUNT(*) AS ALLENTCNT ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                               AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                     WHERE ";
        $query .= "                             B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                         AND B1.JUDGEMENT   != '5' ";
        $query .= "                         AND B1.ENTDIV       = '1' ";
        $query .= "                         AND P1.PREF_CD      = '24' ";
        $query .= "                         AND N1.NAMESPARE1   = '1' ";
        $query .= "                     GROUP BY ";
        $query .= "                         B1.FS_CD ";
        $query .= " ), ALLKANENDOCNT AS ( ";
        $query .= "                     SELECT ";
        $query .= "                         B1.FS_CD, ";
        $query .= "                         COUNT(*) AS ALLGRDDIVCNT ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                               AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                     WHERE ";
        $query .= "                             B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                         AND B1.JUDGEMENT   != '5' ";
        $query .= "                         AND B1.ENTDIV       = '1' ";
        $query .= "                         AND B1.FS_GRDDIV    = '2' ";
        $query .= "                         AND P1.PREF_CD      = '24' ";
        $query .= "                         AND N1.NAMESPARE1   = '1' ";
        $query .= "                     GROUP BY ";
        $query .= "                         B1.FS_CD ";
        $query .= " ) ";
        $query .= " SELECT  ";
        $query .= "     Z003.NAME1 AS CITYNAME, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= "     FCNT{$cnt}.ENTCNT{$cnt}, ";
            $query .= "     KCNT{$cnt}.GRDDIVCNT{$cnt}, ";
            $cnt++;
        }
        $query .= "     ALLFC.ALLENTCNT, ";
        $query .= "     ALLKC.ALLGRDDIVCNT ";
        $query .= " FROM ";
        $query .= "     BASEDATA B1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ";
        $query .= "                          ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST Z003 ";
        $query .= "                          ON Z003.NAMECD1  = 'Z003' ";
        $query .= "                         AND F1.DISTRICTCD = Z003.NAMECD2 ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= "     LEFT JOIN FINSCHCNT{$cnt} FCNT{$cnt} ";
            $query .= "                          ON B1.FS_CD = FCNT{$cnt}.FS_CD ";
            $query .= "     LEFT JOIN KANENDOCNT{$cnt} KCNT{$cnt} ";
            $query .= "                          ON B1.FS_CD = KCNT{$cnt}.FS_CD ";
            $cnt++;
        }
        $query .= "     LEFT JOIN ALLFINSCHCNT ALLFC ";
        $query .= "                          ON B1.FS_CD = ALLFC.FS_CD ";
        $query .= "     LEFT JOIN ALLKANENDOCNT ALLKC ";
        $query .= "                          ON B1.FS_CD = ALLKC.FS_CD ";
        $query .= " ORDER BY ";
        $query .= "     B1.FS_CD ";

        return $query;
    }

    //CSV（三重県以外DATA）
    function selectCsvQuery2($model) {
        $query  = " WITH  B_ALL(PREF_CD, PREF_NAME) AS ( ";
        $query .= "             VALUES('99', '県外合計') ";
        $query .= " ), BASEDATA AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     P1.PREF_CD, ";
        $query .= "                     P1.PREF_NAME ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD  ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT   != '5' ";
        $query .= "                     AND B1.ENTDIV       = '1' ";
        $query .= "                     AND P1.PREF_CD     != '24' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     P1.PREF_CD, ";
        $query .= "                     P1.PREF_NAME ";
        $query .= "                 UNION ";
        $query .= "                 SELECT ";
        $query .= "                     PREF_CD, ";
        $query .= "                     PREF_NAME ";
        $query .= "                 FROM ";
        $query .= "                     B_ALL ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= " ), NOTMIECNT{$cnt} AS ( ";
            $query .= "                 SELECT ";
            $query .= "                     P1.PREF_CD, ";
            $query .= "                     COUNT(*) AS NOTMCNT ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                 WHERE ";
            $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                     AND B1.JUDGEMENT   != '5' ";
            $query .= "                     AND B1.ENTDIV       = '1' ";
            $query .= "                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '".$cmVal."' ";
            $query .= "                     AND P1.PREF_CD     != '24' ";
            $query .= "                     AND N1.NAMESPARE1   = '1' ";
            $query .= "                 GROUP BY ";
            $query .= "                     P1.PREF_CD ";
            $query .= " ), NMKANENCNT{$cnt} AS ( ";
            $query .= "                 SELECT ";
            $query .= "                     P1.PREF_CD, ";
            $query .= "                     COUNT(*) AS KANENDOCNT ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                 WHERE ";
            $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                     AND B1.JUDGEMENT   != '5' ";
            $query .= "                     AND B1.FS_GRDDIV    = '2' ";
            $query .= "                     AND B1.ENTDIV       = '1' ";
            $query .= "                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '".$cmVal."' ";
            $query .= "                     AND P1.PREF_CD     != '24' ";
            $query .= "                     AND N1.NAMESPARE1   = '1' ";
            $query .= "                 GROUP BY ";
            $query .= "                     P1.PREF_CD ";
            $query .= " ), CM_NOTMIE_ALL{$cnt} AS ( ";
            $query .= "                 SELECT ";
            $query .= "                     '99' AS PREF_CD, ";
            $query .= "                     COUNT(*) AS CM_NM_ALL ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                 WHERE ";
            $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                     AND B1.JUDGEMENT   != '5' ";
            $query .= "                     AND B1.ENTDIV       = '1' ";
            $query .= "                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '".$cmVal."' ";
            $query .= "                     AND P1.PREF_CD     != '24' ";
            $query .= "                     AND N1.NAMESPARE1   = '1' ";
            $query .= " ), CM_KNOTMIE_ALL{$cnt} AS ( ";//県外合計過年度（学科・コース別）
            $query .= "                 SELECT ";
            $query .= "                     '99' AS PREF_CD, ";
            $query .= "                     COUNT(*) AS CM_KNM_ALL ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                 WHERE ";
            $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                     AND B1.JUDGEMENT   != '5' ";
            $query .= "                     AND B1.ENTDIV       = '1' ";
            $query .= "                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '".$cmVal."' ";
            $query .= "                     AND B1.FS_GRDDIV    = '2' ";
            $query .= "                     AND P1.PREF_CD     != '24' ";
            $query .= "                     AND N1.NAMESPARE1   = '1' ";
            $cnt++;
        }
        $query .= " ), PR_NOTMIE_ALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     P1.PREF_CD, ";
        $query .= "                     COUNT(*) AS P_CNT_ALL ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT   != '5' ";
        $query .= "                     AND B1.ENTDIV       = '1' ";
        $query .= "                     AND P1.PREF_CD     != '24' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     P1.PREF_CD ";
        $query .= " ), PR_KNOTMIE_ALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     P1.PREF_CD, ";
        $query .= "                     COUNT(*) AS P_KCNT_ALL ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT   != '5' ";
        $query .= "                     AND B1.ENTDIV       = '1' ";
        $query .= "                     AND B1.FS_GRDDIV    = '2' ";
        $query .= "                     AND P1.PREF_CD     != '24' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     P1.PREF_CD ";
        $query .= " ), NOTMIECNTALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     '99' AS PREF_CD, ";
        $query .= "                     COUNT(*) AS NOTMCNTALL ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT   != '5' ";
        $query .= "                     AND B1.ENTDIV       = '1' ";
        $query .= "                     AND P1.PREF_CD     != '24' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= " ), NMKANENCNTALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     '99' AS PREF_CD, ";
        $query .= "                     COUNT(*) AS KANENDOALL ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT   != '5' ";
        $query .= "                     AND B1.ENTDIV       = '1' ";
        $query .= "                     AND B1.FS_GRDDIV    = '2' ";
        $query .= "                     AND P1.PREF_CD     != '24' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.PREF_NAME, ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= "     CASE WHEN B1.PREF_CD = '99' THEN CMNA{$cnt}.CM_NM_ALL ELSE NMC{$cnt}.NOTMCNT END, ";
            $query .= "     CASE WHEN B1.PREF_CD = '99' THEN CMKA{$cnt}.CM_KNM_ALL ELSE KANEN{$cnt}.KANENDOCNT END, ";
            $cnt++;
        }
        $query .= "     CASE WHEN B1.PREF_CD = '99' THEN NMCA.NOTMCNTALL ELSE P_CA.P_CNT_ALL END, ";
        $query .= "     CASE WHEN B1.PREF_CD = '99' THEN KALL.KANENDOALL ELSE P_KCA.P_KCNT_ALL END ";
        $query .= " FROM ";
        $query .= "     BASEDATA B1 ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= "     LEFT JOIN NOTMIECNT{$cnt}       NMC{$cnt} ON B1.PREF_CD = NMC{$cnt}.PREF_CD ";
            $query .= "     LEFT JOIN NMKANENCNT{$cnt}    KANEN{$cnt} ON B1.PREF_CD = KANEN{$cnt}.PREF_CD ";
            $query .= "     LEFT JOIN CM_NOTMIE_ALL{$cnt}  CMNA{$cnt} ON B1.PREF_CD = CMNA{$cnt}.PREF_CD ";
            $query .= "     LEFT JOIN CM_KNOTMIE_ALL{$cnt} CMKA{$cnt} ON B1.PREF_CD = CMKA{$cnt}.PREF_CD ";
            $cnt++;
        }
        $query .= "     LEFT JOIN NOTMIECNTALL  NMCA ON B1.PREF_CD = NMCA.PREF_CD ";
        $query .= "     LEFT JOIN NMKANENCNTALL KALL ON B1.PREF_CD = KALL.PREF_CD ";
        $query .= "     LEFT JOIN PR_NOTMIE_ALL  P_CA  ON B1.PREF_CD = P_CA.PREF_CD ";
        $query .= "     LEFT JOIN PR_KNOTMIE_ALL P_KCA ON B1.PREF_CD = P_KCA.PREF_CD ";
        $query .= " ORDER BY ";
        $query .= "     B1.PREF_CD ";

        return $query;
    }

    //CSV（合計DATA）
    function selectCsvQuery3($model) {
        $query  = " WITH  BASE_ALL(ENTEXAMYEAR) AS ( ";
        $query .= "             VALUES('".$model->ObjYear."') ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= " ), CM_ALL{$cnt} AS ( ";
            $query .= "                 SELECT ";
            $query .= "                     B1.ENTEXAMYEAR, ";
            $query .= "                     COUNT(*) AS CM_ALLCNT ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                 WHERE ";
            $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                     AND B1.JUDGEMENT   != '5' ";
            $query .= "                     AND B1.ENTDIV       = '1' ";
            $query .= "                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '".$cmVal."' ";
            $query .= "                     AND N1.NAMESPARE1   = '1' ";
            $query .= "                 GROUP BY ";
            $query .= "                     B1.ENTEXAMYEAR ";
            $query .= " ), CM_KANEN_ALL{$cnt} AS ( ";
            $query .= "                 SELECT ";
            $query .= "                     B1.ENTEXAMYEAR, ";
            $query .= "                     COUNT(*) AS CM_KANEN_CNT ";
            $query .= "                 FROM ";
            $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                 WHERE ";
            $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                     AND B1.JUDGEMENT   != '5' ";
            $query .= "                     AND B1.ENTDIV       = '1' ";
            $query .= "                     AND B1.FS_GRDDIV    = '2' ";
            $query .= "                     AND B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE = '".$cmVal."' ";
            $query .= "                     AND N1.NAMESPARE1   = '1' ";
            $query .= "                 GROUP BY ";
            $query .= "                     B1.ENTEXAMYEAR ";
            $cnt++;
        }
        $query .= " ), ENT_ALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     B1.ENTEXAMYEAR, ";
        $query .= "                     COUNT(*) AS ENT_ALL_CNT ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT   != '5' ";
        $query .= "                     AND B1.ENTDIV       = '1' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     B1.ENTEXAMYEAR ";
        $query .= " ), ENT_KANEN_ALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     B1.ENTEXAMYEAR, ";
        $query .= "                     COUNT(*) AS ENT_K_ALL_CNT ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT   != '5' ";
        $query .= "                     AND B1.ENTDIV       = '1' ";
        $query .= "                     AND B1.FS_GRDDIV    = '2' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= "                 GROUP BY ";
        $query .= "                     B1.ENTEXAMYEAR ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= " ), JCM_ALL{$cnt} AS ( ";
            $query .= "                 SELECT ";
            $query .= "                     B1.ENTEXAMYEAR, ";
            $query .= "                     COUNT(*) AS JCM_ALLCNT ";
            $query .= "                 FROM ";
            $query .= "                     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                 WHERE ";
            $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                     AND B1.JUDGEMENT    = '2' ";
            $query .= "                     AND B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE = '".$cmVal."' ";
            $query .= "                     AND B1.ENTDIV       = '2' ";
            $query .= "                 GROUP BY ";
            $query .= "                     B1.ENTEXAMYEAR ";
            $query .= " ), JCM_KANEN_ALL{$cnt} AS ( ";
            $query .= "                 SELECT ";
            $query .= "                     B1.ENTEXAMYEAR, ";
            $query .= "                     COUNT(*) AS JCM_KANEN_CNT ";
            $query .= "                 FROM ";
            $query .= "                     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                 WHERE ";
            $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                     AND B1.JUDGEMENT    = '2' ";
            $query .= "                     AND B1.FS_GRDDIV    = '2' ";
            $query .= "                     AND B1.DAI1_COURSECD || B1.DAI1_MAJORCD || B1.DAI1_COURSECODE = '".$cmVal."' ";
            $query .= "                     AND B1.ENTDIV       = '2' ";
            $query .= "                 GROUP BY ";
            $query .= "                     B1.ENTEXAMYEAR ";
            $cnt++;
        }
        $query .= " ), JENT_ALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     B1.ENTEXAMYEAR, ";
        $query .= "                     COUNT(*) AS JENT_ALL_CNT ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT    = '2' ";
        $query .= "                     AND B1.ENTDIV       = '2' ";
        $query .= "                 GROUP BY ";
        $query .= "                     B1.ENTEXAMYEAR ";
        $query .= " ), JENT_KANEN_ALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     B1.ENTEXAMYEAR, ";
        $query .= "                     COUNT(*) AS JENT_K_ALL_CNT ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT    = '2' ";
        $query .= "                     AND B1.FS_GRDDIV    = '2' ";
        $query .= "                     AND B1.ENTDIV       = '2' ";
        $query .= "                 GROUP BY ";
        $query .= "                     B1.ENTEXAMYEAR ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     'A 入学者 合計' AS SORT, ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= "     CASE WHEN CM_A{$cnt}.CM_ALLCNT IS NULL THEN 0 ELSE CM_A{$cnt}.CM_ALLCNT END, ";
            $query .= "     CASE WHEN CM_K{$cnt}.CM_KANEN_CNT IS NULL THEN 0 ELSE CM_K{$cnt}.CM_KANEN_CNT END, ";
            $cnt++;
        }
        $query .= "     CASE WHEN EALL.ENT_ALL_CNT IS NULL THEN 0 ELSE EALL.ENT_ALL_CNT END, ";
        $query .= "     CASE WHEN KALL.ENT_K_ALL_CNT IS NULL THEN 0 ELSE KALL.ENT_K_ALL_CNT END ";
        $query .= " FROM ";
        $query .= "     BASE_ALL B1 ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= "     LEFT JOIN CM_ALL{$cnt}        CM_A{$cnt} ON B1.ENTEXAMYEAR = CM_A{$cnt}.ENTEXAMYEAR ";
            $query .= "     LEFT JOIN CM_KANEN_ALL{$cnt}  CM_K{$cnt} ON B1.ENTEXAMYEAR = CM_K{$cnt}.ENTEXAMYEAR ";
            $cnt++;
        }
        $query .= "     LEFT JOIN ENT_ALL       EALL ON B1.ENTEXAMYEAR = EALL.ENTEXAMYEAR ";
        $query .= "     LEFT JOIN ENT_KANEN_ALL KALL ON B1.ENTEXAMYEAR = KALL.ENTEXAMYEAR ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     'B 合格後の辞退者数' AS SORT, ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= "     CASE WHEN JCM_A{$cnt}.JCM_ALLCNT IS NULL THEN 0 ELSE JCM_A{$cnt}.JCM_ALLCNT END, ";
            $query .= "     CASE WHEN JCM_K{$cnt}.JCM_KANEN_CNT IS NULL THEN 0 ELSE JCM_K{$cnt}.JCM_KANEN_CNT END, ";
            $cnt++;
        }
        $query .= "     CASE WHEN JEALL.JENT_ALL_CNT IS NULL THEN 0 ELSE JEALL.JENT_ALL_CNT END, ";
        $query .= "     CASE WHEN JKALL.JENT_K_ALL_CNT IS NULL THEN 0 ELSE JKALL.JENT_K_ALL_CNT END ";
        $query .= " FROM ";
        $query .= "     BASE_ALL B1 ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= "     LEFT JOIN JCM_ALL{$cnt}        JCM_A{$cnt} ON B1.ENTEXAMYEAR = JCM_A{$cnt}.ENTEXAMYEAR ";
            $query .= "     LEFT JOIN JCM_KANEN_ALL{$cnt}  JCM_K{$cnt} ON B1.ENTEXAMYEAR = JCM_K{$cnt}.ENTEXAMYEAR ";
            $cnt++;
        }
        $query .= "     LEFT JOIN JENT_ALL       JEALL ON B1.ENTEXAMYEAR = JEALL.ENTEXAMYEAR ";
        $query .= "     LEFT JOIN JENT_KANEN_ALL JKALL ON B1.ENTEXAMYEAR = JKALL.ENTEXAMYEAR ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    //県への報告取得
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     REPORT_ENTEXAM_L340W_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.REGISTERCD = L1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' AND ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->ObjYear."' AND ";
        $query .= "     T1.CSV_PRG          = 1 AND ";
        $query .= "     T1.CSVDIV           = 0 ";

        return $query;
    }

    //フィールド取得
    function getFieldName($table) {
        $query  = " SELECT ";
        $query .= "     COLUMN_NAME, ";
        $query .= "     DATA_TYPE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABLE_NAME = '".$table."' AND ";
        $query .= "     COLUMN_NAME NOT IN ('REGISTERCD', 'UPDATED') ";
        $query .= " ORDER BY ";
        $query .= "     ORDINAL_POSITION ";

        return $query;
    }

    //学校側・データ取得
    function getDataQuery($model, $table, $where="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table." T1 ";
        if ($where) {
            $query .= " WHERE ".$where;
        }

        return $query;
    }

    //県側・削除
    function getDeleteQuery($model, $table, $where="") {
        $query  = " DELETE FROM ";
        $query .= "     EDBOARD_".$table." T1 ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        if ($where) {
            $query .= " AND ".$where;
        }

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        /**********************************/
        /*  学校側から県側へデータコピー  */
        /**********************************/

        //対象テーブル一覧／条件
        $table_array = array();
        $table_array["COURSE_MST"]      = "";
        $table_array["COURSE_YDAT"]     = " YEAR IN ('".CTRL_YEAR."', '".$model->ObjYear."') ";
        $table_array["MAJOR_MST"]       = "";
        $table_array["MAJOR_YDAT"]      = " YEAR = '".$model->ObjYear."' ";
        $table_array["COURSECODE_MST"]  = "";
        $table_array["COURSECODE_YDAT"] = " YEAR = '".$model->ObjYear."' ";
        $table_array["ENTEXAM_APPLICANTBASE_DAT"] = " ENTEXAMYEAR = '".$model->ObjYear."' ";

        foreach ($table_array as $table => $where) {
            //フィールド一覧取得
            $field_array = array();
            $query = knjl341wQuery::getFieldName($table);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["DATA_TYPE"] == "CHARACTER VARYING") {
                    $data_type = TEXT;
                } else if (in_array($row["DATA_TYPE"], array("INTEGER", "SMALLINT", "DECIMAL"))) {
                    $data_type = NUMBER;
                } else {
                    $data_type = $row["DATA_TYPE"];
                }
                $field_array[$row["COLUMN_NAME"]] = $data_type;
            }
            $result->free();

            //DELETE -- 県側
            $query = knjl341wQuery::getDeleteQuery($model, $table, $where);
            $db2->query($query);

            //INSERT -- 学校側から県側へコピー
            $query = knjl341wQuery::getDataQuery($model, $table, $where);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array(); //初期化
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                foreach ($field_array as $field => $type) {
                    $data[$field][$type]            = $row[$field];
                }
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";
                $query  = Query::insertSQL($data, "EDBOARD_".$table);
                $db2->query($query);
            }
            $result->free();
        }

        /******************/
        /*  報告テーブル  */
        /******************/

        //DELETE -- 県側
        $query  = " DELETE FROM REPORT_ENTEXAM_L340W_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= "   AND ENTEXAMYEAR       = '".$model->ObjYear."' ";
        $query .= "   AND CSV_PRG           = 1 ";
        $query .= "   AND CSVDIV            = 0 ";
        $db2->query($query);

        //INSERT -- 県側
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
        $data["CSV_PRG"][NUMBER]            = 1;
        $data["CSVDIV"][NUMBER]             = 0;
        $data["EXECUTE_DATE"][NUMBER]       = "sysdate()";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $query  = Query::insertSQL($data, "REPORT_ENTEXAM_L340W_DAT");
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }
}
?>
