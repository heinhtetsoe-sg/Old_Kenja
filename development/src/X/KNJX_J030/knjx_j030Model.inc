<?php

require_once('for_php7.php');
class knjx_j030Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjx_j030Model()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            unset($this->selectSchoolKind);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /****************/
    /*  ＣＳＶ取込  */
    /****************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "2") {
                    knjx_j030Query::deleteQueryCsv($this, $this->data_arr);
                } else {
                    knjx_j030Query::updateQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"           => array(4, "年度"),
                               "SCHREGNO"       => array(8, "学籍番号"),
                               "CLUBCD"         => array(4, "部/クラブコード"),
                               "SDATE"          => array(10,"入部日付"),
                               "EDATE"          => array(10,"退部日付"),
                               "EXECUTIVECD"    => array(2, "役職コード"),
                               "REMARK"         => array(40,"備考"));

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
            $primaryKey1   = $this->getPrimaryKey('SCHREG_CLUB_HIST_DAT');
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

                    $data = array("YEAR"            => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0])  : "",
                                  "SCHREGNO"        => (strlen(trim($tmp[4])))  ? sprintf("%08d", $tmp[4])  : "",
                                  "CLUBCD"          => (strlen(trim($tmp[7])))  ? sprintf("%04d", $tmp[7])  : "",
                                  "SDATE"           => $tmp[9],
                                  "EDATE"           => $tmp[10],
                                  "EXECUTIVECD"     => $tmp[11],
                                  "REMARK"          => $tmp[13]);

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
        $masterErrFlg       = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $dateFormatErrFlg   = false;    //日付(フォーマット)エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $dateFormatErrMsg   = "";       //日付(フォーマット)エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $dateFormatSep   = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SCHREGNO","CLUBCD","SDATE"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("YEAR","SCHREGNO","CLUBCD","EXECUTIVECD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //日付チェック
            if (in_array($key, array("SDATE","EDATE"))) {
                if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                    $returnFlg = $this->errSet($dateFormatErrFlg, $dateFormatErrMsg, "※日付(フォーマット):", $dateFormatSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //存在チェック
            if (in_array($key, array("SCHREGNO","CLUBCD","EXECUTIVECD"))) {
                $query = "";
                if ($key == "SCHREGNO") {
                    $query = knjx_j030Query::checkSchregRegdDat($this, $data["YEAR"], $data[$key]);
                }
                if ($key == "CLUBCD") {
                    $query = knjx_j030Query::checkClubMst($this, $data[$key], $data["SCHREGNO"]);
                }
                if ($key == "EXECUTIVECD") {
                    $query = knjx_j030Query::checkNameMst('J001', $data[$key]);
                }

                if ($query) {
                    if ($data[$key] != "" && 0 == $db->getOne($query)) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }
            }
        }

        //存在チェック
        if ($this->field["SHORI_MEI"] == "2") {
            if (0 == $db->getOne(knjx_j030Query::checkSchregClubHistDat($this, $data["SCHREGNO"], $data["CLUBCD"], $data["SDATE"]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "所属クラブではありません");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg.$dateFormatErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_j030Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //日付変換 項目ﾁｪｯｸ後に行う。
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

    /****************/
    /*  ＣＳＶ出力  */
    /****************/
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダデータ
        $header = array("YEAR"              => "※年度",
                        "GRADE"             => "年",
                        "HR_CLASS"          => "組",
                        "ATTENDNO"          => "番",
                        "SCHREGNO"          => "※学籍番号",
                        "NAME"              => "氏名",
                        "NAME_KANA"         => "氏名ふりがな",
                        "CLUBCD"            => "※部/クラブコード",
                        "CLUBNAME"          => "部/クラブ名称",
                        "SDATE"             => "※入部日付",
                        "EDATE"             => "退部日付",
                        "EXECUTIVECD"       => "役職コード",
                        "EXECUTIVENAME"     => "役職名称",
                        "REMARK"            => "備考",
                        $this->lastColumn   => $this->lastColumn);

        //ヘッダ例
        $header_rei = array("YEAR"            => CTRL_YEAR,
                            "GRADE"           => "01",
                            "HR_CLASS"        => "001",
                            "ATTENDNO"        => "001",
                            "SCHREGNO"        => "12345678",
                            "NAME"            => "アルプ太郎",
                            "NAME_KANA"       => "あるぷたろう",
                            "CLUBCD"          => "0001",
                            "CLUBNAME"        => "野球部",
                            "SDATE"           => CTRL_YEAR."-04-01",
                            "EDATE"           => (CTRL_YEAR+1)."-03-31",
                            "EXECUTIVECD"     => "1",
                            "EXECUTIVENAME"   => "部員",
                            "REMARK"          => "全角20文字まで",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";    //クリア

        $csv_output = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ出力
            $csv_output = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー出力
            $tables = array();
            $tables = array("1" => "SCHREG_CLUB_HIST_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $headerDuplicate = array();
            $headerDuplicate["SCHOOLCD"]    = "校種学校コード";
            $headerDuplicate["SCHOOL_KIND"] = "校種";
            $headerDuplicate["SCHREGNO"]    = "学籍番号";
            $headerDuplicate["CLUBCD"]      = "部/クラブコード";
            $headerDuplicate["SDATE"]       = "入部日付";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            if ($this->field["OUTPUT"] == "3") {
                //エラー出力
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ出力
                $query = knjx_j030Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $csv_output = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($csv_output) {
            if ($this->field["OUTPUT"] == "3") {    //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "部員名簿データ";
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
