<?php
class knjl120iQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}' AND ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "    AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //英検取得旧
    public function getQualifiedNameMst()
    {
        $query  = " SELECT  ";
        $query .= "     T1.RANK AS VALUE ";
        $query .= "   , L1.NAME1 AS LABEL ";
        $query .= " FROM QUALIFIED_RANK_DAT T1 ";
        $query .= " INNER JOIN NAME_MST L1 ON ";
        $query .= "         L1.NAMECD1 = 'H312' ";
        $query .= "     AND L1.NAMECD2 = T1.RANK ";
        $query .= " WHERE ";
        $query .= "     T1.QUALIFIED_CD = '0001' ";
        $query .= " ORDER BY ";
        $query .= "     T1.RANK ";
        return $query;
    }

    //入試区分
    public function getTestDiv($model)
    {
        $query  = "     SELECT ";
        $query .= "       TESTDIV AS VALUE, ";
        $query .= "       TESTDIV || ':' || TESTDIV_ABBV AS LABEL";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_TESTDIV_MST ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV IN ('1', '2') "; //試験区分は「1:A方式」「2:B方式」のみ対象
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //最初のEXAMNOを取得
    public function SelectFstExamno($model)
    {
        $query  = " WITH MAXCHKTBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.EXAMNO) AS RENBAN, ";
        $query .= "     T1.EXAMNO ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND VALUE(T1.JUDGEMENT, '') <> '5' ";
        $query .= "   ORDER BY ";
        $query .= "     RENBAN ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   TX.EXAMNO ";
        $query .= " FROM ";
        $query .= "   MAXCHKTBL TX ";
        $query .= " ORDER BY ";
        $query .= "   TX.EXAMNO ";
        $query .= " FETCH FIRST ROWS ONLY ";

        return $query;
    }

    //最後のEXAMNOを取得
    public function SelectLstExamno($model)
    {
        $query  = " WITH MAXCHKTBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.EXAMNO) AS RENBAN, ";
        $query .= "     T1.EXAMNO ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ";
        $query .= "       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "      AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "      AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND VALUE(T1.JUDGEMENT, '') <> '5' ";
        $query .= "   ORDER BY ";
        $query .= "     RENBAN ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "  TW.EXAMNO ";
        $query .= " FROM ";
        $query .= "  MAXCHKTBL TW ";
        $query .= " WHERE ";
        $query .= "  TW.RENBAN = (SELECT count(*) FROM MAXCHKTBL) ";
        return $query;
    }

    //ポイント設定値取得
    public function getPointMst($model)
    {
        $query  = "     SELECT ";
        $query .= "       PLUS_POINT, ";
        $query .= "       MINUS_POINT ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_POINT_MST ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR       =	'{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  =   '{$model->applicantdiv}' ";
        $query .= "       AND TESTDIV       =	'{$model->ajaxTesiDiv}' ";
        $query .= "       AND POINTCD       =	'{$model->ajaxPointCd}' ";
        $query .= "       AND '{$model->ajaxInputVal}' BETWEEN value(POINTLOW, 0) AND value(POINTHIGH, 999) ";

        return $query;
    }

    //他入試区分にて既に調査書データが登録されていた場合に、現在の入試区分にコピー
    public function copyApplicantConfrpt($db, $model)
    {
        //ENTEXAM_APPLICANTCONFRPT_DATのカラム一覧
        $query  = "    SELECT ";
        $query .= "        * ";
        $query .= "    FROM ";
        $query .= "        SYSIBM.COLUMNS ";
        $query .= "    WHERE ";
        $query .= "        TABLE_NAME = 'ENTEXAM_APPLICANTCONFRPT_DAT' ";
        $query .= "    ORDER BY ";
        $query .= "        ORDINAL_POSITION ";
        $result = $db->query($query);

        $columns = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $columns[] = $row["COLUMN_NAME"];
        }

        //コピー処理
        $query  = " INSERT INTO ENTEXAM_APPLICANTCONFRPT_DAT (".implode(",", $columns).") ";
        $query .= " WITH RECOM_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         BASE.ENTEXAMYEAR, ";
        $query .= "         BASE.APPLICANTDIV, ";
        $query .= "         BASE.RECOM_EXAMNO, ";
        $query .= "         BASE.EXAMNO AS CURRENT_EXAMNO";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     WHERE ";
        $query .= "         BASE.ENTEXAMYEAR       =   '{$model->examyear}' ";
        $query .= "         AND BASE.APPLICANTDIV  =   '{$model->applicantdiv}' ";
        $query .= "         AND BASE.TESTDIV       =   '{$model->field["TESTDIV"]}' ";
        $query .= "         AND VALUE(BASE.JUDGEMENT, '') <> '5' ";
        $query .= "         AND NOT EXISTS ( ";
        $query .= "             SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ";
        $query .= "             WHERE ";
        $query .= "                 CONFRPT.ENTEXAMYEAR      = BASE.ENTEXAMYEAR ";
        $query .= "                 AND CONFRPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                 AND CONFRPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "         ) ";
        $query .= "         AND BASE.RECOM_EXAMNO IS NOT NULL ";
        $query .= " ), ";
        $query .= " RECOM_CONFRPT AS ( ";
        $query .= "     SELECT ";
        $query .= "         CONFRPT.*, ";
        $query .= "         RECOM.CURRENT_EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ";
        $query .= "         INNER JOIN RECOM_DATA RECOM ";
        $query .= "              ON CONFRPT.ENTEXAMYEAR    =  RECOM.ENTEXAMYEAR ";
        $query .= "             AND CONFRPT.APPLICANTDIV   =  RECOM.APPLICANTDIV ";
        $query .= "             AND CONFRPT.EXAMNO         =  RECOM.RECOM_EXAMNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $sep = "";
        foreach ($columns as $column) {
            if ($column == "EXAMNO") {
                $query .= $sep." CURRENT_EXAMNO ";
            } else {
                $query .= $sep." {$column} ";
            }
            $sep = ",";
        }
        $query .= " FROM ";
        $query .= "     RECOM_CONFRPT ";

        $db->query($query);
    }

    //一覧表示
    public function SelectQuery($model, $arr_classCd)
    {
        $query  = "     SELECT ";
        $query .= "       T1.EXAMNO, ";
        $query .= "       T1.NAME, ";
        $query .= "       T1.RECOM_EXAMNO, ";
        $query .= "       T2.CONFIDENTIAL_RPT01 AS SCORE1, ";
        $query .= "       T2.CONFIDENTIAL_RPT02 AS SCORE2, ";
        $query .= "       T2.CONFIDENTIAL_RPT03 AS SCORE3, ";
        $query .= "       T2.CONFIDENTIAL_RPT04 AS SCORE4, ";
        $query .= "       T2.CONFIDENTIAL_RPT05 AS SCORE5, ";
        $query .= "       T2.CONFIDENTIAL_RPT06 AS SCORE6, ";
        $query .= "       T2.CONFIDENTIAL_RPT07 AS SCORE7, ";
        $query .= "       T2.CONFIDENTIAL_RPT08 AS SCORE8, ";
        $query .= "       T2.CONFIDENTIAL_RPT09 AS SCORE9, ";
        $query .= "       T2.TOTAL_ALL, ";
        $query .= "       T2.TOTAL3, ";
        $query .= "       T2.ABSENCE_DAYS, ";
        $query .= "       T2.ABSENCE_DAYS2, ";
        $query .= "       T2.ABSENCE_DAYS3, ";
        $query .= "       T3.REMARK1 AS TOTAL_HYOUTEI, ";
        $query .= "       T3.REMARK2 AS ACHIEVEMENT, ";
        $query .= "       T3.REMARK3 AS REPORT_PLUS, ";
        $query .= "       T3.REMARK4 AS REPORT_MINUS, ";
        $query .= "       T3.REMARK5 AS SELF_REC_PLUS, ";
        $query .= "       T3.REMARK6 AS SELF_REC_MINUS, ";
        $query .= "       T3.REMARK7 AS DOUSOU_PLUS, ";
        $query .= "       T3.REMARK8 AS QUALIFIED_ENG, ";
        $query .= "       T3.REMARK9 AS TYOUSEI_PLUS, ";
        $query .= "       T3.REMARK10 AS TYOUSEI_MINUS, ";
        $query .= "       T3.REMARK11 AS TOTAL_PLUS, ";
        $query .= "       T3.REMARK12 AS TOTAL_MINUS, ";
        $query .= "       T4.REMARK1 AS TOTAL9_PLUS, ";
        $query .= "       T4.REMARK2 AS TOTAL9_MINUS, ";
        $query .= "       T4.REMARK3 AS TOTAL3_PLUS, ";
        $query .= "       T4.REMARK4 AS TOTAL3_MINUS, ";
        $query .= "       T4.REMARK5 AS ABSENCE_TOTAL_PLUS, ";
        $query .= "       T4.REMARK6 AS ABSENCE_TOTAL_MINUS, ";
        $query .= "       T4.REMARK7 AS HYOUTEI1_2_PLUS, ";
        $query .= "       T4.REMARK8 AS HYOUTEI1_2_MINUS, ";
        $query .= "       T5.SCORE1 AS INTERVIEW_PLUS, ";
        $query .= "       T5.SCORE2 AS INTERVIEW_MINUS ";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ";
        $query .= "         ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T3 ";
        $query .= "         ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T3.EXAMNO = T1.EXAMNO ";
        $query .= "         AND T3.SEQ = '001' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T4 ";
        $query .= "         ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T4.EXAMNO = T1.EXAMNO ";
        $query .= "         AND T4.SEQ = '002' ";
        $query .= "       LEFT JOIN ENTEXAM_INTERVIEW_DAT T5 ";
        $query .= "         ON  T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T5.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T5.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "         AND T5.EXAMNO = T1.EXAMNO ";

        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND VALUE(T1.JUDGEMENT, '') <> '5' ";
        if ($model->cmd == "search") {
            if ($model->field["S_EXAMNO"] != "") {
                $query .= "   AND T1.EXAMNO >= '".$model->field["S_EXAMNO"]."' ";
                if ($model->field["E_EXAMNO"] != "") {
                    $query .= "   AND T1.EXAMNO <= '".$model->field["E_EXAMNO"]."' ";
                }
            }
        } elseif ($model->cmd == "back") {
            if ($model->field["S_EXAMNO"] != "") {
                $query .= "   AND T1.EXAMNO >= '".$model->field["S_EXAMNO"]."' - 50 ";
                $query .= "   AND T1.EXAMNO < '".$model->field["S_EXAMNO"]."' ";
            }
        } elseif ($model->cmd == "next") {
            $query .= "   AND T1.EXAMNO > '".$model->field["E_EXAMNO"]."' ";
        } elseif ($model->cmd == "changeDispDiv") { //表示区分切替時は取得できる集団を変えないようにする
            $query .= "   AND T1.EXAMNO BETWEEN '".$model->field["S_EXAMNO"]."' AND '".$model->field["E_EXAMNO"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";
        $query .= " FETCH FIRST 50 ROWS ONLY "; //画面表示はMAX50件

        return $query;
    }

    //存在チェック ENTEXAM_APPLICANTCONFRPT_DAT
    public function getExistsConfRpt($model, $examno)
    {
        $query = "     SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND EXAMNO        = '{$examno}' ";

        return $query;
    }

    //存在チェック ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
    public function getExistsConfRptDetSeq($model, $examno, $seq)
    {
        $query = "     SELECT ";
        $query .= "       COUNT(*) ";
        $query .= "     FROM ";
        $query .= "       ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "       ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "       AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "       AND EXAMNO        = '{$examno}' ";
        $query .= "       AND SEQ           = '{$seq}' ";

        return $query;
    }

    //更新 (表示区分「調査書評定・出欠」)
    public function updateQuery1($db, $model)
    {
        foreach ($model->arr_examno as $examno) {
            //画面上のデータを更新
            $examnoField = $model->mainField[$examno];
            knjl120iQuery::updateApplicantConfrptQuery($db, $model, $examno, $examnoField);

            //他入試区分が存在する場合は同じ値で更新
            $recomExamno = $model->arr_recom_examno[$examno];
            if ($recomExamno != "") {
                knjl120iQuery::updateApplicantConfrptQuery($db, $model, $recomExamno, $examnoField);
            }

            //更新処理２ -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(REMARK11～12)
            $query = knjl120iQuery::updateSubQueryCommon($db, $model, $examno);
        }
    }

    public function updateApplicantConfrptQuery($db, $model, $examno, $field)
    {
        //更新処理１ --ENTEXAM_APPLICANTCONFRPT_DAT
        $data = array();
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        for ($score_i = 1; $score_i <= 9; $score_i++) {
            $zeroumeNum = sprintf("%02d", $score_i);
            $data["CONFIDENTIAL_RPT{$zeroumeNum}"][TEXT] = $field["SCORE{$score_i}"];
        }
        $data["ABSENCE_DAYS"][TEXT]     = $field["ABSENCE_DAYS"];
        $data["ABSENCE_DAYS2"][TEXT]    = $field["ABSENCE_DAYS2"];
        $data["ABSENCE_DAYS3"][TEXT]    = $field["ABSENCE_DAYS3"];
        $data["TOTAL3"][TEXT]           = $field["TOTAL3"];
        $data["TOTAL_ALL"][TEXT]        = $field["TOTAL_ALL"];

        if (0 < $db->getOne(knjl120iQuery::getExistsConfRpt($model, $examno))) {
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
            $where .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
            $where .= "     EXAMNO          = '{$examno}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
        } else {
            $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        }
        $db->query($query);
    }

    //更新 (表示区分「その他」)
    public function updateQuery2($db, $model)
    {
        foreach ($model->arr_examno as $examno) {
            //更新処理１ -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(REMARK1～10)
            $data = array();
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $data["REMARK1"][TEXT]          = $model->mainField[$examno]["TOTAL_HYOUTEI"];
            $data["REMARK2"][TEXT]          = $model->mainField[$examno]["ACHIEVEMENT"];
            $data["REMARK3"][TEXT]          = $model->mainField[$examno]["REPORT_PLUS"];
            $data["REMARK4"][TEXT]          = $model->mainField[$examno]["REPORT_MINUS"];
            $data["REMARK5"][TEXT]          = $model->mainField[$examno]["SELF_REC_PLUS"];
            $data["REMARK6"][TEXT]          = $model->mainField[$examno]["SELF_REC_MINUS"];
            $data["REMARK7"][TEXT]          = $model->mainField[$examno]["DOUSOU_PLUS"];
            $data["REMARK8"][TEXT]          = $model->mainField[$examno]["QUALIFIED_ENG"];
            $data["REMARK9"][TEXT]          = $model->mainField[$examno]["TYOUSEI_PLUS"];
            $data["REMARK10"][TEXT]         = $model->mainField[$examno]["TYOUSEI_MINUS"];

            if (0 < $db->getOne(knjl120iQuery::getExistsConfRptDetSeq($model, $examno, "001"))) {
                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
                $where .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
                $where .= "     EXAMNO          = '{$examno}' AND ";
                $where .= "     SEQ             = '001' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              ='001';

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            }
            $db->query($query);

            //更新処理２-- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(REMARK11～12)
            knjl120iQuery::updateSubQueryCommon($db, $model, $examno);
        }
    }

    //更新
    public function updateSubQueryCommon($db, $model, $examno)
    {

        //更新 -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(SEQ001)
        $data = array();
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        $data["REMARK11"][TEXT]         = $model->mainField[$examno]["TOTAL_PLUS"];
        $data["REMARK12"][TEXT]         = $model->mainField[$examno]["TOTAL_MINUS"];

        if (0 < $db->getOne(knjl120iQuery::getExistsConfRptDetSeq($model, $examno, "001"))) {
            //更新
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
            $where .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
            $where .= "     EXAMNO          = '{$examno}' AND ";
            $where .= "     SEQ             = '001' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        } else {
            //追加
            $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              ='001';

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        }
        $db->query($query);

        //更新 -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(SEQ002)
        $data = array();
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";
        $data["REMARK1"][TEXT]         = $model->mainField[$examno]["TOTAL9_PLUS"];
        $data["REMARK2"][TEXT]         = $model->mainField[$examno]["TOTAL9_MINUS"];
        $data["REMARK3"][TEXT]         = $model->mainField[$examno]["TOTAL3_PLUS"];
        $data["REMARK4"][TEXT]         = $model->mainField[$examno]["TOTAL3_MINUS"];
        $data["REMARK5"][TEXT]         = $model->mainField[$examno]["ABSENCE_TOTAL_PLUS"];
        $data["REMARK6"][TEXT]         = $model->mainField[$examno]["ABSENCE_TOTAL_MINUS"];
        if ($model->field["TESTDIV"] == "2") {
            $data["REMARK7"][TEXT]         = $model->mainField[$examno]["HYOUTEI1_2_PLUS"];
            $data["REMARK8"][TEXT]         = $model->mainField[$examno]["HYOUTEI1_2_MINUS"];
        }

        if (0 < $db->getOne(knjl120iQuery::getExistsConfRptDetSeq($model, $examno, "002"))) {
            //更新
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
            $where .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
            $where .= "     EXAMNO          = '{$examno}' AND ";
            $where .= "     SEQ             = '002' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        } else {
            //追加
            $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              ='002';

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        }
        $db->query($query);

        //テーブルから持ってくるように変更する
        //更新 -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(SEQ003)
        $data = array();
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";
        if ($model->field["TESTDIV"] == "1") {
            //+ポイントの9教科+3教科+出欠+特活(調査書+)の合計
            $hyoukaPlus = $model->mainField[$examno]["TOTAL9_PLUS"]
                        + $model->mainField[$examno]["TOTAL3_PLUS"]
                        + $model->mainField[$examno]["ABSENCE_TOTAL_PLUS"]
                        + $model->mainField[$examno]["REPORT_PLUS"];

            $hyoukaMinus = $model->mainField[$examno]["TOTAL9_MINUS"]
                         + $model->mainField[$examno]["TOTAL3_MINUS"]
                         + $model->mainField[$examno]["ABSENCE_TOTAL_MINUS"]
                         + $model->mainField[$examno]["REPORT_MINUS"];

            $data["REMARK1"][TEXT]         = $hyoukaPlus;
            $data["REMARK2"][TEXT]         = $hyoukaMinus;
        } elseif ($model->field["TESTDIV"] == "2") {
            $souten = $model->mainField[$examno]["TOTAL9_PLUS"]
                    + $model->mainField[$examno]["TOTAL3_PLUS"]
                    + $model->mainField[$examno]["HYOUTEI1_2_PLUS"]
                    + $model->mainField[$examno]["DOUSOU_PLUS"]
                    + $model->mainField[$examno]["ACHIEVEMENT"]
                    + $model->mainField[$examno]["INTERVIEW_PLUS"]
                    + $model->mainField[$examno]["SELF_REC_PLUS"]
                    + $model->mainField[$examno]["ABSENCE_TOTAL_PLUS"]
                    + $model->mainField[$examno]["TYOUSEI_PLUS"]
                    - $model->mainField[$examno]["TOTAL9_MINUS"]
                    - $model->mainField[$examno]["TOTAL3_MINUS"]
                    - $model->mainField[$examno]["HYOUTEI1_2_MINUS"]
                    - $model->mainField[$examno]["INTERVIEW_MINUS"]
                    - $model->mainField[$examno]["SELF_REC_MINUS"]
                    - $model->mainField[$examno]["ABSENCE_TOTAL_MINUS"]
                    - $model->mainField[$examno]["TYOUSEI_MINUS"];

            $data["REMARK3"][TEXT]         = $souten;
        }

        if (0 < $db->getOne(knjl120iQuery::getExistsConfRptDetSeq($model, $examno, "003"))) {
            //更新
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
            $where .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
            $where .= "     EXAMNO          = '{$examno}' AND ";
            $where .= "     SEQ             = '003' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        } else {
            //追加
            $data["ENTEXAMYEAR"][TEXT]      = $model->examyear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["SEQ"][TEXT]              ='003';

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        }
        $db->query($query);
    }

    public function updateExecQuery($db, $model)
    {
        $query  = " SELECT ";
        $query .= "      T1.EXAMNO, ";
        $query .= "      RANK() OVER(ORDER BY INT (T2.REMARK3) DESC) AS RANK ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT T2 ";
        $query .= "         ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "         AND T2.SEQ = '003' ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";

        $data = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examno = $row["EXAMNO"];
            $rank   = $row["RANK"];

            //更新 -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT(SEQ003)
            $data = array();
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            $data["REMARK4"][TEXT]          = $rank;

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
            $where .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
            $where .= "     EXAMNO          = '{$examno}' AND ";
            $where .= "     SEQ             = '003' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            $db->query($query);
        }
    }
}
