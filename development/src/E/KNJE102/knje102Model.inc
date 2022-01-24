<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knje102Model extends Model
{
    public $data       = array();
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $data_arr = array();
    public $cmd;
    public $file;
    public $field      = array();
    public $controls = array();
    public $headercheck;
    public $testsubclasscd;
    public $testsubclassnm;
    public $coursecd;
    public $majorcd;
    public $coursecode;
    public $wk;
    public $appli;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knje102Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD; //登録者コード
    }
    public function init()
    {
        $this->field = array("OUTPUT"         =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"           =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "HEADER"         =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"      =>  VARS::post("SHORI_MEI")         //処理名
                             );

        if (VARS::get("PROGRAMID")) {
            $this->headercheck = "1";
        } else {
            $this->headercheck = VARS::post("HEADER");
        }
        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->getPropertiesAll(); //プロパティファイル
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
                    knje102Query::insertQuery($this, $this->data_arr);
                } else {
                    knje102Query::deleteQuery($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "読込み件数：".get_count($this->data_arr)."件");
            }
        }
    }

    //ファイルの文字列の取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            //項目チェック用の配列
            $data_each = array( "YEAR"              => array(4,"年度"),
                                "SCHREGNO"          => array(8,"学籍番号"),
                                "SHAMEXAMCD"        => array(2,"試験コード"),
                                "CLASSCD"           => array(2,"教科コード"),
                                "SCHOOL_KIND"       => array(1,"学校種別"),
                                "CURRICULUM_CD"     => array(1,"教育課程コード"),
                                "SUBCLASSCD"        => array(6,"科目コード"),
                                "SUBCLASSNAME"      => array(30,"科目名"),
                                "SUBCLASSCD_CNT"    => array(4,"試験科目数"),
                                "TRADE"             => array(90,"業者名"),
                                "SCORE"             => array(6,"得点"),
                                "PRECEDENCE"        => array(6,"全国席次"),
                                "DEVIATION"         => array(6,"全国偏差値"),
                                "SCHOOL_PRECEDENCE" => array(6,"校内席次"),
                                "SCHOOL_DEVIATION"  => array(6,"校内偏差値"),
                                "WISHSCHOOLCD1"     => array(7,"志望校1"),
                                "WISHSCHOOLCD2"     => array(7,"志望校2"),
                                "WISHSCHOOLCD3"     => array(7,"志望校3"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('SHAMEXAMINATION_DAT');
            $duplicateCsv1 = array();
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array( "YEAR"               => $tmp[0],
                                    "SHAMEXAMCD"        => (strlen(trim($tmp[6]))) ? sprintf("%02d", $tmp[6]) : "",
                                    "SCHREGNO"          => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                    "SUBCLASSCD"        => (strlen(trim($tmp[7]))) ? sprintf("%06d", $tmp[7]) : "",
                                    "SUBCLASSNAME"      => $tmp[8],
                                    "SUBCLASSCD_CNT"    => (strlen(trim($tmp[9]))) ? sprintf("%d", $tmp[9]) : "",
                                    "TRADE"             => $tmp[10],
                                    "SCORE"             => (strlen(trim($tmp[11]))) ? number_format($tmp[11], 1) : "",
                                    "PRECEDENCE"        => $tmp[12],
                                    "DEVIATION"         => (strlen(trim($tmp[13]))) ? number_format($tmp[13], 1) : "",
                                    "SCHOOL_PRECEDENCE" => $tmp[14],
                                    "SCHOOL_DEVIATION"  => (strlen(trim($tmp[15]))) ? number_format($tmp[15], 1) : "",
                                    "WISHSCHOOLCD1"     => (strlen(trim($tmp[16]))) ? sprintf("%07d", $tmp[16]) : "",
                                    "WISHSCHOOLCD2"     => (strlen(trim($tmp[17]))) ? sprintf("%07d", $tmp[17]) : "",
                                    "WISHSCHOOLCD3"     => (strlen(trim($tmp[18]))) ? sprintf("%07d", $tmp[18]) : "",
                                    "CLASSCD"           => (strlen(trim($tmp[5]))) ? sprintf("%02d", $tmp[19]) : "",
                                    "SCHOOL_KIND"       => $tmp[20],
                                    "CURRICULUM_CD"     => $tmp[21]);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)) {
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

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";       //セパレータ
        $numericSep         = "";       //セパレータ
        $lengthSep          = "";       //セパレータ
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SHAMEXAMCD","SCHREGNO","SUBCLASSCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //文字数チェック
            if (!in_array($key, array("SCORE","DEVIATION","SCHOOL_DEVIATION"))) {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //得点チェック
            if (in_array($key, array("SCORE","DEVIATION","SCHOOL_DEVIATION"))) {
                if ($data[$key] != "") {
                    $itema = explode(".", $data[$key]);
                    $itemb = str_replace(".", "", $data[$key]);
                    if ($itemb != "" && !is_numeric($itemb)) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                    if (strlen($itema[0]) > 3) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }

            //数値チェック
            if (in_array($key, array("SHAMEXAMCD","SCHREGNO","SUBCLASSCD","SUBCLASSCD_CNT","PRECEDENCE","SCHOOL_PRECEDENCE","WISHSCHOOLCD1","WISHSCHOOLCD2","WISHSCHOOLCD3"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje102Query::insertQueryErr($db, $line, $checkError);
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

    //テンプレートの書き出し
    public function outputDataFile()
    {
        $header = array("YEAR"              => "年度",
                        "SHAMEXAMCD"        => "試験コード",
                        "SCHREGNO"          => "学籍番号",
                        "CLASSCD"           => "教科コード",
                        "SCHOOL_KIND"       => (get_count($school_kind) > 0) ? "学校種別(".implode('/', $school_kind).")" : "学校種別(J:中学)",
                        "CURRICULUM_CD"     => "教育課程コード",
                        "SUBCLASSCD"        => "科目コード");
        $contents = "";
        if ($this->field["OUTPUT"]=="2") {
            //エラー
            $tables = array();
            $tables = array("1" => "SHAMEXAMINATION_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= "行番号,メッセージ\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } else {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= "※年度,学年,組,出席番号,※学籍番号,氏名,※試験コード,※科目コード,科目名,試験科目数,業者名,得点,全国席次,全国偏差値,校内席次,校内偏差値,志望校1,志望校2,志望校3,教科コード,学校種別,教育課程コード,".$this->lastColumn."\n";
            }
            $query = knje102Query::getselectDataSQL($this);
        }

        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->field["OUTPUT"] == "3" && !$row["SCHREGNO"]) {
                continue;
            }
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="2") {
                $filename = sprintf("%s.csv", "対外模試エラーデータ");
            } else {
                $filename = sprintf("%s.csv", "対外模試データ");
            }
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        $contents = "※年度,学年,組,出席番号,※学籍番号,氏名,※試験コード,※科目コード,科目名,試験科目数,業者名,得点,全国席次,全国偏差値,校内席次,校内偏差値,志望校1,志望校2,志望校3,教科コード,学校種別,教育課程コード,".$this->lastColumn."\n";
        $contents .="9999,99,999,999,99999999,賢者太郎,99,999999,１０桁,9999,３０桁,999.9,999999,999.9,999999,999.9,9999999,9999999,9999999,99,J,9,".$this->lastColumn;
        $filename = sprintf("%s.csv", "対外模試データ");

        common::downloadContents($contents, $filename);
        return true;
    }
}
