<?php

require_once('for_php7.php');

class knjb3049Model extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;

    function init() {

        $this->field = array("OUTPUT"       =>  VARS::post("OUTPUT"),       //出力取込種別
                             "YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "BSCSEQ"       =>  VARS::post("BSCSEQ"),       //基本時間割
                             "TITLE"        =>  VARS::post("TITLE"),        //タイトル
                             "HEADER"       =>  VARS::post("HEADER")        //ヘッダ有
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
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                //データ取込
                list ($cnt, $bscseq) = knjb3049Query::updateQueryCsv($this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");

                //新規のときは新しいSEQをセットする
                if ($this->field["BSCSEQ"] == "NEW") {
                    $this->field["BSCSEQ"] = $bscseq;
                    unset($this->field["TITLE"]);
                }
            }
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            $checkdata = array();
            $checkdata["WEEK"]          = array(9,    "HISSU",  "",     "曜日");
            $checkdata["PERIODCD"]      = array(1,    "HISSU",  "INT",  "時限");
            $checkdata["FACCD"]         = array(3,    "HISSU",  "INT",  "施設");
            $checkdata["SUBCLASSCD"]    = array(4,    "HISSU",  "INT",  "科目");
            $stf_size = ($this->Properties["useStaffcdFieldSize"]) ? $this->Properties["useStaffcdFieldSize"] : 8;
            $checkdata["STAFFCD"]       = array($stf_size,  "",       "INT",  "先生");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            //校時一覧取得（チェック用）
            $query = knjb3049Query::getPeriodcd($this);
            $this->periodArray = $db->getCol($query);

            //科目一覧取得（チェック用）
            $query = knjb3049Query::getSubclasscd($this);
            $this->subclassArray = $db->getCol($query);

            //職員一覧取得（チェック用）
            $query = knjb3049Query::getStaffcd($this);
            $this->staffArray = $db->getCol($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey2 = array();
            $duplicateCsv2 = array();
            $primaryKey3 = array();
            $duplicateCsv3 = array();
            $primaryKey1 = $this->getPrimaryKey('CHAIR_DAT');
            $primaryKey2 = $this->getPrimaryKey('CHAIR_STF_DAT');
            $primaryKey3 = $this->getPrimaryKey('SCH_PTRN_DAT');
            while (!feof ($fp)) {
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
                    $checkcount = get_count($tmp);

                    $data = array();
                    $data["WEEK"]           = $tmp[0];
                    $data["PERIODCD"]       = $tmp[1];
                    $data["FACCD"]          = $tmp[2];
                    $data["SUBCLASSCD"]     = $tmp[3];
                    $data["STAFFCD"]        = ($this->Properties["useStaffcdFieldSize"]) ? sprintf("%0{$this->Properties["useStaffcdFieldSize"]}d", $tmp[4]) : $tmp[4];

                    //項目数によるファイルチェック
                    if ($checkcount != get_count($data)) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }
                    //講座コード
                    $chaircd            = $tmp[3].$tmp[2];
                    $data1              = array();
                    $data1["YEAR"]      = $this->field["YEAR"];
                    $data1["SEMESTER"]  = $this->field["SEMESTER"];
                    $data1["CHAIRCD"]   = $chaircd;
                    $data1["WEEK"]      = $tmp[0];
                    $data1["PERIODCD"]  = $tmp[1];
                    $data1["FACCD"]     = $tmp[2];
                    $data1["SUBCLASSCD"]= $tmp[3];
                    $data1["BSCSEQ"]    = "";
                    $data1["DAYCD"]     = "";
                    $data1["STAFFCD"]   = ($this->Properties["useStaffcdFieldSize"]) ? sprintf("%0{$this->Properties["useStaffcdFieldSize"]}d", $tmp[4]) : $tmp[4];
                    //エラーチェック
                    if ($this->errCheck($db, $data1, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data1[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data1[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data1[$val];
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
    function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3) {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        // 重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            //存在チェック
            if ($key == "WEEK") {
                $weekArray = array('月' => "2", '火' => "3", '水' => "4", '木' => "5", '金' => "6", '土' => "7", '日' => "1");

                $daycd = "";
                foreach ($weekArray as $str => $cd) {
                    if ($daycd) break;
                    if (preg_match("/{$str}/", $data[$key])) {
                        $daycd = $cd;
                    }
                }

                if ($data[$key] && $daycd == "") {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if ($key == "PERIODCD") {
                if ($data[$key] && !in_array($data[$key], $this->periodArray)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if ($key == "SUBCLASSCD") {
                if ($data[$key] && !in_array($data[$key].'00', $this->subclassArray)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if ($key == "STAFFCD") {
                if ($data[$key] && !in_array($data[$key], $this->staffArray)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjb3049Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();
        $header = $header_rei = array();

        //エラー出力項目
        $header = array("MSGROW"        => "行番号",
                        "MSGREMARK"     => "メッセージ");

        $contents  = "";    //クリア
        $data_flg = false;  //出力フラグ

        $headerDuplicate = array("YEAR"                 => "年度",
                                "SEMESTER"              => "学期",
                                "CHAIRCD"               => "講座コード",
                                "STAFFCD"               => "先生",
                                "BSCSEQ"                => "SEQ(パターン)",
                                "DAYCD"                 => "曜日コード",
                                "PERIODCD"              => "校時コード");
        $tables = array();
        $tables = array("1" => "CHAIR_DAT", "2" => "CHAIR_STF_DAT", "3" => "SCH_PTRN_DAT");
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            $contents .= implode($headerErr, ",")."\n";
        }
        // ヘッダー出力
        $contents .= implode($header, ",")."\n";

        //エラーデータ出力
        $query = $this->selectCsvErrQuery(PROGRAMID);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $data_flg = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data_flg) {
            $filename = sprintf("%s.csv", "エラー出力");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
