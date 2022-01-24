<?php

require_once('for_php7.php');

class knjl270gQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        if ($namecd1 == "L013") {
            $query .= "     AND NAMECD2 <> '4' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //第１志望コース
    function getExamcourse($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE desc ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     L013.NAME1 AS JUDGE_NAME, ";
        $query .= "     BASE.SUC_COURSECD || '-' || BASE.SUC_MAJORCD || '-' || BASE.SUC_COURSECODE AS SUC_COURSE, ";
        $query .= "     COURSE.EXAMCOURSE_ABBV AS SUC_COURSE_NAME ";
        $query .= "  FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN V_NAME_MST L013 ";
        $query .= "              ON L013.YEAR         = BASE.ENTEXAMYEAR ";
        $query .= "             AND L013.NAMECD1      = 'L013' ";
        $query .= "             AND L013.NAMECD2      = BASE.JUDGEMENT ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST COURSE ";
        $query .= "              ON COURSE.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "             AND COURSE.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "             AND COURSE.TESTDIV      = BASE.TESTDIV ";
        $query .= "             AND COURSE.COURSECD     = BASE.SUC_COURSECD ";
        $query .= "             AND COURSE.MAJORCD      = BASE.SUC_MAJORCD ";
        $query .= "             AND COURSE.EXAMCOURSECD = BASE.SUC_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR    = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV        = '".$model->testdiv."' ";
        $query .= "     AND BASE.SHDIV          = '".$model->shdiv."' ";
        $query .= "     AND BASE.DAI1_COURSECD || '-' || BASE.DAI1_MAJORCD || '-' || BASE.DAI1_COURSECODE = '".$model->examcourse."' ";
        $query .= "     AND VALUE(BASE.JUDGEMENT, '0') <> '4' "; //4:欠席は対象外
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格コース(第１志望)
        $course_array = array();
        $course_array = explode("-", $model->examcourse);

        //合格フラグ
        $query = knjl270gQuery::getJudgeFlg($model->year, "L013", $model->judgement);
        $judgeFlg = $db->getOne($query);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $examno) {

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$examno] != "on") continue;

            //基礎データ
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $model->judgement;
            $data["SUC_COURSECD"][TEXT]     = $judgeFlg == "1" ? $course_array[0] : "";
            $data["SUC_MAJORCD"][TEXT]      = $judgeFlg == "1" ? $course_array[1] : "";
            $data["SUC_COURSECODE"][TEXT]   = $judgeFlg == "1" ? $course_array[2] : "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //名称マスタより取得
    function getJudgeFlg($year, $namecd1, $namecd2) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }
}
?>