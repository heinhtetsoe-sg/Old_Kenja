<?php
class knjl651hModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $exam_type;
    public $s_receptno;
    public $e_receptno;
    public $file;
    public $field;
    public $err_data;

    public function knjl651hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR + 1;
        $this->exam_type    = 1; //固定値
        $this->applicantdiv = 2; //固定値
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->s_receptno     = VARS::post("S_RECEPTNO");
        $this->e_receptno     = VARS::post("E_RECEPTNO");
        $this->file           = VARS::file("FILE");
        $this->field          = array("HEADER" => VARS::post("HEADER"),            //ヘッダ有
                                      "OUTPUT" => VARS::post("OUTPUT"),            //CSV取込書出種別
                                );
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $hid_receptno = VARS::post("HID_RECEPTNO");
        $receptnoArray = explode(",", $hid_receptno);
        $this->interview = array();
        if (count($receptnoArray) > 0) {
            foreach ($receptnoArray as $key => $receptno) {
                $this->interview[$receptno]["INTERVIEW_A"] = VARS::post("INTERVIEW_A-{$receptno}");
                $this->interview[$receptno]["ATTEND_FLG"]  = VARS::post("ATTEND_FLG-{$receptno}");
                $this->interview[$receptno]["JUDGEMENT"]   = VARS::post("JUDGEMENT-{$receptno}");
                $this->interview[$receptno]["EXAMNO"]   = VARS::post("EXAMNO-{$receptno}");
            }
        }

        knjl651hQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
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
            $cnt = knjl651hQuery::updateQueryCsv($this, $data_arr);

            $erraDatas = "";
            foreach ($this->err_data as $errKey => $errVal) {
                $erraDatas .= $errKey . "行目　" . $errVal["MSG"] . "\\n";
            }
            $this->setMessage("MSG202", "処理済件数　：" . $cnt . "件\\nエラー件数    ：" . count($this->err_data)."件\\n" . $erraDatas);
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
            $checkdata["ENTEXAMYEAR"]    = array(4,      "HISSU",    "INT",  "入試年度");
            $checkdata["TESTDIV"]        = array(2,      "HISSU",    "INT",  "入試日程コード");
            $checkdata["RECEPTNO"]       = array(4,      "HISSU",    "INT",  "受験番号");
            $checkdata["HYOKA"]          = array("",      "",        "",     "評価");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $intaviewList = array();
            $query = knjl651hQuery::getInterview($this->ObjYear, $this->applicantdiv); // 高校用を取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $intaviewList[$row["VALUE"]] = $row["LABEL"];
            }

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

                    $cnt = 0;
                    $data = array();
                    $data["ENTEXAMYEAR"] = $tmp[$cnt++];
                    $data["TESTDIV"]     = $tmp[$cnt++];
                    $data["RECEPTNO"]    = $tmp[$cnt++];
                    $data["NAME"]        = $tmp[$cnt++];
                    $data["HYOKA"]       = $tmp[$cnt++];

                    $dataCnt = count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $intaviewList)) {
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
    public function errCheck($db, $data, $data_each, $line, $intaviewList)
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

            //年度
            if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試日程コード
            if ($key == "TESTDIV" && $this->testdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //受験番号
            if ($key == "RECEPTNO" && $data[$key] != "") {
                $query = knjl651hQuery::checkReceptNo($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //フラグチェック(値は1か0かNULL)
            if ($val[1] == "FLG" && !($data[$key] == "1" || $data[$key] == "0" || !strlen(trim($data[$key])))) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※フラグの値:", $otherSep, $val[3]);
            }

            //文字数チェック
            if ($val[0] != "" && strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }

            //評価
            if ($key == "HYOKA" && $data["HYOKA"] != "" && $data["HYOKA"] != "*") {
                if (!in_array($data["HYOKA"], $intaviewList)) {
                    $intaviewStr = "";
                    foreach ($intaviewList as $value) {
                        if ($intaviewStr != "") {
                            $intaviewStr .= "、";
                        }
                        $intaviewStr .= $value;
                    }
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※評価: {$intaviewStr} のいづれかを入力してください。", $otherSep, $val[3]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error  = mb_strcut($check_error, 0, 115);
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

    //CSV出力
    public function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]    = "入試年度";
        $header["TESTDIV"]        = "入試日程コード";
        $header["RECEPTNO"]       = "受験番号";
        $header["NAME"]           = "氏名";
        $header["HYOKA"]          = "評価";

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header) . "\n";
        }

        //データ出力
        $data = false;
        $query = knjl651hQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                if ($field == "HYOKA") {
                    //面接欠席「ATTEND_FLG = 1」の時、得点欄に「*」を表示する
                    $row[$field] = ($row["ATTEND_FLG"] == "1") ? "*" : $row[$field];
                }

                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //入試日程名称取得
        $row = $db->getRow(knjl651hQuery::getTestdivMst($this, $this->testdiv), DB_FETCHMODE_ASSOC);
        $testdivName = $row["TESTDIV_NAME"];

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            //CSVファイル名
            $title = $this->ObjYear . "年度入試 ".$testdivName." 面接評価入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
