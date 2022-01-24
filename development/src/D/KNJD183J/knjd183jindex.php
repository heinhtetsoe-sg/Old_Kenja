<?php

require_once('for_php7.php');

require_once('knjd183jModel.inc');
require_once('knjd183jQuery.inc');

class knjd183jController extends Controller {
    var $ModelClassName = "knjd183jModel";
    var $ProgramID      = "KNJD183J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd183j";
                    $sessionInstance->knjd183jModel();
                    $this->callView("knjd183jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd183jCtl = new knjd183jController;
?>
