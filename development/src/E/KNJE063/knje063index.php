<?php

require_once('for_php7.php');

require_once('knje063Model.inc');
require_once('knje063Query.inc');

class knje063Controller extends Controller
{
    public $ModelClassName = "knje063Model";
    public $ProgramID      = "KNJE063";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "curriculum":
                case "class":
                case "add_year":
                case "subclasscd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje063Form2");
                    break 2;
                case "right_list":
                case "list":
                case "sort":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knje063Form1");
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
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/E/KNJE063/knje063index.php?cmd=right") ."&button=1";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";

                    $args["right_src"] = "knje063index.php?cmd=right";
                    $args["cols"] = "20%,80%";
                    View::frame($args);
                    return;
                case "right":
                    $args["right_src"] = "knje063index.php?cmd=right_list";
                    $args["edit_src"]  = "knje063index.php?cmd=edit";
                    $args["rows"] = "35%,*%";
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
$knje063Ctl = new knje063Controller();
