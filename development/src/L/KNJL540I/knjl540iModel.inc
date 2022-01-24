<?php
class knjl540iModel extends Model
{
    public $cmd;
    public $ObjYear;
    public $exam_type;
    public $applicantdiv;
    public $testdiv;
    public $examhallcd;
    public $selectLeft;
    public $selectRight;
    public $field = array();
    public $file;
    public $err_data;
    public $examTypeList;

    public function knjl540iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->testdiv);
            unset($this->exam_type);
            unset($this->examhallcd);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear = CTRL_YEAR + 1;
        $this->applicantdiv = 2; //固定値
        $this->exam_type       = VARS::post("EXAM_TYPE"); //学科
        $this->testdiv         = VARS::post("TESTDIV");
        $this->examhallcd      = VARS::post("EXAMHALLCD");
        $this->selectLeft      = VARS::post("selectLeft");
        $this->selectRight     = VARS::post("selectRight");

        $this->file = VARS::file("FILE");

        $this->field = array(
            "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
            "OUTPUT"           =>  VARS::post("OUTPUT"),            //CSV取込書出種別
        );

        //学科リスト
        $this->examTypeList = array(
            "1" => "普通科",
            "2" => "工業科",
        );
    }

    //入力項目チェック
    public function validateInput()
    {
        //データ指定なし
        if (!$this->selectLeft && !$this->selectRight) {
            $this->setWarning("MSG304");
            return false;
        }
        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            //更新処理
            $result1 = knjl540iQuery::getUpdateQuery($this, "left");  //左側リスト
            $result2 = knjl540iQuery::getUpdateQuery($this, "right"); //右側リスト
            if ($result1 || $result2) {
                $this->setMessage("MSG201");
            }
        }
    }

    //CSV取込
    public function &getCsvInputModel()
    {
        //ファイルの文字列の取得
        $data_arr = array();
        $this->err_data = array();
        $data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($data_arr) && !isset($this->warning)) {
            $cnt = knjl540iQuery::updateQueryCsv($this, $data_arr);

            $erraDatas = "";
            foreach ($this->err_data as $errKey => $errVal) {
                $erraDatas .= $errKey . "行目　" . $errVal["MSG"] . "\\n";
            }
            $this->setMessage("MSG202", "処理済件数　：" . $cnt . "件\\nエラー件数    ：" . count($this->err_data) . "件\\n" . $erraDatas);
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
            return;
        } else {
            $checkdata = array();
            $checkdata["TESTDIV0"]          = array(1,      "HISSU",    "INT",     "学科コード");
            $checkdata["TESTDIV"]           = array(2,      "HISSU",    "INT",     "入試区分コード");
            $checkdata["EXAMNO"]            = array(4,      "HISSU",    "INT",     "受験番号");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

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
                    //配列の要素数チェック
                    $checkcount = count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }

                    $data = array();
                    //CSVフォーマット
                    $csvFormat = $this->getCsvFormat();
                    foreach ($csvFormat as $fieldKey => $fieldVal) {
                        if ($fieldKey == "TESTDIV") {
                            $data[$fieldKey] = sprintf("%02d", $tmp[$fieldVal["CSV_NO"]]);
                        } elseif ($fieldKey == "EXAMHALLCD") {
                            $data[$fieldKey] = sprintf("%04d", $tmp[$fieldVal["CSV_NO"]]);
                        } elseif ($fieldKey == "EXAMNO") {
                            $data[$fieldKey] = sprintf("%04d", $tmp[$fieldVal["CSV_NO"]]);
                        } else {
                            $data[$fieldKey] = $tmp[$fieldVal["CSV_NO"]];
                        }
                    }
                    $data["ERROR"]     = "0"; // 入力チェック用の要素

                    $dataCnt = count($data) - 1;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    $temp_arr[$i] = $data;

                    //エラーチェック
                    if (!$this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i]["ERROR"] = "1";
                    }
                    $i++;
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
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
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

        $hissuSep       = "";
        $existenceSep   = "";
        $existenceSep   = "";
        $numericSep     = "";
        $lengthSep      = "";
        $otherSep       = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            //学科コード
            if ($key == "TESTDIV0" && $this->exam_type != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試区分
            if ($key == "TESTDIV" && $this->testdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl540iQuery::checkReceptNo($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg . $existenceErrMsg . $numericErrMsg . $lengthErrMsg . $otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
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
        $errMsg .= $sep . $val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：" . count($data_arr) . "件");
            return false;
        }
        return true;
    }

    //CSVフォーマット
    public function getCsvFormat()
    {
        $csvInfo = array();
        $csvInfo["TESTDIV0"]       = array("CSV_NO" => "0", "TITLE" => "学科コード");
        $csvInfo["GAKKA_NAME"]     = array("CSV_NO" => "1", "TITLE" => "学科名");
        $csvInfo["TESTDIV"]        = array("CSV_NO" => "2", "TITLE" => "入試区分コード");
        $csvInfo["TESTDIV_NAME"]   = array("CSV_NO" => "3", "TITLE" => "入試区分名");
        $csvInfo["EXAMHALLCD"]     = array("CSV_NO" => "4", "TITLE" => "会場コード");
        $csvInfo["EXAMHALL_NAME"]  = array("CSV_NO" => "5", "TITLE" => "会場名");
        $csvInfo["EXAMNO"]         = array("CSV_NO" => "6", "TITLE" => "受験番号");
        $csvInfo["NAME"]           = array("CSV_NO" => "7", "TITLE" => "氏名");
        return $csvInfo;
    }

    //CSV出力
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $sample = "";
        $contents = "";
        $header_data = false;

        //ヘッダ
        $header = array();

        //CSVフォーマット
        $csvFormat = $this->getCsvFormat();
        foreach ($csvFormat as $fieldKey => $fieldVal) {
            $hissuMark = "";
            if (in_array($fieldKey, array("TESTDIV0", "TESTDIV", "EXAMNO"))) {
                $hissuMark = "※";
            }
            $header[$fieldKey] = $hissuMark.$fieldVal["TITLE"];
        }

        //学科名称
        $gakkaName = $this->examTypeList[$this->exam_type];

        //入試区分名称
        $query = knjl540iQuery::getTestdivMst($this, $this->testdiv);
        $testdivRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $testdivName = $testdivRow["TESTDIV_NAME"];

        //会場名称
        $query = knjl540iQuery::getExamHall($this, $this->examhallcd);
        $hallRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $hallName = " ".$hallRow["EXAMHALL_NAME"];

        //ヘッダー例
        $header_rei["TESTDIV0"]       = $this->exam_type;
        $header_rei["GAKKA_NAME"]     = $gakkaName;
        $header_rei["TESTDIV"]        = $this->testdiv;
        $header_rei["TESTDIV_NAME"]   = $testdivName;
        $header_rei["EXAMHALLCD"]     = $this->examhallcd;
        $header_rei["EXAMHALL_NAME"]  = $hallName;
        $header_rei["EXAMNO"]         = "4001";
        $header_rei["NAME"]           = "志願者　太郎";

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header) . "\n";
        }

        //データ出力
        if ($this->field["OUTPUT"] == "2") {
            $query = knjl540iQuery::getSelectQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //欠席者(左側)
                if ($row["JUDGEMENT"] == "4") {
                    $sep = "";
                    foreach ($header as $field => $label) {
                        $contents .= $sep.$row[$field];
                        $sep = ",";
                    }
                    $contents .= "\n";
                    $header_data = true;
                }
            }
            $result->free();

        //ヘッダー出力
        } else {
            $sample = "（サンプル）";
            $header_data = true;
            $sep = "";
            foreach ($header_rei as $field => $label) {
                $contents .= $sep.$label;
                $sep = ",";
            }
            $contents .= "\n";
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            $title = $this->ObjYear."年度入試 欠席入力".$sample;
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
