<?php
class knjl418hModel extends model
{
    public $cmd;
    public $examYear;
    public $applicantdiv;
    public $examcoursecd;
    public $file;
    public $field;
    public $err_data;
    public $hid_examno;
    public $updateData;

    public function knjl418hModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->examcoursecd);
        }

        $this->cmd = VARS::request("cmd");
        $this->examYear     = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::request("APPLICANTDIV");
        $this->examcoursecd = VARS::request("EXAMCOURSECD");
        $this->file         = VARS::file("FILE");
        $this->field        = array("HEADER" => VARS::post("HEADER"),            //ヘッダ有
                                    "OUTPUT" => VARS::post("OUTPUT"),            //CSV取込書出種別
                                );
        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $examnoArray = explode(",", $this->hid_examno);
        $this->updateData = array();
        if (count($examnoArray) > 0) {
            foreach ($examnoArray as $key => $examno) {
                $this->updateData[$examno]["EXAMNO"]      = $examno;
                $this->updateData[$examno]["HANTEI_LAST"] = VARS::post("HANTEI_LAST-{$examno}");
                $this->updateData[$examno]["KUBUN"]       = VARS::post("KUBUN-{$examno}");
            }
        }

        knjl418hQuery::getUpdateQuery($this);
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
            $cnt = knjl418hQuery::updateQueryCsv($this, $data_arr);

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
            $checkdata["APPLICANTDIV"]   = array(4,      "HISSU",    "INT",  "入試年度");
            $checkdata["COURSE_CD"]      = array(2,      "HISSU",    "INT",  "受験コース");
            $checkdata["EXAMNO"]         = array(4,      "HISSU",    "INT",  "志願者SEQ");
            $checkdata["HANTEI_LAST"]    = array(1,      "",         "INT",  "最終特別奨学生");
            $checkdata["KUBUN"]          = array(1,      "",         "INT",  "最終特別奨学生区分");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //入試回数取得
            $headerKai = array();
            $result = $db->query(knjl418hQuery::getSettingMst($this, "L004"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $headerKai[$row["VALUE"]] = $row["NAME1"];
            }
            $result->free();

            //最終特別奨学生判定リストを取得
            $hanteiList = array(
                "1" => "全額",
                "2" => "半額");

            //最終特別奨学生区分リストを取得
            $kubunList = array();
            $result = $db->query(knjl418hQuery::getSettingMst($this, "L102"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $kubunList[$row["VALUE"]] = $row["NAME1"];
            }
            $result->free();

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
                    $data["ENTEXAMYEAR"]   = $tmp[$cnt++];
                    $data["APPLICANTDIV"]  = $tmp[$cnt++];
                    $data["COURSE_CD"]     = $tmp[$cnt++];
                    $data["EXAMNO"]        = $tmp[$cnt++];
                    $data["NAME"]          = $tmp[$cnt++];
                    foreach ($headerKai as $key => $val) {
                        $data["HANTEI_{$key}"] = $tmp[$cnt++];
                    }
                    $data["HANTEI_LAST"]   = $tmp[$cnt++];
                    $data["KUBUN"]         = $tmp[$cnt++];

                    $dataCnt = count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $hanteiList, $kubunList)) {
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
    public function errCheck($db, $data, $data_each, $line, $hanteiList, $kubunList)
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
            if ($key == "ENTEXAMYEAR" && $this->examYear != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //志願者SEQ
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl418hQuery::selectQuery($this, $data[$key]);
                $rowCount = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($rowCount)) {
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

            //最終特別奨学生判定
            if ($key == "HANTEI_LAST" && $data[$key] != "") {
                $hanteiStr = "";
                $hanteiFlg = false;
                foreach ($hanteiList as $cd => $value) {
                    if ($hanteiStr != "") {
                        $hanteiStr .= "、";
                    }
                    $hanteiStr .= $cd.":".$value;
                    if ($cd == $data[$key]) {
                        $hanteiFlg = true;
                    }
                }
                if (!$hanteiFlg) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※最終特別奨学生区分: {$hanteiStr} のいずれかを入力してください。", $otherSep, $val[3]);
                }
            }

            //最終特別奨学生区分
            if ($key == "KUBUN" && $data[$key] != "") {
                $kubunStr = "";
                $kubunFlg = false;
                foreach ($kubunList as $cd => $value) {
                    if ($kubunStr != "") {
                        $kubunStr .= "、";
                    }
                    $kubunStr .= $cd.":".$value;
                    if ($cd == $data[$key]) {
                        $kubunFlg = true;
                    }
                }
                if (!$kubunFlg) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※最終特別奨学生区分: {$kubunStr} のいずれかを入力してください。", $otherSep, $val[3]);
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

        //入試回数取得
        $headerKai = array();
        $result = $db->query(knjl418hQuery::getSettingMst($this, "L004"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $headerKai[$row["VALUE"]] = $row["NAME1"];
        }
        $result->free();

        //初期化
        $contents = "";

        //ヘッダ
        $header = array();
        $header["ENTEXAMYEAR"]     = "入試年度";
        $header["APPLICANTDIV"]    = "入試制度";
        $header["COURSE_CD"]       = "受験コース";
        $header["EXAMNO"]          = "志願者SEQ";
        $header["NAME"]            = "氏名";
        foreach ($headerKai as $key => $val) {
            $header["HANTEI_{$key}"]   = $val;
        }
        $header["HANTEI_LAST"]     = "最終特別奨学生";
        $header["KUBUN"]           = "最終特別奨学生区分";

        //ヘッダ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header) . "\n";
        }

        //データ出力
        $data = false;
        $query = knjl418hQuery::selectQuery($this);
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

        //入試制度名称取得
        $query = knjl418hQuery::getNameMst($this->examYear, "L003", $this->applicantdiv);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $applicantdivName = $row["NAME1"];

        //受験コース名称取得
        $query = knjl418hQuery::getExamCourseMst($this, $this->examcoursecd);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $examcourseName = $row["EXAMCOURSE_NAME"];

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            //CSVファイル名
            $title = $this->examYear . "年度 ".$applicantdivName." 入試 ".$examcourseName." 最終特別奨学生入力";
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
