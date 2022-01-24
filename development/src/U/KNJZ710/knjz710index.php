<?php

require_once('for_php7.php');

require_once('knjz710Model.inc');
require_once('knjz710Query.inc');

class knjz710Controller extends Controller {
    var $ModelClassName = "knjz710Model";
    var $ProgramID      = "KNJz710";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                case "copy":
                    $this->callView("knjz710Form2");
                    break 2;
                /*case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;*/
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "makeMock":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->updateMock();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "list":
                case "changeMockyear":
                    $this->callView("knjz710Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz710index.php?cmd=list";
                    $args["right_src"] = "knjz710index.php?cmd=edit";
                    $args["cols"] = "50%,50%";
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
$knjz710Ctl = new knjz710Controller;
//var_dump($_REQUEST);
?>
