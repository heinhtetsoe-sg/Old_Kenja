<?php

require_once('for_php7.php');

require_once('knjp801Model.inc');
require_once('knjp801Query.inc');

class knjp801Controller extends Controller {
    var $ModelClassName = "knjp801Model";
    var $ProgramID      = "KNJP801";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp801":
                case "change_class":
                case "read":
                    $sessionInstance->knjp801Model();
                    $this->callView("knjp801Form1");
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
$knjp801Ctl = new knjp801Controller;
var_dump($_REQUEST);
?>
