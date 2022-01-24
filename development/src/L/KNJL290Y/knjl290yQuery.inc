<?php

require_once('for_php7.php');

class knjl290yQuery extends Query 
{
    //名称マスタよりデータを取得
    function getName($ObjYear, $namecd1) {
        $query  = " SELECT ";
        $query .= "     namecd1, ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     name2, ";
        $query .= "     namespare1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year    = '".$ObjYear ."' AND ";
        $query .= "     namecd1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     namecd2 ";
        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.entexamyear,";
        $query .= "      T1.examno,";
        $query .= "      T1.name,";
        $query .= "      T1.name_kana,";
        $query .= "      T1.birth_y,";
        $query .= "      T1.birth_m,";
        $query .= "      T1.birth_d,";
        $query .= "      T1.birthday,";
        $query .= "      T1.sex,";
        $query .= "      T1.applicantdiv,";
        $query .= "      N0.name1 AS applicantdivname,";
        $query .= "      T1.testdiv,";
        $query .= "      T1.testdiv1 || '：' || N1.name1 AS testdivname1,";
        $query .= "      T1.testdiv2 || '：' || N2.name1 AS testdivname2,";
        $query .= "      T1.testdiv3 || '：' || N3.name1 AS testdivname3,";
        $query .= "      T1.judgement,";
        $query .= "      T1.judgement || '：' || T6.name1 AS judgement_name,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.proceduredate,";
        $query .= "      T1.PAY_MONEY,";
        $query .= "      T1.entdiv,";
        $query .= "      T2.name1 AS sexname,";
        $query .= "      T5.name1 AS era_name";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1 ";
        $query .= "      LEFT OUTER JOIN v_name_mst T2 ";
        $query .= "                   ON T2.year = T1.entexamyear AND T2.namecd2 = T1.sex   AND T2.namecd1 = 'Z002' ";
        $query .= "      LEFT OUTER JOIN v_name_mst T5 ";
        $query .= "                   ON T5.year = T1.entexamyear AND T5.namecd2 = T1.eracd AND T5.namecd1 = 'L007'";
        $query .= "      LEFT OUTER JOIN v_name_mst T6 ";
        $query .= "                   ON T6.year = T1.entexamyear AND T6.namecd2 = T1.judgement AND T6.namecd1 = 'L013'";
        $query .= "      LEFT OUTER JOIN v_name_mst N0 ";
        $query .= "                   ON N0.year = T1.entexamyear AND N0.namecd2 = T1.applicantdiv AND N0.namecd1 = 'L003'";
        $query .= "      LEFT OUTER JOIN v_name_mst N1 ";
        $query .= "                   ON N1.year = T1.entexamyear AND N1.namecd2 = T1.testdiv1 AND N1.namecd1 = 'L004'";
        $query .= "      LEFT OUTER JOIN v_name_mst N2 ";
        $query .= "                   ON N2.year = T1.entexamyear AND N2.namecd2 = T1.testdiv2 AND N2.namecd1 = 'L004'";
        $query .= "      LEFT OUTER JOIN v_name_mst N3 ";
        $query .= "                   ON N3.year = T1.entexamyear AND N3.namecd2 = T1.testdiv3 AND N3.namecd1 = 'L004'";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "      T1.examno < '".$model->examno."'";
            $query .= " ORDER BY T1.examno DESC ";
            $query .= " FETCH FIRST 1 ROWS ONLY "; 
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "      T1.examno > '".$model->examno."'";
            $query .= " ORDER BY T1.examno ASC ";
            $query .= " FETCH FIRST 1 ROWS ONLY "; 
        } else {
            $query .= "      T1.examno = '".$model->examno."'";
        }

        return $query;
    }

    //選択された入試区分以外の受付データのすべての合否取得
    function getReceptJudge($model) {
        $query  = "SELECT";
        $query .= "        SUM(CASE judgediv WHEN '1' THEN 1 ELSE 0 END)              AS pass";         //合格
        $query .= "       ,SUM(CASE coalesce(judgediv, '') WHEN '' THEN 1 ELSE 0 END) AS unknown";      //未設定
        $query .= "       ,COUNT(*) AS cnt";                                                            //受験回数
        $query .= "  FROM entexam_recept_dat ";
        $query .= " WHERE entexamyear = '".$model->year."'";
        $query .= "   AND examno      = '".$model->examno."'";
        $query .= "   AND testdiv    <> '".$model->testdiv2."'";
        return $query;
    }

    //最大testdiv取得
    function getMaxtestdiv($model, $applicantdiv) {
        $query  = " SELECT max(testdiv) ";
        $query .= "   FROM entexam_recept_dat ";
        $query .= "  WHERE entexamyear  = '".$model->year."' ";
        $query .= "    AND applicantdiv = '{$applicantdiv}' ";
        $query .= "    AND examno       = '".$model->examno."' ";
        return $query;
    }

    //受付データのテーブル
    function getRecept($model, $applicantdiv) {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.receptno,"; //←追加　表示用
        $query .= "      T1.examno,";
        $query .= "      T1.testdiv AS testdiv2,";
        $query .= "      T1.exam_type,";
        $query .= "      T1.judgediv,";
        for ($i = 1; $i <= 4; $i++) {
            $query .= "  T1.total{$i},";
            $query .= "  T1.avarage{$i},";
            $query .= "  T1.total_rank{$i},";
        }
        $query .= "      T3.examhall_name,";
        $query .= "      N1.name1 AS interview_value2_name";
        $query .= "  FROM";
        $query .= "      entexam_recept_dat T1 ";
        $query .= "      LEFT JOIN entexam_hall_list_ydat T2 ";
        $query .= "             ON  T1.entexamyear  = T2.entexamyear";
        $query .= "             AND T1.applicantdiv = T2.applicantdiv";
        $query .= "             AND T1.testdiv      = T2.testdiv";
        $query .= "             AND T1.exam_type    = T2.exam_type";
        $query .= "             AND T1.receptno     = T2.receptno";
        $query .= "      LEFT JOIN entexam_hall_ydat T3 ";
        $query .= "             ON  T2.entexamyear = T3.entexamyear";
        $query .= "             AND T2.applicantdiv = T3.applicantdiv";
        $query .= "             AND T2.testdiv = T3.testdiv";
        $query .= "             AND T2.exam_type = T3.exam_type";
        $query .= "             AND T2.examhallcd = T3.examhallcd";
        $query .= "      LEFT JOIN entexam_interview_dat I1 ";
        $query .= "             ON  T1.entexamyear  = I1.entexamyear";
        $query .= "             AND T1.applicantdiv = I1.applicantdiv";
        $query .= "             AND T1.testdiv      = I1.testdiv";
        $query .= "             AND T1.examno       = I1.examno";
        $query .= "      LEFT JOIN name_mst N1 ";
        $query .= "             ON  N1.namecd1      = 'L030'";
        $query .= "             AND N1.namecd2      = I1.interview_value2";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear  = '{$model->year}' AND ";
        $query .= "      T1.applicantdiv = '{$applicantdiv}' AND ";
        $query .= "      T1.testdiv      = '{$model->testdiv2}' AND ";
        $query .= "      T1.examno       = '{$model->examno}' ";
        //echo $query;

        return $query;
    }

    //得点データ取得
    function getScore($model, $applicantdiv) {
        $query  = " SELECT ";
        $query .= "     T2.TESTSUBCLASSCD, ";
        $query .= "     T2.ATTEND_FLG, ";
        $query .= "     T2.SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_SCORE_DAT T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND T2.EXAM_TYPE    = T1.EXAM_TYPE ";
        $query .= "         AND T2.RECEPTNO     = T1.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     T1.TESTDIV      = '{$model->testdiv2}' AND ";
        $query .= "     T1.EXAMNO       = '{$model->examno}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.TESTSUBCLASSCD ";

        return $query;
    }

    //存在チェック
    function Existdata($model) {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        EXAMNO      = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 1.志願者受付データの更新
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->field["JUDGEDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE entexamyear   = '".$model->year."' AND ";
        $where .= "       examno        = '".$model->examno."' AND ";
        $where .= "       testdiv       = '".$model->testdiv2."'";
        $query = Query::updateSQL($data, "entexam_recept_dat", $where);
        $db->query($query);

        // 2.志願者基礎データの更新
        $data = array();
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]        = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["PAY_MONEY"][NUMBER]          = str_replace(",", "", $model->field["PAY_MONEY"]);
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>