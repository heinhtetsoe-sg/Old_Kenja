<?php

require_once('for_php7.php');
class knjl090yQuery extends Query 
{
    //試験科目データよりデータを取得
    function getTestsubclasscd($model)
    {
        $query  = "";
        $query .= "SELECT  APPLICANTDIV, ";
        $query .= "        TESTSUBCLASSCD,1 as SHOWORDER ";
        $query .= "FROM    ENTEXAM_PERFECT_MST ";
        $query .= " WHERE ";
        $query .= "     entexamyear  = '".$model->year."' and ";
        $query .= "     testdiv      = '".$model->testdiv2."' ";
        $query .= "ORDER BY TESTSUBCLASSCD ";
        return $query;
    }
    //名称マスタよりデータを取得
    function getName($ObjYear, $namecd1)
    {
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

    //受験コースマスタより志望学科を取得
    function get_coursemajor($year, $applicantdiv, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     t1.coursecd, t1.majorcd, t1.examcoursecd, ";
        $query .= "     RTRIM(t1.examcourse_name) AS coursename ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst t1 ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '{$year}' and ";
        $query .= "     t1.applicantdiv = '{$applicantdiv}' and ";
        $query .= "     t1.testdiv      = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd, t1.majorcd, t1.examcoursecd ";

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
        $query .= "      T1.testdiv,";
        $query .= "      case when T1.applicantdiv = '1' then N3.name1 else  N1.name1 end AS testdivname,";
        $query .= "      T1.shdiv,";
        $query .= "      N2.name1 AS shdivname,";
        $query .= "      T1.suc_coursecd,";
        $query .= "      T1.suc_majorcd,";
        $query .= "      T1.suc_coursecode,";
        $query .= "      T1.judgement,";
        $query .= "      T1.judge_kind,";
        $query .= "      T6.name1 AS judgement_name,";
        $query .= "      T1.special_measures,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.proceduredate,";
        $query .= "      T1.PAY_MONEY,";
        $query .= "      T1.entdiv,";
        $query .= "      T1.honordiv,";
        $query .= "      T1.remark1,";
        $query .= "      T1.remark2,";
        $query .= "      T1.success_noticeno,";
        $query .= "      T1.failure_noticeno,";
        $query .= "      T1.GENERAL_FLG,";
        $query .= "      T1.SLIDE_FLG,";
        $query .= "      T1.SPORTS_FLG,";
        $query .= "      BDET005.REMARK1 AS SPORTS_FLG2,";
        $query .= "      T1.SHIFT_DESIRE_FLG,";
        $query .= "      T1.SELECT_SUBCLASS_DIV,";
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
        $query .= "                   ON N1.year = T1.entexamyear AND N1.namecd2 = T1.testdiv AND N1.namecd1 = 'L004'";
        $query .= "      LEFT OUTER JOIN v_name_mst N3 ";
        $query .= "                   ON N3.year = T1.entexamyear AND N3.namecd2 = T1.testdiv AND N3.namecd1 = 'L024'";
        $query .= "      LEFT OUTER JOIN v_name_mst N2 ";
        $query .= "                   ON N2.year = T1.entexamyear AND N2.namecd2 = T1.shdiv AND N2.namecd1 = 'L006'";
        $query .= "      LEFT OUTER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDET005 ";
        $query .= "                   ON BDET005.entexamyear = T1.entexamyear AND BDET005.examno = T1.examno AND BDET005.SEQ = '005'";
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
    function getReceptJudge($model)
    {
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
    function getMaxtestdiv($model, $applicantdiv)
    {
        $query  = " SELECT max(testdiv) ";
        $query .= "   FROM entexam_recept_dat ";
        $query .= "  WHERE entexamyear  = '".$model->year."' ";
        $query .= "    AND applicantdiv = '{$applicantdiv}' ";
        $query .= "    AND examno       = '".$model->examno."' ";
        return $query;
    }

    //特進チャレンジ者の1:学特入試での進学コース合格を取得
    function getJudgeCourse($model, $applicantdiv) {
        $query  = " WITH MAX_WISHDIV AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         DESIREDIV, ";
        $query .= "         MAX(WISHNO) AS WISHNO "; //進学コース
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST ";
        $query .= "     GROUP BY ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV, ";
        $query .= "         DESIREDIV ";
        $query .= "     ) ";
        $query .= " , T_WISHDIV_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         W1.ENTEXAMYEAR, ";
        $query .= "         W1.APPLICANTDIV, ";
        $query .= "         W1.TESTDIV, ";
        $query .= "         W1.DESIREDIV, ";
        $query .= "         W1.WISHNO, ";
        $query .= "         W1.COURSECD, ";
        $query .= "         W1.MAJORCD, ";
        $query .= "         W1.EXAMCOURSECD ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_WISHDIV_MST W1 ";
        $query .= "         INNER JOIN MAX_WISHDIV W2 ";
        $query .= "             ON  W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
        $query .= "             AND W2.APPLICANTDIV = W1.APPLICANTDIV ";
        $query .= "             AND W2.TESTDIV = W1.TESTDIV ";
        $query .= "             AND W2.DESIREDIV = W1.DESIREDIV ";
        $query .= "             AND W2.WISHNO = W1.WISHNO ";
        $query .= "     ) ";
        $query .= "SELECT T2.RECEPTNO ";
        $query .= "      ,T2.EXAMNO ";
        $query .= "      ,T2.JUDGEDIV ";
        $query .= "      ,W1.COURSECD || W1.MAJORCD || W1.EXAMCOURSECD AS COURSEMAJOR ";
        $query .= "      ,W2.EXAMCOURSE_NAME ";
        $query .= "  FROM ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "       INNER JOIN ENTEXAM_RECEPT_DAT T2 ";
        $query .= "             ON  T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.TESTDIV      = T1.TESTDIV "; //出願区分(1:学特入試)
        $query .= "             AND T2.EXAM_TYPE    = '1' ";
        $query .= "             AND T2.EXAMNO       = T1.EXAMNO ";
        $query .= "       INNER JOIN T_WISHDIV_MST W1 ";
        $query .= "             ON  W1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "             AND W1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND W1.TESTDIV      = T1.TESTDIV ";
        $query .= "             AND W1.DESIREDIV    = T1.DESIREDIV ";
        $query .= "       INNER JOIN ENTEXAM_COURSE_MST W2 ";
        $query .= "             ON  W2.ENTEXAMYEAR  = W1.ENTEXAMYEAR ";
        $query .= "             AND W2.APPLICANTDIV = W1.APPLICANTDIV ";
        $query .= "             AND W2.TESTDIV      = W1.TESTDIV ";
        $query .= "             AND W2.COURSECD     = W1.COURSECD ";
        $query .= "             AND W2.MAJORCD      = W1.MAJORCD ";
        $query .= "             AND W2.EXAMCOURSECD = W1.EXAMCOURSECD ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '{$model->year}'";
        $query .= "   AND T1.APPLICANTDIV   = '{$applicantdiv}' ";
        $query .= "   AND T1.EXAMNO         = '{$model->examno}' ";
        return $query;
    }

    //受付データのテーブル
    function getRecept($model, $applicantdiv)
    {
        $query  = "";
        $query .= " WITH T_WISHNO_COURSE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         T1.RECOM_KIND, ";
        $query .= "         N1.NAME1 AS RECOM_KIND_NAME, ";
        $query .= "         T1.DESIREDIV, ";
        $query .= "         T1.WISHNO, ";
        $query .= "         T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD as COURSEMAJOR, ";
        $query .= "         T2.EXAMCOURSE_NAME, ";
        $query .= "         T2.EXAMCOURSE_ABBV, ";
        $query .= "         T2.EXAMCOURSE_MARK ";
        $query .= "     FROM ";
        $query .= "         V_ENTEXAM_APPLICANTDESIRE_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "             ON  T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV      = T2.TESTDIV ";
        $query .= "             AND T1.COURSECD     = T2.COURSECD ";
        $query .= "             AND T1.MAJORCD      = T2.MAJORCD ";
        $query .= "             AND T1.EXAMCOURSECD = T2.EXAMCOURSECD ";
        $query .= "         LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L023' AND N1.NAMECD2 = T1.RECOM_KIND ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "         T1.APPLICANTDIV    = '{$applicantdiv}' AND ";
        $query .= "         T1.TESTDIV         = '{$model->testdiv2}' AND ";
        $query .= "         T1.EXAMNO          = '{$model->examno}' ";
        $query .= "     ) ";

        $query .= "  SELECT";
        $query .= "      T1.receptno,"; //←追加　表示用
        $query .= "      T1.examno,";
        $query .= "      T1.testdiv AS testdiv2,";
        $query .= "      T1.exam_type,";
        $query .= "      T1.judgediv,";
        $query .= "      T1.honordiv,";
        $query .= "      T1.adjournmentdiv,";
        for ($i = 1; $i <= 4; $i++) {
            $query .= "  T1.total{$i},";
            $query .= "  T1.avarage{$i},";
            $query .= "  T1.total_rank{$i},";
            $query .= "  T1.div_rank{$i},";
        }
//        $query .= "      T2.name1,";
        $query .= "      T3.examhall_name"; //←追加　表示用
        $query .= "     ,W1.RECOM_KIND_NAME AS RECOM_KIND_NAME1";
        $query .= "     ,W1.EXAMCOURSE_NAME AS EXAMCOURSE_NAME1";
        $query .= "     ,W2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2";
        $query .= "     ,W1.COURSEMAJOR AS COURSEMAJOR1";
        $query .= "     ,W2.COURSEMAJOR AS COURSEMAJOR2";
        $query .= "  FROM";
        $query .= "      entexam_recept_dat T1 ";
        $query .= "      LEFT JOIN entexam_hall_ydat T3 ";
        $query .= "                   ON  T1.entexamyear = T3.entexamyear";
        $query .= "                   AND T1.applicantdiv = T3.applicantdiv";
        $query .= "                   AND T1.testdiv = T3.testdiv";
        $query .= "                   AND T1.exam_type = t3.exam_type";
        $query .= "                   AND T1.receptno between t3.s_receptno and t3.e_receptno";
        $query .= "      LEFT JOIN T_WISHNO_COURSE W1 ";
        $query .= "                   ON  W1.EXAMNO = T1.EXAMNO";
        $query .= "                   AND W1.WISHNO = '1'";//志望コース1
        $query .= "      LEFT JOIN T_WISHNO_COURSE W2 ";
        $query .= "                   ON  W2.EXAMNO = T1.EXAMNO";
        $query .= "                   AND W2.WISHNO = '2'";//志望コース2
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
/***
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     T1.EXAMNO, ";
***/
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

    //スライド合格（スライド希望）チェック
    function slideCheck($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        EXAMNO      = '".$model->examno."' AND
                        SLIDE_FLG   = '1' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //特別判定合格（特別判定希望）チェック
    function specialCheck($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND
                        EXAMNO      = '".$model->examno."' AND
                        SHIFT_DESIRE_FLG   = '1' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
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
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        $data["JUDGE_KIND"][TEXT]           = $model->field["JUDGE_KIND"];
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]        = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["PAY_MONEY"][NUMBER]          = str_replace(",", "", $model->field["PAY_MONEY"]);
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
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