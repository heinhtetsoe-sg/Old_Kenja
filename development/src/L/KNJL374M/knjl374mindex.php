<?php

require_once('for_php7.php');

require_once('knjl374mModel.inc');
require_once('knjl374mQuery.inc');

class knjl374mController extends Controller {
    var $ModelClassName = "knjl374mModel";
    var $ProgramID      = "KNJL374M";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl374m":
                    $sessionInstance->knjl374mModel();
                    $this->callView("knjl374mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl374mCtl = new knjl374mController;
?>
