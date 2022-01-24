<?php

require_once('for_php7.php');

require_once('knje387Model.inc');
require_once('knje387Query.inc');

class knje387Controller extends Controller {
    var $ModelClassName = "knje387Model";
    var $ProgramID      = "KNJE387";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knje387Form1");
                    break 2;
                case "score1_sort":
                case "score2_sort":
                case "score3_sort":
                case "score4_sort":
                case "score5_sort":
                case "sort_total":
                case "sort_class":
                    $sessionInstance->sortSetting(trim($sessionInstance->cmd));
                    $this->callView("knje387Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje387Ctl = new knje387Controller;
?>
