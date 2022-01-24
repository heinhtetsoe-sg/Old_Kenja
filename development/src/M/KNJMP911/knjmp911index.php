<?php

require_once('for_php7.php');

require_once('knjmp911Model.inc');
require_once('knjmp911Query.inc');

class knjmp911Controller extends Controller {
    var $ModelClassName = "knjmp911Model";
    var $ProgramID      = "KNJMP911";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjmp911Form1");
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
$knjmp911Ctl = new knjmp911Controller;
//var_dump($_REQUEST);
?>
