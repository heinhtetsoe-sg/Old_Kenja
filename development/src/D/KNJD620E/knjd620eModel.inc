<?php

require_once('for_php7.php');
class knjd620eModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $selectSemeName;
    public $selectTestName;
    public $lastColumn;//CSVの最後の列の値

    public function knjd620eModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD; //登録者コード
    }

    public function init()
    {
        $this->group_div   = "2";
        $this->target_div  = "0";
        $this->stf_auth_cd = "00008888";

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),       //出力取込種別 1:ヘッダ出力 2:データ取込 3:エラー出力
                             "SEMESTER"         =>  VARS::post("SEMESTER"),     //学期
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD")    //考査種別
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        $this->selectSemeName  = VARS::post("selectSemeName");
        $this->selectTestName  = VARS::post("selectTestName");

        $this->getPropertiesAll();
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $db = Query::dbCheckOut();
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knjd620eQuery::insertQueryCsv($this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                Query::dbCheckIn($db);
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
            $checkdata = array("CLASSCD"        => array(2,"教科CD"),
                               "CURRICULUM_CD"  => array(2,"教育課程CD"),
                               "SUBCLASSCD"     => array(6,"科目CD"),
                               "SCHREGNO"       => array(8,"学籍番号"),
                               "SCORE"          => array(2,"得点")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('RECORD_SCORE_DAT');
            $duplicateCsv1 = array();
            $header = false;
            while (!feof($fp)) {
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

                    $data = array("CLASSCD"         => (strlen(trim($tmp[0]))) ? $tmp[0] : "",
                                  "CURRICULUM_CD"   => (strlen(trim($tmp[1]))) ? $tmp[1] : "",
                                  "SUBCLASSCD"      => (strlen(trim($tmp[2]))) ? $tmp[2] : "",
                                  "SCHREGNO"        => (strlen(trim($tmp[3]))) ? $tmp[3] : "",
                                  "SCORE"           => (strlen(trim($tmp[4]))) ? $tmp[4] : ""
                                  );

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

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;  //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数値エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";    //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("CLASSCD","CURRICULUM_CD","SUBCLASSCD", "SCHREGNO", "SCORE"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("SCORE"))) {
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
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjd620eQuery::insertQueryErr($db, $line, $checkError);
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
        $header_err = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
        //ヘッダデータ
        $header = array("CLASSCD"           => "教科CD",
                        "CURRICULUM_CD"     => "教育課程CD",
                        "SUBCLASSCD"        => "科目CD",
                        "SCHREGNO"          => "学籍番号",
                        "SCORE"             => "得点",
                        "YEAR"              => "年度",
                        "SEMESTER"          => "学期",
                        "TESTKINDCD"        => "テスト種別(大分類)",
                        "TESTITEMCD"        => "テスト項目（中分類）",
                        "SCORE_DIV"         => "得点種類（小分類）",
                        "SCHOOL_KIND"       =>  "学校種別"
                        );
        $header[$this->lastColumn] = $this->lastColumn;

        $header_rei = array("CLASSCD"           => "12",
                            "CURRICULUM_CD"     => "12",
                            "SUBCLASSCD"        => "123456",
                            "SCHREGNO"          => "12345678",
                            "SCORE"             => "12"
                            );
        $header_rei[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") { //1:ヘッダ出力
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") { //3:エラー出力
            //エラー
            $tables = array();
            $tables = array("1" => "RECORD_SCORE_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") { //1:ヘッダ出力
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") { //3:エラー出力
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") { //3:エラー出力
                $filename = "エラー出力";
            } else {                         //エラー以外
                $filename = CTRL_YEAR."年度入試 成績データ（".$this->selectTestName."）";
            }
            $filename = sprintf("%s.csv", $filename);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
//var_dump($_REQUEST);
