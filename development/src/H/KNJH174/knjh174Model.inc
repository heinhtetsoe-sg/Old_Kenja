<?php

require_once('for_php7.php');
class knjh174Model extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $year;

    function init() {
        if(VARS::get("PROGRAMID")) {
            unset($this->copyyear);
        }

        $this->field = array(
                            "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                            "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                            "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                            "TRGTYEAR"         =>  VARS::post("TRGTYEAR"),         //CSV出力対象年度
                            "house"            =>  VARS::post("house"),            //ハウス
                            "family"           =>  VARS::post("family"),           //ファミリー
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        /**************/
        /*  ヘッダー  */
        /**************/

        $this->family_max_num = "6";   //1つのグループに所属できる最大人数

        $this->header = array();
        $this->header["GRADE_NAME1"]        = "学年";
        $this->header["HR_NAME"]            = "クラス";
        $this->header["ATTENDNO"]           = "番号";
        $this->header["HOUSE_GROUP_CD"]     = "ハウスグループコード";
        $this->header["NAME1"]              = "ハウス名称";
        $this->header["NAME2"]              = "ファミリー名称";
        $this->header["NAME3"]              = "BS名称";
        $this->header["SCHREGNO"]           = "学籍番号";
        $this->header["NAME"]               = "名前";
        $this->header["NAME_KANA"]          = "ふりがな";
        $this->header["ABBV1"]              = "性別";

        //取込用新規フォーマット
        $this->header2 = array();
        $this->header2["HOUSE_GROUP_CD"]             = "ハウスコード";
        $this->header2["SCHREGNO"]                   = "学籍番号";

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
                if ($this->field["SHORI_MEI"] == "1") {
                    knjh174Query::updateQueryCsv($this, $this->data_arr);
                } else if ($this->field["SHORI_MEI"] == "2") {
                    knjh174Query::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
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

            //チェック対象項目
            $checkdata = array();
            $checkdata["HOUSE_GROUP_CD"]              = array(3,    "HISSU", "INT", "HOUSE");
            $checkdata["SCHREGNO"]                    = array(8,    "HISSU", "INT", "SCHREG");

            //ハウスコードチェック用
            $this->namecd1H050 = array();
            $this->namecd1H051 = array();
            $this->namecd1H052 = array();
            $query = knjh174Query::getHouseCdErrorChk($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["NAMECD1"] == "H050") {
                    $this->namecd1H050[] = $row["NAMECD2"];
                } else if ($row["NAMECD1"] == "H051") {
                    $this->namecd1H051[] = $row["NAMECD2"];
                } else if ($row["NAMECD1"] == "H052") {
                    $this->namecd1H052[] = $row["NAMECD2"];
                }
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey('HOUSE_GROUP_DAT');
            $primaryKey2   = $this->getPrimaryKey('SCHREG_BASE_MST');
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
                    $dataCnt = 0;
                    foreach ($this->header2 as $field => $label) {
                        $data[$field] = $tmp[$dataCnt];
                        $dataCnt++;
                    }
                    $data["YEAR"] = $this->field["TRGTYEAR"];

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
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
    function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2) {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep           = "";
        $existenceSep       = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        foreach ($this->header2 as $field => $label) {

            if (in_array($field, array_keys($checkdata))) {

                list ($size, $hissu, $div, $exist) = $checkdata[$field];

                //必須項目チェック
                if ($hissu == "HISSU" && !strlen(trim($data[$field]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $label);
                }

                //数値チェック
                if (($div == "INT" || $div == "INT2") && $data[$field] != "" && !is_numeric($data[$field])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $label);
                }

                //存在チェック
                if ($data[$field] != "" && $exist == "SCHREG") {
                    if ($db->getOne(knjh174Query::getSchregExists($this, $data[$field])) < 1) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                }

                //ハウスコード(名称マスタ存在チェック)
                if ($data[$field] != "" && $exist == "HOUSE") {
                    $cd1 = substr($data[$field], 0, 1); //ハウス
                    $cd2 = substr($data[$field], 1, 1); //ファミリー
                    $cd3 = substr($data[$field], 2, 1); //BS
                    $notExistsFlg1 = !in_array($cd1, $this->namecd1H050);
                    $notExistsFlg2 = !in_array($cd2, $this->namecd1H051);
                    $notExistsFlg3 = !in_array($cd3, $this->namecd1H052);
                    if ($notExistsFlg1 || $notExistsFlg2 || $notExistsFlg3) {
                        $errKeta = "";
                        if ($notExistsFlg1) {
                            $errKeta = "1桁目";
                        } else if ($notExistsFlg2) {
                            $errKeta = "2桁目";
                        } else if ($notExistsFlg3) {
                            $errKeta = "3桁目";
                        }

                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label."({$errKeta})");
                    }
                }

                //文字数チェック
                if (strlen($data[$field]) > $size) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label);
                }

            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjh174Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    function date_rep($data) {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if ($data) $data = str_replace("/","-",$data);
        }

        return $data;
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

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        /****************/
        /*  ヘッダー例  */
        /****************/

        $header_rei = array();
        $header_rei["HOUSE_GROUP_CD"]               = "3桁";
        $header_rei["SCHREGNO"]                     = "8桁";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($this->header2, ',')."\n";
            $sep = '';
            foreach ($this->header2 as $field => $label) {
                $contents .= $sep.$header_rei[$field];
                $sep = ',';
            }
            $contents .= "\n";
        } else if ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $headerDuplicate = array("YEAR"       => "※年度",
                                     "SCHREGNO"   => "※学籍番号"
                                    );
            $tables = array();
            $tables = array("1" => "HOUSE_GROUP_DAT", "2" => "SCHREG_BASE_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"] == "4") {
            // データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($this->header, ",")."\n";
            }
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] != "1") {
            $query = "";
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $contents .= $this->outputError($db, $this, $header_data);
            } else if ($this->field["OUTPUT"] == "4") {
                $contents .= $this->outputData($db, $this, $header_data);
            }
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/

        //ファイル名
        $title = 'ハウスグループCSV出力'.str_replace("-", "", CTRL_DATE);

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            //エラー出力
            if ($this->field["OUTPUT"] == "3") {
                $filename = sprintf("%s.csv", "エラー出力");
            //エラー出力以外
            } else {
                $filename = sprintf("%s.csv", $title);
            }
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //エラー出力用関数
    function outputError($db, $model, &$header_data) {
        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        $query = "";
        $query = $this->selectCsvErrQuery(PROGRAMID);

        $contents = "";

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $array = $header_err;

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($array as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";
                $header_data = true;
            }
            $result->free();
        }
        
        return $contents;
    }

    //データ出力
    function outputData($db, $model, &$header_data) {
        $contents = "";
        $query = knjh174Query::selectMainQuery($model);

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            $befHouseCd = "";
            $row = array(); //csvの一行に対応
            while ($tmpRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //カンマ変換
                $tmpRow = str_replace("\r\n","",str_replace(",",".",$tmpRow));
                $array = $this->header;
                $contents .= implode($tmpRow, ",") ."\n";
                $header_data = true;
            }
            //最後の一行分の処理
            $contents .= $row[$befHouseCd]."\n";

            $result->free();
        }

        return $contents;
    }

    //前年度のデータをコピー
    function &getCopyModel() {
        //今年度のデータが存在するか確認
        $t_year = knjh174Query::CheckData($this, $this->field["TRGTYEAR"]);
        if (isset($t_year)) {
           $this->setWarning('MSG203', "対象年度のデータが存在します。");
           return false;
        }
        //前年度のデータが存在するか確認
        $pre_year = knjh174Query::CheckData($this, $this->field["TRGTYEAR"] - 1);
        if (!isset($pre_year)) {
           $this->setWarning('MSG203', "前年度データが存在しません。");
           return false;
        }

        knjh174Query::getCopyQuery($this);
        $this->setMessage("MSG201");
        return;
    }

}
?>
