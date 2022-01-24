<?php
/**
 * 定数定義
*/
class knjp742bModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    public function knjp742bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->seq        = '001'; // 現状は"001"固定（2019.04.04）

        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        $this->field = array("INPUT"            =>  VARS::post("INPUT"),             //取込種別（1:引落し, 2:返金）
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),       //校種
                             "BANK_CD"          =>  VARS::post("BANK_CD"),           //取扱銀行
                             "PAID_MONTH"       =>  VARS::post("PAID_MONTH"),        //引落し月
                             "HEADER"           =>  VARS::post("HEADER")             //ヘッダ有
                             );

        //初期値セット
        $this->field["INPUT"] = ($this->field["INPUT"] == "") ? "1" : $this->field["INPUT"];

        $db = Query::dbCheckOut();
        //学校銀行情報取得
        $this->bankRow = array();
        $query = knjp742bQuery::getCollectSchoolBankMst($this);
        $this->bankRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->printSyubetsu   = $this->bankRow["SHUBETSU_CD"];       // 種別コード
        $this->bankTransferFee = $this->bankRow["BANK_TRANSFER_FEE"]; // 手数料

        //顧客番号取得
        $this->customerArr = array();
        $query = knjp742bQuery::getCustomerDat();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->customerArr[$row["SCHREGNO"]] = $row["CUSTOMER_NUMBER"];
        }
        //校種取得
        $this->schKindArr = array();
        $query = knjp742bQuery::getSchoolKindSch();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->schKindArr[$row["CUSTOMER_NUMBER"]] = $row["SCHOOL_KIND"];
        }
        //引落予定金額取得
        $this->planMoneyArr = array();
        $query = knjp742bQuery::getPlanCsvDat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key1 = $row["SCHOOL_KIND"];
            $key2 = sprintf("%07d", $row["ACCOUNTNO"]);
            $key3 = $row["ACCOUNTNAME"];
            $key4 = $row["CUSTOMER_NUMBER"];
            $key5 = $row["PLAN_MONTH"];
            $key6 = $row["PLAN_MONEY"];
            $setKey = $key1.'-'.$key2.'-'.$key3.'-'.$key4.'-'.$key5.'-'.$key6;
            $this->planMoneyArr[$setKey] = $row["PAID_FLG"];
        }
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->file = VARS::file("FILE");

        $this->keiyakuSyubetsu    = ($this->field["INPUT"] == "1") ? "30": "09";
    }

    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        //エラー
        $contents .= implode($header_err, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //エラー
        $query = knjp742bQuery::selectCsvErrQuery();

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $array = $header_err;

            //ヘッダ順に値をセット
            $comma = "";
            foreach ($array as $key => $headerName) {
                $contents .= $comma.$row[$key];
                $comma = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ＣＳＶ取込
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $cnt = knjp742bQuery::insertQueryCsv($this, $this->data_arr);

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $misyori = $this->paidCnt + $errcnt;
            $this->setMessage(MSG202, "\\n【CSVデータ件数】：{$this->totalCnt}件\\n\\n【正常取込件数】 ：{$cnt}件\\n\\n【未処理件数】    ：{$misyori}件\\n  〇入金済件数  ：{$this->paidCnt}件\\n  〇エラー件数     ：{$errcnt}件");
        }
    }

    //取込
    public function GetFileString()
    {
        $temp_arr = array();
        $this->totalCnt = 0;
        $this->paidCnt = 0;
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata["RECORD_DATA"]      = array(120,    "HISSU",    "",   "レコードデータ");
            $checkdata["RECORD_DIV"]       = array(1,      "",         "",   "レコード区分");
            $checkdata["PLAN_MONEY"]       = array(10,     "",         "",   "請求金額");
            $checkdata["CUSTOMER_NUMBER"]  = array(20,     "",         "",   "顧客番号");
            $checkdata["REQUEST_GK"]       = array(10,     "",         "",   "返金額");
            $checkdata["RESULT_CD"]        = array(1,      "",         "",   "振替結果");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjp742bQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp);
                $startIdx = 0;
                if ($buffer != '') {
                    //文字コードを判別、改行コードを削除
                    $buffer = i18n_convert($buffer, i18n_http_output(), "SJIS-win");

                    // 120文字数毎に切り出して処理していく(改行コード分を引いておく)
                    for ($j=0; $j < mb_strlen($buffer) - 2; $j += 120) {
                        $tmp = mb_substr($buffer, $startIdx, 120);
                        $startIdx += 120;

                        //入金フォーマット
                        if ($this->field["INPUT"] == "1") {
                            $data["RECORD_DATA"]            = $tmp;
                            $data["RECORD_DIV"]             = mb_substr($tmp, 0, 1); // データ区分
                            $data["DATA_DIV"]               = mb_substr($tmp, 0, 1); // データ区分

                            // ヘッダー・レコード
                            if ($data["RECORD_DIV"] == "1") {
                                $data["TYPE_CD"]            = mb_substr($tmp, 1, 2); // 種別コード
                                $data["CD_DIV"]             = mb_substr($tmp, 3, 1); // コード区分
                                $data["CLIENT_CD"]          = mb_substr($tmp, 4, 10); // 依頼人コード
                                $data["CLIENT_NAME"]        = mb_substr($tmp, 14, 40); // 委託業者
                                $data["PADE_DATE"]          = mb_substr($tmp, 54, 4); // 引落し日
                                $data["T_BANKCD"]           = mb_substr($tmp, 58, 4); // 取引銀行番号
                                $data["T_BANKNAME"]         = mb_substr($tmp, 62, 15); // 取引銀行名
                                $data["T_BRANCHCD"]         = mb_substr($tmp, 77, 3); // 取引支店番号
                                $data["T_BRANCHNAME"]       = mb_substr($tmp, 80, 15); // 取引支店名
                                $data["DEPOSIT_DIV"]        = mb_substr($tmp, 95, 1); // 預金種目
                                $data["T_ACCOUNTNO"]        = mb_substr($tmp, 96, 7); // 口座番号
                                $data["DUMMY"]              = mb_substr($tmp, 103, 17); // ダミー

                                //契約種別コードが一致しているか
                                if ($data["TYPE_CD"] != $this->printSyubetsu) {
                                    $this->warning = "処理不可データです。\\n入金データを確認して下さい。\\n※契約種別コード不一致";
                                    return;
                                }

                                // 引落日(ヘッダーデータから取得)
                                $padeDate = $data["PADE_DATE"];

                                //銀行コードが一致しているか
                                if ($data["T_BANKCD"] != $this->field["BANK_CD"]) {
                                    $this->warning = "処理不可データです。\\n入金データを確認して下さい。\\n※銀行コード不一致";
                                    return;
                                }

                                // データ・レコード
                            } elseif ($data["RECORD_DIV"] == "2") {
                                $data["BANKCD"]                 = mb_substr($tmp, 1, 4); // 引落銀行番号
                                $data["BANKNAME"]               = mb_substr($tmp, 5, 15); // 引落銀行名
                                $data["BRANCHCD"]               = mb_substr($tmp, 20, 3); // 引落支店番号
                                $data["BRANCHNAME"]             = mb_substr($tmp, 23, 15); // 引落支店名
                                $data["DUMMY1"]                 = mb_substr($tmp, 38, 4); // ダミー
                                $data["DEPOSIT_ITEM"]           = mb_substr($tmp, 42, 1); // 預金種目
                                $data["ACCOUNTNO"]              = mb_substr($tmp, 43, 7); // 通常貯金番号
                                $data["ACCOUNTNAME"]            = mb_substr($tmp, 50, 30); // 氏名
                                $data["PLAN_MONEY"]             = (int)mb_substr($tmp, 80, 10); // 請求金額
                                $data["SHINKI_CD"]              = mb_substr($tmp, 90, 1); // 新規コード
                                $data["CUSTOMER_NUMBER"]        = mb_substr($tmp, 91, 20); // 顧客番号
                                $data["RESULT_CD"]              = mb_substr($tmp, 111, 1); // 振替結果コード
                                $data["DUMMY2"]                 = mb_substr($tmp, 112, 8); // ダミー
                                if ($this->Properties["IncludeBankTransferFee"] == "1") {
                                    $data["PLAN_MONEY"] -= $this->bankTransferFee;
                                }
                                $this->totalCnt++;

                            // トレーラ・レコード
                            } elseif ($data["RECORD_DIV"] == "8") {
                                $data["TOTAL_CNT"]          = mb_substr($tmp, 1, 6); // 合計件数
                                $data["TOTAL_MONEY"]        = mb_substr($tmp, 7, 12); // 合計金額
                                $data["TRANSFER_CNT"]       = mb_substr($tmp, 19, 6); // 振替済件数
                                $data["TRANSFER_MONEY"]     = mb_substr($tmp, 25, 12); // 振替済金額
                                $data["NOT_TRANSFER_CNT"]   = mb_substr($tmp, 37, 6); // 振替不能件数
                                $data["NOT_TRANSFER_MONEY"] = mb_substr($tmp, 43, 12); // 振替不能金額
                                $data["DUMMY"]              = mb_substr($tmp, 55, 65); // ダミー

                                $totalDataRecCnt = $data["TOTAL_CNT"];
                                $totaldataMoney  = $data["TOTAL_MONEY"];

                            // エンド・レコード
                            } elseif ($data["RECORD_DIV"] == "9") {
                                $data["DUMMY"]          = mb_substr($tmp, 1, 119); // ダミー
                            }

                            $data["PAID_DATE"]    = $padeDate;
                            $data["DIRECT_DEBIT"] = $padeDate; //各テーブルのキー

                        //返金フォーマット
                        } else {
                            $data["RECORD_DATA"]            = $tmp[0];
                            $data["RECORD_DIV"]             = mb_substr($tmp[0], 0, 1); // レコード区分
                            $data["BANKCD"]                 = mb_substr($tmp[0], 1, 4); // 金融機関コード
                            $data["BANKNAME1"]              = mb_substr($tmp[0], 5, 15); // 金融機関名１
                            $data["BRANCHCD"]               = mb_substr($tmp[0], 20, 3); // 通常貯金記号(店番)
                            $data["BANKNAME2"]              = mb_substr($tmp[0], 23, 15); // 金融機関名２
                            $data["TEGATANO"]               = mb_substr($tmp[0], 38, 4); // 手形交換所番号
                            $data["YOKINSYUMOKU"]           = mb_substr($tmp[0], 42, 1); // 預金種目
                            $data["ACCOUNTNO"]              = mb_substr($tmp[0], 43, 7); // 通常貯金番号
                            $data["ACCOUNTNAME"]            = mb_substr($tmp[0], 50, 30); // 氏名
                            $data["REQUEST_GK"]             = mb_substr($tmp[0], 80, 10); // 送金金額
                            $data["SINKI_CD"]               = mb_substr($tmp[0], 90, 1); // 新規コード
                            $data["GRADE"]                  = mb_substr($tmp[0], 91, 2); // 顧客番号（学年）
                            $data["HR_CLASS"]               = mb_substr($tmp[0], 93, 3); // 顧客番号（組）
                            $data["ATTENDNO"]               = mb_substr($tmp[0], 96, 3); // 顧客番号（番号）
                            $data["SCHREGNO"]               = mb_substr($tmp[0], 99, 8); // 顧客番号（学籍番号）
                            $data["PLAN_YEAR_MONTH"]        = mb_substr($tmp[0], 107, 4); // 顧客番号（月日）
                            $data["RESULT_CD"]              = mb_substr($tmp[0], 111, 1); // 振替結果コード
                            $data["YOBI"]                   = mb_substr($tmp[0], 112, 6); // 予備
                            $data["PLAN_MONTH"]             = mb_substr($tmp[0], 118, 2); // 補助文言表示

                            if ($data["RECORD_DIV"] == "1") {
                                $setHenkinDate = mb_substr($tmp[0], 54, 4); // 取扱月日
                                if ($this->keiyakuSyubetsu != mb_substr($tmp[0], 1, 2)) {//契約種別コードが一致しているか
                                    $this->warning = "処理不可データです。\\n返金データか確認して下さい。";
                                    return;
                                }
                            }
                            $data["HENKIN_DATE"] = $setHenkinDate;
                        }

                        //エラーチェック
                        if ($this->errCheck($db, $data, $checkdata, $line)) {
                            $temp_arr[$i] = $data;
                            $i++;
                        }
                        $line++;
                    }
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg       = true;
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
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //2:データレコードのみ、チェック。※1:ヘッダーレコード 8:トレーラレコード 9:エンドレコード
            if ($data["RECORD_DIV"] == "2") {
                /********************/
                /**  存在チェック  **/
                /********************/
                //入金フォーマット
                if ($this->field["INPUT"] == "1") {
                    if ($key == 'RESULT_CD') {
                        if ($data[$key] != '0') {
                            if ($data[$key] == '1') {
                                $setMessage = "資金不足";
                            } elseif ($data[$key] == '2') {
                                $setMessage = "口座なし";
                            } else {
                                $setMessage = "その他({$data[$key]})";
                            }
                            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※", $otherSep, $setMessage);
                        }
                    }
                    //データの金額がCSV_DATに存在するか
                    if (($key == "PLAN_MONEY")) {
                        $key1 = $this->schKindArr[$data["CUSTOMER_NUMBER"]];
                        $key2 = $data["ACCOUNTNO"];
                        $key3 = rtrim($data["ACCOUNTNAME"]);
                        $key4 = $data["CUSTOMER_NUMBER"];
                        $key5 = ($this->field["PAID_MONTH"]) ? $this->field["PAID_MONTH"] : mb_substr($data["PAID_DATE"], 0, 2);
                        $key6 = $data["PLAN_MONEY"];
                        $chkKey = $key1.'-'.$key2.'-'.$key3.'-'.$key4.'-'.$key5.'-'.$key6;
                        if (!array_key_exists($chkKey, $this->planMoneyArr)) {
                            $setMessage = "引落予定金額データ";
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $setMessage);
                        } elseif ($this->planMoneyArr[$chkKey] == '1') {
                            //入金済みの場合読み飛ばす
                            $this->paidCnt++;
                            return false;
                        }
                    }
                    //顧客番号が存在するか
                    if (($key == "CUSTOMER_NUMBER")) {
                        if (!in_array($data["CUSTOMER_NUMBER"], $this->customerArr, true)) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]."({$data["CUSTOMER_NUMBER"]})");
                        }
                    }
                } else {
                    //データと返金伝票の金額が一致しているか
                    if ($key == "REQUEST_GK") {
                        $chkMoney = sprintf("%010d", $db->getOne(knjp742bQuery::checkHenkinDat($this, $data)));
                        if ($data[$key] != $chkMoney) {
                            $setMessage = "返金予定額（{$chkMoney}）　"."返金額（{$data[$key]}）";
                            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※金額不一致：", $otherSep, $setMessage);
                        }
                    }
                }
            }
        }
        if (!$returnFlg) {
            $check_error = $data["CUSTOMER_NUMBER"].$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjp742bQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }
}
