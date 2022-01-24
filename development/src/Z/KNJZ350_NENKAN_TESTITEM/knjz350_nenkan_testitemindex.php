<?php

require_once('for_php7.php');

require_once('knjz350_nenkan_testitemModel.inc');
require_once('knjz350_nenkan_testitemQuery.inc');

class knjz350_nenkan_testitemController extends Controller {
    var $ModelClassName = "knjz350_nenkan_testitemModel";
    var $ProgramID      = "KNJZ350_NENKAN_TESTITEM";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "edit":
                case "reset":
                case "changeCmb":
                    $this->callView("knjz350_nenkan_testitemForm2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":                
                case "leftChange":                
                    $this->callView("knjz350_nenkan_testitemForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz350_nenkan_testitemindex.php?cmd=list";
                    $args["right_src"] = "knjz350_nenkan_testitemindex.php?cmd=edit";
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
$knjz350_nenkan_testitemCtl = new knjz350_nenkan_testitemController;
//var_dump($_REQUEST);
?>
