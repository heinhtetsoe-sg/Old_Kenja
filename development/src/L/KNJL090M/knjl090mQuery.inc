<?php

require_once('for_php7.php');

class knjl090mQuery extends Query 
{
    //満点マスタより科目を取得
    function getTestsubclasscd($ObjYear)
    {
        $query  = "";
        $query .= "SELECT  TESTSUBCLASSCD ";
        $query .= "FROM    ENTEXAM_PERFECT_MST ";
        $query .= "WHERE   ENTEXAMYEAR  = '".$ObjYear ."' ";
        $query .= "ORDER BY TESTSUBCLASSCD ";
        return $query;
    }
    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     namecd1, ";
        $query .= "     namecd2, ";
        $query .= "     name1, ";
        $query .= "     NAMESPARE1 ";
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

    //志望学科を取得
    function get_coursemajor($year)
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    T1.COURSECD, ";
        $query .= "    T1.MAJORCD, ";
        $query .= "    T1.EXAMCOURSECD, ";
        $query .= "    T1.EXAMCOURSE_NAME AS COURSENAME ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_COURSE_MST T1 ";
        $query .= "WHERE ";
        $query .= "    T1.ENTEXAMYEAR = '".$year."' AND ";
        $query .= "    T1.APPLICANTDIV = '1' AND ";
        $query .= "    T1.TESTDIV = '1' ";
        return $query;
    }

    //受験区分が「有り」のものを取得
    function get_desire_flg($model)
    {
        $query  = " SELECT ";
        $query .= "     examinee_div ";
        $query .= " FROM ";
        $query .= "     entexam_desire_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->year."' AND ";
        $query .= "     examno       = '".$model->examno."' AND ";
        $query .= "     examinee_div = '1' ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
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
        $query .= "      T1.suc_coursecd,";
        $query .= "      T1.suc_majorcd,";
        $query .= "      T1.suc_coursecode,";
        $query .= "      T1.judgement,";
        $query .= "      T6.name1 AS judgement_name,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.proceduredate,";
        $query .= "      T1.entdiv,";
        $query .= "      T1.remark1,";
        $query .= "      T1.remark2,";
        $query .= "      T1.success_noticeno,";
        $query .= "      T1.failure_noticeno,";
        $query .= "      T1.SUB_ORDER,";
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

    //受付データのテーブル
    function getRecept($model)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.receptno,"; //←追加　表示用
        $query .= "      T1.examno,";
        $query .= "      T1.testdiv AS testdiv2,";
        $query .= "      T1.judgediv,";
        $query .= "      T1.total4,";
        $query .= "      T1.total_rank4,";
        $query .= "      T1.JUDGE_DEVIATION,";
        $query .= "      T1.JUDGE_DEVIATION_RANK,";
        $query .= "      T3.examhall_name"; //←追加　表示用
        $query .= "  FROM";
        $query .= "      entexam_recept_dat T1 ";
        $query .= "      LEFT OUTER JOIN entexam_hall_dat T3 ";
        $query .= "                   ON T1.testdiv = T3.testdiv AND T1.exam_type = t3.exam_type AND T1.receptno between t3.s_receptno and t3.e_receptno";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";
        $query .= "      T1.examno      = '".$model->examno."' ";
        //echo $query;

        return $query;
    }

    //得点データ取得
    function getScore($model)
    {
        $query  = " SELECT ";
        $query .= "     testsubclasscd,attend_flg,score,decimal(round(std_score,1),4,1) as std_score,rank ";
        $query .= " FROM ";
        $query .= "     entexam_score_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->year."' AND ";
        $query .= "     (applicantdiv,testdiv,receptno,exam_type) IN (SELECT ";
        $query .= "                                 applicantdiv,testdiv,receptno,exam_type ";
        $query .= "                              FROM ";
        $query .= "                                 entexam_recept_dat ";
        $query .= "                              WHERE ";
        $query .= "                                 entexamyear = '".$model->year."' AND ";
        $query .= "                                 examno      = '".$model->examno."' ";
        $query .= "                             ) ";
        $query .= " ORDER BY ";
        $query .= "     testsubclasscd ";

        return $query;
    }

    //存在チェック
    function Existdata($model)
    {
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

    //補員順位の重複チェック
    function checkSubOrder($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO, SUB_ORDER
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR  = '".$model->year."' AND
                        EXAMNO      <> '".$model->examno."' AND
                        SUB_ORDER    = '".$model->field["SUB_ORDER"]."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 2.志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]        = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["SUCCESS_NOTICENO"][TEXT]     = (strlen($model->field["SUCCESS_NOTICENO"])? sprintf("%04d",$model->field["SUCCESS_NOTICENO"]) : "");
        $data["FAILURE_NOTICENO"][TEXT]     = (strlen($model->field["FAILURE_NOTICENO"])? sprintf("%04d",$model->field["FAILURE_NOTICENO"]) : "");
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
        $data["SUB_ORDER"][TEXT]            = $model->field["SUB_ORDER"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);

        //判定履歴データ（4:補員合格）追加
        $judgement = '4';
        //人数を取得
        $query  = "SELECT COUNT(*) AS JUDGE_CNT ";
        $query .= "  FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "   AND JUDGEMENT    = '{$judgement}' ";
        $judge_cnt = $db->getOne($query);
        //削除
        $query  = "DELETE FROM ENTEXAM_JUDGEMENT_HISTORY_DAT";
        $query .= " WHERE ENTEXAMYEAR = '{$model->year}'";
        $query .= "   AND JUDGEMENT   = '{$judgement}'";
        $db->query($query);
        //追加
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["JUDGEMENT"][TEXT]    = $judgement;
        $data["JUDGE_CNT"][NUMBER]  = $judge_cnt;
        $data["REGISTERCD"][TEXT]   = STAFFCD ;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_JUDGEMENT_HISTORY_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>