<?php

require_once('for_php7.php');

class knjz182rQuery extends Query {

/*****************************↓左フレーム↓******************************/

    //テスト種別コンボ
    function getTestkindcd() {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     T2.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                                         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "                                         AND T3.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                         AND T3.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                         AND T3.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "                                         AND T3.CLASSCD = '00' ";
        $query .= "                                         AND T3.SCHOOL_KIND = 'H' ";
        $query .= "                                         AND T3.CURRICULUM_CD = '00' ";
        $query .= "                                         AND T3.SUBCLASSCD = '000000' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //リスト取得
    function getLeftList($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS COURSE, ";
        $query .= "     N0.GRADE_NAME1, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS SET_CD, ";
        if ($model->field["DIV"] === '5') {
            $query .= "     N1.GROUP_NAME AS SET_NAME, ";
        } else {
            $query .= "     N2.COURSENAME || N2.MAJORNAME || '　' || N3.COURSECODENAME AS SET_NAME, ";
        }
        $query .= "     T1.RANK_LEVEL, ";
        $query .= "     T1.RANK_MARK, ";
        $query .= "     T1.RANK_LOW, ";
        $query .= "     T1.RANK_HIGH ";
        $query .= " FROM ";
        $query .= "     ASSESS_RANK_SDIV_MST T1 ";
        //学年名
        $query .= "     INNER JOIN SCHREG_REGD_GDAT N0 ON N0.YEAR = T1.YEAR ";
        $query .= "                                   AND N0.GRADE = T1.GRADE ";
        $query .= "                                   AND N0.SCHOOL_KIND = 'H' ";
        //コースまたはコースグループ名
        if ($model->field["DIV"] === '5') {
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
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "     AND T1.DIV = '{$model->field["DIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     SET_CD, ";
        $query .= "     T1.RANK_LEVEL ";
        return $query;
    }

    //学年数取得
    function getGradeCnt($model, $grade) {
        $query  = " WITH MAIN AS ( ";
        $query .= knjz182rQuery::getLeftList($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS G_CNT ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     GRADE = '{$grade}' ";
        return $query;
    }

    //コースグループ数取得
    function getCourseGroupCnt($model, $grade, $group_cd) {
        $query  = " WITH MAIN AS ( ";
        $query .= knjz182rQuery::getLeftList($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS G_CNT ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     GRADE = '{$grade}' ";
        $query .= "     AND SET_CD = '{$group_cd}' ";
        return $query;
    }

/*****************************↓右フレーム↓******************************/

    //学年取得
    function getGrade() {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コースグループ取得
    function getGroupCd($model) {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     GROUP_CD AS VALUE, ";
        $query .= "     GROUP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //コース取得
    function getCourseMajor($model) {
        $year = CTRL_YEAR;
        $test_array = array();
        $test_array = explode(":", $model->testcd);
        $setSemester = ($test_array[0] === '9') ? CTRL_SEMESTER : $test_array[0];
        $query  = " WITH SCHREG_DATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND SEMESTER = '{$setSemester}' ";
        $query .= " AND GRADE = '{$model->grade}' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || '-' || T1.COURSECODE || ':' || L1.COURSENAME || L1.MAJORNAME || '　' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_DATA T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                    AND L1.COURSECD = T1.COURSECD ";
        $query .= "                                    AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2 ON L2.YEAR = T1.YEAR ";
        $query .= "                                  AND L2.COURSECODE = T1.COURSECODE ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //段階数
    function getAssessLevelCnt($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     COUNT(T1.RANK_LEVEL) AS ASSESSLEVELCNT ";
        $query .= " FROM ";
        $query .= "     ASSESS_RANK_SDIV_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '{$model->testcd}' ";
        $query .= "     AND T1.DIV      = '{$model->div}' ";
        $query .= "     AND T1.GRADE    = '{$model->grade}' ";
        //表示切替(1:学年 2:クラス 3:コース 4:学科 5:コースグループ)
        if ($model->div == '3') {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->course}' ";
        } else {
            $query .= "     AND T1.MAJORCD = '{$model->groupcd}' ";
        }

        return $query;
    }

    //データの取得（右画面）
    function getRow($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT DISTINCT ";
        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            $query .= "     L$i.RANK_LEVEL AS RANK_LEVEL_$i, ";
            $query .= "     L$i.RANK_MARK AS RANK_MARK_$i, ";
            $query .= "     L$i.RANK_LOW AS RANK_LOW_$i, ";
            $query .= "     L$i.RANK_HIGH AS RANK_HIGH_$i, ";
        }
        $query .= "     T1.DIV AS SET_DIV ";
        $query .= " FROM ";
        $query .= "     ASSESS_RANK_SDIV_MST T1 ";
        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            $query .= "     LEFT JOIN ASSESS_RANK_SDIV_MST L$i ON L$i.YEAR           = T1.YEAR ";
            $query .= "                                       AND L$i.SEMESTER       = T1.SEMESTER ";
            $query .= "                                       AND L$i.TESTKINDCD     = T1.TESTKINDCD ";
            $query .= "                                       AND L$i.TESTITEMCD     = T1.TESTITEMCD ";
            $query .= "                                       AND L$i.SCORE_DIV      = T1.SCORE_DIV ";
            $query .= "                                       AND L$i.DIV            = T1.DIV ";
            $query .= "                                       AND L$i.GRADE          = T1.GRADE ";
            $query .= "                                       AND L$i.HR_CLASS       = T1.HR_CLASS ";
            $query .= "                                       AND L$i.COURSECD       = T1.COURSECD ";
            $query .= "                                       AND L$i.MAJORCD        = T1.MAJORCD ";
            $query .= "                                       AND L$i.COURSECODE     = T1.COURSECODE ";
            $query .= "                                       AND L$i.RANK_LEVEL     = $i ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '{$model->testcd}' ";
        $query .= "     AND T1.DIV      = '{$model->div}' ";
        $query .= "     AND T1.GRADE    = '{$model->grade}' ";
        //表示切替(1:学年 2:クラス 3:コース 4:学科 5:コースグループ)
        if ($model->div == '3') {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->course}' ";
        } else {
            $query .= "     AND T1.MAJORCD = '{$model->groupcd}' ";
        }
        return $query;
    }

    //データ削除
    function delete($model, $db) {
        $year = CTRL_YEAR;
        $query  = " DELETE FROM ";
        $query .= "     ASSESS_RANK_SDIV_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= " AND T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '{$model->testcd}' ";
        $query .= " AND T1.DIV = '{$model->div}' ";
        $query .= " AND T1.GRADE = '{$model->grade}' ";
        $query .= " AND T1.HR_CLASS = '{$model->hr_class}' ";
        $query .= " AND T1.COURSECD = '{$model->coursecd}' ";
        $query .= " AND T1.MAJORCD = '{$model->groupcd}' ";
        $query .= " AND T1.COURSECODE = '{$model->coursecode}' ";
        $db->query($query);
        
        return;
    }

    /* データ更新処理 */
    function update($model, $db) {
        $year = CTRL_YEAR;
        $test_array = array();
        $test_array = explode(":", $model->testcd);
        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            $data = array();
            $data["YEAR"][TEXT]          = $year;
            $data["SEMESTER"][TEXT]      = $test_array[0];
            $data["TESTKINDCD"][TEXT]    = $test_array[1];
            $data["TESTITEMCD"][TEXT]    = $test_array[2];
            $data["SCORE_DIV"][TEXT]     = $test_array[3];
            $data["DIV"][TEXT]           = $model->div;
            $data["GRADE"][TEXT]         = $model->grade;
            $data["HR_CLASS"][TEXT]      = $model->hr_class;
            $data["COURSECD"][TEXT]      = $model->coursecd;
            $data["MAJORCD"][TEXT]       = $model->groupcd;
            $data["COURSECODE"][TEXT]    = $model->coursecode;
            $data["RANK_LEVEL"][NUMBER]  = $i;
            $data["RANK_MARK"][TEXT]     = $model->field2["RANK_MARK_".$i];
            $data["RANK_LOW"][NUMBER]    = $model->field2["RANK_LOW_".$i];
            $data["RANK_HIGH"][NUMBER]   = $model->field2["RANK_HIGH_".$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "ASSESS_RANK_SDIV_MST");
            $db->query($query);
        }
        return;
    }



    //コピー時の存在チェック用
    function getCntCopyQuery($model, $copycd) {
        //変数
        $year = CTRL_YEAR;
        //SQL
        $query  = " SELECT COUNT(*) FROM ";
        $query .= "     ASSESS_RANK_SDIV_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '{$copycd}' ";
        return $query;
    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;
        $test_array = array();
        $test_array = explode(":", $model->testcd);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ASSESS_RANK_SDIV_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '{$model->testcd}' ";
        $db->query($query);

        //COPY
        $query  = " INSERT INTO ASSESS_RANK_SDIV_MST ";
        $query .= " SELECT ";
        $query .= "     '{$year}', ";
        $query .= "     '{$test_array[0]}', ";
        $query .= "     '{$test_array[1]}', ";
        $query .= "     '{$test_array[2]}', ";
        $query .= "     '{$test_array[3]}', ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.RANK_LEVEL, ";
        $query .= "     T1.RANK_MARK, ";
        $query .= "     T1.RANK_LOW, ";
        $query .= "     T1.RANK_HIGH, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ASSESS_RANK_SDIV_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD || ':' || T1.SCORE_DIV = '{$model->field["PRE_TESTCD"]}' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
