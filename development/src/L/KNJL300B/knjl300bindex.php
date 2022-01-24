<?php

require_once('for_php7.php');

require_once('knjl300bModel.inc');
require_once('knjl300bQuery.inc');

class knjl300bController extends Controller {
    var $ModelClassName = "knjl300bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300b":
                    $sessionInstance->knjl300bModel();
                    $this->callView("knjl300bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl300bCtl = new knjl300bController;
var_dump($_REQUEST);
?>
