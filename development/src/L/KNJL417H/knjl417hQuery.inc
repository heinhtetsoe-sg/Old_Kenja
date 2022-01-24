<?php
class knjl417hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '" . $year . "' AND ";
        $query .= "     NAMECD1 = '" . $namecd1 . "' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '" . $namecd2 . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタより取得
    public function getEntexamSettingMst($model, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     SEQ AS KEY, ";
        $query .= "     NAME1 AS NAME, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND SETTING_CD   = '{$settingCd}' ";
        if ($seq != "") {
            $query .= "     AND SEQ   = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //志望コース取得
    public function getExamCourseMst($model)
    {
        $query  = "   SELECT ";
        $query .= "       EXAMCOURSECD AS VALUE, ";
        $query .= "       EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL, ";
        $query .= "       EXAMCOURSECD AS KEY, ";
        $query .= "       EXAMCOURSE_NAME AS NAME";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_COURSE_MST ";
        $query .= "   WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV      = '0' ";
        $query .= "   ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //入試区分
    public function getTestdiv($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function selectQuery($model, $div="")
    {
        $query  = "   SELECT ";
        $query .= "       BASE.EXAMNO, ";
        $query .= "       BASE.NAME, ";
        $query .= "       CD003.REMARK1  AS MOSHI_NAME, ";
        $query .= "       CD003.REMARK2  AS MOSHI_SCORE, ";
        $query .= "       CD004.REMARK4  AS OPT_JUDGE, ";
        $query .= "       CD017.REMARK1  AS SCORE_SCHOLAR1, ";
        $query .= "       CD017.REMARK2  AS SCORE_SCHOLAR2, ";
        $query .= "       CD017.REMARK3  AS SCORE_SCHOLAR3, ";
        $query .= "       CD017.REMARK4  AS SCORE_SCHOLAR4, ";
        $query .= "       CD017.REMARK5  AS SCORE_SCHOLAR5, ";
        $query .= "       CD018.REMARK1  AS NAISHIN_SCHOLAR, ";
        $query .= "       CD018.REMARK2  AS MOSHI_SCHOLAR, ";
        $query .= "       CD018.REMARK3  AS OPT_SCHOLAR, ";
        $query .= "       CD018.REMARK11 AS LAST_SCHOLAR_DIV, ";
        $query .= "       CD018.REMARK12 AS LAST_SCHOLAR ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_APPLICANTCONFRPT_DAT CONF ";
        $query .= "       INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "           ON BASE.ENTEXAMYEAR    = CONF.ENTEXAMYEAR ";
        $query .= "           AND BASE.APPLICANTDIV  = CONF.APPLICANTDIV ";
        $query .= "           AND BASE.EXAMNO        = CONF.EXAMNO ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD003 ";
        $query .= "           ON CD003.ENTEXAMYEAR = CONF.ENTEXAMYEAR ";
        $query .= "           AND CD003.APPLICANTDIV = CONF.APPLICANTDIV ";
        $query .= "           AND CD003.EXAMNO = CONF.EXAMNO ";
        $query .= "           AND CD003.SEQ = '003' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD004 ";
        $query .= "           ON CD004.ENTEXAMYEAR = CONF.ENTEXAMYEAR ";
        $query .= "           AND CD004.APPLICANTDIV = CONF.APPLICANTDIV ";
        $query .= "           AND CD004.EXAMNO = CONF.EXAMNO ";
        $query .= "           AND CD004.SEQ = '004' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD017 ";
        $query .= "           ON CD017.ENTEXAMYEAR   = CONF.ENTEXAMYEAR ";
        $query .= "           AND CD017.APPLICANTDIV = CONF.APPLICANTDIV ";
        $query .= "           AND CD017.EXAMNO       = CONF.EXAMNO ";
        $query .= "           AND CD017.SEQ          = '017' ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD018 ";
        $query .= "           ON CD018.ENTEXAMYEAR   = CONF.ENTEXAMYEAR ";
        $query .= "           AND CD018.APPLICANTDIV = CONF.APPLICANTDIV ";
        $query .= "           AND CD018.EXAMNO       = CONF.EXAMNO ";
        $query .= "           AND CD018.SEQ          = '018' ";
        $query .= "   WHERE ";
        $query .= "       CONF.ENTEXAMYEAR      = '{$model->examYear}' ";
        $query .= "       AND CONF.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "       AND EXISTS ( ";
        $query .= "           SELECT ";
        $query .= "               'X' ";
        $query .= "           FROM ";
        $query .= "               ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "               LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD016 ";
        $query .= "                   ON RD016.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "                   AND RD016.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "                   AND RD016.TESTDIV       = RECEPT.TESTDIV ";
        $query .= "                   AND RD016.EXAM_TYPE     = RECEPT.EXAM_TYPE ";
        $query .= "                   AND RD016.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "                   AND RD016.SEQ           = '016' ";
        $query .= "               LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD017 ";
        $query .= "                   ON RD017.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "                   AND RD017.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "                   AND RD017.TESTDIV       = RECEPT.TESTDIV ";
        $query .= "                   AND RD017.EXAM_TYPE     = RECEPT.EXAM_TYPE ";
        $query .= "                   AND RD017.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "                   AND RD017.SEQ           = '017' ";
        $query .= "               LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD018 ";
        $query .= "                   ON RD018.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "                   AND RD018.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "                   AND RD018.TESTDIV       = RECEPT.TESTDIV ";
        $query .= "                   AND RD018.EXAM_TYPE     = RECEPT.EXAM_TYPE ";
        $query .= "                   AND RD018.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "                   AND RD018.SEQ           = '018' ";
        $query .= "           WHERE ";
        $query .= "               RECEPT.ENTEXAMYEAR        = CONF.ENTEXAMYEAR ";
        $query .= "               AND RECEPT.APPLICANTDIV   = CONF.APPLICANTDIV ";
        $query .= "               AND RECEPT.EXAMNO         = CONF.EXAMNO ";
        $query .= "               AND ( ";
        $query .= "                      ('{$model->field["EXAMCOURSECD"]}', '1') = (RD016.REMARK1, RD016.REMARK3) ";
        $query .= "                   OR ('{$model->field["EXAMCOURSECD"]}', '1') = (RD017.REMARK1, RD017.REMARK3) ";
        $query .= "                   OR ('{$model->field["EXAMCOURSECD"]}', '1') = (RD018.REMARK1, RD018.REMARK3) ";
        $query .= "               ) ";
        $query .= "       ) ";
        if ($div == "") {
            $query .= "     AND CONF.EXAMNO   >= '{$model->s_examno}' ";
        }
        if ($div == "back") {
        }
        $query .= "     ORDER BY ";
        $query .= "         CONF.EXAMNO ";
        if ($div == "") {
            $query .= "     FETCH FIRST 50 ROWS ONLY ";
        }

        return $query;
    }

    public function getConfRptCount($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        return $query;
    }

    public function getConfDetailCount($model, $examno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->examData as $examno => $row) {

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(UPD or INS)
            $seq = '018';
            $reccnt = $db->getOne(knjl417hQuery::getConfDetailCount($model, $examno, $seq));
            $data = array();
            $data["REMARK11"][TEXT]         = $row["LAST_SCHOLAR_DIV"];
            $data["REMARK12"][TEXT]         = $row["LAST_SCHOLAR"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($reccnt > 0) {
                $where  = "     WHERE ENTEXAMYEAR  = '" . $model->examYear . "' ";
                $where .= "       AND APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "' ";
                $where .= "       AND EXAMNO       = '" . $examno . "' ";
                $where .= "       AND SEQ          = '" . $seq . "' ";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->examYear;
                $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = $seq;
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
