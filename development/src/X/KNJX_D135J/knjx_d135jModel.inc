<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_d135jModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $candidate = array();
    public $schoolkind;
    public $schoolName;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_d135jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),          //学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $db = Query::dbCheckOut();
        //学校を判断
        $this->schoolName = $db->getOne(knjx_d135jQuery::getSchoolName());
        //学期制取得
        $this->maxSemester = $db->getOne(knjx_d135jQuery::getMaxSemester());
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        if (VARS::get("SCHOOL_KIND") != "") {
            $this->schoolkind = VARS::get("SCHOOL_KIND");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"]) {
            $this->getSelectSchoolKind();
        }

        //総合的な学習の時間の判定
        $sem = ($this->Properties["tutisyoSougouHyoukaTunen"] == 1) ? "9" : "";

        $this->detail[] = array($sem, "01", "01", "REMARK1", 25, 9, "総合的な学習の時間・学習活動");
        $this->detail[] = array($sem, "01", "02", "REMARK1", 32, 9, "総合的な学習の時間・評価");
        $this->detail[] = array("",   "02", "01", "REMARK1", 27, 9, "特別活動");
        $this->detail[] = array("9",  "03", "01", "REMARK1", 30, 9, "部活動");
        $this->detail[] = array("",   "04", "01", "REMARK1", 28, 9, "所見");

        foreach ($this->detail as $key) {
            list($semester, $div, $code, $field, $default_moji, $default_gyou, $comment) = $key;
            $p_name = "reportSpecialSize".$div."_".$code;
            $name   = 'DETAIL_'.$div.'_'.$code.'_'.$field;

            $this->field[$name] = VARS::post($name);

            //文字数・行数
            if ($this->Properties[$p_name]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties[$p_name]);
                $this->getPro[$name]["moji"] = (int)trim($moji);
                $this->getPro[$name]["gyou"] = (int)trim($gyou);
            } else {
                $this->getPro[$name]["moji"] = $default_moji;
                $this->getPro[$name]["gyou"] = $default_gyou;
            }
        }

        if ($this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
            $this->getPro["ATTENDREC_REMARK"]["moji"] = (int)trim($moji);
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["ATTENDREC_REMARK"]["moji"] = 30;
            $this->getPro["ATTENDREC_REMARK"]["gyou"] = 1;
        }

        if ($this->maxSemester == "3") {
            $this->getPro["MORAL"]["moji"] = 24;
            $this->getPro["MORAL"]["gyou"] = 9;
        } else {
            $this->getPro["MORAL"]["moji"] = 23;
            $this->getPro["MORAL"]["gyou"] = 14;
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
                    knjx_d135jQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_d135jQuery::deleteQueryCsv($this, $this->data_arr);
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
            $setVal1 = ($this->getPro["DETAIL_01_01_REMARK1"]["moji"] * 3 * $this->getPro["DETAIL_01_01_REMARK1"]["gyou"]);
            $setVal2 = ($this->getPro["DETAIL_01_02_REMARK1"]["moji"] * 3 * $this->getPro["DETAIL_01_02_REMARK1"]["gyou"]);
            $setVal3 = ($this->getPro["DETAIL_02_01_REMARK1"]["moji"] * 3 * $this->getPro["DETAIL_02_01_REMARK1"]["gyou"]);
            $setVal4 = ($this->getPro["DETAIL_03_01_REMARK1"]["moji"] * 3 * $this->getPro["DETAIL_03_01_REMARK1"]["gyou"]);
            if ($this->schoolName == "kyoto") {
                $setMoral = ($this->getPro["MORAL"]["moji"] * 3 * $this->getPro["MORAL"]["gyou"]);
            }
            $setVal5 = ($this->getPro["DETAIL_04_01_REMARK1"]["moji"] * 3 * $this->getPro["DETAIL_04_01_REMARK1"]["gyou"]);
            $setVal6 = ($this->getPro["ATTENDREC_REMARK"]["moji"] * 3 * $this->getPro["ATTENDREC_REMARK"]["gyou"]);

            $checkdata = array();
            $checkdata["YEAR"]          = array(4,  "HISSU",  "INT",  "年度");
            $checkdata["SEMESTER"]      = array(4,  "HISSU",  "INT",  "学期");
            $checkdata["SCHREGNO"]      = array(8,  "HISSU",  "INT",  "学籍番号");
            $checkdata["DETAIL_01_01_REMARK1"] = array($setVal1,  "",       "",     "総合的な学習の時間（ 追記 ）学習活動");
            $checkdata["DETAIL_01_02_REMARK1"] = array($setVal2,  "",       "",     "総合的な学習の時間（ 追記 ）評価");
            $checkdata["DETAIL_02_01_REMARK1"] = array($setVal3,  "",       "",     "特別活動");
            $checkdata["DETAIL_03_01_REMARK1"] = array($setVal4,  "",       "",     "部活動（ 追記 ）");
            if ($this->schoolName == "kyoto") {
                $checkdata["MORAL"]                = array($setMoral,  "",       "",     "道徳（ 追記 ）");
            }
            $checkdata["DETAIL_04_01_REMARK1"] = array($setVal5,  "",       "",     "所見");
            $checkdata["ATTENDREC_REMARK"] = array($setVal6,  "",       "",     "出欠の記録備考");

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
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey('HREPORTREMARK_DAT');
            $primaryKey2   = $this->getPrimaryKey('HREPORTREMARK_DETAIL_DAT');
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
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $tmpcnt = 0;
                    $data = array();
                    if ($this->schoolName == "kyoto") {
                        $data["YEAR"]                  = $tmp[4];
                        $data["SEMESTER"]              = $tmp[5];
                        $data["SCHREGNO"]              = $tmp[6];
                        $data["DETAIL_01_01_REMARK1"]  = $tmp[7];
                        $data["DETAIL_01_02_REMARK1"]  = $tmp[8];
                        $data["DETAIL_02_01_REMARK1"]  = $tmp[9];
                        $data["DETAIL_03_01_REMARK1"]  = $tmp[10];
                        $data["MORAL"]                 = $tmp[11];
                        $data["DETAIL_04_01_REMARK1"]  = $tmp[12];
                        $data["ATTENDREC_REMARK"]      = $tmp[13];
                        $dataCnt = 15;
                    } else {
                        $data["YEAR"]                  = $tmp[4];
                        $data["SEMESTER"]              = $tmp[5];
                        $data["SCHREGNO"]              = $tmp[6];
                        $data["DETAIL_01_01_REMARK1"]  = $tmp[7];
                        $data["DETAIL_01_02_REMARK1"]  = $tmp[8];
                        $data["DETAIL_02_01_REMARK1"]  = $tmp[9];
                        $data["DETAIL_03_01_REMARK1"]  = $tmp[10];
                        $data["DETAIL_04_01_REMARK1"]  = $tmp[11];
                        $data["ATTENDREC_REMARK"]      = $tmp[12];
                        $dataCnt = 14;
                    }
                    $data["DIV"]                       = "";
                    $data["CODE"]                      = "";

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

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
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

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
        }

        if ($this->schoolkind) {
            //存在チェック
            $cnt_check = $db->getOne(knjx_d135jQuery::checkSchoolKind($this, $data));
            if ($cnt_check == 0) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
            }
        }
        //存在チェック
        $sem_check = $db->getCol(knjx_d135jQuery::checkSemester($data["YEAR"]));
        if (!in_array($data["SEMESTER"], $sem_check)) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学期データがありません。");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_d135jQuery::insertQueryErr($db, $line, $checkError);
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
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        //ヘッダデータ
        $header = array();
        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "氏名";
        $header["YEAR"]             = "※年度";
        $header["SEMESTER"]         = "※学期";
        $header["SCHREGNO"]         = "※学籍番号";
        $header["DETAIL_01_01_REMARK1"]         = "総合的な学習の時間（ 追記 ）学習活動";
        $header["DETAIL_01_02_REMARK1"]         = "総合的な学習の時間（ 追記 ）評価";
        $header["DETAIL_02_01_REMARK1"]         = "特別活動";
        $header["DETAIL_03_01_REMARK1"]         = "部活動（ 追記 ）";
        if ($this->schoolName == "kyoto") {
            $header["MORAL"]                        = "道徳（ 追記 ）";
        }
        $header["DETAIL_04_01_REMARK1"] = "所見";
        $header["ATTENDREC_REMARK"]     = "出欠の記録備考";
        $header[$this->lastColumn]      = $this->lastColumn;

        $header_rei = array();
        $header_rei["GRADE"]            = "99";
        $header_rei["HR_CLASS"]         = "999";
        $header_rei["ATTENDNO"]         = "999";
        $header_rei["NAME"]             = "アルプ太郎";
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header_rei["SEMESTER"]         = CTRL_SEMESTER;
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["DETAIL_01_01_REMARK1"]     = "(全角".$this->getPro["DETAIL_01_01_REMARK1"]["moji"]."文字X".$this->getPro["DETAIL_01_01_REMARK1"]["gyou"]."行まで)";
        $header_rei["DETAIL_01_02_REMARK1"]     = "(全角".$this->getPro["DETAIL_01_02_REMARK1"]["moji"]."文字X".$this->getPro["DETAIL_01_02_REMARK1"]["gyou"]."行まで)";
        $header_rei["DETAIL_02_01_REMARK1"]     = "(全角".$this->getPro["DETAIL_02_01_REMARK1"]["moji"]."文字X".$this->getPro["DETAIL_02_01_REMARK1"]["gyou"]."行まで)";
        $header_rei["DETAIL_03_01_REMARK1"]     = "(全角".$this->getPro["DETAIL_03_01_REMARK1"]["moji"]."文字X".$this->getPro["DETAIL_03_01_REMARK1"]["gyou"]."行まで)";
        if ($this->schoolName == "kyoto") {
            $header_rei["MORAL"]                    = "(全角".$this->getPro["MORAL"]["moji"]."文字X".$this->getPro["MORAL"]["gyou"]."行まで)";
        }
        $header_rei["DETAIL_04_01_REMARK1"]     = "(全角".$this->getPro["DETAIL_04_01_REMARK1"]["moji"]."文字X".$this->getPro["DETAIL_04_01_REMARK1"]["gyou"]."行まで)";
        $header_rei["ATTENDREC_REMARK"]         = "(全角".$this->getPro["ATTENDREC_REMARK"]["moji"]."文字X".$this->getPro["ATTENDREC_REMARK"]["gyou"]."行まで)";
        
        $header_rei[$this->lastColumn]          = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $headerDuplicate = array("YEAR"     => "年度",
                                     "SEMESTER" => "学期",
                                     "SCHREGNO" => "学籍番号",
                                     "DIV"      => "区分",
                                     "CODE"     => "コード");

            $tables = array();
            $tables = array("1" => "HREPORTREMARK_DAT", "2" => "HREPORTREMARK_DETAIL_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
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
                $query = knjx_d135jQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                    $array = $header;
                } else {
                    $array = $header_err;
                }

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
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "通知表所見データ";
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