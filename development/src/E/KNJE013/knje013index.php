<?php

require_once('for_php7.php');

require_once('knje013Model.inc');
require_once('knje013Query.inc');

class knje013Controller extends Controller {
    var $ModelClassName = "knje013Model";
    var $ProgramID      = "KNJE013";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje013":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knje013Model();
                    $this->callView("knje013Form1");
                    exit;
                case "exec":     //実行
                    if (!$sessionInstance->getUpdateModel()) {
                        $this->callView("knje013Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knje013Ctl = new knje013Controller;
?>
