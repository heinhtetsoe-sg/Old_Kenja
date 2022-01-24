<?php

require_once('for_php7.php');

class knjm701dModel extends Model {
    var $cmd;
    var $control;        //コントロールマスタ
    var $field = array();

    function knjm701dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->field);
        }

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "ATTEND_HIST"      =>  VARS::post("ATTEND_HIST")       //出席履歴
                             );
        $this->getPropertiesAll(); //プロパティファイルの読込み

    }
}
?>
