<?php

require_once('for_php7.php');
class knjl213cModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $entexamyear;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        //入試年度
        $this->entexamyear = CTRL_YEAR + 1;

        $this->field = array("OUTPUT"        =>  VARS::post("OUTPUT"),        //処理種別
                             "APPLICANTDIV"  =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "HEADER"        =>  VARS::post("HEADER"),        //ヘッダ有
                             "SHORI_MEI"     =>  VARS::post("SHORI_MEI")      //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

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
            if ($this->field["SHORI_MEI"] == "1") { //1:更新
                $cnt = knjl213cQuery::udpateQueryCsv($this, $this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
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
            $checkdata =  array("APPLICANTDIV"      => array(1,     "※入試制度"),
                                "PRE_RECEPTNO"      => array(5,     "※受付番号"),
                                "PRE_TESTDIV"       => array(1,     "※プレテスト区分"),
                                "PRE_RECEPTDIV"     => array(1,     "申し込み方法"),
                                "PRE_RECEPTDATE"    => array(10,    "※受付日付"),
                                "NAME"              => array(60,    "※氏名"),
                                "NAME_KANA"         => array(120,   "※かな"),
                                "SEX"               => array(1,     "※性別"),
                                "GNAME"             => array(60,    "※保護者氏名"),
                                "GKANA"             => array(120,   "※保護者かな"),
                                "ZIPCD"             => array(8,     "郵便番号"),
                                "ADDRESS1"          => array(75,    "住所"),
                                "ADDRESS2"          => array(75,    "方書"),
                                "TELNO"             => array(14,    "電話番号"),
                                "FS_CD"             => array(7,     "出身学校コード"),
                                "PS_CD"             => array(7,     "塾コード"),
                                "PS_CONTACT"        => array(1,     "塾への連絡を希望"),
                                "PRE_EXAM_TYPE"     => array(1,     "※受験型")
                                );

            //バス情報表示
            if ($this->Properties["Pretest_bus_Not_Hyouji"] != "1") {
                $checkdata["BUS_USE"]           = array(1,     "学園バス");
                $checkdata["STATIONDIV"]        = array(1,     "乗降地");
                $checkdata["BUS_USER_COUNT"]    = array(2,     "ご利用人数");
            }
            $checkdata["REMARK"]            = array(45,    "備考");
            $checkdata["RECOM_EXAMNO"]      = array(5,     "重複受験番号");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl213cQuery::deleteQueryErr();
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

                    $data = array("APPLICANTDIV"    => $tmp[0],
                                  "PRE_RECEPTNO"    => (strlen(trim($tmp[1])))  ? sprintf("%05d", $tmp[1]) : "",
                                  "PRE_TESTDIV"     => $tmp[2],
                                  "PRE_RECEPTDIV"   => $tmp[3],
                                  "PRE_RECEPTDATE"  => $tmp[4],
                                  "NAME"            => $tmp[5],
                                  "NAME_KANA"       => $tmp[6],
                                  "SEX"             => $tmp[7],
                                  "GNAME"           => $tmp[8],
                                  "GKANA"           => $tmp[9],
                                  "ZIPCD"           => $tmp[10],
                                  "ADDRESS1"        => $tmp[11],
                                  "ADDRESS2"        => $tmp[12],
                                  "TELNO"           => $tmp[13],
                                  "FS_CD"           => (strlen(trim($tmp[14])))  ? sprintf("%07d", $tmp[14]) : "",
                                  "PS_CD"           => (strlen(trim($tmp[15])))  ? sprintf("%07d", $tmp[15]) : "",
                                  "PS_CONTACT"      => $tmp[16],
                                  "PRE_EXAM_TYPE"   => $tmp[17]
                                  );

                    //バス情報表示
                    if ($this->Properties["Pretest_bus_Not_Hyouji"] != "1") {
                        $data["BUS_USE"]        = $tmp[18];
                        $data["STATIONDIV"]     = $tmp[19];
                        $data["BUS_USER_COUNT"] = $tmp[20];
                        $data["RECOM_EXAMNO"]   = (strlen(trim($tmp[21])))  ? sprintf("%05d", $tmp[21]) : "";
                        $data["REMARK"]         = $tmp[22];
                    } else {
                        $data["RECOM_EXAMNO"]   = (strlen(trim($tmp[18])))  ? sprintf("%05d", $tmp[18]) : "";
                        $data["REMARK"]         = $tmp[19];
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

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";

        $query = knjl213cQuery::getVnameMst($this->entexamyear, "L104");
        $result = $db->query($query);
        $this->L104nmsp1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->L104nmsp1[$row["NAMECD2"]] = $row["NAMESPARE1"];
        }
        $result->free();

        $query = knjl213cQuery::getVnameMst($this->entexamyear, "L105");
        $result = $db->query($query);
        $this->L105nmsp1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->L105nmsp1[$row["NAMECD2"]] = $row["NAMESPARE1"];
        }
        $result->free();

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("APPLICANTDIV","PRE_RECEPTNO","PRE_TESTDIV","PRE_RECEPTDATE","NAME","NAME_KANA","SEX","GNAME","GKANA","PRE_EXAM_TYPE"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //存在チェック
            if ($key == 'APPLICANTDIV' && 0 == $db->getOne(knjl213cQuery::getApplicantdiv("cnt", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'PRE_TESTDIV' && 0 == $db->getOne(knjl213cQuery::getVnameMst($this->entexamyear, "L104", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'PRE_RECEPTDIV' && !in_array($data[$key], array(1,2,3))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'SEX' && strlen($data[$key]) && 0 == $db->getOne(knjl213cQuery::getSex("cnt", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'FS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl213cQuery::chkFinSchool($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'PS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl213cQuery::chkPriSchool($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'PS_CONTACT' && strlen($data["PS_CD"]) && !in_array($data[$key], array(1,2))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            } elseif ($key == 'PS_CONTACT' && strlen($data[$key]) && !in_array($data[$key], array(1,2))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'PRE_EXAM_TYPE' && !in_array($data[$key], array(1,2,3))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'BUS_USE' && strlen($data[$key]) && $data[$key] != '1') {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'STATIONDIV' && strlen($data["BUS_USE"]) && !in_array($data[$key], array(1,2,3))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            if ($key == 'BUS_USER_COUNT' && strlen($data["BUS_USE"]) && !(0 < $data[$key] && $data[$key] < 100)) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            //重複受験番号＋名前
            if ($key == 'RECOM_EXAMNO' && $data[$key] && 0 == $db->getOne(knjl213cQuery::existdataNoAndName($this, $data))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            //受験型
            if ($key == 'PRE_EXAM_TYPE' && $this->L105nmsp1[$data[$key]] != $data["PRE_TESTDIV"]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
            }
            //頭２桁チェック
            if ($key == 'PRE_RECEPTNO' && substr($data["PRE_RECEPTNO"], 0, 2) != $this->L104nmsp1[$data["PRE_TESTDIV"]]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1].$this->L104nmsp1[$data["PRE_TESTDIV"]]."始まり");
            }
            $checkPreTestDiv = $data["PRE_TESTDIV"] == "1" ? "2" : "1";
            if ($key == 'RECOM_EXAMNO' && strlen($data["RECOM_EXAMNO"]) > 0 && substr($data["RECOM_EXAMNO"], 0, 2) != $this->L104nmsp1[$checkPreTestDiv]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1].$this->L104nmsp1[$checkPreTestDiv]."始まり");
            }

            //数値チェック
            if (in_array($key, array("APPLICANTDIV", "PRE_RECEPTNO", "PRE_RECEPTDIV", "SEX", "FS_CD", "PS_CD", "PS_CONTACT", "PRE_EXAM_TYPE", "BUS_USE", "STATIONDIV", "BUS_USER_COUNT"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl213cQuery::insertQueryErr($db, $line, $check_error);
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
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        $header =     array("APPLICANTDIV"      => "※入試制度",
                            "PRE_RECEPTNO"      => "※受付番号",
                            "PRE_TESTDIV"       => "※プレテスト区分",
                            "PRE_RECEPTDIV"     => "申し込み方法",
                            "PRE_RECEPTDATE"    => "※受付日付",
                            "NAME"              => "※氏名",
                            "NAME_KANA"         => "※氏名かな",
                            "SEX"               => "※性別",
                            "GNAME"             => "※保護者氏名",
                            "GKANA"             => "※保護者氏名かな",
                            "ZIPCD"             => "郵便番号",
                            "ADDRESS1"          => "住所",
                            "ADDRESS2"          => "方書",
                            "TELNO"             => "電話番号",
                            "FS_CD"             => "出身学校コード",
                            "PS_CD"             => "塾コード",
                            "PS_CONTACT"        => "塾への連絡を希望",
                            "PRE_EXAM_TYPE"     => "※受験型"
                            );

        //バス情報表示
        if ($this->Properties["Pretest_bus_Not_Hyouji"] != "1") {
            $header["BUS_USE" ]         = "学園バス利用";
            $header["STATIONDIV"]       = "乗降地";
            $header["BUS_USER_COUNT"]   = "ご利用人数";
        }
        $header["RECOM_EXAMNO"]     = "重複受験番号";
        $header["REMARK"]           = "備考";
        $header[$this->lastColumn]  = $this->lastColumn;

        //データから取得
        $applicantdiv = $db->getCol(knjl213cQuery::getApplicantdiv("list"));
        $sex = $db->getCol(knjl213cQuery::getSex("list"));

        $query = knjl213cQuery::getVnameMst($this->entexamyear, "L104");
        $result = $db->query($query);
        $setTestDiv = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setTestDiv .= $sep.$row["NAMECD2"].":".$row["NAME1"];
            $sep = " ";
        }
        $result->free();

        $header_rei = array("APPLICANTDIV"      => implode(' ', $applicantdiv),
                            "PRE_RECEPTNO"      => "12345",
                            "PRE_TESTDIV"       => $setTestDiv,
                            "PRE_RECEPTDIV"     => "1:FAX 2:郵送 3:インターネット",
                            "PRE_RECEPTDATE"    => str_replace("-", "/", CTRL_DATE),
                            "NAME"              => "アルプ　太郎",
                            "NAME_KANA"         => "あるぷ　たろう",
                            "SEX"               => implode(' ', $sex),
                            "GNAME"             => "アルプ　父",
                            "GKANA"             => "あるぷ　ちち",
                            "ZIPCD"             => "123-4567",
                            "ADDRESS1"          => "全角で25文字",
                            "ADDRESS2"          => "全角で25文字",
                            "TELNO"             => "123-456-7890",
                            "FS_CD"             => "1234567",
                            "PS_CD"             => "1234567",
                            "PS_CONTACT"        => "1:する 2:しない",
                            "PRE_EXAM_TYPE"     => "1:Ⅰ型 2:Ⅱ型"
                            );

        //バス情報表示
        if ($this->Properties["Pretest_bus_Not_Hyouji"] != "1") {
            $header_rei["BUS_USE"]          = "1:利用する";
            $header_rei["STATIONDIV"]       = "1:林間田園都市駅 2:福神駅 3:JR五条駅";
            $header_rei["BUS_USER_COUNT"]   = "12";
        }
        $header_rei["RECOM_EXAMNO"]     = "12345";
        $header_rei["REMARK"]           = "全角で15文字";
        $header_rei[$this->lastColumn]  = $this->lastColumn;

        $contents  = "";    //クリア
        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"] == "1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            // データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjl213cQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjl213cQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {        //データ
                    $row["PRE_RECEPTDATE"] = str_replace("-", "/", $row["PRE_RECEPTDATE"]);
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode(",", $row) ."\n";
                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {    //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "プレテスト受付データ";
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
