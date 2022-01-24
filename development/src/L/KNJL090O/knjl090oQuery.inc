<?php

require_once('for_php7.php');

class knjl090oQuery extends Query 
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

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS NAMECD2, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }
    
    //志望区分マスタより出願コースを取得
    function get_desirediv($year)
    {
        $query = "  SELECT
                        t1.desirediv,
                        t2.examcourse_name
                    FROM
                        entexam_wishdiv_mst t1
                        LEFT OUTER JOIN entexam_course_mst t2
                                     ON t1.entexamyear  = t2.entexamyear AND
                                        t1.coursecd     = t2.coursecd AND
                                        t1.majorcd      = t2.majorcd AND
                                        t1.examcoursecd = t2.examcoursecd
                    WHERE
                        t1.entexamyear  = '".$year."' AND
                        T1.wishno       ='1'
                ";
        return $query;
    }

    //志望区分マスタより志望学科を取得
    function get_coursemajor($model)
    {
        $query  = " SELECT ";
        $query .= "     t1.coursecd, t1.majorcd, t1.examcoursecd, ";
        $query .= "     RTRIM(t4.examcourse_name) AS coursename ";
        $query .= " FROM ";
        $query .= "     entexam_wishdiv_mst t1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     entexam_course_mst t4 ";
        $query .= " ON ";
        $query .= "     t1.entexamyear = t4.entexamyear and ";
        $query .= "     t1.coursecd || t1.majorcd || t1.examcoursecd = t4.coursecd || t4.majorcd || t4.examcoursecd ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' and ";
        $query .= "     t1.desirediv    = '".$model->desirediv."'";
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
        /*
        $query  = " SELECT ";
        $query .= "     T1.entexamyear,T1.examno,T1.name,T1.name_kana,T1.birth_y, ";
        $query .= "     T1.birth_m,T1.birth_d,T1.sex,T1.fs_grdyear,T1.applicantdiv, ";
        $query .= "     T1.testdiv,T1.desirediv,T1.suc_coursecd,T1.suc_majorcd, ";
        $query .= "     T1.suc_coursecode,T1.judgement,T1.special_measures, T1.procedurediv,T1.entdiv, ";
        $query .= "     T1.honordiv,T1.remark1,T1.remark2, ";
        $query .= "     T1.success_noticeno,T1.failure_noticeno,T1.interview_attend_flg, ";
        $query .= "     T2.name1 AS sexname, ";
        $query .= "     T4.confidential_rpt01,T4.confidential_rpt02,T4.confidential_rpt03,T4.confidential_rpt04,T4.confidential_rpt05,T4.confidential_rpt06, ";
        $query .= "     T4.confidential_rpt07,T4.confidential_rpt08,T4.confidential_rpt09,T4.confidential_rpt10,T4.confidential_rpt11,T4.confidential_rpt12, ";
        $query .= "     T4.average5,T4.average_all,T4.absence_days, ";
        $query .= "     T5.name2 AS era_name ";
        $query .= " FROM ";
        $query .= "      entexam_applicantbase_dat T1 ";
        $query .= " LEFT OUTER JOIN v_name_mst T2 ";
        $query .= "      ON T2.year    = T1.entexamyear ";
        $query .= "      AND T2.namecd2 = T1.sex ";
        $query .= "      AND T2.namecd1 = 'Z002' ";
        $query .= " LEFT OUTER JOIN entexam_applicantconfrpt_dat T4 ";
        $query .= "      ON T4.entexamyear = T1.entexamyear ";
        $query .= "     AND T4.examno = T1.examno ";
        $query .= " LEFT OUTER JOIN v_name_mst T5 ";
        $query .= "      ON T5.year    = T1.entexamyear ";
        $query .= "     AND T5.namecd2 = T1.eracd ";
        $query .= "     AND T5.namecd1 = 'L007' ";
        $query .= " WHERE ";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query.= " T1.examno < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.examno DESC ";
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query.= " T1.examno > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.examno ASC ";
        } else {
            $query.= " T1.examno = '".$model->examno."'";
        }
        */

        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.entexamyear,";
        $query .= "      T1.examno,";
        $query .= "      T1.name,";
        $query .= "      T1.name_kana,";
        $query .= "      T1.birth_y,";
        $query .= "      T1.birth_m,";
        $query .= "      T1.birth_d,";
        $query .= "      T1.sex,";
        $query .= "      T1.fs_grdyear,";
        $query .= "      T1.applicantdiv,";
        $query .= "      T1.testdiv,";
        $query .= "      T1.testdiv0,";
        $query .= "      T1.testdiv1,";
        $query .= "      T1.testdiv2,";
        $query .= "      T1.testdiv3,";
        $query .= "      T1.testdiv4,";
        $query .= "      T1.testdiv5,";
        $query .= "      T1.testdiv6,";
        $query .= "      T1.desirediv,";
        $query .= "      T1.suc_coursecd,";
        $query .= "      T1.suc_majorcd,";
        $query .= "      T1.suc_coursecode,";
        $query .= "      T1.judgement,";
        $query .= "      MEISYOU_GET(T1.judgement,'L013',1) AS judgement_name,";
        $query .= "      case when T1.special_measures in ('1','2') then T1.special_measures end AS special_measures,";
        $query .= "      case when T1.special_measures in ('3')     then T1.special_measures end AS special_measures3,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.entdiv,";
        $query .= "      T1.entclass,";
        $query .= "      T1.honordiv,";
        $query .= "      T1.remark1,";
        $query .= "      T1.remark2,";
        $query .= "      T1.success_noticeno,";
        $query .= "      T1.failure_noticeno,";
        $query .= "      T1.interview_attend_flg,";
        $query .= "      T2.name1 AS sexname,";
        $query .= "      T4.confidential_rpt01,";
        $query .= "      T4.confidential_rpt02,";
        $query .= "      T4.confidential_rpt03,";
        $query .= "      T4.confidential_rpt04,";
        $query .= "      T4.confidential_rpt05,";
        $query .= "      T4.confidential_rpt06,";
        $query .= "      T4.confidential_rpt07,";
        $query .= "      T4.confidential_rpt08,";
        $query .= "      T4.confidential_rpt09,";
        $query .= "      T4.confidential_rpt10,";
        $query .= "      T4.confidential_rpt11,";
        $query .= "      T4.confidential_rpt12,";
        $query .= "      T4.average5,";
        $query .= "      T4.average_all,";
        $query .= "      T4.absence_days,";
        $query .= "      T5.name1 AS era_name";
        $query .= "  FROM";
        $query .= "      entexam_applicantbase_dat T1 ";
        $query .= "      LEFT OUTER JOIN v_name_mst T2 ";
        $query .= "                   ON T2.year = T1.entexamyear AND T2.namecd2 = T1.sex   AND T2.namecd1 = 'Z002' ";
        $query .= "      LEFT OUTER JOIN entexam_applicantconfrpt_dat T4 ";
        $query .= "                   ON T4.entexamyear = T1.entexamyear AND T4.examno = T1.examno ";
        $query .= "      LEFT OUTER JOIN v_name_mst T5 ";
        $query .= "                   ON T5.year = T1.entexamyear AND T5.namecd2 = T1.eracd AND T5.namecd1 = 'L007'";
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
        $query .= "      T1.judgeclass,";
        $query .= "      T1.honordiv,";
        $query .= "      T1.adjournmentdiv,";
        $query .= "      T1.total2,";
        $query .= "      T1.avarage2,";
        $query .= "      T1.total_rank2,";
        $query .= "      T1.div_rank2,";
        $query .= "      T1.total4,";
        $query .= "      T1.avarage4,";
        $query .= "      T1.total_rank4,";
        $query .= "      T1.div_rank4,";
        $query .= "      T1.katen,";
        $query .= "      T2.name1,";
        $query .= "      T3.examhall_name"; //←追加　表示用
        $query .= "  FROM";
        $query .= "      entexam_recept_dat T1 ";
        $query .= "      LEFT OUTER JOIN v_name_mst       T2 ";
        $query .= "                   ON T1.entexamyear = T2.year AND T2.namecd1 = 'L005' AND T2.namecd2 = T1.exam_type";
        $query .= "      LEFT OUTER JOIN entexam_hall_dat T3 ";
        $query .= "                   ON T1.testdiv = T3.testdiv AND T1.exam_type = t3.exam_type AND T1.receptno between t3.s_receptno and t3.e_receptno";
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
        $query .= "     testsubclasscd,attend_flg,score,std_score,rank ";
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
            $data["JUDGECLASS"][TEXT]       = $model->field["JUDGECLASS"];

            $where  = " WHERE entexamyear   = '".$model->year."' AND ";
            $where .= "       examno        = '".$model->examno."' AND ";
            $where .= "       testdiv       = '".$model->testdiv2."'";
            $query = Query::updateSQL($data, "entexam_recept_dat", $where);
            $db->query($query);


        // 2.志願者基礎データの更新

            $data = array();

#            $query  = " SELECT * FROM entexam_recept_dat ";
#            $query .= " WHERE entexamyear   = '".$model->year."' AND ";
#            $query .= "       examno        = '".$model->examno."' AND ";
#            $query .= "       judgediv      = '1'"; //合格
#            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
#            //１度でも合格していればデータがあれば更新
#            if (is_array($row)) {
#                $data["JUDGEMENT"][TEXT]    = "1";  //合格
#            }else{
#                $data["JUDGEMENT"][TEXT]    = "2";  //不合格
#            }

            $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
            $data["DESIREDIV"][TEXT]            = $model->field["DESIREDIV"];
            $data["INTERVIEW_ATTEND_FLG"][TEXT] = $model->field["INTERVIEW_ATTEND_FLG"];
            $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
            $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
            $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
#            $data["JUDGEMENT"][TEXT]            = $model->field["JUDGEMENT"];
            $data["JUDGEMENT"][TEXT]            = $model->judgement;
            $special_measures = "";
            if (strlen($model->field["SPECIAL_MEASURES"])) {
                $special_measures = $model->field["SPECIAL_MEASURES"];
            } else if (strlen($model->field["SPECIAL_MEASURES3"])) {
                $special_measures = $model->field["SPECIAL_MEASURES3"];
            }
            $data["SPECIAL_MEASURES"][TEXT]     = $special_measures;
            $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
            $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
            $data["ENTCLASS"][TEXT]             = $model->field["ENTCLASS"];
            $data["HONORDIV"][TEXT]             = $model->field["HONORDIV1"];
            $data["SUCCESS_NOTICENO"][TEXT]     = (strlen($model->field["SUCCESS_NOTICENO"])? sprintf("%04d",$model->field["SUCCESS_NOTICENO"]) : "");
            $data["FAILURE_NOTICENO"][TEXT]     = (strlen($model->field["FAILURE_NOTICENO"])? sprintf("%04d",$model->field["FAILURE_NOTICENO"]) : "");
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
                    $query  = " DELETE FROM entexam_desire_dat";
                    $query .= " WHERE entexamyear   = '".$model->year."' AND ";
                    $query .= "       examno        = '".$model->examno."' AND ";
                    $query .= "       testdiv       = '".$model->testdiv2."'";

                    $db->query($query);
                    if ((strlen($model->field["APPLICANT_DIV"])) && (strlen($model->field["EXAMINEE_DIV"]))) {
                        $data = array();
                        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
                        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
                        $data["TESTDIV"][TEXT]       = $model->testdiv2;
                        $data["EXAMNO"][TEXT]        = $model->examno;
                        $data["APPLICANT_DIV"][TEXT] = $model->field["APPLICANT_DIV"];
                        $data["EXAMINEE_DIV"][TEXT]  = $model->field["EXAMINEE_DIV"];
                        $data["REGISTERCD"][TEXT]    = STAFFCD;
                        $data["UPDATED"][FUNC]       = "sysdate()";

                        $query = Query::insertSQL($data, "entexam_desire_dat");
                        $db->query($query);
                    }
                }

            } else {
                $query  = " DELETE FROM entexam_desire_dat";
                $query .= " WHERE entexamyear   = '".$model->year."' AND ";
                $query .= "       examno        = '".$model->examno."' AND ";
                $query .= "       testdiv       = '".$model->testdiv2."'";

                $db->query($query);
                if ((strlen($model->field["APPLICANT_DIV"])) && (strlen($model->field["EXAMINEE_DIV"]))) {
                    $data = array();
                    $data["ENTEXAMYEAR"][TEXT]   = $model->year;
                    $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
                    $data["TESTDIV"][TEXT]       = $model->testdiv2;
                    $data["EXAMNO"][TEXT]        = $model->examno;
                    $data["APPLICANT_DIV"][TEXT] = $model->field["APPLICANT_DIV"];
                    $data["EXAMINEE_DIV"][TEXT]  = $model->field["EXAMINEE_DIV"];
                    $data["REGISTERCD"][TEXT]    = STAFFCD;
                    $data["UPDATED"][FUNC]       = "sysdate()";

                    $query = Query::insertSQL($data, "entexam_desire_dat");
                    $db->query($query);
                }
            }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>