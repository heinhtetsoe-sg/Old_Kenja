<?php

require_once('for_php7.php');

require_once('knjb0010Model.inc');
require_once('knjb0010Query.inc');

class knjb0010Controller extends Controller
{
    public $ModelClassName = "knjb0010Model";
    public $ProgramID      = "KNJB0010";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjb0010Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjb0010Form1");
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
                    $args["left_src"] = "knjb0010index.php?cmd=list";
                    $args["right_src"] = "knjb0010index.php?cmd=edit";
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
$knjb0010Ctl = new knjb0010Controller();
