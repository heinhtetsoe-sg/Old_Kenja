<?php

require_once('for_php7.php');

class knjl025qModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $ObjYear;
    var $fields = array();

    function knjl025qModel() {}

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->sort         = strlen(VARS::post("SORT")) ? VARS::post("SORT") : VARS::post("HID_SORT");
        $this->show         = strlen(VARS::post("SHOW")) ? VARS::post("SHOW") : VARS::post("HID_SHOW");
        $this->fs_cd        = strlen(VARS::post("FS_CD")) ? VARS::post("FS_CD") : VARS::post("HID_FS_CD");

        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->fields = array();

        //再配置
        $arr_examno = explode(",", $this->hid_examno);
        foreach ($arr_examno as $key => $examno) {
            $this->fields["SCORE1"][$examno] = VARS::post("SCORE1"."-".$examno);
            $this->fields["SCORE2"][$examno] = VARS::post("SCORE2"."-".$examno);
            $this->fields["KAKUYAKU_FLG"][$examno] = VARS::post("KAKUYAKU_FLG"."-".$examno);
        }

        //更新
        knjl025qQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
