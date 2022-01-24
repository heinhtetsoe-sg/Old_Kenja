<?php

require_once('for_php7.php');
class knjd658cModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $subclasscd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード

    public function knjd658cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD; //登録者コード
    }

    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),         //出力取込種別 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
                             "MOCKCD"           =>  VARS::post("MOCKCD"),         //模試名コンボ
                             "HEADER"           =>  VARS::post("HEADER"),         //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")       //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->getPropertiesAll();
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

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
                    knjd658cQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjd658cQuery::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array("YEAR"             => array(4,"年度"),
                               "MOCKCD"           => array(9,"模試コード"),
                               "SCHREGNO"         => array(8,"学籍番号"),
                               "MOCK_SUBCLASS_CD" => array(6,"模試科目コード"),
                               "SCORE"            => array(4,"得点"),
                               "AVG"              => array(10,"校内平均点"),
                               "DEVIATION"        => array(5,"校内偏差値"),
                               "RANK"             => array(5,"校内順位"),
                               "COUNT"            => array(5,"校内人数"),
                               "ZENKOKU_HENSATI"  => array(5,"全国偏差値")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('MOCK2_DAT');
            $duplicateCsv1 = array();
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

                    $data = array("YEAR"             => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]) : "",
                                  "MOCKCD"           => (strlen(trim($tmp[1]))) ? sprintf("%09d", $tmp[1]) : "",
                                  "SCHREGNO"         => (strlen(trim($tmp[2]))) ? sprintf("%08d", $tmp[2]) : "",
                                  "MOCK_SUBCLASS_CD" => (strlen(trim($tmp[3]))) ? sprintf("%06d", $tmp[3]) : "",
                                  "SCORE"            => (strlen(trim($tmp[4]))) ? $tmp[4] : "",
                                  "AVG"              => (strlen(trim($tmp[5]))) ? $tmp[5] : "",
                                  "DEVIATION"        => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                  "RANK"             => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                  "COUNT"            => (strlen(trim($tmp[8]))) ? $tmp[8] : "",
                                  "ZENKOKU_HENSATI"  => (strlen(trim($tmp[9]))) ? $tmp[9] : ""
                                  );

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
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","MOCKCD","SCHREGNO", "MOCK_SUBCLASS_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","MOCKCD","SCHREGNO", "MOCK_SUBCLASS_CD", "SCORE", "AVG", "DEVIATION", "RANK", "COUNT", "ZENKOKU_HENSATI"))) {
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
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjd658cQuery::insertQueryErr($db, $line, $checkError);
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
        $header_err = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
        //ヘッダデータ
        $header = array("YEAR"             => "※年度",
                        "MOCKCD"           => "※模試コード",
                        "SCHREGNO"         => "※学籍番号",
                        "MOCK_SUBCLASS_CD" => "※模試科目コード",
                        "SCORE"            => "得点",
                        "AVG"              => "校内平均点",
                        "DEVIATION"        => "校内偏差値",
                        "RANK"             => "校内順位",
                        "COUNT"            => "校内人数",
                        "ZENKOKU_HENSATI"  => "全国偏差値"
                        );
        $header[$this->lastColumn] = $this->lastColumn;

        $mojisu = array();
        $header_rei = array("YEAR"             => "9999",
                            "MOCKCD"           => "123456789",
                            "SCHREGNO"         => "12345678",
                            "MOCK_SUBCLASS_CD" => "123456",
                            "SCORE"            => "100",
                            "AVG"              => "1234.56789",
                            "DEVIATION"        => "123.4",
                            "RANK"             => "123",
                            "COUNT"            => "123",
                            "ZENKOKU_HENSATI"  => "123.4"
                            );
        $header_rei[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") { //1:ヘッダ出力
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") { //3:エラー出力
            $duplHeader = array("YEAR"             => "年度",
                                "MOCKCD"           => "模試コード",
                                "SCHREGNO"         => "学籍番号",
                                "MOCK_SUBCLASS_CD" => "模試科目コード",
                                "MOCKDIV"          => "模試区分"
                               );
            //エラー
            $tables = array();
            $tables = array("1" => "MOCK2_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") { //4:データ出力
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") { //1:ヘッダ出力
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") { //3:エラー出力
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") { //4:データ出力
                //データ
                $query = knjd658cQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") { //4:データ出力
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
            if ($this->field["OUTPUT"]=="3") { //3:エラー出力
                $couse = "エラー出力";
            } else {                         //エラー以外
                $couse = "成績一覧表の備考データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
