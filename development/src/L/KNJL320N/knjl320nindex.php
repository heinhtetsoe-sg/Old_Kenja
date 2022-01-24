<?php

require_once('for_php7.php');

require_once('knjl320nModel.inc');
require_once('knjl320nQuery.inc');

class knjl320nController extends Controller {
    var $ModelClassName = "knjl320nModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl320n":
                    $sessionInstance->knjl320nModel();
                    $this->callView("knjl320nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl320nCtl = new knjl320nController;
var_dump($_REQUEST);
?>
