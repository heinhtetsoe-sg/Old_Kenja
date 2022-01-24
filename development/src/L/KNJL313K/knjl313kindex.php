<?php

require_once('for_php7.php');

require_once('knjl313kModel.inc');
require_once('knjl313kQuery.inc');

class knjl313kController extends Controller {
    var $ModelClassName = "knjl313kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl313k":
                    $sessionInstance->knjl313kModel();
                    $this->callView("knjl313kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl313kCtl = new knjl313kController;
var_dump($_REQUEST);
?>
