<?php

require_once('for_php7.php');

require_once('knjl323nModel.inc');
require_once('knjl323nQuery.inc');

class knjl323nController extends Controller {
    var $ModelClassName = "knjl323nModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323n":
                    $sessionInstance->knjl323nModel();
                    $this->callView("knjl323nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl323nCtl = new knjl323nController;
var_dump($_REQUEST);
?>
