<?php

require_once('for_php7.php');

require_once('knjd154hModel.inc');
require_once('knjd154hQuery.inc');

class knjd154hController extends Controller {
    var $ModelClassName = "knjd154hModel";
    var $ProgramID      = "KNJD154H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd154h";
                    $sessionInstance->knjd154hModel();
                    $this->callView("knjd154hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd154hCtl = new knjd154hController;
?>
