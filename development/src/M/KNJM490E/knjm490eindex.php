<?php
require_once('knjm490eModel.inc');
require_once('knjm490eQuery.inc');

class knjm490eController extends Controller {
    var $ModelClassName = "knjm490eModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm490e":
                    $sessionInstance->knjm490eModel();
                    $this->callView("knjm490eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm490eCtl = new knjm490eController;
var_dump($_REQUEST);
?>
