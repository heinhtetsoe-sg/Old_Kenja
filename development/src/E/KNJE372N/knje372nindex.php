<?php
require_once('knje372nModel.inc');
require_once('knje372nQuery.inc');

class knje372nController extends Controller {
    var $ModelClassName = "knje372nModel";
    var $ProgramID      = "KNJE372N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "select":
                case "clear":
                case "changeClass":
                    $this->callView("knje372nForm2");
                    break 2;
                case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                case "changeOyear":
                    $this->callView("knje372nForm1");
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
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $sessionInstance->boot_flg = true;
                    $args["left_src"] = "knje372nindex.php?cmd=list";
                    $args["right_src"] = "knje372nindex.php?cmd=edit";
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
$knje372nCtl = new knje372nController;
?>
