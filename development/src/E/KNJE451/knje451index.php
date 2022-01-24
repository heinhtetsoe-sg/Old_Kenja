<?php

require_once('for_php7.php');

require_once('knje451Model.inc');
require_once('knje451Query.inc');

class knje451Controller extends Controller {
    var $ModelClassName = "knje451Model";
    var $ProgramID      = "KNJE451";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje451":
                    $sessionInstance->knje451Model();
                    $this->callView("knje451Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje451Ctl = new knje451Controller;
?>
