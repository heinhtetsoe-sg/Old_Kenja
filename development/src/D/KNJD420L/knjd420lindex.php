<?php

require_once('for_php7.php');

require_once('knjd420lModel.inc');
require_once('knjd420lQuery.inc');

class knjd420lController extends Controller {
    var $ModelClassName = "knjd420lModel";
    var $ProgramID      = "KNJD420L";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list_set":
                case "edit":
                case "clear":
                    $sessionInstance->knjd420lModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd420lForm1");
                    break 2;
                case "listdelete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420lForm1", $sessionInstance->auth);
                    $sessionInstance->getListDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "allcopy":
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420lForm1", $sessionInstance->auth);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420lForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420lForm1", $sessionInstance->auth);
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
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD420L/knjd420lindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                    $args["right_src"] = "knjd420lindex.php?cmd=edit";
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
$knjd420lCtl = new knjd420lController;
?>
