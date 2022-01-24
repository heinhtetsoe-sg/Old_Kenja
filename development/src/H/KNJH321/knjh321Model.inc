<?php

require_once('for_php7.php');


class knjh321Model extends Model {

    var $cmd;
    var $schregno;
    var $mock_group_cd;
    var $adpara;
    var $cmbIndex;
    var $paraSubcd;
    var $mock_target = array();
    var $group_div;
    var $stf_auth_cd;
    var $groupcd;

    function knjh321Model()
    {
    }

    function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->schregno      = VARS::request("schregno");
        $this->mock_group_cd = VARS::request("mock_group_cd");
        $mock = preg_split("/-/", $this->mock_group_cd);
        $this->group_div   = $mock[0];
        $this->stf_auth_cd = $mock[1];
        $this->groupcd     = $mock[2];

        $this->adpara        = VARS::request("adpara");
        $this->cmbIndex      = VARS::request("cmbIndex");
        $this->paraSubcd     = VARS::request("paraSubcd");

        $this->mock_target[0] = VARS::post("MOCK_TARGET_CD1");
        $this->mock_target[1] = VARS::post("MOCK_TARGET_CD2");
        $this->mock_target[2] = VARS::post("MOCK_TARGET_CD3");
    }

}
?>
