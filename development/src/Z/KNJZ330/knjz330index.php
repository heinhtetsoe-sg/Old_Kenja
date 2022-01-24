<?php

require_once('for_php7.php');

require_once('knjz330Model.inc');
require_once('knjz330Query.inc');

class knjz330Controller extends Controller {
    var $ModelClassName = "knjz330Model";
    var $ProgramID      = "KNJZ330";     //プログラムID

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
                    $this->callView("knjz330Form2");
                    break 2;
                case "init":
                case "tree":
                    $sessionInstance->getTreeModel();
                    $this->callView("knjz330Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "listauth";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz330Form3");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjz330index.php?cmd=tree";
                    $args["right_src"]  = "knjz330index.php?cmd=main";
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
$knjz330Ctl = new knjz330Controller;
?>
