<?php

require_once('for_php7.php');


class knjj010_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $clubcd;        //primary
    var $clubcd12;
    var $selectItem = array();
    var $schKind_CI;

    function knjj010_2Model()
    {
    }

    function init()
    {              
        $this->field = array("CLUBCD"           =>  VARS::post("CLUBCD"),           //部クラブコード
                             "CLUBNAME"         =>  VARS::post("CLUBNAME"),         //部クラブ名
                             "SDATE"            =>  VARS::post("SDATE"),            //設立日付
                             "ACTIVITY_PLACE"   =>  VARS::post("ACTIVITY_PLACE"),   //活動場所
                             "CLUBROOM_ASSIGN"  =>  VARS::post("CLUBROOM_ASSIGN"),  //部室割り当て
                             "UPDATED"          =>  VARS::post("UPDATED")           //更新日付
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1"){
            unset($this->clubcd);
            unset($this->schKind);
            unset($this->schKind_CI);
            unset($this->selectSchoolKind);
        }
        $temp = VARS::get("CLUBCD");
        if (isset($temp)){
            $this->clubcd      = VARS::get("CLUBCD");
        }
        $this->clubcd12 = $this->field["CLUBCD"];

        if (VARS::request("SCHKIND") != "") {
            $this->schKind = VARS::request("SCHKIND");
        }
        if (VARS::post("SCHKIND_CI") != "") {
            $this->schKind_CI = VARS::post("SCHKIND_CI");
        }
        //校種初期値
        $db = Query::dbCheckOut();
        $firstSchkind = $db->getOne(knjj010_2Query::getSchkind($this));
        Query::dbCheckIn($db);
        $this->schkind = ($this->schkind) ? $this->schkind : $firstSchkind;

        $this->selectItem = explode(",", VARS::post("selectItem"));

        $this->getPropertiesAll();
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        // 権限チェック 
        if(common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE){
            $this->setWarning("MSG300");
            return false;
        }

        //キーのチェック
        if (trim($this->field["CLUBCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG901", "(部クラブコード)");
            return false;
        }
        if ($this->clubcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->cmd == "update" && $this->clubcd != $this->clubcd12) {
            $this->setWarning("MSG308");
            return;
        }

        //部クラブコードのゼロ埋め
        if (strlen($this->field["CLUBCD"]) < 4) {
            $this->field["CLUBCD"] = sprintf("%04d",$this->field["CLUBCD"]);
        }
        
        //１レコード取得
        $db = Query::dbCheckOut();
        $row = $db->getRow(knjj010_2Query::getRow($this, $this->clubcd12), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(部クラブコード)");
            return false;
        }

        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["CLUBNAME"]) > 45 ) {
            $this->setWarning("MSG901", "(部クラブ名) 45バイトまで");
            return false;
        } elseif (strlen($this->field["ACTIVITY_PLACE"]) > 30 ) {
            $this->setWarning("MSG901", "(活動場所) 30バイトまで");
            return false;
        } elseif (strlen($this->field["CLUBROOM_ASSIGN"]) > 30 ) {
            $this->setWarning("MSG901", "(部室割り当て) 30バイトまで");
            return false;
        }

        return true;
    }
    
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }    
    }
    
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->clubcd = $this->clubcd12;
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        // 権限チェック
        if(common::SecurityCheck(STAFFCD,PROGRAMID) != DEF_UPDATABLE){
            $this->setWarning("MSG300");
            return false;
        }

        //キーの更新チェック
        if ($this->clubcd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->clubcd != $this->clubcd12) {
            $this->setWarning("MSG308");
            return;
        }

        $db = Query::dbCheckOut();
        $row = $db->getRow(knjj010_2Query::getRow($this, $this->clubcd), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if (!is_array($row)){
            $this->setWarning("MSG303");
            return; 
        }
        if (knjj010_2Query::IsExisting($this, $this->clubcd)){
            $this->setWarning('MSG919',"年度データに登録されています。");
            return;
        }
        knjj010_2Query::getDeleteQuery($this, $this->field);
        unset($this->clubcd);
        $this->setMessage("MSG201");
    }
 
    function &insert()
    {
        knjj010_2Query::getInsertQuery($this, $this->field);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjj010_2Query::getUpdateQuery($this, $this->field);
    }
    
    //変数をクリアします
    function clean()
    {
    }

    //種目登録の更新
    function &getUpdateSubModel1()
    {
        //入力チェック
        if ($this->clubcd){
            knjj010_2Query::getUpdateSubQuery1($this);
            $this->setMessage('MSG201');
        }    
    }

}
?>
