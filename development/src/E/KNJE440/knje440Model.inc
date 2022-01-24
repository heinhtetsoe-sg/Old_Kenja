<?php

require_once('for_php7.php');
    //定数定義
//配列内の数値のみ取得
function num_filter($var)
{
    return (is_numeric($var));
}

class knje440Model extends Model
{
    public $cmd;
    public $control_data = array();        //コントロールのデータを保持
    public $staffcd;                       //セキュリティーチェック
    public $sec_competence;                //権限保持
    public $updated = array();             //UPDATED値保持
    public $fields = array();
    public $schoolcd;      //学校コード
    public $year;
    public $execute_date;  //県への報告用登録日付
    public $lastColumn;//CSVの最後の列の値

    public function knje440Model()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->execute_date);
        }

        $this->cmd          = VARS::request("cmd");
        $this->year         = VARS::post("YEAR");
        $this->execute_date = VARS::post("EXECUTE_DATE");

        //CSV
        $this->field = array("OUTPUT"           => VARS::post("OUTPUT"),
                             "DOC_NUMBER"       => VARS::post("DOC_NUMBER"),
                             "HEADER"           => VARS::post("HEADER")
                            );

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJE440");

        //項目名(列)
        $this->nameArray = array("0101999"
                                ,"0102999"
                                ,"0103999"
                                ,"0104999"
                                ,"0105999"
                                ,"0106999"
                                ,"0299999"
                                ,"0301999"
                                ,"0302999"
                                ,"0499999"
                                ,"0599999"
                                ,"0699999"
                                ,"0799999"
                                ,"0899999"
                                ,"9999999"
                                ,"9901999"
                                ,"9902999"
                                ,"9903999"
                                ,"9904999"
                                ,"1001999"
                                ,"1002999"
                                ,"1101999"
                                ,"1102999"
                                ,"1103999"
                                ,"1104999");

        foreach ($this->nameArray as $name) {
            $temp["CNT".$name] = VARS::post("CNT".$name);
        }
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
        knje440Query::getUpdateQuery($this);
        return;
    }

    //県への報告
    public function &getUpdateEdboardModel()
    {
        //県側・進路統計テーブル
        //学校側・報告テーブル
        knje440Query::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    //*******CVSファイル*************/
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        if ($this->field["OUTPUT"] == "3") {
            $headerDuplicate = array( "EDBOARD_SCHOOLCD" => "教育委員会用学校コード",
                                      "YEAR"             => "年度",
                                      "COURSECD"         => "学科コード",
                                      "MAJORCD"          => "課程コード",
                                      "LARGE_DIV"        => "大区分",
                                      "MIDDLE_DIV"       => "中区分",
                                      "SMALL_DIV"        => "小区分",
                                      "SEX"              => "性別コード"
                                    );
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $tables = array();
            $tables = array("1" => "AFT_DISEASE_ADDITION440_DAT");
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
                            "９ 状況別卒業者数",
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
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "10 ｢９｣卒業者総数のうち大学（学部）、短期大学（本科）への入学志願者数",
                            "",
                            "11 2005年3月以前の卒業者のうち本年度大学（学部）、短期大学（本科）への入学志願者数",
                            "",
                            "",
                            "",
                            $this->lastColumn);

            $header1 = array("",
                            "",
                            "",
                            "Ａ 大学等進学者",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "Ｂ 専修学校（専門課程）進学者",
                            "Ｃ 専修学校（一般課程）等入学者",
                            "",
                            "Ｄ 公共職業能力開発施設等入学者",
                            "Ｅ 就職者(左記Ａ、Ｂ、Ｃ、Ｄを除く。)",
                            "Ｆ 一時的な仕事に就 い た 者",
                            "Ｇ 左記以外の者",
                            "Ｈ 不詳・死亡の者",
                            "計 (卒業者総数)",
                            "（再掲）左記Ａ、Ｂ、Ｃ、Ｄのうち就職している者",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "2005年3月卒業者",
                            "",
                            "2004年3月以前卒業者",
                            "",
                            $this->lastColumn);

            $header2 = array("学科コード",
                            "課程コード",
                            "性別コード",
                            "大学（学部）",
                            "短 期 大 学（本科）",
                            "大学・短期大学の通信教育部及び放送大学",
                            "大学・短期大学（別科）",
                            "高等学校（専攻科）",
                            "特別支援学校高等部（専攻科）",
                            "",
                            "専修学校（一般課程）等",
                            "各種学校",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "Ａのうち",
                            "Ｂのうち",
                            "Ｃのうち",
                            "Ｄのうち",
                            "大学（学部）",
                            "短期大学（本科）",
                            "大学（学部）",
                            "短期大学（本科）",
                            "大学（学部）",
                            "短期大学（本科）",
                            $this->lastColumn);
                            
                            
            if ($this->field["HEADER"]) {
                $contents  = implode(",", $header0) . "\r\n";
                $contents .= implode(",", $header1) . "\r\n";
                $contents .= implode(",", $header2) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knje440Query::readQuery($this, "data", "csv");
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
                $couse="状況別卒業者数エラー";
            } else {
                $couse="状況別卒業者数一覧";
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
                knje440Query::insertQueryCsv($db, $this, $this->data_arr);
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
                               "CNT0101999" => array(5  ,"大学(学部)の人数(Ａ 大学等進学者)" ),
                               "CNT0102999" => array(5  ,"短期大学(本科)の人数(Ａ 大学等進学者)" ),
                               "CNT0103999" => array(5  ,"大学・短期大学の通信教育部及び放送大学の人数(Ａ 大学等進学者)" ),
                               "CNT0104999" => array(5  ,"大学・短期大学(別科)の人数(Ａ 大学等進学者)" ),
                               "CNT0105999" => array(5  ,"高等学校(専攻科)の人数(Ａ 大学等進学者)" ),
                               "CNT0106999" => array(5  ,"特別支援学校高等部(専攻科)の人数(Ａ 大学等進学者)" ),
                               "CNT0299999" => array(5  ,"Ｂ 専修学校(専門課程)進学者の人数" ),
                               "CNT0301999" => array(5  ,"(一般課程)等専修学校の人数(Ｃ 専修学校(一般課程)等入学者)" ),
                               "CNT0302999" => array(5  ,"各種学校の人数(Ｃ 専修学校(一般課程)等入学者)" ),
                               "CNT0499999" => array(5  ,"Ｄ 公共職業能力開発施設等入学者の人数" ),
                               "CNT0599999" => array(5  ,"Ｅ 就職者(Ａ、Ｂ、Ｃ、Ｄを除く)の人数" ),
                               "CNT0699999" => array(5  ,"Ｆ 一時的な仕事に就いた者の人数" ),
                               "CNT0799999" => array(5  ,"Ｇ 左記以外の者の人数" ),
                               "CNT0899999" => array(5  ,"Ｈ 不詳・死亡の者の人数" ),
                               "CNT9999999" => array(5  ,"計 (卒業者総数)の人数" ),
                               "CNT9901999" => array(5  ,"(再掲)Ａのうち就職している者の人数" ),
                               "CNT9902999" => array(5  ,"(再掲)Ｂのうち就職している者の人数" ),
                               "CNT9903999" => array(5  ,"(再掲)Ｃのうち就職している者の人数" ),
                               "CNT9904999" => array(5  ,"(再掲)Ｄのうち就職している者の人数" ),
                               "CNT1001999" => array(5  ,"大学(学部)の人数(10 ｢9｣卒業者総数のうち大学（学部）、短期大学(本科)への入学志願者数)" ),
                               "CNT1002999" => array(5  ,"短期大学(本科)の人数(10 ｢9｣卒業者総数のうち大学（学部）、短期大学(本科)への入学志願者数)" ),
                               "CNT1101999" => array(5  ,"2005年3月卒業者の大学(学部)の人数(11 2005年3月以前の卒業者のうち本年度大学(学部)、短期大学(本科)への入学志願者数)" ),
                               "CNT1102999" => array(5  ,"2005年3月卒業者の短期大学(本科)の人数(11 2005年3月以前の卒業者のうち本年度大学(学部)、短期大学(本科)への入学志願者数)の人数" ),
                               "CNT1103999" => array(5  ,"2004年3月以前卒業者の大学(学部)の人数(11 2005年3月以前の卒業者のうち本年度大学(学部)、短期大学(本科)への入学志願者数)の人数" ),
                               "CNT1104999" => array(5  ,"2004年3月以前卒業者の短期大学(本科)の人数(11 2005年3月以前の卒業者のうち本年度大学(学部)、短期大学(本科)への入学志願者数)の人数の人数" )
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
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('AFT_DISEASE_ADDITION440_DAT');
            $duplicateCsv1 = array();
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
                    $data = array("COURSECD"    => trim($tmp[0]),
                                  "MAJORCD"     => trim($tmp[1]),
                                  "SEX"         => trim($tmp[2]),
                                  "CNT0101999"  => trim($tmp[3]),
                                  "CNT0102999"  => trim($tmp[4]),
                                  "CNT0103999"  => trim($tmp[5]),
                                  "CNT0104999"  => trim($tmp[6]),
                                  "CNT0105999"  => trim($tmp[7]),
                                  "CNT0106999"  => trim($tmp[8]),
                                  "CNT0299999"  => trim($tmp[9]),
                                  "CNT0301999"  => trim($tmp[10]),
                                  "CNT0302999"  => trim($tmp[11]),
                                  "CNT0499999"  => trim($tmp[12]),
                                  "CNT0599999"  => trim($tmp[13]),
                                  "CNT0699999"  => trim($tmp[14]),
                                  "CNT0799999"  => trim($tmp[15]),
                                  "CNT0899999"  => trim($tmp[16]),
                                  "CNT9999999"  => trim($tmp[17]),
                                  "CNT9901999"  => trim($tmp[18]),
                                  "CNT9902999"  => trim($tmp[19]),
                                  "CNT9903999"  => trim($tmp[20]),
                                  "CNT9904999"  => trim($tmp[21]),
                                  "CNT1001999"  => trim($tmp[22]),
                                  "CNT1002999"  => trim($tmp[23]),
                                  "CNT1101999"  => trim($tmp[24]),
                                  "CNT1102999"  => trim($tmp[25]),
                                  "CNT1103999"  => trim($tmp[26]),
                                  "CNT1104999"  => trim($tmp[27]),
                                  "EDBOARD_SCHOOLCD"     => "",
                                  "YEAR"                 => "",
                                  "LARGE_DIV"            => "",
                                  "MIDDLE_DIV"           => "",
                                  "SMALL_DIV"            => "");
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
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

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
            knje440Query::insertQueryErr($db, $line, $checkError);
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
