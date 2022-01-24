<?php

require_once('for_php7.php');

require_once('knjl180dModel.inc');
require_once('knjl180dQuery.inc');

class knjl180dController extends Controller {
    var $ModelClassName = "knjl180dModel";
    var $ProgramID      = "KNJL180D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "read":
                case "main":
                case "reset":
                case "read2":
                case "next":
                case "back":
                case "end":
                    $this->callView("knjl180dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl180dForm1");
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
$knjl180dCtl = new knjl180dController;
?>
