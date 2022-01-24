<?php

require_once('for_php7.php');

require_once('knjf140aModel.inc');
require_once('knjf140aQuery.inc');

class knjf140aController extends Controller {
    var $ModelClassName = "knjf140aModel";
    var $ProgramID      = "KNJF140A";

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
                        $this->callView("knjf140aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjf140aForm1");
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
$knjf140aCtl = new knjf140aController;
?>
