<?php

require_once('for_php7.php');

require_once('../../common/mycalendar.php');

class knjp910_approvalModel extends Model {

    var $cmd;
    var $field  = array();

    function init() {
        if (VARS::get("SEND_AUTH")) {
            $this->getAuth         = VARS::get("SEND_AUTH");
            $this->getSchoolKind   = VARS::get("SEND_SCHOOL_KIND");
            $this->getYear         = VARS::get("SEND_YEAR");
            $this->getPrgId        = VARS::get("SEND_PRGID");
            $this->getOutgoLcd     = VARS::get("SEND_OUTGO_L_CD");
            $this->getOutgoMcd     = VARS::get("SEND_OUTGO_M_CD");
            $this->getOutgoLMcd    = VARS::get("SEND_OUTGO_L_M_CD");
            $this->getRequestNo    = VARS::get("SEND_REQUEST_NO");
            $this->getIncomeLcd    = VARS::get("SEND_INCOME_L_CD");
            $this->getLineNo       = VARS::get("SEND_LINE_NO");
        }
        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        $this->field = array("OUTGO_APPROVAL"         => VARS::post("OUTGO_APPROVAL"),
                             "OUTGO_CANCEL"           => VARS::post("OUTGO_CANCEL"),
                             "OUTGO_APPROVAL_VALUE"   => VARS::request("OUTGO_APPROVAL_VALUE"),
                             "OUTGO_CANCEL_VALUE"     => VARS::request("OUTGO_CANCEL_VALUE"),
                             
                             "OUTGO_CHECK1"           => VARS::post("OUTGO_CHECK1"),
                             "OUTGO_CHECK1_DATE"      => VARS::post("OUTGO_CHECK1_DATE"),
                             "OUTGO_CHECK1_STAFF"     => VARS::post("OUTGO_CHECK1_STAFF"),
                             "OUTGO_CHECK2"           => VARS::post("OUTGO_CHECK2"),
                             "OUTGO_CHECK3"           => VARS::post("OUTGO_CHECK3"),
                             
                             "OUTGO_DATE"             => VARS::post("OUTGO_DATE"),
                             "OUTGO_EXPENSE_FLG"      => VARS::post("OUTGO_EXPENSE_FLG"),
                             "OUTGO_CERTIFICATE_CNT"  => VARS::post("OUTGO_CERTIFICATE_CNT")
                              );

        $this->cmd         = VARS::request("cmd");
    }
    
    
    //�X�V�p�̃��f����Ԃ��܂�
    function &getUpdateModel() {
        $db = Query::dbCheckOut();

        knjp910_approvalQuery::getUpdateQuery($db, $this);

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }
    
}
?>
