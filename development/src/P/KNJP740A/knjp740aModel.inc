<?php

require_once('for_php7.php');

class knjp740aModel extends Model
{
    public $cmd;
    public $control;
    public $field      = array();

    public function knjp740aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");
        $this->seq        = '001'; // 現状は"001"固定（2019.04.04）

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力種別（1:引落し, 2:返金）
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),       //校種

                             //引落し
                             "MONTH"            =>  VARS::post("MONTH"),             //月
                             "MONTH_DATE"       =>  VARS::post("MONTH_DATE"),        //対象日
                             "BANK_CD"          =>  VARS::post("BANK_CD"),           //取扱銀行
                             "TRANSFER_DATE"    =>  VARS::post("TRANSFER_DATE"),     //振替日

                             //返金
                             "HENKIN_DATE"      =>  VARS::post("HENKIN_DATE")        //返金日
                             );

        if ($this->field["OUTPUT"] == "") {
            $this->field["OUTPUT"] = 1;
        }

        //学校銀行情報取得
        $db = Query::dbCheckOut();
        $query = knjp740aQuery::getCollectSchoolBankMst($this);
        $this->bankRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //セット
        $this->printTotalCnt    = 0;
        $this->printTotalMoney  = 0;
        $this->printSyubetsu    = $this->bankRow["SHUBETSU_CD"];       // 種別コード
        $this->printCdDiv       = $this->bankRow["CODE_DIV"];          // コード区分
        $this->printAcountCd    = $this->bankRow["ACCOUNT_CD"];        // 委託者コード
        $this->acountName       = $this->bankRow["ACCOUNTNAME_KANA"];  // 委託者名
        $this->bankCd           = $this->bankRow["BANK_CD"];           // 銀行コード
        $this->bankName         = $this->bankRow["BANKNAME_KANA"];     // 銀行名
        $this->branchCd         = $this->bankRow["BRANCHCD"];          // 支店コード
        $this->branchName       = $this->bankRow["BRANCHNAME_KANA"];   // 支店名
        $this->depositType      = $this->bankRow["DEPOSIT_TYPE"];      // 預金種目
        $this->printAcountNo    = $this->bankRow["ACCOUNTNO"];         // 口座番号
        $this->bankTransferFee  = $this->bankRow["BANK_TRANSFER_FEE"]; // 手数料

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //CSV出力
    public function getDownloadModel()
    {
        if ($this->bankRow["SCHOOL_KIND"] == '') {
            $this->setWarning("MSG203", "CSV出力設定が未登録です。");
            return false;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //銀行休業日取得
        $holidayArray = $this->getHolidayArray($db);

        //引落
        if ($this->field["OUTPUT"] == "1") {
            $this->setLimitDay = $this->setLimitDate($db, $holidayArray);
        //返金
        } else {
            //取扱月日セット
            $this->toriDate = "";
            $date = str_replace("/", "-", $this->field["HENKIN_DATE"]);
            //土日祝日の場合は翌営業日
            $toriatsukai_date = $date;
            for ($d = 0; $d < 100; $d++) {
                $toriatsukai_date = date("Y-m-d", strtotime("$date $d day"));
                //土(6)・日(0)・祝日以外
                if (!in_array(date('w', strtotime($toriatsukai_date)), array(0,6)) && !in_array($toriatsukai_date, $holidayArray)) {
                    break;
                }
            }
            $this->toriDate = str_replace("-", "", substr($toriatsukai_date, 5, 5));
        }

        //初期化
        $contents = "";

        //顧客番号設定チェック用
        $custmoerNoCheckArr = array();

        //SQL文発行
        $data = false;
        $henkinHeader = true;
        $setNewLine   = "";
        $befLmitDate  = "";
        $totalCount   = 0;
        $totalMoney   = 0;
        $henZeroCnt   = 0;
        $setYear      = substr(($this->field["MONTH"] < 4) ? $this->year + 1: $this->year, -2);
        $setYearMonth = $setYear.$this->field["MONTH"];
        //引落
        if ($this->field["OUTPUT"] == "1") {
            $query = knjp740aQuery::getCsvQuery($this, $this->setLimitDay);
        //返金
        } else {
            $query = knjp740aQuery::getCsvQueryHenkin($this);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $row["LIMIT_DATE"] = ($this->field["TRANSFER_DATE"] != "") ? substr(str_replace("/", "", $this->field["TRANSFER_DATE"]), 4) : str_replace("-", "", $row["LIMIT_DATE"]);

            if ($this->field["OUTPUT"] == "1") {
                if ($befLmitDate != "" && $befLmitDate != $row["LIMIT_DATE"]) {
                    //トレーラレコード作成
                    $contents .= padding("8", "0", 1, "RIGHT");      // データ区分
                    $contents .= padding($totalCount, "0", 6, "RIGHT");      // 合計（件数）
                    $contents .= padding($totalMoney, "0", 12, "RIGHT");      // 合計（金額）
                    $contents .= padding("", "0", 6, "RIGHT");      // 振替済（件数）
                    $contents .= padding("", "0", 12, "RIGHT");      // 振替済（金額）
                    $contents .= padding("", "0", 6, "RIGHT");      // 振替不能（件数）
                    $contents .= padding("", "0", 12, "RIGHT");      // 振替不能（金額）
                    $contents .= padding("", "", 65, "LEFT");       // ダミー
                    $contents .= "\r\n";

                    //エンドレコード作成
                    $contents .= padding("9", "0", 1, "RIGHT");       //データ区分
                    $contents .= padding("", "", 119, "LEFT");        //ダミー
                    $contents .= "\r\n";

                    $totalCount   = 0; //リセット
                    $totalMoney   = 0; //リセット
                }

                if ($befLmitDate == "" || $befLmitDate != $row["LIMIT_DATE"]) {
                    //ヘッダレコード作成
                    $contents .= $setNewLine;
                    $contents .= padding("1", "0", 1, "RIGHT");      // データ区分
                    $contents .= padding($this->printSyubetsu, "0", 2, "RIGHT");      // 種別コード
                    $contents .= padding($this->printCdDiv, "0", 1, "RIGHT");      // コード区分
                    $contents .= padding($this->printAcountCd, "0", 10, "RIGHT");      // 委託者コード
                    $contents .= padding($this->acountName, "", 40, "LEFT");       // 委託者名
                    $contents .= padding($row["LIMIT_DATE"], "0", 4, "RIGHT");      // 引落日
                    $contents .= padding($this->bankCd, "0", 4, "RIGHT");      // 銀行コード
                    $contents .= padding($this->bankName, "", 15, "LEFT");       // 銀行名
                    $contents .= padding($this->branchCd, "0", 3, "RIGHT");      // 支店コード
                    $contents .= padding($this->branchName, "", 15, "LEFT");       // 支店名
                    $contents .= padding($this->depositType, "0", 1, "RIGHT");      // 預金種目
                    $contents .= padding($this->printAcountNo, "0", 7, "RIGHT");      // 口座番号
                    $contents .= padding("", "", 17, "LEFT");       // ダミー
                    $contents .= "\r\n";
                    $this->printLimitDate = $row["LIMIT_DATE"];
                    $this->printSaiHuri = $setSaiHuri;
                }

                //データレコード作成
                $planYearMonth = substr($row["PLAN_YEAR"], 2, 3).$row["PLAN_MONTH"];
                $planMoney = $row["PLAN_MONEY"] - $row["COUNTRY_MONEY"] - $row["COUNTRY_ADDMONEY"] - $row["PREF_MONEY1"] - $row["PREF_MONEY2"] - $row["BURDEN_CHARGE1"] - $row["BURDEN_CHARGE2"] - $row["SCHOOL_1"] - $row["SCHOOL_2"];
                if ($this->Properties["IncludeBankTransferFee"] == "1") {
                    $planMoneyIncludeFee = $planMoney + $this->bankTransferFee;
                } else {
                    $planMoneyIncludeFee = $planMoney;
                }

                $befLmitDate = $row["LIMIT_DATE"];

                if ($planMoney > 0) {
                    $contents .= padding("2", "0", 1, "RIGHT");      // データ区分
                    $contents .= padding($row["BANKCD"], "0", 4, "RIGHT");      // 引落銀行番号
                    $contents .= padding($row["BANKNAME_KANA"], "", 15, "LEFT");       // 引落銀行名
                    $contents .= padding($row["BRANCHCD"], "0", 3, "RIGHT");      // 引落支店番号
                    $contents .= padding($row["BRANCHNAME_KANA"], "", 15, "LEFT");       // 引落支店名
                    $contents .= padding("", "", 4, "LEFT");       // ダミー
                    $contents .= padding($row["DEPOSIT_ITEM"], "0", 1, "RIGHT");      // 預金種目
                    $contents .= padding($row["ACCOUNTNO"], "0", 7, "RIGHT");      // 通常貯金番号
                    $contents .= padding($row["ACCOUNTNAME"], "", 30, "LEFT");       // 氏名
                    $contents .= padding($planMoneyIncludeFee, "0", 10, "RIGHT");      // 請求金額
                    $contents .= padding("1", "0", 1, "RIGHT");      // 新規コード
                    $contents .= padding($row["CUSTOMER_NUMBER"], "0", 20, "RIGHT");      // 顧客番号
                    $contents .= padding("0", "0", 1, "RIGHT");      // 新規コード
                    $contents .= padding("", "", 8, "LEFT");       // ダミー
                    $contents .= "\r\n";

                    if ($this->cmd == 'csv2') {
                        //COLLECT_SLIP_PLAN_CSV_DATを更新
                        knjp740aQuery::updeteQueryCsvDat($db, $this, $row, $planMoney);
                    }

                    $setNewLine = "\r\n";
                    if ($row["PLAN_MONEY"] != "0") {
                        $totalCount++;
                        $this->printTotalCnt++;
                    }
                    if ($row["PLAN_MONEY"] != "0") {
                        $totalMoney += $planMoneyIncludeFee;
                        $this->printTotalMoney += $planMoneyIncludeFee;
                    }
                }

                //顧客番号がnullの人が一人でもいればエラーを返す用
                if ($row["CUSTOMER_NUMBER"] == '') {
                    $custmoerNoCheckArr[$row["SCHREGNO"]] = $row["SCHREGNO"];
                }
            }
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            if ($this->field["OUTPUT"] == "1") {
                //トレーラレコード作成
                $contents .= padding("8", "0", 1, "RIGHT");      // データ区分
                $contents .= padding($totalCount, "0", 6, "RIGHT");      // 合計（件数）
                $contents .= padding($totalMoney, "0", 12, "RIGHT");      // 合計（金額）
                $contents .= padding("", "0", 6, "RIGHT");      // 振替済（件数）
                $contents .= padding("", "0", 12, "RIGHT");      // 振替済（金額）
                $contents .= padding("", "0", 6, "RIGHT");      // 振替不能（件数）
                $contents .= padding("", "0", 12, "RIGHT");      // 振替不能（金額）
                $contents .= padding("", "", 65, "LEFT");       // ダミー
                $contents .= "\r\n";

                //エンドレコード作成
                $contents .= padding("9", "0", 1, "RIGHT");       //データ区分
                $contents .= padding("", "", 119, "LEFT");        //ダミー
                $contents .= "\r\n";

            //返金
            } else {

                //トレーラレコード作成
                $contents .= padding("8", "0", 1, "RIGHT");  // レコード区分
                $contents .= padding($totalCount, "0", 6, "RIGHT");  // 合計（件数）
                $contents .= padding($totalMoney, "0", 12, "RIGHT");  // 合計（金額）
                $contents .= padding("", "", 6, "LEFT");   // 処理済（件数）
                $contents .= padding("", "", 12, "LEFT");   // 処理済（金額）
                $contents .= padding("", "", 6, "LEFT");   // 処理不能（件数）
                $contents .= padding("", "", 12, "LEFT");   // 処理不能（金額）
                $contents .= padding($henZeroCnt, "0", 6, "RIGHT");  // 照会請求件数(請求金額0円の件数)
                $contents .= padding("", "", 6, "LEFT");   // 照会正常件数
                $contents .= padding("", "", 6, "LEFT");   // 照会事故件数
                $contents .= padding("", "", 47, "LEFT");   // 予備
                $contents .= "\r\n";

                //エンドレコード作成
                $contents .= padding("9", "0", 1, "RIGHT");   //レコード区分
                $contents .= padding("", "", 119, "LEFT");    //予備
                $contents .= "\r\n";
            }
        }

        //顧客番号がnullの人が一人でもいればエラーを返す
        $setSchregNo = $sep = '';
        foreach ($custmoerNoCheckArr as $schregNo => $val) {
            $setSchregNo .= $sep.$schregNo;
            $sep = "\\n";
        }

        //ＣＳＶ出力
        if ($data) {
            if ($this->cmd == 'csv') {
                return true;
            }
            if ($this->field["OUTPUT"] == "1") {
                $titleDay = str_replace("-", "_", $this->field["MONTH_DATE"]);
                $title = "引落しフォーマット".$titleDay;
            } else {
                $title = "返金フォーマット";
            }

            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");

            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function getHolidayArray($db)
    {
        $holidayArray = array();
        $result = $db->query(knjp740aQuery::getBankHolidayDat($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holidayArray[] = $row["EXECUTEDATE"];
        }
        return $holidayArray;
    }

    public function setLimitDate($db, $holidayArray)
    {
        //引き落とし日取得
        $debitDate = $db->getOne(knjp740aQuery::getDirectDebitDate($this));

        //入金日は、土日祝日の場合は翌営業日
        $this->setLimitDay = "";
        $paid_limit_date = "";
        if (strlen($this->field["MONTH"])) {
            $month = intval($this->field["MONTH"], 10);
            $year = ($this->field["MONTH"] <= 3) ? (CTRL_YEAR + 1) : CTRL_YEAR;
            $lastday = date("t", mktime(0, 0, 0, $this->field["MONTH"], 1, $year));
            $day = ($debitDate > $lastday) ? $lastday : $debitDate;

            $date = $year."-".sprintf("%02d", $this->field["MONTH"])."-".sprintf("%02d", $day);

            //土日祝日の場合は翌営業日
            $paid_limit_date = $date;
            for ($d = 0; $d < 100; $d++) {
                $paid_limit_date = date("Y-m-d", strtotime("$date $d day"));
                //土(6)・日(0)・祝日以外
                if (!in_array(date('w', strtotime($paid_limit_date)), array(0,6)) && !in_array($paid_limit_date, $holidayArray)) {
                    break;
                }
            }
        }
        return substr($paid_limit_date, 5, 5);
    }
}
function padding($str, $padStr, $pad_length, $align)
{
    if ($align == "LEFT") {
        $addStr = "";
        $addLength = $pad_length - mb_strlen($str);
        for ($i = 0; $i < $addLength; $i++) {
            $addStr .= " ";
        }
        $str = $str.$addStr;
    } else {
        $str = sprintf("%".$padStr.$pad_length ."s", $str);
    }

    $str = mb_substr($str, 0, $pad_length);

    return $str;
}
