<?php

require_once('for_php7.php');

require_once('knjl342mModel.inc');
require_once('knjl342mQuery.inc');

class knjl342mController extends Controller {
    var $ModelClassName = "knjl342mModel";
    var $ProgramID      = "KNJL342M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342m":
                    $sessionInstance->knjl342mModel();
                    $this->callView("knjl342mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl342mCtl = new knjl342mController;
?>
