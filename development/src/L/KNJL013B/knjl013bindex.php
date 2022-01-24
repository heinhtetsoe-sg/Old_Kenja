<?php

require_once('for_php7.php');

require_once('knjl013bModel.inc');
require_once('knjl013bQuery.inc');

class knjl013bController extends Controller {
    var $ModelClassName = "knjl013bModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl013b":
                    $sessionInstance->knjl013bModel();
                    $this->callView("knjl013bForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl013b");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl013bCtl = new knjl013bController;
var_dump($_REQUEST);
?>
