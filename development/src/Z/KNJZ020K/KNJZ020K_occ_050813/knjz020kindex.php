<?php

require_once('for_php7.php');

require_once('knjz020kModel.inc');
require_once('knjz020kQuery.inc');

class knjz020kController extends Controller {
    var $ModelClassName = "knjz020kModel";
    var $ProgramID      = "KNJZ020K";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjz020kForm2");
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
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                case "end":
                    $this->callView("knjz020kForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "ado":
                case "change_testdiv":
                case "adoreset":
                    $this->callView("adoptiondiv");
                    break 2;
                case "adoupdate":
                    $sessionInstance->getAdoUpdateModel();
                    $sessionInstance->setCmd("ado");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz020kindex.php?cmd=list";
                    $args["right_src"] = "knjz020kindex.php?cmd=edit";
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
$knjz020kCtl = new knjz020kController;
//var_dump($_REQUEST);
?>
