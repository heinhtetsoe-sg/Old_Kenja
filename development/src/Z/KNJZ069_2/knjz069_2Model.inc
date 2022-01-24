<?php

require_once('for_php7.php');

//定数定義

class knjz069_2Model extends Model {
    var $field      = array();
    var $cmd;

    //メンバ変数の初期化
    function knjz069_2Model()
    {
    }

    function init()
    {
        $this->cmd             = VARS::request("cmd");

        $this->field = array(
            "ELEMENT_DIV"     =>    VARS::post("ELEMENT_DIV"),    //評価の要素区分
            "ELEMENT_CD"      =>    VARS::post("ELEMENT_CD"),     //評価の要素コード
            "ELEMENT_NAME"    =>    VARS::post("ELEMENT_NAME"),   //評価の要素名称
        );

        if (VARS::get("ELEMENT_DIV")) {
            $this->element_div = VARS::get("ELEMENT_DIV");
        }
        if (VARS::get("ELEMENT_CD")) {
            $this->element_cd = VARS::get("ELEMENT_CD");
        }
        if (VARS::get("ELEMENT_NAME")) {
            $this->element_name = VARS::get("ELEMENT_NAME");
        }
    }

    //入力項目チェック
    function validateInput($kind)
    {
        //必須項目チェック
        if (!$this->field["ELEMENT_DIV"]) {
            $this->setWarning("MSG310", "( 評価の要素区分 )");
            return false;
        }
        if (!$this->field["ELEMENT_CD"]) {
            $this->setWarning("MSG310", "( 評価の要素コード )");
            return false;
        }

        //文字数チェック
        if (strlen($this->field["ELEMENT_CD"]) > 3 ) {
            $this->setWarning("MSG915", "( 評価の要素コード ) 3バイトまで");
            return false;
        }
        if (strlen($this->field["ELEMENT_NAME"]) > 39 ) {
            $this->setWarning("MSG915", "( 評価の要素名称 ) 39バイトまで");
            return false;
        }

        //更新チェック
        if ($kind=="update" || $kind=="delete") {
            if ($this->element_div != $this->field["ELEMENT_DIV"] || $this->element_cd != $this->field["ELEMENT_CD"]){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
        }
        //登録、更新チェック
        if ($kind == "insert") {
            $row = knjz069_2Query::getRow($this->field["ELEMENT_DIV"], $this->field["ELEMENT_CD"]);
            if (get_count($row) > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        }

        return true;
    }

    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")){
            knjz069_2Query::getInsertQuery($this->field);
            $this->element_div = $this->field["ELEMENT_DIV"];
            $this->element_cd = $this->field["ELEMENT_CD"];
            $this->setMessage("MSG201");
        }
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")){
            knjz069_2Query::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
    }

    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")){
            knjz069_2Query::getDeleteQuery($this->field);
            $this->setMessage("MSG201");
            $this->element_div = "";
            $this->element_cd = "";
        }
    }
}
?>
