<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_hreportremark_datModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $fieldSize = array();
    public $candidate = array();
    public $schoolkind;
    public $getPrgId;
    public $sendAuth;
    public $auth;
    public $ignoreNameArry;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx_hreportremark_datModel()
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
                             "GRADE"            =>  VARS::post("GRADE"),             //学年一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        if (VARS::get("FIELDSIZE")) {
            $this->fieldSize = array();
            $fieldget = preg_split("{,}", VARS::get("FIELDSIZE"));
            for ($i = 0; $i < get_count($fieldget); $i++) {
                list($fName, $fSize, $fMeisyou) = preg_split("{=}", $fieldget[$i]);
                if ($fSize > 0) {
                    $this->fieldSize[$fName]["SIZE"] = $fSize;
                    $this->fieldSize[$fName]["MEISYOU"] = $fMeisyou;
                }
            }

            $this->schoolkind = VARS::get("SCHOOL_KIND") ? VARS::get("SCHOOL_KIND") : "";

            //初期化
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //学年コード
        $db = Query::dbCheckOut();
        $this->gradeCD = array();
        $query = knjx_hreportremark_datquery::getGrade($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->gradeCD[$row["VALUE"]] = $row["GRADE_CD"];
        }
        $result->free();
        Query::dbCheckIn($db);

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
                    knjx_hreportremark_datQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_hreportremark_datQuery::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array("YEAR"       => array(4, "年度"),
                               "SEMESTER"   => array(4, "学期"),
                               "SCHREGNO"   => array(8, "学籍番号"));
            //対象項目のみデータに追加する。
            foreach ($this->fieldSize as $key => $val) {
                $checkdata[$key] = array(array($val["SIZE"], $val["MEISYOU"]));
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
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('HREPORTREMARK_DAT');
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
                    $data = array("GRADE"      => $tmp[$tmpcnt++],
                                  "HR_CLASS"   => $tmp[$tmpcnt++],
                                  "ATTENDNO"   => $tmp[$tmpcnt++],
                                  "NAME"       => $tmp[$tmpcnt++],
                                  "YEAR"       => $tmp[$tmpcnt++],
                                  "SEMESTER"   => $tmp[$tmpcnt++],
                                  "SCHREGNO"   => $tmp[$tmpcnt++]);

                    //KNJD138P呼び出し対象外レコード
                    $ignoreNameArry = array();
                    $ignoreNameArry = array("TOTALSTUDYTIME", "COMMITTEE", "CLUB");
                    //対象項目のみデータに追加する。
                    foreach ($this->fieldSize as $key => $val) {
                        if ($this->getPrgId == "KNJD138P" && (int)$this->gradeCD[$this->field["GRADE"]] < 3 && in_array($key, $ignoreNameArry)) {
                            continue;
                        }
                        $data[$key] = $tmp[$tmpcnt++];
                    }
                    $data[$this->lastColumn] = $tmp[$tmpcnt++];

                    //項目数によるファイルチェック
                    if ($checkcount != $tmpcnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return array();
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

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
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

        if ($this->schoolkind) {
            //存在チェック
            $cnt_check = $db->getOne(knjx_hreportremark_datQuery::checkSchoolKind($this, $data));
            if ($cnt_check == 0) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_hreportremark_datQuery::insertQueryErr($db, $line, $checkError);
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
        $header = array("GRADE"             => "学年",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "YEAR"              => "※年度",
                        "SEMESTER"          => "※学期",
                        "SCHREGNO"          => "※学籍番号");

        //KNJD138P呼び出し対象外レコード
        $ignoreNameArry = array();
        $ignoreNameArry = array("TOTALSTUDYTIME", "COMMITTEE", "CLUB");
        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize as $key => $val) {
            if ($this->getPrgId == "KNJD138P" && (int)$this->gradeCD[$this->field["GRADE"]] < 3 && in_array($key, $ignoreNameArry)) {
                continue;
            }
            $header[$key] = $val["MEISYOU"];
        }
        $header[$this->lastColumn] = $this->lastColumn;

        $header_rei = array("GRADE"            => "99",
                            "HR_CLASS"         => "999",
                            "ATTENDNO"         => "999",
                            "NAME"             => "アルプ太郎",
                            "YEAR"             => "9999",
                            "SEMESTER"         => "9",
                            "SCHREGNO"         => "12345678");

        //対象項目のみヘッダデータに追加する。
        foreach ($this->fieldSize as $key => $val) {
            if ($this->getPrgId == "KNJD138P" && (int)$this->gradeCD[$this->field["GRADE"]] < 3 && in_array($key, $ignoreNameArry)) {
                continue;
            }
            $header_rei[$key] = "全角".$val["SIZE"]."文字";
        }
        $header_rei[$this->lastColumn] = $this->lastColumn;

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
            $tables = array("1" => "HREPORTREMARK_DAT");
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

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
                $array = $header_err;
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_hreportremark_datQuery::selectMainQuery($this);
                $array = $header;
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($array as $field => $fieldLabel) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
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
