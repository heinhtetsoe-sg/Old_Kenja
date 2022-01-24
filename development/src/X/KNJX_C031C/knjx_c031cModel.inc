<?php

require_once('for_php7.php');
class knjx_c031cModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx_c031cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "MONTH"            =>  VARS::post("MONTH"),            //対象月
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        if (VARS::get("SEND_PRGID")) {
            $this->field["GRADE_HR_CLASS"] = (VARS::get("SEND_hr_class")) ? str_replace("-", "", VARS::get("SEND_hr_class")) : $this->field["GRADE_HR_CLASS"];
            $this->field["MONTH"]          = (VARS::get("SEND_month"))    ? VARS::get("SEND_month") : $this->field["MONTH"];
        }

        if ($this->field["GRADE_HR_CLASS"]) {
            $this->grade = substr($this->field["GRADE_HR_CLASS"], 0, 2);
        }

        $this->cmd  = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
            if (VARS::post("selectSchoolKind")) {
                $this->selectSchoolKind = VARS::post("selectSchoolKind");
            } elseif (VARS::get("selectSchoolKind")) {
                $this->selectSchoolKind = VARS::get("selectSchoolKind");
            }
        }

        $this->file = VARS::file("FILE");

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
                    knjx_c031cQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_c031cQuery::deleteQueryCsv($this, $this->data_arr);
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
            $db = Query::dbCheckOut();

            //欠課種別
            $titleName = array();
            $query = knjx_c031cQuery::getSickDiv();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $titleName[$row["VALUE"]] = $row["LABEL"];
            }

            //詳細種別
            $titleDetailName = array();
            $query = knjx_c031cQuery::getDetailDiv();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $titleDetailName[$row["VALUE"]] = $row["LABEL"];
            }

            $checkdata["YEAR"]      = array(4,"年度");
            $checkdata["MONTH"]     = array(2,"対象月");
            $checkdata["SEMESTER"]  = array(1,"学期");
            $checkdata["SCHREGNO"]  = array(8,"学籍番号");
            $checkdata["LESSON"]    = array(3,"授業日数");
            $checkdata["OFFDAYS"]   = array(3,"休学日数");
            $checkdata["ABROAD"]    = array(3,"留学日数");
            $checkdata["ABSENT"]    = array(3,"公欠日数");
            if ($this->Properties["useKoudome"] == "true") {
                $checkdata["SUSPEND"]   = array(3,"出停(法止)");
                $checkdata["KOUDOME"]   = array(3,"出停(交止)");
            } else {
                $checkdata["SUSPEND"]   = array(3,"出停");
            }
            if ($this->Properties["useVirus"] == "true") {
                $checkdata["VIRUS"]   = array(3,"出停(伝染)");
            }
            $checkdata["MOURNING"]  = array(3,"忌引");

            $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
            foreach ($titleName as $key => $val) {
                $checkdata[$setFieldName[$key]] = array(3, $val);
            }
            $checkdata["LATE"]      = array(3,"遅刻");
            $checkdata["EARLY"]     = array(3,"早退");

            foreach ($titleDetailName as $key => $val) {
                $checkdata["DETAIL_".$key] = array(3, $val);
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey2 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('ATTEND_SEMES_DAT');
            $primaryKey2 = $this->getPrimaryKey('ATTEND_SEMES_DETAIL_DAT');

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

                    $dataCnt = 3;
                    $data["YEAR"]       = (strlen(trim($tmp[$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["MONTH"]      = (strlen(trim($tmp[++$dataCnt]))) ? sprintf("%02d", $tmp[$dataCnt]) : "";
                    $data["SEMESTER"]   = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["SCHREGNO"]   = (strlen(trim($tmp[++$dataCnt]))) ? sprintf("%08d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["LESSON"]     = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["OFFDAYS"]    = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["ABROAD"]     = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["ABSENT"]     = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["SUSPEND"]    = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    if ($this->Properties["useKoudome"] == "true") {
                        $data["KOUDOME"]    = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    }
                    if ($this->Properties["useVirus"] == "true") {
                        $data["VIRUS"]    = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    }
                    $data["MOURNING"]   = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";

                    $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
                    foreach ($titleName as $key => $val) {
                        $data[$setFieldName[$key]] = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    }
                    $data["LATE"]       = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["EARLY"]      = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
                    $data["COPYCD"]     = "";
                    $data["SEQ"]        = "";

                    foreach ($titleDetailName as $key => $val) {
                        $data["DETAIL_".$key] = (strlen(trim($tmp[++$dataCnt]))) ? $tmp[$dataCnt] : "";
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
            $db->commit();  // トランザクションをコミットする。
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
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
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
            if (in_array($key, array("SCHREGNO","YEAR","MONTH","SEMESTER"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if ($data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        //存在チェック（在籍）
        if (1 > $db->getOne(knjx_c031cQuery::checkRegdDat($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
        }

        //存在チェック（対象月）
        if (!$db->getOne(knjx_c031cQuery::selectMonthQuery($data["YEAR"], $data["MONTH"], $this))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された年月のデータがありません。");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_c031cQuery::insertQueryErr($db, $line, $checkError);
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
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダー（エラー）
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //欠課種別
        $titleName = array();
        $query = knjx_c031cQuery::getSickDiv();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $titleName[$row["VALUE"]] = $row["LABEL"];
        }

        //詳細種別
        $titleDetailName = array();
        $query = knjx_c031cQuery::getDetailDiv();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $titleDetailName[$row["VALUE"]] = $row["LABEL"];
        }

        //ヘッダー
        $header["HR_NAME"]          = "学級";
        $header["ATTENDNO"]         = "No.";
        $header["NAME"]             = "氏名";
        $header["YEAR"]             = "※年度";
        $header["MONTH"]            = "※対象月";
        $header["SEMESTER"]         = "※学期";
        $header["SCHREGNO"]         = "※学籍番号";
        $header["APPOINTED_DAY"]    = "締め日";
        $header["LESSON"]           = "授業日数";
        $header["OFFDAYS"]          = "休学日数";
        $header["ABROAD"]           = "留学日数";
        $header["ABSENT"]           = "公欠日数";
        if ($this->Properties["useKoudome"] == "true") {
            $header["SUSPEND"]          = "出停(法止)";
            $header["KOUDOME"]          = "出停(交止)";
        } else {
            $header["SUSPEND"]          = "出停";
        }
        if ($this->Properties["useVirus"] == "true") {
            $header["VIRUS"]            = "出停(伝染)";
        }
        $header["MOURNING"]         = "忌引";

        $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
        foreach ($titleName as $key => $val) {
            $header[$setFieldName[$key]] = $val;
        }
        $header["LATE"]             = "遅刻";
        $header["EARLY"]            = "早退";

        foreach ($titleDetailName as $key => $val) {
            $header["DETAIL_".$key] = $val;
        }

        $header[$this->lastColumn]   = $this->lastColumn;

        //ヘッダー例
        $header_rei["HR_NAME"]          = "1年1組";
        $header_rei["ATTENDNO"]         = "001";
        $header_rei["NAME"]             = "アルプ太郎";
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header_rei["MONTH"]            = "04";
        $header_rei["SEMESTER"]         = "1";
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["APPOINTED_DAY"]    = "30";
        $header_rei["LESSON"]           = "10";
        $header_rei["OFFDAYS"]          = "10";
        $header_rei["ABROAD"]           = "10";
        $header_rei["ABSENT"]           = "10";
        $header_rei["SUSPEND"]          = "10";
        if ($this->Properties["useKoudome"] == "true") {
            $header_rei["KOUDOME"]          =  "10";
        }
        if ($this->Properties["useVirus"] == "true") {
            $header_rei["VIRUS"]            =  "10";
        }
        $header_rei["MOURNING"]         = "10";

        $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
        foreach ($titleName as $key => $val) {
            $header_rei[$setFieldName[$key]] = "10";
        }

        $header_rei["LATE"]             = "10";
        $header_rei["EARLY"]            = "10";

        foreach ($titleDetailName as $key => $val) {
            $header_rei["DETAIL_".$key] = "10";
        }

        $header_rei[$this->lastColumn]  = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("YEAR"                 => "年度",
                                    "SEMESTER"              => "学期",
                                    "COPYCD"                => "複写区分",
                                    "SEQ"                   => "詳細区分",
                                    "SCHREGNO"              => "学籍番号",
                                    "MONTH"                 => "対象月");
            $tables = array();
            $tables = array("1" => "ATTEND_SEMES_DAT", "2" => "ATTEND_SEMES_DETAIL_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
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
            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_c031cQuery::selectMainQuery($this, $titleName, $titleDetailName);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {              //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "出欠情報入力";
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
