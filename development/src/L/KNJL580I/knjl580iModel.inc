<?php
class knjl580iModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examYear;
    public $exam_type;
    public $file;
    public $field = array();
    public $selectdata;
    public $err_data;
    public $examTypeList;
    public $procTypeList;

    public function knjl580iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->exam_type);
            unset($this->testdiv);
        }

        $this->cmd = VARS::request("cmd");

        $this->examYear = CTRL_YEAR + 1;

        $this->applicantdiv = "2";

        $this->exam_type      = VARS::post("EXAM_TYPE"); //学科

        if (VARS::request("TESTDIV") != "") {
            $this->testdiv = VARS::request("TESTDIV");
        }

        $this->selectdata = VARS::post("selectdata");

        $this->file = VARS::file("FILE");

        $this->field = array(
            "PROC_TYPE"        =>  VARS::post("PROC_TYPE"),         //手続種別
            "PROCEDUREDATE"    =>  VARS::post("PROCEDUREDATE"),     //手続年月日
            "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
            "OUTPUT"           =>  VARS::post("OUTPUT"),            //CSV取込書出種別
        );

        //学科リスト
        $this->examTypeList = array(
            "1" => "普通科",
            "2" => "工業科",
        );

        //手続種別ラジオボタン 1:入学申込金 2:施設設備費 3:入学確約書
        $this->procTypeList = array(
            "1" => "入学申込金",
            "2" => "施設設備費",
            "3" => "入学確約書",
        );
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        knjl580iQuery::getUpdateQuery($db, $this);

        //必要な全ての手続きが終了した受験者に対して最終的な手続終了フラグをセット
        knjl580iQuery::getUpdateProcedureDivQuery($db, $this);

        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);
    }

    //CSV取込
    public function &getCsvInputModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ファイルの文字列の取得
        $data_arr = array();
        $this->err_data = array();
        $data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($data_arr) && !isset($this->warning)) {
            $cnt = knjl580iQuery::updateQueryCsv($db, $this, $data_arr);

            $erraDatas = "";
            foreach ($this->err_data as $errKey => $errVal) {
                $erraDatas .= $errKey . "行目　" . $errVal["MSG"] . "\\n";
            }
            $this->setMessage("MSG202", "処理済件数　：" . $cnt . "件\\nエラー件数    ：" . count($this->err_data) . "件\\n" . $erraDatas);

            //必要な全ての手続きが終了した受験者に対して最終的な手続終了フラグをセット
            knjl580iQuery::getUpdateProcedureDivQuery($db, $this);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["TESTDIV"]         = array(2,      "HISSU",    "INT2",     "入試区分");
            $checkdata["RECEPTNO"]        = array(4,      "HISSU",    "INT2",     "受験番号");

            $csvFormat = $this->getCsvFormat();

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl580iQuery::deleteQueryErr();
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
                    //配列の要素数チェック
                    $checkcount = count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }

                    $cnt = 0;
                    $data = array();
                    $ebdata = array();
                    foreach ($csvFormat as $fieldKey => $fieldVal) {
                        //ホワイトスペースを取り除く
                        $ebdata[$fieldKey] = trim($tmp[$fieldVal["CSV_NO"]]);
                    }

                    //No.22 摘要 入試区分(2桁)＋受験者番号(4桁)＋受験者氏名（カナ）(50桁)
                    $testdiv = mb_substr($ebdata["EB_DATA_22"], 0, 2);
                    $receptno = mb_substr($ebdata["EB_DATA_22"], 2, 4);
                    $name = mb_substr($ebdata["EB_DATA_22"], 6, 50);

                    $data["TESTDIV"]   = $testdiv;
                    $data["RECEPTNO"]  = $receptno;
                    $data["NAME"]      = $name;
                    $data["ERROR"]     = "0"; // 入力チェック用の要素

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
    public function errCheck($db, &$data, $data_each, $line)
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

            //入試区分
            if ($key == "TESTDIV" && $this->testdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }

            //受験番号
            if ($key == "RECEPTNO" && $data[$key] != "") {
                $query = knjl580iQuery::checkReceptNo($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg . $existenceErrMsg . $numericErrMsg . $lengthErrMsg . $otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
            knjl580iQuery::insertQueryErr($db, $line, $check_error);
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
        $csvInfo["EB_DATA_1"]    = array("CSV_NO" => "0",  "TITLE" => "項目名称");
        $csvInfo["EB_DATA_2"]    = array("CSV_NO" => "1",  "TITLE" => "月");
        $csvInfo["EB_DATA_3"]    = array("CSV_NO" => "2",  "TITLE" => "日");
        $csvInfo["EB_DATA_4"]    = array("CSV_NO" => "3",  "TITLE" => "時");
        $csvInfo["EB_DATA_5"]    = array("CSV_NO" => "4",  "TITLE" => "分");
        $csvInfo["EB_DATA_6"]    = array("CSV_NO" => "5",  "TITLE" => "連絡先名");
        $csvInfo["EB_DATA_7"]    = array("CSV_NO" => "6",  "TITLE" => "金融機関名");
        $csvInfo["EB_DATA_8"]    = array("CSV_NO" => "7",  "TITLE" => "支店名");
        $csvInfo["EB_DATA_9"]    = array("CSV_NO" => "8",  "TITLE" => "口座番号区分");
        $csvInfo["EB_DATA_10"]   = array("CSV_NO" => "9",  "TITLE" => "口座種別");
        $csvInfo["EB_DATA_11"]   = array("CSV_NO" => "10", "TITLE" => "口座番号");
        $csvInfo["EB_DATA_12"]   = array("CSV_NO" => "11", "TITLE" => "再送表示");
        $csvInfo["EB_DATA_13"]   = array("CSV_NO" => "12", "TITLE" => "取引先名");
        $csvInfo["EB_DATA_14"]   = array("CSV_NO" => "13", "TITLE" => "取引番号");
        $csvInfo["EB_DATA_15"]   = array("CSV_NO" => "14", "TITLE" => "明細区分");
        $csvInfo["EB_DATA_16"]   = array("CSV_NO" => "15", "TITLE" => "取扱日付月");
        $csvInfo["EB_DATA_17"]   = array("CSV_NO" => "16", "TITLE" => "取扱日付日");
        $csvInfo["EB_DATA_18"]   = array("CSV_NO" => "17", "TITLE" => "起算日月");
        $csvInfo["EB_DATA_19"]   = array("CSV_NO" => "18", "TITLE" => "起算日日");
        $csvInfo["EB_DATA_20"]   = array("CSV_NO" => "19", "TITLE" => "金額");
        $csvInfo["EB_DATA_21"]   = array("CSV_NO" => "20", "TITLE" => "小切手区分");
        $csvInfo["EB_DATA_22"]   = array("CSV_NO" => "21", "TITLE" => "摘要");
        $csvInfo["EB_DATA_23"]   = array("CSV_NO" => "22", "TITLE" => "取立枚数");
        $csvInfo["EB_DATA_24"]   = array("CSV_NO" => "23", "TITLE" => "取立番号");
        $csvInfo["EB_DATA_25"]   = array("CSV_NO" => "24", "TITLE" => "需要家番号");
        $csvInfo["EB_DATA_26"]   = array("CSV_NO" => "25", "TITLE" => "金融機関名");
        $csvInfo["EB_DATA_27"]   = array("CSV_NO" => "26", "TITLE" => "支店名");
        $csvInfo["EB_DATA_28"]   = array("CSV_NO" => "27", "TITLE" => "振込メッセージ");
        $csvInfo["EB_DATA_29"]   = array("CSV_NO" => "28", "TITLE" => "備考");
        $csvInfo["EB_DATA_30"]   = array("CSV_NO" => "29", "TITLE" => "件数");
        return $csvInfo;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //初期化
        $sample = "";
        $contents = "";
        $header_data = false;
        $header = array();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //CSVフォーマット
        $csvFormat = $this->getCsvFormat();
        foreach ($csvFormat as $fieldKey => $fieldVal) {
            $hissuMark = "";
            $header[$fieldKey] = $hissuMark.$fieldVal["TITLE"];
        }

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        if ($this->field["OUTPUT"] == "2") {
            //ヘッダー出力（タイトル行のみ）
            $contents .= implode(",", $header) . "\n";

            $sample = "（サンプル）";
            $header_data = true;
        } elseif ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode(",", $header_err)."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] == "3") {
            //エラー出力
            $query = knjl580iQuery::selectCsvErrQuery();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header_err as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";
                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //エラー出力
            if ($this->field["OUTPUT"] == "3") {
                $filename = sprintf("%s.csv", "エラー出力");
            } else {
                $title = $this->examYear.'年度'.'手続終了入力'.$sample;
                $filename = sprintf("%s.csv", $title);
            }
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //受験者毎にどの手続きが必要かを返す関数
    public function getNecessaryProcTypes($baseData)
    {
        $procTypeArr = array();
        if ($baseData["SP_CD"] != "" && $baseData["SP_REASON_CD"] != "") {
            //特待生に紐付く特待コードの"入学申込金"が設定されている場合は手続きが必要
            if ($baseData["ENT_FEE"] > 0) {
                $procTypeArr[] = "1";
            }

            //特待生に紐付く特待コードの"施設設備費"が設定されている場合は手続が必要
            if ($baseData["FACILITY_FEE"] > 0) {
                $procTypeArr[] = "2";
            }

            //特待生に紐付く特待理由の"入学確約書"が設定されている場合は手続が必要
            if ($baseData["ENT_PROMISE_FLG"] == "1") {
                $procTypeArr[] = "3";
            }
        } else {
            //非特待者は「入学申込金」「施設設備費」の手続が必要
            $procTypeArr[] = "1";
            $procTypeArr[] = "2";
        }

        return $procTypeArr;
    }
}
