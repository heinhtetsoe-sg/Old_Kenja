<?php

require_once('for_php7.php');

require_once('knjl328mModel.inc');
require_once('knjl328mQuery.inc');

class knjl328mController extends Controller {
    var $ModelClassName = "knjl328mModel";
    var $ProgramID      = "KNJL328M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328m":
                    $sessionInstance->knjl328mModel();
                    $this->callView("knjl328mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl328mCtl = new knjl328mController;
?>
