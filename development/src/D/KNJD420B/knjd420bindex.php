<?php

require_once('for_php7.php');

require_once('knjd420bModel.inc');
require_once('knjd420bQuery.inc');

class knjd420bController extends Controller {
    var $ModelClassName = "knjd420bModel";
    var $ProgramID      = "knjd420b";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list_set":
                case "sort":
                case "edit":
                case "set":
                case "clear":
                    $sessionInstance->knjd420bModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd420bForm1");
                    break 2;
                case "allcopy":
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420bForm1", $sessionInstance->auth);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420bForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd420bForm1", $sessionInstance->auth);
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD420B/knjd420bindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                    $args["right_src"] = "knjd420bindex.php?cmd=edit";
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
$knjd420bCtl = new knjd420bController;
?>
