<?php

require_once('for_php7.php');


class knjd210iQuery extends Query {

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
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
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
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query  = " SELECT ";
            $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
            $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || '：' || TESTITEMNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
            $query .= "     TESTKINDCD || TESTITEMCD || '：' || TESTITEMNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
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
        $testFlg = substr($model->field["TESTKIND"], 0, 2) != "99" ? "1" : "2"; //1:中間・期末 2:学期末

        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS VALUE, ";
	        $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD || '：' || L1.SUBCLASSNAME AS LABEL ";
        } else {
	        $query .= "     T1.COMBINED_SUBCLASSCD AS VALUE, ";
	        $query .= "     T1.COMBINED_SUBCLASSCD || '：' || L1.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_COURSE_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.COMBINED_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     					AND L1.CLASSCD = T1.COMBINED_CLASSCD ";
            $query .= "     					AND L1.SCHOOL_KIND = T1.COMBINED_SCHOOL_KIND ";
            $query .= "    						AND L1.CURRICULUM_CD = T1.COMBINED_CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.FLG      = '{$testFlg}' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";

        //更新可能(制限付の条件)
        if (AUTHORITY != DEF_UPDATABLE) {
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD IN ( ";
            } else {
                $query .= "     AND T1.COMBINED_SUBCLASSCD IN ( ";
            }
            $query .= "         SELECT ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ";
            } else {
                $query .= "             T3.SUBCLASSCD ";
            }
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1 ";
            $query .= "             INNER JOIN CHAIR_STD_DAT T2 ";
            $query .= "                  ON T2.YEAR       = T1.YEAR ";
            $query .= "                 AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "                 AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             INNER JOIN CHAIR_STF_DAT T4 ";
            $query .= "                  ON T4.YEAR       = T1.YEAR ";
            $query .= "                 AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "                 AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             INNER JOIN SUBCLASS_MST T3 ON ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             T3.CLASSCD = T1.CLASSCD AND ";
                $query .= "             T3.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "             T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "                 T3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "             LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T2.SCHREGNO AND T5.YEAR = T2.YEAR AND T5.SEMESTER = T2.SEMESTER ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '".CTRL_YEAR."' ";
            if ("9" == $model->field["SEMESTER"]) {
                $query .= "             AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            } else {
                $query .= "             AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            }
            //講座担任
            $query .= "             AND ((T4.STAFFCD = '".STAFFCD."') ";
            //学年主任
            $query .= "                 OR (T5.GRADE IN ";
            $query .= "                     (SELECT ";
            $query .= "                          FIELD2 ";
            $query .= "                      FROM ";
            $query .= "                          STAFF_DETAIL_MST ST ";
            $query .= "                      WHERE ";
            $query .= "                          ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                          AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                          AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                          AND ST.FIELD1 = '0200')) ";
            //教科主任
            $query .= "                 OR (T1.CLASSCD IN ";
            $query .= "                     (SELECT ";
            $query .= "                          FIELD2 ";
            $query .= "                      FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == '1') {
                $query .= "                          STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                          STAFF_DETAIL_MST ST ";
            }
            $query .= "                      WHERE ";
            $query .= "                          ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                          AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                          AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                          AND ST.FIELD1 = '1050')) ";
            $query .= "                 ) ";
            $query .= "         GROUP BY ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "             T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ";
            } else {
                $query .= "             T3.SUBCLASSCD ";
            }
            $query .= "     ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
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
            $queryAttend = knjd210iQuery::getAttendSubclassSql($model, $combSubcd);
            $resultAttend = $db->query($queryAttend);
            while ($rowAttend = $resultAttend->fetchRow(DB_FETCHMODE_ASSOC)) {
                $combSubArray[$combSubcd][$rowAttend["SEQ"]][$rowAttend["ATTEND_SUBCLASSCD"]] = $rowAttend["WEIGHTING"];
            }
            $resultAttend->free();
//var_dump($combSubArray);
        }

        //指定した学年・コースの生徒を抽出
        $queryRegd = knjd210iQuery::getStudentSql($model);
        $resultRegd = $db->query($queryRegd);
        while ($rowRegd = $resultRegd->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $rowRegd["SCHREGNO"];

            //先科目ループ
            foreach ($combSubArray as $combSubcd => $seqArray) {
                //処理生徒の先科目のレコード削除
                knjd210iQuery::delScoreSql($db, $model, $combSubcd, $schregNo);
                $inserted = false;
                //seqループ
                foreach ($seqArray as $seq => $attendArray) {
                    //初期化
                    $recordCount = 0; //元科目のレコード数
                    $scoreArray = array(); //元科目の成績
                    //元科目ループ
                    foreach ($attendArray as $attendSubcd => $weighting) {
                        //処理生徒の元科目の成績を抽出
                        $queryScore = knjd210iQuery::getScoreSql($model, $attendSubcd, $schregNo);
                        $resultScore = $db->query($queryScore);
                        while ($rowScore = $resultScore->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $recordCount++;
                            if (strlen($rowScore["SCORE"])) {
                                $scoreArray[] = $rowScore["SCORE"] * $weighting / 100;
                            }
                            $score = $rowScore["SCORE"];
                            $scoreWeighting = $rowScore["SCORE"] * $weighting / 100;
//echo "schregNo=" .$schregNo .", combSubcd=" .$combSubcd .", seq=" .$seq .", attendSubcd=" .$attendSubcd .", score=" .$score .", scoreWeighting=" .$scoreWeighting ."<BR>";
                        }
                        $resultScore->free();
                    }
                    //処理生徒の先科目のレコード追加。seqループから抜ける。
                    if (get_count($attendArray) == $recordCount) {
                        $score = get_count($attendArray) == get_count($scoreArray) ? round(array_sum($scoreArray)) : "";
                        knjd210iQuery::insScoreSql($db, $model, $combSubcd, $schregNo, $score);
//echo "schregNo=" .$schregNo .", combSubcd=" .$combSubcd .", seq=" .$seq .", score=" .$score .", break" ."<BR>";
                        $inserted = true;
                        break;
                    }
                }
                if ("kaijyo" == $model->z010 && $inserted == false) {
                    knjd210iQuery::insScoreSql($db, $model, $combSubcd, $schregNo, "");
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $combSubcd);
        }
        $testFlg = substr($model->field["TESTKIND"], 0, 2) != "99" ? "1" : "2"; //1:中間・期末 2:学期末

        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ";
        } else {
	        $query .= "     T1.ATTEND_SUBCLASSCD, ";
        }
        //重み（海城）
        if ($model->Properties["weightingHyouki"] == '1') {
            $query .= "     T1.WEIGHTING2 AS WEIGHTING ";
        } else {
            $query .= "     T1.WEIGHTING ";
        }
        $query .= " FROM ";
        $query .= "     SUBCLASS_WEIGHTING_COURSE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.FLG      = '{$testFlg}' ";
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$model->field["COURSE"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     AND T1.COMBINED_CLASSCD 		= '".$subclass_array[0]."' ";
	        $query .= "     AND T1.COMBINED_SCHOOL_KIND 	= '".$subclass_array[1]."' ";
	        $query .= "     AND T1.COMBINED_CURRICULUM_CD 	= '".$subclass_array[2]."' ";
	        $query .= "     AND T1.COMBINED_SUBCLASSCD 		= '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND T1.COMBINED_SUBCLASSCD = '{$combSubcd}' "; //指定した先科目
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     T1.ATTEND_CLASSCD, ";
	        $query .= "     T1.ATTEND_SCHOOL_KIND, ";
	        $query .= "     T1.ATTEND_CURRICULUM_CD, ";
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $attendSubcd);
        }
        $scoreDiv = substr($model->field["TESTKIND"], 0, 2) != "99" ? "01" : "00"; //01:中間・期末 00:学期末
        $scoreField = substr($model->field["TESTKIND"], 0, 2) != "99" ? "T1.SCORE" : "T1.VALUE"; //SCORE:中間・期末 VALUE:学期末

        $query  = " SELECT ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "     T1.SCORE AS SCORE ";
        } else {
            $query .= "     {$scoreField} AS SCORE ";
        }
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKIND"]."' ";
        } else {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTKIND"]."' ";
            $query .= "     AND T1.SCORE_DIV    = '{$scoreDiv}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  	AND  T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "  	AND  T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "  	AND  T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "  	AND  T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND  T1.SUBCLASSCD   = '{$attendSubcd}' ";
        }
        $query .= "     AND T1.SCHREGNO     = '{$schregNo}' ";
        return $query;
    }

    //先科目のレコード削除
    function delScoreSql($db, $model, $combSubcd, $schregNo) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $combSubcd);
        }
        $scoreDiv = substr($model->field["TESTKIND"], 0, 2) != "99" ? "01" : "00"; //01:中間・期末 00:学期末

        $query  = " DELETE FROM RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '".$model->field["TESTKIND"]."' ";
        } else {
            $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '".$model->field["TESTKIND"]."' ";
            $query .= "     AND T1.SCORE_DIV    = '{$scoreDiv}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  	AND  T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "  	AND  T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "  	AND  T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "  	AND  T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND T1.SUBCLASSCD   = '{$combSubcd}' ";
        }
        $query .= "     AND T1.SCHREGNO     = '{$schregNo}' ";

        $db->query($query);
    }

    //先科目のレコード追加
    function insScoreSql($db, $model, $combSubcd, $schregNo, $score) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $combSubcd);
        }
        $scoreDiv = substr($model->field["TESTKIND"], 0, 2) != "99" ? "01" : "00"; //01:中間・期末 00:学期末
        $scoreField = substr($model->field["TESTKIND"], 0, 2) != "99" ? "SCORE" : "VALUE"; //SCORE:中間・期末 VALUE:学期末

        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        $data["TESTKINDCD"][TEXT]           = substr($model->field["TESTKIND"], 0, 2);
        $data["TESTITEMCD"][TEXT]           = substr($model->field["TESTKIND"], 2, 2);
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $data["SCORE_DIV"][TEXT]            = substr($model->field["TESTKIND"], 4, 2);
        } else {
            $data["SCORE_DIV"][TEXT]            = $scoreDiv;
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
	        $data["SUBCLASSCD"][TEXT]           = $combSubcd;
        }
        $data["SCHREGNO"][TEXT]             = $schregNo;
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $data["SCORE"][NUMBER]              = $score;
        } else {
            $data[$scoreField][NUMBER]          = $score;
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        $db->query($query);
    }

}
?>