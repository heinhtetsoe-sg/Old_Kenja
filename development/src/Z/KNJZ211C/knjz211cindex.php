<?php

require_once('for_php7.php');

require_once('knjz211cModel.inc');
require_once('knjz211cQuery.inc');

class knjz211cController extends Controller {
    var $ModelClassName = "knjz211cModel";
    var $ProgramID      = "KNJZ211C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            $sessionInstance->knjz211cModel();        //コントロールマスタの呼び出し
            switch (trim($sessionInstance->cmd)) {
                case "list":
                case "change":
                    $this->callView("knjz211cForm1");
                    break 2;
                case "edit":
                case "edit2":
                case "kakutei":
                case "reset":
                    $this->callView("knjz211cForm2");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("change");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjz211cindex.php?cmd=list";
                    $args["right_src"] = "knjz211cindex.php?cmd=edit";
                    $args["cols"] = "30%,70%";
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
$knjz211cCtl = new knjz211cController;
?>
