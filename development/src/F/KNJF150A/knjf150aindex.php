<?php

require_once('for_php7.php');

require_once('knjf150aModel.inc');
require_once('knjf150aQuery.inc');

class knjf150aController extends Controller
{
    public $ModelClassName = "knjf150aModel";
    public $ProgramID      = "KNJF150A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150aForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "subform1":
                case "subform1A":
                case "subform1B":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150aSubForm1");
                    break 2;
                case "subform2":
                case "subform2A":
                case "subform2B":
                case "subform2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150aSubForm2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel("update");
                    break 1;
                case "insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel("insert");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["use_prg_schoolkind"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150A/knjf150aindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    } elseif ($sessionInstance->Properties["useSchool_KindField"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150A/knjf150aindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&SCHOOL_KIND=".SCHOOLKIND;
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150A/knjf150aindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    }
                    $args["right_src"] = "knjf150aindex.php?cmd=edit";
                    $args["cols"] = "22%,*";
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
$knjf150aCtl = new knjf150aController();
