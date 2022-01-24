<?php

require_once('for_php7.php');

class knjl082aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getTestdivMst($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     NULL AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     TDIV.TESTDIV_NAME AS TESTDIVNAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     L012.NAME1 AS COURSENAME, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     D022.REMARK2 AS JITAI_FLG ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD030 ";
        $query .= "          ON AD030.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND AD030.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND AD030.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         AND AD030.SEQ           = '030' ";
        $query .= "         AND AD030.REMARK1       = RCPT.TESTDIV ";
        $query .= "         AND AD030.REMARK2       = RCPT.RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D022 ";
        $query .= "          ON D022.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND D022.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND D022.EXAMNO         = RCPT.EXAMNO ";
        $query .= "         AND D022.SEQ            = '022' ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST TDIV ";
        $query .= "          ON TDIV.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND TDIV.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND TDIV.TESTDIV        = RCPT.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "          ON Z002.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND Z002.NAMECD1    = 'Z002' ";
        $query .= "         AND Z002.NAMECD2    = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L003 ";
        $query .= "          ON L003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L003.NAMECD1    = 'L003' ";
        $query .= "         AND L003.NAMECD2    = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L012 ";
        $query .= "          ON L012.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L012.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ";
        $query .= "         AND L012.NAMECD2    = BASE.ENTDIV ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '".$model->applicantdiv."' ";
        if ($model->testdiv != "ALL") {
            $query .= "     AND RCPT.TESTDIV    = '".$model->testdiv."' ";
        }
        $query .= "     AND RCPT.EXAM_TYPE      = '".$model->examType."' ";
        $query .= "     AND BASE.PROCEDUREDIV   = '1' ";
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    //存在チェック
    public function checkExists($model)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV    = '".$model->applicantdiv."' ";
        $query .= "     AND SEQ             = '022'";
        $query .= " GROUP BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //更新処理
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        $exists = array();
        $query = knjl082aQuery::checkExists($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $exists[$row["EXAMNO"]] = $row["CNT"];
        }
        $result->free();

        //更新
        foreach ($model->setReceptNo as $key => $ReceptNo) {
            $examno = $model->arrExamNo[$ReceptNo];

            $data = array();
            $data["REMARK2"][TEXT]      = $model->arrJitaiFlg[$ReceptNo];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            if ($exists[$examno] > 0) {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "   AND EXAMNO        = '".$examno."' ";
                $where .= "   AND SEQ           = '022' ";

                //更新
                $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where));
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
                $data["EXAMNO"][TEXT]       = $examno;
                $data["SEQ"][TEXT]          = '022';

                //追加
                $db->query(Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
