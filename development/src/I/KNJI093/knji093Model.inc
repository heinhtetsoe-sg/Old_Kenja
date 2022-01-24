<?php

require_once('for_php7.php');

class knji093Model extends Model {
    var $field = array();
    var $cmd;
    var $control;

    function knji093Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),        //年度
                            "SCHOOL_KIND"   =>  VARS::post("SCHOOL_KIND"), //校種
                            "MIKOMI"        =>  VARS::post("MIKOMI"),      //卒業見込み出力
                            "GAKKI"         =>  VARS::post("GAKKI"),       //学期コード
                            "CLASS_MAJOR"   =>  VARS::post("CLASS_MAJOR"), //クラスor学科
                            "DBNAME"        =>  VARS::post("DBNAME")       //DB変更に備えて
                            );

        $this->cmd  = VARS::request("cmd");
        if ($this->cmd == "changeSchKind") { 
            $this->field["CLASS_MAJOR"] = "";
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
