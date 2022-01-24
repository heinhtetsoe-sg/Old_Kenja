<?php

require_once('for_php7.php');

require_once('knjl333rModel.inc');
require_once('knjl333rQuery.inc');

class knjl333rController extends Controller {
    var $ModelClassName = "knjl333rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl333r":
                    $sessionInstance->knjl333rModel();
                    $this->callView("knjl333rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl333rCtl = new knjl333rController;
var_dump($_REQUEST);
?>
