<?php

require_once('for_php7.php');

require_once('knje063bModel.inc');
require_once('knje063bQuery.inc');

class knje063bController extends Controller
{
    public $ModelClassName = "knje063bModel";
    public $ProgramID      = "KNJE063B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "add_year":
                case "class":
                case "curriculum":
                case "subclass":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje063bForm2");
                    break 2;
                case "right_list":
                case "list":
                case "sort":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knje063bForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                case "delete2":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    if (trim($sessionInstance->cmd) == "delete") {
                        $sessionInstance->setCmd("list");
                    } else {
                        $sessionInstance->setCmd("edit");
                    }
                    break 1;
                case "subform1":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje063bSubForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/E/KNJE063B/knje063bindex.php?cmd=right") ."&button=1&HANDICAP_FLG=1";
                    $args["right_src"] = "knje063bindex.php?cmd=right";
                    $args["cols"] = "20%,80%";
                    View::frame($args);
                    return;
                case "right":
                    $args["right_src"] = "knje063bindex.php?cmd=right_list";
                    $args["edit_src"]  = "knje063bindex.php?cmd=edit";
                    $args["rows"] = "30%,*%";
                    View::frame($args, "frame3.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje063bCtl = new knje063bController();
?>
