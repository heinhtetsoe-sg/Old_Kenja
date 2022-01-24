<?php

require_once('for_php7.php');

require_once('knjf140bModel.inc');
require_once('knjf140bQuery.inc');

class knjf140bController extends Controller {
    var $ModelClassName = "knjf140bModel";
    var $ProgramID      = "KNJF140B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":        //CSV取り込み
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":   //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf140bForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjf140bForm1");
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
$knjf140bCtl = new knjf140bController;
?>
