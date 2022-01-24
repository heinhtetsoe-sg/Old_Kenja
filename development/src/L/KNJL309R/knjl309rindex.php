<?php

require_once('for_php7.php');

require_once('knjl309rModel.inc');
require_once('knjl309rQuery.inc');

class knjl309rController extends Controller {
    var $ModelClassName = "knjl309rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl309r":
                    $sessionInstance->knjl309rModel();
                    $this->callView("knjl309rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl309rCtl = new knjl309rController;
var_dump($_REQUEST);
?>
