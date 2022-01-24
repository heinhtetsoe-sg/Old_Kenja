<?php

require_once('for_php7.php');

class knjl090rQuery extends Query 
{
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

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $namecd2="")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($namecd2 != '') {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験コースマスタより志望学科を取得
    function get_coursemajor($year, $applicantdiv, $testdiv, $coursemajor = "")
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
        if (strlen($coursemajor)) {
            $query .= "     and t1.coursecd || t1.majorcd || t1.examcoursecd = '{$coursemajor}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     t1.coursecd, t1.majorcd, t1.examcoursecd ";

        return $query;
    }

    //受験区分
    function getDesirediv($year, $applicantdiv, $testdiv, $desirediv) {
        $query  = " SELECT ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.WISHNO, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.EXAMCOURSECD, ";
        $query .= "     T2.COURSECD || T2.MAJORCD || T2.EXAMCOURSECD AS EXAMCOURSE, ";
        $query .= "     T2.EXAMCOURSE_NAME, ";
        $query .= "     T2.EXAMCOURSE_MARK, ";
        $query .= "     T2.ENTER_COURSECD || T2.ENTER_MAJORCD || T2.ENTER_COURSECODE AS ENTER_COURSE, ";
        $query .= "     T3.COURSENAME || T3.MAJORNAME AS COURSEMAJORNAME, ";
        $query .= "     T4.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST T1 ";
        $query .= "     INNER JOIN ENTEXAM_COURSE_MST T2 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "         AND T2.COURSECD = T1.COURSECD ";
        $query .= "         AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "         AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST T3 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T3.YEAR ";
        $query .= "         AND T2.ENTER_COURSECD = T3.COURSECD ";
        $query .= "         AND T2.ENTER_MAJORCD = T3.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T4 ";
        $query .= "         ON  T2.ENTEXAMYEAR = T4.YEAR ";
        $query .= "         AND T2.ENTER_COURSECODE = T4.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV = '{$testdiv}' ";
        $query .= "     AND T1.DESIREDIV = '{$desirediv}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.DESIREDIV, ";
        $query .= "     T1.WISHNO ";

        return $query;
    }

    //入学課程コース取得
    function getCourseMajorCoursecode($year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE AS VALUE, ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE || ':' || L1.COURSENAME || L1.MAJORNAME || '　' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L1.COURSECD = T1.ENTER_COURSECD ";
        $query .= "                                    AND L1.MAJORCD = ENTER_MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2   ON L2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L2.COURSECODE = T1.ENTER_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }
    
    //事前データ取得
    function getEntexamApplicantBeforeDat($model, $year, $applicantdiv, $testdiv, $jizen_page, $jizen_seq)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.ABBV3 AS JIZEN_TEST_CD, ";
        $query .= "     L2.EXAMCOURSE_MARK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.NAMECD1 = 'L004' ";
        $query .= "                            AND L1.NAMECD2 = T1.TESTDIV ";
        $query .= "                            AND L1.YEAR    = T1.ENTEXAMYEAR ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L2 ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                    AND L2.TESTDIV = T1.TESTDIV ";
        $query .= "                                    AND L2.COURSECD = T1.BEFORE_COURSECD ";
        $query .= "                                    AND L2.MAJORCD = T1.BEFORE_MAJORCD ";
        $query .= "                                    AND L2.EXAMCOURSECD = T1.BEFORE_EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR   = '".$year."' ";
        $query .= " AND T1.APPLICANTDIV  = '".$applicantdiv."' ";
        $query .= " AND T1.TESTDIV       = '".$testdiv."' ";
        $query .= " AND T1.BEFORE_PAGE   = '".$jizen_page."' ";
        $query .= " AND T1.BEFORE_SEQ    = '".$jizen_seq."' ";

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
        $query .= "      T1.DESIREDIV, ";
        $query .= "      T1.applicantdiv,";
        $query .= "      N0.name1 AS applicantdivname,";
        $query .= "      T1.testdiv,";
        $query .= "      N1.name1 AS testdivname,";
        $query .= "      T1.suc_coursecd || T1.suc_majorcd || T1.suc_coursecode as COURSEMAJOR,";
        $query .= "      E1.REMARK1 || E1.REMARK2 || E1.REMARK3 as ENTER_COURSEMAJOR,";
        $query .= "      T1.judgement,";
        $query .= "      T6.name1 AS judgement_name,";
        $query .= "      T1.judge_kind,";
//        $query .= "      T1.special_measures,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.proceduredate,";
        $query .= "      T1.entdiv,";
        $query .= "      J1.REMARK1 AS JIZEN_PAGE, ";
        $query .= "      J1.REMARK2 AS JIZEN_SEQ, ";
        $query .= "      J1.REMARK1 || '-' || J1.REMARK2 AS JIZEN, ";
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
        $query .= "     LEFT OUTER JOIN ";
        $query .= "         ENTEXAM_APPLICANTBASE_DETAIL_DAT J1 ";
        $query .= "     ON ";
        $query .= "         J1.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND ";
        $query .= "         J1.APPLICANTDIV = T1.APPLICANTDIV AND ";
        $query .= "         J1.EXAMNO       = T1.EXAMNO AND ";
        $query .= "         J1.SEQ          = '002' ";
        $query .= "     LEFT OUTER JOIN ";
        $query .= "         ENTEXAM_APPLICANTBASE_DETAIL_DAT E1 ";
        $query .= "     ON ";
        $query .= "         E1.ENTEXAMYEAR  = T1.ENTEXAMYEAR AND ";
        $query .= "         E1.APPLICANTDIV = T1.APPLICANTDIV AND ";
        $query .= "         E1.EXAMNO       = T1.EXAMNO AND ";
        $query .= "         E1.SEQ          = '007' ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear  = '".$model->year."' AND ";
        $query .= "      T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "      T1.examno < '".$model->examno."'";
            $query .= " ORDER BY T1.examno DESC ";
            //$query .= " FETCH FIRST 1 ROWS ONLY "; 
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "      T1.examno > '".$model->examno."'";
            $query .= " ORDER BY T1.examno ASC ";
            //$query .= " FETCH FIRST 1 ROWS ONLY "; 
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
        $query .= "    AND applicantdiv = '".$applicantdiv."' ";
        $query .= "    AND examno       = '".$model->examno."' ";
        return $query;
    }

    //受付データのテーブル
    function getRecept($model, $applicantdiv)
    {
        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.receptno,"; //←追加　表示用
        $query .= "      T1.examno,";
        $query .= "      T1.testdiv AS testdiv2,";
        $query .= "      T1.exam_type,";
        $query .= "      T1.judgediv,";
        $query .= "      T1.PROCEDUREDIV1,";
        $query .= "      T1.PROCEDUREDATE1,";
        $query .= "      T1.total4,";
        $query .= "      T1.avarage4,";
        $query .= "      T1.total_rank4,";
        $query .= "      T1.div_rank4,";
        $query .= "      T3.examhall_name"; //←追加　表示用
        $query .= "  FROM";
        $query .= "      entexam_recept_dat T1 ";
        $query .= "      LEFT JOIN entexam_hall_ydat T3 ";
        $query .= "                   ON  T1.entexamyear = T3.entexamyear";
        $query .= "                   AND T1.applicantdiv = T3.applicantdiv";
        $query .= "                   AND T1.testdiv = T3.testdiv";
        $query .= "                   AND T1.exam_type = t3.exam_type";
        $query .= "                   AND T1.receptno between t3.s_receptno and t3.e_receptno";
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
    function Existdata($model)
    {
        $db = Query::dbCheckOut();

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR  = '".$model->year."' AND
                        APPLICANTDIV = '".$model->applicantdiv."' AND
                        EXAMNO       = '".$model->examno."'";

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
        $data["PROCEDUREDIV1"][TEXT]    = $model->field["PROCEDUREDIV1"];
        $data["PROCEDUREDATE1"][TEXT]   = str_replace("/", "-", $model->field["PROCEDUREDATE1"]);
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
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);
        
        // 3.入学基礎詳細データの更新（入学コース）
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' AND ";
        $query .= "       APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "       EXAMNO       = '".$model->examno."' AND ";
        $query .= "       SEQ          = '007' ";

        $db->query($query);
        if ($model->field["ENTER_COURSEMAJOR"]) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "007";
            $data["REMARK1"][TEXT]      = $model->field["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]      = $model->field["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]      = $model->field["ENTER_COURSECODE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>