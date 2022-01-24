<?php

require_once('for_php7.php');

class knja224dModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectdata;
    var $schoolName;
    var $schoolCode;

    function knja224dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;       //職員コード
    }

    function init() {
        $this->field = array("FRM_PATERN"       =>  VARS::post("FRM_PATERN"),       //帳票種類
                             "KANA_PRINT"       =>  VARS::post("KANA_PRINT"),       //ふりがな出力
                             "KENSUU"           =>  VARS::post("KENSUU"),           //出力件数
                             "GRD_NAME_NASI"    =>  VARS::post("GRD_NAME_NASI"),    //除籍者名前無し
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->selectdata  = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (method_exists($this, 'getPrgDefaultVal')) {
            $this->getPrgDefaultVal("KNJA224D", SCHOOLCD, SCHOOLKIND);
        }
    }
}
?>
