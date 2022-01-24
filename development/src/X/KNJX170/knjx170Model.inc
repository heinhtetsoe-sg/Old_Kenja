<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx170Model extends Model
{
    public $data     = array();
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $data_arr = array();
    public $cmd;
    public $file;
    public $field    = array();
    public $controls = array();
    public $headercheck;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx170Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        if (VARS::get("PROGRAMID")) {
            $this->headercheck  = "1";
        } else {
            $this->headercheck  = VARS::post("HEADER");
        }
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx170Query::insertQuery($this, $this->data_arr);
                } else {
                    knjx170Query::deleteQuery($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //ファイルの文字列の取得
    public function getFileString()
    {
        $temp_arr = array();
        if (is_uploaded_file($this->file["tmp_name"])) {
            //項目チェック用の配列
            $data_each = array( "GROUPCD"       => array(4,"群コード"),
                                "GROUPNAME"     => array(9,"群名称"),
                                "GROUPABBV"     => array(6,"群略称"),
                                "REMARK"        => array(90,"備考"),
                                "SHOWORDER"     => array(4,"表示順"));

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
            $primaryKey1   = $this->getPrimaryKey('ELECTCLASS_MST');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array( "GROUPCD"       => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]) : "",
                                    "GROUPNAME"     => $tmp[1],
                                    "GROUPABBV"     => $tmp[2],
                                    "REMARK"        => $tmp[3],
                                    "SHOWORDER"     => (strlen(trim($tmp[4]))) ? trim($tmp[4]) : "");
                    //エラーチェック
                    if ($this->errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)) {
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
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $duplicateDbErrFlg  = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $duplicateDbErrMsg  = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";
        $duplicateDbSep  = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("GROUPCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                } else {
                    if ($this->field["SHORI_MEI"] == "1" && 0 < $db->getOne(knjx170Query::dataCntSql($data[$key]))) {
                        $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, $val[1]);
                    }
                }
            }

            //必須項目チェック
            if (in_array($key, array("GROUPNAME","GROUPABBV"))) {
                if ($this->field["SHORI_MEI"] == "1" && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("GROUPCD","SHOWORDER"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx170Query::insertQueryErr($db, $line, $checkError);
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

    //テンプレートの書き出し
    public function outputDataFile()
    {
        $contents = "";
        if ($this->field["OUTPUT"] == "2") {
            //エラー
            $tables = array();
            $tables = array("1" => "ELECTCLASS_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $headerDupl = array();
            $headerDupl["GROUPCD"] = "群コード";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDupl);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= "行番号,メッセージ\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } else {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= "※群コード,※群名称,※群略称,備考,表示順,".$this->lastColumn."\n";
            }
            $query = knjx170Query::getselectDataSQL($this->field["YEAR"], $this->lastColumn);
        }

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "2") {
                $filename = sprintf("%s.csv", "選択科目マスタエラー");
            } else {
                $filename = sprintf("%s.csv", "選択科目マスタ");
            }
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        $contents = "※群コード,※群名称,※群略称,備考,表示順,".$this->lastColumn."\n";
        $contents .="9999,３桁,２桁,３０桁,9999,".$this->lastColumn;
        $filename = sprintf("%s.csv", "選択科目マスタ");

        common::downloadContents($contents, $filename);
        return true;
    }
}
