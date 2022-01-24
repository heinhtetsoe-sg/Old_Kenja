<?php

require_once('for_php7.php');

require_once('knjc032dModel.inc');
require_once('knjc032dQuery.inc');

class knjc032dController extends Controller {
    var $ModelClassName = "knjc032dModel";
    var $ProgramID      = "KNJC033D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc032d":
                    $sessionInstance->knjc032dModel();      //コントロールマスタの呼び出し
                    $this->callView("knjc032dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc032dCtl = new knjc032dController;
?>
