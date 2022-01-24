<?php

require_once('for_php7.php');

require_once('knjz210tModel.inc');
require_once('knjz210tQuery.inc');

class knjz210tController extends Controller {
    var $ModelClassName = "knjz210tModel";
    var $ProgramID      = "KNJZ210T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                    $this->callView("knjz210tForm2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjz210tForm1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "copy_2":
                    $sessionInstance->getCopyModel_2();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "update":
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
                    $args["left_src"] = "knjz210tindex.php?cmd=list";
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
$knjz210tCtl = new knjz210tController;
?>
