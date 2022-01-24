<?php

require_once('for_php7.php');

require_once('knjm801Model.inc');
require_once('knjm801Query.inc');

class knjm801Controller extends Controller {
    var $ModelClassName = "knjm801Model";
    var $ProgramID      = "KNJM801";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm801":
                case "change_class":
                case "read":
                    $sessionInstance->knjm801Model();
                    $this->callView("knjm801Form1");
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
$knjm801Ctl = new knjm801Controller;
var_dump($_REQUEST);
?>
