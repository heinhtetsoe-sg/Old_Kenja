<?php

require_once('for_php7.php');
class knjl050gQuery extends Query
{

    //学校コードを取得
    public function getNameMstZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    public function getEntexamHallYdat($model, $examhallcd = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.EXAMHALLCD AS VALUE, ";
        $query .= "     T1.EXAMHALL_NAME || ' (' || T1.S_RECEPTNO || '～' || T1.E_RECEPTNO || ')' AS LABEL, ";
        $query .= "     T1.S_RECEPTNO, ";
        $query .= "     T1.E_RECEPTNO, ";
        $query .= "     T1.EXAMHALL_NAME AS CSV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        if ($model->isKeiai == "1") {
            $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT R1 ON R1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "         AND R1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND R1.TESTDIV = T1.TESTDIV ";
            $query .= "         AND R1.RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ";
            $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ON B1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "         AND B1.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND B1.EXAMNO = R1.EXAMNO ";
            $query .= "         AND B1.SHDIV = '".$model->exam_type."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     T1.APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "     T1.TESTDIV         = '{$model->testdiv}' AND ";
        if ($model->isKeiai == "1") {
            $query .= "     T1.EXAM_TYPE       = '{$model->exam_type}' AND ";
        }
        $query .= "     VALUE(T1.S_RECEPTNO,'') != '' AND ";
        $query .= "     VALUE(T1.E_RECEPTNO,'') != '' ";
        if ($examhallcd) {
            $query .= " AND T1.EXAMHALLCD      = '{$examhallcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    public function getTestSubclasscd($model, $testsubclasscd = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1 AS CSV_NAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME1 IS NOT NULL AND ";
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     TESTSUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PERFECT_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
        $query .= "                     TESTDIV         = '{$model->testdiv}' ";
        $query .= "                 ) ";
        if ($testsubclasscd) {
            $query .= " AND NAMECD2     = '{$testsubclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         S1.NAME, ";
        $query .= "         S1.NAME_KANA, ";
        $query .= "         L1.ATTEND_FLG, ";
        $query .= "         L1.SCORE, ";
        $query .= "         VALUE(S4.PERFECT, 100) AS PERFECT, ";
        $query .= "         S1.JUDGEMENT AS JUDGEMENT, ";
        $query .= "         N1.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "         N2.NAME1 AS TESTDIV_NAME, ";
        $query .= "         N3.NAME1 AS TESTSUBCLASSCD_NAME, ";
        $query .= "         L2.EXAMHALL_NAME ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        if ($model->isKeiai == "1") {
            $query .= "             AND S1.SHDIV            = '".$model->exam_type."' ";
        }
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S2 ";
        $query .= "              ON S2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S2.EXAMNO           = T1.EXAMNO ";
        $query .= "             AND S2.SEQ              = '001' ";
        $query .= "         INNER JOIN ENTEXAM_PERFECT_MST S4 ";
        $query .= "              ON S4.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S4.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND S4.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND S4.COURSECD         = S2.REMARK8 ";
        $query .= "             AND S4.MAJORCD          = S2.REMARK9 ";
        $query .= "             AND S4.EXAMCOURSECD     = S2.REMARK10 ";
        $query .= "             AND S4.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L1.EXAM_TYPE        = T1.EXAM_TYPE ";
        $query .= "             AND L1.RECEPTNO         = T1.RECEPTNO ";
        $query .= "             AND L1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN NAME_MST N1 ";
        $query .= "              ON N1.NAMECD1          = 'L003' ";
        $query .= "             AND N1.NAMECD2          = T1.APPLICANTDIV ";
        $query .= "         LEFT JOIN NAME_MST N2 ";
        $query .= "              ON N2.NAMECD1          = 'L004' ";
        $query .= "             AND N2.NAMECD2          = T1.TESTDIV ";
        $query .= "         LEFT JOIN V_NAME_MST N3 ";
        $query .= "              ON N3.YEAR             = T1.ENTEXAMYEAR ";
        $query .= "             AND N3.NAMECD1          = 'L009' ";
        $query .= "             AND N3.NAMECD2          = '{$model->testsubclasscd}' ";
        $query .= "         LEFT JOIN ENTEXAM_HALL_YDAT L2 ";
        $query .= "              ON L2.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L2.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L2.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L2.EXAMHALLCD       = '{$model->examhallcd}' ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' AND ";
        if ($model->isKeiai == "1") {
            $query .= "         L2.EXAM_TYPE    = '{$model->exam_type}' AND ";
        }
        $query .= "         T1.RECEPTNO BETWEEN L2.S_RECEPTNO AND L2.E_RECEPTNO ";
        if (strlen($model->special_reason_div)) {
            $query .= "         AND T1.RECEPTNO IN (";
            $query .= "               SELECT ";
            $query .= "                   W1.RECEPTNO ";
            $query .= "               FROM ";
            $query .= "                   ENTEXAM_RECEPT_DAT W1 ";
            $query .= "                   INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ";
            $query .= "                         ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
            $query .= "                        AND W2.EXAMNO = W1.EXAMNO ";
            $query .= "                        AND W2.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "               WHERE ";
            $query .= "                       W1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                   AND W1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                   AND W1.TESTDIV      = '".$model->testdiv."' ";
            $query .= "             )";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        //満点チェック
        $result = $db->query(knjl050gQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $checkScore = $model->score[$row["RECEPTNO"]];
            if ($checkScore != "*" && !is_numeric($checkScore) && strlen($checkScore)) {
                $model->setWarning("MSG901", "得点欄は数値又は、＊(小文字)のみ入力可能です。\\n受験番号：".$row["RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
            if ($checkScore > (int)$row["PERFECT"] && strlen($checkScore)) {
                $model->setWarning("MSG901", "満点：".(int)$row["PERFECT"]."以下で入力してください\\n受験番号：".$row["RECEPTNO"]);
                Query::dbCheckIn($db);
                return false;
            }
        }

        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAM_TYPE      = '1' ";
        $query .= "   AND TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "   AND RECEPTNO IN ('".implode("','", array_keys($model->score))."')";
        $db->query($query);

        //追加
        foreach ($model->score as $receptno => $score) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = 1;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
            if ($score === '*') {
                $data["ATTEND_FLG"][TEXT]   = "0";
                $data["SCORE"][NUMBER]      = "";
            } else {
                $data["ATTEND_FLG"][TEXT]   = !strlen($score) ? "" : "1";
                $data["SCORE"][NUMBER]      = !strlen($score) ? "" : $score;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //ＣＳＶ出力
    public function getOutputQueryCsv($model, $subclassList)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.ENTEXAMYEAR, ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         N1.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "         T1.TESTDIV, ";
        $query .= "         N2.NAME1 AS TESTDIV_NAME, ";
        $query .= "         T1.RECEPTNO, ";
        foreach ($subclassList as $subclassCd => $subclassName) {
            $query .= "         '{$subclassCd}'             AS TESTSUBCLASSCD{$subclassCd}, ";
            $query .= "         '{$subclassName}'           AS TESTSUBCLASSCD_NAME{$subclassCd}, ";
            $query .= "         L{$subclassCd}.SCORE        AS SCORE{$subclassCd}, ";
            $query .= "         L{$subclassCd}.ATTEND_FLG   AS ATTEND_FLG{$subclassCd}, ";
        }
        $query .= "         '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        foreach ($subclassList as $subclassCd => $subclassName) {
            $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT L{$subclassCd} ";
            $query .= "              ON L{$subclassCd}.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
            $query .= "             AND L{$subclassCd}.APPLICANTDIV     = T1.APPLICANTDIV ";
            $query .= "             AND L{$subclassCd}.TESTDIV          = T1.TESTDIV ";
            $query .= "             AND L{$subclassCd}.EXAM_TYPE        = T1.EXAM_TYPE ";
            $query .= "             AND L{$subclassCd}.RECEPTNO         = T1.RECEPTNO ";
            $query .= "             AND L{$subclassCd}.TESTSUBCLASSCD   = '{$subclassCd}' ";
        }
        $query .= "         LEFT JOIN NAME_MST N1 ";
        $query .= "              ON N1.NAMECD1          = 'L003' ";
        $query .= "             AND N1.NAMECD2          = T1.APPLICANTDIV ";
        $query .= "         LEFT JOIN NAME_MST N2 ";
        $query .= "              ON N2.NAMECD1          = 'L004' ";
        $query .= "             AND N2.NAMECD2          = T1.TESTDIV ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' ";
        if (strlen($model->special_reason_div)) {
            $query .= "         AND T1.RECEPTNO IN (";
            $query .= "               SELECT ";
            $query .= "                   W1.RECEPTNO ";
            $query .= "               FROM ";
            $query .= "                   ENTEXAM_RECEPT_DAT W1 ";
            $query .= "                   INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ";
            $query .= "                         ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
            $query .= "                        AND W2.EXAMNO = W1.EXAMNO ";
            $query .= "                        AND W2.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "               WHERE ";
            $query .= "                       W1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                   AND W1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                   AND W1.TESTDIV      = '".$model->testdiv."' ";
            $query .= "             )";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }
}
