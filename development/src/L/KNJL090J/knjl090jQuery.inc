<?php

require_once('for_php7.php');

class knjl090jQuery extends Query 
{
    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     namecd1, ";
        $query .= "     namecd2, ";
        $query .= "     name1 ";
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

    //志望区分マスタより志望学科を取得
    function get_coursemajor($model)
    {
        $query  = " SELECT ";
        $query .= "     t1.coursecd, t1.majorcd, t1.examcoursecd, ";
        $query .= "     RTRIM(t2.coursename) || "; 
        $query .= "     RTRIM(t3.majorname) || ";
        $query .= "     RTRIM(t1.examcourse_name) AS coursename ";
        $query .= " FROM ";
        $query .= "     entexam_course_mst t1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_course_mst t2 ";
        $query .= " ON ";
        $query .= "     t1.entexamyear  = t2.year and ";
        $query .= "     t1.coursecd     = t2.coursecd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_major_mst t3 ";
        $query .= " ON ";
        $query .= "     t1.entexamyear  = t3.year and ";
        $query .= "     t1.majorcd      = t3.majorcd ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' and ";
        $query .= "     t1.testdiv = '".$model->testdiv2."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd, t1.majorcd, t1.examcoursecd ";

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

    //特待区分または延期区分が「有り」のものを取得
    function get_recept_flg($model, $field)
    {
        $query  = " SELECT ";
        $query .= "     $field ";
        $query .= " FROM ";
        $query .= "     entexam_recept_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear = '".$model->year."' AND ";
        $query .= "     examno = '".$model->examno."' AND ";
        $query .= "     $field = '1'";

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
        $query .= "      T1.fs_grdyear,";
        $query .= "      T1.applicantdiv,";
        $query .= "      N0.name1 AS applicantdivname,";
        $query .= "      T1.receptdate,";
        $query .= "      N1.name1 AS test_type1,";
        $query .= "      N2.name1 AS test_type2,";
        $query .= "      N3.name1 AS test_type3,";
        $query .= "      N4.name1 AS test_type4,";
        $query .= "      N5.name1 AS test_type5,";
        $query .= "      N6.name1 AS test_type6,";
        $query .= "      T1.suc_coursecd,";
        $query .= "      T1.suc_majorcd,";
        $query .= "      T1.suc_coursecode,";
        $query .= "      T1.judgement,";
        $query .= "      T6.name1 AS judgement_name,";
        $query .= "      T1.special_measures,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.proceduredate,";
        $query .= "      T1.entdiv,";
        $query .= "      T1.honordiv as honordiv1,";
        $query .= "      T1.remark1,";
        $query .= "      T1.remark2,";
        $query .= "      T1.success_noticeno,";
        $query .= "      T1.failure_noticeno,";
        $query .= "      T1.interview_attend_flg,";
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
        $query .= "                   ON N1.year = T1.entexamyear AND N1.namecd2 = T1.testdiv1 AND N1.namecd1 = 'L005'";
        $query .= "      LEFT OUTER JOIN v_name_mst N2 ";
        $query .= "                   ON N2.year = T1.entexamyear AND N2.namecd2 = T1.testdiv2 AND N2.namecd1 = 'L005'";
        $query .= "      LEFT OUTER JOIN v_name_mst N3 ";
        $query .= "                   ON N3.year = T1.entexamyear AND N3.namecd2 = T1.testdiv3 AND N3.namecd1 = 'L005'";
        $query .= "      LEFT OUTER JOIN v_name_mst N4 ";
        $query .= "                   ON N4.year = T1.entexamyear AND N4.namecd2 = T1.testdiv4 AND N4.namecd1 = 'L005'";
        $query .= "      LEFT OUTER JOIN v_name_mst N5 ";
        $query .= "                   ON N5.year = T1.entexamyear AND N5.namecd2 = T1.testdiv5 AND N5.namecd1 = 'L005'";
        $query .= "      LEFT OUTER JOIN v_name_mst N6 ";
        $query .= "                   ON N6.year = T1.entexamyear AND N6.namecd2 = T1.testdiv6 AND N6.namecd1 = 'L005'";
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
    function getMaxtestdiv($model)
    {
        $query  = " SELECT max(testdiv) ";
        $query .= "   FROM entexam_recept_dat ";
        $query .= "  WHERE entexamyear  = '".$model->year."' ";
        $query .= "    AND examno       = '".$model->examno."' ";
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
        $query .= "      T1.exam_type,";
        $query .= "      T1.judgediv,";
        $query .= "      T1.honordiv as honordiv2,";
        $query .= "      T1.adjournmentdiv,";
        $query .= "      T1.procedurediv1,";
        $query .= "      T1.proceduredate1,";
        $query .= "      T1.total2,";
        $query .= "      T1.avarage2,";
        $query .= "      T1.total_rank2,";
        $query .= "      T1.div_rank2,";
        $query .= "      T1.total4,";
        $query .= "      T1.avarage4,";
        $query .= "      T1.total_rank4,";
        $query .= "      T1.div_rank4,";
        $query .= "      T1.judge_deviation,";
        $query .= "      T1.link_judge_deviation,";
        $query .= "      T2.name1,";
        $query .= "      T3.examhall_name"; //←追加　表示用
        $query .= "  FROM";
        $query .= "      entexam_recept_dat T1 ";
        $query .= "      LEFT OUTER JOIN v_name_mst       T2 ";
        $query .= "                   ON T1.entexamyear = T2.year AND T2.namecd1 = 'L005' AND T2.namecd2 = T1.exam_type";
        $query .= "      LEFT OUTER JOIN entexam_hall_dat T3 ";
        $query .= "                   ON T1.testdiv = T3.testdiv AND '1' = t3.exam_type AND T1.receptno between t3.s_receptno and t3.e_receptno";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";
        $query .= "      T1.examno      = '".$model->examno."' AND ";
        $query .= "      T1.testdiv     = '".$model->testdiv2."' ";
        //echo $query;

        return $query;
    }

    //志願者データ取得
    function getDesire($model)
    {
        $query  = " SELECT ";
        $query .= "     applicant_div, ";
        $query .= "     examinee_div ";
        $query .= " FROM ";
        $query .= "     entexam_desire_dat ";
        $query .= " WHERE ";
        $query .= "     entexamyear     = '".$model->year."' AND ";
        $query .= "     examno          = '".$model->examno."' AND ";
        $query .= "     testdiv         = '".$model->testdiv2."' ";
        
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
        $query .= "     testdiv = '".$model->testdiv2."' AND ";
        $query .= "     (applicantdiv,receptno,exam_type) IN (SELECT ";
        $query .= "                                 applicantdiv,receptno,exam_type ";
        $query .= "                              FROM ";
        $query .= "                                 entexam_recept_dat ";
        $query .= "                              WHERE ";
        $query .= "                                 entexamyear = '".$model->year."' AND ";
        $query .= "                                 testdiv     = '".$model->testdiv2."' AND ";
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

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 1.志願者受付データの更新
            $data = array();
            $data["JUDGEDIV"][TEXT]         = $model->field["JUDGEDIV"];
            $data["HONORDIV"][TEXT]         = $model->field["HONORDIV2"];
            $data["ADJOURNMENTDIV"][TEXT]   = $model->field["ADJOURNMENTDIV"];
            $data["PROCEDUREDIV1"][TEXT]    = $model->field["PROCEDUREDIV1"];
            $data["PROCEDUREDATE1"][TEXT]   = str_replace("/","-",$model->field["PROCEDUREDATE1"]);

            $where  = " WHERE entexamyear   = '".$model->year."' AND ";
            $where .= "       examno        = '".$model->examno."' AND ";
            $where .= "       testdiv       = '".$model->testdiv2."'";
            $query = Query::updateSQL($data, "entexam_recept_dat", $where);
            $db->query($query);


        // 2.志願者基礎データの更新

            $data = array();

            $data["INTERVIEW_ATTEND_FLG"][TEXT] = $model->field["INTERVIEW_ATTEND_FLG"];
            $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
            $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
            $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
            $data["JUDGEMENT"][TEXT]            = $model->judgement;
            $data["SPECIAL_MEASURES"][TEXT]     = $model->field["SPECIAL_MEASURES"];
            $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
            $data["PROCEDUREDATE"][TEXT]        = str_replace("/","-",$model->field["PROCEDUREDATE"]);
            $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
            $data["HONORDIV"][TEXT]             = $model->field["HONORDIV1"];
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
            $data["registercd"][TEXT]           = STAFFCD;
            $data["updated"][FUNC]              = "sysdate()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
            $where .= "       EXAMNO        = '".$model->examno."'";
            $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
            $db->query($query);


        // 3.志願者データの更新
            $query  = " SELECT * FROM entexam_desire_dat ";
            $query .= " WHERE entexamyear   = '".$model->year."' AND ";
            $query .= "       examno        = '".$model->examno."' AND ";
            $query .= "       testdiv       = '".$model->testdiv2."'";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //データがあれば更新
            if (is_array($row)) {
                $query  = " SELECT * FROM entexam_recept_dat ";
                $query .= " WHERE entexamyear   = '".$model->year."' AND ";
                $query .= "       examno        = '".$model->examno."' AND ";
                $query .= "       testdiv       = '".$model->testdiv2."'";

                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($row)) {
                    if ((strlen($model->field["APPLICANT_DIV"])) && (strlen($model->field["EXAMINEE_DIV"]))) {
                        $data = array();
                        $data["APPLICANT_DIV"][TEXT] = $model->field["APPLICANT_DIV"];
                        $data["EXAMINEE_DIV"][TEXT]  = $model->field["EXAMINEE_DIV"];

                        $where  = " WHERE entexamyear   = '".$model->year."' AND ";
                        $where .= "       examno        = '".$model->examno."' AND ";
                        $where .= "       testdiv       = '".$model->testdiv2."'";
                        $query = Query::updateSQL($data, "entexam_desire_dat", $where);
                        $db->query($query);
                    }
                }
            }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>