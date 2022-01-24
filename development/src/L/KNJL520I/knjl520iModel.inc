<?php
class knjl520iModel extends model
{
    public $cmd;
    public $examYear;
    public $applicantdiv;
    public $testdiv;
    public $exam_type;
    public $s_examno;
    public $e_examno;
    public $hid_examno;
    public $recordarry;
    public $field = array();
    public $file;
    public $examTypeList;
    public $kyokaSeqArray;

    public function knjl520iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->testdiv);
            unset($this->exam_type);
            unset($this->s_examno);
            unset($this->e_examno);
        }

        $this->cmd          = VARS::request("cmd");
        $this->examYear     = CTRL_YEAR + 1;
        $this->applicantdiv = 2; //固定値
        $this->exam_type      = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE"); //学科
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->s_examno     = VARS::post("S_EXAMNO");
        $this->e_examno     = VARS::post("E_EXAMNO");
        $this->hid_examno   = VARS::post("HID_EXAMNO"); //"EXAMNO"-"COUNT"で取得

        $this->file = VARS::file("FILE");

        $this->field = array(
            "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
            "OUTPUT"           =>  VARS::post("OUTPUT"),            //CSV取込書出種別
        );

        //教科SEQ配列
        $this->kyokaSeqArray = array();
        for ($j = 1; $j <= 9; $j++) {
            $this->kyokaSeqArray[] = sprintf("%02d", $j);
        }

        //学科リスト
        $this->examTypeList = array(
            "1" => "普通科",
            "2" => "工業科",
        );
    }

    //受験番号をセット
    public function &getMainModel()
    {
        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //内申点を受験番号ごとに配置
        $this->recordarry = array();
        $examno_array = array();
        $tmp = explode(",", $this->hid_examno);
        foreach ($tmp as $key => $examno_count) {
            $examno_array = explode("-", $examno_count);
            $addwk = array();
            for ($i = 1; $i <= 3; $i++) {
                foreach ($this->kyokaSeqArray as $kyokaseq) {
                    $addwk["kyoka{$i}_{$kyokaseq}"] = VARS::post("KYOKA{$i}_{$kyokaseq}-".$examno_array[1]);
                }
            }
            $addwk["total_all1"] = VARS::post("HID_TOTAL_ALL1-".$examno_array[1]);
            $addwk["total_all2"] = VARS::post("HID_TOTAL_ALL2-".$examno_array[1]);
            $addwk["total_all3"] = VARS::post("HID_TOTAL_ALL3-".$examno_array[1]);
            $addwk["average_all1"] = VARS::post("HID_AVERAGE_ALL1-".$examno_array[1]);
            $addwk["average_all2"] = VARS::post("HID_AVERAGE_ALL2-".$examno_array[1]);
            $addwk["average_all3"] = VARS::post("HID_AVERAGE_ALL3-".$examno_array[1]);
            $addwk["kesseki1"] = VARS::post("KESSEKI1-".$examno_array[1]);
            $addwk["kesseki2"] = VARS::post("KESSEKI2-".$examno_array[1]);
            $addwk["kesseki3"] = VARS::post("KESSEKI3-".$examno_array[1]);
            $this->recordarry[$examno_array[1]] = $addwk;
        }

        //入力チェック
        if ($this->validateInput()) {
            //更新処理
            knjl520iQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //データチェック
        if (!$this->hid_examno) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
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
            $cnt = knjl520iQuery::updateQueryCsv($this, $data_arr);

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
            $checkdata["EXAMNO"]            = array(4,      "HISSU",    "INT",     "受験番号");

            //CSVフォーマット
            $csvFormat = $this->getCsvFormat();
            foreach ($csvFormat as $fieldKey => $fieldVal) {
                if (strpos($fieldKey, "KYOKA") !== false) {
                    $checkdata[$fieldKey]            = array(1,      "",        "INT",     $fieldVal["TITLE"]);
                } elseif (strpos($fieldKey, "KESSEKI") !== false) {
                    $checkdata[$fieldKey]            = array(3,      "",        "INT",     $fieldVal["TITLE"]);
                }
            }

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
                    foreach ($csvFormat as $fieldKey => $fieldVal) {
                        if ($fieldKey == "TESTDIV") {
                            $data[$fieldKey] = sprintf("%02d", $tmp[$fieldVal["CSV_NO"]]);
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

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl520iQuery::checkExamNo($this, $data[$key]);
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
        //DB接続
        $db = Query::dbCheckOut();

        //教科名取得
        $headerKyoka = array();
        $result = $db->query(knjl520iQuery::getSettingMst($this, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerKyoka[$row["SEQ"]] = $row["NAME1"];
        }

        //DB切断
        Query::dbCheckIn($db);

        $cnt = 0;
        $csvInfo = array();
        $csvInfo["TESTDIV0"]          = array("CSV_NO" => $cnt++, "TITLE" => "学科コード");
        $csvInfo["GAKKA_NAME"]        = array("CSV_NO" => $cnt++, "TITLE" => "学科名");
        $csvInfo["TESTDIV"]           = array("CSV_NO" => $cnt++, "TITLE" => "入試区分コード");
        $csvInfo["TESTDIV_NAME"]      = array("CSV_NO" => $cnt++, "TITLE" => "入試区分名");
        $csvInfo["EXAMNO"]            = array("CSV_NO" => $cnt++, "TITLE" => "受験番号");
        $csvInfo["NAME"]              = array("CSV_NO" => $cnt++, "TITLE" => "名前");
        $csvInfo["FINSCHOOL_NAME"]    = array("CSV_NO" => $cnt++, "TITLE" => "中学校");

        for ($i = 1; $i <= 3; $i++) {
            foreach ($headerKyoka as $seq => $val) {
                $csvInfo["KYOKA{$i}_{$seq}"] = array("CSV_NO" => $cnt, "TITLE" => "{$i}年 {$val}");
                $cnt++;
            }
            $csvInfo["TOTAL_ALL{$i}"]   = array("CSV_NO" => $cnt++, "TITLE" => "{$i}年 合計");
            $csvInfo["AVERAGE_ALL{$i}"] = array("CSV_NO" => $cnt++, "TITLE" => "{$i}年 平均");
        }

        $csvInfo["KESSEKI1"]    = array("CSV_NO" => $cnt++, "TITLE" => "1年 欠席");
        $csvInfo["KESSEKI2"]    = array("CSV_NO" => $cnt++, "TITLE" => "2年 欠席");
        $csvInfo["KESSEKI3"]    = array("CSV_NO" => $cnt++, "TITLE" => "3年 欠席");

        return $csvInfo;
    }

    //CSV出力
    public function getCsvModel()
    {
        //初期化
        $contents = "";

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

        //ヘッダ出力
        $header_data = false;
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header) . "\n";
            $header_data = true;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //データ出力
        $query = knjl520iQuery::selectQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            $title = $this->examYear."年度入試 内申点入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function clean()
    {
    }
}
