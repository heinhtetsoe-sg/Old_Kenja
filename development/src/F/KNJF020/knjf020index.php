<?php

require_once('for_php7.php');

require_once('knjf020Model.inc');
require_once('knjf020Query.inc');

class knjf020Controller extends Controller
{
    public $ModelClassName = "knjf020Model";
    public $ProgramID      = "KNJF020";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "send":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getSendModel();
                    break 2;
                case "edit":
                case "edit2":
                case "change":
                case "subEnd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf020Form1");
                    break 2;
                case "replace":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf020SubForm1");
                    break 2;
                case "sisiki":
                case "sisiki2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf020SubForm2");
                    break 2;
                case "replace_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "subUpdate":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getSubUpdateModel();
                    $sessionInstance->setCmd("sisiki");
                    break 1;
                case "subUpdate2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getSubUpdateModel();
                    $sessionInstance->setCmd("sisiki2");
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
                case "reset":
                    $this->callView("knjf020Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["useSpecial_Support_Hrclass"] == "1") {
                        $search = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF020/knjf020index.php?cmd=edit") ."&button=1&SES_FLG=2&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    } else {
                        $search = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF020/knjf020index.php?cmd=edit") ."&button=1&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    }
                    // no break
                case "back":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["useSpecial_Support_Hrclass"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php" .$search;
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    }
                    $args["right_src"] = "knjf020index.php?cmd=edit";
                    $args["cols"] = "22%,78%";
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
$knjf020Ctl = new knjf020Controller();
