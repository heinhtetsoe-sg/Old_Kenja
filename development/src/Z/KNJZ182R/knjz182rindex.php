<?php

require_once('for_php7.php');

require_once('knjz182rModel.inc');
require_once('knjz182rQuery.inc');

class knjz182rController extends Controller {
    var $ModelClassName = "knjz182rModel";
    var $ProgramID      = "KNJZ182R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "list_change":
                case "up_list":
                    $this->callView("knjz182rForm1");
                    break 2;
                case "sel":
                case "change":
                case "level":
                case "clear":
                    $this->callView("knjz182rForm2");
                    break 2;
                case "delete":
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz182rindex.php?cmd=list";
                    $args["right_src"] = "knjz182rindex.php?cmd=sel";
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
$knjz182rCtl = new knjz182rController;
?>
