<?php
//メニュー画面
class knjl051oModel extends model
{
    public $cmd;
    public $ObjYear;
    public $applicantdiv;
    public $testdiv;
    public $exam_type;
    public $examhallcd;
    public $field = array();
    public $file;
    public $score;

    public function knjl051oModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->exam_type);
            unset($this->examhallcd);
            unset($this->score);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear = CTRL_YEAR + 1;

        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->examhallcd = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");

        $this->tmp_score      = VARS::post("SCORE"); //配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");

        $this->field = array("HEADER" => VARS::post("HEADER"),             //ヘッダ有
                             "OUTPUT" =>  VARS::post("OUTPUT"),            //CSV取込書出種別
                            );

        $this->file = VARS::file("FILE");
    }

    public function &getMainModel()
    {
        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->score = array();

        //得点を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->score[$receptno] = $this->tmp_score[$key];
        }
        $result = knjl051oQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201", "\\n全受験型の得点入力が完了したら\\n偏差値算出処理を実行して下さい。");
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
        if ($this->isDataMatched($data_arr)) {
            $cnt = knjl051oQuery::updateQueryCsv($this, $data_arr);

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
            $checkdata["APPLICANTDIV"]   = array(1,      "HISSU",    "INT",  "入試制度コード");
            $checkdata["TESTDIV"]        = array(1,      "HISSU",    "INT",  "入試区分コード");
            $checkdata["EXAM_TYPE"]      = array(1,      "HISSU",    "INT",  "受験型");
            $checkdata["TESTSUBCLASSCD"] = array(1,      "HISSU",    "INT",  "受験科目");
            $checkdata["RECEPTNO"]       = array(4,      "HISSU",    "INT",  "座席番号");
            $checkdata["EXAMNO"]         = array(5,      "HISSU",    "INT",  "受験番号");
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
                    $data["ENTEXAMYEAR"]       = $tmp[$cnt++];
                    $data["APPLICANTDIV"]      = $tmp[$cnt++];
                    $data["APPLICANTDIV_NAME"] = $tmp[$cnt++];
                    $data["TESTDIV"]           = $tmp[$cnt++];
                    $data["TESTDIV_NAME"]      = $tmp[$cnt++];
                    $data["EXAM_TYPE"]         = $tmp[$cnt++];
                    $data["EXAM_TYPE_NAME"]    = $tmp[$cnt++];
                    $data["TESTSUBCLASSCD"]    = $tmp[$cnt++];
                    $data["TESTSUBCLASS_NAME"] = $tmp[$cnt++];
                    $data["RECEPTNO"]          = $tmp[$cnt++];
                    $data["EXAMNO"]            = $tmp[$cnt++];
                    $data["NAME"]              = $tmp[$cnt++];
                    $data["NAME_KANA"]         = $tmp[$cnt++];
                    $data["SEXCD"]             = $tmp[$cnt++];
                    $data["SEX"]               = $tmp[$cnt++];
                    $data["SCORE"]             = $tmp[$cnt++];
                    $data["DESIREDIV"]         = $tmp[$cnt++];
                    $data["EXAMCOURSE_NAME"]   = $tmp[$cnt++];

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

        $checkNameMstCd = array(
            "APPLICANTDIV"      => "L003",
            "EXAM_TYPE"         => "L005",
            "TESTSUBCLASSCD"    => "L009",
        );

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

            //入試制度
            if ($key == "APPLICANTDIV" && $this->applicantdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試区分
            if ($key == "TESTDIV" && $this->testdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //受験型
            if ($key == "EXAM_TYPE" && $this->exam_type != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //受験科目
            if ($key == "TESTSUBCLASSCD" && $this->testsubclasscd != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            
            //名称マスタ
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl051oQuery::nameCntSql($this, $checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在(名称マスタ):", $existenceSep, $val[3]);
                }
            }

            //座席番号
            if ($key == "RECEPTNO" && $data[$key] != "") {
                $query = knjl051oQuery::checkReceptNo($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl051oQuery::checkExamNo($this, $data[$key]);
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
                $row = $db->getrow(knjl051oQuery::selectQuery($this, $data["RECEPTNO"]), DB_FETCHMODE_ASSOC);

                if ($data["SCORE"] > (int)$row["PERFECT"]) {
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
        $header["ENTEXAMYEAR"]       = "入試年度";
        $header["APPLICANTDIV"]      = "入試制度コード";
        $header["APPLICANTDIV_NAME"] = "入試制度名";
        $header["TESTDIV"]           = "入試区分コード";
        $header["TESTDIV_NAME"]      = "入試区分名";
        $header["EXAM_TYPE"]         = "受験型";
        $header["EXAM_TYPE_NAME"]    = "受験型名";
        $header["TESTSUBCLASSCD"]    = "受験科目";
        $header["TESTSUBCLASS_NAME"] = "受験科目名";
        $header["RECEPTNO"]          = "座席番号";
        $header["EXAMNO"]            = "受験番号";
        $header["NAME"]              = "氏名";
        $header["NAME_KANA"]         = "氏名かな";
        $header["SEXCD"]             = "性別コード";
        $header["SEX"]               = "性別";
        $header["SCORE"]             = "得点";
        $header["DESIREDIV"]         = "出願コース";
        $header["EXAMCOURSE_NAME"]   = "出願コース名";

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header) . "\n";
        }

        //データ出力
        $data = false;
        $query = knjl051oQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->ObjYear . "入試_得点データ";
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
