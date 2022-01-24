<?php
class knjl052oModel extends Model
{
    public $field      = array();
    public $cmd;
    public $file;
    public $ObjYear;           //入試年度
    public $applicantdiv;      //入試制度
    public $exam_type;
    public $auth;
    public $err_data;

    public function knjl052oModel()
    {
    }

    public function init()
    {
        $this->auth = AUTHORITY;

        $this->cmd = VARS::request("cmd");
        $this->ObjYear = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV"); //固定
        $this->exam_type = 2; //固定

        $this->field = array(
            "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
            "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
        );

        $this->file = VARS::file("FILE");
    }

    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->err_data = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjl052oQuery::insertQueryCsv($this, $this->data_arr);
            } else {
                $cnt = knjl052oQuery::deleteQueryCsv($this, $this->data_arr);
            }

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
            $checkdata["RECEPTNO"]       = array(4,      "HISSU",    "INT",  "受験番号");
            $checkdata["SCORE"]          = array(3,      "",         "INT",  "合計得点");
            $checkdata["TESTDIV"]        = array(1,      "HISSU",    "INT",  "入学試験コード");
            $checkdata["TESTSUBCLASSCD"] = array(1,      "HISSU",    "INT",  "教科");

            $csvFormat = $this->getCsvFormat();

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

                    $data = array();
                    foreach ($csvFormat as $fieldKey => $fieldVal) {
                        //受付番号はの後ろ4桁だけを取込
                        if ($fieldKey == "RECEPTNO") {
                            $tmp[$fieldVal["CSV_NO"]] = substr(trim($tmp[$fieldVal["CSV_NO"]]), -4);
                        }
                        $mapping = array();
                        $mapping["1"] = "0"; //第１回入試
                        $mapping["5"] = "1"; //特待生選抜入試
                        $mapping["2"] = "2"; //第２回入試
                        $mapping["9"] = "3"; //感染症対応入試
                        if ($fieldKey == "TESTDIV") {
                            //入学試験コードを賢者の入試区分に変換
                            $inputTestDiv = trim($tmp[$fieldVal["CSV_NO"]]);
                            $tmp[$fieldVal["CSV_NO"]] = (isset($mapping[$inputTestDiv])) ? $mapping[$inputTestDiv] : "";
                        }
                        if ($fieldKey == "TESTSUBCLASSCD") {
                            //3桁の英数字入力の2桁目を科目コードとして取込
                            $tmp[$fieldVal["CSV_NO"]] = substr(trim($tmp[$fieldVal["CSV_NO"]]), 1, 1);
                        }
                        //ホワイトスペースを取り除く
                        $data[$fieldKey] = trim($tmp[$fieldVal["CSV_NO"]]);
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
    public function errCheck($db, &$data, $checkdata, $line)
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

        $testdivErrFlg = false; //入学試験コードエラーフラグ
        $testsubclasscdErrFlg = false; //エラーフラグ

        //エラーチェック
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }
            //数値チェック
            if ($val[2] == "INT" && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "" && $this->dateRep($data[$key]) == "") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }

            //入学試験コード
            if ($key == "TESTDIV" && $data[$key] != "") {
                $query = knjl052oQuery::checkTestdiv($this, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount <= 0) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    $testdivErrFlg = true;
                }
            }

            //科目コード
            if ($key == "TESTSUBCLASSCD" && $data[$key] != "") {
                $query = knjl052oQuery::nameCntSql($this, "L009", $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount <= 0) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    $testsubclasscdErrFlg = true;
                }
            }
        }

        if (!$testdivErrFlg) {

            //受験番号
            $query = knjl052oQuery::getReceptDat($this, $data, $data["RECEPTNO"]);
            $rowRecept = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!is_array($rowRecept)) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "受験番号");
            }
        }

        //更新
        if ($this->field["SHORI_MEI"] == "1") {
            //満点チェック
            if ($data["SCORE"] != "" && !$testdivErrFlg && !$testsubclasscdErrFlg) {
                $query = knjl052oQuery::checkPerfectMst($this, $data["TESTDIV"], $data["TESTSUBCLASSCD"]);
                $row = $db->getrow($query, DB_FETCHMODE_ASSOC);
                if ($data["SCORE"] > (int)$row["PERFECT"]) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※満点:", $otherSep, (int)$row["PERFECT"] . "以下で入力してください");
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
        }
        return $returnFlg;
    }

    //CSVフォーマット
    public function getCsvFormat()
    {
        $csvInfo = array();
        $csvInfo["GYOMUNO"]         = array("CSV_NO" => "0", "TITLE" => "業務データNo");
        $csvInfo["EXAMNO"]          = array("CSV_NO" => "1", "TITLE" => "受付番号(前6桁割愛)");
        $csvInfo["RECEPTNO"]        = array("CSV_NO" => "2", "TITLE" => "受験番号");
        $csvInfo["SCORE"]           = array("CSV_NO" => "3", "TITLE" => "合計得点");
        $csvInfo["TESTDIV"]         = array("CSV_NO" => "4", "TITLE" => "入学試験コード");
        $csvInfo["TESTSUBCLASSCD"]  = array("CSV_NO" => "5", "TITLE" => "教科");
        return $csvInfo;
    }

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
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

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        $header = array();

        //CSVフォーマット
        $csvFormat = $this->getCsvFormat();
        foreach ($csvFormat as $fieldKey => $fieldVal) {
            $hissuMark = "";
            if (in_array($fieldKey, array("EXAMNO", "RECEPTNO", "TESTDIV", "TESTSUBCLASSCD"))) {
                $hissuMark = "※";
            }
            $header[] = $hissuMark.$fieldVal["TITLE"];
        }

        //クリア
        $contents  = "";
        //ヘッダ出力
        $contents .= implode(",", $header)."\n";

        //ファイル名
        $title = $this->ObjYear.'年度 得点CSV取込ヘッダデータ';

        //ＣＳＶ出力
        $filename = sprintf("%s.csv", $title);
        common::downloadContents($contents, $filename);
        return true;
    }
}
//var_dump($_REQUEST);
