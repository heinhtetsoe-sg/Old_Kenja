<?php

require_once('for_php7.php');
    //定数定義
//配列内の数値のみ取得
function num_filter($var)
{
    return (is_numeric($var));
}

class knje420Model extends Model
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

    public function knje420Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJE420");

        $this->field = array("YEAR"             => VARS::post("YEAR"),
                             "DOC_NUMBER"       => VARS::post("DOC_NUMBER"),
                             "EXECUTE_DATE"     => VARS::post("EXECUTE_DATE"),
                             "REPORT"           => VARS::post("REPORT"),
                             //CSV
                             "OUTPUT"           => VARS::post("OUTPUT"),
                             "HEADER"           => VARS::post("HEADER")
                            );

        $temp = array("ZENTAI_GOUKEI"                       => VARS::post("ZENTAI_GOUKEI"),
                      "SHINGAKU_GOUKEI"                     => VARS::post("SHINGAKU_GOUKEI"),
                      "SHUSHOKU_KIBOU_TOTAL_GAKKOU"         => VARS::post("SHUSHOKU_KIBOU_TOTAL_GAKKOU"),
                      "SHUSHOKU_KIBOU_KENNAI_GAKKOU"        => VARS::post("SHUSHOKU_KIBOU_KENNAI_GAKKOU"),
                      "SHUSHOKU_KIBOU_KENGAI_GAKKOU"        => VARS::post("SHUSHOKU_KIBOU_KENGAI_GAKKOU"),
                      "SHUSHOKU_KIBOU_TOTAL_JIBUN"          => VARS::post("SHUSHOKU_KIBOU_TOTAL_JIBUN"),
                      "SHUSHOKU_KIBOU_KENNAI_JIBUN"         => VARS::post("SHUSHOKU_KIBOU_KENNAI_JIBUN"),
                      "SHUSHOKU_KIBOU_KENGAI_JIBUN"         => VARS::post("SHUSHOKU_KIBOU_KENGAI_JIBUN"),
                      "SHUSHOKU_KIBOU_TOTAL_KOUMUIN"        => VARS::post("SHUSHOKU_KIBOU_TOTAL_KOUMUIN"),
                      "SHUSHOKU_KIBOU_KENNAI_KOUMUIN"       => VARS::post("SHUSHOKU_KIBOU_KENNAI_KOUMUIN"),
                      "SHUSHOKU_KIBOU_KENGAI_KOUMUIN"       => VARS::post("SHUSHOKU_KIBOU_KENGAI_KOUMUIN"),
                      "SHUSHOKU_KIBOU_TOTAL_GOUKEI"         => VARS::post("SHUSHOKU_KIBOU_TOTAL_GOUKEI"),
                      "SHUSHOKU_KIBOU_KENNAI_GOUKEI"        => VARS::post("SHUSHOKU_KIBOU_KENNAI_GOUKEI"),
                      "SHUSHOKU_KIBOU_KENGAI_GOUKEI"        => VARS::post("SHUSHOKU_KIBOU_KENGAI_GOUKEI"),
                      "SONOTA_KEIKAKU_ARI"                  => VARS::post("SONOTA_KEIKAKU_ARI"),
                      "SONOTA_KEIKAKU_NASHI"                => VARS::post("SONOTA_KEIKAKU_NASHI"),
                      "SHUSHOKU_NAITEI_TOTAL_GAKKOU"        => VARS::post("SHUSHOKU_NAITEI_TOTAL_GAKKOU"),
                      "SHUSHOKU_NAITEI_KENNAI_GAKKOU"       => VARS::post("SHUSHOKU_NAITEI_KENNAI_GAKKOU"),
                      "SHUSHOKU_NAITEI_KENGAI_GAKKOU"       => VARS::post("SHUSHOKU_NAITEI_KENGAI_GAKKOU"),
                      "SHUSHOKU_NAITEI_TOTAL_JIBUN"         => VARS::post("SHUSHOKU_NAITEI_TOTAL_JIBUN"),
                      "SHUSHOKU_NAITEI_KENNAI_JIBUN"        => VARS::post("SHUSHOKU_NAITEI_KENNAI_JIBUN"),
                      "SHUSHOKU_NAITEI_KENGAI_JIBUN"        => VARS::post("SHUSHOKU_NAITEI_KENGAI_JIBUN"),
                      "SHUSHOKU_NAITEI_TOTAL_KOUMUIN"       => VARS::post("SHUSHOKU_NAITEI_TOTAL_KOUMUIN"),
                      "SHUSHOKU_NAITEI_KENNAI_KOUMUIN"      => VARS::post("SHUSHOKU_NAITEI_KENNAI_KOUMUIN"),
                      "SHUSHOKU_NAITEI_KENGAI_KOUMUIN"      => VARS::post("SHUSHOKU_NAITEI_KENGAI_KOUMUIN"),
                      "SHUSHOKU_NAITEI_TOTAL_GOUKEI"        => VARS::post("SHUSHOKU_NAITEI_TOTAL_GOUKEI"),
                      "SHUSHOKU_NAITEI_KENNAI_GOUKEI"       => VARS::post("SHUSHOKU_NAITEI_KENNAI_GOUKEI"),
                      "SHUSHOKU_NAITEI_KENGAI_GOUKEI"       => VARS::post("SHUSHOKU_NAITEI_KENGAI_GOUKEI"),
                      "SHINGAKU_SHUSHOKU"                   => VARS::post("SHINGAKU_SHUSHOKU"),
                      "SHUSHOKU_KIBOU_TOTAL_MINAITEI"       => VARS::post("SHUSHOKU_KIBOU_TOTAL_MINAITEI"),
                      "SHUSHOKU_KIBOU_KENNAI_MINAITEI"      => VARS::post("SHUSHOKU_KIBOU_KENNAI_MINAITEI"),
                      "SHUSHOKU_KIBOU_KENGAI_MINAITEI"      => VARS::post("SHUSHOKU_KIBOU_KENGAI_MINAITEI"),
                      "SHINGAKU_IGAI_MIKKETEI"              => VARS::post("SHINGAKU_IGAI_MIKKETEI")
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
        knje420Query::getUpdateQuery($this);
        return;
    }

    //県への報告
    public function &getUpdateEdboardModel()
    {
        //県側・一覧テーブル
        //学校側・報告テーブル
        knje420Query::getUpdateReport($this);
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
                                     "COURSECD"         => "学部コード",
                                     "MAJORCD"          => "課程コード",
                                     "LARGE_DIV"        => "大区分",
                                     "MIDDLE_DIV"       => "中区分",
                                     "SMALL_DIV"        => "小区分",
                                     "SEX"              => "性別コード");
            $tables = array();
            $tables = array("1" => "AFT_DISEASE_ADDITION420_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",")."\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } else {
            //ヘッダ、書出
            $header0 = array("",
                            "",
                            "",
                            "卒業予定者数",
                            "進学希望者数",
                            "就職希望者",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "その他",
                            "",
                            "就職内定者数",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "進学就職",
                            "就職希望者の内末内定者",
                            "",
                            "",
                            "進学希望以外の進路未決定者",
                            $this->lastColumn);

            $header1 = array("",
                            "",
                            "",
                            "",
                            "",
                            "学校紹介",
                            "",
                            "",
                            "自己・縁故",
                            "",
                            "",
                            "公務員",
                            "",
                            "",
                            "合計",
                            "",
                            "",
                            "卒業後の計画",
                            "",
                            "学校紹介",
                            "",
                            "",
                            "自己・縁故",
                            "",
                            "",
                            "公務員",
                            "",
                            "",
                            "合計",
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
                            "",
                            "",
                            "計",
                            "県内",
                            "県外",
                            "計",
                            "県内",
                            "県外",
                            "計",
                            "県内",
                            "県外",
                            "計",
                            "県内",
                            "県外",
                            "有",
                            "無",
                            "計",
                            "県内",
                            "県外",
                            "計",
                            "県内",
                            "県外",
                            "計",
                            "県内",
                            "県外",
                            "計",
                            "県内",
                            "県外",
                            "",
                            "計",
                            "県内",
                            "県外",
                            "",
                            $this->lastColumn);
                            
                            
            if ($this->field["HEADER"]) {
                $contents  = implode(",", $header0) . "\r\n";
                $contents .= implode(",", $header1) . "\r\n";
                $contents .= implode(",", $header2) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knje420Query::readQuery($this, "csv");
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
                $couse="就職内定状況集計表エラー";
            } else {
                $couse="就職内定状況集計表一覧";
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
                knje420Query::insertQueryCsv($db, $this, $this->data_arr);
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
            $checkdata = array("COURSECD"                       => array(1  ,"学科コード" ),
                               "MAJORCD"                        => array(3  ,"課程コード" ),
                               "SEX"                            => array(1  ,"性別コード" ),
                               "ZENTAI_GOUKEI"                  => array(5  ,"卒業予定者数" ),
                               "SHINGAKU_GOUKEI"                => array(5  ,"進学希望者数" ),
                               "SHUSHOKU_KIBOU_TOTAL_GAKKOU"    => array(5  ,"合計人数(就職希望者　学校紹介)" ),
                               "SHUSHOKU_KIBOU_KENNAI_GAKKOU"   => array(5  ,"県内人数(就職希望者　学校紹介)" ),
                               "SHUSHOKU_KIBOU_KENGAI_GAKKOU"   => array(5  ,"県外人数(就職希望者　学校紹介)" ),
                               "SHUSHOKU_KIBOU_TOTAL_JIBUN"     => array(5  ,"合計人数(就職希望者　自己・縁故)" ),
                               "SHUSHOKU_KIBOU_KENNAI_JIBUN"    => array(5  ,"県内人数(就職希望者　自己・縁故)" ),
                               "SHUSHOKU_KIBOU_KENGAI_JIBUN"    => array(5  ,"県外人数(就職希望者　自己・縁故)" ),
                               "SHUSHOKU_KIBOU_TOTAL_KOUMUIN"   => array(5  ,"合計人数(就職希望者　公務員)" ),
                               "SHUSHOKU_KIBOU_KENNAI_KOUMUIN"  => array(5  ,"県内人数(就職希望者　公務員)" ),
                               "SHUSHOKU_KIBOU_KENGAI_KOUMUIN"  => array(5  ,"県外人数(就職希望者　公務員)" ),
                               "SHUSHOKU_KIBOU_TOTAL_GOUKEI"    => array(5  ,"合計人数(就職希望者　合計)" ),
                               "SHUSHOKU_KIBOU_KENNAI_GOUKEI"   => array(5  ,"県内人数(就職希望者　合計)" ),
                               "SHUSHOKU_KIBOU_KENGAI_GOUKEI"   => array(5  ,"県外人数(就職希望者　合計)" ),
                               "SONOTA_KEIKAKU_ARI"             => array(5  ,"卒業後の計画有の人数(その他)" ),
                               "SONOTA_KEIKAKU_NASHI"           => array(5  ,"卒業後の計画無の人数(その他)" ),
                               "SHUSHOKU_NAITEI_TOTAL_GAKKOU"   => array(5  ,"合計人数(就職内定者数　学校紹介)" ),
                               "SHUSHOKU_NAITEI_KENNAI_GAKKOU"  => array(5  ,"県内人数(就職内定者数　学校紹介)" ),
                               "SHUSHOKU_NAITEI_KENGAI_GAKKOU"  => array(5  ,"県外人数(就職内定者数　学校紹介)" ),
                               "SHUSHOKU_NAITEI_TOTAL_JIBUN"    => array(5  ,"合計人数(就職内定者数　自己・縁故)" ),
                               "SHUSHOKU_NAITEI_KENNAI_JIBUN"   => array(5  ,"県内人数(就職内定者数　自己・縁故)" ),
                               "SHUSHOKU_NAITEI_KENGAI_JIBUN"   => array(5  ,"県外人数(就職内定者数　自己・縁故)" ),
                               "SHUSHOKU_NAITEI_TOTAL_KOUMUIN"  => array(5  ,"合計人数(就職内定者数　公務員)" ),
                               "SHUSHOKU_NAITEI_KENNAI_KOUMUIN" => array(5  ,"県内人数(就職内定者数　公務員)" ),
                               "SHUSHOKU_NAITEI_KENGAI_KOUMUIN" => array(5  ,"県外人数(就職内定者数　公務員)" ),
                               "SHUSHOKU_NAITEI_TOTAL_GOUKEI"   => array(5  ,"合計人数(就職内定者数　合計)" ),
                               "SHUSHOKU_NAITEI_KENNAI_GOUKEI"  => array(5  ,"県内人数(就職内定者数　合計)" ),
                               "SHUSHOKU_NAITEI_KENGAI_GOUKEI"  => array(5  ,"県外人数(就職内定者数　合計)" ),
                               "SHINGAKU_SHUSHOKU"              => array(5  ,"進学就職人数" ),
                               "SHUSHOKU_KIBOU_TOTAL_MINAITEI"  => array(5  ,"合計人数(就職希望者の未内定者)" ),
                               "SHUSHOKU_KIBOU_KENNAI_MINAITEI" => array(5  ,"県内人数(就職希望者の未内定者)" ),
                               "SHUSHOKU_KIBOU_KENGAI_MINAITEI" => array(5  ,"県外人数(就職希望者の未内定者)" ),
                               "SHINGAKU_IGAI_MIKKETEI"         => array(5  ,"進学希望以外の進路未決定者" )
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
            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('AFT_DISEASE_ADDITION420_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        if ($line == 3) {//headerが3行分
                            $header = true;
                        }
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array("COURSECD"                       => trim($tmp[0]),
                                  "MAJORCD"                        => trim($tmp[1]),
                                  "SEX"                            => trim($tmp[2]),
                                  "ZENTAI_GOUKEI"                  => trim($tmp[3]),
                                  "SHINGAKU_GOUKEI"                => trim($tmp[4]),
                                  "SHUSHOKU_KIBOU_TOTAL_GAKKOU"    => trim($tmp[5]),
                                  "SHUSHOKU_KIBOU_KENNAI_GAKKOU"   => trim($tmp[6]),
                                  "SHUSHOKU_KIBOU_KENGAI_GAKKOU"   => trim($tmp[7]),
                                  "SHUSHOKU_KIBOU_TOTAL_JIBUN"     => trim($tmp[8]),
                                  "SHUSHOKU_KIBOU_KENNAI_JIBUN"    => trim($tmp[9]),
                                  "SHUSHOKU_KIBOU_KENGAI_JIBUN"    => trim($tmp[10]),
                                  "SHUSHOKU_KIBOU_TOTAL_KOUMUIN"   => trim($tmp[11]),
                                  "SHUSHOKU_KIBOU_KENNAI_KOUMUIN"  => trim($tmp[12]),
                                  "SHUSHOKU_KIBOU_KENGAI_KOUMUIN"  => trim($tmp[13]),
                                  "SHUSHOKU_KIBOU_TOTAL_GOUKEI"    => trim($tmp[14]),
                                  "SHUSHOKU_KIBOU_KENNAI_GOUKEI"   => trim($tmp[15]),
                                  "SHUSHOKU_KIBOU_KENGAI_GOUKEI"   => trim($tmp[16]),
                                  "SONOTA_KEIKAKU_ARI"             => trim($tmp[17]),
                                  "SONOTA_KEIKAKU_NASHI"           => trim($tmp[18]),
                                  "SHUSHOKU_NAITEI_TOTAL_GAKKOU"   => trim($tmp[19]),
                                  "SHUSHOKU_NAITEI_KENNAI_GAKKOU"  => trim($tmp[20]),
                                  "SHUSHOKU_NAITEI_KENGAI_GAKKOU"  => trim($tmp[21]),
                                  "SHUSHOKU_NAITEI_TOTAL_JIBUN"    => trim($tmp[22]),
                                  "SHUSHOKU_NAITEI_KENNAI_JIBUN"   => trim($tmp[23]),
                                  "SHUSHOKU_NAITEI_KENGAI_JIBUN"   => trim($tmp[24]),
                                  "SHUSHOKU_NAITEI_TOTAL_KOUMUIN"  => trim($tmp[25]),
                                  "SHUSHOKU_NAITEI_KENNAI_KOUMUIN" => trim($tmp[26]),
                                  "SHUSHOKU_NAITEI_KENGAI_KOUMUIN" => trim($tmp[27]),
                                  "SHUSHOKU_NAITEI_TOTAL_GOUKEI"   => trim($tmp[28]),
                                  "SHUSHOKU_NAITEI_KENNAI_GOUKEI"  => trim($tmp[29]),
                                  "SHUSHOKU_NAITEI_KENGAI_GOUKEI"  => trim($tmp[30]),
                                  "SHINGAKU_SHUSHOKU"              => trim($tmp[31]),
                                  "SHUSHOKU_KIBOU_TOTAL_MINAITEI"  => trim($tmp[32]),
                                  "SHUSHOKU_KIBOU_KENNAI_MINAITEI" => trim($tmp[33]),
                                  "SHUSHOKU_KIBOU_KENGAI_MINAITEI" => trim($tmp[34]),
                                  "SHINGAKU_IGAI_MIKKETEI"         => trim($tmp[35]),
                                  "EDBOARD_SCHOOLCD"               => "",
                                  "YEAR"                           => "",
                                  "LARGE_DIV"                      => "",
                                  "MIDDLE_DIV"                     => "",
                                  "SMALL_DIV"                      => "");

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
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
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
            knje420Query::insertQueryErr($db, $line, $checkError);
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
