<?php

require_once('for_php7.php');

require_once('knjl375mModel.inc');
require_once('knjl375mQuery.inc');

class knjl375mController extends Controller {
    var $ModelClassName = "knjl375mModel";
    var $ProgramID      = "KNJL375M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl375m":
                    $sessionInstance->knjl375mModel();
                    $this->callView("knjl375mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl375mCtl = new knjl375mController;
?>
