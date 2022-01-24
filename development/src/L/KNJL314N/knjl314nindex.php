<?php

require_once('for_php7.php');

require_once('knjl314nModel.inc');
require_once('knjl314nQuery.inc');

class knjl314nController extends Controller {
    var $ModelClassName = "knjl314nModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl314n":
                    $sessionInstance->knjl314nModel();
                    $this->callView("knjl314nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl314nCtl = new knjl314nController;
var_dump($_REQUEST);
?>
