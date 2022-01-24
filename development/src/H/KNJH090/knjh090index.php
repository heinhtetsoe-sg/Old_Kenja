<?php

require_once('for_php7.php');

require_once('knjh090Model.inc');
require_once('knjh090Query.inc');

class knjh090Controller extends Controller
{
    public $ModelClassName = "knjh090Model";
    public $ProgramID      = "KNJH090";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "edit2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh090Form2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjh090Form1");
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
                case "execute":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":

                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH090/knjh090index.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjh090index.php?cmd=right_list";
                    $args["edit_src"]  = "knjh090index.php?cmd=edit";
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
$knjh090Ctl = new knjh090Controller;
