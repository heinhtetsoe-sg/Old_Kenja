<?php

require_once('for_php7.php');
class knjl521hModel extends model
{
    public $field      = array();
    public $koumoku    = array();
    public $cmd;
    public $year;
    public $applicantdiv;
    public $distinctId;        //入試判別
    public $s_examno;
    public $e_examno;
    public $lastColumn;//CSVの最後の列の値

    public function knjl521hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->distinctId);
            unset($this->s_examno);
            unset($this->e_examno);
        }

        $this->exam_type    = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        //Windowリサイズ用
        $this->windowWidth  = VARS::post("windowWidth");
        $this->windowHeight = VARS::post("windowHeight");

        $this->field = array("HEADER"           =>  VARS::post("HEADER")             //ヘッダ有
                            );

        $temp = VARS::request("YEAR");
        if (isset($temp)) {
            $this->year = VARS::request("YEAR");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("DISTINCT_ID");
        if (isset($temp)) {
            $this->distinctId = VARS::request("DISTINCT_ID");
        }

        $this->year         = strlen(VARS::post("YEAR"))         ? VARS::post("YEAR")         : VARS::post("HID_YEAR");
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->distinctId   = strlen(VARS::post("DISTINCT_ID"))  ? VARS::post("DISTINCT_ID")  : VARS::post("HID_DISTINCT_ID");
        $this->s_examno     = strlen(VARS::post("S_EXAMNO"))     ? VARS::post("S_EXAMNO")     : VARS::post("HID_S_EXAMNO");
        $this->e_examno     = strlen(VARS::post("E_EXAMNO"))     ? VARS::post("E_EXAMNO")     : VARS::post("HID_E_EXAMNO");

        $this->hid_examno       = VARS::post("HID_EXAMNO");

        $this->confData = array();
        $this->arrayExam = explode(",", $this->hid_examno);
        foreach ($this->arrayExam as $key => $examno) {
            foreach ($this->koumoku as $key => $val) {
                $this->confData[$examno][$key] = VARS::post($key."-".$examno);
            }
        }

        $this->file = VARS::file("FILE");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        //空文字チェック
        if (strlen($this->applicantdiv) == 0) {
            $this->setWarning("MSG301", "( 学校種別 )");
            return false;
        }
        if (strlen($this->distinctId) == 0) {
            $this->setWarning("MSG301", "( 入試判別 )");
            return false;
        }
        //文字数チェック + 合計セット
        foreach ($this->arrayExam as $key => $examno) {
            if (strlen($this->confData[$examno]["DET001_1"]) > 9) {
                $this->setWarning("MSG915", "( その他は9バイトまでです。)");
                return false;
            }
            // 合計(内申)
            if ($this->confData[$examno]["TOTAL_ALL"] == "") {
                // 中学の時かつ４、５、６年がセットされている時、
                if ($this->applicantdiv == "1" &&
                    $this->confData[$examno]["DET001_6"] != "" &&
                    $this->confData[$examno]["DET001_7"] != "" &&
                    $this->confData[$examno]["DET001_8"] != ""
                    ) {
                    $this->confData[$examno]["TOTAL_ALL"] = $this->confData[$examno]["DET001_6"] + $this->confData[$examno]["DET001_7"] + $this->confData[$examno]["DET001_8"];
                } else {
                    $confTotal = 0;
                    foreach ($this->subConf as $fieldName => $name) {
                        $confTotal += $this->confData[$examno][$fieldName];
                    }
                    $this->confData[$examno]["TOTAL_ALL"] = $confTotal;
                }
            }
            // 合計(行動、特別、出席)
            if ($this->confData[$examno]["DET001_5"] == "") {
                $this->confData[$examno]["DET001_5"] = $this->confData[$examno]["DET001_2"] + $this->confData[$examno]["DET001_3"] + $this->confData[$examno]["DET001_4"];
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //更新
        //入力チェック
        if ($this->validateInput("update")) {
            knjl521hQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //CSV取込
    public function &getCsvInputModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->err_data = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $cnt = knjl521hQuery::insertQueryCsv($this, $this->data_arr);

            $erraDatas = "";
            foreach ($this->err_data as $errKey => $errVal) {
                $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
            }
            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".get_count($this->err_data)."件\\n".$erraDatas);
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
            $checkdata["ENTEXAMYEAR"]   = array(4,      "HISSU",    "INT",  "入試年度");
            $checkdata["APPLICANTDIV"]  = array(1,      "HISSU",    "INT",  "学校種別");
            $checkdata["DISTINCT_ID"]   = array(3,      "HISSU",    "INT",  "入試判別");
            $checkdata["EXAMNO"]        = array(10,     "HISSU",    "",     "受験番号");
            foreach ($this->koumoku as $fieldName => $nameVal) {
                if ($fieldName == "TOTAL_ALL") {
                    $checkdata[$fieldName]        = array(2,     "",    "INT",     "内申合計点");
                } elseif ($fieldName == "DET001_1") {
                    $checkdata[$fieldName]        = array(9,     "",    "INT",     $nameVal);
                } else {
                    $checkdata[$fieldName]        = array(2,     "",    "INT",     $nameVal);
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
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $data = array();
                    $data["ENTEXAMYEAR"]    = $tmp[0];
                    $data["APPLICANTDIV"]   = $tmp[1];
                    $data["DISTINCT_ID"]    = $tmp[2];
                    $data["EXAMNO"]         = $tmp[3];
                    $tmpCol = 8;
                    foreach ($this->koumoku as $fieldName => $nameVal) {
                        $data[$fieldName]      = $tmp[++$tmpCol];
                    }

                    $dataCnt = $tmpCol + 2;

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

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep       = "";
        $existenceSep   = "";
        $existenceSep   = "";
        $numericSep     = "";
        $lengthSep      = "";

        $checkNameMstCd = array("APPLICANTDIV"         => "L003");

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
            if ($key == "ENTEXAMYEAR" && $this->year != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //学校種別
            if ($key == "APPLICANTDIV" && $this->applicantdiv != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //入試判別
            if ($key == "DISTINCT_ID" && $this->distinctId != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //名称マスタ
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjl521hQuery::nameCntSql($this, $checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在(名称マスタ):", $existenceSep, $val[3]);
                }
            }

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl521hQuery::checkExamNo($this, $data[$key]);
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
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
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
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
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
        $header["ENTEXAMYEAR"]      = "入試年度";
        $header["APPLICANTDIV"]     = "学校種別";
        $header["DISTINCT_ID"]      = "入試判別";
        $header["EXAMNO"]           = "受験番号";
        $header["SHIGANSYA_SEQ"]    = "志願者SEQ";
        $header["MIRAI_TESTDIV"]    = "入試区分";
        $header["NAME"]             = "氏名";
        $header["SEX"]              = "性別";
        $header["TEST_NAME_ABBV"]   = "試験名略称";
        foreach ($this->koumoku as $fieldName => $nameVal) {
            if ($fieldName == "TOTAL_ALL") {
                $setName = "内申合計点";
            } else {
                $setName = $nameVal;
            }
            $header[$fieldName]        = $setName;
        }
        $header[$this->lastColumn]  = $this->lastColumn;

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //データ出力
        $data = false;
        $query = knjl521hQuery::selectQuery($this, "csv");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($header as $field => $label) {
                // 内申データが無いときは、「3」をセットする。
                if ($row["TOTAL_ALL"] == "") {
                    $row["TOTAL_ALL"] = "3";
                }

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
            $title = $this->year."年度"."内申点入力";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
