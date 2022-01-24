<?php

require_once('for_php7.php');

require_once('knjm700Model.inc');
require_once('knjm700Query.inc');

class knjm700Controller extends Controller {
    var $ModelClassName = "knjm700Model";
    var $ProgramID      = "KNJM700";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "alldel":
                case "chdel":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                case "read":
                case "addread":
                case "main":
                    $this->callView("knjm700Form1");
                    break 2;
                case "dsub":
                    $this->callView("knjm700Form1");
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
$knjm700Ctl = new knjm700Controller;
//var_dump($_REQUEST);
?>
