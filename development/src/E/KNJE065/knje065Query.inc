<?php

require_once('for_php7.php');

class knje065Query extends Query {

    //Z010
    function getZ010()
    {
        $query  = "SELECT NAME1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学籍在籍データの存在確認
    function getSchSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_regd_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }
    //評定マスタの存在確認
    function getAssesscd()
    {
        $query  = "SELECT COUNT(assesscd) FROM assess_mst ";
        $query .= " WHERE assesscd = '4' ";
        return $query;
    }

    //コンボボックスの中
    function getGradeQuery()
    {
        $query  = "SELECT DISTINCT(grade) ";
        $query .= "  FROM schreg_regd_hdat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= " ORDER BY GRADE";

        return $query;
    }

    //表示用
    function ReadQuery($model)
    {
        if ($model->Properties["gaihyouGakkaBetu"] == '1') {
            $query  = "SELECT T1.coursecd, T1.majorcd, ";
            $query .= "       (W2.coursename || W2.majorname) AS name, ";
            $query .= "       T1.coursecode, W3.coursecodename, ";
            $query .= "       W1.a_member, W1.b_member, W1.c_member, W1.d_member, W1.e_member, ";
            $query .= "       W1.course_member, W1.grade_member ";
            $query .= "  FROM (SELECT distinct coursecd, majorcd, coursecode ";
            $query .= "          FROM schreg_regd_dat ";
            $query .= "         WHERE year = '".CTRL_YEAR."'";
            $query .= "           AND semester = '".CTRL_SEMESTER."'";
            $query .= "           AND grade = '".$model->gc_select."') T1 ";
            $query .= "       LEFT OUTER JOIN geneviewmbr_dat W1 ";
            $query .= "         ON T1.coursecd = W1.coursecd ";
            $query .= "        AND T1.majorcd = W1.majorcd ";
            $query .= "        AND T1.coursecode = W1.coursecode ";
            $query .= "        AND W1.year = '".CTRL_YEAR."'";
            $query .= "        AND W1.grade = '".$model->gc_select."'";
            $query .= "       LEFT OUTER JOIN v_course_major_mst W2 ";
            $query .= "         ON T1.coursecd = W2.coursecd ";
            $query .= "        AND T1.majorcd = W2.majorcd ";
            $query .= "        AND W2.year = '".CTRL_YEAR."'";
            $query .= "       LEFT OUTER JOIN v_coursecode_mst W3 ";
            $query .= "         ON T1.coursecode = W3.coursecode ";
            $query .= "        AND W3.year = '".CTRL_YEAR."'";
            $query .= " ORDER BY T1.coursecd, T1.majorcd, T1.coursecode ";
        } else {
            $query  = "SELECT T1.coursecd, T1.majorcd, ";
            $query .= "       (W2.coursename || W2.majorname) AS name, ";
            $query .= "       '0000' as coursecode, ";
            $query .= "       W1.a_member, W1.b_member, W1.c_member, W1.d_member, W1.e_member, ";
            $query .= "       W1.course_member, W1.grade_member ";
            $query .= "  FROM (SELECT distinct coursecd, majorcd ";
            $query .= "          FROM schreg_regd_dat ";
            $query .= "         WHERE year = '".CTRL_YEAR."'";
            $query .= "           AND semester = '".CTRL_SEMESTER."'";
            $query .= "           AND grade = '".$model->gc_select."') T1 ";
            $query .= "       LEFT OUTER JOIN ";
            $query .= "                    (SELECT ";
            $query .= "                         YEAR, ";
            $query .= "                         COURSECD, ";
            $query .= "                         MAJORCD, ";
            $query .= "                         GRADE, ";
            $query .= "                         SUM(A_MEMBER) AS A_MEMBER, ";
            $query .= "                         SUM(B_MEMBER) AS B_MEMBER, ";
            $query .= "                         SUM(C_MEMBER) AS C_MEMBER, ";
            $query .= "                         SUM(D_MEMBER) AS D_MEMBER, ";
            $query .= "                         SUM(E_MEMBER) AS E_MEMBER, ";
            $query .= "                         SUM(COURSE_MEMBER) AS COURSE_MEMBER, ";
            $query .= "                         SUM(GRADE_MEMBER) AS GRADE_MEMBER ";
            $query .= "                     FROM ";
            $query .= "                         GENEVIEWMBR_DAT ";
            $query .= "                     GROUP BY ";
            $query .= "                         YEAR, ";
            $query .= "                         COURSECD, ";
            $query .= "                         MAJORCD, ";
            $query .= "                         GRADE) W1 ";
            $query .= "         ON T1.coursecd = W1.coursecd ";
            $query .= "        AND T1.majorcd = W1.majorcd ";
            $query .= "        AND W1.year = '".CTRL_YEAR."'";
            $query .= "        AND W1.grade = '".$model->gc_select."'";
            $query .= "       LEFT OUTER JOIN v_course_major_mst W2 ";
            $query .= "         ON T1.coursecd = W2.coursecd ";
            $query .= "        AND T1.majorcd = W2.majorcd ";
            $query .= "        AND W2.year = '".CTRL_YEAR."'";
            $query .= " ORDER BY T1.coursecd, T1.majorcd ";
        }

        return $query;
    }

    //再計算
    function &getRecalculateQuery($model)
    {
        $valuation = "T1.valuation";
        //評定1の場合は2で処理する
        if ($model->hyoteiYomikae == "1") $valuation = "case when T1.valuation = 1 then 2 else T1.valuation end";
        //同一グループ科目設定の処理にて使用
        $valuationCase  = "case when 0 < T1.valuation then T1.valuation end";
        $creditCase     = "case when 0 < T1.valuation then T1.CREDIT end";

        if ($model->Properties["gaihyouGakkaBetu"] == '1') {
            $query  = " WITH REGD_T AS ( ";
            $query .= " SELECT ";
            $query .= "     SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."'";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."'";
            $query .= "     AND GRADE = '".$model->gc_select."' ";
            $query .= " ), REGD_YEAR AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ANNUAL, ";
            $query .= "     MAX(T1.YEAR) AS YEAR ";
            $query .= " FROM ";
            $query .= "     SCHREG_STUDYREC_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             REGD_T ";
            $query .= "         WHERE ";
            $query .= "             T1.SCHREGNO = REGD_T.SCHREGNO ";
            $query .= "     ) ";
            if ($model->z010["GAKUNENSEI"] == "1") {
                $query .= "     AND T1.ANNUAL > '03' ";
            }
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ANNUAL ";
            $query .= " ), SUBCLASS_GROUP AS ( ";
            $query .= "     SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T2.CLASSCD, ";
                $query .= "     T2.SCHOOL_KIND, ";
                $query .= "     T2.CURRICULUM_CD, ";
            }
            $query .= "         T2.SUBCLASSCD, ";
            $query .= "         T2.SUBCLASSCD2 ";
            $query .= "     FROM ";
            $query .= "         SUBCLASS_MST T2, ";
            $query .= "         SUBCLASS_MST T3 ";
            $query .= "     WHERE ";
            $query .= "         T2.SUBCLASSCD2 IS NOT NULL AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T2.CLASSCD = T3.CLASSCD AND ";
                $query .= "     T2.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
                $query .= "     T2.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
            }
            $query .= "         T2.SUBCLASSCD2 = T3.SUBCLASSCD ";
            $query .= " ), T_SCHREG_STUDYREC AS ( ";
            $query .= "     SELECT T1.YEAR,T1.SCHREGNO,T1.CLASSCD,T1.VALUATION ";
            $query .= "           ,CASE WHEN T1.ADD_CREDIT IS NOT NULL ";
            $query .= "                 THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ";
            $query .= "                 ELSE T1.GET_CREDIT END AS CREDIT ";
            $query .= "           ,L1.GVAL_CALC ";
            $query .= "           ,CASE WHEN L2.SUBCLASSCD2 IS NOT NULL THEN L2.SUBCLASSCD2 ";
            $query .= "                 ELSE T1.SUBCLASSCD END AS SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       ,T1.SCHOOL_KIND ";
                $query .= "       ,T1.CURRICULUM_CD ";
            }
            $query .= "     FROM SCHREG_STUDYREC_DAT T1 ";
            $query .= "          LEFT JOIN SCHOOL_MST L1 ON L1.YEAR = T1.YEAR ";
            $query .= "          LEFT JOIN SUBCLASS_GROUP L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                 AND L2.CLASSCD = T1.CLASSCD ";
                $query .= "                                 AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                                 AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "     WHERE ";
            if ($model->z010["NAME1"] == "jisyukan") {
                $query .= "            (T1.CLASSCD BETWEEN '01' AND '89' OR T1.SUBCLASSCD = '941001')";
            } else if ($model->z010["NAME1"] == "HOUSEI") {
                $query .= "            T1.CLASSCD BETWEEN '01' AND '85' ";
            } else {
                $query .= "            T1.CLASSCD BETWEEN '01' AND '89' ";
            }
            $query .= "            AND EXISTS ( ";
            $query .= "                        SELECT 'X' ";
            $query .= "                        FROM REGD_YEAR E1 ";
            $query .= "                        WHERE T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "                          AND T1.YEAR = E1.YEAR ";
            $query .= "                       ) ";
            $query .= " ), STUDYREC AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC,T1.VALUATION ";
            $query .= "     FROM ";
            $query .= "         T_SCHREG_STUDYREC T1 ";
            $query .= "     WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' ||  ";
                $query .= "     T1.SCHOOL_KIND || '-' ||  ";
                $query .= "     T1.CURRICULUM_CD || '-' ||  ";
            }
            $query .= "         T1.SUBCLASSCD NOT IN (SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         W2.CLASSCD || '-' ||  ";
                $query .= "         W2.SCHOOL_KIND || '-' ||  ";
                $query .= "         W2.CURRICULUM_CD || '-' ||  ";
            }
            $query .= "             W2.SUBCLASSCD2 FROM SUBCLASS_GROUP W2) ";
            $query .= "     UNION ALL ";
            $query .= "     SELECT ";
            $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC ";
            $query .= "        ,case when COUNT(*) = 1 then MAX(T1.VALUATION) ";
            $query .= "              when T1.GVAL_CALC = '0' then ROUND(AVG(FLOAT(".$valuationCase.")),0) ";
            $query .= "              when T1.GVAL_CALC = '1' and 0 < SUM(".$creditCase.") then ROUND(FLOAT(SUM((".$valuationCase.")*T1.CREDIT))/SUM(".$creditCase."),0) ";
            $query .= "              else MAX(T1.VALUATION) end AS VALUATION ";
            $query .= "     FROM ";
            $query .= "         T_SCHREG_STUDYREC T1 ";
            $query .= "     WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' ||  ";
                $query .= "     T1.SCHOOL_KIND || '-' ||  ";
                $query .= "     T1.CURRICULUM_CD || '-' ||  ";
            }
            $query .= "         T1.SUBCLASSCD IN (SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         W2.CLASSCD || '-' ||  ";
                $query .= "         W2.SCHOOL_KIND || '-' ||  ";
                $query .= "         W2.CURRICULUM_CD || '-' ||  ";
            }
            $query .= "             W2.SUBCLASSCD2 FROM SUBCLASS_GROUP W2) ";
            $query .= "     GROUP BY ";
            $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC ";
            $query .= " ) ";
            $query .= "SELECT  T1.coursecd , T1.majorcd ";
            $query .= "       ,(W3.coursename || W3.majorname) AS name ";
            $query .= "       ,T1.coursecode, W4.coursecodename ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=5 THEN 1 ELSE 0 END) AS A_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=4 THEN 1 ELSE 0 END) AS B_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=3 THEN 1 ELSE 0 END) AS C_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=2 THEN 1 ELSE 0 END) AS D_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=1 THEN 1 ELSE 0 END) AS E_MEMBER ";
            $query .= "  FROM (SELECT distinct year, schregno, grade, coursecd, majorcd, coursecode ";
            $query .= "          FROM schreg_regd_dat w1";
            $query .= "         WHERE year     = '".CTRL_YEAR."'";
            $query .= "           AND semester = '".CTRL_SEMESTER."'";
            $query .= "           AND grade    = '".$model->gc_select."' ";
            $query .= "           AND NOT EXISTS ( ";
            $query .= "                           SELECT 'X' ";
            $query .= "                           FROM schreg_base_mst w2 ";
            $query .= "                           WHERE w2.schregno = w1.schregno ";
            $query .= "                             AND w2.grd_div in ('2','3','6','7') "; //2:退学 3:転学 6:除籍 7:転籍 者は除く
            $query .= "                          ) ";
            $query .= "       ) T1 ";
            $query .= "          LEFT OUTER JOIN (";
            $query .= "                           SELECT schregno, avg, assesslevel FROM ";
            $query .= "                                  (SELECT T1.schregno, round(AVG(decimal(".$valuation.")),1)  AS avg ";
            $query .= "                                   FROM STUDYREC T1 ";
            $query .= "                                    GROUP BY T1.schregno) W1,";
            $query .= "                                  (SELECT * ";
            $query .= "                                     FROM assess_mst ";
            $query .= "                                    WHERE assesscd = '4' ) W2 ";
            $query .= "                            where W1.avg between W2.assesslow AND W2.assesshigh ";
            $query .= "       ) T2 ON T1.schregno = T2.schregno ";
            $query .= "          LEFT OUTER JOIN v_course_major_mst W3 ";
            $query .= "            ON T1.coursecd = W3.coursecd ";
            $query .= "           AND T1.majorcd  = W3.majorcd ";
            $query .= "           AND T1.year     = W3.year ";
            $query .= "          LEFT OUTER JOIN v_coursecode_mst W4 ";
            $query .= "            ON T1.coursecode = W4.coursecode ";
            $query .= "           AND T1.year       = W4.year ";
            $query .= " GROUP BY T1.coursecd ";
            $query .= "         ,T1.majorcd ";
            $query .= "         ,W3.coursename ";
            $query .= "         ,W3.majorname ";
            $query .= "         ,T1.coursecode ";
            $query .= "         ,W4.coursecodename ";
            $query .= " ORDER BY T1.coursecd, T1.majorcd ";
            $query .= "         ,T1.coursecode ";
        } else {
            $query  = " WITH REGD_T AS ( ";
            $query .= " SELECT ";
            $query .= "     SCHREGNO ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."'";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."'";
            $query .= "     AND GRADE = '".$model->gc_select."' ";
            $query .= " ), REGD_YEAR AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ANNUAL, ";
            $query .= "     MAX(T1.YEAR) AS YEAR ";
            $query .= " FROM ";
            $query .= "     SCHREG_STUDYREC_DAT T1 ";
            $query .= " WHERE ";
            $query .= "     EXISTS( ";
            $query .= "         SELECT ";
            $query .= "             'x' ";
            $query .= "         FROM ";
            $query .= "             REGD_T ";
            $query .= "         WHERE ";
            $query .= "             T1.SCHREGNO = REGD_T.SCHREGNO ";
            $query .= "     ) ";
            if ($model->z010["GAKUNENSEI"] == "1") {
                $query .= "     AND T1.ANNUAL > '03' ";
            }
            $query .= " GROUP BY ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ANNUAL ";
            $query .= " ), SUBCLASS_GROUP AS ( ";
            $query .= "     SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T2.CLASSCD, ";
                $query .= "     T2.SCHOOL_KIND, ";
                $query .= "     T2.CURRICULUM_CD, ";
            }
            $query .= "         T2.SUBCLASSCD, ";
            $query .= "         T2.SUBCLASSCD2 ";
            $query .= "     FROM ";
            $query .= "         SUBCLASS_MST T2, ";
            $query .= "         SUBCLASS_MST T3 ";
            $query .= "     WHERE ";
            $query .= "         T2.SUBCLASSCD2 IS NOT NULL AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T2.CLASSCD = T3.CLASSCD AND ";
                $query .= "     T2.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
                $query .= "     T2.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
            }
            $query .= "         T2.SUBCLASSCD2 = T3.SUBCLASSCD ";
            $query .= " ), T_SCHREG_STUDYREC AS ( ";
            $query .= "     SELECT T1.YEAR,T1.SCHREGNO,T1.CLASSCD,T1.VALUATION ";
            $query .= "           ,CASE WHEN T1.ADD_CREDIT IS NOT NULL ";
            $query .= "                 THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ";
            $query .= "                 ELSE T1.GET_CREDIT END AS CREDIT ";
            $query .= "           ,L1.GVAL_CALC ";
            $query .= "           ,CASE WHEN L2.SUBCLASSCD2 IS NOT NULL THEN L2.SUBCLASSCD2 ";
            $query .= "                 ELSE T1.SUBCLASSCD END AS SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       ,T1.SCHOOL_KIND ";
                $query .= "       ,T1.CURRICULUM_CD ";
            }
            $query .= "     FROM SCHREG_STUDYREC_DAT T1 ";
            $query .= "          LEFT JOIN SCHOOL_MST L1 ON L1.YEAR = T1.YEAR ";
            $query .= "          LEFT JOIN SUBCLASS_GROUP L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                 AND L2.CLASSCD = T1.CLASSCD ";
                $query .= "                                 AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                                 AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "     WHERE ";
            if ($model->z010["NAME1"] == "jisyukan") {
                $query .= "            (T1.CLASSCD BETWEEN '01' AND '89' OR T1.SUBCLASSCD = '941001')";
            } else if ($model->z010["NAME1"] == "HOUSEI") {
                $query .= "            T1.CLASSCD BETWEEN '01' AND '85' ";
            } else {
                $query .= "            T1.CLASSCD BETWEEN '01' AND '89' ";
            }
            $query .= "            AND EXISTS ( ";
            $query .= "                        SELECT 'X' ";
            $query .= "                        FROM REGD_YEAR E1 ";
            $query .= "                        WHERE T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "                          AND T1.YEAR = E1.YEAR ";
            $query .= "                       ) ";
            $query .= " ), STUDYREC AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC,T1.VALUATION ";
            $query .= "     FROM ";
            $query .= "         T_SCHREG_STUDYREC T1 ";
            $query .= "     WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' ||  ";
                $query .= "     T1.SCHOOL_KIND || '-' ||  ";
                $query .= "     T1.CURRICULUM_CD || '-' ||  ";
            }
            $query .= "         T1.SUBCLASSCD NOT IN (SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         W2.CLASSCD || '-' ||  ";
                $query .= "         W2.SCHOOL_KIND || '-' ||  ";
                $query .= "         W2.CURRICULUM_CD || '-' ||  ";
            }
            $query .= "             W2.SUBCLASSCD2 FROM SUBCLASS_GROUP W2) ";
            $query .= "     UNION ALL ";
            $query .= "     SELECT ";
            $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC ";
            $query .= "        ,case when COUNT(*) = 1 then MAX(T1.VALUATION) ";
            $query .= "              when T1.GVAL_CALC = '0' then ROUND(AVG(FLOAT(".$valuationCase.")),0) ";
            $query .= "              when T1.GVAL_CALC = '1' and 0 < SUM(".$creditCase.") then ROUND(FLOAT(SUM((".$valuationCase.")*T1.CREDIT))/SUM(".$creditCase."),0) ";
            $query .= "              else MAX(T1.VALUATION) end AS VALUATION ";
            $query .= "     FROM ";
            $query .= "         T_SCHREG_STUDYREC T1 ";
            $query .= "     WHERE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' ||  ";
                $query .= "     T1.SCHOOL_KIND || '-' ||  ";
                $query .= "     T1.CURRICULUM_CD || '-' ||  ";
            }
            $query .= "         T1.SUBCLASSCD IN (SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         W2.CLASSCD || '-' ||  ";
                $query .= "         W2.SCHOOL_KIND || '-' ||  ";
                $query .= "         W2.CURRICULUM_CD || '-' ||  ";
            }
            $query .= "             W2.SUBCLASSCD2 FROM SUBCLASS_GROUP W2) ";
            $query .= "     GROUP BY ";
            $query .= "         T1.YEAR,T1.SCHREGNO,T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD,T1.GVAL_CALC ";
            $query .= " ) ";
            $query .= "SELECT  T1.coursecd , T1.majorcd ";
            $query .= "       ,(W3.coursename || W3.majorname) AS name ";
            $query .= "       ,'0000' as coursecode ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=5 THEN 1 ELSE 0 END) AS A_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=4 THEN 1 ELSE 0 END) AS B_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=3 THEN 1 ELSE 0 END) AS C_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=2 THEN 1 ELSE 0 END) AS D_MEMBER ";
            $query .= "       ,SUM(CASE WHEN t2.assesslevel=1 THEN 1 ELSE 0 END) AS E_MEMBER ";
            $query .= "  FROM (SELECT distinct year, schregno, grade, coursecd, majorcd ";
            $query .= "          FROM schreg_regd_dat w1";
            $query .= "         WHERE year     = '".CTRL_YEAR."'";
            $query .= "           AND semester = '".CTRL_SEMESTER."'";
            $query .= "           AND grade    = '".$model->gc_select."' ";
            $query .= "           AND NOT EXISTS ( ";
            $query .= "                           SELECT 'X' ";
            $query .= "                           FROM schreg_base_mst w2 ";
            $query .= "                           WHERE w2.schregno = w1.schregno ";
            $query .= "                             AND w2.grd_div in ('2','3','6','7') "; //2:退学 3:転学 6:除籍 7:転籍 者は除く
            $query .= "                          ) ";
            $query .= "       ) T1 ";
            $query .= "          LEFT OUTER JOIN (";
            $query .= "                           SELECT schregno, avg, assesslevel FROM ";
            $query .= "                                  (SELECT T1.schregno, round(AVG(decimal(".$valuation.")),1)  AS avg ";
            $query .= "                                   FROM STUDYREC T1 ";
            $query .= "                                    GROUP BY T1.schregno) W1,";
            $query .= "                                  (SELECT * ";
            $query .= "                                     FROM assess_mst ";
            $query .= "                                    WHERE assesscd = '4' ) W2 ";
            $query .= "                            where W1.avg between W2.assesslow AND W2.assesshigh ";
            $query .= "       ) T2 ON T1.schregno = T2.schregno ";
            $query .= "          LEFT OUTER JOIN v_course_major_mst W3 ";
            $query .= "            ON T1.coursecd = W3.coursecd ";
            $query .= "           AND T1.majorcd  = W3.majorcd ";
            $query .= "           AND T1.year     = W3.year ";
            $query .= " GROUP BY T1.coursecd ";
            $query .= "         ,T1.majorcd ";
            $query .= "         ,W3.coursename ";
            $query .= "         ,W3.majorname ";
            $query .= " ORDER BY T1.coursecd, T1.majorcd ";
        }

        return $query;
    }

    //学年人数合計
    function &getAllCntQuery($model)
    {
        $query  = "SELECT COUNT(schregno) AS GRADE_MEMBER ";
        $query .= "  FROM schreg_regd_dat w1 ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        $query .= "   AND grade    = '".$model->gc_select."'";
        $query .= "   AND NOT EXISTS ( ";
        $query .= "                    SELECT 'X' ";
        $query .= "                    FROM schreg_base_mst w2 ";
        $query .= "                    WHERE w2.schregno = w1.schregno ";
        $query .= "                      AND w2.grd_div in ('2','3','6','7') "; //2:退学 3:転学 6:除籍 7:転籍 者は除く
        $query .= "                   ) ";

        return $query;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM geneviewmbr_dat ";
        $query .= " WHERE year  = '".CTRL_YEAR."'";
        $query .= "   AND grade = '".$model->gc_select."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return true;
    }

    //UPDATE
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        for($i=0; $i<get_count($model->fields["CODE"]); $i++)
        {
            $code_arr = explode(",",$model->fields["CODE"][$i]);

            $course_member = $model->fields["A_MEMBER"][$i];
            $course_member += $model->fields["B_MEMBER"][$i];
            $course_member += $model->fields["C_MEMBER"][$i];
            $course_member += $model->fields["D_MEMBER"][$i];
            $course_member += $model->fields["E_MEMBER"][$i];

            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["COURSECD"][TEXT]         = $code_arr[0];
            $data["MAJORCD"][TEXT]          = $code_arr[1];
            $data["GRADE"][TEXT]            = $model->gc_select;
            $data["COURSECODE"][TEXT]       = $code_arr[2];
            $data["A_MEMBER"][NUMBER]       = $model->fields["A_MEMBER"][$i];
            $data["B_MEMBER"][NUMBER]       = $model->fields["B_MEMBER"][$i];
            $data["C_MEMBER"][NUMBER]       = $model->fields["C_MEMBER"][$i];
            $data["D_MEMBER"][NUMBER]       = $model->fields["D_MEMBER"][$i];
            $data["E_MEMBER"][NUMBER]       = $model->fields["E_MEMBER"][$i];
            $data["COURSE_MEMBER"][NUMBER]  = $course_member;
            $data["GRADE_MEMBER"][NUMBER]   = $model->fields["GRADE_MEMBER"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        =" sysdate()";

            $db->query(Query::insertSQL($data, "geneviewmbr_dat"));
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
