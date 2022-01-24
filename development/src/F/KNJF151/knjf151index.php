<?php

require_once('for_php7.php');

require_once('knjf151Model.inc');
require_once('knjf151Query.inc');

class knjf151Controller extends Controller {
    var $ModelClassName = "knjf151Model";
    var $ProgramID      = "KNJF151";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "new":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf151Form2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjf151Form1");
                    break 2;
                case "upload":      //PDFアップロード
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getUploadModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "download":    //PDFダウンロード
                    $sessionInstance->setAccessLogDetail("P", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf151Form1");
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
                    if ($sessionInstance->Properties["use_prg_schoolkind"] == "1") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF151/knjf151index.php?cmd=right") ."&button=1" ."&SES_FLG=2&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";
                    } elseif ($sessionInstance->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF151/knjf151index.php?cmd=right") ."&button=1" ."&SES_FLG=2&SCHOOL_KIND=".SCHOOLKIND;
                    } else {
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/F/KNJF151/knjf151index.php?cmd=right") ."&button=1" ."&SES_FLG=2";
                    }
                    $args["right_src"] = "knjf151index.php?cmd=right";
                    $args["cols"] = "30%,70%";
                    View::frame($args);
                    return;
                case "right":
                    $args["right_src"] = "knjf151index.php?cmd=right_list";
                    $args["edit_src"]  = "knjf151index.php?cmd=edit";
                    $args["rows"] = "40%,60%";
                    View::frame($args,"frame3.html");
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf151Ctl = new knjf151Controller;
?>
