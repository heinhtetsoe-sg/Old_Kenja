<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx011Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx011Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
    }
    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "STAFFCD"          =>  VARS::post("STAFFCD"),          //職員コード
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
        $db = Query::dbCheckOut();
        $query = knjX011Query::getDataBaseInfo();
        $this->dataBaseinfo = $db->getOne($query); //教育委員会情報
        Query::dbCheckIn($db);

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    /**
     * データ取込
    */
    public function &getExecModel()
    {
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();

        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                //更新
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    $table = "staff_mst";
                    $where = "where staffcd = '".$this->dataArr[$i]["STAFFCD"]."'";
                    $query = knjx011Query::getMasterSQL($table, $where);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($flg) {   //ＤＢにあれば、update
                            knjx011Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx011Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        }
                        //STAFF_DETAIL_MSTへの更新
                        knjx011Query::getUpdateDetailQuery($db, $this->dataArr[$i], $this->staffcd, $this);
                    //削除
                    } else {
                        if ($this->dataBaseinfo === '2') {
                            $db2 = Query::dbCheckOut2();
                            $db2->autoCommit(false);
                        }
                        if ($flg) {   //ＤＢにあれば、delete
                            knjx011Query::deleteQueryCsv($db, $this->dataArr[$i], $db2, $this);
                        }
                        if ($this->dataBaseinfo === '2') {
                            $db2->commit(); // トランザクションをコミットする。
                            Query::dbCheckIn($db2);
                        }
                    }
                }
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //更新(データ取込)
    public function fileUpload()
    {
        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            //STAFFCDフィールドサイズ変更対応
            if ($this->Properties["useStaffcdFieldSize"] === '10') {
                $setsize = 10;
            } else {
                $setsize = 8;
            }
            if ($this->field["SHORI_MEI"] == "1") {
                $checkdata = array( "STAFFCD"               => array($setsize, "職員コード"),
                                    "STAFFNAME"             => array(60, "職員氏名"),
                                    "STAFFNAME_SHOW"        => array(15, "職員氏名表示用"),
                                    "STAFFNAME_KANA"        => array(120, "職員氏名かな"),
                                    "STAFFNAME_ENG"         => array(60, "職員氏名英字"),
                                    "STAFFNAME_REAL"        => array(120, "戸籍氏名"),
                                    "STAFFNAME_KANA_REAL"   => array(240, "戸籍氏名かな"),
                                    "JOBCD"                 => array(4, "職名コード"),
                                    "SECTIONCD"             => array(4, "所属コード"),
                                    "DUTYSHARECD"           => array(4, "校務分掌部コード"),
                                    "CHARGECLASSCD"         => array(1, "授業受持区分"),
                                    "STAFFSEX"              => array(1, "職員性別"),
                                    "STAFFBIRTHDAY"         => array(10, "職員生年月日"),
                                    "STAFFZIPCD"            => array(5, "職員郵便番号"),
                                    "STAFFADDR1"            => array(90, "職員住所1"),
                                    "STAFFADDR2"            => array(90, "職員住所2"),
                                    "STAFFTELNO"            => array(14, "職員電話番号"),
                                    "STAFFFAXNO"            => array(14, "職員FAX番号"),
                                    "STAFFE_MAIL"           => array(50, "職員メールアドレス"),
                                    "YEAR"                  => array(4, "年度"),
                                    "POSITIONCD1"           => array(4, "肩書き1(教務主任等コード)"),
                                    "POSITIONCD1_MANAGER1"  => array(2, "学年主任(学年)または教科主任(教科)"),
                                    "POSITIONCD1_MANAGER2"  => array(1, "教科主任(学校校種)"),
                                    "POSITIONCD2"           => array(4, "肩書き2(教務主任等コード)"),
                                    "POSITIONCD2_MANAGER1"  => array(2, "学年主任(学年)または教科主任(教科)"),
                                    "POSITIONCD2_MANAGER2"  => array(1, "教科主任(学校校種)"),
                                    "POSITIONCD3"           => array(4, "肩書き3(教務主任等コード)"),
                                    "POSITIONCD3_MANAGER1"  => array(2, "学年主任(学年)または教科主任(教科)"),
                                    "POSITIONCD3_MANAGER2"  => array(1, "教科主任(学校校種)"));
            } else {
                $checkdata = array( "STAFFCD"               => array($setsize, "職員コード"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;         //行番号(初期値)

            //取得主キー
            $primaryKey1    = array();
            $duplicateCsv1  = array();
            $primaryKey2    = array();
            $duplicateCsv2  = array();
            $primaryKey1    = $this->getPrimaryKey('STAFF_MST');
            $primaryKey2    = $this->getPrimaryKey('STAFF_DETAIL_MST');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    //教育課程対応
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($this->Properties["useCurriculumcd"] == '1') {
                            $data = array("STAFFCD"             => trim($tmp[0]),
                                          "STAFFNAME"           => trim($tmp[1]),
                                          "STAFFNAME_SHOW"      => trim($tmp[2]),
                                          "STAFFNAME_KANA"      => trim($tmp[3]),
                                          "STAFFNAME_ENG"       => trim($tmp[4]),
                                          "STAFFNAME_REAL"      => trim($tmp[5]),
                                          "STAFFNAME_KANA_REAL" => trim($tmp[6]),
                                          "JOBCD"               => trim($tmp[7]),
                                          "SECTIONCD"           => trim($tmp[8]),
                                          "DUTYSHARECD"         => trim($tmp[9]),
                                          "CHARGECLASSCD"       => trim($tmp[10]),
                                          "STAFFSEX"            => trim($tmp[11]),
                                          "STAFFBIRTHDAY"       => trim($tmp[12]),
                                          "STAFFZIPCD"          => trim($tmp[13]),
                                          "STAFFADDR1"          => trim($tmp[14]),
                                          "STAFFADDR2"          => trim($tmp[15]),
                                          "STAFFTELNO"          => trim($tmp[16]),
                                          "STAFFFAXNO"          => trim($tmp[17]),
                                          "STAFFE_MAIL"         => trim($tmp[18]),
                                          "YEAR"                => trim($tmp[19]),
                                          "POSITIONCD1"            => trim($tmp[20]),
                                          "POSITIONCD1_MANAGER1"   => trim($tmp[21]),
                                          "POSITIONCD1_MANAGER2"   => trim($tmp[22]),
                                          "POSITIONCD2"            => trim($tmp[23]),
                                          "POSITIONCD2_MANAGER1"   => trim($tmp[24]),
                                          "POSITIONCD2_MANAGER2"   => trim($tmp[25]),
                                          "POSITIONCD3"            => trim($tmp[26]),
                                          "POSITIONCD3_MANAGER1"   => trim($tmp[27]),
                                          "POSITIONCD3_MANAGER2"   => trim($tmp[28]),
                                          "STAFF_SEQ"              => ""
                                          );
                        } else {
                            $data = array("STAFFCD"             => trim($tmp[0]),
                                          "STAFFNAME"           => trim($tmp[1]),
                                          "STAFFNAME_SHOW"      => trim($tmp[2]),
                                          "STAFFNAME_KANA"      => trim($tmp[3]),
                                          "STAFFNAME_ENG"       => trim($tmp[4]),
                                          "STAFFNAME_REAL"      => trim($tmp[5]),
                                          "STAFFNAME_KANA_REAL" => trim($tmp[6]),
                                          "JOBCD"               => trim($tmp[7]),
                                          "SECTIONCD"           => trim($tmp[8]),
                                          "DUTYSHARECD"         => trim($tmp[9]),
                                          "CHARGECLASSCD"       => trim($tmp[10]),
                                          "STAFFSEX"            => trim($tmp[11]),
                                          "STAFFBIRTHDAY"       => trim($tmp[12]),
                                          "STAFFZIPCD"          => trim($tmp[13]),
                                          "STAFFADDR1"          => trim($tmp[14]),
                                          "STAFFADDR2"          => trim($tmp[15]),
                                          "STAFFTELNO"          => trim($tmp[16]),
                                          "STAFFFAXNO"          => trim($tmp[17]),
                                          "STAFFE_MAIL"         => trim($tmp[18]),
                                          "YEAR"                => trim($tmp[19]),
                                          "POSITIONCD1"            => trim($tmp[20]),
                                          "POSITIONCD1_MANAGER1"   => trim($tmp[21]),
                                          "POSITIONCD2"            => trim($tmp[22]),
                                          "POSITIONCD2_MANAGER1"   => trim($tmp[23]),
                                          "POSITIONCD3"            => trim($tmp[24]),
                                          "POSITIONCD3_MANAGER1"   => trim($tmp[25]),
                                          "STAFF_SEQ"              => ""
                                          );
                        }
                        $data["JOBCD"]          = $this->zeroUme($data["JOBCD"], 4);
                        $data["SECTIONCD"]      = $this->zeroUme($data["SECTIONCD"], 4);
                        $data["DUTYSHARECD"]    = $this->zeroUme($data["DUTYSHARECD"], 4);
                    } else {
                        $data = array("STAFFCD" => trim($tmp[0]));
                    }
                    //０埋め
                    $data["STAFFCD"]        = $this->zeroUme($data["STAFFCD"], $setsize);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }

                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$record_no][$val] = $data[$val];
                    }
                }
                $record_no++;   //行番号(カウント)
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;  //必須エラーフラグ
        $numericErrFlg      = false;  //数値エラーフラグ
        $lengthErrFlg       = false;  //レングスエラーフラグ
        $masterErrFlg       = false;  //存在エラーフラグ
        $duplicateCsvErrFlg = false;  //重複(CSV)エラーフラグ
        $valuesErrFlg       = false;  //値エラーフラグ
        $dateErrFlg         = false;  //日付エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $masterErrMsg       = "";     //存在エラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ
        $valuesErrMsg       = "";     //値エラーメッセージ
        $dateErrMsg         = "";     //日付エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";
        $valuesSep       = "";
        $dateSep         = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            if ($this->field["SHORI_MEI"] == "2") {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg1[1] ."行目)");
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複4(". $dupFlg1[1] ."行目)");
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複5(". $dupFlg1[1] ."行目)");
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複6(". $dupFlg1[1] ."行目)");
            }
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if (in_array($key, array("STAFFCD", "YEAR", "STAFFNAME"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                if (in_array($key, array("JOBCD","SECTIONCD","DUTYSHARECD","CHARGECLASSCD","STAFFSEX","YEAR","POSITIONCD1","POSITIONCD1_MANAGER1","POSITIONCD2","POSITIONCD2_MANAGER1","POSITIONCD3","POSITIONCD3_MANAGER1"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //値チェック
                if ($key == "CHARGECLASSCD" && $data[$key] != "" && !in_array($data["CHARGECLASSCD"], array("0","1"))) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }

                if ($key == "STAFFSEX" && $data[$key] != "" && !in_array($data["STAFFSEX"], array("1","2"))) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            } else {
                if (in_array($key, array("STAFFCD"))) {
                    //必須項目チェック
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }

                    //文字数チェック
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }
        }

        if ($returnFlg) {
            if ($this->field["SHORI_MEI"] == "1") {
                if ($this->Properties["useStaffcdFieldSize"] === '10') {
                    $setsize = 10;
                } else {
                    $setsize = 8;
                }
                if ($data["STAFFBIRTHDAY"] != "" && !$this->dateRep($data["STAFFBIRTHDAY"])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "職員生年月日");
                }
                //職名マスタ
                if ($data["JOBCD"] != "") {
                    $table = "job_mst";
                    $where = "where jobcd = '".$data["JOBCD"]."'";
                    $sonzai_flg = $db->getOne(knjx011Query::getMasterSQL($table, $where));
                    if ($sonzai_flg == null || $sonzai_flg == "") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "職名コード");
                    }
                }
                //所属マスタ
                if ($data["SECTIONCD"] != "") {
                    $table = "section_mst";
                    $where = "where sectioncd = '".$data["SECTIONCD"]."'";
                    $sonzai_flg = $db->getOne(knjx011Query::getMasterSQL($table, $where));
                    if ($sonzai_flg == null || $sonzai_flg == "") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "所属コード");
                    }
                }
                //校務分掌マスタ
                if ($data["DUTYSHARECD"] != "") {
                    $table = "dutyshare_mst";
                    $where = "where dutysharecd = '".$data["DUTYSHARECD"]."'";
                    $sonzai_flg = $db->getOne(knjx011Query::getMasterSQL($table, $where));
                    if ($sonzai_flg == null || $sonzai_flg == "") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "マスタチェック:", $masterSep, "校務分掌部コード");
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$masterErrMsg.$numericErrMsg.$valuesErrMsg.$lengthErrMsg.$dateErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx011Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶへエラー出力
    public function getDownloadModel()
    {
        $header = array("MSGROW"            => "行番号",
                        "MSGREMARK"         => "メッセージ");

        //DB接続
        $db = Query::dbCheckOut();
        if ($this->field["OUTPUT"] == "3") {
            $duplicateHeader = array();
            $tables = array();
            if ($this->field["SHORI_MEI"] == "1") {
                $duplicateHeader = array("STAFFCD"   => "職員コード",
                                         "YEAR"      => "年度",
                                         "STAFF_SEQ" => "職員SEQ");
                $tables = array("1" => "STAFF_MST", "2" => "STAFF_DETAIL_MST");
            } else {
                $duplicateHeader = array("STAFFCD"          => "職員コード",
                                         "YEAR"             => "年度",
                                         "STAFF_DIV"        => "教育委員会フラグ",
                                         "EDBOARD_SCHOOLCD" => "学校コード",
                                         "SCHOOLCD"         => "校種",
                                         "SCHOOL_KIND"      => "年度",
                                         "PASSWD"           => "利用者パスワード",
                                         "STAFF_SEQ"        => "職員SEQ");
                $tables = array("1" => "STAFF_MST", "2" => "STAFF_DETAIL_MST", "3" => "EDBOARD_STAFF_DAT", "4" => "STAFF_YDAT", "5" => "USER_MST", "6" => "USER_PWD_HIST_MST");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplicateHeader);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header, ",")."\n";
        }
        //SQL文発行
        $query = $this->selectCsvErrQuery(PROGRAMID);
        $result = $db->query($query);
        $err_data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $err_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($err_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ヘッダ出力
    public function getHeaderModel()
    {
        $staffcd_rei = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $staffcd_rei = "1234567890";
        } else {
            $staffcd_rei = "12345678";
        }
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header = array("STAFFCD"               => "※職員コード",
                            "STAFFNAME"             => "※職員氏名",
                            "STAFFNAME_SHOW"        => "職員氏名表示用",
                            "STAFFNAME_KANA"        => "職員氏名かな",
                            "STAFFNAME_ENG"         => "職員氏名英字",
                            "STAFFNAME_REAL"        => "戸籍氏名",
                            "STAFFNAME_KANA_REAL"   => "戸籍氏名かな",
                            "JOBCD"                 => "職名コード",
                            "SECTIONCD"             => "所属コード",
                            "DUTYSHARECD"           => "校務分掌部コード",
                            "CHARGECLASSCD"         => "授業受持区分",
                            "STAFFSEX"              => "職員性別",
                            "STAFFBIRTHDAY"         => "職員生年月日",
                            "STAFFZIPCD"            => "職員郵便番号",
                            "STAFFADDR1"            => "職員住所1",
                            "STAFFADDR2"            => "職員住所2",
                            "STAFFTELNO"            => "職員電話番号",
                            "STAFFFAXNO"            => "職員FAX番号",
                            "STAFFE_MAIL"           => "職員メールアドレス",
                            "YEAR"                  => "※年度",
                            "POSITIONCD1"           => "肩書き1(教務主任等コード)",
                            "POSITIONCD1_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD1_MANAGER2"  => "教科主任(学校校種)",
                            "POSITIONCD2"           => "肩書き2(教務主任等コード)",
                            "POSITIONCD2_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD2_MANAGER2"  => "教科主任(学校校種)",
                            "POSITIONCD3"           => "肩書き3(教務主任等コード)",
                            "POSITIONCD3_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD3_MANAGER2"  => "教科主任(学校校種)",
                            $this->lastColumn       => $this->lastColumn);

            $header_rei = array("STAFFCD"           => $staffcd_rei,
                            "STAFFNAME"             => "広島　太郎",
                            "STAFFNAME_SHOW"        => "広島　太郎",
                            "STAFFNAME_KANA"        => "ひろしま　たろう",
                            "STAFFNAME_ENG"         => "tarou hiroshima",
                            "STAFFNAME_REAL"        => "広島　太郎",
                            "STAFFNAME_KANA_REAL"   => "ひろしま　たろう",
                            "JOBCD"                 => "9999",
                            "SECTIONCD"             => "9999",
                            "DUTYSHARECD"           => "9999",
                            "CHARGECLASSCD"         => "1",
                            "STAFFSEX"              => "1",
                            "STAFFBIRTHDAY"         => "1960-12-31",
                            "STAFFZIPCD"            => "900-0032",
                            "STAFFADDR1"            => "広島県あああ市いいい１２３",
                            "STAFFADDR2"            => "広島アパート１２３号室",
                            "STAFFTELNO"            => "090-123-1234",
                            "STAFFFAXNO"            => "090-123-1234",
                            "STAFFE_MAIL"           => "aaa123@bbb.co.jp",
                            "YEAR"                  => "2006",
                            "POSITIONCD1"           => "0200",
                            "POSITIONCD1_MANAGER1"  => "01",
                            "POSITIONCD1_MANAGER2"  => "",
                            "POSITIONCD2"           => "1050",
                            "POSITIONCD2_MANAGER1"  => "01",
                            "POSITIONCD2_MANAGER2"  => "H",
                            "POSITIONCD3"           => "1050",
                            "POSITIONCD3_MANAGER1"  => "02",
                            "POSITIONCD3_MANAGER2"  => "H",
                            $this->lastColumn       => $this->lastColumn);
        } else {
            $header = array("STAFFCD"               => "※職員コード",
                            "STAFFNAME"             => "※職員氏名",
                            "STAFFNAME_SHOW"        => "職員氏名表示用",
                            "STAFFNAME_KANA"        => "職員氏名かな",
                            "STAFFNAME_ENG"         => "職員氏名英字",
                            "STAFFNAME_REAL"        => "戸籍氏名",
                            "STAFFNAME_KANA_REAL"   => "戸籍氏名かな",
                            "JOBCD"                 => "職名コード",
                            "SECTIONCD"             => "所属コード",
                            "DUTYSHARECD"           => "校務分掌部コード",
                            "CHARGECLASSCD"         => "授業受持区分",
                            "STAFFSEX"              => "職員性別",
                            "STAFFBIRTHDAY"         => "職員生年月日",
                            "STAFFZIPCD"            => "職員郵便番号",
                            "STAFFADDR1"            => "職員住所1",
                            "STAFFADDR2"            => "職員住所2",
                            "STAFFTELNO"            => "職員電話番号",
                            "STAFFFAXNO"            => "職員FAX番号",
                            "STAFFE_MAIL"           => "職員メールアドレス",
                            "YEAR"                  => "※年度",
                            "POSITIONCD1"           => "肩書き1(教務主任等コード)",
                            "POSITIONCD1_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD2"           => "肩書き2(教務主任等コード)",
                            "POSITIONCD2_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD3"           => "肩書き3(教務主任等コード)",
                            "POSITIONCD3_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            $this->lastColumn       => $this->lastColumn);

            $header_rei = array("STAFFCD"           => $staffcd_rei,
                            "STAFFNAME"             => "広島　太郎",
                            "STAFFNAME_SHOW"        => "広島　太郎",
                            "STAFFNAME_KANA"        => "ひろしま　たろう",
                            "STAFFNAME_ENG"         => "tarou hiroshima",
                            "STAFFNAME_REAL"        => "広島　太郎",
                            "STAFFNAME_KANA_REAL"   => "ひろしま　たろう",
                            "JOBCD"                 => "9999",
                            "SECTIONCD"             => "9999",
                            "DUTYSHARECD"           => "9999",
                            "CHARGECLASSCD"         => "1",
                            "STAFFSEX"              => "1",
                            "STAFFBIRTHDAY"         => "1960-12-31",
                            "STAFFZIPCD"            => "900-0032",
                            "STAFFADDR1"            => "広島県あああ市いいい１２３",
                            "STAFFADDR2"            => "広島アパート１２３号室",
                            "STAFFTELNO"            => "090-123-1234",
                            "STAFFFAXNO"            => "090-123-1234",
                            "STAFFE_MAIL"           => "aaa123@bbb.co.jp",
                            "YEAR"                  => "2006",
                            "POSITIONCD1"           => "0200",
                            "POSITIONCD1_MANAGER1"  => "01",
                            "POSITIONCD2"           => "1050",
                            "POSITIONCD2_MANAGER1"  => "01",
                            "POSITIONCD3"           => "1050",
                            "POSITIONCD3_MANAGER1"  => "02",
                            $this->lastColumn       => $this->lastColumn);
        }
        $contents  = implode($header, ",")."\n";
        $contents .= implode($header_rei, ",")."\n";

        $header_data = true;

        if ($header_data) {
            $couse = "職員マスタ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //データ出力
    public function getDataModel()
    {
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header = array("STAFFCD"               => "※職員コード",
                            "STAFFNAME"             => "※職員氏名",
                            "STAFFNAME_SHOW"        => "職員氏名表示用",
                            "STAFFNAME_KANA"        => "職員氏名かな",
                            "STAFFNAME_ENG"         => "職員氏名英字",
                            "STAFFNAME_REAL"        => "戸籍氏名",
                            "STAFFNAME_KANA_REAL"   => "戸籍氏名かな",
                            "JOBCD"                 => "職名コード",
                            "SECTIONCD"             => "所属コード",
                            "DUTYSHARECD"           => "校務分掌部コード",
                            "CHARGECLASSCD"         => "授業受持区分",
                            "STAFFSEX"              => "職員性別",
                            "STAFFBIRTHDAY"         => "職員生年月日",
                            "STAFFZIPCD"            => "職員郵便番号",
                            "STAFFADDR1"            => "職員住所1",
                            "STAFFADDR2"            => "職員住所2",
                            "STAFFTELNO"            => "職員電話番号",
                            "STAFFFAXNO"            => "職員FAX番号",
                            "STAFFE_MAIL"           => "職員メールアドレス",
                            "YEAR"                  => "※年度",
                            "POSITIONCD1"           => "肩書き1(教務主任等コード)",
                            "POSITIONCD1_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD1_MANAGER2"  => "教科主任(学校校種)",
                            "POSITIONCD2"           => "肩書き2(教務主任等コード)",
                            "POSITIONCD2_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD2_MANAGER2"  => "教科主任(学校校種)",
                            "POSITIONCD3"           => "肩書き3(教務主任等コード)",
                            "POSITIONCD3_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD3_MANAGER2"  => "教科主任(学校校種)",
                            $this->lastColumn       => $this->lastColumn);
        } else {
            $header = array("STAFFCD"               => "※職員コード",
                            "STAFFNAME"             => "※職員氏名",
                            "STAFFNAME_SHOW"        => "職員氏名表示用",
                            "STAFFNAME_KANA"        => "職員氏名かな",
                            "STAFFNAME_ENG"         => "職員氏名英字",
                            "STAFFNAME_REAL"        => "戸籍氏名",
                            "STAFFNAME_KANA_REAL"   => "戸籍氏名かな",
                            "JOBCD"                 => "職名コード",
                            "SECTIONCD"             => "所属コード",
                            "DUTYSHARECD"           => "校務分掌部コード",
                            "CHARGECLASSCD"         => "授業受持区分",
                            "STAFFSEX"              => "職員性別",
                            "STAFFBIRTHDAY"         => "職員生年月日",
                            "STAFFZIPCD"            => "職員郵便番号",
                            "STAFFADDR1"            => "職員住所1",
                            "STAFFADDR2"            => "職員住所2",
                            "STAFFTELNO"            => "職員電話番号",
                            "STAFFFAXNO"            => "職員FAX番号",
                            "STAFFE_MAIL"           => "職員メールアドレス",
                            "YEAR"                  => "※年度",
                            "POSITIONCD1"           => "肩書き1(教務主任等コード)",
                            "POSITIONCD1_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD2"           => "肩書き2(教務主任等コード)",
                            "POSITIONCD2_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            "POSITIONCD3"           => "肩書き3(教務主任等コード)",
                            "POSITIONCD3_MANAGER1"  => "学年主任(学年)または教科主任(教科)",
                            $this->lastColumn       => $this->lastColumn);
        }
        $contents  = "";
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjx011Query::getOutPutMasterSQL($this);
        $result = $db->query($query);
        $header_data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["REGISTERCD"] = "";
            $row["REGISTERCD"] = $this->lastColumn;//2005.09.28nakamoto
            $contents .= implode($row, ",")."\n";
            $header_data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($header_data) {
            $couse = "職員マスタ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }


    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        $data = common::DateConv1(str_replace("-", "/", $data), 4);
        if ($data) {
            $data = str_replace("/", "-", $data);
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data, $ketasuu)
    {
        $shosiki = "%0".$ketasuu."s";
        if ($data != "") {
            if (strlen($data) < $ketasuu) {
                $data = sprintf($shosiki, $data);
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning(MSG915, "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
