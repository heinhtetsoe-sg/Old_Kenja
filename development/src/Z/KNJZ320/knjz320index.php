<?php

require_once('for_php7.php');

require_once('knjz320Model.inc');
require_once('knjz320Query.inc');

class knjz320Controller extends Controller {
    var $ModelClassName = "knjz320Model";
    var $ProgramID      = "KNJZ320";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "main":
                case "copy":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjz320Form2");
                    break 2;
                case "init":
                case "tree":
                    $sessionInstance->getTreeModel();
                    $this->callView("knjz320Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "listauth":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz320Form3");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjz320index.php?cmd=tree";
                    $args["right_src"]  = "knjz320index.php?cmd=main";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz320Ctl = new knjz320Controller;
?>
