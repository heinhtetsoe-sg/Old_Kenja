<?php

require_once('for_php7.php');
class knjp740Model extends Model {
    var $cmd;
    var $control;
    var $field      = array();

    function knjp740Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力種別（1:引落し, 2:返金）
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),       //校種

                             //引落し
                             "STAFF_LUNCH"      =>  VARS::post("STAFF_LUNCH"),       //教員給食費
                             "MONTH"            =>  VARS::post("MONTH"),             //月
                             "MONTH_DATE"       =>  VARS::post("MONTH_DATE"),        //対象日
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),        //送付日
                             "TRANSFER_DATE"    =>  VARS::post("TRANSFER_DATE"),     //振替日
                             "RETRANSFER_DATE"  =>  VARS::post("RETRANSFER_DATE"),   //再振替日
                             "PRINT_TYPE"       =>  VARS::post("PRINT_TYPE"),        //帳票種類

                             //返金
                             "HENKIN_DATE"      =>  VARS::post("HENKIN_DATE")        //返金日
                             );

        if ($this->field["OUTPUT"] == "") {
            $this->field["OUTPUT"] = 1;
        }

        //定数化
        $this->printKouzaKigou  = "014";
        $this->printKyokuBan    = "186657";
        $this->printLimitDate   = "";
        $this->printSaiHuri     = "";
        $this->printTotalCnt    = 0;
        $this->printTotalMoney  = 0;
        $this->printSyubetsu    = ($this->field["OUTPUT"] == "1") ? "30": "09";
        $this->printJigyounushi = ($this->field["OUTPUT"] == "1") ? "01709946": "19055688";
        $this->printUkeireJcCd  = "01";
        $this->toriKyouName     = "010230";
        $this->printLimitDate2  = str_replace("-", "", $this->field["MONTH_DATE"]);

        //学校銀行情報取得
        $db = Query::dbCheckOut();
        $query = knjp740Query::getCollectSchoolBankMst($this);
        $this->bankRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        $this->bankTransferFee  = $this->bankRow["BANK_TRANSFER_FEE"];

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //ファイル
        $this->file = VARS::file("FILE");
    }

    //CSV出力
    function getDownloadModel() {

        //教員給食費checkがある時、ＣＳＶ取込
        if ($this->field["STAFF_LUNCH"] == "1") {
            //ファイルの文字列の取得
            $this->data_arr = array();
            if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
                $this->warning = "ファイル名が不正です。";
                return false;
            } else {
                $checkdata["YUCHO_CD"]      = array(4,      "HISSU",  "INT2",  "金融機関コード");
                $checkdata["YUCHO_NAME"]    = array(15,     "HISSU",  "KANA",  "金融機関名");
                $checkdata["BRANCHCD"]      = array(3,      "HISSU",  "INT2",  "記号");
                $checkdata["ACCOUNTNO"]     = array(7,      "HISSU",  "INT2",  "貯金番号");
                $checkdata["STAFFCD"]       = array(8,      "HISSU",  "INT2",  "教職員番号");
                $checkdata["ACCOUNTNAME"]   = array(30,     "HISSU",  "KANA",  "氏名");
                $checkdata["PLAN_MONEY"]    = array(10,     "HISSU",  "INT2",  "請求金額");
                $checkdata["YEAR_MONTH"]    = array(4,      "HISSU",  "INT2",  "年月");
                $checkdata["SYORI_CD"]      = array(2,      "HISSU",  "INT2",  "コード");
                $checkdata["HOJO"]          = array(2,      "HISSU",  "INT2",  "補助文言表示");

                $fp = fopen ($this->file["tmp_name"], "r");
                $i  = 0;
                $line = 1;  //行数
                $header = false;
                while (!feof ($fp)) {
                    $buffer = fgets($fp, 4096);
                    if (trim($buffer) != '') {
                        if (!$header && $this->field["HEADER"] == "on") {
                            $header = true;
                            $line++;
                            continue;
                        }
                        //文字コードを判別、改行コードを削除
                        $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                        $tmp    = common::csv2array($buffer);
                        //配列の要素数チェック
                        $checkcount = get_count($tmp);

                        $data["YUCHO_CD"]       = $tmp[0];
                        $data["YUCHO_NAME"]     = $tmp[1];
                        $data["BRANCHCD"]       = $tmp[2];
                        $data["ACCOUNTNO"]      = $tmp[3];
                        $data["STAFFCD"]        = $tmp[4];
                        $data["ACCOUNTNAME"]    = $tmp[5];
                        $data["PLAN_MONEY"]     = $tmp[6];
                        $data["YEAR_MONTH"]     = $tmp[7];
                        $data["SYORI_CD"]       = $tmp[8];
                        $data["HOJO"]           = $tmp[9];

                        $dataCnt = 10;

                        //項目数によるファイルチェック
                        if ($checkcount != $dataCnt) {
                            $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                            return false;
                        }

                        $this->data_arr[$i] = $data;
                        $i++;

                        //エラーチェック
                        $errMsg = $this->errCheck($db, $data, $checkdata, $line);
                        if ($errMsg != "") {
                            $this->warning = $errMsg;
                            return false;
                        }

                        $line++;
                    }
                }
                fclose($fp);
            }
            knjp740Query::insertQueryCsv($this, $this->data_arr);
        } else {
            knjp740Query::deleteQuery();
        }

        //DB接続
        $db = Query::dbCheckOut();

        //銀行休業日取得
        $holidayArray = $this->getHolidayArray($db);

        $henkinYearMonth = "";
        //引落
        if ($this->field["OUTPUT"] == "1") {
            //引き落とし日取得
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
            list($henY, $henM, $henD) = explode("-", $toriatsukai_date);
            $henkinYearMonth = substr($henY, -2).$this->field["MONTH"];
            $this->toriDate = str_replace("-", "", substr($toriatsukai_date, 5, 5));
        }

        //初期化
        $contents = "";

        //SQL文発行
        $data = false;
        $henkinHeader = true;
        $setNewLine   = "";
        $befLmitDate  = "";
        $totalCount   = 0;
        $totalMoney   = 0;
        $henZeroCnt   = 0;
        $jigyousyumei = "ﾁﾖﾀﾞｸﾘﾂｸﾀﾞﾝﾁｭｳﾄｳｷｮｳｲｸｶﾞｯｺｳ";
        $setYear      = substr(($this->field["MONTH"] < 4) ? $this->year + 1: $this->year, -2);
        $setYearMonth = $setYear.$this->field["MONTH"];
        $setSaiHuri   = substr(str_replace("/", "", $this->field["RETRANSFER_DATE"]), 4, 4);
        //引落
        if ($this->field["OUTPUT"] == "1") {
            $query = knjp740Query::getCsvQuery($this, $this->setLimitDay);
        //返金
        } else {
            $query = knjp740Query::getCsvQueryHenkin($this);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);

            $row["LIMIT_DATE"] = str_replace("-", "", $row["LIMIT_DATE"]);

            //引落
            if ($this->field["OUTPUT"] == "1") {

                if ($befLmitDate == "") {
                    //ヘッダレコード作成
                    $contents .= $setNewLine;
                    $contents .= padding("1",                       "0",  1, "RIGHT");      // レコード区分
                    $contents .= padding($this->printSyubetsu,      "0",  2, "RIGHT");      // 契約種別コード
                    $contents .= padding("",                         "",  3, "LEFT");       // 予備
                    $contents .= padding($this->printJigyounushi,   "0",  8, "RIGHT");      // 事業主番号
                    $contents .= padding($jigyousyumei,              "", 40, "LEFT");       // 事業主名
                    $contents .= padding(str_replace("-", "", $this->field["MONTH_DATE"]), "0",  4, "RIGHT");      // 請求月日
                    $contents .= padding("9900",                    "0",  4, "RIGHT");      // 金融機関コード
                    $contents .= padding("ﾕｳﾁｮｷﾞﾝｺｳ",                "", 15, "LEFT");       // 金融機関名
                    $contents .= padding($this->printKouzaKigou,    "0",  3, "RIGHT");      // 振替口座記号
                    $contents .= padding("",                         "", 17, "LEFT");       // 予備
                    $contents .= padding($this->printKyokuBan,      "0",  6, "RIGHT");      // 取扱局番号
                    $contents .= padding($setSaiHuri,                "",  4, "RIGHT");      // 再振替日
                    $contents .= padding("",                         "",  1, "RIGHT");      // 振替回数
                    $contents .= padding("",                         "", 12, "RIGHT");      // 予備
                    $contents .= "\r\n";
                    //$this->printLimitDate = $row["LIMIT_DATE"];
                    $this->printSaiHuri = $setSaiHuri;
                }

                //データレコード作成
                $planYearMonth = substr($row["PLAN_YEAR"], 2, 3).$row["PLAN_MONTH"];
                $planMoney = $row["PLAN_MONEY"] - $row["COUNTRY_MONEY"] - $row["COUNTRY_ADDMONEY"] - $row["PREF_MONEY1"] - $row["PREF_MONEY2"] - $row["BURDEN_CHARGE1"] - $row["BURDEN_CHARGE2"] - $row["SCHOOL_1"] - $row["SCHOOL_2"];
                if ($this->Properties["IncludeBankTransferFee"] == "1") {
                    $planMoney += $this->bankTransferFee;
                }
                $contents .= padding("2",                       "0",  1, "RIGHT");      // レコード区分
                $contents .= padding("9900",                    "0",  4, "RIGHT");      // 金融機関コード
                $contents .= padding("ﾕｳﾁｮｷﾞﾝｺｳ",                "", 15, "LEFT");       // 金融機関名
                $contents .= padding($row["BRANCHCD"],          "0",  3, "RIGHT");      // 通常貯金記号(店番)
                $contents .= padding("",                         "", 15, "LEFT");       // 予備
                $contents .= padding("00",                      "0",  2, "RIGHT");      // 振替口座表示
                $contents .= padding($this->printSyubetsu,      "0",  2, "RIGHT");      // 契約種別表示
                $contents .= padding("",                         "",  1, "LEFT");       // 予備
                $contents .= padding($row["ACCOUNTNO"],         "0",  7, "RIGHT");      // 通常貯金番号
                $contents .= padding($row["ACCOUNTNAME"],        "", 30, "LEFT");       // 氏名
                $contents .= padding($planMoney,                "0", 10, "RIGHT");      // 請求金額
                $contents .= padding("1",                       "0",  1, "RIGHT");      // 照会表示
                $contents .= padding($row["GRADE"],             "0",  2, "RIGHT");      // 顧客番号（学年）
                $contents .= padding($row["HR_CLASS"],          "0",  3, "RIGHT");      // 顧客番号（組）
                $contents .= padding($row["ATTENDNO"],          "0",  3, "RIGHT");      // 顧客番号（番号）
                $contents .= padding($row["SCHREGNO"],          "0",  8, "RIGHT");      // 顧客番号（学籍番号）
                $contents .= padding($planYearMonth,            "0",  4, "RIGHT");      // 顧客番号（月日）
                $contents .= padding("",                         "",  1, "LEFT");       // 振替結果コード
                $contents .= padding($planYearMonth,            "0",  4, "RIGHT");      // 優先処理（月日）
                $contents .= padding("00",                       "",  2, "RIGHT");      // 優先処理（コード）
                $contents .= padding($row["PLAN_MONTH"],         "",  2, "RIGHT");      // 補助文言表示
                $contents .= "\r\n";

                $befLmitDate = $row["LIMIT_DATE"];
                $setNewLine = "\r\n";
                if ($row["PLAN_MONEY"] != "0") {
                    $totalCount++;
                    $this->printTotalCnt++;
                }
                if ($row["PLAN_MONEY"] != "0") {
                    $totalMoney += $planMoney;
                    $this->printTotalMoney += $planMoney;
                }

            //返金
            } else {
                if ($henkinHeader) {
                    $setItakuCd = $this->printUkeireJcCd.$this->printJigyounushi;
                    //ヘッダレコード作成
                    $contents .= padding("1",                       "0",  1, "RIGHT");      // レコード区分
                    $contents .= padding($this->printSyubetsu,      "0",  2, "RIGHT");      // 契約種別コード
                    $contents .= padding("",                         "",  1, "LEFT");       // コード区分
                    $contents .= padding($this->printUkeireJcCd,    "0",  2, "RIGHT");      // 委託者コード（JCコード）
                    $contents .= padding($this->printJigyounushi,   "0",  8, "RIGHT");      // 委託者コード（事業主番号）
                    $contents .= padding($jigyousyumei,              "", 40, "LEFT");       // 事業主名
                    $contents .= padding($this->toriDate,           "0",  4, "RIGHT");      // 取扱月日
                    $contents .= padding("9900",                    "0",  4, "RIGHT");      // 金融機関コード
                    $contents .= padding("ﾕｳﾁｮｷﾞﾝｺｳ",                "", 15, "LEFT");       // 金融機関名
                    $contents .= padding("",                         "",  3, "LEFT");       // 振替口座記号
                    $contents .= padding("",                         "", 15, "LEFT");       // 取扱局名
                    $contents .= padding("",                         "",  1, "LEFT");       // 口座種目
                    $contents .= padding($this->toriKyouName,        "",  7, "RIGHT");      // 振替口座番号
                    $contents .= padding("",                         "", 17, "LEFT");       // 予備
                    $contents .= "\r\n";
                    $this->printLimitDate = $this->toriDate;
                }
                //データレコード作成
                $contents .= padding("2",                       "0",  1, "RIGHT");      // レコード区分
                $contents .= padding("9900",                    "0",  4, "RIGHT");      // 金融機関コード
                $contents .= padding("ﾕｳﾁｮｷﾞﾝｺｳ",                "", 15, "LEFT");       // 金融機関名１
                $contents .= padding($row["BRANCHCD"],          "0",  3, "RIGHT");      // 通常貯金記号(店番)
                $contents .= padding("",                         "", 15, "LEFT");       // 金融機関名２
                $contents .= padding("",                         "",  4, "LEFT");       // 手形交換所番号
                $contents .= padding("",                         "",  1, "LEFT");       // 預金種目
                $contents .= padding($row["ACCOUNTNO"],         "0",  7, "RIGHT");      // 通常貯金番号
                $contents .= padding($row["ACCOUNTNAME"],        "", 30, "LEFT");       // カナ氏名
                $fee = $row["BANK_TRANSFER_FEE"] ? $row["BANK_TRANSFER_FEE"] : $this->tesuRyo;
                $setRequestGk = $row["REQUEST_GK"] - $fee;
                if ($this->Properties["useBenefit"] == "1") {
                    $setRequestGk = $setRequestGk + $row["KYUFU_MONEY"];             // 給付金を精算総額に足しこむ
                }
                $contents .= padding($setRequestGk,             "0", 10, "RIGHT");      // 送金金額
                $contents .= padding("1",                       "0",  1, "RIGHT");      // 新規コード
                $contents .= padding($row["GRADE"],             "0",  2, "RIGHT");      // 顧客番号（学年）
                $contents .= padding($row["HR_CLASS"],          "0",  3, "RIGHT");      // 顧客番号（組）
                $contents .= padding($row["ATTENDNO"],          "0",  3, "RIGHT");      // 顧客番号（番号）
                $contents .= padding($row["SCHREGNO"],          "0",  8, "RIGHT");      // 顧客番号（学籍番号）
                $contents .= padding($henkinYearMonth,          "0",  4, "RIGHT");      // 顧客番号（月日）
                $contents .= padding("",                         "",  1, "LEFT");       // 処理結果コード
                $contents .= padding("",                         "",  6, "LEFT");       // 予備
                $contents .= padding("",                         "",  2, "LEFT");       // 補助文言表示
                $contents .= "\r\n";

                if ($row["REQUEST_GK"] != "0") {
                    $totalCount++;
                    $totalMoney += $setRequestGk;
                    $this->printTotalMoney += $setRequestGk;
                } else {
                    $henZeroCnt++;
                }
                $this->printTotalCnt++;

                $henkinHeader = false;
            }
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            if ($this->field["OUTPUT"] == "1") {
                //教員給食費
                if ($this->field["STAFF_LUNCH"] == "1") {
                    if ($this->data_arr[0] != "") {
                        for ($i = 0; $i < get_count($this->data_arr); $i++) {
                            //データレコード作成
                            $contents .= padding("2",                                  "0",  1, "RIGHT");      // レコード区分
                            $contents .= padding($this->data_arr[$i]["YUCHO_CD"],      "0",  4, "RIGHT");      // 金融機関コード
                            $contents .= padding($this->data_arr[$i]["YUCHO_NAME"],     "", 15, "LEFT");       // 金融機関名
                            $contents .= padding($this->data_arr[$i]["BRANCHCD"],      "0",  3, "RIGHT");      // 通常貯金記号(店番)
                            $contents .= padding("",                                    "", 15, "LEFT");       // 予備
                            $contents .= padding("00",                                 "0",  2, "RIGHT");      // 振替口座表示
                            $contents .= padding("{$this->printSyubetsu}",             "0",  2, "RIGHT");      // 契約種別表示
                            $contents .= padding("",                                    "",  1, "LEFT");       // 予備
                            $contents .= padding($this->data_arr[$i]["ACCOUNTNO"],     "0",  7, "RIGHT");      // 通常貯金番号
                            $contents .= padding($this->data_arr[$i]["ACCOUNTNAME"],    "", 30, "LEFT");       // 氏名
                            $contents .= padding($this->data_arr[$i]["PLAN_MONEY"],    "0", 10, "RIGHT");      // 請求金額
                            $contents .= padding("1",                                  "0",  1, "RIGHT");      // 照会表示
                            $contents .= padding("",                                   "0",  2, "RIGHT");      // 顧客番号（00）
                            $contents .= padding("",                                   "0",  3, "RIGHT");      // 顧客番号（000）
                            $contents .= padding("",                                   "0",  3, "RIGHT");      // 顧客番号（000）
                            $contents .= padding($this->data_arr[$i]["STAFFCD"],       "0",  8, "RIGHT");      // 顧客番号（職員番号）
                            $contents .= padding($setYearMonth,                        "0",  4, "RIGHT");      // 顧客番号（0000）
                            $contents .= padding("",                                    "",  1, "LEFT");       // 振替結果コード
                            $contents .= padding($this->data_arr[$i]["YEAR_MONTH"],    "0",  4, "RIGHT");      // 優先処理（月日）
                            $contents .= padding($this->data_arr[$i]["SYORI_CD"],       "",  2, "RIGHT");      // 優先処理（コード）
                            $contents .= padding($this->data_arr[$i]["HOJO"],           "",  2, "RIGHT");      // 補助文言表示
                            $contents .= "\r\n";
                            if ($this->data_arr[$i]["PLAN_MONEY"] != "0") {
                                $totalCount++;
                                $this->printTotalCnt++;
                            }
                            if ($this->data_arr[$i]["PLAN_MONEY"] != "0") {
                                $totalMoney += $this->data_arr[$i]["PLAN_MONEY"];
                                $this->printTotalMoney += $this->data_arr[$i]["PLAN_MONEY"];
                            }

                        }
                    }
                }

                //トレーラレコード作成
                $contents .= padding("8",           "0",  1, "RIGHT");  // レコード区分
                $contents .= padding($totalCount,   "0",  6, "RIGHT");  // 合計（件数）
                $contents .= padding($totalMoney,   "0", 12, "RIGHT");  // 合計（金額）
                $contents .= padding("",             "",  6, "LEFT");   // 払込（件数）
                $contents .= padding("",             "", 12, "LEFT");   // 払込（金額）
                $contents .= padding("",             "",  6, "LEFT");   // 払込不能（件数）
                $contents .= padding("",             "", 12, "LEFT");   // 払込不能（金額）
                $contents .= padding("0",           "0",  6, "RIGHT");  // 照会請求件数
                $contents .= padding("",             "",  6, "LEFT");   // 照会正常件数
                $contents .= padding("",             "",  6, "LEFT");   // 照会事故件数
                $contents .= padding("",             "",  6, "LEFT");   // 再振替（件数）
                $contents .= padding("",             "", 12, "LEFT");   // 再振替（金額）
                $contents .= padding("",             "", 29, "LEFT");   // 予備
                $contents .= "\r\n";

                //エンドレコード作成
                $contents .= padding("9",         "0",   1, "RIGHT");   //レコード区分
                $contents .= padding("",           "", 119, "LEFT");    //予備
                $contents .= "\r\n";

            //返金
            } else {
                //教員給食費
                if ($this->field["STAFF_LUNCH"] == "1") {
                    if ($this->data_arr[0] != "") {
                        for ($i = 0; $i < get_count($this->data_arr); $i++) {
                            //データレコード作成

                            $contents .= padding("2",                                  "0",  1, "RIGHT");      // レコード区分
                            $contents .= padding($this->data_arr[$i]["YUCHO_CD"],      "0",  4, "RIGHT");      // 金融機関コード
                            $contents .= padding($this->data_arr[$i]["YUCHO_NAME"],     "", 15, "LEFT");       // 金融機関名
                            $contents .= padding($this->data_arr[$i]["BRANCHCD"],      "0",  3, "RIGHT");      // 通常貯金記号(店番)
                            $contents .= padding("",                                    "", 15, "LEFT");       // 金融機関名２
                            $contents .= padding("",                                    "",  4, "LEFT");       // 手形交換所番号
                            $contents .= padding("",                                    "",  1, "LEFT");       // 預金種目
                            $contents .= padding($this->data_arr[$i]["ACCOUNTNO"],     "0",  7, "RIGHT");      // 通常貯金番号
                            $contents .= padding($this->data_arr[$i]["ACCOUNTNAME"],    "", 30, "LEFT");       // 氏名
                            $setRequestGk = $this->data_arr[$i]["PLAN_MONEY"] - $this->tesuRyo;
                            $contents .= padding($setRequestGk,    "0", 10, "RIGHT");                          // 送金金額
                            $contents .= padding("1",                                  "0",  1, "RIGHT");      // 新規コード
                            $contents .= padding("",                                   "0",  2, "RIGHT");      // 顧客番号（00）
                            $contents .= padding("",                                   "0",  3, "RIGHT");      // 顧客番号（000）
                            $contents .= padding("",                                   "0",  3, "RIGHT");      // 顧客番号（000）
                            $contents .= padding($this->data_arr[$i]["STAFFCD"],       "0",  8, "RIGHT");      // 顧客番号（職員番号）
                            $contents .= padding($henkinYearMonth,                     "0",  4, "RIGHT");      // 顧客番号（0000）
                            $contents .= padding("",                                    "",  1, "LEFT");       // 処理結果コード
                            $contents .= padding("",                                    "",  6, "LEFT");       // 予備
                            $contents .= padding("",                                    "",  2, "LEFT");       // 補助文言表示
                            $contents .= "\r\n";

                            if ($setRequestGk != "0") {
                                $totalCount++;
                                $this->printTotalCnt++;
                            }
                            if ($setRequestGk != "0") {
                                $totalMoney += $setRequestGk;
                                $this->printTotalMoney += $setRequestGk;
                            }

                        }
                    }
                }

                //トレーラレコード作成
                $contents .= padding("8",           "0",  1, "RIGHT");  // レコード区分
                $contents .= padding($totalCount,   "0",  6, "RIGHT");  // 合計（件数）
                $contents .= padding($totalMoney,   "0", 12, "RIGHT");  // 合計（金額）
                $contents .= padding("",             "",  6, "LEFT");   // 処理済（件数）
                $contents .= padding("",             "", 12, "LEFT");   // 処理済（金額）
                $contents .= padding("",             "",  6, "LEFT");   // 処理不能（件数）
                $contents .= padding("",             "", 12, "LEFT");   // 処理不能（金額）
                $contents .= padding($henZeroCnt,   "0",  6, "RIGHT");  // 照会請求件数(請求金額0円の件数)
                $contents .= padding("",             "",  6, "LEFT");   // 照会正常件数
                $contents .= padding("",             "",  6, "LEFT");   // 照会事故件数
                $contents .= padding("",             "", 47, "LEFT");   // 予備
                $contents .= "\r\n";

                //エンドレコード作成
                $contents .= padding("9",         "0",   1, "RIGHT");   //レコード区分
                $contents .= padding("",           "", 119, "LEFT");    //予備
                $contents .= "\r\n";
            }
        }

        if ($this->cmd == "print") {
            return true;
        }

        //ＣＳＶ出力
        if ($data) {
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

    //エラーチェック
    function errCheck($db, $data, $data_each, $line) {
        $returnFlg       = true;
        $returnMsg       = "";
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $otherErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $otherErrMsg     = "";       //その他エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $otherSep     = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
//                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                $returnMsg = "※必須:{$val[3]}({$line}行目)";
                return $returnMsg;
            }
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
//                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                $returnMsg = "※数値:{$val[3]}({$line}行目)";
                return $returnMsg;
            }
            //文字数チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && strlen($data[$key]) > $val[0]) {
//                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                $returnMsg = "※サイズ:{$val[3]}({$line}行目)";
                return $returnMsg;
            }
            //半角カナチェック
            if ($val[2] == "KANA" && !preg_match('/^[ｦ-ﾟｰ 0-9]+$/u', $data[$key])) {
                $returnMsg = "※半角カナ:{$val[3]}({$line}行目)";
                return $returnMsg;
            }
            //文字数チェック(ｶﾅ)
            if (($val[2] == "KANA") && mb_strlen($data[$key]) > $val[0]) {
//                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                $returnMsg = "※サイズ:{$val[3]}({$line}行目)";
                return $returnMsg;
            }

/*
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }
*/
        }

/*
        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjx185Query::insertQueryErr($db, $line, $check_error);
        }
*/

        return $returnMsg;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    function getHolidayArray($db) {
        $holidayArray = array();
        $result = $db->query(knjp740Query::getBankHolidayDat($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $holidayArray[] = $row["EXECUTEDATE"];
        }
        return $holidayArray;
    }

    function setLimitDate($db, $holidayArray) {
        //引き落とし日取得
        $debitDate = $db->getOne(knjp740Query::getDirectDebitDate($this));

        //入金日は、土日祝日の場合は翌営業日
        $this->setLimitDay = "";
        $paid_limit_date = "";
        if (strlen($this->field["MONTH"])) {
            $month = intval($this->field["MONTH"], 10);
            $year = ($this->field["MONTH"] <= 3) ? (CTRL_YEAR + 1) : CTRL_YEAR;
            $lastday = date("t", mktime( 0, 0, 0, $this->field["MONTH"], 1, $year));
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
function padding($str, $padStr, $pad_length, $align) {
    if ($align == "LEFT") {
//        $str = sprintf("%-".$padStr.$pad_length ."s", $str);
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

//    $str = mb_convert_encoding($str, "SJIS-win", "UTF-8");
//    $str = mb_convert_encoding($str, "jis", "SJIS-win");

    return $str;
} 
//ファイルダウンロード
function downloadContents2($contents, $filename, $encode="SJIS-win"){
    $filename = mb_convert_encoding($filename, "SJIS-win", mb_internal_encoding());
    $contents = mb_convert_encoding($contents, $encode, mb_internal_encoding());

    /* HTTPヘッダの出力 */
    mb_http_output("pass");
    header("Accept-Ranges: none");
    header("Content-Disposition: inline; filename=$filename");
    header("Content-Transfer-Encoding: binary");
    header("Content-Length: ". strlen($contents) );
//    header("Content-Type: text/plain");
    header("Content-Type: application/octet-stream");
//    header("Content-Type: application/pdf");

    echo $contents;
//    readfile($contents);
}
?>
