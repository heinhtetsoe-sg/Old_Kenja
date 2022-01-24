<?php

require_once('for_php7.php');

require_once('knjf334aModel.inc');
require_once('knjf334aQuery.inc');

class knjf334aController extends Controller {
    var $ModelClassName = "knjf334aModel";
    var $ProgramID      = "KNJF334A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf334a":
                    $sessionInstance->knjf334aModel();
                    $this->callView("knjf334aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf334aCtl = new knjf334aController;
?>
