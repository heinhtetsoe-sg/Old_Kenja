<?php

require_once('for_php7.php');

require_once('knjx183Model.inc');
require_once('knjx183Query.inc');

class knjx183Controller extends Controller {
    var $ModelClassName = "knjx183Model";
    var $ProgramID      = "KNJX183";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":       //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjx183Form1");
                    }
                    break 2;
                case "":
                case "main":
                case "changeCmb":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjx183Form1");
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
$knjx183Ctl = new knjx183Controller;
?>
