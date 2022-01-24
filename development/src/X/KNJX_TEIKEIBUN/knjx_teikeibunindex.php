<?php

require_once('for_php7.php');

require_once('knjx_teikeibunModel.inc');
require_once('knjx_teikeibunQuery.inc');

class knjx_teikeibunController extends Controller {
    var $ModelClassName = "knjx_teikeibunModel";
    var $ProgramID      = "KNJX_TEIKEIBUN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "teikei":
                    $this->callView("knjx_teikeibunForm1");
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
$knjx_teikeibunCtl = new knjx_teikeibunController;
?>
