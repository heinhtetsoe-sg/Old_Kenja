<?php

require_once('for_php7.php');

require_once('knjf160Model.inc');
require_once('knjf160Query.inc');

class knjf160Controller extends Controller {
    var $ModelClassName = "knjf160Model";
    var $ProgramID      = "KNJF160";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf160Form1");
                    break 2;
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf160SubForm1");
                    break 2;
                case "subform2":
                case "subform2A":
                case "subform2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf160SubForm2");
                    break 2;
                case "subform3":
                case "subform3A":
                case "subform3_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf160SubForm3");
                    break 2;
                case "subform4":
                case "subform4A":
                case "subform4_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf160SubForm4");
                    break 2;
                case "subform5":
                case "subform5A":
                case "subform5_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf160SubForm5");
                    break 2;
                case "subform6":
                case "subform6A":
                case "subform6_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf160SubForm6");
                    break 2;
                case "subform1_delete":
                case "subform2_delete":
                case "subform3_delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    break 1;
                case "subform1_insert":
                case "subform2_insert":
                case "subform3_insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "subform1_update":
                case "subform2_update":
                case "subform3_update":
                case "subform4_update":
                case "subform5_update":
                case "subform6_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "":
                    //分割フレーム作成
                    if ($sessionInstance->Properties["use_prg_schoolkind"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF160/knjf160index.php?cmd=edit") ."&button=1" ."&SES_FLG=2&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    } elseif ($sessionInstance->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF160/knjf160index.php?cmd=edit") ."&button=1" ."&SES_FLG=2&SCHOOL_KIND=".SCHOOLKIND;
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF160/knjf160index.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    }
                    $args["right_src"] = "knjf160index.php?cmd=edit";
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
$knjf160Ctl = new knjf160Controller;
?>
