<?php

require_once('for_php7.php');

require_once('knjp050kModel.inc');
require_once('knjp050kQuery.inc');

class knjp050kController extends Controller {
    var $ModelClassName = "knjp050kModel";
    var $ProgramID      = "knjp050k";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                case "change2":
                case "reload":
                    $this->callView("knjp050kForm2");
                    break 2;
                case "list":
                case "change":
                    $this->callView("knjp050kForm1");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
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
                    $sessionInstance->boot_flg = true;
                    $args["left_src"] = "knjp050kindex.php?cmd=list";
                    $args["right_src"] = "knjp050kindex.php?cmd=edit";
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
$knjp050kCtl = new knjp050kController;
?>
