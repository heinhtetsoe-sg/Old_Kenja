<?php

require_once('for_php7.php');

require_once('knjwa170Model.inc');
require_once('knjwa170Query.inc');

class knjwa170Controller extends Controller {
    var $ModelClassName = "knjwa170Model";
    var $ProgramID      = "KNJWA170";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "monthChange":
                case "change":
                case "edit":
                case "search":
                case "read":
                    $this->callView("knjwa170Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
                    break 1;
                //上覧から下欄へ
                case "read2":
                    $this->callView("knjwa170Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJWA170Ctl = new knjwa170Controller;
//var_dump($_REQUEST);
?>
