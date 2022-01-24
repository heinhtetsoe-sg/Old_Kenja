<?php

require_once('for_php7.php');
class knjx232Model extends Model
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

    public function knjx232Model()
    {
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
                             "STUDENT"          =>  VARS::post("STUDENT"),          //生徒一覧
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "STAFFCD"          =>  VARS::post("STAFFCD"),          //教員一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
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
                    knjx232Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx232Query::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array("SCHREGNO"       => array(8, "学籍番号"),
                               "ATTENDDATE"     => array(10,"出欠日付"),
                               "PERIODCD"       => array(1, "校時コード"),
                               "DI_CD"          => array(2, "勤怠コード"),
                               "DI_REMARK_CD"   => array(3, "備考コード"),
                               "DI_REMARK"      => array(60,"勤怠備考"));

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
            $primaryKey1 = $this->getPrimaryKey('ATTEND_DAT');
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
                    $data = array("SCHREGNO"        => trim($tmp[7]),
                                  "ATTENDDATE"      => trim($tmp[8]),
                                  "PERIODCD"        => trim($tmp[9]),
                                  "DI_CD"           => trim($tmp[10]),
                                  "DI_REMARK_CD"    => (trim($tmp[11])) ? sprintf("%03d", trim($tmp[11])) : trim($tmp[11]),
                                  "DI_REMARK"       => trim($tmp[12]));

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
        $dateErrFlg         = false;    //日付エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $duplicateCsvErrFlg = false;  //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数値エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $dateErrMsg         = "";       //日付エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $dateSep         = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if (in_array($key, array("SCHREGNO","ATTENDDATE","PERIODCD","DI_CD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("SCHREGNO","DI_CD","DI_REMARK_CD"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //存在チェック
                if (in_array($key, array("PERIODCD","DI_CD","DI_REMARK_CD"))) {
                    if ($key == "DI_CD") {
                        $cnt1 = $db->getOne(knjx232Query::checkAttendDiCdDat($this, $data[$key]));
                    } else {
                        $namecd1 = ($key == "PERIODCD") ? "B001" : "C901";
                        $cnt1 = $db->getOne(knjx232Query::checkNameMst($this, $namecd1, $data[$key]));
                    }
                    if ($data[$key] != "" && !$cnt1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "名称マスタに".$namecd1."の値が指定されてません。");
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("SCHREGNO","ATTENDDATE","PERIODCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //存在チェック
                if (in_array($key, array("PERIODCD"))) {
                    $namecd1 = "B001";
                    $cnt1 = $db->getOne(knjx232Query::checkNameMst($this, $namecd1, $data[$key]));
                    if ($data[$key] != "" && !$cnt1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]."(".$namecd1.")");
                    }
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //日付チェック
            if (in_array($key, array("ATTENDDATE"))) {
                if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, $val[1]);
                }
            }
        }
        if ($data["SCHREGNO"] != "" && $data["ATTENDDATE"] != "" && $this->dateRep($data["ATTENDDATE"]) && $data["PERIODCD"] != "") {
            //存在チェック
            $cnt2 = $db->getOne(knjx232Query::checkSchChrDat($this, $data["SCHREGNO"], $data["ATTENDDATE"], $data["PERIODCD"]));
            if (!$cnt2) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "時間割データが登録されていません。");
            }
        }


        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$dateErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx232Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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
        $header = array("SUBCLASSCD"        => "科目コード",
                        "SUBCLASSNAME"      => "科目名",
                        "CHAIRCD"           => "講座コード",
                        "CHAIRNAME"         => "講座名",
                        "HR_NAME"           => "年組",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "SCHREGNO"          => "※学籍番号",
                        "ATTENDDATE"        => "※出欠日付",
                        "PERIODCD"          => "※校時コード",
                        "DI_CD"             => "※勤怠コード",
                        "DI_REMARK_CD"      => "備考コード",
                        "DI_REMARK"         => "勤怠備考",
                        $this->lastColumn   => $this->lastColumn);

        if ($this->Properties["useCurriculumcd"] == '1') {
            $subclasscd_rei = "12-H-1-123456";
        } else {
            $subclasscd_rei = "123456";
        }

        $header_rei = array("SUBCLASSCD"      => $subclasscd_rei,
                            "SUBCLASSNAME"    => "国語",
                            "CHAIRCD"         => "1234567",
                            "CHAIRNAME"       => "国語1-1",
                            "HR_NAME"         => "1年1組",
                            "ATTENDNO"        => "001番",
                            "NAME"            => "アルプ太郎",
                            "SCHREGNO"        => "12345678",
                            "ATTENDDATE"      => str_replace("-", "/", CTRL_DATE),
                            "PERIODCD"        => "1",
                            "DI_CD"           => "1",
                            "DI_REMARK_CD"    => "001",
                            "DI_REMARK"       => "全角20文字",
                            $this->lastColumn => $this->lastColumn);

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
            $tables = array("1" => "ATTEND_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $header = array();
            $header["SCHREGNO"]    = "学籍番号";
            $header["ATTENDDATE"]  = "出欠日付";
            $header["PERIODCD"]    = "校時コード";
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
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx232Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "出欠情報データ";
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
