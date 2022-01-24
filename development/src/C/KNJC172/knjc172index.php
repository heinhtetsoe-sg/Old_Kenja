<?php

require_once('for_php7.php');

require_once('knjc172Model.inc');
require_once('knjc172Query.inc');

class knjc172Controller extends Controller {
    var $ModelClassName = "knjc172Model";
    var $ProgramID      = "KNJC172";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->knjc172Model();        //コントロールマスタの呼び出し
                    $this->callView("knjc172Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc172Ctl = new knjc172Controller;
//var_dump($_REQUEST);
?>
