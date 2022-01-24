<?php

require_once('for_php7.php');

require_once('knjz216aModel.inc');
require_once('knjz216aQuery.inc');

class knjz216aController extends Controller {
    var $ModelClassName = "knjz216aModel";
    var $ProgramID      = "KNJZ216A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "changeCmb":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz216aForm2");
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("changeCmb");
                    break 1;
                case "list":                
                case "leftChange":                
                    $this->callView("knjz216aForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz216aindex.php?cmd=list";
                    $args["right_src"] = "knjz216aindex.php?cmd=edit";
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
$knjz216aCtl = new knjz216aController;
//var_dump($_REQUEST);
?>
