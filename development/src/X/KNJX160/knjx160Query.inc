<?php

require_once('for_php7.php');
class knjx160query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX160' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

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

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "SELECT DISTINCT T1.YEAR,T1.SEMESTER,T2.SEMESTERNAME ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR=T2.YEAR AND T1.SEMESTER=T2.SEMESTER ";
        $query .= "ORDER BY T1.YEAR,T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT DISTINCT GRADE,HR_CLASS,HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "ORDER BY GRADE,HR_CLASS";

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

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query = knjx160Query::getMaxAddress($data_arr[$i]);
            $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //データセット
            $data["SCHREGNO"][TEXT]                = $data_arr[$i]["SCHREGNO"];
            $data["RELATIONSHIP"][TEXT]            = $data_arr[$i]["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]              = $data_arr[$i]["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]              = $data_arr[$i]["GUARD_KANA"];
            $data["GUARD_REAL_NAME"][TEXT]         = $data_arr[$i]["GUARD_REAL_NAME"];
            $data["GUARD_REAL_KANA"][TEXT]         = $data_arr[$i]["GUARD_REAL_KANA"];
            $data["GUARD_SEX"][TEXT]               = $data_arr[$i]["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]          = str_replace("/", "-", $data_arr[$i]["GUARD_BIRTHDAY"]);
            $data["GUARD_ZIPCD"][TEXT]             = $setAddr["GUARD_ZIPCD"];
            $data["GUARD_ADDR1"][TEXT]             = $setAddr["GUARD_ADDR1"];
            $data["GUARD_ADDR2"][TEXT]             = $setAddr["GUARD_ADDR2"];
            $data["GUARD_TELNO"][TEXT]             = $setAddr["GUARD_TELNO"];
            $data["GUARD_TELNO2"][TEXT]            = $setAddr["GUARD_TELNO2"];
            $data["GUARD_FAXNO"][TEXT]             = $setAddr["GUARD_FAXNO"];
            $data["GUARD_E_MAIL"][TEXT]            = $setAddr["GUARD_E_MAIL"];
            $data["GUARD_JOBCD"][TEXT]             = $data_arr[$i]["GUARD_JOBCD"];
            $data["GUARD_WORK_NAME"][TEXT]         = $data_arr[$i]["GUARD_WORK_NAME"];
            $data["GUARD_WORK_TELNO"][TEXT]        = $data_arr[$i]["GUARD_WORK_TELNO"];
            $data["GUARANTOR_RELATIONSHIP"][TEXT]  = $data_arr[$i]["GUARANTOR_RELATIONSHIP"];
            $data["GUARANTOR_NAME"][TEXT]          = $data_arr[$i]["GUARANTOR_NAME"];
            $data["GUARANTOR_KANA"][TEXT]          = $data_arr[$i]["GUARANTOR_KANA"];
            $data["GUARANTOR_SEX"][TEXT]           = $data_arr[$i]["GUARANTOR_SEX"];
            $data["GUARANTOR_ZIPCD"][TEXT]         = $data_arr[$i]["GUARANTOR_ZIPCD"];
            $data["GUARANTOR_ADDR1"][TEXT]         = $data_arr[$i]["GUARANTOR_ADDR1"];
            $data["GUARANTOR_ADDR2"][TEXT]         = $data_arr[$i]["GUARANTOR_ADDR2"];
            $data["GUARANTOR_TELNO"][TEXT]         = $data_arr[$i]["GUARANTOR_TELNO"];
            $data["GUARANTOR_JOBCD"][TEXT]         = $data_arr[$i]["GUARANTOR_JOBCD"];
            $data["PUBLIC_OFFICE"][TEXT]           = $data_arr[$i]["PUBLIC_OFFICE"];
            $data["REGISTERCD"][TEXT]              = STAFFCD;
            $data["UPDATED"][NUMBER]               = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM GUARDIAN_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."'")) {
                $query = Query::insertSQL($data, "GUARDIAN_DAT");
            } else {
                $where  = "WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
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
            $query  = " DELETE FROM GUARDIAN_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.RELATIONSHIP, ";
        $query .= "     T3.GUARD_NAME, ";
        $query .= "     T3.GUARD_KANA, ";
        $query .= "     T3.GUARD_REAL_NAME, ";
        $query .= "     T3.GUARD_REAL_KANA, ";
        $query .= "     T3.GUARD_SEX, ";
        $query .= "     T3.GUARD_BIRTHDAY, ";
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
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T3.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
