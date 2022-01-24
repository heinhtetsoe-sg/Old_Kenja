<?php

require_once('for_php7.php');


class knjp706aModel extends Model {

    var $cmd;

    function knjp706aModel() {
    }
 
    function init()
    {
        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->cmd == "search") {
            $this->search = array ( "SEARCH_DIV"    => VARS::get("LEFT_SEARCH_DIV"),
                                    "HR_CLASS_HYOUJI_FLG"  => VARS::get("LEFT_HR_CLASS_HYOUJI_FLG"),
                                    "GRADE"         => VARS::get("LEFT_GRADE"),
                                    "HR_CLASS"      => VARS::get("LEFT_HR_CLASS"),
                                    "ENT_YEAR"      => VARS::get("LEFT_ENT_YEAR"),
                                    "GRD_YEAR"      => VARS::get("LEFT_GRD_YEAR"),
                                    "SCHREGNO"      => VARS::get("LEFT_SCHREGNO"),
                                    "NAME"          => VARS::get("LEFT_NAME"),
                                    "NAME_KANA"     => VARS::get("LEFT_NAME_KANA"),
                                    "A028"          => VARS::get("LEFT_A028"),
                                    "PAID_UMU"      => VARS::get("LEFT_PAID_UMU")
            );
        }
        if ($this->cmd == "") {
            unset($this->search);
        }

        //フィールド
        $this->field = array ("PAID_MONEY_DATE" => VARS::post("PAID_MONEY_DATE"),
                              "PAID_FLG"        => VARS::post("PAID_FLG"),
                              "UN_ENT"          => VARS::post("UN_ENT")
        );
    }
    
    //データチェック
    function dataCheck()
    {
        return true;
    }
    
    //更新処理
    function &getUpdateModel()
    {
        if (!$this->dataCheck()) {
            return;
        }
        knjp706aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
        return;
    }
}
?>
