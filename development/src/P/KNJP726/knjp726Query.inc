<?php

require_once('for_php7.php');
class knjp726Query extends Query {

    //年組取得
    function getGrade() {
        $query .= " SELECT DISTINCT ";
        $query .= "     REGD_H.GRADE AS VALUE, ";
        $query .= "     REGD_G.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REGD_G ON REGD_H.YEAR = REGD_G.YEAR ";
        $query .= "           AND REGD_H.GRADE = REGD_G.GRADE ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getGradeHr($model) {
        $query .= " SELECT ";
        $query .= "     GRADE || ':' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE"] != "99") {
            $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    function updateQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //DEL
            $query  = " DELETE FROM REGISTBANK_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHREGNO = '".$data_arr[$i]["SCHREGNO1"]."' ";
            $db->query($query);
            for ($seq = 1; $seq <= $model->maxSeq; $seq++) {
                if ($data_arr[$i]["UPD_FLG{$seq}"]) {
                    //INS
                    $data = array();
                    $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                    $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO{$seq}"];
                    $data["SEQ"][TEXT]          = $seq;
                    $data["BANKCD"][TEXT]       = $data_arr[$i]["BANKCD{$seq}"];
                    $data["BRANCHCD"][TEXT]     = $data_arr[$i]["BRANCHCD{$seq}"];
                    $data["DEPOSIT_ITEM"][TEXT] = $data_arr[$i]["DEPOSIT_ITEM{$seq}"];
                    $data["ACCOUNTNO"][TEXT]    = $data_arr[$i]["ACCOUNTNO{$seq}"];
                    $data["ACCOUNTNAME"][TEXT]  = $data_arr[$i]["ACCOUNTNAME{$seq}"];
                    $data["RELATIONSHIP"][TEXT] = $data_arr[$i]["RELATIONSHIP{$seq}"];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";
                    $query = Query::insertSQL($data, "REGISTBANK_DAT");
                    $db->query($query);
                }
            }

            //口座名義を小文字等を変換する
            $query  = " UPDATE REGISTBANK_DAT ";
            $query .= " SET ";
            $query .= "     ACCOUNTNAME = TRIM(REPLACE(REPLACE(REPLACE(REPLACE(translate(REPLACE(ACCOUNTNAME, '　', ' '),'ｱｲｳｴｵﾔﾕﾖﾂ','ｧｨｩｪｫｬｭｮｯ'), 'ー', '-'), '－', '-'), '―', '-'), '‐', '-')) ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO1"]}' ";
            $db->query($query);
 
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //銀行マスタ
    function bankCntSql($bankCd, $branchCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     BANK_MST ";
        $query .= " WHERE ";
        $query .= "     BANKCD = '{$bankCd}' ";
        $query .= "     AND BRANCHCD = '{$branchCd}' ";

        return $query;
    }

    //名称マスタ登録コードチェック
    function nameCntSql($namecd, $setcd, $where="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //模試データ出力用
    function getRegistBankData($model)
    {
        $query  = " SELECT ";
        $query .= "     REGD_H.HR_NAME || '-' || REGD.ATTENDNO AS VALUE, ";
        $query .= "     BASE.NAME, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGIST_B1.BANKCD AS BANKCD1, ";
        $query .= "     BANK1.BANKNAME AS BANKNAME1, ";
        $query .= "     REGIST_B1.BRANCHCD AS BRANCHCD1, ";
        $query .= "     BANK1.BRANCHNAME AS BRANCHNAME1, ";
        $query .= "     REGIST_B1.DEPOSIT_ITEM AS DEPOSIT_ITEM1, ";
        $query .= "     REGIST_B1.ACCOUNTNO AS ACCOUNTNO1, ";
        $query .= "     CASE WHEN REGIST_B1.BANKCD IS NOT NULL ";
        $query .= "          THEN TRANSLATE_H_HK(REGIST_B1.ACCOUNTNAME) ";
        $query .= "          ELSE TRANSLATE_H_HK(GUARDIAN.GUARD_KANA) ";
        $query .= "     END AS ACCOUNTNAME1, ";
        $query .= "     CASE WHEN REGIST_B1.BANKCD IS NOT NULL ";
        $query .= "          THEN REGIST_B1.RELATIONSHIP ";
        $query .= "          ELSE GUARDIAN.RELATIONSHIP ";
        $query .= "     END AS RELATIONSHIP1, ";
        $query .= "     REGIST_B2.BANKCD AS BANKCD2, ";
        $query .= "     BANK2.BANKNAME AS BANKNAME2, ";
        $query .= "     REGIST_B2.BRANCHCD AS BRANCHCD2, ";
        $query .= "     BANK2.BRANCHNAME AS BRANCHNAME2, ";
        $query .= "     REGIST_B2.DEPOSIT_ITEM AS DEPOSIT_ITEM2, ";
        $query .= "     REGIST_B2.ACCOUNTNO AS ACCOUNTNO2, ";
        $query .= "     REGIST_B2.ACCOUNTNAME AS ACCOUNTNAME2, ";
        $query .= "     REGIST_B2.RELATIONSHIP AS RELATIONSHIP2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT GUARDIAN ON REGD.SCHREGNO = GUARDIAN.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD.YEAR = REGD_H.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGD_H.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGD_H.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGD_H.HR_CLASS ";
        $query .= "     LEFT JOIN REGISTBANK_DAT REGIST_B1 ON REGIST_B1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "          AND REGD.SCHREGNO = REGIST_B1.SCHREGNO ";
        $query .= "          AND REGIST_B1.SEQ = '1' ";
        $query .= "     LEFT JOIN BANK_MST BANK1 ON REGIST_B1.BANKCD = BANK1.BANKCD ";
        $query .= "          AND REGIST_B1.BRANCHCD = BANK1.BRANCHCD ";
        $query .= "     LEFT JOIN REGISTBANK_DAT REGIST_B2 ON REGIST_B2.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "          AND REGD.SCHREGNO = REGIST_B2.SCHREGNO ";
        $query .= "          AND REGIST_B2.SEQ = '2' ";
        $query .= "     LEFT JOIN BANK_MST BANK2 ON REGIST_B2.BANKCD = BANK2.BANKCD ";
        $query .= "          AND REGIST_B2.BRANCHCD = BANK2.BRANCHCD ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["GRADE"] != "99") {
            $query .= "     AND REGD.GRADE = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["GRADE_HR"] != "99") {
            $query .= "     AND REGD.GRADE || ':' || REGD.HR_CLASS = '{$model->field["GRADE_HR"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }
}
?>
