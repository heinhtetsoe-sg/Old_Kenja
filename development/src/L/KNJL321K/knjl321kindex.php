<?php
require_once('knjl321kModel.inc');
require_once('knjl321kQuery.inc');

class knjl321kController extends Controller {
    var $ModelClassName = "knjl321kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl321k");
                    break 1;
                case "":
                case "knjl321k":
                    $sessionInstance->knjl321kModel();
                    $this->callView("knjl321kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl321kCtl = new knjl321kController;
var_dump($_REQUEST);
?>
