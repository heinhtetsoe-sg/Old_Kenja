<?php

require_once('for_php7.php');

class knja041Model extends Model
{
    public $cmd;
    public $new_year;
    public $file;
    public $data_arr = array();
    public $controls = array();
    public $field = array();
    public $school_name;
    public $lastColumn;//CSVの最後の列の値

    public function knja041Model()
    {
        common::GetControlMaster_Fnc($this->controls);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->new_year);
            $this->headercheck = "1";
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();

        $this->new_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->headercheck = VARS::post("HEADERCHECK");

        $this->file = VARS::file("FILE");

        $query = knja041Query::getSchoolName();
        $this->school_name = $db->getOne($query);

        Query::dbCheckIn($db);

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getUpdateModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = $this->getFileString();

        if ($this->warning) {
            return false;
        }

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knja041Query::UpdateQuery($this);
                } else {
                    //削除処理は、ない。
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"            => array(4,"年度"),
                               "SCHREGNO"        => array(8,"学籍番号"),
                               "SEMESTER"        => array(1,"学期"),
                               "GRADE"           => array(2,"学年"),
                               "HR_CLASS"        => array(3,"組"),
                               "ATTENDNO"        => array(3,"出席番号"),
                               "COURSECD"        => array(1,"課程コード"),
                               "MAJORCD"         => array(3,"学科コード"),
                               "COURSECODE"      => array(4,"コースコード"),
                               "REMAINGRADE_FLG" => array(1,"留年フラグ"),
                               "OLD_SCHREGNO"    => array(8,"旧学籍番号"),
                               "OLD_GRADE"       => array(2,"旧学年"),
                               "OLD_HR_CLASS"    => array(3,"旧組"),
                               "OLD_ATTENDNO"    => array(3,"旧出席番号"),
                               "SCORE"           => array(5,"成績"));

            //エラーデータの削除
            $db = Query::dbCheckOut();
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);
            Query::dbCheckIn($db);

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('CLASS_FORMATION_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    if ($this->school_name == "KINJUNIOR" || $this->school_name == "KINDAI") {
                        $data = array("YEAR"             => $tmp[0],                                                   //年度
                                      "SCHREGNO"         => (strlen(trim($tmp[1]))) ? sprintf("%08d", $tmp[1]) : "",   //学籍番号
                                      "SEMESTER"         => $tmp[2],                                                   //学期
                                      "GRADE"            => (strlen(trim($tmp[6]))) ? sprintf("%02d", $tmp[6]) : "",   //学年
                                      "HR_CLASS"         => $tmp[7],                                                   //組
                                      "ATTENDNO"         => (strlen(trim($tmp[8]))) ? sprintf("%03d", $tmp[8]) : "",   //出席番号
                                      "COURSECD"         => $tmp[9],                                                   //課程コード
                                      "MAJORCD"          => (strlen(trim($tmp[10]))) ? sprintf("%03d", $tmp[10]) : "", //学科コード
                                      "COURSECODE"       => (strlen(trim($tmp[11]))) ? sprintf("%04d", $tmp[11]) : "", //コースコード
                                      "REMAINGRADE_FLG"  => $tmp[12],                                                  //留年フラグ
                                      "OLD_SCHREGNO"     => (strlen(trim($tmp[13]))) ? sprintf("%08d", $tmp[13]) : "", //旧学籍番号
                                      "OLD_GRADE"        => $tmp[14],                                                  //旧学年
                                      "OLD_HR_CLASS"     => $tmp[15],                                                  //旧組
                                      "OLD_ATTENDNO"     => $tmp[16],                                                  //旧出席番号
                                      "SCORE"            => $tmp[17]);                                                 //成績
                    } else {
                        $data = array("YEAR"             => $tmp[0],                                                   //年度
                                      "SCHREGNO"         => (strlen(trim($tmp[1]))) ? sprintf("%08d", $tmp[1]) : "",   //学籍番号
                                      "SEMESTER"         => $tmp[2],                                                   //学期
                                      "GRADE"            => (strlen(trim($tmp[6]))) ? sprintf("%02d", $tmp[6]) : "",   //学年
                                      "HR_CLASS"         => (strlen(trim($tmp[7]))) ? sprintf("%03d", $tmp[7]) : "",   //組
                                      "ATTENDNO"         => (strlen(trim($tmp[8]))) ? sprintf("%03d", $tmp[8]) : "",   //出席番号
                                      "COURSECD"         => $tmp[9],                                                   //課程コード
                                      "MAJORCD"          => (strlen(trim($tmp[10]))) ? sprintf("%03d", $tmp[10]) : "", //学科コード
                                      "COURSECODE"       => (strlen(trim($tmp[11]))) ? sprintf("%04d", $tmp[11]) : "", //コースコード
                                      "REMAINGRADE_FLG"  => $tmp[12],                                                  //留年フラグ
                                      "OLD_SCHREGNO"     => (strlen(trim($tmp[13]))) ? sprintf("%08d", $tmp[13]) : "", //旧学籍番号
                                      "OLD_GRADE"        => (strlen(trim($tmp[14]))) ? sprintf("%02d", $tmp[6]) : "",  //旧学年
                                      "OLD_HR_CLASS"     => (strlen(trim($tmp[15]))) ? sprintf("%03d", $tmp[7]) : "",  //旧組
                                      "OLD_ATTENDNO"     => (strlen(trim($tmp[16]))) ? sprintf("%03d", $tmp[8]) : "",  //旧出席番号
                                      "SCORE"            => $tmp[17]);                                                 //成績
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
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

    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {

        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SCHREGNO", "SEMESTER"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //年度が現在年度+1ではない場合
            if ($key == "YEAR" && $this->new_year != $data[$key]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "年度不正".$val[1]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //学籍番号が処理年度の在籍データに存在している場合
                if ($key == "SCHREGNO" && !knja041Query::SchregnoExist($this, $data[$key], $db)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }

                //数値チェック
                if (in_array($key, array("YEAR", "SCHREGNO", "SEMESTER"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                //得点チェック
                if ($key == "SCORE") {
                    $item = explode(".", $data[$key]);
                    for ($chk = 0; $chk < sizeof($item); $chk++) {
                        if (is_numeric($item[$chk])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                    }
                    if ($item[0] > 999 || $item[0] < -999) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }

                //留年フラグが0でない場合
                if ($key == "REMAINGRADE_FLG" && $data[$key] != "0" && $data[$key] != "1") {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }

                //単位制で同学年に進級のとき、学年と旧学年をチェック
                $school = $db->getRow(knja041Query::getSchoolMst($this), DB_FETCHMODE_ASSOC);
                if ($this->Properties["useKeepGrade"] == "1" && $school["SCHOOLDIV"] == "1") {
                    if ($key == "GRADE" && $data[$key] != "" && $data[$key] != $data["OLD_GRADE"]) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            } else {
                //削除処理は、ない。
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja041Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents = ($this->headercheck == 1) ? "年度,学籍番号,学期,氏名漢字,氏名かな,性別,学年,組,出席番号,課程コード,学科コード,コースコード,留年フラグ,旧学籍番号,旧学年,旧組,旧出席番号,成績,".$this->lastColumn."\n" : "";
            $contents.= "9999,99999999,9,山田太郎,やまだ　たろう,9,99,999,999,1,001,0001,0,99999999,99,999,999,999,".$this->lastColumn;
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $tables = array("1" => "CLASS_FORMATION_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $header = array();
            $header["YEAR"] = "年度";
            $header["SCHREGNO"] = "学籍番号";
            $header["SEMESTER"] = "学期";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] == "3") {
            //DB接続
            $db = Query::dbCheckOut();

            $query = $this->selectCsvErrQuery(PROGRAMID);

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
                $couse = "クラス分けCSV";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
