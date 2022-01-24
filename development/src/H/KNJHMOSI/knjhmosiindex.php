<?php

require_once('for_php7.php');

require_once('knjhmosiModel.inc');
require_once('knjhmosiQuery.inc');

class knjhmosiController extends Controller {
    var $ModelClassName = "knjhmosiModel";
    var $ProgramID      = "KNJHMOSI";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjhmosiForm1");
                    exit;
                case "":
                    $sessionInstance->knjhmosiModel();
                    $this->callView("knjhmosiForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjhmosiCtl = new knjhmosiController;
?>
