<?php
require_once('knjl359kModel.inc');
require_once('knjl359kQuery.inc');

class knjl359kController extends Controller {
    var $ModelClassName = "knjl359kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl359k":
                    $sessionInstance->knjl359kModel();
                    $this->callView("knjl359kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl359kCtl = new knjl359kController;
var_dump($_REQUEST);
?>
