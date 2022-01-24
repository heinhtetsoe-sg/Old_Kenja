<?php

require_once('for_php7.php');

class knjl052nquery extends Query {
    //入試制度
    function getApplicantdiv($model, $flg="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->test_year}' AND ";
        $query .= "     NAMECD1 = 'L003' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //入試区分取得
    function getTestDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->test_year}' AND ";
        $query .= "     NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分取得
    function getEntExamCourse($model, $flg="")
    {
        $query .= " SELECT ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= " AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " AND EXAMCOURSECD NOT IN ('0001', '0002') ";
        $query .= " ORDER BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";

        return $query;
    }

    //受験科目
    function getTestSubclasscd($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD AS VALUE, ";
        $query .= "     L1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PERFECT_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                            AND L1.NAMECD1 = 'L009' ";
        $query .= "                            AND L1.NAMECD2 = T1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " AND T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD = '{$model->field["TOTALCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
        
    //マスタの追加（ＣＳＶデータより読込）
    function udpateQueryCsv($model,&$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
        
            //データ削除　ENTEXAM_SCORE_DAT
//            if ($cnt == 0) {
                $query  = " DELETE FROM ";
                $query .= "     ENTEXAM_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR     = '{$model->test_year}' ";
                $query .= " AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
                $query .= " AND TESTDIV         = '{$model->field["TESTDIV"]}' ";
                $query .= " AND EXAM_TYPE       = '1' ";
                $query .= " AND RECEPTNO        = '{$data_arr[$i]["EXAMNO"]}' ";
                $query .= " AND TESTSUBCLASSCD  = '{$model->field["TESTSUBCLASSCD"]}' ";

                $db->query($query);
//            }

            //データ追加　ENTEXAM_SCORE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]     = 1;
            $data["RECEPTNO"][TEXT]      = $data_arr[$i]["EXAMNO"];
            $data["TESTSUBCLASSCD"][TEXT]= $data_arr[$i]["TESTSUBCLASSCD"];
            $data["ATTEND_FLG"][TEXT]    = 1;
            $data["SCORE"][NUMBER]       = $data_arr[$i]["SCORE"];
            $data["STD_SCORE"][NUMBER]   = $data_arr[$i]["STD_SCORE"];
            $data["RANK"][NUMBER]        = $data_arr[$i]["RANK"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    function selectCsvErrQuery() {
        $query  = "SELECT * FROM W_CSVMSG_DAT";
        return $query;
    }

    //CSVデータ出力(名簿のみ)と存在チェック
    function selectMainQuery($model, $examno="") {
        $query  = " SELECT ";
        if (!$examno) {
            $query .= "     T4.EXAMNO, ";
            $query .= "     T1.NAME ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "                                               AND T1.EXAMNO      = L1.EXAMNO ";
        $query .= "                                               AND L1.SEQ         = '001' ";
        $query .= " INNER JOIN ENTEXAM_RECEPT_DAT T4 ON T1.ENTEXAMYEAR = T4.ENTEXAMYEAR ";
        $query .= "                                AND T1.APPLICANTDIV = T4.APPLICANTDIV ";
        $query .= "                                AND T1.TESTDIV = T4.TESTDIV ";
        $query .= "                                AND T4.EXAM_TYPE = '1' ";
        $query .= "                                AND T1.EXAMNO = T4.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " AND L1.REMARK8 || L1.REMARK9 || L1.REMARK10 = '{$model->field["TOTALCD"]}' ";
        if (!$examno) {
            $query .= " ORDER BY ";
            $query .= "     T4.EXAMNO ";
        } else {
            $query .= " AND T4.EXAMNO = '{$examno}' ";
        }
        return $query;
    }

}
?>
