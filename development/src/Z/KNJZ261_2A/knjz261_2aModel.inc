<?php

require_once('for_php7.php');
class knjz261_2aModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;
    public $dutysharecd = "";
    public $lastColumn;//CSVの最後の列の値

    public function knjz261_2aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("DUTYSHARECD"      =>  VARS::post("DUTYSHARECD"),
                             "SHARENAME"        =>  VARS::post("SHARENAME"),
                             "BASE_SHARENAME"   =>  VARS::post("BASE_SHARENAME"),
                             "UPDATED"          =>  VARS::post("UPDATED"),
                             "OUTPUT"           =>  VARS::post("OUTPUT"),
                             "HEADER"           =>  VARS::post("HEADER")
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
        if (VARS::get("mode") == "1") {
            unset($this->dutysharecd);
        }
        $temp = VARS::get("DUTYSHARECD");
        if (isset($temp)) {
            $this->dutysharecd = VARS::get("DUTYSHARECD");
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //キーのチェック
        if (!strlen($this->field["DUTYSHARECD"]) && $this->cmd == "add") {
            $this->setWarning("MSG301", "(校務分掌部コード)");
            return false;
        }
        if (!strlen($this->dutysharecd) && $this->cmd == "update") {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->dutysharecd != $this->field["DUTYSHARECD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }

        //コードのゼロ埋め
        if (strlen($this->field["DUTYSHARECD"]) < 4) {
            $this->field["DUTYSHARECD"] = sprintf("%04d", $this->field["DUTYSHARECD"]);
        }

        //１レコード取得
        //学校DB
        $row = knjz261_2aQuery::getRow($this->field["DUTYSHARECD"]);
        //教育委員会DB
        $row_edboard = knjz261_2aQuery::getRow($this->field["DUTYSHARECD"], "edboard");

        //データの存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(校務分掌部コード)");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row_edboard)) {
            $this->setWarning("MSG918", "(教育委員会登録の校務分掌部コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["SHARENAME"]) == 0) {
            $this->setWarning("MSG301", "(校務分掌部名称)");
            return false;
        } elseif (strlen($this->field["SHARENAME"]) > 24) {
            $this->setWarning("MSG915", "(校務分掌部名称) 24バイトまで");
            return false;
        }
        if (strlen($this->field["BASE_SHARENAME"]) > 24) {
            $this->setWarning("MSG915", "(学校基本調査名称) 24バイトまで");
            return false;
        }

        return true;
    }


    //CSVファイル（ヘッダ出力 / 書出 / エラー出力）
    public function getDownloadCsvModel()
    {
        $data = false;
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ（エラー出力）
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

            $tables = array();
            $tables = array("1" => "DUTYSHARE_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $headerDuplicate = array();
            $headerDuplicate["DUTYSHARECD"] = "校務分掌部コード";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header) . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ（ヘッダ出力 / 書出）   フィールド => ヘッダ名, ヘッダ例
            $csvItem = array(
                             "DUTYSHARECD"      =>  array("※校務分掌部コード", "4バイト"),
                             "SHARENAME"        =>  array("※校務分掌部名称",   "24バイト"),
                             "BASE_SHARENAME"   =>  array("学校基本調査名称",   "24バイト"),
                             $this->lastColumn  =>  array($this->lastColumn,   $this->lastColumn)
                            );
            $contents = "";
            //ヘッダ・ヘッダ例取得
            $header = $header_rei = "";
            foreach ($csvItem as $key => $val) {
                $header     .= ($header == "") ? $val[0] : ",".$val[0];
                $header_rei .= ($header_rei == "") ? $val[1] : ",".$val[1];
            }
            //ヘッダセット
            if ($this->field["HEADER"]) {
                $contents .= $header."\n";
            }
            $query = knjz261_2aQuery::getCsvData($this);
        }

        if ($this->field["OUTPUT"] == "4") {
            //ヘッダ例セット
            $contents .= $header_rei."\n";
            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ・改行があれば置換
                $row = str_replace(",", ".", $row);
                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r", "", $row);
                $row = str_replace("\n", "", $row);

                if ($this->field["OUTPUT"] == "2") {
                    $cnt = 0;
                    foreach ($csvItem as $key => $val) {
                        $contents .= ($cnt == 0) ? $row[$key] : ",".$row[$key];
                        $cnt++;
                    }
                } else {
                    $contents .= implode($row, ",");
                }

                $contents .= "\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse = "校務分掌マスタエラー";
            } else {
                $couse = "校務分掌マスタ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CSVファイル（取込）
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
                knjz261_2aQuery::insertQueryCsv($db, $this, $this->data_arr);
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array(
                                 "DUTYSHARECD"       =>  array(4,       "※校務分掌部コード"),
                                 "SHARENAME"         =>  array(24,      "※校務分掌部名称"),
                                 "BASE_SHARENAME"    =>  array(24,      "学校基本調査名称")
                               );

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
            $primaryKey1 = $this->getPrimaryKey('DUTYSHARE_MST');
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
                                  "DUTYSHARECD"         => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]): $tmp[0],
                                  "SHARENAME"           => $tmp[1],
                                  "BASE_SHARENAME"      => $tmp[2]
                                  );

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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;  //重複エラーフラグ
        $duplicateDbErrFlg  = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $duplicateDbErrMsg  = "";

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";
        $duplicateDbSep     = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        //対象外チェック（重複）
        if ($data["DUTYSHARECD"] != "" && is_numeric($data["DUTYSHARECD"])) {
            $query = knjz261_2aQuery::checkDutyShareMst($data["DUTYSHARECD"]);
            $rowCount = $db->getOne($query);
            if ($rowCount > "0") {
                $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
            }
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            $arychkflg = false;
            if (in_array($key, array("DUTYSHARECD", "SHARENAME"))) {
                $arychkflg = true;
            }

            if ($arychkflg) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("DUTYSHARECD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz261_2aQuery::insertQueryErr($db, $line, $checkError);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }


    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->dutysharecd = $this->field["DUTYSHARECD"];
            $this->setMessage("MSG201");
        }
    }

    //更新
    public function &update()
    {
        knjz261_2aQuery::getUpdateQuery($this->field, $this->cmd);
        return;
    }

    //DELETE
    public function &getDeleteModel()
    {
        //キーの更新チェック
        if (!strlen($this->dutysharecd)) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return false;
        }
        if ($this->dutysharecd != $this->field["DUTYSHARECD"]) {
            $this->setWarning("MSG308", "キー値は変更できません");
            return;
        }
        $row = knjz261_2aQuery::getRow($this->dutysharecd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        if (knjz261_2aQuery::isExisting($this->dutysharecd)) {
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }

        //削除
        knjz261_2aQuery::getDeleteQuery($this->field);
        $this->dutysharecd = "";
        $this->setMessage("MSG201");
    }
}
