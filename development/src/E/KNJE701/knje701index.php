<?php

require_once('for_php7.php');

require_once('knje701Model.inc');
require_once('knje701Query.inc');

class knje701Controller extends Controller
{
    public $ModelClassName = "knje701Model";
    public $ProgramID      = "KNJE701";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "header_chenge":
                    $this->callView("knje701Form2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knje701Form1");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
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
                    $args["left_src"] = "knje701index.php?cmd=list";
                    $args["right_src"] = "knje701index.php?cmd=edit";
                    $args["cols"] = "45%,55%";
                    View::frame($args);
                    // no break
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje701Ctl = new knje701Controller();
