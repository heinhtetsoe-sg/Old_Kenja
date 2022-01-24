<?php

require_once('for_php7.php');
require_once('knjh650Model.inc');
require_once('knjh650Query.inc');

class knjh650Controller extends Controller
{
    public $ModelClassName = "knjh650Model";
    public $ProgramID      = "KNJH650";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "gakki":
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjh650Form1");
                    break 2;
                case "updateMain":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $programpath = $sessionInstance->getProgrampathModel();
                    //分割フレーム作成
                    if ($programpath == "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH650/knjh650index.php?cmd=edit") ."&button=3" ."&SES_FLG=2" ."&SEND_AUTH={$sessionInstance->auth}&grdGrade=1";
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode($programpath."/knjh650index.php?cmd=edit") ."&button=3" ."&SES_FLG=2" ."&SEND_AUTH={$sessionInstance->auth}&grdGrade=1";
                    }
                    $args["right_src"] = "knjh650index.php?cmd=edit";
                    $args["cols"] = "20%,*";
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
$knjh650Ctl = new knjh650Controller();
