<?php

require_once('for_php7.php');

require_once('knjd179eModel.inc');
require_once('knjd179eQuery.inc');

class knjd179eController extends Controller {
    var $ModelClassName = "knjd179eModel";
    var $ProgramID      = "KNJD179E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                    $sessionInstance->knjd179eModel();
                    $this->callView("knjd179eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd179eCtl = new knjd179eController;
?>
