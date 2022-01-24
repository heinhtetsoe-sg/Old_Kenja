<?php

require_once('for_php7.php');

require_once('knjz060_3Model.inc');
require_once('knjz060_3Query.inc');

class knjz060_3Controller extends Controller {
    var $ModelClassName = "knjz060_3Model";
    var $ProgramID      = "KNJZ060";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ060_3");
                    $this->callView("knjz060_3Form2");
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ060_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ060_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz060_3Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ060_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz060_3index.php?cmd=list";
                    $args["right_src"] = "knjz060_3index.php?cmd=edit";
                    $args["cols"] = "42%,58%";
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
$knjz060_3Ctl = new knjz060_3Controller;
//var_dump($_REQUEST);
?>
