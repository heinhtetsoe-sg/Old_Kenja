<?php

require_once('for_php7.php');

require_once('knjm410Model.inc');
require_once('knjm410Query.inc');

class knjm410Controller extends Controller {
    var $ModelClassName = "knjm410Model";
    var $ProgramID      = "KNJM410";

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
                case "main":
                case "addread":
                case "read":
                    $this->callView("knjm410Form1");
                    break 2;
                case "dsub":
                    $this->callView("knjm410Form1");
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
$knjm410Ctl = new knjm410Controller;
//var_dump($_REQUEST);
?>
