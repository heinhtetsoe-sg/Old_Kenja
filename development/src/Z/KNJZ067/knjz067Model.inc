<?php

require_once('for_php7.php');

class knjz067Model extends Model {
    var $field = array();
    var $cmd;
    var $ibyear;
    var $ibgrade;
    var $ibclasscd;
    var $ibprg_course;
    var $ibcurriculum_cd;
    var $ibsubclasscd;

    function knjz067Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->ibyear);
            unset($this->ibgrade);
            unset($this->ibclasscd);
            unset($this->ibprg_course);
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
        }

        list ($ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd) = explode('-', VARS::post("IBSUBCLASS"));

        $this->field = array("IBGRADE"          =>  VARS::post("IBGRADE"),
                             "IBPRG_COURSE"     =>  VARS::post("IBPRG_COURSE"),
                             "IBCLASSCD"        =>  $ibclasscd,
                             "IBCURRICULUM_CD"  =>  $ibcurriculum_cd,
                             "IBSUBCLASSCD"     =>  $ibsubclasscd
                             );

        $this->cmd = VARS::request("cmd");

        //年度（初期値）
        $this->ibyear = ($this->ibyear == "") ? CTRL_YEAR : $this->ibyear;

        if (VARS::post("IBYEAR") != "") {
            $this->ibyear = VARS::post("IBYEAR");
        }

        if(VARS::get("IBGRADE")) {
            $this->ibgrade = VARS::get("IBGRADE");
        }
        if(VARS::get("IBCLASSCD")) {
            $this->ibclasscd = VARS::get("IBCLASSCD");
        }
        if(VARS::get("IBPRG_COURSE")) {
            $this->ibprg_course = VARS::get("IBPRG_COURSE");
        }
        if(VARS::get("IBCURRICULUM_CD")) {
            $this->ibcurriculum_cd = VARS::get("IBCURRICULUM_CD");
        }
        if(VARS::get("IBSUBCLASSCD")) {
            $this->ibsubclasscd = VARS::get("IBSUBCLASSCD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (trim($this->ibyear) == "") {
            $this->setWarning("MSG301", "(年度)");
            return false;
        }
        if (trim($this->field["IBGRADE"]) == "") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        if (trim($this->field["IBPRG_COURSE"]) == "") {
            $this->setWarning("MSG301", "(IBコース)");
            return false;
        }
        if (trim($this->field["IBCLASSCD"]) == "" || trim($this->field["IBCURRICULUM_CD"]) == "" || trim($this->field["IBSUBCLASSCD"]) == "") {
            $this->setWarning("MSG301", "(科目)");
            return false;
        }

        if ($this->cmd == "delete") {
            //キーの更新チェック
            if ($this->ibgrade != $this->field["IBGRADE"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibclasscd != $this->field["IBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibprg_course != $this->field["IBPRG_COURSE"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibcurriculum_cd != $this->field["IBCURRICULUM_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibsubclasscd != $this->field["IBSUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if (!strlen($this->ibgrade) || !strlen($this->ibclasscd) || !strlen($this->ibprg_course) || !strlen($this->ibcurriculum_cd) || !strlen($this->ibsubclasscd)) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz067Query::getIBSubclassSelectDat($this->ibyear, $this->field["IBGRADE"], $this->field["IBCLASSCD"], $this->field["IBPRG_COURSE"],  $this->field["IBCURRICULUM_CD"],  $this->field["IBSUBCLASSCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //対象がなければエラー
        if ($this->cmd == "delete" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertModel() {
        if ($this->validateInput()){
            //登録
            knjz067Query::getInsertQuery($this);
            $this->setMessage("MSG201");

            $this->ibgrade          = $this->field["IBGRADE"];
            $this->ibclasscd        = $this->field["IBCLASSCD"];
            $this->ibprg_course     = $this->field["IBPRG_COURSE"];
            $this->ibcurriculum_cd  = $this->field["IBCURRICULUM_CD"];
            $this->ibsubclasscd     = $this->field["IBSUBCLASSCD"];
        }
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteModel() {
        if ($this->validateInput()) {
            //削除
            knjz067Query::getDeleteQuery($this);
            $this->setMessage("MSG201");

            unset($this->ibgrade);
            unset($this->ibclasscd);
            unset($this->ibprg_course);
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
            unset($this->field);
        }
    }

    /********/
    /* COPY */
    /********/
    function &getCopyModel() {
        $db = Query::dbCheckOut();

        //存在チェック（前年度）
        $pre_year = $this->ibyear - 1;
        $cnt_pre = $db->getOne(knjz067Query::checkIBSubclassSelectDat($pre_year));
        if ($cnt_pre == 0) {
            $this->setWarning("MSG917","前年度のデータが存在しません。");
            return false;
        }
        //存在チェック（対象年度）
        $cnt_this = $db->getOne(knjz067Query::checkIBSubclassSelectDat($this->ibyear));
        if ($cnt_this > 0) {
            $this->setWarning("MSG917","対象年度にデータが存在します。");
            return false;
        }

        Query::dbCheckIn($db);

        //コピー
        knjz067Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>
