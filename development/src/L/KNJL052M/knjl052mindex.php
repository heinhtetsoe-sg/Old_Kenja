<?php

require_once('for_php7.php');

require_once('knjl052mModel.inc');
require_once('knjl052mQuery.inc');

class knjl052mController extends Controller {
    var $ModelClassName = "knjl052mModel";
    var $ProgramID      = "KNJL052M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjl052mForm1");
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
$knjl052mCtl = new knjl052mController;
?>
