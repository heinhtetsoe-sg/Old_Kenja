<?php

require_once('for_php7.php');
require_once('knjf323Model.inc');
require_once('knjf323Query.inc');

class knjf323Controller extends Controller {
    var $ModelClassName = "knjf323Model";
    var $ProgramID      = "KNJF323";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf323Form1");
                    break 2;
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf323SubForm1");
                    break 2;
                case "subform2":
                case "subform2A":
                case "subform2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf323SubForm2");
                    break 2;
                case "subform3":
                case "subform3A":
                case "subform3_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf323SubForm3");
                    break 2;
                case "subform4":
                case "subform4A":
                case "subform4_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf323SubForm4");
                    break 2;
                case "subform6":
                case "subform6A":
                case "subform6_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjf323SubForm6");
                    break 2;
                case "subform1_update":
                case "subform2_update":
                case "subform3_update":
                case "subform4_update":
                case "subform6_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getSubformUpdateModel();
                    break 1;
                case "subform1_copy":
                case "subform2_copy":
                case "subform3_copy":
                case "subform4_copy":
                case "subform6_copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getSubformCopyModel();
                    break 1;
                case "update":
                case "update2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf323Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/F/KNJF323/knjf323index.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjf323index.php?cmd=edit";
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
$knjf323Ctl = new knjf323Controller;
?>
