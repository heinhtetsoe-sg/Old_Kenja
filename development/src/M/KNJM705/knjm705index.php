<?php

require_once('for_php7.php');

require_once('knjm705Model.inc');
require_once('knjm705Query.inc');

class knjm705Controller extends Controller {
    var $ModelClassName = "knjm705Model";
    var $ProgramID      = "KNJM705";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "chdel":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                case "change":
                case "read":
                case "addread":
                case "main":
                case "sort":
                    $this->callView("knjm705Form1");
                    break 2;
                case "dsub":
                    $this->callView("knjm705Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm705Ctl = new knjm705Controller;
?>
