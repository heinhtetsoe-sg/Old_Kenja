<?php

require_once('for_php7.php');

require_once('knjz070_3Model.inc');
require_once('knjz070_3Query.inc');

class knjz070_3Controller extends Controller {
    var $ModelClassName = "knjz070_3Model";
    var $ProgramID      = "KNJZ070_3";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ070_3");
                    $this->callView("knjz070_3Form2");
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ070_3");
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ070_3");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz070_3Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ070_3");
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz070_3index.php?cmd=list";
                    $args["right_src"] = "knjz070_3index.php?cmd=edit";
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
$knjz070_3Ctl = new knjz070_3Controller;
//var_dump($_REQUEST);
?>
