<?php

require_once('for_php7.php');

require_once('knjmp801Model.inc');
require_once('knjmp801Query.inc');

class knjmp801Controller extends Controller {
    var $ModelClassName = "knjmp801Model";
    var $ProgramID      = "KNJMP801";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp801":
                case "change_class":
                case "read":
                    $sessionInstance->knjmp801Model();
                    $this->callView("knjmp801Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
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
$knjmp801Ctl = new knjmp801Controller;
var_dump($_REQUEST);
?>
