<?php

require_once('for_php7.php');

require_once('knjl327mModel.inc');
require_once('knjl327mQuery.inc');

class knjl327mController extends Controller {
    var $ModelClassName = "knjl327mModel";
    var $ProgramID      = "KNJL327M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327m":
                    $sessionInstance->knjl327mModel();
                    $this->callView("knjl327mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl327mCtl = new knjl327mController;
?>
