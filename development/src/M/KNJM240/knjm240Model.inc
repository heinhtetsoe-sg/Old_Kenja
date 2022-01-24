<?php

require_once('for_php7.php');

class knjm240Model extends Model {

    var $field      = array();
    var $cmd;
    var $Year;
    var $AddorUp;               //登録判定用
    var $subclasscd;            //科目コード
    var $chaircd;               //講座コード
    var $subclass_show;          //講座名

    //更新するフィールド
    var $item = array("KAMOKU",
                      "SCHCNT",
                      "CHECKCNT"
                      );

    var $control_data = array();    //コントロールマスタデータを保持

    function knjm240Model()
    {
       common::GetControlMaster_Fnc($this->control_data);

    }
    function init()
    {
        if (VARS::get("CHAIRCD") != "")     $this->chaircd     = VARS::get("CHAIRCD");
        if (VARS::get("SUBCLASSCD") != "")     $this->subclasscd     = VARS::get("SUBCLASSCD");
        if (VARS::get("SUBCLASS_SHOW") != "")     $this->subclass_show     = VARS::get("SUBCLASS_SHOW");

        if (VARS::post("GrYEAR")) {
            $this->Year = VARS::post("GrYEAR");
        }

        if (VARS::get("GetYear")) {
            $this->Year = VARS::get("GetYear");
        }
        $this->cmd       = VARS::request("cmd");
        if ($this->cmd == 'init' || VARS::get("PROGRAMID")) {
            unset($this->chaircd);
            unset($this->subclasscd);
            unset($this->subclass_show);
        }
        foreach ($this->item as $field) {
            $this->field[$field] = VARS::post($field);
        }
        $this->getPropertiesAll(); //プロパティファイル
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->subclasscd == '' || $this->chaircd == '') {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->field["SCHCNT"] == '') {
            $this->setWarning("MSG304", "(回数)");
            return false;
        }

        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if ($this->AddorUp == "up"){
                $this->update();
            } else {
                $this->insert();
            }
            $this->setMessage("MSG201");
        }
    }

    function &getInsertModel()
    {
    }
    /**
     * アップデート時の内部メソッド
     */
    function &insert()
    {
        knjm240Query::getInsertQuery($this);
    }
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        knjm240Query::getUpdateQuery($this);
    }

}
?>
