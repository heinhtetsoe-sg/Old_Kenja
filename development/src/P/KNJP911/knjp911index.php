<?php

require_once('for_php7.php');

require_once('knjp911Model.inc');
require_once('knjp911Query.inc');

class knjp911Controller extends Controller {
    var $ModelClassName = "knjp911Model";
    var $ProgramID      = "KNJP911";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjp911Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjp911Ctl = new knjp911Controller;
//var_dump($_REQUEST);
?>
