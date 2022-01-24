<?php

require_once('for_php7.php');

require_once('knjl081rModel.inc');
require_once('knjl081rQuery.inc');

class knjl081rController extends Controller {
    var $ModelClassName = "knjl081rModel";
    var $ProgramID      = "KNJL081R";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl081rForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl081rForm1");
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
$knjl081rCtl = new knjl081rController;
?>
