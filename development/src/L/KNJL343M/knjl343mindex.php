<?php

require_once('for_php7.php');

require_once('knjl343mModel.inc');
require_once('knjl343mQuery.inc');

class knjl343mController extends Controller {
    var $ModelClassName = "knjl343mModel";
    var $ProgramID      = "KNJL343M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343m":
                    $sessionInstance->knjl343mModel();
                    $this->callView("knjl343mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl343mCtl = new knjl343mController;
?>
