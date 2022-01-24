<?php

require_once('for_php7.php');

require_once('knjl308nModel.inc');
require_once('knjl308nQuery.inc');

class knjl308nController extends Controller {
    var $ModelClassName = "knjl308nModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl308n":
                    $sessionInstance->knjl308nModel();
                    $this->callView("knjl308nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl308nCtl = new knjl308nController;
var_dump($_REQUEST);
?>
