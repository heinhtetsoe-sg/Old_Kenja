<?php
class knjl650hModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $exam_type;
    public $testsubclasscd;
    public $s_receptno;
    public $e_receptno;
    public $file;
    public $field;
    public $err_data;

    public function knjl650hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->testdiv);
            unset($this->testsubclasscd);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear        = CTRL_YEAR + 1;
        $this->exam_type      = 1; //固定値
        $this->applicantdiv   = 2; //固定値
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->s_receptno     = VARS::post("S_RECEPTNO");
        $this->e_receptno     = VARS::post("E_RECEPTNO");
        $this->file           = VARS::file("FILE");
        $this->field          = array("HEADER" => VARS::post("HEADER"),            //ヘッダ有
                                      "OUTPUT" => VARS::post("OUTPUT"),            //CSV取込書出種別
                                );
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->updField = array();

        $hid_receptno = VARS::post("HID_RECEPTNO");
        $receptnoArray = explode(",", $hid_receptno);

        foreach ($receptnoArray as $receptno) {
            $this->updField[$receptno]["SCORE"]     = VARS::post("SCORE-{$receptno}");
            $this->updField[$receptno]["JUDGEMENT"]  = VARS::post("JUDGEMENT-{$receptno}");
        }

        //更新
        knjl650hQuery::getUpdateQuery($this);
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
            $cnt = knjl650hQuery::updateQueryCsv($this, $data_arr);

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
            $checkdata["TESTSUBCLASSCD"] = array(2,      "HISSU",    "INT",  "科目コード");
            $checkdata["SCORE"]          = array(3,      "",         "INT",  "得点");

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

                    $cnt = 0;
                    $data = array();
                    $data["ENTEXAMYEAR"]           = $tmp[$cnt++];
                    $data["TESTDIV"]           = $tmp[$cnt++];
                    $data["RECEPTNO"]          = $tmp[$cnt++];
                    $data["NAME"]              = $tmp[$cnt++];
                    $data["TESTSUBCLASSCD"]    = $tmp[$cnt++];
                    $data["SCORE"]             = $tmp[$cnt++];

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
        $scoreErrFlg    = false;     //得点エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $scoreErrMsg    = "";        //得点エラーメッセージ

        $hissuSep       = "";
        $existenceSep   = "";
        $existenceSep   = "";
        $numericSep     = "";
        $lengthSep      = "";
        $scoreSep      = "";

        $checkSettingMstCd = array(
            "TESTSUBCLASSCD"    => "L009",
        );

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            if ($key == "SCORE") {
                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && $data[$key] != "*" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
            } else {
                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
            }

            //年度
            if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試日程コード
            if ($key == "TESTDIV" && $this->testdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //科目コード
            if ($key == "TESTSUBCLASSCD" && $this->testsubclasscd != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //設定マスタ
            if ($checkSettingMstCd[$key] && $data[$key] != "") {
                $query = knjl650hQuery::checkSettingMst($this, $checkSettingMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在(設定マスタ):", $existenceSep, $val[3]);
                }
            }

            //受験番号
            if ($key == "RECEPTNO" && $data[$key] != "") {
                $query = knjl650hQuery::checkReceptNo($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }

            //満点チェック
            if ($key == "SCORE" && $data[$key] != "") {
                $row = $db->getRow(knjl650hQuery::getTestsubclasscd($this, $data["TESTSUBCLASSCD"]), DB_FETCHMODE_ASSOC);

                if (is_array($row) && $data["SCORE"] > (int)$row["PERFECT"]) {
                    $returnFlg = $this->errSet($scoreErrFlg, $scoreErrMsg, "※満点:" . (int)$row["PERFECT"] . "以下で入力してください:", $scoreSep, $val[3]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$scoreErrMsg;
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
        $header["TESTSUBCLASSCD"] = "科目コード";
        $header["SCORE"]          = "得点";

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header) . "\n";
        }

        //データ出力
        $data = false;
        $query = knjl650hQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                if ($field == "SCORE") {
                    //科目欠席「ATTEND_FLG = 1」の時、得点欄に「*」を表示する
                    $row[$field] = ($row["ATTEND_FLG"] == "1") ? "*" : $row[$field];
                }

                if ($field == "TESTSUBCLASSCD") {
                    $row[$field] = ($row["TESTSUBCLASSCD"] == "") ? $this->testsubclasscd : $row[$field];
                }

                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //科目名称取得
        $row = $db->getRow(knjl650hQuery::getTestsubclasscd($this, $this->testsubclasscd), DB_FETCHMODE_ASSOC);
        $subclassName = $row["TESTSUBCLASS_NAME"];

        //入試日程名称取得
        $row = $db->getRow(knjl650hQuery::getTestdivMst($this, $this->testdiv), DB_FETCHMODE_ASSOC);
        $testdivName = $row["TESTDIV_NAME"];

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            //CSVファイル名
            $title = $this->ObjYear . "年度入試 ".$testdivName." ".$subclassName." 得点入力";
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
