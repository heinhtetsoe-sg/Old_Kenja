<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knje385Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;

    /**
     * メンバ変数の初期化
     */
    function knje385Model() {
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->field);
            unset($this->file);
        }

        $this->field = array("YEAR"       =>  VARS::post("YEAR"),        //年度
                             "INPUT"      =>  VARS::post("INPUT"),       //取込種別
                             "OUTPUT"     =>  VARS::post("OUTPUT"),      //出力取込種別
                             "SHORI_MEI"  =>  VARS::post("SHORI_MEI"),      //処理種別
                             );

        //取込対象テーブル
        if ($this->field["INPUT"] == "1") {
            $this->updateTableName = "AFT_TOTAL_STUDY_TOEFL_DAT";
        } elseif ($this->field["INPUT"] == "2") {
            $this->updateTableName = "AFT_TOTAL_STUDY_SCORE_DAT";
        } else {
            $this->updateTableName = "AFT_TOTAL_STUDY_ADJUSTMENT_DAT";
        }

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->file = VARS::file("FILE");
    }

    function &getMainModel() {
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $header_data = false;   //出力フラグ

        $contents  = "";    //クリア

        //DB接続
        $db = Query::dbCheckOut();

        if ($this->field["OUTPUT"] == "3") {
            $duplHeader = array("YEAR"            => "年度",
                                "SCHREGNO"        => "学籍番号",
                                "TEST_DATE"       => "Test Date"
            );
            //項目
            $header_err = array("MSGROW"            => "行番号",
                                "MSGREMARK"         => "メッセージ");
            $tables = array();
            if ($this->field["INPUT"] == "1") {
                $tables = array("1" => "AFT_TOTAL_STUDY_TOEFL_DAT");
            } elseif ($this->field["INPUT"] == "2") {
                $tables = array("1" => "AFT_TOTAL_STUDY_SCORE_DAT");
            } else {
                $tables = array("1" => "AFT_TOTAL_STUDY_ADJUSTMENT_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";

            $query = $this->selectCsvErrQuery(PROGRAMID);
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $array = $header_err;

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($array as $key => $headerName) {
                    $contents .= $comma.$row[$key];
                    $comma = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        } else {
            if ($this->field["INPUT"] == "1") {
                $header = array(
                    "TEST_DATE"                => "Test Date"         ,
                    "GRADE_HR_CLASS_ATTENDNO"  => "年組番"            ,
                    "CANDIDATE_NAME"           => "Candidate Name"    ,
                    "SECTION_1_SCORE"          => "Section I Score"   ,
                    "SECTION_2_SCORE"          => "Section II Score"  ,
                    "SECTION_3_SCORE"          => "Section III Score" ,
                    "TOTAL_SCORE"              => "Total Score"       ,
                    "NATIVE_COUNTRY"           => "Native Country"    ,
                    "NATIVE_LANGUAGE"          => "Native Language"   ,
                    "BARTHDAY"                 => "誕生日"            ,
                );
                $header_rei = array(
                    "TEST_DATE"                => "08/29/2019"        ,
                    "GRADE_HR_CLASS_ATTENDNO"  => "1101"              ,
                    "CANDIDATE_NAME"           => ""                  ,
                    "SECTION_1_SCORE"          => "12"                ,
                    "SECTION_2_SCORE"          => "12"                ,
                    "SECTION_3_SCORE"          => "12"                ,
                    "TOTAL_SCORE"              => "123"               ,
                    "NATIVE_COUNTRY"           => "300"               ,
                    "NATIVE_LANGUAGE"          => "331"               ,
                    "BARTHDAY"                 => "04/20/2001"        ,
                );
            } else {
                $header = array(
                    "YEAR"      => "年度"       ,
                    "GRADE"     => "学年"       ,
                    "HR_CLASS"  => "年組"       ,
                    "ATTENDNO"  => "出席番号"   ,
                    "SCORE"     => "得点"       ,
                );
                $header_rei = array(
                    "YEAR"      => "2019"      ,
                    "GRADE"     => "01"        ,
                    "HR_CLASS"  => "001"       ,
                    "ATTENDNO"  => "001"       ,
                    "SCORE"     => "123"       ,
                );
            }
            $contents  = implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";

            $header_data = true;
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse = "エラー出力";
            } else {
                if ($this->field["INPUT"] == "1") {
                    $couse = "TOEFL点";
                } elseif ($this->field["INPUT"] == "2") {
                    $couse = "学力点";
                } else {
                    $couse = "加減点";
                }
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ＣＳＶ取込
    function &getExecModel() {

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knje385Query::updateQueryCsv($this, $this->data_arr);
                } else {
                    knje385Query::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {

            if ($this->field["INPUT"] == "1") {
                $checkdata["YEAR"]       = array(4,    "HISSU",    "INT",   "年度");
                $checkdata["TEST_DATE"]  = array(10,   "HISSU",    "DATE",  "実施年月日");
                $checkdata["GRADE"]      = array(2,    "HISSU",    "INT",   "学年");
                $checkdata["HR_CLASS"]   = array(3,    "HISSU",    "INT",   "クラス");
                $checkdata["ATTENDNO"]   = array(3,    "HISSU",    "INT",   "出席番号");
                $checkdata["SCORE"]      = array(4,    "",         "INT",   "得点");
                $checkdata["BIRTHDAY"]   = array(10,   "",    "DATE",  "誕生日");
            } else {
                $checkdata["YEAR"]       = array(4,    "HISSU",    "INT",   "年度");
                $checkdata["GRADE"]      = array(2,    "HISSU",    "INT",   "学年");
                $checkdata["HR_CLASS"]   = array(3,    "HISSU",    "INT",   "クラス");
                $checkdata["ATTENDNO"]   = array(3,    "HISSU",    "INT",   "出席番号");
                $checkdata["SCORE"]      = array(4,    "",         "INT",   "得点");
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            if ($this->field["INPUT"] == "1") {
                $primaryKey1 = $this->getPrimaryKey('AFT_TOTAL_STUDY_TOEFL_DAT');
            } elseif ($this->field["INPUT"] == "2") {
                $primaryKey1 = $this->getPrimaryKey('AFT_TOTAL_STUDY_SCORE_DAT');
            } else {
                $primaryKey1 = $this->getPrimaryKey('AFT_TOTAL_STUDY_ADJUSTMENT_DAT');
            }
            $primaryKey2[] = "DUPLICATE_CHECK_KEY";
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    if ($this->field["INPUT"] == "1") {
                        $data = array(
                            "YEAR"                  => $this->getNendo(strtotime($tmp[0])),
                            "TEST_DATE"             => date("Y-m-d", strtotime($tmp[0])),
                            "GRADE"                 => substr($tmp[1], 0, 1),
                            "HR_CLASS"              => substr($tmp[1], 1, 1),
                            "ATTENDNO"              => substr($tmp[1], 2),
                            "SCORE"                 => $tmp[6],
                            "BIRTHDAY"              => (strlen(trim($tmp[9])) > 0) ? date("Y-m-d", strtotime($tmp[9])) : "",
                            "DUPLICATE_CHECK_KEY"   => $tmp[1].":".date("Y-m-d", strtotime($tmp[9])),
                        );
                    } else {
                        $data = array(
                            "YEAR"     => $tmp[0],
                            "GRADE"    => $tmp[1],
                            "HR_CLASS" => $tmp[2],
                            "ATTENDNO" => $tmp[3],
                            "SCORE"    => $tmp[4],
                        );
                    }
                    $schregno = $db->getOne(knje385Query::getSchregno($this, $data));
                    $data["SCHREGNO"] = $schregno;
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $temp_arr, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                }
                $line++;
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, $data, $data_each, $line, $temp_arr, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg        = false;   //値エラーフラグ
        $dateFormatErrFlg   = false;    //日付(フォーマット)エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ 

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg        = "";      //値エラーメッセージ
        $dateFormatErrMsg   = "";       //日付(フォーマット)エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $valuesSep           = "";
        $dateFormatSep      = "";
        $duplicateCsvSep    = "";

        if ($this->field["SHORI_MEI"] == "1" && $this->field["INPUT"] == '1') {
            //CSVファイル内の重複チェック
            $existDupl = 0;
            foreach ($temp_arr as $temp) {
                if (in_array($data["DUPLICATE_CHECK_KEY"], $temp)) {
                    $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
                    if ($dupFlg2[0] == 1) {
                        $existDupl = 1;
                        $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg2[1] ."行目)");
                    }
                }
            }
            if ($existDupl == 0) {
                $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
                if ($dupFlg1[0] == 1) {
                    $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
                }
            }
        } else {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //文字数チェック
            if ($this->field["SHORI_MEI"] == "1" || $this->field["SHORI_MEI"] == "2" && $val[1] == "HISSU") {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
            //日付型チェック
            if ($val[2] == "DATE") {
                if (common::isDate($data[$key]) == false) {
                    $returnFlg = $this->errSet($dateFormatErrFlg, $dateFormatErrMsg, "※日付(フォーマット):", $dateFormatSep, $val[1]);
                }
            }
        }
        //存在チェック
        $query = knje385Query::checkSchregRegdDat($this, $data);
        if (0 == $db->getOne($query)) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学籍番号");
        }
        //年度チェック 年度コンボとCSVの年度が不一致の場合エラー
        if ($data["YEAR"] != $this->field["YEAR"]) {
            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "年度不正");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$dateFormatErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje385Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //タイムスタンプから年度を求める
    function getNendo($timestamp){
        return intval(date('m', $timestamp)) > 3 ? intval(date('Y', $timestamp)) : intval(date('Y', $timestamp)) - 1;
    }
}
?>
