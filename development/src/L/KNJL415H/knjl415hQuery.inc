<?php
class knjl415hQuery extends Query
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
        $query .= "         	ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND 	APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND 	SETTING_CD   = '{$settingCd}' ";
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

    public function selectQuery($model, $div = "")
    {
        $query  = " WITH MAKE_EXAMKEY AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.APPLICANTDIV, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME ";
        $query .= "   FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT B0 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "         ON B1.ENTEXAMYEAR = B0.ENTEXAMYEAR ";
        $query .= "        AND B1.APPLICANTDIV = B0.APPLICANTDIV ";
        $query .= "        AND B1.EXAMNO = B0.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "       B0.ENTEXAMYEAR       = '{$model->examYear}' ";
        $query .= "       AND B0.APPLICANTDIV  = '{$model->field["APPLICANTDIV"]}' ";
        if ($div == "") {
            $query .= "       AND B0.RECEPTNO      >= '{$model->s_receptno}' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   CONFRPT.EXAMNO, ";
        $query .= "   BASE.NAME, ";
        $query .= "   CD005.REMARK1  AS CD005_REMARK1, ";      //内申内諾判定コース
        $query .= "   CD005.REMARK2  AS CD005_REMARK2, ";      //内申内諾判定出願区分
        $query .= "   CD005.REMARK5  AS CD005_REMARK5, ";      //強化クラブ内諾判定コース
        $query .= "   CD005.REMARK6  AS CD005_REMARK6, ";      //強化クラブ内諾判定出願区分
        $query .= "   CD003.REMARK1  AS CD003_REMARK1, ";      //模試コード
        $query .= "   CD003.REMARK2  AS CD003_REMARK2, ";      //模試成績
        $query .= "   CD003.REMARK10 AS CD003_REMARK10, ";     //模試内諾判定コース
        $query .= "   CD003.REMARK11 AS CD003_REMARK11, ";     //模試内諾判定出願区分
        $query .= "   CD004.REMARK4  AS CD004_REMARK4, ";      //OPT判定
        $query .= "   CD004.REMARK10 AS CD004_REMARK10, ";     //OPT内諾判定コース
        $query .= "   CD004.REMARK11 AS CD004_REMARK11, ";     //OPT内諾判定出願区分
        $query .= "   CD006.EXAMNO   AS SEQ006_EXAMNO, ";      //受験番号(更新済フラグ用)
        $query .= "   CD006.REMARK10 AS CD006_REMARK10, ";     //最終内諾判定(手入力)
        $query .= "   CD006.REMARK11 AS CD006_REMARK11, ";     //最終内諾出願区分(手入力)
        $query .= "   CD006.REMARK12 AS CD006_REMARK12 ";      //最終内諾判定区分(手入力)
        $query .= " FROM ";
        $query .= "   MAKE_EXAMKEY BASE ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ";
        $query .= "     ON BASE.ENTEXAMYEAR     = CONFRPT.ENTEXAMYEAR ";
        $query .= "    AND BASE.APPLICANTDIV   = CONFRPT.APPLICANTDIV ";
        $query .= "    AND BASE.EXAMNO         = CONFRPT.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD003 ";
        $query .= "     ON CD003.ENTEXAMYEAR    = CONFRPT.ENTEXAMYEAR ";
        $query .= "    AND CD003.APPLICANTDIV  = CONFRPT.APPLICANTDIV ";
        $query .= "    AND CD003.EXAMNO        = CONFRPT.EXAMNO ";
        $query .= "    AND CD003.SEQ           = '003' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD004 ";
        $query .= "     ON CD004.ENTEXAMYEAR    = CONFRPT.ENTEXAMYEAR ";
        $query .= "    AND CD004.APPLICANTDIV  = CONFRPT.APPLICANTDIV ";
        $query .= "    AND CD004.EXAMNO        = CONFRPT.EXAMNO ";
        $query .= "    AND CD004.SEQ           = '004' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD005 ";
        $query .= "     ON CD005.ENTEXAMYEAR    = CONFRPT.ENTEXAMYEAR ";
        $query .= "    AND CD005.APPLICANTDIV  = CONFRPT.APPLICANTDIV ";
        $query .= "    AND CD005.EXAMNO        = CONFRPT.EXAMNO ";
        $query .= "    AND CD005.SEQ           = '005' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CD006 ";
        $query .= "     ON CD006.ENTEXAMYEAR    = CONFRPT.ENTEXAMYEAR ";
        $query .= "    AND CD006.APPLICANTDIV  = CONFRPT.APPLICANTDIV ";
        $query .= "    AND CD006.EXAMNO        = CONFRPT.EXAMNO ";
        $query .= "    AND CD006.SEQ           = '006' ";
        $query .= " WHERE ";
        $query .= "   CONFRPT.ENTEXAMYEAR           = '{$model->examYear}' ";
        $query .= "   AND CONFRPT.APPLICANTDIV      = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["MOCK_LINE"] > 0) {
            $query .= " AND VALUE(CD003.REMARK2, 0) >= {$model->field["MOCK_LINE"]} ";
        }
        $query .= "   AND EXISTS ( ";
        $query .= "       SELECT ";
        $query .= "         'X' ";
        $query .= "       FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD015 ";
        $query .= "           ON RD015.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "          AND RD015.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "          AND RD015.TESTDIV       = RECEPT.TESTDIV ";
        $query .= "          AND RD015.EXAM_TYPE     = RECEPT.EXAM_TYPE ";
        $query .= "          AND RD015.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "          AND RD015.SEQ           = '015' ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD016 ";
        $query .= "           ON RD016.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "          AND RD016.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "          AND RD016.TESTDIV       = RECEPT.TESTDIV ";
        $query .= "          AND RD016.EXAM_TYPE     = RECEPT.EXAM_TYPE ";
        $query .= "          AND RD016.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "          AND RD016.SEQ            = '016' ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD017 ";
        $query .= "           ON RD017.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ";
        $query .= "          AND RD017.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "          AND RD017.TESTDIV       = RECEPT.TESTDIV ";
        $query .= "          AND RD017.EXAM_TYPE     = RECEPT.EXAM_TYPE ";
        $query .= "          AND RD017.RECEPTNO      = RECEPT.RECEPTNO ";
        $query .= "          AND RD017.SEQ           = '017' ";
        $query .= "       WHERE ";
        $query .= "         RECEPT.ENTEXAMYEAR      = CONFRPT.ENTEXAMYEAR ";
        $query .= "         AND RECEPT.APPLICANTDIV = CONFRPT.APPLICANTDIV ";
        $query .= "         AND RECEPT.EXAMNO       = CONFRPT.EXAMNO ";
        $query .= "         AND '{$model->field["EXAMCOURSECD"]}' IN ( ";
        $query .= "             RD015.REMARK1, ";
        $query .= "             RD016.REMARK1, ";
        $query .= "             RD017.REMARK1  ";
        $query .= "         ) ";
        $query .= "   ) ";
        $query .= "  ";
        $query .= "     ORDER BY ";
        $query .= "         CONFRPT.EXAMNO ";
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

    public function getExamNo($model, $receptno)
    {
        $query  = " SELECT ";
        $query .= "   MIN(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND RECEPTNO        = '{$receptno}' ";
        return $query;
    }

    public function getReceptNo($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "   MIN(RECEPTNO) AS RECEPTNO ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$examno}' ";
        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->examData as $examno => $row) {
            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(UPD or INS)
            $seq = '006';
            $reccnt = $db->getOne(knjl415hQuery::getConfDetailCount($model, $examno, $seq));
            $data = array();
            $data["REMARK10"][TEXT]         = $row["CD006_REMARK10"];
            $data["REMARK11"][TEXT]         = $row["CD006_REMARK11"];
            $data["REMARK12"][TEXT]         = $row["CD006_REMARK12"];
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
