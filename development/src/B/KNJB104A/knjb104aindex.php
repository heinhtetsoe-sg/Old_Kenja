<?php

require_once('for_php7.php');

require_once('knjb104aModel.inc');
require_once('knjb104aQuery.inc');

class knjb104aController extends Controller
{
    public $ModelClassName = "knjb104aModel";
    public $ProgramID      = "KNJB104A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "gyouretu":
                case "edit":
                case "clear":
                    $this->callView("knjb104aForm2");
                    break 2;
                case "list":
                case "init":
                    $this->callView("knjb104aForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
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
                    $args["left_src"] = "knjb104aindex.php?cmd=init";
                    $args["right_src"] = "knjb104aindex.php?cmd=edit";
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
$knjb104aCtl = new knjb104aController();
