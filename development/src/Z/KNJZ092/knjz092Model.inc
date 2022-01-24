<?php

require_once('for_php7.php');
class knjz092Model extends Model
{
    public $field = array();
    public $cmd;
    public $finschoolcd;        //primary
    public $file;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjz092Model()
    {
    }

    public function init()
    {
        $this->field = array("FINSCHOOLCD"         => VARS::post("FINSCHOOLCD"),         //学校コード
                             "FINSCHOOL_TYPE"      => VARS::post("FINSCHOOL_TYPE"),      //職種
                             "FINSCHOOL_DISTCD"    => VARS::post("FINSCHOOL_DISTCD"),    //学校立コード
                             "FINSCHOOL_DISTCD2"   => VARS::post("FINSCHOOL_DISTCD2"),   //学区コード
                             "FINSCHOOL_DIV"       => VARS::post("FINSCHOOL_DIV"),       //学校種別
                             "FINSCHOOL_NAME"      => VARS::post("FINSCHOOL_NAME"),      //学校名
                             "FINSCHOOL_KANA"      => VARS::post("FINSCHOOL_KANA"),      //学校名かな
                             "FINSCHOOL_NAME_ABBV" => VARS::post("FINSCHOOL_NAME_ABBV"), //学校名略称
                             "FINSCHOOL_KANA_ABBV" => VARS::post("FINSCHOOL_KANA_ABBV"), //学校名かな略称
                             "PRINCNAME"           => VARS::post("PRINCNAME"),           //学校長氏名
                             "PRINCNAME_SHOW"      => VARS::post("PRINCNAME_SHOW"),      //学校長氏名表示用
                             "PRINCKANA"           => VARS::post("PRINCKANA"),           //学校長氏名かな
                             "DISTRICTCD"          => VARS::post("DISTRICTCD"),          //地区コード
                             "FINSCHOOL_ZIPCD"     => VARS::post("FINSCHOOL_ZIPCD"),     //郵便番号
                             "FINSCHOOL_ADDR1"     => VARS::post("FINSCHOOL_ADDR1"),     //住所
                             "FINSCHOOL_ADDR2"     => VARS::post("FINSCHOOL_ADDR2"),     //住所
                             "FINSCHOOL_TELNO"     => VARS::post("FINSCHOOL_TELNO"),     //電話番号
                             "FINSCHOOL_FAXNO"     => VARS::post("FINSCHOOL_FAXNO"),     //FAX番号
                             "EDBOARDCD"           => VARS::post("EDBOARDCD"),           //教育委員会コード
                             "UPDATED"             => VARS::post("UPDATED"),

                             "HEADER"              => VARS::post("HEADER"),              //ヘッダの有無
                             "OUTPUT"              => VARS::post("OUTPUT")               //処理の種類(1:取込、2:書出、3:エラー出力)
                             );

        $this->cmd = VARS::request("cmd");

        $this->file = VARS::file("FILE");

        if (VARS::get("mode") == "1") {
            unset($this->finschoolcd);
        }
        $temp = VARS::get("FINSCHOOLCD");
        if (isset($temp)) {
            $this->finschoolcd  = VARS::get("FINSCHOOLCD");
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        $duplHeader = array("FINSCHOOLCD"  => "出身学校コード");
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            //エラー
            $tables = array();
            $tables = array("1" => "FINHIGHSCHOOL_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header) . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header = array("出身学校コード",       //0   (7)
                            "校種",                 //1   (1)
                            "学校立コード",         //2   (3)
                            "学区コード",           //3   (3)
                            "学校種別",             //4   (1)
                            "出身学校名",           //5   (75)
                            "出身学校名かな",       //6   (75)
                            "出身学校略称名",       //7   (30)
                            "出身学校名かな略称",   //8   (75)
                            "出身学校長氏名",       //9   (60)
                            "出身学校長氏名表示用", //10  (30)
                            "出身学校長氏名かな",   //11  (120)
                            "地区コード",           //12  (2)
                            "出身学校郵便番号",     //13  (8)
                            "出身学校住所1",        //14  (75)
                            "出身学校住所2",        //15  (75)
                            "出身学校電話番号",     //16  (14)
                            "出身学校FAX番号",      //17  (14)
                            "教育委員会コード",     //18  (6)
                            $this->lastColumn
                            );
            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjz092Query::getCsvData();
        }

        if ($this->field["OUTPUT"] == "4") {
            $contents .= "1234567,";       // FINSCHOOLCD         出身学校コード
            $contents .= "1,";             // FINSCHOOL_TYPE      校種
            $contents .= "1234,";          // FINSCHOOL_DISTCD    学校立コード
            $contents .= "01,";            // FINSCHOOL_DISTCD2   学区コード
            $contents .= "1,";             // FINSCHOOL_DIV       学校種別
            $contents .= "全角25文字,";    // FINSCHOOL_NAME      出身学校名
            $contents .= "全角25文字,";    // FINSCHOOL_KANA      出身学校名かな
            $contents .= "全角10文字,";    // FINSCHOOL_NAME_ABBV 出身学校略称名
            $contents .= "全角25文字,";    // FINSCHOOL_KANA_ABBV 出身学校名かな略称
            $contents .= "全角20文字,";    // PRINCNAME           出身学校長氏名
            $contents .= "全角10文字,";    // PRINCNAME_SHOW      出身学校長氏名表示用
            $contents .= "全角40文字,";    // PRINCKANA           出身学校長氏名かな
            $contents .= "01,";            // DISTRICTCD          地区コード
            $contents .= "123-4567,";      // FINSCHOOL_ZIPCD     出身学校郵便番号
            $contents .= "全角25文字,";    // FINSCHOOL_ADDR1     出身学校住所1
            $contents .= "全角25文字,";    // FINSCHOOL_ADDR2     出身学校住所2
            $contents .= "03-1234-5678,";  // FINSCHOOL_TELNO     出身学校電話番号
            $contents .= "03-1234-5678,";  // FINSCHOOL_FAXNO     出身学校FAX番号
            $contents .= "123456,";        // EDBOARDCD           教育委員会コード
            $contents .= $this->lastColumn."\n";

            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="出身学校一覧エラー";
            } else {
                $couse="出身学校一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    //CVSファイル
    //クライアント ⇒ サーバ
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
                knjz092Query::insertQueryCsv($db, $this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                Query::dbCheckIn($db);
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
            $checkdata = array("FINSCHOOLCD"         => array(7   ,"出身学校コード"       ), //0
                               "FINSCHOOL_TYPE"      => array(1   ,"校種"                 ), //1
                               "FINSCHOOL_DISTCD"    => array(4   ,"学校立コード"         ), //2
                               "FINSCHOOL_DISTCD2"   => array(3   ,"学区コード"           ), //3
                               "FINSCHOOL_DIV"       => array(1   ,"学校種別"             ), //4
                               "FINSCHOOL_NAME"      => array(75  ,"出身学校名"           ), //5
                               "FINSCHOOL_KANA"      => array(75  ,"出身学校名かな"       ), //6
                               "FINSCHOOL_NAME_ABBV" => array(30  ,"出身学校略称名"       ), //7
                               "FINSCHOOL_KANA_ABBV" => array(75  ,"出身学校名かな略称"   ), //8
                               "PRINCNAME"           => array(60  ,"出身学校長氏名"       ), //9
                               "PRINCNAME_SHOW"      => array(30  ,"出身学校長氏名表示用" ), //10
                               "PRINCKANA"           => array(120 ,"出身学校長氏名かな"   ), //11
                               "DISTRICTCD"          => array(2   ,"地区コード"           ), //12
                               "FINSCHOOL_ZIPCD"     => array(8   ,"出身学校郵便番号"     ), //13
                               "FINSCHOOL_ADDR1"     => array(75  ,"出身学校住所1"        ), //14
                               "FINSCHOOL_ADDR2"     => array(75  ,"出身学校住所2"        ), //15
                               "FINSCHOOL_TELNO"     => array(14  ,"出身学校電話番号"     ), //16
                               "FINSCHOOL_FAXNO"     => array(14  ,"出身学校FAX番号"      ), //17
                               "EDBOARDCD"           => array(6   ,"教育委員会コード"     )  //18
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
            $primaryKey1 = $this->getPrimaryKey('FINHIGHSCHOOL_MST');
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
                    $data = array("FINSCHOOLCD"         => (strlen(trim($tmp[0]))) ? sprintf("%07d", $tmp[0]) : "",
                                  "FINSCHOOL_TYPE"      => $tmp[1],
                                  "FINSCHOOL_DISTCD"    => $tmp[2],
                                  "FINSCHOOL_DISTCD2"   => $tmp[3],
                                  "FINSCHOOL_DIV"       => $tmp[4],
                                  "FINSCHOOL_NAME"      => $tmp[5],
                                  "FINSCHOOL_KANA"      => $tmp[6],
                                  "FINSCHOOL_NAME_ABBV" => $tmp[7],
                                  "FINSCHOOL_KANA_ABBV" => $tmp[8],
                                  "PRINCNAME"           => $tmp[9],
                                  "PRINCNAME_SHOW"      => $tmp[10],
                                  "PRINCKANA"           => $tmp[11],
                                  "DISTRICTCD"          => $tmp[12],
                                  "FINSCHOOL_ZIPCD"     => $tmp[13],
                                  "FINSCHOOL_ADDR1"     => $tmp[14],
                                  "FINSCHOOL_ADDR2"     => $tmp[15],
                                  "FINSCHOOL_TELNO"     => $tmp[16],
                                  "FINSCHOOL_FAXNO"     => $tmp[17],
                                  "EDBOARDCD"           => $tmp[18]
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
        $returnFlg  = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //名称マスターにないエラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //名称マスターにないエラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $masterSep          = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $name_mst_codeArray = array("FINSCHOOL_TYPE"     => "L019",
                                    "FINSCHOOL_DISTCD"   => "L001",
                                    "FINSCHOOL_DISTCD2"  => "Z015",
                                    "FINSCHOOL_DIV"      => "L015",
                                    "DISTRICTCD"         => "Z003",
                                    );
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("FINSCHOOLCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //名称マスターに存在するかチェック
            if (in_array($key, array("FINSCHOOL_TYPE","FINSCHOOL_DISTCD","FINSCHOOL_DISTCD2","FINSCHOOL_DIV","DISTRICTCD","EDBOARDCD"))) {
                if ($data[$key] != '') {
                    if ($key == "EDBOARDCD") {
                        $query = knjz092Query::checkEdboardcd($data[$key]);
                    } else {
                        $query = knjz092Query::checkNameMst($name_mst_codeArray["$key"], $data[$key]);
                    }
                    $rowCount = $db->getOne($query);
                    if ($rowCount == '0') {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                    }
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz092Query::insertQueryErr($db, $line, $checkError);
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


    //入力項目チェック
    public function validateInput()
    {
        //キーのチェック
        if (trim($this->field["FINSCHOOLCD"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901', "( 学校コード )");
            return false;
        }
        if ($this->finschoolcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->finschoolcd != $this->field["FINSCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["FINSCHOOLCD"]) < 7) {
            $this->field["FINSCHOOLCD"] = sprintf("%07d", $this->field["FINSCHOOLCD"]);
        }

        //DB接続
        $db = Query::dbCheckOut();

        //１レコード取得
        $row = $db->getRow(knjz092Query::getRow($this->field["FINSCHOOLCD"]), DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning('MSG918', "( 学校コード )");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning('MSG307');
            return false;
        }
        if (strlen($this->field["FINSCHOOLCD"]) > 7) {
            $this->setWarning('MSG901', "( 学校コード ) 7バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_NAME"])>75) {
            $this->setWarning('MSG901', "( 学校名 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_KANA"])>75) {
            $this->setWarning('MSG901', "( 学校名かな) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_NAME_ABBV"])>30) {
            $this->setWarning('MSG901', "( 学校名略称 ) 30バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_KANA_ABBV"])>75) {
            $this->setWarning('MSG901', "( 学校名かな略称) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME"])>60) {
            $this->setWarning('MSG901', "( 学校長氏名 ) 60バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME_SHOW"])>30) {
            $this->setWarning('MSG901', "( 学校長氏名表示用 ) 30バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCKANA"])>120) {
            $this->setWarning('MSG901', "( 学校長氏名カナ ) 120バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_ZIPCD"])>8) {
            $this->setWarning('MSG901', "( 郵便番号 ) 8バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_ADDR1"])>75) {
            $this->setWarning('MSG901', "( 住所1 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_ADDR2"])>75) {
            $this->setWarning('MSG901', "( 住所2 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_TELNO"])>14) {
            $this->setWarning('MSG901', "( TEL番号 ) 14バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_FAXNO"])>14) {
            $this->setWarning('MSG901', "( FAX番号 ) 14バイトまで");
            return false;
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage('MSG201');
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->finschoolcd = $this->field["FINSCHOOLCD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    public function &getDeleteModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //キーの更新チェック
        if ($this->finschoolcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->finschoolcd != $this->field["FINSCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }

        $row = $db->getRow(knjz092Query::getRow($this->finschoolcd), DB_FETCHMODE_ASSOC);
        if (!is_array($row)) {
            $this->setWarning('MSG303');
            return;
        }

        //年度データの存在チェック
        if (knjz092Query::isExisting($db, $this->finschoolcd)) {
            $this->setWarning('MSG919', "年度データに登録されています。");
            return;
        }

        //削除
        knjz092Query::getDeleteQuery($db, $this->field);
        $this->setMessage('MSG201');

        //DB切断
        Query::dbCheckIn($db);
    }

    //追加
    public function &insert()
    {
        knjz092Query::getInsertQuery($this->field);
    }

    //更新
    public function &update()
    {
        knjz092Query::getUpdateQuery($this->field);
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
