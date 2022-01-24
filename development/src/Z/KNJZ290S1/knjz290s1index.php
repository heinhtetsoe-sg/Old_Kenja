<?php

require_once('for_php7.php');

require_once('knjz290s1Model.inc');
require_once('knjz290s1Query.inc');

class knjz290s1Controller extends Controller {
    var $ModelClassName = "knjz290s1Model";
    var $ProgramID      = "KNJZ290S1";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "selectEdit":
                case "addEdit":
                case "updEdit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz290s1Form2");
                    break 2;
                case "list":
                    $this->callView("knjz290s1Form1");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("addEdit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz290s1index.php?cmd=list";
                    $args["right_src"] = "knjz290s1index.php?cmd=edit";
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
$knjz290s1Ctl = new knjz290s1Controller;
//var_dump($_REQUEST);
?>
