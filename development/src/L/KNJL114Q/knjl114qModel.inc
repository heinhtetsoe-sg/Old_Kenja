<?php

require_once('for_php7.php');

class knjl114qModel extends model {
    var $cmd;
    var $applicantdiv;
    var $hid_examno;
    var $ObjYear;
    var $artSelFstLst;
    var $artSelSndLst;
    var $examNoLst;

    function knjl114qModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examhallcd);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "2";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->hid_examno       = VARS::post("HID_EXAMNO");

        $this->artSelFstLst = array();
        $this->artSelSndLst = array();
        $this->examNoLst = explode(",", $this->hid_examno);
        foreach ($this->examNoLst as $key => $examno) {
            $this->artSelFstLst[$examno]     = VARS::post("ART_SELECT_FST-".$examno);
            $this->artSelSndLst[$examno]     = VARS::post("ART_SELECT_SND-".$examno);
        }
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //更新
         knjl114qQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
