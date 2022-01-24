<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx230Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $fieldSize = array();
    public $candidate = array();

    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx230Model()
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

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "STUDENT"          =>  VARS::post("STUDENT"),           //生徒一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");

        $this->file = VARS::file("FILE");

        //タイトル設定
        $db = Query::dbCheckOut();
        $result = $db->query(knjx230Query::getSickDiv());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["TITLE_".$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //ヘッダ候補
        $this->candidate = array();
        $this->fieldSize = array();
        $this->candidate["APPOINTED_DAY"]   = "指定日";
        $this->candidate["LESSON"]          = "授業日数";
        $this->candidate["OFFDAYS"]         = "休学日数";
        $this->candidate["ABROAD"]          = "留学日数";
        $this->candidate["ABSENT"]          = "公欠日数";
        $this->candidate["SUSPEND"]         = $arg["TITLE_2"];
        if ($this->Properties["useVirus"] == 'true') {
            $this->candidate["VIRUS"]       = $arg["TITLE_19"];
        }
        if ($this->Properties["useKoudome"] == 'true') {
            $this->candidate["KOUDOME"]     = $arg["TITLE_25"];
        }
        $this->candidate["MOURNING"]        = "忌引";
        $this->candidate["SICK"]            = $arg["TITLE_4"];
        $this->candidate["NOTICE"]          = $arg["TITLE_5"];
        $this->candidate["NONOTICE"]        = $arg["TITLE_6"];
        $this->candidate["LATE"]            = "遅刻";
        $this->candidate["EARLY"]           = "早退";

        $this->fieldSize["APPOINTED_DAY"]   = "2";
        $this->fieldSize["LESSON"]          = "2";
        $this->fieldSize["OFFDAYS"]         = "2";
        $this->fieldSize["ABROAD"]          = "2";
        $this->fieldSize["ABSENT"]          = "2";
        $this->fieldSize["SUSPEND"]         = "2";
        if ($this->Properties["useVirus"] == 'true') {
            $this->fieldSize["VIRUS"]       = "2";
        }
        if ($this->Properties["useKoudome"] == 'true') {
            $this->fieldSize["KOUDOME"]     = "2";
        }
        $this->fieldSize["MOURNING"]        = "2";
        $this->fieldSize["SICK"]            = "2";
        $this->fieldSize["NOTICE"]          = "2";
        $this->fieldSize["NONOTICE"]        = "2";
        $this->fieldSize["LATE"]            = "2";
        $this->fieldSize["EARLY"]           = "2";

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx230Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx230Query::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("COPYCD"         => array(1,"複写区分"),
                               "YEAR"           => array(4,"年度"),
                               "MONTH"          => array(2,"対象月"),
                               "SEMESTER"       => array(1,"学期"),
                               "SCHREGNO"       => array(8,"学籍番号"));

            //対象項目のみデータに追加する。
            foreach ($this->fieldSize as $key => $val) {
                $checkdata[$key] = array($this->fieldSize[$key],$this->candidate[$key]);
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey("ATTEND_SEMES_DAT");
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
                    $data = array("COPYCD"           => (strlen(trim($tmp[4]))) ? sprintf("%01d", $tmp[4]) : "",
                                  "YEAR"             => (strlen(trim($tmp[5]))) ? sprintf("%04d", $tmp[5]) : "",
                                  "MONTH"            => (strlen(trim($tmp[6]))) ? sprintf("%02d", $tmp[6]) : "",
                                  "SEMESTER"         => (strlen(trim($tmp[7]))) ? sprintf("%01d", $tmp[7]) : "",
                                  "SCHREGNO"         => (strlen(trim($tmp[8]))) ? sprintf("%08d", $tmp[8]) : "");
                    $tmpcnt = 9;
                    //対象項目のみデータに追加する。
                    foreach ($this->fieldSize as $key => $val) {
                        $data[$key] = $tmp[$tmpcnt];
                        $tmpcnt++;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
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

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg     = true;
        $hissuErrFlg   = false;    //必須エラーフラグ
        $numericErrFlg = false;    //数値エラーフラグ
        $lengthErrFlg  = false;    //レングスエラーフラグ
        $valuesErrFlg  = false;
        $outErrFlg     = false;
        $duplicateCsvErrFlg = false;

        $hissuErrMsg   = "";       //必須エラーメッセージ
        $numericErrMsg = "";       //数値エラーメッセージ
        $lengthErrMsg  = "";       //レングスエラーメッセージ
        $valuesErrMsg  = "";
        $outErrMsg     = "";
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $valuesSep    = "";
        $outSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("COPYCD","YEAR","MONTH","SEMESTER","SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("COPYCD","YEAR","MONTH","SEMESTER","SCHREGNO","APPOINTED_DAY","LESSON","OFFDAYS","ABROAD","ABSENT","SUSPEND","VIRUS","KOUDOME","MOURNING","SICK","NOTICE","NONOTICE","LATE","EARLY"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //数字チェック
            if (in_array($key, array("COPYCD"))) {
                if ($data[$key] != "0") {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }

            //範囲チェック
            if (in_array($key, array("MONTH"))) {
                if ($data[$key] < 1 || $data[$key] > 12) {
                    $returnFlg = $this->errSet($outErrFlg, $outErrMsg, "※範囲外:", $outSep, $val[1]);
                }
            }
            if (in_array($key, array("APPOINTED_DAY"))) {
                if ($data[$key] < 1 || $data[$key] > 31) {
                    $returnFlg = $this->errSet($outErrFlg, $outErrMsg, "※範囲外:", $outSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$outErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx230Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }


    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        //ヘッダデータ
        $header = array("GRADE"             => "学年",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "COPYCD"            => "※複写区分",
                        "YEAR"              => "※年度",
                        "MONTH"             => "※対象月",
                        "SEMESTER"          => "※学期",
                        "SCHREGNO"          => "※学籍番号");

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize as $key => $val) {
            $header[$key] = $this->candidate[$key];
        }
        $header[$this->lastColumn] = $this->lastColumn;

        $header_rei = array("GRADE"         => "99",
                            "HR_CLASS"      => "999",
                            "ATTENDNO"      => "999",
                            "NAME"          => "アルプ太郎",
                            "COPYCD"        => "0",
                            "YEAR"          => "9999",
                            "MONTH"         => "99",
                            "SEMESTER"      => "9",
                            "SCHREGNO"      => "12345678");

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize as $key => $val) {
            $header_rei[$key] = "99";
        }
        $header_rei[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $tables = array();
            $tables = array("1" => "ATTEND_SEMES_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx230Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "出欠情報データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}