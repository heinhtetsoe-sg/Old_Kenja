<?php

require_once('for_php7.php');

require_once('knjz071Model.inc');
require_once('knjz071Query.inc');

class knjz071Controller extends Controller {
    var $ModelClassName = "knjz071Model";
    var $ProgramID      = "KNJZ071";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz071Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "changeCmb":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz071Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "call":
                    //分割フレーム作成
                    $args["left_src"] = "knjz071index.php?cmd=list&year_code=".VARS::request("year_code");
                    $args["right_src"] = "knjz071index.php?cmd=edit&year_code=".VARS::request("year_code");
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
$knjz071Ctl = new knjz071Controller;
//var_dump($_REQUEST);
?>
