<?php

require_once('for_php7.php');

require_once('knjb3052Model.inc');
require_once('knjb3052Query.inc');

class knjb3052Controller extends Controller {
    var $ModelClassName = "knjb3052Model";
    var $ProgramID      = "KNJB3052";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "changeSeme":
                case "changeDate":
                case "knjb3052":
                    $sessionInstance->knjb3052Model();
                    $this->callView("knjb3052Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjb3052");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb3052Ctl = new knjb3052Controller;
//var_dump($_REQUEST);
?>
