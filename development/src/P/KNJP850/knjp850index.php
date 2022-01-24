<?php

require_once('for_php7.php');

require_once('knjp850Model.inc');
require_once('knjp850Query.inc');

class knjp850Controller extends Controller
{
    public $ModelClassName = "knjp850Model";
    public $ProgramID      = "KNJP850";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjp850Form2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjp850Form1");
                    break 2;
                case "csv":
                case "csv2":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjp850Form2");
                    }
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sendSearch":
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP_KOUNOUKIN/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    if (trim($sessionInstance->cmd) == "sendSearch") {
                        $args["left_src"] .= "&cmd=sendSearch";
                        $args["left_src"] .= "&SEND_SCHREGNO=".$sessionInstance->sendSchregno;
                    }
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&search_div_KNJP850=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&sort=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&schkind=1";
                    $args["left_src"] .= "&grade=1";
                    $args["left_src"] .= "&hr_class=1";
                    $args["left_src"] .= "&grdDateDisp=1";
                    $args["left_src"] .= "&repayDisp=1";
                    $args["left_src"] .= "&select_grd=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["left_src"] .= "&PATH=" .urlencode("/P/KNJP850/knjp850index.php?cmd=right_list");
                    $args["right_src"] = "knjp850index.php?cmd=right_list";
                    if (trim($sessionInstance->cmd) == "sendSearch") {
                        $args["right_src"] .= "&SCHREGNO=".$sessionInstance->sendSchregno;
                    }
                    $args["edit_src"]  = "knjp850index.php?cmd=edit";
                    $args["cols"] = "435px,*";
                    $args["rows"] = "55%,45%";
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
$knjp850Ctl = new knjp850Controller();
