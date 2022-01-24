<?php
class knjl418hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->examYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND SETTING_CD   = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //受験コース取得
    public function getExamCourseMst($model, $examcoursecd = "")
    {
        $query  = "   SELECT ";
        $query .= "       *, ";
        $query .= "       EXAMCOURSECD AS VALUE, ";
        $query .= "       EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= "   FROM ";
        $query .= "       ENTEXAM_COURSE_MST ";
        $query .= "   WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '0' "; // '0'固定
        if ($examcoursecd) {
            $query .= "     AND EXAMCOURSECD = '{$examcoursecd}' ";
        }
        $query .= "   ORDER BY ";
        $query .= "       VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $setExamno = "")
    {
        $query  = " WITH GOKAKU_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         RD1.ENTEXAMYEAR, ";
        $query .= "         RD1.APPLICANTDIV, ";
        $query .= "         RD1.TESTDIV, ";
        $query .= "         RD1.EXAM_TYPE, ";
        $query .= "         RD1.RECEPTNO, ";
        $query .= "         RD1.SEQ, ";
        $query .= "         RD1.REMARK1 AS COURSE_CD, ";
        $query .= "         RD1.REMARK2, ";
        $query .= "         RD1.REMARK3 AS COURSE_JUDGE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DETAIL_DAT RD1 ";
        $query .= "     WHERE ";
        $query .= "         RD1.SEQ = '016' ";
        $query .= "         AND RD1.REMARK3 = '1' "; //1:合格
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     R1.ENTEXAMYEAR, ";
        $query .= "     R1.APPLICANTDIV, ";
        $query .= "     G1.COURSE_CD, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     SUM(CASE WHEN G1.TESTDIV = '1' THEN RD019.REMARK9 ELSE NULL END) AS HANTEI_1, ";
        $query .= "     SUM(CASE WHEN G1.TESTDIV = '2' THEN RD019.REMARK9 ELSE NULL END) AS HANTEI_2, ";
        $query .= "     SUM(CASE WHEN G1.TESTDIV = '3' THEN RD019.REMARK9 ELSE NULL END) AS HANTEI_3, ";
        $query .= "     SUM(CASE WHEN G1.TESTDIV = '4' THEN RD019.REMARK9 ELSE NULL END) AS HANTEI_4, ";
        $query .= "     SUM(CASE WHEN G1.TESTDIV = '5' THEN RD019.REMARK9 ELSE NULL END) AS HANTEI_5, ";
        $query .= "     BD029.REMARK6 AS HANTEI_LAST, ";
        $query .= "     BD029.REMARK7 AS KUBUN ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN GOKAKU_DAT G1 ";
        $query .= "          ON G1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND G1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND G1.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND G1.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND G1.RECEPTNO     = R1.RECEPTNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD029 ";
        $query .= "          ON BD029.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND BD029.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND BD029.EXAMNO       = B1.EXAMNO ";
        $query .= "         AND BD029.SEQ          = '029' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD019 ";
        $query .= "          ON RD019.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND RD019.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND RD019.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND RD019.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND RD019.RECEPTNO     = R1.RECEPTNO ";
        $query .= "         AND RD019.SEQ          = '019' ";
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR      = '{$model->examYear}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND G1.COURSE_CD    = '{$model->examcoursecd}' ";
        if ($setExamno != "") {
            $query .= "  AND R1.EXAMNO = '{$setExamno}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     R1.ENTEXAMYEAR, ";
        $query .= "     R1.APPLICANTDIV, ";
        $query .= "     G1.COURSE_CD, ";
        $query .= "     R1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     BD029.REMARK6,";
        $query .= "     BD029.REMARK7 ";
        $query .= " ORDER BY ";
        $query .= "     R1.EXAMNO ";

        return $query;
    }

    //最終特別奨学生判定データの存在チェック
    public function getExistsBaseDatSql($model, $examno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR   = '{$model->examYear}' ";
        $query .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND EXAMNO        = '{$examno}' ";
        $query .= "   AND SEQ           = '{$seq}' ";

        return $query;
    }

    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->updateData as $key => $value) {
            //EXAMNO
            $examno = $value["EXAMNO"];
            $seq = "029";

            //存在チェック
            $dataCnt = $db->getOne(knjl418hQuery::getExistsBaseDatSql($model, $examno, $seq));

            //データセット
            $data = array();
            $data["REMARK6"][TEXT]          = $value["HANTEI_LAST"];
            $data["REMARK7"][TEXT]          = $value["KUBUN"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->examYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO        = '{$examno}' ";
                $where .= "   AND SEQ           = '{$seq}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->examYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = $seq;
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < count($data_arr); $i++) {
            //EXAMNO
            $examno = $data_arr[$i]["EXAMNO"];
            $seq = "029";

            //存在チェック
            $dataCnt = $db->getOne(knjl418hQuery::getExistsBaseDatSql($model, $examno, $seq));

            //データセット
            $data = array();
            $data["REMARK6"][TEXT]          = $data_arr[$i]["HANTEI_LAST"];
            $data["REMARK7"][TEXT]          = $data_arr[$i]["KUBUN"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($dataCnt > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '{$model->examYear}' ";
                $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
                $where .= "   AND EXAMNO        = '{$examno}' ";
                $where .= "   AND SEQ           = '{$seq}' ";
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->examYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $data["SEQ"][TEXT]              = $seq;
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            }

            $db->query($query);
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
?>
