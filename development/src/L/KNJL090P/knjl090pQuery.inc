<?php

require_once('for_php7.php');

class knjl090pQuery extends Query 
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
        $query .= "      T1.TESTDIV,";
        $query .= "      L1.NAME1 AS TESTDIV_NAME,";
        $query .= "      T1.SIKEN_KAIJOU_CD1,";
        $query .= "      L2.EXAMHALL_NAME AS SIKEN_KAIJOU_CD1_NAME,";
        $query .= "      T1.shdiv,";
        $query .= "      T1.judgement,";
        $query .= "      T1.SPECIAL_REASON_DIV, ";
        $query .= "      T1.PROCEDUREDATE,";
        $query .= "      T1.procedurediv,";
        $query .= "      T1.entdiv,";
        $query .= "      T2.name1 AS sexname ";
        $query .= "  FROM";
        $query .= "      V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "      LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.ENTEXAMYEAR AND T2.NAMECD1 = 'Z002' AND T2.NAMECD2 = T1.SEX ";
        $namecd1 = ($model->applicantdiv == "2") ? "L004" : "L024";
        $query .= "      LEFT JOIN V_NAME_MST L1 ON L1.YEAR = T1.ENTEXAMYEAR AND L1.NAMECD1 = '{$namecd1}' AND L1.NAMECD2 = T1.TESTDIV ";
        $query .= "      LEFT JOIN ENTEXAM_HALL_YDAT L2 ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND L2.APPLICANTDIV = T1.APPLICANTDIV AND L2.TESTDIV = T1.TESTDIV AND L2.EXAMHALLCD = T1.SIKEN_KAIJOU_CD1 ";
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
        $query .= " WHERE entexamyear  = '".$model->year."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."' ";
        $query .= "   AND examno       = '".$model->examno."'";
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
        $query .= "     T1.* ";
        $query .= " FROM";
        $query .= "     entexam_recept_dat T1 ";
        $query .= " WHERE";
        $query .= "     T1.entexamyear  = '{$model->year}' AND ";
        $query .= "     T1.applicantdiv = '{$model->applicantdiv}' AND ";
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
        $query .= "     T1.EXAMNO       = '{$model->examno}' ";
        $query .= " ORDER BY ";
        $query .= "     T2.TESTSUBCLASSCD ";

        return $query;
    }

    //受験科目
    function getTestSubclasscd($model, $testdiv) {
        $subNo = ($model->applicantdiv == "2") ? "2" : "1";

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME{$subNo} AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->year}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME{$subNo} IS NOT NULL AND ";
        //満点マスタの設定が前提
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     TESTSUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PERFECT_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR     = '{$model->year}' AND ";
        $query .= "                     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                     TESTDIV         = '{$testdiv}' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //調査書・作文・実績・面接データ取得
    function getScoreSuisen($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     L1.TOTAL_ALL AS CHOUSASHO, ";
        $query .= "     L2.TOTAL AS SAKUBUN, ";
        $query .= "     L3.SCORE AS JISSEKI, ";
        $query .= "     L4.SCORE AS MENSETU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "         ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L1.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_SAKUBUN_DAT L2 ";
        $query .= "         ON  L2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L2.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND L2.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_JISSEKI_DAT L3 ";
        $query .= "         ON  L3.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L3.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND L3.EXAMNO       = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT L4 ";
        $query .= "         ON  L4.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND L4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L4.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND L4.EXAMNO       = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->year}' AND ";
        $query .= "     T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     T1.EXAMNO       = '{$model->examno}' ";

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

        // 1.受付データの更新
        $data = array();
        $data["JUDGEDIV"][TEXT]         = $model->judgement;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
        $db->query($query);

        //合格・入学コース取得したい
        $courseRow = $db->getRow(knjl090pQuery::getSucEntCourse($model), DB_FETCHMODE_ASSOC);

        // 2.志願者基礎データの更新
        $data = array();
        $data["SUC_COURSECD"][TEXT]     = ($model->judgement != "1") ? "" : $courseRow["SUC_COURSECD"];
        $data["SUC_MAJORCD"][TEXT]      = ($model->judgement != "1") ? "" : $courseRow["SUC_MAJORCD"];
        $data["SUC_COURSECODE"][TEXT]   = ($model->judgement != "1") ? "" : $courseRow["SUC_COURSECODE"];
        $data["JUDGEMENT"][TEXT]        = $model->judgement;
        //第１回手続情報
        $data["PROCEDUREDIV"][TEXT]     = $model->field["PROCEDUREDIV"];
        $data["PROCEDUREDATE"][TEXT]    = str_replace("/", "-", $model->field["PROCEDUREDATE"]);
        //第２回手続情報
        $data["ENTDIV"][TEXT]           = $model->field["ENTDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' AND";
        $where .= "       APPLICANTDIV  = '".$model->applicantdiv."' AND ";
        $where .= "       EXAMNO        = '".$model->examno."'";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
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