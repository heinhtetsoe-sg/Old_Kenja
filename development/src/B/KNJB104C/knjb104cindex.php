<?php

require_once('for_php7.php');

require_once('knjb104cModel.inc');
require_once('knjb104cQuery.inc');

class knjb104cController extends Controller
{
    public $ModelClassName = "knjb104cModel";
    public $ProgramID      = "KNJB104C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjb104cForm2");
                    break 2;
                case "list":
                case "list2":
                case "init":
                    $this->callView("knjb104cForm1");
                    break 2;
                case "delete":
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
                    $args["left_src"] = "knjb104cindex.php?cmd=init";
                    $args["right_src"] = "knjb104cindex.php?cmd=edit";
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
$knjb104cCtl = new knjb104cController();
