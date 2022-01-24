<?php

require_once('for_php7.php');

class knjh536Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjh536Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        $this->field = array("OUTPUT"                   =>  VARS::post("OUTPUT"),                   //出力取込種別
                             "SEMESTER"                 =>  VARS::post("SEMESTER"),                 //学期
                             "GRADE"                    =>  VARS::post("GRADE"),                    //学年一覧
                             "GRADE_HR_CLASS"           =>  VARS::post("GRADE_HR_CLASS"),           //年組一覧
                             "PROFICIENCYDIV"           =>  VARS::post("PROFICIENCYDIV"),           //データ種別
                             "PROFICIENCYCD"            =>  VARS::post("PROFICIENCYCD"),            //テスト名称
                             "PROFICIENCY_SUBCLASS_CD"  =>  VARS::post("PROFICIENCY_SUBCLASS_CD"),  //科目
                             "QUALIFIED"                =>  VARS::post("QUALIFIED"),                //1:全員　2:資格取得者以外
                             "HEADER"                   =>  VARS::post("HEADER"),                   //ヘッダ有
                             "SHORI_MEI"                =>  VARS::post("SHORI_MEI")                 //処理名
                             );

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

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

    //実行モデルを返します
    public function &getExecModel()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjh536Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjh536Query::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array("YEAR"                      => array(4, "年度"),
                               "SEMESTER"                  => array(1, "学期"),
                               "PROFICIENCYDIV"            => array(2, "データ種別"),
                               "PROFICIENCYCD"             => array(4, "テストCD"),
                               "SCHREGNO"                  => array(8, "学籍番号"),
                               "PROFICIENCY_SUBCLASS_CD"   => array(6, "科目コード"),
                               "SCORE"                     => array(3, "得点"),
                               "SCORE_DI"                  => array(1, "DIコード"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);

            $fp = fopen($this->file["tmp_name"], "r");
            $idx  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('PROFICIENCY_DAT');
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

                    $data = array("YEAR"                      => $tmp[0],
                                  "SEMESTER"                  => $tmp[1],
                                  "PROFICIENCYDIV"            => $tmp[2],
                                  "PROFICIENCYCD"             => $tmp[3],
                                  "SCHREGNO"                  => $tmp[7],
                                  "PROFICIENCY_SUBCLASS_CD"   => $tmp[9],
                                  "SCORE"                     => $tmp[11],
                                  "SCORE_DI"                  => $tmp[12]);

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$idx] = $data;
                        $idx++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, &$data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg      = true;
        $hissuErrFlg    = false;    //必須エラーフラグ
        $numericErrFlg  = false;    //数字エラーフラグ
        $lengthErrFlg   = false;    //レングスエラーフラグ
        $sonzaiErrFlg   = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg    = "";       //必須エラーメッセージ
        $numericErrMsg  = "";       //数字エラーメッセージ
        $lengthErrMsg   = "";       //レングスエラーメッセージ
        $sonzaiErrMsg    = "";      //存在エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $sonzaiSep    = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($this->field["SHORI_MEI"] == "1") {
                if (in_array($key, array("YEAR", "SEMESTER", "PROFICIENCYDIV", "PROFICIENCYCD", "SCHREGNO", "PROFICIENCY_SUBCLASS_CD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SEMESTER", "PROFICIENCYDIV", "PROFICIENCYCD", "SCHREGNO", "PROFICIENCY_SUBCLASS_CD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //得点チェック
            if (in_array($key, array("SCORE"))) {
                if ($data[$key] != "" && (!is_numeric($data[$key]) && $data[$key] != "*")) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        if (1 > $db->getOne(knjh536Query::getProfMstSonzai($data))) {
            if (1 > $db->getOne(knjh536Query::getProfMstSonzai($data, "ZEROUME"))) {
                $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "実力テスト");
            } else {
                $query = knjh536Query::getProfMstSonzai($data, "ZEROUME_SELECT");
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $data["PROFICIENCYDIV"] = $row["PROFICIENCYDIV"];
                    $data["PROFICIENCYCD"] = $row["PROFICIENCYCD"];
                }
                $result->free();
            }
        }

        //存在チェック
        if (1 > $db->getOne(knjh536Query::getProfSubclassMstSonzai($data))) {
            $returnFlg = $this->errSet($sonzaiErrFlg, $sonzaiErrMsg, "※存在:", $sonzaiSep, "科目");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$sonzaiErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjh536Query::insertQueryErr($db, $line, $checkError);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

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
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダデータ
        $header = array("YEAR"                      => "※年度",
                        "SEMESTER"                  => "※学期",
                        "PROFICIENCYDIV"            => "※データ種別",
                        "PROFICIENCYCD"             => "※テストCD",
                        "GRADE"                     => "学年",
                        "HR_CLASS"                  => "クラス",
                        "ATTENDNO"                  => "出席番号",
                        "SCHREGNO"                  => "※学籍番号",
                        "NAME"                      => "氏名",
                        "PROFICIENCY_SUBCLASS_CD"   => "※科目コード",
                        "PROFICIENCY_SUBCLASS_NAME" => "科目名",
                        "SCORE"                     => "得点",
                        "SCORE_DI"                  => "DIコード",
                        $this->lastColumn           => $this->lastColumn);


        $header_rei = array("YEAR"                      => "9999",
                            "SEMESTER"                  => "9",
                            "PROFICIENCYDIV"            => "99",
                            "PROFICIENCYCD"             => "9999",
                            "GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "SCHREGNO"                  => "12345678",
                            "NAME"                      => "氏名",
                            "PROFICIENCY_SUBCLASS_CD"   => "123456",
                            "PROFICIENCY_SUBCLASS_NAME" => "科目名",
                            "SCORE"                     => "999",
                            "SCORE_DI"                  => "*",
                            $this->lastColumn           => $this->lastColumn);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $tables = array("1" => "PROFICIENCY_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjh536Query::selectMainQuery($this);
            }

            $dataTemp = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "実力テストデータ";
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
