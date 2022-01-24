<?php
class knjl550iModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $exam_type;
    public $testsubclasscd;
    public $examhallcd;
    public $s_receptno;
    public $e_receptno;
    public $field = array();
    public $file;
    public $err_data;
    public $examTypeList;

    public function knjl550iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->exam_type);
            unset($this->testdiv);
            unset($this->testsubclasscd);
            unset($this->examhallcd);
            unset($this->s_receptno);
            unset($this->e_receptno);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR + 1;
        $this->applicantdiv = 2; //固定値
        $this->exam_type    = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd   = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->examhallcd   = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        $this->s_receptno   = VARS::post("S_RECEPTNO");
        $this->e_receptno   = VARS::post("E_RECEPTNO");

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

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();

        $hid_examno = VARS::post("HID_EXAMNO");
        $examnoArray = explode(",", $hid_examno);
        foreach ($examnoArray as $key => $examno) {
            $this->updField[$examno]["SCORE"]     = VARS::post("SCORE-{$examno}");
            $this->updField[$examno]["JUDGEMENT"] = VARS::post("JUDGEMENT-{$examno}");
        }

        //更新
        knjl550iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201", "\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
    }

    //CSV取込
    public function &getCsvInputModel()
    {
        //ファイルの文字列の取得
        $data_arr = array();
        $this->err_data = array();
        $data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($data_arr)) {
            $cnt = knjl550iQuery::updateQueryCsv($this, $data_arr);

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
        } else {
            $checkdata = array();
            $checkdata["TESTDIV0"]          = array(1,      "HISSU",    "INT",     "学科コード");
            $checkdata["TESTDIV"]           = array(2,      "HISSU",    "INT",     "入試区分コード");
            $checkdata["TESTSUBCLASSCD"]    = array(1,      "HISSU",    "INT",     "科目コード");
            $checkdata["EXAMNO"]            = array(4,      "HISSU",    "INT",     "受験番号");
            $checkdata["SCORE"]             = array(3,      "",         "INT",     "得点");
    
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

                    $dataCnt = count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
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

            //科目
            if ($key == "TESTSUBCLASSCD" && $this->testsubclasscd != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl550iQuery::checkExamNo($this, $data[$key]);
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

        //満点チェック
        if ($key == "SCORE" && $data[$key] != "") {
            $query = knjl550iQuery::getPerfectExamtypeMst($this);
            $perfectRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $perfect = strlen($perfectRow["PERFECT"]) ? (int)$perfectRow["PERFECT"] : 0;

            if ($data["SCORE"] > $perfect) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "得点は".$perfect."以下で入力してください:");
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
        $csvInfo["TESTDIV0"]          = array("CSV_NO" => "0", "TITLE" => "学科コード");
        $csvInfo["GAKKA_NAME"]        = array("CSV_NO" => "1", "TITLE" => "学科名");
        $csvInfo["TESTDIV"]           = array("CSV_NO" => "2", "TITLE" => "入試区分コード");
        $csvInfo["TESTDIV_NAME"]      = array("CSV_NO" => "3", "TITLE" => "入試区分名");
        $csvInfo["TESTSUBCLASSCD"]    = array("CSV_NO" => "4", "TITLE" => "科目コード");
        $csvInfo["TESTSUBCLASS_NAME"] = array("CSV_NO" => "5", "TITLE" => "科目名");
        $csvInfo["EXAMHALLCD"]        = array("CSV_NO" => "6", "TITLE" => "会場コード");
        $csvInfo["EXAMHALL_NAME"]     = array("CSV_NO" => "7", "TITLE" => "会場名");
        $csvInfo["EXAMNO"]            = array("CSV_NO" => "8", "TITLE" => "受験番号");
        $csvInfo["SCORE"]             = array("CSV_NO" => "9", "TITLE" => "得点");
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
            if (in_array($fieldKey, array("TESTDIV0", "TESTDIV", "TESTSUBCLASSCD", "EXAMNO"))) {
                $hissuMark = "※";
            }
            $header[$fieldKey] = $hissuMark.$fieldVal["TITLE"];
        }

        //学科名称
        $gakkaName = $this->examTypeList[$this->exam_type];

        //入試区分名称
        $query = knjl550iQuery::getTestdivMst($this, $this->testdiv);
        $testdivRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $testdivName = $testdivRow["TESTDIV_NAME"];

        //科目名称
        $query = knjl550iQuery::getSettingMst($this, "L009");
        $testsublassNameRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $testsublassName = $testsublassNameRow["NAME1"];

        $reiExamhallcd = $this->examhallcd;
        if ($this->examhallcd == "ALL") {
            $reiExamhallcd = "";
        }
        //会場名称
        $query = knjl550iQuery::getExamHall($this, $reiExamhallcd);
        $hallRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $reiExamhallName = $hallRow["EXAMHALL_NAME"];
        $hallName = " ".$hallRow["EXAMHALL_NAME"];
        if ($reiExamhallcd == "") {
            $reiExamhallcd = $hallRow["EXAMHALLCD"];
            $hallName = "";
        }

        //ヘッダー例
        $header_rei["TESTDIV0"]          = $this->exam_type;
        $header_rei["GAKKA_NAME"]        = $gakkaName;
        $header_rei["TESTDIV"]           = $this->testdiv;
        $header_rei["TESTDIV_NAME"]      = $testdivName;
        $header_rei["TESTSUBCLASSCD"]    = $this->testsubclasscd;
        $header_rei["TESTSUBCLASS_NAME"] = $testsublassName;
        $header_rei["EXAMHALLCD"]        = $reiExamhallcd;
        $header_rei["EXAMHALL_NAME"]     = $reiExamhallName;
        $header_rei["EXAMNO"]            = "4001";
        $header_rei["SCORE"]             = "数字3桁以内";

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header) . "\n";
        }

        //データ出力
        if ($this->field["OUTPUT"] == "2") {
            $query = knjl550iQuery::selectQuery($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sep = "";
                foreach ($header as $field => $label) {
                    //欠席者
                    if ($field == "SCORE" && $row["JUDGEMENT"] == "4") {
                        $contents .= $sep."";
                    } else {
                        $contents .= $sep.$row[$field];
                    }
                    $sep = ",";
                }
                $contents .= "\n";
                $header_data = true;
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
            $title = $this->ObjYear."年度入試 得点入力".$sample;
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
