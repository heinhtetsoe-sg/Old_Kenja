<?php

require_once('for_php7.php');

class knjl072cQuery extends Query {

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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //第１志望コース
    function getSucCourse($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSE_NAME AS LABEL, ";
        $query .= "     T1.TESTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        //$query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= "SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.TOTAL4, ";
        $query .= "     T1.TOTAL_RANK4, ";
//        $query .= "     T2.NAME, ";
        $query .= "     T2.JUDGEMENT, ";
        $query .= "     T1.JUDGEDIV, ";
        $query .= "     N1.NAME1 AS JUDGE_NAME, ";
//        $query .= "     T1.JUDGEDIV || ':' || N1.NAME1 AS JUDGE_NAME, ";
        $query .= "     T2.SUC_COURSECD || '-' || T2.SUC_MAJORCD || '-' || T2.SUC_COURSECODE AS SUC_COURSE, ";
        $query .= "     C2.EXAMCOURSE_NAME AS SUC_COURSE_NAME ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT T1 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T1.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "              ON C2.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND C2.APPLICANTDIV = T2.APPLICANTDIV ";
//        $query .= "             AND C2.TESTDIV      = T2.TESTDIV ";
        $query .= "             AND C2.COURSECD     = T2.SUC_COURSECD ";
        $query .= "             AND C2.MAJORCD      = T2.SUC_MAJORCD ";
        $query .= "             AND C2.EXAMCOURSECD = T2.SUC_COURSECODE ";
        $query .= " WHERE T2.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T2.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T2.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T2.SHDIV          = '".$model->shdiv."' ";
        //合格者で第２志望ありの者のみ
        $query .= "   AND N1.NAMESPARE1 = '1' ";
        $query .= "   AND T2.TESTDIV1 IS NOT NULL ";
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:受験番号順 2:成績順
        if ($model->sort == "2") $query .= "     value(T1.TOTAL4, -1) DESC, ";
        $query .= "     T1.EXAMNO ";
        return $query;
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

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格コース
        $course_array = array();
        $course_array = explode("-", $model->suc_course);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$receptno] != "on") continue;

            //基礎データ
            $data = array();
            $data["SUC_COURSECD"][TEXT]     = $course_array[0];
            $data["SUC_MAJORCD"][TEXT]      = $course_array[1];
            $data["SUC_COURSECODE"][TEXT]   = $course_array[2];
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
}
?>