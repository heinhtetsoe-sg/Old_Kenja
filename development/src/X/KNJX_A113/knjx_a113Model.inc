<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_a113Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード

    /**
     * メンバ変数の初期化
     */
    public function knjx_a113Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }

    public function init()
    {
        $this->field = array("GRADE"         =>  VARS::post("GRADE"),                //学年一覧
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
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
                    knjx_a113Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_a113Query::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array();
            $checkdata["YEAR"]          = array(4,  "HISSU",  "INT",  "年度");
            $checkdata["SCHREGNO"]      = array(8,  "HISSU",  "INT",  "学籍番号");
            $checkdata["SCHOLARSHIP"]   = array(3,  "HISSU",  "INT",  "校納金コード");
            $checkdata["FROM_DATE"]     = array(10, "HISSU",  "DATE", "開始年月");
            $checkdata["TO_DATE"]       = array(10, "HISSU",  "DATE", "終了年月");
            $checkdata["REMARK"]        = array(50, "",       "",     "備考");

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
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_SCHOLARSHIP_HIST_DAT');
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
                    $data["YEAR"]           = $tmp[$tmpcnt++];
                    $data["SCHREGNO"]       = $tmp[$tmpcnt++];
                    $data["GRADE"]          = $tmp[$tmpcnt++];
                    $data["HR_CLASS"]       = $tmp[$tmpcnt++];
                    $data["ATTENDNO"]       = $tmp[$tmpcnt++];
                    $data["SCHOLARSHIP"]    = $tmp[$tmpcnt++];
                    $data["FROM_DATE"]      = $tmp[$tmpcnt++];
                    $data["TO_DATE"]        = $tmp[$tmpcnt++];
                    $data["REMARK"]         = $tmp[$tmpcnt++];

                    $dataCnt = get_count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //校種セット
                    $data["SCHOOL_KIND"] = "";

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
    public function errCheck($db, &$data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $dateErrFlg         = false;
        $duplicateDbErrFlg  = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $dateErrMsg         = "";
        $duplicateDbErrMsg  = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $dateSep         = "";
        $duplicateDbSep  = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
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

        //存在チェック（在籍）
        if (1 > $db->getOne(knjx_a113Query::checkSchData($this, $data["YEAR"], $data["SCHREGNO"]))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学籍番号");
        }

        if ($returnFlg) {
            //校種取得
            $query = knjx_a113Query::getSchoolKind($data["YEAR"], $data["SCHREGNO"]);
            $rowSchoolKind = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $data["SCHOOL_KIND"] = $rowSchoolKind["SCHOOL_KIND"];

            if ($this->isDate($data["FROM_DATE"]."/01") == false) {
                $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付不正:", $dateSep, "開始年月");
            }

            //開始年月を年月日へ変換
            $data["FROM_DATE"] = date('Y-m-d', strtotime('first day of ' . str_replace("/", "-", $data["FROM_DATE"])));

            //日付チェック 開始年月
            if ($this->dateRep($data["FROM_DATE"]) == "") {
                $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "開始年月");
            }
        }

        if ($this->field["SHORI_MEI"] == "1" && $returnFlg) {
            if ($this->isDate($data["TO_DATE"]."/01") == false) {
                $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付不正:", $dateSep, "終了年月");
            }

            //開始年月を年月日へ変換
            $data["TO_DATE"] = date('Y-m-d', strtotime('last day of ' . str_replace("/", "-", $data["TO_DATE"])));

            //日付チェック 終了年月
            if ($this->dateRep($data["TO_DATE"]) == "") {
                $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "終了年月");
            }

            //存在チェック（校納金コード）
            if (1 > $db->getOne(knjx_a113Query::checkReductionDivCd($this, $data))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "校納金コード");
            }

            //開始年月と終了年月の大小チェック
            if (($data["FROM_DATE"] > $data["TO_DATE"])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付の大小:", $numericSep, "期間の開始年月と終了年月の大小");
            }

            //開始年月と終了年月の期間の重複チェック
            if (0 < $db->getOne(knjx_a113Query::checkScholarshipHistDat($this, $data))) {
                $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$dateErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_a113Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //日付の妥当性チェック
    public function isDate($datestr)
    {
        if ($datestr == "") {
            return true;
        }
        $tmp = explode(substr($datestr, 4, 1), $datestr);
        return checkdate($tmp[1], $tmp[2], $tmp[0]);
    }

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
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

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダデータ
        $header = array();
        $header["YEAR"]             = "※年度";
        $header["SCHREGNO"]         = "※学籍番号";
        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["SCHOLARSHIP"]      = "※校納金コード";
        $header["FROM_DATE"]        = "※開始年月"; //TODO: 開始日→開始年月
        $header["TO_DATE"]          = "※終了年月"; //TODO: 終了日→終了年月
        $header["REMARK"]           = "備考";

        $header_rei = array();
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["GRADE"]            = "99";
        $header_rei["HR_CLASS"]         = "999";
        $header_rei["ATTENDNO"]         = "999";
        $header_rei["SCHOLARSHIP"]      = "99";
        $header_rei["FROM_DATE"]        = CTRL_YEAR."/04";
        $header_rei["TO_DATE"]          = CTRL_YEAR."/12";
        $header_rei["REMARK"]           = "備考";

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array();
            $headerDuplicate["SCHOOLCD"]    = "校種学校コード";
            $headerDuplicate["SCHOOL_KIND"] = "校種";
            $headerDuplicate["SCHOLARSHIP"] = "※校納金コード";
            $headerDuplicate["SCHREGNO"]    = "※学籍番号";
            $headerDuplicate["FROM_DATE"]   = "※開始年月";
            $tables = array("1" => "SCHREG_SCHOLARSHIP_HIST_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header_err)."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode(",", $header)."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_a113Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {  //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                }

                foreach (array("FROM_DATE", "TO_DATE") as $key) {
                    if ($row[$key]) {
                        list($yyyy, $mm, $dd) = explode("-", $row[$key]);
                        $row[$key] = $yyyy."/".$mm;
                    }
                }

                $contents .= implode(",", $row) ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $title = "エラー出力";
            } else {                                //エラー以外
                $title = "特待合格者データ";
            }
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
