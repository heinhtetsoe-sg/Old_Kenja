<?php

require_once('for_php7.php');
class knjh720Model extends Model
{
    public $field = array();
    public $data = array();
    public $testcdArray = array();
    public $cmd;
    public $useSlumpHyouka;
    public $fieldsCalc = array();
    public $testcdMotoArray = array();
    public $schregno;
    public $attendno;
    public $name_show;
    public $dataArr;  //取込んだCSVデータ

    public function knjh720Model()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            $this->testcdMotoArray = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field["INPUT_NUM"]    = VARS::post("INPUT_NUM");     //入力回数
        $this->field["RADIO"]        = VARS::post("RADIO");         //CSV取込、書出
        $this->field["HEADER_CHECK"] = VARS::post("HEADER_CHECK");  //CSVヘッダ有り
        if (VARS::file("FILE")) {
            $this->field["FILE"]         = VARS::file("FILE");  //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
        $this->dataArr = null;
        $this->cmd = VARS::request("cmd");
        //学力テスト区分コード
        $this->field["TESTDIV"] = VARS::request("TESTDIV");
        //教科コード
        $this->field["CLASSCD"] = VARS::request("CLASSCD");
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        //教室コード
        $this->field["FACCD"] = VARS::request("FACCD");
        //行数
        $this->field["COUNTER"] = VARS::post("COUNTER");

        if ($this->cmd == "update") {
            $this->field["TESTDIV"]    = VARS::post("H_TESTDIV");
            $this->field["CLASSCD"]    = VARS::post("H_CLASSCD");
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
            $this->field["FACCD"]      = VARS::post("H_FACCD");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function getUpdateModel()
    {
        //初期化
        $this->fields = array();
        $this->testInsArray = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            //学籍番号を更新時に取得
            $this->fields["SCHREGNO"][] = VARS::post("SCHREGNO"."-".$i);
            $this->fields["SCORE1"][]   = VARS::post("SCORE1"."-".$i);
            $this->fields["ABSENCE1"][] = VARS::post("ABSENCE1"."-".$i);
            $this->fields["SCORE2"][]   = VARS::post("SCORE2"."-".$i);
            $this->fields["ABSENCE2"][] = VARS::post("ABSENCE2"."-".$i);
        }

        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            $score1   = $this->fields["SCORE1"][$i];
            $absence1 = $this->fields["ABSENCE1"][$i];
            $score2   = $this->fields["SCORE2"][$i];
            $absence2 = $this->fields["ABSENCE2"][$i];

            if (($score1 != "" && $absence1 == "1")
                || ($score2 != "" && $absence2 == "1")) {
                //素点と欠席のどちらも入力されている場合はエラー
                $this->setWarning("MSG307");
                return false;
            }
        }
        return true;
    }

    //成績データ更新処理
    public function update()
    {
        //DB接続
        $db = Query::dbCheckOut();

        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            $schno    = $this->fields["SCHREGNO"][$i];
            $score1   = $this->fields["SCORE1"][$i];
            $absence1 = $this->fields["ABSENCE1"][$i];
            $score2   = $this->fields["SCORE2"][$i];
            $absence2 = $this->fields["ABSENCE2"][$i];

            //存在チェック
            $query = knjh720Query::checkExistData($schno, $this);
            $flg   = ($db->getOne($query) > 0) ? "update" : "insert";
            //登録・更新
            knjh720Query::updInsRecordScore($flg, $this, $schno, $score1, $absence1, $score2, $absence2, $db);
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //CSVファイル取込、書出
    public function execCSV()
    {
        if ($this->field["RADIO"] == "1") {
            //取込
            $this->intputCSV();
            return true;
        } else {
            //書出
            if (!$this->outputCSV()) {
                return true;
            } else {
                return false;
            }
        }
    }

    //ＣＳＶ取込
    public function intputCSV()
    {
        $this->dataArr = array();
        $this->dataArr = $this->getFileString();
    }

    //CSVデータ読み込み
    public function getFileString()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);

        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            $this->errFlg = true;
            return false;
        }

        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array();
            $checkdata["YEAR"]       = array(4, "年度");
            $checkdata["TESTDIV"]    = array(1, "学力テストID");
            $checkdata["CLASSCD"]    = array(2, "教科コード");
            $checkdata["SUBCLASSCD"] = array(6, "科目コード");
            $checkdata["SCHREGNO"]   = array(8, "学籍番号");
            $checkdata["SCORE"]      = array(3, "素点");
            $checkdata["ABSENCE"]    = array(1, "欠席");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $tempArrRow = 0;
            $record_no  = 1;  //行番号(初期値)
            $primaryKey = array();
            $primaryKey = $this->getPrimaryKey('ACADEMICTEST_SCORE_DAT');
            $duplicateCsv = array();
            $fp = fopen($this->field["FILE"]["tmp_name"], "r"); //ファイルまたはURLをオープンする

            while (!feof($fp)) {                                //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                     //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER_CHECK"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");  //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    //CSVデータ1行取得
                    $tmpCnt = 0;
                    $data["YEAR"]       = trim($tmp[$tmpCnt++]);
                    $data["TESTDIV"]    = trim($tmp[$tmpCnt++]);
                    $data["CLASSCD"]    = trim($tmp[$tmpCnt++]);
                    $data["SUBCLASSCD"] = trim($tmp[$tmpCnt++]);
                    $data["SCHREGNO"]   = trim($tmp[$tmpCnt++]);
                    $tmpCnt++;
                    $data["SCORE"]      = trim($tmp[$tmpCnt++]);
                    $data["ABSENCE"]    = trim($tmp[$tmpCnt++]);

                    //０埋め
                    $data = $this->zeroUme($data);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $duplicateCsv, $primaryKey)) {
                        $tempArr[$tempArrRow] = $data;
                        $tempArrRow++;
                    } else {
                        $tempArr = null;
                        break;
                    }
                    foreach ($primaryKey as $key => $val) {
                        $duplicateCsv[$record_no][$val] = $data[$val];
                    }
                }//ifの綴り
                $record_no++; //行番号(カウント)
            }//whileの綴り
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
            $this->errFlg = true;
        }
        return $tempArr;
    }

    //主キー取得
    public function getPrimaryKey($tableName)
    {
        $db = Query::dbCheckOut();
        $query = knjh720Query::getPrimaryKeyData($tableName);
        $result = $db->query($query);
        $primaryKey = array();
        if (!PEAR::isError($result)) {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $primaryKey[] = $row["NAME"];
            }
        }
        return $primaryKey;
    }

    //重複チェック
    public function duplicateCsvCheck($duplicateCsv, $primaryKey, $data)
    {
        $dupFlg = 0;
        $dupLine = 0;
        foreach ($duplicateCsv as $dkey => $dval) {
            $dup = 1;
            foreach ($primaryKey as $pkey => $pval) {
                if ($dval[$pval] != $data[$pval]) {
                    $dup = 0;
                }
            }
            if ($dup == 1) {
                $dupFlg = 1;
                $dupLine = $dkey;
                break;
            }
        }
        return array($dupFlg, $dupLine);
    }

    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        if ($firstMsg === "※重複(CSV):" || $firstMsg === "※重複(登録済):") {
            $errMsg .= $sep.$val;
            $sep = "，";
        } else {
            $errMsg .= "[".$val."]";
        }
        return false;
    }

    public function checkErrLength($checkErr)
    {
        if (strlen($checkErr) > 120) {
            $checkErr = mb_strcut($checkErr, 0, 115);
            $checkErr .= "..他";
        }

        return $checkErr;
    }

    //０埋め
    public function zeroUme($data)
    {
        if ($data["SCHREGNO"] != "") {
            if (strlen($data["SCHREGNO"]) < 8 && is_numeric($data["SCHREGNO"])) {
                $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
            }
        }
        return $data;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $duplicateCsv, $primaryKey)
    {
        $returnFlg          = true;
        $importErrFlg       = false;    //取込先エラーフラグ
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $importErrMsg       = "";       //取込先エラーメッセージ
        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数値エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $importSep       = "";
        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";
        $otherSep        = "";

        //重複(CSV)チェック
        $dupFlg = $this->duplicateCsvCheck($duplicateCsv, $primaryKey, $data);
        if ($dupFlg[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg[1] ."行目)");
        }
        //素点と欠席のどちらも入力されている場合はエラー
        if ($data["SCORE"] != null && $data["ABSENCE"] != null) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※素点欠席:", $otherSep, "");
        }

        foreach ($checkdata as $key => $val) {
            //取込先チェック(年度と画面で選択しているコンボボックスの値が違う場合はエラー)
            if (in_array($key, array("YEAR", "TESTDIV", "CLASSCD", "SUBCLASSCD"))) {
                if ($key == "YEAR") {
                    if ($data[$key] != CTRL_YEAR) {
                        $returnFlg = $this->errSet($importErrFlg, $importErrMsg, "※取込先:", $importSep, $val[1]);
                    }
                } else {
                    if ($data[$key] != $this->field[$key]) {
                        $returnFlg = $this->errSet($importErrFlg, $importErrMsg, "※取込先:", $importSep, $val[1]);
                    }
                }
            }

            //必須チェック
            if (in_array($key, array("YEAR", "TESTDIV", "CLASSCD", "SUBCLASSCD", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "TESTDIV", "CLASSCD", "SUBCLASSCD", "SCHREGNO", "SCORE", "ABSENCE"))
                && $data[$key] != "" && !is_numeric($data[$key])
            ) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
            }

            //値チェック
            if (in_array($key, array("SCORE", "ABSENCE"))) {
                if ($data[$key] != "" && !$this->numValueCheck($key, $data[$key])) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }

            //文字数チェック
            if ($data[$key] != "" && strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if ($returnFlg) {
            //存在チェック
            if ($data["YEAR"] != "" && $data["SCHREGNO"] != "") {
                $query = knjh720Query::getMasterCheck($data);
                if ($db->getOne($query) < 1) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データ");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$importErrMsg.$hissuErrMsg.$numericErrMsg.$valuesErrMsg.$masterErrMsg.$lengthErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            //エラーメッセージを表示する場合はここで$this->setMessage($checkError);
            $this->errFlg = true;
        }
        return $returnFlg;
    }

    //素点、欠席の値チェック
    public function numValueCheck($key, $data)
    {
        $flg = true;

        if ($key == "SCORE"
            && ($data < 0 || 100 < $data)) {
            $flg = false;
        } else if ($key == "ABSENCE"
                   && ($data != "1" && $data != "")) {
            $flg = false;
        }
        return $flg;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ書出
    public function outputCSV()
    {
        //ヘッダー
        $header["YEAR"]       = "※年度";
        $header["TESTDIV"]    = "※学力テストID";
        $header["CLASSCD"]    = "※教科コード";
        $header["SUBCLASSCD"] = "※科目コード";
        $header["SCHREGNO"]   = "※学籍番号";
        $header["NAME"]       = "生徒氏名";
        $header["SCORE"]      = "素点";
        $header["ABSENCE"]    = "欠席";
        $header["LASTCOLUMN"] = "LASTCOLUMN";

        $contents  = ""; //クリア

        if ($this->field["HEADER_CHECK"] == "on") {
            $contents .= implode($header, ",")."\n";
        }
        $header_data = false;
        //DB接続
        $db = Query::dbCheckOut();
        $query = knjh720Query::getScoreCSV($this, $this->field["INPUT_NUM"]);

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$row[$key];
                $sep = ",";
            }
            $contents .= "\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = "学力テスト素点.csv";
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
