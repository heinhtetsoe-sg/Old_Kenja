<?php

require_once('for_php7.php');

require_once('knjz233Model.inc');
require_once('knjz233Query.inc');

class knjz233Controller extends Controller {
    var $ModelClassName = "knjz233Model";
    var $ProgramID      = "KNJZ233";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz233Form2");
                    break 2;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                case "check":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                    $this->callView("knjz233Form1");
                    break 2;
                case "clear":
                case "list2":
                    $this->callView("knjz233Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz233index.php?cmd=list";
                    $args["right_src"] = "knjz233index.php?cmd=sel";
                    $args["cols"] = "50%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz233Ctl = new knjz233Controller;
//var_dump($_REQUEST);
?>
