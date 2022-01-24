<?php

require_once('for_php7.php');

require_once('knjl342sModel.inc');
require_once('knjl342sQuery.inc');

class knjl342sController extends Controller {
    var $ModelClassName = "knjl342sModel";
    var $ProgramID      = "KNJL342S";

    function main()  {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342s":
                    $sessionInstance->knjl342sModel();
                    $this->callView("knjl342sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl342sCtl = new knjl342sController;
?>
