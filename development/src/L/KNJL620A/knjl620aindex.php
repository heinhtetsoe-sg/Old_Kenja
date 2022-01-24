<?php

require_once('for_php7.php');

require_once('knjl620aModel.inc');
require_once('knjl620aQuery.inc');

class knjl620aController extends Controller {
    var $ModelClassName = "knjl620aModel";
    var $ProgramID      = "KNJL620A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl620aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->knjl620aModel();
                    $this->callView("knjl620aForm1");
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
$knjl620aCtl = new knjl620aController;
?>
