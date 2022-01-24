<?php

require_once('for_php7.php');

class knjl305oQuery extends Query 
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

    //社会、理科の得点データ NO001
    function selectScore($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND TESTSUBCLASSCD IN ('3','4') ";

        return $query;
    }

    //名称マスタよりデータを取得
    function GetName2($ObjYear, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     namecd2 || ':' || name1 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     year    = '".$ObjYear ."' ";
        $query .= "     AND namecd1 = '".$namecd1."' ";
		if ($namecd2 != "") {
	        $query .= "     AND namecd2 = '".$namecd2."' ";
		}
        $query .= " ORDER BY ";
        $query .= "     namecd1, ";
        $query .= "     namecd2 ";
        return $query;
    }

    //入試区分の最大値を取得
    function GetMaxDesirediv($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     namecd2 ";
        $query .= " FROM ";
        $query .= "     v_name_mst ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$ObjYear ."' ";
        $query .= "     AND namecd1 = 'L004' ";
        $query .= "     AND NAMECD2 = (SELECT ";
        $query .= "                        MAX(NAMECD2) ";
        $query .= "                    FROM ";
        $query .= "                        v_name_mst ";
        $query .= "                    WHERE ";
        $query .= "                        YEAR = '".$ObjYear ."' ";
        $query .= "                        AND NAMECD1 = 'L004' ";
        $query .= "                        AND NAMESPARE2 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     namecd2 ";
        return $query;
    }

    //試験会場データ
    function getHall($testdiv,$examtype)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '".$testdiv."' ";
        $query .= "     AND EXAM_TYPE = '".$examtype."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";
        return $query;
    }

    //試験会場データ
    function getHall2($year,$testdiv,$examtype,$applicant)
    {
        $query  = " WITH MAXRECEPT AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(RECEPTNO) AS RECEPTNO, ";
        $query .= "     CAST(MAX(RECEPTNO) AS SMALLINT)+1 AS CHANGERECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicant."' ";
        $query .= "     AND TESTDIV = '".$testdiv."' ";
        $query .= "     AND EXAM_TYPE = '".$examtype."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     t1.RECEPTNO, ";
        $query .= "     t1.CHANGERECEPTNO, ";
        $query .= "     t2.EXAMHALLCD, ";
        $query .= "     t2.EXAMHALL_NAME ";
        $query .= " FROM ";
        $query .= "     MAXRECEPT t1 ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_DAT t2 ON t2.TESTDIV = '".$testdiv."' ";
        $query .= "     AND t2.EXAM_TYPE = '".$examtype."' ";
        $query .= "     AND t1.CHANGERECEPTNO >= CAST(t2.S_RECEPTNO AS SMALLINT) ";
        $query .= "     AND t1.CHANGERECEPTNO <= CAST(t2.E_RECEPTNO AS SMALLINT) ";

        return $query;
    }

    //変更後受験型
    function get_change_data($year,$testdiv,$examtype,$hall,$applicant)
    {
        $query  = " WITH MAX_HALL_RECEPT AS ( ";
        $query .= " SELECT ";
        $query .= "     E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '".$testdiv."' ";
        $query .= "     AND EXAM_TYPE = '".$examtype."' ";
        $query .= "     AND EXAMHALLCD = '".$hall."' ";
        $query .= " ), MAXRECEPT AS ( ";
        $query .= " SELECT ";
        $query .= "     CAST(MAX(RECEPTNO) AS SMALLINT)+1 AS CHANGERECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicant."' ";
        $query .= "     AND TESTDIV = '".$testdiv."' ";
        $query .= "     AND EXAM_TYPE = '".$examtype."' ";
        $query .= "     AND RECEPTNO <= (SELECT ";
        $query .= "                          E_RECEPTNO ";
        $query .= "                      FROM ";
        $query .= "                          MAX_HALL_RECEPT) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CHANGERECEPTNO, ";
        $query .= "     E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     MAXRECEPT, ";
        $query .= "     MAX_HALL_RECEPT ";
        return $query;
    }

    //志望区分マスタより出願コースを取得
    function get_desirediv($year,$desirediv)
    {
        $query = "  SELECT
                        t1.desirediv || t2.examcourse_name
                    FROM
                        entexam_wishdiv_mst t1
                        LEFT OUTER JOIN entexam_course_mst t2
                                     ON t1.entexamyear  = t2.entexamyear AND
                                        t1.coursecd     = t2.coursecd AND
                                        t1.majorcd      = t2.majorcd AND
                                        t1.examcoursecd = t2.examcoursecd
                    WHERE
                        t1.entexamyear  = '".$year."' AND
                        T1.wishno       ='1' AND
                        T1.DESIREDIV    = '".$desirediv."'
                ";
        return $query;
    }

    //志望区分マスタより志望学科を取得
    function get_coursemajor($model)
    {
        $query  = " SELECT ";
        $query .= "     t1.coursecd || t1.majorcd || t1.examcoursecd || ";
        $query .= "     RTRIM(t2.coursename) || "; 
        $query .= "     RTRIM(t3.majorname) || ";
        $query .= "     RTRIM(t4.examcourse_name) AS coursename ";
        $query .= " FROM ";
        $query .= "     entexam_wishdiv_mst t1 ";
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
        $query .= " LEFT OUTER JOIN ";
        $query .= "     entexam_course_mst t4 ";
        $query .= " ON ";
        $query .= "     t1.entexamyear = t4.entexamyear and ";
        $query .= "     t1.coursecd || t1.majorcd || t1.examcoursecd = t4.coursecd || t4.majorcd || t4.examcoursecd ";
        $query .= " WHERE ";
        $query .= "     t1.entexamyear  = '".$model->year."' and ";
        $query .= "     T1.wishno       ='1' AND ";
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
        $query  = " SELECT ";
        $query .= "     t2.NAME, ";
        $query .= "     t2.NAME_KANA, ";
        $query .= "     t2.SEX, ";
        $query .= "     t2.SEX || ':' || t3.NAME1 AS SEXNAME, ";
        $query .= "     t4.NAME1 || t2.BIRTH_Y || '年' || ";
        $query .= "     t2.BIRTH_M || '月' || ";
        $query .= "     t2.BIRTH_D || '日' AS BIRTHDAY, ";
        $query .= "     t2.FS_NAME, ";
        $query .= "     t1.RECEPTNO, ";
        $query .= "     t1.EXAMNO, ";
        $query .= "     t1.EXAM_TYPE, ";
        $query .= "     t1.APPLICANTDIV, ";
        $query .= "     t5.NAME1 APPLICANTDIVNM, ";
        $query .= "     t6.NAME1 AS TESTDIVNM, ";
        $query .= "     t7.EXAMHALLCD, ";
        $query .= "     t7.EXAMHALL_NAME, ";
        $query .= "     t7.S_RECEPTNO, ";
        $query .= "     t7.E_RECEPTNO, ";
        $query .= "     t8.NAME1 AS EXAM_TYPENM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT t1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT t2 ON t2.ENTEXAMYEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t2.EXAMNO = t1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST t3 ON t3.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t3.NAMECD1 = 'Z002' ";
        $query .= "     AND t3.NAMECD2 = t2.SEX ";
        $query .= "     LEFT JOIN V_NAME_MST t4 ON t4.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t4.NAMECD1 = 'L007' ";
        $query .= "     AND t4.NAMECD2 = t2.ERACD ";
        $query .= "     LEFT JOIN V_NAME_MST t5 ON t5.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t5.NAMECD1 = 'L003' ";
        $query .= "     AND t5.NAMECD2 = t2.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST t6 ON t6.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t6.NAMECD1 = 'L004' ";
        $query .= "     AND t6.NAMECD2 = t1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_DAT t7 ON t7.TESTDIV = t1.TESTDIV ";
        $query .= "     AND t7.EXAM_TYPE = t1.EXAM_TYPE ";
        $query .= "     AND t1.RECEPTNO BETWEEN t7.S_RECEPTNO AND t7.E_RECEPTNO ";
        $query .= "     LEFT JOIN V_NAME_MST t8 ON t8.YEAR = t1.ENTEXAMYEAR ";
        $query .= "     AND t8.NAMECD1 = 'L005' ";
        $query .= "     AND t8.NAMECD2 = t1.EXAM_TYPE ";
        $query .= " WHERE ";
        $query .= "     t1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND t1.TESTDIV = '".$model->testdiv."' ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query .= "      AND t1.examno < '".$model->examno."'";
            $query .= " ORDER BY T1.examno DESC ";
            $query .= " FETCH FIRST 1 ROWS ONLY "; 
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query .= "      AND t1.examno > '".$model->examno."'";
            $query .= " ORDER BY T1.examno ASC ";
            $query .= " FETCH FIRST 1 ROWS ONLY "; 
        } else {
            $query .= "      AND t1.examno = '".$model->examno."'";
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
                        RECEPTNO
                    FROM
                        ENTEXAM_RECEPT_DAT
                    WHERE
                        ENTEXAMYEAR  = '".$model->year."' AND
                        APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND
                        TESTDIV      = '".$model->field["TESTDIV"]."' AND
                        EXAM_TYPE    = '".$model->field["CHANGE_EXAM_TYPE"]."' AND
                        RECEPTNO     = '".$model->field["CHANGE_RECEPTNO"]."'";

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
		$data["EXAM_TYPE"][TEXT]        = $model->field["CHANGE_EXAM_TYPE"];
		$data["RECEPTNO"][TEXT]         = $model->field["CHANGE_RECEPTNO"];
		$data["REGISTERCD"][TEXT]       = STAFFCD;
		$data["UPDATED"][FUNC]          = "sysdate()";

		$where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
		$where .= "       APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' AND ";
		$where .= "       TESTDIV       = '".$model->field["TESTDIV"]."' AND ";
		$where .= "       EXAM_TYPE     = '".$model->field["EXAM_TYPE"]."' AND ";
		$where .= "       RECEPTNO      = '".$model->field["RECEPTNO"]."' AND ";
		$where .= "       EXAMNO        = '".$model->examno."' ";
		$query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
		$db->query($query);

		// 1.得点データの更新
		$data = array();
		$data["EXAM_TYPE"][TEXT]        = $model->field["CHANGE_EXAM_TYPE"];
		$data["RECEPTNO"][TEXT]         = $model->field["CHANGE_RECEPTNO"];
		$data["REGISTERCD"][TEXT]       = STAFFCD;
		$data["UPDATED"][FUNC]          = "sysdate()";

		$where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
		$where .= "       APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' AND ";
		$where .= "       TESTDIV       = '".$model->field["TESTDIV"]."' AND ";
		$where .= "       EXAM_TYPE     = '".$model->field["EXAM_TYPE"]."' AND ";
		$where .= "       RECEPTNO      = '".$model->field["RECEPTNO"]."' ";
		$query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
		$db->query($query);

		$db->commit();
		Query::dbCheckIn($db);
		return;
    }

}
?>