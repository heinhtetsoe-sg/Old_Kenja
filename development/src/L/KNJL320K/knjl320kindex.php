<?php

require_once('for_php7.php');

require_once('knjl320kModel.inc');
require_once('knjl320kQuery.inc');

class knjl320kController extends Controller {
    var $ModelClassName = "knjl320kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl320k":
                    $sessionInstance->knjl320kModel();
                    $this->callView("knjl320kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl320kCtl = new knjl320kController;
var_dump($_REQUEST);
?>
