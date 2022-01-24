<?php
require_once('knjl302kModel.inc');
require_once('knjl302kQuery.inc');

class knjl302kController extends Controller {
    var $ModelClassName = "knjl302kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302k":
                    $sessionInstance->knjl302kModel();
                    $this->callView("knjl302kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl302kCtl = new knjl302kController;
var_dump($_REQUEST);
?>
