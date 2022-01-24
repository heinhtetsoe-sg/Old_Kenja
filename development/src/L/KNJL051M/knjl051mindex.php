<?php

require_once('for_php7.php');

require_once('knjl051mModel.inc');
require_once('knjl051mQuery.inc');

class knjl051mController extends Controller {
    var $ModelClassName = "knjl051mModel";
    var $ProgramID      = "KNJL051M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjl051mForm1");
                    break 2;
                case "error":
                    $this->callView("error");
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
$knjl051mCtl = new knjl051mController;
?>
