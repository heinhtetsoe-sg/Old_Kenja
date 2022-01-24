<?php

require_once('for_php7.php');

class knjl550aModel extends model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $applicantdiv;
    public $ObjYear;
    public $hid_examno;
    public $hid_classcd;
    public $file;
    public $err_data;
    public $csvField = array();

    public function knjl550aModel()
    {
    }

    public function init()
    {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->field = array("TESTDIV"      =>  VARS::post("TESTDIV"),      //受験区分
                             "DESIREDIV"    =>  VARS::post("DESIREDIV"),    //志望区分
                             "S_EXAMNO"     =>  VARS::post("S_EXAMNO"),     //受験番号 FROM
                             "E_EXAMNO"     =>  VARS::post("E_EXAMNO"),     //受験番号 TO
                             "SORT"         =>  VARS::post("SORT"),         //ソート順
                             "DIV"          =>  VARS::post("DIV")           //入力教科
                             );

        //ajax
        if (VARS::post("AJAX_YEAR")) {
            $this->ajaxParam = array(
                "AJAX_YEAR"         => VARS::post("AJAX_YEAR"),
                "AJAX_REMARK"       => VARS::post("AJAX_REMARK")
            );
        }

        $this->hid_examno       = VARS::post("HID_EXAMNO");
        $this->hid_classcd      = VARS::post("HID_CLASSCD");

        $array       = explode(",", $this->hid_examno);
        $class_array = explode(",", $this->hid_classcd);
        foreach ($array as $key => $examno) {
            foreach ($class_array as $key => $classCd) {
                $this->data["SCORE".$classCd][$examno]  = strlen(VARS::post("HID_SCORE".$classCd."_".$examno)) ? VARS::post("HID_SCORE".$classCd."_".$examno) : VARS::post("SCORE".$classCd."_".$examno);
            }
            $this->data["REMARK1"][$examno]           = strlen(VARS::post("HID_REMARK1_".$examno)) ? VARS::post("HID_REMARK1_".$examno) : VARS::post("REMARK1_".$examno);
            $this->data["REMARK2"][$examno]           = strlen(VARS::post("HID_REMARK2_".$examno)) ? VARS::post("HID_REMARK2_".$examno) : VARS::post("REMARK2_".$examno);
            $this->data["REMARK3"][$examno]           = strlen(VARS::post("HID_REMARK3_".$examno)) ? VARS::post("HID_REMARK3_".$examno) : VARS::post("REMARK3_".$examno);
            $this->data["REMARK4"][$examno]           = strlen(VARS::post("HID_REMARK4_".$examno)) ? VARS::post("HID_REMARK4_".$examno) : VARS::post("REMARK4_".$examno);
        }

        $this->file = VARS::file("FILE");

        $this->csvField = array(
            "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
            "OUTPUT"           =>  VARS::post("OUTPUT"),            //CSV取込書出種別
        );

        //ラジオボタン切替時直前のコマンド保持(読込み、前へ、後ろへ)
        if ($this->cmd == "main" || $this->cmd == "") {
            $this->hoji_cmd = "";
        } elseif ($this->cmd == "search" || $this->cmd == "back" || $this->cmd == "next") {
            $this->hoji_cmd = $this->cmd;
        }
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //更新
        knjl550aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $data_arr = array();
        $this->err_data = array();
        $data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($data_arr)) {
            $examnoArray = array();
            foreach ($data_arr as $val) {
                $examnoArray[] = $val["EXAMNO"];
            }

            //入力値の重複チェック
            $temp = array_count_values($examnoArray);
            foreach ($temp as $key => $val) {
                if (strlen($key) && $val > 1) {
                    $this->setWarning("MSG203", "取込データ内で受験番号".$key."が重複していたため処理を中止しました。");
                    return;
                }
            }

            $cnt = knjl550aQuery::updateQueryCsvHeigan($this, $data_arr);

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
            $checkdata["EXAMNO"]         = array(5,      "HISSU",    "INT2",  "受験番号");
            $checkdata["SCHOOLCD1"]      = array(3,      "",         "INT2",  "学校コード1");
            $checkdata["SCHOOLCD2"]      = array(3,      "",         "INT2",  "学校コード2");
            $checkdata["SCHOOLCD3"]      = array(3,      "",         "INT2",  "学校コード3");
            $checkdata["SCHOOLCD4"]      = array(3,      "",         "INT2",  "学校コード4");

            $csvFormat = $this->getCsvFormat();

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl550aQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->csvField["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = count($tmp);

                    $data = array();
                    foreach ($csvFormat as $fieldKey => $fieldVal) {
                        //ホワイトスペースを取り除く
                        $data[$fieldKey] = trim($tmp[$fieldVal["CSV_NO"]]);
                    }

                    $dataCnt = count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $schoolcdErrArray)) {
                        $data["SCHOOLCD_ERROR"] = $schoolcdErrArray;
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
    //(schoolcdErrArray: 学校コードエラー配列)
    public function errCheck($db, &$data, $checkdata, $line, &$schoolcdErrArray)
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

        $examnoErrFlg = false; //受験番号エラーフラグ

        //エラーチェック
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        //受験番号
        $query = knjl550aQuery::checkExamNo($this, $data["EXAMNO"]);
        $rowCount = $db->getOne($query);
        if ($rowCount == 0) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "", $existenceSep, "受験番号の一致する志願者が存在しません。");
        }

        if ($returnFlg && !$examnoErrFlg) {
            $query = knjl550aQuery::checkKesseki($this, $data["EXAMNO"]);
            $judgediv = $db->getOne($query);
            if ($judgediv == "4") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "", $otherSep, "受験番号{$data["EXAMNO"]}：欠席者のため登録できません。");
            }
        }

        $schoolcdErrArray = array();
        $schoolcdErrCnt = 0;
        if ($returnFlg && !$examnoErrFlg) {
            //学校コード1、学校コード2、学校コード3、学校コード4
            for ($i = 1; $i <= 4; $i++) {
                $schoolcdErrArray[$i] = false;
                if ($data["SCHOOLCD{$i}"] != "") {
                    $query = knjl550aQuery::checkSchoolcd($this, $data["SCHOOLCD{$i}"]);
                    $rowCount = $db->getOne($query);
                    if ($rowCount == 0) {
                        //学校コードチェック処理がエラーの場合、 returnFlg を false に設定しない
                        $this->errSet($otherErrFlg, $otherErrMsg, "", $otherSep, "受験番号{$data["EXAMNO"]}：学校コード{$i}に対応するコードが設定されていません。");
                        //学校コードエラー配列
                        $schoolcdErrArray[$i] = true;
                        $schoolcdErrCnt++;
                    }
                }
            }
        }

        if (!$returnFlg || $schoolcdErrCnt > 0) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
            knjl550aQuery::insertQueryErr($db, $line, $check_error);
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
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }

    //CSVフォーマット
    public function getCsvFormat()
    {
        $csvInfo = array();

        $csvInfo["GRADE"]         = array("CSV_NO" => "0",  "TITLE" => "年");
        $csvInfo["CLASS"]         = array("CSV_NO" => "1",  "TITLE" => "クラス");
        $csvInfo["EXAMNO"]        = array("CSV_NO" => "2",  "TITLE" => "番号");
        $csvInfo["SCHOOLCD1"]     = array("CSV_NO" => "3",  "TITLE" => "学校コード1");
        $csvInfo["JUKEN1"]        = array("CSV_NO" => "4",  "TITLE" => "受験方法1");
        $csvInfo["GOUHI1"]        = array("CSV_NO" => "5",  "TITLE" => "合否1");
        $csvInfo["NYUGAKU1"]      = array("CSV_NO" => "6",  "TITLE" => "入学1");
        $csvInfo["SCHOOLCD2"]     = array("CSV_NO" => "7",  "TITLE" => "学校コード2");
        $csvInfo["JUKEN2"]        = array("CSV_NO" => "8",  "TITLE" => "受験方法2");
        $csvInfo["GOUHI2"]        = array("CSV_NO" => "9",  "TITLE" => "合否2");
        $csvInfo["NYUGAKU2"]      = array("CSV_NO" => "10", "TITLE" => "入学2");
        $csvInfo["SCHOOLCD3"]     = array("CSV_NO" => "11", "TITLE" => "学校コード3");
        $csvInfo["JUKEN3"]        = array("CSV_NO" => "12", "TITLE" => "受験方法3");
        $csvInfo["GOUHI3"]        = array("CSV_NO" => "13", "TITLE" => "合否3");
        $csvInfo["NYUGAKU3"]      = array("CSV_NO" => "14", "TITLE" => "入学3");
        $csvInfo["SCHOOLCD4"]     = array("CSV_NO" => "15", "TITLE" => "学校コード4");
        $csvInfo["DUMMY"]         = array("CSV_NO" => "16", "TITLE" => "DUMMY");
        return $csvInfo;
    }

    //CSV出力
    public function getDownloadModel()
    {
        //DB接続
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
            if (in_array($fieldKey, array("EXAMNO"))) {
                $hissuMark = "※";
            }
            $header[$fieldKey] = $hissuMark.$fieldVal["TITLE"];
        }

        if ($this->csvField["OUTPUT"] == "2") {
            //ヘッダー出力
            $contents .= implode(",", $header) . "\n";
        } elseif ($this->csvField["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode(",", $header_err)."\n";
        }

        //ヘッダー例
        $header_rei["GRADE"]         = "01";
        $header_rei["CLASS"]         = "01";
        $header_rei["EXAMNO"]        = "10001";
        $header_rei["SCHOOLCD1"]     = "001";
        $header_rei["JUKEN1"]        = "";
        $header_rei["GOUHI1"]        = "";
        $header_rei["NYUGAKU1"]      = "";
        $header_rei["SCHOOLCD2"]     = "002";
        $header_rei["JUKEN2"]        = "";
        $header_rei["GOUHI2"]        = "";
        $header_rei["NYUGAKU2"]      = "";
        $header_rei["SCHOOLCD3"]     = "003";
        $header_rei["JUKEN3"]        = "";
        $header_rei["GOUHI3"]        = "";
        $header_rei["NYUGAKU3"]      = "";
        $header_rei["SCHOOLCD4"]     = "003";
        $header_rei["DUMMY"]         = "DUMMY";

        if ($this->csvField["OUTPUT"] == "2") {
            //ヘッダー出力
            $sample = "（サンプル）";
            $sep = "";
            foreach ($header_rei as $field => $label) {
                $contents .= $sep.$label;
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        } elseif ($this->csvField["OUTPUT"] == "3") {
            // エラー出力
            $query = knjl550aQuery::selectCsvErrQuery();
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
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            if ($this->csvField["OUTPUT"] == "3") {
                $filename = sprintf("%s.csv", "エラー出力");
            } else {
                $title = $this->ObjYear."年度入試_併願校入力".$sample;
                $filename = sprintf("%s.csv", $title);
            }
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
