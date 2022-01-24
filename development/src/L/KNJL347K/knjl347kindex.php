<?php
require_once('knjl347kModel.inc');
require_once('knjl347kQuery.inc');

class knjl347kController extends Controller {
    var $ModelClassName = "knjl347kModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl347k":
                    $sessionInstance->knjl347kModel();
                    $this->callView("knjl347kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl347kCtl = new knjl347kController;
var_dump($_REQUEST);
?>
