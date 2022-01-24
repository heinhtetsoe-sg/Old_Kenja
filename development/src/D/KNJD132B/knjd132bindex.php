<?php

require_once('for_php7.php');
require_once('knjd132bModel.inc');
require_once('knjd132bQuery.inc');

class knjd132bController extends Controller {
    var $ModelClassName = "knjd132bModel";
    var $ProgramID      = "KNJD132B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "semester":
                case "updEdit":
                case "clear":
                    $this->callView("knjd132bForm1");
                    break 2;
                case "subform1": //部活動参照
                    $this->callView("knjd132bSubForm1");
                    break 2;
                case "subform2": //委員会参照
                    $this->callView("knjd132bSubForm2");
                    break 2;
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
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD132B/knjd132bindex.php?cmd=edit") ."&button=1" ."&SES_FLG=1" ."&SCHOOL_KIND=J";
                    $args["right_src"] = "knjd132bindex.php?cmd=edit";
                    $args["cols"] = "20%,80%";
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
$knjd132bCtl = new knjd132bController;
?>
