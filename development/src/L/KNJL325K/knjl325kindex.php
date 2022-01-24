<?php
require_once('knjl325kModel.inc');
require_once('knjl325kQuery.inc');

class knjl325kController extends Controller {
    var $ModelClassName = "knjl325kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325k":
                    $sessionInstance->knjl325kModel();
                    $this->callView("knjl325kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl325kCtl = new knjl325kController;
var_dump($_REQUEST);
?>
