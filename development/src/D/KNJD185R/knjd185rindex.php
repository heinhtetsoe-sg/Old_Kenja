<?php

require_once('for_php7.php');

require_once('knjd185rModel.inc');
require_once('knjd185rQuery.inc');

class knjd185rController extends Controller {
    var $ModelClassName = "knjd185rModel";
    var $ProgramID      = "KNJD185R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185r":
                case "knjd185rChangeSemester":
                    $sessionInstance->knjd185rModel();
                    $this->callView("knjd185rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd185rCtl = new knjd185rController;
?>
