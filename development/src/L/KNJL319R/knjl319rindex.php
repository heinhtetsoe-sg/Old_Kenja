<?php

require_once('for_php7.php');

require_once('knjl319rModel.inc');
require_once('knjl319rQuery.inc');

class knjl319rController extends Controller {
    var $ModelClassName = "knjl319rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl319r":
                    $sessionInstance->knjl319rModel();
                    $this->callView("knjl319rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl319rCtl = new knjl319rController;
var_dump($_REQUEST);
?>
