<?php

require_once('for_php7.php');

require_once('knjp854Model.inc');
require_once('knjp854Query.inc');

class knjp854Controller extends Controller
{
    public $ModelClassName = "knjp854Model";
    public $ProgramID      = "KNJP854";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "right_list":
                case "main":
                case "chgMonth":
                case "chgSlip":
                case "clear":
                    $this->callView("knjp854Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sendSearch":
                    $args["left_src"]  = REQUESTROOT ."/X/KNJXEXP_KOUNOUKIN/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&search_div=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&schkind=1";
                    $args["left_src"] .= "&grade=1";
                    $args["left_src"] .= "&hr_class=1";
                    $args["left_src"] .= "&select_grd=1";
                    $args["left_src"] .= "&schno=1";
                    $args["left_src"] .= "&name_width=150";
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    $args["left_src"] .= "&PATH=" .urlencode("/P/KNJP854/knjp854index.php?cmd=right_list");
                    $args["right_src"] = "knjp854index.php?cmd=right_list";
                    $args["cols"] = "380,*";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp854Ctl = new knjp854Controller();
