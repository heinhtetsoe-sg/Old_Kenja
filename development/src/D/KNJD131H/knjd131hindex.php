<?php

require_once('for_php7.php');

require_once('knjd131hModel.inc');
require_once('knjd131hQuery.inc');

class knjd131hController extends Controller {
    var $ModelClassName = "knjd131hModel";
    var $ProgramID      = "KNJD131H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjd131h":
                case "clear":
                    $sessionInstance->knjd131hModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd131hForm1");
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
$knjd131hCtl = new knjd131hController;
?>
