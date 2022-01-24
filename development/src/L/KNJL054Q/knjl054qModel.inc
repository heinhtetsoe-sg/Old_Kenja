<?php

require_once('for_php7.php');

class knjl054qModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control; //コントロールマスタ
    public $staffcd; //登録者コード
    public $fieldSize = array();
    public $test_year;

    public function init()
    {
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"         =>  VARS::post("OUTPUT"),        //出力取込種別
                             "APPLICANTDIV"   =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "TESTDIV"        =>  VARS::post("TESTDIV"),       //入試区分
                             "TESTSUBCLASSCD" =>  VARS::post("TESTSUBCLASSCD"),//受験科目
                             "HEADER"         =>  VARS::post("HEADER"),        //ヘッダ有
                             "SHORI_MEI"      =>  VARS::post("SHORI_MEI")      //処理名
                             );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
    }

    /***************/
    /* CSVの取込み */
    /***************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                $cnt = knjl054qQuery::udpateQueryCsv($this, $this->data_arr);
            } else {
//                $cnt = knjl054qQuery::DeleteQueryCsv($this,$this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /***********************/
    /* CSVファイルを配列へ */
    /***********************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();

            //校長推薦、自己推薦
            if ($this->field["TESTDIV"] == "3" || $this->field["TESTDIV"] == "4") {
                $class_array = array("1" => "国語",
                                     "2" => "数学",
                                     "5" => "英語",
                                     "3" => "理科",
                                     "4" => "社会"
                                    );
                $checkdata["EXAMNO"]    = array(5,    "HISSU",  "INT2",  "受験番号");       //0
                foreach ($class_array as $code => $label) {
                    $checkdata["SCORE".$code] = array(2,    "",     "INT",   $label."得点");//101-104
                }
            }

            //一般入試
            if ($this->field["TESTDIV"] == "5") {
                $checkdata["EXAMNO"]    = array(5,    "HISSU",  "INT2",  "受験番号");       //4
                $checkdata["SCORE"]     = array(3,    "",       "INT",   "得点");           //6
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl054qQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
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

                    //校長推薦、自己推薦
                    if ($this->field["TESTDIV"] == "3" || $this->field["TESTDIV"] == "4") {
                        //0
                        $data["EXAMNO"] = (strlen(trim($tmp[0]))) ? sprintf("%05d", $tmp[0]) : "";
                        //1-4
                        $tmpCnt = 0;
                        foreach ($class_array as $code => $label) {
                            $data["SCORE".$code]  = $tmp[++$tmpCnt];
                        }
                    }

                    //一般入試
                    if ($this->field["TESTDIV"] == "5") {
                        //4
                        $data["EXAMNO"] = (strlen(trim($tmp[4]))) ? sprintf("%05d", $tmp[4]) : "";
                        //6
                        $data["SCORE"]  = $tmp[6];
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

    /*****************/
    /*エラーチェック */
    /*****************/
    public function errCheck($db, $data, $checkdata, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $etcErrFlg       = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $etcErrMsg       = "";       //その他エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $etcSep    = "";

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //存在チェック
            if ($key == 'EXAMNO' && strlen($data[$key]) && 0 == $db->getOne(knjl054qQuery::selectMainQuery($this, $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$etcErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl054qQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    /****************************/
    /* エラーメッセージをセット */
    /****************************/
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

    /**************************/
    /* データの整合性チェック */
    /**************************/
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    public function getDownloadModel()
    {
        $header = $header_rei = array();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        //出力用ヘッダー
        $header_output = array("GRADE"         => "学年",
                               "HR_CLASS"      => "組",
                               "EXAMNO"        => "受験番号",
                               "NAME"          => "氏名");

        //校長推薦
        if ($this->field["TESTDIV"] == "3" || $this->field["TESTDIV"] == "4") {
            //科目（フォーマットが決まっているため固定配列とした）
            $class_array = array("1" => "国語",
                                 "2" => "数学",
                                 "5" => "英語",
                                 "3" => "理科",
                                 "4" => "社会"
                                );
            //ヘッダー
            $header["EXAMNO"] = "※受験番号"; //取込No.0
            foreach ($class_array as $code => $label) {
                $header["SCORE".$code] = $label."得点"; //取込No.1-4
            }
            //ヘッダー例
            $header_rei["EXAMNO"] = "99999";
            foreach ($class_array as $code => $label) {
                $header_rei["SCORE".$code] = "99";
            }
        }

        //一般入試
        if ($this->field["TESTDIV"] == "5") {
            //ヘッダー
            $header["TEST_NO"]      = "テスト番号";
            $header["TEST_NAME"]    = "テスト名";
            $header["GRADE"]        = "年";
            $header["HR_CLASS"]     = "組";
            $header["EXAMNO"]       = "※受験番号"; //取込No.4
            $header["NAME"]         = "氏名";
            $header["SCORE"]        = "得点";       //取込No.6
            $header["STD_SCORE"]    = "偏差値";
            $header["SCORE_RATE"]   = "得点率";
            $header["ALL_RANK"]     = "全体順位";
            $header["HR_RANK"]      = "組順位";
            $header["KIJUN"]        = "基準以下";
            //ヘッダー例
            $header_rei["TEST_NO"]      = "-";
            $header_rei["TEST_NAME"]    = "-";
            $header_rei["GRADE"]        = "-";
            $header_rei["HR_CLASS"]     = "-";
            $header_rei["EXAMNO"]       = "99999";  //前0無
            $header_rei["NAME"]         = "-";
            $header_rei["SCORE"]        = "999";
            $header_rei["STD_SCORE"]    = "999";
            $header_rei["SCORE_RATE"]   = "999";
            $header_rei["ALL_RANK"]     = "999";
            $header_rei["HR_RANK"]      = "999";
            $header_rei["KIJUN"]        = "*";
        }

        //ヘッダー部
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            // データ出力
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header_output, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = knjl054qQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjl054qQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode(",", $row) ."\n";
                $header_data = true;
            }

            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {            //エラー
                $couse = "エラー出力";
            } elseif ($this->field["OUTPUT"]=="4") {   //エラー以外
                $couse = "志願者名簿データ";
            } else {
                //校長推薦
                if ($this->field["TESTDIV"] == "3") {
                    $testdivName = "（校長推薦）";
                }
                //自己推薦
                if ($this->field["TESTDIV"] == "4") {
                    $testdivName = "（自己推薦）";
                }
                //一般入試
                if ($this->field["TESTDIV"] == "5") {
                    $testdivName = "（一般入試・基準テスト）";
                }
                $couse = "MarkViewフォーマット".$testdivName;
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}

?>
