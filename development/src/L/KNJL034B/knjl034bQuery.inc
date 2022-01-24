<?php

require_once('for_php7.php');

class knjl034bQuery extends Query {

    //入試年度コンボ
    function getYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS LABEL, ";
        $query .= "     ENTEXAMYEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //履歴一覧
    function getList($model, $div = "") {
        $query  = " SELECT ";
        $query .= "     NORMAL_PASSCOURSECD || '-' || NORMAL_PASSMAJORCD || '-' || NORMAL_PASSEXAMCOURSECD AS NORMAL_PASSCOURSECD_NAME_SET, ";
        $query .= "     EARLY_PASSCOURSECD || '-' ||  EARLY_PASSMAJORCD || '-' || EARLY_PASSEXAMCOURSECD AS EARLY_PASSCOURSECD_NAME_SET, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        if ($div == "select") {
            $query .= "     AND JUDGMENT_DIV = '{$model->judgment_div}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     JUDGMENT_DIV ";

        return $query;
    }

    //受験コース合否マスタより取得
    function getNameMst($year, $name, $model, $setcd)
    {
        $query  = " SELECT DISTINCT ";
        if (!$setcd) {
            $query .= "     T1.EXAMCOURSE_NAME || '（' || T1.EXAMCOURSE_ABBV || '）' AS LABEL, ";
            $query .= "     T1.COURSECD || '-' || T1.MAJORCD ||'-' || T1.EXAMCOURSECD AS VALUE ";
        } else {
            $query .= "     T1.EXAMCOURSE_NAME || '（' || T1.EXAMCOURSE_ABBV || '）' AS EXAMCOURSE_NAME ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST T2 ON ";
        $query .= "         T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ";
        if ($name == "SEIKI") {
            $query .= "         T1.COURSECD = T2.NORMAL_PASSCOURSECD AND ";
            $query .= "         T1.MAJORCD  = T2.NORMAL_PASSMAJORCD AND ";
            $query .= "         T1.EXAMCOURSECD = T2.NORMAL_PASSEXAMCOURSECD ";
        } else {
            $query .= "         T1.COURSECD = T2.EARLY_PASSCOURSECD AND ";
            $query .= "         T1.MAJORCD  = T2.EARLY_PASSMAJORCD AND ";
            $query .= "         T1.EXAMCOURSECD = T2.EARLY_PASSEXAMCOURSECD ";
        }
        $query .= " WHERE ";
        $query .= "      T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "  AND T1.APPLICANTDIV = '1' ";
        $query .= "  AND T1.TESTDIV = ( ";
        $query .= "                     SELECT ";
        $query .= "                         MIN(M1.TESTDIV) ";
        $query .= "                     FROM ";
        $query .= "                         ENTEXAM_COURSE_MST M1 ";
        $query .= "                     WHERE ";
        $query .= "                         M1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                     AND M1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                     AND M1.COURSECD = T1.COURSECD ";
        $query .= "                     AND M1.MAJORCD = T1.MAJORCD ";
        $query .= "                     AND M1.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "                   ) ";
        if ($setcd) {
            if ($name == "SEIKI") {
                $nomalpass_array = array();
                $nomalpass_array = explode("-", $setcd);
                $query .= "     AND T1.COURSECD      = '".$nomalpass_array[0]."' ";
                $query .= "     AND T1.MAJORCD       = '".$nomalpass_array[1]."' ";
                $query .= "     AND T1.EXAMCOURSECD  = '".$nomalpass_array[2]."' ";
            } else {
                $earlypass_array = array();
                $earlypass_array = explode("-", $setcd);
                $query .= "     AND T1.COURSECD      = '".$earlypass_array[0]."' ";
                $query .= "     AND T1.MAJORCD       = '".$earlypass_array[1]."' ";
                $query .= "     AND T1.EXAMCOURSECD  = '".$earlypass_array[2]."' ";
            }
        }
        if (!$setcd) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }
    
    //ある年度にENTEXAM_COURSE_JUDGMENT_MSTが存在するのかチェックする。
    function getCheckCourseJudgmentMstSql($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //今年度のレコードを次年度としてインサート
    function getCopyQuery($thisYear, $nextYear) {
        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " SELECT ";
        $query .= "     '{$nextYear}', ";
        $query .= "     JUDGMENT_DIV, ";
        $query .= "     PROMISE_COURSE_NAME, ";
        $query .= "     PROMISE_COURSE_ABBV, ";
        $query .= "     PROMISE_RECOMMEND_TEST_FLG, ";
        $query .= "     PROMISE_GENERAL_TEST_FLG, ";
        $query .= "     JUDGMENT_COURSE_NAME, ";
        $query .= "     JUDGMENT_COURSE_ABBV, ";
        $query .= "     TAKE_RECOMMEND_TEST_FLG, ";
        $query .= "     TAKE_GENERAL_TEST_FLG, ";
        $query .= "     CHANGE_SINGLE_TEST_FLG, ";
        $query .= "     NORMAL_PASSCOURSECD, ";
        $query .= "     NORMAL_PASSMAJORCD, ";
        $query .= "     NORMAL_PASSEXAMCOURSECD, ";
        $query .= "     EARLY_PASSCOURSECD, ";
        $query .= "     EARLY_PASSMAJORCD, ";
        $query .= "     EARLY_PASSEXAMCOURSECD, ";
        $query .= "     PASSCOURSE_DIV, ";
        $query .= "     SCHOOLWORK_DIV, ";
        $query .= "     SPECIAL_DIV, ";        
        $query .= "     '" . STAFFCD . "', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$thisYear}' ";

        return $query;
    }
    
    //データカウント
    function getCountdata($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_JUDGMENT_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' AND";
        $query .= "     JUDGMENT_DIV = '".$model->field["JUDGMENT_DIV"]."'";

        return $query;
    }
    
    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //管理者権限ののみ修正可能
        if (AUTHORITY == DEF_UPDATABLE) {
            //データチェック
            $getCount = $db->getOne(knjl034bQuery::getCountdata($model));
            //ENTEXAM_COURSE_JUDGMENT_MST ---UPDATE
            $data = array();
            if ($getCount == 0) {
                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["JUDGMENT_DIV"][TEXT]         = sprintf("%02d", $model->field["JUDGMENT_DIV"]);
            }
            //$data["JUDGMENT_DIV"][TEXT]                 = $model->field["JUDGMENT_DIV"];
            $data["PROMISE_COURSE_NAME"][TEXT]          = $model->field["PROMISE_COURSE_NAME"];
            $data["PROMISE_COURSE_ABBV"][TEXT]          = $model->field["PROMISE_COURSE_ABBV"];
            $data["PROMISE_RECOMMEND_TEST_FLG"][TEXT]   = $model->field["PROMISE_RECOMMEND_TEST_FLG"];
            $data["PROMISE_GENERAL_TEST_FLG"][TEXT]     = $model->field["PROMISE_GENERAL_TEST_FLG"];
            $data["JUDGMENT_COURSE_NAME"][TEXT]         = $model->field["JUDGMENT_COURSE_NAME"];
            $data["JUDGMENT_COURSE_ABBV"][TEXT]         = $model->field["JUDGMENT_COURSE_ABBV"];
            $data["TAKE_RECOMMEND_TEST_FLG"][TEXT]      = $model->field["TAKE_RECOMMEND_TEST_FLG"];
            $data["TAKE_GENERAL_TEST_FLG"][TEXT]        = $model->field["TAKE_GENERAL_TEST_FLG"];
            $data["CHANGE_SINGLE_TEST_FLG"][TEXT]       = $model->field["CHANGE_SINGLE_TEST_FLG"];
            if ($model->field["NORMAL_PASSCOURSECD_NAME_SET"]) {
                $nomalpass_array = array();
                $nomalpass_array = explode("-", $model->field["NORMAL_PASSCOURSECD_NAME_SET"]);
                $data["NORMAL_PASSCOURSECD"][TEXT]          = $nomalpass_array[0];
                $data["NORMAL_PASSMAJORCD"][TEXT]           = $nomalpass_array[1];
                $data["NORMAL_PASSEXAMCOURSECD"][TEXT]      = $nomalpass_array[2];
            } else {
                $data["NORMAL_PASSCOURSECD"][TEXT]          = "";
                $data["NORMAL_PASSMAJORCD"][TEXT]           = "";
                $data["NORMAL_PASSEXAMCOURSECD"][TEXT]      = "";
            }
            if ($model->field["EARLY_PASSCOURSECD_NAME_SET"]) {
                $earlypass_array = array();
                $earlypass_array = explode("-", $model->field["EARLY_PASSCOURSECD_NAME_SET"]);
                $data["EARLY_PASSCOURSECD"][TEXT]           = $earlypass_array[0];
                $data["EARLY_PASSMAJORCD"][TEXT]            = $earlypass_array[1];
                $data["EARLY_PASSEXAMCOURSECD"][TEXT]       = $earlypass_array[2];
            } else {
                $data["EARLY_PASSCOURSECD"][TEXT]           = "";
                $data["EARLY_PASSMAJORCD"][TEXT]            = "";
                $data["EARLY_PASSEXAMCOURSECD"][TEXT]       = "";
            }
            $data["PASSCOURSE_DIV"][TEXT]               = $model->field["PASSCOURSE_DIV"];
            $data["SCHOOLWORK_DIV"][TEXT]               = $model->field["SCHOOLWORK_DIV"];
            $data["SPECIAL_DIV"][TEXT]                  = $model->field["SPECIAL_DIV"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            if ($getCount == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_COURSE_JUDGMENT_MST");
            } else {
                $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' AND";
                $where .= "       JUDGMENT_DIV = '".$model->judgment_div."'";
                $query = Query::updateSQL($data, "ENTEXAM_COURSE_JUDGMENT_MST", $where);
            }
            $db->query($query);
        //管理者以外の権限の場合は名称、略称のみ修正可能
        } else {
            $data["PROMISE_COURSE_NAME"][TEXT]          = $model->field["PROMISE_COURSE_NAME"];
            $data["PROMISE_COURSE_ABBV"][TEXT]          = $model->field["PROMISE_COURSE_ABBV"];
            $data["JUDGMENT_COURSE_NAME"][TEXT]         = $model->field["JUDGMENT_COURSE_NAME"];
            $data["JUDGMENT_COURSE_ABBV"][TEXT]         = $model->field["JUDGMENT_COURSE_ABBV"];
            $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' AND";
            $where .= "       JUDGMENT_DIV = '".$model->judgment_div."'";
            $query = Query::updateSQL($data, "ENTEXAM_COURSE_JUDGMENT_MST", $where);
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
    
}
?>
