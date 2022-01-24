<?php

require_once('for_php7.php');

class knjl064iQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $year, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 || ':' || ABBV2 AS LABEL2, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 != '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分マスタ取得
    public function getEntexamTestDivMst($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV = '2' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //重複チェック項目
    public function getCenterTitle()
    {
        $query  = " WITH T_LIST (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1','（カナ氏名○、生年月日○、電話番号○）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2','（カナ氏名○、生年月日○、電話番号×）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3','（カナ氏名○、生年月日×、電話番号○）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('4','（カナ氏名×、生年月日○、電話番号○）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('5','（カナ氏名○、生年月日×、電話番号×）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('6','（カナ氏名×、生年月日○、電話番号×）リスト') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('7','（カナ氏名×、生年月日×、電話番号○）リスト') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     VALUE,";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     T_LIST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    public function selectMainQuery($model)
    {
        $query .= " WITH OTHER_TESTDIV AS ( ";
        $query .= "     SELECT ";
        $query .= "         BASE.ENTEXAMYEAR, ";
        $query .= "         BASE.APPLICANTDIV, ";
        $query .= "         BASE.EXAMNO, ";
        $query .= "         BASE.TESTDIV, ";
        $query .= "         BASE.NAME_KANA, ";
        $query .= "         BASE.BIRTHDAY, ";
        $query .= "         ADDR.TELNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "         INNER JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "             ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "             AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "             AND ADDR.EXAMNO = BASE.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         BASE.ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "         AND BASE.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "         AND BASE.TESTDIV        <> '{$model->field["TESTDIV"]}' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     OTHER.TESTDIV AS OTHER_TESTDIV, ";
        $query .= "     OTHER.EXAMNO AS OTHER_EXAMNO, ";
        $query .= "     OTHER.NAME_KANA AS OTHER_NAME_KANA, ";
        $query .= "     OTHER.BIRTHDAY AS OTHER_BIRTHDAY, ";
        $query .= "     OTHER.TELNO AS OTHER_TELNO, ";
        $query .= "     CASE WHEN BASE.RECOM_EXAMNO = OTHER.EXAMNO THEN BASE.RECOM_EXAMNO END AS RECOM_EXAMNO, ";
        $query .= "     COUNT(*) OVER(PARTITION BY BASE.EXAMNO) AS EXAMNO_CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "         ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO = BASE.EXAMNO ";
        $query .= "     INNER JOIN OTHER_TESTDIV OTHER ";
        $query .= "         ON OTHER.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "         AND OTHER.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "     AND BASE.APPLICANTDIV   = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND BASE.TESTDIV        = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND OTHER.EXAMNO        IS NOT NULL ";
        $query .= "     AND OTHER.NAME_KANA     IS NOT NULL ";
        $query .= "     AND OTHER.BIRTHDAY      IS NOT NULL ";
        $query .= "     AND OTHER.TELNO         IS NOT NULL ";
        if ($model->nameKanaMatchFlg) {
            $query .= "     AND BASE.NAME_KANA  = OTHER.NAME_KANA ";
        }
        if ($model->birthDayMatchFlg) {
            $query .= "     AND BASE.BIRTHDAY   = OTHER.BIRTHDAY ";
        }
        if ($model->telNoMatchFlg) {
            $query .= "     AND ADDR.TELNO      = OTHER.TELNO  ";
        }
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     OTHER_EXAMNO";

        return $query;
    }

    public function executeQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //下段のRECOM_EXAMNOをクリア
        for ($i = 0; $i < get_count($model->upd_data_before); $i++) {
            list($examno, $otherExamno) = split('-', $model->upd_data_before[$i]);

            $dataArr = array();
            $dataArr["EXAMNO"]          = $examno;
            $dataArr["OTHER_EXAMNO"]    = '';

            $query = knjl064iQuery::getUpdApplicantbaseDatQuery($model, $dataArr);
            $db->query($query);
        }

        //上段のRECOM_EXAMNOにOTHER_EXAMNOをセット
        for ($i = 0; $i < get_count($model->upd_data_base); $i++) {
            list($examno, $otherExamno) = split('-', $model->upd_data_base[$i]);

            $dataArr = array();
            $dataArr["EXAMNO"]          = $examno;
            $dataArr["OTHER_EXAMNO"]    = $otherExamno;

            $query = knjl064iQuery::getUpdApplicantbaseDatQuery($model, $dataArr);
            $db->query($query);

            $dataArr = array();
            $dataArr["EXAMNO"]          = $otherExamno;
            $dataArr["OTHER_EXAMNO"]    = $examno;

            $query = knjl064iQuery::getUpdApplicantbaseDatQuery2($model, $dataArr);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function executeSyougouQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl064iQuery::selectMainQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["EXAMNO_CNT"] == 1) {
                $query = knjl064iQuery::getUpdApplicantbaseDatQuery($model, $row);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function getUpdApplicantbaseDatQuery($model, $dataArr)
    {
        $data = array();
        $data["RECOM_EXAMNO"][TEXT]     =  $dataArr["OTHER_EXAMNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $where .= "   AND EXAMNO        = '{$dataArr["EXAMNO"]}' ";
        $where .= "   AND TESTDIV       = '{$model->field["TESTDIV"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        return $query;
    }

    public function getUpdApplicantbaseDatQuery2($model, $dataArr)
    {
        $data = array();
        $data["RECOM_EXAMNO"][TEXT]     =  $dataArr["OTHER_EXAMNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $where .= "   AND EXAMNO        = '{$dataArr["EXAMNO"]}' ";
        $where .= "   AND TESTDIV       <> '{$model->field["TESTDIV"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        return $query;
    }

    public function clearAllRecomExamno($model)
    {
        $data = array();
        $data["RECOM_EXAMNO"][TEXT]     =  '';
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR   = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        $where .= "   AND TESTDIV       = '{$model->field["TESTDIV"]}' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        return $query;
    }
}
