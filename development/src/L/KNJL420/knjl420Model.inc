<?php

require_once('for_php7.php');

class knjl420Model extends Model {
    var $field = array();
    var $cmd;

    function knjl420Model()
    {
        $this->field = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->prischoolCd);
            unset($this->prischoolClassCd);
            unset($this->recruitNo);
        }

        if (VARS::get("PRISCHOOLCD")) {
            $this->prischoolCd = VARS::get("PRISCHOOLCD");
        }
        if (VARS::get("PRISCHOOL_CLASS_CD")) {
            $this->prischoolClassCd = VARS::get("PRISCHOOL_CLASS_CD");
        }
        $this->recruitNo = $this->prischoolCd.$this->prischoolClassCd;
        $this->field = array("DIRECT_MAIL_FLG"     => VARS::post("DIRECT_MAIL_FLG"),
                             "STAFF_NAME"          => VARS::post("STAFF_NAME"),
                             "MOBILE_PHONE_NUMBER" => VARS::post("MOBILE_PHONE_NUMBER"),
                             "EMAIL"               => VARS::post("EMAIL")
                             );

        $this->schoolKindHenkan["1"] = "J";
        $this->schoolKindHenkan["2"] = "H";

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SEND_PRGRID")){
            $this->getPrgId     = VARS::get("SEND_PRGRID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->recruit_no   = VARS::get("SEND_RECRUIT_NO");
            $this->name         = VARS::get("SEND_NAME");
            $this->kana         = VARS::get("SEND_KANA");
            $this->finschoolcd  = VARS::get("SEND_FINSCHOOLCD");
        }
        //権限
        $this->auth = ($this->getPrgId) ? $this->sendAuth : AUTHORITY;

        $this->recruitYear = CTRL_YEAR + 1;
    }

    //更新処理
    function &getUpdateModel()
    {
        $this->update();

        $this->setMessage("MSG201");
    }

    //削除処理
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl420Query::delDetail($this);
        $db->query($query);

        $this->setMessage("MSG201");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //更新処理
    function update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (!$this->prischoolCd || !$this->prischoolClassCd) {
            $this->setWarning('MSG309');
            return false;
        }
        //担当者
        if (strlen($this->field["STAFF_NAME"]) > 75 ) {
            $this->setWarning('MSG901', "( 担当者 ) 75バイトまで");
            return false;
        }

        $query = knjl420Query::delDetail($this);
        $db->query($query);

        $query = knjl420Query::insDetail($this);
        $db->query($query);

        $query = knjl420Query::updPriSchoolClass($this);
        $db->query($query);

        $query = knjl420Query::getPriSchoolClass($this);
        $this->priClassInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query = knjl420Query::delRecruitPsDat($this);
        $db->query($query);

        $query = knjl420Query::insRecruitPsDat($this);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //削除処理
    function &getSendDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($this->sendDelkey as $key => $val) {
            if (VARS::post("DELCHECK_".$val) == "1") {
                $query = knjl420Query::delSendHistStdDat($this, $val);
                $db->query($query);
            }
        }

        $this->setMessage("MSG201");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

}
?>
