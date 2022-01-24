<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx180Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $fieldSize = array();
    public $gyouSize  = array();
    public $candidate = array();
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $schoolNm;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx180Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        if (VARS::get("FIELDSIZE")) {
            $this->fieldSize = array();
            $fieldget = preg_split("{,}", VARS::get("FIELDSIZE"));
            for ($i = 0; $i < get_count($fieldget); $i++) {
                $fieldArray = preg_split("{=}", $fieldget[$i]);
                if ($fieldArray[1] > 0) {
                    $this->fieldSize[$fieldArray[0]] = $fieldArray[1];
                }
            }
        }
        if (VARS::get("GYOUSIZE")) {
            $this->gyouSize = array();
            $gyouget = preg_split("{,}", VARS::get("GYOUSIZE"));
            for ($i = 0; $i < get_count($gyouget); $i++) {
                $gyouArray = preg_split("{=}", $gyouget[$i]);
                if ($gyouArray[1] > 0) {
                    $this->gyouSize[$gyouArray[0]] = $gyouArray[1];
                }
            }
        }

        //学校名取得
        $db = Query::dbCheckOut();
        $this->schoolNm = $db->getOne(knjx180Query::getSchoolNm());
        Query::dbCheckIn($db);

        //ヘッダ候補
        foreach ($this->fieldSize as $key => $val) {
            if (in_array($key, array("TRAIN_REF1", "TRAIN_REF2", "TRAIN_REF3"))) {
                $check_HTRAINREMARK_DETAIL_DAT = true;
            }
            if (in_array($key, array("TRAIN_REF4", "TRAIN_REF5", "TRAIN_REF6"))) {
                $check_HTRAINREMARK_TRAINREF_DAT = true;
            }
        }
        if ($check_HTRAINREMARK_TRAINREF_DAT) {
            $check_HTRAINREMARK_DETAIL_DAT = false;
        }
        //
        $this->candidate = array();
        $this->candidate["TOTALSTUDYACT"]    = "総合学習活動";
        $this->candidate["TOTALSTUDYVAL"]    = "総合学習評価";
        $this->candidate["REMARK1_003"]      = "活動内容";
        $this->candidate["REMARK2_003"]      = "評価";
        $this->candidate["SPECIALACTREMARK"] = "特別活動所見";
        $this->candidate["TOTALREMARK"]      = "総合所見";
        if ($check_HTRAINREMARK_TRAINREF_DAT) {
            if ("shimaneken" == $this->schoolNm) {
                $this->candidate["TRAIN_REF1"]       = "(1)各教科・科目等の学習に関する所見";
                $this->candidate["TRAIN_REF2"]       = "(2)行動に関する所見・特技等";
                $this->candidate["TRAIN_REF3"]       = "(3)進路指導に関する事項";
                $this->candidate["TRAIN_REF4"]       = "(4)取得資格、検定等";
                $this->candidate["TRAIN_REF5"]       = "(5)部活動、ボランティア活動等";
                $this->candidate["TRAIN_REF6"]       = "(6)その他";
            } else {
                $this->candidate["TRAIN_REF1"]       = "(1)学習における特徴等";
                $this->candidate["TRAIN_REF2"]       = "(2)行動の特徴，特技等";
                $this->candidate["TRAIN_REF3"]       = "(3)部活動，ボランティア活動，留学・海外経験等";
                $this->candidate["TRAIN_REF4"]       = "(4)取得資格，検定等";
                $this->candidate["TRAIN_REF5"]       = "(5)表彰・顕彰等の記録";
                $this->candidate["TRAIN_REF6"]       = "(6)その他";
            }
        } else {
            $this->candidate["TRAIN_REF1"]       = "学習／行動の特技・特徴等";
            $this->candidate["TRAIN_REF2"]       = "部活動・資格取得等";
            $this->candidate["TRAIN_REF3"]       = "その他";
        }
        $this->candidate["ATTENDREC_REMARK"] = "出欠の記録備考";
        $this->candidate["VIEWREMARK"]       = "観点";
        $this->candidate["BEHAVEREC_REMARK"] = "行動の記録備考";
        $this->candidate["CLASSACT"]         = "学級活動";
        $this->candidate["STUDENTACT"]       = "生徒会活動";
        $this->candidate["CLUBACT"]          = "クラブ活動";
        $this->candidate["SCHOOLEVENT"]      = "学校行事";
        if ($this->schoolNm = 'naraken') {
            //narakenの場合、奈良Timeを追加
            $this->candidate["REMARK1_005"] = "奈良Time";
        }

        $this->getPropertiesAll(); //プロパティファイル
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

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx180Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx180Query::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"             => array(4,"年度"),
                               "SCHREGNO"         => array(8,"学籍番号"),
                               "ANNUAL"           => array(2,"年次"));
            $checkdata2 = array();
            //対象項目のみデータに追加する。
            foreach ($this->fieldSize as $key => $val) {
                $checkdata[$key] = array($this->fieldSize[$key],$this->candidate[$key]);
            }
            //対象項目のみデータに追加する。(行数チェック)
            foreach ($this->gyouSize as $key => $val) {
                $checkdata2[$key] = array($this->gyouSize[$key], $this->candidate[$key]);
            }


            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $primaryKey3   = array();
            $primaryKey4   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $duplicateCsv4 = array();
            $primaryKey1   = $this->getPrimaryKey('HTRAINREMARK_DAT');
            if ($check_HTRAINREMARK_TRAINREF_DAT) {
                $primaryKey2   = $this->getPrimaryKey('HTRAINREMARK_TRAINREF_DAT');
            } else {
                if ($check_HTRAINREMARK_DETAIL_DAT) {
                    $primaryKey2   = $this->getPrimaryKey('HTRAINREMARK_DETAIL_DAT');
                }
            }
            if ($check_HTRAINREMARK_DETAIL2_DAT) {
                if ($this->Properties["seitoSidoYorokuHoushiNentani"] == "2") {
                    if ($primaryKey2 != "") {
                        $primaryKey3   = $this->getPrimaryKey('HTRAINREMARK_HDAT');
                    } else {
                        $primaryKey2   = $this->getPrimaryKey('HTRAINREMARK_HDAT');
                    }
                } else {
                    if ($primaryKey2 != "") {
                        $primaryKey3   = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_DAT');
                    } else {
                        $primaryKey2   = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_DAT');
                    }
                }
            }
            if ($check_HTRAINREMARK_DETAIL2_DAT_005) {
                if (!($check_HTRAINREMARK_DETAIL2_DAT && $this->Properties["seitoSidoYorokuHoushiNentani"] != "2")) {
                    if ($primaryKey3 != "") {
                        $primaryKey4   = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_DAT');
                    } elseif ($primaryKey2 != "") {
                        $primaryKey3   = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_DAT');
                    } else {
                        $primaryKey2   = $this->getPrimaryKey('HTRAINREMARK_DETAIL2_DAT');
                    }
                }
            }
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

                    $data = array("YEAR"             => (strlen(trim($tmp[4]))) ? sprintf("%04d", $tmp[4]) : "",
                                  "SCHREGNO"         => (strlen(trim($tmp[5]))) ? sprintf("%08d", $tmp[5]) : "",
                                  "ANNUAL"           => (strlen(trim($tmp[6]))) ? sprintf("%02d", $tmp[6]) : "");
                    $tmpcnt = 7;
                    //対象項目のみデータに追加する。
                    foreach ($this->fieldSize as $key => $val) {
                        $data[$key] = $tmp[$tmpcnt];
                        $tmpcnt++;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $checkdata2, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey4 as $key => $val) {
                        $duplicateCsv4[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $data_each2, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $orderErrFlg        = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $orderErrMsg        = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $orderSep        = "";
        $duplicateCsvSep = "";

        //項目名
        $query = knjx180Query::getItemName($data["YEAR"]);
        $itemName = $db->getOne($query);
        $itemName = ($itemName == "") ? 'キャリアプラン・' : $itemName.'・';

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
        $dupFlg4 = $this->duplicateCsvCheck($duplicateCsv4, $primaryKey4, $data);
        if ($dupFlg4[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複4(". $dupFlg4[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SCHREGNO","ANNUAL"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","SCHREGNO","ANNUAL"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen(str_replace("\n", "", $data[$key])) > $val[0]) {
                    $item = (in_array($key, array("REMARK1_003", "REMARK2_003"))) ? $itemName : "";
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $item.$val[1]);
                }
            }
        }
        //行数チェック
        foreach ($data_each2 as $key => $val) {
            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (substr_count($data[$key], "\n") >= $val[0]) {
                    $item = (in_array($key, array("REMARK1_003", "REMARK2_003"))) ? $itemName : "";
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, $item.$val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx180Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }


    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        //ヘッダデータ
        $header = array("GRADE"            => "学年",
                        "HR_CLASS"         => "クラス",
                        "ATTENDNO"         => "出席番号",
                        "NAME"             => "氏名",
                        "YEAR"             => "※年度",
                        "SCHREGNO"         => "※学籍番号",
                        "ANNUAL"           => "※年次");

        //項目名
        $db = Query::dbCheckOut();
        if ($this->field["OUTPUT"] == "4") {
            $year = substr($this->field["YEAR"], 0, 4);
        } else {
            $year = CTRL_YEAR;
        }
        $query = knjx180Query::getItemName($year);
        $itemName = $db->getOne($query);
        $itemName = ($itemName == "") ? 'キャリアプラン・' : $itemName.'・';

        Query::dbCheckIn($db);

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize as $key => $val) {
            if (in_array($key, array("REMARK1_003", "REMARK2_003"))) {
                $header[$key] = $itemName.$this->candidate[$key];
            } else {
                $header[$key] = $this->candidate[$key];
            }
        }
        $header[$this->lastColumn] = $this->lastColumn;

        $mojisu = array();
        foreach ($this->fieldSize as $key => $val) {
            $data = ($val / 3);
            if (mb_strpos($data, ".") > 0) {
                $mojisu[$key] = substr($data, 0, mb_strpos($data, "."));
            } else {
                $mojisu[$key] = $data;
            }
        }
        $header_rei = array("GRADE"            => "99",
                            "HR_CLASS"         => "999",
                            "ATTENDNO"         => "999",
                            "NAME"             => "アルプ太郎",
                            "YEAR"             => "9999",
                            "SCHREGNO"         => "12345678",
                            "ANNUAL"           => "99");

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize as $key => $val) {
            $header_rei[$key] = "全角".$mojisu[$key]."文字";
        }
        $header_rei[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $tables = array();
            $table2 = array();
            $table3 = array();
            $table4 = array();
            $tables = array("1" => "HTRAINREMARK_DAT");
            if ($check_HTRAINREMARK_TRAINREF_DAT) {
                $table2 = array("2" => "HTRAINREMARK_TRAINREF_DAT");
                $tables = array_merge($tables, $table2);
            } else {
                if ($check_HTRAINREMARK_DETAIL_DAT) {
                    $table2 = array("2" => "HTRAINREMARK_DETAIL_DAT");
                    $tables = array_merge($tables, $table2);
                }
            }
            if ($check_HTRAINREMARK_DETAIL2_DAT) {
                if ($this->Properties["seitoSidoYorokuHoushiNentani"] == "2") {
                    if ($table2 != "") {
                        $table3 = array("3" => "HTRAINREMARK_HDAT");
                        $tables = array_merge($tables, $table3);
                    } else {
                        $table2 = array("2" => "HTRAINREMARK_HDAT");
                        $tables = array_merge($tables, $table2);
                    }
                } else {
                    if ($table2 != "") {
                        $table3 = array("3" => "HTRAINREMARK_DETAIL2_DAT");
                        $tables = array_merge($tables, $table3);
                    } else {
                        $table2 = array("2" => "HTRAINREMARK_DETAIL2_DAT");
                        $tables = array_merge($tables, $table2);
                    }
                }
            }
            if ($check_HTRAINREMARK_DETAIL2_DAT_005) {
                if (!($check_HTRAINREMARK_DETAIL2_DAT && $this->Properties["seitoSidoYorokuHoushiNentani"] != "2")) {
                    if ($table3 != "") {
                        $table4 = array("3" => "HTRAINREMARK_DETAIL2_DAT");
                        $tables = array_merge($tables, $table4);
                    } elseif ($table2 != "") {
                        $table3 = array("2" => "HTRAINREMARK_DETAIL2_DAT");
                        $tables = array_merge($tables, $table3);
                    } else {
                        $table2 = array("2" => "HTRAINREMARK_DETAIL2_DAT");
                        $tables = array_merge($tables, $table2);
                    }
                }
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $headerDupl = array();
            $headerDupl["YEAR"]       = "年度";
            $headerDupl["SCHREGNO"]   = "学籍番号";
            $headerDupl["HTRAIN_SEQ"] = "SEQ";
            $headerDupl["TRAIN_SEQ"]  = "SEQ";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDupl);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx180Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                if ($this->field["GRADE_HR_CLASS"]) {
                    $substr_grade = substr($this->field["GRADE_HR_CLASS"], 0, 2);
                    $substr_hr_class = substr($this->field["GRADE_HR_CLASS"], 2, 4);
                    $couse = "指導要録所見データ".ltrim($substr_grade, '0').'-'.str_replace("00", "0", $substr_hr_class);
                } else {
                    $couse = "指導要録所見データ";
                }
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