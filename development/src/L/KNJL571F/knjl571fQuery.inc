<?php

require_once('for_php7.php');

class knjl571fQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1, ";
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
        if ($namecd1 == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }
        return $query;
    }

    //受験型
    function getExamType($year, $testdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND VALUE(ABBV2, '') IN ( ";
        $query .= "         SELECT ";
        $query .= "             VALUE(ABBV2, '') ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "                 YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L024' ";
        $query .= "             AND NAMECD2 = '".$testdiv."' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //志望区分
    function getExamcourse($model, $examcourse="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMCOURSE_NAME AS CSV_NAME, ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV         = '1' ";
        if ($examcourse) {
            $query .= "     AND COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD = '{$examcourse}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格コース
    function getSucCourse($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '1' ";//固定
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = "";

        $query .= " SELECT ";
        $query .= "   T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , T1.EXAM_TYPE ";
        $query .= "   , T1.RECEPTNO ";
        $query .= "   , T1.EXAMNO ";

        $query .= "   , CASE WHEN T1.APPLICANTDIV = '1' THEN T1.TOTAL1 ELSE T1.TOTAL3 END AS TOTAL ";
        $query .= "   , CASE WHEN T1.APPLICANTDIV = '1' THEN T1.TOTAL_RANK1 ELSE T1.TOTAL_RANK3 END AS TOTAL_RANK ";

        $query .= "   , T1.TOTAL4 ";
        $query .= "   , T1.TOTAL_RANK4 ";
        $query .= "   , T1.TOTAL3 ";
        $query .= "   , T1.TOTAL_RANK3 ";
        $query .= "   , T1.TOTAL2 ";
        $query .= "   , T1.TOTAL_RANK2 ";
        $query .= "   , T1.TOTAL1 ";
        $query .= "   , T1.TOTAL_RANK1 ";
        $query .= "   , T1.JUDGEDIV ";
        $query .= "   , S1.JUDGEMENT ";
        $query .= "   , N3.NAME1 AS JUDGE_NAME ";
        $query .= "   , D001.REMARK8 AS EXAMCOURSECD ";
        $query .= "   , D001.REMARK9 AS EXAMMAJORCD ";
        $query .= "   , D001.REMARK10 AS EXAMCOURSECODE ";
        $query .= "   , C001.EXAMCOURSE_NAME ";
        $query .= "   , C001.EXAMCOURSE_ABBV ";
        $query .= "   , D017.REMARK1 AS EXAMCOURSECD2 ";
        $query .= "   , D017.REMARK2 AS EXAMMAJORCD2 ";
        $query .= "   , D017.REMARK3 AS EXAMCOURSECODE2 ";
        $query .= "   , C017.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2 ";
        $query .= "   , C017.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV2 ";
        $query .= "   , S1.SUC_COURSECD ";
        $query .= "   , S1.SUC_MAJORCD ";
        $query .= "   , S1.SUC_COURSECODE ";
        $query .= "   , S1.NAME ";
        $query .= "   , S1.NAME_KANA ";
        $query .= "   , N1.NAME1 AS APPLICANTDIV_NAME ";
        $query .= "   , N2.NAME1 AS TESTDIV_NAME  ";
        $query .= " FROM ";
        $query .= "   V_ENTEXAM_RECEPT_DAT T1 ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "     ON T1.ENTEXAMYEAR = S1.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = S1.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = S1.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D001 ";
        $query .= "     ON T1.ENTEXAMYEAR = D001.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = D001.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = D001.EXAMNO ";
        $query .= "     AND D001.SEQ = '001' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D017 ";
        $query .= "     ON T1.ENTEXAMYEAR = D017.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = D017.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = D017.EXAMNO ";
        $query .= "     AND D017.SEQ = '017' ";
        $query .= "   LEFT JOIN ENTEXAM_COURSE_MST C001 ";
        $query .= "     ON T1.ENTEXAMYEAR = C001.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = C001.APPLICANTDIV ";
        $query .= "     AND C001.TESTDIV = '1' ";
        $query .= "     AND D001.REMARK8 = C001.COURSECD ";
        $query .= "     AND D001.REMARK9 = C001.MAJORCD ";
        $query .= "     AND D001.REMARK10 = C001.EXAMCOURSECD ";
        $query .= "   LEFT JOIN ENTEXAM_COURSE_MST C017 ";
        $query .= "     ON T1.ENTEXAMYEAR = C017.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = C017.APPLICANTDIV ";
        $query .= "     AND C017.TESTDIV = '1' ";
        $query .= "     AND D017.REMARK1 = C017.COURSECD ";
        $query .= "     AND D017.REMARK2 = C017.MAJORCD ";
        $query .= "     AND D017.REMARK3 = C017.EXAMCOURSECD ";
        $query .= "   LEFT JOIN NAME_MST N1 ";
        $query .= "     ON N1.NAMECD1 = 'L003' ";
        $query .= "     AND N1.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "   LEFT JOIN NAME_MST N2 ";
        $query .= "     ON N2.NAMECD1 = 'L004' ";
        $query .= "     AND N2.NAMECD2 = T1.TESTDIV ";
        $query .= "   LEFT JOIN NAME_MST N3 ";
        $query .= "     ON N3.NAMECD1 = 'L013' ";
        $query .= "     AND N3.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= "   AND D001.REMARK8 || '-' || D001.REMARK9 || '-' || D001.REMARK10 = '{$model->examcourse}' ";
        if ($model->shdiv <> '9') {
            $query .= "   AND S1.SHDIV = '{$model->shdiv}' ";
        }
        $query .= "   AND VALUE(T1.JUDGEDIV, '0') <> '4' "; //4:欠席は対象外
        //特別入試対象者は、合否判定処理をしないため、母集団から除く。
        if ($model->testdiv == "5") {
            $query .= "   AND VALUE(S1.GENERAL_FLG,'') != '1' ";
        }

        $query .= " ORDER BY ";
        //表示順序ラジオボタン 1:成績順 2:受験番号順
        if ($model->sort == "1") {
            if ($model->applicantdiv == "1") {
                $query .= "     value(T1.TOTAL1, -1) DESC, ";
            } else {
                $query .= "     value(T1.TOTAL3, -1) DESC, ";
            }
        }
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

        $keyList = array();
        $keyList = explode(",", $model->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];

            //受付データ
            $data = array();
            // 判定区分 (空白の場合は「1:合格」をデフォルト設定)
            $judgeDev = $model->judgediv[$receptno];
            if (!$judgeDev) {
                $judgeDev = "1";
            }
            //合格フラグ
            $query = knjl571fQuery::getJudgeFlg($model->year, "L013", $judgeDev);
            $judgeFlg = $db->getOne($query);

            //合格コース
            $course_array = array();
            $course_array = explode("-", $model->examcourse);
            if ($judgeDev == "3") {
                $course_array = array();
                $course_array = explode("-", $model->succourse[$receptno]);
            }

            $data["JUDGEDIV"][TEXT]         = $judgeDev;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO      = '".$receptno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

            //受付詳細データ
            //DELETE
            $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $query .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $query .= "   AND TESTDIV       = '".$model->testdiv."' ";
            $query .= "   AND RECEPTNO      = '".$receptno."' ";
            $query .= "   AND SEQ           = '001' ";
            $db->query($query);

            //INSERT
            if (strlen($judgeFlg) && strlen($model->examcourse)) {

                $query  = " SELECT ";
                $query .= "     MAX(EXAM_TYPE) AS EXAM_TYPE ";
                $query .= " FROM ";
                $query .= "     ENTEXAM_RECEPT_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR = '{$model->year}' ";
                $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
                $query .= "     AND TESTDIV = '{$model->testdiv}' ";
                $query .= "     AND RECEPTNO = '{$receptno}' ";
                $setExamType = $db->getOne($query);

                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAM_TYPE"][TEXT]        = $setExamType;
                $data["RECEPTNO"][TEXT]         = $receptno;
                $data["SEQ"][TEXT]              = "001";
                $data["REMARK1"][TEXT]          = $course_array[0];
                $data["REMARK2"][TEXT]          = $course_array[1];
                $data["REMARK3"][TEXT]          = $course_array[2];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
                $db->query($query);
            }

            //基礎データ
            $data = array();
            $data["JUDGEMENT"][TEXT]        = $judgeDev;
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

            // 3.入学コースの更新
            $data = array();
            $data["REMARK1"][TEXT]              = $course_array[0];
            $data["REMARK2"][TEXT]              = $course_array[1];
            $data["REMARK3"][TEXT]              = $course_array[2];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
            $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
            $query .= "       EXAMNO        = '".$examno."' AND";
            $query .= "       SEQ           = '007' ";
            $cnt = $db->getOne($query);
            if ($cnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
                $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
                $where .= "       EXAMNO        = '".$examno."' AND";
                $where .= "       SEQ           = '007' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = '007';
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>