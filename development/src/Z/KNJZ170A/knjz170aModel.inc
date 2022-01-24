<?php

require_once('for_php7.php');


class knjz170aModel extends Model {
    var $field      = array();
    var $cmd;
    var $year;        //primary
    var $groupcd;     //primary
    var $selectYear;

    function knjz170aModel()
    {
        ;
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            $this->year = "";
            $this->selectYear = CTRL_YEAR;
            $this->groupcd = "";
        }
        if (VARS::post("SELECT_YEAR")) {
            $this->selectYear = VARS::post("SELECT_YEAR");
        }
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "GROUPCD"      =>  VARS::post("GROUPCD"),
                             "GROUPNAME"    =>  VARS::post("GROUPNAME"),
                             "GROUPABBV"    =>  VARS::post("GROUPABBV"),
                             "REMARK"       =>  VARS::post("REMARK"),
                             "SHOWORDER"    =>  VARS::post("SHOWORDER"),
                             "UPDATED"      =>  VARS::post("UPDATED")
                             );

        $this->cmd      = VARS::request("cmd");
        
        //リンク用から取得した科目
        if (VARS::get("YEAR")!="") {
            $this->year    = VARS::get("YEAR");
        }
        if (VARS::get("GROUPCD")!="") {
            $this->groupcd    = VARS::get("GROUPCD");
        }
        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (trim($this->field["GROUPCD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(群コード)");
            return false;
        }
        if ($this->groupcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }

        $checkArray = array("GROUPNAME" => "(群名称)", "GROUPABBV" => "(群略称)");
        foreach ($checkArray as $key => $val) {
            if ($this->field[$key] == "") {
                $this->setWarning("MSG301", $val);
                return false;
            }
        }

        if ($this->cmd == "update" && $this->groupcd != $this->field["GROUPCD"]) {
            $this->setWarning("MSG308","キー値が変更されています。");
            return;
        }
        //１レコード取得
        $row = knjz170aQuery::getRow($this->field["GROUPCD"], $this);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG302", "(群コード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]){
            $this->setWarning("MSG307");
            return false;
        }
        if (strlen($this->field["GROUPNAME"]) >9 ) {
            $this->setWarning("MSG914", "(群名称) 9バイトまで");
            return false;
        } elseif (strlen($this->field["GROUPABBV"]) >6 ) {
            $this->setWarning("MSG914", "(群略称) 6バイトまで");
            return false;
        } elseif (strlen($this->field["REMARK"])>90){ 
            $this->setWarning("MSG914", "(備考) 90バイトまで");
            return false;
        } elseif (strlen($this->field["SHOWORDER"])> 4){ 
            $this->setWarning("MSG914", "(表示順) 4バイトまで");
            return false;
        }
        return true;
    }
    
    //次年度コピー用のモデルを返します
    function &getCopyModel()
    {
        $cntselected = knjz170aQuery::cntThisOrder($this);
        $cntlastorder = knjz170aQuery::cntLastOrder($this);
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntlastorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjz170aQuery::getCopyQuery($this);
            if ( !isset($model->warning)){
                $this->setWarning('MSG201',"（前年度からコピー）");
            }
            return $result;
        }
    }
    
    
    /**
     * 編集完了用のモデルを返します
    */
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
            $this->groupcd = $this->field["GROUPCD"];
            $this->setMessage("MSG201");
        }
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->groupcd == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->groupcd != $this->field["GROUPCD"]) {
            $this->setWarning("MSG308","キー値が変更されています。");
            return;
        }
         $row = knjz170aQuery::getRow($this->groupcd, $this);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        knjz170aQuery::getDeleteQuery($this, $this->field);
        unset($this->groupcd);
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        knjz170aQuery::getInsertQuery($this, $this->field);
    }

    function &update()
    {
        knjz170aQuery::getUpdateQuery($this, $this->field);
    }

    function clean()
    {
    }
}
?>
