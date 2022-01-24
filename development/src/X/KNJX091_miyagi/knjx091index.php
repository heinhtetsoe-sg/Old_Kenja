<?php

require_once('for_php7.php');

require_once('knjx091Model.inc');
require_once('knjx091Query.inc');

class knjx091Controller extends Controller {
    var $ModelClassName = "knjx091Model";
    var $ProgramID      = "knjx091";

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
                case "csv":
                case "head":
                case "error":
                case "data":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjx091Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjx091Form1");
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
$knjx091Ctl = new knjx091Controller;
?>
