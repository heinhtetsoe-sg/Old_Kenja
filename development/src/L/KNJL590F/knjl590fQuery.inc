<?php

require_once('for_php7.php');

class knjl590fQuery extends Query 
{

    //名称マスタよりデータを取得
    function getNameMst($ObjYear, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     *, ";
        $query .= "     NAMECD2 || '' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$ObjYear ."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }

    //名称マスタよりデータを取得
    function getName($ObjYear, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$ObjYear ."' AND ";
        $query .= "     NAMECD1 IN ('".implode($namecd1, "','") ."') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";
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
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '3' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAME3 AS DATE, ";
        $query .= "         '4' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L044' ";
        $query .= "         AND NAMECD2 = '{$testdiv}' ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         NAMESPARE1 AS DATE, ";
        $query .= "         '5' AS TESTDIV0 ";
        $query .= "     FROM ";
        $query .= "         V_NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "         AND NAMECD1 = 'L059' ";
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
    function getSucCourse($year, $applicantdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入学課程コース取得
    function getCourseMajorCoursecode($year, $applicantdiv) {
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
        $query .= "     AND T1.TESTDIV      = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     APP_B.APPLICANTDIV, ";
        $query .= "     APP_B.EXAMNO, ";
        $query .= "     APP_B.NAME, ";
        $query .= "     APP_B.NAME_KANA, ";
        $query .= "     BASE_D_001.REMARK8 || '-' || BASE_D_001.REMARK9 || '-' || BASE_D_001.REMARK10 AS EXAMCOURSE, ";
        $query .= "     COURSE1.EXAMCOURSE_NAME, ";
        $query .= "     BASE_D_017.REMARK1 || '-' || BASE_D_017.REMARK2 || '-' || BASE_D_017.REMARK3 AS EXAMCOURSE2, ";
        $query .= "     COURSE2.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2, ";
        $query .= "     APP_B.JUDGEMENT, ";
        $query .= "     APP_B.TESTDIV, ";
        $query .= "     L055.NAMESPARE2 AS MINASHI, ";
        $query .= "     APP_B.SUC_COURSECD || APP_B.SUC_MAJORCD || APP_B.SUC_COURSECODE AS COURSEMAJOR, ";
        $query .= "     APP_B.PROCEDUREDIV, ";
        $query .= "     APP_B.PROCEDUREDATE, ";
        $query .= "     APP_B.ENTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT APP_B ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT APP_ADDR ON APP_B.ENTEXAMYEAR = APP_ADDR.ENTEXAMYEAR ";
        $query .= "     AND APP_B.APPLICANTDIV = APP_ADDR.APPLICANTDIV ";
        $query .= "     AND APP_B.EXAMNO = APP_ADDR.EXAMNO ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_001 ";
        $query .= "       ON APP_B.ENTEXAMYEAR = BASE_D_001.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = BASE_D_001.APPLICANTDIV ";
        $query .= "      AND APP_B.EXAMNO = BASE_D_001.EXAMNO ";
        $query .= "      AND BASE_D_001.SEQ = '001' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_017 ";
        $query .= "       ON APP_B.ENTEXAMYEAR = BASE_D_017.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = BASE_D_017.APPLICANTDIV ";
        $query .= "      AND APP_B.EXAMNO = BASE_D_017.EXAMNO ";
        $query .= "      AND BASE_D_017.SEQ = '017' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_005 ";
        $query .= "       ON APP_B.ENTEXAMYEAR = BASE_D_005.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = BASE_D_005.APPLICANTDIV ";
        $query .= "      AND APP_B.EXAMNO = BASE_D_005.EXAMNO ";
        $query .= "      AND BASE_D_005.SEQ = '005' ";
        $query .= " LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ";
        $query .= "       ON APP_B.ENTEXAMYEAR = COURSE1.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = COURSE1.APPLICANTDIV ";
        $query .= "      AND COURSE1.TESTDIV = '1' ";
        $query .= "      AND BASE_D_001.REMARK8  = COURSE1.COURSECD ";
        $query .= "      AND BASE_D_001.REMARK9  = COURSE1.MAJORCD ";
        $query .= "      AND BASE_D_001.REMARK10 = COURSE1.EXAMCOURSECD ";
        $query .= " LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ";
        $query .= "       ON APP_B.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ";
        $query .= "      AND APP_B.APPLICANTDIV = COURSE2.APPLICANTDIV ";
        $query .= "      AND COURSE2.TESTDIV = '1' ";
        $query .= "      AND BASE_D_017.REMARK1 = COURSE2.COURSECD ";
        $query .= "      AND BASE_D_017.REMARK2 = COURSE2.MAJORCD ";
        $query .= "      AND BASE_D_017.REMARK3 = COURSE2.EXAMCOURSECD ";
        $query .= " LEFT JOIN NAME_MST L055 ";
        $query .= "       ON L055.NAMECD1 = 'L055' ";
        $query .= "      AND BASE_D_005.REMARK1 = L055.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     APP_B.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APP_B.APPLICANTDIV = '{$model->applicantdiv}' AND ";

        if ($model->cmd == "back1" || $model->cmd == "back2") {
            $query.= " APP_B.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     APP_B.EXAMNO DESC ";
        } elseif ($model->cmd == "next1" || $model->cmd == "next2") {
            $query.= " APP_B.EXAMNO > '".$model->examno."'";
//            $query.= " APP_B.EXAMNO > '".sprintf("%05d", $model->examno)."'";
            $query.= " ORDER BY ";
            $query.= "     APP_B.EXAMNO ASC ";
        } else {
            $query.= " APP_B.EXAMNO = '".$model->examno."'";
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
        $query .= "  FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE entexamyear = '".$model->year."'";
        $query .= "   AND examno      = '".$model->examno."'";
        $query .= "   AND testdiv    <> '".$model->testdiv2."'";
        return $query;
    }

    //合格した入試区分(baseの入試区分と同じ)取得
    function getMintestdiv($model, $applicantdiv)
    {
        $query  = " SELECT min(R1.testdiv) ";
        $query .= "   FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "        inner join entexam_applicantbase_dat B1 ";
        $query .= "                 ON B1.entexamyear  = R1.entexamyear ";
        $query .= "                AND B1.applicantdiv = R1.applicantdiv ";
        $query .= "                AND B1.testdiv      = R1.testdiv ";
        $query .= "                AND B1.examno       = R1.examno ";
        $query .= "  WHERE R1.entexamyear  = '".$model->year."' ";
        $query .= "    AND R1.applicantdiv = '".$applicantdiv."' ";
        $query .= "    AND R1.examno       = '".$model->examno."' ";
        $query .= "    AND R1.judgediv     = '1' ";

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
        $query .= "     RECEPT.RECEPTNO, ";
        $query .= "     RECEPT.EXAMNO, ";
        $query .= "     RECEPT.TOTAL4, ";
        $query .= "     RECEPT.AVARAGE4, ";
        $query .= "     RECEPT.TOTAL_RANK4, ";
        $query .= "     RECEPT.JUDGE_DEVIATION ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' AND L013.NAMECD2 = RECEPT.JUDGEDIV ";
        $query .= " WHERE ";
        $query .= "     RECEPT.ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     RECEPT.APPLICANTDIV = '{$applicantdiv}' AND ";
        $query .= "     RECEPT.TESTDIV      = '{$model->testdiv2}' AND ";
        $query .= "     RECEPT.EXAMNO       = '{$model->examno}' ";

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
                        APPLICANTDIV = '".$model->applicantdiv."' AND
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

        //合格した入試区分(baseの入試区分と同じ)を取得
        $testdiv2 = $db->getOne(knjl590fQuery::getMintestdiv($model, "2"));

        // 1.志願者受付データの更新
        // 主に、手続日を訂正する時に更新したい。現状、合格者を対象としておく。
        // 手続日、手続区分を更新
        if (strlen($testdiv2)) {
            $data = array();
            $data["PROCEDUREDATE1"][TEXT]       = !strlen($model->field["PROCEDUREDATE"]) ? "" : str_replace("/", "-", $model->field["PROCEDUREDATE"]);
            if (strlen($model->field["PROCEDUREDATE"])) {
                $data["PROCEDUREDIV1"][TEXT]    = !strlen($model->field["PROCEDUREDIV"]) ? "1" : $model->field["PROCEDUREDIV"];
            } else {
                $data["PROCEDUREDIV1"][TEXT]    = $model->field["PROCEDUREDIV"] == "1" ? "" : $model->field["PROCEDUREDIV"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //合格した入試区分(baseの入試区分と同じ)のレコードを更新
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '2' ";
            $where .= "   AND TESTDIV       = '".$testdiv2."' ";
            $where .= "   AND EXAMNO        = '".$model->examno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);

            $db->query($query);
        }

        // 2.志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]         = $model->field["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = $model->field["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = $model->field["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->field["JUDGEMENT"];
        $data["PROCEDUREDATE"][TEXT]        = !strlen($model->field["PROCEDUREDATE"]) ? "" : str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        // 3.入学コースの更新
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["SUC_COURSECD"];
        $data["REMARK2"][TEXT]              = $model->field["SUC_MAJORCD"];
        $data["REMARK3"][TEXT]              = $model->field["SUC_COURSECODE"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $query  = " SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
        $query .= "       EXAMNO        = '".$model->examno."' AND";
        $query .= "       SEQ           = '007' ";
        $cnt = $db->getOne($query);
        if ($cnt > 0) {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
            $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND";
            $where .= "       EXAMNO        = '".$model->examno."' AND";
            $where .= "       SEQ           = '007' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            $db->query($query);
        } else {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = '007';
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>
