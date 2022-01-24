<?php

require_once('for_php7.php');
class knjz090_2aModel extends Model
{
    public $field = array();
    public $cmd;
    public $finschoolcd;        //primary
    public $selectFinschoolType;
    public $file;
    public $lastColumn;//CSVの最後の列の値
    public $finschoolStaffcdKeta;

    /**
     * メンバ変数の初期化
     */
    public function knjz090_2aModel()
    {
    }

    public function init()
    {
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7);
        $this->finschoolStaffcdKeta = ($this->Properties["useStaffcdFieldSize"] == '10' ? 10 : 8);

        $this->field = array("FINSCHOOLCD"         => (strlen(trim(VARS::post("FINSCHOOLCD")))) ? sprintf("%0".$this->finschoolcdKeta."d", VARS::post("FINSCHOOLCD")) : "",         //学校コード
                             "FINSCHOOL_TYPE"      => VARS::post("FINSCHOOL_TYPE"),      //校種
                             "FINSCHOOL_DISTCD"    => VARS::post("FINSCHOOL_DISTCD"),    //地区コード
                             "FINSCHOOL_DISTCD2"   => VARS::post("FINSCHOOL_DISTCD2"),   //学区コード
                             "FINSCHOOL_DIV"       => VARS::post("FINSCHOOL_DIV"),       //学校立コード
                             "FINSCHOOL_NAME"      => VARS::post("FINSCHOOL_NAME"),      //学校名
                             "FINSCHOOL_KANA"      => VARS::post("FINSCHOOL_KANA"),      //学校名かな
                             "FINSCHOOL_NAME_ABBV" => VARS::post("FINSCHOOL_NAME_ABBV"), //学校名略称
                             "FINSCHOOL_KANA_ABBV" => VARS::post("FINSCHOOL_KANA_ABBV"), //学校名かな略称
                             "PRINCNAME"           => VARS::post("PRINCNAME"),           //学校長氏名
                             "PRINCNAME_SHOW"      => VARS::post("PRINCNAME_SHOW"),      //学校長氏名表示用
                             "PRINCKANA"           => VARS::post("PRINCKANA"),           //学校長氏名かな
                             "FINSCHOOL_STAFFCD"   => VARS::post("FINSCHOOL_STAFFCD"),   //担当者
                             "DISTRICTCD"          => VARS::post("DISTRICTCD"),          //地区コード
                             "FINSCHOOL_PREF_CD"   => VARS::post("FINSCHOOL_PREF_CD"),   //都道府県
                             "FINSCHOOL_CITY_CD"   => VARS::post("FINSCHOOL_CITY_CD"),   //市区町村
                             "FINSCHOOL_ZIPCD"     => VARS::post("FINSCHOOL_ZIPCD"),     //郵便番号
                             "FINSCHOOL_ADDR1"     => VARS::post("FINSCHOOL_ADDR1"),     //住所
                             "FINSCHOOL_ADDR2"     => VARS::post("FINSCHOOL_ADDR2"),     //住所
                             "FINSCHOOL_TELNO"     => VARS::post("FINSCHOOL_TELNO"),     //電話番号
                             "FINSCHOOL_FAXNO"     => VARS::post("FINSCHOOL_FAXNO"),     //FAX番号
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
            $this->selectFinschoolType = VARS::post("SELECT_FINSCHOOL_TYPE");
        }

        $this->file = VARS::file("FILE");

        if (VARS::get("mode") == "1") {
            unset($this->finschoolcd);
            unset($this->selectFinschoolType);
        }
        $temp = VARS::get("FINSCHOOLCD");
        if (isset($temp)) {
            $this->finschoolcd  = VARS::get("FINSCHOOLCD");
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
        if ($this->selectFinschoolType == "" && ($this->field["OUTPUT"] == "1" || $this->field["OUTPUT"] == "2")) {
            $this->setWarning("左画面より校種を選択してください。");
            return false;
        }
        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $headerDuplicate = array("FINSCHOOLCD"   => "出身学校コード",
                                     "FINSCHOOL_SEQ" => "出身学校SEQ",
                                     "MIRAI_FS_CD"   => "ミライコンパス学校コード");
            $tables = array();
            if ($this->Properties["useMiraicompass"] == '1') {
                $tables = array("1" => "FINSCHOOL_MST", "2" => "FINSCHOOL_DETAIL_MST", "3" => "ENTEXAM_MIRAI_FS_REP_DAT");
            } else {
                $tables = array("1" => "FINSCHOOL_MST", "2" => "FINSCHOOL_DETAIL_MST");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header) . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出
            $header = array("※出身学校コード",     //0   (7)
                            "校種",                 //1   (1)
                            "地区コード",           //2   (3)
                            "学区コード",           //3   (3)
                            "学校立コード",         //4   (1)
                            "出身学校名",           //5   (75)
                            "出身学校名かな",       //6   (75)
                            "出身学校略称名",       //7   (30)
                            "出身学校名かな略称",   //8   (75)
                            "担当者コード",         //9   (10)
                            "担当者名",             //10  (60)
                            "出身学校長氏名",       //11   (60)
                            "出身学校長氏名表示用", //12  (30)
                            "出身学校長氏名かな",   //13  (120)
                            "地区コード",           //14  (2)
                            "地区名",              //15  (60)
                            "都道府県コード",       //16  (2)
                            "都道府県名",           //17  (12)
                            "市区町村コード",       //18  (3)
                            "市区町村名",           //19  (120)
                            "出身学校郵便番号",     //20  (8)
                            "出身学校住所1",        //21  (75)
                            "出身学校住所2",        //22  (75)
                            "出身学校電話番号",     //23  (14)
                            "出身学校FAX番号",      //24  (14)
                            "教育委員会コード",     //25  (6)
                            "統廃合フラグ"          //26  (1)
                            );
            if ($this->Properties["useLocationMst"] == '1') {
                $header[] = "所在地コード"; //27  (5)
            }

            if ($this->Properties["useMiraicompass"] == '1') {
                $header[] = "ミライコンパス学校コード"; //28  (10)
            }
            $header[] = "「卒業」印字しない"; //29  (1)
            $header[] = $this->lastColumn;
            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjz090_2aQuery::getCsvData($this);
        }

        if ($this->field["OUTPUT"] == "4") {
            if ($this->finschoolcdKeta == 12) {
                $contents .= "123456789012,";       // FINSCHOOLCD         出身学校コード
            } else {
                $contents .= "1234567,";       // FINSCHOOLCD         出身学校コード
            }
            $contents .= "1,";             // FINSCHOOL_TYPE      校種
            $contents .= "1234,";          // FINSCHOOL_DISTCD    地区コード
            $contents .= "01,";            // FINSCHOOL_DISTCD2   学区コード
            $contents .= "1,";             // FINSCHOOL_DIV       学校立コード
            $contents .= "全角25文字,";    // FINSCHOOL_NAME      出身学校名
            $contents .= "全角25文字,";    // FINSCHOOL_KANA      出身学校名かな
            $contents .= "全角10文字,";    // FINSCHOOL_NAME_ABBV 出身学校略称名
            $contents .= "全角25文字,";    // FINSCHOOL_KANA_ABBV 出身学校名かな略称
            if ($this->finschoolStaffcdKeta == 10) {
                $contents .= "1234567890,";    // FINSCHOOL_STAFFCD   担当者コード
            } else {
                $contents .= "12345678,";      // FINSCHOOL_STAFFCD   担当者コード
            }
            $contents .= "全角20文字,";    // FINSCHOOL_STAFF_NAME     担当者名
            $contents .= "全角20文字,";    // PRINCNAME                出身学校長氏名
            $contents .= "全角10文字,";    // PRINCNAME_SHOW           出身学校長氏名表示用
            $contents .= "全角40文字,";    // PRINCKANA                出身学校長氏名かな
            $contents .= "01,";            // DISTRICTCD              地区コード
            $contents .= "全角20文字,";     // FINSCHOOL_DISTRICT_NAME 地区名
            $contents .= "01,";            // FINSCHOOL_PREF_CD       都道府県コード
            $contents .= "全角4文字,";      // FINSCHOOL_PREF_NAME     都道府県名
            $contents .= "123,";           // FINSCHOOL_CITY_CD       市区町村コード
            $contents .= "全角40文字,";     // FINSCHOOL_CITY_NAME     市区町村名
            $contents .= "123-4567,";      // FINSCHOOL_ZIPCD         出身学校郵便番号
            $contents .= "全角30文字,";    // FINSCHOOL_ADDR1          出身学校住所1
            $contents .= "全角30文字,";    // FINSCHOOL_ADDR2          出身学校住所2
            $contents .= "03-1234-5678,";  // FINSCHOOL_TELNO         出身学校電話番号
            $contents .= "03-1234-5678,";  // FINSCHOOL_FAXNO         出身学校FAX番号
            $contents .= "123456,";        // EDBOARDCD               教育委員会コード
            $contents .= "1,";             // TOUHAIKOU               統廃合フラグ
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
                $contents .= implode(",", $row) ."\n";
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
        if ($this->selectFinschoolType == "" && ($this->field["OUTPUT"] == "1" || $this->field["OUTPUT"] == "2")) {
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
                knjz090_2aQuery::insertQueryCsv($db, $this, $this->data_arr);
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
            $checkdata = array("FINSCHOOLCD"         => array($this->finschoolcdKeta ,"出身学校コード"), //0
                               "FINSCHOOL_TYPE"      => array(1   ,"校種"                 ), //1
                               "FINSCHOOL_DISTCD"    => array(4   ,"地区コード"           ), //2
                               "FINSCHOOL_DISTCD2"   => array(3   ,"学区コード"           ), //3
                               "FINSCHOOL_DIV"       => array(1   ,"学校立コード"         ), //4
                               "FINSCHOOL_NAME"      => array(75  ,"出身学校名"           ), //5
                               "FINSCHOOL_KANA"      => array(75  ,"出身学校名かな"       ), //6
                               "FINSCHOOL_NAME_ABBV" => array(30  ,"出身学校略称名"       ), //7
                               "FINSCHOOL_KANA_ABBV" => array(75  ,"出身学校名かな略称"   ), //8
                               "PRINCNAME"           => array(60  ,"出身学校長氏名"       ), //9
                               "PRINCNAME_SHOW"      => array(30  ,"出身学校長氏名表示用" ), //10
                               "PRINCKANA"           => array(120 ,"出身学校長氏名かな"   ), //11
                               "FINSCHOOL_STAFFCD"   => array($this->finschoolStaffcdKeta ,"担当者コード"), //12
                               "DISTRICTCD"          => array(2   ,"地区コード"           ), //13
                               "FINSCHOOL_PREF_CD"   => array(2   ,"都道府県コード"       ), //14
                               "FINSCHOOL_CITY_CD"   => array(3   ,"市区町村コード"       ), //15
                               "FINSCHOOL_ZIPCD"     => array(8   ,"出身学校郵便番号"     ), //16
                               "FINSCHOOL_ADDR1"     => array(90  ,"出身学校住所1"        ), //17
                               "FINSCHOOL_ADDR2"     => array(90  ,"出身学校住所2"        ), //18
                               "FINSCHOOL_TELNO"     => array(14  ,"出身学校電話番号"     ), //19
                               "FINSCHOOL_FAXNO"     => array(14  ,"出身学校FAX番号"      ), //20
                               "EDBOARDCD"           => array(6   ,"教育委員会コード"     ), //21
                               "TOUHAIKOU"           => array(1   ,"統廃合フラグ"         )  //22
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
            $primaryKey1 = $this->getPrimaryKey("FINSCHOOL_MST");
            $primaryKey2 = $this->getPrimaryKey("FINSCHOOL_DETAIL_MST");
            if ($this->Properties["useMiraicompass"] == '1') {
                $primaryKey3 = $this->getPrimaryKey("ENTEXAM_MIRAI_FS_REP_DAT");
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
                    $data = array("FINSCHOOLCD"             => (strlen(trim($tmp[0]))) ? sprintf("%0".$this->finschoolcdKeta."d", $tmp[0]) : "",
                                  "FINSCHOOL_TYPE"          => $tmp[1],
                                  "FINSCHOOL_DISTCD"        => $tmp[2],
                                  "FINSCHOOL_DISTCD2"       => $tmp[3],
                                  "FINSCHOOL_DIV"           => $tmp[4],
                                  "FINSCHOOL_NAME"          => $tmp[5],
                                  "FINSCHOOL_KANA"          => $tmp[6],
                                  "FINSCHOOL_NAME_ABBV"     => $tmp[7],
                                  "FINSCHOOL_KANA_ABBV"     => $tmp[8],
                                  "FINSCHOOL_STAFFCD"       => (strlen(trim($tmp[9]))) ? sprintf("%0".$this->finschoolStaffcdKeta."d", $tmp[9]) : "",
                                  "FINSCHOOL_STAFF_NAME"    => $tmp[10],
                                  "PRINCNAME"               => $tmp[11],
                                  "PRINCNAME_SHOW"          => $tmp[12],
                                  "PRINCKANA"               => $tmp[13],
                                  "DISTRICTCD"              => $tmp[14],
                                  "FINSCHOOL_DISTRICT_NAME" => $tmp[15],
                                  "FINSCHOOL_PREF_CD"       => (strlen(trim($tmp[16]))) ? sprintf("%02d", $tmp[16]) : "",
                                  "FINSCHOOL_PREF_NAME"     => $tmp[17],
                                  "FINSCHOOL_CITY_CD"       => (strlen(trim($tmp[18]))) ? sprintf("%03d", $tmp[18]) : "", //18
                                  "FINSCHOOL_CITY_NAME"     => $tmp[19],
                                  "FINSCHOOL_ZIPCD"         => $tmp[20],
                                  "FINSCHOOL_ADDR1"         => $tmp[21],
                                  "FINSCHOOL_ADDR2"         => $tmp[22],
                                  "FINSCHOOL_TELNO"         => $tmp[23],
                                  "FINSCHOOL_FAXNO"         => $tmp[24],
                                  "EDBOARDCD"               => $tmp[25],
                                  "TOUHAIKOU"               => $tmp[26],
                                  "FINSCHOOL_SEQ"           => ""
                                  );

                    if ($this->Properties["useLocationMst"] == '1') {
                        $data["DISTRICT_NAME"] = (strlen(trim($tmp[27]))) ? sprintf("%05d", $tmp[27]) : "";
                        if ($this->Properties["useMiraicompass"] == '1') {
                            $data["MIRAI_FS_CD"] = (strlen(trim($tmp[28]))) ? sprintf("%010s", $tmp[28]) : "";
                            $data["INJI_FLG"] = $tmp[29];
                        } else {
                            $data["INJI_FLG"] = $tmp[28];
                        }
                    } else {
                        if ($this->Properties["useMiraicompass"] == '1') {
                            $data["MIRAI_FS_CD"] = (strlen(trim($tmp[27]))) ? sprintf("%010s", $tmp[27]) : "";
                            $data["INJI_FLG"] = $tmp[28];
                        } else {
                            $data["INJI_FLG"] = $tmp[27];
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
        $returnFlg    = true;
        $hissuErrFlg  = false;  //必須エラーフラグ
        $masterErrFlg = false;  //名称マスターにないエラーフラグ
        $lengthErrFlg = false;  //レングスエラーフラグ
        $valuesErrFlg = false;
        $duplicateCsvErrFlg = false;

        $hissuErrMsg  = "";     //必須エラーメッセージ
        $masterErrMsg = "";     //名称マスターにないエラーメッセージ
        $lengthErrMsg = "";     //レングスエラーメッセージ
        $valuesErrMsg = "";     //その他エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep  = "";
        $masterSep = "";
        $lengthSep = "";
        $valuesSep = "";
        $duplicateCsvSep = "";

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

        $name_mst_codeArray = array("FINSCHOOL_TYPE"     => "L019",
                                    "FINSCHOOL_DISTCD"   => "L001",
                                    "FINSCHOOL_DISTCD2"  => "Z015",
                                    "FINSCHOOL_DIV"      => "L015",
                                    "DISTRICTCD"         => "Z003",
                                    );

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("FINSCHOOLCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //校種チェック
            if (in_array($key, array("FINSCHOOL_TYPE"))) {
                if ($this->selectFinschoolType != "99" && $data[$key] != $this->selectFinschoolType) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }

            //名称マスターに存在するかチェック
            if (in_array($key, array("FINSCHOOL_TYPE","FINSCHOOL_DISTCD","FINSCHOOL_DISTCD2","FINSCHOOL_DIV","FINSCHOOL_STAFFCD","DISTRICTCD","FINSCHOOL_PREF_CD","FINSCHOOL_CITY_CD","EDBOARDCD","DISTRICT_NAME"))) {
                if ($data[$key] != '') {
                    if ($key == "EDBOARDCD") {
                        $query = knjz090_2aQuery::checkEdboardcd($data[$key]);
                    } elseif ($key == "FINSCHOOL_PREF_CD") {
                        $query = knjz090_2aQuery::checkPrefcd($data[$key]);
                    } elseif ($key == "FINSCHOOL_CITY_CD") {
                        $query = knjz090_2aQuery::checkCitycd($data["FINSCHOOL_PREF_CD"], $data["FINSCHOOL_CITY_CD"]);
                    } elseif ($key == "FINSCHOOL_STAFFCD") {
                        $query = knjz090_2aQuery::checkStaffcd($data[$key]);
                    } elseif ($key == "DISTRICT_NAME") {
                        if ($this->Properties["useLocationMst"] == '1') {
                            $query = knjz090_2aQuery::checkDistrictcd($data[$key]);
                        }
                    } else {
                        $query = knjz090_2aQuery::checkNameMst($name_mst_codeArray["$key"], $data[$key]);
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
            knjz090_2aQuery::insertQueryErr($db, $line, $checkError);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning('MSG915', "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
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
        $row = $db->getRow(knjz090_2aQuery::getScoolData($this->field["FINSCHOOLCD"], $this), DB_FETCHMODE_ASSOC);

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
            array("FINSCHOOL_TELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                      "FINSCHOOL_FAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                 ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }
        if (strlen($this->field["FINSCHOOLCD"]) > $this->finschoolcdKeta) {
            $this->setWarning('MSG901', "( 学校コード ) ".$this->finschoolcdKeta."バイトまで");
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
        } elseif (strlen($this->field["FINSCHOOL_ADDR1"])>90) {
            $this->setWarning('MSG901', "( 住所 ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_ADDR2"])>90) {
            $this->setWarning('MSG901', "( 方書き(アパート名等) ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["FINSCHOOL_TELNO"])>14) {
            $this->setWarning('MSG901', "( 電話番号 ) 14バイトまで");
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

        $row = $db->getRow(knjz090_2aQuery::getScoolData($this->finschoolcd, $this), DB_FETCHMODE_ASSOC);
        if (!is_array($row)) {
            $this->setWarning('MSG303');
            return;
        }

        //年度データの存在チェック
        if (knjz090_2aQuery::isExisting($db, $this->finschoolcd)) {
            $this->setWarning('MSG919', "年度データに登録されています。");
            return;
        }

        //削除
        knjz090_2aQuery::getDeleteQuery($db, $this->field, $this);
        $this->setMessage('MSG201');

        //DB切断
        Query::dbCheckIn($db);
    }

    //追加
    public function &insert()
    {
        knjz090_2aQuery::getInsertQuery($this->field, $this);
    }

    //更新
    public function &update()
    {
        knjz090_2aQuery::getUpdateQuery($this->field, $this);
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
