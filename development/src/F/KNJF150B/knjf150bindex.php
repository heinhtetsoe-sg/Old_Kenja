<?php

require_once('for_php7.php');

require_once('knjf150bModel.inc');
require_once('knjf150bQuery.inc');

class knjf150bController extends Controller {
    var $ModelClassName = "knjf150bModel";
    var $ProgramID      = "KNJF150B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "editA":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150bForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("form2A");
                    break 1;
                case "form2":
                case "form2A":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150bForm2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel("update");
                    break 1;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel("add");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $urlSchoolKind="";
                    //分割フレーム作成
                    if ($sessionInstance->Properties["use_prg_schoolkind"] == "1") {
                        $urlSchoolKind = "&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    } elseif ($sessionInstance->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $urlSchoolKind = "&SCHOOL_KIND=".SCHOOLKIND;
                    }
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150B/knjf150bindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2".$urlSchoolKind;
                    $args["right_src"] = "knjf150bindex.php?cmd=edit";
                    $args["edit_src"] = "knjf150bindex.php?cmd=form2";
                    $args["cols"] = "25%,75%";
                    $args["rows"] = "50%,50%";
                    View::frame($args, "frame2.html");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf150bCtl = new knjf150bController;
?>
