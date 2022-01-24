<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjmp900_approvalModel extends Model {

    var $cmd;
    var $field  = array();

    function init() {
        if (VARS::get("SEND_AUTH")) {
            $this->getAuth         = VARS::get("SEND_AUTH");
            $this->getYear         = VARS::get("SEND_YEAR");
            $this->getPrgId        = VARS::get("SEND_PRGID");
            $this->getIncomeLcd    = VARS::get("SEND_INCOME_L_CD");
            $this->getIncomeMcd    = VARS::get("SEND_INCOME_M_CD");
            $this->getIncomeLMcd   = VARS::get("SEND_INCOME_L_M_CD");
            $this->getRequestNo    = VARS::get("SEND_REQUEST_NO");
            $this->getCollectLMScd = VARS::get("SEND_COLLECT_L_M_S_CD");
            $this->getLineNo       = VARS::get("SEND_LINE_NO");
        }
        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        $this->field = array("INCOME_APPROVAL"         => VARS::post("INCOME_APPROVAL"),
                             "INCOME_CANCEL"           => VARS::post("INCOME_CANCEL"),
                             "INCOME_APPROVAL_VALUE"   => VARS::request("INCOME_APPROVAL_VALUE"),
                             "INCOME_CANCEL_VALUE"     => VARS::request("INCOME_CANCEL_VALUE"),
                             "INCOME_DATE"             => VARS::post("INCOME_DATE"),
                             "INCOME_STAFF"            => VARS::post("INCOME_STAFF"),
                             "INCOME_CERTIFICATE_CNT"  => VARS::post("INCOME_CERTIFICATE_CNT")
                              );

        $this->cmd         = VARS::request("cmd");
    }
    
    
    //�X�V�p�̃��f����Ԃ��܂�
    function &getUpdateModel() {
        $db = Query::dbCheckOut();

        knjmp900_approvalQuery::getUpdateQuery($db, $this);

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }
    
}
?>
