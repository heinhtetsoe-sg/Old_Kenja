<?php

require_once('for_php7.php');

require_once('knjx_d132aModel.inc');
require_once('knjx_d132aQuery.inc');

class knjx_d132aController extends Controller {
    var $ModelClassName = "knjx_d132aModel";
    var $ProgramID      = "KNJX_D132A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "teikei":
                    $this->callView("knjx_d132aForm1");
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
$knjx_d132aCtl = new knjx_d132aController;
?>
