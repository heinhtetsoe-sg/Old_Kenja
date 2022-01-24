<?php

require_once('for_php7.php');

require_once('knjl583aModel.inc');
require_once('knjl583aQuery.inc');

class knjl583aController extends Controller {
    var $ModelClassName = "knjl583aModel";
    var $ProgramID      = "KNJL583A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "end":
                    $this->callView("knjl583aForm1");
                    break 2;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl583aForm1");
                    }
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl583aCtl = new knjl583aController;
?>
