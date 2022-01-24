<?php

require_once('for_php7.php');
    //定数定義
//配列内の数値のみ取得
function num_filter($var)
{
    return (is_numeric($var));
}

class knje410Model extends Model
{
    public $cmd;
    public $gc_select;                     //セレクトメニュー
    public $control_data = array();        //コントロールのデータを保持
    public $staffcd;                       //セキュリティーチェック
    public $sec_competence;                //権限保持
    public $updated = array();             //UPDATED値保持
    public $field = array();
    public $fields = array();
    public $schoolcd;  //学校コード
    public $lastColumn;//CSVの最後の列の値

    public function knje410Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJE410");

        $this->field = array("YEAR"             => VARS::post("YEAR"),
                             "DOC_NUMBER"       => VARS::post("DOC_NUMBER"),
                             "EXECUTE_DATE"     => VARS::post("EXECUTE_DATE"),
                             "REPORT"           => VARS::post("REPORT"),
                             //CSV
                             "OUTPUT"           => VARS::post("OUTPUT"),
                             "HEADER"           => VARS::post("HEADER")
                            );

        $temp = array("SHINGAKU_DAIGAKU"        => VARS::post("SHINGAKU_DAIGAKU"),
                      "SHINGAKU_TANDAI"         => VARS::post("SHINGAKU_TANDAI"),
                      "SHINGAKU_SENMON"         => VARS::post("SHINGAKU_SENMON"),
                      "SHINGAKU_SONOTA"         => VARS::post("SHINGAKU_SONOTA"),
                      "SHINGAKU_REMARK"         => VARS::post("SHINGAKU_REMARK"),
                      "SHINGAKU_GOUKEI"         => VARS::post("SHINGAKU_GOUKEI"),
                      "SHUSHOKU_KENNAI"         => VARS::post("SHUSHOKU_KENNAI"),
                      "SHUSHOKU_KENNAI_KOUMUIN" => VARS::post("SHUSHOKU_KENNAI_KOUMUIN"),
                      "SHUSHOKU_KENGAI"         => VARS::post("SHUSHOKU_KENGAI"),
                      "SHUSHOKU_KENGAI_KOUMUIN" => VARS::post("SHUSHOKU_KENGAI_KOUMUIN"),
                      "SHUSHOKU_SONOTA"         => VARS::post("SHUSHOKU_SONOTA"),
                      "SHUSHOKU_REMARK"         => VARS::post("SHUSHOKU_REMARK"),
                      "SHUSHOKU_GOUKEI"         => VARS::post("SHUSHOKU_GOUKEI"),
                      "SONOTA_GOUKEI"           => VARS::post("SONOTA_GOUKEI"),
                      "ZENTAI_GOUKEI"           => VARS::post("ZENTAI_GOUKEI")
                      );
        $this->fields = array_merge($this->fields, $temp);

        //CSVファイル
        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        knje410Query::getUpdateQuery($this);
        return;
    }

    //県への報告
    public function &getUpdateEdboardModel()
    {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knje410Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //*******CVSファイル*************/
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

            $headerDuplicate = array("EDBOARD_SCHOOLCD" => "教育委員会用学校コード",
                                     "YEAR"             => "年度",
                                     "COURSECD"         => "学科コード",
                                     "MAJORCD"          => "課程コード",
                                     "LARGE_DIV"        => "大区分",
                                     "MIDDLE_DIV"       => "中区分",
                                     "SMALL_DIV"        => "小区分",
                                     "SEX"              => "性別コード");
            $tables = array();
            $tables = array("1" => "AFT_DISEASE_ADDITION410_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header) . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } else {
            //ヘッダ、書出
            $header1 = array("",
                            "",
                            "",
                            "進学希望者",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "就職希望者",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            $this->lastColumn);

            $header2 = array("学科コード",
                            "課程コード",
                            "性別コード",
                            "大学",
                            "短大",
                            "専門学校",
                            "その他",
                            "　",
                            "合計",
                            "県内",
                            "県内公務員",
                            "県外",
                            "県外公務員",
                            "その他",
                            "　",
                            "合計",
                            "その他",
                            "総合計",
                            $this->lastColumn);

            if ($this->field["HEADER"]) {
                $contents  = implode(",", $header1) . "\n";
                $contents .= implode(",", $header2) . "\n";
            } else {
                $contents = "";
            }
            $query = knje410Query::readQuery($this, "csv");
        }
        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //配列の値を,で連結
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);
        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="進路希望エラー";
            } else {
                $couse="進路希望一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //クライアント ⇒ サーバ側の処理
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
                $db->autoCommit(false);
                //Insertまたはupdate
                knje410Query::insertQueryCsv($db, $this, $this->data_arr);
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("COURSECD"                => array(1  ,"学科コード" ),
                               "MAJORCD"                 => array(3  ,"課程コード" ),
                               "SEX"                     => array(1  ,"性別コード" ),
                               "SHINGAKU_DAIGAKU"        => array(5  ,"大学人数(進学希望者)" ),
                               "SHINGAKU_TANDAI"         => array(5  ,"短大人数(進学希望者)" ),
                               "SHINGAKU_SENMON"         => array(5  ,"専門人数(進学希望者)" ),
                               "SHINGAKU_SONOTA"         => array(5  ,"その他人数(進学希望者)" ),
                               "SHINGAKU_REMARK"         => array(5  ,"人数(進学希望者)" ),
                               "SHINGAKU_GOUKEI"         => array(5  ,"合計人数(進学希望者)" ),
                               "SHUSHOKU_KENNAI"         => array(5  ,"県内人数(就職希望者)" ),
                               "SHUSHOKU_KENNAI_KOUMUIN" => array(5  ,"県内公務員人数(就職希望者)" ),
                               "SHUSHOKU_KENGAI"         => array(5  ,"県外人数(就職希望者)" ),
                               "SHUSHOKU_KENGAI_KOUMUIN" => array(5  ,"県外公務員人数(就職希望者)" ),
                               "SHUSHOKU_SONOTA"         => array(5  ,"その他人数(就職希望者)" ),
                               "SHUSHOKU_REMARK"         => array(5  ,"人数(就職希望者)" ),
                               "SHUSHOKU_GOUKEI"         => array(5  ,"合計人数(就職希望者)" ),
                               "SONOTA_GOUKEI"           => array(5  ,"その他人数" ),
                               "ZENTAI_GOUKEI"           => array(5  ,"総合計人数" )
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey("AFT_DISEASE_ADDITION410_DAT");
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        if ($line == 2) {//headerが2行分
                            $header = true;
                        }
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array("COURSECD"                => trim($tmp[0]),
                                  "MAJORCD"                 => trim($tmp[1]),
                                  "SEX"                     => trim($tmp[2]),
                                  "SHINGAKU_DAIGAKU"        => trim($tmp[3]),
                                  "SHINGAKU_TANDAI"         => trim($tmp[4]),
                                  "SHINGAKU_SENMON"         => trim($tmp[5]),
                                  "SHINGAKU_SONOTA"         => trim($tmp[6]),
                                  "SHINGAKU_REMARK"         => trim($tmp[7]),
                                  "SHINGAKU_GOUKEI"         => trim($tmp[8]),
                                  "SHUSHOKU_KENNAI"         => trim($tmp[9]),
                                  "SHUSHOKU_KENNAI_KOUMUIN" => trim($tmp[10]),
                                  "SHUSHOKU_KENGAI"         => trim($tmp[11]),
                                  "SHUSHOKU_KENGAI_KOUMUIN" => trim($tmp[12]),
                                  "SHUSHOKU_SONOTA"         => trim($tmp[13]),
                                  "SHUSHOKU_REMARK"         => trim($tmp[14]),
                                  "SHUSHOKU_GOUKEI"         => trim($tmp[15]),
                                  "SONOTA_GOUKEI"           => trim($tmp[16]),
                                  "ZENTAI_GOUKEI"           => trim($tmp[17]),
                                  "EDBOARD_SCHOOLCD"        => "",
                                  "YEAR"                    => "",
                                  "LARGE_DIV"               => "",
                                  "MIDDLE_DIV"              => "",
                                  "SMALL_DIV"               => "");

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
        $returnFlg     = true;
        $hissuErrFlg   = false;    //必須エラーフラグ
        $numericErrFlg = false;    //数字エラーフラグ
        $lengthErrFlg  = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg   = "";       //必須エラーメッセージ
        $numericErrMsg = "";       //数字エラーメッセージ
        $lengthErrMsg  = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("COURSECD", "MAJORCD", "SEX"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック(全て)
            if ($data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje410Query::insertQueryErr($db, $line, $checkError);
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
}
