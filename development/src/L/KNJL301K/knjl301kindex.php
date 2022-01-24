<?php
require_once('knjl301kModel.inc');
require_once('knjl301kQuery.inc');

class knjl301kController extends Controller {
    var $ModelClassName = "knjl301kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301k":
                    $sessionInstance->knjl301kModel();
                    $this->callView("knjl301kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl301kCtl = new knjl301kController;
var_dump($_REQUEST);
?>
