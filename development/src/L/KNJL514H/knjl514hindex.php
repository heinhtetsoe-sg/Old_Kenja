<?php

require_once('for_php7.php');

require_once('knjl514hModel.inc');
require_once('knjl514hQuery.inc');

class knjl514hController extends Controller {
    var $ModelClassName = "knjl514hModel";
    var $ProgramID      = "KNJL514H";

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
                case "csv2":
                case "head":
                case "error":
                case "data":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl514hForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl514hForm1");
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
$knjl514hCtl = new knjl514hController;
?>
