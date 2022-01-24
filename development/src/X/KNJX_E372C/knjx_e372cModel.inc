<?php

require_once('for_php7.php');

class knjx_e372cModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx_e372cModel()
    {
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
            $this->sendGradeHrClass = VARS::get("SEND_GRADE_HR_CLASS");
            $this->limitCnt = VARS::get("LIMITCNT");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("OUTPUT"               =>  VARS::post("OUTPUT"),               //出力取込種別
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //年組一覧
                             "HEADER"               =>  VARS::post("HEADER"),               //ヘッダ有
                             "SHORI_MEI"            =>  VARS::post("SHORI_MEI"),            //処理名
                             "SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND")           //校種
                             );

        if (VARS::get("SEND_PRGID")) {
            $this->field["GRADE_HR_CLASS"] = (VARS::get("SEND_GRADE_HR_CLASS")) ? VARS::get("SEND_GRADE_HR_CLASS") : $this->field["SEND_GRADE_HR_CLASS"];
        }

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $db = Query::dbCheckOut();

        //推薦枠マスタ学科番号 取得
        $this->departmentCdArray = array();
        $this->departmentCdArray = knjx_e372cQuery::getRecLimitDepartmentCd($db);

        //校友会活動CD 取得
        $query = knjx_e372cQuery::getNameMstE071();
        $result = $db->query($query);
        $this->nameMstE071 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->nameMstE071[$row["VALUE"]] = $row["LABEL"];
        }

        Query::dbCheckIn($db);

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
                    knjx_e372cQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_e372cQuery::deleteQueryCsv($this, $this->data_arr);
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

            $checkdata = array();
            $checkdata["GRADE"]                 = array("HISSU", 2, "学年");
            $checkdata["HR_CLASS"]              = array("HISSU", 3, "組");
            $checkdata["ATTENDNO"]              = array("HISSU", 3, "出席番号");
            for ($i = 1; $i <= $this->limitCnt; $i++) {
                $hopeOrder = sprintf("%02d", $i);
                $checkdata["HOPE_ORDER_{$hopeOrder}"]      = array("HOPEORDER", 2, "学科番号{$i}");
            }
            $checkdata["ACTIVITY_CD"]           = array("HISSU", 1, "校友会活動CD");
            $checkdata["ACTIVITY_CONTENT"]      = array("", 300, "校友会活動内容");
            $checkdata["DECLINE_FLG"]           = array("", 1, "辞退");

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
            $primaryKey1   = $this->getPrimaryKey('AFT_SCHREG_HOPE_DEPARTMENT');
            $primaryKey2   = $this->getPrimaryKey('AFT_SCHREG_RECOMMENDATION_INFO_DAT');
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

                    $data = array();
                    $data["GRADE"]          = (strlen(trim($tmp[0]))) ? sprintf("%02d", $tmp[0]) : "";
                    $data["HR_CLASS"]       = (strlen(trim($tmp[1]))) ? sprintf("%03d", $tmp[1]) : "";
                    $data["ATTENDNO"]       = (strlen(trim($tmp[2]))) ? sprintf("%03d", $tmp[2]) : "";
                    $csvItemNo = 3;
                    for ($j = 1; $j <= $this->limitCnt; $j++) {
                        $hopeOrder = sprintf("%02d", $j);
                        $data["HOPE_ORDER_{$hopeOrder}"]  = (strlen(trim($tmp[$csvItemNo]))) ? $tmp[$csvItemNo] : "";
                        $csvItemNo++;
                    }
                    $csvItemNo = 3 + $this->limitCnt;
                    $data["ACTIVITY_CD"]        = (strlen(trim($tmp[$csvItemNo]))) ? $tmp[$csvItemNo] : "";
                    $csvItemNo++;
                    $data["ACTIVITY_CONTENT"]   = (strlen(trim($tmp[$csvItemNo]))) ? $tmp[$csvItemNo] : "";
                    $csvItemNo++;
                    $data["DECLINE_FLG"]        = (strlen(trim($tmp[$csvItemNo]))) ? $tmp[$csvItemNo] : "";
                    $checkCnt = 7;
                    $checkCnt += $this->limitCnt;
                    $data["YEAR"]               = CTRL_YEAR;
                    $data["SCHREGNO"]           = $db->getOne(knjx_e372cQuery::getSchregno($data["GRADE"], $data["HR_CLASS"], $data["ATTENDNO"]));

                    //項目数によるファイルチェック
                    if (get_count($tmp) != $checkCnt) {
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
        $valuesErrFlg       = false;    //値不正エラーフラグ
        $orderErrFlg        = false;    //固有エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $valuesErrMsg       = "";       //値不正エラーメッセージ
        $orderErrMsg        = "";       //固有エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $valuesSep       = "";
        $orderSep        = "";
        $duplicateCsvSep = "";

        if ($data["SCHREGNO"]) {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[0] == "HISSU") {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[2]);
                }
            }
            //数値チェック
            if (in_array($key, array("GRADE", "HR_CLASS", "ATTENDNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[2]);
                }
            } elseif ($this->field["SHORI_MEI"] == "1" && in_array($key, array("ACTIVITY_CD", "DECLINE_FLG"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[2]);
                }
            }
            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if ($val[0] == "TEXT") {
                    list($moji, $gyou) = $val[3];
                    if ($this->validate_row_cnt($data[$key], $moji * 2) > $gyou) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[2]);
                    }
                } elseif (strlen($data[$key]) > $val[1]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[2]);
                }

                //存在チェック（学科番号)
                if ($val[0] == "HOPEORDER" && $data[$key] != "" && !in_array($data[$key], $this->departmentCdArray)) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "指定された学科番号がありません。");
                }

                //存在チェック（校友会活動CD)
                if ($key == "ACTIVITY_CD" && $data[$key] != "" && !array_key_exists($data[$key], $this->nameMstE071)) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[2]);
                }

                //値チェック(辞退フラグ ※0:辞退でないor1:辞退)
                if ($key == "DECLINE_FLG" && $data[$key] != "" && !in_array($data[$key], array("0", "1"))) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[2]);
                }
            }
        }

        //存在チェック（在籍）
        if (1 > $db->getOne(knjx_e372cQuery::checkRegdDat($data["GRADE"], $data["HR_CLASS"], $data["ATTENDNO"]))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データがありません。");
        }

        //学科番号チェック
        $chkStartFlg = false;
        $dupChkArray = array();
        for ($j = 1; $j <= $this->limitCnt; $j++) {
            $hopeOrder = sprintf("%02d", $j);
            $departmentCd = $data["HOPE_ORDER_{$hopeOrder}"];

            //1.学科番号が志望順に登録されているかチェック(順位を飛ばして登録されたらエラー)
            if (!$chkStartFlg && $departmentCd == "") {
                $chkStartFlg = true;
            }
            if ($chkStartFlg && $departmentCd != "") {
                $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "志望順エラー");
                break;
            }

            //2.異なる順位に同じ学科番号が登録されているかチェック
            if ($departmentCd != "") {
                if (in_array($departmentCd, $dupChkArray)) {
                    $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "学科番号重複");
                    break;
                }
                $dupChkArray[] = $departmentCd;
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$valuesErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_e372cQuery::insertQueryErr($db, $line, $checkError);
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

        //ヘッダー
        $header = array();
        $header["GRADE"]                = "※学年";
        $header["HR_CLASS"]             = "※組";
        $header["ATTENDNO"]             = "※出席番号";
        for ($i = 1; $i <= $this->limitCnt; $i++) {
            $hopeOrder = sprintf("%02d", $i);
            $header["HOPE_ORDER_".$hopeOrder] = "学科番号{$i}";
        }
        $header["ACTIVITY_CD"]          = "校友会活動CD";
        $header["ACTIVITY_CONTENT"]     = "校友会活動内容";
        $header["DECLINE_FLG"]          = "※辞退";
        $header[$this->lastColumn]      = $this->lastColumn;

        //ヘッダー例
        $header_rei = array();
        $header_rei["GRADE"]                = "2桁";
        $header_rei["HR_CLASS"]             = "3桁";
        $header_rei["ATTENDNO"]             = "3桁";
        for ($i = 1; $i <= $this->limitCnt; $i++) {
            $hopeOrder = sprintf("%02d", $i);
            $header_rei["HOPE_ORDER_".$hopeOrder] = "2桁";
        }
        $header_rei["ACTIVITY_CD"]          = implode("/", $this->nameMstE071);
        $header_rei["ACTIVITY_CONTENT"]     = "全角100文字";
        $header_rei["DECLINE_FLG"]          = "0:辞退しない/1:辞退";
        $header_rei[$this->lastColumn]      = $this->lastColumn;


        $contents  = "";    //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("YEAR"           => "年度",
                                     "SCHREGNO"       => "学籍番号",
                                     "HOPE_ORDER"     => "学科番号");

            $tables = array();
            $tables = array("1" => "AFT_SCHREG_HOPE_DEPARTMENT", "2" => "AFT_SCHREG_RECOMMENDATION_INFO_DAT");
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
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_e372cQuery::getList($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {         //データ出力
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);

                    $row[$this->lastColumn] = $this->lastColumn;

                    $sep = "";
                    foreach ($header as $key => $val) {
                        $contents .= $sep.$row[$key];
                        $sep = ",";
                    }
                    $contents .= "\n";
                } else {        //エラー出力
                    $contents .= implode($row, ",") ."\n";
                }
                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {     //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "志望学科登録";
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
