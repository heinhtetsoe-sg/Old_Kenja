<?php

require_once('for_php7.php');

require_once('knjl013rModel.inc');
require_once('knjl013rQuery.inc');

class knjl013rController extends Controller {
    var $ModelClassName = "knjl013rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl013r":
                    $sessionInstance->knjl013rModel();
                    $this->callView("knjl013rForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl013r");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl013rCtl = new knjl013rController;
var_dump($_REQUEST);
?>
