<?php
require_once('for_php7.php');
class knjl411mquery extends Query
{
    /* データ更新処理 */
    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;
        for ($i = 0; $i < count($data_arr); $i++) {
            knjl411mQuery::insupdAppBaseDat($db, $model, $data_arr[$i]);
            knjl411mQuery::insupdAppGuardDat($db, $model, $data_arr[$i]);
            knjl411mQuery::insupdReceptDat($db, $model, $data_arr[$i]);
            if ($model->field["EXAM"] == knjl411mModel::EXAM_NYUSHI_KIKOKUSEI) {
                knjl411mQuery::insupdAppConfRemarkDat($db, $model, $data_arr[$i]);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    public function insupdAppBaseDat($db, $model, &$data_arr_i)
    {
        $query  = " SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTBASE_DAT ";
        $where  = " WHERE ";
        $where .= " YEAR = '{$data_arr_i["ENTEXAMYEAR"]}' ";
        $where .= " AND EXAMNO = '{$data_arr_i["EXAMNO"]}' ";

        $cnt = $db->getOne($query.$where);

        $data["NAME"][TEXT] = $data_arr_i["NAME"];
        $data["NAME_KANA"][TEXT] = $data_arr_i["NAME_KANA"];
        $data["SEX"][TEXT] = $data_arr_i["SEX"];
        $data["BIRTHDAY"][DATE] = $data_arr_i["BIRTHDAY"];
        $data["FINSCHOOLCD"][TEXT] = $data_arr_i["FINSCHOOLCD"];
        $data["ZIPCD"][TEXT] = $data_arr_i["ZIPCD"];
        $data["ADDR1"][TEXT] = $data_arr_i["ADDR1"];
        $data["ADDR2"][TEXT] = $data_arr_i["ADDR2"];
        $data["TELNO"][TEXT] = $data_arr_i["TELNO"];
        $data["EMAIL"][TEXT] = $data_arr_i["EMAIL"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "SYSDATE()";

        if ($cnt > 0) {
            $query = Query::updateSQL($data, "ENTEXAM_STD_APPLICANTBASE_DAT", $where);
        } else {
            $data["YEAR"][TEXT] = $data_arr_i["ENTEXAMYEAR"];
            $data["EXAMNO"][TEXT] = $data_arr_i["EXAMNO"];
            $query = Query::insertSQL($data, "ENTEXAM_STD_APPLICANTBASE_DAT");
        }
        $db->query($query);
    }

    public function insupdAppGuardDat($db, $model, &$data_arr_i)
    {
        $query  = " SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTGUARDIAN_DAT ";
        $where  = " WHERE ";
        $where .= " YEAR = '{$data_arr_i["ENTEXAMYEAR"]}' ";
        $where .= " AND EXAMNO = '{$data_arr_i["EXAMNO"]}' ";
        $cnt = $db->getOne($query.$where);

        $data["GUARD_NAME"][TEXT] = $data_arr_i["GUARD_NAME"];
        $data["GUARD_NAME_KANA"][TEXT] = $data_arr_i["GUARD_NAME_KANA"];
        $data["RELATION"][TEXT] = $data_arr_i["RELATION"];
        $data["GUARD_ZIP"][TEXT] = $data_arr_i["ZIPCD"];
        $data["GUARD_ADDR1"][TEXT] = $data_arr_i["ADDR1"];
        $data["GUARD_ADDR2"][TEXT] = $data_arr_i["ADDR2"];
        $data["GUARD_TELNO"][TEXT] = $data_arr_i["GUARD_TELNO"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "SYSDATE()";
        if ($cnt > 0) {
            $query = Query::updateSQL($data, "ENTEXAM_STD_APPLICANTGUARDIAN_DAT", $where);
        } else {
            $data["YEAR"][TEXT] = $data_arr_i["ENTEXAMYEAR"];
            $data["EXAMNO"][TEXT] = $data_arr_i["EXAMNO"];
            $query = Query::insertSQL($data, "ENTEXAM_STD_APPLICANTGUARDIAN_DAT");
        }
        $db->query($query);
    }

    public function insupdReceptDat($db, $model, &$data_arr_i)
    {
        $query  = " SELECT COUNT(*) FROM ENTEXAM_STD_RECEPT_DAT ";
        $where  = " WHERE ";
        $where .= " YEAR = '{$data_arr_i["ENTEXAMYEAR"]}' ";
        $where .= " AND EXAMNO = '{$data_arr_i["EXAMNO"]}' ";
        $where .= " AND EXAM_SCHOOL_KIND = '{$data_arr_i["EXAM_SCHOOL_KIND"]}' ";
        $where .= " AND APPLICANT_DIV = '{$data_arr_i["APPLICANT_DIV"]}' ";
        $where .= " AND COURSE_DIV = '{$data_arr_i["COURSE_DIV"]}' ";
        $where .= " AND FREQUENCY = '{$data_arr_i["FREQUENCY"]}' ";
        $where .= " AND RECEPTNO = '{$data_arr_i["RECEPTNO"]}' ";
        $cnt = $db->getOne($query.$where);

        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "SYSDATE()";
        if ($cnt > 0) {
            //更新日付、更新者だけの更新となる。
            $query = Query::updateSQL($data, "ENTEXAM_STD_RECEPT_DAT", $where);
        } else {
            $data["YEAR"][TEXT] = $data_arr_i["ENTEXAMYEAR"];
            $data["EXAMNO"][TEXT] = $data_arr_i["EXAMNO"];
            $data["EXAM_SCHOOL_KIND"][TEXT] = $data_arr_i["EXAM_SCHOOL_KIND"];
            $data["APPLICANT_DIV"][TEXT] = $data_arr_i["APPLICANT_DIV"];
            $data["COURSE_DIV"][TEXT] = $data_arr_i["COURSE_DIV"];
            $data["FREQUENCY"][TEXT] = $data_arr_i["FREQUENCY"];
            $data["RECEPTNO"][TEXT] = $data_arr_i["RECEPTNO"];
            $query = Query::insertSQL($data, "ENTEXAM_STD_RECEPT_DAT");
        }
        $db->query($query);
    }

    public function insupdAppConfRemarkDat($db, $model, &$data_arr_i)
    {
        $query  = " SELECT COUNT(*) FROM ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT ";
        $where  = " WHERE ";
        $where .= " YEAR = '{$data_arr_i["ENTEXAMYEAR"]}' ";
        $where .= " AND EXAMNO = '{$data_arr_i["EXAMNO"]}' ";
        $cnt = $db->getOne($query.$where);

        $data["REMARK1"][TEXT] = $data_arr_i["REMARK1"];
        $data["REMARK2"][TEXT] = $data_arr_i["REMARK2"];
        $data["SPECIAL_REMARK"][TEXT] = $data_arr_i["SPECIAL_REMARK"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "SYSDATE()";
        if ($cnt > 0) {
            $query = Query::updateSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT", $where);
        } else {
            $data["YEAR"][TEXT] = $data_arr_i["ENTEXAMYEAR"];
            $data["EXAMNO"][TEXT] = $data_arr_i["EXAMNO"];
            $query = Query::insertSQL($data, "ENTEXAM_STD_APPLICANTCONFRPT_REMARK_DAT");
        }
        $db->query($query);
    }

    /*================================== 存在チェック ==================================*/

    //データ/件数取得 -- ENTEXAM_SETTING_MST(L301)
    public function getTestName($data, $cmd = "")
    {
        $query  = " SELECT ";
        if ($cmd == "cnt") {
            $query .= " count(*) ";
        } else {
            $query .= " ABBV1 AS APPLICANT_DIV, ABBV2 AS COURSE_DIV, ABBV3 AS FREQUENCY ";
        }
        $query .= " FROM ";
        $query .= "   ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '".$data["ENTEXAMYEAR"]."' ";
        $query .= "   AND APPLICANTDIV = '".$data["EXAM_SCHOOL_KIND"]."' ";
        $query .= "   AND SETTING_CD = 'L301' ";
        $query .= "   AND NAME1 = '".$data["TESTNAME"]."' ";
        return $query;
    }
    
    //データ/件数取得 -- NAME_MST(H201)
    public function getRelationName($data, $cmd = "")
    {
        $query  = " SELECT ";
        if ($cmd == "cnt") {
            $query .= " count(*) ";
        } else {
            $query .= " NAMECD2 AS RELATION ";
        }
        $query .= " FROM ";
        $query .= "   V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$data["ENTEXAMYEAR"]."' ";
        $query .= "   AND NAMECD1 = 'H201' ";
        $query .= "   AND NAME1 = '".$data["RELATIONNAME"]."' ";
        return $query;
    }

    /*================================== エラーデータ処理 ==================================*/
    //件数確認
    public function getCsvMsgPrgDatCnt()
    {
        return " SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
    }
    
    //エラーデータの削除
    public function deleteCsvMsgPrgDat()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT ";
        $query .= "WHERE PROGRAMID = '".PROGRAMID."'";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertCsvMsgPrgDat(&$db, $line, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $line;
        $data1["MSGREMARK"][TEXT] = (strlen($check_error) > 119) ? substr($check_error, 0, 119)." " : $check_error." ";

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);

        return;
    }

    //CVS作成用のQUERY
    public function selectCsvMsgPrgDat()
    {
        $query  = "SELECT MSGROW, MSGREMARK ";
        $query .= "FROM W_CSVMSG_PRG_DAT ";
        $query .= "WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }
}
