<?php

require_once('for_php7.php');

require_once('knjl312nModel.inc');
require_once('knjl312nQuery.inc');

class knjl312nController extends Controller {
    var $ModelClassName = "knjl312nModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312n":
                    $sessionInstance->knjl312nModel();
                    $this->callView("knjl312nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl312nCtl = new knjl312nController;
var_dump($_REQUEST);
?>
