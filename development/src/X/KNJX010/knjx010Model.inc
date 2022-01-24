<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx010Model extends Model
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
    public function knjx010Model()
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
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                $table = "staff_mst";
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    $where = "where staffcd = '".$this->dataArr[$i]["STAFFCD"]."'";
                    $query = knjx010Query::getMasterSQL($table, $where);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($flg) {   //ＤＢにあれば、update
                            knjx010Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx010Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        }
                        if ($this->dataArr[$i]["YEAR"]) {
                            //STAFF_DETAIL_MSTへの更新
                            knjx010Query::getUpdateDetailQuery($db, $this->dataArr[$i], $this->staffcd, $this);
                        }
                    } else {
                        if ($flg) {   //ＤＢにあれば、delete
                            knjx010Query::deleteQueryCsv($db, $this->dataArr[$i]);
                        }
                    }
                }
                $db->commit();
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
            $checkData = array("STAFFCD"              => array($setsize, "職員コード"),
                               "STAFFNAME"            => array(60, "職員氏名"),
                               "STAFFNAME_SHOW"       => array(15, "職員氏名表示用"),
                               "STAFFNAME_KANA"       => array(120, "職員氏名かな"),
                               "STAFFNAME_ENG"        => array(60, "職員氏名英字"),
                               "STAFFNAME_REAL"       => array(120, "戸籍氏名"),
                               "STAFFNAME_KANA_REAL"  => array(240, "戸籍氏名かな"),
                               "JOBCD"                => array(4, "職名コード"),
                               "SECTIONCD"            => array(4, "所属コード"),
                               "DUTYSHARECD"          => array(4, "校務分掌部コード"),
                               "CHARGECLASSCD"        => array(1, "授業受持区分"),
                               "STAFFSEX"             => array(1, "職員性別"),
                               "STAFFBIRTHDAY"        => array(10, "職員生年月日"),
                               "STAFFZIPCD"           => array(8, "職員郵便番号"),
                               "STAFFADDR1"           => array(90, "職員住所1"),
                               "STAFFADDR2"           => array(90, "職員住所2"),
                               "STAFFTELNO"           => array(14, "職員電話番号"),
                               "STAFFFAXNO"           => array(14, "職員FAX番号"),
                               "STAFFE_MAIL"          => array(50, "職員メールアドレス"),
                               "YEAR"                 => array(4, "年度"),
                               "POSITIONCD1"          => array(4, "肩書き1(教務主任等コード)"),
                               "POSITIONCD1_MANAGER1" => array(2, "学年主任(学年)または教科主任(教科)"),
                               "POSITIONCD1_MANAGER2" => array(1, "教科主任(学校校種)"),
                               "POSITIONCD2"          => array(4, "肩書き2(教務主任等コード)"),
                               "POSITIONCD2_MANAGER1" => array(2, "学年主任(学年)または教科主任(教科)"),
                               "POSITIONCD2_MANAGER2" => array(1, "教科主任(学校校種)"),
                               "POSITIONCD3"          => array(4, "肩書き3(教務主任等コード)"),
                               "POSITIONCD3_MANAGER1" => array(2, "学年主任(学年)または教科主任(教科)"),
                               "POSITIONCD3_MANAGER2" => array(1, "教科主任(学校校種)"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $record_no = 1;         //行番号(初期値)
            $i = 0;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $primaryKey4 = array();
            $primaryKey5 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $duplicateCsv4 = array();
            $duplicateCsv5 = array();
            if ($this->field["SHORI_MEI"] == "1") {
                $primaryKey1 = $this->getPrimaryKey("STAFF_MST");
                $primaryKey2 = $this->getPrimaryKey("STAFF_DETAIL_MST");
            } else {
                $primaryKey1 = $this->getPrimaryKey("STAFF_MST");
                $primaryKey2 = $this->getPrimaryKey("STAFF_DETAIL_MST");
                $primaryKey3 = $this->getPrimaryKey("STAFF_YDAT");
                $primaryKey4 = $this->getPrimaryKey("USER_MST");
                $primaryKey5 = $this->getPrimaryKey("USER_PWD_HIST_MST");
            }

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
                                      "POSITIONCD3_MANAGER2"   => trim($tmp[28])
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
                                      );
                    }
                    $data["STAFF_SEQ"] = "";
                    //０埋め
                    $data["STAFFCD"]     = $this->zeroUme($data["STAFFCD"], $setsize);
                    $data["JOBCD"]       = $this->zeroUme($data["JOBCD"], 4);
                    $data["SECTIONCD"]   = $this->zeroUme($data["SECTIONCD"], 4);
                    $data["DUTYSHARECD"] = $this->zeroUme($data["DUTYSHARECD"], 4);
                    if ($data["STAFFBIRTHDAY"] != "" && $this->dateRep($data["STAFFBIRTHDAY"])) {
                        $data["STAFFBIRTHDAY"] = $this->dateRep($data["STAFFBIRTHDAY"]);
                    }

                    //取り込み処理（エラーの場合、エラーＤＢへの追加）
                    if ($this->errCheck($db, $data, $checkData, $record_no, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    if ($this->field["SHORI_MEI"] == "1") {
                        foreach ($primaryKey1 as $key => $val) {
                            $duplicateCsv1[$record_no][$val] = $data[$val];
                        }
                        if ($data["YEAR"]) {
                            foreach ($primaryKey2 as $key => $val) {
                                $duplicateCsv2[$record_no][$val] = $data[$val];
                            }
                        }
                    } else {
                        foreach ($primaryKey1 as $key => $val) {
                            $duplicateCsv1[$record_no][$val] = $data[$val];
                        }
                        foreach ($primaryKey2 as $key => $val) {
                            $duplicateCsv2[$record_no][$val] = $data[$val];
                        }
                        foreach ($primaryKey3 as $key => $val) {
                            $duplicateCsv3[$record_no][$val] = $data[$val];
                        }
                        foreach ($primaryKey4 as $key => $val) {
                            $duplicateCsv4[$record_no][$val] = $data[$val];
                        }
                        foreach ($primaryKey5 as $key => $val) {
                            $duplicateCsv5[$record_no][$val] = $data[$val];
                        }
                    }
                }
                $record_no++;   //行番号(カウント)
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
            if($koumoku_error != "") {
                $this->message = "CSV登録に失敗しました。";
            } else {
                $this->message = "CSV登録に成功しました。";
            }
            //１万件を超えたら強制終了する。
            if ($record_no > 10000) {
                $this->message .= "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";
            }
        } else {
            $this->warning = "ファイル名が不正です。";
        }
    }

    //削除(データ取込)
    public function fileUploadDel()
    {
        $setsize = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
        } else {
            $setsize = 8;
        }
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx010Query::deleteQueryErr();
            $result = $db->query($query);

            $record_no = 1;         //行番号(初期値)

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //１万件を超えたらループから抜ける。
                    if ($record_no > 10000) {
                        break;
                    }
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("STAFFCD"         => trim($tmp[0]));

                    $koumoku_error = "";    //エラー出力用

                    //桁数チェック
                    if (strlen($data["STAFFCD"]) > $setsize) {
                        $koumoku_error .= "桁数エラー。";
                    }
                    //必須チェック
                    if ($data["STAFFCD"] == "") {
                        $koumoku_error .= "必須項目エラー。";
                    }

                    //存在チェック(項目チェックがＯＫの場合)
                    if ($koumoku_error == "") {
                        //０埋め
                        $data["STAFFCD"]        = $this->zeroUme($data["STAFFCD"], $setsize);
                    }

                    //取り込み処理（エラーの場合、エラーＤＢへの追加）
                    if ($koumoku_error != "") {
                        $check_error = "";
                        $check_error = $koumoku_error;
                        knjx010Query::insertQueryErr($db, $record_no, $check_error);
                    } else {
                        $table = "staff_mst";
                        $where = "where staffcd = '".$data["STAFFCD"]."'";
                        $query = knjx010Query::getMasterSQL($table, $where);
                        $result = $db->query($query);
                        $flg = false;
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $flg = true;
                        }
                        if ($flg) {   //ＤＢにあれば、delete
                            knjx010Query::deleteQueryCsv($db, $data);
                        }
                    }//$ifの綴り
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
            if($koumoku_error != "") {
                $this->message = "CSV登録に失敗しました。";
            } else {
                $this->message = "CSV登録に成功しました。";
            }
            //１万件を超えたら強制終了する。
            if ($record_no > 10000) {
                $this->message .= "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";
            }
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

    //ＣＳＶへエラー出力
    public function getDownloadModel()
    {
        $header = array("MSGROW"            => "行番号",
                        "MSGREMARK"         => "メッセージ");
        $headerDuplicate = array("STAFFCD"     => "職員コード",
                                 "YEAR"        => "年度",
                                 "STAFF_SEQ"   => "職員SEQ",
                                 "SCHOOLCD"    => "学校コード",
                                 "SCHOOL_KIND" => "学校校種",
                                 "PASSWD"      => "パスワード");
        $tables = array();
        if ($this->field["SHORI_MEI"] == "1") {
            $tables = array("1" => "STAFF_MST", "2" => "STAFF_DETAIL_MST");
        } else {
            $tables = array("1" => "STAFF_MST", "2" => "STAFF_DETAIL_MST", "3" => "STAFF_YDAT", "4" => "USER_MST", "5" => "USER_PWD_HIST_MST");
        }
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            $contents .= implode($headerErr, ",")."\n";
        }
        $contents .= implode($header, ",")."\n";
        //DB接続
        $db = Query::dbCheckOut();

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
                            "YEAR"                  => "年度",
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
                            "YEAR"                  => "年度",
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
                            "YEAR"                  => "年度",
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
                            "YEAR"                  => "年度",
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
        $query = knjx010Query::getOutPutMasterSQL($this);
        $result = $db->query($query);
        $header_data = false;
        list($simo, $fuseji) = explode(" | ", $this->Properties["showMaskStaffCd"]);//職員番号マスク
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $row["STAFFCD"] = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - $simo), $simo);
            }
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
     * 項目チェック
     */
    public function errCheck($db, $data, $checkData, $record_no, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5)
    {
        $returnFlg     = true;
        $hissuErrFlg   = false;    //必須エラーフラグ
        $lengthErrFlg  = false;    //レングスエラーフラグ
        $numericErrFlg = false;    //数字エラーフラグ
        $valuesErrFlg  = false;
        $masterErrFlg  = false;
        $dateErrFlg    = false;
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg   = "";       //必須エラーメッセージ
        $lengthErrMsg  = "";       //レングスエラーメッセージ
        $numericErrMsg = "";       //数字エラーメッセージ
        $valuesErrMsg  = "";
        $masterErrMsg  = "";
        $dateErrMsg    = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep   = "";
        $lengthSep  = "";
        $numericSep = "";
        $valuesSep  = "";
        $masterSep  = "";
        $dateSep    = "";
        $duplicateCsvSep = "";

        if ($this->field["SHORI_MEI"] == "1") {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
            if ($data["YEAR"]) {
                $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
                if ($dupFlg2[0] == 1) {
                    $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
                }
            }
        } else {
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
            $dupFlg4 = $this->duplicateCsvCheck($duplicateCsv4, $primaryKey4, $data);
            if ($dupFlg4[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複4(". $dupFlg4[1] ."行目)");
            }
            $dupFlg5 = $this->duplicateCsvCheck($duplicateCsv5, $primaryKey5, $data);
            if ($dupFlg5[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複5(". $dupFlg5[1] ."行目)");
            }
        }
        $setsize = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
        } else {
            $setsize = 8;
        }
        if ($this->field["SHORI_MEI"] == "1") {
            foreach ($checkData as $key => $val) {
                //桁数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
                //必須チェック
                if (in_array($key, array("STAFFCD", "STAFFNAME"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //数字チェック
                if (in_array($key, array("JOBCD", "SECTIONCD", "DUTYSHARECD", "CHARGECLASSCD", "STAFFSEX", "YEAR", "POSITIONCD1", "POSITIONCD1_MANAGER1", "POSITIONCD2", "POSITIONCD2_MANAGER1", "POSITIONCD3", "POSITIONCD3_MANAGER1"))) {
                    if (strlen($data[$key]) && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
                //値チェック
                if (in_array($key, array("CHARGECLASSCD"))) {
                    if (!$this->ataiCheck($data[$key], array("0","1"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
                if (in_array($key, array("STAFFSEX"))) {
                    if (!$this->ataiCheck($data[$key], array("1","2"))) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }
                }
            }
            //存在チェック(項目チェックがＯＫの場合)
            if ($returnFlg) {
                //日付変換
                if ($data["STAFFBIRTHDAY"] != "" && !$this->dateRep($data["STAFFBIRTHDAY"])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "職員生年月日");
                }

                //職名マスタ
                $table = "job_mst";
                $where = "where jobcd = '".$data["JOBCD"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["JOBCD"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "職名コード");
                }
                //所属マスタ
                $table = "section_mst";
                $where = "where sectioncd = '".$data["SECTIONCD"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["SECTIONCD"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "所属コード");
                }
                //校務分掌マスタ
                $table = "dutyshare_mst";
                $where = "where dutysharecd = '".$data["DUTYSHARECD"]."'";
                $sonzaiFlg = $this->sonzaiCheck($db, $data["DUTYSHARECD"], $table, $where);
                if (!$sonzaiFlg) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "校務分掌部コード");
                }
            }
        } else {
            if (strlen($data["STAFFCD"]) > $setsize) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "職員コード");
            }
            if (!strlen(trim($data["STAFFCD"]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "職員コード");
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$dateErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx010Query::insertQueryErr($db, $record_no, $checkError);
        }
        return $returnFlg;
    }

    //値チェック
    public function ataiCheck($data, $atai)
    {
        $flg = true;

        if ($data != "") {
            if (!in_array($data, $atai)) {
                $flg = false;
            }
        }

        return $flg;
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

    /**
     * マスタの存在チェック
     */
    public function sonzaiCheck(&$db, $data, $table, $where)
    {
        $sonzaiFlg = true;
        if ($data != "") {
            $sonzaiFlg = false;
            $query = knjx010Query::getMasterSQL($table, $where);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sonzaiFlg = true;
            }
        }
        return $sonzaiFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
