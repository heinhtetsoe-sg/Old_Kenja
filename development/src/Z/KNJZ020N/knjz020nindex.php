<?php

require_once('for_php7.php');

require_once('knjz020nModel.inc');
require_once('knjz020nQuery.inc');

class knjz020nController extends Controller {
    var $ModelClassName = "knjz020nModel";
    var $ProgramID      = "KNJZ020N";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                    $this->callView("knjz020nForm2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyYearModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list2":
                case "list":
                    $this->callView("knjz020nForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz020nindex.php?cmd=list";
                    $args["right_src"] = "knjz020nindex.php?cmd=edit";
                    $args["cols"] = "40%,60%";
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
$knjz020nCtl = new knjz020nController;
?>
