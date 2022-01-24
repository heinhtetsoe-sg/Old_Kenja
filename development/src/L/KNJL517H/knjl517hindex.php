<?php

require_once('for_php7.php');

require_once('knjl517hModel.inc');
require_once('knjl517hQuery.inc');

class knjl517hController extends Controller {
    var $ModelClassName = "knjl517hModel";
    var $ProgramID      = "KNJL517H";

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
                case "data":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl517hForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl517hForm1");
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
$knjl517hCtl = new knjl517hController;
?>
