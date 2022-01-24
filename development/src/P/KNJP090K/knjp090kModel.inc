<?php

require_once('for_php7.php');


class knjp090kModel extends Model {
    var $cmd;
    var $file;
    var $data_arr       = array();
    var $err_data_arr   = array();
    var $controls       = array();
    var $montharray     = array();    //NO008
    var $paid_month;
    var $schooldiv;

    function knjp090kModel()
    {
        $this->new_year = CTRL_YEAR + 1;
        common::GetControlMaster_Fnc($this->controls);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->paid_month);
        }

        $db = Query::dbCheckOut();
        $this->schooldiv = 0;
        $query = knjp090kQuery::getschool();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->schooldiv = 1;
        }
        $result->free();
        Query::dbCheckIn($db);

        $this->cmd          = VARS::request("cmd");
        $this->file         = VARS::file("FILE");
        $this->paid_month   = VARS::post("PAID_MONTH");
    }

    //CSVファイル取込＆登録処理
    function InsertCSVFile(&$db)
    {
        $query = knjp090kQuery::getReplaceData();
        $result = $db->query($query);
        $repDate = array();
        while ($dateRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dateRow["PAID_MONEY_DATE"] = str_replace("-", "", $dateRow["PAID_MONEY_DATE"]);
            $dateRow["REPLACE_MONEY_DATE"] = str_replace("-", "", $dateRow["REPLACE_MONEY_DATE"]);
            $repDate[$dateRow["PAID_MONEY_DATE"]] = $dateRow["REPLACE_MONEY_DATE"];
        }
        $result->free();

        $db->expectError('*');
        $temp_arr = array();
        if (is_uploaded_file($this->file["tmp_name"])){
            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $j  = 1;
            while (!feof ($fp))
            {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '')
                {
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $ii     = 0;

                    $data = array("FILE_LINE_NUMBER"                => $j,
                                  "DATA_DATE"                       => trim($tmp[$ii]),
                                  "SCHOOL_CODE"                     => trim($tmp[++$ii]),
                                  "COURSE_CODE"                     => trim($tmp[++$ii]),
                                  "SCHREGNO"                        => trim($tmp[++$ii]),
                                  "BIRTHDAY"                        => (trim($tmp[++$ii]) == "00000000") ? "" : trim($tmp[$ii]),
                                  "NAME_KANA"                       => trim($tmp[++$ii]),
                                  "NAME"                            => trim($tmp[++$ii]),
                                  "PAYER_NAME_KANA"                 => trim($tmp[++$ii]),
                                  "PAYER_NAME"                      => trim($tmp[++$ii]),
                                  "BIKO"                            => trim($tmp[++$ii]),
                                  "PAYER_TELNO"                     => trim($tmp[++$ii]),
                                  "BANKCD"                          => trim($tmp[++$ii]),
                                  "BRANCHCD"                        => trim($tmp[++$ii]),
                                  "BANKNAME_KANA"                   => trim($tmp[++$ii]),
                                  "BRANCHNAME_KANA"                 => trim($tmp[++$ii]),
                                  "DEPOSIT_ITEM"                    => trim($tmp[++$ii]),
                                  "ACCOUNTNO"                       => trim($tmp[++$ii]),
                                  "ACCOUNTNAME"                     => trim($tmp[++$ii]),
                                  "EXPENSE01_PAID_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE01_NECE_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE02_PAID_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE02_NECE_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE03_PAID_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE03_NECE_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE04_PAID_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE04_NECE_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE05_PAID_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE05_NECE_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE06_PAID_MONEY"            => trim($tmp[++$ii]),
                                  "EXPENSE06_NECE_MONEY"            => trim($tmp[++$ii]),
                                  "TERM01_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM01_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM01_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM01_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM01_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM01_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM01_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM01_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM01_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM01_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM01_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM01_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM01_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM01_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM02_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM02_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM02_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM02_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM02_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM02_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM02_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM02_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM02_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM02_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM02_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM02_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM02_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM02_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM03_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM03_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM03_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM03_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM03_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM03_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM03_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM03_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM03_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM03_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM03_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM03_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM03_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM03_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM04_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM04_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM04_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM04_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM04_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM04_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM04_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM04_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM04_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM04_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM04_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM04_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM04_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM04_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM05_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM05_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM05_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM05_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM05_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM05_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM05_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM05_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM05_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM05_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM05_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM05_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM05_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM05_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM06_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM06_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM06_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM06_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM06_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM06_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM06_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM06_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM06_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM06_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM06_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM06_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM06_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM06_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM07_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM07_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM07_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM07_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM07_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM07_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM07_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM07_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM07_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM07_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM07_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM07_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM07_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM07_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM08_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM08_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM08_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM08_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM08_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM08_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM08_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM08_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM08_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM08_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM08_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM08_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM08_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM08_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM09_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM09_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM09_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM09_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM09_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM09_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM09_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM09_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM09_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM09_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM09_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM09_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM09_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM09_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM10_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM10_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM10_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM10_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM10_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM10_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM10_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM10_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM10_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM10_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM10_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM10_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM10_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM10_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM11_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM11_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM11_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM11_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM11_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM11_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM11_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM11_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM11_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM11_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM11_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM11_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM11_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM11_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "TERM12_EXPENSE01_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM12_EXPENSE02_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM12_EXPENSE03_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM12_EXPENSE04_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM12_EXPENSE05_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM12_EXPENSE06_MONEY_DUE"      => trim($tmp[++$ii]),
                                  "TERM12_SUM_MONEY_DUE"            => trim($tmp[++$ii]),
                                  "TERM12_FLAG01"                   => trim($tmp[++$ii]),
                                  "TERM12_FLAG02"                   => trim($tmp[++$ii]),
                                  "TERM12_FLAG03"                   => trim($tmp[++$ii]),
                                  "TERM12_FLAG04"                   => trim($tmp[++$ii]),
                                  "TERM12_FLAG05"                   => trim($tmp[++$ii]),
                                  "TERM12_PAID_MONEY_DATE"          => (trim($tmp[++$ii]) == "0000000000") ? "" : $repDate[substr(trim($tmp[$ii]),2)],
                                  "TERM12_FURI_MONEY_DATE"          => (trim($tmp[$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE01_TRANS_SDATE"      => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE01_MONEY"            => trim($tmp[++$ii]),
                                  "EXTR_EXPENSE01_FLAG02"           => $tmp[++$ii],
                                  "EXTR_EXPENSE01_FLAG03"           => $tmp[++$ii],
                                  "EXTR_EXPENSE01_FLAG04"           => $tmp[++$ii],
                                  "EXTR_EXPENSE01_FLAG05"           => $tmp[++$ii],
                                  "EXTR_EXPENSE01_PAID_MONEY_DATE"  => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE02_TRANS_SDATE"      => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE02_MONEY"            => trim($tmp[++$ii]),
                                  "EXTR_EXPENSE02_FLAG02"           => $tmp[++$ii],
                                  "EXTR_EXPENSE02_FLAG03"           => $tmp[++$ii],
                                  "EXTR_EXPENSE02_FLAG04"           => $tmp[++$ii],
                                  "EXTR_EXPENSE02_FLAG05"           => $tmp[++$ii],
                                  "EXTR_EXPENSE02_PAID_MONEY_DATE"  => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE03_TRANS_SDATE"      => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE03_MONEY"            => trim($tmp[++$ii]),
                                  "EXTR_EXPENSE03_FLAG02"           => $tmp[++$ii],
                                  "EXTR_EXPENSE03_FLAG03"           => $tmp[++$ii],
                                  "EXTR_EXPENSE03_FLAG04"           => $tmp[++$ii],
                                  "EXTR_EXPENSE03_FLAG05"           => $tmp[++$ii],
                                  "EXTR_EXPENSE03_PAID_MONEY_DATE"  => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE04_TRANS_SDATE"      => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE04_MONEY"            => trim($tmp[++$ii]),
                                  "EXTR_EXPENSE04_FLAG02"           => $tmp[++$ii],
                                  "EXTR_EXPENSE04_FLAG03"           => $tmp[++$ii],
                                  "EXTR_EXPENSE04_FLAG04"           => $tmp[++$ii],
                                  "EXTR_EXPENSE04_FLAG05"           => $tmp[++$ii],
                                  "EXTR_EXPENSE04_PAID_MONEY_DATE"  => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE05_TRANS_SDATE"      => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE05_MONEY"            => trim($tmp[++$ii]),
                                  "EXTR_EXPENSE05_FLAG02"           => $tmp[++$ii],
                                  "EXTR_EXPENSE05_FLAG03"           => $tmp[++$ii],
                                  "EXTR_EXPENSE05_FLAG04"           => $tmp[++$ii],
                                  "EXTR_EXPENSE05_FLAG05"           => $tmp[++$ii],
                                  "EXTR_EXPENSE05_PAID_MONEY_DATE"  => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE06_TRANS_SDATE"      => (trim($tmp[++$ii]) == "0000000000") ? "" : substr(trim($tmp[$ii]),2),
                                  "EXTR_EXPENSE06_MONEY"            => trim($tmp[++$ii]),
                                  "EXTR_EXPENSE06_FLAG02"           => $tmp[++$ii],
                                  "EXTR_EXPENSE06_FLAG03"           => $tmp[++$ii],
                                  "EXTR_EXPENSE06_FLAG04"           => $tmp[++$ii],
                                  "EXTR_EXPENSE06_FLAG05"           => $tmp[++$ii],
                                  "EXTR_EXPENSE06_PAID_MONEY_DATE"  => (trim($tmp[++$ii]) == "0000") ? trim($tmp[++$ii]) : ""
                                   );
                                  
                    $db->autoCommit(false);
                    //新入生チェック NO009
                    $freshFlg = false;
                    $query = knjp090kQuery::checkFreshman($data["SCHREGNO"]);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                        if ($row["FRESHCNT"] > 0 && $this->schooldiv == 0) {
                            $freshFlg = true;
                        }
                    }
                    $result->free();

                    //重複チェック NO010
                    $query = knjp090kQuery::checkTmp1($data["SCHREGNO"]);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                        if ($row["TMPCNT"] > 0){
                            $this->setWarning("MSG917", "重複データが存在します。\\n行番号".$j."\\n学籍番号".$data["SCHREGNO"]);
                            return false;
                        }
                    }
                    $result->free();

                    //日付チェック
                    for ($furiCnt = 1; $furiCnt <= 12; $furiCnt++) {
                        $setFuri = sprintf("%02d", $furiCnt);
                        if ($data["TERM".$setFuri."_FURI_MONEY_DATE"] &&
                            !$data["TERM".$setFuri."_PAID_MONEY_DATE"]
                        ) {
                            $this->setWarning("MSG917", "振替日変更処理が必要です。\\n行番号".$j."\\n学籍番号".$data["SCHREGNO"]."\\n日付".$data["TERM".$setFuri."_FURI_MONEY_DATE"]);
                            return false;
                        }
                    }

                    $query = knjp090kQuery::InsertQuery($data,$freshFlg);

                    $result = $db->query($query);
                    //エラー処理
                    if (PEAR::isError($result)){
                        $this->InsertCSVErr($db,$data,$j);
                    }
                    $db->commit();
                    $i++;
                    $j++;
                }
            }
            fclose($fp);

        } else {
            $this->setWarning("MSG203", "ファイルが不正です。");
        }
        $db->popExpect();
        return true;
    }
    
    function InsertCSVErr(&$db,&$data_arr,$rownum){
        //チェック用データの作成
        $check_arr = array( "DATA_DATE"                      => array("date", "学データ日付"),
                           "SCHOOL_CODE"                     => array("char", 5, "学校(コード/区分)"),
                           "COURSE_CODE"                     => array("char", 8, "コースコード(学部/学科/学年/クラス)"),
                           "SCHREGNO"                        => array("char", 8, "学生番号"),
                           "BIRTHDAY"                        => array("date", "生年月日"),
                           "NAME_KANA"                       => array("char", 48, "学生氏名(カナ)"),
                           "NAME"                            => array("char", 36, "学生氏名(漢字)"),
                           "PAYER_NAME_KANA"                 => array("char", 48, "納付者氏名(カナ)"),
                           "PAYER_NAME"                      => array("char", 36, "納付者氏名(漢字)"),
                           "PAYER_TELNO"                     => array("char", 15, "納付者電話番号"),
                           "BANKCD"                          => array("char", 4, "引落口座情報(金融機関コード＿銀行コード)"),
                           "BRANCHCD"                        => array("char", 3, "引落口座情報(金融機関コード＿支店コード)"),
                           "BANKNAME_KANA"                   => array("char", 45, "引落口座情報(銀行名)"),
                           "BRANCHNAME_KANA"                 => array("char", 45, "引落口座情報(支店名)"),
                           "DEPOSIT_ITEM"                    => array("char", 1, "引落口座情報(預金種目)"),
                           "ACCOUNTNO"                       => array("char", 7, "引落口座情報(口座番号)"),
                           "ACCOUNTNAME"                     => array("char", 48, "引落口座情報(口座名義)"),
                           "EXPENSE01_PAID_MONEY"            => array("integer", "費目別内訳(費目01＿充当納入済額)"),
                           "EXPENSE01_NECE_MONEY"            => array("integer", "費目別内訳(費目01＿未充当納入必要額)"),
                           "EXPENSE02_PAID_MONEY"            => array("integer", "費目別内訳(費目02＿充当納入済額)"),
                           "EXPENSE02_NECE_MONEY"            => array("integer", "費目別内訳(費目02＿未充当納入必要額)"),
                           "EXPENSE03_PAID_MONEY"            => array("integer", "費目別内訳(費目03＿充当納入済額)"),
                           "EXPENSE03_NECE_MONEY"            => array("integer", "費目別内訳(費目03＿未充当納入必要額)"),
                           "EXPENSE04_PAID_MONEY"            => array("integer", "費目別内訳(費目04＿充当納入済額)"),
                           "EXPENSE04_NECE_MONEY"            => array("integer", "費目別内訳(費目04＿未充当納入必要額)"),
                           "EXPENSE05_PAID_MONEY"            => array("integer", "費目別内訳(費目05＿充当納入済額)"),
                           "EXPENSE05_NECE_MONEY"            => array("integer", "費目別内訳(費目05＿未充当納入必要額)"),
                           "EXPENSE06_PAID_MONEY"            => array("integer", "費目別内訳(費目06＿充当納入済額)"),
                           "EXPENSE06_NECE_MONEY"            => array("integer", "費目別内訳(費目06＿未充当納入必要額)"),
                           "TERM01_EXPENSE01_MONEY_DUE"      => array("integer", "01期分＿納入必要額内訳(費目01)"),
                           "TERM01_EXPENSE02_MONEY_DUE"      => array("integer", "01期分＿納入必要額内訳(費目02)"),
                           "TERM01_EXPENSE03_MONEY_DUE"      => array("integer", "01期分＿納入必要額内訳(費目03)"),
                           "TERM01_EXPENSE04_MONEY_DUE"      => array("integer", "01期分＿納入必要額内訳(費目04)"),
                           "TERM01_EXPENSE05_MONEY_DUE"      => array("integer", "01期分＿納入必要額内訳(費目05)"),
                           "TERM01_EXPENSE06_MONEY_DUE"      => array("integer", "01期分＿納入必要額内訳(費目06)"),
                           "TERM01_SUM_MONEY_DUE"            => array("integer", "01期分＿納入必要額内訳(納入必要金額)"),
                           "TERM01_FLAG01"                   => array("char", 1, "01期分＿納入必要額内訳(前受区分)"),
                           "TERM01_FLAG02"                   => array("char", 1, "01期分＿納入必要額内訳(請求状況区分)"),
                           "TERM01_FLAG03"                   => array("char", 1, "01期分＿納入必要額内訳(振替結果コード)"),
                           "TERM01_FLAG04"                   => array("char", 1, "01期分＿納入必要額内訳(入金状況区分)"),
                           "TERM01_FLAG05"                   => array("char", 1, "01期分＿納入必要額内訳(取消区分)"),
                           "TERM01_PAID_MONEY_DATE"          => array("date", "01期分＿納入必要額内訳(入金日)"),
                           "TERM01_FURI_MONEY_DATE"          => array("date", "01期分＿納入必要額内訳(入金日)"),
                           "TERM02_EXPENSE01_MONEY_DUE"      => array("integer", "02期分＿納入必要額内訳(費目01)"),
                           "TERM02_EXPENSE02_MONEY_DUE"      => array("integer", "02期分＿納入必要額内訳(費目02)"),
                           "TERM02_EXPENSE03_MONEY_DUE"      => array("integer", "02期分＿納入必要額内訳(費目03)"),
                           "TERM02_EXPENSE04_MONEY_DUE"      => array("integer", "02期分＿納入必要額内訳(費目04)"),
                           "TERM02_EXPENSE05_MONEY_DUE"      => array("integer", "02期分＿納入必要額内訳(費目05)"),
                           "TERM02_EXPENSE06_MONEY_DUE"      => array("integer", "02期分＿納入必要額内訳(費目06)"),
                           "TERM02_SUM_MONEY_DUE"            => array("integer", "02期分＿納入必要額内訳(納入必要金額)"),
                           "TERM02_FLAG01"                   => array("char", 1, "02期分＿納入必要額内訳(前受区分)"),
                           "TERM02_FLAG02"                   => array("char", 1, "02期分＿納入必要額内訳(請求状況区分)"),
                           "TERM02_FLAG03"                   => array("char", 1, "02期分＿納入必要額内訳(振替結果コード)"),
                           "TERM02_FLAG04"                   => array("char", 1, "02期分＿納入必要額内訳(入金状況区分)"),
                           "TERM02_FLAG05"                   => array("char", 1, "02期分＿納入必要額内訳(取消区分)"),
                           "TERM02_PAID_MONEY_DATE"          => array("date", "02期分＿納入必要額内訳(入金日)"),
                           "TERM02_FURI_MONEY_DATE"          => array("date", "02期分＿納入必要額内訳(入金日)"),
                           "TERM03_EXPENSE01_MONEY_DUE"      => array("integer", "03期分＿納入必要額内訳(費目01)"),
                           "TERM03_EXPENSE02_MONEY_DUE"      => array("integer", "03期分＿納入必要額内訳(費目02)"),
                           "TERM03_EXPENSE03_MONEY_DUE"      => array("integer", "03期分＿納入必要額内訳(費目03)"),
                           "TERM03_EXPENSE04_MONEY_DUE"      => array("integer", "03期分＿納入必要額内訳(費目04)"),
                           "TERM03_EXPENSE05_MONEY_DUE"      => array("integer", "03期分＿納入必要額内訳(費目05)"),
                           "TERM03_EXPENSE06_MONEY_DUE"      => array("integer", "03期分＿納入必要額内訳(費目06)"),
                           "TERM03_SUM_MONEY_DUE"            => array("integer", "03期分＿納入必要額内訳(納入必要金額)"),
                           "TERM03_FLAG01"                   => array("char", 1, "03期分＿納入必要額内訳(前受区分)"),
                           "TERM03_FLAG02"                   => array("char", 1, "03期分＿納入必要額内訳(請求状況区分)"),
                           "TERM03_FLAG03"                   => array("char", 1, "03期分＿納入必要額内訳(振替結果コード)"),
                           "TERM03_FLAG04"                   => array("char", 1, "03期分＿納入必要額内訳(入金状況区分)"),
                           "TERM03_FLAG05"                   => array("char", 1, "03期分＿納入必要額内訳(取消区分)"),
                           "TERM03_PAID_MONEY_DATE"          => array("date", "03期分＿納入必要額内訳(入金日)"),
                           "TERM03_FURI_MONEY_DATE"          => array("date", "03期分＿納入必要額内訳(入金日)"),
                           "TERM04_EXPENSE01_MONEY_DUE"      => array("integer", "04期分＿納入必要額内訳(費目01)"),
                           "TERM04_EXPENSE02_MONEY_DUE"      => array("integer", "04期分＿納入必要額内訳(費目02)"),
                           "TERM04_EXPENSE03_MONEY_DUE"      => array("integer", "04期分＿納入必要額内訳(費目03)"),
                           "TERM04_EXPENSE04_MONEY_DUE"      => array("integer", "04期分＿納入必要額内訳(費目04)"),
                           "TERM04_EXPENSE05_MONEY_DUE"      => array("integer", "04期分＿納入必要額内訳(費目05)"),
                           "TERM04_EXPENSE06_MONEY_DUE"      => array("integer", "04期分＿納入必要額内訳(費目06)"),
                           "TERM04_SUM_MONEY_DUE"            => array("integer", "04期分＿納入必要額内訳(納入必要金額)"),
                           "TERM04_FLAG01"                   => array("char", 1, "04期分＿納入必要額内訳(前受区分)"),
                           "TERM04_FLAG02"                   => array("char", 1, "04期分＿納入必要額内訳(請求状況区分)"),
                           "TERM04_FLAG03"                   => array("char", 1, "04期分＿納入必要額内訳(振替結果コード)"),
                           "TERM04_FLAG04"                   => array("char", 1, "04期分＿納入必要額内訳(入金状況区分)"),
                           "TERM04_FLAG05"                   => array("char", 1, "04期分＿納入必要額内訳(取消区分)"),
                           "TERM04_PAID_MONEY_DATE"          => array("date", "04期分＿納入必要額内訳(入金日)"),
                           "TERM04_FURI_MONEY_DATE"          => array("date", "04期分＿納入必要額内訳(入金日)"),
                           "TERM05_EXPENSE01_MONEY_DUE"      => array("integer", "05期分＿納入必要額内訳(費目01)"),
                           "TERM05_EXPENSE02_MONEY_DUE"      => array("integer", "05期分＿納入必要額内訳(費目02)"),
                           "TERM05_EXPENSE03_MONEY_DUE"      => array("integer", "05期分＿納入必要額内訳(費目03)"),
                           "TERM05_EXPENSE04_MONEY_DUE"      => array("integer", "05期分＿納入必要額内訳(費目04)"),
                           "TERM05_EXPENSE05_MONEY_DUE"      => array("integer", "05期分＿納入必要額内訳(費目05)"),
                           "TERM05_EXPENSE06_MONEY_DUE"      => array("integer", "05期分＿納入必要額内訳(費目06)"),
                           "TERM05_SUM_MONEY_DUE"            => array("integer", "05期分＿納入必要額内訳(納入必要金額)"),
                           "TERM05_FLAG01"                   => array("char", 1, "05期分＿納入必要額内訳(前受区分)"),
                           "TERM05_FLAG02"                   => array("char", 1, "05期分＿納入必要額内訳(請求状況区分)"),
                           "TERM05_FLAG03"                   => array("char", 1, "05期分＿納入必要額内訳(振替結果コード)"),
                           "TERM05_FLAG04"                   => array("char", 1, "05期分＿納入必要額内訳(入金状況区分)"),
                           "TERM05_FLAG05"                   => array("char", 1, "05期分＿納入必要額内訳(取消区分)"),
                           "TERM05_PAID_MONEY_DATE"          => array("date", "05期分＿納入必要額内訳(入金日)"),
                           "TERM05_FURI_MONEY_DATE"          => array("date", "05期分＿納入必要額内訳(入金日)"),
                           "TERM06_EXPENSE01_MONEY_DUE"      => array("integer", "06期分＿納入必要額内訳(費目01)"),
                           "TERM06_EXPENSE02_MONEY_DUE"      => array("integer", "06期分＿納入必要額内訳(費目02)"),
                           "TERM06_EXPENSE03_MONEY_DUE"      => array("integer", "06期分＿納入必要額内訳(費目03)"),
                           "TERM06_EXPENSE04_MONEY_DUE"      => array("integer", "06期分＿納入必要額内訳(費目04)"),
                           "TERM06_EXPENSE05_MONEY_DUE"      => array("integer", "06期分＿納入必要額内訳(費目05)"),
                           "TERM06_EXPENSE06_MONEY_DUE"      => array("integer", "06期分＿納入必要額内訳(費目06)"),
                           "TERM06_SUM_MONEY_DUE"            => array("integer", "06期分＿納入必要額内訳(納入必要金額)"),
                           "TERM06_FLAG01"                   => array("char", 1, "06期分＿納入必要額内訳(前受区分)"),
                           "TERM06_FLAG02"                   => array("char", 1, "06期分＿納入必要額内訳(請求状況区分)"),
                           "TERM06_FLAG03"                   => array("char", 1, "06期分＿納入必要額内訳(振替結果コード)"),
                           "TERM06_FLAG04"                   => array("char", 1, "06期分＿納入必要額内訳(入金状況区分)"),
                           "TERM06_FLAG05"                   => array("char", 1, "06期分＿納入必要額内訳(取消区分)"),
                           "TERM06_PAID_MONEY_DATE"          => array("date", "06期分＿納入必要額内訳(入金日)"),
                           "TERM06_FURI_MONEY_DATE"          => array("date", "06期分＿納入必要額内訳(入金日)"),
                           "TERM07_EXPENSE01_MONEY_DUE"      => array("integer", "07期分＿納入必要額内訳(費目01)"),
                           "TERM07_EXPENSE02_MONEY_DUE"      => array("integer", "07期分＿納入必要額内訳(費目02)"),
                           "TERM07_EXPENSE03_MONEY_DUE"      => array("integer", "07期分＿納入必要額内訳(費目03)"),
                           "TERM07_EXPENSE04_MONEY_DUE"      => array("integer", "07期分＿納入必要額内訳(費目04)"),
                           "TERM07_EXPENSE05_MONEY_DUE"      => array("integer", "07期分＿納入必要額内訳(費目05)"),
                           "TERM07_EXPENSE06_MONEY_DUE"      => array("integer", "07期分＿納入必要額内訳(費目06)"),
                           "TERM07_SUM_MONEY_DUE"            => array("integer", "07期分＿納入必要額内訳(納入必要金額)"),
                           "TERM07_FLAG01"                   => array("char", 1, "07期分＿納入必要額内訳(前受区分)"),
                           "TERM07_FLAG02"                   => array("char", 1, "07期分＿納入必要額内訳(請求状況区分)"),
                           "TERM07_FLAG03"                   => array("char", 1, "07期分＿納入必要額内訳(振替結果コード)"),
                           "TERM07_FLAG04"                   => array("char", 1, "07期分＿納入必要額内訳(入金状況区分)"),
                           "TERM07_FLAG05"                   => array("char", 1, "07期分＿納入必要額内訳(取消区分)"),
                           "TERM07_PAID_MONEY_DATE"          => array("date", "07期分＿納入必要額内訳(入金日)"),
                           "TERM07_FURI_MONEY_DATE"          => array("date", "07期分＿納入必要額内訳(入金日)"),
                           "TERM08_EXPENSE01_MONEY_DUE"      => array("integer", "08期分＿納入必要額内訳(費目01)"),
                           "TERM08_EXPENSE02_MONEY_DUE"      => array("integer", "08期分＿納入必要額内訳(費目02)"),
                           "TERM08_EXPENSE03_MONEY_DUE"      => array("integer", "08期分＿納入必要額内訳(費目03)"),
                           "TERM08_EXPENSE04_MONEY_DUE"      => array("integer", "08期分＿納入必要額内訳(費目04)"),
                           "TERM08_EXPENSE05_MONEY_DUE"      => array("integer", "08期分＿納入必要額内訳(費目05)"),
                           "TERM08_EXPENSE06_MONEY_DUE"      => array("integer", "08期分＿納入必要額内訳(費目06)"),
                           "TERM08_SUM_MONEY_DUE"            => array("integer", "08期分＿納入必要額内訳(納入必要金額)"),
                           "TERM08_FLAG01"                   => array("char", 1, "08期分＿納入必要額内訳(前受区分)"),
                           "TERM08_FLAG02"                   => array("char", 1, "08期分＿納入必要額内訳(請求状況区分)"),
                           "TERM08_FLAG03"                   => array("char", 1, "08期分＿納入必要額内訳(振替結果コード)"),
                           "TERM08_FLAG04"                   => array("char", 1, "08期分＿納入必要額内訳(入金状況区分)"),
                           "TERM08_FLAG05"                   => array("char", 1, "08期分＿納入必要額内訳(取消区分)"),
                           "TERM08_PAID_MONEY_DATE"          => array("date", "08期分＿納入必要額内訳(入金日)"),
                           "TERM08_FURI_MONEY_DATE"          => array("date", "08期分＿納入必要額内訳(入金日)"),
                           "TERM09_EXPENSE01_MONEY_DUE"      => array("integer", "09期分＿納入必要額内訳(費目01)"),
                           "TERM09_EXPENSE02_MONEY_DUE"      => array("integer", "09期分＿納入必要額内訳(費目02)"),
                           "TERM09_EXPENSE03_MONEY_DUE"      => array("integer", "09期分＿納入必要額内訳(費目03)"),
                           "TERM09_EXPENSE04_MONEY_DUE"      => array("integer", "09期分＿納入必要額内訳(費目04)"),
                           "TERM09_EXPENSE05_MONEY_DUE"      => array("integer", "09期分＿納入必要額内訳(費目05)"),
                           "TERM09_EXPENSE06_MONEY_DUE"      => array("integer", "09期分＿納入必要額内訳(費目06)"),
                           "TERM09_SUM_MONEY_DUE"            => array("integer", "09期分＿納入必要額内訳(納入必要金額)"),
                           "TERM09_FLAG01"                   => array("char", 1, "09期分＿納入必要額内訳(前受区分)"),
                           "TERM09_FLAG02"                   => array("char", 1, "09期分＿納入必要額内訳(請求状況区分)"),
                           "TERM09_FLAG03"                   => array("char", 1, "09期分＿納入必要額内訳(振替結果コード)"),
                           "TERM09_FLAG04"                   => array("char", 1, "09期分＿納入必要額内訳(入金状況区分)"),
                           "TERM09_FLAG05"                   => array("char", 1, "09期分＿納入必要額内訳(取消区分)"),
                           "TERM09_PAID_MONEY_DATE"          => array("date", "09期分＿納入必要額内訳(入金日)"),
                           "TERM09_FURI_MONEY_DATE"          => array("date", "09期分＿納入必要額内訳(入金日)"),
                           "TERM10_EXPENSE01_MONEY_DUE"      => array("integer", "10期分＿納入必要額内訳(費目01)"),
                           "TERM10_EXPENSE02_MONEY_DUE"      => array("integer", "10期分＿納入必要額内訳(費目02)"),
                           "TERM10_EXPENSE03_MONEY_DUE"      => array("integer", "10期分＿納入必要額内訳(費目03)"),
                           "TERM10_EXPENSE04_MONEY_DUE"      => array("integer", "10期分＿納入必要額内訳(費目04)"),
                           "TERM10_EXPENSE05_MONEY_DUE"      => array("integer", "10期分＿納入必要額内訳(費目05)"),
                           "TERM10_EXPENSE06_MONEY_DUE"      => array("integer", "10期分＿納入必要額内訳(費目06)"),
                           "TERM10_SUM_MONEY_DUE"            => array("integer", "10期分＿納入必要額内訳(納入必要金額)"),
                           "TERM10_FLAG01"                   => array("char", 1, "10期分＿納入必要額内訳(前受区分)"),
                           "TERM10_FLAG02"                   => array("char", 1, "10期分＿納入必要額内訳(請求状況区分)"),
                           "TERM10_FLAG03"                   => array("char", 1, "10期分＿納入必要額内訳(振替結果コード)"),
                           "TERM10_FLAG04"                   => array("char", 1, "10期分＿納入必要額内訳(入金状況区分)"),
                           "TERM10_FLAG05"                   => array("char", 1, "10期分＿納入必要額内訳(取消区分)"),
                           "TERM10_PAID_MONEY_DATE"          => array("date", "10期分＿納入必要額内訳(入金日)"),
                           "TERM10_FURI_MONEY_DATE"          => array("date", "10期分＿納入必要額内訳(入金日)"),
                           "TERM11_EXPENSE01_MONEY_DUE"      => array("integer", "11期分＿納入必要額内訳(費目01)"),
                           "TERM11_EXPENSE02_MONEY_DUE"      => array("integer", "11期分＿納入必要額内訳(費目02)"),
                           "TERM11_EXPENSE03_MONEY_DUE"      => array("integer", "11期分＿納入必要額内訳(費目03)"),
                           "TERM11_EXPENSE04_MONEY_DUE"      => array("integer", "11期分＿納入必要額内訳(費目04)"),
                           "TERM11_EXPENSE05_MONEY_DUE"      => array("integer", "11期分＿納入必要額内訳(費目05)"),
                           "TERM11_EXPENSE06_MONEY_DUE"      => array("integer", "11期分＿納入必要額内訳(費目06)"),
                           "TERM11_SUM_MONEY_DUE"            => array("integer", "11期分＿納入必要額内訳(納入必要金額)"),
                           "TERM11_FLAG01"                   => array("char", 1, "11期分＿納入必要額内訳(前受区分)"),
                           "TERM11_FLAG02"                   => array("char", 1, "11期分＿納入必要額内訳(請求状況区分)"),
                           "TERM11_FLAG03"                   => array("char", 1, "11期分＿納入必要額内訳(振替結果コード)"),
                           "TERM11_FLAG04"                   => array("char", 1, "11期分＿納入必要額内訳(入金状況区分)"),
                           "TERM11_FLAG05"                   => array("char", 1, "11期分＿納入必要額内訳(取消区分)"),
                           "TERM11_PAID_MONEY_DATE"          => array("date", "11期分＿納入必要額内訳(入金日)"),
                           "TERM11_FURI_MONEY_DATE"          => array("date", "11期分＿納入必要額内訳(入金日)"),
                           "TERM12_EXPENSE01_MONEY_DUE"      => array("integer", "12期分＿納入必要額内訳(費目01)"),
                           "TERM12_EXPENSE02_MONEY_DUE"      => array("integer", "12期分＿納入必要額内訳(費目02)"),
                           "TERM12_EXPENSE03_MONEY_DUE"      => array("integer", "12期分＿納入必要額内訳(費目03)"),
                           "TERM12_EXPENSE04_MONEY_DUE"      => array("integer", "12期分＿納入必要額内訳(費目04)"),
                           "TERM12_EXPENSE05_MONEY_DUE"      => array("integer", "12期分＿納入必要額内訳(費目05)"),
                           "TERM12_EXPENSE06_MONEY_DUE"      => array("integer", "12期分＿納入必要額内訳(費目06)"),
                           "TERM12_SUM_MONEY_DUE"            => array("integer", "12期分＿納入必要額内訳(納入必要金額)"),
                           "TERM12_FLAG01"                   => array("char", 1, "12期分＿納入必要額内訳(前受区分)"),
                           "TERM12_FLAG02"                   => array("char", 1, "12期分＿納入必要額内訳(請求状況区分)"),
                           "TERM12_FLAG03"                   => array("char", 1, "12期分＿納入必要額内訳(振替結果コード)"),
                           "TERM12_FLAG04"                   => array("char", 1, "12期分＿納入必要額内訳(入金状況区分)"),
                           "TERM12_FLAG05"                   => array("char", 1, "12期分＿納入必要額内訳(取消区分)"),
                           "TERM12_PAID_MONEY_DATE"          => array("date", "12期分＿納入必要額内訳(入金日)"),
                           "TERM12_FURI_MONEY_DATE"          => array("date", "12期分＿納入必要額内訳(入金日)"),
                           "EXTR_EXPENSE01_TRANS_SDATE"      => array("date", "臨時費目納入必要額＿費目01(引落予定日)"),
                           "EXTR_EXPENSE01_MONEY"            => array("integer", "臨時費目納入必要額＿費目01(納入必要額)"),
                           "EXTR_EXPENSE01_FLAG01"           => array("char", 1, "臨時費目納入必要額＿費目01(前受区分)"),
                           "EXTR_EXPENSE01_FLAG02"           => array("char", 1, "臨時費目納入必要額＿費目01(請求状況区分)"),
                           "EXTR_EXPENSE01_FLAG03"           => array("char", 1, "臨時費目納入必要額＿費目01(振替結果コード)"),
                           "EXTR_EXPENSE01_FLAG04"           => array("char", 1, "臨時費目納入必要額＿費目01(入金状況区分)"),
                           "EXTR_EXPENSE01_FLAG05"           => array("char", 1, "臨時費目納入必要額＿費目01(取消区分)"),
                           "EXTR_EXPENSE01_PAID_MONEY_DATE"  => array("date", "臨時費目納入必要額＿費目01(入金日)"),
                           "EXTR_EXPENSE02_TRANS_SDATE"      => array("date", "臨時費目納入必要額＿費目02(引落予定日)"),
                           "EXTR_EXPENSE02_MONEY"            => array("integer", "臨時費目納入必要額＿費目02(納入必要額)"),
                           "EXTR_EXPENSE02_FLAG01"           => array("char", 1, "臨時費目納入必要額＿費目02(前受区分)"),
                           "EXTR_EXPENSE02_FLAG02"           => array("char", 1, "臨時費目納入必要額＿費目02(請求状況区分)"),
                           "EXTR_EXPENSE02_FLAG03"           => array("char", 1, "臨時費目納入必要額＿費目02(振替結果コード)"),
                           "EXTR_EXPENSE02_FLAG04"           => array("char", 1, "臨時費目納入必要額＿費目02(入金状況区分)"),
                           "EXTR_EXPENSE02_FLAG05"           => array("char", 1, "臨時費目納入必要額＿費目02(取消区分)"),
                           "EXTR_EXPENSE02_PAID_MONEY_DATE"  => array("date", "臨時費目納入必要額＿費目02(入金日)"),
                           "EXTR_EXPENSE03_TRANS_SDATE"      => array("date", "臨時費目納入必要額＿費目03(引落予定日)"),
                           "EXTR_EXPENSE03_MONEY"            => array("integer", "臨時費目納入必要額＿費目03(納入必要額)"),
                           "EXTR_EXPENSE03_FLAG01"           => array("char", 1, "臨時費目納入必要額＿費目03(前受区分)"),
                           "EXTR_EXPENSE03_FLAG02"           => array("char", 1, "臨時費目納入必要額＿費目03(請求状況区分)"),
                           "EXTR_EXPENSE03_FLAG03"           => array("char", 1, "臨時費目納入必要額＿費目03(振替結果コード)"),
                           "EXTR_EXPENSE03_FLAG04"           => array("char", 1, "臨時費目納入必要額＿費目03(入金状況区分)"),
                           "EXTR_EXPENSE03_FLAG05"           => array("char", 1, "臨時費目納入必要額＿費目03(取消区分)"),
                           "EXTR_EXPENSE03_PAID_MONEY_DATE"  => array("date", "臨時費目納入必要額＿費目03(入金日)"),
                           "EXTR_EXPENSE04_TRANS_SDATE"      => array("date", "臨時費目納入必要額＿費目04(引落予定日)"),
                           "EXTR_EXPENSE04_MONEY"            => array("integer", "臨時費目納入必要額＿費目04(納入必要額)"),
                           "EXTR_EXPENSE04_FLAG01"           => array("char", 1, "臨時費目納入必要額＿費目04(前受区分)"),
                           "EXTR_EXPENSE04_FLAG02"           => array("char", 1, "臨時費目納入必要額＿費目04(請求状況区分)"),
                           "EXTR_EXPENSE04_FLAG03"           => array("char", 1, "臨時費目納入必要額＿費目04(振替結果コード)"),
                           "EXTR_EXPENSE04_FLAG04"           => array("char", 1, "臨時費目納入必要額＿費目04(入金状況区分)"),
                           "EXTR_EXPENSE04_FLAG05"           => array("char", 1, "臨時費目納入必要額＿費目04(取消区分)"),
                           "EXTR_EXPENSE04_PAID_MONEY_DATE"  => array("date", "臨時費目納入必要額＿費目04(入金日)"),
                           "EXTR_EXPENSE05_TRANS_SDATE"      => array("date", "臨時費目納入必要額＿費目05(引落予定日)"),
                           "EXTR_EXPENSE05_MONEY"            => array("integer", "臨時費目納入必要額＿費目05(納入必要額)"),
                           "EXTR_EXPENSE05_FLAG01"           => array("char", 1, "臨時費目納入必要額＿費目05(前受区分)"),
                           "EXTR_EXPENSE05_FLAG02"           => array("char", 1, "臨時費目納入必要額＿費目05(請求状況区分)"),
                           "EXTR_EXPENSE05_FLAG03"           => array("char", 1, "臨時費目納入必要額＿費目05(振替結果コード)"),
                           "EXTR_EXPENSE05_FLAG04"           => array("char", 1, "臨時費目納入必要額＿費目05(入金状況区分)"),
                           "EXTR_EXPENSE05_FLAG05"           => array("char", 1, "臨時費目納入必要額＿費目05(取消区分)"),
                           "EXTR_EXPENSE05_PAID_MONEY_DATE"  => array("date", "臨時費目納入必要額＿費目05(入金日)"),
                           "EXTR_EXPENSE06_TRANS_SDATE"      => array("date", "臨時費目納入必要額＿費目06(引落予定日)"),
                           "EXTR_EXPENSE06_MONEY"            => array("integer", "臨時費目納入必要額＿費目06(納入必要額)"),
                           "EXTR_EXPENSE06_FLAG01"           => array("char", 1, "臨時費目納入必要額＿費目06(前受区分)"),
                           "EXTR_EXPENSE06_FLAG02"           => array("char", 1, "臨時費目納入必要額＿費目06(請求状況区分)"),
                           "EXTR_EXPENSE06_FLAG03"           => array("char", 1, "臨時費目納入必要額＿費目06(振替結果コード)"),
                           "EXTR_EXPENSE06_FLAG04"           => array("char", 1, "臨時費目納入必要額＿費目06(入金状況区分)"),
                           "EXTR_EXPENSE06_FLAG05"           => array("char", 1, "臨時費目納入必要額＿費目06(取消区分)"),
                           "EXTR_EXPENSE06_PAID_MONEY_DATE"  => array("date", "臨時費目納入必要額＿費目05(入金日)")
                         );
                         
        foreach($check_arr as $key => $val) {
            $msg = "";
            //データ型によって処理を分ける
            switch ($val[0]) {
                case "char":
                    //文字数チェック
                    if (strlen($data_arr[$key]) > $val[1]) {
                        $msg = "MSG914 入力範囲を超えています。";
                    }
                    break 1;
                case "integer":
                    //数値チェック
                    if (strlen($data_arr[$key]) && !is_numeric($data_arr[$key])) {
                        $msg = "MSG907 入力された値は不正です。「数字」を入力してください。";
                    } elseif (strlen($data_arr[$key]) && $data_arr[$key] > 2147483647) {
                        $msg = "MSG914 入力範囲を超えています。";
                    }
                    break 1;
                case "date":
                    if (strlen($data_arr[$key]) && !preg_match("/([0-9]{8})/",$data_arr[$key])) {
                      $msg = "MSG902 入力された値は不正です。「日付」を入力してください。";
                    //日付の妥当性チェック
                    } elseif ($this->IsDate($data_arr[$key]) == false) {
                      $msg = "MSG902 入力された値は不正です。「日付」を入力してください。";
                    }
                    break 1;
                default:
                    break 1;
            }        
            if ($msg != ""){
                $err_data = array("PROCESSCD"           => "1",
                                 "FILE_LINE_NUMBER"    => $rownum,
                                 "COLNAME"             => $key,
                                 "ERR_MSG"             => $msg);

                $result = $db->query(knjp090kQuery::ErrDataInsert($err_data));
            }
        }
    }
    //CSVファイル取込処理
    function &getInsertModel()
    {
        set_time_limit(0);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除処理TMP
        $db->query(knjp090kQuery::DeleteQuery("BANK_RESULT_TMP_DATA"));
        $db->commit();
        //削除処理TMP
        $db->query(knjp090kQuery::DeleteQuery2("BANK_RESULT_TMP_DATA"));
        $db->commit();

        //削除処理ERR
        $db->query(knjp090kQuery::DeleteQuery("BANK_RESULT_ERR_DATA"));
        $db->commit();
        //削除処理ERR
        $db->query(knjp090kQuery::DeleteQuery2("BANK_RESULT_ERR_DATA"));
        $db->commit();

        //ファイルの文字列の取得
        if ($this->InsertCSVFile($db) == false){
            //削除処理TMP
            $db->query(knjp090kQuery::DeleteQuery("BANK_RESULT_TMP_DATA"));
            $db->commit();
            //削除処理TMP
            $db->query(knjp090kQuery::DeleteQuery2("BANK_RESULT_TMP_DATA"));
            $db->commit();
        }

        //TMP3削除 NO008
        $db->query(knjp090kQuery::DeleteQuery("BANK_RESULT_TMP_DATA3"));
        $db->commit();
        //TMP3削除 NO008
        $db->query(knjp090kQuery::DeleteQuery2("BANK_RESULT_TMP_DATA3"));
        $db->commit();

        //TMP3作成 NO008
        $db->query(knjp090kQuery::InsertDat3($db, $this));
        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    //銀行入金データ消込処理
    function &getUpdateModel2()
    {
        set_time_limit(0);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //エラーテーブルの処理区分が消込処理のものを削除
        $db->query(knjp090kQuery::ErrDataDelete("2"));

        //事前エラーチェック
        knjp090kQuery::InsertBeforeErrors($db, $this);        

        $result = $db->query(knjp090kQuery::getPaidMonth());
        $this->montharray = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["PAID_MONTH"] < 4){
                $this->montharray[$row["PAID_MONTH"]] =  $row["PAID_MONTH"]+12;
            }else {
                $this->montharray[$row["PAID_MONTH"]] =  $row["PAID_MONTH"];
            }
        }

        //事前エラーチェックOKデータの更新
        knjp090kQuery::UpdateAfterProcess2($db, $this);        
        //NO008
        for ($cnt = 4; $cnt <= $this->montharray[$this->paid_month] ; $cnt++){
            $month = $cnt;
            $year  = CTRL_YEAR;
            if ($cnt > 12){
                $month = $cnt - 12;
                $year  = CTRL_YEAR + 1;
            }
            if ($this->montharray[$month] != ""){
                //入金中分類データ登録
                knjp090kQuery::InsertAll_MoneyPaidMDat($db, $this, $month, $year);
                //入金中分類データ更新
                knjp090kQuery::Update_MoneyPaidMDat($db, $this, $month, $year);
                //入金小分類データ登録
                knjp090kQuery::InsertAll_MoneyPaidSDat($db, $this, $month, $year);
                //入金小分類データ登録(入金額オーバーのレコードを追加）
                knjp090kQuery::InsertOver_MoneyPaidSDat($db, $this, $month, $year);
                //入金小分類データ更新
                knjp090kQuery::Update_MoneyPaidSDat($db, $this, $month, $year);
                //入金小分類データ更新(入金額オーバーのレコードを更新）
                knjp090kQuery::Update_OverMoneyPaidSDat($db, $this, $month, $year);
            }
        }
        //TMP2削除 NO008
        knjp090kQuery::DeleteDat2($db, $this);
        //TMP2にデータコピー NO008
        knjp090kQuery::InsertDat2($db, $this);

        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        return;
    }


    //登録銀行データ取込更新処理
    function &getUpdateModel3()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //エラーテーブルの処理区分が消込処理のものを削除
        $db->query(knjp090kQuery::ErrDataDelete("2"));

        //事前エラーチェック(銀行入金データ消込処理)
        knjp090kQuery::InsertBeforeErrors($db, $this);        

        //エラーテーブルの処理区分が更新処理のものを削除
        $db->query(knjp090kQuery::ErrDataDelete("3"));

        //事前エラーチェック（銀行データ取込）
        knjp090kQuery::InsertBeforeBankErrors($db, $this);

        /*** ADD 2005/12/02 by ameku ***/
        //三段目追加分データのカウント用 NO006
        knjp090kQuery::UpdateBankMstCnt($db, $this);
        //銀行マスタ登録
        knjp090kQuery::InsertBankMst($db, $this);
        //銀行マスタ更新 NO005
        knjp090kQuery::UpdateBankMst($db, $this);
        //登録銀行データ登録
        knjp090kQuery::InsertRegistBankDat($db, $this);
        //登録銀行データ更新
        knjp090kQuery::UpdateRegistBankDat($db, $this);


        $db->commit();

        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
        return;
    }

    function IsDataMatched(&$data_arr, $check_arr)
    {

        foreach($check_arr as $key => $val) {

            //データ型によって処理を分ける
            switch ($val[0]) {
                case "char":
                    //文字数チェック
                    if (strlen($data_arr[$key]) > $val[1]) {
                        $vals = array($key,"MSG914 入力範囲を超えています。");
                        return $vals;
                    }
                    break 1;
                case "integer":
                    //数値チェック
                    if (strlen($data_arr[$key]) && !is_numeric($data_arr[$key])) {
                        $vals = array($key,"MSG907 入力された値は不正です。「数字」を入力してください。");
                        return $vals;
                    } elseif (strlen($data_arr[$key]) && $data_arr[$key] > 2147483647) {
                        $vals = array($key,"MSG914 入力範囲を超えています。");
                        return $vals;
                    }
                    break 1;
                case "date":

                  //日付型チェック
                  if (strlen($data_arr[$key]) && !preg_match("/([0-9]{8})/",$data_arr[$key])) {

                      $vals = array($key,"MSG902 入力された値は不正です。「日付」を入力してください。");
                      return $vals;
                  //日付の妥当性チェック
                  } elseif ($this->IsDate($data_arr[$key]) == false) {

                      $vals = array($key,"MSG902 入力された値は不正です。「日付」を入力してください。");
                      return $vals;
                  }

                    break 1;
                default:
                    break 1;
            }
        }
        return true;    
    }
    
    //日付の妥当性チェック
    function IsDate($datestr)
    {
        if ($datestr == "") return true;
         $tmp[0] = substr($datestr, 0, 4);
         $tmp[1] = substr($datestr, 4, 2);
         $tmp[2] = substr($datestr, 6, 2);

        return checkdate($tmp[1], $tmp[2], $tmp[0]);
    }    
}
?>
