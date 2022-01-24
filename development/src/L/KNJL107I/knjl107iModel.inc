<?php
class knjl107iModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $sendPrgId;
    public $sendAuth;
    public $auth;

    public function knjl107iModel()
    {
    }

    public function init()
    {
        $this->auth = AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->file = VARS::file("FILE");
    }

    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $cnt = knjl107iQuery::insertQueryCsv($this, $this->data_arr);

            $db = Query::dbCheckOut();
            $query  = " SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
            $errcnt = $db->getOne($query);
            Query::dbCheckIn($db);

            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    public function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]       = array(4,    "HISSU",  "INT",   "入試年度");
            $checkdata["EXAMNO"]            = array(4,    "HISSU",  "INT",   "受験番号"); //下4桁
            $checkdata["PROCEDUREDIV1"]     = array(1,    "HISSU",  "INT",   "一次手続き済フラグ");
            $checkdata["PROCEDUREDIV2"]     = array(1,    "HISSU",  "INT",   "二次手続き済フラグ");
            $checkdata["PROCEDUREDATE"]     = array(10,   "HISSU",  "DATE",  "手続き完了日");

            $this->csvFormat = $this->getCsvFormat();

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl107iQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
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
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array();
                    foreach ($this->csvFormat as $fieldKey => $fieldVal) {
                        $data[$fieldKey] = $tmp[$fieldVal["CSV_NO"]];
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
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
    public function errCheck($db, &$data, $checkdata, $line)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $otherSep           = "";

        //データ変換
        if (strlen($data["EXAMNO"]) == 10) {
            $data["EXAMNO"] = substr($data["EXAMNO"], 6, 4);        //受験番号(下4桁)
        }

        //エラーチェック
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }
            //数値チェック
            if ($val[2] == "INT" && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "" && $this->date_rep($data[$key]) == "") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
            //年度チェック
            if (in_array($key, array("ENTEXAMYEAR")) && $data[$key] != $this->year) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
        }

        //手続き済フラグチェック
        if ($data["PROCEDUREDIV1"] == "1" && $data["PROCEDUREDIV2"] == "0" || $data["PROCEDUREDIV1"] == "0" && $data["PROCEDUREDIV2"] == "1") {
        } else {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "一次二次手続き済フラグが両方1または両方0は不正");
        }

        //合格者チェック
        if (strlen($data["ENTEXAMYEAR"]) && strlen($data["EXAMNO"])) {
            $cnt = $db->getOne(knjl107iQuery::checkJudge($this, $data));
            if ($cnt == 0) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "合格者以外不可");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl107iQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //CSVフォーマット
    public function getCsvFormat()
    {
        $csvInfo = array();
        $csvInfo["ENTEXAMYEAR"]     = array("CSV_NO" => "0", "TITLE" => "入試年度");
        $csvInfo["EXAMNO"]          = array("CSV_NO" => "1", "TITLE" => "受験番号");
//        $csvInfo["NAME"]            = array("CSV_NO" => "2", "TITLE" => "志願者氏名");
        $csvInfo["PROCEDUREDIV1"]   = array("CSV_NO" => "3", "TITLE" => "一次手続き済フラグ");
        $csvInfo["PROCEDUREDIV2"]   = array("CSV_NO" => "4", "TITLE" => "二次手続き済フラグ");
        $csvInfo["PROCEDUREDATE"]   = array("CSV_NO" => "5", "TITLE" => "手続き完了日");
        return $csvInfo;
    }

    //日付変換 項目チェック後に行う。
    public function date_rep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
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

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        $contents .= implode($header_err, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //エラー
        $query = knjl107iQuery::selectCsvErrQuery();

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

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
//var_dump($_REQUEST);
