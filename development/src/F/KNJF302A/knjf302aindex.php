<?php

require_once('for_php7.php');

require_once('knjf302aModel.inc');
require_once('knjf302aQuery.inc');

class knjf302aController extends Controller {
    var $ModelClassName = "knjf302aModel";
    var $ProgramID      = "KNJF302A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf302a":
                    $sessionInstance->knjf302aModel();
                    $this->callView("knjf302aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf302aCtl = new knjf302aController;
?>
