<?php

require_once('for_php7.php');

class knjd219tQuery extends Query {

    //左画面の項目
    //学期
    function getSemester($setSemeter="") {
        $query  = " SELECT ";
        if ($setSemeter == "") {
            $query .= "     SEMESTER AS VALUE, ";
        }
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($setSemeter) {
            $query .= "     AND SEMESTER = '{$setSemeter}' ";
        }
        if (!$setSemeter) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //学年取得
    function getGrade($setGrade="") {
        $query  = " SELECT ";
        if ($setGrade == "") {
            $query .= "     GRADE AS VALUE, ";
        }
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND IN ('H') ";
        if ($setGrade) {
            $query .= "     AND GRADE = '{$setGrade}' ";
        }
        if (!$setGrade) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        
        return $query;
    }

    //学校校種を取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        
        return $query;
    }

    //コースグループを取得する
    function getCourseGroup($model, $set_groupcd="", $flg="") {
        $query  = " WITH SCHREG_DATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     GRADE, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] === '9') {
            $setSemester = CTRL_SEMESTER;
        } else {
            $setSemester = $model->field["SEMESTER"];
        }
        $query .= " AND SEMESTER = '".$setSemester."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        if ($flg) {
            $query .= "     L2.GROUP_NAME AS LABEL ";
        } else {
            $query .= "     T1.GROUP_CD AS VALUE, ";
            $query .= "     T1.GROUP_CD || ':' || L2.GROUP_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_DATA L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                              AND L1.COURSECD = T1.COURSECD ";
        $query .= "                              AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "                              AND L1.COURSECODE = T1.COURSECODE ";
        $query .= "                              AND L1.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_HDAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                      AND L2.GRADE = T1.GRADE ";
        $query .= "                                      AND L2.GROUP_CD = T1.GROUP_CD ";
        if ($set_groupcd) {
            $query .= " WHERE ";
            $query .= "     T1.GROUP_CD = '".$set_groupcd."' ";
        }
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //コースを表示(中学校用)
    function getCourseMajor($model, $set_cd="", $flg="") {
        $query  = " WITH SCHREG_DATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] === '9') {
            $setSemester = CTRL_SEMESTER;
        } else {
            $setSemester = $model->field["SEMESTER"];
        }
        $query .= " AND SEMESTER = '".$setSemester."' ";
        $query .= " AND GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        if ($flg) {
            $query .= "     L1.COURSENAME || L1.MAJORNAME || '　' || L2.COURSECODENAME AS LABEL ";
        } else {
            $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
            $query .= "     T1.COURSECD || T1.MAJORCD || '-' || T1.COURSECODE || ':' || L1.COURSENAME || L1.MAJORNAME || '　' || L2.COURSECODENAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                    AND L1.COURSECD = T1.COURSECD ";
        $query .= "                                    AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                  AND L2.COURSECODE = T1.COURSECODE ";
        if ($set_groupcd) {
            $query .= " WHERE ";
            $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '".$set_cd."' ";
        }
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //テスト種別
    function getTestitem($model, $set_testItemcd="", $flg="") {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     L1.TESTITEMNAME AS LABEL ";
        } else {
            $query .= "     T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV AS VALUE, ";
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.TESTITEMNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " AND T1.SCORE_DIV = '08' ";
        $query .= " AND T1.CLASSCD = '00' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= " AND T1.CURRICULUM_CD = '00' ";
        $query .= " AND T1.SUBCLASSCD = '000000' ";
        if ($set_testItemcd) {
            $query .= " AND T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '".$set_testItemcd."' ";
        }
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //全体の一覧の取得（左画面）
    function selectQuery($model, $flg="") {
        $query  = " WITH MAIN_DATA AS ( ";
        
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV AS TESTKIND_ITEMCD, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SET_SUBCLASSCD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.RUISEKI_DIV, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS COURSE_MAJOR_CODE, ";
        $query .= "     N0.GRADE_NAME1, ";
        $query .= "     CASE WHEN T1.RUISEKI_DIV = '1' THEN '考査別' ELSE '累積別' END AS RUISEKI_DIV_NAME, ";
        if ($model->schoolkind === 'H') {
            $query .= "     N1.GROUP_NAME AS SET_NAME, ";
        } else {
            $query .= "     N2.COURSENAME || N2.MAJORNAME || '　' || N3.COURSECODENAME AS SET_NAME, ";
        }
        $query .= "     N4.TESTITEMNAME, ";
        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            $query .= "     L$i.ASSESSLEVEL AS ASSESSLEVEL_$i, ";
            $query .= "     L$i.ASSESSLOW AS ASSESSLOW_$i, ";
            $query .= "     L$i.ASSESSHIGH AS ASSESSHIGH_$i, ";
        }
        $query .= "     L0.SUBCLASSNAME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || L0.SUBCLASSNAME AS SET_SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_SDIV_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L0 ON L0.CLASSCD = T1.CLASSCD ";
        $query .= "                              AND L0.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                              AND L0.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                              AND L0.SUBCLASSCD = T1.SUBCLASSCD ";
        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            $query .= "     LEFT JOIN ASSESS_LEVEL_SDIV_MST L$i ON L$i.YEAR           = T1.YEAR ";
            $query .= "                                        AND L$i.SEMESTER       = T1.SEMESTER ";
            $query .= "                                        AND L$i.TESTKINDCD     = T1.TESTKINDCD ";
            $query .= "                                        AND L$i.TESTITEMCD     = T1.TESTITEMCD ";
            $query .= "                                        AND L$i.CLASSCD        = T1.CLASSCD ";
            $query .= "                                        AND L$i.SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "                                        AND L$i.CURRICULUM_CD  = T1.CURRICULUM_CD ";
            $query .= "                                        AND L$i.SUBCLASSCD     = T1.SUBCLASSCD ";
            $query .= "                                        AND L$i.RUISEKI_DIV    = T1.RUISEKI_DIV ";
            $query .= "                                        AND L$i.DIV            = T1.DIV ";
            $query .= "                                        AND L$i.GRADE          = T1.GRADE ";
            $query .= "                                        AND L$i.HR_CLASS       = T1.HR_CLASS ";
            $query .= "                                        AND L$i.COURSECD       = T1.COURSECD ";
            $query .= "                                        AND L$i.MAJORCD        = T1.MAJORCD ";
            $query .= "                                        AND L$i.COURSECODE     = T1.COURSECODE ";
            $query .= "                                        AND L$i.ASSESSLEVEL    = $i ";
        }
        //学年名
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT N0 ON N0.YEAR = T1.YEAR ";
        $query .= "                                  AND N0.GRADE = T1.GRADE ";
        //コースまたはコースグループ名
        if ($model->schoolkind === 'H') {
            $query .= "     LEFT JOIN COURSE_GROUP_CD_HDAT N1 ON N1.YEAR = T1.YEAR ";
            $query .= "                                      AND N1.GRADE = T1.GRADE ";
            $query .= "                                      AND N1.GROUP_CD = T1.MAJORCD ";
        } else {
            $query .= "     LEFT JOIN V_COURSE_MAJOR_MST N2 ON N2.YEAR = T1.YEAR ";
            $query .= "                                    AND N2.COURSECD = T1.COURSECD ";
            $query .= "                                    AND N2.MAJORCD = T1.MAJORCD ";
            $query .= "     LEFT JOIN V_COURSECODE_MST N3 ON N3.YEAR = T1.YEAR ";
            $query .= "                                  AND N3.COURSECODE = T1.COURSECODE ";
        }
        //テスト種別名
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV N4 ON T1.YEAR = N4.YEAR ";
        $query .= "                                                AND T1.SEMESTER = N4.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = N4.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = N4.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = N4.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        //一覧表示の時
        if ($flg == "") {
            $query .= " AND T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '".$model->field["TESTKIND_ITEMCD"]."' ";
            $query .= " AND T1.RUISEKI_DIV = '".$model->field["RUISEKI_DIV"]."' ";
            $query .= " AND T1.GRADE = '".$model->field["GRADE"]."' ";
            if ($model->schoolkind === 'H') {
                $query .= " AND T1.COURSECD = '0' ";//固定
                $query .= " AND T1.MAJORCD = '".$model->field["GROUP_CD"]."' ";//コースグループをセット
                $query .= " AND T1.COURSECODE = '0000' ";//固定
            } else {
                $query .= " AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '".$model->field["COURSE_MAJOR_CODE"]."' ";
            }
        }
        $query .= " AND T1.HR_CLASS = '000' ";//固定
        if ($model->schoolkind === 'H') {
            $query .= " AND T1.DIV = '5' ";//コースグループの設定値
        } else {
            $query .= " AND T1.DIV = '3' ";//コースの設定値
        }
        //制限付き権限
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.CLASSCD || '-' || T1.SCHOOL_KIND IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 || '-' ||  FIELD3 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '1050' ";
            $query .= "             ) ";
            $query .= "       OR (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) IN (SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD  ";
            $query .= "              FROM ";
            $query .= "                  CHAIR_STF_DAT T1 ";
            $query .= "                  INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND T1.SEMESTER <= '".$model->field["SEMESTER"]."' ";
            $query .= "                  AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "             )) ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_DATA ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     RUISEKI_DIV, ";
        $query .= "     TESTKIND_ITEMCD, ";
        $query .= "     COURSE_MAJOR_CODE, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CLASSCD, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }    

    //右画面の項目
    //科目コンボ
    function getSubclass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SCHOOL_KIND = '".$model->schoolkind."' ";
        if ($model->div == '5' && $model->grade != '' && $model->coursecd != '' && $model->groupcd != '' && $model->coursecode != '') {
            $query .= " AND EXISTS (SELECT 'X' FROM  ";
            $query .= "              CREDIT_MST L1 ";
            $query .= "              INNER JOIN COURSE_GROUP_CD_DAT L2 ON L1.YEAR = L2.YEAR ";
            $query .= "                AND L1.GRADE = L2.GRADE ";
            $query .= "                AND L2.GROUP_CD = '".$model->groupcd."' ";
            $query .= "                AND L1.COURSECD = L2.COURSECD ";
            $query .= "                AND L1.MAJORCD = L2.MAJORCD ";
            $query .= "                AND L1.COURSECODE = L2.COURSECODE ";
            $query .= "              WHERE L1.YEAR = '".CTRL_YEAR."' ";
            $query .= "                AND L1.GRADE = '".$model->grade."' ";
            $query .= "                AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "            ) ";
        } else if ($model->div == '3' && $model->grade != '' && $model->coursecd != '' && $model->majorcd != '' && $model->coursecode != '') {
            $query .= " AND EXISTS (SELECT 'X' FROM  ";
            $query .= "              CREDIT_MST L1 ";
            $query .= "              WHERE L1.YEAR = '".CTRL_YEAR."' ";
            $query .= "                AND L1.GRADE = '".$model->grade."' ";
            $query .= "                AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                AND L1.COURSECD = '".$model->coursecd."' ";
            $query .= "                AND L1.MAJORCD = '".$model->majorcd."' ";
            $query .= "                AND L1.COURSECODE = '".$model->coursecode."' ";
        }
        //制限付き権限
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.CLASSCD || '-' || T1.SCHOOL_KIND IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 || '-' ||  FIELD3 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = ST.YEAR ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '1050' ";
            $query .= "             ) ";
            $query .= "       OR (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) IN (SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD  ";
            $query .= "              FROM ";
            $query .= "                  CHAIR_STF_DAT T1 ";
            $query .= "                  INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "              WHERE ";
            $query .= "                  T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND T1.SEMESTER <= '".$model->field["SEMESTER"]."' ";
            $query .= "                  AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "             )) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }    

    //満点、赤点の抽出
    //科目のテスト設定あり
    function getTestSubclassCnt($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->subclasscd);
    
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->ruiseki_div == '2') {
            $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
        }
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= "     AND T1.CLASSCD = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$subclass_array[3]}' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= "         IN (SELECT ";
        $query .= "                 I1.COURSECD || I1.MAJORCD || I1.COURSECODE ";
        $query .= "             FROM ";
        $query .= "                 COURSE_GROUP_CD_DAT I1 ";
        $query .= "             WHERE ";
        $query .= "                 I1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "                 AND I1.GRADE   = '{$model->grade}' ";
        $query .= "                 AND I1.GROUP_CD   = '{$model->groupcd}' ";
        $query .= "         ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    //テスト数
    function getTestCnt($model, $classCd, $sKind, $curriculum, $subclassCd, $groupCd) {
        list($testkind, $testitemcd, $score_div) = explode(':', $model->testkind_itemcd);
        $setSemeTest = $model->semester.$testkind.$testitemcd;

        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     SEMESTER, ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_TESTITEM_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->ruiseki_div == '2') {
            $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
        }
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE ";
        $query .= "         IN (SELECT ";
        $query .= "                 I1.COURSECD || I1.MAJORCD || I1.COURSECODE ";
        $query .= "             FROM ";
        $query .= "                 COURSE_GROUP_CD_DAT I1 ";
        $query .= "             WHERE ";
        $query .= "                 I1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "                 AND I1.GRADE   = '{$model->grade}' ";
        $query .= "                 AND I1.GROUP_CD   = '{$groupCd}' ";
        $query .= "         ) ";
        $query .= "     AND T1.CLASSCD = '{$classCd}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$sKind}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$curriculum}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$subclassCd}' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '{$setSemeTest}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    //満点取得
    function getPerfect($model, $groupCd) {
        list($testkind, $testitemcd, $score_div) = explode(':', $model->testkind_itemcd);
        $setSemeTest = $model->semester.$testkind.$testitemcd;
        $subclass_array = array();
        $subclass_array = explode("-", $model->subclasscd);
        
        $query  = " SELECT ";
        $query .= "     SUM(T1.PERFECT) AS SUM_PERFECT, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->ruiseki_div == '2') {
            $query .= "     AND T1.SEMESTER = '".$model->semester."' ";
        }
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '{$setSemeTest}' ";
        $query .= "     AND T1.TESTKINDCD IN ('01', '02') ";
        $query .= "     AND T1.SCORE_DIV = '{$score_div}' ";
        $query .= "     AND T1.CLASSCD = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD = '{$subclass_array[3]}' ";
        $query .= "     AND T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE '{$model->grade}' END ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ";
        $query .= "         CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "              THEN '00000000' ";
        $query .= "              ELSE '0' || '{$groupCd}' || '0000' END ";

        return $query;
    }

    //赤点の算出(考査別)
    function getPassScoreLineTantai($model) {
        $query  = " SELECT ";
        $query .= "     SCORE_LINE ";
        $query .= " FROM ";
        $query .= "     PASS_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '".$model->semester."' ";
        $query .= " AND TESTKINDCD || ':' || TESTITEMCD || ':' || SCORE_DIV = '".$model->testkind_itemcd."' ";
        $query .= " AND RUISEKI_DIV = '".$model->ruiseki_div."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        if ($model->schoolkind === 'H') {
            $query .= " AND PASS_DIV = '5' ";//コースグループの設定値
            $query .= " AND GRADE = '".$model->grade."' ";
            $query .= " AND HR_CLASS = '000' ";
            $query .= " AND COURSECD = '0' ";
            $query .= " AND MAJORCD = '".$model->groupcd."' ";
            $query .= " AND COURSECODE = '0000' ";
        } else {
            $query .= " AND PASS_DIV = '3' ";//コースの設定値
            $query .= " AND GRADE = '".$model->grade."' ";
            $query .= " AND HR_CLASS = '000' ";
            $query .= " AND COURSECD = '".$model->coursecd."' ";
            $query .= " AND MAJORCD = '".$model->majorcd."' ";
            $query .= " AND COURSECODE = '".$model->coursecode."' ";
        }

        return $query;
    }
    
    //赤点の算出(累積)
    function getPassScoreLineRuiseki($model) {
        list($testkind, $testitemcd, $score_div) = explode(':', $model->testkind_itemcd);
        $setSemeTest = $model->semester.$testkind.$testitemcd;
    
        $query  = " WITH T_COUNT AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     PASS_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->ruiseki_div == '2') {
            $query .= "     AND SEMESTER = '".$model->semester."' ";
        }
        $query .= " AND SEMESTER || TESTKINDCD || TESTITEMCD <= '{$setSemeTest}' ";
        $query .= " AND TESTKINDCD IN ('01', '02') ";
        $query .= " AND SCORE_DIV = '{$score_div}' ";
        $query .= " AND RUISEKI_DIV = '".$model->ruiseki_div."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        if ($model->schoolkind === 'H') {
            $query .= " AND PASS_DIV = '5' ";//コースグループの設定値
            $query .= " AND GRADE = '".$model->grade."' ";
            $query .= " AND HR_CLASS = '000' ";
            $query .= " AND COURSECD = '0' ";
            $query .= " AND MAJORCD = '".$model->groupcd."' ";
            $query .= " AND COURSECODE = '0000' ";
        } else {
            $query .= " AND PASS_DIV = '3' ";//コースの設定値
            $query .= " AND GRADE = '".$model->grade."' ";
            $query .= " AND HR_CLASS = '000' ";
            $query .= " AND COURSECD = '".$model->coursecd."' ";
            $query .= " AND MAJORCD = '".$model->majorcd."' ";
            $query .= " AND COURSECODE = '".$model->coursecode."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     SCORE_LINE AS SUM_SCORE_LINE, ";
        $query .= "     T2.CNT ";
        $query .= " FROM ";
        $query .= "     PASS_SCORE_DAT ";
        $query .= "     , T_COUNT T2 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER || TESTKINDCD || TESTITEMCD = '{$setSemeTest}' ";
        $query .= " AND SCORE_DIV = '{$score_div}' ";
        $query .= " AND RUISEKI_DIV = '".$model->ruiseki_div."' ";
        $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        if ($model->schoolkind === 'H') {
            $query .= " AND PASS_DIV = '5' ";//コースグループの設定値
            $query .= " AND GRADE = '".$model->grade."' ";
            $query .= " AND HR_CLASS = '000' ";
            $query .= " AND COURSECD = '0' ";
            $query .= " AND MAJORCD = '".$model->groupcd."' ";
            $query .= " AND COURSECODE = '0000' ";
        } else {
            $query .= " AND PASS_DIV = '3' ";//コースの設定値
            $query .= " AND GRADE = '".$model->grade."' ";
            $query .= " AND HR_CLASS = '000' ";
            $query .= " AND COURSECD = '".$model->coursecd."' ";
            $query .= " AND MAJORCD = '".$model->majorcd."' ";
            $query .= " AND COURSECODE = '".$model->coursecode."' ";
        }

        return $query;
    }

    //データの取得（右画面）
    function getRow($model) {
        $query  = " SELECT DISTINCT ";
        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            $query .= "     L$i.ASSESSLEVEL AS ASSESSLEVEL_$i, ";
            $query .= "     L$i.ASSESSLOW AS ASSESSLOW_$i, ";
            $query .= "     L$i.ASSESSHIGH AS ASSESSHIGH_$i, ";
        }
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SET_SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_SDIV_MST T1 ";
        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            $query .= "     LEFT JOIN ASSESS_LEVEL_SDIV_MST L$i ON L$i.YEAR           = T1.YEAR ";
            $query .= "                                        AND L$i.SEMESTER       = T1.SEMESTER ";
            $query .= "                                        AND L$i.TESTKINDCD     = T1.TESTKINDCD ";
            $query .= "                                        AND L$i.TESTITEMCD     = T1.TESTITEMCD ";
            $query .= "                                        AND L$i.CLASSCD        = T1.CLASSCD ";
            $query .= "                                        AND L$i.SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "                                        AND L$i.CURRICULUM_CD  = T1.CURRICULUM_CD ";
            $query .= "                                        AND L$i.SUBCLASSCD     = T1.SUBCLASSCD ";
            $query .= "                                        AND L$i.RUISEKI_DIV    = T1.RUISEKI_DIV ";
            $query .= "                                        AND L$i.DIV            = T1.DIV ";
            $query .= "                                        AND L$i.GRADE          = T1.GRADE ";
            $query .= "                                        AND L$i.HR_CLASS       = T1.HR_CLASS ";
            $query .= "                                        AND L$i.COURSECD       = T1.COURSECD ";
            $query .= "                                        AND L$i.MAJORCD        = T1.MAJORCD ";
            $query .= "                                        AND L$i.COURSECODE     = T1.COURSECODE ";
            $query .= "                                        AND L$i.ASSESSLEVEL    = $i ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->semester."' ";
        $query .= " AND T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '".$model->testkind_itemcd."' ";
        $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->subclasscd."' ";
        $query .= " AND T1.RUISEKI_DIV = '".$model->ruiseki_div."' ";
        $query .= " AND T1.DIV = '".$model->div."' ";
        $query .= " AND T1.GRADE = '".$model->grade."' ";
        $query .= " AND T1.HR_CLASS = '".$model->hr_class."' ";
        $query .= " AND T1.COURSECD = '".$model->coursecd."' ";
        $query .= " AND T1.MAJORCD = '".$model->groupcd."' ";
        $query .= " AND T1.COURSECODE = '".$model->coursecode."' ";

        return $query;
    }

    //データ削除
    function delete($model, $db) {
        $query  = " DELETE FROM ";
        $query .= "     ASSESS_LEVEL_SDIV_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->semester."' ";
        $query .= " AND T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '".$model->testkind_itemcd."' ";
        $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->subclasscd."' ";
        $query .= " AND T1.RUISEKI_DIV = '".$model->ruiseki_div."' ";
        $query .= " AND T1.DIV = '".$model->div."' ";
        $query .= " AND T1.GRADE = '".$model->grade."' ";
        $query .= " AND T1.HR_CLASS = '".$model->hr_class."' ";
        $query .= " AND T1.COURSECD = '".$model->coursecd."' ";
        $query .= " AND T1.MAJORCD = '".$model->groupcd."' ";
        $query .= " AND T1.COURSECODE = '".$model->coursecode."' ";
        $db->query($query);
        
        return;
    }
        
    /* データ更新処理 */
    function update($model, $db) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->subclasscd);
        $test_array = array();
        $test_array = explode(":", $model->testkind_itemcd);
        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = $model->semester;
            $data["TESTKINDCD"][TEXT]    = $test_array[0];
            $data["TESTITEMCD"][TEXT]    = $test_array[1];
            $data["SCORE_DIV"][TEXT]     = $test_array[2];
            $data["CLASSCD"][TEXT]       = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
            $data["RUISEKI_DIV"][TEXT]   = $model->ruiseki_div;
            $data["DIV"][TEXT]           = $model->div;
            $data["GRADE"][TEXT]         = $model->grade;
            $data["HR_CLASS"][TEXT]      = $model->hr_class;
            $data["COURSECD"][TEXT]      = $model->coursecd;
            $data["MAJORCD"][TEXT]       = $model->groupcd;
            $data["COURSECODE"][TEXT]    = $model->coursecode;
            $data["ASSESSLEVEL"][NUMBER] = $i;
            $data["ASSESSLOW"][NUMBER]   = $model->field2["ASSESSLOW_".$i];
            $data["ASSESSHIGH"][NUMBER]  = $model->field2["ASSESSHIGH_".$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "ASSESS_LEVEL_SDIV_MST");
            $db->query($query);
        }
        return;
    }


}
?>
