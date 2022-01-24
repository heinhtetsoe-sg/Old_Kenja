<?php

require_once('for_php7.php');
class knjb1213Model extends Model {
    var $field = array();
    var $cmd;
    var $schregno;
    var $search_div;
    var $name;
    var $year;
    var $register_date;
    var $selectdata;

    function knjb1213Model() {
        $this->field = array();
    }

    function init() {

        if(VARS::get("PROGRAMID")){
           unset($this->schregno);
           unset($this->search_div);
           unset($this->name);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("REGISTER_DATE"        => VARS::post("REGISTER_DATE"),
                             "TOTAL_GK"             => str_replace(",", "", VARS::post("TOTAL_GK")),
                             "BOOKDIV1_GK"          => str_replace(",", "", VARS::post("BOOKDIV1_GK")),
                             "BOOKDIV2_GK"          => str_replace(",", "", VARS::post("BOOKDIV2_GK")),
                             "TOTAL_COUNT"          => VARS::post("TOTAL_COUNT"),
                             "PROVIDE_REASON"       => VARS::post("PROVIDE_REASON"),
                             "ATTACH_DOCUMENTS"     => VARS::post("ATTACH_DOCUMENTS"),
                             "REMARK"               => VARS::post("REMARK"),
                             "JUDGE_RESULT"         => VARS::post("JUDGE_RESULT"),
                             "DECISION_DATE"        => VARS::post("DECISION_DATE"),
                            );

        $this->cmd = VARS::request("cmd");

        $this->year = VARS::request("YEAR");
        $this->register_date = VARS::request("REGISTER_DATE_CMB");

        $this->selectdata = VARS::post("selectdata");

        if (VARS::get("SCHREGNO")) {
            $this->schregno     = VARS::get("SCHREGNO");
            $this->search_div   = VARS::get("SEARCH_DIV");
            $this->name         = VARS::get("NAME");
        }
    }

    //入力チェック
    function validateInput($cmd) {

        $db = Query::dbCheckOut();

        if ($cmd == "insert") {
            //存在チェック
            $cntF = $db->getOne(knjb1213Query::checkSchregTextbookFreeDat($this));
            $cntA = $db->getOne(knjb1213Query::checkSchregTextbookFreeApplyDat($this));
            if ($cntF > 0 || $cntA > 0) {
                $this->setWarning("MSG302");
                $this->cmd = "knjb1213";
                return false;
            }

            //重複教科書有無チェック
            $cntT = $db->getOne(knjb1213Query::checkTextbookExist($this));
            if ($cntT > 0) {
                $this->setWarning("MSG302", "（無償給与対象）");
                $this->cmd = "knjb1213";
                return false;
            }
        }

        //バイト数チェック
        $check_item = array("ATTACH_DOCUMENTS"  => array(45,"(添付書類)"),
                            "REMARK"            => array(45,"(備考)"));
        foreach ($check_item as $key => $val) {
            if (strlen($this->field[$key]) > $val[0]) {
                $this->setWarning("MSG915",$val[1]);
                return false;
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    //更新処理
    function &getUpdateModel() {

        $cmd = (str_replace("/", "-", $this->register_date) == str_replace("/", "-", $this->field["REGISTER_DATE"])) ? "update" : "insert";
        if ($this->validateInput($cmd)) {
            if ($cmd == "update") {
                knjb1213Query::getUpdateQuery($this);
                $this->cmd = "main";
            } else {
                knjb1213Query::getInsertQuery($this);
                $this->cmd = "main";
                $this->register_date = str_replace("/", "-", $this->field["REGISTER_DATE"]);
            }
        } else {
            $this->cmd = "knjb1213";
        }
        $this->setMessage("MSG201");
    }

    //削除処理
    function &getDeleteModel() {

        knjb1213Query::getDeleteQuery($this);

        unset($this->field);
        unset($this->register_date);
        $this->setMessage("MSG201");
    }
}
?>
