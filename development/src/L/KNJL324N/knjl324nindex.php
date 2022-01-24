<?php

require_once('for_php7.php');

require_once('knjl324nModel.inc');
require_once('knjl324nQuery.inc');

class knjl324nController extends Controller {
    var $ModelClassName = "knjl324nModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324n":
                    $sessionInstance->knjl324nModel();
                    $this->callView("knjl324nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl324nCtl = new knjl324nController;
var_dump($_REQUEST);
?>
