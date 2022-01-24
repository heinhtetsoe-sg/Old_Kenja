<?php

require_once('for_php7.php');
require_once('knjd139jModel.inc');
require_once('knjd139jQuery.inc');

class knjd139jController extends Controller {
    var $ModelClassName = "knjd139jModel";
    var $ProgramID      = "KNJD139J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd139jForm1");
                    break 2;
                case "subform1":    //部活動参照
                    $this->callView("knjd139jSubForm1");
                    break 2;
                case "subform2": //委員会参照
                    $this->callView("knjd139jSubForm2");
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
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD139J/knjd139jindex.php?cmd=edit") ."&button=1" ."&SES_FLG=2";
                    $args["right_src"] = "knjd139jindex.php?cmd=edit";
                    $args["cols"] = "25%,*";
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
$knjd139jCtl = new knjd139jController;
?>
