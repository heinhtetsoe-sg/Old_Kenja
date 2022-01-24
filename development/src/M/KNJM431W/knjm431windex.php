<?php

require_once('for_php7.php');
require_once('knjm431wModel.inc');
require_once('knjm431wQuery.inc');

class knjm431wController extends Controller
{
    public $ModelClassName = "knjm431wModel";
    public $ProgramID      = "KNJM431W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "edit2":
                case "updEdit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjm431wForm2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjm431wForm1");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_SUBCLASS/index.php?";
                    $args["left_src"] .= "PROGRAMID=" .$this->ProgramID;
                    $args["left_src"] .= "&semester=1";
                    $args["left_src"] .= "&subclass=1";
                    $args["left_src"] .= "&chair=1";
                    if ($sessionInstance->Properties["knjm431wUseGakkiHyouka"] == "1") {
                        $args["left_src"] .= "&testtype=2";
                    } else {
                        $args["left_src"] .= "&testtype=1";
                    }
                    $args["left_src"] .= "&req_flg=1";
                    $args["left_src"] .= "&resultcnt=1";
                    $args["left_src"] .= "&search=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}";
                    $args["left_src"] .= "&PATH=" .urlencode("/M/KNJM431W/knjm431windex.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjm431windex.php?cmd=right_list";
                    $args["edit_src"]  = "knjm431windex.php?cmd=edit";
                    $args["cols"] = "30%,70%";
                    $args["rows"] = "50%,50%";
                    View::frame($args, "frame2.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm431wCtl = new knjm431wController();
