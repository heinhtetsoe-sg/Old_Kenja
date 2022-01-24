<?php

require_once('for_php7.php');
class knjx160aQuery extends Query
{
    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX160A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL($model)
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT T1.GRADE,HR_CLASS,HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY T1.GRADE,HR_CLASS";

        return $query;
    }

    //名称マスタ存在チェックSQL
    public function checkMstData($namecd1, $namecd2)
    {
        $query  = "SELECT COUNT(*)";
        $query .= " FROM NAME_MST";
        $query .= " WHERE NAMECD1 = '".$namecd1."' ";
        $query .= "       AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();
        $addr = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            //データセット（保護者住所データ）
            $issuedate = date("Y-m-d", strtotime($data_arr[$i]["ISSUEDATE"]));
            $expiredate = date("Y-m-d", strtotime($data_arr[$i]["EXPIREDATE"]));

            $checkUpd = $db->getOne(knjx160aQuery::getUpdData($data_arr[$i]["SCHREGNO"], $issuedate, $expiredate));

            $addr["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
            $addr["ISSUEDATE"][TEXT]            = $issuedate;
            $addr["EXPIREDATE"][TEXT]           = $expiredate;
            $addr["GUARD_ZIPCD"][TEXT]          = $data_arr[$i]["GUARD_ZIPCD"];
            $addr["GUARD_ADDR1"][TEXT]          = $data_arr[$i]["GUARD_ADDR1"];
            $addr["GUARD_ADDR2"][TEXT]          = $data_arr[$i]["GUARD_ADDR2"];
            $addr["GUARD_ADDR_FLG"][TEXT]       = $data_arr[$i]["GUARD_ADDR_FLG"];
            $addr["GUARD_TELNO"][TEXT]          = $data_arr[$i]["GUARD_TELNO"];
            $addr["GUARD_TELNO2"][TEXT]         = $data_arr[$i]["GUARD_TELNO2"];
            $addr["GUARD_FAXNO"][TEXT]          = $data_arr[$i]["GUARD_FAXNO"];
            $addr["GUARD_E_MAIL"][TEXT]         = $data_arr[$i]["GUARD_E_MAIL"];
            $addr["REGISTERCD"][TEXT]           = STAFFCD;
            $addr["UPDATED"][NUMBER]            = "SYSDATE()";

            if (1 > $db->getOne("SELECT COUNT(*) FROM GUARDIAN_ADDRESS_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($addr, "GUARDIAN_ADDRESS_DAT");
            } else {
                if ($checkUpd > 0) {
                    $where  = " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                    $where .= "       AND ISSUEDATE = '".$issuedate."' ";
                    $where .= "       AND EXPIREDATE = '".$expiredate."' ";

                    $query = Query::updateSQL($addr, "GUARDIAN_ADDRESS_DAT", $where);
                } else {
                    //最新データの終了日を取得した開始日の前日にする
                    $pre_date = date("Y-m-d", strtotime("-1 day", strtotime($issuedate)));
                    $query = knjx160aQuery::addrUpdateQuery($data_arr[$i]["SCHREGNO"], $pre_date);
                    $db->query($query);

                    $query = Query::insertSQL($addr, "GUARDIAN_ADDRESS_DAT");
                }
            }
            $db->query($query);

            knjx160aQuery::insUpdAddressDat($db, $data_arr[$i], $model);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //保護者住所更新
    public function insUpdAddressDat($db, $data_arr, $model)
    {

        //データセット（保護者データ）
        $birthday = strlen($data_arr["GUARD_BIRTHDAY"]) ? date("Y-m-d", strtotime($data_arr["GUARD_BIRTHDAY"])) : "";
        $query = knjx160aQuery::getMaxAddress($data_arr);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $data["SCHREGNO"][TEXT]                = $data_arr["SCHREGNO"];
        $data["RELATIONSHIP"][TEXT]            = $data_arr["RELATIONSHIP"];
        $data["GUARD_NAME"][TEXT]              = $data_arr["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]              = $data_arr["GUARD_KANA"];
        $data["GUARD_REAL_NAME"][TEXT]         = $data_arr["GUARD_REAL_NAME"];
        $data["GUARD_REAL_KANA"][TEXT]         = $data_arr["GUARD_REAL_KANA"];
        $data["GUARD_SEX"][TEXT]               = $data_arr["GUARD_SEX"];
        $data["GUARD_BIRTHDAY"][TEXT]          = $birthday;
        $data["GUARD_ZIPCD"][TEXT]             = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]             = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]             = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]             = $setAddr["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]            = $setAddr["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]             = $setAddr["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]            = $setAddr["GUARD_E_MAIL"];
        $data["GUARD_JOBCD"][TEXT]             = $data_arr["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]         = $data_arr["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT]        = $data_arr["GUARD_WORK_TELNO"];
        $data["GUARANTOR_RELATIONSHIP"][TEXT]  = $data_arr["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]          = $data_arr["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]          = $data_arr["GUARANTOR_KANA"];
        $data["GUARANTOR_SEX"][TEXT]           = $data_arr["GUARANTOR_SEX"];
        $data["GUARANTOR_ZIPCD"][TEXT]         = $data_arr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]         = $data_arr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]         = $data_arr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]         = $data_arr["GUARANTOR_TELNO"];
        $data["GUARANTOR_JOBCD"][TEXT]         = $data_arr["GUARANTOR_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]           = $data_arr["PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]              = STAFFCD;
        $data["UPDATED"][NUMBER]               = "SYSDATE()";

        $insFlg = false;
        if (1 > $db->getOne("SELECT COUNT(*) FROM GUARDIAN_DAT WHERE SCHREGNO = '".$data_arr["SCHREGNO"]."'")) {
            $query = Query::insertSQL($data, "GUARDIAN_DAT");
            $insFlg = true;
        } else {
            $where = " WHERE SCHREGNO = '".$data_arr["SCHREGNO"]."' ";

            $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        }

        if ($model->field["SHORI_MEI"] == "1" || !$insFlg) {
            $db->query($query);
        }
    }

    //更新（保護者住所データ）
    public function addrUpdateQuery($schregno, $date)
    {
        $query  = " UPDATE ";
        $query .= "     GUARDIAN_ADDRESS_DAT";
        $query .= " SET ";
        $query .= "     EXPIREDATE = '".$date."' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     ISSUEDATE IN (SELECT ";
        $query .= "                         MAX(ISSUEDATE) ";
        $query .= "                     FROM ";
        $query .= "                         GUARDIAN_ADDRESS_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         SCHREGNO = '".$schregno."' ";
        $query .= "                     ) ";

        return $query;
    }

    //住所履歴の最新取得
    public function getMaxAddress($data)
    {
        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$data["SCHREGNO"]."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MAX_T, ";
        $query .= "     GUARDIAN_ADDRESS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     MAX_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND MAX_T.ISSUEDATE = T1.ISSUEDATE ";

        return $query;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $issuedate = date("Y-m-d", strtotime($data_arr[$i]["ISSUEDATE"]));
            //保護者住所データ
            $query  = " DELETE FROM GUARDIAN_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "     AND ISSUEDATE = '".$issuedate."' ";
            $db->query($query);

            knjx160aQuery::insUpdAddressDat($db, $data_arr[$i], $model);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新対象データか否か
    public function getUpdData($schregno, $issueDate, $expireDate)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= "     AND ISSUEDATE = '".$issueDate."' ";
        $query .= "     AND EXPIREDATE = '".$expireDate."' ";

        return $query;
    }

    //最新の開始日取得
    public function getAddrIssuedate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     MAX(ISSUEDATE)";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T3.RELATIONSHIP, ";
        $query .= "     T3.GUARD_NAME, ";
        $query .= "     T3.GUARD_KANA, ";
        $query .= "     T3.GUARD_REAL_NAME, ";
        $query .= "     T3.GUARD_REAL_KANA, ";
        $query .= "     T3.GUARD_SEX, ";
        $query .= "     T3.GUARD_BIRTHDAY, ";
        $query .= "     T4.ISSUEDATE, ";
        $query .= "     T4.EXPIREDATE, ";
        $query .= "     T4.GUARD_ZIPCD, ";
        $query .= "     T4.GUARD_ADDR1, ";
        $query .= "     T4.GUARD_ADDR2, ";
        $query .= "     T4.GUARD_ADDR_FLG, ";
        $query .= "     T4.GUARD_TELNO, ";
        $query .= "     T4.GUARD_TELNO2, ";
        $query .= "     T4.GUARD_FAXNO, ";
        $query .= "     T4.GUARD_E_MAIL, ";
        $query .= "     T3.GUARD_JOBCD, ";
        $query .= "     T3.GUARD_WORK_NAME, ";
        $query .= "     T3.GUARD_WORK_TELNO, ";
        $query .= "     T3.GUARANTOR_RELATIONSHIP, ";
        $query .= "     T3.GUARANTOR_NAME, ";
        $query .= "     T3.GUARANTOR_KANA, ";
        $query .= "     T3.GUARANTOR_SEX, ";
        $query .= "     T3.GUARANTOR_ZIPCD, ";
        $query .= "     T3.GUARANTOR_ADDR1, ";
        $query .= "     T3.GUARANTOR_ADDR2, ";
        $query .= "     T3.GUARANTOR_TELNO, ";
        $query .= "     T3.GUARANTOR_JOBCD, ";
        $query .= "     T3.PUBLIC_OFFICE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_ADDRESS_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= "     AND (T4.ISSUEDATE IS NULL OR T4.ISSUEDATE = ( SELECT MAX(TW4.ISSUEDATE) FROM GUARDIAN_ADDRESS_DAT TW4 WHERE T4.SCHREGNO = TW4.SCHREGNO )) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T4.ISSUEDATE ";

        return $query;
    }
}
