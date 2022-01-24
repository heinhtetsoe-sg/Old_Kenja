<?php

require_once('for_php7.php');

require_once('knjx_syukketsukirokuModel.inc');
require_once('knjx_syukketsukirokuQuery.inc');

class knjx_syukketsukirokuController extends Controller {
    var $ModelClassName = "knjx_syukketsukirokuModel";
    var $ProgramID      = "KNJX_SYUKKETSUKIROKU";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_syukketsukirokuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_syukketsukirokuCtl = new knjx_syukketsukirokuController;
//var_dump($_REQUEST);
?>
