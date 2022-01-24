<?php

require_once('for_php7.php');

require_once('knjwsearchwizasModel.inc');
require_once('knjwsearchwizasQuery.inc');

class knjwsearchwizasController extends Controller {
    var $ModelClassName = "knjwsearchwizasModel";
    var $ProgramID      = "KNJWSEARCHWIZAS";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "edit":
                case "select":
                case "search":
                case "search2":
                    $this->callView("knjwsearchwizasForm1");
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
$knjwsearchwizasCtl = new knjwsearchwizasController;
//var_dump($_REQUEST);
?>
