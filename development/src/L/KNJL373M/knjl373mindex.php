<?php

require_once('for_php7.php');

require_once('knjl373mModel.inc');
require_once('knjl373mQuery.inc');

class knjl373mController extends Controller {
    var $ModelClassName = "knjl373mModel";
    var $ProgramID      = "KNJL373M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl373m":
                    $sessionInstance->knjl373mModel();
                    $this->callView("knjl373mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl373mCtl = new knjl373mController;
?>
