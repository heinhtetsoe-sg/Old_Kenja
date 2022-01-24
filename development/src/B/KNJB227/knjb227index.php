<?php

require_once('for_php7.php');

require_once('knjb227Model.inc');
require_once('knjb227Query.inc');

class knjb227Controller extends Controller {
    var $ModelClassName = "knjb227Model";
    var $ProgramID      = "KNJB227";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb227":
                    $sessionInstance->knjb227Model();
                    $this->callView("knjb227Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb227Ctl = new knjb227Controller;
//var_dump($_REQUEST);
?>
