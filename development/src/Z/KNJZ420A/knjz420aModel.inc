<?php

require_once('for_php7.php');
//定数定義
class knjz420aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $company_cd;
    public $lastColumn;//CSVの最後の列の値

    //メンバ変数の初期化
    public function knjz420aModel()
    {
    }
    public function init()
    {
        $this->field = array("COMPANY_CD"         =>  VARS::post("COMPANY_CD"),
                             "COMPANY_NAME"       =>  VARS::post("COMPANY_NAME"),
                             "SHUSHOKU_ADDR"      =>  VARS::post("SHUSHOKU_ADDR"),
                             "SHIHONKIN"          =>  VARS::post("SHIHONKIN"),
                             "SONINZU"            =>  VARS::post("SONINZU"),
                             "TONINZU"            =>  VARS::post("TONINZU"),
                             "INDUSTRY_LCD"       =>  VARS::post("INDUSTRY_LCD"),
                             "INDUSTRY_MCD"       =>  VARS::post("INDUSTRY_MCD"),
                             "COMPANY_SORT"       =>  VARS::post("COMPANY_SORT"),
                             "TARGET_SEX"         =>  VARS::post("TARGET_SEX"),
                             "ZIPCD"              =>  VARS::post("ZIPCD"),
                             "ADDR1"              =>  VARS::post("ADDR1"),
                             "ADDR2"              =>  VARS::post("ADDR2"),
                             "TELNO"              =>  VARS::post("TELNO"),
                             "REMARK"             =>  VARS::post("REMARK"),
                             "OUTPUT"             =>  VARS::post("OUTPUT"),
                             "HEADER"             =>  VARS::post("HEADER")
                             );

        $this->cmd             = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        if (VARS::get("PROGRAMID")) {
            $this->company_cd = "";
        }
        if (VARS::get("COMPANY_CD")!="") {
            $this->company_cd    = VARS::get("COMPANY_CD");
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    //入力項目チェック
    public function validateInput($kind)
    {
        if (trim($this->field["COMPANY_CD"]) == "") {
            $this->setWarning("MSG301", "(会社コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->company_cd=="") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["COMPANY_CD"] !=$this->company_cd) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["COMPANY_CD"] = sprintf("%08d", $this->field["COMPANY_CD"]);
            //１レコード取得
            $row = knjz420aQuery::getRow($this->field["COMPANY_CD"]);

            //存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            //コードのゼロ埋め
            $this->field["COMPANY_CD"] = sprintf("%08d", $this->field["COMPANY_CD"]);
            //１レコード取得
            $row = knjz420aQuery::getRow($this->field["COMPANY_CD"]);

            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918", "(会社コード)");
                return false;
            }
            if (strlen($this->field["COMPANY_NAME"]) > 120) {
                $this->setWarning("MSG915", "(会社名は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SHUSHOKU_ADDR"]) > 120) {
                $this->setWarning("MSG915", "(就業場所は120バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SHIHONKIN"]) > 17) {
                $this->setWarning("MSG915", "(資本金は17バイトまでです。)");
                return false;
            } elseif (strlen($this->field["SONINZU"]) > 8) {
                $this->setWarning("MSG915", "(全体人数は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TONINZU"]) > 8) {
                $this->setWarning("MSG915", "(当事務所人数は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ZIPCD"]) > 8) {
                $this->setWarning("MSG915", "(郵便番号は8バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ADDR1"]) > 90) {
                $this->setWarning("MSG915", "(住所１は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ADDR2"]) > 90) {
                $this->setWarning("MSG915", "(住所２は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["TELNO"]) > 16) {
                $this->setWarning("MSG915", "(電話番号は16バイトまでです。)");
                return false;
            } elseif (strlen($this->field["REMARK"]) > 120) {
                $this->setWarning("MSG915", "(備考は120バイトまでです。)");
                return false;
            }
        }
        return true;
    }

    //CSVファイル（ヘッダ出力 / 書出 / エラー出力）
    public function getDownloadCsvModel()
    {
        $data = false;
        if ($this->field["OUTPUT"] == "3") {
            $duplHeader = array("COMPANY_CD"  =>  "会社コード");
            //ヘッダ（エラー出力）
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $tables = array();
            $tables = array("1" => "COMPANY_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",")."\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ（ヘッダ出力 / 書出）   フィールド => ヘッダ名, ヘッダ例
            $csvItem = array(
                             "COMPANY_CD"      =>  array("※会社コード",   "8バイト"),
                             "COMPANY_NAME"    =>  array("会社名",         "120バイト"),
                             "SHUSHOKU_ADDR"   =>  array("就業場所",       "120バイト"),
                             "SHIHONKIN"       =>  array("資本金",         "17バイト"),
                             "SONINZU"         =>  array("全体人数",       "8バイト"),
                             "TONINZU"         =>  array("当事務所人数",    "8バイト"),
                             "INDUSTRY_LCD"    =>  array("産業大分類",      "1バイト"),
                             "INDUSTRY_MCD"    =>  array("産業中分類",      "2バイト"),
                             "COMPANY_SORT"    =>  array("職種コード",      "2バイト"),
                             "TARGET_SEX"      =>  array("募集対象",        "1バイト"),
                             "ZIPCD"           =>  array("郵便番号",        "8バイト"),
                             "ADDR1"           =>  array("住所１",          "90バイト"),
                             "ADDR2"           =>  array("住所２",          "90バイト"),
                             "TELNO"           =>  array("電話番号",        "16バイト"),
                             "REMARK"          =>  array("備考",            "120バイト"),
                             $this->lastColumn =>  array($this->lastColumn, $this->lastColumn)
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
            $query = knjz420aQuery::getCsvData($this);
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
                $couse = "会社マスタエラー";
            } else {
                $couse = "会社マスタ";
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
                knjz420aQuery::insertQueryCsv($db, $this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                Query::dbCheckIn($db);
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
            $checkdata = array("COMPANY_CD"    =>  array(8   ,   "※会社コード"),
                               "COMPANY_NAME"  =>  array(120 ,   "会社名"),
                               "SHUSHOKU_ADDR" =>  array(120 ,   "就業場所"),
                               "SHIHONKIN"     =>  array(17  ,   "資本金"),
                               "SONINZU"       =>  array(8   ,   "全体人数"),
                               "TONINZU"       =>  array(8   ,   "当事務所人数"),
                               "INDUSTRY_LCD"  =>  array(1   ,   "産業大分類"),
                               "INDUSTRY_MCD"  =>  array(2   ,   "産業中分類"),
                               "COMPANY_SORT"  =>  array(2   ,   "職種コード"),
                               "TARGET_SEX"    =>  array(1   ,   "募集対象"),
                               "ZIPCD"         =>  array(8   ,   "郵便番号"),
                               "ADDR1"         =>  array(90  ,   "住所１"),
                               "ADDR2"         =>  array(90  ,   "住所２"),
                               "TELNO"         =>  array(16  ,   "電話番号"),
                               "REMARK"        =>  array(120 ,   "備考")
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
            $primaryKey1 = $this->getPrimaryKey('COMPANY_MST');
            $duplicateCsv1 = array();
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
                    $data = array("COMPANY_CD"      => (strlen(trim($tmp[0]))) ? sprintf("%08d", $tmp[0]): $tmp[0],
                                  "COMPANY_NAME"    => $tmp[1],
                                  "SHUSHOKU_ADDR"   => $tmp[2],
                                  "SHIHONKIN"       => $tmp[3],
                                  "SONINZU"         => $tmp[4],
                                  "TONINZU"         => $tmp[5],
                                  "INDUSTRY_LCD"    => $tmp[6],
                                  "INDUSTRY_MCD"    => (strlen(trim($tmp[7]))) ? sprintf("%02d", $tmp[7]): $tmp[7],
                                  "COMPANY_SORT"    => (strlen(trim($tmp[8]))) ? sprintf("%02d", $tmp[8]): $tmp[8],
                                  "TARGET_SEX"      => $tmp[9],
                                  "ZIPCD"           => $tmp[10],
                                  "ADDR1"           => $tmp[11],
                                  "ADDR2"           => $tmp[12],
                                  "TELNO"           => $tmp[13],
                                  "REMARK"          => $tmp[14],
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
        $returnFlg = true;
        $hissuErrFlg         = false;    //必須エラーフラグ
        $numericErrFlg       = false;    //数字エラーフラグ
        $lengthErrFlg        = false;    //レングスエラーフラグ
        $masterErrFlg        = false;    //存在エラーフラグ
        $duplicateCsvErrFlg  = false;    //重複エラーフラグ
        $duplicateDbErrFlg   = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $$masterErrMsg      = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $duplicateDbErrMsg  = "";

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $masterSep          = "";
        $duplicateCsvSep    = "";
        $duplicateDbSep     = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $checkNameMstCd = array("COMPANY_SORT"      => "E008",
                                "TARGET_SEX"        => "E007",
                                );
        //対象外チェック（重複）
        if ($data["COMPANY_CD"] != "" && is_numeric($data["COMPANY_CD"])) {
            $query = knjz420aQuery::checkCompanyMst($data["COMPANY_CD"]);
            $rowCount = $db->getOne($query);
            if ($rowCount > "0") {
                $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
            }
        }
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            $arychkflg = false;
            if (in_array($key, array("COMPANY_CD"))) {
                $arychkflg = true;
            }

            if ($arychkflg) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("COMPANY_CD", "SONINZU", "TONINZU", "INDUSTRY_MCD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //存在チェック（名称マスタ）
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjz420aQuery::checkNameMst($checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]."(".$checkNameMstCd[$key].")");
                }
            }
            //存在チェック（産業分類マスタ）
            if (in_array($key, array("INDUSTRY_LCD", "INDUSTRY_MCD")) && $data[$key] != "") {
                $query = knjz420aQuery::checkIndustoryTypeCd($key, $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz420aQuery::insertQueryErr($db, $line, $checkError);
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

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->company_cd = $this->field["COMPANY_CD"];
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }
    public function &delete()
    {
        if (!isset($this->company_cd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz420aQuery::getDeleteQuery($this->field);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    public function &insert()
    {
        $result = knjz420aQuery::getInsertQuery($this->field);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjz420aQuery::getUpdateQuery($this->field);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //変数をクリアします
    public function clean()
    {
    }
}
