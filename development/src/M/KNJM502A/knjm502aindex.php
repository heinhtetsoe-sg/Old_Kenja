<?php

require_once('for_php7.php');

require_once('knjm502aModel.inc');
require_once('knjm502aQuery.inc');

class knjm502aController extends Controller {
    var $ModelClassName = "knjm502aModel";
    var $ProgramID      = "KNJM502A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjm502a":
                case "clear":
                    $sessionInstance->knjm502aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjm502aForm1");
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
$knjm502aCtl = new knjm502aController;
?>
