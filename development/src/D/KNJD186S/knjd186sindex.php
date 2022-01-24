<?php

require_once('for_php7.php');

require_once('knjd186sModel.inc');
require_once('knjd186sQuery.inc');

class knjd186sController extends Controller {
    var $ModelClassName = "knjd186sModel";
    var $ProgramID      = "KNJD186S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd186s":
                    $sessionInstance->knjd186sModel();
                    $this->callView("knjd186sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186sCtl = new knjd186sController;
?>
