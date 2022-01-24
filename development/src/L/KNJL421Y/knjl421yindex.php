<?php

require_once('for_php7.php');

require_once('knjl421yModel.inc');
require_once('knjl421yQuery.inc');

class knjl421yController extends Controller {
    var $ModelClassName = "knjl421yModel";
    var $ProgramID      = "KNJL421Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl421y":
                case "change":
                    $sessionInstance->knjl421yModel();
                    $this->callView("knjl421yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl421yCtl = new knjl421yController;
?>
