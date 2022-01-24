<?php
require_once('knjh531Model.inc');
require_once('knjh531Query.inc');

class knjh531Controller extends Controller {
    var $ModelClassName = "knjh531Model";
    var $ProgramID      = "KNJH531";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjh531");
                    break 1;
                case "":
                case "knjh531":
                case "changeYear":
                case "back":
                    $sessionInstance->knjh531Model();
                    $this->callView("knjh531Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh531Ctl = new knjh531Controller;
?>
