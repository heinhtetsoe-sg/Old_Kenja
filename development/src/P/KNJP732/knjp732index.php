<?php

require_once('for_php7.php');

require_once('knjp732Model.inc');
require_once('knjp732Query.inc');

class knjp732Controller extends Controller {
    var $ModelClassName = "knjp732Model";
    var $ProgramID      = "KNJP732";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "error":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjp732Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjp732Form1");
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
$knjp732Ctl = new knjp732Controller;
?>
