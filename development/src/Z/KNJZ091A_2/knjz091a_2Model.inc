<?php

require_once('for_php7.php');

class knjz091a_2Model extends Model
{
    public $field      = array();
    public $cmd;
    public $prischoolcd;
    public $file;
    public $lastColumn;//CSVの最後の列の値
    public $prischoolStaffcdKeta;

    /**
     * メンバ変数の初期化
     */
    public function knjz091a_2Model()
    {
    }
    public function init()
    {
        $this->field = array("PRISCHOOLCD"          =>  VARS::post("PRISCHOOLCD"),
                             "PRISCHOOL_NAME"       =>  VARS::post("PRISCHOOL_NAME"),
                             "PRISCHOOL_KANA"       =>  VARS::post("PRISCHOOL_KANA"),
                             "PRINCNAME"            =>  VARS::post("PRINCNAME"),
                             "PRINCNAME_SHOW"       =>  VARS::post("PRINCNAME_SHOW"),
                             "PRINCKANA"            =>  VARS::post("PRINCKANA"),
                             "PRISCHOOL_STAFFCD"    =>  VARS::post("PRISCHOOL_STAFFCD"),
                             "DISTRICTCD"           =>  VARS::post("DISTRICTCD"),
                             "PRISCHOOL_PREF_CD"    =>  VARS::post("PRISCHOOL_PREF_CD"),
                             "PRISCHOOL_CITY_CD"    =>  VARS::post("PRISCHOOL_CITY_CD"),
                             "PRISCHOOL_ZIPCD"      =>  VARS::post("PRISCHOOL_ZIPCD"),
                             "PRISCHOOL_ADDR1"      =>  VARS::post("PRISCHOOL_ADDR1"),
                             "PRISCHOOL_ADDR2"      =>  VARS::post("PRISCHOOL_ADDR2"),
                             "PRISCHOOL_TELNO"      =>  VARS::post("PRISCHOOL_TELNO"),
                             "PRISCHOOL_FAXNO"      =>  VARS::post("PRISCHOOL_FAXNO"),
                             "GRP_PRISCHOOLCD"      =>  VARS::post("GRP_PRISCHOOLCD"),
                             "MIRAI_PS_CD"          =>  VARS::post("MIRAI_PS_CD"),         //ミライコンパス塾コード

                             "HEADER"               => VARS::post("HEADER"),              //ヘッダの有無
                             "OUTPUT"               => VARS::post("OUTPUT"),              //処理の種類(1:取込、2:書出、3:エラー出力)

                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd       = VARS::request("cmd");
        if (VARS::get("mode") == "1") {
            unset($this->prischoolcd);
        }
        $temp = VARS::get("PRISCHOOLCD");
        if (isset($temp)) {
            $this->prischoolcd  = VARS::get("PRISCHOOLCD");
        }

        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }

        $this->prischoolStaffcdKeta = ($this->Properties["useStaffcdFieldSize"] == '10' ? 10 : 8);
    }

    //入力項目チェック
    public function validateInput()
    {
        //キーのチェック
        if (trim($this->field["PRISCHOOLCD"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901', "( 塾コード )");
            return false;
        }
        if ($this->prischoolcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->cmd == "update" && $this->prischoolcd != $this->field["PRISCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        //コードのゼロ埋め
        if (strlen($this->field["PRISCHOOLCD"]) < 7) {
            $this->field["PRISCHOOLCD"] = sprintf("%07d", $this->field["PRISCHOOLCD"]);
        }

        //１レコード取得
        $row = knjz091a_2Query::getRow($this->field["PRISCHOOLCD"], $this);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning('MSG918', "( 塾コード )");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning('MSG307');
            return false;
        }
        //文字型チェック
        foreach (
                    array("PRISCHOOL_TELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                          "PRISCHOOL_FAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }
        if (strlen($this->field["PRISCHOOLCD"]) >7) {
            $this->setWarning('MSG901', "( 塾コード ) 7バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_NAME"])>75) {
            $this->setWarning('MSG901', "( 塾名 ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_KANA"])>75) {
            $this->setWarning('MSG901', "( 塾名かな ) 75バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME"])>60) {
            $this->setWarning('MSG901', "( 塾長氏名 ) 60バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCNAME_SHOW"])>30) {
            $this->setWarning('MSG901', "( 塾長氏名表示用 ) 30バイトまで");
            return false;
        } elseif (strlen($this->field["PRINCKANA"])>120) {
            $this->setWarning('MSG901', "( 塾長氏名カナ ) 120バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_ZIPCD"])>8) {
            $this->setWarning('MSG901', "( 郵便番号 ) 8バイトまで");
            return false;
        } elseif (strlen($this->field["DISTRICTCD"])>2) {
            $this->setWarning('MSG901', "( 地区コード ) 2バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_ADDR1"])>90) {
            $this->setWarning('MSG901', "( 住所 ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_ADDR2"])>90) {
            $this->setWarning('MSG901', "( 方書き(アパート名等) ) 90バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_TELNO"])>14) {
            $this->setWarning('MSG901', "( 電話番号 ) 14バイトまで");
            return false;
        } elseif (strlen($this->field["PRISCHOOL_FAXNO"])>14) {
            $this->setWarning('MSG901', "( FAX番号 ) 14バイトまで");
            return false;
        }
        //塾グループ【塾コード（親）】が塾マスタに存在するか
        if (strlen($this->field["GRP_PRISCHOOLCD"])) {
            $row = knjz091a_2Query::getRow($this->field["GRP_PRISCHOOLCD"]);
            if (($this->cmd == "add" || $this->cmd == "update") && !is_array($row)) {
                $this->setWarning('MSG303', "( 塾グループ )");
                return false;
            }
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
            $this->prischoolcd = $this->field["PRISCHOOLCD"];
            $this->setMessage('MSG201');
        }
    }

    // 削除用のモデルを返します
    public function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->prischoolcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->prischoolcd != $this->field["PRISCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }
        $row = knjz091a_2Query::getRow($this->prischoolcd, $this);
        if (!is_array($row)) {
            $this->setWarning('MSG303');
            return;
        }
        if (knjz091a_2Query::isExisting($this->prischoolcd)) {
            $this->setWarning('MSG919', "年度データに登録されています。");
            return;
        }
        knjz091a_2Query::getDeleteQuery($this->field, $this);
        $this->setMessage('MSG201');
    }
    
    public function &insert()
    {
        knjz091a_2Query::getInsertQuery($this->field, $this);
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        knjz091a_2Query::getUpdateQuery($this->field, $this);
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();

        //PRISCHOOL_MST（塾マスタ）
        $header["PRISCHOOLCD"]              = "※塾コード";
        $header["PRISCHOOL_NAME"]           = "塾名";
        $header["PRISCHOOL_KANA"]           = "塾名かな";
        $header["PRISCHOOL_STAFFCD"]        = "担当者コード";
        $header["PRISCHOOL_STAFF_NAME"]     = "担当者名";
        $header["PRINCNAME"]                = "塾長氏名";
        $header["PRINCNAME_SHOW"]           = "塾長氏名表示用";
        $header["PRINCKANA"]                = "塾長氏名かな";
        $header["DISTRICTCD"]               = "地区コード";
        $header["DISTRICT_NAME"]            = "地区名";
        $header["PRISCHOOL_PREF_CD"]        = "都道府県コード";
        $header["PRISCHOOL_PREF_NAME"]      = "都道府県名";
        $header["PRISCHOOL_CITY_CD"]        = "市区町村コード";
        $header["PRISCHOOL_CITY_NAME"]      = "市区町村名";
        $header["PRISCHOOL_ZIPCD"]          = "郵便番号";
        $header["PRISCHOOL_ADDR1"]          = "住所";
        $header["PRISCHOOL_ADDR2"]          = "方書き(アパート名等)";
        $header["PRISCHOOL_TELNO"]          = "電話番号";
        $header["PRISCHOOL_FAXNO"]          = "FAX番号";
        if ($this->Properties["useMiraicompass"] == '1') {
            $header["MIRAI_PS_CD"]              = "ミライコンパス塾コード";
        }
        $header["GRP_PRISCHOOLCD"]          = "塾グループコード";
        //PRISCHOOL_CLASS_MST（塾教室マスタ）
        $header["C_PRISCHOOLCD"]            = "※塾コード";
        $header["C_PRISCHOOL_CLASS_CD"]     = "※教室コード";
        $header["C_PRISCHOOL_NAME"]         = "教室名";
        $header["C_PRISCHOOL_KANA"]         = "教室名かな";
        $header["C_PRINCNAME"]              = "校舎長氏名";
        $header["C_PRINCNAME_SHOW"]         = "校舎長氏名表示用";
        $header["C_PRINCKANA"]              = "校舎長氏名かな";
        $header["C_DISTRICTCD"]             = "地区コード";
        $header["C_PRISCHOOL_ZIPCD"]        = "郵便番号";
        $header["C_PRISCHOOL_ADDR1"]        = "住所";
        $header["C_PRISCHOOL_ADDR2"]        = "方書き(アパート名等)";
        $header["C_PRISCHOOL_TELNO"]        = "電話番号";
        $header["C_PRISCHOOL_FAXNO"]        = "FAX番号";
        $header["C_ROSEN_1"]                = "路線１";
        $header["C_ROSEN_2"]                = "路線２";
        $header["C_ROSEN_3"]                = "路線３";
        $header["C_ROSEN_4"]                = "路線４";
        $header["C_ROSEN_5"]                = "路線５";
        $header["C_NEAREST_STATION_NAME1"]  = "最寄駅名１";
        $header["C_NEAREST_STATION_KANA1"]  = "かな１";
        $header["C_NEAREST_STATION_NAME2"]  = "最寄駅名２";
        $header["C_NEAREST_STATION_KANA2"]  = "かな２";
        $header["C_DIRECT_MAIL_FLG"]        = "DM不可フラグ";
        $header[$this->lastColumn]          = $this->lastColumn;

        $header_rei = array();
        //PRISCHOOL_MST
        $header_rei["PRISCHOOLCD"]              = "1234567";
        $header_rei["PRISCHOOL_NAME"]           = "アルプ塾";
        $header_rei["PRISCHOOL_KANA"]           = "あるぷじゅく";
        if ($this->prischoolStaffcdKeta == 10) {
            $header_rei["PRISCHOOL_STAFFCD"]           = "1234567890";
        } else {
            $header_rei["PRISCHOOL_STAFFCD"]           = "12345678";
        }
        $header_rei["PRISCHOOL_STAFF_NAME"]     = "スタッフ　太郎";
        $header_rei["PRINCNAME"]                = "アルプ　太郎";
        $header_rei["PRINCNAME_SHOW"]           = "アルプ　太郎";
        $header_rei["PRINCKANA"]                = "あるぷ　たろう";
        $header_rei["DISTRICTCD"]               = "01";
        $header_rei["DISTRICT_NAME"]            = "東京";
        $header_rei["PRISCHOOL_PREF_CD"]        = "01";
        $header_rei["PRISCHOOL_PREF_NAME"]      = "東京都";
        $header_rei["PRISCHOOL_CITY_CD"]        = "123";
        $header_rei["PRISCHOOL_CITY_NAME"]      = "千代田区";
        $header_rei["PRISCHOOL_ZIPCD"]          = "102-0093";
        $header_rei["PRISCHOOL_ADDR1"]          = "東京都千代田区平河町1-2-10";
        $header_rei["PRISCHOOL_ADDR2"]          = "平河町第一生命ビル";
        $header_rei["PRISCHOOL_TELNO"]          = "03-3234-3571";
        $header_rei["PRISCHOOL_FAXNO"]          = "098-869-1785";
        if ($this->Properties["useMiraicompass"] == '1') {
            $header_rei["MIRAI_PS_CD"]              = "1234567890,";
        }
        $header_rei["GRP_PRISCHOOLCD"]          = "1234567";
        //PRISCHOOL_CLASS_MST
        $header_rei["C_PRISCHOOLCD"]            = "1234567";
        $header_rei["C_PRISCHOOL_CLASS_CD"]     = "1234567";
        $header_rei["C_PRISCHOOL_NAME"]         = "アルプ教室";
        $header_rei["C_PRISCHOOL_KANA"]         = "あるぷきょうしつ";
        $header_rei["C_PRINCNAME"]              = "アルプ　太郎";
        $header_rei["C_PRINCNAME_SHOW"]         = "アルプ　太郎";
        $header_rei["C_PRINCKANA"]              = "あるぷ　たろう";
        $header_rei["C_DISTRICTCD"]             = "01";
        $header_rei["C_PRISCHOOL_ZIPCD"]        = "102-0093";
        $header_rei["C_PRISCHOOL_ADDR1"]        = "東京都千代田区平河町1-2-10";
        $header_rei["C_PRISCHOOL_ADDR2"]        = "平河町第一生命ビル";
        $header_rei["C_PRISCHOOL_TELNO"]        = "03-3234-3571";
        $header_rei["C_PRISCHOOL_FAXNO"]        = "098-869-1785";
        $header_rei["C_ROSEN_1"]                = "12345";
        $header_rei["C_ROSEN_2"]                = "12345";
        $header_rei["C_ROSEN_3"]                = "12345";
        $header_rei["C_ROSEN_4"]                = "12345";
        $header_rei["C_ROSEN_5"]                = "12345";
        $header_rei["C_NEAREST_STATION_NAME1"]  = "半蔵門駅";
        $header_rei["C_NEAREST_STATION_KANA1"]  = "はんぞうもんえき";
        $header_rei["C_NEAREST_STATION_NAME2"]  = "麹町駅";
        $header_rei["C_NEAREST_STATION_KANA2"]  = "こうじまちえき";
        $header_rei["C_DIRECT_MAIL_FLG"]        = "1";
        $header_rei[$this->lastColumn]          = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="4") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            //エラー
            $headerDuplicate = array("PRISCHOOLCD"        => "※塾コード",
                                     "PRISCHOOL_CLASS_CD" => "塾教室コード");
            $tables = array();
            if ($this->Properties["useMiraicompass"] == '1') {
                $tables = array("1" => "PRISCHOOL_MST", "2" => "PRISCHOOL_CLASS_MST", "3" => "ENTEXAM_MIRAI_PS_REP_DAT");
            } else {
                $tables = array("1" => "PRISCHOOL_MST", "2" => "PRISCHOOL_CLASS_MST");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header_err)."\n";
        } elseif ($this->field["OUTPUT"]=="2") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode(",", $header)."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "4") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="2") {
                //データ
                $query = knjz091a_2Query::selectMainQuery($this);
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"]=="2") {                //データ
                        $row = str_replace(array("\r\n", "\r", "\n"), "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                        $array = $header;
                    } else {
                        $array = $header_err;
                    }

                    //ヘッダ順に値をセット
                    $comma = "";
                    foreach ($array as $key => $headerName) {
                        $contents .= $comma.$row[$key];
                        $comma = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;
                }
                $result->free();
            }
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "塾マスタデータ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
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
                knjz091a_2Query::insertQueryCsv($this, $this->data_arr);
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
            //PRISCHOOL_MST（塾マスタ）
            $checkdata["PRISCHOOLCD"]              = array(7,    "HISSU",  "INT2",  "塾コード");
            $checkdata["PRISCHOOL_NAME"]           = array(75,   "",       "",      "塾名");
            $checkdata["PRISCHOOL_KANA"]           = array(75,   "",       "",      "塾名かな");
            $checkdata["PRISCHOOL_STAFFCD"]        = array($this->prischoolStaffcdKeta,   "", "INT2",  "担当者コード");
            $checkdata["PRINCNAME"]                = array(60,   "",       "",      "塾長氏名");
            $checkdata["PRINCNAME_SHOW"]           = array(30,   "",       "",      "塾長氏名表示用");
            $checkdata["PRINCKANA"]                = array(120,  "",       "",      "塾長氏名かな");
            $checkdata["DISTRICTCD"]               = array(2,    "",       "INT2",  "地区コード");
            $checkdata["PRISCHOOL_PREF_CD"]        = array(2,    "",       "INT2",  "都道府県コード");
            $checkdata["PRISCHOOL_CITY_CD"]        = array(3,    "",       "INT2",  "市区町村コード");
            $checkdata["PRISCHOOL_ZIPCD"]          = array(8,    "",       "",      "郵便番号");
            $checkdata["PRISCHOOL_ADDR1"]          = array(150,  "",       "",      "住所");
            $checkdata["PRISCHOOL_ADDR2"]          = array(150,  "",       "",      "方書き(アパート名等)");
            $checkdata["PRISCHOOL_TELNO"]          = array(14,   "",       "",      "電話番号");
            $checkdata["PRISCHOOL_FAXNO"]          = array(14,   "",       "",      "FAX番号");
            if ($this->Properties["useMiraicompass"] == '1') {
                $checkdata["MIRAI_PS_CD"]          = array(10,   "",       "INT",   "ミライコンパス塾コード");
            }
            $checkdata["GRP_PRISCHOOLCD"]          = array(7,    "",       "INT2",  "塾グループコード");
            //PRISCHOOL_CLASS_MST（塾教室マスタ）
            $checkdata["C_PRISCHOOLCD"]            = array(7,    "HISSU2", "INT2",  "塾コード");
            $checkdata["C_PRISCHOOL_CLASS_CD"]     = array(7,    "HISSU2", "INT2",  "教室コード");
            $checkdata["C_PRISCHOOL_NAME"]         = array(75,   "",       "",      "教室名");
            $checkdata["C_PRISCHOOL_KANA"]         = array(75,   "",       "",      "教室名かな");
            $checkdata["C_PRINCNAME"]              = array(60,   "",       "",      "校舎長氏名");
            $checkdata["C_PRINCNAME_SHOW"]         = array(30,   "",       "",      "校舎長氏名表示用");
            $checkdata["C_PRINCKANA"]              = array(120,  "",       "",      "校舎長氏名かな");
            $checkdata["C_DISTRICTCD"]             = array(2,    "",       "INT2",  "地区コード");
            $checkdata["C_PRISCHOOL_ZIPCD"]        = array(8,    "",       "",      "郵便番号");
            $checkdata["C_PRISCHOOL_ADDR1"]        = array(150,  "",       "",      "住所");
            $checkdata["C_PRISCHOOL_ADDR2"]        = array(150,  "",       "",      "方書き(アパート名等)");
            $checkdata["C_PRISCHOOL_TELNO"]        = array(14,   "",       "",      "電話番号");
            $checkdata["C_PRISCHOOL_FAXNO"]        = array(14,   "",       "",      "FAX番号");
            $checkdata["C_ROSEN_1"]                = array(5,    "",       "INT",   "路線１");
            $checkdata["C_ROSEN_2"]                = array(5,    "",       "INT",   "路線２");
            $checkdata["C_ROSEN_3"]                = array(5,    "",       "INT",   "路線３");
            $checkdata["C_ROSEN_4"]                = array(5,    "",       "INT",   "路線４");
            $checkdata["C_ROSEN_5"]                = array(5,    "",       "INT",   "路線５");
            $checkdata["C_NEAREST_STATION_NAME1"]  = array(75,   "",       "",      "最寄駅名１");
            $checkdata["C_NEAREST_STATION_KANA1"]  = array(75,   "",       "",      "かな１");
            $checkdata["C_NEAREST_STATION_NAME2"]  = array(75,   "",       "",      "最寄駅名２");
            $checkdata["C_NEAREST_STATION_KANA2"]  = array(75,   "",       "",      "かな２");
            $checkdata["C_DIRECT_MAIL_FLG"]        = array(1,    "FLAG",   "INT",   "DM不可フラグ");

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
            $primaryKey1 = $this->getPrimaryKey("PRISCHOOL_MST");
            $primaryKey2 = $this->getPrimaryKey("PRISCHOOL_CLASS_MST");
            if ($this->Properties["useMiraicompass"] == '1') {
                $primaryKey3 = $this->getPrimaryKey("ENTEXAM_MIRAI_PS_REP_DAT");
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
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $colCnt = 0;
                    //PRISCHOOL_MST（塾マスタ）
                    $data["PRISCHOOLCD"]              = (strlen(trim($tmp[$colCnt]))) ? sprintf("%07d", $tmp[$colCnt]): "";
                    $colCnt++;
                    $data["PRISCHOOL_NAME"]           = $tmp[$colCnt++];
                    $data["PRISCHOOL_KANA"]           = $tmp[$colCnt++];
                    $data["PRISCHOOL_STAFFCD"]        = (strlen(trim($tmp[$colCnt]))) ? sprintf("%0".$this->prischoolStaffcdKeta."d", $tmp[$colCnt]) : "";
                    $colCnt++;
                    $data["PRISCHOOL_STAFF_NAME"]     = $tmp[$colCnt++];
                    $data["PRINCNAME"]                = $tmp[$colCnt++];
                    $data["PRINCNAME_SHOW"]           = $tmp[$colCnt++];
                    $data["PRINCKANA"]                = $tmp[$colCnt++];
                    $data["DISTRICTCD"]               = (strlen(trim($tmp[$colCnt]))) ? sprintf("%02d", $tmp[$colCnt]): "";
                    $colCnt++;
                    $data["DISTRICT_NAME"]            = $tmp[$colCnt++];
                    $data["PRISCHOOL_PREF_CD"]        = (strlen(trim($tmp[$colCnt]))) ? sprintf("%02d", $tmp[$colCnt]): "";
                    $colCnt++;
                    $data["PRISCHOOL_PREF_NAME"]      = $tmp[$colCnt++];
                    $data["PRISCHOOL_CITY_CD"]        = (strlen(trim($tmp[$colCnt]))) ? sprintf("%03d", $tmp[$colCnt]): "";
                    $colCnt++;
                    $data["PRISCHOOL_CITY_NAME"]      = $tmp[$colCnt++];
                    $data["PRISCHOOL_ZIPCD"]          = $tmp[$colCnt++];
                    $data["PRISCHOOL_ADDR1"]          = $tmp[$colCnt++];
                    $data["PRISCHOOL_ADDR2"]          = $tmp[$colCnt++];
                    $data["PRISCHOOL_TELNO"]          = $tmp[$colCnt++];
                    $data["PRISCHOOL_FAXNO"]          = $tmp[$colCnt++];
                    if ($this->Properties["useMiraicompass"] == '1') {
                        $data["MIRAI_PS_CD"]          = $tmp[$colCnt++];
                    }
                    $data["GRP_PRISCHOOLCD"]          = (strlen(trim($tmp[$colCnt]))) ? sprintf("%07d", $tmp[$colCnt]): "";
                    $colCnt++;
                    //PRISCHOOL_CLASS_MST（塾教室マスタ）
                    $data["C_PRISCHOOLCD"]            = (strlen(trim($tmp[$colCnt]))) ? sprintf("%07d", $tmp[$colCnt]): "";
                    $colCnt++;
                    $data["C_PRISCHOOL_CLASS_CD"]     = (strlen(trim($tmp[$colCnt]))) ? sprintf("%07d", $tmp[$colCnt]): "";
                    $colCnt++;
                    $data["C_PRISCHOOL_NAME"]         = $tmp[$colCnt++];
                    $data["C_PRISCHOOL_KANA"]         = $tmp[$colCnt++];
                    $data["C_PRINCNAME"]              = $tmp[$colCnt++];
                    $data["C_PRINCNAME_SHOW"]         = $tmp[$colCnt++];
                    $data["C_PRINCKANA"]              = $tmp[$colCnt++];
                    $data["C_DISTRICTCD"]             = (strlen(trim($tmp[$colCnt]))) ? sprintf("%02d", $tmp[$colCnt]): "";
                    $colCnt++;
                    $data["C_PRISCHOOL_ZIPCD"]        = $tmp[$colCnt++];
                    $data["C_PRISCHOOL_ADDR1"]        = $tmp[$colCnt++];
                    $data["C_PRISCHOOL_ADDR2"]        = $tmp[$colCnt++];
                    $data["C_PRISCHOOL_TELNO"]        = $tmp[$colCnt++];
                    $data["C_PRISCHOOL_FAXNO"]        = $tmp[$colCnt++];
                    $data["C_ROSEN_1"]                = $tmp[$colCnt++];
                    $data["C_ROSEN_2"]                = $tmp[$colCnt++];
                    $data["C_ROSEN_3"]                = $tmp[$colCnt++];
                    $data["C_ROSEN_4"]                = $tmp[$colCnt++];
                    $data["C_ROSEN_5"]                = $tmp[$colCnt++];
                    $data["C_NEAREST_STATION_NAME1"]  = $tmp[$colCnt++];
                    $data["C_NEAREST_STATION_KANA1"]  = $tmp[$colCnt++];
                    $data["C_NEAREST_STATION_NAME2"]  = $tmp[$colCnt++];
                    $data["C_NEAREST_STATION_KANA2"]  = $tmp[$colCnt++];
                    $data["C_DIRECT_MAIL_FLG"]        = $tmp[$colCnt++];
                    $data["PRISCHOOL_CLASS_CD"]       = $data["C_PRISCHOOL_CLASS_CD"];

                    $dataCnt = $colCnt + 1;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $masterErrFlg    = false;
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $valuesErrFlg    = false;    //その他エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $masterErrMsg    = "";
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $valuesErrMsg    = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $existenceSep = "";
        $masterSep    = "";
        $numericSep   = "";
        $lengthSep    = "";
        $valuesSep    = "";
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

        $checkNameMstCd = array("DISTRICTCD"        => "Z060",
                                "C_DISTRICTCD"      => "Z003");

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            } elseif ($val[1] == "HISSU2") {
                $prischoolClassCdIkou = false; // 列が教室コード以降
                $checkHissu2 = false;
                foreach ($data_each as $key2 => $val2) {
                    if (in_array($key2, array("C_PRISCHOOLCD", "C_PRISCHOOL_CLASS_CD"))) {
                        $prischoolClassCdIkou = true;
                        continue;
                    }
                    if ($prischoolClassCdIkou && $data[$key2]) {
                        // 教室コード以降に値が設定されているので教室コードのキーを必須チェックする
                        $checkHissu2 = true;
                    }
                }
                if ($checkHissu2 && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
            }
            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }

            /********************/
            /**  存在チェック  **/
            /********************/
            //名称マスタ
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjz091a_2Query::nameCntSql($checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                }
            }
            //路線１～５
            if (in_array($key, array("C_ROSEN_1", "C_ROSEN_2", "C_ROSEN_3", "C_ROSEN_4", "C_ROSEN_5")) && strlen($data[$key]) && 0 == $db->getOne(knjz091a_2Query::stationNetMstCntSql($data[$key]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
            }
            //職員マスタ
            if ($key == "PRISCHOOL_STAFFCD" && strlen($data[$key])
                && 0 == $db->getOne(knjz091a_2Query::staffMstCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:(職員マスタ)", $existenceSep, $val[3]);
            }
            //都道府県マスタ
            if ($key == "PRISCHOOL_PREF_CD" && strlen($data[$key])
                && 0 == $db->getOne(knjz091a_2Query::prefMstCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:(都道府県マスタ)", $existenceSep, $val[3]);
            }
            //市区町村マスタ
            if ($key == "PRISCHOOL_CITY_CD" && strlen($data[$key])
                && 0 == $db->getOne(knjz091a_2Query::cityMstCntSql($data["PRISCHOOL_PREF_CD"], $data["PRISCHOOL_CITY_CD"]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:(市区町村マスタ)", $existenceSep, $val[3]);
            }

            /**********************/
            /**  文字数チェック  **/
            /**********************/
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }

            //フラグチェック(値は1かNULL)
            if ($val[1] == "FLAG" && !($data[$key] == "1" || !strlen(trim($data[$key])))) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$masterErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz091a_2Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 10000) {
            $this->setWarning('MSG915', "処理件数を１万件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }
}
