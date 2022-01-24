<?php

require_once('for_php7.php');
require_once('knjd131iModel.inc');
require_once('knjd131iQuery.inc');

class knjd131iController extends Controller {
    var $ModelClassName = "knjd131iModel";
    var $ProgramID      = "KNJD131I";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "torikomi":
                case "edit":
                case "updEdit":
                case "clear":
                    $this->callView("knjd131iForm1");
                    break 2;
                case "subform1": //部活動参照
                    $this->callView("knjd131iSubForm1");
                    break 2;
                case "subform2": //委員会参照
                    $this->callView("knjd131iSubForm2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("change");
                    break 1;
                case "setupdate":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel2();
                    $sessionInstance->setCmd("change");
                    break 1;
                case "change":
                case "replace":
                    $this->callView("knjd131iSubForm3");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD131I/knjd131iindex.php?cmd=edit") ."&button=1"."&SES_FLG=2";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjd131iindex.php?cmd=edit";
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
$knjd131iCtl = new knjd131iController;
?>
