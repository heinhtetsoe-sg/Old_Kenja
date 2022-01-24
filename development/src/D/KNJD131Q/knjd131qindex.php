<?php

require_once('for_php7.php');
require_once('knjd131qModel.inc');
require_once('knjd131qQuery.inc');

class knjd131qController extends Controller {
    var $ModelClassName = "knjd131qModel";
    var $ProgramID      = "KNJD131Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd131qForm1");
                    break 2;
                case "subform1": //部活動参照
                    $this->callView("knjd131qSubForm1");
                    break 2;
                case "subform2": //委員会参照
                    $this->callView("knjd131qSubForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "replace":
                    $this->callView("knjd131qSubForm3");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD131Q/knjd131qindex.php?cmd=edit") ."&button=1"."&SES_FLG=2";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjd131qindex.php?cmd=edit";
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
$knjd131qCtl = new knjd131qController;
?>
