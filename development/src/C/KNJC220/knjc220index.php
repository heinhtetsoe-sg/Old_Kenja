<?php

require_once('for_php7.php');

require_once('knjc220Model.inc');
require_once('knjc220Query.inc');

class knjc220Controller extends Controller {
    var $ModelClassName = "knjc220Model";
    var $ProgramID      = "KNJC220";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjc220Model();
                    $this->callView("knjc220Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjc220Ctl = new knjc220Controller;
?>
