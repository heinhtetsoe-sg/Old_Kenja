<?php

require_once('for_php7.php');

require_once('knjl312kModel.inc');
require_once('knjl312kQuery.inc');

class knjl312kController extends Controller {
    var $ModelClassName = "knjl312kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312k":
                    $sessionInstance->knjl312kModel();
                    $this->callView("knjl312kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl312kCtl = new knjl312kController;
var_dump($_REQUEST);
?>
