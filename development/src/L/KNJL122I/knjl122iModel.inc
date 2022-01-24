<?php
class knjl122iModel extends model {
    var $cmd;
    var $ObjYear;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $examhallcd;
    var $replace_data = array();

    function knjl122iModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->examhallcd);
        }

        $this->cmd = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = 2; //固定値(2:高校)
        $this->exam_type    = 1; //固定値
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->examhallcd   = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");

        $hid_examno = VARS::post("HID_EXAMNO");
        $examnoArray = explode(",", $hid_examno);
        $this->interview = array();
        if (count($examnoArray) > 0) {
            foreach ($examnoArray as $key => $examno) {
                $this->interview[$examno]["INTERVIEW_A"] = VARS::post("INTERVIEW_A-{$examno}");
                $this->interview[$examno]["INTERVIEW_B"] = VARS::post("INTERVIEW_B-{$examno}");
                $this->interview[$examno]["INTERVIEW_C"] = VARS::post("INTERVIEW_C-{$examno}");
                $this->interview[$examno]["ATTEND_FLG"]  = VARS::post("ATTEND_FLG-{$examno}");
            }
        }

        //一括更新
        if ($this->cmd == "replace_update") {
            $this->replace_data = array();
            $this->replace_data["selectdata"] = VARS::post("selectdata");
            $this->replace_data["CHECK_ALL"] = VARS::post("CHECK_ALL");

            $this->replace_data["check"]["INTERVIEW_A"] = VARS::post("CHECK_INTERVIEW_A");
            $this->replace_data["check"]["INTERVIEW_B"] = VARS::post("CHECK_INTERVIEW_B");
            $this->replace_data["check"]["INTERVIEW_C"] = VARS::post("CHECK_INTERVIEW_C");

            $this->replace_data["field"]["INTERVIEW_A"] = VARS::post("INTERVIEW_A");
            $this->replace_data["field"]["INTERVIEW_B"] = VARS::post("INTERVIEW_B");
            $this->replace_data["field"]["INTERVIEW_C"] = VARS::post("INTERVIEW_C");
        }

        //メイン画面へ切り替え
        if ($this->cmd == "back") {
            unset($this->replace_data);
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        knjl122iQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

    //一括更新
    function &getReplaceModel() {
        //対象者が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304","（対象者）");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["check"])) {
            $this->setWarning("MSG304","（チェック）");
            return false;
        }

        //チェックがついている項目のみを更新
        knjl122iQuery::getReplaceUpdateQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
