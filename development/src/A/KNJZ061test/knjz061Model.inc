<?php

require_once('for_php7.php');


class knjz061Model extends Model {
    var $field      = array();
    var $cmd;
    var $classcd = "";        //primary
    var $school_kind = "";    //primary

    function knjz061Model()
    {
    }

    function init()
    {
        $this->field = array("CLASSCD"          =>  VARS::post("CLASSCD"),
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),           //学校種別
                             
                             "CLASSNAME"        =>  VARS::post("CLASSNAME"),
                             "CLASSABBV"        =>  VARS::post("CLASSABBV"),
                             "CLASSNAME_ENG"    =>  VARS::post("CLASSNAME_ENG"),
                             "CLASSABBV_ENG"    =>  VARS::post("CLASSABBV_ENG"),
                             "CLASSORDERNAME1"  =>  VARS::post("CLASSORDERNAME1"),
                             "CLASSORDERNAME2"  =>  VARS::post("CLASSORDERNAME2"),
                             "CLASSORDERNAME3"  =>  VARS::post("CLASSORDERNAME3"),
                             "SUBCLASSES"       =>  VARS::post("SUBCLASSES"),
                             "SHOWORDER"        =>  VARS::post("SHOWORDER"),
                             "SHOWORDER2"       =>  VARS::post("SHOWORDER2"),
                             "SHOWORDER3"       =>  VARS::post("SHOWORDER3"),
                             "SHOWORDER4"       =>  VARS::post("SHOWORDER4"),
                             "ELECTDIV"         =>  VARS::post("ELECTDIV"),
                             "SPECIALDIV"       =>  VARS::post("SPECIALDIV"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd             = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        
        if (VARS::get("mode") == "1"){
            unset($this->classcd);
        }
        $temp = VARS::get("CLASSCD");
        if (isset($temp)){
            $this->classcd      = VARS::get("CLASSCD");
            $this->school_kind  = VARS::get("SCHOOL_KIND");
        }

        if ($this->cmd == '') {
            unset($this->send_prgid);
            unset($this->sendAuth);
        }

        //呼出された場合
        if (VARS::get("SEND_PRGID")) {
            $this->send_prgid = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        $this->auth = $this->sendAuth ? $this->sendAuth : AUTHORITY;
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["CLASSCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(教科コード)");
            return false;
        }
        //キーのチェック
        if (trim($this->field["SCHOOL_KIND"]) == "" && $this->cmd == "add"){
            $this->setWarning("MSG301", "(学校校種)");
            return false;
        }
        if ($this->classcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->classcd != $this->field["CLASSCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        if ($this->school_kind == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        //キー値が変更されていますかをチェック
        if ($this->cmd == "update" && $this->field["SCHOOL_KIND"] != $this->school_kind){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        //教科コードのゼロ埋め
        if (strlen($this->field["CLASSCD"]) < 2) {
            $this->field["CLASSCD"] = sprintf("%02d",$this->field["CLASSCD"]);
        }
        //１レコード取得
        $row = knjz061Query::getRow($this->field["CLASSCD"], $this, $this->field["SCHOOL_KIND"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(教科コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["CLASSNAME"]) > 30 ) {
            $this->setWarning("MSG915", "(教科名) 30バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSABBV"]) > 15 ) {
            $this->setWarning("MSG915", "(教科略称) 15バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSNAME_ENG"]) > 40 ) {
            $this->setWarning("MSG915", "(教科名英字) 40バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSABBV_ENG"]) > 30 ) {
            $this->setWarning("MSG915", "(教科略称英字) 30バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSORDERNAME1"]) > 60 ) {
            $this->setWarning("MSG915", "(調査書用教科名) 60バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSORDERNAME2"]) > 60 ) {
            $this->setWarning("MSG915", "(教科名その他２) 60バイトまで");
            return false;
        } elseif (strlen($this->field["CLASSORDERNAME3"]) > 60 ) {
            $this->setWarning("MSG915", "(教科名その他３) 60バイトまで");
            return false;
        } elseif (strlen($this->field["SUBCLASSES"]) > 2 ) {
            $this->setWarning("MSG915", "(科目数) 2バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER"]) > 2 ) {
            $this->setWarning("MSG915", "(表示順) 2バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER2"]) > 2 ) {
            $this->setWarning("MSG915", "(調査書用表示順) 2バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER3"]) > 2 ) {
            $this->setWarning("MSG915", "(通知表用表示順) 2バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER4"]) > 2 ) {
            $this->setWarning("MSG915", "(成績一覧用表示順) 2バイトまで");
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
            //教育課程用追加
            $this->classcd = $this->field["CLASSCD"];
            $this->setMessage("MSG201");
        }
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->classcd      = $this->field["CLASSCD"];
            $this->school_kind  = $this->field["SCHOOL_KIND"];
            
            $this->setMessage("MSG201");
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->classcd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->classcd != $this->field["CLASSCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //キーの更新チェック
        if ($this->school_kind == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        //キー値が変更されていますかをチェック
        if ($this->field["SCHOOL_KIND"] != $this->school_kind){
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        $row = knjz061Query::getRow($this->classcd, $this, $this->school_kind);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        /*if (knjz061Query::IsExisting($this->classcd, $this, $this->school_kind)){
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }*/
        knjz061Query::getDeleteQuery($this->field, $this, $this->school_kind);
        //$this->classcd="";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        knjz061Query::getInsertQuery($this->field, $this, $this->school_kind);
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz061Query::getUpdateQuery($this->field, $this, $this->school_kind);
    }
    
    //変数をクリアします
    function clean()
    {
    }
}
?>
