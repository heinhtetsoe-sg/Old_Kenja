<?php
class knjl590iModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $examYear;
    public $file;
    public $field = array();
    public $selectdata;
    public $err_data;

    public function knjl590iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
        }

        $this->cmd = VARS::request("cmd");

        $this->examYear = CTRL_YEAR + 1;

        $this->applicantdiv = "2";

        $this->selectdata = VARS::post("selectdata");

        $this->file = VARS::file("FILE");

        $this->field = array(
            "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
            "OUTPUT"           =>  VARS::post("OUTPUT")             //CSV取込書出種別
        );
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //更新
        knjl590iQuery::getUpdateQuery($this);
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
        if ($this->isDataMatched($data_arr) && !isset($this->warning)) {
            $cnt = knjl590iQuery::updateQueryCsv($this, $data_arr);

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
            $checkdata["EXAMNO"]          = array(4,      "HISSU",    "INT2",     "受験番号");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl590iQuery::deleteQueryErr();
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
                    $data["EXAMNO"]    = $tmp[$cnt++];
                    $data["NAME"]      = $tmp[$cnt++];
                    $data["ERROR"]     = "0"; // 入力チェック用の要素

                    $dataCnt = count($data) - 1;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

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

            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }

            //受験番号
            if ($key == "EXAMNO" && $data[$key] != "") {
                $query = knjl590iQuery::checkExamNo($this, $data[$key]);
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
            knjl590iQuery::insertQueryErr($db, $line, $check_error);
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
        //初期化
        $sample = "";
        $contents = "";
        $header_data = false;
        $header = array();

        //ヘッダ
        $header["EXAMNO"]          = "受験番号";
        $header["NAME"]            = "氏名";

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダー例
        $header_rei["EXAMNO"]      = "1001";
        $header_rei["NAME"]        = "志願者　太郎";

        //データ書出
        if ($this->field["OUTPUT"] == "2") {
            //ヘッダ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode(",", $header) . "\n";
            }
        //ヘッダ出力
        } elseif ($this->field["OUTPUT"] == "3") {
            $contents .= implode(",", $header) . "\n";
            $header_data = true;
        //エラー出力
        } elseif ($this->field["OUTPUT"] == "4") {
            $contents .= implode(",", $header_err)."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //データ書出
        if ($this->field["OUTPUT"] == "2") {
            $query = knjl590iQuery::getBaseDatData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //手続終了者一覧(左側)
                if ($row["ENTDIV"] == "2") {
                    $contents .= $row["EXAMNO"].",";
                    $contents .= $row["NAME"];
                    $contents .= "\n";
                    $header_data = true;
                }
            }
            $result->free();
        //ヘッダ出力
        } elseif ($this->field["OUTPUT"] == "3") {
            $sample = "（サンプル）";
            $sep = "";
            foreach ($header_rei as $field => $label) {
                $contents .= $sep.$label;
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        //エラー出力
        } elseif ($this->field["OUTPUT"] == "4") {
            // エラー出力
            $query = knjl590iQuery::selectCsvErrQuery();
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

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "4") {
                $filename = sprintf("%s.csv", "エラー出力");
            } else {
                $title = $this->examYear . "年度" . "入学辞退入力".$sample;
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
