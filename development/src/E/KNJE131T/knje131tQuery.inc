<?php

require_once('for_php7.php');

class knje131tQuery extends Query {

    function getNameMstZ010() {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND NAMECD1 = 'Z010' ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model, $flg="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        if ($flg == "") {
            $query .= "     ,T2.GRADE_CD, ";
            $query .= "     T2.GRADE_NAME1 AS LABEL, ";
            $query .= "     T1.GRADE AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_GDAT T2 ON  T1.YEAR = T2.YEAR ";
        $query .= "                         AND T1.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.SCHOOL_KIND IN ('J','H') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($flg) {
            $query .= " AND T2.GRADE = '".$model->field["GRADE"]."' ";
        }
        if ($flg == "") {
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND ((T1.TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          T1.SUBTR_CD3 = '".STAFFCD."') ";
                $query .= "       OR (T1.GRADE IN ";
                $query .= "             (SELECT ";
                $query .= "                  FIELD2 ";
                $query .= "              FROM ";
                $query .= "                  STAFF_DETAIL_MST ST ";
                $query .= "              WHERE ";
                $query .= "                  T1.YEAR = ST.YEAR ";
                $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
                $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
                $query .= "                  AND ST.FIELD1 = '0200')) ";
                $query .= "         ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }


    //年組取得（権限チェック）
    function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND T2.GRADE = '".$model->field["GRADE"]."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND ((T2.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.TR_CD3 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T2.SUBTR_CD3 = '".STAFFCD."') ";
            $query .= "       OR (T2.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  T2.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //教科一覧リスト
    function getSelectClassMst($model, $getSchoolKind="")
    {
        $query  = "SELECT CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     || '-' || SCHOOL_KIND ";
        }
        $query .= "     AS VALUE, ";
        $query .= "     CLASSNAME ";
        $query .= " FROM ";
        $query .= "     CLASS_MST ";
        if ($getSchoolKind) {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".$getSchoolKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE";

        return $query;
    }


    //科目
    function getSubClassName($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO,ANNUAL ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='" .CTRL_YEAR ."' AND SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "            GRADE||HR_CLASS IN ('" . $model->selectdata . "') ) ";
        if ($model->field["OUT_DIV"] == "1") { //学年評価
            $query .= " , STUDYREC AS ( ";
            $query .= "     SELECT T1.YEAR, T1.SCHREGNO, L1.ANNUAL, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            } else {
                $query .= "        substr(T1.SUBCLASSCD,1,2) as CLASSCD, ";
            }
            $query .= "            T1.SUBCLASSCD, T1.SCORE as VALUATION ";
            if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "     FROM   RECORD_RANK_SDIV_DAT T1";
            } else {
                $query .= "     FROM   RECORD_RANK_DAT T1";
            }
            $query .= "            LEFT JOIN SCHNO L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     WHERE  T1.SCORE IS NOT NULL AND T1.SCORE < 11 ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND  T1.CLASSCD < '90' ";
            } else {
                $query .= "   AND  substr(T1.SUBCLASSCD,1,2) < '90' ";
            }
            $query .= "       AND  T1.YEAR = '" .CTRL_YEAR ."' "; //今年度
            $query .= "       AND  T1.SUBCLASSCD NOT IN ('333333', '555555') AND T1.SUBCLASSCD NOT LIKE '99999%' ";
            if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "       AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990009' ) ";
            } else {
                $query .= "       AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '99900' ) ";
            }
        } else { //評定
            $query .= " , STUDYREC0 AS ( ";
            $query .= "     SELECT T1.YEAR, T1.SCHREGNO, T1.ANNUAL, T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            }
            $query .= "            VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD ";
            $query .= "     FROM   SCHREG_STUDYREC_DAT T1";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "     WHERE  T1.CLASSCD < '90' AND VALUATION IS NOT NULL AND T1.VALUATION < 11 ) ";
            $query .= " , STUDYREC AS ( ";
            $query .= "     SELECT SCHREGNO, ANNUAL, CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            }
            $query .= "            SUBCLASSCD  ";
            $query .= "     FROM   STUDYREC0 T1";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     WHERE NOT EXISTS ( ";
                $query .= "                    SELECT ";
                $query .= "                        'x' ";
                $query .= "                    FROM ";
                $query .= "                        SCHREG_REGD_GDAT E1 ";
                $query .= "                    WHERE ";
                $query .= "                        T1.YEAR = E1.YEAR ";
                $query .= "                        AND T1.ANNUAL = E1.GRADE ";
                $query .= "                        AND VALUE(E1.SCHOOL_KIND, '-') <> '".$model->getSchoolKind."' ";
                $query .= "                    ) ";
            }
            $query .= "     GROUP BY SCHREGNO, ANNUAL, T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD) ";
        }
        $query .= "  ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "        T1.SUBCLASSCD, T1.ANNUAL, S1.SUBCLASSNAME ";
        $query .= " FROM   SCHNO T2 ";
        $query .= "        INNER JOIN STUDYREC T1 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN SUBCLASS_MST S1 ON S1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND S1.CLASSCD = T1.CLASSCD ";
            $query .= "                             AND S1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                             AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
        }
        $query .= "        T1.SUBCLASSCD, T1.ANNUAL ";

        return $query;
    }


    //教科
    function getClassName($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO,ANNUAL ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='" .CTRL_YEAR ."' AND SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "            GRADE||HR_CLASS IN ('" . $model->selectdata . "') ) ";
        if ($model->field["OUT_DIV"] == "1") { //学年評価
            $query .= " , STUDYREC AS ( ";
            $query .= "     SELECT T1.YEAR, T1.SCHREGNO, L1.ANNUAL, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            } else {
                $query .= "        substr(T1.SUBCLASSCD,1,2) as CLASSCD, ";
            }
            $query .= "            T1.SUBCLASSCD, T1.SCORE as VALUATION ";
            if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "     FROM   RECORD_RANK_SDIV_DAT T1";
            } else {
                $query .= "     FROM   RECORD_RANK_DAT T1";
            }
            $query .= "            LEFT JOIN SCHNO L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     WHERE  T1.SCORE IS NOT NULL AND T1.SCORE < 11 ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND  T1.CLASSCD < '90' ";
            } else {
                $query .= "   AND  substr(T1.SUBCLASSCD,1,2) < '90' ";
            }
            $query .= "       AND  T1.YEAR = '" .CTRL_YEAR ."' "; //今年度
            $query .= "       AND  T1.SUBCLASSCD NOT IN ('333333', '555555') AND T1.SUBCLASSCD NOT LIKE '99999%' ";
            if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "       AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990009' ) ";
            } else {
                $query .= "       AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '99900' ) ";
            }
        } else { //評定
            $query .= " , STUDYREC AS ( ";
            $query .= "     SELECT YEAR, SCHREGNO, ANNUAL, CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            }
            $query .= "            SUBCLASSCD, VALUATION ";
            $query .= "     FROM   SCHREG_STUDYREC_DAT T1";
            $query .= "     WHERE  T1.CLASSCD < '90' AND VALUATION IS NOT NULL AND T1.VALUATION < 11 ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     AND NOT EXISTS ( ";
                $query .= "                    SELECT ";
                $query .= "                        'x' ";
                $query .= "                    FROM ";
                $query .= "                        SCHREG_REGD_GDAT E1 ";
                $query .= "                    WHERE ";
                $query .= "                        T1.YEAR = E1.YEAR ";
                $query .= "                        AND T1.ANNUAL = E1.GRADE ";
                $query .= "                        AND VALUE(E1.SCHOOL_KIND, '-') <> '".$model->getSchoolKind."' ";
                $query .= "                    ) ";
            }
            $query .= "                    ) ";
        }
        $query .= "  ";
        $query .= " SELECT DISTINCT T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.SCHOOL_KIND, ";
        }
        $query .= "        C1.CLASSNAME ";
        $query .= " FROM   SCHNO T2 ";
        $query .= "        INNER JOIN STUDYREC T1 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN CLASS_MST C1 ON C1.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " ORDER BY T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     ,T1.SCHOOL_KIND ";
        }

        return $query;
    }


    //性別
    function getSexName()
    {
        $query  = " SELECT NAMECD2, NAME1, ABBV1 ";
        $query .= " FROM   V_NAME_MST ";
        $query .= " WHERE  YEAR = '" .CTRL_YEAR ."' AND NAMECD1 = 'Z002' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }


    //学校区分取得
    function getSchoolDiv($model, $getSchoolKind)
    {
        $query  = " SELECT SCHOOLDIV ";
        $query .= " FROM   SCHOOL_MST ";
        $query .= " WHERE  YEAR = '" .CTRL_YEAR ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '" .$getSchoolKind."' ";
        }

        return $query;
    }


    //CSV
    function getSelectCsv($model)
    {
        $query  = " WITH SCHNO AS ( ";
        $query .= "     SELECT SCHREGNO,GRADE,HR_CLASS,ATTENDNO,ANNUAL ";
        $query .= "     FROM   SCHREG_REGD_DAT ";
        $query .= "     WHERE  YEAR='" .CTRL_YEAR ."' AND SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "            GRADE||HR_CLASS IN ('" . $model->selectdata . "') ";
        $query .= " ) ";
        if ($model->field["OUT_DIV"] == "1") { //学年評価
            $query .= " , STUDYREC AS ( ";
            $query .= "     SELECT T1.YEAR, T1.SCHREGNO, L1.ANNUAL, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            } else {
                $query .= "        substr(T1.SUBCLASSCD,1,2) as CLASSCD, ";
            }
            $query .= "            T1.SUBCLASSCD ";
            //評定1の場合は2で処理する　仮評定の場合のみ2とする
            if ($model->field["HYOTEI_YOMIKAE"] == "1") {
                //仮評定フラグ
                if ($model->Properties["useProvFlg"] == '1') {
                    $query .= "   ,case when T1.SCORE = 1 AND T3.PROV_FLG = '1' then 2 else T1.SCORE end as VALUATION ";
                } else {
                    $query .= "   ,case when T1.SCORE = 1 then 2 else T1.SCORE end as VALUATION ";
                }
            } else {
                $query .= "       ,T1.SCORE as VALUATION ";
            }
            if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "     FROM   RECORD_RANK_SDIV_DAT T1";
            } else {
                $query .= "     FROM   RECORD_RANK_DAT T1";
            }
            $query .= "            LEFT JOIN SCHNO L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            //仮評定フラグ
            if ($model->Properties["useProvFlg"] == '1') {
                $query .= "     LEFT JOIN RECORD_PROV_FLG_DAT T3 ";
                $query .= "            ON  T3.YEAR = T1.YEAR ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "        AND T3.CLASSCD = T1.CLASSCD ";
                    $query .= "        AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "        AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "            AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
                $query .= "            AND T3.SCHREGNO = T1.SCHREGNO ";
            }
            $query .= "     WHERE  T1.SCORE IS NOT NULL AND T1.SCORE < 11 ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND  T1.CLASSCD < '90' ";
            } else {
                $query .= "   AND  substr(T1.SUBCLASSCD,1,2) < '90' ";
            }
            $query .= "       AND  T1.YEAR = '" .CTRL_YEAR ."' "; //今年度
            $query .= "       AND  T1.SUBCLASSCD NOT IN ('333333', '555555') AND T1.SUBCLASSCD NOT LIKE '99999%' ";
            if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
                $query .= "       AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990009' ) ";
            } else {
                $query .= "       AND  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '99900' ) ";
            }
        } else { //評定

            $query .= " , STUDYREC_YEAR AS ( ";
            $query .= "     SELECT  ";
            $query .= "         SCHREGNO, ";
            $query .= "         ANNUAL, ";
            if ($model->schooldiv == '0') {   
                $query .= "         MAX(YEAR) AS YEAR ";
            } else {
                $query .= "         YEAR ";
            }
            $query .= "     FROM ";
            $query .= "         SCHREG_STUDYREC_DAT ";
            if ($model->schooldiv == '0') {   
                $query .= "     GROUP BY ";
                $query .= "         SCHREGNO, ";
                $query .= "         ANNUAL ";
            }
            $query .= " ) ";
            $query .= " , STUDYREC0 AS ( ";
            $query .= "     SELECT T1.YEAR, T1.SCHREGNO, T1.ANNUAL, T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            }
            $query .= "            VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD ";
            //評定1の場合は2で処理する　仮評定の場合のみ2とする
            if ($model->field["HYOTEI_YOMIKAE"] == "1") {
                //仮評定フラグ
                if ($model->Properties["useProvFlg"] == '1') {
                    $query .= "   ,case when T1.VALUATION = 1 AND T3.PROV_FLG = '1' then 2 else T1.VALUATION end as VALUATION ";
                } else {
                    $query .= "   ,case when T1.VALUATION = 1 then 2 else T1.VALUATION end as VALUATION ";
                }
            } else {
                $query .= "       ,T1.VALUATION ";
            }
            $query .= "           ,CASE WHEN ADD_CREDIT IS NOT NULL OR GET_CREDIT IS NOT NULL THEN VALUE(ADD_CREDIT, 0) + VALUE(GET_CREDIT, 0) END AS CREDIT ";
            $query .= "     FROM   SCHREG_STUDYREC_DAT T1";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            //仮評定フラグ
            if ($model->Properties["useProvFlg"] == '1') {
                $query .= "     LEFT JOIN STUDYREC_PROV_FLG_DAT T3 ";
                $query .= "            ON  T3.SCHOOLCD = T1.SCHOOLCD ";
                $query .= "            AND T3.YEAR = T1.YEAR ";
                $query .= "            AND T3.SCHREGNO = T1.SCHREGNO ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "        AND T3.CLASSCD = T1.CLASSCD ";
                    $query .= "        AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "        AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "            AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
            }
            $query .= "     WHERE ";
            $query .= "         T1.CLASSCD < '90' ";
            $query .= "         AND 0 < T1.VALUATION AND T1.VALUATION < 11 ";
            $query .= "         AND EXISTS ( ";
            $query .= "                    SELECT ";
            $query .= "                        'x' ";
            $query .= "                    FROM ";
            $query .= "                        SCHNO E1 ";
            $query .= "                    WHERE ";
            $query .= "                        T1.SCHREGNO = E1.SCHREGNO ";
            $query .= "                    ) ";
            $query .= "         AND T1.YEAR IN ( ";
            $query .= "                    SELECT ";
            $query .= "                        E2.YEAR ";
            $query .= "                    FROM ";
            $query .= "                        STUDYREC_YEAR E2 ";
            $query .= "                    WHERE ";
            $query .= "                        T1.SCHREGNO = E2.SCHREGNO ";
            $query .= "                    ) ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     AND NOT EXISTS ( ";
                $query .= "                    SELECT ";
                $query .= "                        'x' ";
                $query .= "                    FROM ";
                $query .= "                        SCHREG_REGD_GDAT E1 ";
                $query .= "                    WHERE ";
                $query .= "                        T1.YEAR = E1.YEAR ";
                $query .= "                        AND T1.ANNUAL = E1.GRADE ";
                $query .= "                        AND VALUE(E1.SCHOOL_KIND, '-') <> '".$model->getSchoolKind."' ";
                $query .= "                    ) ";
            }

            $query .= " ), STUDYREC AS ( ";
            $query .= "     SELECT SCHREGNO, ANNUAL, CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            }
            $query .= "            SUBCLASSCD,  ";
            $query .= "     CASE WHEN COUNT(*) = 1 THEN MAX(VALUATION) ";
            $query .= "          WHEN GVAL_CALC = '0' THEN ROUND(AVG(FLOAT(CASE WHEN 0 < VALUATION THEN VALUATION END)),0) ";
            $query .= "          WHEN GVAL_CALC = '1' AND 0 < SUM(CASE WHEN 0 < VALUATION THEN CREDIT END) THEN ROUND(FLOAT(SUM((CASE WHEN 0 < VALUATION THEN VALUATION END)*CREDIT))/SUM(CASE WHEN 0 < VALUATION THEN CREDIT END),0) ";
            $query .= "          ELSE MAX(VALUATION) END AS VALUATION ";
            $query .= "     FROM   STUDYREC0 T1";
            $query .= "     LEFT JOIN SCHOOL_MST T2 ON T2.YEAR = T1.YEAR ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND T2.SCHOOL_KIND = '" .$model->getSchoolKind."' ";
            }
            $query .= "     GROUP BY SCHREGNO, ANNUAL, T1.CLASSCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        T1.SCHOOL_KIND, T1.CURRICULUM_CD, ";
            }
            $query .= "         T1.SUBCLASSCD, GVAL_CALC ";

            $query .= " ) ";
        }

        //各科目の評定および全教科の評定平均
        $query .= " ,STUDYREC_2 AS ( ";
        $query .= "     SELECT SCHREGNO,  ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,5,1) AS VAL_ALL ";
        for ($i = 0; $i < get_count($model->opt_subclasscd); $i++) {
            $query .= "        ,SUM(CASE WHEN ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                   CLASSCD||'-'||SCHOOL_KIND||'-'||CURRICULUM_CD||'-'|| ";
            }
            $query .= "                       SUBCLASSCD||'-'||ANNUAL = '".$model->opt_subclasscd[$i]."' THEN VALUATION ELSE NULL END) AS VALUATION".$i." ";
        }
        $query .= "     FROM   STUDYREC ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "  ) ";

        //各教科の評定平均
        $query .= " ,STUDYREC_3 AS ( ";
        $query .= "     SELECT T1.SCHREGNO ";
        for ($i = 0; $i < get_count($model->opt_classcd); $i++) {
            $query .= "        ,SUM(CASE WHEN T1.CLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                   ||'-'||T1.SCHOOL_KIND ";
            }
            $query .= "                       = '".$model->opt_classcd[$i]."' THEN T1.VAL_AVG ELSE NULL END) AS VAL".str_replace("-", "", $model->opt_classcd[$i])." ";
        }
        $query .= "     FROM ( ";
        $query .= "         SELECT SCHREGNO, CLASSCD,  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            SCHOOL_KIND, ";
        }
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,5,1) AS VAL_AVG ";
        $query .= "         FROM   STUDYREC ";
        $query .= "         GROUP BY SCHREGNO, CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            ,SCHOOL_KIND ";
        }
        $query .= "                 ) T1  ";
        $query .= "     GROUP BY T1.SCHREGNO ";
        $query .= " ) ";

        $query .= " , T_CREDIT AS ( ";
        $query .= "   SELECT T1.SCHREGNO ";
        $query .= "     , SUM(T1.GET_CREDIT) AS GET_CREDIT ";
        $query .= "     , SUM(T1.COMP_CREDIT) AS COMP_CREDIT ";
        $query .= "   FROM SCHREG_STUDYREC_DAT T1 ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     WHERE NOT EXISTS ( ";
            $query .= "                    SELECT ";
            $query .= "                        'x' ";
            $query .= "                    FROM ";
            $query .= "                        SCHREG_REGD_GDAT E1 ";
            $query .= "                    WHERE ";
            $query .= "                        T1.YEAR = E1.YEAR ";
            $query .= "                        AND E1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "                        AND T1.SCHOOL_KIND <> E1.SCHOOL_KIND ";
            $query .= "                    ) ";
        }
        $query .= "   GROUP BY T1.SCHREGNO ";
        $query .= " ) ";

        //主要教科の評定平均
        $query .= " ,STUDYREC_5 AS ( ";
        $query .= "     SELECT SCHREGNO,  ";
        $query .= "            DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,5,1) AS VAL_5ALL ";
        $query .= "     FROM   STUDYREC ";
        $query .= "     WHERE  CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                ||'-'||SCHOOL_KIND ";
        }
        $query .= "                    IN ('" . $model->selectdata2 . "') ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT DISTINCT T2.SCHREGNO, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, B1.NAME, B1.SEX ";
        if ($model->field["OUT_DIV"] == "1") { //学年評価
            for ($i = 0; $i < get_count($model->opt_subclasscd); $i++) {
                $query .= ",CASE WHEN T1.VALUATION" . $i . " -  CEIL(T1.VALUATION" . $i . ") = 0 THEN RTRIM(CAST(CAST(T1.VALUATION" . $i . " AS INT) AS CHAR(4))) ";
                $query .= "      WHEN T1.VALUATION" . $i . " = 0  THEN '0' ";
                $query .= "      ELSE RTRIM(CAST(CAST(ROUND(T1.VALUATION" . $i . " * 10, 0) / 10 AS DECIMAL(4,1)) AS CHAR(4))) ";
                $query .= " END AS VALUATION" . $i . " ";
            }
            for ($i = 0; $i < get_count($model->opt_classcd); $i++) {
                $query .= ",T3.VAL" . str_replace("-", "", $model->opt_classcd[$i]) . " ";
            }
            $query .= "    ,T5.VAL_5ALL ,T1.VAL_ALL ";
        } else {
            $query .= "    , T6.GET_CREDIT ";
            $query .= "    , T6.COMP_CREDIT ";
            $query .= "    , SCHM.GRAD_CREDITS ";
            $query .= "    , ASSESSM.ASSESSMARK ";
            $query .= "    ,T1.VAL_ALL ,T5.VAL_5ALL ";
            for ($i = 0; $i < get_count($model->opt_classcd); $i++) {
                $query .= ",T3.VAL" . str_replace("-", "", $model->opt_classcd[$i]) . " ";
            }
            for ($i = 0; $i < get_count($model->opt_subclasscd); $i++) {
                $query .= ",CASE WHEN T1.VALUATION" . $i . " -  CEIL(T1.VALUATION" . $i . ") = 0 THEN RTRIM(CAST(CAST(T1.VALUATION" . $i . " AS INT) AS CHAR(4))) ";
                $query .= "      WHEN T1.VALUATION" . $i . " = 0  THEN '0' ";
                $query .= "      ELSE RTRIM(CAST(CAST(ROUND(T1.VALUATION" . $i . " * 10, 0) / 10 AS DECIMAL(4,1)) AS CHAR(4))) ";
                $query .= " END AS VALUATION" . $i . " ";
            }
        }
        $query .= " FROM   SCHNO T2 ";
        $query .= "        LEFT JOIN STUDYREC_2 T1 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT JOIN STUDYREC_3 T3 ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT JOIN STUDYREC_5 T5 ON T5.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT JOIN SCHOOL_MST SCHM ON SCHM.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "            AND SCHM.SCHOOL_KIND = '".$model->getSchoolKind."' ";
        }
        $query .= "        LEFT JOIN ASSESS_MST ASSESSM ON ASSESSM.ASSESSCD = '4' ";
        $query .= "            AND T5.VAL_5ALL BETWEEN ASSESSLOW AND ASSESSHIGH ";
        $query .= "        LEFT JOIN T_CREDIT T6 ON T6.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ";

        return $query;
    }


    //学年取得
    function getSelectAnnual($model) {
        $query  = "";
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT ";
        $query .= "     REGD.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "       ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER ."' ";
        $query .= "     AND REGD.GRADE||REGD.HR_CLASS IN ('" . $model->selectdata . "') ";
        $query .= " ) ";
        $query .= " , MAIN AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     , T2.SCHOOL_KIND ";
        }
        $query .= "     , T1.ANNUAL ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.GRADE = T2.GRADE ";
        $query .= "   WHERE ";
        $query .= "     SCHREGNO IN (SELECT * FROM SCHREG) ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   , SCHOOL_KIND ";
        }
        $query .= "   , ANNUAL ";
        $query .= " FROM ";
        $query .= "   MAIN ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "   SCHOOL_KIND IN ( ";
            $query .= "     SELECT SCHOOL_KIND ";
            $query .= "     FROM MAIN ";
            $query .= "     WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "   ) ";
        }
        $query .= " ORDER BY ";
        $query .= "   YEAR ";

        return $query;
    }

    //通知表（出欠情報）
    function getAttendSemes($year, $schregNo) {

        $query  = " SELECT ";
        $query .= "   YEAR ";
        $query .= "   , SCHREGNO ";
        $query .= "   , VALUE(SUM(SICK), 0) + VALUE(SUM(NOTICE), 0) + VALUE(SUM(NONOTICE), 0) SICK ";
        $query .= "   , SUM(SUSPEND) SUSPEND ";
        $query .= "   , VALUE(SUM(LATE), 0) + VALUE(SUM(EARLY), 0) LATE_EARLY ";
        $query .= " FROM ";
        $query .= "   ATTEND_SEMES_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";
        $query .= "   AND SCHREGNO = '".$schregNo."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR ";
        $query .= "   , SCHREGNO ";

        return $query;
    }
    //調査書・要録（出欠情報）
    function getSchregAttendDat($year, $schregNo) {

        $query  = " WITH ATTENDREC AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= "     , VALUE (SUM(SICK), 0) + VALUE (SUM(ACCIDENTNOTICE), 0) + VALUE (SUM(NOACCIDENTNOTICE), 0) SICK ";
        $query .= "     , SUM(SUSPEND) SUSPEND ";
        $query .= "   FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";
        $query .= "   GROUP BY ";
        $query .= "     YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= " ) ";
        $query .= " , ATTEND_SEMES AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= "     , VALUE (SUM(LATE), 0) + VALUE (SUM(EARLY), 0) LATE_EARLY ";
        $query .= "   FROM ";
        $query .= "     ATTEND_SEMES_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHREGNO = '".$schregNo."' ";
        $query .= "   GROUP BY ";
        $query .= "     YEAR ";
        $query .= "     , SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   REC.YEAR ";
        $query .= "   , REC.SCHREGNO ";
        $query .= "   , VALUE(REC.SICK, 0) SICK ";
        $query .= "   , VALUE(REC.SUSPEND, 0) SUSPEND ";
        $query .= "   , VALUE(SEM.LATE_EARLY, 0) LATE_EARLY ";
        $query .= " FROM ";
        $query .= "   ATTENDREC REC ";
        $query .= "   LEFT JOIN ATTEND_SEMES SEM ";
        $query .= "     ON REC.YEAR = SEM.YEAR ";
        $query .= "     AND REC.SCHREGNO = SEM.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   REC.YEAR = '".$year."' ";
        $query .= "   AND REC.SCHREGNO = '".$schregNo."' ";

        return $query;
    }


    //CVS作成用のQUERY（科目別:対象学期までの累計）NO001
    function getAttendSubclassDat($model, $year, $schregNo) {

        $virus   = ($model->Properties["useVirus"] == 'true') ? true : false;
        $koudome = ($model->Properties["useKoudome"] == 'true') ? true : false;

        $absent_cov = $knjSchoolMst['ABSENT_COV'];
        $absent_cov_late = $knjSchoolMst['ABSENT_COV_LATE'];

        $query = "";
        $query .= " WITH ATTEND_SUM AS( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT W1.SCHREGNO,W1.CLASSCD,W1.SCHOOL_KIND,W1.CURRICULUM_CD,W1.SUBCLASSCD,W1.SEMESTER, ";
        } else {
            $query .= "    SELECT W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER, ";
        }
        $query .= "           SUM(W1.LESSON) LESSON, ";
        $query .= "           SUM(W1.OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(W1.ABROAD) ABROAD, ";
        $query .= "           SUM(W1.ABSENT) ABSENT, ";
        $query .= "           SUM(W1.SUSPEND) SUSPEND, ";
        $query .= "           SUM(W1.MOURNING) MOURNING, ";
        $query .= "           SUM(W1.SICK) SICK, ";
        if ($virus) {
            $query .= "           SUM(W1.VIRUS) VIRUS, ";
        }
        if ($koudome) {
            $query .= "           SUM(W1.KOUDOME) KOUDOME, ";
        }
        $query .= "           SUM(W1.NOTICE) NOTICE, ";
        $query .= "           SUM(W1.NONOTICE) NONOTICE, ";
        $query .= "           SUM(W1.NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(W1.LATE) LATE, ";
        $query .= "           SUM(W1.EARLY) EARLY ";
        $query .= "    FROM   ATTEND_SUBCLASS_DAT W1 ";
        $query .= "    WHERE  W1.YEAR = '".$year."' ";
        $query .= "      AND  W1.SCHREGNO = '".$schregNo."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY W1.SCHREGNO,W1.CLASSCD,W1.SCHOOL_KIND,W1.CURRICULUM_CD,W1.SUBCLASSCD,W1.SEMESTER ";
        } else {
            $query .= "    GROUP BY W1.SCHREGNO,W1.SUBCLASSCD,W1.SEMESTER ";
        }

        $query .= " ) ";
        //学期毎に清算
        $query .= ", ATTEND_SUM2 AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEMESTER, ";
        } else {
            $query .= "    SELECT SCHREGNO,SUBCLASSCD,SEMESTER, ";
        }
        $query .= "           SUM(LESSON) LESSON, ";
        $query .= "           SUM(OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(ABROAD) ABROAD, ";
        $query .= "           SUM(ABSENT) ABSENT, ";
        $query .= "           SUM(SUSPEND) SUSPEND, ";
        $query .= "           SUM(MOURNING) MOURNING, ";
        $query .= "           SUM(SICK) SICK, ";
        if ($virus) {
            $query .= "           SUM(VIRUS) VIRUS, ";
        }
        if ($koudome) {
            $query .= "           SUM(KOUDOME) KOUDOME, ";
        }
        $query .= "           SUM(NOTICE) NOTICE, ";
        $query .= "           SUM(NONOTICE) NONOTICE, ";
        $query .= "           SUM(NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(LATE) LATE, ";
        $query .= "           SUM(EARLY) EARLY ";

        $tmpSql = "";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $tmpSql .= " + VALUE(SUM(OFFDAYS), 0) ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $tmpSql .= " + VALUE(SUM(SUSPEND), 0) ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $tmpSql .= " + VALUE(SUM(VIRUS), 0) ";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $tmpSql .= " + VALUE(SUM(KOUDOME), 0) ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $tmpSql .= " + VALUE(SUM(MOURNING), 0) ";
        }
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $tmpSql .= " + VALUE(SUM(ABSENT), 0) ";
        }

        if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(SUM(LATE) + SUM(EARLY)) / ".$absent_cov_late.") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) + SUM(NURSEOFF) ".$tmpSql;
            $query .= "      ), 4, 1) AS NOTICE_LATE ";
        } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((SUM(LATE) + SUM(EARLY)) / ".$absent_cov_late.") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) + SUM(NURSEOFF) ".$tmpSql;
            $query .= "      ) AS NOTICE_LATE ";
        } else {
            $query .= "      ,value(SUM(NOTICE),0) + value(SUM(NONOTICE),0) + value(SUM(SICK),0) + value(SUM(NURSEOFF),0) ".$tmpSql;
            $query .= "      AS NOTICE_LATE ";
        }

        $query .= "    FROM   ATTEND_SUM ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEMESTER ";
        } else {
            $query .= "    GROUP BY SCHREGNO,SUBCLASSCD,SEMESTER ";
        }
        $query .= " ) ";
        //年間で清算
        $query .= ", ATTEND_SUM3 AS ( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD, ";
        } else {
            $query .= "    SELECT SCHREGNO,SUBCLASSCD, ";
        }
        $query .= "           SUM(LESSON) LESSON, ";
        $query .= "           SUM(ABSENT) ABSENT, ";
        if ($virus) {
            if ($koudome) {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ";
                $query .= "                WHEN SUM(VIRUS) IS NULL AND SUM(KOUDOME) IS NOT NULL THEN SUM(SUSPEND) + SUM(KOUDOME) ";
                $query .= "                WHEN SUM(VIRUS) IS NOT NULL AND SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) + SUM(VIRUS) ";
                $query .= "                ELSE SUM(SUSPEND) + SUM(VIRUS) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           CASE WHEN SUM(VIRUS) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(VIRUS) END AS SUSPEND, ";
            }
        } else {
            if ($koudome) {
                $query .= "           CASE WHEN SUM(KOUDOME) IS NULL THEN SUM(SUSPEND) ELSE SUM(SUSPEND) + SUM(KOUDOME) END AS SUSPEND, ";
            } else {
                $query .= "           SUM(SUSPEND) SUSPEND, ";
            }
        }
        $query .= "           SUM(MOURNING) MOURNING, ";
        $query .= "           SUM(OFFDAYS) OFFDAYS, ";
        $query .= "           SUM(ABROAD) ABROAD, ";
        $query .= "           SUM(SICK) SICK, ";
        $query .= "           SUM(NOTICE) NOTICE, ";
        $query .= "           SUM(NONOTICE) NONOTICE, ";
        $query .= "           SUM(NURSEOFF) NURSEOFF, ";
        $query .= "           SUM(LATE) LATE, ";
        $query .= "           SUM(EARLY) EARLY ";
        $query .= "          ,SUM(NOTICE_LATE) NOTICE_LATE1 ";

        $tmpSql2 = "";
        if ($knjSchoolMst["SUB_OFFDAYS"] == "1") {
            $tmpSql2 .= " + VALUE(SUM(OFFDAYS), 0) ";
        }
        if ($knjSchoolMst["SUB_SUSPEND"] == "1") {
            $tmpSql2 .= " + VALUE(SUM(SUSPEND), 0) ";
        }
        if ($knjSchoolMst["SUB_VIRUS"] == "1") {
            $tmpSql2 .= " + VALUE(SUM(VIRUS), 0) ";
        }
        if ($knjSchoolMst["SUB_KOUDOME"] == "1") {
            $tmpSql2 .= " + VALUE(SUM(KOUDOME), 0) ";
        }
        if ($knjSchoolMst["SUB_MOURNING"] == "1") {
            $tmpSql2 .= " + VALUE(SUM(MOURNING), 0) ";
        }
        if ($knjSchoolMst["SUB_ABSENT"] == "1") {
            $tmpSql2 .= " + VALUE(SUM(ABSENT), 0) ";
        }

        if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,decimal((float(SUM(LATE) + SUM(EARLY)) / ".$absent_cov_late.") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) + SUM(NURSEOFF) ".$tmpSql2;
            $query .= "      ), 4, 1) AS NOTICE_LATE2 ";
        } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "      ,((SUM(LATE) + SUM(EARLY)) / ".$absent_cov_late.") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) + SUM(NURSEOFF) ".$tmpSql2;
            $query .= "      ) AS NOTICE_LATE2 ";
        } else {
            $query .= "      ,VALUE(SUM(NOTICE),0) + VALUE(SUM(NONOTICE),0) + VALUE(SUM(SICK),0) + VALUE(SUM(NURSEOFF),0) ".$tmpSql2;
            $query .= "      AS NOTICE_LATE2 ";
        }
        $query .= "    FROM ATTEND_SUM2 ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD ";
        } else {
            $query .= "    GROUP BY SCHREGNO,SUBCLASSCD ";
        }
        $query .= " ) ";

        //メイン
        $query .= "SELECT T3.SCHREGNO ";

        $noticeLate = ($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0) ? "NOTICE_LATE1" : "NOTICE_LATE2" ;
        $query .= "   , SUM(".$noticeLate.") AS NOTICE_LATE ";
        $query .= " FROM ATTEND_SUM3 T3 ";
        $query .= " WHERE  T3.SCHREGNO = '".$schregNo."'";
        $query .= " GROUP BY T3.SCHREGNO ";

        return $query;
    }


    //部活動取得
    function getClub($model, $year, $schregNo) {
        $query = "";

        $query .= " WITH SEMES AS ( ";
        $query .= "   SELECT ";
        $query .= "     MIN(SDATE) SDATE ";
        $query .= "     , MAX(EDATE) EDATE ";
        $query .= "   FROM SEMESTER_MST ";
        $query .= "   WHERE YEAR = '".$year."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T2.CLUBNAME AS CLUB_SHOW, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 AS EXECUTIVE_SHOW, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     CLUB_MST T2 ON  T1.CLUBCD = T2.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        }
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " , SEMES ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregNo."' AND ";
        $query .= "     ( ";
        $query .= "         (T1.SDATE                      <= SEMES.SDATE AND VALUE(T1.EDATE, '9999-12-31') >= SEMES.EDATE) OR ";
        $query .= "         (T1.SDATE                      >= SEMES.SDATE AND T1.SDATE                      <= SEMES.EDATE) OR ";
        $query .= "         (VALUE(T1.EDATE, '9999-12-31') >= SEMES.SDATE AND VALUE(T1.EDATE, '9999-12-31') <= SEMES.EDATE) ";
        $query .= "     ) ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND IN ( SELECT ";
            $query .= "                             S2.SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_DAT S1, ";
            $query .= "                             SCHREG_REGD_GDAT S2 ";
            $query .= "                         WHERE ";
            $query .= "                             S1.YEAR     = S2.YEAR AND ";
            $query .= "                             S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                             S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                             S1.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "                             S1.GRADE    = S2.GRADE ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model, $year, $schregNo) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L3.NAME1 AS SEMESTER_SHOW, ";
        $query .= "     T1.SEQ, ";
        $query .= "     L1.COMMITTEENAME AS COMMITTEE_SHOW, ";
        $query .= "     T1.CHARGENAME AS CHARGE_SHOW, ";
        $query .= "     L2.NAME1 AS EXECUTIVE_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR         = G1.YEAR ";
        $query .= "         AND T1.GRADE        = G1.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND  = G1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN COMMITTEE_MST L1 ";
        $query .= "          ON T1.COMMITTEE_FLG    = L1.COMMITTEE_FLG ";
        $query .= "         AND T1.COMMITTEECD      = L1.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD         = L1.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1      = 'J002' ";
        $query .= "         AND T1.EXECUTIVECD  = L2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L3 ";
        $query .= "          ON L3.NAMECD1      = 'J004' ";
        $query .= "         AND T1.SEMESTER     = L3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SCHREGNO     = '".$schregNo."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //大会記録
    function getSchregClubHdetailDat($model, $year, $schregNo) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.CLUBNAME AS CLUB_SHOW, ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     L2.HOSTNAME, ";
        $query .= "     T1.MEET_NAME, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     CASE T1.DIV WHEN '1' THEN '個人' WHEN '2' THEN '団体' ELSE '' END AS DIV_NAME, ";
        $query .= "     T1.RECORDCD, ";
        $query .= "     L3.RECORDNAME, ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     T1.KINDCD, ";
        $query .= "     L4.KINDNAME, ";
        $query .= "     T1.DOCUMENT, ";
        $query .= "     T1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST L1 ";
        $query .= "          ON T1.CLUBCD       = L1.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L1.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_HOST_MST L2 ";
        $query .= "          ON T1.HOSTCD       = L2.HOSTCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L2.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L2.SCHOOL_KIND ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L2.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L2.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_RECORD_MST L3 ";
        $query .= "          ON T1.RECORDCD     = L3.RECORDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L3.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L3.SCHOOL_KIND ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L3.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L3.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_ITEM_KIND_MST L4 ";
        $query .= "          ON T1.ITEMCD       = L4.ITEMCD ";
        $query .= "         AND T1.KINDCD       = L4.KINDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L4.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L4.SCHOOL_KIND ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L4.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L4.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregNo."' ";
        $query .= " AND FISCALYEAR(T1.DETAIL_DATE) = '".$year."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND IN ( SELECT ";
            $query .= "                             S2.SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_DAT S1, ";
            $query .= "                             SCHREG_REGD_GDAT S2 ";
            $query .= "                         WHERE ";
            $query .= "                             S1.YEAR     = S2.YEAR AND ";
            $query .= "                             S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                             S1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "                             S1.GRADE    = S2.GRADE AND ";
            $query .= "                             S1.SCHREGNO = '".$schregNo."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     T1.DETAIL_DATE DESC, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }

    //学籍資格データよりデータを取得
    function getAward($model, $db, $year, $schregNo) {
        $query  = "";
        if ($model->Properties["useQualifiedMst"] == '1') {
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE L1.CONDITION_DIV ";
            $query .= "          WHEN '1' ";
            $query .= "          THEN '国家資格' ";
            $query .= "          WHEN '2' ";
            $query .= "          THEN '公的資格' ";
            $query .= "          WHEN '3' ";
            $query .= "          THEN '民間資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";
            $query .= "     L1.QUALIFIED_ABBV, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";
            $query .= "     T1.SCORE, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            if (0 < $db->getOne(knje131tQuery::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " INNER JOIN ";
                $query .= "     QUALIFIED_RANK_DAT L3 ON L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "                          AND L3.RANK = T1.RANK ";
                $query .= "                          AND L3.NOT_PRINT IS NULL ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$year}' AND ";
            $query .= "     T1.SCHREGNO = '{$schregNo}' ";
            if ($model->Properties["useSchregQualified"] == 'SUBCLASS_QUALIFIED_TEST_DAT') {
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     0 AS SEQ, ";
                $query .= "     T1.TEST_DATE AS REGDDATE, ";
                $query .= "     '00-00-00-000000' AS SUBCLASSCD, ";
                $query .= "     CASE L1.CONDITION_DIV ";
                $query .= "          WHEN '1' ";
                $query .= "          THEN '国家資格' ";
                $query .= "          WHEN '2' ";
                $query .= "          THEN '公的資格' ";
                $query .= "          WHEN '3' ";
                $query .= "          THEN '民間資格' ";
                $query .= "          ELSE 'その他' ";
                $query .= "     END AS CONDITION_DIV, ";
                $query .= "     T1.QUALIFIED_CD, ";
                $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";
                $query .= "     L1.QUALIFIED_ABBV, ";
                $query .= "     L1.PROMOTER, ";
                $query .= "     '' AS CONTENTS, ";
                $query .= "     L2.TEST_NAME AS RANK, ";
                $query .= "     NULL AS SCORE, ";
                $query .= "     N1.NAME1 AS REMARK, ";
                $query .= "     0 AS CREDITS ";
                $query .= " FROM ";
                $query .= "     SCHREG_QUALIFIED_TEST_DAT T1 ";
                $query .= "     LEFT JOIN QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "     LEFT JOIN QUALIFIED_TEST_MST L2 ";
                $query .= "          ON L2.YEAR         = T1.YEAR ";
                $query .= "         AND L2.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "         AND L2.TEST_CD      = T1.TEST_CD ";
                $query .= "     LEFT JOIN NAME_MST N1 ";
                $query .= "          ON N1.NAMECD1      = 'Z050' ";
                $query .= "         AND SUBSTR(T1.RESULT_CD, 4) = N1.NAMECD2 ";
                if (0 < $db->getOne(knje131tQuery::getSyscatColumns("QUALIFIED_RESULT_MST"))) {
                    $query .= " INNER JOIN ";
                    $query .= "     QUALIFIED_RESULT_MST L3 ON L3.YEAR = T1.YEAR ";
                    $query .= "                            AND L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                    $query .= "                            AND L3.RESULT_CD = T1.RESULT_CD ";
                    $query .= "                            AND L3.NOT_PRINT IS NULL ";
                }
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = '{$year}' AND ";
                $query .= "     T1.SCHREGNO = '{$schregNo}' AND ";
                $query .= "     T1.RESULT_CD NOT IN ('8888', '9999') "; //欠席・不合格を除く
            }
            $query .= " ORDER BY ";
            $query .= "     REGDDATE, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SEQ ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "          THEN T1.CONDITION_DIV || ':資格' ";
            $query .= "          ELSE T1.CONDITION_DIV || ':その他' ";
            $query .= "          END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     T1.SCORE, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                   AND T4.YEAR     = T1.YEAR ";
            $query .= "                                   AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "       LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.YEAR ";
            $query .= "                              AND T5.COURSECD   = T4.COURSECD ";
            $query .= "                              AND T5.MAJORCD    = T4.MAJORCD ";
            $query .= "                              AND T5.GRADE      = T4.GRADE ";
            $query .= "                              AND T5.COURSECODE = T4.COURSECODE ";
            $query .= "                              AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T5.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                          AND T5.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            if (0 < $db->getOne(knje131tQuery::getSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " INNER JOIN ";
                $query .= "     QUALIFIED_RANK_DAT L3 ON L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "                          AND L3.RANK = T1.RANK ";
                $query .= "                          AND L3.NOT_PRINT IS NULL ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$year}' AND ";
            $query .= "     T1.SCHREGNO = '{$schregNo}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }

    //NOT_PRINTがテーブルにあるかどうか取得
    function getSyscatColumns($tableName) {
        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '".$tableName."' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }

    //賞データ取得
    function getHyosyo($year, $schregNo) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     DETAIL_SDATE, ";
        $query .= "     DETAILCD, ";
        $query .= "     L1.NAME1 AS DETAILCDNAME, ";
        $query .= "     CONTENT AS CONTENT_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H303' AND L1.NAMECD2 = T1.DETAILCD ";
        $query .= " WHERE ";
        $query .= "      SCHREGNO     = '".$schregNo."' ";
        $query .= " AND  DETAIL_DIV   = '1' ";
        $query .= " AND  FISCALYEAR(DETAIL_SDATE) = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     DETAIL_SDATE ";

        return $query;
    }


}
?>
