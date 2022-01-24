<?php

require_once('for_php7.php');

require_once('knjb1217Model.inc');
require_once('knjb1217Query.inc');

class knjb1217Controller extends Controller {
    var $ModelClassName = "knjb1217Model";
    var $ProgramID      = "KNJB1217";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "knjb1217":
                case "read":
                    $sessionInstance->knjb1217Model();
                    $this->callView("knjb1217Form1");
                    exit;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1217Ctl = new knjb1217Controller;
var_dump($_REQUEST);
?>
