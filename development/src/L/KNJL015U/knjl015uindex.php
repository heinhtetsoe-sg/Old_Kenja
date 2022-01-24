<?php

require_once('for_php7.php');

require_once('knjl015uModel.inc');
require_once('knjl015uQuery.inc');

class knjl015uController extends Controller {
    var $ModelClassName = "knjl015uModel";
    var $ProgramID      = "KNJL015U";

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
                        $this->callView("knjl015uForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl015uForm1");
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
$knjl015uCtl = new knjl015uController;
?>
