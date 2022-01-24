<?php
require_once('knjl355kModel.inc');
require_once('knjl355kQuery.inc');

class knjl355kController extends Controller {
    var $ModelClassName = "knjl355kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl355k":
                    $sessionInstance->knjl355kModel();
                    $this->callView("knjl355kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl355kCtl = new knjl355kController;
var_dump($_REQUEST);
?>
