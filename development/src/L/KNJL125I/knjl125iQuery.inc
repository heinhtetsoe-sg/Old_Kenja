<?php
class knjl125iQuery extends Query
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

    //合格コンボ
    public function getEntexamSettingMstL013($model)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE,";
        $query .= "     SEQ || ':' || VALUE(NAME1,'') AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->examyear}' AND ";
        $query .= "     APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     SETTING_CD = 'L013' AND ";
        $query .= "     SEQ <> '4' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = "     SELECT ";
        $query .= "         RECEPT.RECEPTNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         RECEPT.TOTAL1, ";
        $query .= "         RECEPT.JUDGEDIV, ";
        $query .= "         L013.NAME1 AS JUDGEDIV_NAME, ";
        $query .= "         BASE.JUDGE_KIND, ";
        $query .= "         L025.ABBV1 AS JUDGE_KIND_MARK, ";
        $query .= "         RECEPT.EXAMNO "; //更新時使用
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "             ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "             AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "             AND RECEPT.EXAMNO = BASE.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_SETTING_MST L013 ";
        $query .= "             ON L013.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND L013.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND L013.SETTING_CD = 'L013' ";
        $query .= "             AND L013.SEQ = RECEPT.JUDGEDIV ";
        $query .= "         LEFT JOIN ENTEXAM_SETTING_MST L025 ";
        $query .= "             ON L025.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "             AND L025.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "             AND L025.SETTING_CD = 'L025' ";
        $query .= "             AND L025.SEQ = BASE.JUDGE_KIND ";
        $query .= "     WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "         AND RECEPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND RECEPT.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "         AND RECEPT.EXAM_TYPE    = '1' ";
        $query .= "         AND VALUE(RECEPT.JUDGEDIV, '') <> '4' "; //欠席者は除外
        if ($model->field["DISP_DIV"] != "1") { //抽出
            $sexDiv = ($model->field["DISP_DIV"] == "2") ? "1" : "2";
            $query .= "         AND BASE.SEX  = '{$sexDiv}' ";
        }
        $query .= "     ORDER BY ";
        if ($model->field["SORT"] == "2") { //表示順
            $query .= "         RECEPT.EXAMNO ";
        } else {
            $query .= "         RECEPT.TOTAL1 ";
        }

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

    //更新
    public function updateQuery($db, $model)
    {
        foreach ($model->recept_arr as $receptno) {
            if ($model->checkVal[$receptno]) {
                //更新 --ENTEXAM_RECEPT_DAT
                $data = array();
                $data["JUDGEDIV"][TEXT]         = $model->field["JUDGEDIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
                $where .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
                $where .= "     TESTDIV         = '{$model->field["TESTDIV"]}' AND ";
                $where .= "     EXAM_TYPE       = '1' AND ";
                $where .= "     RECEPTNO        = '{$receptno}' ";

                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
                $db->query($query);

                //更新 --ENTEXAM_APPLICANTBASE_DAT
                $examno = $model->receptExamno[$receptno];

                $data = array();
                $data["JUDGEMENT"][TEXT]        = $model->field["JUDGEDIV"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $where  = " WHERE ";
                $where .= "     ENTEXAMYEAR     = '{$model->examyear}' AND ";
                $where .= "     APPLICANTDIV    = '{$model->applicantdiv}' AND ";
                $where .= "     EXAMNO          = '{$examno}' ";

                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            }
        }
    }
}
