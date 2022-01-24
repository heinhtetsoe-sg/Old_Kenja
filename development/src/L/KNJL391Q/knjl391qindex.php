<?php

require_once('for_php7.php');

require_once('knjl391qModel.inc');
require_once('knjl391qQuery.inc');

class knjl391qController extends Controller {
    var $ModelClassName = "knjl391qModel";
    var $ProgramID      = "KNJL391Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                    $this->callView("knjl391qForm1");
                    break 2;
                case "exec":
                    $this->callView("knjl391qForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjl391qCtl = new knjl391qController;
?>
