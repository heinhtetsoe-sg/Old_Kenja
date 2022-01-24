<?php

require_once('for_php7.php');

require_once('knjh540Model.inc');
require_once('knjh540Query.inc');

class knjh540Controller extends Controller {
    var $ModelClassName = "knjh540Model";
    var $ProgramID      = "KNJH540";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                    $this->callView("knjh540Form2");
                    break 2;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":                
                case "list2":                
                case "changeOyear":                
                    $this->callView("knjh540Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjh540index.php?cmd=list";
                    $args["right_src"] = "knjh540index.php?cmd=edit";
                    $args["cols"] = "50%,*%";
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
$knjh540Ctl = new knjh540Controller;
//var_dump($_REQUEST);
?>
