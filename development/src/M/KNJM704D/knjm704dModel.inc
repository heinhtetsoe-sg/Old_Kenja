<?php

require_once('for_php7.php');
//定数定義
class knjm704dModel extends Model
{
    public $field = array();
    public $cmd;
    public $grade;         //学年
    public $year;
    public $file;
    public $lastColumn;//CSVの最後の列の値

    //メンバ変数の初期化
    public function knjm704dModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->copyYear);
            unset($this->year);
            unset($this->specialcd);
        }

        if (VARS::post("COPY_YEAR")) {
            $this->copyYear = VARS::post("COPY_YEAR");
        }

        if (VARS::get("YEAR")) {
            $this->year = VARS::get("YEAR");
        }
        if (VARS::get("SPECIALCD")) {
            $this->specialcd = VARS::get("SPECIALCD");
        }

        $this->field = array("YEAR"                    => VARS::post("YEAR"),                //年度
                             "SPECIALCD"               => VARS::post("SPECIALCD"),           //管理番号
                             "SPECIAL_SDATE"           => VARS::post("SPECIAL_SDATE"),       //開始日付
                             "SPECIAL_EDATE"           => VARS::post("SPECIAL_EDATE"),       //終了日付
                             "SPECIALACTIVITYNAME"     => VARS::post("SPECIALACTIVITYNAME"), //特別活動内容
                             "SPECIALACTIVITYTIME"     => VARS::post("SPECIALACTIVITYTIME"), //活動時間数
                             "OUTPUT"                  => VARS::post("OUTPUT"),              //CSV出力区分
                             "HEADER"                  => VARS::post("HEADER"),              //ヘッダ有
                             );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /***** 追加、更新、削除 *****/
    //追加処理
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            knjm704dQuery::getInsertQuery($this);
            $this->year = $this->field["YEAR"];
            $this->specialcd = $this->field["SPECIALCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            knjm704dQuery::getUpdateQuery($this);
            $this->year = $this->field["YEAR"];
            $this->specialcd = $this->field["SPECIALCD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            knjm704dQuery::getDeleteQuery($this);
            $this->year = "";
            $this->specialcd = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if ($kind == "insert" || $kind == "update") {
            //空文字チェック
            if (strlen($this->field["YEAR"]) == 0) {
                $this->setWarning("MSG301", "(年度)");
                return false;
            }
            if (strlen($this->field["SPECIALCD"]) == 0) {
                $this->setWarning("MSG301", "(都道府県コード)");
                return false;
            }

            //数値チェック
            if ($this->field["YEAR"] != "" && !preg_match('/^[0-9]+$/', $this->field["YEAR"])) {
                $this->setWarning("MSG909", "(年度)");
                return false;
            }
            if ($this->field["SPECIALCD"] != "" && !preg_match('/^[0-9]+$/', $this->field["SPECIALCD"])) {
                $this->setWarning("MSG909", "(管理番号)");
                return false;
            }
            if ($this->field["SPECIALACTIVITYTIME"] != "" && !preg_match('/^[0-9]+$/', $this->field["SPECIALACTIVITYTIME"])) {
                $this->setWarning("MSG909", "(活動時間数)");
                return false;
            }

            //文字数チェック
            if (strlen($this->field["YEAR"]) > 4) {
                $this->setWarning("MSG915", "(年度は4バイトまでです。)");
                return false;
            }
            if (strlen($this->field["SPECIALCD"]) > 3) {
                $this->setWarning("MSG915", "(管理番号は3バイトまでです。)");
                return false;
            }
            if (strlen($this->field["SPECIALACTIVITYNAME"]) > 90) {
                $this->setWarning("MSG915", "(特別活動内容は90バイトまでです。)");
                return false;
            }
            if (strlen($this->field["SPECIALACTIVITYTIME"]) > 3) {
                $this->setWarning("MSG915", "(活動時間数は3バイトまでです。)");
                return false;
            }

            //日付チェック
            //年度内の日付かどうか
            $yearStartDate = $this->field["YEAR"] . "/04/01";
            $yearEndDate = ($this->field["YEAR"] + 1) . "/03/31";
            if ($this->field["SPECIAL_SDATE"] != "" && ($this->field["SPECIAL_SDATE"] < $yearStartDate || $yearEndDate < $this->field["SPECIAL_SDATE"])) {
                $this->setWarning("MSG916", "(開始日付が年度外の日付です。)");
                return false;
            }
            if ($this->field["SPECIAL_EDATE"] != "" && ($this->field["SPECIAL_EDATE"] < $yearStartDate || $yearEndDate < $this->field["SPECIAL_EDATE"])) {
                $this->setWarning("MSG916", "(終了日付が年度外の日付です。)");
                return false;
            }
            //開始日付 > 終了日付のときはエラー
            if ($this->field["SPECIAL_SDATE"] != "" && $this->field["SPECIAL_EDATE"] != "" && $this->field["SPECIAL_SDATE"] > $this->field["SPECIAL_EDATE"]) {
                $this->setWarning("MSG916", "(日付の大小が不正です。)");
                return false;
            }
        }

        //入力項目の１レコード取得
        $row = knjm704dQuery::getRow($this->field["YEAR"], $this->field["SPECIALCD"]);
        //登録時に既にデータがあればエラー
        if ($kind == "insert" && is_array($row)) {
            $this->setWarning("MSG302", "(この管理番号は既に登録されています。)");
            return false;
        }
        //更新、削除の存在チェック
        if ($kind == "update" || $kind == "delete") {
            if ($this->year != $this->field["YEAR"] || $this->specialcd != $this->field["SPECIALCD"]) {
                $this->setWarning("MSG308");
                return false;
            }

            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        return true;
    }

    /***** 前年度コピー処理 *****/
    //前年度のデータをコピー
    public function &getCopyModel()
    {
        //今年度と選択年度のデータ存在チェック
        $check = array();
        $check = knjm704dQuery::checkData($this);

        if (in_array('X', $check)) {
            $this->setWarning('MSG203', "(今年度のデータが存在します。)");
            return false;
        } elseif (!in_array('Y', $check)) {
            $this->setWarning('MSG203', "(選択年度のデータが存在しません。)");
            return false;
        } else {
            knjm704dQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }



    /***** CSV関連 *****/
    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        if ($this->field["OUTPUT"] == "3") {
            $duplHeader = array("YEAR"       => "年度",
                                "SPECIALCD"  => "管理番号");

            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                "MSGREMARK" => "メッセージ");
            $tables = array();
            $tables = array("1" => "SPECIAL_ACTIVITY_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }

            $contents .= implode(",", $header) . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header = array(
                "※年度",
                "※管理番号",
                "開始日付",
                "終了日付",
                "特別活動内容",
                "活動時間数",
                $this->lastColumn,
            );
            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjm704dQuery::getCsvData();
        }
        if ($this->field["OUTPUT"] == "4") {
            $contents .= "2020,";          // YEAR                 年度
            $contents .= "1,";             // SPECIALCD            管理番号
            $contents .= "2020/07/24,";    // SPECIAL_SDATE        開始日付
            $contents .= "2020/08/09,";    // SPECIAL_EDATE        終了日付
            $contents .= "全角30文字,";    // SPECIALACTIVITYNAME  特別活動内容
            $contents .= "1,";             // SPECIALACTIVITYTIME  活動時間数
            $contents .= $this->lastColumn."\n";

            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                //置換（日付）
                foreach ($row as $key => $val) {
                    if (preg_match('/DATE/', $key) && $row[$key] != "") {
                        $row[$key] = str_replace("-", "/", $val);
                    }
                }

                //配列の値を,で連結
                $contents .= implode($row, ",") ."\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="特別活動一覧エラー";
            } else {
                $couse="特別活動一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //ここから取り込み処理
    //CVSファイル
    //クライアント ⇒ サーバ側の処理
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                knjm704dQuery::insertQueryCsv($db, $this->data_arr);
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array(
                "YEAR"                 => array(4   ,"年度"         ), //0
                "SPECIALCD"            => array(3   ,"管理番号"     ), //1
                "SPECIAL_SDATE"        => array(10  ,"開始日付"     ), //2
                "SPECIAL_EDATE"        => array(10  ,"終了日付"     ), //3
                "SPECIALACTIVITYNAME"  => array(90  ,"特別活動内容" ), //4
                "SPECIALACTIVITYTIME"  => array(3   ,"活動時間数"   ), //5
            );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SPECIAL_ACTIVITY_MST');
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

                    $data = array(
                        "YEAR"                => $tmp[0],
                        "SPECIALCD"           => $tmp[1],
                        "SPECIAL_SDATE"       => $tmp[2],
                        "SPECIAL_EDATE"       => $tmp[3],
                        "SPECIALACTIVITYNAME" => $tmp[4],
                        "SPECIALACTIVITYTIME" => $tmp[5],
                        "PRIMARY_KEY"         => $tmp[0].$tmp[1], // データ重複チェック用
                    );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $temp_arr, $duplicateCsv1, $primaryKey1)) {
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
    public function errCheck($db, $data, $checkdata, $line, $temp_arr, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;       //必須エラーフラグ
        $numericErrFlg      = false;       //数値エラーフラグ
        $lengthErrFlg       = false;       //レングスエラーフラグ
        $rangeErrFlg        = false;       //範囲エラーフラグ
        $dateFormatErrFlg   = false;       //日付(フォーマット)エラーフラグ
        $duplicateCsvErrFlg = false;       //重複エラーフラグ
        $duplicateDbErrFlg  = false;       //重複(DB)

        $hissuErrMsg        = "";          //必須エラーメッセージ
        $numericErrMsg      = "";          //数値エラーメッセージ
        $lengthErrMsg       = "";          //レングスエラーメッセージ
        $rangeErrMsg        = "";          //範囲エラーメッセージ
        $dateFormatErrMsg   = "";          //日付(フォーマット)エラーメッセージ
        $duplicateCsvErrMsg = "";          //重複エラーメッセージ
        $duplicateDbErrMsg  = "";          //重複(DB)

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $rangeSep        = "";
        $dateFormatSep   = "";
        $duplicateCsvSep = "";
        $duplicateDbSep  = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SPECIALCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("YEAR","SPECIALCD","SPECIALACTIVITYTIME"))) {
                if ($data[$key] != "" && !preg_match('/^[0-9]+$/', $data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //日付チェック
            if (in_array($key, array("SPECIAL_SDATE"))) {
                if (common::isDate($data[$key]) == false) {
                    $returnFlg = $this->errSet($dateFormatErrFlg, $dateFormatErrMsg, "※日付(フォーマット):", $dateFormatSep, $val[1]);
                }
            }
            if (in_array($key, array("SPECIAL_EDATE"))) {
                if (common::isDate($data[$key]) == false) {
                    $returnFlg = $this->errSet($dateFormatErrFlg, $dateFormatErrMsg, "※日付(フォーマット):", $dateFormatSep, $val[1]);
                }
            }
        }
        //年度内の日付かどうか
        $yearStartDate = $data["YEAR"] . "-04-01";
        $yearEndDate = ($data["YEAR"] + 1) . "-03-31";
        $specialSdate = date("Y-m-d", strtotime($data["SPECIAL_SDATE"]));
        $specialEdate = date("Y-m-d", strtotime($data["SPECIAL_EDATE"]));
        if ($data["SPECIAL_SDATE"] != "" && $specialSdate < $yearStartDate || $yearEndDate < $specialSdate) {
            $returnFlg = $this->errSet($rangeErrFlg, $rangeErrMsg, "※範囲:", $rangeSep, "年度外の日付(".$checkdata["SPECIAL_SDATE"][1].")");
        }
        if ($data["SPECIAL_EDATE"] != "" && $specialEdate < $yearStartDate || $yearEndDate < $specialEdate) {
            $returnFlg = $this->errSet($rangeErrFlg, $rangeErrMsg, "※範囲:", $rangeSep, "年度外の日付(".$checkdata["SPECIAL_EDATE"][1].")");
        }
        //開始日付 > 終了日付のときはエラー
        if ($data["SPECIAL_SDATE"] != "" && $data["SPECIAL_EDATE"] != "" && $specialSdate > $specialEdate) {
            $returnFlg = $this->errSet($rangeErrFlg, $rangeErrMsg, "※範囲:", $rangeSep, "日付の大小が不正(".$checkdata["SPECIAL_SDATE"][1].". ".$checkdata["SPECIAL_EDATE"][1].")");
        }
        //既存データが存在すればエラー
        if (0 != $db->getOne(knjm704dQuery::checkDbDul($data))) {
            $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$rangeErrMsg.$dateFormatErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjm704dQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        $data = common::DateConv1($data, 4);
        if ($data) {
            $data = str_replace("/", "-", $data);
        }

        return $data;
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
}
