<?php
require_once('knjl715hModel.inc');
require_once('knjl715hQuery.inc');

class knjl715hController extends Controller {
    var $ModelClassName = "knjl715hModel";
    var $ProgramID      = "KNJL715H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl715hForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl715hForm1");
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
$knjl715hCtl = new knjl715hController;
?>
