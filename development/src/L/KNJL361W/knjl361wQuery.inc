<?php

require_once('for_php7.php');

class knjl361wQuery extends Query {
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
        $query .= "         AND L1.CSV_PRG          = 1 ";
        $query .= "         AND L1.CSVDIV           = 0 ";
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

    //受検学科コース取得
    function getCouseMajor($model, $schoolCd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE AS CM_CD, ";
        $query .= "     V1.MAJORNAME || '・' || V2.COURSECODENAME AS CM_NAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN V_EDBOARD_COURSE_MAJOR_MST V1 ";
        $query .= "          ON B1.EDBOARD_SCHOOLCD = V1.EDBOARD_SCHOOLCD ";
        $query .= "         AND B1.ENTEXAMYEAR      = V1.YEAR ";
        $query .= "         AND B1.SUC_COURSECD     = V1.COURSECD ";
        $query .= "         AND B1.SUC_MAJORCD      = V1.MAJORCD ";
        $query .= "     LEFT JOIN V_EDBOARD_COURSECODE_MST V2 ";
        $query .= "          ON B1.EDBOARD_SCHOOLCD = V2.EDBOARD_SCHOOLCD ";
        $query .= "         AND B1.ENTEXAMYEAR      = V2.YEAR ";
        $query .= "         AND B1.SUC_COURSECODE   = V2.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     B1.EDBOARD_SCHOOLCD = '".$schoolCd."' AND ";
        $query .= "     B1.ENTEXAMYEAR      = '".$model->ObjYear."' AND ";
        $query .= "     B1.SUC_COURSECD || B1.SUC_MAJORCD || B1.SUC_COURSECODE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     CM_CD ";

        return $query;
    }

    //CSV（三重県DATA）
    function selectCsvQuery($model, $schoolCd) {
        $query  = " WITH BASEDATA AS ( ";
        $query .= "                     SELECT ";
        $query .= "                         B1.FS_CD ";
        $query .= "                     FROM ";
        $query .= "                         EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                         INNER JOIN NAME_MST N1 ";
        $query .= "                                              ON N1.NAMECD1   = 'L013' ";
        $query .= "                                             AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                     WHERE ";
        $query .= "                             B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                         AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                         EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                               AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                     WHERE ";
            $query .= "                             B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                         AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                         EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                               AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                     WHERE ";
            $query .= "                             B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                         AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
        $query .= "                         EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                               AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                     WHERE ";
        $query .= "                             B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                         AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
        $query .= "                         EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                         INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                               AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                         LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                         LEFT JOIN PREF_MST P1      ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                     WHERE ";
        $query .= "                             B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                         AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
    function selectCsvQuery2($model, $schoolCd) {
        $query  = " WITH  B_ALL(PREF_CD, PREF_NAME) AS ( ";
        $query .= "             VALUES('99', '県外合計') ";
        $query .= " ), BASEDATA AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     P1.PREF_CD, ";
        $query .= "                     P1.PREF_NAME ";
        $query .= "                 FROM ";
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD  ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                 WHERE ";
            $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                 WHERE ";
            $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                 WHERE ";
            $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
            $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
            $query .= "                 WHERE ";
            $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT   != '5' ";
        $query .= "                     AND B1.ENTDIV       = '1' ";
        $query .= "                     AND P1.PREF_CD     != '24' ";
        $query .= "                     AND N1.NAMESPARE1   = '1' ";
        $query .= " ), NMKANENCNTALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     '99' AS PREF_CD, ";
        $query .= "                     COUNT(*) AS KANENDOALL ";
        $query .= "                 FROM ";
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                     LEFT JOIN FINSCHOOL_MST F1 ON B1.FS_CD = F1.FINSCHOOLCD ";
        $query .= "                     LEFT JOIN PREF_MST P1 ON F1.FINSCHOOL_PREF_CD = P1.PREF_CD ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
    function selectCsvQuery3($model, $schoolCd) {
        $query  = " WITH  BASE_ALL(ENTEXAMYEAR) AS ( ";
        $query .= "             VALUES('".$model->ObjYear."') ";
        $cnt = 1;
        foreach ($model->cmCd as $cmVal) {
            $query .= " ), CM_ALL{$cnt} AS ( ";
            $query .= "                 SELECT ";
            $query .= "                     B1.ENTEXAMYEAR, ";
            $query .= "                     COUNT(*) AS CM_ALLCNT ";
            $query .= "                 FROM ";
            $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                 WHERE ";
            $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                 WHERE ";
            $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                 WHERE ";
            $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "                     V_EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
            $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
            $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
            $query .= "                 WHERE ";
            $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
            $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "                     AND B1.JUDGEMENT    = '2' ";
        $query .= "                     AND B1.ENTDIV       = '2' ";
        $query .= "                 GROUP BY ";
        $query .= "                     B1.ENTEXAMYEAR ";
        $query .= " ), JENT_KANEN_ALL AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     B1.ENTEXAMYEAR, ";
        $query .= "                     COUNT(*) AS JENT_K_ALL_CNT ";
        $query .= "                 FROM ";
        $query .= "                     EDBOARD_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "                     INNER JOIN NAME_MST N1 ON N1.NAMECD1   = 'L013' ";
        $query .= "                                           AND B1.JUDGEMENT = N1.NAMECD2 ";
        $query .= "                 WHERE ";
        $query .= "                         B1.EDBOARD_SCHOOLCD  = '".$schoolCd."' ";
        $query .= "                     AND B1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
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
            $query .= "     CSV_PRG             = 1 AND ";
            $query .= "     CSVDIV              = 0 ";

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
