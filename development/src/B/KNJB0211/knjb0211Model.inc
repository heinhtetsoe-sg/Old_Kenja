<?php

require_once('for_php7.php');


class knjb0211Model extends Model {
    var $field      = array();
    var $cmd;
    var $groupcd;     //primary
    var $selectYear;

    function knjb0211Model()
    {
        ;
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            $this->selectYear = CTRL_YEAR;
            $this->groupcd = "";
        }
        if (VARS::post("SELECT_YEAR")) {
            $this->selectYear = VARS::request("SELECT_YEAR");
        }
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "GROUP_CD"     =>  VARS::post("GROUP_CD"),
                             "GROUP_NAME"   =>  VARS::post("GROUP_NAME"),
                             "GROUP_ABBV"   =>  VARS::post("GROUP_ABBV"),
                             );

        $this->cmd = VARS::request("cmd");

        //リンク用から取得した科目
        if (VARS::get("GROUP_CD")!="") {
            $this->groupcd = VARS::get("GROUP_CD");
        }
        if (VARS::get("GRADE")!="") {
            $this->field["GRADE"] = VARS::get("GRADE");
            $this->schoolKind = VARS::get("SCHOOL_KIND");
        }
    }
    //入力項目チェック
    function validateInput()
    {

        //DB接続
        $db = Query::dbCheckOut();

        //キーのチェック
        if (trim($this->field["GROUP_CD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(グループCD)");
            return false;
        }
        if (trim($this->field["GRADE"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        if ($this->groupcd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308");
            return false;
        }

        $checkArray = array("GROUP_NAME" => "(名称)", "GROUP_ABBV" => "(略称)");
        foreach ($checkArray as $key => $val) {
            if ($this->field[$key] == "") {
                $this->setWarning("MSG301", $val);
                return false;
            }
        }

        if ($this->cmd == "update" && $this->groupcd != $this->field["GROUP_CD"]) {
            $this->setWarning("MSG308","キー値が変更されています。");
            return;
        }

        //１レコード取得
        $query = knjb0211Query::getRow($this->field["GROUP_CD"], $this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG302");
            return false;
        }
        if (strlen($this->field["GROUP_NAME"]) > 60) {
            $this->setWarning("MSG914", "(名称) 60バイトまで");
            return false;
        } else if (strlen($this->field["GROUP_ABBV"]) > 45) {
            $this->setWarning("MSG914", "(略称) 45バイトまで");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //次年度コピー用のモデルを返します
    function &getCopyModel()
    {
        $cntselected = knjb0211Query::cntThisOrder($this);
        $cntlastorder = knjb0211Query::cntLastOrder($this);
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntlastorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjb0211Query::getCopyQuery($this);
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
            $this->groupcd = $this->field["GROUP_CD"];
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
        if ($this->groupcd != $this->field["GROUP_CD"]) {
            $this->setWarning("MSG308","キー値が変更されています。");
            return;
        }

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjb0211Query::getRow($this->field["GROUP_CD"], $this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;
        }

        knjb0211Query::getDeleteQuery($this, $this->field);
        unset($this->groupcd);
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        knjb0211Query::getInsertQuery($this, $this->field);
    }

    function &update()
    {
        knjb0211Query::getUpdateQuery($this, $this->field);
    }

    function clean()
    {
    }
}
?>
