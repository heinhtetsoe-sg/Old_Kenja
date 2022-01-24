<?php

require_once('for_php7.php');

require_once('knjj120aModel.inc');
require_once('knjj120aQuery.inc');

class knjj120aController extends Controller {
    var $ModelClassName = "knjj120aModel";
    var $ProgramID      = "KNJJ120A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "edit3":
                    $this->callView("knjj120aForm2");
                    break 2;
                case "clear":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "list2":
                    $this->callView("knjj120aForm1");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjj120aindex.php?cmd=list";
                    $args["right_src"]  = "knjj120aindex.php?cmd=edit";
                    $args["cols"] = "43%,58%";
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
$knjj120aCtl = new knjj120aController;
?>
