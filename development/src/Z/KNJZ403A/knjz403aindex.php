<?php

require_once('for_php7.php');

require_once('knjz403aModel.inc');
require_once('knjz403aQuery.inc');

class knjz403aController extends Controller {
    var $ModelClassName = "knjz403aModel";
    var $ProgramID      = "KNJZ403A";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":                
                case "reset":
                    $this->callView("knjz403aForm2");
                    break 2;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    $this->callView("knjz403aForm1");
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
                case "list":                
                    $this->callView("knjz403aForm1");
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
                    $args["left_src"] = "knjz403aindex.php?cmd=list";
                    $args["right_src"] = "knjz403aindex.php?cmd=edit";
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
$knjz403aCtl = new knjz403aController;
//var_dump($_REQUEST);
?>
