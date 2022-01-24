<?php
class knjl083iQuery extends Query
{
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

    //入試区分コンボ
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->examYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //班選択コンボ
    public function getEntexamHallYdat($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->examYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->examType."' AND ";
        $query .= "     EXAMHALLCD      LIKE '20%' "; // 面接班：先頭2桁が'20'のデータ
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function SelectQuery($model)
    {
        $query  = " WITH HALL_SEROWNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXAMHALLCD, ";
        $query .= "         T1.CAPA_CNT, ";
        $query .= "         T1.ENTEXAMYEAR, ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.TESTDIV, ";
        $query .= "         SUM(T2.CAPA_CNT) - T1.CAPA_CNT + 1 AS S_ROWNO, ";
        $query .= "         SUM(T2.CAPA_CNT) AS E_ROWNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_HALL_YDAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_HALL_YDAT AS T2 ";
        $query .= "             ON T2.EXAMHALLCD <= T1.EXAMHALLCD ";
        $query .= "             AND T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.TESTDIV = T1.TESTDIV ";
        $query .= "             AND T2.EXAMHALLCD LIKE '20%' "; // 面接班：先頭2桁が'20'のデータ
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '".$model->examYear."' ";
        $query .= "         AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND T1.TESTDIV = '".$model->testdiv."' ";
        $query .= "         AND T1.EXAMHALLCD LIKE '20%' "; // 面接班：先頭2桁が'20'のデータ
        // ALL以外の場合
        if ($model->examHallCd != 'ALL') {
            $query .= "         AND T1.EXAMHALLCD = '".$model->examHallCd."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.EXAMHALLCD, ";
        $query .= "         T1.CAPA_CNT, ";
        $query .= "         T1.ENTEXAMYEAR, ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.TESTDIV ";
        $query .= "     ORDER BY ";
        $query .= "         T1.EXAMHALLCD ";
        $query .= " ), EXAMNO_ROW_NUMBER AS ( ";
        $query .= "     SELECT ";
        $query .= "         ROWNUMBER() OVER() AS ROW_NUMBER, ";
        $query .= "         EXAMNO, ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         TESTDIV ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->examYear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND TESTDIV = '".$model->testdiv."' ";
        $query .= "         AND (JUDGEMENT = '4' OR JUDGEMENT IS NULL) "; // 合否区分対象：null、4:欠席
        $query .= "     ORDER BY ";
        $query .= "         EXAMNO ";
        $query .= " ), EXAMNO_EXAMHALLCD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T_ROWNUM.ROW_NUMBER, ";
        $query .= "         T_ROWNUM.EXAMNO, ";
        $query .= "         T_HALL.EXAMHALLCD, ";
        $query .= "         T_ROWNUM.ENTEXAMYEAR, ";
        $query .= "         T_ROWNUM.APPLICANTDIV ";
        $query .= "     FROM ";
        $query .= "         EXAMNO_ROW_NUMBER T_ROWNUM ";
        $query .= "         INNER JOIN HALL_SEROWNO T_HALL ";
        $query .= "             ON T_HALL.ENTEXAMYEAR = T_ROWNUM.ENTEXAMYEAR ";
        $query .= "             AND T_HALL.APPLICANTDIV = T_ROWNUM.APPLICANTDIV ";
        $query .= "             AND T_HALL.TESTDIV = T_ROWNUM.TESTDIV ";
        $query .= "             AND T_ROWNUM.ROW_NUMBER BETWEEN T_HALL.S_ROWNO AND T_HALL.E_ROWNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     HALL.ROW_NUMBER, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     TRANSLATE_HK_K(BASE.NAME_KANA) AS NAME_KANA, ";
        $query .= "     FINS.FINSCHOOL_NAME, ";
        $query .= "     HALL.EXAMHALLCD, ";
        $query .= "     BASE.JUDGEMENT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN EXAMNO_EXAMHALLCD HALL ";
        $query .= "          ON HALL.ENTEXAMYEAR  = BASE.ENTEXAMYEAR  ";
        $query .= "         AND HALL.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND HALL.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ";
        $query .= "         ON FINS.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

/***** 更新処理 *****/
    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updField as $examno => $value) {
            if ($value["UPD_FLG"] !== "1") {
                continue;
            }

            $data = array();
            $data["JUDGEMENT"][TEXT] = $value["JUDGEDIV"];
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->examYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."'";
            $where .= "   AND VALUE(JUDGEMENT, '4') = '4' "; //欠席またはNULL

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}