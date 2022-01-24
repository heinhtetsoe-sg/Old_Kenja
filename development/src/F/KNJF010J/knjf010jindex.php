<?php

require_once('for_php7.php');

require_once('knjf010jModel.inc');
require_once('knjf010jQuery.inc');

class knjf010jController extends Controller
{
    public $ModelClassName = "knjf010jModel";
    public $ProgramID      = "knjf010j";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf010jForm1");
                    break 2;
                case "replace1":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf010jSubForm1");
                    break 2;
                case "replace_update1":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel1();
                    $sessionInstance->setCmd("replace1");
                    break 1;
                case "replace2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf010jSubForm2");
                    break 2;
                case "replace_update2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel2();
                    $sessionInstance->setCmd("replace2");
                    break 1;
                case "replace3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf010jSubForm3");
                    break 2;
                case "replace_update3":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel3();
                    $sessionInstance->setCmd("replace3");
                    break 1;
                case "replace4":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf010jSubForm4");
                    break 2;
                case "replace_update4":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel4();
                    $sessionInstance->setCmd("replace4");
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
                    $this->callView("knjf010jForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["useSpecial_Support_Hrclass"] == "1") {
                        $search = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF010J/knjf010jindex.php?cmd=edit") ."&button=1&SES_FLG=2&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    } else {
                        $search = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF010J/knjf010jindex.php?cmd=edit") ."&button=1&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    }
                    // no break
                case "back":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["useSpecial_Support_Hrclass"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php" .$search;
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    }
                    $args["right_src"] = "knjf010jindex.php?cmd=edit";
                    $args["cols"] = "19%,81%";
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
$knjf010jCtl = new knjf010jController();
