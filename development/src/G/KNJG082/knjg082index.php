<?php

require_once('for_php7.php');

require_once('knjg082Model.inc');
require_once('knjg082Query.inc');

class knjg082Controller extends Controller {
    var $ModelClassName = "knjg082Model";
    var $ProgramID      = "KNJG082";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                    $this->callView("knjg082Form1");
                    break 2;
                case "sel":
                case "change":
                case "clear":
                    $this->callView("knjg082Form2");
                    break 2;
                case "insert":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjg082index.php?cmd=list";
                    $args["right_src"] = "knjg082index.php?cmd=sel";
                    $args["cols"] = "35%,*";
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
$knjg082Ctl = new knjg082Controller;
?>
