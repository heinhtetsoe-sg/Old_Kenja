<?php
class knjl590iQuery extends Query
{
    //リストtoリスト取得
    public function getBaseDatData($model)
    {
        $query  = "";
        $query .= " WITH GOKAKU_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR,";
        $query .= "         APPLICANTDIV, ";
        $query .= "         SEQ ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SETTING_MST ";
        $query .= "     WHERE ";
        $query .= "         SETTING_CD     = 'L013' ";
        $query .= "         AND NAMESPARE1 = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO AS VALUE, ";
        $query .= "     B1.EXAMNO || '：' || B1.NAME AS LABEL, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.ENTDIV, ";
        $query .= "     GEN01.REMARK2 AS SUC_TOTALCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     INNER JOIN GOKAKU_SEQ G1 ";
        $query .= "          ON G1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND G1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND G1.SEQ          = R1.JUDGEDIV ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD015 ";
        $query .= "          ON RD015.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND RD015.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND RD015.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND RD015.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND RD015.RECEPTNO     = R1.RECEPTNO ";
        $query .= "         AND RD015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GEN03 ";
        $query .= "          ON GEN03.ENTEXAMYEAR  = RD015.ENTEXAMYEAR ";
        $query .= "         AND GEN03.APPLICANTDIV = RD015.APPLICANTDIV ";
        $query .= "         AND GEN03.TESTDIV      = '0' ";
        $query .= "         AND GEN03.GENERAL_DIV  = '03' ";
        $query .= "         AND GEN03.GENERAL_CD   = RD015.REMARK3 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GEN02 ";
        $query .= "          ON GEN02.ENTEXAMYEAR  = GEN03.ENTEXAMYEAR ";
        $query .= "         AND GEN02.APPLICANTDIV = GEN03.APPLICANTDIV ";
        $query .= "         AND GEN02.TESTDIV      = '0' ";
        $query .= "         AND GEN02.GENERAL_DIV  = '02' ";
        $query .= "         AND GEN02.GENERAL_CD   = GEN03.REMARK1 ";
        $query .= "     LEFT JOIN ENTEXAM_GENERAL_MST GEN01 ";
        $query .= "          ON GEN01.ENTEXAMYEAR  = GEN02.ENTEXAMYEAR ";
        $query .= "         AND GEN01.APPLICANTDIV = GEN02.APPLICANTDIV ";
        $query .= "         AND GEN01.TESTDIV      = '0' ";
        $query .= "         AND GEN01.GENERAL_DIV  = '01' ";
        $query .= "         AND GEN01.GENERAL_CD   = GEN02.REMARK1 ";
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR      = '".$model->examYear."' ";
        $query .= "     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.PROCEDUREDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入学辞退者一覧を配列に格納
        $passExamNo = explode(",", $model->selectdata);

        $queryBase = knjl590iQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($base["EXAMNO"], $passExamNo)) {
                $setEntDiv = "2"; // 辞退
                $setSucCoursecd = "";
                $setSucMajorcd  = "";
            } else {
                $setEntDiv = "1"; // 入学
                $setSucCoursecd = (strlen($base["SUC_TOTALCD"]) == 4) ? substr($base["SUC_TOTALCD"], 0, 1) : "";
                $setSucMajorcd  = (strlen($base["SUC_TOTALCD"]) == 4) ? substr($base["SUC_TOTALCD"], 1, 3) : "";
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $query = knjl590iQuery::updateBaseDatSql($model, $base["EXAMNO"], $setEntDiv, $setSucCoursecd, $setSucMajorcd);

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
        $queryBase = knjl590iQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = array_search($base["EXAMNO"], array_column($data_arr, "EXAMNO"));
            if ($key !== false) {
                if ($data_arr[$key]["ERROR"] == "1") {
                    // エラーとなったレコードは処理しない
                    continue;
                }

                $setEntDiv = "2"; // 辞退
                $setSucCoursecd = "";
                $setSucMajorcd  = "";
            } else {
                $setEntDiv = "1"; // 入学
                $setSucCoursecd = (strlen($base["SUC_TOTALCD"]) == 4) ? substr($base["SUC_TOTALCD"], 0, 1) : "";
                $setSucMajorcd  = (strlen($base["SUC_TOTALCD"]) == 4) ? substr($base["SUC_TOTALCD"], 1, 3) : "";
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $query = knjl590iQuery::updateBaseDatSql($model, $base["EXAMNO"], $setEntDiv, $setSucCoursecd, $setSucMajorcd);

            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //ENTEXAM_APPLICANTBASE_DAT更新用SQL
    public function updateBaseDatSql($model, $examno, $setEntDiv, $sucCoursecd, $sucMajorcd)
    {
        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTDIV"][TEXT]           = $setEntDiv;
        $data["SUC_COURSECD"][TEXT]     = $sucCoursecd;
        $data["SUC_MAJORCD"][TEXT]      = $sucMajorcd;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->examYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND EXAMNO        = '{$examno}' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //受験番号　getBaseDatDataと同じ条件
    public function checkExamNo($model, $examNo)
    {
        $query  = "";
        $query .= " WITH GOKAKU_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR,";
        $query .= "         APPLICANTDIV, ";
        $query .= "         SEQ ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SETTING_MST ";
        $query .= "     WHERE ";
        $query .= "         SETTING_CD     = 'L013' ";
        $query .= "         AND NAMESPARE1 = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     INNER JOIN GOKAKU_SEQ G1 ";
        $query .= "          ON G1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND G1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND G1.SEQ          = R1.JUDGEDIV ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD020 ";
        $query .= "          ON RD020.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND RD020.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND RD020.TESTDIV      = R1.TESTDIV ";
        $query .= "         AND RD020.EXAM_TYPE    = R1.EXAM_TYPE ";
        $query .= "         AND RD020.RECEPTNO     = R1.RECEPTNO ";
        $query .= "         AND RD020.SEQ          = '020' ";
        $query .= "         AND RD020.REMARK1      = '1' ";    //入学金申込金 手続区分 1:済み
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR      = '".$model->examYear."' ";
        $query .= "     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.EXAMNO       = '".$examNo."' ";

        return $query;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }
}
