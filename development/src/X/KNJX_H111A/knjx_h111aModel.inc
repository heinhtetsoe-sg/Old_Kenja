<?php
class knjx_h111aModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $cntNotPrintRank;
    public $cntNotPrintResult;
    public $showScore;
    public $lastColumn;//CSVの最後の列の値

    public function knjx_h111aModel()
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
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "STUDENT"          =>  VARS::post("STUDENT"),          //生徒一覧
                             "HEADER"           =>  VARS::post("HEADER")            //ヘッダ有
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        //NOT_PRINTカラムが存在するかどうかを取得
        $db = Query::dbCheckOut();
        $this->cntNotPrintRank = $db->getOne(knjx_h111aQuery::getSyscatColumns("QUALIFIED_RANK_DAT"));
        $this->cntNotPrintResult = $db->getOne(knjx_h111aQuery::getSyscatColumns("QUALIFIED_RESULT_MST"));
        Query::dbCheckIn($db);

        if ($this->Properties["useQualifiedManagementFlg"] == '1') {
            if ($this->cntNotPrintResult > 0) {
                $this->showScore = 1;
            }
        } else {
            if ($this->cntNotPrintRank > 0) {
                $this->showScore = 1;
            }
        }

        //得点の入力可能桁数
        $this->scoreLen = 3;
        if ($this->Properties["useQualifiedScoreLen"] > 0) {
            $this->scoreLen = $this->Properties["useQualifiedScoreLen"];
        }

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
                if ($this->field["SHORI_MEI"] == "3") {
                    knjx_h111aQuery::deleteQueryCsv($this, $this->data_arr);
                } else {
                    knjx_h111aQuery::updateQueryCsv($this, $this->data_arr);
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
            $checkdata = array("YEAR"               => array(4, "年度"),
                               "SCHREGNO"           => array(8, "学籍番号"),
                               "REGDDATE"           => array(10,"取得日付"),
                               "SEQ"                => array(2, "連番"),
                               "CONDITION_DIV"      => array(1, "設定区分"),
                               "QUALIFIED_CD"       => array(4, "検定・資格コード"),
                               "RANK"               => array(($this->Properties["useQualifiedManagementFlg"] == '1') ? 4 : 3, "級・段位区分"),
                               "HOBBY_SCORE"        => array($this->scoreLen, "得点"),
                               "REMARK"             => array(90,"備考"));

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
            if ($this->Properties["useQualifiedManagementFlg"] == '1') {
                $primaryKey1   = $this->getPrimaryKey('SCHREG_QUALIFIED_TEST_DAT');
            } else {
                $primaryKey1   = $this->getPrimaryKey('SCHREG_QUALIFIED_HOBBY_DAT');
            }
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

                    if ($this->showScore == 1) {
                        //資格点あり
                        $data = array("YEAR"                => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0])  : "",
                                      "SCHREGNO"            => (strlen(trim($tmp[4])))  ? sprintf("%08d", $tmp[4])  : "",
                                      "REGDDATE"            => $tmp[7],
                                      "SEQ"                 => $tmp[8],
                                      "CONDITION_DIV"       => $tmp[9],
                                      "QUALIFIED_CD"        => (strlen(trim($tmp[11])))  ? sprintf("%04d", $tmp[11])  : "",
                                      "RANK"                => $tmp[13],
                                      "HOBBY_SCORE"         => $tmp[16],
                                      "CERTIFICATE"         => $tmp[18],
                                      "REMARK"              => $tmp[19]);
                    } else {
                        //資格点なし
                        $data = array("YEAR"                => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0])  : "",
                                      "SCHREGNO"            => (strlen(trim($tmp[4])))  ? sprintf("%08d", $tmp[4])  : "",
                                      "REGDDATE"            => $tmp[7],
                                      "SEQ"                 => $tmp[8],
                                      "CONDITION_DIV"       => $tmp[9],
                                      "QUALIFIED_CD"        => (strlen(trim($tmp[11])))  ? sprintf("%04d", $tmp[11])  : "",
                                      "RANK"                => $tmp[13],
                                      "HOBBY_SCORE"         => $tmp[15],
                                      "CERTIFICATE"         => $tmp[17],
                                      "REMARK"              => $tmp[18]);
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
        $hissuErrFlg        = false;       //必須エラーフラグ
        $masterErrFlg       = false;       //マスタエラーフラグ
        $numericErrFlg      = false;       //数字エラーフラグ
        $lengthErrFlg       = false;       //レングスエラーフラグ
        $dateFormatErrFlg   = false;       //日付(フォーマット)エラーフラグ
        $valuesErrFlg       = false;       //値エラーフラグ
        $duplicateCsvErrFlg = false;       //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $dateFormatErrMsg   = "";       //日付(フォーマット)エラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $dateFormatSep   = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SCHREGNO","REGDDATE","QUALIFIED_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if ($this->field["SHORI_MEI"] != "1" && $key == "SEQ" && !strlen(trim($data[$key])) && $this->Properties["useQualifiedManagementFlg"] != '1') {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
            }
            if ($key == "RANK" && !strlen(trim($data[$key])) && $this->Properties["useQualifiedManagementFlg"] == '1') {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
            }
            //数値チェック
            if (in_array($key, array("YEAR","SCHREGNO","SEQ","CONDITION_DIV","QUALIFIED_CD","RANK","HOBBY_SCORE"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //日付チェック
            if (in_array($key, array("REGDDATE"))) {
                if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                    $returnFlg = $this->errSet($dateFormatErrFlg, $dateFormatErrMsg, "※日付(フォーマット):", $dateFormatSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //存在チェック
            if ($this->Properties["useQualifiedManagementFlg"] == '1') {
                if (in_array($key, array("SCHREGNO","CONDITION_DIV","QUALIFIED_CD","RANK"))) {
                    $query = "";
                    if ($key == "SCHREGNO") {
                        $query = knjx_h111aQuery::checkSchregRegdDat($data["YEAR"], $data[$key]);
                    }
                    if ($key == "QUALIFIED_CD") {
                        $query = knjx_h111aQuery::checkQualifiedMst($data[$key]);
                    }
                    if ($key == "RANK") {
                        $query = knjx_h111aQuery::checkQualifiedResultMst($data["YEAR"], $data["QUALIFIED_CD"], $data[$key]);
                    }

                    if ($query) {
                        if ($data[$key] != "" && 0 == $db->getOne($query)) {
                            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                        }
                    } else {
                        if ($data[$key] != "" && !in_array($data[$key], array("1","2","3"))) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                }
            } else {
                if (in_array($key, array("SCHREGNO","SEQ","CONDITION_DIV","QUALIFIED_CD","RANK"))) {
                    $query = "";
                    if ($key == "SCHREGNO") {
                        $query = knjx_h111aQuery::checkSchregRegdDat($data["YEAR"], $data[$key]);
                    }
                    if ($key == "SEQ" && $data[$key] != "" && is_numeric($data[$key])) {
                        $query = knjx_h111aQuery::checkSEQ($data["YEAR"], $data["SCHREGNO"], $data[$key]);
                    }
                    if ($key == "QUALIFIED_CD") {
                        $query = knjx_h111aQuery::checkQualifiedMst($data[$key]);
                    }
                    if ($key == "RANK") {
                        $query = knjx_h111aQuery::checkNameMst('H312', $data[$key]);
                    }
                    if ($query) {
                        if (!($this->field["SHORI_MEI"] == "1" && $key == "SEQ")) {
                            if ($data[$key] != "" && 0 == $db->getOne($query)) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                            }
                        }
                    } else {
                        if ($data[$key] != "" && !in_array($data[$key], array("1","2","3"))) {
                            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                        }
                    }
                }
            }
        }

        // SCHREG_QUALIFIED_TEST_DATの存在チェック
        if ($this->Properties["useQualifiedManagementFlg"] == '1') {
            $data["REGDDATE"] = $this->dateRep($data["REGDDATE"]);
            $query = knjx_h111aQuery::checkSchregQualifiedTestDatMst($data["YEAR"], $data["SCHREGNO"], $data["REGDDATE"], $data["QUALIFIED_CD"], $data["RANK"]);
            if ($this->field["SHORI_MEI"] == "1" && 0 < $db->getOne($query)) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍学校管理資格データが存在します。");
            } elseif ($this->field["SHORI_MEI"] == "2" && 0 == $db->getOne($query)) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック", $masterSep, "学籍学校管理資格データがありません");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg.$dateFormatErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_h111aQuery::insertQueryErr($db, $line, $checkError);
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
        if (count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
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
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header_rei_score = "";
        for ($i = 1; $i <= $this->scoreLen; $i++) {
            $header_rei_score .= $i;
        }

        if ($this->showScore == 1) {
            //資格点あり

            //ヘッダデータ
            $header = array("YEAR"                  => "※年度",
                            "GRADE"                 => "年",
                            "HR_CLASS"              => "組",
                            "ATTENDNO"              => "番",
                            "SCHREGNO"              => "※学籍番号",
                            "NAME"                  => "氏名",
                            "NAME_KANA"             => "氏名ふりがな",
                            "REGDDATE"              => "※取得日付",
                            "SEQ"                   => ($this->Properties["useQualifiedManagementFlg"] == '1') ? "連番" : "※連番",
                            "CONDITION_DIV"         => "設定区分",
                            "CONDITION_DIV_NAME"    => "設定名称",
                            "QUALIFIED_CD"          => "※検定・資格コード",
                            "QUALIFIED_NAME"        => "検定・資格名称",
                            "RANK"                  => ($this->Properties["useQualifiedManagementFlg"] == '1') ? "級・段位区分" : "※級・段位区分",
                            "RANK_NAME"             => "級・段位区分名称",
                            "SCORE "                => "資格点",
                            "HOBBY_SCORE"           => "得点",
                            "PROMOTER"              => "主催団体",
                            "CERTIFICATE"           => "資格証書",
                            "REMARK"                => "備考",
                            $this->lastColumn       => $this->lastColumn);

            //ヘッダ例
            $header_rei = array("YEAR"                  => CTRL_YEAR,
                                "GRADE"                 => "01",
                                "HR_CLASS"              => "001",
                                "ATTENDNO"              => "001",
                                "SCHREGNO"              => "12345678",
                                "NAME"                  => "アルプ太郎",
                                "NAME_KANA"             => "あるぷたろう",
                                "REGDDATE"              => CTRL_DATE,
                                "SEQ"                   => "追加時は自動付番",
                                "CONDITION_DIV"         => "3",
                                "CONDITION_DIV_NAME"    => "公的資格",
                                "QUALIFIED_CD"          => "1234",
                                "QUALIFIED_NAME"        => "英検",
                                "RANK"                  => "001",
                                "RANK_NAME"             => "1級",
                                "SCORE "                => $header_rei_score,
                                "HOBBY_SCORE"           => $header_rei_score,
                                "PROMOTER"              => "日本英語検定協会",
                                "CERTIFICATE"           => "1",
                                "REMARK"                => "90バイトまで",
                                $this->lastColumn       => $this->lastColumn);
        } else {
            //資格点なし

            //ヘッダデータ
            $header = array("YEAR"                  => "※年度",
                            "GRADE"                 => "年",
                            "HR_CLASS"              => "組",
                            "ATTENDNO"              => "番",
                            "SCHREGNO"              => "※学籍番号",
                            "NAME"                  => "氏名",
                            "NAME_KANA"             => "氏名ふりがな",
                            "REGDDATE"              => "※取得日付",
                            "SEQ"                   => ($this->Properties["useQualifiedManagementFlg"] == '1') ? "連番" : "※連番",
                            "CONDITION_DIV"         => "設定区分",
                            "CONDITION_DIV_NAME"    => "設定名称",
                            "QUALIFIED_CD"          => "※検定・資格コード",
                            "QUALIFIED_NAME"        => "検定・資格名称",
                            "RANK"                  => ($this->Properties["useQualifiedManagementFlg"] == '1') ? "級・段位区分" : "※級・段位区分",
                            "RANK_NAME"             => "級・段位区分名称",
                            "HOBBY_SCORE"           => "得点",
                            "PROMOTER"              => "主催団体",
                            "CERTIFICATE"           => "資格証書",
                            "REMARK"                => "備考",
                            $this->lastColumn       => $this->lastColumn);

            //ヘッダ例
            $header_rei = array("YEAR"                  => CTRL_YEAR,
                                "GRADE"                 => "01",
                                "HR_CLASS"              => "001",
                                "ATTENDNO"              => "001",
                                "SCHREGNO"              => "12345678",
                                "NAME"                  => "アルプ太郎",
                                "NAME_KANA"             => "あるぷたろう",
                                "REGDDATE"              => CTRL_DATE,
                                "SEQ"                   => "追加時は自動付番",
                                "CONDITION_DIV"         => "3",
                                "CONDITION_DIV_NAME"    => "公的資格",
                                "QUALIFIED_CD"          => "1234",
                                "QUALIFIED_NAME"        => "英検",
                                "RANK"                  => "001",
                                "RANK_NAME"             => "1級",
                                "HOBBY_SCORE"           => $header_rei_score,
                                "PROMOTER"              => "日本英語検定協会",
                                "CERTIFICATE"           => "1",
                                "REMARK"                => "90バイトまで",
                                $this->lastColumn       => $this->lastColumn);
        }

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
            if ($this->Properties["useQualifiedManagementFlg"] == '1') {
                $tables = array("1" => "SCHREG_QUALIFIED_TEST_DAT");
            } else {
                $tables = array("2" => "SCHREG_QUALIFIED_HOBBY_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $headerDupl = array();
            $headerDupl["YEAR"]         = "年度";
            $headerDupl["SCHREGNO"]     = "学籍番号";
            $headerDupl["TEST_DATE"]    = "試験日";
            $headerDupl["QUALIFIED_CD"] = "検定・資格コード";
            $headerDupl["TEST_CD"]      = "受験級";
            $headerDupl["SEQ"]          = "SEQ（連番)";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDupl);
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
                $query = knjx_h111aQuery::selectMainQuery($this);
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
                $couse = "資格取得データ";
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
