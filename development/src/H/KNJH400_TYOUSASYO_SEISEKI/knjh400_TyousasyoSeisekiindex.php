<?php

require_once('for_php7.php');

require_once('knjh400_TyousasyoSeisekiModel.inc');
require_once('knjh400_TyousasyoSeisekiQuery.inc');

class knjh400_TyousasyoSeisekiController extends Controller
{
    public $ModelClassName = "knjh400_TyousasyoSeisekiModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "curriculum":
                case "class":
                case "add_year":
                case "subclasscd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_TyousasyoSeisekiForm2");
                    break 2;
                case "right_list":
                case "list":
                case "sort":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjh400_TyousasyoSeisekiForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjh400_TyousasyoSeisekiForm1");
                    break 2;
                case "right":
                    $args["right_src"] = "knjh400_TyousasyoSeisekiindex.php?cmd=right_list";
                    $args["edit_src"]  = "knjh400_TyousasyoSeisekiindex.php?cmd=edit";
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
$knjh400_TyousasyoSeisekiCtl = new knjh400_TyousasyoSeisekiController();
