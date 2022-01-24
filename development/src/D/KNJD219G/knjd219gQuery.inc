<?php

require_once('for_php7.php');


class knjd219gQuery extends Query {

    function getZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->z010 != 'kaijyo') {
            $query .= "     AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別
    function getTest($model) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || '：' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        //テスト区分　1:考査 2:学期成績
        if ($model->field["TEST_FLG"] == "2") {
            $query .= "     AND TESTKINDCD  = '99' ";
        } else {
            $query .= "     AND TESTKINDCD <> '99' ";
        }
        $query .= "     AND JYORETSU_FLG = '1' "; //1:序列対象
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コース
    function getCourse($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || '：' || L1.COURSENAME || L2.MAJORNAME || L3.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ";
        $query .= "     INNER JOIN MAJOR_MST L2 ON L2.COURSECD = T1.COURSECD AND L2.MAJORCD = T1.MAJORCD ";
        $query .= "     INNER JOIN COURSECODE_MST L3 ON L3.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //読替先科目
    function getList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS VALUE, ";
        $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD || '：' || L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_COURSE_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST L1 ";
        $query .= "          ON L1.CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "         AND L1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "         AND L1.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "         AND L1.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR = T1.YEAR ";
        $query .= "         AND G1.GRADE = T1.GRADE ";
        $query .= "         AND G1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.FLG      = '".$model->field["TEST_FLG"]."' "; //テスト区分　1:考査 2:学期成績
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実行履歴データ
    function getListRireki($model) {
        $query  = " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME AS SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_RIREKI_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "          ON L1.CLASSCD = T1.COMBINED_CLASSCD ";
        $query .= "         AND L1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
        $query .= "         AND L1.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        $query .= "         AND L1.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKIND"]."' ";
        $query .= "     AND T1.FLG      = '".$model->field["TEST_FLG"]."' "; //テスト区分　1:考査 2:学期成績
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //実行履歴データ・追加
    function getInsertRireki($db, $model, $combSubcd) {
        $subclass_array = array();
        $subclass_array = explode("-", $combSubcd);

        $data = array();
        $data["CALC_DATE"][FUNC]                = "date(sysdate())";
        $data["CALC_TIME"][FUNC]                = "time(sysdate())";
        $data["COMBINED_CLASSCD"][TEXT]         = $subclass_array[0];
        $data["COMBINED_SCHOOL_KIND"][TEXT]     = $subclass_array[1];
        $data["COMBINED_CURRICULUM_CD"][TEXT]   = $subclass_array[2];
        $data["COMBINED_SUBCLASSCD"][TEXT]      = $subclass_array[3];
        $data["YEAR"][TEXT]                     = CTRL_YEAR;
        $data["SEMESTER"][TEXT]                 = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]               = substr($model->field["TESTKIND"], 0, 2);
        $data["TESTITEMCD"][TEXT]               = substr($model->field["TESTKIND"], 2, 2);
        $data["SCORE_DIV"][TEXT]                = substr($model->field["TESTKIND"], 4, 2);
        $data["FLG"][TEXT]                      = $model->field["TEST_FLG"];
        $data["GRADE"][TEXT]                    = $model->field["GRADE"];
        $data["COURSECD"][TEXT]                 = substr($model->field["COURSE"], 0, 1);
        $data["MAJORCD"][TEXT]                  = substr($model->field["COURSE"], 1, 3);
        $data["COURSECODE"][TEXT]               = substr($model->field["COURSE"], 4, 4);
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_WEIGHTING_RIREKI_DAT");
        $db->query($query);
    }

    //更新処理
    function insertExe($model) {
        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //処理対象科目(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());

        //指定した先科目の元科目を抽出し、配列にセットして置く(先科目,SEQ,元科目,重み)
        $combSubArray = array();
        foreach ($array as $key => $combSubcd) {
            $queryAttend = knjd219gQuery::getAttendSubclassSql($model, $combSubcd);
            $resultAttend = $db->query($queryAttend);
            while ($rowAttend = $resultAttend->fetchRow(DB_FETCHMODE_ASSOC)) {
                $combSubArray[$combSubcd][$rowAttend["SEQ"]][$rowAttend["ATTEND_SUBCLASSCD"]] = $rowAttend["WEIGHTING"];
            }
            $resultAttend->free();

            //実行履歴データ・追加
            knjd219gQuery::getInsertRireki($db, $model, $combSubcd);
        }

        //指定した学年・コースの生徒を抽出
        $queryRegd = knjd219gQuery::getStudentSql($model);
        $resultRegd = $db->query($queryRegd);
        while ($rowRegd = $resultRegd->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $rowRegd["SCHREGNO"];

            //先科目ループ
            foreach ($combSubArray as $combSubcd => $seqArray) {
                //処理生徒の先科目のレコード削除
                knjd219gQuery::delScoreSql($db, $model, $combSubcd, $schregNo);
                $inserted = false;
                //seqループ
                foreach ($seqArray as $seq => $attendArray) {
                    //初期化
                    $recordCount = 0; //元科目のレコード数
                    $scoreArray = array(); //元科目の成績
                    //元科目ループ
                    foreach ($attendArray as $attendSubcd => $weighting) {
                        //処理生徒の元科目の成績を抽出
                        $resultScore = $db->query(knjd219gQuery::getScoreSql($model, $attendSubcd, $schregNo));
                        while ($rowScore = $resultScore->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $recordCount++;
                            if (strlen($rowScore["SCORE"])) {
                                $scoreArray[] = $rowScore["SCORE"] * $weighting / 100;
                            }
                        }
                        $resultScore->free();
                    }
                    //処理生徒の先科目のレコード追加。seqループから抜ける。
                    if (get_count($attendArray) == $recordCount) {
                        $score = get_count($attendArray) == get_count($scoreArray) ? round(array_sum($scoreArray)) : "";
                        knjd219gQuery::insScoreSql($db, $model, $combSubcd, $schregNo, $score);
                        $inserted = true;
                        break;
                    }
                }
                if ("kaijyo" == $model->z010 && $inserted == false) {
                    knjd219gQuery::insScoreSql($db, $model, $combSubcd, $schregNo, "");
                }
            }
        }
        $resultRegd->free();

        //DB切断
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //指定した先科目の元科目を抽出し、配列にセットして置く(key:SEQ)
    function getAttendSubclassSql($model, $combSubcd) {
        $subclass_array = array();
        $subclass_array = explode("-", $combSubcd);

        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ";
        //重み（海城）
        if ($model->Properties["weightingHyouki"] == '1') {
            $query .= "     T1.WEIGHTING2 AS WEIGHTING ";
        } else {
            $query .= "     T1.WEIGHTING AS WEIGHTING ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.FLG      = '".$model->field["TEST_FLG"]."' "; //テスト区分　1:考査 2:学期成績
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";
        $query .= "     AND T1.COMBINED_CLASSCD         = '".$subclass_array[0]."' ";
        $query .= "     AND T1.COMBINED_SCHOOL_KIND     = '".$subclass_array[1]."' ";
        $query .= "     AND T1.COMBINED_CURRICULUM_CD   = '".$subclass_array[2]."' ";
        $query .= "     AND T1.COMBINED_SUBCLASSCD      = '".$subclass_array[3]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.ATTEND_CLASSCD, ";
        $query .= "     T1.ATTEND_SCHOOL_KIND, ";
        $query .= "     T1.ATTEND_CURRICULUM_CD, ";
        $query .= "     T1.ATTEND_SUBCLASSCD ";

        return $query;
    }

    //指定した学年・コースの生徒を抽出
    function getStudentSql($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        if ("9" == $model->field["SEMESTER"]) {
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        } else {
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //処理生徒の元科目の成績を抽出
    function getScoreSql($model, $attendSubcd, $schregNo) {
        $subclass_array = array();
        $subclass_array = explode("-", $attendSubcd);

        $query  = " SELECT ";
        $query .= "     T1.SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKIND"]."' ";
        $query .= "     AND T1.CLASSCD      = '".$subclass_array[0]."' ";
        $query .= "     AND T1.SCHOOL_KIND  = '".$subclass_array[1]."' ";
        $query .= "     AND T1.CURRICULUM_CD= '".$subclass_array[2]."' ";
        $query .= "     AND T1.SUBCLASSCD   = '".$subclass_array[3]."' ";
        $query .= "     AND T1.SCHREGNO     = '{$schregNo}' ";

        return $query;
    }

    //先科目のレコード削除
    function delScoreSql($db, $model, $combSubcd, $schregNo) {
        $subclass_array = array();
        $subclass_array = explode("-", $combSubcd);

        $query  = " DELETE FROM RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKIND"]."' ";
        $query .= "     AND T1.CLASSCD      = '".$subclass_array[0]."' ";
        $query .= "     AND T1.SCHOOL_KIND  = '".$subclass_array[1]."' ";
        $query .= "     AND T1.CURRICULUM_CD= '".$subclass_array[2]."' ";
        $query .= "     AND T1.SUBCLASSCD   = '".$subclass_array[3]."' ";
        $query .= "     AND T1.SCHREGNO     = '{$schregNo}' ";

        $db->query($query);
    }

    //先科目のレコード追加
    function insScoreSql($db, $model, $combSubcd, $schregNo, $score) {
        $subclass_array = array();
        $subclass_array = explode("-", $combSubcd);

        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]           = substr($model->field["TESTKIND"], 0, 2);
        $data["TESTITEMCD"][TEXT]           = substr($model->field["TESTKIND"], 2, 2);
        $data["SCORE_DIV"][TEXT]            = substr($model->field["TESTKIND"], 4, 2);
        $data["CLASSCD"][TEXT]              = $subclass_array[0];
        $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
        $data["CURRICULUM_CD"][TEXT]        = $subclass_array[2];
        $data["SUBCLASSCD"][TEXT]           = $subclass_array[3];
        $data["SCHREGNO"][TEXT]             = $schregNo;
        $data["SCORE"][NUMBER]              = $score;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        $db->query($query);
    }

}
?>