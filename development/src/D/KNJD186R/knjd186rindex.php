<?php

require_once('for_php7.php');

require_once('knjd186rModel.inc');
require_once('knjd186rQuery.inc');

class knjd186rController extends Controller {
    var $ModelClassName = "knjd186rModel";
    var $ProgramID      = "KNJD186R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd186r":
                case "knjd186rChangeSemester":
                    $sessionInstance->knjd186rModel();
                    $this->callView("knjd186rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186rCtl = new knjd186rController;
?>
