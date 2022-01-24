<?php

require_once('for_php7.php');

require_once('knjl355mModel.inc');
require_once('knjl355mQuery.inc');

class knjl355mController extends Controller {
    var $ModelClassName = "knjl355mModel";
    var $ProgramID      = "KNJL355M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl355m":
                    $sessionInstance->knjl355mModel();
                    $this->callView("knjl355mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl355mCtl = new knjl355mController;
?>
