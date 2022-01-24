<?php

require_once('for_php7.php');

require_once('knjd131fModel.inc');
require_once('knjd131fQuery.inc');

class knjd131fController extends Controller {
    var $ModelClassName = "knjd131fModel";
    var $ProgramID      = "KNJD131F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjd131f":
                case "clear":
                    $sessionInstance->knjd131fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd131fForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd131fCtl = new knjd131fController;
?>
