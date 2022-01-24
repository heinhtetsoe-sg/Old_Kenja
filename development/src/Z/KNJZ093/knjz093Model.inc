<?php

require_once('for_php7.php');

class knjz093Model extends Model {
    var $field = array();
    var $cmd;
    var $finschoolcd;        //primary
    var $selectFinschoolType;
    var $auth;
    var $prgid;

    function knjz093Model() {}

    function init() {
        $this->getPropertiesAll();
        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7);

        if (VARS::get("PROGRAMID") != "") {
            unset($this->selectFinschoolType);
            unset($this->finschoolcd);

            unset($this->sendAuth);
            unset($this->sendPrgid);
            unset($this->sendSubmit);
        }

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
                             "FINSCHOOL_PREF_CD"   => VARS::post("FINSCHOOL_PREF_CD"),   //都道府県
                             "FINSCHOOL_ZIPCD"     => VARS::post("FINSCHOOL_ZIPCD"),     //郵便番号
                             "FINSCHOOL_ADDR1"     => VARS::post("FINSCHOOL_ADDR1"),     //住所
                             "FINSCHOOL_ADDR2"     => VARS::post("FINSCHOOL_ADDR2"),     //住所
                             "FINSCHOOL_TELNO"     => VARS::post("FINSCHOOL_TELNO"),     //電話番号
                             "FINSCHOOL_FAXNO"     => VARS::post("FINSCHOOL_FAXNO"),     //FAX番号
                             "EDBOARDCD"           => VARS::post("EDBOARDCD"),           //教育委員会コード
                             "DISTRICT_NAME"       => VARS::post("DISTRICT_NAME"),       //所在地コード
                             "UPDATED"             => VARS::post("UPDATED")
                             );
        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "changeType") {
            $this->selectFinschoolType = VARS::post("SELECT_FINSCHOOL_TYPE");
        }

        if (VARS::get("mode") == "1"){
            unset($this->finschoolcd);
            unset($this->selectFinschoolType);
        }

        $temp = VARS::get("FINSCHOOLCD");
        if (isset($temp)){
            $this->finschoolcd  = VARS::get("FINSCHOOLCD");
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->sendPrgid    = VARS::get("SEND_PRGID");
            $this->sendSubmit   = VARS::get("SEND_SUBMIT");
        }

        $this->auth = ($this->sendAuth == "") ? AUTHORITY : $this->sendAuth;
        $this->prgid = ($this->sendPrgid == "") ? PROGRAMID : $this->sendPrgid;

    }

    //入力項目チェック
    function validateInput() {

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //キーのチェック
        if (trim($this->field["FINSCHOOLCD"]) == "" && $this->cmd == "add") {
            $this->setWarning('MSG901',"( 学校コード )");
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
        if (strlen($this->field["FINSCHOOLCD"]) < $this->finschoolcdKeta){
            $this->field["FINSCHOOLCD"] = sprintf("%0".$this->finschoolcdKeta."d", $this->field["FINSCHOOLCD"]);
        }

        //１レコード取得
        $query = knjz093Query::getSchoolData($this->field["FINSCHOOLCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //存在チェック
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning('MSG303');
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning('MSG918', "( 学校コード )");
            return false;
        }

        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning('MSG307');
            return false;
        }

        if ($this->cmd == "add") {
            if ($this->sendSubmit == "") {
                //学校別出身学校外テーブル存在チェック
                $query = knjz093Query::chkEdboardFinschoolDat($this->field["FINSCHOOLCD"]);
                $edboard = $db->getRow($query);
                if (is_array($edboard)){
                    $this->setWarning('MSG918',"学校に登録済み");
                    return;
                }
            } else {
                if ($this->iinkai == "") {
                    //重複チェック（教育委員会）
                    $query = knjz093Query::getSchoolData($this->field["FINSCHOOLCD"]);
                    $row = $db2->getRow($query, DB_FETCHMODE_ASSOC);
                    if (is_array($row)){
                        $this->setWarning('MSG918',"教育委員会に登録済み");
                        return;
                    }

                    //教育委員会用学校コード存在チェック
                    if ($this->edboard_schoolcd == ""){
                        $this->setWarning('MSG305',"教育委員会用学校コード未登録");
                        return;
                    }
                }
            }
        }

        //文字型チェック
        foreach(array("FINSCHOOL_TELNO"   => array("[^-|0-9]",        "( 電話番号 )"),
                      "FINSCHOOL_FAXNO"   => array("[^-|0-9]",        "( FAX番号 )")
                ) as $key => $val
        ) {
            if ($this->field[$key] !="" && preg_match('{' . $val[0] . '}i', trim($this->field[$key]))) {
                $this->setWarning("MSG901", $val[1]);
                return false;
            }
        }

        //桁数チェック
        if (strlen($this->field["FINSCHOOLCD"]) > $this->finschoolcdKeta) {
            $this->setWarning('MSG901', "( 学校コード ) ".$this->finschoolcdKeta."バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_NAME"]) > 75) {
            $this->setWarning('MSG901', "( 学校名 ) 75バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_KANA"]) > 75) {
            $this->setWarning('MSG901', "( 学校名かな) 75バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_NAME_ABBV"]) > 30) {
            $this->setWarning('MSG901', "( 学校名略称 ) 30バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_KANA_ABBV"]) > 75) {
            $this->setWarning('MSG901', "( 学校名かな略称) 75バイトまで");
            return false;
        } else if (strlen($this->field["PRINCNAME"]) > 60) {
            $this->setWarning('MSG901', "( 学校長氏名 ) 60バイトまで");
            return false;
        } else if (strlen($this->field["PRINCNAME_SHOW"]) > 30) {
            $this->setWarning('MSG901', "( 学校長氏名表示用 ) 30バイトまで");
            return false;
        } else if (strlen($this->field["PRINCKANA"]) > 120) {
            $this->setWarning('MSG901', "( 学校長氏名カナ ) 120バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_ZIPCD"]) > 8) {
            $this->setWarning('MSG901', "( 郵便番号 ) 8バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_ADDR1"]) > 90) {
            $this->setWarning('MSG901', "( 住所 ) 90バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_ADDR2"]) > 90){
            $this->setWarning('MSG901', "( 方書き(アパート名等) ) 90バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_TELNO"]) > 14) {
            $this->setWarning('MSG901', "( 電話番号 ) 14バイトまで");
            return false;
        } else if (strlen($this->field["FINSCHOOL_FAXNO"]) > 14) {
            $this->setWarning('MSG901', "( FAX番号 ) 14バイトまで");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }    
    }

    //追加用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->finschoolcd = $this->field["FINSCHOOLCD"];
            $this->setMessage('MSG201');
        }
    }

    //追加
    function &insert() {
        //追加 -- FINSCHOOL_MST
        knjz093Query::getInsertQuery($this->field);

        //追加 -- EDBOARD_FINSCHOOL_DAT （学校側のとき）
        if ($this->sendSubmit != "") {
            knjz093Query::insertEdboardFinschoolDat($this, $this->field);
        }
    }

    //更新
    function &update() {
        //更新 -- FINSCHOOL_MST
        knjz093Query::getUpdateQuery($this->field);
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //キーの更新チェック
        if ($this->finschoolcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        if ($this->finschoolcd != $this->field["FINSCHOOLCD"]) {
            $this->setWarning('MSG308');
            return;
        }

        //存在チェック
        $row = $db->getRow(knjz093Query::getSchoolData($this->finschoolcd), DB_FETCHMODE_ASSOC);
        if (!is_array($row)){
            $this->setWarning('MSG303');
            return;
        }

        //年度データの存在チェック
        if (knjz093Query::IsExisting($db, $this->finschoolcd)){
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }

        if ($this->sendSubmit == "") {
            //学校別出身学校外テーブル存在チェック
            $query = knjz093Query::chkEdboardFinschoolDat($this->finschoolcd);
            $edboard = $db2->getRow($query);
            if (is_array($edboard)){
                $this->setWarning('MSG919',"学校に登録されています。");
                return;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //削除
        knjz093Query::getDeleteQuery($this, $this->field);
        $this->setMessage('MSG201');
        unset($this->finschoolcd);
        unset($this->field);
    }
}
?>
