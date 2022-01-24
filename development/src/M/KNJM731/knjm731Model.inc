<?php

require_once('for_php7.php');

class knjm731Model extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjm731Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->field = array("SEARCH_DIV"       =>  VARS::post("SEARCH_DIV"),
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),
                            );

        $schregno_array = explode(',',$this->data["SCHREGNO"]);
        foreach($schregno_array as $key => $schregno) {
            $this->data["MUSYOU_KAISU".$schregno]     = VARS::post("MUSYOU_KAISU".$schregno);
        }

        $this->cmd = VARS::request("cmd");
    }

    //更新用のモデルを返します
    function &getUpdateModel() {

        $db = Query::dbCheckOut();

        $schregno_array = explode(',',$this->data["SCHREGNO"]);
        foreach($schregno_array as $key => $schregno) {
            //更新
            knjm731Query::getDetailUpdateQuery($db, $schregno, $this->data["MUSYOU_KAISU".$schregno]);
        }

        $this->cmd = "knjm731";
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }
}
?>
