<?php

require_once('for_php7.php');

require_once('knjxexp_ghrModel.inc');
require_once('knjxexp_ghrQuery.inc');

class knjxexp_ghrController extends Controller {
    var $ModelClassName = "knjxexp_ghrModel";
    var $ProgramID      = "KNJXEXP_GHR";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right":
                    $this->callView("knjxSearch");
                    break 2;
                case "list":
                case "chg_hukusiki_radio":
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjxexp_ghrForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("list");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxexp_ghrCtl = new knjxexp_ghrController;
//var_dump($_REQUEST);
?>
