<?php

require_once('for_php7.php');
class knjd657cModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $lastColumn;//CSVの最後の列の値

    public function knjd657cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD; //登録者コード
    }

    public function init()
    {
        $this->group_div   = "2";
        $this->target_div  = "0";
        $this->stf_auth_cd = "0000008888";

        $this->field = array("OUTPUT"    =>  VARS::post("OUTPUT"),    //出力取込種別 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
                             "GROUPCD"   =>  VARS::post("GROUPCD"),   //模試グループ
                             "GRADE"     =>  VARS::post("GRADE"),     //学年
                             "HEADER"    =>  VARS::post("HEADER"),    //ヘッダ有
                             "SHORI_MEI" =>  VARS::post("SHORI_MEI")  //処理名
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
                    knjd657cQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjd657cQuery::deleteQueryCsv($this, $this->data_arr);
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
                               "GROUPCD"          => array(4,"模試グループ"),
                               "GRADE"            => array(2,"学年"),
                               "MOCK_SUBCLASS_CD" => array(6,"模試科目コード"),
                               "REMARK1"          => array(300,"宿題提出数"),
                               "REMARK2"          => array(300,"宿題提出状況")
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
            $primaryKey1 = $this->getPrimaryKey('MOCK_DOCUMENT_GDAT');
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
                                  "GROUPCD"          => (strlen(trim($tmp[1]))) ? sprintf("%04d", $tmp[1]) : "",
                                  "GRADE"            => (strlen(trim($tmp[2]))) ? sprintf("%02d", $tmp[2]) : "",
                                  "MOCK_SUBCLASS_CD" => (strlen(trim($tmp[3]))) ? sprintf("%06d", $tmp[3]) : "",
                                  "REMARK1"          => (strlen(trim($tmp[4]))) ? $tmp[4] : "",
                                  "REMARK2"          => (strlen(trim($tmp[5]))) ? $tmp[5] : ""
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
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","GROUPCD","GRADE", "MOCK_SUBCLASS_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //コードが存在するかチェック
            if (in_array($key, array("GROUPCD","MOCK_SUBCLASS_CD"))) {
                if ($data[$key] != "" && 1 > $db->getOne(knjd657cQuery::checkMstData($this, $data[$key], $key))) {
                    if (!($key == 'MOCK_SUBCLASS_CD' && $data[$key] == '000000')) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","GROUPCD","GRADE", "MOCK_SUBCLASS_CD"))) {
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
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjd657cQuery::insertQueryErr($db, $line, $checkError);
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
                        "GROUPCD"          => "※模試グループ",
                        "GRADE"            => "※学年",
                        "MOCK_SUBCLASS_CD" => "※模試科目コード",
                        "REMARK1"          => "試験範囲",
                        "REMARK2"          => "宿題提出状況の備考"
                        );
        $header[$this->lastColumn] = $this->lastColumn;

        $mojisu = array();
        $header_rei = array("YEAR"             => "9999",
                            "GROUPCD"          => "1234",
                            "GRADE"            => "12",
                            "MOCK_SUBCLASS_CD" => "123456",
                            "REMARK1"          => "全角100文字",
                            "REMARK2"          => "全角100文字"
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
                                "GROUP_DIV"        => "グループ区分",
                                "STF_AUTH_CD"      => "職員権限コード",
                                "GROUPCD"          => "模試グループ",
                                "GRADE"            => "※学年",
                                "MOCK_SUBCLASS_CD" => "模試科目コード"
                               );
            //エラー
            $tables = array();
            $tables = array("1" => "MOCK_DOCUMENT_GDAT");
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
                $query = knjd657cQuery::selectMainQuery($this);
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
                $couse = "学力到達度テスト個人成績データ";
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
