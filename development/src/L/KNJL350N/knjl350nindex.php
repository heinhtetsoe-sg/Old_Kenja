<?php

require_once('for_php7.php');

require_once('knjl350nModel.inc');
require_once('knjl350nQuery.inc');

class knjl350nController extends Controller {
    var $ModelClassName = "knjl350nModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl350n":
                    $sessionInstance->knjl350nModel();
                    $this->callView("knjl350nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl350nCtl = new knjl350nController;
var_dump($_REQUEST);
?>
