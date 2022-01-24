<?php

require_once('for_php7.php');

require_once('knjl312bModel.inc');
require_once('knjl312bQuery.inc');

class knjl312bController extends Controller {
    var $ModelClassName = "knjl312bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312b":
                    $sessionInstance->knjl312bModel();
                    $this->callView("knjl312bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl312bCtl = new knjl312bController;
var_dump($_REQUEST);
?>
