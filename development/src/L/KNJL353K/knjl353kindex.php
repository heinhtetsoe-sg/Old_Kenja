<?php
require_once('knjl353kModel.inc');
require_once('knjl353kQuery.inc');

class knjl353kController extends Controller {
    var $ModelClassName = "knjl353kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl353k":
                    $sessionInstance->knjl353kModel();
                    $this->callView("knjl353kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl353kCtl = new knjl353kController;
var_dump($_REQUEST);
?>
