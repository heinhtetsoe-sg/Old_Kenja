<?php

require_once('knjl065iModel.inc');
require_once('knjl065iQuery.inc');

class knjl065iController extends Controller {
    var $ModelClassName = "knjl065iModel";
    var $ProgramID      = "KNJL065I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl065iForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl065iCtl = new knjl065iController;
?>
