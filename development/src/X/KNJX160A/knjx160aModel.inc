<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx160aModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $nameSLen;
    public $nameSLen2;
    public $lastColumn;//CSVの最後の列の値
    public $addrSLen;  //住所の入力文字数

    /**
     * メンバ変数の初期化
     */
    public function knjx160aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
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

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->file = VARS::file("FILE");
        $this->nameSLen = $this->Properties["NAME_INPUT_SIZE"] ? $this->Properties["NAME_INPUT_SIZE"] : 20;
        $this->nameSLen2 = $this->Properties["NAME_INPUT_SIZE"] ? $this->Properties["NAME_INPUT_SIZE"] : 40;
        $this->addrSLen = $this->Properties["ADDR_INPUT_SIZE"] ? $this->Properties["ADDR_INPUT_SIZE"] : 50;

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
                    knjx160aQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx160aQuery::deleteQueryCsv($this, $this->data_arr);
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
            $checkdata = array("SCHREGNO"                   => array(8,"学籍番号"),
                               "RELATIONSHIP"               => array(2,"続柄"),
                               "GUARD_NAME"                 => array(intval($this->nameSLen) * 3, "保護者氏名"),
                               "GUARD_KANA"                 => array(120,"保護者氏名かな"),
                               "GUARD_REAL_NAME"            => array(intval($this->nameSLen2) * 3,"戸籍保護者氏名"),
                               "GUARD_REAL_KANA"            => array(240,"戸籍保護者氏名かな"),
                               "GUARD_SEX"                  => array(1,"性別"),
                               "GUARD_BIRTHDAY"             => array(10,"生年月日"),
                               "ISSUEDATE"                  => array(10,"有効期間開始日"),
                               "EXPIREDATE"                 => array(10,"有効期間終了日"),
                               "GUARD_ZIPCD"                => array(8,"郵便番号"),
                               "GUARD_ADDR1"                => array(array(intval($this->addrSLen) * 3, "住所1")),
                               "GUARD_ADDR2"                => array(array(intval($this->addrSLen) * 3, "住所2")),
                               "GUARD_ADDR_FLG"             => array(1,"肩書き出力可"),
                               "GUARD_TELNO"                => array(14,"電話番号"),
                               "GUARD_TELNO2"               => array(14,"電話番号２"),
                               "GUARD_FAXNO"                => array(14,"FAX番号"),
                               "GUARD_E_MAIL"               => array(50,"E-MAIL"),
                               "GUARD_JOBCD"                => array(2,"職種コード"),
                               "GUARD_WORK_NAME"            => array(60,"勤務先名称"),
                               "GUARD_WORK_TELNO"           => array(14,"勤務先電話番号"),
                               "GUARANTOR_RELATIONSHIP"     => array(2,"保証人続柄"),
                               "GUARANTOR_NAME"             => array(intval($this->nameSLen) * 3,"保証人氏名"),
                               "GUARANTOR_KANA"             => array(120,"保証人氏名かな"),
                               "GUARANTOR_SEX"              => array(1,"保証人性別"),
                               "GUARANTOR_ZIPCD"            => array(8,"保証人郵便番号"),
                               "GUARANTOR_ADDR1"            => array(array(intval($this->addrSLen) * 3, "保証人住所1")),
                               "GUARANTOR_ADDR2"            => array(array(intval($this->addrSLen) * 3, "保証人住所2")),
                               "GUARANTOR_TELNO"            => array(14,"保証人電話番号"),
                               "GUARANTOR_JOBCD"            => array(2,"保証人職種コード"),
                               "PUBLIC_OFFICE"              => array(30,"兼ねている公職"));

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
            $primaryKey1 = $this->getPrimaryKey('GUARDIAN_ADDRESS_DAT');
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

                    $data = array("SCHREGNO"                => (strlen(trim($tmp[0]))) ? sprintf("%08d", $tmp[0]) : "",
                                  "RELATIONSHIP"            => (strlen(trim($tmp[5]))) ? sprintf("%02d", $tmp[5]) : "",
                                  "GUARD_NAME"              => $tmp[6],
                                  "GUARD_KANA"              => $tmp[7],
                                  "GUARD_REAL_NAME"         => $tmp[8],
                                  "GUARD_REAL_KANA"         => $tmp[9],
                                  "GUARD_SEX"               => $tmp[10],
                                  "GUARD_BIRTHDAY"          => $tmp[11],
                                  "ISSUEDATE"               => $tmp[12],
                                  "EXPIREDATE"              => $tmp[13],
                                  "GUARD_ZIPCD"             => $tmp[14],
                                  "GUARD_ADDR1"             => $tmp[15],
                                  "GUARD_ADDR2"             => $tmp[16],
                                  "GUARD_ADDR_FLG"          => $tmp[17],
                                  "GUARD_TELNO"             => $tmp[18],
                                  "GUARD_TELNO2"            => $tmp[19],
                                  "GUARD_FAXNO"             => $tmp[20],
                                  "GUARD_E_MAIL"            => $tmp[21],
                                  "GUARD_JOBCD"             => (strlen(trim($tmp[22]))) ? sprintf("%02d", $tmp[22]) : "",
                                  "GUARD_WORK_NAME"         => $tmp[23],
                                  "GUARD_WORK_TELNO"        => $tmp[24],
                                  "GUARANTOR_RELATIONSHIP"  => (strlen(trim($tmp[25]))) ? sprintf("%02d", $tmp[25]) : "",
                                  "GUARANTOR_NAME"          => $tmp[26],
                                  "GUARANTOR_KANA"          => $tmp[27],
                                  "GUARANTOR_SEX"           => $tmp[28],
                                  "GUARANTOR_ZIPCD"         => $tmp[29],
                                  "GUARANTOR_ADDR1"         => $tmp[30],
                                  "GUARANTOR_ADDR2"         => $tmp[31],
                                  "GUARANTOR_TELNO"         => $tmp[32],
                                  "GUARANTOR_JOBCD"         => (strlen(trim($tmp[33]))) ? sprintf("%02d", $tmp[33]) : "",
                                  "PUBLIC_OFFICE"           => $tmp[34]);

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
        $masterErrFlg       = false;    //マスタエラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $orderErrFlg        = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $orderErrMsg        = "";

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";
        $orderSep        = "";
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $sonzaiarray = array("RELATIONSHIP"             => "H201",
                             "GUARD_SEX"                => "Z002",
                             "GUARD_JOBCD"              => "H202",
                             "GUARANTOR_RELATIONSHIP"   => "H201",
                             "GUARANTOR_SEX"            => "Z002",
                             "GUARANTOR_JOBCD"          => "H202");
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO","RELATIONSHIP","GUARD_NAME","GUARD_KANA","ISSUEDATE","EXPIREDATE"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                if (in_array($key, array("RELATIONSHIP","GUARD_SEX","GUARD_JOBCD","GUARANTOR_RELATIONSHIP","GUARANTOR_SEX","GUARANTOR_JOBCD"))) {
                    if ($data[$key] != "" && 1 > $db->getOne(knjx160aQuery::checkMstData($sonzaiarray[$key], $data[$key]))) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }

                //日付の書式チェック
                if (in_array($key, array("GUARD_BIRTHDAY","ISSUEDATE","EXPIREDATE"))) {
                    if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                    }
                }

                //有効期間日付の大小チェック
                if (in_array($key, array("ISSUEDATE"))) {
                    if ($this->dateRep($data["EXPIREDATE"]) != "" && $this->dateRep($data["ISSUEDATE"]) > $this->dateRep($data["EXPIREDATE"])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付の大小:", $numericSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("SCHREGNO","RELATIONSHIP","GUARD_JOBCD","GUARANTOR_JOBCD"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //１ OR NULL以外はエラー
                if (in_array($key, array("GUARD_ADDR_FLG"))) {
                    if ($data[$key] && $data[$key] != "1") {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                if ($this->Properties["chkNameInputHankaku"] == "1") {
                    if (in_array($key, array("GUARD_NAME", "GUARD_REAL_NAME", "GUARANTOR_NAME"), true)) {
                        if (!$this->chkHankaku($data[$key])) {
                            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, $val[1]);
                        }
                    }
                }
                if ($this->Properties["chkAddrInputHankaku"] == "1") {
                    if (in_array($key, array("GUARD_ADDR1", "GUARD_ADDR2", "GUARANTOR_ADDR1", "GUARANTOR_ADDR2"), true)) {
                        if (!$this->chkHankaku($data[$key])) {
                            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, $val[1]);
                        }
                    }
                }

                //文字数チェック
                if ($this->Properties["NAME_INPUT_SIZE"] != "" && in_array($key, array("GUARD_NAME", "GUARD_REAL_NAME", "GUARANTOR_NAME"), true)) {
                    $putMsgLen = $key == "GUARD_REAL_NAME" ? $this->nameSLen2 : $this->nameSLen;
                    if (mb_strlen($data[$key], 'UTF-8') > $putMsgLen) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && in_array($key, array("GUARD_ADDR1", "GUARD_ADDR2", "GUARANTOR_ADDR1", "GUARANTOR_ADDR2"), true)) {
                    if (mb_strlen($data[$key], 'UTF-8') > $this->addrSLen) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, $key."※サイズ:", $lengthSep, $val[1]);
                    }
                } else {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }

                //入力範囲チェック
                $checkUpd = ($this->dateRep($data["ISSUEDATE"]) && $this->dateRep($data["EXPIREDATE"])) ? $db->getOne(knjx160aQuery::getUpdData($data["SCHREGNO"], $this->dateRep($data["ISSUEDATE"]), $this->dateRep($data["EXPIREDATE"]))) : 0;
                if ($checkUpd == 0) {
                    $issuedate = $db->getone(knjx160aQuery::getAddrIssuedate($data["SCHREGNO"]));
                    if (in_array($key, array("ISSUEDATE"))) {
                        if ($issuedate != "" && $this->dateRep($data["ISSUEDATE"]) <= $issuedate) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※範囲外:", $numericSep, $val[1]);
                        }
                    }
                }
            } else {
                //数値チェック
                if (in_array($key, array("SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //日付の書式チェック
                if (in_array($key, array("ISSUEDATE"))) {
                    if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx160aQuery::insertQueryErr($db, $line, $checkError);
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
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array("SCHREGNO"                  => "※学籍番号",
                        "GRADE"                     => "学年",
                        "HR_CLASS"                  => "クラス",
                        "ATTENDNO"                  => "出席番号",
                        "NAME"                      => "氏名",
                        "RELATIONSHIP"              => "※続柄",
                        "GUARD_NAME"                => "※保護者氏名",
                        "GUARD_KANA"                => "※保護者氏名かな",
                        "GUARD_REAL_NAME"           => "戸籍保護者氏名",
                        "GUARD_REAL_KANA"           => "戸籍保護者氏名かな",
                        "GUARD_SEX"                 => "性別",
                        "GUARD_BIRTHDAY"            => "生年月日",
                        "ISSUEDATE"                 => "※有効期間開始日",
                        "EXPIREDATE"                => "※有効期間終了日",
                        "GUARD_ZIPCD"               => "郵便番号",
                        "GUARD_ADDR1"               => "住所1",
                        "GUARD_ADDR2"               => "住所2",
                        "GUARD_ADDR_FLG"            => "肩書き出力可",
                        "GUARD_TELNO"               => "電話番号",
                        "GUARD_TELNO2"              => "電話番号２",
                        "GUARD_FAXNO"               => "FAX番号",
                        "GUARD_E_MAIL"              => "E-MAIL",
                        "GUARD_JOBCD"               => "職種コード",
                        "GUARD_WORK_NAME"           => "勤務先名称",
                        "GUARD_WORK_TELNO"          => "勤務先電話番号",
                        "GUARANTOR_RELATIONSHIP"    => "保証人続柄",
                        "GUARANTOR_NAME"            => "保証人氏名",
                        "GUARANTOR_KANA"            => "保証人氏名かな",
                        "GUARANTOR_SEX"             => "保証人性別",
                        "GUARANTOR_ZIPCD"           => "保証人郵便番号",
                        "GUARANTOR_ADDR1"           => "保証人住所1",
                        "GUARANTOR_ADDR2"           => "保証人住所2",
                        "GUARANTOR_TELNO"           => "保証人電話番号",
                        "GUARANTOR_JOBCD"           => "保証人職種コード",
                        "PUBLIC_OFFICE"             => "兼ねている公職",
                        $this->lastColumn           => $this->lastColumn);

        $header_rei = array("SCHREGNO"                  => "12345678",
                            "GRADE"                     => "99",
                            "HR_CLASS"                  => "999",
                            "ATTENDNO"                  => "999",
                            "NAME"                      => "アルプ太郎",
                            "RELATIONSHIP"              => "99",
                            "GUARD_NAME"                => "全角20",
                            "GUARD_KANA"                => "全角40",
                            "GUARD_REAL_NAME"           => "全角40",
                            "GUARD_REAL_KANA"           => "全角80",
                            "GUARD_SEX"                 => "9",
                            "GUARD_BIRTHDAY"            => "9999-99-99",
                            "ISSUEDATE"                 => "9999-99-99",
                            "EXPIREDATE"                => "9999-99-99",
                            "GUARD_ZIPCD"               => "999-9999",
                            "GUARD_ADDR1"               => "全角50",
                            "GUARD_ADDR2"               => "全角50",
                            "GUARD_ADDR_FLG"            => "1 又は 無し",
                            "GUARD_TELNO"               => "9999-9999-9999",
                            "GUARD_TELNO2"              => "9999-9999-9999",
                            "GUARD_FAXNO"               => "9999-9999-9999",
                            "GUARD_E_MAIL"              => "aaa123@bbb.co.jp",
                            "GUARD_JOBCD"               => "99",
                            "GUARD_WORK_NAME"           => "全角20",
                            "GUARD_WORK_TELNO"          => "9999-9999-9999",
                            "GUARANTOR_RELATIONSHIP"    => "99",
                            "GUARANTOR_NAME"            => "全角20",
                            "GUARANTOR_KANA"            => "全角40",
                            "GUARANTOR_SEX"             => "9",
                            "GUARANTOR_ZIPCD"           => "999-9999",
                            "GUARANTOR_ADDR1"           => "全角50",
                            "GUARANTOR_ADDR2"           => "全角50",
                            "GUARANTOR_TELNO"           => "9999-9999-9999",
                            "GUARANTOR_JOBCD"           => "99",
                            "PUBLIC_OFFICE"             => "全角10",
                            $this->lastColumn           => $this->lastColumn);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $tables = array();
            $tables = array("1" => "GUARDIAN_ADDRESS_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents  = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx160aQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "保護者データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    public function chkHankaku($chkStr)
    {
        foreach (preg_split('//u', $chkStr, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 1) {
                return false;
            }
        }
        return true;
    }
}
//var_dump($_REQUEST);
