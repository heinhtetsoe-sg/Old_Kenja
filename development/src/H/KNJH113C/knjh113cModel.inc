<?php

require_once('for_php7.php');

class knjh113cModel extends model {
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $field = array();
    var $sort;
    var $judgediv;

    function knjh113cModel() {}

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->sikakuCd     = strlen(VARS::post("SIKAKUCD"))       ? VARS::post("SIKAKUCD")      : VARS::post("HID_SIKAKUCD");
        $this->gradeHrClass = strlen(VARS::post("GRADE_HR_CLASS")) ? VARS::post("GRADE_HR_CLASS"): VARS::post("HID_GRADE_HR_CLASS");
        $this->testDate     = strlen(VARS::post("TEST_DATE"))      ? VARS::post("TEST_DATE")     : VARS::post("HID_TEST_DATE");
        $this->judgeDiv     = VARS::post("JUDGEDIV");
        $this->hid_schregNo = VARS::post("HID_SCHREGNO");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut(); //DB接続
        //学期開始、終了日付
        $this->sDate = $db->getOne(knjh113cQuery::getSEdate("SDATE"));
        $this->eDate = $db->getOne(knjh113cQuery::getSEdate("EDATE"));
        Query::dbCheckIn($db);     //DB切断
    }

    function &getMainModel() {}

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_schregNo);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $schregNo = $tmp[0];
            $testCd   = $tmp[1];
            $this->field["CHK_DATA"][$schregNo.$testCd] = VARS::post("CHK_DATA"."-".$schregNo."-".$testCd);
        }
        if (isset($this->field["CHK_DATA"])) {
            $result = knjh113cQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
