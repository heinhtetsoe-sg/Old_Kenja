<?php

require_once('for_php7.php');


require_once('knjd321Model.inc');
require_once('knjd321Query.inc');

class knjd321Controller extends Controller {
    var $ModelClassName = "knjd321Model";
    var $ProgramID      = "KNJD321";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "search":
                case "reset":
                    $this->callView("knjd321Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd321Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjd321Ctl = new knjd321Controller;
?>
