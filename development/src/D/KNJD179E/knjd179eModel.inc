<?php

require_once('for_php7.php');

class knjd179eModel extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd179eModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        $this->field = array("TYUI_TYOUKA"      =>  VARS::post("TYUI_TYOUKA")      //注意・超過ラジオボタン
                             );

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        //プロパティファイル
        $this->getPropertiesAll();

    }
}
?>
