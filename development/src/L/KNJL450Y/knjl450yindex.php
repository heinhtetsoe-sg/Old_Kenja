<?php

require_once('for_php7.php');

require_once('knjl450yModel.inc');
require_once('knjl450yQuery.inc');

class knjl450yController extends Controller {
    var $ModelClassName = "knjl450yModel";
    var $ProgramID      = "KNJL450Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl450y":
                case "change":
                    $sessionInstance->knjl450yModel();
                    $this->callView("knjl450yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl450yCtl = new knjl450yController;
?>
