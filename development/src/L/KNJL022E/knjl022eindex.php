<?php

require_once('for_php7.php');

require_once('knjl022eModel.inc');
require_once('knjl022eQuery.inc');

class knjl022eController extends Controller {
    var $ModelClassName = "knjl022eModel";
    var $ProgramID      = "KNJL022E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl022eForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl022eForm1");
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
$knjl022eCtl = new knjl022eController;
?>
