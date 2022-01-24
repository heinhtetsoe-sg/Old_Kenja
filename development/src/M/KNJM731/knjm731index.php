<?php

require_once('for_php7.php');

require_once('knjm731Model.inc');
require_once('knjm731Query.inc');

class knjm731Controller extends Controller {
    var $ModelClassName = "knjm731Model";
    var $ProgramID      = "KNJM731";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm731":
                case "clear":
                    $sessionInstance->knjm731Model();      //コントロールマスタの呼び出し
                    $this->callView("knjm731Form1");
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
$knjm731Ctl = new knjm731Controller;
?>
