<?php

require_once('for_php7.php');
class knjx030query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX030' ";
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
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT DISTINCT T1.YEAR, T1.SEMESTER, T2.SEMESTERNAME ";
        $query .= " FROM SCHREG_REGD_HDAT T1 ";
        $query .= "        INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY T1.YEAR, T1.SEMESTER";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT T1.GRADE, HR_CLASS, HR_NAME ";
        $query .= " FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE T1.YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY T1.GRADE, HR_CLASS";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($table, $where)
    {
        $query = " SELECT UPDATED FROM " . $table . $where;

        return $query;
    }

    //CSVデータ取得
    public function getMainCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T3.NAME, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.AREACD, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2, ";
        $query .= "     T1.ADDR_FLG, ";
        $query .= "     T1.ADDR1_ENG, ";
        $query .= "     T1.ADDR2_ENG, ";
        $query .= "     T1.TELNO, ";
        $query .= "     T1.TELNO2, ";
        $query .= "     T1.FAXNO, ";
        $query .= "     T1.EMAIL, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO=T3.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T2.YEAR AND GDAT.GRADE = T2.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.YEAR || T2.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= "     AND T1.ISSUEDATE = ( SELECT MAX(TW1.ISSUEDATE) FROM SCHREG_ADDRESS_DAT TW1 WHERE T1.SCHREGNO = TW1.SCHREGNO ) ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM schreg_address_dat ";
        $query .= "where SCHREGNO = '".$data["SCHREGNO"]."' AND ISSUEDATE = '".$data["ISSUEDATE"]."'";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["SCHREGNO"][TEXT]            = $data["SCHREGNO"];
        $data1["ISSUEDATE"][TEXT]           = $data["ISSUEDATE"];
        $data1["EXPIREDATE"][TEXT]          = $data["EXPIREDATE"];
        $data1["ZIPCD"][TEXT]               = $data["ZIPCD"];
        //AREACDがnullの時、00をセット
        if ($data["AREACD"] !== '') {
            $data1["AREACD"][TEXT]              = $data["AREACD"];
        } else {
            $data1["AREACD"][TEXT]              = '00';
        }
        $data1["ADDR1"][TEXT]               = $data["ADDR1"];
        $data1["ADDR2"][TEXT]               = $data["ADDR2"];
        $data1["ADDR_FLG"][TEXT]            = $data["ADDR_FLG"];
        $data1["ADDR1_ENG"][TEXT]           = $data["ADDR1_ENG"];
        $data1["ADDR2_ENG"][TEXT]           = $data["ADDR2_ENG"];
        $data1["TELNO"][TEXT]               = $data["TELNO"];
        $data1["TELNO2"][TEXT]              = $data["TELNO2"];
        $data1["FAXNO"][TEXT]               = $data["FAXNO"];
        $data1["EMAIL"][TEXT]               = $data["EMAIL"];
        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "schreg_address_dat");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["SCHREGNO"][TEXT]            = $data["SCHREGNO"];
        $data1["ISSUEDATE"][TEXT]           = $data["ISSUEDATE"];
        $data1["EXPIREDATE"][TEXT]          = $data["EXPIREDATE"];
        $data1["ZIPCD"][TEXT]               = $data["ZIPCD"];
        //AREACDがnullの時、00をセット
        if ($data["AREACD"] !== '') {
            $data1["AREACD"][TEXT]              = $data["AREACD"];
        } else {
            $data1["AREACD"][TEXT]              = '00';
        }
        $data1["ADDR1"][TEXT]               = $data["ADDR1"];
        $data1["ADDR2"][TEXT]               = $data["ADDR2"];
        $data1["ADDR_FLG"][TEXT]            = $data["ADDR_FLG"];
        $data1["ADDR1_ENG"][TEXT]           = $data["ADDR1_ENG"];
        $data1["ADDR2_ENG"][TEXT]           = $data["ADDR2_ENG"];
        $data1["TELNO"][TEXT]               = $data["TELNO"];
        $data1["TELNO2"][TEXT]              = $data["TELNO2"];
        $data1["FAXNO"][TEXT]               = $data["FAXNO"];
        $data1["EMAIL"][TEXT]               = $data["EMAIL"];
        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = " WHERE SCHREGNO = '" .$data["SCHREGNO"] ."' ";
        $where .= "and ISSUEDATE = '".$data["ISSUEDATE"]."'";
        $query = Query::updateSQL($data1, "schreg_address_dat", $where);

        $result = $db->query($query);
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
}
