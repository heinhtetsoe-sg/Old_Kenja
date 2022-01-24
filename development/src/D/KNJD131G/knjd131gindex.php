<?php

require_once('for_php7.php');

require_once('knjd131gModel.inc');
require_once('knjd131gQuery.inc');

class knjd131gController extends Controller {
    var $ModelClassName = "knjd131gModel";
    var $ProgramID      = "KNJD131G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjd131g":
                case "clear":
                    $sessionInstance->knjd131gModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd131gForm1");
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
$knjd131gCtl = new knjd131gController;
?>
