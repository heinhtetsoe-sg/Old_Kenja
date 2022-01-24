<?php

require_once('for_php7.php');

require_once('knjz351vModel.inc');
require_once('knjz351vQuery.inc');

class knjz351vController extends Controller {
    var $ModelClassName = "knjz351vModel";
    var $ProgramID      = "KNJZ350V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ351V");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("change");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ351V");
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "list":
                    $this->callView("knjz351vFormList");
                    break 2;
                case "edit":
                case "sel";
                case "subclasscd";
                case "change":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ351V");
                    $this->callView("knjz351vForm1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz351vindex.php?cmd=list";
                    $args["right_src"] = "knjz351vindex.php?cmd=edit";
                    $args["cols"] = "45%,55%";
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
$knjz351vCtl = new knjz351vController;
//var_dump($_REQUEST);
?>
