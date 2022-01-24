<?php

require_once('for_php7.php');


class knjl410Model extends Model
{
    
    public $field = array();
    public $cmd;

    public function knjl410Model()
    {
        $this->field = array();
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->recruitNo);
            unset($this->fubanMukou);
        }

        if (VARS::get("RECRUIT_NO")) {
            $this->recruitNo   = VARS::get("RECRUIT_NO");
        }
        if (VARS::get("RECRUIT_YEAR")) {
            $this->recruitYear   = VARS::get("RECRUIT_YEAR");
        }
        $this->field = array("SCHOOL_KIND"         => VARS::post("SCHOOL_KIND"),
                             "RECRUIT_NO"          => VARS::post("RECRUIT_NO"),
                             "NAME"                => VARS::post("NAME"),
                             "NAME_KANA"           => VARS::post("NAME_KANA"),
                             "FINSCHOOLCD"         => VARS::post("FINSCHOOLCD"),
                             "GRADE"               => VARS::post("GRADE"),
                             "SCHOOL_TEACHER"      => VARS::post("SCHOOL_TEACHER"),
                             "PRISCHOOLCD"         => VARS::post("PRISCHOOLCD"),
                             "PRISCHOOL_CLASS_CD"  => VARS::post("PRISCHOOL_CLASS_CD"),
                             "PRISCHOOL_TEACHER"   => VARS::post("PRISCHOOL_TEACHER"),
                             "SEX"                 => VARS::post("SEX"),
                             "BIRTHDAY"            => VARS::post("BIRTHDAY"),
                             "ZIPCD"               => VARS::post("ZIPCD"),
                             "GUARD_NAME"          => VARS::post("GUARD_NAME"),
                             "GUARD_KANA"          => VARS::post("GUARD_KANA"),
                             "ADDR1"               => VARS::post("ADDR1"),
                             "ADDR2"               => VARS::post("ADDR2"),
                             "TELNO"               => VARS::post("TELNO"),
                             "TELNO2"              => VARS::post("TELNO2"),
                             "FAXNO"               => VARS::post("FAXNO"),
                             "EMAIL"               => VARS::post("EMAIL"),
                             "REMARK"              => VARS::post("REMARK")
                             );

        $this->schoolKindHenkan["1"] = "J";
        $this->schoolKindHenkan["2"] = "H";

        if (VARS::get("SEND_PRGRID")) {
            $this->getPrgId     = VARS::get("SEND_PRGRID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->recruit_no   = VARS::get("SEND_RECRUIT_NO");
            $this->name         = VARS::get("SEND_NAME");
            $this->kana         = VARS::get("SEND_KANA");
            $this->finschoolcd  = VARS::get("SEND_FINSCHOOLCD");
        }
        //権限
        $this->auth = ($this->getPrgId) ? $this->sendAuth : AUTHORITY;
    }

    //更新処理
    public function &getUpdateModel()
    {
        $this->update();

        $this->setMessage("MSG201");
    }

    //削除処理
    public function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl410Query::delRecruitDat($this, "RECRUIT_DAT");
        $db->query($query);
        $query = knjl410Query::delRecruitDat($this, "RECRUIT_CONSULT_DAT");
        $db->query($query);
        $query = knjl410Query::delRecruitDat($this, "RECRUIT_CONSULT_WRAPUP_DAT");
        $db->query($query);
        $query = knjl410Query::delRecruitDat($this, "RECRUIT_EVENT_DAT");
        $db->query($query);
        $query = knjl410Query::delRecruitDat($this, "RECRUIT_SEND_HIST_STD_DAT");
        $db->query($query);
        $query = knjl410Query::delRecruitDat($this, "RECRUIT_VISIT_ACTIVE_DAT");
        $db->query($query);
        $query = knjl410Query::delRecruitDat($this, "RECRUIT_VISIT_DAT");
        $db->query($query);
        $query = knjl410Query::delRecruitDat($this, "RECRUIT_VISIT_MOCK_DAT");
        $db->query($query);
        $query = knjl410Query::delRecruitDat($this, "RECRUIT_VISIT_SCORE_DAT");
        $db->query($query);

        $this->setMessage("MSG201");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //更新処理
    public function update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->cmd == "add") {
            $query = knjl410Query::getRecruitDatCnt($this, (CTRL_YEAR + 1));
            $recruitCtn = $db->getOne($query);
            if ($recruitCtn > 0) {
                $this->setWarning("MSG302");
                return false;
            }
        }
        if ($this->cmd == "update") {
            $query = knjl410Query::getRecruitDatCnt($this, $this->recruitYear);
            $recruitCtn = $db->getOne($query);
            if ($recruitCtn == 0) {
                $this->setWarning("MSG303");
                return false;
            }
        }

        //氏名
        if ($this->validate_row_cnt($this->field["NAME"], (40 * 2)) > 1) {
            $this->setWarning("MSG915", "(氏名は1行までです)");
            return false;
        }

        //氏名かな
        if ($this->validate_row_cnt($this->field["NAME_KANA"], (80 * 2)) > 1) {
            $this->setWarning("MSG915", "(氏名かなは1行までです)");
            return false;
        }

        //保護者氏名
        if ($this->validate_row_cnt($this->field["GUARD_NAME"], (20 * 2)) > 1) {
            $this->setWarning("MSG915", "(保護者氏名は1行までです)");
            return false;
        }

        //保護者氏名かな
        if ($this->validate_row_cnt($this->field["GUARD_KANA"], (40 * 2)) > 1) {
            $this->setWarning("MSG915", "(保護者氏名かなは1行までです)");
            return false;
        }

        //住所１
        if ($this->validate_row_cnt($this->field["ADDR1"], (50 * 2)) > 1) {
            $this->setWarning("MSG915", "(住所１は1行までです)");
            return false;
        }

        //住所２
        if ($this->validate_row_cnt($this->field["ADDR2"], (50 * 2)) > 1) {
            $this->setWarning("MSG915", "(住所２は1行までです)");
            return false;
        }

        //備考
        if ($this->validate_row_cnt($this->field["REMARK"], (40 * 2)) > 2) {
            $this->setWarning("MSG915", "(備考は2行までです)");
            return false;
        }

        if ($this->cmd == 'add') {
            $this->recruitYear = (CTRL_YEAR + 1);
        }

        $query = knjl410Query::delRecruitDat($this, "RECRUIT_DAT");
        $db->query($query);

        $query = knjl410Query::insRecruitDat($this);
        $db->query($query);

        $this->recruitNo = $this->field["RECRUIT_NO"];
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //削除処理
    public function &getSendDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($this->sendDelkey as $key => $val) {
            if (VARS::post("DELCHECK_".$val) == "1") {
                $query = knjl410Query::delSendHistStdDat($this, $val);
                $db->query($query);
            }
        }

        $this->setMessage("MSG201");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }
}
?>
