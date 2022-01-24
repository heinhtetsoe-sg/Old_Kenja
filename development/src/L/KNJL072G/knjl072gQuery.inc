<?php

require_once('for_php7.php');

class knjl072gQuery extends Query {

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
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= "SELECT T1.RECEPTNO, ";
        $query .= "       T1.EXAMNO, ";
        $query .= "       T2.NAME, ";
        $query .= "       T1.PROCEDUREDIV1, ";
        $query .= "       T1.TOTAL4, ";
        $query .= "       T1.JUDGEDIV, ";
        $query .= "       N1.NAME1 AS JUDGE_NAME, ";
        $query .= "       T2.DAI2_COURSECD || '-' || T2.DAI2_MAJORCD || '-' || T2.DAI2_COURSECODE AS DAI2_COURSE, ";
        $query .= "       T2.SUC_COURSECD || '-' || T2.SUC_MAJORCD || '-' || T2.SUC_COURSECODE AS SUC_COURSE, ";
        $query .= "       C2.EXAMCOURSE_ABBV AS SUC_COURSE_NAME, ";
        $query .= "       C1.EXAMCOURSE_ABBV AS DAI1_COURSE_NAME ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T1.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T1.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "              ON C2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND C2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND C2.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND C2.COURSECD     = T2.SUC_COURSECD ";
        $query .= "             AND C2.MAJORCD      = T2.SUC_MAJORCD ";
        $query .= "             AND C2.EXAMCOURSECD = T2.SUC_COURSECODE ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "              ON C1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND C1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND C1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND C1.COURSECD     = T2.DAI1_COURSECD ";
        $query .= "             AND C1.MAJORCD      = T2.DAI1_MAJORCD ";
        $query .= "             AND C1.EXAMCOURSECD = T2.DAI1_COURSECODE ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T2.SHDIV          = '".$model->shdiv."' ";
        $query .= "   AND T2.DAI1_COURSECD || '-' || T2.DAI1_MAJORCD || '-' || T2.DAI1_COURSECODE = '".$model->examcourse."' ";
        //特別措置者のみ
        if (strlen($model->special_reason_div)) {
            $query .= "   AND T2.SPECIAL_REASON_DIV IS NOT NULL ";
        } else {
            $query .= "   AND VALUE(T1.JUDGEDIV, '0') <> '4' "; //4:欠席は対象外
        }
        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") $query .= "     value(T1.TOTAL4, -1) DESC, ";
        $query .= "     T1.RECEPTNO ";
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

        //合格コース(第１志望)
        $course_array = array();
        $course_array = explode("-", $model->examcourse);

        //合格フラグ
        $query = knjl072gQuery::getJudgeFlg($model->year, "L013", $model->judgediv);
        $judgeFlg = $db->getOne($query);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$receptno] != "on") continue;

            //受付データ
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $model->judgediv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //基礎データ
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $model->judgediv;
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
}
?>