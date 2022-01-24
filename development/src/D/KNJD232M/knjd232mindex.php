<?php

require_once('for_php7.php');

require_once('knjd232mModel.inc');
require_once('knjd232mQuery.inc');

class knjd232mController extends Controller {
    var $ModelClassName = "knjd232mModel";
    var $ProgramID      = "KNJD232M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd232m":
                case "semechg":
                case "gakki":
                case "grade":
                    $sessionInstance->knjd232mModel();
                    $this->callView("knjd232mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd232mCtl = new knjd232mController;
var_dump($_REQUEST);
?>
