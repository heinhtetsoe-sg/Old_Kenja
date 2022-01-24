<?php

require_once('for_php7.php');

class knjd234vQuery extends Query {

    //名称マスタ
    function getNameMst($model, $namecd1) {
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ
    function getZ010($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学校校種取得
    function getSchoolkindQuery($grade) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE = '".$grade."' ";

        return $query;
    }

    //テスト種別
    function getTest($model, $flg = 1) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
            $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L1 ON T1.YEAR = L1.YEAR ";
        } else {
            $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
            $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        }
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "                                                AND T1.GRADE = L1.GRADE ";
            $query .= "                                                AND T1.COURSECD = L1.COURSECD ";
            $query .= "                                                AND T1.MAJORCD = L1.MAJORCD ";
        }
        if ($flg == 1) {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR ";
            $query .= "                                   AND L2.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '{$model->semester}' ";
        $query .= "     AND T1.CLASSCD      = '00' ";
        if ($flg != 1) {
            $query .= "     AND T1.SCHOOL_KIND = '00' ";
        }
        $query .= "     AND T1.CURRICULUM_CD  = '00' ";
        $query .= "     AND T1.SUBCLASSCD  = '000000' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T1.GRADE = '00' ";
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別
    function getTestBefore($model, $flg = 1) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || VALUE(L3.SEMESTERNAME, '') || VALUE(L1.TESTITEMNAME, '') AS LABEL ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
            $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L1 ON T1.YEAR = L1.YEAR ";
        } else {
            $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
            $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        }
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "                                                AND T1.GRADE = L1.GRADE ";
            $query .= "                                                AND T1.COURSECD = L1.COURSECD ";
            $query .= "                                                AND T1.MAJORCD = L1.MAJORCD ";
        }
        $query .= "     INNER JOIN SEMESTER_MST L3 ON T1.YEAR = L3.YEAR ";
        $query .= "                               AND T1.SEMESTER = L3.SEMESTER ";
        if ($flg == 1) {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR ";
            $query .= "                                   AND L2.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV < '{$model->semester}{$model->test_cd}' ";
        $query .= "     AND T1.CLASSCD      = '00' ";
        if ($flg != 1) {
            $query .= "     AND T1.SCHOOL_KIND = '00' ";
        }
        $query .= "     AND T1.CURRICULUM_CD  = '00' ";
        $query .= "     AND T1.SUBCLASSCD  = '000000' ";
        $query .= "     AND NOT (T1.SEMESTER = '9' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '08') "; // 学年評価をのぞく
        $query .= "     AND NOT (T1.SEMESTER <> '9' AND T1.SCORE_DIV = '09') "; // 学年末以外の仮評価をのぞく
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T1.GRADE = '00' ";
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //評定マスタ
    function getAssessHigh() {
        $query  = " SELECT ";
        $query .= "     INT(T1.ASSESSHIGH) AS ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     ASSESS_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ASSESSCD = '3' ";
        $query .= "     AND T1.ASSESSLEVEL = 1 ";

        return $query;
    }

    //指定考査種別のSIDOU_INPUTを取得
    function getSidouInput($model) {
        $query  = " SELECT ";
        $query .= "     SIDOU_INPUT, ";
        $query .= "     SIDOU_INPUT_INF ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->test_cd}' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T1.GRADE = '00' ";
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }

        return $query;
    }

    //指導表示取得
    function getSidouHyouji($model) {
        $query  = " SELECT ";
        $query .= "     T1.NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1";
        $query .= " WHERE ";
        $query .= "         T1.NAMECD1 = 'D054' ";
        $query .= "     AND T1.NAMECD2 IN ( SELECT";
        $query .= "                             MAX(M1.NAMECD2) ";
        $query .= "                         FROM ";
        $query .= "                             NAME_MST M1";
        $query .= "                         WHERE ";
        $query .= "                             T1.NAMECD1 = M1.NAMECD1 ) ";
        
        return $query;
    }

    //学期詳細マスタ(日付の取得)
    function getSemesterDetailMst($semesterDetail) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTER_DETAIL, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     VALUE(SDATE, '9999-12-31') AS SDATE, ";
        $query .= "     VALUE(EDATE, '9999-12-31') AS EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER_DETAIL = '{$semesterDetail}' ";

        return $query;
    }

    function get_semester_detail($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER_DETAIL ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        $query .= "     SEMESTER = '{$model->semester}' AND ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->test_cd}' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T1.GRADE = '00' ";
            $query .= "     AND T1.COURSECD || T1.MAJORCD = '".$model->field["MAJOR"]."' ";
        }

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || ':' || GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学科
    function getMajor($model) {
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " WITH MAIN_T AS ( ";
            $query .= " SELECT ";
            $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
            $query .= "     VCM.COURSECD || VCM.MAJORCD AS VALUE, ";
            $query .= "     A043.NAME1 ";
            $query .= " FROM ";
            $query .= "     V_COURSE_MAJOR_MST VCM ";
            $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
            $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
            $query .= " WHERE ";
            $query .= "     VCM.YEAR = '".CTRL_YEAR."' ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     MAIN_T ";
            $query .= " WHERE ";
            $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        
        } else {
            if ($model->Properties["knjd234vUseMajorcdKeta1"] == '1') {
                $query  = " WITH ALL(LABEL, VALUE) AS (VALUES('00:-すべて-', '00'))";
                $query .= " SELECT ";
                $query .= "     LABEL, ";
                $query .= "     VALUE ";
                $query .= " FROM ";
                $query .= "     ALL ";
                $query .= " UNION ALL ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.COURSECD || SUBSTR(T1.MAJORCD, 1, 1) || ':' || CASE WHEN 0 < LOCATE(T1.MAJORNAME, '　') THEN SUBSTR(T1.MAJORNAME, 1, LOCATE(T1.MAJORNAME, '　')) ELSE T1.MAJORNAME END AS LABEL, ";
                $query .= "     T1.COURSECD || SUBSTR(T1.MAJORCD, 1, 1) AS VALUE ";
            } else {
                $query  = " WITH ALL(LABEL, VALUE) AS (VALUES('0000:-すべて-', '0000'))";
                $query .= " SELECT ";
                $query .= "     LABEL, ";
                $query .= "     VALUE ";
                $query .= " FROM ";
                $query .= "     ALL ";
                $query .= " UNION ALL ";
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.COURSECD || T1.MAJORCD || ':' || T1.MAJORNAME AS LABEL, ";
                $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE ";
            }
            $query .= " FROM ";
            $query .= "     MAJOR_MST T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '". CTRL_YEAR ."' ";
            if ($model->semester == '9') {
                $query .= "         AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "         AND T2.SEMESTER = '".$model->semester."' ";
            }
            $query .= "         AND T2.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "         AND T2.COURSECD = T1.COURSECD ";
            $query .= "         AND T2.MAJORCD = T1.MAJORCD ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //更新チェック
    function dataCnt($paraData) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SPECIAL_REASON_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$paraData["YEAR"]}' ";
        $query .= "     AND SEMESTER = '{$paraData["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD = '{$paraData["TESTKINDCD"]}' ";
        $query .= "     AND TESTITEMCD = '{$paraData["TESTITEMCD"]}' ";
        $query .= "     AND SCORE_DIV = '{$paraData["SCORE_DIV"]}' ";
        $query .= "     AND REASON_DIV = '{$paraData["REASON_DIV"]}' ";
        $query .= "     AND CLASSCD = '{$paraData["CLASSCD"]}' ";
        $query .= "     AND SCHOOL_KIND = '{$paraData["SCHOOL_KIND"]}' ";
        $query .= "     AND CURRICULUM_CD = '{$paraData["CURRICULUM_CD"]}' ";
        $query .= "     AND SUBCLASSCD = '{$paraData["SUBCLASSCD"]}' ";
        $query .= "     AND SCHREGNO = '{$paraData["SCHREGNO"]}' ";

        return $query;
    }


    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $data = array();
            //データセット
            $data["REMARK"][TEXT]       = $data_arr[$i]["REMARK"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = knjd234vQuery::dataCnt($data_arr[$i]);
            $dataCnt = $db->getOne($query);
            if (1 > $dataCnt) {
                $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
                $data["SEMESTER"][TEXT]     = $data_arr[$i]["SEMESTER"];
                $data["TESTKINDCD"][TEXT]   = $data_arr[$i]["TESTKINDCD"];
                $data["TESTITEMCD"][TEXT]   = $data_arr[$i]["TESTITEMCD"];
                $data["SCORE_DIV"][TEXT]    = $data_arr[$i]["SCORE_DIV"];
                $data["REASON_DIV"][TEXT]   = $data_arr[$i]["REASON_DIV"];
                $data["CLASSCD"][TEXT]      = $data_arr[$i]["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]  = $data_arr[$i]["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]= $data_arr[$i]["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]   = $data_arr[$i]["SUBCLASSCD"];
                $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
                $query = Query::insertSQL($data, "SPECIAL_REASON_TEST_DAT");
            } else {
                $where  = "WHERE ";
                $where .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $where .= "     AND SEMESTER = '{$data_arr[$i]["SEMESTER"]}' ";
                $where .= "     AND TESTKINDCD = '{$data_arr[$i]["TESTKINDCD"]}' ";
                $where .= "     AND TESTITEMCD = '{$data_arr[$i]["TESTITEMCD"]}' ";
                $where .= "     AND SCORE_DIV = '{$data_arr[$i]["SCORE_DIV"]}' ";
                $where .= "     AND REASON_DIV = '{$data_arr[$i]["REASON_DIV"]}' ";
                $where .= "     AND CLASSCD = '{$data_arr[$i]["CLASSCD"]}' ";
                $where .= "     AND SCHOOL_KIND = '{$data_arr[$i]["SCHOOL_KIND"]}' ";
                $where .= "     AND CURRICULUM_CD = '{$data_arr[$i]["CURRICULUM_CD"]}' ";
                $where .= "     AND SUBCLASSCD = '{$data_arr[$i]["SUBCLASSCD"]}' ";
                $where .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";

                $query = Query::updateSQL($data, "SPECIAL_REASON_TEST_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM SPECIAL_REASON_TEST_DAT ";
            $query .= " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "       AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "       AND TESTKINDCD = '".$data_arr[$i]["TESTKINDCD"]."' ";
            $query .= "       AND TESTITEMCD = '".$data_arr[$i]["TESTITEMCD"]."' ";
            $query .= "       AND SCORE_DIV = '".$data_arr[$i]["SCORE_DIV"]."' ";
            $query .= "       AND CLASSCD = '{$data_arr[$i]["CLASSCD"]}' ";
            $query .= "       AND SCHOOL_KIND = '{$data_arr[$i]["SCHOOL_KIND"]}' ";
            $query .= "       AND CURRICULUM_CD = '{$data_arr[$i]["CURRICULUM_CD"]}' ";
            $query .= "       AND SUBCLASSCD = '{$data_arr[$i]["SUBCLASSCD"]}' ";
            $query .= "       AND REASON_DIV = '".$data_arr[$i]["REASON_DIV"]."' ";
            $query .= "       AND SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {

        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);

    }
}
?>
