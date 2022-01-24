<?php

require_once('for_php7.php');
    //定数定義
//配列内の数値のみ取得
function num_filter($var)
{
    return (is_numeric($var));
}

class knje432Model extends Model
{
    public $cmd;
    public $gc_select;                     //セレクトメニュー
    public $control_data = array();        //コントロールのデータを保持
    public $staffcd;                       //セキュリティーチェック
    public $sec_competence;                //権限保持
    public $updated = array();             //UPDATED値保持
    public $fields = array();
    public $hyoteiYomikae; // 評定1の場合は2で処理する
    public $schoolcd;  //学校コード
    public $execute_date; //県への報告用登録日付
    public $lastColumn;//CSVの最後の列の値

    public function knje432Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー
        $this->hyoteiYomikae    = VARS::post("hyoteiYomikae");
        $this->execute_date = VARS::post("EXECUTE_DATE");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJE432");

        $this->field = array("YEAR"             => VARS::post("YEAR"),
                             "DOC_NUMBER"       => VARS::post("DOC_NUMBER"),
                             "EXECUTE_DATE"     => VARS::post("EXECUTE_DATE"),
                             "REPORT"           => VARS::post("REPORT"),
                             //CSV
                             "OUTPUT"           => VARS::post("OUTPUT"),
                             "HEADER"           => VARS::post("HEADER")
                            );

        $temp = array();
        for ($i = 1; $i <= get_count($this->industryData) * 2; $i++) {
            $temp["COUNT".$i] = VARS::post("COUNT".$i);
        }
        $temp["COUNT98"] = VARS::post("COUNT98");
        $temp["COUNT99"] = VARS::post("COUNT99");

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
        knje432Query::getUpdateQuery($this);
        return;
    }

    //県への報告
    public function &getUpdateEdboardModel()
    {
        //県側・疾病等結果一覧テーブル
        //学校側・報告テーブル
        knje432Query::getUpdateReport($this);
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

            $tables = array();
            $tables = array("1" => "AFT_DISEASE_ADDITION432_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $headerDuplicate = array("EDBOARD_SCHOOLCD" => "教育委員会用学校コード",
                                     "YEAR"             => "年度",
                                     "COURSECD"         => "学科コード",
                                     "MAJORCD"          => "課程コード",
                                     "LARGE_DIV"        => "大区分",
                                     "MIDDLE_DIV"       => "中区分",
                                     "SMALL_DIV"        => "小区分",
                                     "SEX"              => "性別コード");
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header) . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } else {
            //ヘッダ、書出
            $header1 = array("",
                            "",
                            "",
                            "A 農業，林業",
                            "",
                            "B 漁業",
                            "",
                            "C 鉱業，採石業，砂利採取業",
                            "",
                            "D 建設業",
                            "",
                            "E 製造業",
                            "",
                            "F 電気・ガス・熱供給・水道業",
                            "",
                            "G 情報通信業",
                            "",
                            "H 運輸業，郵便業",
                            "",
                            "I 卸売業，小売業",
                            "",
                            "J 金融業，保険業",
                            "",
                            "K 不動産業，物品賃貸業",
                            "",
                            "L 学術研究，専門・技術サービス業",
                            "",
                            "M 宿泊業，飲食サービス業",
                            "",
                            "N 生活関連サービス業，娯楽業",
                            "",
                            "O 教育，学習支援業",
                            "",
                            "P 医療，福祉",
                            "",
                            "Q 複合サービス事業",
                            "",
                            "R サービス業（他に分類されないもの）",
                            "",
                            "S 公務（他に分類されるものを除く）",
                            "",
                            "T 分類不能の産業",
                            "",
                            "計",
                            "",
                            $this->lastColumn);

            $header2 = array("学科コード",
                            "課程コード",
                            "性別コード",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            "県内",
                            "県外",
                            $this->lastColumn);

            if ($this->field["HEADER"]) {
                $contents  = implode(",", $header1) . "\r\n";
                $contents .= implode(",", $header2) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knje432Query::readQuery($this, "csv");
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
                $couse="産業別の就職者数エラー";
            } else {
                $couse="産業別の就職者数一覧";
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
                knje432Query::insertQueryCsv($db, $this, $this->data_arr);
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
            $checkdata = array("COURSECD"   => array(1  ,"学科コード" ),
                               "MAJORCD"    => array(3  ,"課程コード" ),
                               "SEX"        => array(1  ,"性別コード" ),
                               "COUNT1"     => array(5  ,"県内の合計" ),
                               "COUNT2"     => array(5  ,"県外の人数" ),
                               "COUNT3"     => array(5  ,"県内の合計" ),
                               "COUNT4"     => array(5  ,"県外の人数" ),
                               "COUNT5"     => array(5  ,"県内の合計" ),
                               "COUNT6"     => array(5  ,"県外の人数" ),
                               "COUNT7"     => array(5  ,"県内の合計" ),
                               "COUNT8"     => array(5  ,"県外の人数" ),
                               "COUNT9"     => array(5  ,"県内の合計" ),
                               "COUNT10"    => array(5  ,"県外の人数" ),
                               "COUNT11"    => array(5  ,"県内の合計" ),
                               "COUNT12"    => array(5  ,"県外の人数" ),
                               "COUNT13"    => array(5  ,"県内の合計" ),
                               "COUNT14"    => array(5  ,"県外の人数" ),
                               "COUNT15"    => array(5  ,"県内の合計" ),
                               "COUNT16"    => array(5  ,"県外の人数" ),
                               "COUNT17"    => array(5  ,"県内の合計" ),
                               "COUNT18"    => array(5  ,"県外の人数" ),
                               "COUNT19"    => array(5  ,"県内の合計" ),
                               "COUNT20"    => array(5  ,"県外の人数" ),
                               "COUNT21"    => array(5  ,"県内の合計" ),
                               "COUNT22"    => array(5  ,"県外の人数" ),
                               "COUNT23"    => array(5  ,"県内の合計" ),
                               "COUNT24"    => array(5  ,"県外の人数" ),
                               "COUNT25"    => array(5  ,"県内の合計" ),
                               "COUNT26"    => array(5  ,"県外の人数" ),
                               "COUNT27"    => array(5  ,"県内の合計" ),
                               "COUNT28"    => array(5  ,"県外の人数" ),
                               "COUNT29"    => array(5  ,"県内の合計" ),
                               "COUNT30"    => array(5  ,"県外の人数" ),
                               "COUNT31"    => array(5  ,"県内の合計" ),
                               "COUNT32"    => array(5  ,"県外の人数" ),
                               "COUNT33"    => array(5  ,"県内の合計" ),
                               "COUNT34"    => array(5  ,"県外の人数" ),
                               "COUNT35"    => array(5  ,"県内の合計" ),
                               "COUNT36"    => array(5  ,"県外の人数" ),
                               "COUNT37"    => array(5  ,"県内の合計" ),
                               "COUNT38"    => array(5  ,"県外の人数" ),
                               "COUNT39"    => array(5  ,"県内の合計" ),
                               "COUNT40"    => array(5  ,"県外の人数" ),
                               "COUNT98"    => array(5  ,"県内の合計" ),
                               "COUNT99"    => array(5  ,"県外の合計" )
                               );
            /*for ($i = 1; $i <= COUNT($this->industryData) * 2; $i++) {
                $checkdata = array("COUNT".$i => array(5  ,"いずれかの人数" ));
            }*/

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('AFT_DISEASE_ADDITION432_DAT');
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
                    $data = array("COURSECD"    => trim($tmp[0]),
                                  "MAJORCD"     => trim($tmp[1]),
                                  "SEX"         => trim($tmp[2]),
                                  "COUNT1"      => trim($tmp[3]),
                                  "COUNT2"      => trim($tmp[4]),
                                  "COUNT3"      => trim($tmp[5]),
                                  "COUNT4"      => trim($tmp[6]),
                                  "COUNT5"      => trim($tmp[7]),
                                  "COUNT6"      => trim($tmp[8]),
                                  "COUNT7"      => trim($tmp[9]),
                                  "COUNT8"      => trim($tmp[10]),
                                  "COUNT9"      => trim($tmp[11]),
                                  "COUNT10"     => trim($tmp[12]),
                                  "COUNT11"     => trim($tmp[13]),
                                  "COUNT12"     => trim($tmp[14]),
                                  "COUNT13"     => trim($tmp[15]),
                                  "COUNT14"     => trim($tmp[16]),
                                  "COUNT15"     => trim($tmp[17]),
                                  "COUNT16"     => trim($tmp[18]),
                                  "COUNT17"     => trim($tmp[19]),
                                  "COUNT18"     => trim($tmp[20]),
                                  "COUNT19"     => trim($tmp[21]),
                                  "COUNT20"     => trim($tmp[22]),
                                  "COUNT21"     => trim($tmp[23]),
                                  "COUNT22"     => trim($tmp[24]),
                                  "COUNT23"     => trim($tmp[25]),
                                  "COUNT24"     => trim($tmp[26]),
                                  "COUNT25"     => trim($tmp[27]),
                                  "COUNT26"     => trim($tmp[28]),
                                  "COUNT27"     => trim($tmp[29]),
                                  "COUNT28"     => trim($tmp[30]),
                                  "COUNT29"     => trim($tmp[31]),
                                  "COUNT30"     => trim($tmp[32]),
                                  "COUNT31"     => trim($tmp[33]),
                                  "COUNT32"     => trim($tmp[34]),
                                  "COUNT33"     => trim($tmp[35]),
                                  "COUNT34"     => trim($tmp[36]),
                                  "COUNT35"     => trim($tmp[37]),
                                  "COUNT36"     => trim($tmp[38]),
                                  "COUNT37"     => trim($tmp[39]),
                                  "COUNT38"     => trim($tmp[40]),
                                  "COUNT39"     => trim($tmp[41]),
                                  "COUNT40"     => trim($tmp[42]),
                                  "COUNT98"     => trim($tmp[43]),//COUNTの最終の添え字の次の値
                                  "COUNT99"     => trim($tmp[44]));
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
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;  //重複エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

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
            knje432Query::insertQueryErr($db, $line, $checkError);
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
