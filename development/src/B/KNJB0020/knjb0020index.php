<?php

require_once('for_php7.php');

require_once('knjb0020Model.inc');
require_once('knjb0020Query.inc');

class knjb0020Controller extends Controller {
    var $ModelClassName = "knjb0020Model";
    var $ProgramID      = "KNJB0020";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjb0020Form1");
                    break 2;
                case "list":
                case "combo":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjb0020Form2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjb0020index.php?cmd=list";
                    $args["right_src"] = "knjb0020index.php?cmd=edit";
                    $args["cols"] = "39%,61%";
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
$knjb0020Ctl = new knjb0020Controller;
?>
