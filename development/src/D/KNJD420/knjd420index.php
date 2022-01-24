<?php

require_once('for_php7.php');
require_once('knjd420Model.inc');
require_once('knjd420Query.inc');

class knjd420Controller extends Controller {
    var $ModelClassName = "knjd420Model";
    var $ProgramID      = "KNJD420";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list_set":
                case "edit":
                case "set":
                case "clear":
                    $sessionInstance->knjd420Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd420Form1");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
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
                    if ($sessionInstance->retprg != "") {
                        $this->callView("knjd420Form1");
                        break 2;
                    } else {
                        //分割フレーム作成
                        $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GHR/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD420/knjd420index.php?cmd=edit") ."&button=1" ."&SES_FLG=2&HANDICAP_FLG=1";
                        $args["right_src"] = "knjd420index.php?cmd=edit";
                        $args["cols"] = "25%,*";
                        View::frame($args);
                        exit;
                    }
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd420Ctl = new knjd420Controller;
?>
