<?php

require_once('for_php7.php');

require_once('knjl052nModel.inc');
require_once('knjl052nQuery.inc');

class knjl052nController extends Controller {
    var $ModelClassName = "knjl052nModel";
    var $ProgramID      = "KNJL052N";

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
                        $this->callView("knjl052nForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl052nForm1");
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
$knjl052nCtl = new knjl052nController;
?>
