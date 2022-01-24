<?php

require_once('for_php7.php');

class knjl090gQuery extends Query 
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

    //入試制度を取得
    function get_name_cd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試回数(2:高校のみ)
    function getTestdiv0($year, $testdiv, $testdiv0) {
        $query  = " WITH T_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '1' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '2' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L004' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 || '（' || L1.DATE || '）' AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN T_DATE L1 ON L1.TESTDIV0 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.NAMECD1 = 'L034' ";
        $query .= "     AND NAMECD2 = '{$testdiv0}' ";
        $query .= "     AND L1.DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格コース
    function getSucCourse($year, $applicantdiv, $testdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入学課程コース取得
    function getCourseMajorCoursecode($year, $applicantdiv, $testdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE AS VALUE, ";
        $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE || ':' || L2.COURSECODENAME AS LABEL ";
//      $query .= "     T1.ENTER_COURSECD || T1.ENTER_MAJORCD || T1.ENTER_COURSECODE || ':' || L1.COURSENAME || L1.MAJORNAME || '　' || L2.COURSECODENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L1.COURSECD = T1.ENTER_COURSECD ";
        $query .= "                                    AND L1.MAJORCD = T1.ENTER_MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L2   ON L2.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L2.COURSECODE = T1.ENTER_COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
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
        $query .= "      N1.name1 AS testdivname,";
        $query .= "      T1.shdiv,";
        $query .= "      N3.name1 AS shdivname,";
        $query .= "      T1.DAI2_SENPEIDIV AS shdiv2,";
        $query .= "      N4.name1 AS shdivname2,";
        $query .= "      T1.DAI1_COURSECD || T1.DAI1_MAJORCD || T1.DAI1_COURSECODE AS EXAMCOURSE, ";
        $query .= "      T1.DAI2_COURSECD || T1.DAI2_MAJORCD || T1.DAI2_COURSECODE AS EXAMCOURSE2, ";
        $query .= "      C1.EXAMCOURSE_NAME AS EXAMCOURSE_NAME, ";
        $query .= "      C2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2, ";
        $query .= "      T1.suc_coursecd || T1.suc_majorcd || T1.suc_coursecode as COURSEMAJOR,";
        $query .= "      E1.REMARK1 || E1.REMARK2 || E1.REMARK3 as ENTER_COURSEMAJOR,";
        $query .= "      T1.judgement,";
        $query .= "      T6.name1 AS judgement_name,";
        $query .= "      T1.JUDGE_KIND,";
        $query .= "      N2.NAME1 AS JUDGE_KIND_NAME,";
        $query .= "      T1.SPECIAL_REASON_DIV, ";
        $query .= "      T1.PROCEDUREDATE,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.entdiv,";
        $query .= "      T2.name1 AS sexname ";
        $query .= "  FROM";
        $query .= "      V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "      LEFT JOIN v_name_mst T2 ";
        $query .= "                   ON T2.year = T1.entexamyear AND T2.namecd1 = 'Z002' AND T2.namecd2 = T1.sex ";
        $query .= "      LEFT JOIN v_name_mst T6 ";
        $query .= "                   ON T6.year = T1.entexamyear AND T6.namecd1 = 'L013' AND T6.namecd2 = T1.judgement ";
        $query .= "      LEFT JOIN v_name_mst N0 ";
        $query .= "                   ON N0.year = T1.entexamyear AND N0.namecd1 = 'L003' AND N0.namecd2 = T1.applicantdiv ";
        $query .= "      LEFT JOIN v_name_mst N1 ";
        $query .= "                   ON N1.year = T1.entexamyear AND N1.namecd1 = 'L004' AND N1.namecd2 = T1.testdiv ";
        $query .= "      LEFT JOIN v_name_mst N2 ";
        $query .= "                   ON N2.year = T1.entexamyear AND N2.namecd1 = 'L025' AND N2.namecd2 = T1.JUDGE_KIND ";
        $query .= "      LEFT JOIN v_name_mst N3 ";
        $query .= "                   ON N3.year = T1.entexamyear AND N3.namecd1 = 'L006' AND N3.namecd2 = T1.shdiv ";
        $query .= "      LEFT JOIN v_name_mst N4 ";
        $query .= "                   ON N4.year = T1.entexamyear AND N4.namecd1 = 'L006' AND N4.namecd2 = T1.DAI2_SENPEIDIV ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "                   ON  C1.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "                   AND C1.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "                   AND C1.TESTDIV        = T1.TESTDIV ";
        $query .= "                   AND C1.COURSECD       = T1.DAI1_COURSECD ";
        $query .= "                   AND C1.MAJORCD        = T1.DAI1_MAJORCD ";
        $query .= "                   AND C1.EXAMCOURSECD   = T1.DAI1_COURSECODE ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST C2 ";
        $query .= "                   ON  C2.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
        $query .= "                   AND C2.APPLICANTDIV   = T1.APPLICANTDIV ";
        $query .= "                   AND C2.TESTDIV        = T1.TESTDIV ";
        $query .= "                   AND C2.COURSECD       = T1.DAI2_COURSECD ";
        $query .= "                   AND C2.MAJORCD        = T1.DAI2_MAJORCD ";
        $query .= "                   AND C2.EXAMCOURSECD   = T1.DAI2_COURSECODE ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT E1 ";
        $query .= "                   ON  E1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                   AND E1.EXAMNO = T1.EXAMNO ";
        $query .= "                   AND E1.SEQ = '007' ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";

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
    function getRecept($model, $applicantdiv) {
        $query  = "";
        $query .= " SELECT";
        $query .= "     T1.receptno,";
        $query .= "     T1.examno,";
        $query .= "     T1.testdiv AS testdiv2,";
        $query .= "     T1.JUDGEDIV,";
        $query .= "     T1.JUDGEDIV || ':' || N1.NAME1 AS JUDGEDIV_NAME,";
        $query .= "     T1.TOTAL4,";
        $query .= "     T1.AVARAGE4,";
        $query .= "     T1.DIV_RANK4,";
        $query .= "     T1.TOTAL_RANK4";
        $query .= " FROM";
        $query .= "     entexam_recept_dat T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE";
        $query .= "     T1.entexamyear  = '{$model->year}' AND ";
        $query .= "     T1.applicantdiv = '{$applicantdiv}' AND ";
        $query .= "     T1.testdiv      = '{$model->testdiv2}' AND ";
        $query .= "     T1.examno       = '{$model->examno}' ";

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

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     EXAMNO      = '".$model->examno."' ";
        $baseInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //受付データ
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->judgement;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        //条件
        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV  = '".$baseInfo["APPLICANTDIV"]."' ";
        $where .= "   AND TESTDIV       = '".$baseInfo["TESTDIV"]."' ";
        $where .= "   AND RECEPTNO      = '".$model->examno."' ";
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        // 2.志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]        = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);

        // 3.入学基礎詳細データの更新（入学コース）
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "       EXAMNO      = '".$model->examno."' AND ";
        $query .= "       SEQ         = '007' ";
        $db->query($query);

        if ($model->field["ENTER_COURSEMAJOR"]) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
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