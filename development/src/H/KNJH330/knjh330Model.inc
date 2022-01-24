<?php

require_once('for_php7.php');

class knjh330Model extends Model {
    var $field      = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $mockdiv = "";       //primary

    function knjh330Model()
    {
    }

    function init()
    {
        $this->field = array("MOCK_DIV"	    =>  VARS::post("MOCK_DIV"),
                             "MOCKCD"		=>  VARS::post("MOCKCD"),
                             "MOCKNAME1"	=>  VARS::post("MOCKNAME1"),
                             "MOCKNAME2"	=>  VARS::post("MOCKNAME2"),
                             "MOCKNAME3"	=>  VARS::post("MOCKNAME3")
                             );

        $this->cmd          = VARS::request("cmd");
        if (VARS::get("mode") == "1" || VARS::get("PROGRAMID")){
            unset($this->mockcd);
            unset($this->mockdiv);
        }
        $temp = VARS::get("MOCKCD");
        if (isset($temp)){
            $this->mockcd = substr(VARS::get("MOCKCD"), 1);
            $this->mockdiv = substr(VARS::get("MOCKCD"), 0, 1);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if ((trim($this->field["MOCK_DIV"]) == "" || trim($this->field["MOCKCD"]) == "") && $this->cmd == "add") {
            $this->setWarning("MSG301","(模試コード)");
            return false;
        }
        if (strlen($this->field["MOCKNAME1"]) == 0) {
            $this->setWarning("MSG301","(模試名称)");
            return false;
        }

        if (($this->mockdiv == "" || $this->mockcd == "" ) && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && ($this->mockdiv != $this->field["MOCK_DIV"] || $this->mockcd != $this->field["MOCKCD"])) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $row = knjh330Query::getRow($this->field["MOCK_DIV"].$this->field["MOCKCD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG918", "(模試コード)");
            return false;
        }
        //文字数チェック
        $checkStrLen["MOCKNAME1"] = array("LEN" => 60, "MESAGE" => "(模試名称)");
        $checkStrLen["MOCKNAME2"] = array("LEN" => 60, "MESAGE" => "(模試略称１)");
        $checkStrLen["MOCKNAME3"] = array("LEN" => 60, "MESAGE" => "(模試略称２)");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
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
            $this->update();
            $this->setMessage("MSG201");
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->mockcd = $this->field["MOCKCD"];
            $this->mockdiv = $this->field["MOCK_DIV"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel()
    {
        //キーの更新チェック
        if ($this->mockdiv == "" || $this->mockcd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->mockdiv != $this->field["MOCK_DIV"] || $this->mockcd != $this->field["MOCKCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
         $row = knjh330Query::getRow($this->mockdiv.$this->mockcd);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
		knjh330Query::getDeleteQuery($this->field);
        $this->mockcd = "";
        $this->mockdiv = "";
        $this->setMessage("MSG201");
    }

    function &insert()
    {
        $result = knjh330Query::getInsertQuery($this->field);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $result = knjh330Query::getUpdateQuery($this->field);
        return $result;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
