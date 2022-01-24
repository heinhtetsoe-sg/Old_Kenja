<?php

require_once('for_php7.php');
class knjz095_2Model extends Model
{
    public $field = array();
    public $cmd;
    public $partner_schoolcd;        //primary
    public $selectPartnerSchoolType;
    
    public $file;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjz095_2Model()
    {
    }

    public function init()
    {
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->partner_schoolcdKeta = ($this->Properties["usePartnerSchoolcdFieldSize"] == '12' ? 12 : 7);

        $this->field = array("PARTNER_SCHOOLCD"         => (strlen(trim(VARS::post("PARTNER_SCHOOLCD")))) ? sprintf("%0".$this->partner_schoolcdKeta."d", VARS::post("PARTNER_SCHOOLCD")) : "",         //学校コード
                             "PARTNER_SCHOOL_TYPE"      => VARS::post("PARTNER_SCHOOL_TYPE"),      //校種
                             "PARTNER_SCHOOL_DISTCD"    => VARS::post("PARTNER_SCHOOL_DISTCD"),    //地区コード
                             "PARTNER_SCHOOL_DISTCD2"   => VARS::post("PARTNER_SCHOOL_DISTCD2"),   //学区コード
                             "PARTNER_SCHOOL_DIV"       => VARS::post("PARTNER_SCHOOL_DIV"),       //学校立コード
                             "PARTNER_SCHOOL_NAME"      => VARS::post("PARTNER_SCHOOL_NAME"),      //学校名
                             "PARTNER_SCHOOL_KANA"      => VARS::post("PARTNER_SCHOOL_KANA"),      //学校名かな
                             "PARTNER_SCHOOL_NAME_ABBV" => VARS::post("PARTNER_SCHOOL_NAME_ABBV"), //学校名略称
                             "PARTNER_SCHOOL_KANA_ABBV" => VARS::post("PARTNER_SCHOOL_KANA_ABBV"), //学校名かな略称
                             "PRINCNAME"           => VARS::post("PRINCNAME"),           //学校長氏名
                             "PRINCNAME_SHOW"      => VARS::post("PRINCNAME_SHOW"),      //学校長氏名表示用
                             "PRINCKANA"           => VARS::post("PRINCKANA"),           //学校長氏名かな
                             "DISTRICTCD"          => VARS::post("DISTRICTCD"),          //地区コード
                             "PARTNER_SCHOOL_PREF_CD"   => VARS::post("PARTNER_SCHOOL_PREF_CD"),   //都道府県
                             "PARTNER_SCHOOL_ZIPCD"     => VARS::post("PARTNER_SCHOOL_ZIPCD"),     //郵便番号
                             "PARTNER_SCHOOL_ADDR1"     => VARS::post("PARTNER_SCHOOL_ADDR1"),     //住所
                             "PARTNER_SCHOOL_ADDR2"     => VARS::post("PARTNER_SCHOOL_ADDR2"),     //住所
                             "PARTNER_SCHOOL_TELNO"     => VARS::post("PARTNER_SCHOOL_TELNO"),     //電話番号
                             "PARTNER_SCHOOL_FAXNO"     => VARS::post("PARTNER_SCHOOL_FAXNO"),     //FAX番号
                             "EDBOARDCD"           => VARS::post("EDBOARDCD"),           //教育委員会コード
                             "TOUHAIKOU"           => VARS::post("TOUHAIKOU"),           //統廃合フラグ
                             "DISTRICT_NAME"       => VARS::post("DISTRICT_NAME"),       //所在地コード
                             "MIRAI_FS_CD"         => VARS::post("MIRAI_FS_CD"),         //ミライコンパス学校コード
                             "INJI_FLG"            => VARS::post("INJI_FLG"),            //卒業印字フラグ
                             "UPDATED"             => VARS::post("UPDATED"),
                             "HEADER"              => VARS::post("HEADER"),              //ヘッダの有無
                             "OUTPUT"              => VARS::post("OUTPUT")               //処理の種類(1:取込、2:書出、3:エラー出力)
                             );

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "changeType") {
            $this->selectPartnerSchoolType = VARS::post("SELECT_PARTNER_SCHOOL_TYPE");
        }

        $this->file = VARS::file("FILE");

        if (VARS::get("mode") == "1") {
            unset($this->partner_schoolcd);
            unset($this->selectPartnerSchoolType);
        }
        $temp = VARS::get("PARTNER_SCHOOLCD");
        if (isset($temp)) {
            $this->partner_schoolcd  = VARS::get("PARTNER_SCHOOLCD");
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
        if ($this->selectPartnerSchoolType == "" && ($this->field["OUTPUT"] == "1" || $this->field["OUTPUT"] == "2")) {
            $this->setWarning("左画面より校種を選択してください。");
            return false;
        }
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $headerDuplicate = array();
            $headerDuplicate["PARTNER_SCHOOLCD"]   = "※協力校コード";
            $headerDuplicate["PARTNER_SCHOOL_SEQ"] = "SEQ";
            $headerDuplicate["MIRAI_FS_CD"]        = "ミライコンパス学校コード";
            $tables = array("1" => "PARTNER_SCHOOL_MST", "2" => "PARTNER_SCHOOL_DETAIL_MST");
            if ($this->Properties["useMiraicompass"] == '1') {
                $tables = array("3" => "ENTEXAM_MIRAI_FS_REP_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",") . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header = array("※協力校コード",     //0   (7)
                            "校種",                 //1   (1)
                            "地区コード",           //2   (3)
                            "学区コード",           //3   (3)
                            "学校立コード",         //4   (1)
                            "協力校名",           //5   (75)
                            "協力校名かな",       //6   (75)
                            "協力校略称名",       //7   (30)
                            "協力校名かな略称",   //8   (75)
                            "協力校長氏名",       //9   (60)
                            "協力校長氏名表示用", //10  (30)
                            "協力校長氏名かな",   //11  (120)
                            "地区コード",           //12  (2)
                            "都道府県",             //13  (2)
                            "協力校郵便番号",     //14  (8)
                            "協力校住所1",        //15  (75)
                            "協力校住所2",        //16  (75)
                            "協力校電話番号",     //17  (14)
                            "協力校FAX番号",      //18  (14)
                            "教育委員会コード",     //19  (6)
                            "統廃合フラグ"          //20  (1)
                            );
            if ($this->Properties["useLocationMst"] == '1') {
                $header[] = "所在地コード"; //21  (5)
            }

            if ($this->Properties["useMiraicompass"] == '1') {
                $header[] = "ミライコンパス学校コード"; //22  (10)
            }
            $header[] = "「卒業」印字しない"; //23  (1)
            $header[] = $this->lastColumn;
            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjz095_2Query::getCsvData($this);
        }

        if ($this->field["OUTPUT"] == "4") {
            if ($this->partner_schoolcdKeta == 12) {
                $contents .= "123456789012,";       // PARTNER_SCHOOLCD         協力校コード
            } else {
                $contents .= "1234567,";       // PARTNER_SCHOOLCD         協力校コード
            }
            $contents .= "1,";             // PARTNER_SCHOOL_TYPE      校種
            $contents .= "1234,";          // PARTNER_SCHOOL_DISTCD    地区コード
            $contents .= "01,";            // PARTNER_SCHOOL_DISTCD2   学区コード
            $contents .= "1,";             // PARTNER_SCHOOL_DIV       学校立コード
            $contents .= "全角25文字,";    // PARTNER_SCHOOL_NAME      協力校名
            $contents .= "全角25文字,";    // PARTNER_SCHOOL_KANA      協力校名かな
            $contents .= "全角10文字,";    // PARTNER_SCHOOL_NAME_ABBV 協力校略称名
            $contents .= "全角25文字,";    // PARTNER_SCHOOL_KANA_ABBV 協力校名かな略称
            $contents .= "全角20文字,";    // PRINCNAME           協力校長氏名
            $contents .= "全角10文字,";    // PRINCNAME_SHOW      協力校長氏名表示用
            $contents .= "全角40文字,";    // PRINCKANA           協力校長氏名かな
            $contents .= "01,";            // DISTRICTCD          地区コード
            $contents .= "01,";            // PARTNER_SCHOOL_PREF_CD   都道府県
            $contents .= "123-4567,";      // PARTNER_SCHOOL_ZIPCD     協力校郵便番号
            $contents .= "全角30文字,";    // PARTNER_SCHOOL_ADDR1     協力校住所1
            $contents .= "全角30文字,";    // PARTNER_SCHOOL_ADDR2     協力校住所2
            $contents .= "03-1234-5678,";  // PARTNER_SCHOOL_TELNO     協力校電話番号
            $contents .= "03-1234-5678,";  // PARTNER_SCHOOL_FAXNO     協力校FAX番号
            $contents .= "123456,";        // EDBOARDCD           教育委員会コード
            $contents .= "1,";             // TOUHAIKOU           統廃合フラグ
            if ($this->Properties["useLocationMst"] == '1') {
                $contents .= "12345,";         // DISTRICT_NAME       所在地コード
            }
            if ($this->Properties["useMiraicompass"] == '1') {
                $contents .= "1234567890,";    // MIRAI_FS_CD         ミライコンパス学校コード
            }
            $contents .= "1,";             // INJI_FLG            卒業印字フラグ
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
                $couse="協力校一覧エラー";
            } else {
                $couse="協力校一覧";
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
        if ($this->selectPartnerSchoolType == "" && ($this->field["OUTPUT"] == "1" || $this->field["OUTPUT"] == "2")) {
            $this->setWarning("左画面より校種を選択してください。");
            return false;
        }
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                knjz095_2Query::insertQueryCsv($db, $this, $this->data_arr);
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
            $checkdata = array("PARTNER_SCHOOLCD"         => array($this->partner_schoolcdKeta ,"協力校コード"), //0
                               "PARTNER_SCHOOL_TYPE"      => array(1   ,"校種"                 ), //1
                               "PARTNER_SCHOOL_DISTCD"    => array(4   ,"地区コード"           ), //2
                               "PARTNER_SCHOOL_DISTCD2"   => array(3   ,"学区コード"           ), //3
                               "PARTNER_SCHOOL_DIV"       => array(1   ,"学校立コード"         ), //4
                               "PARTNER_SCHOOL_NAME"      => array(75  ,"協力校名"           ), //5
                               "PARTNER_SCHOOL_KANA"      => array(75  ,"協力校名かな"       ), //6
                               "PARTNER_SCHOOL_NAME_ABBV" => array(30  ,"協力校略称名"       ), //7
                               "PARTNER_SCHOOL_KANA_ABBV" => array(75  ,"協力校名かな略称"   ), //8
                               "PRINCNAME"           => array(60  ,"協力校長氏名"       ), //9
                               "PRINCNAME_SHOW"      => array(30  ,"協力校長氏名表示用" ), //10
                               "PRINCKANA"           => array(120 ,"協力校長氏名かな"   ), //11
                               "DISTRICTCD"          => array(2   ,"地区コード"           ), //12
                               "PARTNER_SCHOOL_PREF_CD"   => array(2   ,"都道府県"             ), //13
                               "PARTNER_SCHOOL_ZIPCD"     => array(8   ,"協力校郵便番号"     ), //14
                               "PARTNER_SCHOOL_ADDR1"     => array(90  ,"協力校住所1"        ), //15
                               "PARTNER_SCHOOL_ADDR2"     => array(90  ,"協力校住所2"        ), //16
                               "PARTNER_SCHOOL_TELNO"     => array(14  ,"協力校電話番号"     ), //17
                               "PARTNER_SCHOOL_FAXNO"     => array(14  ,"協力校FAX番号"      ), //18
                               "EDBOARDCD"           => array(6   ,"教育委員会コード"     ), //19
                               "TOUHAIKOU"           => array(1   ,"統廃合フラグ"         )  //20
                               );

            if ($this->Properties["useLocationMst"] == '1') {
                $checkdata["DISTRICT_NAME"] = array(5  ,"所在地コード"         ); //21
            }

            if ($this->Properties["useMiraicompass"] == '1') {
                $checkdata["MIRAI_FS_CD"] = array(10  ,"ミライコンパス学校コード"         ); //22
            }

            $checkdata["INJI_FLG"] = array(1  ,"「卒業」印字しない"         ); //23

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
            $primaryKey2 = array();
            $primaryKey3 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $primaryKey1 = $this->getPrimaryKey('PARTNER_SCHOOL_MST');
            $primaryKey2 = $this->getPrimaryKey('PARTNER_SCHOOL_DETAIL_MST');
            if ($this->Properties["useMiraicompass"] == '1') {
                $primaryKey3 = $this->getPrimaryKey('ENTEXAM_MIRAI_FS_REP_DAT');
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
                    $data = array("PARTNER_SCHOOLCD"         => (strlen(trim($tmp[0]))) ? sprintf("%0".$this->partner_schoolcdKeta."d", $tmp[0]) : "",
                                  "PARTNER_SCHOOL_TYPE"      => $tmp[1],
                                  "PARTNER_SCHOOL_DISTCD"    => $tmp[2],
                                  "PARTNER_SCHOOL_DISTCD2"   => $tmp[3],
                                  "PARTNER_SCHOOL_DIV"       => $tmp[4],
                                  "PARTNER_SCHOOL_NAME"      => $tmp[5],
                                  "PARTNER_SCHOOL_KANA"      => $tmp[6],
                                  "PARTNER_SCHOOL_NAME_ABBV" => $tmp[7],
                                  "PARTNER_SCHOOL_KANA_ABBV" => $tmp[8],
                                  "PRINCNAME"           => $tmp[9],
                                  "PRINCNAME_SHOW"      => $tmp[10],
                                  "PRINCKANA"           => $tmp[11],
                                  "DISTRICTCD"          => $tmp[12],
                                  "PARTNER_SCHOOL_PREF_CD"   => (strlen(trim($tmp[13]))) ? sprintf("%02d", $tmp[13]) : "",
                                  "PARTNER_SCHOOL_ZIPCD"     => $tmp[14],
                                  "PARTNER_SCHOOL_ADDR1"     => $tmp[15],
                                  "PARTNER_SCHOOL_ADDR2"     => $tmp[16],
                                  "PARTNER_SCHOOL_TELNO"     => $tmp[17],
                                  "PARTNER_SCHOOL_FAXNO"     => $tmp[18],
                                  "EDBOARDCD"           => $tmp[19],
                                  "TOUHAIKOU"           => $tmp[20]
                                  );

                    if ($this->Properties["useLocationMst"] == '1') {
                        $data["DISTRICT_NAME"] = (strlen(trim($tmp[21]))) ? sprintf("%05d", $tmp[21]) : "";
                        if ($this->Properties["useMiraicompass"] == '1') {
                            $data["MIRAI_FS_CD"] = (strlen(trim($tmp[22]))) ? sprintf("%010s", $tmp[22]) : "";
                            $data["INJI_FLG"] = $tmp[23];
                        } else {
                            $data["INJI_FLG"] = $tmp[22];
                        }
                    } else {
                        if ($this->Properties["useMiraicompass"] == '1') {
                            $data["MIRAI_FS_CD"] = (strlen(trim($tmp[21]))) ? sprintf("%010s", $tmp[21]) : "";
                            $data["INJI_FLG"] = $tmp[22];
                        } else {
                            $data["INJI_FLG"] = $tmp[21];
                        }
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;  //必須エラーフラグ
        $masterErrFlg       = false;  //名称マスターにないエラーフラグ
        $lengthErrFlg       = false;  //レングスエラーフラグ
        $valuesErrFlg       = false;  //校種エラーフラグ
        $duplicateCsvErrFlg = false;  //重複エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $masterErrMsg       = "";     //名称マスターにないエラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $valuesErrMsg       = "";     //校種エラーメッセージ
        $duplicateCsvErrMsg = "";     //重複エラーメッセージ

        $hissuSep        = "";
        $masterSep       = "";
        $lengthSep       = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";

        $name_mst_codeArray = array("PARTNER_SCHOOL_TYPE"     => "L019",
                                    "PARTNER_SCHOOL_DISTCD"   => "L001",
                                    "PARTNER_SCHOOL_DISTCD2"  => "Z015",
                                    "PARTNER_SCHOOL_DIV"      => "L015",
                                    "DISTRICTCD"         => "Z003",
                                    );

        if ($this->selectPartnerSchoolType == "99" || $this->selectPartnerSchoolType == $data["PARTNER_SCHOOL_TYPE"]) {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
            $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
            if ($dupFlg3[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
            }
        }
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("PARTNER_SCHOOLCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //校種チェック
            if (in_array($key, array("PARTNER_SCHOOL_TYPE"))) {
                if ($this->selectPartnerSchoolType != "99" && $data[$key] != $this->selectPartnerSchoolType) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }

            //名称マスターに存在するかチェック
            if (in_array($key, array("PARTNER_SCHOOL_TYPE","PARTNER_SCHOOL_DISTCD","PARTNER_SCHOOL_DISTCD2","PARTNER_SCHOOL_DIV","DISTRICTCD","PARTNER_SCHOOL_PREF_CD","EDBOARDCD","DISTRICT_NAME"))) {
                if ($data[$key] != '') {
                    if ($key == "EDBOARDCD") {
                        $query = knjz095_2Query::checkEdboardcd($data[$key]);
                    } elseif ($key == "PARTNER_SCHOOL_PREF_CD") {
                        $query = knjz095_2Query::checkPrefcd($data[$key]);
                    } elseif ($key == "DISTRICT_NAME") {
                        if ($this->Properties["useLocationMst"] == '1') {
                            $query = knjz095_2Query::checkDistrictcd($data[$key]);
                        }
                    } else {
                        $query = knjz095_2Query::checkNameMst($name_mst_codeArray["$key"], $data[$key]);
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

            //フラグチェック(値は1かNULL)
            if (in_array($key, array("TOUHAIKOU"))) {
                if ($data[$key] > 1) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }

            //フラグチェック(値は1かNULL)
            if (in_array($key, array("INJI_FLG"))) {
                if ($data[$key] > 1) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$valuesErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz095_2Query::insertQueryErr($db, $line, $checkError);
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


    //入力項目チェック
    public function validateInput()
    {
        //キーのチェック
        if (trim($this->field["PARTNER_SCHOOLCD"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901', "( 学校コード )");
            return false;
        }
        if ($this->partner_schoolcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->partner_schoolcd != $this->field["PARTNER_SCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["PARTNER_SCHOOLCD"]) < 7) {
            $this->field["PARTNER_SCHOOLCD"] = sprintf("%07d", $this->field["PARTNER_SCHOOLCD"]);
        }

        //DB接続
        $db = Query::dbCheckOut();

        //１レコード取得
        $row = $db->getRow(knjz095_2Query::getScoolData($this->field["PARTNER_SCHOOLCD"], $this), DB_FETCHMODE_ASSOC);

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
        //文字型チェック
        foreach (
                array("PARTNER_SCHOOL_TELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                      "PARTNER_SCHOOL_FAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }
        if (strlen($this->field["PARTNER_SCHOOLCD"]) > $this->partner_schoolcdKeta) {
            $this->setWarning('MSG901', "( 学校コード ) ".$this->partner_schoolcdKeta."バイトまで");
            return false;
        } elseif (strlen($this->field["PARTNER_SCHOOL_NAME"])>75) {
            $this->setWarning('MSG901', "( 学校名 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PARTNER_SCHOOL_KANA"])>75) {
            $this->setWarning('MSG901', "( 学校名かな) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PARTNER_SCHOOL_NAME_ABBV"])>30) {
            $this->setWarning('MSG901', "( 学校名略称 ) 30バイトまで");
            return false;
        } elseif (strlen($this->field["PARTNER_SCHOOL_KANA_ABBV"])>75) {
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
        } elseif (strlen($this->field["PARTNER_SCHOOL_ZIPCD"])>8) {
            $this->setWarning('MSG901', "( 郵便番号 ) 8バイトまで");
            return false;
        } elseif (strlen($this->field["PARTNER_SCHOOL_ADDR1"])>90) {
            $this->setWarning('MSG901', "( 住所 ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["PARTNER_SCHOOL_ADDR2"])>90) {
            $this->setWarning('MSG901', "( 方書き(アパート名等) ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["PARTNER_SCHOOL_TELNO"])>14) {
            $this->setWarning('MSG901', "( 電話番号 ) 14バイトまで");
            return false;
        } elseif (strlen($this->field["PARTNER_SCHOOL_FAXNO"])>14) {
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
            $this->partner_schoolcd = $this->field["PARTNER_SCHOOLCD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    public function &getDeleteModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //キーの更新チェック
        if ($this->partner_schoolcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->partner_schoolcd != $this->field["PARTNER_SCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }

        $row = $db->getRow(knjz095_2Query::getScoolData($this->partner_schoolcd, $this), DB_FETCHMODE_ASSOC);
        if (!is_array($row)) {
            $this->setWarning('MSG303');
            return;
        }

        //年度データの存在チェック
        if (knjz095_2Query::isExisting($db, $this->partner_schoolcd)) {
            $this->setWarning('MSG919', "年度データに登録されています。");
            return;
        }

        //削除
        knjz095_2Query::getDeleteQuery($db, $this->field, $this);
        $this->setMessage('MSG201');

        //DB切断
        Query::dbCheckIn($db);
    }

    //追加
    public function &insert()
    {
        knjz095_2Query::getInsertQuery($this->field, $this);
    }

    //更新
    public function &update()
    {
        knjz095_2Query::getUpdateQuery($this->field, $this);
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
