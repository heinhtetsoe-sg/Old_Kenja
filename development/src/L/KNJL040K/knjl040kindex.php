<?php
require_once('knjl040kModel.inc');
require_once('knjl040kQuery.inc');

class knjl040kController extends Controller {
    var $ModelClassName = "knjl040kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl040k":
                    $sessionInstance->knjl040kModel();
                    $this->callView("knjl040kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl040kCtl = new knjl040kController;
var_dump($_REQUEST);
?>
