<?php

require_once('for_php7.php');

class knjd625iModel extends Model {
    var $field      = array();
    var $cmd;

    function knjd625iModel()
    {
    }

    function init()
    {
        $this->field = array(
                             "MOCKCD"		    =>  VARS::post("MOCKCD"),
                             "COURSECODE"	    =>  VARS::post("COURSECODE"),
                             "MOCK_SUBCLASS_CD"	=>  VARS::post("MOCK_SUBCLASS_CD")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("PROGRAMID")){
            unset($this->mockcd);
            unset($this->cocediv);
            unset($this->mocksubclassdiv);
        }
        $datacd = VARS::request("DATACD");
        if (isset($datacd)){
            $data = explode(',',$datacd);
            $this->mockcd  = $data[0];
            $this->cocediv = $data[1];
            $this->mocksubclassdiv = $data[2];
        }
        $this->pre_year = CTRL_YEAR - 1;
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->field["MOCKCD"] == "" || $this->field["COURSECODE"] == "") {
            $this->setWarning("MSG301");
            return false;
        }
        if($this->cmd == "add"){
            //存在チェック
            $row = knjd625iQuery::getMockTotalSubclassDat($this->field["MOCKCD"], $this->field["COURSECODE"]);
            if (is_array($row)) {
                $this->setWarning("MSG302");
                return false;
            }
        }

        if($this->cmd == "update"){
            //コンボボックス変更チェック
            if ($this->mockcd != $this->field["MOCKCD"] || $this->cocediv != $this->field["COURSECODE"]) {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //存在チェック
            $row = knjd625iQuery::getMockTotalSubclassDat($this->field["MOCKCD"], $this->field["COURSECODE"]);
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        if($this->cmd == "delete"){
            //コンボボックス変更チェック
            if ($this->mockcd != $this->field["MOCKCD"] || $this->cocediv != $this->field["COURSECODE"] || $this->mocksubclassdiv != $this->field["MOCK_SUBCLASS_CD"]) {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //存在チェック
            $row = knjd625iQuery::getMockTotalSubclassDat($this->field["MOCKCD"], $this->field["COURSECODE"]);
            if (!is_array($row)){
                $this->setWarning("MSG303");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjd625iQuery::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }    
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjd625iQuery::getInsertQuery($this->field);
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //削除処理
        if ($this->validateInput()){
            knjd625iQuery::getDeleteQuery($this->field);
            $this->mockcd = "";
            $this->cocediv = "";
            $this->mocksubclassdiv = "";
            $this->setMessage("MSG201");
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
    * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $preYear_cnt = $db->getOne(knjd625iQuery::getCopyDataOld($this->pre_year, 'cnt'));
        //前年度データチェック
        if ($preYear_cnt == 0) {
            $this->setWarning("MSG203", "前年度データがありません。");
            return false;  
        }
        Query::dbCheckIn($db);
        knjd625iQuery::getCopyQuery($this);
        $this->setMessage("MSG201");
    }

}
?>