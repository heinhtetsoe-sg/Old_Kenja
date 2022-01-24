<?php

require_once('for_php7.php');

require_once('knjm260Model.inc');
require_once('knjm260Query.inc');

class knjm260Controller extends Controller {
    var $ModelClassName = "knjm260Model";
    var $ProgramID      = "KNJM260";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "alldel":
                case "chdel":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm260Form1");
                    break 2;
                case "dsub":
                    $this->callView("knjm260Form1");
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
$knjm260Ctl = new knjm260Controller;
//var_dump($_REQUEST);
?>
