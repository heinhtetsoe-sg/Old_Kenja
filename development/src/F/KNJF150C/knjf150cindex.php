<?php

require_once('for_php7.php');

require_once('knjf150cModel.inc');
require_once('knjf150cQuery.inc');

class knjf150cController extends Controller {
    var $ModelClassName = "knjf150cModel";
    var $ProgramID      = "KNJF150C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150cForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150cSubForm1");
                    break 2;
                case "subform2":
                case "subform2A":
                case "subform2B":
                case "subform2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150cSubForm2");
                    break 2;
                case "subform3":
                case "subform3A":
                case "subform3_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150cSubForm3");
                    break 2;
                case "subform4":
                case "subform4A":
                case "subform4_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150cSubForm4");
                    break 2;
                case "subform5":
                case "subform5A":
                case "subform5_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf150cSubForm5");
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
                case "execute":
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getFileExecModel();
                    break 1;
                case "delFile":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getFileDeleteModel();
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    if ($sessionInstance->sendPrgId == "KNJF150D") {
                        if ($sessionInstance->schregno != "") {
                            $loadFrame = "false";
                        } else {
                            $loadFrame = "true";
                        }
                        if ($sessionInstance->grade != "" && $sessionInstance->hr_class != "") {
                            $grhrcls = $sessionInstance->grade."-".$sessionInstance->hr_class;
                        } else {
                            $grhrcls = "";
                        }
                        //分割フレーム作成
                        if ($sessionInstance->Properties["use_prg_schoolkind"] == "1") {
                            $args["left_src"] = REQUESTROOT ."/X/KNJXEXP5/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150C/knjf150cindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}&LOADFRM={$loadFrame}&DIAGTYPE={$sessionInstance->type}&GRADE={$grhrcls}";
                        } elseif ($sessionInstance->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $args["left_src"] = REQUESTROOT ."/X/KNJXEXP5/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150C/knjf150cindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&SCHOOL_KIND=".SCHOOLKIND."&LOADFRM={$loadFrame}&DIAGTYPE={$sessionInstance->type}&GRADE={$grhrcls}";
                        } else {
                            $args["left_src"] = REQUESTROOT ."/X/KNJXEXP5/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150C/knjf150cindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&LOADFRM={$loadFrame}&DIAGTYPE={$sessionInstance->type}&GRADE={$grhrcls}";
                        }
                    } else {
                        //分割フレーム作成
                        if ($sessionInstance->Properties["use_prg_schoolkind"] == "1") {
                            $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150C/knjf150cindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                        } elseif ($sessionInstance->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150C/knjf150cindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2&SCHOOL_KIND=".SCHOOLKIND;
                        } else {
                            $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF150C/knjf150cindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                        }
                    }
                    $args["right_src"] = "knjf150cindex.php?cmd=edit";
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
$knjf150cCtl = new knjf150cController;
?>
