<?php

require_once('for_php7.php');

require_once('knjz210vModel.inc');
require_once('knjz210vQuery.inc');

class knjz210vController extends Controller {
    var $ModelClassName = "knjz210vModel";
    var $ProgramID      = "KNJZ210V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz210vForm2");
                    break 2;
                case "list":
                case "combo":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz210vForm1");
                    break 2;
                case "copy_2":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel_2();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz210vindex.php?cmd=list";
                    $args["right_src"] = "";
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
$knjz210vCtl = new knjz210vController;
?>
