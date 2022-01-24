<?php
class knjl632fModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $data_arr = array();
        $data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                // 更新
                $cnt = knjl632fQuery::updateQueryCsv($this, $data_arr);
            }

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne(knjl632fQuery::cntCsvErrQuery());
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            //チェック用 CSV取込項目
            $checkdata = array();
            $checkdata["EXAMNO"]                = array(4,    "HISSU",  "INT",   "受験番号");//半角数字4桁
            $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");//全角20文字以内
            $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "かな氏名");//全角40文字以内

            $checkdata["JITURYOKU_3GOKEI"]      = array(3,    "",       "INT",   "実力テスト3科合計");//整数3桁
            $checkdata["JITURYOKU_3HEIKIN"]     = array(3,    "",       "INT",   "実力テスト3科合計平均");//整数3桁
            $checkdata["JITURYOKU_5GOKEI"]      = array(3,    "",       "INT",   "実力テスト5科合計");//整数3桁
            $checkdata["JITURYOKU_5HEIKIN"]     = array(3,    "",       "INT",   "実力テスト5科合計平均");//整数3桁

            $checkdata["CONFIDENTIAL_RPT01"]    = array(1,    "",       "INT",   "国語");//半角数字1桁、1～5
            $checkdata["CONFIDENTIAL_RPT02"]    = array(1,    "",       "INT",   "社会");//半角数字1桁、1～5
            $checkdata["CONFIDENTIAL_RPT03"]    = array(1,    "",       "INT",   "数学");//半角数字1桁、1～5
            $checkdata["CONFIDENTIAL_RPT04"]    = array(1,    "",       "INT",   "理科");//半角数字1桁、1～5
            $checkdata["CONFIDENTIAL_RPT05"]    = array(1,    "",       "INT",   "音楽");//半角数字1桁、1～5
            $checkdata["CONFIDENTIAL_RPT06"]    = array(1,    "",       "INT",   "美術");//半角数字1桁、1～5
            $checkdata["CONFIDENTIAL_RPT07"]    = array(1,    "",       "INT",   "保健体育");//半角数字1桁、1～5
            $checkdata["CONFIDENTIAL_RPT08"]    = array(1,    "",       "INT",   "技術家庭");//半角数字1桁、1～5
            $checkdata["CONFIDENTIAL_RPT09"]    = array(1,    "",       "INT",   "英語");//半角数字1桁、1～5

            $checkdata["JIZENSODAN_TEXT"]       = array(30,   "",       "",      "事前相談文言");//全角10文字以内
            $checkdata["JIZENSODAN_CD"]         = array(1,    "",       "INT",   "事前相談コード");
            $checkdata["BUKATUDO"]              = array(45,   "",       "",      "部活動");//全角15文字以内
            $checkdata["SHOUGAKU_CD"]           = array(2,    "",       "INT",   "奨学区分コード");//
            $checkdata["BIKOU"]                 = array(120,  "",       "",      "備考");//特別条件：全角40文字以内、特活・部活・特記事項：全角30文字以内

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl632fQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (($buffer = fgetcsv($fp)) !== false) {
                //文字コードを判別、改行コードを削除
                mb_convert_variables(mb_http_output(), "SJIS-win", $buffer);
                $buffer = implode(",", $buffer);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }

                    //チェック用 CSV取込項目
                    $cnt = 0;
                    $data = array();
                    $data["EXAMNO"]                = $tmp[$cnt++];
                    $data["NAME"]                  = $tmp[$cnt++];
                    $data["NAME_KANA"]             = $tmp[$cnt++];
                    $data["JITURYOKU_3GOKEI"]      = $tmp[$cnt++];
                    $data["JITURYOKU_3HEIKIN"]     = $tmp[$cnt++];
                    $data["JITURYOKU_5GOKEI"]      = $tmp[$cnt++];
                    $data["JITURYOKU_5HEIKIN"]     = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT01"]    = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT02"]    = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT03"]    = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT04"]    = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT05"]    = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT06"]    = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT07"]    = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT08"]    = $tmp[$cnt++];
                    $data["CONFIDENTIAL_RPT09"]    = $tmp[$cnt++];
                    $data["JIZENSODAN_TEXT"]       = $tmp[$cnt++];
                    $data["JIZENSODAN_CD"]         = $tmp[$cnt++];
                    $data["BUKATUDO"]              = $tmp[$cnt++];
                    $data["SHOUGAKU_CD"]           = $tmp[$cnt++];
                    $data["BIKOU"]                 = $tmp[$cnt++];

                    $dataCnt = count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        //特記事項
                        $tokki = "";
                        if ($data["BIKOU"] != "") {
                            $tokki = mb_substr($data["BIKOU"], 0, 30);
                        }
                        $data["TOKKI"] = $tokki;

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

    /********************/
    /*  エラーチェック  */
    /********************/
    public function errCheck($db, $data, $checkdata, $line)
    {
        $returnFlg          = true;
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
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $otherSep           = "";

        foreach ($checkdata as $key => $val) {
            // 更新処理
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }

                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値を入力してください", $numericSep, $val[3]);
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※文字数オーバーです。:", $lengthSep, $val[3]);
                }

                //受験番号
                if ($key == "EXAMNO" && $data[$key] != "") {
                    $query = knjl632fQuery::checkExamno($this, $data[$key]);
                    $rowCount = $db->getOne($query);
                    if ($rowCount == "0") {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※", $otherSep, "願書データ未登録");
                    }
                }

                //かな氏名
                if ($key == "NAME_KANA" && $data[$key] != "") {
                    $query = knjl632fQuery::checkNameKana($this, $data["EXAMNO"], $data[$key]);
                    $rowCount = $db->getOne($query);
                    if ($rowCount == "0") {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※", $otherSep, "かな氏名不一致");
                    }
                }

                //事前相談コード
                if ($key == "JIZENSODAN_CD" && $data[$key] != "") {
                    $query = knjl632fQuery::checkJizensodanCd($this, $data[$key]);
                    $rowCount = $db->getOne($query);
                    if ($rowCount == "0") {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※", $otherSep, "事前相談コード間違い");
                    }
                }

                //奨学区分コード
                if ($key == "SHOUGAKU_CD" && $data[$key] != "") {
                    $query = knjl632fQuery::checkShougakuCd($this, $data[$key]);
                    $rowCount = $db->getOne($query);
                    if ($rowCount == "0") {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※", $otherSep, "奨学区分コード間違い");
                    }
                }

                //評定チェック
                for ($i = 0; $i < 9; $i++) {
                    $num = sprintf("%02d", $i + 1);
                    if ($key == "CONFIDENTIAL_RPT{$num}" && $data[$key] != "" && ($data[$key] < 1 || $data[$key] > 5)) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※", $otherSep, "{$val[3]}評定が不正");
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl632fQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    /******************************/
    /*  エラーメッセージをセット  */
    /******************************/
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep."(".$val.")";
        $sep = "/";
        return false;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $header = array();
        $header["EXAMNO"]                 = "受験番号";
        $header["NAME"]                   = "氏名";
        $header["NAME_KANA"]              = "かな氏名";
        $header["JITURYOKU_3GOKEI"]       = "実力テスト3科合計";
        $header["JITURYOKU_3HEIKIN"]      = "実力テスト3科合計平均";
        $header["JITURYOKU_5GOKEI"]       = "実力テスト5科合計";
        $header["JITURYOKU_5HEIKIN"]      = "実力テスト5科合計平均";
        $header["CONFIDENTIAL_RPT01"]     = "国語内申評定";
        $header["CONFIDENTIAL_RPT02"]     = "社会内申評定";
        $header["CONFIDENTIAL_RPT03"]     = "数学内申評定";
        $header["CONFIDENTIAL_RPT04"]     = "理科内申評定";
        $header["CONFIDENTIAL_RPT05"]     = "音楽内申評定";
        $header["CONFIDENTIAL_RPT06"]     = "美術内申評定";
        $header["CONFIDENTIAL_RPT07"]     = "保健体育内申評定";
        $header["CONFIDENTIAL_RPT08"]     = "技術家庭内申評定";
        $header["CONFIDENTIAL_RPT09"]     = "英語内申評定";
        $header["JIZENSODAN_TEXT"]        = "事前相談文言";
        $header["JIZENSODAN_CD"]          = "事前相談コード";
        $header["BUKATUDO"]               = "部活動";
        $header["SHOUGAKU_CD"]            = "奨学区分";
        $header["BIKOU"]                  = "備考";

        $header_rei["EXAMNO"]                 = "9999";
        $header_rei["NAME"]                   = "橋本　太郎";
        $header_rei["NAME_KANA"]              = "はしもと　たろう";
        $header_rei["JITURYOKU_3GOKEI"]       = "999";
        $header_rei["JITURYOKU_3HEIKIN"]      = "999";
        $header_rei["JITURYOKU_5GOKEI"]       = "999";
        $header_rei["JITURYOKU_5HEIKIN"]      = "999";
        $header_rei["CONFIDENTIAL_RPT01"]     = "5";
        $header_rei["CONFIDENTIAL_RPT02"]     = "5";
        $header_rei["CONFIDENTIAL_RPT03"]     = "5";
        $header_rei["CONFIDENTIAL_RPT04"]     = "5";
        $header_rei["CONFIDENTIAL_RPT05"]     = "5";
        $header_rei["CONFIDENTIAL_RPT06"]     = "5";
        $header_rei["CONFIDENTIAL_RPT07"]     = "5";
        $header_rei["CONFIDENTIAL_RPT08"]     = "5";
        $header_rei["CONFIDENTIAL_RPT09"]     = "5";
        $header_rei["JIZENSODAN_TEXT"]        = "事前相談";
        $header_rei["JIZENSODAN_CD"]          = "1";
        $header_rei["BUKATUDO"]               = "バレーボール部";
        $header_rei["SHOUGAKU_CD"]            = "11";
        $header_rei["BIKOU"]                  = "備考";

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode(",", $header_err)."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjl632fQuery::selectCsvErrQuery();
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setArray = $header_err;

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($setArray as $field => $label) {
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
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = $this->ObjYear."年度高校入試事前相談データ";
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
