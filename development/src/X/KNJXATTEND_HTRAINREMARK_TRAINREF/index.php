<?php

require_once('for_php7.php');
require_once('knjxattend_htrainremark_trainrefModel.inc');
require_once('knjxattend_htrainremark_trainrefQuery.inc');

class knjxattend_htrainremark_trainrefController extends Controller
{
    var $ModelClassName = "knjxattend_htrainremark_trainrefModel";
    var $ProgramID      = "knjxattend_htrainremark_trainref";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjxattend_htrainremark_trainrefForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxattend_htrainremark_trainrefCtl = new knjxattend_htrainremark_trainrefController();
