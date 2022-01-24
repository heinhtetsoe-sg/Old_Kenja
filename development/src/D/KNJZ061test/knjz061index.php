<?php

require_once('for_php7.php');

require_once('knjz061Model.inc');
require_once('knjz061Query.inc');

class knjz061Controller extends Controller {
    var $ModelClassName = "knjz061Model";
    var $ProgramID      = "KNJZ061";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjz061Form2");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz061Form1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "call":
                    //分割フレーム作成
                    $args["left_src"] = "knjz061index.php?cmd=list";
                    $args["right_src"] = "knjz061index.php?cmd=edit";
                    $args["cols"] = "55%,45%";
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
$knjz061Ctl = new knjz061Controller;
//var_dump($_REQUEST);
?>
