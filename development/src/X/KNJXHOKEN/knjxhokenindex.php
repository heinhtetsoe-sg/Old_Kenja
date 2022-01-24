<?php

require_once('for_php7.php');

require_once('knjxhokenModel.inc');
require_once('knjxhokenQuery.inc');

class knjxhokenController extends Controller {
    var $ModelClassName = "knjxhokenModel";
    var $ProgramID      = "knjxhoken";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "hoken1":
                case "hoken2":
                case "hoken3":
                case "hoken4":
                    $this->callView("knjxhokenForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxhokenCtl = new knjxhokenController;
?>
