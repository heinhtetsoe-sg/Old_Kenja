<?php

require_once('for_php7.php');

class knjl271gQuery extends Query {

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
        $query .= "     VALUE DESC ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";
        $query .= "SELECT ";
        $query .= "       T2.EXAMNO, ";
        $query .= "       T2.NAME, ";
        $query .= "       T3_031.REMARK4 as JUGYORYO, ";
        $query .= "       T3_031.REMARK5 as SHISETSU, ";
        $query .= "       T2.JUDGE_KIND, ";
        $query .= "       L025.NAME1 AS JUDGEKIND_NAME, ";
        $query .= "       L025_KIBOU.NAME1 AS JUDGEKIND_NAME_KIBOU, ";
        $query .= "       N1.NAME1 AS JUDGE_NAME, ";
        $query .= "       T2.DAI2_COURSECD || '-' || T2.DAI2_MAJORCD || '-' || T2.DAI2_COURSECODE AS DAI2_COURSE, ";
        $query .= "       T2.SUC_COURSECD || '-' || T2.SUC_MAJORCD || '-' || T2.SUC_COURSECODE AS SUC_COURSE, ";
        $query .= "       C2.EXAMCOURSE_ABBV AS SUC_COURSE_NAME ";
        $query .= "  FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON N1.YEAR         = T2.ENTEXAMYEAR ";
        $query .= "             AND N1.NAMECD1      = 'L013' ";
        $query .= "             AND N1.NAMECD2      = T2.JUDGEMENT ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "              ON C2.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND C2.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND C2.TESTDIV      = T2.TESTDIV ";
        $query .= "             AND C2.COURSECD     = T2.SUC_COURSECD ";
        $query .= "             AND C2.MAJORCD      = T2.SUC_MAJORCD ";
        $query .= "             AND C2.EXAMCOURSECD = T2.SUC_COURSECODE ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "              ON C1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND C1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND C1.TESTDIV      = T2.TESTDIV ";
        $query .= "             AND C1.COURSECD     = T2.DAI1_COURSECD ";
        $query .= "             AND C1.MAJORCD      = T2.DAI1_MAJORCD ";
        $query .= "             AND C1.EXAMCOURSECD = T2.DAI1_COURSECODE ";
        $query .= "     LEFT JOIN V_NAME_MST L025 ";
        $query .= "              ON L025.YEAR         = T2.ENTEXAMYEAR ";
        $query .= "             AND L025.NAMECD1      = 'L025' ";
        $query .= "             AND L025.NAMECD2      = T2.JUDGE_KIND ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3_029 ";
        $query .= "              ON T3_029.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND T3_029.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND T3_029.EXAMNO       = T2.EXAMNO ";
        $query .= "             AND T3_029.SEQ          = '029' ";
        $query .= "             AND T3_029.REMARK1      = '1' ";
        $query .= "     LEFT JOIN V_NAME_MST L025_KIBOU ";
        $query .= "              ON L025_KIBOU.YEAR         = T2.ENTEXAMYEAR ";
        $query .= "             AND L025_KIBOU.NAMECD1      = 'L025' ";
        $query .= "             AND L025_KIBOU.NAMECD2      = T3_029.REMARK2 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3_031 ";
        $query .= "              ON T3_031.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND T3_031.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND T3_031.EXAMNO       = T2.EXAMNO ";
        $query .= "             AND T3_031.SEQ          = '031' ";
        $query .= " WHERE T2.ENTEXAMYEAR    = '".$model->year."'";
        $query .= "   AND T2.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T2.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND T2.SHDIV          = '".$model->shdiv."' ";
        $query .= "   AND T2.DAI1_COURSECD || '-' || T2.DAI1_MAJORCD || '-' || T2.DAI1_COURSECODE = '".$model->examcourse."' ";
        $query .= "   AND VALUE(T2.JUDGEMENT, '0') <> '4' "; //4:欠席は対象外
        $query .= " ORDER BY ";
        $query .= "     T2.EXAMNO ";
        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $val) {
            $examno = $val;

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$examno] != "on") continue;

            //基礎データ
            $data = array();
            $data["JUDGE_KIND"][TEXT]       = $model->judgekind;
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