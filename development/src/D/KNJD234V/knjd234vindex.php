<?php

require_once('for_php7.php');

require_once('knjd234vModel.inc');
require_once('knjd234vQuery.inc');

class knjd234vController extends Controller {
    var $ModelClassName = "knjd234vModel";
    var $ProgramID      = "KNJD234V";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd234vModel();
                    $this->callView("knjd234vForm1");
                    exit;
                case "csvExe":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID); 
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvErr":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd234vForm1");
                    }
                    break 2;
                case "knjd234v";
                    $sessionInstance->knjd234vModel();
                    $this->callView("knjd234vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd234vCtl = new knjd234vController;
?>
