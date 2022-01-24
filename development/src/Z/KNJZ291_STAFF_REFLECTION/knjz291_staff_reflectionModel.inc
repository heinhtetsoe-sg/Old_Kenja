<?php

require_once('for_php7.php');

class knjz291_staff_reflectionModel extends Model {

    var $staffCd  = array();
    var $cmd;
    var $sendSubmit;
    var $sendAuth;
    var $sendPrgid;
    var $field;
    var $fields;

    function knjz291_staff_reflectionModel() {}

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->sendPrgid = VARS::get("SEND_PRGID");
            $this->sendSubmit = VARS::get("SEND_SUBMIT");
            unset($this->fields);
        }
        
        $this->field = array("STAFFCD"              =>  VARS::post("STAFFCD"),
                             "STAFFNAME"            =>  VARS::post("STAFFNAME"),
                             "STAFFNAME_KANA"       =>  VARS::post("STAFFNAME_KANA")
                             );

        $this->cmd = VARS::request("cmd");

        $db = Query::dbCheckOut();
        $query = knjz291_staff_reflectionQuery::getZ010();
        $this->z010Name1 = $db->getOne($query);
        Query::dbCheckIn($db);
    }

    function validateInput()
    {
        return true;
    }

    function &getUpdateModel() {
        $updateStaff = array();
        foreach ($this->staffCd as $key => $val) {
            $updateStaff[] = VARS::post("CHECK_STAFF".$val);
            $this->fields["UP_STAFFCD"][$val] = VARS::post("UP_STAFFCD".$val);
        }
        if ($this->validateInput()) {
            knjz291_staff_reflectionQuery::update($this, $updateStaff);
            $this->setMessage("MSG201");
        }
    }
}
?>
