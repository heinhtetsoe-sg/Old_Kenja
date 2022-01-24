<?php

require_once('for_php7.php');

require_once('knjd428bModel.inc');
require_once('knjd428bQuery.inc');

class knjd428bController extends Controller {
    var $ModelClassName = "knjd428bModel";
    var $ProgramID      = "knjd428b";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list_set":
                case "sort":
                case "edit":
                case "set":
                case "clear":
                    $sessionInstance->knjd428bModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd428bForm1");
                    break 2;
                case "guidance_copy":
                case "all_copy":
                case "sub_copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd428bForm1", $sessionInstance->auth);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd428bForm1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd428bForm1", $sessionInstance->auth);
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD428B/knjd428bindex.php?cmd=edit") ."&button=1" ."&SEND_AUTH={$sessionInstance->auth}&HANDICAP_FLG=1";
                    $args["right_src"] = "knjd428bindex.php?cmd=edit";
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
$knjd428bCtl = new knjd428bController;
?>
