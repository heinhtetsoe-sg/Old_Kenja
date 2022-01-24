<?php

require_once('for_php7.php');

class knjl091qQuery extends Query 
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

    //入試制度を取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号帯の受験区分取得
    function getKubun($model) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.NAME1 AS KUBUN_TESTDIV0, ";
        $query .= "     T3.NAME1 AS KUBUN_TESTDIV, ";
        $query .= "     T4.NAME1 AS KUBUN_REMARK_DIV, ";
        $query .= "     T5.EXAMHALL_NAME AS KUBUN_EXAMHALLCD, ";
        $query .= "     T6.EXAMCOURSE_NAME AS KUBUN_EXAMCOURSE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'L045' AND T2.NAMECD2 = T1.TESTDIV0 ";
        $query .= "     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'L004' AND T3.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'L046' AND T4.NAMECD2 = T1.REMARK_DIV ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T5.APPLICANTDIV = T1.APPLICANTDIV AND T5.TESTDIV = T1.TESTDIV AND T5.EXAMHALLCD = T1.EXAMHALLCD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST T6 ON T6.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T6.APPLICANTDIV = T1.APPLICANTDIV AND T6.TESTDIV = T1.TESTDIV AND T6.COURSECD = T1.COURSECD AND T6.MAJORCD = T1.MAJORCD AND T6.EXAMCOURSECD = T1.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND '".$model->examno."' BETWEEN T1.EXAMNO_FROM AND T1.EXAMNO_TO ";
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
        $query .= "      T1.testdiv,";
        $query .= "      T1.shdiv,";
        $query .= "      T1.judgement,";
        $query .= "      T1.SPECIAL_REASON_DIV, ";
        $query .= "      T1.PROCEDUREDATE,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.entdiv,";
        $query .= "      T2.name1 AS sexname ";
        $query .= "  FROM";
        $query .= "      V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "      LEFT JOIN v_name_mst T2 ON T2.year = T1.entexamyear AND T2.namecd1 = 'Z002' AND T2.namecd2 = T1.sex ";
        $query .= "  WHERE";
        $query .= "      T1.entexamyear = '".$model->year."' AND ";
        $query .= "      T1.APPLICANTDIV= '".$model->applicantdiv."' AND ";

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
    function getMaxtestdiv($model) {
        $query  = " SELECT max(testdiv) ";
        $query .= "   FROM entexam_recept_dat ";
        $query .= "  WHERE entexamyear  = '".$model->year."' ";
        $query .= "    AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "    AND examno       = '".$model->examno."' ";
        return $query;
    }

    //受付データのテーブル
    function getRecept($model) {
        $query  = "";
        $query .= " SELECT";
        $query .= "     T1.receptno,";
        $query .= "     T1.examno,";
        $query .= "     T1.testdiv AS testdiv2,";
        $query .= "     T1.JUDGEDIV,";
        $query .= "     T1.JUDGEDIV || ':' || N1.NAME1 AS JUDGEDIV_NAME,";
        $query .= "     T1.TOTAL4,";
        $query .= "     T1.AVARAGE4,";
        $query .= "     T1.JUDGE_DEVIATION,";
        $query .= "     T1.TOTAL_RANK4";
        $query .= " FROM";
        $query .= "     entexam_recept_dat T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = T1.JUDGEDIV ";
        $query .= " WHERE";
        $query .= "     T1.entexamyear  = '{$model->year}' AND ";
        $query .= "     T1.applicantdiv = '{$model->applicantdiv}' AND ";
        $query .= "     T1.testdiv      = '{$model->testdiv2}' AND ";
        $query .= "     T1.examno       = '{$model->examno}' ";

        return $query;
    }

    //得点データ取得
    function getScore($model) {
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
        $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
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
                        APPLICANTDIV = '".$model->applicantdiv."' AND
                        EXAMNO      = '".$model->examno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //合格・入学コース
    function getSucEntCourse($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     B1.DAI1_COURSECD AS SUC_COURSECD, ";
        $query .= "     B1.DAI1_MAJORCD AS SUC_MAJORCD, ";
        $query .= "     B1.DAI1_COURSECODE AS SUC_COURSECODE, ";
        $query .= "     C1.ENTER_COURSECD, ";
        $query .= "     C1.ENTER_MAJORCD, ";
        $query .= "     C1.ENTER_COURSECODE ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ON C1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND C1.TESTDIV = B1.TESTDIV ";
        $query .= "         AND C1.COURSECD = B1.DAI1_COURSECD ";
        $query .= "         AND C1.MAJORCD = B1.DAI1_MAJORCD ";
        $query .= "         AND C1.EXAMCOURSECD = B1.DAI1_COURSECODE ";
        $query .= " WHERE ";
        $query .= "         B1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.EXAMNO       = '{$model->examno}' ";
        return $query;
    }
    //名称マスタよりデータを取得
    function getJudge($model) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->year}' AND ";
        $query .= "     NAMECD1 = 'L013' AND ";
        $query .= "     NAMECD2 = '{$model->judgement}' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //合格・入学コース取得したい
        $courseRow = $db->getRow(knjl091qQuery::getSucEntCourse($model), DB_FETCHMODE_ASSOC);

        //合否フラグ取得したい
        $judge = $db->getOne(knjl091qQuery::getJudge($model));

        // 2.志願者基礎データの更新
        $data = array();
        //合否情報
        $data["SUC_COURSECD"][TEXT]         = ($judge != "1") ? "" : $courseRow["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]          = ($judge != "1") ? "" : $courseRow["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]       = ($judge != "1") ? "" : $courseRow["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]            = $model->judgement;
        //第１回手続情報
        $data["PROCEDUREDIV"][TEXT]         = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]        = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        //受験区分取得したい
        $kubunRow = $db->getRow(knjl091qQuery::getKubun($model), DB_FETCHMODE_ASSOC);
        //専併区分（1:海外入試の場合）
        if ($kubunRow["TESTDIV0"] == "1") {
            if ($model->field["PROCEDUREDIV"] == "1") {
                $data["SHDIV"][TEXT]        = $model->field["SHDIV"];
            } else {
                $data["SHDIV"][TEXT]        = "0";
            }
        }
        //第２回手続情報
        $data["ENTDIV"][TEXT]               = $model->field["ENTDIV"];
        $data["registercd"][TEXT]           = STAFFCD;
        $data["updated"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "entexam_applicantbase_dat", $where);
        $db->query($query);

        //第２回手続情報
        // 3.入学基礎詳細データの更新（入学コース）
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->year."' AND ";
        $query .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $query .= "       EXAMNO        = '".$model->examno."' AND ";
        $query .= "       SEQ           = '007' ";
        $db->query($query);

        if ($model->field["ENTDIV"] == "1") {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "007";
            $data["REMARK1"][TEXT]      = $courseRow["ENTER_COURSECD"];
            $data["REMARK2"][TEXT]      = $courseRow["ENTER_MAJORCD"];
            $data["REMARK3"][TEXT]      = $courseRow["ENTER_COURSECODE"];
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