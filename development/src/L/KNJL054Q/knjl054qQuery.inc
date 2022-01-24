<?php

require_once('for_php7.php');
class knjl054qquery extends Query
{
    //入試制度
    public function getApplicantdiv($model, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '{$model->test_year}' ";
        $query .= "     AND NAMECD1    = 'L003' ";
        $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //入試区分取得(中学用)
    public function getTestDivJ($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->test_year}' ";
        $query .= "     AND NAMECD1 = 'L024' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    public function getTestDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->test_year}' ";
        $query .= "     AND NAMECD1 = 'L004' ";
        $query .= "     AND NAMECD2 IN ('3', '4', '5') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    public function getTestSubclasscd($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->test_year}' ";
        $query .= "     AND NAMECD1 = 'L009' ";
        if ($model->field["TESTDIV"] == "3") {
            $query .= "     AND NAMECD2  IN ('1', '2', '3', '5') ";
        } else {
            $query .= "     AND NAMECD2  IN ('1', '5') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function udpateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //校長推薦、自己推薦
        if ($model->field["TESTDIV"] == "3" || $model->field["TESTDIV"] == "4") {
            $class_array = array("1" => "国語",
                                 "2" => "数学",
                                 "5" => "英語",
                                 "3" => "理科",
                                 "4" => "社会"
                                );
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //校長推薦、自己推薦
            if ($this->field["TESTDIV"] == "3" || $model->field["TESTDIV"] == "4") {
                foreach ($class_array as $code => $label) {
                    //各科目得点のみ利用
                    knjl054qQuery::delInsScoreData($db, $model, $data_arr[$i]["EXAMNO"], $code, $data_arr[$i]["SCORE".$code]);
                }
            }

            //一般入試
            if ($this->field["TESTDIV"] == "5") {
                //レイアウトには科目コードなし。得点のみ利用（指示画面の科目を利用）
                knjl054qQuery::delInsScoreData($db, $model, $data_arr[$i]["EXAMNO"], $model->field["TESTSUBCLASSCD"], $data_arr[$i]["SCORE"]);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //ENTEXAM_SCORE_DAT
    public function delInsScoreData($db, $model, $examno, $testsubclasscd, $score)
    {
        //データ取得　ENTEXAM_SCORE_DAT
        $query  = " SELECT ";
        $query .= "     SCORE, ";
        $query .= "     SCORE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->test_year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV         = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE       = '1' ";
        $query .= "     AND RECEPTNO        = '{$examno}' ";
        $query .= "     AND TESTSUBCLASSCD  = '{$testsubclasscd}' ";

        $row    = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //データ削除　ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->test_year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV         = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE       = '1' ";
        $query .= "     AND RECEPTNO        = '{$examno}' ";
        $query .= "     AND TESTSUBCLASSCD  = '{$testsubclasscd}' ";

        $db->query($query);

        //データ追加　ENTEXAM_SCORE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["EXAM_TYPE"][TEXT]     = 1;
        $data["RECEPTNO"][TEXT]      = $examno;
        $data["TESTSUBCLASSCD"][TEXT]= $testsubclasscd;
        if ($score === '*') {
            $data["ATTEND_FLG"][TEXT]   = "0";
            $data["SCORE"][NUMBER]      = "";
            $data["SCORE2"][NUMBER]     = "";
        } else {
            $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
            $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
            $data["SCORE2"][NUMBER]     = !strlen($score) ? "" : $score;
        }
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "SYSDATE()";

        $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
        $db->query($query);
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = "SELECT * FROM W_CSVMSG_DAT";
        return $query;
    }

    //CSVデータ出力(名簿のみ)と存在チェック
    public function selectMainQuery($model, $examno = "")
    {
        $query  = " SELECT ";
        if (!$examno) {
            $query .= "     '0' AS GRADE, ";
            $query .= "     '00' AS HR_CLASS, ";
            $query .= "     T4.EXAMNO, ";
            $query .= "     T4.NAME ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T4 ";
        $query .= "          ON T4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T4.EXAMNO       = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->test_year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        if (!$examno) {
            $query .= "     AND VALUE(T1.JUDGEDIV,'') <> '4' ";//欠席者は除く
            $query .= " ORDER BY ";
            $query .= "     T4.EXAMNO ";
        } else {
            $query .= " AND T4.EXAMNO = '{$examno}' ";
        }

        return $query;
    }
}
?>
