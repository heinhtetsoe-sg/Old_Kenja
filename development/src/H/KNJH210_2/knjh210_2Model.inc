<?php

require_once('for_php7.php');


class knjh210_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $domicd;        //primary
    var $domicd12;
    var $selectItem = array();

    function knjh210_2Model()
    {
    }

    function init()
    {              
        $this->field = array("DOMI_CD"          =>  VARS::post("DOMI_CD"),        //寮コード
                             "DOMI_NAME"        =>  VARS::post("DOMI_NAME"),      //寮名
                             "DOMI_ZIPCD"       =>  VARS::post("DOMI_ZIPCD"),     //郵便番号
                             "DOMI_ADDR1"       =>  VARS::post("DOMI_ADDR1"),     //住所
                             "DOMI_ADDR2"       =>  VARS::post("DOMI_ADDR2"),     //方書き
                             "DOMI_TELNO"       =>  VARS::post("DOMI_TELNO"),     //電話番号1
                             "DOMI_TELNO2"      =>  VARS::post("DOMI_TELNO2"),    //電話番号1
                             "DOMI_FAXNO"       =>  VARS::post("DOMI_FAXNO"),     //FAX番号
                             "DOMI_LEADER"      =>  VARS::post("DOMI_LEADER"),    //寮長名
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),    //校種
                             "COLLECT_LM_CD"    =>  VARS::post("COLLECT_LM_CD"),  //入金項目
                             "MONTH_MONEY"      =>  VARS::post("MONTH_MONEY"),    //一時退寮費
                             "DAY_MONEY"        =>  VARS::post("DAY_MONEY"),      //一時退寮費(日)
                             "UPDATED"          =>  VARS::post("UPDATED")         //更新日付
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->domicd);
        }
        $temp = VARS::get("DOMI_CD");
        if (isset($temp)){
            $this->domicd      = VARS::get("DOMI_CD");
        }
        $this->domicd12 = $this->field["DOMI_CD"];

        //校種初期値
        $db = Query::dbCheckOut();
        Query::dbCheckIn($db);

        $this->selectItem = explode(",", VARS::post("selectItem"));

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        // 権限チェック 
        if (common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE) {
            $this->setWarning("MSG300");
            return false;
        }

        //キーのチェック
        if (trim($this->field["DOMI_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG901", "(部クラブコード)");
            return false;
        }
        if ($this->domicd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "update" && $this->domicd != $this->domicd12) {
            $this->setWarning("MSG308");
            return;
        }

        //キーのチェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["SCHOOL_KIND"] != "" && $this->field["COLLECT_LM_CD"] == "" ||
                $this->field["SCHOOL_KIND"] == "" && $this->field["COLLECT_LM_CD"] != ""
            ) {
                $this->setWarning("校種と入金項目の指定が不正です。");
                return false;
            }
        }

        //部クラブコードのゼロ埋め
        if (strlen($this->field["DOMI_CD"]) < 3) {
            $this->field["DOMI_CD"] = sprintf("%03d",$this->field["DOMI_CD"]);
        }
        
        //１レコード取得
        $db = Query::dbCheckOut();
        $row = $db->getRow(knjh210_2Query::getRow($this, $this->domicd12), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(部クラブコード)");
            return false;
        }

        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["DOMI_NAME"]) > 120 ) {
            $this->setWarning("MSG901", "(寮名) 120バイトまで");
            return false;
        } elseif (strlen($this->field["DOMI_ZIPCD"]) > 8 ) {
            $this->setWarning("MSG901", "(郵便番号) 8バイトまで");
            return false;
        } elseif (strlen($this->field["DOMI_ADDR1"]) > 150 ) {
            $this->setWarning("MSG901", "(住所1) 150バイトまで");
            return false;
        } elseif (strlen($this->field["DOMI_ADDR2"]) > 150 ) {
            $this->setWarning("MSG901", "(住所2) 150バイトまで");
            return false;
        } elseif (strlen($this->field["DOMI_TELNO"]) > 14 ) {
            $this->setWarning("MSG901", "(電話番号1) 14バイトまで");
            return false;
        } elseif (strlen($this->field["DOMI_TELNO2"]) > 14 ) {
            $this->setWarning("MSG901", "(電話番号2) 14バイトまで");
            return false;
        } elseif (strlen($this->field["DOMI_FAXNO"]) > 14 ) {
            $this->setWarning("MSG901", "(FAX番号) 14バイトまで");
            return false;
        } elseif (strlen($this->field["DOMI_LEADER"]) > 120 ) {
            $this->setWarning("MSG901", "(寮長名) 120バイトまで");
            return false;
        } elseif (strlen($this->field["MONTH_MONEY"]) > 8 ) {
            $this->setWarning("MSG901", "(一時退寮費) 8バイトまで");
            return false;
        } elseif (strlen($this->field["DAY_MONEY"]) > 8 ) {
            $this->setWarning("MSG901", "(一時退寮費(日)) 8バイトまで");
            return false;
        }

        return true;
    }
    
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->domicd = $this->domicd12;
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        // 権限チェック
        if (common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE) {
            $this->setWarning("MSG300");
            return false;
        }

        //キーの更新チェック
        if ($this->domicd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->domicd != $this->domicd12) {
            $this->setWarning("MSG308");
            return;
        }

        $db = Query::dbCheckOut();
        $row = $db->getRow(knjh210_2Query::getRow($this, $this->domicd), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return; 
        }
        if (knjh210_2Query::IsExisting($this, $this->domicd)) {
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }
        knjh210_2Query::getDeleteQuery($this, $this->field);
        unset($this->domicd);
        $this->setMessage("MSG201");
    }
 
    function &insert()
    {
        knjh210_2Query::getInsertQuery($this, $this->field);
        if ($this->Properties["useCollectMoneySchool"] == "1") {
            knjh210_2Query::getDelInsMdomitoryQuery($this, $this->field);
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjh210_2Query::getUpdateQuery($this, $this->field);
        if ($this->Properties["useCollectMoneySchool"] == "1") {
            knjh210_2Query::getDelInsMdomitoryQuery($this, $this->field);
        }
    }

    //変数をクリアします
    function clean()
    {
    }

}
?>
