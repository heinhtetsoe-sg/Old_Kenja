<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp856Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $schoolCd;

    /**
     * メンバ変数の初期化
     */
    public function knjp856Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->year     = CTRL_YEAR;
        $this->lastYear = $this->year - 1;
        $this->semester = CTRL_SEMESTER;
        $this->schoolCd = SCHOOLCD;
        
        $this->monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        $this->field = array("INPUT"            =>  VARS::post("INPUT"),             //取込種別（1:引落し, 2:返金）
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "INPUT_DIV"        =>  VARS::post("INPUT_DIV"),         //取込区分
                             "HEADER"           =>  VARS::post("HEADER")             //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }

        $this->file = VARS::file("FILE");

        //ヘッダ
        $this->header = array();
        $this->header["YEAR"]             = "※年度";
        $this->header["SCHOOL_KIND"]      = "※校種コード";
        $this->header["SCHOOL_KIND_NAME"] = "校種";
        $this->header["GRADE"]            = "学年";
        $this->header["HR_CLASS"]         = "組";
        $this->header["ATTENDNO"]         = "出席番号";
        $this->header["SCHREGNO"]         = "※学籍番号";
        $this->header["NAME"]             = "生徒氏名";
        $this->header["INCOME_1"]         = "所得割額計(前年)";
        $this->header["INCOME_1_1"]       = "市町村所得割1(前年)";
        $this->header["INCOME_1_2"]       = "都道府県所得割1(前年)";
        $this->header["INCOME_1_3"]       = "市町村所得割2(前年)";
        $this->header["INCOME_1_4"]       = "都道府県所得割2(前年)";
        $this->header["INCOME_2"]         = "所得割額計(今年)";
        $this->header["INCOME_2_1"]       = "市町村所得割1(今年)";
        $this->header["INCOME_2_2"]       = "都道府県所得割1(今年)";
        $this->header["INCOME_2_3"]       = "市町村所得割2(今年)";
        $this->header["INCOME_2_4"]       = "都道府県所得割2(今年)";
        $this->header[$this->lastColumn]  = $this->lastColumn;

        //DB接続
        $db = Query::dbCheckOut();

        //名称マスタA023取得
        $this->nameMstA023 = array();
        $query  = knjp856Query::getNameMst($this, "A023");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->nameMstA023[] = $row["NAME1"];
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    public function &getMainModel()
    {
    }

    //ヘッダ出力
    public function getDownloadHeaderModel()
    {
        //ヘッダ例
        $header_rei = array();
        $header_rei["YEAR"]             = $this->year;
        $header_rei["SCHOOL_KIND"]      = "H";
        $header_rei["SCHOOL_KIND_NAME"] = "高校";
        $header_rei["GRADE"]            = "01";
        $header_rei["HR_CLASS"]         = "001";
        $header_rei["ATTENDNO"]         = "001";
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["NAME"]             = "アルプ太郎";
        $header_rei["INCOME_1"]         = "(金額)";
        $header_rei["INCOME_1_1"]       = "(金額)";
        $header_rei["INCOME_1_2"]       = "(金額)";
        $header_rei["INCOME_1_3"]       = "(金額)";
        $header_rei["INCOME_1_4"]       = "(金額)";
        $header_rei["INCOME_2"]         = "(金額)";
        $header_rei["INCOME_2_1"]       = "(金額)";
        $header_rei["INCOME_2_2"]       = "(金額)";
        $header_rei["INCOME_2_3"]       = "(金額)";
        $header_rei["INCOME_2_4"]       = "(金額)";
        $header_rei[$this->lastColumn]  = $this->lastColumn;

        $csvObj = new OutputCsvModel();

        //出力にヘッダを追加
        $csvObj->addHeaderToCsv($this->header);

        //CSVに1行追加
        $csvObj->addRowToCsv($header_rei);

        //出力
        $result = $csvObj->outputCsv("所得割額登録");
        if (!$result) {
            $this->warning = "該当データは、ありません。";
        }

        return $result;
    }

    //エラー出力
    public function getDownloadErrorModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err["MSGROW"]    = "行番号";
        $header_err["MSGREMARK"] = "メッセージ";

        $csvObj = new OutputCsvModel();

        //CSVにヘッダを追加
        $csvObj->addHeaderToCsv($header_err);

        //CSVにSQL取得データを追加
        $selectFields = array_keys($header_err);
        $query  = knjp856Query::selectCsvErrQuery();
        $csvObj->addQueryDataToCsv($db, $selectFields, $query);

        //出力
        $result = $csvObj->outputCsv("エラー出力");
        if (!$result) {
            $this->warning = "該当データは、ありません。";
        }

        //DB切断
        Query::dbCheckIn($db);

        return $result;
    }

    //ＣＳＶ取込
    public function &getExecModel()
    {
        //CSVファイルを取り込み、配列に格納
        $data_arr = array();
        $data_arr = $this->GetFileString();

        //取込データがあるかチェック
        if (!$data_arr) {
            return;
        }

        //重複行があるかチェック
        if ($this->chkDuplicateRow($data_arr)) {
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //エラーデータの削除
        $query = knjp856Query::deleteQueryErr();
        $db->query($query);

        //チェック済み配列を取得
        $checked_data_arr = $this->getCheckedData($db, $data_arr);

        //データの整合性チェック
        if ($this->IsDataMatched($checked_data_arr)) {
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");

            $cnt = 0;
            if ($errcnt == 0) {
                $cnt = knjp856Query::insertQueryCsv($db, $this, $checked_data_arr);
            }

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }

        //DB切断
        $db->commit();
        Query::dbCheckIn($db);
    }

    //取込
    public function GetFileString()
    {
        $temp_arr = array();

        if (!is_uploaded_file($this->file["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
            return null;
        }

        $fp = fopen($this->file["tmp_name"], "r");
        $line = 1;  //行数
        $header = false;
        while (!feof($fp)) {
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

                //項目数によるファイルチェック
                $checkcount = 19;
                $dataCnt = get_count($tmp);
                if ($dataCnt != $checkcount) {
                    $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                    return null;
                }

                $colCnt = 0;
                $data["YEAR"]               = $tmp[$colCnt++];
                $data["SCHOOL_KIND"]        = $tmp[$colCnt++];
                $data["SCHOOL_KIND_NAME"]   = $tmp[$colCnt++];
                $data["GRADE"]              = sprintf("%02d", $tmp[$colCnt++]);
                $data["HR_CLASS"]           = sprintf("%03d", $tmp[$colCnt++]);
                $data["ATTENDNO"]           = sprintf("%03d", $tmp[$colCnt++]);
                $data["SCHREGNO"]           = sprintf("%08d", $tmp[$colCnt++]);
                $data["NAME"]               = $tmp[$colCnt++];
                $data["INCOME_1"]           = $tmp[$colCnt++];
                $data["INCOME_1_1"]         = $tmp[$colCnt++];
                $data["INCOME_1_2"]         = $tmp[$colCnt++];
                $data["INCOME_1_3"]         = $tmp[$colCnt++];
                $data["INCOME_1_4"]         = $tmp[$colCnt++];
                $data["INCOME_2"]           = $tmp[$colCnt++];
                $data["INCOME_2_1"]         = $tmp[$colCnt++];
                $data["INCOME_2_2"]         = $tmp[$colCnt++];
                $data["INCOME_2_3"]         = $tmp[$colCnt++];
                $data["INCOME_2_4"]         = $tmp[$colCnt++];
                $data[$this->lastColumn]    = $tmp[$colCnt++];

                $temp_arr[$line] = $data;
            }
            $line++;
        }
        fclose($fp);

        return $temp_arr;
    }

    //重複チェック
    public function chkDuplicateRow($data_arr)
    {
        $dupChkRow = array();
        foreach ($data_arr as $line => $data) {
            $duplicateKey = $data["YEAR"]."-".$data["SCHREGNO"]."-".$data["SCHOOL_KIND"];
            if (!array_key_exists($duplicateKey, $dupChkRow)) {
                $dupChkRow[$duplicateKey] = array();
            }
            $dupChkRow[$duplicateKey][] = $line;
        }
        foreach ($dupChkRow as $duplicateKey => $lines) {
            if (get_count($lines) >= 2) {
                $this->warning = "重複している行があります。(" . implode(", ", $lines) . "行目)";
                return true;
            }
        }

        return false;
    }

    //CSV取込データのエラーチェック済み配列を返す
    public function getCheckedData($db, &$data_arr)
    {
        //エラーチェック
        $checkdata = array();
        $checkdata["YEAR"]           = array(4, "HISSU",  "INT",   "年度");
        $checkdata["SCHOOL_KIND"]    = array(2, "HISSU",  "",      "校種コード");
        $checkdata["SCHREGNO"]       = array(8, "HISSU",  "INT2",  "学籍番号");
        $checkdata["INCOME_1"]       = array(10, "",      "INT",   "所得割額計(前年)");
        $checkdata["INCOME_1_1"]     = array(10, "",      "INT",   "市町村所得割1(前年)");
        $checkdata["INCOME_1_2"]     = array(10, "",      "INT",   "都道府県所得割1(前年)");
        $checkdata["INCOME_1_3"]     = array(10, "",      "INT",   "市町村所得割2(前年)");
        $checkdata["INCOME_1_4"]     = array(10, "",      "INT",   "都道府県所得割2(前年)");
        $checkdata["INCOME_2"]       = array(10, "",      "INT",   "所得割額計(今年)");
        $checkdata["INCOME_2_1"]     = array(10, "",      "INT",   "市町村所得割1(今年)");
        $checkdata["INCOME_2_2"]     = array(10, "",      "INT",   "都道府県所得割1(今年)");
        $checkdata["INCOME_2_3"]     = array(10, "",      "INT",   "市町村所得割2(今年)");
        $checkdata["INCOME_2_4"]     = array(10, "",      "INT",   "都道府県所得割2(今年)");

        $temp_arr = array();
        foreach ($data_arr as $line => $data) {
            //エラーチェック
            if ($this->errCheck($db, $data, $checkdata, $line)) {
                $temp_arr[$line] = $data;
            }
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, &$data, $data_each, $line)
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

            /********************/
            /**  存在チェック  **/
            /********************/
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }

            //存在チェック
            if ($key == "SCHOOL_KIND") {
                if (!in_array($data[$key], $this->nameMstA023)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
        }

        //学籍
        if (0 == $db->getOne(knjp856Query::getSchExistsCnt($data["YEAR"], $data["SCHOOL_KIND"], $data["SCHREGNO"]))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学籍データ");
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjp856Query::insertQueryErr($db, $line, $check_error);
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
        if (get_count($data_arr) > 5000) { //５千件を超えたら制限エラー
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //前年度確定コピー
    public function getCopyModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //前年度データが存在するかチェック
        $lastYearCnt = $db->getOne(knjp856Query::getLastYearReducDat($db, $this));
        if ($lastYearCnt == 0) {
            $this->setWarning("MSG203", "前年度に所得割額情報が1件も登録されていません。");
            return;
        }

        $query = knjp856Query::getCopyQuery($this);
        $result = $db->query($query);
        $cnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //更新
            knjp856Query::updateReductionIncomeDat($db, $this, $row, "INCOME_DIV_1");
            $cnt++;
        }

        if ($cnt > 0) {
            $this->setMessage("MSG202", "処理済件数　：".$cnt."件");
        } else {
            $this->setMessage("MSG303");
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}

/**
 * CSV出力用クラス
*/
class OutputCsvModel
{
    private $_contents = "";
    private $_dataFlg  = false;
    const HEADER_RECORD = 1;
    const DATA_RECORD   = 2;

    //ヘッダをCSVに追加
    public function addHeaderToCsv($headerArr)
    {
        $this->addCommonRowToCsv($headerArr, HEADER_RECORD);
    }

    //レコード1行をCSVに追加
    public function addRowToCsv($row)
    {
        $this->addCommonRowToCsv($row, DATA_RECORD);
    }

    private function addCommonRowToCsv($row, $recordType = "")
    {
        $this->_contents .= implode($row, ",")."\n";
        if ($recordType == DATA_RECORD) {
            $this->_dataFlg = true;
        }
    }

    //SQL取得データをCSVに追加
    public function addQueryDataToCsv($db, $fieldArr, $query)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $comma = "";
            foreach ($fieldArr as $key) {
                $this->_contents .= $comma.$row[$key];
                $comma = ",";
            }

            $this->_contents .= "\n";
            $this->_dataFlg = true;
        }
        $result->free();
    }

    //CSVデータ出力
    public function outputCsv($csvTitle)
    {
        if ($this->_dataFlg) {
            $filename = sprintf("%s.csv", $csvTitle);
            common::downloadContents($this->_contents, $filename);
            return true;
        } else {
            //出力データ無し
            return false;
        }
    }
}
