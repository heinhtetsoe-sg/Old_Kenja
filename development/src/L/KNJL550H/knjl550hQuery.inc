<?php

require_once('for_php7.php');
class knjl550hQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試判別取得
    public function getDistinctId($model)
    {
        $query  = " SELECT ";
        $query .= "     DISTINCT_ID AS VALUE, ";
        $query .= "     DISTINCT_ID || ':' || DISTINCT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DISTINCTION_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験科目
    public function getExamTypeTestSubclass($model)
    {
        $query  = " WITH RECEPT AS ( ";
        $query .= "                 SELECT ";
        $query .= "                     ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_RECEPT_DAT ";
        $query .= "                 WHERE ";
        $query .= "                         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "                     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                     AND DISTINCT_ID  = '{$model->distinctId}' ";
        $query .= "                 GROUP BY ";
        $query .= "                     ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUBC.SUBCLASSCD AS VALUE, ";
        $query .= "     SUBC.SUBCLASSCD || ':' || L009.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_SUBCLASS_MST SUBC ";
        $query .= "     INNER JOIN RECEPT RCPT ON SUBC.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                           AND SUBC.APPLICANTDIV = RCPT.APPLICANTDIV  ";
        $query .= "                           AND SUBC.EXAM_TYPE    = RCPT.EXAM_TYPE  ";
        $query .= "     LEFT JOIN V_NAME_MST L009 ON L009.YEAR    = SUBC.ENTEXAMYEAR ";
        $query .= "                              AND L009.NAMECD1 = 'L009'  ";
        $query .= "                              AND L009.NAMECD2 = SUBC.SUBCLASSCD  ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別、試験方式を取得
    public function getDistinctionMst($year, $applicantdiv, $distinctId)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DISTINCTION_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND DISTINCT_ID  = '{$distinctId}' ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $div)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        if ($div == "csv") {
            $query .= "         RCPT.ENTEXAMYEAR, ";
            $query .= "         RCPT.APPLICANTDIV, ";
            $query .= "         RCPT.DISTINCT_ID, ";
            $query .= "         D007.REMARK1 AS SHIGANSYA_SEQ, ";
            $query .= "         D007.REMARK2 AS MIRAI_TESTDIV, ";
            $query .= "         '".$model->lastColumn."' AS ".$model->lastColumn.", ";
        }
        $query .= "         RCPT.EXAMNO, ";
        $query .= "         RCPT.RECEPTNO, ";
        $query .= "         BASE.NAME, ";
        $query .= "         BASE.NAME_KANA, ";
        $query .= "         Z002.NAME1 AS SEX, ";
        $query .= "         D007.REMARK8 AS TEST_NAME_ABBV, ";
        $query .= "         '".$model->testSubclassCd."' AS TESTSUBCLASSCD, ";
        $query .= "         SCRE.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                                 AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                                 AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT SCRE ON SCRE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "                                         AND SCRE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "                                         AND SCRE.TESTDIV        = RCPT.TESTDIV ";
        $query .= "                                         AND SCRE.EXAM_TYPE      = RCPT.EXAM_TYPE ";
        $query .= "                                         AND SCRE.RECEPTNO       = RCPT.RECEPTNO ";
        $query .= "                                         AND SCRE.TESTSUBCLASSCD = '{$model->testSubclassCd}' ";
        $query .= "         LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "                                AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "         LEFT JOIN NAME_MST L003 ON L003.NAMECD1 = 'L003' ";
        $query .= "                                AND L003.NAMECD2 = RCPT.APPLICANTDIV ";
        $query .= "         LEFT JOIN ENTEXAM_DISTINCTION_MST DIST ON DIST.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                                               AND DIST.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                               AND DIST.DISTINCT_ID  = RCPT.DISTINCT_ID ";
        $query .= "         LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D007 ";
        $query .= "              ON D007.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "             AND D007.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "             AND D007.TESTDIV      = RCPT.TESTDIV ";
        $query .= "             AND D007.EXAM_TYPE    = RCPT.EXAM_TYPE ";
        $query .= "             AND D007.RECEPTNO     = RCPT.RECEPTNO ";
        $query .= "             AND D007.SEQ          = '007' ";
        $query .= "     WHERE ";
        $query .= "             RCPT.ENTEXAMYEAR    = '{$model->year}' ";
        $query .= "         AND RCPT.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "         AND RCPT.DISTINCT_ID    = '{$model->distinctId}' ";
        if ($div == "list") {
            if ($model->cmd == "next") {
                $query .= "         AND RCPT.EXAMNO > '{$model->e_examno}' ";
            } elseif ($model->cmd == "back") {
                $query .= "         AND RCPT.EXAMNO < '{$model->s_examno}' ";
            } else {
                if (strlen($model->s_examno)) {
                    $query .= "         AND RCPT.EXAMNO >= '{$model->s_examno}' ";
                }
            }

            $sort = ($model->cmd == "back") ? "DESC" : "";
            $query .= "     ORDER BY ";
            $query .= "         RCPT.EXAMNO ".$sort;
            $query .= "     FETCH FIRST 40 ROWS ONLY ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //登録
        foreach ($model->score as $receptno => $value) {
            //登録済みチェック
            //未登録なら追加、登録済みなら更新
            $conf_cnt = $db->getOne(knjl550hQuery::cntEntexamScoreDat($model, $receptno));

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testDiv;
            $data["EXAM_TYPE"][TEXT]        = $model->examType;
            $data["RECEPTNO"][TEXT]         = $receptno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testSubclassCd;

            $data["SCORE"][NUMBER]          = $value;

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR    = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $where .= "   AND TESTDIV        = '{$model->testDiv}' ";
            $where .= "   AND EXAM_TYPE      = '{$model->examType}' ";
            $where .= "   AND RECEPTNO       = '{$receptno}' ";
            $where .= "   AND TESTSUBCLASSCD = '{$model->testSubclassCd}' ";

            if (0 == $conf_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //件数取得 -- ENTEXAM_SCORE_DAT
    public function cntEntexamScoreDat($model, $receptno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testDiv}' ";
        $query .= "     AND EXAM_TYPE       = '{$model->examType}' ";
        $query .= "     AND RECEPTNO        = '{$receptno}' ";
        $query .= "     AND TESTSUBCLASSCD  = '{$model->testSubclassCd}' ";

        return $query;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            // 受付番号(RECEPTNO)取得
            $setReceptNo = $db->getOne(knjl550hQuery::getReceptNo($model, $data_arr[$i]["EXAMNO"]));

            // 件数取得
            $score_cnt = $db->getOne(knjl550hQuery::cntEntexamScoreDat($model, $setReceptNo));

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $model->testDiv;
            $data["EXAM_TYPE"][TEXT]        = $model->examType;
            $data["RECEPTNO"][TEXT]         = $setReceptNo;
            $data["TESTSUBCLASSCD"][TEXT]   = $data_arr[$i]["TESTSUBCLASSCD"];

            $data["SCORE"][NUMBER]          = $data_arr[$i]["SCORE"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR    = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV   = '{$data_arr[$i]["APPLICANTDIV"]}' ";
            $where .= "   AND TESTDIV        = '{$model->testDiv}' ";
            $where .= "   AND EXAM_TYPE      = '{$model->examType}' ";
            $where .= "   AND RECEPTNO       = '{$setReceptNo}' ";
            $where .= "   AND TESTSUBCLASSCD = '{$data_arr[$i]["TESTSUBCLASSCD"]}' ";

            if (0 == $score_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_SCORE_DAT", $where);
            }

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //受付番号取得(RECEPTNO)
    public function getReceptNo($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV         = '{$model->testDiv}' ";
        $query .= "     AND EXAM_TYPE       = '{$model->examType}' ";
        $query .= "     AND EXAMNO          = '{$examno}' ";
        $query .= "     AND DISTINCT_ID     = '{$model->distinctId}' ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    public function nameCntSql($model, $namecd, $setcd, $where = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //受験番号
    public function checkExamNo($model, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND DISTINCT_ID  = '{$model->distinctId}' ";
        $query .= "     AND EXAMNO       = '{$examNo}' ";

        return $query;
    }
}
