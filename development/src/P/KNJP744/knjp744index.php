<?php

require_once('for_php7.php');

require_once('knjp744Model.inc');
require_once('knjp744Query.inc');

class knjp744Controller extends Controller
{
    public $ModelClassName = "knjp744Model";
    public $ProgramID      = "KNJP744";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "clear":
                case "change":
                    $this->callView("knjp744Form2");
                    break 2;
                case "list":
                case "changeKind":
                    $this->callView("knjp744Form1");
                    break 2;
                case "add":
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
                    $sessionInstance->setAccessLogDetail("C", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("changeKind");
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
                    $args["left_src"]   = "knjp744index.php?cmd=list";
                    $args["right_src"]  = "knjp744index.php?cmd=edit";
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
$knjp744Ctl = new knjp744Controller;
