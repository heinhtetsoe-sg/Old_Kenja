<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjp726Model extends Model {
    var $cmd;
    var $control;
    var $file;
    var $field = array();
    var $data_arr = array();
    var $z010Name1;

    /**
     * メンバ変数の初期化
     */
    function knjp726Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->maxSeq = 2;
        $this->cmd = VARS::request("cmd");
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR"         =>  VARS::post("GRADE_HR"),         //年組
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knjp726Query::updateQueryCsv($this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $db = Query::dbCheckOut();

            $checkdata["SCHREGNO"]              = array(8,    "HISSU",  "INT2",  "学籍番号");
            $checkdata["BANKCD"]                = array(4,    "HISSU",  "INT2",  "銀行コード");
            $checkdata["BRANCHCD"]              = array(3,    "HISSU",  "INT2",  "支店コード");
            $checkdata["DEPOSIT_ITEM"]          = array(1,    "HISSU",   "INT",  "預金種目");
            $checkdata["ACCOUNTNO"]             = array(7,    "HISSU",  "INT2",  "口座番号");
            $checkdata["ACCOUNTNAME"]           = array(120,  "HISSU",      "",  "口座名義");
            $checkdata["RELATIONSHIP"]          = array(2,         "",   "INT",  "続柄");
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();

            $primaryKey1[] = "SCHREGNO1";

            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $tmpCnt = 2;    //年組番が1列目,氏名が2列目
                    $data = array();
                    $data["SCHREGNO1"]              = $tmp[$tmpCnt++];
                    $data["BANKCD1"]                = $tmp[$tmpCnt++];
                    $data["BANKNAME1"]              = $tmp[$tmpCnt++];
                    $data["BRANCHCD1"]              = $tmp[$tmpCnt++];
                    $data["BRANCHNAME1"]            = $tmp[$tmpCnt++];
                    $data["DEPOSIT_ITEM1"]          = $tmp[$tmpCnt++];
                    $data["ACCOUNTNO1"]             = $tmp[$tmpCnt++];
                    $data["ACCOUNTNAME1"]           = $tmp[$tmpCnt++];
                    $data["RELATIONSHIP1"]          = $tmp[$tmpCnt++];
                    $data["UPD_FLG1"]               = false;
                    $data["SCHREGNO2"]              = $tmp[2];
                    $data["BANKCD2"]                = $tmp[$tmpCnt++];
                    $data["BANKNAME2"]              = $tmp[$tmpCnt++];
                    $data["BRANCHCD2"]              = $tmp[$tmpCnt++];
                    $data["BRANCHNAME2"]            = $tmp[$tmpCnt++];
                    $data["DEPOSIT_ITEM2"]          = $tmp[$tmpCnt++];
                    $data["ACCOUNTNO2"]             = $tmp[$tmpCnt++];
                    $data["ACCOUNTNAME2"]           = $tmp[$tmpCnt++];
                    $data["RELATIONSHIP2"]          = $tmp[$tmpCnt++];
                    $data["UPD_FLG2"]               = false;

                    //項目数によるファイルチェック
                    if ($checkcount != $tmpCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    $checkSeq = array();
                    for ($seq = 1; $seq <= $this->maxSeq; $seq++) {
                        if (strlen($data["BANKCD{$seq}"]) > 0 ||
                            strlen($data["BRANCHCD{$seq}"]) > 0 ||
                            strlen($data["DEPOSIT_ITEM{$seq}"]) > 0 ||
                            strlen($data["ACCOUNTNO{$seq}"]) > 0 ||
                            strlen($data["ACCOUNTNAME{$seq}"]) > 0 ||
                            strlen($data["RELATIONSHIP{$seq}"]) > 0
                        ) {
                            $data["UPD_FLG{$seq}"] = true;
                            $checkSeq[$seq] = $seq;
                        }
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $checkSeq, $line, $duplicateCsv1, $primaryKey1)) {
                        foreach ($checkSeq as $seq => $seqVal) {
                            foreach ($checkdata as $key => $val) {
                                //ゼロ埋めしてセット
                                if ($val[2] == "INT2" && $data[$key.$seq] != "" && is_numeric($data[$key.$seq])) {
                                    $data[$key.$seq] = sprintf("%0{$val[0]}d", $data[$key.$seq]);
                                }
                            }
                        }
                        $temp_arr[$i] = $data;
                        $i++;
                    }

                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }

                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    /********************/
    /*  エラーチェック  */
    /********************/
    function errCheck($db, $data, $checkdata, $checkSeq, $line, $duplicateCsv1, $primaryKey1) {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $masterErrFlg       = false;    //マスタチェックエラーフラグ
        $valuesErrFlg       = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $masterErrMsg       = "";       //マスタチェックエラーメッセージ
        $valuesErrMsg       = "";

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";
        $masterErrSep       = "";
        $valuesSep          = "";

        $checkNameMstCd = array("DEPOSIT_ITEM"      => "G203",
                                "RELATIONSHIP"      => "H201"
                                );

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($checkSeq as $seq => $seqVal) {
            foreach ($checkdata as $key => $val) {
                $errLabel = $key == 'SCHREGNO' ? $val[3] : $val[3].$seq;
                //必須項目チェック
                if ($val[1] == "HISSU" && !strlen(trim($data[$key.$seq]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $errLabel);
                }
    
                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key.$seq] != "" && !is_numeric($data[$key.$seq])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $errLabel);
                }
                //チェック用にゼロ埋め
                if ($val[2] == "INT2" && $data[$key.$seq] != "" && is_numeric($data[$key.$seq])) {
                    $data[$key.$seq] = sprintf("%0{$val[0]}d", $data[$key.$seq]);
                }
    
                //存在チェック（銀行マスタ）
                if ($key == "BANKCD" && strlen($data["BANKCD{$seq}"]) > 0 && strlen($data["BRANCHCD{$seq}"]) > 0) {
                    $bankCd = sprintf("%04d", $data["BANKCD{$seq}"]);
                    $branchCd = sprintf("%03d", $data["BRANCHCD{$seq}"]);
                    $query = knjp726Query::bankCntSql($bankCd, $branchCd);
                    $rowCount = $db->getOne($query);
                    if ($rowCount == "0") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterErrSep, "銀行/支店コード".$seq);
                    }
                }
    
                //存在チェック（名称マスタ）
                if ($checkNameMstCd[$key] && $data[$key.$seq] != "") {
                    $query = knjp726Query::nameCntSql($checkNameMstCd[$key], $data[$key.$seq]);
                    $rowCount = $db->getOne($query);
                    if ($rowCount == "0") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterErrSep, $errLabel);
                    }
                }
                //フラグチェック(値は1かNULL)
                if ($val[1] == "FLG" && $data[$key.$seq] != "" && $data[$key.$seq] != "1") {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $errLabel);
                }
    
                //文字数チェック
                if (strlen($data[$key.$seq]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $errLabel);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjp726Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning(MSG915,"処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //CSV出力(ヘッダ)
    function OutputDataFileHead()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ
        $contents .= implode(",", $this->getHeadArray($db))."\n";
        //ヘッダ例
        $contents .= implode(",", $this->getExampleArray($db))."\n";

        //DB切断
        Query::dbCheckIn($db);

        common::downloadContents($contents, "生徒銀行データ(ヘッダ).csv");

        return true;
    }

    //CSV出力(エラー)
    function OutputDataFileError()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        $tables = array("1" => "REGISTBANK_DAT");
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        $header = array();
        $header["SCHOOLCD"] = "学校コード";
        $header["SCHREGNO"] = "学籍番号";
        $header["SEQ"] = "連番";
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $header);
            $contents .= implode($headerErr, ",")."\n";
        }
        //ヘッダ
        $contents .= "行番号, メッセージ\n";

        $header_data = false;
        //SQL文発行
        $query = $this->selectCsvErrQuery(PROGRAMID);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            common::downloadContents($contents, "生徒銀行データ取込みエラー.csv");

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    //CSV出力(データ)
    function OutputDataFile()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ
        $contents .= implode(",", $this->getHeadArray($db))."\n";

        $header_data = false;
        //SQL文発行
        $query = knjp726Query::getRegistBankData($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            common::downloadContents($contents, "生徒銀行データ.csv");

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    function getHeadArray($db) {
        $retHeadArray = array();
        $retHeadArray[] = "年組番";
        $retHeadArray[] = "氏名";
        $retHeadArray[] = "※学籍番号";
        for ($seq = 1; $seq <= $this->maxSeq; $seq++) {
            $setHissu = $seq == 1 ? "※ " : "";
            $retHeadArray[] = "{$setHissu}銀行コード{$seq}";
            $retHeadArray[] = "銀行名{$seq}";
            $retHeadArray[] = "{$setHissu}支店コード{$seq}";
            $retHeadArray[] = "支店名{$seq}";
            $retHeadArray[] = "{$setHissu}預金種目{$seq}";
            $retHeadArray[] = "{$setHissu}口座番号{$seq}";
            $retHeadArray[] = "{$setHissu}口座名義{$seq}";
            $retHeadArray[] = "続柄{$seq}";
        }
        return $retHeadArray;
    }

    //ヘッダ例
    function getExampleArray($db) {
        $retHeadArray = array();
        $retHeadArray[] = "1年1組001";
        $retHeadArray[] = "アルプ　太郎";
        $retHeadArray[] = "99999999";
        for ($seq = 1; $seq <= $this->maxSeq; $seq++) {
            $retHeadArray[] = "1234";
            $retHeadArray[] = "銀行名";
            $retHeadArray[] = "123";
            $retHeadArray[] = "支店名";
            $retHeadArray[] = "1";
            $retHeadArray[] = "1234567";
            $retHeadArray[] = "ﾊﾝｶｸ ｶﾀｶﾅ";
            $retHeadArray[] = "01";
        }

        return $retHeadArray;
    }
}
?>
