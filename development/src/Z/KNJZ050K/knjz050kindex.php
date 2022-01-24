<?php

require_once('for_php7.php');

require_once('knjz050kModel.inc');
require_once('knjz050kQuery.inc');

class knjz050kController extends Controller {
    var $ModelClassName = "knjz050kModel";
    var $ProgramID      = "KNJZ050K";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":                
                    $this->callView("knjz050kForm2");
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
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "change"://NO001
                    $this->callView("knjz050kForm1");
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
                    $args["left_src"] = "knjz050kindex.php?cmd=list";
                    $args["right_src"] = "knjz050kindex.php?cmd=edit";
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
$knjz050kCtl = new knjz050kController;
//var_dump($_REQUEST);
?>
